package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import restx.endpoint.Endpoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 7:55 AM
 */
public class StdRestxRequestMatcher implements RestxRequestMatcher {
    private final Endpoint endpoint;
    private final String stdPathPattern;

    private final Pattern pattern;
    private final ImmutableList<String> groupNames;

    public StdRestxRequestMatcher(Endpoint endpoint) {
        this.endpoint = endpoint;

        PathPatternParser s = new PathPatternParser(endpoint.getPathPattern());
        s.parse();

        pattern = Pattern.compile(s.patternBuilder.toString());
        stdPathPattern = s.stdPathPatternBuilder.toString();
        groupNames = s.groupNamesBuilder.build();
    }

    public StdRestxRequestMatcher(String method, String pathPattern) {
        this(new Endpoint(method, pathPattern));
    }

    @Override
    public Optional<? extends RestxRequestMatch> match(String method, String path) {
        if (!this.endpoint.getMethod().equals(method)) {
            return Optional.absent();
        }
        Matcher m = pattern.matcher(path);
        if (!m.matches()) {
            return Optional.absent();
        }

        ImmutableMap.Builder<String, String> params = ImmutableMap.builder();
        for (int i = 0; i < m.groupCount() && i < groupNames.size(); i++) {
             params.put(groupNames.get(i), m.group(i + 1));
        }

        return Optional.of(new StdRestxRequestMatch(this.endpoint.getPathPattern(), path, params.build()));
    }

    @Override
    public String toString() {
        return endpoint.toString();
    }

    public String getMethod() {
        return endpoint.getMethod();
    }

    public String getPathPattern() {
        return endpoint.getPathPattern();
    }

    public String getStdPathPattern() {
        return stdPathPattern;
    }

    public ImmutableList<String> getPathParamNames() {
        return groupNames;
    }

    // here comes the path pattern parsing logic
    // the code is pretty ugly with lot of cross dependencies, I tried to keep it performant, correct, and maintainable
    // not sure those goals are all achieved though

    private static final class PathPatternParser {
        final int length;
        final String pathPattern;
        int offset = 0;
        PathParserCharProcessor processor = regularCharPathParserCharProcessor;
        ImmutableList.Builder<String> groupNamesBuilder = ImmutableList.builder();
        StringBuilder patternBuilder = new StringBuilder();
        StringBuilder stdPathPatternBuilder = new StringBuilder();

        private PathPatternParser(String pathPattern) {
            this.length = pathPattern.length();
            this.pathPattern = pathPattern;
        }

        void parse() {
            while (offset < length) {
                int curChar = pathPattern.codePointAt(offset);

                processor.handle(curChar, this);

                offset += Character.charCount(curChar);
            }
            processor.end(this);
        }
    }

    private static interface PathParserCharProcessor {
        void handle(int curChar, PathPatternParser pathPatternParser);

        void end(PathPatternParser pathPatternParser);
    }

    private static final class CurlyBracesPathParamPathParserCharProcessor implements PathParserCharProcessor {
        private int openBr = 1;
        private boolean inRegexDef;
        private StringBuilder pathParamName = new StringBuilder();
        private StringBuilder pathParamRegex = new StringBuilder();

        @Override
        public void handle(int curChar, PathPatternParser pathPatternParser) {
            if (curChar == '}') {
                openBr--;
                if (openBr == 0) {
                    // found matching brace, end of path param

                    if (pathParamName.length() == 0) {
                        // it was a mere {}, can't be interpreted as a path param
                        pathPatternParser.processor = regularCharPathParserCharProcessor;
                        pathPatternParser.patternBuilder.append("{}");
                        pathPatternParser.stdPathPatternBuilder.append("{}");
                        return;
                    }

                    if (pathParamRegex.length() == 1) {
                        // only the opening paren
                        throw new IllegalArgumentException(String.format(
                                "illegal path parameter definition '%s' at offset %d - custom regex must not be empty",
                                pathPatternParser.pathPattern, pathPatternParser.offset));
                    }

                    if (pathParamRegex.length() == 0) {
                        // use default regex
                        pathParamRegex.append("([^\\/]+)");
                    } else {
                        // close paren for matching group
                        pathParamRegex.append(")");
                    }

                    pathPatternParser.processor = regularCharPathParserCharProcessor;
                    pathPatternParser.patternBuilder.append(pathParamRegex);
                    pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
                    pathPatternParser.groupNamesBuilder.add(pathParamName.toString());
                    return;
                }
            } else if (curChar == '{') {
                openBr++;
            }

            if (inRegexDef) {
                pathParamRegex.appendCodePoint(curChar);
            } else {
                if (curChar == ':') {
                    // we were in path name, the column marks the separator with the regex definition, we go in regex mode
                    inRegexDef = true;
                    pathParamRegex.append("(");
                } else {
                    if (!Character.isLetterOrDigit(curChar)) {
                        //only letters are authorized in path param name
                        throw new IllegalArgumentException(String.format(
                                "illegal path parameter definition '%s' at offset %d" +
                                        " - only letters and digits are authorized in path param name",
                                pathPatternParser.pathPattern, pathPatternParser.offset));
                    } else {
                        pathParamName.appendCodePoint(curChar);
                    }
                }
            }
        }

        @Override
        public void end(PathPatternParser pathPatternParser) {
        }
    };

    private static final class SimpleColumnBasedPathParamParserCharProcessor implements PathParserCharProcessor {
        private StringBuilder pathParamName = new StringBuilder();
        @Override
        public void handle(int curChar, PathPatternParser pathPatternParser) {
            if (!Character.isLetterOrDigit(curChar)) {
                pathPatternParser.patternBuilder.append("([^\\/]+)");
                pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
                pathPatternParser.groupNamesBuilder.add(pathParamName.toString());
                pathPatternParser.processor = regularCharPathParserCharProcessor;
                pathPatternParser.processor.handle(curChar, pathPatternParser);
            } else {
                pathParamName.appendCodePoint(curChar);
            }
        }

        @Override
        public void end(PathPatternParser pathPatternParser) {
            pathPatternParser.patternBuilder.append("([^\\/]+)");
            pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
            pathPatternParser.groupNamesBuilder.add(pathParamName.toString());
        }
    };

    private static final PathParserCharProcessor regularCharPathParserCharProcessor = new PathParserCharProcessor() {
        @Override
        public void handle(int curChar, PathPatternParser pathPatternParser) {
            if (curChar == '{') {
                pathPatternParser.processor = new CurlyBracesPathParamPathParserCharProcessor();
            } else if (curChar == ':') {
                pathPatternParser.processor = new SimpleColumnBasedPathParamParserCharProcessor();
            } else {
                pathPatternParser.patternBuilder.appendCodePoint(curChar);
                pathPatternParser.stdPathPatternBuilder.appendCodePoint(curChar);
            }
        }

        @Override
        public void end(PathPatternParser pathPatternParser) {
        }
    };
}
