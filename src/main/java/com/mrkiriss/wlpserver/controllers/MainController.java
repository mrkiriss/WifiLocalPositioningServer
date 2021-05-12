package com.mrkiriss.wlpserver.controllers;

import com.mrkiriss.wlpserver.entity.LocationPoint;
import com.mrkiriss.wlpserver.entity.LocationPointInfo;
import com.mrkiriss.wlpserver.model.*;
import com.mrkiriss.wlpserver.services.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/location")
public class MainController {

    @Autowired
    private MainService mainService;

    @PostMapping("/define/room")
    public ResponseEntity<DefinedLocationPoint> getLocationPointWithRoom(@RequestBody CalibrationLocationPoint calibrationLocationPoint){
        try {
            System.out.println("Запрос на определение местоположения");
            DefinedLocationPoint resultPoint = mainService.definedLocationPointWithRoom(calibrationLocationPoint);

            if (resultPoint==null){
                System.out.println("Местоположение не оределено - пустой результат");
                resultPoint=new DefinedLocationPoint();
                resultPoint.setSteps("Местоположение не оределено - пустой результат");
                return ResponseEntity.ok(resultPoint);
            }

            resultPoint.setSteps(resultPoint.getSteps()+"Местоположение оределено успешно");
            System.out.println("Местоположение оределено успешно");
            return ResponseEntity.ok(resultPoint);
        } catch (Exception e){
            e.printStackTrace();
            DefinedLocationPoint errorResult = new DefinedLocationPoint();
            errorResult.setSteps(e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    @GetMapping("/define/instruction")
    public ResponseEntity<StringResponse> getInstructionURL(){
        try{
            String instructionURLByYouTubeVideoId = "BaTm2RfcYPE";
            return ResponseEntity.ok(new StringResponse(instructionURLByYouTubeVideoId));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new StringResponse(e.getMessage()));
        }
    }

    @GetMapping("/define/room/info")
    public ResponseEntity<?> getListOfLPInfo(){
        try{
            ListOfAllMapPoints listOfAllMapPoints = mainService.getListOfLPInfo();
            if (listOfAllMapPoints==null) throw new Exception();
            System.out.println("Запрос на получение всех информаций о точках успешено завершён");
            return ResponseEntity.ok(listOfAllMapPoints);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new StringResponse(e.getMessage()));
        }
    }

    @GetMapping("/define/route")
    public ResponseEntity<List<LocationPointInfo>> getRoute(@RequestParam("start") String start, @RequestParam("end") String end) {
        try {
            System.out.println("Начато определениея маршрута между "+start+"..."+end);
            List<LocationPointInfo> result = mainService.getDefinedRoute(start, end);
            System.out.println("Маршрут успешно определён: "+result);
            return ResponseEntity.ok(result);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/training/room/aps")
    public ResponseEntity<?> addRoomWithoutCoordinates(@RequestBody CalibrationLocationPoint calibrationLocationPoint){
        try {
            System.out.println("Запрос на добавление точки");
            List<LocationPoint> locationPoints = mainService.savePointWithoutCoordinates(calibrationLocationPoint);
            StringResponse response = new StringResponse();
            response.setResponse("Number of added lps: "+locationPoints.size()+"\n"+locationPoints.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(new StringResponse(e.getMessage()));
        }
    }

    @PostMapping("/training/room/info")
    public ResponseEntity<?> addRoomCoordinates(@RequestBody LocationPointInfo locationPointInfo){
        try {
            System.out.println("Запрос на добавление координат точки "+locationPointInfo.toString());
            return ResponseEntity.ok(mainService.saveLocationPointInfo(locationPointInfo));
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(new StringResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/training/room/info")
    public ResponseEntity<StringResponse> deleteRoomCoordinates(@RequestParam String roomName){
        try{
            System.out.println("Удаление инф. части точки началось");
            StringResponse response = mainService.deleteLocationPointInfo(roomName);
            System.out.println("Удаление инф. части точки прошло успешно");
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(new StringResponse(e.getMessage()));        }
    }
    @DeleteMapping("/training/room/aps")
    public ResponseEntity<StringResponse> deleteRoomLocationPoint(@RequestParam String roomName){
        try{
            System.out.println("Удаление точки началось");
            StringResponse response = mainService.deleteLocationPoint(roomName);
            System.out.println("Удаление точки прошло успешно");
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(new StringResponse(e.getMessage()));        }
    }

    @PostMapping("/training/connections")
    public ResponseEntity<StringResponse> processConnections(@RequestBody Connections connections){
        try{
            System.out.println("Началась оработка связей: "+connections.getSecondaryRooms());
            StringResponse response = mainService.processConnections(connections);
            System.out.println("Связи успешно оработаны: "+response.getResponse());
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new StringResponse(e.getMessage()));
        }
    }
    @GetMapping("/training/connections")
    public ResponseEntity<?> getConnections(@RequestParam("name") String name){
        try {
            System.out.println("Началась получения связей по имени одного из узлов");
            Connections response = mainService.downloadConnections(name);
            System.out.println("Связи получен успешно и отправлены на сервер с данными: "+response.toString());
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(new StringResponse(e.getMessage()));
        }
    }
}
