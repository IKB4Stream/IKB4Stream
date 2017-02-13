package com.waves_rsp.ikb4stream.communication.web;

import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.model.BoundingBox;
import com.waves_rsp.ikb4stream.core.communication.model.Request;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class VertxServer extends AbstractVerticle {
    private final Logger LOGGER = LoggerFactory.getLogger(VertxServer.class);
    private IDatabaseReader databaseReader = WebCommunication.databaseReader;

    @Override
    public void start(Future<Void> fut) {
        Router router = Router.router(vertx);

        router.route("/anomaly*").handler(BodyHandler.create()); // enable reading of request's body
        router.get("/anomaly").handler(this::getAnomalies);
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8081), // default value: 8081
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }

    /**
     * Read a request from a routing context, and attach the response to it. It requests the database with DatabaseReader.
     * @param rc RoutingContext, which contains the request, and the response
     */
    private void getAnomalies(RoutingContext rc) {
        // curl http://localhost:8081/anomaly -X GET -H "Content-Type: application/json" -d '{"start":1487004295000,"end":1487004295000, "boundingBox":{"points": [{"latitude":10, "longitude":20},{"latitude":15, "longitude":25}]}, "requestReception":1487004295000}'
        Date start = new Date(rc.getBodyAsJson().getLong("start"));
        Date end = new Date(rc.getBodyAsJson().getLong("end"));
        Date requestReception = new Date(rc.getBodyAsJson().getLong("requestReception"));

        JsonObject jsonbb = rc.getBodyAsJson().getJsonObject("boundingBox");
        JsonArray latlongs = jsonbb.getJsonArray("points");
        LatLong[] ll = new LatLong[latlongs.size()];
        for (int i=0; i < latlongs.size(); i++) {
            JsonObject latlong = latlongs.getJsonObject(i);
            double latitude = latlong.getDouble("latitude");
            double longitude = latlong.getDouble("longitude");
            ll[i] = new LatLong(latitude, longitude);
            System.out.println(ll[i]);
        }
        BoundingBox bb = new BoundingBox(ll);

        Request request = new Request(start, end, bb, requestReception);

        rc.response().putHeader("content-type", "application/json");

        JsonObject jsonResponse = getEvent(request);
        rc.response()
                .end(jsonResponse.encode());
    }


    private JsonObject getEvent(Request request) {
        String[] r = new String[1];
        databaseReader.getEvent(request, (t, result) -> {
            if(t != null) { LOGGER.error("DatabaseReader error: " + t.getMessage()); return; }
            r[0] = result;
        });
        if (r[0] == null) {
            r[0] = "{}";
        }
        return new JsonObject(r[0]);
    }
}