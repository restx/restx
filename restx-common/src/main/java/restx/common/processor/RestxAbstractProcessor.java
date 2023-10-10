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
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
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
		if (typeMirror.getKind().isPrimitive()) {
			return TypeUtils.boxedClass((PrimitiveType) typeMirror);
		} else {
			return (TypeElement) TypeUtils.asElement(typeMirror);
		}
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

	/**
	 * Abstract class to manage resource file generation, it permits to read existing content using the
	 * {@link #processing()} method, which will call the abstract {@link #readContent(java.io.Reader)} method.
	 * And the method {@link #generate()} need to be called once we want to generate the resource, during this process
	 * the {@link #writeContent(java.io.Writer)} method will be called.
	 */
	protected abstract class ResourceDeclaration {
		private final String targetFilePath;
		private FileObject fileObject;

		/**
		 * @param targetFilePath path of the resource to write
		 */
		protected ResourceDeclaration(String targetFilePath) {
			this.targetFilePath = targetFilePath;
		}

		/**
		 * @return true if the resource need to be generated (for example, it permits to skip empty contents)
		 */
		protected abstract boolean requireGeneration();

		/**
		 * called once the file has been written
		 */
		protected abstract void clearContent();

		/**
		 * Writes the resource content.
		 *
		 * @param writer the writer to use
		 * @throws IOException if an I/O error occurs
		 */
		protected abstract void writeContent(Writer writer) throws IOException;

		/**
		 * Reads the resource content.
		 *
		 * @param reader the reader to use
		 * @throws IOException if an I/O error occurs
		 */
		protected abstract void readContent(Reader reader) throws IOException;

		public void generate() throws IOException {
			if (!requireGeneration()) {
				return;
			}

			writeResourceFile(targetFilePath);

			clearContent();

			fileObject = null;
		}

		public void processing() throws IOException {
			readExistingResourceIfExists(targetFilePath);
		}

		private void writeResourceFile(String targetFile) throws IOException {
			if (fileObject != null
					&& fileObject.getClass().getSimpleName().equals("EclipseFileObject")
					) {
				// eclipse does not allow to do a createResource for a fileobject already obtained via getResource
				// but the file object can be used for writing, so it's ok to reuse it in this case

				// see source code at:
				// https://github.com/eclipse/eclipse.jdt.core/blob/master/org.eclipse.jdt.compiler.apt/src/org/eclipse/jdt/internal/compiler/apt/dispatch/BatchProcessingEnvImpl.java
				// https://github.com/eclipse/eclipse.jdt.core/blob/master/org.eclipse.jdt.compiler.tool/src/org/eclipse/jdt/internal/compiler/tool/EclipseFileObject.java
			} else {
				try {
					fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", targetFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			try (Writer writer = fileObject.openWriter()) {
				writeContent(writer);
			}
		}

		private void readExistingResourceIfExists(String targetFile) throws IOException {
			try {
				if (fileObject == null) {
					fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", targetFile);
				}
				try (Reader r = fileObject.openReader(true)) {
					readContent(r);
				}
			} catch (FileNotFoundException ex) {
                /*
                   This is a very strange behaviour of javac during incremantal compilation (at least experienced
                   with Intellij make process): a FileNotFoundException is raised while the file actually exist.

                   "Fortunately" the exception message is the path of the file, so we can try to load it using
                   plain java.io
                 */
				try {
					File file = new File(ex.getMessage());
					if (file.exists()) {
						try (Reader r = new FileReader(file)) {
							readContent(r);
						} catch (IOException e) {
							// ignore
						}
					}
				} catch (Exception e) {
					// ignore
				}
			} catch (IOException | IllegalArgumentException ex) {
				// ignore
			}
		}
	}
}
