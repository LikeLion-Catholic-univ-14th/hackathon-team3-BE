package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.dto.*;
import com.example.hackathon_team3_be.entity.*;
import com.example.hackathon_team3_be.exception.*;
import com.example.hackathon_team3_be.repository.*;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    public static final String STORE_ID="STORE_HAUS_SEOUL";
    public static final String STORE_NAME="MCM HAUS SEOUL";
    private final int durationMinutes;
    private final LocalTime openingTime;
    private final LocalTime closingTime;
    private final AppointmentRepository appointments; private final ExperienceService experiences;
    private final UnseenBagRepository unseenBags;

    public AppointmentService(AppointmentRepository appointments,ExperienceService experiences,
                              UnseenBagRepository unseenBags,
                              @Value("${appointment.slot-minutes:30}") int durationMinutes,
                              @Value("${appointment.opening-time:11:00}") LocalTime openingTime,
                              @Value("${appointment.closing-time:19:00}") LocalTime closingTime){
        this.appointments=appointments; this.experiences=experiences; this.unseenBags=unseenBags;
        this.durationMinutes=durationMinutes; this.openingTime=openingTime; this.closingTime=closingTime;
    }

    public List<StoreResponse> stores(){return List.of(new StoreResponse(STORE_ID,STORE_NAME,
            null,null,null,null,true,durationMinutes,"prototype-unverified"));}

    public List<AppointmentSlotResponse> slots(String storeId, LocalDate date){
        requireStore(storeId);
        if(durationMinutes<=0 || !openingTime.isBefore(closingTime))
            throw new IllegalStateException("Invalid appointment slot configuration");
        long slotCount=Duration.between(openingTime,closingTime).toMinutes()/durationMinutes;
        return Stream.iterate(openingTime,time->time.plusMinutes(durationMinutes)).limit(slotCount)
                .map(time->OffsetDateTime.of(date,time,ZoneOffset.ofHours(9)))
                .map(start->new AppointmentSlotResponse(start,!appointments
                        .existsByStoreIdAndStartAtAndStatus(storeId,start,AppointmentStatus.CONFIRMED)))
                .toList();
    }

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest r){
        requireStore(r.storeId()); experiences.getRequired(r.experienceId());
        UnseenBagEntity unseen=unseenBags.findById(r.unseenId())
                .orElseThrow(()->new UnseenNotFoundException(r.unseenId()));
        if(!unseen.getExperienceId().equals(r.experienceId()))
            throw new IllegalArgumentException("UNSEEN bag does not belong to the experience");
        boolean offered=slots(r.storeId(),r.startAt().toLocalDate()).stream()
                .anyMatch(s->s.startAt().isEqual(r.startAt())&&s.available());
        if(!offered)throw new AppointmentSlotUnavailableException();
        String id="APT_"+UUID.randomUUID().toString().replace("-","").substring(0,10).toUpperCase();
        return response(appointments.save(new AppointmentEntity(id,r.experienceId(),r.unseenId(),
                r.storeId(),r.startAt(),durationMinutes,r.customerName(),r.phone(),r.email())));
    }

    public AppointmentResponse get(String id){return response(appointments.findById(id)
            .orElseThrow(()->new AppointmentNotFoundException(id)));}
    private static void requireStore(String id){if(!STORE_ID.equals(id))throw new IllegalArgumentException("Unknown store: "+id);}
    private static AppointmentResponse response(AppointmentEntity a){return new AppointmentResponse(
            a.getAppointmentId(),a.getStatus(),a.getExperienceId(),a.getUnseenId(),a.getStoreId(),
            STORE_NAME,a.getStartAt(),a.getDurationMinutes());}
}
