package com.mrkiriss.wlpserver.model;

import com.mrkiriss.wlpserver.entity.AccessPoint;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CalibrationLocationPoint {
    private double lat;
    private double lon;
    private String roomName;
    private List<List<AccessPoint>> calibrationSets;
}
