package com.linkedin.connections_service.repository;

import com.linkedin.connections_service.entity.Persons;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface PersonsRepository extends Neo4jRepository<Persons, Long> {

    Optional<Persons> getByName(String name);

    @Query("MATCH (PersonsA:Persons) -[:CONNECTED_TO]- (PersonsB:Persons) " +
            "WHERE PersonsA.userId = $userId " +
            "RETURN PersonsB")
    List<Persons> getFirstDegreeConnections(Long userId);

    @Query("MATCH (p1:Persons)-[r:REQUESTED_TO]->(p2:Persons) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "RETURN count(r) > 0")
    boolean connectionRequestExists(Long senderId, Long receiverId);

    @Query("MATCH (p1:Persons)-[r:CONNECTED_TO]-(p2:Persons) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "RETURN count(r) > 0")
    boolean alreadyConnected(Long senderId, Long receiverId);

    @Query("MATCH (p1:Persons), (p2:Persons) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "CREATE (p1)-[:REQUESTED_TO]->(p2)")
    void addConnectionRequest(Long senderId, Long receiverId);

    @Query("MATCH (p1:Persons)-[r:REQUESTED_TO]->(p2:Persons) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "DELETE r " +
            "CREATE (p1)-[:CONNECTED_TO]->(p2)")
    void acceptConnectionRequest(Long senderId, Long receiverId);

    @Query("MATCH (p1:Persons)-[r:REQUESTED_TO]->(p2:Persons) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "DELETE r")
    void rejectConnectionRequest(Long senderId, Long receiverId);
}
