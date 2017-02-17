package com.waves_rsp.ikb4stream.datasource.openagenda;

/**
 * Created by ikb4stream on 17/02/17.
 */
public class AgendaEvent {
    private final String latlon;
    private final String title;
    private final String description;
    private final String free_text;
    private final String date_start;
    private final String date_end;
    private final String city;
    private final String address;


    public AgendaEvent( String latlon, String title, String description, String free_text, String date_start, String date_end, String city, String address) {
        this.latlon = latlon;
        this.title = title;
        this.description = description;
        this.free_text = free_text;
        this.date_start = date_start;
        this.date_end = date_end;
        this.city = city;
        this.address = address;
    }

    public String getLatlon() {
        return latlon;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFree_text() {
        return free_text;
    }

    public String getDate_start() {
        return date_start;
    }

    public String getDate_end() {
        return date_end;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }
}
