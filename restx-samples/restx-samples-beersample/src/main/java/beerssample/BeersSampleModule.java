package beerssample;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.InvalidViewException;
import com.couchbase.client.protocol.views.ViewDesign;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.SignatureKey;
import restx.factory.Module;
import restx.factory.Provides;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Module
public class BeersSampleModule {
    private final Logger logger = LoggerFactory.getLogger(BeersSampleModule.class);

    public final static String BEER_DESIGN_DOC_NAME = "beer";
    public final static String BREWERY_DESIGN_DOC_NAME = "brewery";
    public final static String BY_NAME_VIEW_NAME = "by_name";
    public final static String ALL_WITH_BEERS_VIEW_NAME = "all_with_beers";

    @Provides
    public SignatureKey signatureKey() {
        return new SignatureKey("this is the key for my restx app".getBytes(Charsets.UTF_8));
    }

    @Provides
    public CouchbaseClient couchbase() {
        logger.info("Connecting to Couchbase Cluster");

        List<URI> nodes = new ArrayList<>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));
        try {
            CouchbaseClient client = new CouchbaseClient(nodes, "beer-sample", "");

            installViews(client);

            return client;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void installViews(CouchbaseClient client) {
        installBeerViews(client);
    }

    private void installBeerViews(CouchbaseClient client) {
        boolean installView = false;
        try {
            client.getView(BEER_DESIGN_DOC_NAME, BY_NAME_VIEW_NAME);
        } catch (InvalidViewException e) {
            installView = true;
        }

        if (installView) {
            logger.info("Installing Beer views....");
            DesignDocument designDoc = new DesignDocument(BEER_DESIGN_DOC_NAME);
            String viewName = BY_NAME_VIEW_NAME;
            String mapFunction =
                    "function (doc, meta) {\n" +
                            "  if(doc.type && doc.type == \"beer\") {\n" +
                            "    emit(doc.name);\n" +
                            "  }\n" +
                            "}";
            ViewDesign viewDesign = new ViewDesign(viewName, mapFunction);
            designDoc.getViews().add(viewDesign);
            client.createDesignDoc(designDoc);
        }
    }
}
