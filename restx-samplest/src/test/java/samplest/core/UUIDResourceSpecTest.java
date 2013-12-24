package samplest.core;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import restx.tests.RestxSpecRule;

/**
 * Date: 24/12/13
 * Time: 15:01
 */
public class UUIDResourceSpecTest {
    @ClassRule
    public static RestxSpecRule rule = new RestxSpecRule();

    @Test
    public void should_get_uuid() throws Exception {
        rule.runTest("specs/uuids.spec.yaml");
    }
}
