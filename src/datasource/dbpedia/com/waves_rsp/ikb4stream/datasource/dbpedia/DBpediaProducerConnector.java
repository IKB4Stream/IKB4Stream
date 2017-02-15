package com.waves_rsp.ikb4stream.datasource.dbpedia;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Search rdf data from dbpedia service from a sparql query
 */
public class DBpediaProducerConnector implements IProducerConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaProducerConnector.class);
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance(DBpediaProducerConnector.class);

    /**
     * Instantiate from static method
     */
    private DBpediaProducerConnector() {

    }

    public static DBpediaProducerConnector getInstance() {
        return new DBpediaProducerConnector();
    }

    /**
     * Sent the sparql query to dbpedia service and load rdf data parsed into IDataProducer object
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
                        "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "prefix prop-fr: <http://fr.dbpedia.org/property/>\n" +
                        "prefix  dc: <http://purl.org/dc/elements/1.1/> \n" +
                        "select * where {\n" +
                        "   ?evenements rdf:type db-owl:Event .\n" +
                        "   ?evenements db-owl:wikiPageWikiLink url-resource:"+resource+" .\n" +
                        "   OPTIONAL {\n" +
                        "      ?evenements prop-fr:latitude ?latitude .\n" +
                        "      ?evenements prop-fr:longitude ?longitude .\n" +
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

                /*
                final Map<String, Object> map = new HashMap<>();
                ResultSetFormatter.toModel(resultSet).listStatements().forEachRemaining(statement -> {
                    RDFNode rdfNode = statement.getObject();
                    if(rdfNode.isResource()) {
                        rdfNode.asResource().listProperties().forEachRemaining(property -> {
                            RDFNode propertyNode = property.getObject();
                            map.put(propertyNode.toString(), property.getString());
                        });
                    }
                });
                    */

            }catch (IllegalArgumentException err) {
                LOGGER.error("bad properties loaded.");
                return;
            }catch (IllegalStateException err) {
                LOGGER.error(err.getMessage());
                return;
            }catch (DateTimeParseException dtp) {
                LOGGER.error("bad date format given.");
            } finally{
                Thread.currentThread().interrupt();
                if(qexec != null) {
                    qexec.close();
                }
            }
        }
    }

    private static Event getEventFromMap(Map<String, Object> map) {
        double latitude = (double) map.get("latitude");
        double longitude = (double) map.get("longitude");
        LatLong latLong = new LatLong(latitude, longitude);
        String description = (String) map.get("description");
        String source = (String) map.get("title");
        Date date = Date.from(Instant.now());
        return new Event(latLong, date, date, description, source);
    }
}
