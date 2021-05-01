package com.mrkiriss.wlpserver.model;

import com.mrkiriss.wlpserver.entity.LocationPointInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Connections {
    private String mainRoomName;
    private List<LocationPointInfo> secondaryRooms;

    public List<String> getListOfNames(){
        List<String> result = new ArrayList<>();
        for (LocationPointInfo lpi: secondaryRooms){
            result.add(lpi.getRoomName());
        }
        return result;
    }
}
