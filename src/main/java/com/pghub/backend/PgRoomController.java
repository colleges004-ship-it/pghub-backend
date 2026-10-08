package com.pghub.backend;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().toString() + File.separator;

    // 1. ADD METHOD: Now accepts the List of facilities from the user form
    // 1. ADD METHOD: Safe from parameter assignment restrictions
    @PostMapping("/add")
    public ResponseEntity<?> addRoom(@RequestParam("roomNumber") String roomNumber,
                          @RequestParam("title") String title,
                          @RequestParam("location") String location,
                          @RequestParam("pricePerMonth") Double pricePerMonth,
                          @RequestParam("image") MultipartFile file,
                          @RequestParam(value = "facilities", required = false) List<String> facilities) throws IOException { 

        if(roomRepository.existsByRoomNumber(roomNumber)){
            return ResponseEntity
                    .badRequest()
                    .body(roomNumber+" this room no is already ragistered ");
        }

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
        Files.write(filePath, file.getBytes());
        
        String imageUrlPath = "/uploads/" + uniqueFileName;

        // FIXED: Using a distinct local variable name
        List<String> finalFacilities = (facilities == null) ? new java.util.ArrayList<>() : facilities;
        
        PgRoom newRoom = new PgRoom(roomNumber,title, location, pricePerMonth, imageUrlPath, finalFacilities);
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

    // 2. UPDATE METHOD: Now allows owners to update the dynamic services list
    // 2. UPDATE METHOD: Safe from lambda scope assignment errors
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRoom(
        @PathVariable Long id,
        @PathVariable("roomNumber") String roomNumber,
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

            // FIXED: Create a local variable inside the lambda scope to avoid reassigning a method parameter
            List<String> finalFacilities = (facilities == null) ? new java.util.ArrayList<>() : facilities;
            room.setFacilities(finalFacilities); 

            PgRoom updatedRoom = roomRepository.save(room);
            return ResponseEntity.ok(updatedRoom);
        }).orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/toggle-availability/{id}")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id){
        return roomRepository.findById(id).map(room ->
            {
                room.setAvailable(!room.isAvailable());
                PgRoom updateRoom= roomRepository.save(room);
                return ResponseEntity.ok(updateRoom);
            }
        ) .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/complaints/add")
    public ResponseEntity<?> addComplaint(@PathVariable Long id, @RequestParam("description") String description) {
        return roomRepository.findById(id).map(room -> {
            // Create a new complaint object linked to this room
            Complaint complaint = new Complaint(description, room);
            complaintRepository.save(complaint);
            return ResponseEntity.ok("Complaint filed successfully.");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/complaints/toggle/{complaintId}")
    public ResponseEntity<?> toggleComplaintStatus(@PathVariable Long complaintId) {
        return complaintRepository.findById(complaintId).map(complaint -> {
            // Switch the status (true -> false or false -> true)
            complaint.setResolved(!complaint.isResolved());
            complaintRepository.save(complaint);
            return ResponseEntity.ok("Complaint status altered.");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/")
        public String redirectToHome() {
        return "redirect:/rooms.html";
    }

    @GetMapping("/api/rooms/public")
        public List<PgRoom> getAllPublicRooms() {
        return roomRepository.findAll(); 
    }

    @PostMapping("/api/rooms")
        public ResponseEntity<PgRoom> createRoom(@RequestBody PgRoom room) {
         // Save room entity to database
        PgRoom savedRoom = roomRepository.save(room);
        return ResponseEntity.ok(savedRoom);
}


}