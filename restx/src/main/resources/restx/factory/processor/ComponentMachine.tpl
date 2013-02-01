package {package};

import restx.factory.*;
import {componentFqcn};

@Machine
public class {machine} extends SingleNameFactoryMachine<{componentType}> {
    public static final Name<{componentType}> NAME = Name.of({componentType}.class, "{componentInjectionName}");

    public {machine}() {
        super(0, NAME, BoundlessComponentBox.FACTORY);
    }

    @Override
    protected {componentType} doNewComponent(Factory factory) {
        return new {componentType}(
            {parameters}
        );
    }
}
