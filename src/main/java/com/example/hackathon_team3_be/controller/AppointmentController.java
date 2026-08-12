package com.example.hackathon_team3_be.controller;

import com.example.hackathon_team3_be.dto.*;
import com.example.hackathon_team3_be.service.AppointmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service){this.service=service;}
    @GetMapping("/stores") public List<StoreResponse> stores(){return service.stores();}
    @GetMapping("/stores/{storeId}/appointment-slots")
    public List<AppointmentSlotResponse> slots(@PathVariable String storeId,@RequestParam LocalDate date){return service.slots(storeId,date);}
    @PostMapping("/appointments") public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest r){
        AppointmentResponse response=service.create(r);
        return ResponseEntity.created(URI.create("/api/appointments/"+response.appointmentId())).body(response);
    }
    @GetMapping("/appointments/{id}") public AppointmentResponse get(@PathVariable String id){return service.get(id);}
}
