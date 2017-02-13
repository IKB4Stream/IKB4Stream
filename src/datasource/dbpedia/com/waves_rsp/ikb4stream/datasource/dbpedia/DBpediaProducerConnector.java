package com.waves_rsp.ikb4stream.datasource.dbpedia;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DBpediaProducerConnector implements IProducerConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaProducerConnector.class);
    private final PropertiesManager propertiesManager = PropertiesManager.getInstance();

    private DBpediaProducerConnector() {

    }

    public static DBpediaProducerConnector getInstance() {
        return new DBpediaProducerConnector();
    }


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
                        "select * where {\n" +
                        "   ?evenements rdf:type db-owl:Event .\n" +
                        "   ?evenements prop-fr:périodicité \"annuelle\"@fr.\n" +
                        "   ?evenements db-owl:wikiPageWikiLink url-resource:"+resource+" .\n" +
                        "   OPTIONAL {\n" +
                        "      ?evenements prop-fr:latitude ?latitude .\n" +
                        "      ?evenements prop-fr:longitude ?longitude .\n" +
                        "      ?evenements prop-fr:titre ?title .\n" +
                        "      FILTER (\n" +
                        "         ?latitude >= "+latitudeMin+" &&\n" +
                        "         ?latitude < "+latitudeMax+" &&       \n" +
                        "         ?longitude >= "+longitudeMin+" &&\n" +
                        "         ?longitude < "+longitudeMax+"\n" +
                        "      )\n" +
                        "   }\n" +
                        "} LIMIT "+limit+"";

                Query request = QueryFactory.create(query);
                qexec = QueryExecutionFactory.sparqlService(service, request);
                ResultSet resultSet = qexec.execSelect();
                Model model = ModelFactory.createDefaultModel();

                Map<String, Object> map = new HashMap<>();

                Resource resources = ResultSetFormatter.asRDF(model, resultSet);
                resources.listProperties().forEachRemaining(statement -> {
                    RDFNode rdfNode = statement.getObject();
                    LOGGER.info(rdfNode.toString());
                });

                map.forEach((k,v) -> System.out.println(k+", "+v));
            }catch (IllegalArgumentException err) {
                LOGGER.error("bad properties loaded.");
                return;
            }catch (IllegalStateException err) {
                LOGGER.error(err.getMessage());
                return;
            }finally {
                Thread.currentThread().interrupt();
                if(qexec != null) {
                    qexec.close();
                }
            }
        }
    }
}
