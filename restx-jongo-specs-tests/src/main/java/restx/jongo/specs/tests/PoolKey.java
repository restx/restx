package restx.jongo.specs.tests;

import com.mongodb.MongoClientURI;
import de.flapdoodle.embed.mongo.distribution.Version;

import java.util.Objects;

class PoolKey {

    private final Version.Main version;
    private final MongoClientURI uri;

    PoolKey(Version.Main version, MongoClientURI uri) {
        this.version = version;
        this.uri = uri;
    }

    public Version.Main getVersion() {
        return version;
    }

    public MongoClientURI getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PoolKey poolKey = (PoolKey) o;
        return version == poolKey.version &&
                Objects.equals(uri, poolKey.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, uri);
    }
}
