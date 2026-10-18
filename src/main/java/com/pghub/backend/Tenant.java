package com.pghub.backend;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String governmentIdNumber; // e.g., Aadhaar or Passport

   @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    // Link this tenant to a specific PG Room
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonBackReference
    private PgRoom pgRoom;

    public Tenant() {}

    public Tenant(String fullName, String phoneNumber, String governmentIdNumber, PgRoom pgRoom) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.governmentIdNumber = governmentIdNumber;
        this.checkInDate = LocalDate.now();
        this.checkOutDate = this.checkInDate.plusDays(1); // Default to 1 day
        this.pgRoom = pgRoom;
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGovernmentIdNumber() { return governmentIdNumber; }
    public void setGovernmentIdNumber(String governmentIdNumber) { this.governmentIdNumber = governmentIdNumber; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public PgRoom getPgRoom() { return pgRoom; }
    public void setPgRoom(PgRoom pgRoom) { this.pgRoom = pgRoom; }
}
