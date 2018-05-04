package samplest.jongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.Id;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.jongo.JongoCollection;
import restx.security.PermitAll;

import javax.inject.Named;

@RestxResource @Component
public class MongoResource {
    public static class ObjectWithIdAnnotation {
        // @Id // 0.34 -> OK ; 0.35 -> KO!
        @Id @JsonProperty("_id") // 0.35 -> KO!
        private String id;
        private String label;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    public static class ObjectWithObjectIdAnnotation {
        // @org.jongo.marshall.jackson.oid.ObjectId @Id // 0.34 -> OK ; 0.35 -> KO!
        @Id @org.jongo.marshall.jackson.oid.ObjectId @JsonProperty("_id") // 0.35 -> KO!
        private String id;
        private String label;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    public static class ObjectWithObjectIdType {
        // @Id // 0.34 -> OK ; 0.35 -> OK
        @Id @JsonProperty("_id") // 0.35 -> OK
        private ObjectId id;
        private String label;

        public String getId() { return id.toString(); }
        public void setId(ObjectId id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    private final JongoCollection objectWithIdAnnotationCollection;
    private final JongoCollection objectWithObjectIdAnnotationCollection;
    private final JongoCollection objectWithObjectIdTypeCollection;

    public MongoResource(
            @Named("objectWithIdAnnotationCollection") JongoCollection objectWithIdAnnotationCollection,
            @Named("objectWithObjectIdAnnotationCollection") JongoCollection objectWithObjectIdAnnotationCollection,
            @Named("objectWithObjectIdTypeCollection") JongoCollection objectWithObjectIdTypeCollection) {
        this.objectWithIdAnnotationCollection = objectWithIdAnnotationCollection;
        this.objectWithObjectIdAnnotationCollection = objectWithObjectIdAnnotationCollection;
        this.objectWithObjectIdTypeCollection = objectWithObjectIdTypeCollection;
    }

    @POST("/mongo/objectsWithIdAnnotation")
    @PermitAll
    public ObjectWithIdAnnotation createFoo(ObjectWithIdAnnotation foo) {
        this.objectWithIdAnnotationCollection.get().insert(foo);
        return foo;
    }

    @POST("/mongo/objectsWithObjectIdAnnotation")
    @PermitAll
    public ObjectWithObjectIdAnnotation createObjectWithObjectIdAnnotation(ObjectWithObjectIdAnnotation objectWithObjectIdAnnotation) {
        this.objectWithObjectIdAnnotationCollection.get().insert(objectWithObjectIdAnnotation);
        return objectWithObjectIdAnnotation;
    }

    @POST("/mongo/objectsWithObjectIdType")
    @PermitAll
    public ObjectWithObjectIdType createObjectWithObjectId(ObjectWithObjectIdType objectWithObjectIdType) {
        this.objectWithObjectIdTypeCollection.get().insert(objectWithObjectIdType);
        return objectWithObjectIdType;
    }


}
