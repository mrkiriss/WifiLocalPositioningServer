package com.mrkiriss.wlpserver.services;

import com.mrkiriss.wlpserver.entity.AccessPoint;
import com.mrkiriss.wlpserver.entity.Connection;
import com.mrkiriss.wlpserver.entity.LocationPoint;
import com.mrkiriss.wlpserver.entity.LocationPointInfo;
import com.mrkiriss.wlpserver.model.*;
import com.mrkiriss.wlpserver.repositories.AccessPointRepository;
import com.mrkiriss.wlpserver.repositories.ConnectionRepository;
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
    @Autowired
    ConnectionRepository connectionRepository;

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
    public StringResponse saveLocationPointInfo(LocationPointInfo locationPointInfo){
        String info="";
        if (lpInfoRepository.findByRoomName(locationPointInfo.getRoomName())!=null){
            lpInfoRepository.deleteByRoomName(locationPointInfo.getRoomName());
            info+="The data is updated\n";
        }

        lpInfoRepository.save(locationPointInfo);

        info+=locationPointInfo.toString();
        return new StringResponse(info);
    }

    // для отображение точек на карте
    public ListOfAllMapPoints getListOfLPInfo(){
        ListOfAllMapPoints result = new ListOfAllMapPoints();
        ArrayList actualList = new ArrayList();
        lpInfoRepository.findAll().iterator().forEachRemaining(actualList::add);
        result.setLocationPointInfos(actualList);
        return result;
    }

    // deleting mode
    public StringResponse deleteLocationPointInfo(String roomName){
        LocationPointInfo lp= lpInfoRepository.findByRoomName(roomName);
        if (lp==null){
            return new StringResponse("Информация о точке не найдена");
        }
        lpInfoRepository.delete(lp);
        return new StringResponse("Информация о точке"+lp.toString()+" успешно удалена");
    }
    public StringResponse deleteLocationPoint(String roomName){
        List<LocationPoint> lp= locationPointRepository.findAllByRoomName(roomName);
        if (lp==null){
            return new StringResponse("Информация о точке не найдена");
        }
        locationPointRepository.deleteAll(lp);
        return new StringResponse("Информация о сканированиях"+lp.toString()+" успешно удалена");
    }

    // connections mode
    private final String MODE_SAVING="saving";
    private final String MODE_DELETING="deleting";
    private final String MODE_UNMODIFIED="unmodified";

    public StringResponse processConnections(Connections connections){
        Map<String, List<String>> moveInformation = new HashMap<>();
        moveInformation.put(MODE_DELETING, new ArrayList<>());
        moveInformation.put(MODE_SAVING, new ArrayList<>());
        String firstName=connections.getMainRoomName();

        // определяем списка на удаление и добавление
        List<String> currentConnections = getExistingConnectionsWithMain(connections.getMainRoomName());
        List<String> modifiedConnections = connections.getListOfNames();

        List<String> unmodifiedConnections = new ArrayList<>(currentConnections);
        unmodifiedConnections.retainAll(modifiedConnections);
        moveInformation.put(MODE_UNMODIFIED, unmodifiedConnections);

        currentConnections.removeAll(unmodifiedConnections); // будут удалены
        modifiedConnections.removeAll(unmodifiedConnections); // будут добавлены

        // проводим удаление
        deleteConnections(firstName, currentConnections, moveInformation);
        saveConnections(firstName, modifiedConnections, moveInformation);

        return new StringResponse(moveInformation.toString());
    }

    private List<String> getExistingConnectionsWithMain(String mainName){
        List<String> result = new ArrayList<>();

        List<Connection> allConnectionsWithMain = new ArrayList<>();
        allConnectionsWithMain.addAll(connectionRepository.findAllByFirstName(mainName));
        allConnectionsWithMain.addAll(connectionRepository.findAllBySecondName(mainName));

        // добаляем узел, связанный с главным mainName
        for (Connection connection: allConnectionsWithMain){
            result.add(connection.getFirstName().equals(mainName)?connection.getSecondName():connection.getFirstName());
        }

        return result;
    }
    private void deleteConnections(String firstName, List<String> secondNames, Map<String, List<String>> moveInformation){
        String[] singleConnection;
        for (String secondName:secondNames){
            singleConnection=new String[]{firstName, secondName};
            sortNames(singleConnection);
            connectionRepository.deleteConnectionByFirstNameAndSecondName(singleConnection[0],singleConnection[1]);

            saveStepInformation(singleConnection, MODE_DELETING, moveInformation);

        }
    }
    private void saveConnections(String firstName, List<String> secondNames, Map<String, List<String>> moveInformation){
        String[] singleConnection;
        for (String secondName:secondNames){
            singleConnection=new String[]{firstName, secondName};
            sortNames(singleConnection);
            Connection newConnection = new Connection(singleConnection[0], singleConnection[1]);
            connectionRepository.save(newConnection);

            saveStepInformation(singleConnection, MODE_SAVING, moveInformation);

        }
    }
    private void sortNames(String[] names){
        Arrays.sort(names);
    }
    private void saveStepInformation(String[] data, String mode, Map<String,List<String>> informationContainer){
        informationContainer.get(mode).add(String.join("<->", data));
    }

    public Connections downloadConnections(String name){
        Connections result = new Connections();
        result.setMainRoomName(name);
        result.setSecondaryRooms(new ArrayList<>());

        List<String> existingConnections = getExistingConnectionsWithMain(name);
        for (String singleConnectionName: existingConnections){
            result.getSecondaryRooms().add(lpInfoRepository.findByRoomName(singleConnectionName));
        }

        return result;
    }
}
