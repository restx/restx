package restx.factory.processor;

import com.github.mustachejava.Mustache;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.google.common.io.CharStreams;
import restx.factory.Component;
import restx.factory.Machine;
import restx.factory.Module;
import restx.factory.Provides;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.inject.Named;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static restx.common.Mustaches.compile;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 10:02 PM
 */
@SupportedAnnotationTypes({
        "restx.factory.Component",
        "restx.factory.Module",
        "restx.factory.Provides",
        "restx.factory.Machine"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FactoryAnnotationProcessor extends AbstractProcessor {
    final Mustache componentMachineTpl;
    final Mustache moduleMachineTpl;
    private final FactoryAnnotationProcessor.ServicesDeclaration machinesDeclaration;

    public FactoryAnnotationProcessor() {
        componentMachineTpl = compile(FactoryAnnotationProcessor.class, "ComponentMachine.mustache");
        moduleMachineTpl = compile(FactoryAnnotationProcessor.class, "ModuleMachine.mustache");
        machinesDeclaration = new ServicesDeclaration("restx.factory.FactoryMachine");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                machinesDeclaration.generate();
            } else {
                processComponents(roundEnv);
                processModules(roundEnv);
                processMachines(roundEnv);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void processModules(RoundEnvironment roundEnv) throws IOException {
        for (Element annotation : roundEnv.getElementsAnnotatedWith(Module.class)) {
            TypeElement typeElem = (TypeElement) annotation;

            ModuleClass module = new ModuleClass(typeElem.getQualifiedName().toString(), typeElem);
            for (Element element : typeElem.getEnclosedElements()) {
                if (element instanceof ExecutableElement
                        && element.getKind() == ElementKind.METHOD
                        && element.getAnnotation(Provides.class) != null) {
                    ExecutableElement exec = (ExecutableElement) element;

                    ProviderMethod m = new ProviderMethod(
                            exec.getReturnType().toString(),
                            exec.getSimpleName().toString(),
                            getInjectionName(exec.getAnnotation(Named.class)),
                            exec);

                    buildInjectableParams(exec, m.parameters);

                    module.providerMethods.add(m);
                }
            }

            generateMachineFile(module);
        }
    }

    private void processMachines(RoundEnvironment roundEnv) throws IOException {
        for (Element annotation : roundEnv.getElementsAnnotatedWith(Machine.class)) {
            TypeElement typeElem = (TypeElement) annotation;
            machinesDeclaration.declareService(typeElem.getQualifiedName().toString());
        }
    }

    private void processComponents(RoundEnvironment roundEnv) throws IOException {
        for (Element elem : roundEnv.getElementsAnnotatedWith(Component.class)) {
            TypeElement component = (TypeElement) elem;

            ExecutableElement exec = findInjectableConstructor(component);

            ComponentClass componentClass = new ComponentClass(
                    component.getQualifiedName().toString(),
                    getInjectionName(component.getAnnotation(Named.class)),
                    component.getAnnotation(Component.class).priority(),
                    component);

            buildInjectableParams(exec, componentClass.parameters);

            generateMachineFile(componentClass);
        }
    }

    private ExecutableElement findInjectableConstructor(TypeElement component) {
        ExecutableElement exec = null;
        for (Element element : component.getEnclosedElements()) {
            if (element instanceof ExecutableElement && element.getKind() == ElementKind.CONSTRUCTOR) {
                if (exec == null
                        || element.getAnnotation(Inject.class) != null) {
                    exec = (ExecutableElement) element;
                    if (exec.getAnnotation(Inject.class) != null) {
                        // if a constructor is marked with @Inject we use it whatever other constructors are available
                        return exec;
                    }
                }
            }
        }
        return exec;
    }

    private void buildInjectableParams(ExecutableElement executableElement, List<InjectableParameter> parameters) {
        for (VariableElement p : executableElement.getParameters()) {
            parameters.add(new InjectableParameter(
                    p.asType().toString(),
                    p.getSimpleName().toString(),
                    getInjectionName(p.getAnnotation(Named.class))
            ));
        }
    }

    private Optional<String> getInjectionName(Named named) {
        return named != null ? Optional.of(named.value()) : Optional.<String>absent();
    }


    private void generateMachineFile(ModuleClass moduleClass) throws IOException {
        List<ImmutableMap<String, Object>> engines = Lists.newArrayList();

        for (ProviderMethod method : moduleClass.providerMethods) {
            engines.add(ImmutableMap.<String, Object>builder()
                    .put("type", method.type)
                    .put("name", method.name)
                    .put("injectionName", method.injectionName.isPresent() ?
                            method.injectionName.get() : method.name)
                    .put("queriesDeclarations", Joiner.on("\n").join(buildQueriesDeclarationsCode(method.parameters)))
                    .put("queries", Joiner.on(",\n").join(buildQueriesNames(method.parameters)))
                    .put("parameters", Joiner.on(",\n").join(buildParamFromSatisfiedBomCode(method.parameters)))
                    .build());
        }

        ImmutableMap<String, Object> ctx = ImmutableMap.<String, Object>builder()
                .put("package", moduleClass.pack)
                .put("machine", moduleClass.name + "FactoryMachine")
                .put("moduleFqcn", moduleClass.fqcn)
                .put("moduleType", moduleClass.name)
                .put("engines", engines)
                .build();

        generateJavaClass(moduleClass.fqcn + "FactoryMachine", moduleMachineTpl, ctx,
                Collections.singleton(moduleClass.originatingElement));
    }

    private void generateMachineFile(ComponentClass componentClass) throws IOException {
        ImmutableMap<String, String> ctx = ImmutableMap.<String, String>builder()
                .put("package", componentClass.pack)
                .put("machine", componentClass.name + "FactoryMachine")
                .put("componentFqcn", componentClass.fqcn)
                .put("componentType", componentClass.name)
                .put("priority", String.valueOf(componentClass.priority))
                .put("componentInjectionName", componentClass.injectionName.isPresent() ?
                        componentClass.injectionName.get() : componentClass.name)
                .put("queriesDeclarations", Joiner.on("\n").join(buildQueriesDeclarationsCode(componentClass.parameters)))
                .put("queries", Joiner.on(",\n").join(buildQueriesNames(componentClass.parameters)))
                .put("parameters", Joiner.on(",\n").join(buildParamFromSatisfiedBomCode(componentClass.parameters)))
                .build();

        generateJavaClass(componentClass.fqcn + "FactoryMachine", componentMachineTpl, ctx,
                Collections.singleton(componentClass.originatingElement));

    }

    private List<String> buildQueriesDeclarationsCode(List<InjectableParameter> parameters) {
        List<String> parametersCode = Lists.newArrayList();
        for (InjectableParameter parameter : parameters) {
            parametersCode.add(parameter.getQueryDeclarationCode());
        }
        return parametersCode;
    }

    private List<String> buildQueriesNames(List<InjectableParameter> parameters) {
        List<String> parametersCode = Lists.newArrayList();
        for (InjectableParameter parameter : parameters) {
            parametersCode.add(parameter.name);
        }
        return parametersCode;
    }

    private List<String> buildParamFromSatisfiedBomCode(List<InjectableParameter> parameters) {
        List<String> parametersCode = Lists.newArrayList();
        for (InjectableParameter parameter : parameters) {
            parametersCode.add(parameter.getFromSatisfiedBomCode());
        }
        return parametersCode;
    }

    private void generateJavaClass(String className, Mustache mustache, ImmutableMap<String, ? extends Object> ctx,
            Set<Element> originatingElements) throws IOException {
        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(className,
                Iterables.toArray(originatingElements, Element.class));
        try (Writer writer = fileObject.openWriter()) {
            mustache.execute(writer, ctx);
        }
    }

    private static class ComponentClass {
        final String fqcn;

        final List<InjectableParameter> parameters = Lists.newArrayList();
        final Element originatingElement;
        final String pack;
        final String name;
        final int priority;
        final Optional<String> injectionName;

        ComponentClass(String fqcn, Optional<String> injectionName, int priority, Element originatingElement) {
            this.fqcn = fqcn;
            this.injectionName = injectionName;
            this.priority = priority;
            this.pack = fqcn.substring(0, fqcn.lastIndexOf('.'));
            this.name = fqcn.substring(fqcn.lastIndexOf('.') + 1);
            this.originatingElement = originatingElement;
        }
    }

    private static class InjectableParameter {
        final String type;
        final String name;
        final Optional<String> injectionName;

        private InjectableParameter(String type, String name, Optional<String> injectionName) {
            this.type = type;
            this.name = name;
            this.injectionName = injectionName;
        }

        public String getQueryDeclarationCode() {
            if (injectionName.isPresent()) {
                return String.format("private final Factory.Query<%s> %s = Factory.Query.byName(Name.of(%s, \"%s\"));",
                        type, name, type + ".class", injectionName.get());
            } else {
                return String.format("private final Factory.Query<%s> %s = Factory.Query.byClass(%s).mandatory();",
                        type, name, type + ".class");
            }
        }

        public String getFromSatisfiedBomCode() {
            return String.format("satisfiedBOM.getOne(%s).get().getComponent()", name);
        }
    }

    private static class ModuleClass {
        final String fqcn;

        final List<ProviderMethod> providerMethods = Lists.newArrayList();
        final Element originatingElement;
        final String pack;
        final String name;

        ModuleClass(String fqcn, Element originatingElement) {
            this.fqcn = fqcn;
            this.pack = fqcn.substring(0, fqcn.lastIndexOf('.'));
            this.name = fqcn.substring(fqcn.lastIndexOf('.') + 1);
            this.originatingElement = originatingElement;
        }
    }

    private static class ProviderMethod {
        final Element originatingElement;
        final String type;
        final String name;
        final Optional<String> injectionName;
        final List<InjectableParameter> parameters = Lists.newArrayList();

        ProviderMethod(String type, String name, Optional<String> injectionName, Element originatingElement) {
            this.type = type;
            this.name = name;
            this.injectionName = injectionName;
            this.originatingElement = originatingElement;
        }
    }


    private class ServicesDeclaration {
        private final Set<String> declaredServices = Sets.newHashSet();
        private final String targetFile;

        private ServicesDeclaration(String targetFile) {
            this.targetFile = targetFile;
        }

        void declareService(String service) {
            declaredServices.add(service);
        }

        void generate() throws IOException {
            if (declaredServices.isEmpty()) {
                return;
            }

            String targetFile = "META-INF/services/" + this.targetFile;

            readExistingServicesIfExists(targetFile);

            FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", targetFile);
            Writer writer = fileObject.openWriter();
            for (String declaredService : Ordering.natural().sortedCopy(declaredServices)) {
                writer.write(declaredService + "\n");
            }

            writer.close();

            declaredServices.clear();
        }

        private void readExistingServicesIfExists(String targetFile) throws IOException {
            try {
                FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", targetFile);
                declaredServices.addAll(
                        CharStreams.readLines(fileObject.openReader(true)));
            } catch (IOException | IllegalArgumentException ex) {
                // ignore
            }
        }
    }
}
