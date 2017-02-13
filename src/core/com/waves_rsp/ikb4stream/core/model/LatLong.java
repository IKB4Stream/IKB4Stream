package com.waves_rsp.ikb4stream.core.model;

public class LatLong {
    private final double latitude;
    private final double longitude;

    /**
     * Create a LatLong
     * @param latitude
     * @param longitude
     * @throws IllegalArgumentException if {@param latitude} is not between -90 an 90, or {@param longitude} is not between -180 and 180
     */
    public LatLong(double latitude, double longitude) {
        if(latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90° and +90° inclusive.");
        }
        if(longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180° and +180° inclusive.");
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Get latitude
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get longitude
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LatLong latLong = (LatLong) o;

        if (Double.compare(latLong.latitude, latitude) != 0) return false;
        return Double.compare(latLong.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return latitude + ", " + longitude;
    }
}
