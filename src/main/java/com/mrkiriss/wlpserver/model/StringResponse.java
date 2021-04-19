package com.mrkiriss.wlpserver.model;

import lombok.Data;

@Data
public class StringResponse {
    private String response;

    public StringResponse(String response){
        this.response=response;
    }
    public StringResponse(){};
}
