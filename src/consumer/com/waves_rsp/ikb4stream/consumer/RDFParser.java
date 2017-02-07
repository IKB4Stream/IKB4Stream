package com.waves_rsp.ikb4stream.consumer;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ikb4stream on 07/02/17.
 * Parser class to parse RDF file
 */
public class RDFParser {

    /**
     * @param anomalyFileName the rdf anomaly file's name
     * @return an AnomalyNode object which contains information about latitude, longitude and date
     */
    public static AnomalyRequest parse(String anomalyFileName) {
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

    private static AnomalyRequest getDataFromMap(Map<String, Object> map) {
        Objects.requireNonNull(map);
        XSDDateTime dateTime = (XSDDateTime) map.get("at");
        Date date = new Date();
        long timeMilliseconds = dateTime.asCalendar().getTimeInMillis();
        date.setTime(timeMilliseconds);
        float minLatitude = (float) map.get("hasMinLatitude");
        float maxLatitude = (float) map.get("hasMaxLatitude");
        float minLongitude = (float) map.get("hasMinLongitude");
        float maxLongitude = (float) map.get("hasMaxLongitude");
        return new AnomalyRequest(date, minLatitude, maxLatitude, minLongitude, maxLongitude);
    }

}