package restx.admin;

import com.google.common.base.Optional;
import restx.AppSettings;
import restx.FSRouter;
import restx.RestxRouter;
import restx.factory.Module;
import restx.factory.Provides;
import java.io.File;

/**
 * Date: 28/11/13
 * Time: 22:58
 */
@Module
public class SourcesModule {
    private static final RestxRouter NO_SOURCES = RestxRouter.builder().name("NO-SOURCES").build();

    @Provides
    public RestxRouter mountSources(AppSettings appSettings) {
        if (!"dev".equals(appSettings.mode())) {
            return NO_SOURCES;
        }
        return sourcesRouter(appSettings).or(NO_SOURCES);
    }

    public Optional<RestxRouter> sourcesRouter(AppSettings appSettings) {
        if (new File(appSettings.mainSources()).exists()) {
            return Optional.of(FSRouter.mount(appSettings.mainSources()).allowDirectoryListing().on("/@/sources/main/"));
        } else {
            return Optional.absent();
        }
    }
}
