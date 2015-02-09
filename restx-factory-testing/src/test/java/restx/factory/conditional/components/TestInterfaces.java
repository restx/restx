package restx.factory.conditional.components;

import java.util.List;

/**
 * @author apeyrard
 */
public class TestInterfaces {

	public static interface Resolver {
		String resolve(String constraint);
	}

	public static interface Workspace {
		List<String> modules();
	}
}
