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
    private final BatchPoints.Builder batchPointsBuilder;

    /**
     * Instantiate MetricsLogger object
     */
    private MetricsLogger() {
        this.metricsConnector = MetricsConnector.getMetricsConnector();
        if(!this.metricsConnector.isConnexionEnabled()) {
            LOGGER.warn("influxdb connexion disabled");
            this.batchPointsBuilder = null;
            return;
        }

        this.metricsConnector.getInfluxDB().enableBatch(1000, 20, TimeUnit.NANOSECONDS);
        this.batchPointsBuilder = BatchPoints.database(metricsConnector.getProperties().getDbName()).tag("async", "true");
    }

    /**
     * Get instance of singleton MetricsLogger
     * @return An unique instance of MetricsLogger
     */
    public static MetricsLogger getMetricsLogger() {
        return new MetricsLogger();
    }

    /**
     * Close the connexion with the influx database
     */
    public void close() {
        metricsConnector.close();
    }

    /**
     * Log a data as value sent to the influx database into a specific measurement
     *
     * @param field specify the field in order to build a point
     * @param data the value to stock
     * @throws NullPointerException if {@param field} or {@param data} is null
     */
    public void log(String field, String data) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(data);

        if(!this.metricsConnector.isConnexionEnabled()) {
            final InfluxDB influxDB = metricsConnector.getInfluxDB();
            influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
            String measurement = metricsConnector.getProperties().getMeasurement();
            Point point = Point.measurement(measurement)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField(field, data).build();
            BatchPoints points = batchPointsBuilder.point(point).build();
            influxDB.write(points);
            LOGGER.info("indexes points : " + points.getPoints());
        }
    }

    /**
     * Log a collections of point and send them into influx database
     * @param points the points to check metrics
     * @throws NullPointerException if {@param points} is null
     */
    public void log(Point... points) {
        Objects.requireNonNull(points);
        if(!this.metricsConnector.isConnexionEnabled()) {
            final InfluxDB influxDB = metricsConnector.getInfluxDB();
            influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
            BatchPoints.Builder builder = BatchPoints.database(metricsConnector.getProperties().getDbName());
            builder.points(points);
            BatchPoints batchPoints = builder.build();
            influxDB.write(batchPoints);
            batchPoints.getPoints().stream().map(Point::lineProtocol).forEach(point -> LOGGER.info("push metrics point " + point));
        }
    }

    /**
     * Read the result of a request to read data loaded into influxdb
     * @param request the specific request
     * @throws NullPointerException if {@param request} is null
     */
    public void read(String request) {
        Objects.requireNonNull(request);
        if(!this.metricsConnector.isConnexionEnabled()) {
            final Query query = new Query(request, metricsConnector.getProperties().getDbName());
            QueryResult fixes = this.metricsConnector.getInfluxDB().query(query);
            List<QueryResult.Result> resultsList = fixes.getResults();
            resultsList.stream().filter(results -> results.getSeries() != null).forEach(results -> results.getSeries().forEach(series -> LOGGER.info(series.toString())));
        }
    }
}
