package com.mrkiriss.wlpserver.model;

import lombok.Data;

@Data
public class DefinedLocationPoint {
    private float x;
    private float y;
    private String roomName;
    private int floorId;
    private String steps;
}

