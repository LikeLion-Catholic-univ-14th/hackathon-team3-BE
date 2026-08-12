package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.entity.AppointmentEntity;
import com.example.hackathon_team3_be.entity.AppointmentStatus;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity,String> {
    boolean existsByStoreIdAndStartAtAndStatus(String storeId, OffsetDateTime startAt, AppointmentStatus status);
}
