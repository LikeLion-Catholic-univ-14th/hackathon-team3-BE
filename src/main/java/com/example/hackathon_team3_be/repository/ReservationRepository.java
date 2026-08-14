package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.domain.DomainEnums.ReservationStatus;
import com.example.hackathon_team3_be.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findBySessionId(UUID sessionId);
    Optional<Reservation> findByPassCodeIgnoreCase(String passCode);
    boolean existsByStoreIdAndScheduledAtAndStatusNot(Long storeId, LocalDateTime scheduledAt, ReservationStatus status);
    List<Reservation> findByStoreIdAndScheduledAtBetweenAndStatusNotOrderByScheduledAtAsc(
            Long storeId,
            LocalDateTime from,
            LocalDateTime to,
            ReservationStatus status
    );
    List<Reservation> findByScheduledAtBetweenAndStatusNotOrderByScheduledAtAsc(
            LocalDateTime from,
            LocalDateTime to,
            ReservationStatus status
    );
}
