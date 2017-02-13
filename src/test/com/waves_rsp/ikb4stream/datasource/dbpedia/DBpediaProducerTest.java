package com.waves_rsp.ikb4stream.datasource.dbpedia;

import org.junit.Test;

import java.util.Arrays;

public class DBpediaProducerTest {

    @Test (expected = NullPointerException.class)
    public void checkNullProducer() {
        DBpediaProducerConnector dBpediaProducerConnector = DBpediaProducerConnector.getInstance();
        dBpediaProducerConnector.load(null);
    }

    @Test
    public void checkProducerResults() {
        DBpediaProducerConnector dBpediaProducerConnector = DBpediaProducerConnector.getInstance();
        dBpediaProducerConnector.load(dataProducer -> {
            //Do nothing
        });
    }

    @Test
    public void checkIllegalArgument() {
        try {
            DBpediaProducerConnector producerConnector = DBpediaProducerConnector.getInstance();
            producerConnector.load(dataProducer -> {
                //Do nothing
            });
        }catch (IllegalArgumentException err) {
            //Do nothing
        }
    }

    @Test
    public void checkThreadForDataProducer() {
        final DBpediaProducerConnector producerConnector = DBpediaProducerConnector.getInstance();
        Thread thread = new Thread(() -> {
           producerConnector.load(dataProducer -> {
               //Do nothing
           });
        });

        thread.start();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            //Do nothing
        }finally {
            thread.interrupt();
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void checkThreadsPoolForDataProducer() {
        Thread[] threads = new Thread[10];
        for (int i=0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                DBpediaProducerConnector producerConnector = DBpediaProducerConnector.getInstance();
                try {
                    producerConnector.load(dataProducer -> {
                        //Do nothing
                    });
                }catch (IllegalArgumentException err) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Arrays.stream(threads).forEach(Thread::start);

        try {
            Thread.sleep(300);
        }catch (InterruptedException err) {
            Arrays.stream(threads).forEach(Thread::interrupt);
        }
    }
}
