package samplest.jongo;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.jongo.JongoCollection;
import restx.security.PermitAll;

import javax.inject.Named;

@RestxResource @Component
public class MongoResource {
    public static class ObjectWithNewJongoAnnotations {
        @MongoId @MongoObjectId
        private String id;
        private String label;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    public static class ObjectWithIdAnnotation {
        // WARN: using only @Id was enough in jongo 1.1, but 1.4 requires to use @MongoObjectId also
        // (and deprecates @Id in favor of @MongoObjectId)
        @Id @MongoObjectId
        private String id;
        private String label;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    public static class ObjectWithObjectIdAnnotation {
        // using deprecated jongo annotations
        @Id @org.jongo.marshall.jackson.oid.ObjectId
        private String id;
        private String label;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    public static class ObjectWithObjectIdType {
        // using deprecated jongo annotations
        @Id
        private ObjectId id;
        private String label;

        public String getId() { return id.toString(); }
        public void setId(ObjectId id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    private final JongoCollection objectWithIdAnnotationCollection;
    private final JongoCollection objectWithMongoIdAnnotationCollection;
    private final JongoCollection objectWithObjectIdAnnotationCollection;
    private final JongoCollection objectWithObjectIdTypeCollection;

    public MongoResource(
            @Named("objectWithIdAnnotationCollection") JongoCollection objectWithIdAnnotationCollection,
            @Named("objectWithMongoIdAnnotationCollection") JongoCollection objectWithMongoIdAnnotationCollection,
            @Named("objectWithObjectIdAnnotationCollection") JongoCollection objectWithObjectIdAnnotationCollection,
            @Named("objectWithObjectIdTypeCollection") JongoCollection objectWithObjectIdTypeCollection) {
        this.objectWithIdAnnotationCollection = objectWithIdAnnotationCollection;
        this.objectWithMongoIdAnnotationCollection = objectWithMongoIdAnnotationCollection;
        this.objectWithObjectIdAnnotationCollection = objectWithObjectIdAnnotationCollection;
        this.objectWithObjectIdTypeCollection = objectWithObjectIdTypeCollection;
    }

    @POST("/mongo/objectsWithIdAnnotation")
    @PermitAll
    public ObjectWithIdAnnotation createFoo(ObjectWithIdAnnotation foo) {
        this.objectWithIdAnnotationCollection.get().insert(foo);
        return foo;
    }

    @POST("/mongo/objectsWithMongoIdAnnotation")
    @PermitAll
    public ObjectWithNewJongoAnnotations createObjectWithMongoIdAnnotation(ObjectWithNewJongoAnnotations o) {
        this.objectWithMongoIdAnnotationCollection.get().insert(o);
        return o;
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
