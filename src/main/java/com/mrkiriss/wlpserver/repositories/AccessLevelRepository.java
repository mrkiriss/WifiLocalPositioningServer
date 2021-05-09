package com.mrkiriss.wlpserver.repositories;

import com.mrkiriss.wlpserver.entity.AccessLevel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RepositoryRestResource(collectionResourceRel = "accessLevels", path = "accessLevels")
public interface AccessLevelRepository  extends CrudRepository<AccessLevel, String> {
    AccessLevel findByUuid(String uuid);
}