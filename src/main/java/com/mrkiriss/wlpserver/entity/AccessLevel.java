package com.mrkiriss.wlpserver.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class AccessLevel {
    @Id
    private String uuid;
    private int level;
}
