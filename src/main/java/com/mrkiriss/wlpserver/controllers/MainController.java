package com.mrkiriss.wlpserver.controllers;

import com.mrkiriss.wlpserver.entity.LocationPoint;
import com.mrkiriss.wlpserver.entity.LocationPointInfo;
import com.mrkiriss.wlpserver.model.CalibrationLocationPoint;
import com.mrkiriss.wlpserver.model.DefinedLocationPoint;
import com.mrkiriss.wlpserver.model.ListOfAllMapPoints;
import com.mrkiriss.wlpserver.model.StringResponse;
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

    @GetMapping("/define/room/coordinates")
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
            return ResponseEntity.badRequest().body(new StringResponse(e.getMessage()));
        }
    }

    @PostMapping("/training/room/coordinates")
    public ResponseEntity<?> addRoomCoordinates(@RequestBody LocationPointInfo locationPointInfo){
        try {
            System.out.println("Запрос на добавление координат точки");
            return ResponseEntity.ok(mainService.saveLocationPointInfo(locationPointInfo));
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new StringResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/training/room/coordinates")
    public ResponseEntity<StringResponse> deleteRoomCoordinates(@RequestBody String roomName){
        try{
            System.out.println("Удаление инф. части точки началось");
            StringResponse response = mainService.deleteLocationPointInfo(roomName);
            System.out.println("Удаление инф. части точки прошло успешно");
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new StringResponse(e.getMessage()));        }
    }
    @DeleteMapping("/training/room/aps")
    public ResponseEntity<StringResponse> deleteRoomLocationPoint(@RequestBody String roomName){
        try{
            System.out.println("Удаление точки началось");
            StringResponse response = mainService.deleteLocationPoint(roomName);
            System.out.println("Удаление точки прошло успешно");
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new StringResponse(e.getMessage()));        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearServer(){
        try {
            System.out.println("Запрос на очистку сервера");
            StringResponse response = new StringResponse();
            response.setResponse("Количество LP после="+mainService.clearServerDB());
            System.out.println("Запрос обработан успешно");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new StringResponse(e.getMessage()));
        }
    }
}
