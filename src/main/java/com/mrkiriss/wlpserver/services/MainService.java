package com.mrkiriss.wlpserver.services;

import com.mrkiriss.wlpserver.entity.AccessPoint;
import com.mrkiriss.wlpserver.entity.LocationPoint;
import com.mrkiriss.wlpserver.entity.LocationPointInfo;
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
        List<LocationPoint> splitLPs = splitPointOfCalibration(calibrationLocationPoint);
        // подбираем все возможные точки (по количеству совпадающих AP)
        if (splitLPs.size()==0) return null;

        Set<LocationPoint> possibleLocationsSet = new HashSet<>();
        Set<Integer> indexes = new HashSet<>();
        indexes.add(0);
        indexes.add(splitLPs.size()-1);
        indexes.add(splitLPs.size()/2);
        for (Integer index: indexes){
            possibleLocationsSet.addAll(chooseAllSuitableLocationPoints(splitLPs.get(index).collectMACs()));
        }

        List<LocationPoint> possibleLocations = new LinkedList<>(possibleLocationsSet);

        // выбираем для каждого набора точку с минимальным евклидовым расстоянием
        List<DefinedLocationPoint> preResult = chooseLocationPointsWithMinDelta(splitLPs, possibleLocations);
        // выбираем самую частовстречаемую точку
        DefinedLocationPoint result = chooseMostCommon(preResult);
        // дополняем результат данными для карты
        LocationPointInfo resultInfo = lpInfoRepository.findByRoomName(result.getRoomName());

        if (resultInfo==null){
            resultInfo=new LocationPointInfo();
            resultInfo.setX(-1);
            resultInfo.setY(-1);
            resultInfo.setFloorId(-1);
        }

        result.setX(resultInfo.getX());
        result.setY(resultInfo.getY());
        result.setFloorId(resultInfo.getFloorId());

        return result;
    }

    private List<LocationPoint> splitPointOfCalibration(CalibrationLocationPoint calibrationLocationPoint){
        List<LocationPoint> results = new ArrayList<>();
        LocationPoint result;

        for (List<AccessPoint> currentAPSet: calibrationLocationPoint.getCalibrationSets()){
            result = new LocationPoint();
            result.setRoomName(calibrationLocationPoint.getRoomName());
            result.setAccessPoints(currentAPSet);
            results.add(result);
        }

        return results;
    }

    // Выбор точек из базы с количеством совподений в MACs с набором от клиента >2
    private List<LocationPoint> chooseAllSuitableLocationPoints(List<String> currentMacsCollection){
        //locationPointRepository.findAllSuitableByMacCount(smoothedLocationPoint.collectMACs())
        //System.out.println("--Выбор точек из базы с количеством совподений в MACs с набором от клиента >2--");

        List<LocationPoint> result = new ArrayList<>();
        List<String> possiblesMacsCollection;

        //System.out.println("Текущий набор MAC\n"+currentMacsCollection.toString());

        for (LocationPoint possibleLP: locationPointRepository.findAll()){
            possiblesMacsCollection=possibleLP.collectMACs();
            //System.out.println("Воможный набор до\n"+possiblesMacsCollection.toString());

            possiblesMacsCollection.retainAll(currentMacsCollection);
            //System.out.println("Воможный набор после\n"+possiblesMacsCollection.toString());

            if (possiblesMacsCollection.size()>2) result.add(possibleLP);
        }

        return result;
    }
    // выбираем точку из suitableLocationPoints с мнимальным евклидовым расстоянием для каждого входного набора currentLocationPoints
    private List<DefinedLocationPoint> chooseLocationPointsWithMinDelta(List<LocationPoint> currentLocationPoints, List<LocationPoint> suitableLocationPoints){
        /*System.out.println("Started chooseLocationPointsWithMinDelta");
        System.out.println("currentLocationPoints: "+currentLocationPoints);
        System.out.println("suitableLocationPoints: "+suitableLocationPoints);*/

        List<DefinedLocationPoint> result = new ArrayList<>();

        final int maxDeltaRssi=45;

        for (LocationPoint currentLocationPoint : currentLocationPoints) {
            DefinedLocationPoint resultSingle = new DefinedLocationPoint();
            double minDelta = Double.MAX_VALUE;

            for (LocationPoint suitableLP : suitableLocationPoints) {

                if (suitableLP.getRoomName() == null) continue;

                double sum = 0;
                for (AccessPoint accessPoint : currentLocationPoint.getAccessPoints()) {
                    AccessPoint foundAP = suitableLP.findAPbyMAC(accessPoint.getMac());
                    if (foundAP != null) {
                        sum += Math.pow(foundAP.getRssi() - accessPoint.getRssi(), 2);
                    } else {
                        sum += Math.pow(maxDeltaRssi, 2);
                    }
                }

                double currentDelta = Math.pow(sum / currentLocationPoint.getAccessPoints().size(), 0.5);
                if (minDelta > currentDelta) {
                    minDelta = currentDelta;
                    //resultSingle.setSteps(resultSingle.getSteps() + "minDelta:" + minDelta + ";" + suitableLP.getRoomName() + "\n");
                    resultSingle.setRoomName(suitableLP.getRoomName());
                }
            }

            result.add(resultSingle);
        }

        return result;
    }
    // из списка определённых точек выбираем ту, которая встречается чаще
    private DefinedLocationPoint chooseMostCommon(List<DefinedLocationPoint> definedLocationPoints){
        int maxNumberOfMatches = 0;
        DefinedLocationPoint result = null;
        //System.out.println("from chooseMostCommon: "+definedLocationPoints);

        List<String> allNames = new LinkedList<>();
        for (DefinedLocationPoint currentDLP : definedLocationPoints){
            allNames.add(currentDLP.getRoomName());
        }

        for (DefinedLocationPoint currentDLP : definedLocationPoints){

            int currentNumberOfMatches=0;
            for (String currentName: allNames){
                if (currentName==null) continue;
                if (currentName.equals(currentDLP.getRoomName())) currentNumberOfMatches++;
            }

            if (currentNumberOfMatches>maxNumberOfMatches || maxNumberOfMatches==0){
                result=currentDLP;
                maxNumberOfMatches=currentNumberOfMatches;
            }
        }

        if (result==null) result=new DefinedLocationPoint();
        //result.setSteps(result.getSteps()+allNames.toString());
        return result;
    }

    /*
    =============Методы сохранения/удаления/получения доп.инфы=============
     */
    public List<LocationPoint> savePointWithoutCoordinates(CalibrationLocationPoint calibrationLocationPoint){
        List<LocationPoint> result = splitPointOfCalibration(calibrationLocationPoint);
       // System.out.println("Location point saved with data: "+result.toString());
        locationPointRepository.saveAll(result);
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
