/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.waves_rsp.ikb4stream.communication.web;

import com.waves_rsp.ikb4stream.consumer.database.DatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.DatabaseReaderCallback;
import com.waves_rsp.ikb4stream.core.communication.IDatabaseReader;
import com.waves_rsp.ikb4stream.core.communication.model.BoundingBox;
import com.waves_rsp.ikb4stream.core.communication.model.Request;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * This server relies on Vertx, to handle the REST requests. It is instanciated by the web communication connector.
 */
public class VertxServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxServer.class);
    private final IDatabaseReader databaseReader = WebCommunication.databaseReader;

    /**
     * Server starting behaviour
     *
     * @param fut Future that handles the start status
     */
    @Override
    public void start(Future<Void> fut) {
        Router router = Router.router(vertx);

        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("X-PINGARUNER")
                .allowedHeader("Content-Type")
        );

        router.route("/anomaly*").handler(BodyHandler.create()); // enable reading of request's body

        router.get("/anomaly").handler(this::getAnomalies);
        router.post("/anomaly").handler(this::getAnomalies);
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
        LOGGER.info("VertxServer started");
    }

    /**
     * Reads a request from a routing context, and attach the response to it. It requests the database with DatabaseReader.
     *
     * @param rc RoutingContext, which contains the request, and the response
     */
    private void getAnomalies(RoutingContext rc) {
        Request request;
        try {
            // curl http://localhost:8081/anomaly -X POST -H "Content-Type: application/json" -d '{"start":1487004295000,"end":1487004295000, "boundingBox":{"points": [{"latitude":10, "longitude":20},{"latitude":15, "longitude":25},{"latitude":25, "longitude":30},{"latitude":10, "longitude":20}]}, "requestReception":1487004295000}'
            LOGGER.info("Received web request: {}", rc.getBodyAsJson());
            request = parseRequest(rc.getBodyAsJson());
        } catch (DecodeException | NullPointerException e) {
            LOGGER.info("Received an invalid format request : {} ", e.getMessage());
            LOGGER.debug("DecodeException: {}", e);
            rc.fail(400);
            return;
        }

        rc.response().putHeader("content-type", "application/json");

        getEvent(request, (t, result) -> {
            if (t != null) {
                LOGGER.error("DatabaseReader error: " + t.getMessage());
                return;
            }
            LOGGER.info("Found events: {}", result);
            JsonObject response = new JsonObject("{\"events\":" + result + "}");
            rc.response().end(response.encode());
        });
    }

    /**
     * Convert a request from Json to Java object
     *
     * @param jsonRequest json formatted request
     * @return Java Request
     */
    private Request parseRequest(JsonObject jsonRequest) {
        Date start = new Date(jsonRequest.getLong("start"));
        Date end = new Date(jsonRequest.getLong("end"));
        Date requestReception = new Date(jsonRequest.getLong("requestReception"));

        JsonObject jsonbb = jsonRequest.getJsonObject("boundingBox");
        JsonArray latlongs = jsonbb.getJsonArray("points");
        LatLong[] ll = new LatLong[latlongs.size()];
        for (int i = 0; i < latlongs.size(); i++) {
            JsonObject latlong = latlongs.getJsonObject(i);
            double latitude = latlong.getDouble("latitude");
            double longitude = latlong.getDouble("longitude");
            ll[i] = new LatLong(latitude, longitude);
        }
        BoundingBox bb = new BoundingBox(ll);

        return new Request(start, end, bb, requestReception);
    }

    /**
     * Retrieve an event from database
     *
     * @param request the user web request
     * @return a JSon Object extracted from the database
     */
    private void getEvent(Request request, DatabaseReaderCallback databaseReaderCallback) {
        databaseReader.getEvent(request, databaseReaderCallback);
    }

    public static void main(String[] args) {

        WebCommunication webCommunication = new WebCommunication();
        webCommunication.start(DatabaseReader.getInstance());

    }


}