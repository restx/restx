package restx.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class RolesTest {
    private String rawName;
    private String[] oids;
    private String expectedInterpolatedRole;

    @Parameterized.Parameters(name="{2}")
    public static Iterable<Object[]> data() throws IOException {
        return Arrays.asList(
                new Object[]{ "FOO", new String[]{ "123", "456" }, "FOO" },
                new Object[]{ "BAR", null, "BAR" },
                new Object[]{ "EDIT_COMPANY_{company}_{subCompany}", new String[]{ "123", "456" }, "EDIT_COMPANY_123_456" },
                new Object[]{ "EDIT_COMPANY_{company}_{subCompany}", new String[]{ "*", "456" }, "EDIT_COMPANY_*_456" },
                new Object[]{ "EDIT_COMPANY_{company}_{subCompany}", new String[]{ "*", "*" }, "EDIT_COMPANY_*_*" },
                new Object[]{ "EDIT_COMPANY_{company}_{subCompany}", new String[]{ "123", "*" }, "EDIT_COMPANY_123_*" }
        );
    }

    public RolesTest(String rawName, String[] oids, String expectedInterpolatedRole) {
        this.rawName = rawName;
        this.oids = oids;
        this.expectedInterpolatedRole = expectedInterpolatedRole;
    }

    @Test
    public void should_roles_interpolation_works() {
        assertThat(Roles.getInterpolatedRoleName(this.rawName, this.oids)).isEqualTo(this.expectedInterpolatedRole);
    }
}
