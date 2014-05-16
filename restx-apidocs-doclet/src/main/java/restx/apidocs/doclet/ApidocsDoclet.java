package restx.apidocs.doclet;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.standard.Standard;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

/**
 * ApidocsDoclet.
 *
 * Extracts javadoc of RESTX endpoints to provide them in API DOCS.
 */
public class ApidocsDoclet extends Doclet {
    static enum Options {
        DISABLE_STANDARD_DOCLET("-disable-standard-doclet", 1),
        TARGET_DIR("-restx-target-dir", 2),
        ENABLE_TRACE("-restx-enable-trace", 1);

        private final String optionName;
        private final int optionLength;

        Options(String name, int optionLength) {
            this.optionName = name;
            this.optionLength = optionLength;
        }

        public String getOptionName() {
            return optionName;
        }

        public int getOptionLength() {
            return optionLength;
        }

        public boolean isSet(String[][] options) {
            for (String[] option : options) {
                if (options.length > 0 && optionName.equals(option[0])) {
                    return true;
                }
            }

            return false;
        }

        public Optional<String> getOption(String[][] options) {
            for (String[] option : options) {
                if (options.length > 1 && optionName.equals(option[0])) {
                    return Optional.of(option[1]);
                }
            }
            return Optional.absent();
        }
    }


    /**
     * The starting point of Javadoc render.
     *
     * _Javadoc spec requirement._
     *
     * @param rootDoc input class documents
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean start(RootDoc rootDoc) {
        Path targetDir = Paths.get(Options.TARGET_DIR.getOption(rootDoc.options()).or(""));

        Trace trace = Options.ENABLE_TRACE.isSet(rootDoc.options()) ?
                new FileTrace(targetDir.resolve("apidoclet.trace").toFile()) :
                new NoTrace()
                ;

        trace.trace("RESTX APIDOCLET " + DateTime.now());
        trace.trace("target dir : " + targetDir.toAbsolutePath().toString());
        trace.trace("current dir: " + Paths.get("").toAbsolutePath().toString());

        for (ClassDoc classDoc : rootDoc.classes()) {
            for (MethodDoc methodDoc : classDoc.methods()) {
                for (AnnotationDesc annotationDesc : methodDoc.annotations()) {
                    if (annotationDesc.annotationType().qualifiedName().startsWith("restx.annotations.")) {
                        trace.trace(classDoc.name() + " > " + methodDoc.qualifiedName() + " > " + annotationDesc.annotationType().name());
                        trace.trace(asList(annotationDesc.elementValues()).toString());
                        trace.trace(methodDoc.commentText());

                        for (ParamTag paramTag : methodDoc.paramTags()) {
                            trace.trace("\t" + paramTag.parameterName() + " > " + paramTag.parameterComment());
                        }

                        for (Tag aReturn : methodDoc.tags("return")) {
                            trace.trace("\t" + aReturn.name() + " > " + aReturn.text());
                        }
                    }
                }
            }
        }

        if (Options.DISABLE_STANDARD_DOCLET.isSet(rootDoc.options())) {
            return true;
        }

        return Standard.start(rootDoc);
    }

    /**
     * Sets the language version to Java 5.
     *
     * _Javadoc spec requirement._
     *
     * @return language version number
     */
    @SuppressWarnings("UnusedDeclaration")
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    /**
     * Sets the option length to the standard Javadoc option length.
     * <p/>
     * _Javadoc spec requirement._
     *
     * @param option input option
     * @return length of required parameters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int optionLength(String option) {
        for (Options opt : Options.values()) {
            if (opt.getOptionName().equalsIgnoreCase(option)) {
                return opt.getOptionLength();
            }
        }

        return Standard.optionLength(option);
    }

    /**
     * Processes the input options by delegating to the standard handler.
     *
     * _Javadoc spec requirement._
     *
     * @param options input option array
     * @param errorReporter error handling
     * @return success
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        return Standard.validOptions(options, errorReporter);
    }




    private static interface Trace {
        public void trace(String msg);
    }

    private static class FileTrace implements Trace {
        private final File traceFile;

        private FileTrace(File traceFile) {
            this.traceFile = traceFile;
        }

        @Override
        public void trace(String msg) {
            try {
                Files.append(msg + "\n", traceFile, Charsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class NoTrace implements Trace {
        @Override
        public void trace(String msg) {
        }
    }
}
