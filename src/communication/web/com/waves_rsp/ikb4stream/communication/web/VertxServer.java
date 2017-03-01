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
import java.util.Objects;

/**
 * This server relies on Vertx, to handle the REST requests. It is instanciated by the web communication connector.
 * @author ikb4stream
 * @version 1.0
 * @see io.vertx.core.Verticle
 * @see io.vertx.core.AbstractVerticle
 */
public class VertxServer extends AbstractVerticle {
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxServer.class);
    /**
     * {@link IDatabaseReader} object to read data from database
     * @see VertxServer#getEvent(Request)
     */
    private static final IDatabaseReader DATABASE_READER = WebCommunication.databaseReader;

    /**
     * Server starting behaviour
     * @param fut Future that handles the start status
     * @throws NullPointerException if fut is null
     */
    @Override
    public void start(Future<Void> fut) {
        Objects.requireNonNull(fut);
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("X-PINGARUNER")
                .allowedHeader("Content-Type"));
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
        LOGGER.info("VertxServer started");
    }

    /**
     * Reads a request from a routing context, and attach the response to it. It requests the database
     * with DatabaseReader.
     * @param rc {@link RoutingContext}, which contains the request, and the response
     * @throws NullPointerException if rc is null
     */
    private void getAnomalies(RoutingContext rc) {
        try {
            LOGGER.info("Received web request: {}", rc.getBodyAsJson());
            Request request = parseRequest(rc.getBodyAsJson());
            rc.response().putHeader("content-type", "application/json");
            JsonObject jsonResponse = getEvent(request);
            rc.response()
                    .end(jsonResponse.encode());
        } catch (DecodeException e) {
            LOGGER.info("Received an invalid format request");
            LOGGER.debug("DecodeException: {}", e);
            rc.fail(400);
        }
        Objects.requireNonNull(rc);
        LOGGER.info("Received web request: {}", rc.getBodyAsJson());
        Request request = parseRequest(rc.getBodyAsJson());
        rc.response().putHeader("content-type", "application/json");
        JsonObject jsonResponse = getEvent(request);
        rc.response()
                .end(jsonResponse.encode());
    }

    /**
     * Convert a request from Json to Java object
     * @param jsonRequest {@link JsonObject} json formatted request
     * @return {@link Request}
     * @throws NullPointerException if jsonRequest is null
     */
    private Request parseRequest(JsonObject jsonRequest) {
        Objects.requireNonNull(jsonRequest);
        Date start = new Date(jsonRequest.getLong("start"));
        Date end = new Date(jsonRequest.getLong("end"));
        Date requestReception = new Date(jsonRequest.getLong("requestReception"));
        JsonObject jsonbb = jsonRequest.getJsonObject("boundingBox");
        JsonArray latlongs = jsonbb.getJsonArray("points");
        LatLong[] ll = new LatLong[latlongs.size()];
        for (int i=0; i < latlongs.size(); i++) {
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
     * @param request {@link Request} the user web request
     * @return {@link JsonObject} extracted from the database
     * @see VertxServer#DATABASE_READER
     */
    private JsonObject getEvent(Request request) {
        String[] r = new String[1];
        DATABASE_READER.getEvent(request, (t, result) -> {
            if(t != null) {
                LOGGER.error("DatabaseReader error: " + t.getMessage()); return;
            }
            r[0] = result;
        });
        JsonObject response;
        if (r[0] == null) {
            LOGGER.info("No event found");
            response = new JsonObject("{\"events\": []}");
        } else {
            LOGGER.info("Found events: {}", r[0]);
            response = new JsonObject("{\"events\":" + r[0] +"}");
        }
        return response;
    }
}