package com.mrkiriss.wlpserver.repositories;

import com.mrkiriss.wlpserver.entity.Connection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RepositoryRestResource(collectionResourceRel = "connections", path = "connections")
public interface ConnectionRepository extends CrudRepository<Connection, Long> {
    Connection findByFirstNameAndSecondName(String firstName, String secondName);
    List<Connection> findAllByFirstName(String firstName);
    List<Connection> findAllBySecondName(String secondName);
    void deleteConnectionByFirstNameAndSecondName(String firstName, String secondName);


}
