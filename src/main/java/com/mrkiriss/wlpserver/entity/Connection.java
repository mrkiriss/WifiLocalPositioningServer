package com.mrkiriss.wlpserver.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Connection {
    @Id
    private Long id;

    private String firstName;
    private String secondName;

    public Connection(String firstName, String secondName){
        this.firstName=firstName;
        this.secondName=secondName;
    }
    public Connection(){};
}
