package com.mrkiriss.wlpserver.repositories;

import com.mrkiriss.wlpserver.entity.LocationPoint;
import com.mrkiriss.wlpserver.entity.LocationPointInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RepositoryRestResource(collectionResourceRel = "locationPointInfos", path = "locationPointInfos")
public interface LPInfoRepository extends CrudRepository<LocationPointInfo, Long> {
    LocationPointInfo findByRoomName(String roomName);
    void deleteByRoomName(String roomName);
}
