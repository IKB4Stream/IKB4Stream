package com.waves_rsp.ikb4stream.consumer;

import java.util.Date;
import java.util.Objects;

/**
 * Created by ikb4stream on 07/02/17.
 * Represents an anomaly request received from Kafka. An anomaly request is represented by values like date, latitude (max and min) and longitude (max and min)
 */
public class AnomalyRequest {

    private final Date date;
    private final double minLatitude;
    private final double maxLatitude;
    private final double minLongitude;
    private final double maxLongitude;

    public AnomalyRequest(Date date, double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        Objects.requireNonNull(date);
        if(minLatitude > maxLatitude) {
            throw new IllegalArgumentException("minLatitude must be lower than maxLatitude.");
        }
        if(minLongitude > maxLongitude) {
            throw new IllegalArgumentException("minLongitude must be lower than maxLongitude.");
        }
        this.date = date;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    public Date getDate() {
        return date;
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    @Override
    public String toString() {
        return "AnomalyRequest{" +
                "date=" + date +
                ", minLatitude=" + minLatitude +
                ", maxLatitude=" + maxLatitude +
                ", minLongitude=" + minLongitude +
                ", maxLongitude=" + maxLongitude +
                '}';
    }

}
