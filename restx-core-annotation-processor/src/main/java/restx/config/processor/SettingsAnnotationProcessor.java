package restx.config.processor;

import com.github.mustachejava.Mustache;
import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.common.Mustaches;
import restx.config.Settings;
import restx.config.SettingsKey;
import restx.exceptions.ErrorCode;
import restx.exceptions.ErrorField;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SettingsAnnotationProcessor extends AbstractProcessor {
    final Mustache settingsProviderTpl;
    final Mustache settingsConfigTpl;

    public SettingsAnnotationProcessor() {
        settingsProviderTpl = Mustaches.compile(SettingsAnnotationProcessor.class, "SettingsProvider.mustache");
        settingsConfigTpl = Mustaches.compile(SettingsAnnotationProcessor.class, "SettingsConfig.mustache");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(Settings.class)) {
            try {
                TypeElement typeElement = (TypeElement) elem;
                if (!typeElement.getKind().isInterface()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
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
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    "invalid settings accessor method - it must not take any parameter", methodElem);
                            continue;
                        }

                        String accessorReturnType = methodElem.getReturnType().toString();
                        boolean optional;
                        String targetType;
                        if (accessorReturnType.startsWith(Optional.class.getCanonicalName())) {
                            optional = true;
                            List<? extends TypeMirror> typeArguments = ((DeclaredType) methodElem.getReturnType()).getTypeArguments();
                            if (typeArguments.isEmpty()) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        String.format("unsupported return type %s for settings accessor method" +
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
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        String.format("unsupported return type %s for settings accessor method" +
                                                " - it must be one of [String, Integer, int]",
                                                accessorReturnType), methodElem);
                                continue;
                        }

                        SettingsKey settingsKey = element.getAnnotation(SettingsKey.class);

                        if (settingsKey == null) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    String.format(
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
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "error when processing " + elem + ": " + e,
                        elem);
            }
        }
        return true;
    }


    private PackageElement getPackage(TypeElement typeElement) {
        Element el = typeElement.getEnclosingElement();
        while (el != null) {
            if (el instanceof PackageElement) {
                return (PackageElement) el;
            }
            el = el.getEnclosingElement();
        }
        throw new IllegalStateException("no package for " + typeElement);
    }

    private void generateJavaClass(String className, Mustache mustache, ImmutableMap<String, Object> ctx,
            Element originatingElements) throws IOException {
        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(className, originatingElements);
        try (Writer writer = fileObject.openWriter()) {
            mustache.execute(writer, ctx);
        }
    }


}
