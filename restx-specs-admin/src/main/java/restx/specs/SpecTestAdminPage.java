package restx.specs;

import restx.admin.AdminPage;
import restx.factory.Component;
import restx.factory.When;

/**
 * User: xavierhanin
 * Date: 9/1/13
 * Time: 3:20 PM
 */
@Component
@When(name="restx.mode", value="infinirest")
public class SpecTestAdminPage extends AdminPage {
    public SpecTestAdminPage() {
        super("/@/ui/tests/", "Spec Tests");
    }
}
