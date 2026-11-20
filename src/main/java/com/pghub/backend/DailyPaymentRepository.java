package com.pghub.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPaymentRepository extends JpaRepository<DailyPayment, Long> {
    List<DailyPayment> findByRoomIdOrderByStayDateAsc(Long roomId);
    List<DailyPayment> findByRoomIdAndStayDateBetween(Long roomId, LocalDate start, LocalDate end);
}
