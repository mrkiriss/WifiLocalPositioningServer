package com.mrkiriss.wlpserver.entity;

import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class LocationPointInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String roomName;

    private int floorId;
    private int x;
    private int y;
    private String isRoom; // иначе коридор (для создания маршрута)

    public boolean isRoom(){
        return isRoom.equals("true");
    }
}
