package restx.specs;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.yaml.snakeyaml.Yaml;
import restx.factory.Factory;
import restx.factory.NamedComponent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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
public class RestxSpecLoader {
    private static final String COOKIE = "Cookie:";

    private final Set<NamedComponent<GivenLoader>> givenLoaders;
    private final String names;

    public RestxSpecLoader() {
        this(Factory.builder()
                .addLocalMachines(Factory.LocalMachines.threadLocal())
                .addLocalMachines(Factory.LocalMachines.contextLocal(RestxSpec.class.getSimpleName()))
                .addLocalMachines(Factory.LocalMachines.contextLocal(RestxSpecLoader.class.getSimpleName()))
                .addFromServiceLoader()
                .build());
    }

    public RestxSpecLoader(Factory factory) {
        this(factory.queryByClass(GivenLoader.class).find());
    }

    public RestxSpecLoader(Set<NamedComponent<GivenLoader>> givenLoaders) {
        this.givenLoaders = givenLoaders;
        List<String> names = Lists.newArrayList();
        for (NamedComponent<GivenLoader> givenLoader : givenLoaders) {
            names.add(givenLoader.getName().getName());
        }
        this.names = Joiner.on(", ").join(names);
    }

    public RestxSpec load(String resource) throws IOException {
        return load(Resources.newReaderSupplier(
                Resources.getResource(resource),
                Charset.forName("UTF-8")));
    }

    public RestxSpec load(InputSupplier<InputStreamReader> inputSupplier) throws IOException {
        Yaml yaml = new Yaml();
        Map spec = (Map) yaml.load(inputSupplier.getInput());
        List<RestxSpec.Given> givens = loadGivens(spec);
        List<RestxSpec.When> whens = Lists.newArrayList();
        Iterable wts = checkInstanceOf("wts", spec.get("wts"), Iterable.class);
        for (Object wt : wts) {
            Map whenThen = checkInstanceOf("when/then", wt, Map.class);
            Object w = whenThen.get("when");
            if (w instanceof String) {
                String ws = (String) w;
                String definition;
                String body;
                Map<String, String> cookies = Maps.newLinkedHashMap();

                int nlIndex = ws.indexOf("\n");
                if (nlIndex != -1) {
                    definition = ws.substring(0, nlIndex);
                    body = ws.substring(nlIndex + 1).trim();

                    while (body.startsWith(COOKIE)) {
                        nlIndex = body.indexOf("\n");
                        String cookieValues;
                        if (nlIndex == -1) {
                            cookieValues = body.substring(COOKIE.length(), body.length());
                            body = "";
                        } else {
                            cookieValues = body.substring(COOKIE.length(), nlIndex);
                            body = body.substring(nlIndex + 1).trim();
                        }

                        for (String s : Splitter.on(";").trimResults().split(cookieValues)) {
                            int i = s.indexOf('=');

                            String name = s.substring(0, i);
                            String value = s.substring(i + 1);
                            cookies.put(name, value);
                        }

                    }
                } else {
                    definition = ws;
                    body = "";
                }

                Matcher matcher = Pattern.compile("(GET|POST|PUT|DELETE|HEAD|OPTIONS) (.+)").matcher(definition);

                if (matcher.matches()) {
                    String method = matcher.group(1);
                    String path = matcher.group(2);
                    String then = checkInstanceOf("then", whenThen.get("then"), String.class).trim();
                    int code = 200;
                    int endLineIndex = then.indexOf("\n");
                    String firstLine = endLineIndex > 0 ? then.substring(0, endLineIndex) : "";
                    Matcher respMatcher = Pattern.compile("^(\\d{3}).*$").matcher(firstLine);
                    if (respMatcher.matches()) {
                        code = Integer.parseInt(respMatcher.group(1));
                        then = then.substring(endLineIndex).trim();
                    }
                    whens.add(new RestxSpec.WhenHttpRequest(method, path, ImmutableMap.copyOf(cookies), body, new RestxSpec.ThenHttpResponse(code, then)));
                } else {
                    throw new IllegalArgumentException("unrecognized 'when' format: it must begin with " +
                            "a HTTP declaration of the form 'VERB resource/path'\nEg: GET users/johndoe\n. Was: '" + ws + "'\n");
                }
            }
        }

        return new RestxSpec(checkInstanceOf("title", spec.get("title"), String.class),
                ImmutableList.copyOf(givens),
                ImmutableList.copyOf(whens));
    }

    private List<RestxSpec.Given> loadGivens(Map testCase) throws IOException {
        List<RestxSpec.Given> givens = Lists.newArrayList();
        Iterable given = checkInstanceOf("given", testCase.get("given"), Iterable.class);
        for (Object g : given) {
            Map given1 = checkInstanceOf("given", g, Map.class);
            if (given1.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("can't load {}: a given has no properties at all", testCase));
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
        RestxSpec.Given load(Map m);
    }
}
