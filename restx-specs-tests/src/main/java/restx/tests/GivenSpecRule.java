package restx.tests;

import restx.factory.Factory;

import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/30/13
 * Time: 11:35 PM
 */
public interface GivenSpecRule {
    Map<String,String> getRunParams();
    void onSetup(Factory.LocalMachines localMachines);
    void onTearDown(Factory.LocalMachines localMachines);
}
