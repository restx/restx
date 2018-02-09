package restx.kt.compiler

import org.jetbrains.kotlin.com.intellij.openapi.*
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.cli.jvm.config.*
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.state.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.load.java.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.resolve.*
import java.io.*
import java.net.*
import java.nio.file.*
import java.util.*
import java.util.jar.*

/**
 * Author: Sergey Mashkov
 * Mostly borrowed from vertx koltin compiler
 */
object KotlinCompilerHelper {
    fun compileKotlinScript(classLoader: ClassLoader,
                            url: URL,
                            destination: Path,
                            predicate: (GenerationState, ClassDescriptor) -> Boolean
    ): Map<Class<*>, ClassDescriptor> {

        val configuration = CompilerConfiguration()
        val printingMessageCollector = PrintingMessageCollector(System.err, MessageRenderer.WITHOUT_PATHS, false)
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, printingMessageCollector)
        configuration.put(CLIConfigurationKeys.ALLOW_KOTLIN_PACKAGE, false)
        configuration.put(CLIConfigurationKeys.REPORT_PERF, false)

        configuration.put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, LanguageVersionSettingsImpl.DEFAULT)

        configuration.put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_1_8)
        configuration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, destination.toFile())
        configuration.put(JVMConfigurationKeys.SKIP_RUNTIME_VERSION_CHECK, true)
        configuration.put(CommonConfigurationKeys.MODULE_NAME, JvmAbi.DEFAULT_MODULE_NAME)

        val classPath = (
                classLoader.classPath()
                        + ClassLoader.getSystemClassLoader().classPath()
                        + (Thread.currentThread().contextClassLoader?.classPath() ?: emptyList())
                        + propertyClassPath("java.class.path")
                        + propertyClassPath("sun.boot.class.path")
                ).distinct().filter { Files.exists(it) }

        for (item in classPath) {
            configuration.add(JVMConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(item.toFile()))
        }
        configuration.add(JVMConfigurationKeys.CONTENT_ROOTS, KotlinSourceRoot(Paths.get(url.toURI()).toString()))

        val collected = HashMap<String, ClassDescriptor>()

        val environment = KotlinCoreEnvironment.createForProduction(Disposable { }, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        val finalState = KotlinToJVMBytecodeCompiler.analyzeAndGenerate(environment)

        if (printingMessageCollector.hasErrors()) {
            throw CompilationException("Compilation failed", null, null)
        }
        if (finalState == null) {
            return emptyMap()
        }

        val compilerClassLoader = GeneratedClassLoader(finalState.factory, classLoader)

        return finalState.factory.getClassFiles().toList()
                .map { it.relativePath.removeSuffix(".class").replace("/", ".") }
                .filter { it !in collected }
                .mapNotNull { finalState.bindingContext.get(BindingContext.FQNAME_TO_CLASS_DESCRIPTOR, FqNameUnsafe(it.replace("$", "."))) }
                .filter { predicate(finalState, it) }
                .associateBy { finalState.typeMapper.mapClass(it).className }
                .mapKeys { compilerClassLoader.loadClass(it.key) }
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