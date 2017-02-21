package com.waves_rsp.ikb4stream.core.metrics;

import org.influxdb.dto.Point;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class MetricsTest {
    private final MetricsLogger logger = MetricsLogger.getMetricsLogger();

    @Before
    public void init() {
        //Do nothing
    }

    @Test
    public void checkConnexionServiceInfluxEnable() {
        logger.log("toto", "tata");
    }

    @Test (expected = NullPointerException.class)
    public void addNullData() {
        logger.log(null, null);
    }

    @Test
    public void addValidData() {
        logger.log("test_reports", "test metrics");
    }

    @Test
    public void checkTimeWhileAddedData() {
        long start = System.currentTimeMillis();
        logger.log("test_reports", "test metrics1");
        logger.log("test_reports", "test metrics2");
        logger.log("test_reports", "test metrics3");
        logger.log("test_reports", "test metrics4");
        long end = System.currentTimeMillis();
        long result = end - start;
        Assert.assertTrue(result < 500);
    }

    @Test
    public void pushManyPointsIfConnexionEnabled() {
        try {
            long start = System.currentTimeMillis();
            IntStream.range(0, 50).forEach(i -> logger.log("test_metrics", "event_test", "data"+i));
            long end = System.currentTimeMillis();
            long result = end - start;
            System.out.println("result : "+result);
        }catch (NullPointerException err) {
            //Do nothing
        }
    }

    @Test
    public void checkTimeWhileAddedPointData() {
        final int max = 10;
        Point[] points = new Point[max];
        IntStream.range(0, max).forEach(i -> {
            points[i] = Point.measurement("test_reports").addField("test_reports", "metrics"+i).build();
        });

        long start = System.currentTimeMillis();
        logger.log(points);
        long end = System.currentTimeMillis();
        long result = end - start;

        Assert.assertTrue(result < 200);
    }

    @Test
    public void checkTimeWhileAddedPointDataFromThreadPool() {
        final int max = 10;
        Thread[] threads = new Thread[max];

        long start = System.currentTimeMillis();
        final Point[] points = new Point[max];
        IntStream.range(0, points.length).forEach(i -> {
            points[i] = Point.measurement("test_metrics").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                                                                     .addField("event_test", "data"+i).build();
        });

        IntStream.range(0, max).forEach(i -> {
            threads[i] = new Thread(() -> {
                MetricsLogger logger = MetricsLogger.getMetricsLogger();
                logger.log(points);
            });
        });

        Arrays.stream(threads).forEach(Thread::start);
        long end = System.currentTimeMillis();
        long result = end - start;

        Assert.assertTrue(result < 700);

        for(int i=0; i < threads.length; i++){
            threads[i].interrupt();
        }
    }

    @Test
    public void checkTimeWhileCollectData() {
        long start = System.currentTimeMillis();
        logger.read("SELECT * FROM test_reports WHERE time < now() - 5m");
        long end = System.currentTimeMillis();
        long result = end - start;
        Assert.assertTrue(result < 500);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //Do nothing
        }
    }

    @After
    public void end() {
        logger.close();
    }
}
