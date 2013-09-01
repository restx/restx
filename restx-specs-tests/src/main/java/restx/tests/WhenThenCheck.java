package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.specs.When;

/**
 * Defines a When Then and Check together, useful to programmatically add additional when-then to
 * a RestxSpec.
 *
 * <code>
 *
 * </code>
 */
public abstract class WhenThenCheck extends When<WhenThenCheck.Then> implements WhenChecker<WhenThenCheck> {
    public static class Then implements restx.specs.Then {
    }

    public WhenThenCheck() {
        super(new Then());
    }

    @Override
    public void toString(StringBuilder sb) {
    }

    @Override
    public Class<WhenThenCheck> getWhenClass() {
        return WhenThenCheck.class;
    }

    @Override
    public void check(WhenThenCheck when, ImmutableMap<String, String> params) {
        check(params);
    }

    protected abstract void check(ImmutableMap<String, String> params);
}
