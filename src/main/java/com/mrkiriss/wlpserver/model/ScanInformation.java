package com.mrkiriss.wlpserver.model;

import lombok.Data;

@Data
public class ScanInformation {
    private String date;
    private int numberOfScanningKits;

    public ScanInformation(String date, int numberOfScanningKits){
        this.date=date;
        this.numberOfScanningKits=numberOfScanningKits;
    }
    public ScanInformation(){};
}
