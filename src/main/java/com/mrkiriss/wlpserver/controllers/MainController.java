package com.mrkiriss.wlpserver.controllers;

import com.mrkiriss.wlpserver.entity.LocationPoint;
import com.mrkiriss.wlpserver.model.CalibrationLocationPoint;
import com.mrkiriss.wlpserver.model.DefinedLocationPoint;
import com.mrkiriss.wlpserver.model.StringResponse;
import com.mrkiriss.wlpserver.services.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
public class MainController {

    @Autowired
    private MainService mainService;

    @PostMapping("/define/room")
    public ResponseEntity<?> getLocationPointWithRoom(@RequestBody CalibrationLocationPoint calibrationLocationPoint){
        try {
            System.out.println("Запрос на определение местоположения");
            DefinedLocationPoint resultPoint = mainService.definedLocationPointWithRoom(calibrationLocationPoint);
            if (resultPoint==null){
                System.out.println("Местоположение не оределено - пустой результат");
                return ResponseEntity.notFound().build();
            }
            System.out.println("Местоположение оределено успешно");
            return ResponseEntity.ok(resultPoint);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/training/room/aps")
    public ResponseEntity<?> addRoomWithoutCoordinates(@RequestBody CalibrationLocationPoint calibrationLocationPoint){
        try {
            System.out.println("Запрос на добавление точки по кабинету");
            LocationPoint locationPoint = mainService.savePointToBase(calibrationLocationPoint);
            System.out.println("Запрос обработан успешно\n");
            StringResponse response = new StringResponse();
            response.setResponse(locationPoint.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/training/room/coordinates")
    public ResponseEntity<?> addRoomCoordinates(@RequestBody CalibrationLocationPoint calibrationLocationPoint){
        try {
            System.out.println("Запрос на добавление точки по кабинету");
            LocationPoint locationPoint = mainService.savePointToBase(calibrationLocationPoint);
            System.out.println("Запрос обработан успешно\n");
            StringResponse response = new StringResponse();
            response.setResponse(locationPoint.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearServer(){
        try {
            System.out.println("Запрос на очистку сервера");
            StringResponse response = new StringResponse();
            response.setResponse("NumberOfLocationPoints="+mainService.clearServerDB());
            System.out.println("Запрос обработан успешно");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            e.printStackTrace();
            StringResponse response = new StringResponse();
            response.setResponse(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
