package beerssample.web;

import beerssample.domain.Beer;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import net.spy.memcached.internal.OperationFuture;
import org.joda.time.DateTime;
import restx.Status;
import restx.annotations.*;
import restx.common.UUIDGenerator;
import restx.factory.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component @RestxResource
public class BeerResource {
    private final CouchbaseClient couchbase;
    private final ObjectMapper objectMapper;

    public BeerResource(CouchbaseClient couchbase, ObjectMapper objectMapper) {
        this.couchbase = couchbase;
        this.objectMapper = objectMapper;
    }

    @GET("/beers/{id}")
    public Beer getBeerById(String id) {
        try {
            Beer beer = objectMapper.readValue(couchbase.get(id).toString(), Beer.class);
            beer.setId(id);
            return beer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @POST("/beers")
    public Beer createBeer(Beer beer) {
        try {
            beer.setType("beer");
            beer.setUpdated(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            if (Strings.isNullOrEmpty(beer.getId())) {
                beer.setId(UUIDGenerator.generate());
            }
            couchbase.set(beer.getId(), 0, objectMapper.writeValueAsString(beer));

            return beer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @PUT("/beers/{id}")
    public Beer updateBeer(String id, Beer beer) {
        try {
            beer.setUpdated(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            couchbase.set(id, 0, objectMapper.writeValueAsString(beer));
            return beer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DELETE("/beers/{id}")
    public Status deleteBeer(String id) {
        OperationFuture<Boolean> delete = couchbase.delete(id);
        try {
            if (delete.get()) {
                return Status.of("deleted");
            } else {
                return Status.of("error");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @GET("/beers")
    public Iterable<Beer> findBeersByName(String name) {
        try {
            Query query = new Query();
            query.setIncludeDocs(true).setLimit(20)
                  .setRangeStart(ComplexKey.of(name))
                  .setRangeEnd(ComplexKey.of(name + "\uefff"));

            View view = couchbase.getView("beer", "by_name");
            ViewResponse result = couchbase.query(view, query);

            List<Beer> beers = new ArrayList<>();
            for (ViewRow row : result) {
                Beer beer = objectMapper.readValue(
                        (String) row.getDocument(), Beer.class);
                beer.setId(row.getId());
                beers.add(beer);
            }
            return beers;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
