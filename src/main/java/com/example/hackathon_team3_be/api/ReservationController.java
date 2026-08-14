package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.api.ApiDtos.ReservationRequest;
import com.example.hackathon_team3_be.api.ApiDtos.ReservationResponse;
import com.example.hackathon_team3_be.api.ApiDtos.SlotResponse;
import com.example.hackathon_team3_be.api.ApiDtos.StoreResponse;
import com.example.hackathon_team3_be.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/stores")
    List<StoreResponse> stores() {
        return reservationService.getStores();
    }

    @GetMapping("/stores/{storeId}/slots")
    List<SlotResponse> slots(@PathVariable Long storeId, @RequestParam LocalDate date) {
        return reservationService.getSlots(storeId, date);
    }

    @PostMapping("/reservations")
    ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.reserve(request));
    }

    @GetMapping("/sessions/{sessionId}/reservation")
    ReservationResponse reservation(@PathVariable UUID sessionId) {
        return reservationService.getBySession(sessionId);
    }
}
