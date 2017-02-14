package com.waves_rsp.ikb4stream.communication.kafka;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Parser class to parse stream RDF
 */
public class RDFParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDFParser.class);

    /**
     * Private constructor, this class provides only static method
     */
    private RDFParser() {

    }

    /**
     * Parse RDF input as string
     * @param input RDF values as String
     * @return an {@link AnomalyRequest} object which contains information about latitude, longitude and date
     * @throws IllegalStateException If RDF is not literal
     */
    public static AnomalyRequest parse(String input) {
        Objects.requireNonNull(input);
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(input.getBytes()), null, "TURTLE");

        Map<String, Object> map = new HashMap<>();
        model.listStatements().forEachRemaining(statement -> {
            RDFNode rdfNode = statement.getObject();
            if(rdfNode.isLiteral()) {
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
     * @param map {@link AnomalyRequest} represented as Map Object
     * @return {@link AnomalyRequest} object which contains information about latitude, longitude and date
     * @throws NullPointerException if {@param map} is null
     * @throws IllegalArgumentException if {@param map} doesn't have needed values
     */
    private static AnomalyRequest getDataFromMap(Map<String, Object> map) {
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
            return new AnomalyRequest(start, end, minLatitude, maxLatitude, minLongitude, maxLongitude);
        } catch (NullPointerException e) {
            LOGGER.error("Error occurred during the deserialization of RDF: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if {@param map} has needed values
     * @param map Map to check
     * @throws IllegalArgumentException If {@param map} doesn't have all information
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
     * @param map Map to check
     * @throws IllegalArgumentException if {@param map} is invalid
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