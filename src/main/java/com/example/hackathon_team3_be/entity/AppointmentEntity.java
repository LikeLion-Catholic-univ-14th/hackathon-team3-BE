package com.example.hackathon_team3_be.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name="appointments",uniqueConstraints=@UniqueConstraint(columnNames={"store_id","start_at"}))
public class AppointmentEntity {
    @Id private String appointmentId;
    @Column(name="experience_id",nullable=false) private String experienceId;
    @Column(name="unseen_id",nullable=false) private String unseenId;
    @Column(name="store_id",nullable=false) private String storeId;
    @Column(name="start_at",nullable=false) private OffsetDateTime startAt;
    private int durationMinutes;
    private String customerName;
    private String phone;
    private String email;
    @Enumerated(EnumType.STRING) private AppointmentStatus status;
    private Instant createdAt;

    protected AppointmentEntity() { }
    public AppointmentEntity(String appointmentId,String experienceId,String unseenId,String storeId,
                             OffsetDateTime startAt,int durationMinutes,String customerName,
                             String phone,String email) {
        this.appointmentId=appointmentId;this.experienceId=experienceId;this.unseenId=unseenId;
        this.storeId=storeId;this.startAt=startAt;this.durationMinutes=durationMinutes;
        this.customerName=customerName;this.phone=phone;this.email=email;
        this.status=AppointmentStatus.CONFIRMED;this.createdAt=Instant.now();
    }
    public String getAppointmentId(){return appointmentId;} public String getExperienceId(){return experienceId;}
    public String getUnseenId(){return unseenId;} public String getStoreId(){return storeId;}
    public OffsetDateTime getStartAt(){return startAt;} public int getDurationMinutes(){return durationMinutes;}
    public String getCustomerName(){return customerName;} public String getPhone(){return phone;}
    public String getEmail(){return email;} public AppointmentStatus getStatus(){return status;}
}
