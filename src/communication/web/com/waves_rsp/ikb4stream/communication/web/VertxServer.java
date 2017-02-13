package com.waves_rsp.ikb4stream.communication.web;

import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.model.Request;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxServer extends AbstractVerticle {
    private final Logger LOGGER = LoggerFactory.getLogger(VertxServer.class);
    private IDatabaseReader databaseReader;

    @Override
    public void start(Future<Void> fut) {
        if (!(config().getValue("DatabaseReader") instanceof IDatabaseReader)) {
            LOGGER.error("Can't reach DatabaseReader");
            fut.fail("Can't reach DatabaseReader");
            return;
        }

        databaseReader = (IDatabaseReader) config().getValue("database");
        // Create a router object.
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

    private void getAnomalies(RoutingContext rc) {
        Request request = Json.decodeValue(rc.getBodyAsString(), Request.class);

        rc.response()
                .putHeader("content-type", "application/json");

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
        return new JsonObject(r[0]);
    }
}