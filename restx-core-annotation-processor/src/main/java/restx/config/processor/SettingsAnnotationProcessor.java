package restx.config.processor;

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.samskivert.mustache.Template;
import restx.common.Mustaches;
import restx.common.processor.RestxAbstractProcessor;
import restx.config.Settings;
import restx.config.SettingsKey;
import restx.exceptions.ErrorCode;
import restx.exceptions.ErrorField;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 */
@SupportedAnnotationTypes({
        "restx.config.Settings"
})
@SupportedOptions({ "debug" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SettingsAnnotationProcessor extends RestxAbstractProcessor {
    final Template settingsProviderTpl;
    final Template settingsConfigTpl;

    public SettingsAnnotationProcessor() {
        settingsProviderTpl = Mustaches.compile(SettingsAnnotationProcessor.class, "SettingsProvider.mustache");
        settingsConfigTpl = Mustaches.compile(SettingsAnnotationProcessor.class, "SettingsConfig.mustache");
    }

    @Override
    protected boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception {
        for (Element elem : roundEnv.getElementsAnnotatedWith(Settings.class)) {
            try {
                TypeElement typeElement = (TypeElement) elem;
                if (!typeElement.getKind().isInterface()) {
                    error(
                            String.format("only an interface can be annotated with @Settings - %s",
                                    typeElement.getSimpleName()), typeElement);
                    continue;
                }

                String fqcn = typeElement.getQualifiedName().toString();
                String pack = getPackage(typeElement).getQualifiedName().toString();
                String settingsSimpleType = typeElement.getSimpleName().toString();

                boolean shouldGenerateProvider = false;
                List<ImmutableMap<String,Object>> keys = new ArrayList<>();

                for (Element element : typeElement.getEnclosedElements()) {
                    if (element.getKind() == ElementKind.METHOD) {
                        ExecutableElement methodElem = (ExecutableElement) element;
                        if (!methodElem.getParameters().isEmpty()) {
                            error("invalid settings accessor method - it must not take any parameter", methodElem);
                            continue;
                        }

                        String accessorReturnType = methodElem.getReturnType().toString();
                        boolean optional;
                        String targetType;
                        if (accessorReturnType.startsWith(Optional.class.getCanonicalName())) {
                            optional = true;
                            List<? extends TypeMirror> typeArguments = ((DeclaredType) methodElem.getReturnType()).getTypeArguments();
                            if (typeArguments.isEmpty()) {
                                error(String.format("unsupported return type %s for settings accessor method" +
                                                " - you must provide generic type when using Optional",
                                                accessorReturnType), methodElem);
                                continue;
                            } else {
                                targetType = typeArguments.get(0).toString();
                            }
                        } else {
                            optional = false;
                            targetType = accessorReturnType;
                        }
                        String configAccessor;
                        switch (targetType) {
                            case "java.lang.String":
                                configAccessor = "getString";
                                break;
                            case "java.lang.Integer":
                            case "int":
                                configAccessor = "getInt";
                                break;
                            default:
                                error(String.format("unsupported return type %s for settings accessor method" +
                                                " - it must be one of [String, Integer, int]",
                                                accessorReturnType), methodElem);
                                continue;
                        }

                        SettingsKey settingsKey = element.getAnnotation(SettingsKey.class);

                        if (settingsKey == null) {
                            error(String.format(
                                    "all methods in a Settings interface must be annotated with @SettingsKey - %s",
                                            element.getSimpleName()), element);
                            continue;
                        }
                        shouldGenerateProvider |= !Strings.isNullOrEmpty(settingsKey.defaultValue())
                                                    || !Strings.isNullOrEmpty(settingsKey.doc());

                        keys.add(ImmutableMap.<String, Object>builder()
                                .put("accessorReturnType", accessorReturnType)
                                .put("configAccessor", configAccessor)
                                .put("accessorName", methodElem.getSimpleName().toString())
                                .put("key", settingsKey.key())
                                .put("get", optional ? "" : ".get()")
                                .put("doc", settingsKey.doc())
                                .put("defaultValue", settingsKey.defaultValue())
                                .build());
                    }
                }

                ImmutableMap<String, Object> ctx = ImmutableMap.<String, Object>builder()
                        .put("package", pack)
                        .put("settingsSimpleType", settingsSimpleType)
                        .put("settingsType", fqcn)
                        .put("keys", keys).build();

                generateJavaClass(pack + "." + settingsSimpleType + "Config", settingsConfigTpl, ctx, elem);
                if (shouldGenerateProvider) {
                    generateJavaClass(pack + "." + settingsSimpleType + "Provider", settingsProviderTpl, ctx, elem);
                }
            } catch (Exception e) {
                fatalError("error when processing " + elem, e, elem);
            }
        }
        return true;
    }
}
