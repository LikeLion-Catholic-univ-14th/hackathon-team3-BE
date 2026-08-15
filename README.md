# MCM Re:SENSE Backend

Spring Boot 기반 Re:SENSE 해커톤 MVP 백엔드입니다. 고객 웹, Advisor 웹, 매장 Reveal 디스플레이가 하나의 `sessionId`로 연결됩니다.

## 설계 문서

- [ERD](docs/ERD.md)
- [API 명세서](docs/API_SPEC.md)
- [OpenAPI 3.0 문서](docs/openapi.yaml)

## 실행

요구 환경은 Java 17 이상입니다. 현재 빌드는 Java 21 컴파일러로 Java 17 호환 바이트코드를 만듭니다.

```powershell
.\gradlew.bat bootRun
```

- API: `http://localhost:8080/api/v1`
- H2 Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:resense`
- Username: `sa`
- Password: 비워둠

프론트엔드 주소는 기본적으로 `http://localhost:3000`, `http://localhost:5173`을 허용합니다. 배포 시 `CORS_ALLOWED_ORIGINS` 환경변수에 쉼표로 구분해 지정합니다.

## 구현 범위

- 데모 고객 식별자와 경험 Session 생성
- 선택지 우선 7단계 취향 입력과 진행률/전환 속도 응답
- 자유 텍스트 키워드 해석 및 기존 선택값 보존 병합
- 음성 파일 STT API 연동과 브라우저 transcript 데모 대체 경로
- 동의한 고객의 이전 취향 이어가기
- 교체 가능한 `IntentInterpreter`와 기본 규칙 기반 Intent 생성
- 트랜잭션 커밋 이후 비동기 UNSEEN 생성
- 외부 이미지 API가 없어도 동작하는 동적 SVG 데모 이미지
- 매장/시간 슬롯 조회 및 중복 예약 방지
- 방문 전 예약 변경·취소 및 취소 슬롯 재사용
- UNSEEN PASS 발급 및 매장 도착 인식
- Advisor Intent Card와 Advisor Touch
- 더미 상품 DB 기반 Personal Edit 3방향 생성
- SSE 기반 Store Reveal 상태 전달
- 구매/미구매 피드백 및 데모 고객 Memory 조회
- 공통 검증·예외 응답과 CORS 설정

## 주요 API 순서

### 1. 세션 생성

`POST /api/v1/sessions`

```json
{
  "demoCustomerId": "demo-lena",
  "customerName": "Lena",
  "dataConsent": true
}
```

### 2. 취향 저장

프론트는 먼저 `GET /api/v1/preference-options`로 7단계 선택지를 받고, 단계마다 아래 API를 호출합니다.

`POST /api/v1/sessions/{sessionId}/inputs/choice`

```json
{
  "step": "SILHOUETTE",
  "values": ["Crossbody"]
}
```

응답의 `progress.percent`, `nextStep`, `recommendedTransitionDelayMs`, `paceMessage`로 진행 화면 속도를 맞출 수 있습니다.

보조 입력은 다음 API를 사용합니다. 텍스트/음성에서 해석된 값은 이미 고른 선택지를 덮어쓰지 않습니다.

- `POST /api/v1/sessions/{sessionId}/inputs/text` — `{ "text": "..." }`
- `POST /api/v1/sessions/{sessionId}/inputs/voice` — `multipart/form-data`
  - `audio`: 선택, mp3/mp4/wav/webm/ogg
  - `browserTranscript`: 선택, Web Speech API 결과
  - `language`: 기본값 `ko`
- `GET /api/v1/sessions/{sessionId}/input-progress`
- `POST /api/v1/sessions/{sessionId}/preferences/continue`

서버 STT는 OpenAI 호환 transcription API를 기준으로 `SPEECH_API_URL`, `SPEECH_API_KEY`, `SPEECH_MODEL` 환경변수로 설정합니다. 설정이 없는 데모에서는 `browserTranscript`를 사용합니다.

전체 취향을 한 번에 저장해야 하는 기존 API도 유지됩니다.

`PUT /api/v1/sessions/{sessionId}/preferences`

```json
{
  "silhouette": "Crossbody",
  "structure": "Soft",
  "proportion": "Compact-Spacious",
  "color": "Cognac",
  "attitude": "Quiet",
  "contexts": ["Work", "Travel"],
  "lockedAttribute": "Shape"
}
```

### 3. Intent 및 UNSEEN 생성

- `POST /api/v1/sessions/{sessionId}/intent`
- `POST /api/v1/sessions/{sessionId}/unseen` - `202 Accepted`
- `GET /api/v1/sessions/{sessionId}/unseen` - `READY`가 될 때까지 조회

실제 이미지 생성 API를 붙일 때는 `UnseenGenerationService`의 생성 부분만 교체하면 됩니다.

### 4. 예약

- `GET /api/v1/stores`
- `GET /api/v1/stores/{storeId}/slots?date=2026-08-15`
- `POST /api/v1/reservations`
- `PATCH /api/v1/reservations/{reservationId}`
- `DELETE /api/v1/reservations/{reservationId}`

```json
{
  "sessionId": "SESSION_UUID",
  "storeId": 1,
  "scheduledAt": "2026-08-15T11:00:00"
}
```

### 5. Advisor

- `GET /api/v1/advisor/appointments?date=2026-08-15&storeId=1`
- `POST /api/v1/advisor/passes/{passCode}/recognize`
- `GET /api/v1/advisor/sessions/{sessionId}/intent-card`
- `PATCH /api/v1/advisor/sessions/{sessionId}/touch`
- `POST /api/v1/advisor/sessions/{sessionId}/personal-edits`
- `GET /api/v1/advisor/sessions/{sessionId}/personal-edits`

Advisor Touch 요청:

```json
{
  "priority": "Styling"
}
```

### 6. Store Reveal

- `GET /api/v1/reveal/sessions/{sessionId}/events` - SSE 구독
- `POST /api/v1/reveal/sessions/{sessionId}/start`
- `POST /api/v1/reveal/sessions/{sessionId}/advance`
- `GET /api/v1/reveal/sessions/{sessionId}`

Reveal 단계는 `WELCOME → UNSEEN_REVEAL → LIFESTYLE_SCENE → FINAL_TRANSITION → COMPLETED` 순서입니다.

### 7. 피드백과 기억

`POST /api/v1/sessions/{sessionId}/feedback`

```json
{
  "loved": ["Shape", "Styling"],
  "concerns": ["Weight"],
  "wants": ["Smaller Size"],
  "result": "NOT_PURCHASED",
  "purchasedProductId": null
}
```

`GET /api/v1/customers/{demoCustomerId}/memory`

## 상태 흐름

`CREATED → PREFERENCES_SAVED → INTENT_READY → UNSEEN_PROCESSING → UNSEEN_READY → RESERVED → ARRIVED → PERSONAL_EDIT_READY / REVEALING → COMPLETED`

## 테스트

```powershell
.\gradlew.bat clean test
```

기본 테스트는 애플리케이션 컨텍스트와 전체 JPA 매핑이 정상적으로 기동되는지 검증합니다.
