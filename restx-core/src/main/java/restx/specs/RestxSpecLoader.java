package restx.specs;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.common.MoreResources;
import restx.factory.Factory;
import restx.factory.NamedComponent;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static restx.common.MorePreconditions.checkInstanceOf;

/**
 * User: xavierhanin
 * Date: 3/30/13
 * Time: 6:19 PM
 */
@Component
public class RestxSpecLoader {
    private static final Logger logger = LoggerFactory.getLogger(RestxSpecLoader.class);

    private final Set<NamedComponent<GivenLoader>> givenLoaders;
    private final Set<NamedComponent<WhenHeaderLoader>> whenHeaderLoaders;
    private final String names;

    @Inject
    public RestxSpecLoader(Factory factory) {
        this(factory.queryByClass(GivenLoader.class).find(), factory.queryByClass(WhenHeaderLoader.class).find());
    }

    public RestxSpecLoader(Set<NamedComponent<GivenLoader>> givenLoaders,
                           Set<NamedComponent<WhenHeaderLoader>> whenHeaderLoaders) {
        this.givenLoaders = givenLoaders;
        this.whenHeaderLoaders = whenHeaderLoaders;
        List<String> names = Lists.newArrayList();
        for (NamedComponent<GivenLoader> givenLoader : givenLoaders) {
            names.add(givenLoader.getName().getName());
        }
        this.names = Joiner.on(", ").join(names);
    }

    public RestxSpec load(String resource) throws IOException {
        return load(resource, Resources.asCharSource(
                MoreResources.getResource(resource, true),
                Charsets.UTF_8));
    }

    public RestxSpec load(String path, CharSource charSource) throws IOException {
        Yaml yaml = new Yaml();
        Map spec = (Map) yaml.load(charSource.read());
        List<Given> givens = loadGivens(spec);
        List<WhenHttpRequest> whens = Lists.newArrayList();
        Iterable wts = checkInstanceOf("wts", spec.get("wts"), Iterable.class);
        for (Object wt : wts) {
            Map whenThen = checkInstanceOf("when/then", wt, Map.class);
            Object w = whenThen.get("when");
            if (w instanceof String) {
                WhenHttpRequest.Builder whenHttpBuilder = WhenHttpRequest.builder();

                String ws = ((String) w)
                        // Being consistent in loaded line ending in order to have platform-specific line ending
                        // Note that Yaml() always loads nodes in a consistent way, using \n everytime
                        // but we need to convert this to platform specific entries otherwise, spec files eol would have to
                        // be defined in git repo (through .gitattributes for instance) to work properly
                        .replaceAll("\n", System.lineSeparator());

                String definition;
                String body;

                int nlIndex = ws.indexOf(System.lineSeparator());
                if (nlIndex != -1) {
                    definition = ws.substring(0, nlIndex);
                    body = ws.substring(nlIndex + System.lineSeparator().length()).trim();


                    Optional<WhenHeaderLoader> whenHeader = resolveFromBody(body);
                    while (whenHeader.isPresent()) {
                        nlIndex = body.indexOf(System.lineSeparator());
                        String headerValue;
                        if (nlIndex == -1) {
                            headerValue = body.substring(whenHeader.get().detectionPattern().length(), body.length());
                            body = "";
                        } else {
                            headerValue = body.substring(whenHeader.get().detectionPattern().length(), nlIndex);
                            body = body.substring(nlIndex + System.lineSeparator().length()).trim();
                        }

                        whenHeader.get().loadHeader(headerValue, whenHttpBuilder);

                        whenHeader = resolveFromBody(body);
                    }
                } else {
                    definition = ws;
                    body = "";
                }

                Matcher methodAndPathMatcher = Pattern.compile("(GET|POST|PUT|DELETE|HEAD|OPTIONS) (.+)").matcher(definition);

                if (methodAndPathMatcher.matches()) {
                    String then = checkInstanceOf("then", whenThen.get("then"), String.class)
                            // Being consistent in loaded line ending in order to have platform-specific line ending
                            // Note that Yaml() always loads nodes in a consistent way, using \n everytime
                            // but we need to convert this to platform specific entries otherwise, spec files eol would have to
                            // be defined in git repo (through .gitattributes for instance) to work properly
                            .replaceAll("\n", System.lineSeparator())
                            .trim();
                    HttpStatus code = HttpStatus.OK;
                    int endLineIndex = then.indexOf(System.lineSeparator());
                    if (endLineIndex == -1) {
                        endLineIndex = then.length();
                    }
                    String firstLine = then.substring(0, endLineIndex);
                    Matcher respMatcher = Pattern.compile("^(\\d{3}).*$").matcher(firstLine);
                    if (respMatcher.matches()) {
                        code = HttpStatus.havingCode(Integer.parseInt(respMatcher.group(1)));
                        then = then.substring(endLineIndex).trim();
                    }

                    whens.add(whenHttpBuilder
                            .withMethod(methodAndPathMatcher.group(1))
                            .withPath(methodAndPathMatcher.group(2))
                            .withBody(body)
                            .withThen(new ThenHttpResponse(code.getCode(), then))
                            .build());
                } else {
                    throw new IllegalArgumentException("unrecognized 'when' format: it must begin with " +
                            "a HTTP declaration of the form 'VERB resource/path'\nEg: GET users/johndoe\n. Was: '" + ws + "'\n");
                }
            }
        }

        return new RestxSpec(path, checkInstanceOf("title", spec.get("title"), String.class),
                ImmutableList.copyOf(givens),
                ImmutableList.copyOf(whens));
    }

    private Optional<WhenHeaderLoader> resolveFromBody(String body) {
        for(NamedComponent<WhenHeaderLoader> whenHeaderLoader : whenHeaderLoaders){
            if(body.startsWith(whenHeaderLoader.getComponent().detectionPattern())){
                return Optional.of(whenHeaderLoader.getComponent());
            }
        }
        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    private List<Given> loadGivens(Map testCase) throws IOException {
        List<Given> givens = Lists.newArrayList();
        Iterable given = checkInstanceOf("given", testCase.get("given"), Iterable.class);
        for (Object g : given) {
            Map<String, ?> given1 = (Map<String, ?>) checkInstanceOf("given", g, Map.class);
            if (given1.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("can't load %s: a given has no properties at all", testCase));
            }
            String firstKey = checkInstanceOf("key", given1.keySet().iterator().next(), String.class);
            givens.add(findLoader(given1, firstKey).load(given1));
        }
        return givens;
    }

    private GivenLoader findLoader(Map given, String type) {
        for (NamedComponent<GivenLoader> givenLoader : givenLoaders) {
            if (givenLoader.getName().getName().equalsIgnoreCase(type)) {
                return givenLoader.getComponent();
            }
        }
        throw new IllegalArgumentException("invalid given " + given + ": unrecognized type " + type + "." +
                " Was expecting one of [" + names + "] as either first field or 'type' property");
    }

    public static interface GivenLoader {
        Given load(Map<String, ?> m);
    }

    public static interface WhenHeaderLoader {
        String detectionPattern();
        void loadHeader(String headerValue, WhenHttpRequest.Builder whenHttpRequestBuilder);
    }
}
