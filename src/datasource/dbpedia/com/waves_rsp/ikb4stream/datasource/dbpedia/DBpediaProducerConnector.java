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

package com.waves_rsp.ikb4stream.datasource.dbpedia;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.waves_rsp.ikb4stream.core.datasource.model.IDataProducer;
import com.waves_rsp.ikb4stream.core.datasource.model.IProducerConnector;
import com.waves_rsp.ikb4stream.core.metrics.MetricsLogger;
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
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(DBpediaProducerConnector.class, "resources/datasource/dbpedia/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaProducerConnector.class);
    private static final MetricsLogger METRICS_LOGGER = MetricsLogger.getMetricsLogger();
    private final String source;
    private final String service;
    private final double latitudeMax;
    private final double latitudeMin ;
    private final double longitudeMax;
    private final double longitudeMin;
    private final String resource;
    private final int limit;
    private final long sleepTime;


    /**
     * Instantiate DBpediaProducerConnector object from static method
     */
    public DBpediaProducerConnector() {
        try {
            source = PROPERTIES_MANAGER.getProperty("dbpedia.source");
            service = PROPERTIES_MANAGER.getProperty("dbpedia.service");
            latitudeMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("dbpedia.latitude.maximum"));
            latitudeMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("dbpedia.latitude.minimum"));
            longitudeMax = Double.valueOf(PROPERTIES_MANAGER.getProperty("dbpedia.longitude.maximum"));
            longitudeMin = Double.valueOf(PROPERTIES_MANAGER.getProperty("dbpedia.longitude.minimum"));
            resource = PROPERTIES_MANAGER.getProperty("dbpedia.resource");
            sleepTime = Long.valueOf(PROPERTIES_MANAGER.getProperty("dbpedia.sleep_time"));
            limit = Integer.valueOf(PROPERTIES_MANAGER.getProperty("dbpedia.limit"));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Bad properties loaded: {}", e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Sent the sparql query to dbpedia service and load rdf data parsed into IDataProducer object.
     * The dbpedia service return a rdf response with nodes corresponding to the fields requested
     *
     * @param dataProducer contains data queue
     */
    @Override
    public void load(IDataProducer dataProducer) {
        Objects.requireNonNull(dataProducer);
        while(!Thread.interrupted()) {
            QueryExecution qexec = null;
            long start = System.currentTimeMillis();
            try {
                String query = "prefix db-owl: <http://dbpedia.org/ontology/>\n" +
                        "prefix url-resource: <http://fr.dbpedia.org/resource/>\n" +
                        "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
                        "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "prefix prop-fr: <http://fr.dbpedia.org/property/>\n" +
                        "prefix  dc: <http://purl.org/dc/elements/1.1/>\n" +
                        "select * where {\n" +
                        "   ?evenements rdf:type db-owl:Event .\n" +
                        "   ?evenements db-owl:wikiPageWikiLink url-resource:" + resource + " .\n" +
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
                    Event event = getEventFromRDFNodes(latitudeNode, longitudeNode, startDateNode, endDateNode, descriptionNode, source);
                    pushIfValidEvent(dataProducer, event, start);
                }
                Thread.sleep(this.sleepTime);
            } catch (IllegalStateException err) {
                LOGGER.error("The current query can't be executed: {}", err);
                Thread.currentThread().interrupt();
                return;
            }catch (DateTimeParseException dtp) {
                LOGGER.error("bad date format given: ", dtp);
                throw new IllegalStateException(dtp.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally{
                if(qexec != null) {
                    qexec.close();
                }
            }
        }
    }

    @Override
    public boolean isActive() {
        try {
            return Boolean.valueOf(PROPERTIES_MANAGER.getProperty("dbpedia.enable"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Can't parse the specific boolean value: ", e);
            return true;
        }
    }

    /**
     * Check if the RDFNodes are null or not
     *
     * @param rdfNodes RDF nodes to check
     * @return true if the rdf nodes are not null
     */
    private static boolean checkRDFNodes(RDFNode... rdfNodes) {
        return Arrays.stream(rdfNodes).anyMatch(Objects::nonNull);
    }

    /**
     * Parse specific RDFNodes in order to get GPS data with latitude and longitude
     *
     * @param latitudeNode RDF formatted latitude node
     * @param longitudeNode RDF formatted longitude node
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
     * @param latitudeNode RDF formatted latitude node
     * @param longitudeNode RDF formatted longitude node
     * @param startDate filter start date
     * @param endDate filter end date
     * @param descriptionNode contains the event
     * @param label tags
     * @return null if the checkRDFNodes is false or a ParseException has been caught, else the Even object
     */
    private static Event getEventFromRDFNodes(RDFNode latitudeNode, RDFNode longitudeNode, RDFNode startDate, RDFNode endDate, RDFNode descriptionNode, String label) {
        if(checkRDFNodes(latitudeNode, longitudeNode, startDate, endDate, descriptionNode)) {
            try {
                LatLong latLong = getLatlongFromRDFNodes(latitudeNode, longitudeNode);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date start = dateFormat.parse(startDate.asLiteral().getString());
                Date end = dateFormat.parse(endDate.asLiteral().getString());
                String desc = descriptionNode.asLiteral().getString();
                return new Event(latLong, start, end, desc, label);
            } catch (ParseException e) {
                LOGGER.error(e.getMessage());
                return null;
            }
        }

        return null;
    }

    /**
     * Push events if scored
     * @param dataProducer contains the data queue
     * @param event the event to push
     * @param start process start time, used for the metrics module
     */
    private void pushIfValidEvent(IDataProducer dataProducer, Event event, long start) {
        if(event != null) {
            dataProducer.push(event);
            long end = System.currentTimeMillis();
            long result = end - start;
            METRICS_LOGGER.log("time_process_"+this.source, result);
        }
    }
}
