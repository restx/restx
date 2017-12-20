package restx.md;

import com.googlecode.junittoolbox.ParallelParameterized;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.build.testing.AbstractRestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest;

import java.util.Arrays;

import static java.util.Collections.EMPTY_LIST;


@RunWith(ParallelParameterized.class)
public class RestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest extends AbstractRestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest {

    private static final String RESTX_SOURCES_ROOT_DIR_SYSPROP = "restxSourcesRootDir";

    @Parameterized.Parameters(name="{0}/{1}")
    public static Iterable<Object[]> data(){
        return AbstractRestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest.data(RESTX_SOURCES_ROOT_DIR_SYSPROP, EMPTY_LIST);
    }

    public RestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest(String moduleName, GenerationType generationType) {
        super(moduleName, generationType);
    }

    @Override
    protected String getRestxSourcesDirSysProp() {
        return RESTX_SOURCES_ROOT_DIR_SYSPROP;
    }
}
