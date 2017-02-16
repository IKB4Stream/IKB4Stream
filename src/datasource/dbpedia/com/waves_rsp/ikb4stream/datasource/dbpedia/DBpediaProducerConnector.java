package com.waves_rsp.ikb4stream.datasource.dbpedia;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * Search rdf data from dbpedia service from a sparql query
 */
public class DBpediaProducerConnector implements IProducerConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaProducerConnector.class);
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance(DBpediaProducerConnector.class);

    /**
     * Instantiate DBpediaProducerConnector object from static method
     */
    public DBpediaProducerConnector() {
        //Do nothing
    }

    /**
     * Sent the sparql query to dbpedia service and load rdf data parsed into IDataProducer object.
     * The dbpedia service return a rdf response with nodes corresponding to the fields requested
     *
     * @param dataProducer
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while(!Thread.interrupted()) {
            QueryExecution qexec = null;
            try {
                String service = propertiesManager.getProperty("dbpedia.service");
                double latitudeMax = Double.valueOf(propertiesManager.getProperty("latitude.maximum"));
                double latitudeMin = Double.valueOf(propertiesManager.getProperty("latitude.minimum"));
                double longitudeMax = Double.valueOf(propertiesManager.getProperty("longitude.maximum"));
                double longitudeMin = Double.valueOf(propertiesManager.getProperty("longitude.minimum"));

                String resource = propertiesManager.getProperty("dbpedia.resource");
                int limit = Integer.valueOf(propertiesManager.getProperty("dbpedia.limit"));

                String query = "prefix db-owl: <http://dbpedia.org/ontology/>\n" +
                        "prefix url-resource: <http://fr.dbpedia.org/resource/>\n" +
                        "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
                        "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "prefix prop-fr: <http://fr.dbpedia.org/property/>\n" +
                        "prefix  dc: <http://purl.org/dc/elements/1.1/>\n" +
                        "select * where {\n" +
                        "   ?evenements rdf:type db-owl:Event .\n" +
                        "   ?evenements db-owl:wikiPageWikiLink url-resource:"+resource+" .\n" +
                        "   OPTIONAL {\n" +
                        "      ?evenements prop-fr:latitude ?latitude .\n" +
                        "      ?evenements prop-fr:longitude ?longitude .\n" +
                        "      ?evenements rdfs:comment ?description .\n" +
                        "      ?evenements dbo:startDate ?startDate .\n " +
                        "      ?evenements dbo:endDate ?endDate .\n" +
                        "      ?evenements rdfs:label ?label .\n" +
                        "      FILTER (\n" +
                        "         ?latitude >= "+latitudeMin+" && \n" +
                        "         ?latitude < "+latitudeMax+" &&       \n" +
                        "         ?longitude >= "+longitudeMin+" && \n" +
                        "         ?longitude < "+longitudeMax+" \n" +
                        "      )\n" +
                        "   }\n" +
                        "} LIMIT "+limit;

                Query request = QueryFactory.create(query);
                qexec = QueryExecutionFactory.sparqlService(service, request);
                ResultSet resultSet = qexec.execSelect();

                while(resultSet.hasNext()) {
                    QuerySolution qs = resultSet.nextSolution();
                    RDFNode latitudeNode = qs.get("latitude");
                    RDFNode longitudeNode = qs.get("longitude");
                    RDFNode descriptionNode = qs.get("description");
                    RDFNode startDateNode = qs.get("startDate");
                    RDFNode endDateNode = qs.get("endDate");
                    RDFNode labelNode = qs.get("label");
                    Event event = getEventFromRDFNodes(latitudeNode, longitudeNode, startDateNode, endDateNode, descriptionNode, labelNode);

                    pushIfValidEvent(dataProducer, event);
                }
            }catch (IllegalArgumentException err) {
                LOGGER.error("bad properties loaded.");
                throw new IllegalStateException(err.getMessage());
            }catch (IllegalStateException err) {
                LOGGER.error(err.getMessage());
                Thread.currentThread().interrupt();
                return;
            }catch (DateTimeParseException dtp) {
                LOGGER.error("bad date format given.");
                throw new IllegalStateException(dtp.getMessage());
            } finally{
                Thread.currentThread().interrupt();
                if(qexec != null) {
                    qexec.close();
                }
            }
        }
    }

    /**
     * Check if the RDFNodes are null or not
     *
     * @param rdfNodes
     * @return true if the rdf nodes are not null
     */
    private static boolean checkRDFNodes(RDFNode... rdfNodes) {
        return Arrays.stream(rdfNodes).anyMatch(Objects::nonNull);
    }

    /**
     * Parse specific RDFNodes in order to get GPS data with latitude and longitude
     *
     * @param latitudeNode
     * @param longitudeNode
     * @return latlong object with the coordinates
     */
    private static LatLong getLatlongFromRDFNodes(RDFNode latitudeNode, RDFNode longitudeNode) {
        double latitude = latitudeNode.asLiteral().getDouble();
        double longitude = longitudeNode.asLiteral().getDouble();
        return new LatLong(latitude, longitude);
    }

    /**
     * Parse specific nodes in order to create an Event object.
     * Check the date from startDate and endDate RDF nodes.
     * Retrieve the label and description from labelNode and descriptionNode.
     *
     * @param latitudeNode
     * @param longitudeNode
     * @param startDate
     * @param endDate
     * @param descriptionNode
     * @param labelNode
     * @return null if the checkRDFNodes is false or a ParseException has been caught, else the Even object
     */
    private static Event getEventFromRDFNodes(RDFNode latitudeNode, RDFNode longitudeNode, RDFNode startDate, RDFNode endDate, RDFNode descriptionNode, RDFNode labelNode) {
        if(checkRDFNodes(latitudeNode, longitudeNode, startDate, endDate, descriptionNode)) {
           try {
                LatLong latLong = getLatlongFromRDFNodes(latitudeNode, longitudeNode);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date start = dateFormat.parse(startDate.asLiteral().getString());
                Date end = dateFormat.parse(endDate.asLiteral().getString());
                String desc = descriptionNode.asLiteral().getString();
                String label = labelNode.asLiteral().getString();
                return new Event(latLong, start, end, desc, label);
            } catch (ParseException e) {
                LOGGER.error(e.getMessage());
                return null;
            }
        }

        return null;
    }

    private void pushIfValidEvent(IDataProducer dataProducer, Event event) {
        if(event != null) {
            dataProducer.push(event);
        }
    }
}
