package com.waves_rsp.ikb4stream.communication.kafka;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ikb4stream on 07/02/17.
 * Parser class to parseFile RDF file
 */
public class RDFParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDFParser.class);

    private RDFParser() {

    }

    /**
     * @param anomalyFileName the rdf anomaly file's name
     * @return an AnomalyNode object which contains information about latitude, longitude and date
     */
    public static AnomalyRequest parseFile(String anomalyFileName) {
        Objects.requireNonNull(anomalyFileName);
        Model model = FileManager.get().loadModel(anomalyFileName);

        Map<String, Object> map = new HashMap<>();
        model.listStatements().forEachRemaining(statement -> {
            RDFNode rdfNode = statement.getObject();
            if(rdfNode.isLiteral()) {
                map.put(statement.getPredicate().getLocalName(), statement.getObject().asLiteral().getValue());
            }
        });

        model.close();
        return getDataFromMap(map);
    }

    public static AnomalyRequest parse(String input) {
        Objects.requireNonNull(input);

        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(input.getBytes()), null, "TURTLE");

        Map<String, Object> map = new HashMap<>();
        model.listStatements().forEachRemaining(statement -> {
            RDFNode rdfNode = statement.getObject();
            if(rdfNode.isLiteral()) {
                map.put(statement.getPredicate().getLocalName(), statement.getObject().asLiteral().getValue());
            }
        });

        model.close();
        return getDataFromMap(map);
    }

    private static AnomalyRequest getDataFromMap(Map<String, Object> map) {
        try {
            Objects.requireNonNull(map);
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
            LOGGER.error("Error occurred during the deserialization of RDF: " + e);
            return null;
        }
    }

}