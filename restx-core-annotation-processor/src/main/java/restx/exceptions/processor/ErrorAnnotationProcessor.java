package restx.exceptions.processor;

import com.github.mustachejava.Mustache;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.common.Mustaches;
import restx.exceptions.ErrorCode;
import restx.exceptions.ErrorField;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 10:20 PM
 */
@SupportedAnnotationTypes({
        "restx.exceptions.ErrorCode"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ErrorAnnotationProcessor extends AbstractProcessor {
    final Mustache errorDescriptorTpl;

    public ErrorAnnotationProcessor() {
        errorDescriptorTpl = Mustaches.compile(ErrorAnnotationProcessor.class, "ErrorDescriptor.mustache");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(ErrorCode.class)) {
            try {
                ErrorCode errorCode = elem.getAnnotation(ErrorCode.class);
                TypeElement typeElement = (TypeElement) elem;

                String fqcn = typeElement.getQualifiedName().toString();
                int i = CharMatcher.JAVA_UPPER_CASE.indexIn(fqcn);
                String pack = fqcn.substring(0, i - 1);
                String name = fqcn.substring(i).replace(".", "");
                String descriptor = name + "Descriptor";

                List<String> fields = Lists.newArrayList();
                for (Element element : typeElement.getEnclosedElements()) {
                    if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                        String field = element.getSimpleName().toString();
                        ErrorField errorField = element.getAnnotation(ErrorField.class);
                        String description = errorField == null ? field.replace("_", " ").toLowerCase(Locale.ENGLISH)
                                : errorField.value();
                        fields.add(String.format(".put(\"%s\", new ErrorDescriptor.ErrorFieldDescriptor(\"%s\", \"%s\"))",
                                field, field, description));
                    }
                }

                ImmutableMap<String, Object> ctx = ImmutableMap.<String, Object>builder()
                        .put("package", pack)
                        .put("descriptor", descriptor)
                        .put("errorStatus", String.valueOf(errorCode.status().getCode()))
                        .put("errorCode", errorCode.code())
                        .put("description", errorCode.description())
                        .put("fields", Joiner.on("\n").join(fields)).build();


                generateJavaClass(pack + "." + descriptor, errorDescriptorTpl, ctx, elem);
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "error when processing " + elem + ": " + e,
                        elem);
            }
        }
        return true;
    }

    private void generateJavaClass(String className, Mustache mustache, ImmutableMap<String, Object> ctx,
            Element originatingElements) throws IOException {
        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(className, originatingElements);
        try (Writer writer = fileObject.openWriter()) {
            mustache.execute(writer, ctx);
        }
    }


}
