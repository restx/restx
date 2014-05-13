package restx.apidocs.doclet;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.standard.Standard;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;

/**
 * ApidocsDoclet.
 *
 * Extracts javadoc of RESTX endpoints to provide them in API DOCS.
 */
public class ApidocsDoclet extends Doclet {
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
        debug("running in " + new File("current").getAbsolutePath());

        for (ClassDoc classDoc : rootDoc.classes()) {
            for (MethodDoc methodDoc : classDoc.methods()) {
                for (AnnotationDesc annotationDesc : methodDoc.annotations()) {
                    if (annotationDesc.annotationType().qualifiedName().startsWith("restx.annotations.")) {
                        debug(classDoc.name() + " > " + methodDoc.qualifiedName() + " > " + annotationDesc.annotationType().name());
                        debug(asList(annotationDesc.elementValues()).toString());
                        debug(methodDoc.commentText());

                        for (ParamTag paramTag : methodDoc.paramTags()) {
                            debug("\t" + paramTag.parameterName() + " > " + paramTag.parameterComment());
                        }

                        for (Tag aReturn : methodDoc.tags("return")) {
                            debug("\t" + aReturn.name() + " > " + aReturn.text());
                        }
                    }
                }
            }
        }

        return Standard.start(rootDoc);
    }

    protected static void debug(String msg) {
        try {
            Files.append(msg + "\n", new File("apidoclet.trace"), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     *
     * _Javadoc spec requirement._
     *
     * @param option input option
     * @return length of required parameters
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int optionLength(String option) {
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
}
