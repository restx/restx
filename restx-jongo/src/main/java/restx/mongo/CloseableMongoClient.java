package restx.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Simple extension of the MongoClient in order to add the {@link AutoCloseable} type.
 *
 * @author apeyrard
 */
public class CloseableMongoClient extends MongoClient implements AutoCloseable {

	public CloseableMongoClient() throws UnknownHostException {
	}

	public CloseableMongoClient(String host) throws UnknownHostException {
		super(host);
	}

	public CloseableMongoClient(String host, MongoClientOptions options) throws UnknownHostException {
		super(host, options);
	}

	public CloseableMongoClient(String host, int port) throws UnknownHostException {
		super(host, port);
	}

	public CloseableMongoClient(ServerAddress addr) {
		super(addr);
	}

	public CloseableMongoClient(ServerAddress addr, List<MongoCredential> credentialsList) {
		super(addr, credentialsList.get(0), new MongoClientOptions.Builder().build());
	}

	public CloseableMongoClient(ServerAddress addr, MongoClientOptions options) {
		super(addr, options);
	}

	public CloseableMongoClient(ServerAddress addr, List<MongoCredential> credentialsList, MongoClientOptions options) {
		super(addr, credentialsList.get(0), options);
	}

	public CloseableMongoClient(List<ServerAddress> seeds) {
		super(seeds);
	}

	public CloseableMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList) {
		super(seeds, credentialsList.get(0), new MongoClientOptions.Builder().build());
	}

	public CloseableMongoClient(List<ServerAddress> seeds, MongoClientOptions options) {
		super(seeds, options);
	}

	public CloseableMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList, MongoClientOptions options) {
		super(seeds, credentialsList.get(0), options);
	}

	public CloseableMongoClient(MongoClientURI uri) throws UnknownHostException {
		super(uri);
	}
}
