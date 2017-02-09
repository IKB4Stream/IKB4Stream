package com.waves_rsp.ikb4stream.core.metrics;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MetricsLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsLogger.class);
    private final MetricsConnector metricsConnector;
    private final BatchPoints.Builder builder;

    private MetricsLogger() {
        this.metricsConnector = MetricsConnector.getMetricsConnector();
        this.metricsConnector.getInfluxDB().enableBatch(1000, 200, TimeUnit.NANOSECONDS);
        this.builder = BatchPoints.database(metricsConnector.getProperties().getDbName()).tag("async", "true");
    }

    public static MetricsLogger getMetricsLogger() {
        return new MetricsLogger();
    }

    public void close() {
        metricsConnector.close();
    }

    /**
     * Log a data as value sent to the influx database into a specific measurement
     *
     * @param field specify the field in order to build a point
     * @param data the value to stock
     */
    public void log(String field, String data) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(data);
        final InfluxDB influxDB = metricsConnector.getInfluxDB();
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        String measurement = metricsConnector.getProperties().getMeasurement();
        Point point = Point.measurement(measurement)
                           .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(field, data).build();
        BatchPoints points = builder.point(point).build();
        influxDB.write(points);
        LOGGER.info("indexes points : "+points.getPoints());
    }

    /**
     * Log a collections of point and send them into influx database
     *
     * @param points the points to check metrics
     */
    public void log(Point... points) {
        Objects.requireNonNull(points);
        final InfluxDB influxDB = metricsConnector.getInfluxDB();
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        BatchPoints.Builder builder = BatchPoints.database(metricsConnector.getProperties().getDbName());
        builder.points(points);
        BatchPoints batchPoints = builder.build();
        influxDB.write(batchPoints);
        batchPoints.getPoints().stream().map(Point::lineProtocol).forEach(point -> LOGGER.info("push metrics point "+point));
    }

    /**
     * Read the result of a request to read data loaded into influxdb
     *
     * @param request the specific request
     */
    public void read(String request) {
        Objects.requireNonNull(request);
        final Query query = new Query(request, metricsConnector.getProperties().getDbName());
        QueryResult fixes = this.metricsConnector.getInfluxDB().query(query);
        List<QueryResult.Result> resultsList = fixes.getResults();
        resultsList.stream().filter(results -> results.getSeries() != null).forEach(results -> results.getSeries().forEach(series -> LOGGER.info(series.toString())));
    }
}
