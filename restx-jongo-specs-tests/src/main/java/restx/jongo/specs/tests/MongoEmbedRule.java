package restx.jongo.specs.tests;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import restx.mongo.MongoModule;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Rule which allows per JVM process : 
 *  1/ download specific version of mongodb 
 *  2/ get free/dynamic port to mongodb
 * Rule which allows per test class :
 *  1/ start mongodb
 *  2/ execute tests
 *  3/ stop mongodb
 */
public class MongoEmbedRule implements TestRule {

	// Download / extract mongodb once.
	private static MongodStarter runtime = MongodStarter.getDefaultInstance();

	// Determine uri mongodb connection once
	private static MongoClientURI mongoClientURI;

	static {
		try {
			InetAddress addr = Network.getLocalHost();
			int port = Network.getFreeServerPort(addr);
			mongoClientURI = new MongoClientURI(new StringBuilder("mongodb://")
					.append(addr.getHostName()).append(":").append(port)
					.toString());
			System.setProperty(MongoModule.MONGO_URI, mongoClientURI.getURI());
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	

	private Version.Main mongoVersion;

	public MongoEmbedRule() {
		this(Version.Main.PRODUCTION);
	}

	public MongoEmbedRule(Version.Main mongoVersion) {
		this.mongoVersion = mongoVersion;
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				MongodExecutable _mongodExe = runtime
						.prepare(new MongodConfigBuilder()
								.version(mongoVersion)
								.net(new Net(Integer.parseInt(mongoClientURI
										.getURI().split(":")[2]), false))
								.build());
				MongodProcess _mongod = _mongodExe.start();
				MongoClient mongoClient = new MongoClient(mongoClientURI);

				base.evaluate();

				mongoClient.close();
				_mongod.stop();
				_mongodExe.stop();
			}
		};
	}

}
