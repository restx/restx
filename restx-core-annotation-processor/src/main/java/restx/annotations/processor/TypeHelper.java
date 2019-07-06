package restx.annotations.processor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Date: 23/10/13
 * Time: 10:26
 */
public class TypeHelper {
    private static ImmutableList<String> PARSED_TYPES_DELIMITERS = ImmutableList.of(",", "<", ">");
    private static ImmutableMap<String, String> TYPE_DESCRIPTION_ALIASES = ImmutableMap.of(
            Integer.class.getCanonicalName(), "int",
            Iterable.class.getCanonicalName(), "LIST",
            List.class.getCanonicalName(), "LIST",
            Map.class.getCanonicalName(), "MAP");
    private static Set<String> RAW_TYPES_STR = Sets.newHashSet("byte", "short", "int", "long", "float", "double", "boolean", "char");

    private static class ParsedType {
        final String className;
        final ParsedType parentParsedType;
        final List<ParsedType> parameters;

        public ParsedType(String className, ParsedType parentParsedType) {
            this.className = className;
            this.parentParsedType = parentParsedType;
            this.parameters = new ArrayList<>();
        }
    }

    static String getTypeExpressionFor(ParsedType currentParsedType) {
        if(currentParsedType.parameters.isEmpty()) { // We're on a raw type
            return String.format("%s.class", currentParsedType.className);
        } else { // We're on a parameterized type
            return String.format("Types.newParameterizedType(%s.class, %s)",
                    currentParsedType.className,
                    FluentIterable.from(currentParsedType.parameters)
                        .transform(new Function<ParsedType, String>() {
                            @Override
                            public String apply(ParsedType param) {
                                return getTypeExpressionFor(param);
                            }
                        }).join(Joiner.on(", "))
                    );
        }
    }

    static String toTypeDescription(ParsedType parsedType) {
        boolean aliasedType = TYPE_DESCRIPTION_ALIASES.containsKey(parsedType.className);
        String type;
        if(aliasedType) {
            type = TYPE_DESCRIPTION_ALIASES.get(parsedType.className);
        } else {
            boolean primitive = parsedType.className.startsWith("java.lang");
            type =  parsedType.className.substring(parsedType.className.lastIndexOf('.') + 1);
            if (primitive) {
                type = type.toLowerCase();
            }
            if ("DateTime".equals(type) || "DateMidnight".equals(type)) {
                type = "Date";
            }
        }

        if(parsedType.parameters.isEmpty()) { // We're on a raw type
            return type;
        } else { // We're on a parameterized type
            String parametersTypeDescriptions = FluentIterable.from(parsedType.parameters)
                    .transform(new Function<ParsedType, String>() {
                        @Override
                        public String apply(ParsedType param) {
                            return toTypeDescription(param);
                        }
                    }).join(Joiner.on(", "));

            if(aliasedType) {
                return String.format("%s[%s]", type, parametersTypeDescriptions);
            } else {
                return String.format("%s<%s>", type, parametersTypeDescriptions);
            }
        }
    }

    static ParsedType parseParameterizedType(String parameterizedType) {
        StringTokenizer tokenizer = new StringTokenizer(parameterizedType+"\n", Joiner.on("").join(PARSED_TYPES_DELIMITERS), true);
        String[] tokens = new String[tokenizer.countTokens()];
        ParsedType rootParsedType = null;
        ParsedType currentParsedType = null;
        for(int i=0; i < tokens.length; i++) {
            String token = tokenizer.nextToken().trim();
            tokens[i] = token;

            if(i == 0) {
                rootParsedType = new ParsedType(token, null);
                currentParsedType = rootParsedType;
            } else if(PARSED_TYPES_DELIMITERS.contains(token)) {
                // Do nothing
            } else {
                int delimiterIndex = i-1;
                Stack<String> delimiters = new Stack<>();
                while(PARSED_TYPES_DELIMITERS.contains(tokens[delimiterIndex])) {
                    delimiters.push(tokens[delimiterIndex]);
                    delimiterIndex--;
                }

                while(!delimiters.empty()) {
                    String delimiter = delimiters.pop();
                    if(",".equals(delimiter)) {
                        // Adding current parsed type to its parent
                        currentParsedType.parentParsedType.parameters.add(currentParsedType);
                        currentParsedType = new ParsedType(token, currentParsedType.parentParsedType);
                    } else if("<".equals(delimiter)) {
                        currentParsedType = new ParsedType(token, currentParsedType);
                    } else if(">".equals(delimiter)) {
                        currentParsedType.parentParsedType.parameters.add(currentParsedType);
                        currentParsedType = currentParsedType.parentParsedType;
                    }
                }
            }
        }
        return rootParsedType;
    }

    static String toTypeDescription(String type) {
        return toTypeDescription(parseParameterizedType(type));
    }

    static String getTypeExpressionFor(String type) {
        return getTypeExpressionFor(parseParameterizedType(type));
    }

    static boolean isParameterizedType(String type) {
        return type.contains("<");
    }

    static String rawTypeFrom(String type) {
        return isParameterizedType(type)?type.substring(0, type.indexOf("<")):type;
    }

    static String getTypeReferenceExpressionFor(String type) {
        return RAW_TYPES_STR.contains(type) ? type + ".class" : "new TypeReference<" + type + ">(){}";
    }
}
