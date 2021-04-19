package com.mrkiriss.wlpserver.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class LocationPoint {
    private double lat;
    private double lon;
    private String roomName;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="LP_ID")
    private List<AccessPoint> accessPoints = new ArrayList<>();

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    public List<String> collectMACs(){
        List<String> result = new ArrayList<>();

        for (AccessPoint accessPoint: accessPoints){
            result.add(accessPoint.getMac());
        }

        return result;
    }
    public AccessPoint findAPbyMAC(String mac){
        for (AccessPoint ap: accessPoints){
            if (ap.getMac().equals(mac)) return ap;
        }
        return null;
    }
    public String toString(){
        String result="";
        result+="lat = "+ lat+"\n";
        result+="lon = "+ lon+"\n";
        result+="room = "+ roomName+"\n";
        for (AccessPoint accessPoint : accessPoints){
            result+="mac: "+accessPoint.getMac()+" rssi: "+accessPoint.getRssi()+"\n";
        }
        return result;
    }
}
