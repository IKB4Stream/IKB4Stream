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

    private MetricsLogger(MetricsConnector metricsConnector) {
        Objects.requireNonNull(metricsConnector);
        this.metricsConnector = metricsConnector;
        this.metricsConnector.getInfluxDB().enableBatch(1000, 200, TimeUnit.NANOSECONDS);
        this.builder = BatchPoints.database(metricsConnector.getProperties().getDbName());
    }

    public static MetricsLogger getMetricsLogger(MetricsConnector connector) {
        return new MetricsLogger(connector);
    }

    /**
     * Log a data as value sent to the influx database into a specific measurement
     *
     * @param measurement the measurement key
     * @param data the value to stock
     */
    public void log(String measurement, String data) {
        Objects.requireNonNull(measurement);
        Objects.requireNonNull(data);
        final InfluxDB influxDB = metricsConnector.getInfluxDB();
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        Point point = Point.measurement(measurement).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("reports", data).build();
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
