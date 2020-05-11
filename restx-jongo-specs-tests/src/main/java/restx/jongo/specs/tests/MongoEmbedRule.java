package restx.jongo.specs.tests;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.distribution.Version;
import restx.mongo.MongoModule;

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
	private static final MongodStarter runtime = MongodStarter.getDefaultInstance();

	private final EmbedMongoFactory factory;
	private final Version.Main mongoVersion;

	public MongoEmbedRule() {
		this(Version.Main.PRODUCTION);
	}

	public MongoEmbedRule(Version.Main mongoVersion) {
		this.mongoVersion = mongoVersion;
		this.factory = new EmbedMongoFactory();
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				PoolKey key = factory.getPoolKey(mongoVersion);
				System.setProperty(MongoModule.MONGO_URI, key.getUri().getURI());
				EmbedMongoClientPool pool = factory.getEmbedMongoClientPool(runtime, key);
				pool.checkIn(this);
				MongoClient mongoClient = new MongoClient(key.getUri());

				base.evaluate();

				mongoClient.close();
				pool.checkOut(this);
			}
		};
	}

}
