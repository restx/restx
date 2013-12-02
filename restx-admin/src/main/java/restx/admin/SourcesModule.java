package restx.admin;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.AppSettings;
import restx.FSRouter;
import restx.ResourcesRoute;
import restx.RestxRouter;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;
import java.io.File;

/**
 * Date: 28/11/13
 * Time: 22:58
 */
@Module
public class SourcesModule {
    private static final RestxRouter NO_SOURCES = RestxRouter.builder().name("NO-SOURCES").build();

    @Provides @Named("Sources")
    public AdminPage getSourcesAdminPage() {
        return new AdminPage("/@/ui/sources/", "Sources");
    }

    @Provides
    public ResourcesRoute sourceUI() {
        return new ResourcesRoute("SourcesUIRoute", "/@/ui/sources", "restx/sources", ImmutableMap.of("", "index.html"));
    }

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

    @Provides
    public RestxRouter mountResources(AppSettings appSettings) {
        if (!"dev".equals(appSettings.mode())) {
            return NO_SOURCES;
        }
        return resourcesRouter(appSettings).or(NO_SOURCES);
    }

    public Optional<RestxRouter> resourcesRouter(AppSettings appSettings) {
        if (new File(appSettings.mainResources()).exists()) {
            return Optional.of(FSRouter.mount(appSettings.mainResources()).allowDirectoryListing().on("/@/sources/resources/"));
        } else {
            return Optional.absent();
        }
    }
}
