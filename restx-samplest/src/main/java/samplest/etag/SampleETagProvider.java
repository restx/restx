package samplest.etag;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import restx.http.ETagProvider;
import restx.factory.Component;
import restx.http.CacheControl;
import restx.http.ETag;

/**
 * Date: 22/5/14
 * Time: 20:36
 */
@Component
public class SampleETagProvider implements ETagProvider<ETagSampleObject> {
    @Override
    public Class<ETagSampleObject> getEntityType() {
        return ETagSampleObject.class;
    }

    @Override
    public ETag provideETagFor(ETagSampleObject entity) {
        return new ETag(Hashing.md5().hashString(entity.getName(), Charsets.UTF_8).toString(),
                new CacheControl(-1, ImmutableList.of("must-revalidate")));
    }
}
