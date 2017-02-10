package com.waves_rsp.ikb4stream.communication.kafka;

import java.util.Date;
import java.util.Objects;

/**
 * Created by ikb4stream on 07/02/17.
 * Represents an anomaly request received from Kafka. An anomaly request is represented by values like date, latitude (max and min) and longitude (max and min)
 */
public class AnomalyRequest {

    private final Date start;
    private final Date end;
    private final double minLatitude;
    private final double maxLatitude;
    private final double minLongitude;
    private final double maxLongitude;

    /**
     * The constructor of AnomalyRequest
     * @param start is the reception date of the anomaly
     * @param minLatitude the minimum latitude
     * @param maxLatitude the maximum latitude
     * @param minLongitude the minimum longitude
     * @param maxLongitude the maximum longitude
     */
    public AnomalyRequest(Date start, Date end, double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if(minLatitude > maxLatitude) {
            throw new IllegalArgumentException("minLatitude must be lower than maxLatitude.");
        }
        if(minLongitude > maxLongitude) {
            throw new IllegalArgumentException("minLongitude must be lower than maxLongitude.");
        }
        this.start = start;
        this.end = end;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    /**
     * @return the reception date of the anomaly
     */
    public Date getStart() {
        return start;
    }

    /**
     * @return the minimum latitude
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @return the maximum latitude
     */
    public double getMinLatitude() {
        return minLatitude;
    }

    /**
     * @return the minimum longitude
     */
    public double getMaxLatitude() {
        return maxLatitude;
    }

    /**
     * @return the minimum longitude
     */
    public double getMinLongitude() {
        return minLongitude;
    }

    /**
     * @return the maximum longitude
     */
    public double getMaxLongitude() {
        return maxLongitude;
    }

    /**
     * Print information about an anomaly
     * @return a String containing anomaly information
     */
    @Override
    public String
    toString() {
        return "AnomalyRequest{" +
                "start=" + start +
                ", end=" + end +
                ", minLatitude=" + minLatitude +
                ", maxLatitude=" + maxLatitude +
                ", minLongitude=" + minLongitude +
                ", maxLongitude=" + maxLongitude +
                '}';
    }
}
