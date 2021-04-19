package com.mrkiriss.wlpserver.services;

import com.mrkiriss.wlpserver.entity.AccessPoint;
import com.mrkiriss.wlpserver.entity.LocationPoint;
import com.mrkiriss.wlpserver.entity.LocationPointInfo;
import com.mrkiriss.wlpserver.model.CalibrationAccessPoint;
import com.mrkiriss.wlpserver.model.CalibrationLocationPoint;
import com.mrkiriss.wlpserver.model.DefinedLocationPoint;
import com.mrkiriss.wlpserver.model.StringResponse;
import com.mrkiriss.wlpserver.repositories.AccessPointRepository;
import com.mrkiriss.wlpserver.repositories.LPInfoRepository;
import com.mrkiriss.wlpserver.repositories.LocationPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MainService {

    @Autowired
    LocationPointRepository locationPointRepository;
    @Autowired
    AccessPointRepository accessPointRepository;
    @Autowired
    LPInfoRepository lpInfoRepository;

    /*
    =============Методы определения=============
     */
    public DefinedLocationPoint definedLocationPointWithRoom(CalibrationLocationPoint calibrationLocationPoint){

        // сглаживаем входные данные
        LocationPoint smoothedLocationPoint = smoothPointOfCalibration(calibrationLocationPoint);
        if (smoothedLocationPoint.getAccessPoints().size()==0) return null;
        // подбираем все возможные точки (по количеству совпадающих AP)
        List<LocationPoint> possibleLocations = chooseAllSuitableLocationPoints(smoothedLocationPoint.collectMACs()); // возможные
        // выбираем точку с минимальным евклидовым расстоянием
        DefinedLocationPoint result = chooseLocationPointWithMinDelta(smoothedLocationPoint, possibleLocations);
        // дополняем результат данными для карты
        LocationPointInfo resultInfo = lpInfoRepository.findByRoomName(result.getRoomName());
        result.setX(resultInfo.getX());
        result.setY(resultInfo.getY());
        result.setFloorId(resultInfo.getFloorId());

        return result;
    }

    private LocationPoint smoothPointOfCalibration(CalibrationLocationPoint calibrationLocationPoint){
        List<CalibrationAccessPoint> listOfCalibrationAccessPoints= generateListOfCalibrationAPs(calibrationLocationPoint);
        LocationPoint result = new LocationPoint();
        result.setLat(calibrationLocationPoint.getLat());
        result.setLon(calibrationLocationPoint.getLon());
        result.setRoomName(calibrationLocationPoint.getRoomName());
        result.setAccessPoints(selectSuitableAPs(listOfCalibrationAccessPoints, calibrationLocationPoint.getCalibrationSets().size()));
        return result;
    }
    // возвращает список объектов, в каждом из которых mac и сумма всех встреченных на этот mac rssi и количество этих rssi
    private List<CalibrationAccessPoint> generateListOfCalibrationAPs(CalibrationLocationPoint calibrationLocationPoint){
        List<CalibrationAccessPoint> result = new ArrayList<>();
        CalibrationAccessPoint currentCalibrationAP;

        for (List<AccessPoint> accessPointList : calibrationLocationPoint.getCalibrationSets()){
            for (AccessPoint accessPoint : accessPointList){

                currentCalibrationAP=findCalibrationAccessPoint(accessPoint.getMac(), result);

                if (currentCalibrationAP==null){
                    currentCalibrationAP=new CalibrationAccessPoint(accessPoint.getMac());
                    result.add(currentCalibrationAP);
                }

                currentCalibrationAP.addToRssiSum(accessPoint.getRssi());
            }
        }

        return result;
    }
    private CalibrationAccessPoint findCalibrationAccessPoint(String checkedMac, List<CalibrationAccessPoint> calibrationAccessPoints){
        for (CalibrationAccessPoint calibrationAP : calibrationAccessPoints){
            if (calibrationAP.getMac().equals(checkedMac)) return calibrationAP;
        }
        return null;
    }
    // возвращает список AP с усреднёнными rssi, только те, количество которых по mac превышает половину во всех тренировочных наборах
    private List<AccessPoint> selectSuitableAPs(List<CalibrationAccessPoint> calibrationAccessPointList, int numberOfCalibrationKits){

        final int thresholdForNumberOfAPs = numberOfCalibrationKits/2;
        int averageRssi;
        List<AccessPoint> result = new ArrayList<>();

        for (CalibrationAccessPoint calibrationAccessPoint : calibrationAccessPointList){
            if (calibrationAccessPoint.getNumberOfRssiAdditions()>thresholdForNumberOfAPs){
                averageRssi=calibrationAccessPoint.getRssiSum()/calibrationAccessPoint.getNumberOfRssiAdditions();
                result.add(new AccessPoint(calibrationAccessPoint.getMac(), averageRssi));
            }
        }

        return result;
    }

    // Выбор точек из базы с количеством совподений в MACs с набором от клиента >2
    private List<LocationPoint> chooseAllSuitableLocationPoints(List<String> currentMacsCollection){
        //locationPointRepository.findAllSuitableByMacCount(smoothedLocationPoint.collectMACs())
        System.out.println("--Выбор точек из базы с количеством совподений в MACs с набором от клиента >2--");

        List<LocationPoint> result = new ArrayList<>();
        List<String> possiblesMacsCollection;

        System.out.println("Текущий набор MAC\n"+currentMacsCollection.toString());

        for (LocationPoint possibleLP: locationPointRepository.findAll()){
            possiblesMacsCollection=possibleLP.collectMACs();
            System.out.println("Воможный набор до\n"+possiblesMacsCollection.toString());

            possiblesMacsCollection.retainAll(currentMacsCollection);
            System.out.println("Воможный набор после\n"+possiblesMacsCollection.toString());

            if (possiblesMacsCollection.size()>2) result.add(possibleLP);
        }

        return result;
    }
    private DefinedLocationPoint chooseLocationPointWithMinDelta(LocationPoint currentLocationPoint, List<LocationPoint> locationPoints){
        DefinedLocationPoint result = new DefinedLocationPoint();

        double minDelta = 0;
        final int maxDeltaRssi=45;
        for (LocationPoint lp: locationPoints){

            if (lp.getRoomName()==null) continue;

            double sum=0;
            for (AccessPoint accessPoint: currentLocationPoint.getAccessPoints()){
                AccessPoint foundAP = lp.findAPbyMAC(accessPoint.getMac());
                if (foundAP!=null){
                    sum+=Math.pow(foundAP.getRssi()-accessPoint.getRssi(), 2);
                }else{
                    sum+=Math.pow(maxDeltaRssi, 2);
                }
            }
            
            double currentDelta=Math.pow(sum/currentLocationPoint.getAccessPoints().size(),0.5);
            if (minDelta==0 || minDelta>currentDelta){
                minDelta=currentDelta;
                result.setSteps(result.getSteps()+"minDelta:"+minDelta+";"+lp.getRoomName()+"\n");
                result.setRoomName(lp.getRoomName());
            }
        }

        return result;
    }

    /*
    =============Методы сохранения/удаления/получения доп.инфы=============
     */
    public LocationPoint savePointWithoutCoordinates(CalibrationLocationPoint calibrationLocationPoint){
        LocationPoint result = smoothPointOfCalibration(calibrationLocationPoint);
        System.out.println("Location point saved with data: "+result.toString());
        locationPointRepository.save(result);
        return result;
    }

    public StringResponse savePointCoordinates(LocationPointInfo locationPointInfo){
        String info="";
        if (lpInfoRepository.findByRoomName(locationPointInfo.getRoomName())!=null){
            lpInfoRepository.deleteByRoomName(locationPointInfo.getRoomName());
            info+="The data is updated\n";
        }

        lpInfoRepository.save(locationPointInfo);

        info+=locationPointInfo.toString();
        return new StringResponse(info);
    }

    public int clearServerDB(){
        locationPointRepository.deleteAll();
        return getNUmberOfLocationPoints();
    }
    private int getNUmberOfLocationPoints(){
        int number=0;
        for(LocationPoint lp: locationPointRepository.findAll()) {
            number++;
        }
        return number;
    }
}
