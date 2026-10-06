package com.pghub.backend;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Table(name="Complaint")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean resolved= false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="roomId",nullable = false)
    @JsonBackReference

    private PgRoom pgRoom;

    public Complaint(){}

    public Complaint(String description, PgRoom pgRoom){
        this.description=description;
        this.pgRoom=pgRoom;
        this.resolved=false;
        this.createdAt=LocalDateTime.now();
    }

    public Long getId(){return id;}
    public void setId( Long id){ this.id=id;}

    public String getDescription(){ return description;}
    public void setDescription(String description){ this.description=description;}

    public PgRoom getPgRoom(){return pgRoom;}
    public void setPgRoom( PgRoom pgRoom){ this.pgRoom=pgRoom;}

    public boolean isResolved(){return resolved;}
    public void setResolved(boolean resolved){ this.resolved=resolved;}

    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt( LocalDateTime createdAt){this.createdAt=createdAt;}
    
}
