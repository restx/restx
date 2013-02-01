package {package};

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import restx.*;
import restx.factory.*;

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
