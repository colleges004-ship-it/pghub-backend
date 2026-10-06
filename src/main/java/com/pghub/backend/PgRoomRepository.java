
package com.pghub.backend;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PgRoomRepository extends JpaRepository<PgRoom,Long>{
boolean existsByRoomNumber(String roomNumber);
    
}