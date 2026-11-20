package com.pghub.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DailyPaymentController {

    @Autowired
    private DailyPaymentRepository dailyPaymentRepository;

    // 1. Fetch all daily payment records for a room
    @GetMapping("/rooms/{roomId}/payments")
    public ResponseEntity<List<DailyPayment>> getRoomDailyPayments(@PathVariable Long roomId) {
        return ResponseEntity.ok(dailyPaymentRepository.findByRoomIdOrderByStayDateAsc(roomId));
    }

    // 2. Approve and permanently lock a daily payment
    @PutMapping("/payments/{paymentId}/approve")
    public ResponseEntity<?> approveDailyPayment(
            @PathVariable Long paymentId,
            @RequestParam String paymentMode,
            @RequestParam(required = false, defaultValue = "") String transactionRef) {

        DailyPayment payment = dailyPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        // HARD IMMUTABILITY LOCK: Reject any modification once approved
        if (payment.isApproved()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Payment for " + payment.getStayDate() + " is already approved and permanently locked.");
        }

        payment.setApproved(true);
        payment.setPaymentMode(paymentMode.trim());
        payment.setTransactionRef(transactionRef.trim());
        payment.setApprovedAt(LocalDateTime.now());

        return ResponseEntity.ok(dailyPaymentRepository.save(payment));
    }
}