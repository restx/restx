package restx.jongo.specs.tests;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import restx.mongo.MongoModule;

import com.mongodb.MongoClient;

import static restx.jongo.specs.tests.MongoVersion.DEFAULT_MONGO_VERSION;

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
	private final EmbedMongoFactory factory;
	private final String mongoVersion;

	public MongoEmbedRule() {
		this(DEFAULT_MONGO_VERSION);
	}

	public MongoEmbedRule(String mongoVersion) {
		this.mongoVersion = mongoVersion;
		this.factory = new EmbedMongoFactory();
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				EmbedMongoClientPool pool = factory.getEmbedMongoClientPool(mongoVersion);
				pool.checkIn(this);
				System.setProperty(MongoModule.MONGO_URI, pool.getConnectionString());
				MongoClient mongoClient = new MongoClient(pool.getMongoUri());

				base.evaluate();

				mongoClient.close();
				pool.checkOut(this);
			}
		};
	}

}
