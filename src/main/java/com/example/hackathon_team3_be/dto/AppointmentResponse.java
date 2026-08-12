package com.example.hackathon_team3_be.dto;

import com.example.hackathon_team3_be.entity.AppointmentStatus;
import java.time.OffsetDateTime;

public record AppointmentResponse(String appointmentId,AppointmentStatus status,String experienceId,
                                  String unseenId,String storeId,String storeName,
                                  OffsetDateTime startAt,int durationMinutes) { }
