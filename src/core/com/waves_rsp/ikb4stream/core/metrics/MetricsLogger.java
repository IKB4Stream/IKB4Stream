package com.waves_rsp.ikb4stream.core.metrics;

import com.waves_rsp.ikb4stream.core.model.Event;
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
    private final MetricsConnector metricsConnector = MetricsConnector.getMetricsConnector();
    private final String measurement;

    /**
     * Instantiate MetricsLogger object
     */
    private MetricsLogger() {
        this.measurement = metricsConnector.getProperties().getMeasurement();
        if(!this.metricsConnector.isConnexionEnabled() || this.metricsConnector.getInfluxDB() == null) {
            LOGGER.warn("influxdb connexion disabled");
        }else {
            LOGGER.info(MetricsLogger.class.getName()+" has been started ...");
        }
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
        if(metricsConnector != null) {
            metricsConnector.close();
            Thread.currentThread().interrupt();
        }
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

        if(checkValidInfluxDBConnexion()) {
            final InfluxDB influxDB = metricsConnector.getInfluxDB();
            Point point = Point.measurement(measurement)
                    .tag("async", "true")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField(field, data).build();
            influxDB.write(this.metricsConnector.getProperties().getDbName(), "autogen", point);
            LOGGER.info(MetricsLogger.class.getName()+" : indexed points " + point.toString());
        }
    }

    public void log(String measurement, String field, String data) {
        Objects.requireNonNull(measurement);
        Objects.requireNonNull(field);
        Objects.requireNonNull(data);

        if(checkValidInfluxDBConnexion()) {
            final InfluxDB influxDB = metricsConnector.getInfluxDB();
            Point point = Point.measurement(measurement)
                                .tag("async", "true")
                                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                                .addField(field, data).build();
            influxDB.write(metricsConnector.getProperties().getDbName(), "autogen", point);
            LOGGER.info(MetricsLogger.class.getName()+" : indexed points "+point.toString());
        }
    }

    /**
     * Log a not null event into the influx database if it's possible
     *
     * @param event
     * @throws NullPointerException if {@param event} is null
     */
    public void log(Event event) {
        Objects.requireNonNull(event);
        if(checkValidInfluxDBConnexion()) {
            final InfluxDB influxDB = metricsConnector.getInfluxDB();
            Point point = Point.measurement(measurement).tag("event_source", event.getSource())
                                                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                                                        .build();
            BatchPoints.Builder builder = BatchPoints.database(metricsConnector.getProperties().getDbName());
            BatchPoints points = builder.point(point).build();
            influxDB.write(points);
            LOGGER.info(MetricsLogger.class.getName()+" : indexed "+points.getPoints());
        }
    }

    /**
     * Log a collections of point and send them into influx database
     * @param points the points to check metrics
     * @throws NullPointerException if {@param points} is null
     */
    public void log(Point... points) {
        Objects.requireNonNull(points);
        if(checkValidInfluxDBConnexion()) {
            final InfluxDB influxDB = metricsConnector.getInfluxDB();
            influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
            BatchPoints.Builder builder = BatchPoints.database(metricsConnector.getProperties().getDbName());
            builder.points(points);
            BatchPoints batchPoints = builder.build();
            influxDB.write(batchPoints);
            batchPoints.getPoints().stream().map(Point::lineProtocol).forEach(point ->
                    LOGGER.info(MetricsLogger.class.getName()+" : push metrics point " + point)
            );
        }
    }

    private boolean checkValidInfluxDBConnexion() {
        return metricsConnector != null && metricsConnector.getInfluxDB() != null && metricsConnector.isConnexionEnabled();
    }

    public boolean isInfluxServiceEnabled() {
        return metricsConnector.isConnexionEnabled();
    }

    /**
     * Read the result of a request to read data loaded into influxdb
     * @param request the specific request
     * @throws NullPointerException if {@param request} is null
     */
    public void read(String request) {
        Objects.requireNonNull(request);
        if(checkValidInfluxDBConnexion()) {
            final Query query = new Query(request, metricsConnector.getProperties().getDbName());
            QueryResult fixes = this.metricsConnector.getInfluxDB().query(query);
            List<QueryResult.Result> resultsList = fixes.getResults();
            resultsList.stream().filter(results -> results.getSeries() != null).forEach(results -> results.getSeries().forEach(series -> LOGGER.info(series.toString())));
        }
    }
}
