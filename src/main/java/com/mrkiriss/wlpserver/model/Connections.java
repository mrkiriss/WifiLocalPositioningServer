package com.mrkiriss.wlpserver.model;

import lombok.Data;

import java.util.List;

@Data
public class Connections {
    private String mainRoomName;
    private List<String> secondaryRoomNames;
}
