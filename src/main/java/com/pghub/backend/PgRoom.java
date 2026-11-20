package com.pghub.backend;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import com.fasterxml.jackson.annotation.JsonManagedReference;




@Entity
public class PgRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(name = "room_number", unique = true, nullable = false)
    private String roomNumber;

    private String title;
    private String location;
    private Double pricePerDay;
    private Integer capacity = 2;
    private String imageUrl;
    private boolean available=true;

    @ElementCollection
    @CollectionTable(name= "room_facilities" , joinColumns = @JoinColumn(name="room_id"))
    @Column(name = "facility_name")
    private List<String> facilities= new ArrayList<>();  
    
    @OneToMany(mappedBy = "pgRoom",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference

    private List<Complaint> complaints= new ArrayList<>();
    private List<Complaint> getComplaints(){
        return complaints;
    }
    public void  setComplaints( List<Complaint> complaints){ this.complaints=complaints;}

    // Add this new relationship for Tenants
    @OneToMany(mappedBy = "pgRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Tenant> tenants = new ArrayList<>();

    public List<Tenant> getTenants() { return tenants; }
    public void setTenants(List<Tenant> tenants) { this.tenants = tenants; }
        


    public PgRoom(){}

    public PgRoom( String roomNumber, String title, String location, Double pricePerDay, String imageUrl, List<String> facilities){
        this.roomNumber=roomNumber;
        this.title=title;
        this.location=location;
        this.pricePerDay=pricePerDay;
        this.imageUrl=imageUrl;
        this.facilities=facilities;
        this.available=true;
    }

    public Long getId(){ return id;}
    public void setId(long id){ this.id= id;}

    public String getRoomNumber(){return roomNumber;}
    public void setRoomNumber(String roomNumber){ this.roomNumber=roomNumber;}

    public String getTitle(){ return title;}
    public void setTitle(String title){ this.title=title;}

    public String getLocation(){ return location;}
    public void setLocation( String location){ this.location=location;}

    public Double getPricePerDay(){ return pricePerDay;}
    public void setPricePerDay( Double pricePerDay){this.pricePerDay=pricePerDay;}

    public Integer getCapacity() {return capacity;}
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getImageUrl(){ return imageUrl;}
    public void setImageUrl( String imageUrl){ this.imageUrl=imageUrl;}

    public List<String> getFacilities(){return facilities;}
    public void setFacilities( List<String> facilities){ this.facilities=facilities;}

    public boolean isAvailable(){return available;}
    public void setAvailable( boolean available){this.available=available;}



}