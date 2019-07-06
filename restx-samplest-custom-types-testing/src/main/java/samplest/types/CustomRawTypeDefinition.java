package samplest.types;

import restx.types.RawTypesDefinition;

public class CustomRawTypeDefinition extends RawTypesDefinition.FQCNBasedRawTypesDefinition {
    public CustomRawTypeDefinition() {
        super("samplest.models.MyCustomValueClass");
    }
}
