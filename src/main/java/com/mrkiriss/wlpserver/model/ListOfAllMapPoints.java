package com.mrkiriss.wlpserver.model;

import com.mrkiriss.wlpserver.entity.LocationPointInfo;
import lombok.Data;
import org.hibernate.boot.archive.scan.spi.ScanResult;

import java.util.List;

@Data
public class ListOfAllMapPoints {
    private List<LocationPointInfo> locationPointInfos;
}
