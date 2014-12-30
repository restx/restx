package samplest.core;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.factory.Factory;
import restx.metrics.codahale.CodahaleMetricRegistry;
import restx.tests.RestxServerRule;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 13/12/13
 * Time: 23:09
 */
public class ProdModeTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule().setMode("prod");

    @Test
    public void should_accept_stars_in_url_and_monitor_it() throws Exception {
        // test for #126

        // first we do a request and check it is properly processed
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/params/path/v1*/v2/35v4");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=v1* b=v2 c=35 d=v4");

        // then we check corresponding timer has been registered in Metrics
        MetricRegistry registry = ((CodahaleMetricRegistry) Factory.getFactory(server.getServer().getServerId()).get()
                .getComponent(restx.common.metrics.api.MetricRegistry.class)).getCodahaleMetricRegistry();

        SortedMap<String, Timer> timers = registry.getTimers(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.indexOf("<HTTP> GET /params/path/") != -1;
            }
        });

        assertThat(timers.size()).isEqualTo(1);
        assertThat(timers.firstKey()).isEqualTo("<HTTP> GET /params/path/v1*/v2/35v4");

        // and now we check a MBean has been created for that timer too.
        // the name of the MBean is escaped, so it is enclosed in quotes: "
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> objectNames = mBeanServer.queryNames(
                ObjectName.getInstance("metrics:name=\"<HTTP> GET /params/path/*\""), null);
        assertThat(objectNames.size()).isEqualTo(1);
    }
}
