package restx.kt.compiler

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.Manifest

class KotlinCompiler(classLoader: ClassLoader = Thread.currentThread().getContextClassLoader(),
                     destination: Path,
                     val kotlinAnnotationProcessingJar: Path,
                     val annotationProcessors: Array<Path>
                     ) {
    private val classPath: List<Path>
    private val compiler: K2JVMCompiler
    private val arguments: K2JVMCompilerArguments

    init {
        classPath = (
                classLoader.classPath()
                        + ClassLoader.getSystemClassLoader().classPath()
                        + (Thread.currentThread().contextClassLoader?.classPath() ?: emptyList())
                        + propertyClassPath("java.class.path")
                        + propertyClassPath("sun.boot.class.path")
                ).distinct().filter { Files.exists(it) }

        compiler = K2JVMCompiler()
        arguments = compiler.createArguments()
        arguments.noStdlib = true
        arguments.useJavac = true
        arguments.compileJava = true
        arguments.destination = destination.toFile().absolutePath
        arguments.classpath = classPath.map { it.toAbsolutePath().toString() }.joinToString(File.pathSeparator)
    }

    fun compile(sourceRoots: Array<Path>) {
        val tmp = Files.createTempDirectory("kc-")

        val printingMessageCollector = PrintingMessageCollector(System.err, MessageRenderer.WITHOUT_PATHS, false)

        arguments.freeArgs = sourceRoots.map { it.toAbsolutePath().toString() }

        arguments.pluginClasspaths = arrayOf(kotlinAnnotationProcessingJar.toString())
        arguments.pluginOptions = arrayOf(
                "plugin:org.jetbrains.kotlin.kapt3:verbose=false",
                "plugin:org.jetbrains.kotlin.kapt3:aptMode=stubsAndApt",
                "plugin:org.jetbrains.kotlin.kapt3:sources=$tmp/generated-sources",
                "plugin:org.jetbrains.kotlin.kapt3:classes=$tmp/generated-classes",
                "plugin:org.jetbrains.kotlin.kapt3:stubs=$tmp/generated-stubs",
                "plugin:org.jetbrains.kotlin.kapt3:apclasspath=${annotationProcessors.joinToString(",")}"
        )

        compiler.exec(printingMessageCollector, Services.EMPTY, arguments)

        arguments.pluginClasspaths = arrayOf()
        arguments.freeArgs = sourceRoots.map { it.toAbsolutePath().toString() }.plus("$tmp/generated-sources")

        compiler.exec(printingMessageCollector, Services.EMPTY, arguments)

        tmp.toFile().deleteRecursively()

        if (printingMessageCollector.hasErrors()) {
            throw CompilationException("Compilation failed", null, null)
        }
    }

    private fun ClassLoader.classPath() = (classPathImpl() + manifestClassPath()).distinct()

    private fun ClassLoader.classPathImpl(): List<Path> {
        val parentUrls = parent?.classPathImpl() ?: emptyList()

        return when {
            this is URLClassLoader -> urLs.filterNotNull().map(URL::toURI).mapNotNull { ifFailed(null) { Paths.get(it) } } + parentUrls
            else -> parentUrls
        }
    }

    private fun ClassLoader.manifestClassPath() =
            getResources("META-INF/MANIFEST.MF")
                    .asSequence()
                    .mapNotNull { ifFailed(null) { it.openStream().use { Manifest().apply { read(it) } } } }
                    .flatMap { it.mainAttributes?.getValue("Class-Path")?.splitToSequence(" ")?.filter(String::isNotBlank) ?: emptySequence() }
                    .mapNotNull { ifFailed(null) { Paths.get(URI.create(it)) } }
                    .toList()

    private fun propertyClassPath(key: String) = System.getProperty(key)
            ?.split(File.pathSeparator)
            ?.filter(String::isNotEmpty)
            ?.map { Paths.get(it) }
            ?: emptyList()

    private inline fun <R> ifFailed(default: R, block: () -> R) = try {
        block()
    } catch (t: Throwable) {
        default
    }

}