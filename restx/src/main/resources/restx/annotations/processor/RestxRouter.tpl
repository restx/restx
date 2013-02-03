package {package};

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import static com.google.common.base.Preconditions.checkNotNull;

import restx.*;
import restx.factory.*;
import static restx.common.MorePreconditions.checkPresent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class {router} extends RestxRouter {

    public {router}(final {resource} resource, final ObjectMapper mapper) {
        super(
                "{router}",
{routes}
        );
    }

}
