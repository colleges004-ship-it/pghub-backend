package com.pghub.backend;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class PgRoomController {

    @Autowired
    private PgRoomRepository roomRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private static final String UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().toString() + File.separator;

    @PostMapping("/add")
    public ResponseEntity<?> addRoom(@RequestParam("roomNumber") String roomNumber,
                                     @RequestParam("title") String title,
                                     @RequestParam("location") String location,
                                     @RequestParam("pricePerMonth") Double pricePerMonth,
                                     @RequestParam(value = "capacity", defaultValue = "2") Integer capacity,
                                     @RequestParam("image") MultipartFile file,
                                     @RequestParam(value = "facilities", required = false) List<String> facilities) throws IOException { 

        if(roomRepository.existsByRoomNumber(roomNumber)){
            return ResponseEntity.badRequest().body(roomNumber+" this room no is already registered ");
        }

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
        Files.write(filePath, file.getBytes());
        
        String imageUrlPath = "/uploads/" + uniqueFileName;

        List<String> finalFacilities = (facilities == null) ? new java.util.ArrayList<>() : facilities;
        
        PgRoom newRoom = new PgRoom(roomNumber,title, location, pricePerMonth, imageUrlPath, finalFacilities);
        newRoom.setCapacity(capacity);
        return ResponseEntity.ok(roomRepository.save(newRoom));
    }    

    @GetMapping("/all")
    public List<PgRoom> getAllRooms() {
        return roomRepository.findAll();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        return roomRepository.findById(id).map(room -> {
            try {
                String imageUrl = room.getImageUrl();
                if (imageUrl != null && imageUrl.startsWith("/uploads")) {
                    String fileName = imageUrl.substring(("/uploads/").length());
                    Path filePath = Paths.get("uploads").resolve(fileName);
                    File file = filePath.toFile();
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                System.out.println("could not delete physical file: " + e.getMessage());
            }
            
            roomRepository.delete(room);
            return ResponseEntity.ok("Room with ID " + id + " deleted successfully!"); 
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRoom(
        @PathVariable Long id,
        @RequestParam("roomNumber") String roomNumber,
        @RequestParam("title") String title,
        @RequestParam("location") String location,
        @RequestParam("pricePerMonth") Double pricePerMonth,
        @RequestParam(value = "facilities", required = false) List<String> facilities 
    ) { 
        return roomRepository.findById(id).map(room -> {
            room.setRoomNumber(roomNumber);
            room.setTitle(title);
            room.setLocation(location);
            room.setPricePerMonth(pricePerMonth);

            List<String> finalFacilities = (facilities == null) ? new java.util.ArrayList<>() : facilities;
            room.setFacilities(finalFacilities); 

            PgRoom updatedRoom = roomRepository.save(room);
            return ResponseEntity.ok(updatedRoom);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/toggle-availability/{id}")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id){
        return roomRepository.findById(id).map(room -> {
            room.setAvailable(!room.isAvailable());
            PgRoom updateRoom = roomRepository.save(room);
            return ResponseEntity.ok(updateRoom);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/complaints/add")
    public ResponseEntity<?> addComplaint(@PathVariable Long id, @RequestParam("description") String description) {
        return roomRepository.findById(id).map(room -> {
            Complaint complaint = new Complaint(description, room);
            complaintRepository.save(complaint);
            return ResponseEntity.ok("Complaint filed successfully.");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/complaints/toggle/{complaintId}")
    public ResponseEntity<?> toggleComplaintStatus(@PathVariable Long complaintId) {
        return complaintRepository.findById(complaintId).map(complaint -> {
            complaint.setResolved(!complaint.isResolved());
            complaintRepository.save(complaint);
            return ResponseEntity.ok("Complaint status altered.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // FIXED: Corrected syntax errors in method signature and mapping logic
@PostMapping("/{id}/tenants/add")
    public ResponseEntity<?> addTenantsToRoom(
            @PathVariable Long id, 
            @RequestParam("familyData") String familyDataJson,
            @RequestParam(value = "idFiles", required = false) List<MultipartFile> idFiles) { // Made required = false
        
        return roomRepository.findById(id).map(room -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule()); 

                List<Tenant> familyMembers = mapper.readValue(familyDataJson, new TypeReference<List<Tenant>>(){});

                if (familyMembers.size() > room.getCapacity()) {
                    return ResponseEntity.badRequest().body("The number of family members exceeds the room's capacity.");
                }
                
                if (!room.isAvailable()) {
                    return ResponseEntity.badRequest().body("This room is already occupied by another family.");
                }

                for (int i = 0; i < familyMembers.size(); i++) {
                    Tenant tenant = familyMembers.get(i);

                    if(tenantRepository.existsByGovernmentIdNumber(tenant.getGovernmentIdNumber())) {
                        return ResponseEntity.badRequest().body("ID " + tenant.getGovernmentIdNumber() + " is already registered.");
                    }

                    // Safely check if a file was uploaded for this specific tenant
                    if (idFiles != null && idFiles.size() > i) {
                        MultipartFile file = idFiles.get(i);
                        if (file != null && !file.isEmpty()) {
                            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                            Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
                            Files.write(filePath, file.getBytes());
                            tenant.setIdProofImageUrl("/uploads/" + uniqueFileName);
                        } else {
                            tenant.setIdProofImageUrl(null); // Explicitly set to null if no file
                        }
                    } else {
                        tenant.setIdProofImageUrl(null);
                    }
                    
                    tenant.setPgRoom(room);
                    tenantRepository.save(tenant);
                }
                
                room.setAvailable(false);
                roomRepository.save(room);
                
                return ResponseEntity.ok("Family of " + familyMembers.size() + " registered successfully.");

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Error processing registration: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/dashboard.html";
    }

    // FIXED: Removed duplicate /api/rooms prefix, just map to /public
    @GetMapping("/public")
    public List<PgRoom> getAllPublicRooms() {
        return roomRepository.findAll(); 
    }

    @PostMapping("/tenants/{tenantId}/upload-id")
    public ResponseEntity<?> uploadTenantIdPhoto(@PathVariable Long tenantId,@RequestParam("file") MultipartFile file){
        return tenantRepository.findById(tenantId).map(tenant ->{
            try{ 
                if(file.isEmpty()) {
                    return ResponseEntity.badRequest().body("No file uploaded.");
                }
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                            Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
                            Files.write(filePath, file.getBytes());
                            tenant.setIdProofImageUrl("/uploads/" + uniqueFileName);
                            tenantRepository.save(tenant);

                            return ResponseEntity.ok("ID Photo uploaded successfully.");

            }
            catch(Exception e){
                return ResponseEntity.internalServerError().body("Error uploading file: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
            
        
    }
    
}