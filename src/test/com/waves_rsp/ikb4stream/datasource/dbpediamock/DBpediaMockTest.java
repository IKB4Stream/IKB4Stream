package com.waves_rsp.ikb4stream.datasource.dbpediamock;

import com.hp.hpl.jena.query.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.IntStream;

public class DBpediaMockTest {

    @Test
    public void createValidInstance() {
        new DBpediaMock();
    }

    @Test (expected = NullPointerException.class)
    public void checkNullDataProducer() {
        DBpediaMock dBpediaMock = new DBpediaMock();
        dBpediaMock.load(null);
    }

    @Test
    public void checkIllegalPropertiesLoaded() {
        try {
            DBpediaMock dBpediaMock = new DBpediaMock();
            dBpediaMock.load(dataProducer -> {
                //Do nothing
            });
        }catch (IllegalArgumentException e) {
            //Do nothing
        }
    }

    @Test
    public void badFilePathLoaded() {
        try {
            DBpediaMock dBpediaMock = new DBpediaMock();
            dBpediaMock.load(dataProducer -> {
               //Do nothing
            });
        }catch (IllegalArgumentException e) {
            //Do nothing
        }
    }

    @Test
    public void checkMock() {
        DBpediaMock dBpediaMock = new DBpediaMock();
        dBpediaMock.load(dataProducer -> {
            //Do nothing
        });
    }

    @Test
    public void checkPerfWithPoolThread() {
        Thread[] threads = new Thread[10];
        IntStream.range(0, threads.length).forEach(i -> {
            threads[i] = new Thread(() -> {
                try {
                    DBpediaMock dBpediaMock = new DBpediaMock();
                    dBpediaMock.load(dataProducer -> {
                        //Do nothing
                    });
                }catch (IllegalArgumentException e) {
                    //Do nothing
                }
            });
        });

        Arrays.stream(threads).forEach(Thread::start);

        try {
            Thread.sleep(100);
            Arrays.stream(threads).forEach(Thread::interrupt);
        }catch (InterruptedException e) {
            //Do nothing
        }
    }

    @Ignore
    @Test
    public void createDbpediaTestFile() {
        String query = "prefix db-owl: <http://dbpedia.org/ontology/>\n" +
                "prefix url-resource: <http://fr.dbpedia.org/resource/>\n" +
                "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix prop-fr: <http://fr.dbpedia.org/property/>\n" +
                "prefix  dc: <http://purl.org/dc/elements/1.1/>\n" +
                "select * where {\n" +
                "   ?evenements rdf:type db-owl:Event .\n" +
                "   OPTIONAL {\n" +
                "      ?evenements prop-fr:latitude ?latitude .\n" +
                "      ?evenements prop-fr:longitude ?longitude .\n" +
                "      ?evenements rdfs:comment ?description .\n" +
                "      ?evenements dbo:startDate ?startDate .\n " +
                "      ?evenements dbo:endDate ?endDate .\n" +
                "      ?evenements rdfs:label ?label .\n" +
                "      FILTER (\n" +
                "         ?latitude >= "+48.7374199768179+" && \n" +
                "         ?latitude < "+48.8781161065379+" &&       \n" +
                "         ?longitude >= "+1.96944422049222+" && \n" +
                "         ?longitude < "+2.22459235221894+" \n" +
                "      )\n" +
                "   }\n" +
                "} LIMIT "+1000;

        Query request = QueryFactory.create(query);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", request);
        ResultSet resultSet = qexec.execSelect();

        try {
            OutputStream outputStream = new FileOutputStream("resources/datasource/dbpediamock/dbpedia_data_"+resultSet.getResultVars().size()+".json");
            ResultSetFormatter.outputAsJSON(outputStream, resultSet);
        } catch (FileNotFoundException e) {
            //Do nothing
        }finally {
            qexec.close();
        }
    }
}
