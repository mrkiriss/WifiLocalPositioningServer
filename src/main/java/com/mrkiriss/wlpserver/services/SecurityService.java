package com.mrkiriss.wlpserver.services;

import com.mrkiriss.wlpserver.entity.AccessLevel;
import com.mrkiriss.wlpserver.repositories.AccessLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    @Autowired
    AccessLevelRepository accessLevelRepository;

    public Integer defineAccessLevel(String uuid){
        AccessLevel accessLevel = accessLevelRepository.findByUuid(uuid);
        if (accessLevel==null){
            return 0;
        }else{
            return accessLevel.getLevel();
        }
    }

    public AccessLevel addAccessLevel(String uuid, int level){
        AccessLevel accessLevel = new AccessLevel();
        accessLevel.setLevel(level);
        accessLevel.setUuid(uuid);

        // исключаем дублирования попыткой удаления
        deleteAccessLevel(uuid);

        accessLevelRepository.save(accessLevel);
        return accessLevel;
    }

    public AccessLevel deleteAccessLevel(String uuid){
        AccessLevel findResult = accessLevelRepository.findByUuid(uuid);
        if (findResult!=null){
            accessLevelRepository.delete(findResult);
            return findResult;
        }else{
            return null;
        }
    }
}
