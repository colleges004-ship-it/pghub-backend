package com.pghub.backend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ComplaintRepository extends JpaRepository<Complaint,Long>{

    
} 
    

