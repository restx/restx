package restx.common.processor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.samskivert.mustache.Template;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

/**
 * Date: 1/3/14
 * Time: 07:50
 */
public abstract class RestxAbstractProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return processImpl(annotations, roundEnv);
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            fatalError("", e);
            return true;
        }
    }

    protected abstract boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception;


    protected void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    protected void error(String msg, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
    }

    protected void warn(String msg, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, element);
    }

    protected void error(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    protected void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }

    protected void fatalError(String msg, Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg + " " + writer);
    }

    protected void fatalError(String msg, Exception e, Element element) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg + " " + writer, element);
    }

    protected PackageElement getPackage(TypeElement typeElement) {
        Element el = typeElement.getEnclosingElement();
        while (el != null) {
            if (el instanceof PackageElement) {
                return (PackageElement) el;
            }
            el = el.getEnclosingElement();
        }
        throw new IllegalStateException("no package for " + typeElement);
    }

    protected TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement)TypeUtils.asElement(typeMirror);
    }


    protected void generateJavaClass(String className, Template mustache, ImmutableMap<String, Object> ctx,
                                     Element originatingElements) throws IOException {
        generateJavaClass(className, mustache, ctx, ImmutableSet.of(originatingElements));
    }

    protected void generateJavaClass(String className, Template mustache, ImmutableMap<String, ? extends Object> ctx,
                                     Set<Element> originatingElements) throws IOException {
        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(className,
                Iterables.toArray(originatingElements, Element.class));
        try (Writer writer = fileObject.openWriter()) {
            mustache.execute(ctx, writer);
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
