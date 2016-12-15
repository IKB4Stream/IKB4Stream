package com.waves_rsp.ikb4stream.core.model;

import java.util.Date;
import java.util.Objects;

public class Event {
    private final LatLong location;
    private final Date start; //TODO: change to LocalDateTime
    private final Date end; //TODO: change to LocalDateTime
    private final String description;
    private final byte score;
    private final String source;

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

    public LatLong getLocation() {
        return location;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public byte getScore() {
        if(score < 0) { throw new IllegalStateException("Score not set."); }
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

    @Override
    public String toString() {
        return "Event{" +
                "location=" + location +
                ", start=" + start +
                ", end=" + end +
                ", description='" + description + '\'' +
                ", score=" + score +
                '}';
    }
}
