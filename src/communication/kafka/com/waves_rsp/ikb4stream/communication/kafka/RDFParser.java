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

package com.waves_rsp.ikb4stream.communication.kafka;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.waves_rsp.ikb4stream.core.communication.model.BoundingBox;
import com.waves_rsp.ikb4stream.core.communication.model.Request;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Parser class to parse stream RDF
 *
 * @author ikb4stream
 * @version 1.0
 */
public class RDFParser {
    /**
     * Logger used to log all information in this module
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RDFParser.class);

    /**
     * Private constructor, this class provides only static method
     */
    private RDFParser() {

    }

    /**
     * Parse RDF input as string
     *
     * @param input RDF values as String
     * @return an {@link Request} object which contains information about latitude, longitude and date
     * @throws IllegalStateException if RDF is not literal
     * @throws NullPointerException  if input is null
     */
    public static Request parse(String input) {
        Objects.requireNonNull(input);
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(input.getBytes()), null, "TURTLE");

        Map<String, Object> map = new HashMap<>();
        model.listStatements().forEachRemaining(statement -> {
            RDFNode rdfNode = statement.getObject();
            if (rdfNode.isLiteral()) {
                try {
                    map.put(statement.getPredicate().getLocalName(), statement.getObject().asLiteral().getValue());
                } catch (Exception e) {
                    LOGGER.error("RDF statement is not literal");
                    throw new IllegalStateException(e.getMessage());
                }
            }
        });

        model.close();
        return getDataFromMap(map);
    }

    /**
     * Get an AnomalyRequest from Map< String, Object >
     *
     * @param map {@link Request} represented as Map Object
     * @return {@link Request} object which contains information about latitude, longitude and date
     * @throws NullPointerException     if map is null
     * @throws IllegalArgumentException if map doesn't have needed values
     */
    private static Request getDataFromMap(Map<String, Object> map) {
        try {
            Objects.requireNonNull(map);
            checkValid(map);
            XSDDateTime startdt = (XSDDateTime) map.get("start");
            XSDDateTime enddt = (XSDDateTime) map.get("end");
            Date start = new Date();
            Date end = new Date();
            start.setTime(startdt.asCalendar().getTimeInMillis());
            end.setTime(enddt.asCalendar().getTimeInMillis());
            float minLatitude = (float) map.get("hasMinLatitude");
            float maxLatitude = (float) map.get("hasMaxLatitude");
            float minLongitude = (float) map.get("hasMinLongitude");
            float maxLongitude = (float) map.get("hasMaxLongitude");

            return new Request(
                    start,
                    end,
                    new BoundingBox(new LatLong[]{
                            new LatLong(minLatitude, minLongitude),
                            new LatLong(maxLatitude, minLongitude),
                            new LatLong(maxLatitude, maxLongitude),
                            new LatLong(minLatitude, maxLongitude),
                            new LatLong(minLatitude, minLongitude),
                    }),
                    Date.from(Instant.now()));
        } catch (NullPointerException e) {
            LOGGER.error("Error occurred during the deserialization of RDF: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if map has needed values
     *
     * @param map Map to check
     * @throws IllegalArgumentException If map doesn't have all information
     */
    private static void checkValid(Map<String, Object> map) {
        if (map.get("start") == null) {
            LOGGER.warn("start is null");
            throw new IllegalArgumentException("start is null");
        } else if (map.get("end") == null) {
            LOGGER.warn("end is null");
            throw new IllegalArgumentException("end is null");
        } else {
            checkPositionValid(map);
        }
    }

    /**
     * Check position valid
     *
     * @param map Map to check
     * @throws IllegalArgumentException if map is invalid
     */
    private static void checkPositionValid(Map<String, Object> map) {
        if (map.get("hasMinLatitude") == null) {
            LOGGER.warn("hasMinLatitude is null");
            throw new IllegalArgumentException("hasMinLatitude is null");
        } else if (map.get("hasMaxLatitude") == null) {
            LOGGER.warn("hasMaxLatitude is null");
            throw new IllegalArgumentException("hasMaxLatitude is null");
        } else if (map.get("hasMinLongitude") == null) {
            LOGGER.warn("hasMinLongitude is null");
            throw new IllegalArgumentException("hasMinLongitude is null");
        } else if (map.get("hasMaxLongitude") == null) {
            LOGGER.warn("hasMaxLongitude is null");
            throw new IllegalArgumentException("hasMaxLongitude is null");
        }
    }
}