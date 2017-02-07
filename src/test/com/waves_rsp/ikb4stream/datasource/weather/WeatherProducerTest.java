package com.waves_rsp.ikb4stream.datasource.weather;

import org.junit.Test;

import java.net.MalformedURLException;


public class WeatherProducerTest {

    @Test (expected = NullPointerException.class)
    public void checkWeatherNullURL() throws MalformedURLException {
        WeatherProducerConnector connector = new WeatherProducerConnector(null);
    }

    @Test
    public void illegalUrlTest() {
        try {
            WeatherProducerConnector connector = new WeatherProducerConnector("htts://qojdiojqsoidjq");
        } catch (MalformedURLException e) {
            //Do nothing
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void checkEmptyURl() {
        try {
            new WeatherProducerConnector("");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkWeatherData() throws MalformedURLException, InterruptedException {
        WeatherProducerConnector connector = new WeatherProducerConnector("http://www.lemonde.fr/paris/rss_full.xml");
        connector.load(System.out::println);
        Thread.sleep(1000);
    }
}
