package com.waves_rsp.ikb4stream.core.model;

import java.util.Date;
import java.util.Objects;

/**
 * Event class represents an event with starting date and end date
 */
public class Event {
    private final LatLong location;
    private final Date start;
    private final Date end;
    private final String description;
    private final byte score;
    private final String source;

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
        Objects.requireNonNull(location);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(description);
        Objects.requireNonNull(source);
        if(source.isEmpty()) { throw new IllegalArgumentException("Source argument cannot be empty."); }

        this.location = location;
        this.start = start;
        this.end = end;
        this.description = description;
        this.score = -1;
        this.source = source;
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
        Objects.requireNonNull(location);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(description);
        Objects.requireNonNull(source);
        if(source.isEmpty()) { throw new IllegalArgumentException("Source argument cannot be empty."); }
        if(score < 0 || score > 100) { throw new IllegalArgumentException("Score need to be between 0 and 100."); }

        this.location = location;
        this.start = start;
        this.end = end;
        this.description = description;
        this.score = score;
        this.source = source;
    }

    /**
     * Get location of this event
     * @return LatLong to represent the position of this Event
     */
    public LatLong getLocation() {
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
        if (location != null ? !location.equals(event.location) : event.location != null) return false;
        if (start != null ? !start.equals(event.start) : event.start != null) return false;
        if (end != null ? !end.equals(event.end) : event.end != null) return false;
        return description != null ? description.equals(event.description) : event.description == null;

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (int) score;
        return result;
    }

    /**
     *
     * @return a string which contains information about an event
     */
    @Override
    public String toString() {
        return "Event{" +
                "location=" + location +
                ", start=" + start +
                ", end=" + end +
                ", description='" + description + '\'' +
                ", score=" + score +
                ", source = " + source +
                '}';
    }
}
