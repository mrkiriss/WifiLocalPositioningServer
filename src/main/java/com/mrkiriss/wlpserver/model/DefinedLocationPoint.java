package com.mrkiriss.wlpserver.model;

import lombok.Data;

@Data
public class DefinedLocationPoint {
    private float x;
    private float y;
    private int floorId;
    private String roomName;

    private String steps="";
}

