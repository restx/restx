package restx.jongo.specs.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import restx.mongo.MongoModule;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Rule which allows :
 *  1/ download specific version of mongodb
 *  2/ get free/dynamic port to mongodb
 *  3/ start mongodb
 *  4/ execute tests
 *  5/ stop mongodb
 */
public class MongoEmbedRule implements TestRule {

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
                InetAddress addr = Network.getLocalHost();
                int port = Network.getFreeServerPort(addr);

                System.setProperty(MongoModule.MONGO_URI, new StringBuilder(
                        "mongodb://").append(addr.getHostName()).append(":")
                        .append(port).toString());

                MongodStarter runtime = MongodStarter.getDefaultInstance();
                MongodExecutable _mongodExe = runtime
                        .prepare(new MongodConfigBuilder()
                                .version(mongoVersion)
                                .net(new Net(port, hostIsIPv6(addr))).build());
                MongodProcess _mongod = _mongodExe.start();
                new MongoClient(addr.getHostName(), port);

                base.evaluate();

                _mongod.stop();
                _mongodExe.stop();
            }

            private boolean hostIsIPv6(InetAddress addr)
                    throws UnknownHostException {
                byte[] ipAddr = addr.getAddress();
                if (ipAddr.length > 4) {
                    return true;
                }
                return false;
            }

        };
    }

}
