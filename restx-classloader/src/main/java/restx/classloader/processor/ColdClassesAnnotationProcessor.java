package restx.classloader.processor;

import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import restx.classloader.Cold;
import restx.classloader.ColdClasses;
import restx.common.processor.RestxAbstractProcessor;

/**
 * Annotation processor for the cold classes.
 *
 * @author apeyrard
 */
@SupportedAnnotationTypes({
		"restx.classloader.Cold",
})
@SupportedOptions({ "debug" })
public class ColdClassesAnnotationProcessor extends RestxAbstractProcessor {
	private final ColdClassesAnnotationProcessor.ColdClassesDeclaration coldClassesDeclaration;

	public ColdClassesAnnotationProcessor() {
		this.coldClassesDeclaration = new ColdClassesDeclaration();
	}

	@Override
	protected boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception {
		coldClassesDeclaration.processing();
		if (roundEnv.processingOver()) {
			coldClassesDeclaration.generate();
		} else {
			processColdClasses(roundEnv);
		}

		return true;
	}

	private void processColdClasses(RoundEnvironment roundEnv) {
		for (Element annotation : roundEnv.getElementsAnnotatedWith(Cold.class)) {
			if (!(annotation instanceof TypeElement)) {
				error("annotating element " + annotation + " of type " + annotation.getKind().name()
						+ " with @Cold is not supported", annotation);
				continue;
			}
			TypeElement typeElem = (TypeElement) annotation;
			coldClassesDeclaration.declareColdClass(typeElem.getQualifiedName().toString());
		}
	}

	private class ColdClassesDeclaration extends ResourceDeclaration {
		private final Set<String> coldClasses = Sets.newHashSet();

		protected ColdClassesDeclaration() {
			super(ColdClasses.COLD_CLASSES_FILE_PATH);
		}

		void declareColdClass(String coldClass) {
			coldClasses.add(coldClass);
		}

		@Override
		protected boolean requireGeneration() {
			return coldClasses.size() > 0;
		}

		@Override
		protected void clearContent() {
			coldClasses.clear();
		}

		@Override
		protected void writeContent(Writer writer) throws IOException {
			writer.write(Joiner.on('\n').join(Ordering.natural().sortedCopy(coldClasses)));
			writer.write('\n');
		}

		@Override
		protected void readContent(Reader reader) throws IOException {
			coldClasses.addAll(CharStreams.readLines(reader));
		}
	}
}
