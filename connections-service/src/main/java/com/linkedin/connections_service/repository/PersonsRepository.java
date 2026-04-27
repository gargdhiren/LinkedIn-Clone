package com.linkedin.connections_service.repository;

import com.linkedin.connections_service.entity.Persons;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface PersonsRepository extends Neo4jRepository<Persons, Long> {
    Optional<Persons> getByName(String name);

    @Query("MATCH (personA:Persons) -[:CONNECTED_TO]- (personB:Persons) WHERE personA.userId = $userId RETURN personB")
    List<Persons> getFirstDegreeConnections(Long userId);
}
