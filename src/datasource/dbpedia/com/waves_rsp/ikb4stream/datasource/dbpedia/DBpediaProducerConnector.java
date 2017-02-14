package com.waves_rsp.ikb4stream.datasource.dbpedia;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.xml.internal.bind.v2.runtime.property.PropertyFactory;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Search rdf data from dbpedia service from a sparql query
 */
public class DBpediaProducerConnector implements IProducerConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaProducerConnector.class);
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance();

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
                int limit = Integer.valueOf(propertiesManager.getProperty("dbpedia.result.limit"));

                String query = "prefix db-owl: <http://dbpedia.org/ontology/>\n" +
                        "prefix url-resource: <http://fr.dbpedia.org/resource/>\n" +
                        "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "prefix prop-fr: <http://fr.dbpedia.org/property/>\n" +
                        "select * where {\n" +
                        "   ?evenements rdf:type db-owl:Event .\n" +
                        "   ?evenements db-owl:wikiPageWikiLink url-resource:"+resource+" .\n" +
                        "   OPTIONAL {\n" +
                        "      ?evenements prop-fr:latitude ?latitude .\n" +
                        "      ?evenements prop-fr:longitude ?longitude .\n" +
                        "      ?evenements prop-fr:périodicité \"annuelle\"@fr .\n" +
                        "      ?evenements prop-fr:titre ?title .\n" +
                        "      ?evenements prop-fr:comments ?descritpion .\n" +
                        "      FILTER (\n" +
                        "         ?latitude >= "+latitudeMin+" &&\n" +
                        "         ?latitude < "+latitudeMax+" &&       \n" +
                        "         ?longitude >= "+longitudeMin+" &&\n" +
                        "         ?longitude < "+longitudeMax+"\n" +
                        "      )\n" +
                        "   }\n" +
                        "} LIMIT "+limit;

                Query request = QueryFactory.create(query);
                qexec = QueryExecutionFactory.sparqlService(service, request);
                ResultSet resultSet = qexec.execSelect();

                Map<String, Object> map = new HashMap<>();
                ResultSetFormatter.toModel(resultSet).listStatements().forEachRemaining(statement -> {
                    RDFNode rdfNode = statement.getObject();
                    if(rdfNode.isLiteral()) {
                        map.put(statement.getObject().asLiteral().getString(), statement.getObject().toString());
                    }
                });

                map.values().forEach(value -> LOGGER.info(value.toString()));

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

    private static LatLong getLatLongFromMap(Map<String, Object> map) {
        double latitude = (double) map.get("latitude");
        double longitude = (double) map.get("longitude");
        return new LatLong(latitude, longitude);
    }

    private static Event getEventFromMap(LatLong latLong, Map<String, Object> map) {
        String description = (String) map.get("description");
        String source = (String) map.get("title");
        Date date = Date.from(Instant.now());
        return new Event(latLong, date, date, description, source);
    }
}
