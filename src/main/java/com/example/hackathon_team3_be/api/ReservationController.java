package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.api.ApiDtos.ReservationRequest;
import com.example.hackathon_team3_be.api.ApiDtos.ReservationResponse;
import com.example.hackathon_team3_be.api.ApiDtos.ReservationUpdateRequest;
import com.example.hackathon_team3_be.api.ApiDtos.SlotResponse;
import com.example.hackathon_team3_be.api.ApiDtos.StoreResponse;
import com.example.hackathon_team3_be.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    List<StoreResponse> stores(@RequestParam(required = false) String city) {
        return reservationService.getStores(city);
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

    @PatchMapping("/reservations/{reservationId}")
    ReservationResponse update(
            @PathVariable UUID reservationId,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return reservationService.update(reservationId, request);
    }

    @DeleteMapping("/reservations/{reservationId}")
    ResponseEntity<Void> cancel(@PathVariable UUID reservationId) {
        reservationService.cancel(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/reservations/{reservationId}/calendar.ics", produces = "text/calendar")
    ResponseEntity<String> calendar(@PathVariable UUID reservationId) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resense-" + reservationId + ".ics\"")
                .body(reservationService.calendar(reservationId));
    }
}
