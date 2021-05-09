package com.mrkiriss.wlpserver.controllers;

import com.mrkiriss.wlpserver.entity.AccessLevel;
import com.mrkiriss.wlpserver.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security")
public class AccessLevelController {

    @Autowired
    SecurityService securityService;

    @GetMapping("/level")
    public ResponseEntity<Integer> defineAccessLevel(@RequestParam("uuid") String uuid){
        try{
            System.out.println("Запрос на определение уровня доступа для uuid="+uuid);
            Integer result = securityService.defineAccessLevel(uuid);
            System.out.println("Уровень доступа для uuid: "+result);
            return ResponseEntity.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }
    @PostMapping("/level")
    public ResponseEntity<AccessLevel> setAccessLevel(@RequestParam("uuid") String uuid, @RequestParam("level") int level){
        try{
            System.out.println("Запрос на добавления уровня доступа с uuid="+uuid+" level="+level);
            AccessLevel result = securityService.addAccessLevel(uuid, level);
            System.out.println("Уровень доступа "+result+" добавлен");
            return ResponseEntity.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }
    @DeleteMapping("/level")
    public ResponseEntity<AccessLevel> deleteAccessLevel(@RequestParam("uuid") String uuid){
        try{
            System.out.println("Запрос на удаление уровня доступа с uuid="+uuid);
            AccessLevel result = securityService.deleteAccessLevel(uuid);
            System.out.println("Уровень доступа "+result+" удалён");
            return ResponseEntity.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(null);
        }
    }
}
