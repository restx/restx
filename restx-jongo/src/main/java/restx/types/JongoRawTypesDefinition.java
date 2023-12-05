package restx.types;

import org.bson.types.ObjectId;

public class JongoRawTypesDefinition extends RawTypesDefinition.ClassBasedRawTypesDefinition {
    public JongoRawTypesDefinition() {
        super(ObjectId.class);
    }
}
