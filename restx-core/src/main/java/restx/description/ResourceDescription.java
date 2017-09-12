package restx.description;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 10:53 AM
 */
public class ResourceDescription {
    public String path;
    public String stdPath;
    public String description = "";

    public List<OperationDescription> operations = Lists.newArrayList();

    public static class Matcher implements Predicate<ResourceDescription> {
        private Predicate<String> pathMatcher = Predicates.alwaysTrue();
        private Predicate<String> stdPathMatcher = Predicates.alwaysTrue();
        private Predicate<String> descriptionMatcher = Predicates.alwaysTrue();

        public Matcher withPathMatcher(Predicate<String> pathMatcher) {
            this.pathMatcher = pathMatcher;
            return this;
        }

        public Matcher withStdPathMatcher(Predicate<String> stdPathMatcher) {
            this.stdPathMatcher = stdPathMatcher;
            return this;
        }

        public Matcher withDescriptionMatcher(Predicate<String> descriptionMatcher) {
            this.descriptionMatcher = descriptionMatcher;
            return this;
        }

        public boolean apply(ResourceDescription description) {
            return pathMatcher.apply(description.path)
                    && stdPathMatcher.apply(description.stdPath)
                    && descriptionMatcher.apply(description.description);
        }
    }
}
