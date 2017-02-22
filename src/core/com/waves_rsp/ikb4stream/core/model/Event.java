package com.waves_rsp.ikb4stream.core.model;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Event class represents an event with starting date and end date
 */
public class Event {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(Event.class);
    private final LatLong[] location;
    private final Date start;
    private final Date end;
    private final String description;
    private final byte score;
    private final String source;

    /**
     * Create an Event without score
     * @param location the location of the event, defined by a BondingBox (LatLong[])
     * @param start The moment when the event begins
     * @param end End of the event, or the current date
     * @param description the event content. For instance, the message of a tweet.
     * @param source from which datasource the event is provided
     * @throws NullPointerException If a param is null
     * @throws IllegalArgumentException If {@param source} is empty
     */
    public Event(LatLong[] location, Date start, Date end, String description, String source) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(description);
        Objects.requireNonNull(source);
        if(source.isEmpty()) { throw new IllegalArgumentException("Source argument cannot be empty."); }
        if(!location[0].equals(location[location.length - 1])) {
            throw new IllegalArgumentException("BoundingBox is not closed.");
        }

        this.location = location;
        this.start = start;
        this.end = end;
        this.description = description;
        this.score = -1;
        this.source = source;
    }

    /**
     * Create an Event with a score
     * @param location the location of the event, defined by a BondingBox (LatLong[])
     * @param start The moment when the event begins
     * @param end End of the event, or the current date
     * @param description the event content. For instance, the message of a tweet.
     * @param score Score of this event between 0 and 100
     * @param source from which datasource the event is provided
     * @throws NullPointerException If one params is null
     * @throws IllegalArgumentException If {@param source} is empty or {@param score} is not between 0 and 100
     */
    public Event(LatLong[] location, Date start, Date end, String description, byte score, String source) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(description);
        Objects.requireNonNull(source);
        if(source.isEmpty()) { throw new IllegalArgumentException("Source argument cannot be empty."); }
        if(score < 0 || score > 100) { throw new IllegalArgumentException("Score need to be between 0 and 100."); }
        if(!location[0].equals(location[location.length - 1])) {
            throw new IllegalArgumentException("BoundingBox is not closed.");
        }


        this.location = location;
        this.start = start;
        this.end = end;
        this.description = description;
        this.score = score;
        this.source = source;
    }

    /**
     * Create an Event without score
     * @param location the location of the event, defined by a LatLong
     * @param start The moment when the event begins
     * @param end End of the event, or the current date
     * @param description the event content. For instance, the message of a tweet.
     * @param source from which datasource the event is provided
     * @throws NullPointerException If a param is null
     * @throws IllegalArgumentException If {@param source} is empty
     */
    public Event(LatLong location, Date start, Date end, String description, String source) {
        this(new LatLong[]{location}, start, end, description, source);
    }

    /**
     * Create an Event with a score
     * @param location the location of the event, defined by a LatLong
     * @param start The moment when the event begins
     * @param end End of the event, or the current date
     * @param description the event content. For instance, the message of a tweet.
     * @param score Score of this event between 0 and 100
     * @param source from which datasource the event is provided
     * @throws NullPointerException If one params is null
     * @throws IllegalArgumentException If {@param source} is empty or {@param score} is not between 0 and 100
     */
    public Event(LatLong location, Date start, Date end, String description, byte score, String source) {
        this(new LatLong[]{location}, start, end, description, score, source);
    }

    /**
     * Get location of this event
     * @return LatLong[] to represent the position of this Event
     */
    public LatLong[] getLocation() {
        return location;
    }

    /**
     * Get the moment when the event begins
     * @return start date
     */
    public Date getStart() {
        return start;
    }

    /**
     * Get the moment when the end of the event
     * @return end date
     */
    public Date getEnd() {
        return end;
    }

    /**
     * Get description of this event
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get source from which datasource, this event is provided
     * @return source
     */
    public String getSource() {
        return source;
    }

    /**
     * Get score of this event after score processing
     * @return score or -1 if this event hasn't been analysed by score processor
     */
    public byte getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (score != event.score) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(location, event.location)) return false;
        if (start != null ? !start.equals(event.start) : event.start != null) return false;
        if (end != null ? !end.equals(event.end) : event.end != null) return false;
        if (description != null ? !description.equals(event.description) : event.description != null) return false;
        return source != null ? source.equals(event.source) : event.source == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(location);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (int) score;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    /**
     *
     * @return a string which contains information about an event
     */
    @Override
    public String toString() {
        return "Event{" +
                "location=" + Arrays.stream(location).map(LatLong::toString).collect(Collectors.joining(",", "[", "]")) +
                ", start=" + start +
                ", end=" + end +
                ", description='" + description + '\'' +
                ", score=" + score +
                ", source = " + source +
                '}';
    }

    /**
     * Get score min for an event
     * @return Score min to apply to an event
     */
    public static byte getScoreMin() {
        try {
            return Byte.parseByte(PROPERTIES_MANAGER.getProperty("score.min"));
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Get score max for an event
     * @return Score max to apply to an event
     */
    public static byte getScoreMax() {
        try {
            return Byte.parseByte(PROPERTIES_MANAGER.getProperty("score.max"));
        } catch (IllegalArgumentException e) {
            return 100;
        }
    }
}
