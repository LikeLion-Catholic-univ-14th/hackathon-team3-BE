package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.api.ApiDtos.AppointmentResponse;
import com.example.hackathon_team3_be.api.ApiDtos.ReservationRequest;
import com.example.hackathon_team3_be.api.ApiDtos.ReservationResponse;
import com.example.hackathon_team3_be.api.ApiDtos.ReservationUpdateRequest;
import com.example.hackathon_team3_be.api.ApiDtos.SlotResponse;
import com.example.hackathon_team3_be.api.ApiDtos.StoreResponse;
import com.example.hackathon_team3_be.common.ConflictException;
import com.example.hackathon_team3_be.common.InvalidStateException;
import com.example.hackathon_team3_be.common.NotFoundException;
import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.GenerationStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.ReservationStatus;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.domain.Reservation;
import com.example.hackathon_team3_be.domain.Store;
import com.example.hackathon_team3_be.repository.ReservationRepository;
import com.example.hackathon_team3_be.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;
    private final JourneyService journeyService;

    @Transactional(readOnly = true)
    public List<StoreResponse> getStores() {
        return storeRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(store -> new StoreResponse(store.getId(), store.getName(), store.getCity(), store.getAddress()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlots(Long storeId, LocalDate date) {
        requireStore(storeId);
        Set<LocalDateTime> reserved = new HashSet<>(reservationRepository
                .findByStoreIdAndScheduledAtBetweenAndStatusNotOrderByScheduledAtAsc(
                        storeId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), ReservationStatus.CANCELLED
                ).stream().map(Reservation::getScheduledAt).toList());
        return IntStream.rangeClosed(11, 18)
                .mapToObj(hour -> LocalDateTime.of(date, LocalTime.of(hour, 0)))
                .map(time -> new SlotResponse(date, time, !reserved.contains(time)))
                .toList();
    }

    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {
        validateSlot(request.scheduledAt());
        ExperienceSession session = journeyService.findSession(request.sessionId());
        if (session.getUnseenStatus() != GenerationStatus.READY) {
            throw new InvalidStateException("UNSEEN 생성 완료 후 예약할 수 있습니다.");
        }
        Reservation previous = reservationRepository.findBySessionId(session.getId()).orElse(null);
        if (previous != null && previous.getStatus() != ReservationStatus.CANCELLED) {
            throw new ConflictException("이 세션에는 이미 예약이 있습니다.");
        }
        if (reservationRepository.existsByStoreIdAndScheduledAtAndStatusNot(
                request.storeId(), request.scheduledAt(), ReservationStatus.CANCELLED
        )) {
            throw new ConflictException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }
        Store store = requireStore(request.storeId());
        Reservation reservation = previous == null ? new Reservation() : previous;
        if (previous == null) {
            reservation.setSession(session);
            reservation.setPassCode("PASS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        reservation.setStore(store);
        reservation.setScheduledAt(request.scheduledAt());
        reservation.setStatus(ReservationStatus.BOOKED);
        session.setStatus(ExperienceStatus.RESERVED);
        return ApiMapper.toReservation(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse update(UUID reservationId, ReservationUpdateRequest request) {
        validateSlot(request.scheduledAt());
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() != ReservationStatus.BOOKED) {
            throw new InvalidStateException("방문 전 예약만 변경할 수 있습니다.");
        }
        if (reservationRepository.existsByStoreIdAndScheduledAtAndStatusNot(
                request.storeId(), request.scheduledAt(), ReservationStatus.CANCELLED
        ) && !(reservation.getStore().getId().equals(request.storeId())
                && reservation.getScheduledAt().equals(request.scheduledAt()))) {
            throw new ConflictException("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
        }
        reservation.setStore(requireStore(request.storeId()));
        reservation.setScheduledAt(request.scheduledAt());
        return ApiMapper.toReservation(reservation);
    }

    @Transactional
    public void cancel(UUID reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() != ReservationStatus.BOOKED) {
            throw new InvalidStateException("방문 전 예약만 취소할 수 있습니다.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.getSession().setStatus(ExperienceStatus.UNSEEN_READY);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getBySession(UUID sessionId) {
        return ApiMapper.toReservation(findBySession(sessionId));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> appointments(LocalDate date, Long storeId) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        List<Reservation> reservations = storeId == null
                ? reservationRepository.findByScheduledAtBetweenAndStatusNotOrderByScheduledAtAsc(from, to, ReservationStatus.CANCELLED)
                : reservationRepository.findByStoreIdAndScheduledAtBetweenAndStatusNotOrderByScheduledAtAsc(
                        storeId, from, to, ReservationStatus.CANCELLED
                );
        return reservations.stream().map(reservation -> new AppointmentResponse(
                ApiMapper.toReservation(reservation),
                reservation.getSession().getCustomerName(),
                reservation.getSession().getUnseenPublicId(),
                reservation.getSession().getIntentSummary()
        )).toList();
    }

    @Transactional
    public Reservation recognizePass(String passCode) {
        Reservation reservation = reservationRepository.findByPassCodeIgnoreCase(passCode)
                .orElseThrow(() -> new NotFoundException("UNSEEN PASS를 인식할 수 없습니다."));
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidStateException("취소된 예약입니다.");
        }
        reservation.setStatus(ReservationStatus.ARRIVED);
        reservation.getSession().setStatus(ExperienceStatus.ARRIVED);
        return reservation;
    }

    @Transactional(readOnly = true)
    public Reservation findBySession(UUID sessionId) {
        return reservationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
    }

    private Reservation findReservation(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다: " + reservationId));
    }

    private void validateSlot(LocalDateTime scheduledAt) {
        if (scheduledAt.getMinute() != 0
                || scheduledAt.getSecond() != 0
                || scheduledAt.getHour() < 11
                || scheduledAt.getHour() > 18) {
            throw new InvalidStateException("예약은 11:00부터 18:00까지 정각 슬롯으로 선택해 주세요.");
        }
    }

    private Store requireStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("매장을 찾을 수 없습니다: " + storeId));
        if (!store.isActive()) {
            throw new InvalidStateException("현재 예약할 수 없는 매장입니다.");
        }
        return store;
    }
}
