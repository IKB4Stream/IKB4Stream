package com.waves_rsp.ikb4stream.consumer;

import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Event event = new Event(new LatLong(0, 0), new Date(0), new Date(1), "coucou");
        System.out.println(event.getDescription());
    }
}
