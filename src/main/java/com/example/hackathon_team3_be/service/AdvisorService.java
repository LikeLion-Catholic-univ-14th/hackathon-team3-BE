package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.api.ApiDtos.AdvisorTouchRequest;
import com.example.hackathon_team3_be.api.ApiDtos.IntentCardResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PassRecognitionResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PersonalEditResponse;
import com.example.hackathon_team3_be.common.InvalidStateException;
import com.example.hackathon_team3_be.domain.AdvisorEdit;
import com.example.hackathon_team3_be.domain.DomainEnums.EditDirection;
import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.domain.Product;
import com.example.hackathon_team3_be.domain.Reservation;
import com.example.hackathon_team3_be.repository.AdvisorEditRepository;
import com.example.hackathon_team3_be.repository.ExperienceProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdvisorService {

    private static final List<EditDirection> DIRECTIONS = List.of(
            EditDirection.THE_EVERYDAY,
            EditDirection.THE_NOMAD,
            EditDirection.THE_UNEXPECTED
    );

    private final JourneyService journeyService;
    private final ReservationService reservationService;
    private final ExperienceProductRepository productRepository;
    private final AdvisorEditRepository advisorEditRepository;

    @Transactional(readOnly = true)
    public IntentCardResponse intentCard(UUID sessionId) {
        ExperienceSession session = journeyService.findSession(sessionId);
        Reservation reservation = reservationService.findBySession(sessionId);
        return new IntentCardResponse(
                sessionId,
                session.getCustomerName(),
                session.getUnseenPublicId(),
                session.getUnseenImageUrl(),
                ApiMapper.toIntent(session),
                session.getAdvisorPriority(),
                ApiMapper.toReservation(reservation)
        );
    }

    @Transactional
    public IntentCardResponse advisorTouch(UUID sessionId, AdvisorTouchRequest request) {
        ExperienceSession session = journeyService.findSession(sessionId);
        session.setAdvisorPriority(request.priority().trim());
        return intentCard(sessionId);
    }

    @Transactional
    public PersonalEditResponse generateEdits(UUID sessionId) {
        ExperienceSession session = journeyService.findSession(sessionId);
        if (session.getIntentPurpose() == null) {
            throw new InvalidStateException("Intent Profile이 없어 Personal Edit을 만들 수 없습니다.");
        }
        List<Product> products = productRepository.findByAvailableTrueOrderByIdAsc();
        if (products.size() < 3) {
            throw new InvalidStateException("Personal Edit용 상품 데이터가 부족합니다.");
        }
        advisorEditRepository.deleteBySessionId(sessionId);
        List<AdvisorEdit> edits = IntStream.range(0, 3).mapToObj(index -> buildEdit(
                session, products.get(index % products.size()), DIRECTIONS.get(index)
        )).map(advisorEditRepository::save).toList();
        session.setStatus(ExperienceStatus.PERSONAL_EDIT_READY);
        return new PersonalEditResponse(sessionId, edits.stream().map(ApiMapper::toEdit).toList());
    }

    @Transactional(readOnly = true)
    public PersonalEditResponse getEdits(UUID sessionId) {
        journeyService.findSession(sessionId);
        return new PersonalEditResponse(
                sessionId,
                advisorEditRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                        .map(ApiMapper::toEdit)
                        .toList()
        );
    }

    @Transactional
    public PassRecognitionResponse recognizePass(String passCode) {
        Reservation reservation = reservationService.recognizePass(passCode);
        ExperienceSession session = reservation.getSession();
        return new PassRecognitionResponse(
                true,
                session.getId(),
                session.getCustomerName(),
                session.getUnseenPublicId(),
                ApiMapper.toReservation(reservation)
        );
    }

    private AdvisorEdit buildEdit(ExperienceSession session, Product product, EditDirection direction) {
        AdvisorEdit edit = new AdvisorEdit();
        edit.setSession(session);
        edit.setProduct(product);
        edit.setDirection(direction);
        switch (direction) {
            case THE_EVERYDAY -> {
                edit.setStrap("Classic short leather strap");
                edit.setAccessory("Minimal keyring");
                edit.setRationale("%s와 일상 활용성을 중심으로 가장 자연스러운 구성을 준비했습니다."
                        .formatted(session.getIntentSignature()));
            }
            case THE_NOMAD -> {
                edit.setStrap("Adjustable comfort crossbody strap");
                edit.setAccessory("Travel tag charm");
                edit.setRationale("%s 우선순위와 이동이 많은 Context에 맞춘 가벼운 구성입니다."
                        .formatted(session.getIntentPriority()));
            }
            case THE_UNEXPECTED -> {
                edit.setStrap("Contrast webbing strap");
                edit.setAccessory("Statement charm");
                edit.setRationale("기본 취향은 유지하면서 %s에 새로운 포인트를 더하는 구성입니다."
                        .formatted(session.getAttitude()));
            }
        }
        return edit;
    }
}
