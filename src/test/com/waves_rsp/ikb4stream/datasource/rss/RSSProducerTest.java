package com.waves_rsp.ikb4stream.datasource.rss;

import org.junit.Test;

import java.net.MalformedURLException;

import static java.lang.Thread.sleep;

public class RSSProducerTest {

    @Test(expected = NullPointerException.class)
    public void nullDataProducerRegistration() throws MalformedURLException {
        RSSProducerConnector rsss = new RSSProducerConnector("http://www.lemonde.fr/rss/une.xml");
        rsss.load(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyStringRSSProducer() throws MalformedURLException {
        new RSSProducerConnector("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void malformedURLRSSProducer() throws MalformedURLException {
        new RSSProducerConnector("malformedurl");
    }

    /**
     * This test is supposed to print found event on an RSS flux. Cause of the architecture,
     * we can't test the DataProducer
     *
     * @throws MalformedURLException
     */
    @Test
    public void testFeedRSSProducer() throws MalformedURLException {
        RSSProducerConnector rsspc = new RSSProducerConnector("http://www.geonames.org/recent-changes.xml");
        rsspc.load(System.out::println);

        /*wait for the thread to request rss*/
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
