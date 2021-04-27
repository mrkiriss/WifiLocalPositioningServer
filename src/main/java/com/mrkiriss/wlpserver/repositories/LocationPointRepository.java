package com.mrkiriss.wlpserver.repositories;


import com.mrkiriss.wlpserver.entity.LocationPoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RepositoryRestResource(collectionResourceRel = "locationPoints", path = "locationPoints")
public interface LocationPointRepository extends CrudRepository<LocationPoint, Long>{
    LocationPoint findByRoomName(String roomName);
}