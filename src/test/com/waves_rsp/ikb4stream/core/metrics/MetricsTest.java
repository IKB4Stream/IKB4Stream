package com.waves_rsp.ikb4stream.core.metrics;

import org.influxdb.dto.Point;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

public class MetricsTest {

    @Ignore
    @Test
    public void testPing() {
        MetricsConnector connector = MetricsConnector.getMetricsConnector();
        System.out.println(connector.getInfluxDB().ping());
    }

    @Ignore
    @Test (expected = NullPointerException.class)
    public void addNullData() {
        MetricsLogger logger = MetricsLogger.getMetricsLogger();
        logger.log(null, null);
    }

    @Ignore
    @Test
    public void addValidData() {
        MetricsLogger logger = MetricsLogger.getMetricsLogger();
        logger.log("test_reports", "test metrics");
    }

    @Ignore
    @Test
    public void checkTimeWhileAddedData() {
        MetricsLogger logger = MetricsLogger.getMetricsLogger();
        long start = System.currentTimeMillis();
        logger.log("test_reports", "test metrics1");
        logger.log("test_reports", "test metrics2");
        logger.log("test_reports", "test metrics3");
        logger.log("test_reports", "test metrics4");
        long end = System.currentTimeMillis();
        long result = end - start;
        Assert.assertTrue(result < 500);
    }

    @Ignore
    @Test
    public void checkTimeWhileAddedPointData() {
        MetricsLogger logger = MetricsLogger.getMetricsLogger();
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

    @Ignore
    @Test
    public void checkTimeWhileAddedPointDataFromThreadPool() {
        final int max = 10;
        Thread[] threads = new Thread[max];

        long start = System.currentTimeMillis();
        IntStream.range(0, max).forEach(i -> {
            threads[i] = new Thread(() -> {
                MetricsLogger logger = MetricsLogger.getMetricsLogger();
                Point[] points = new Point[max];
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

    @Ignore
    @Test
    public void checkTimeWhileCollectData() {
        MetricsLogger logger = MetricsLogger.getMetricsLogger();
        long start = System.currentTimeMillis();
        logger.read("SELECT * FROM test_reports WHERE time < now() - 5m");
        long end = System.currentTimeMillis();
        long result = end - start;
        Assert.assertTrue(result < 500);
    }
}
