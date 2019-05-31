package restx.annotations.processor;

import java.util.Arrays;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import restx.factory.processor.FactoryAnnotationProcessor;

public class TestGeneratedMethodAnnotations {

	private static final String PACKAGE_NAME = "com.example";
	private static final String NEW_LINE = System.getProperty("line.separator");

	@Test
	public void test() {
		final JavaFileObject input = JavaFileObjects.forSourceLines(
		        "com.example.A",
		        new String[] {
		                "package "
		                + PACKAGE_NAME
		                + ";",
		                "",
		                "import restx.RestxLogLevel;" , 
		                "import restx.RestxRequest;" , 
		                "import restx.annotations.GET;" , 
		                "import restx.annotations.Param;" , 
		                "import restx.annotations.Param.Kind;" , 
		                "import restx.annotations.RestxResource;" , 
		                "import restx.annotations.Verbosity;" , 
		                "import restx.annotations.SuccessStatus;" ,
		                "import restx.validation.ValidatedFor;" ,
		                "import restx.factory.Component;",
		                "@Component" , 
		                "@RestxResource",
		                "public class A {",
		                "",
		                "public @interface MyAnnotation {",
		                "boolean[] severalBools() default { false, true };",
		                "Class<? extends Number>[] severalParameterizedTypeClasses() default { Integer.class, Long.class, Double.class };",
		                "}",
		                "   @GET(\"/abc\")",
		                "   @SuccessStatus",
		                "   @MyAnnotation",
		                "   public void b(@ValidatedFor(A.class) int num) {",
		                "   }",
		                "}"}
		        )
		;
		Truth.assert_().about(JavaSourcesSubjectFactory.javaSources()).that(Arrays.asList(input))
				.processedWith(new RestxAnnotationProcessor()).compilesWithoutError().and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, PACKAGE_NAME, "ARouter.java")
				.and().generatesSources(JavaFileObjects.forResource("restx/annotations/processor/ARouterWithAnnotations.java"));
		Truth.assert_().about(JavaSourcesSubjectFactory.javaSources()).that(Arrays.asList(input))
		.processedWith(new RestxAnnotationProcessor(), new FactoryAnnotationProcessor()).compilesWithoutError().and()
		.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, PACKAGE_NAME, "AFactoryMachine.java")
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, PACKAGE_NAME, "ARouterFactoryMachine.java")
				.and().generatesSources(JavaFileObjects.forResource("restx/annotations/processor/AFactoryMachine.java"))
				.and().generatesSources(JavaFileObjects.forResource("restx/annotations/processor/ARouterFactoryMachine.java"));
	}	

}
