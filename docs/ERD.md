# MCM Re:SENSE ERD

이 문서는 현재 `main` 브랜치에 구현된 JPA 엔티티를 기준으로 작성되었습니다.

## 1. 전체 ERD

```mermaid
erDiagram
    EXPERIENCE_SESSIONS ||--o| RESERVATIONS : "has"
    STORES ||--o{ RESERVATIONS : "accepts"
    EXPERIENCE_SESSIONS ||--o{ ADVISOR_EDITS : "receives"
    EXPERIENCE_SESSIONS ||--o{ UNSEEN_CANDIDATES : "compares"
    PRODUCTS ||--o{ ADVISOR_EDITS : "selected for"

    EXPERIENCE_SESSIONS {
        UUID id PK
        VARCHAR_80 demo_customer_id "데모 고객 식별자"
        VARCHAR_80 customer_name
        VARCHAR_30 phone
        VARCHAR_160 email
        VARCHAR_30 gender
        BOOLEAN data_consent
        VARCHAR_40 status "ExperienceStatus"
        VARCHAR silhouette
        VARCHAR structure_preference
        VARCHAR proportion
        VARCHAR color
        VARCHAR attitude
        VARCHAR_500 contexts "파이프(|) 구분"
        VARCHAR locked_attribute
        VARCHAR_30 last_input_mode "InputMode"
        VARCHAR_2000 free_text_input
        VARCHAR_2000 voice_transcript
        VARCHAR intent_purpose
        VARCHAR intent_priority
        VARCHAR intent_style
        VARCHAR intent_signature
        VARCHAR intent_concern
        VARCHAR_1000 intent_summary
        VARCHAR_30 unseen_status "GenerationStatus"
        VARCHAR_30 unseen_public_id UK
        VARCHAR_500 unseen_image_url
        VARCHAR_2000 unseen_prompt
        VARCHAR_1000 unseen_error
        VARCHAR advisor_priority
        VARCHAR_40 reveal_stage "RevealStage"
        VARCHAR_500 feedback_loved "파이프(|) 구분"
        VARCHAR_500 feedback_concern "파이프(|) 구분"
        VARCHAR_500 feedback_wants "파이프(|) 구분"
        VARCHAR_30 purchase_result "PurchaseResult"
        BIGINT purchased_product_id "논리 참조"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    STORES {
        BIGINT id PK
        VARCHAR_100 name UK
        VARCHAR_80 city
        VARCHAR_300 address
        BOOLEAN active
    }

    RESERVATIONS {
        UUID id PK
        UUID session_id FK,UK
        BIGINT store_id FK
        TIMESTAMP scheduled_at
        VARCHAR_30 status "ReservationStatus"
        VARCHAR_30 pass_code UK
        TIMESTAMP created_at
    }

    PRODUCTS {
        BIGINT id PK
        VARCHAR_50 sku UK
        VARCHAR_120 name
        VARCHAR_50 silhouette
        VARCHAR_50 structure_type
        VARCHAR_50 color
        VARCHAR_300 image_url
        BOOLEAN available
    }

    ADVISOR_EDITS {
        UUID id PK
        UUID session_id FK
        BIGINT product_id FK
        VARCHAR_40 direction "EditDirection"
        VARCHAR_100 strap
        VARCHAR_100 accessory
        VARCHAR_1000 rationale
        TIMESTAMP created_at
    }

    UNSEEN_CANDIDATES {
        UUID id PK
        UUID session_id FK
        VARCHAR_500 image_url
        VARCHAR_50 shape
        VARCHAR_30 size
        VARCHAR_50 color
        INT display_rank
        BOOLEAN selected
        TIMESTAMP created_at
    }
```

## 2. 관계와 제약조건

| 부모 | 자식 | 관계 | 구현 규칙 |
|---|---|---|---|
| `experience_sessions` | `reservations` | 1 : 0..1 | `reservations.session_id`가 Unique이므로 세션당 예약은 최대 1개입니다. |
| `stores` | `reservations` | 1 : N | 한 매장은 여러 예약을 받습니다. |
| `experience_sessions` | `advisor_edits` | 1 : N | 세션당 기본 3개 Personal Edit을 생성합니다. |
| `products` | `advisor_edits` | 1 : N | 하나의 상품이 여러 고객 세션의 Edit에 사용될 수 있습니다. |
| `experience_sessions` | `unseen_candidates` | 1 : N | 세션마다 최대 4개 결과 후보를 만들며 `(session_id, display_rank)`는 Unique입니다. |

예약 테이블에는 `(store_id, scheduled_at, status)` 복합 Unique 제약조건 `uk_store_schedule_status`가 적용됩니다. 활성 예약 중복은 서비스 계층에서도 차단하며, 취소된 슬롯은 다시 예약할 수 있습니다.

`experience_sessions.purchased_product_id`는 현재 해커톤 MVP에서 논리 참조값으로만 저장되며 실제 외래키는 아닙니다. 실제 운영 전환 시 `products.id` 외래키로 변경하는 것을 권장합니다.

## 3. 세션 애그리거트

현재 구현은 해커톤에서 End-to-End 흐름을 빠르게 연결하기 위해 다음 정보를 `experience_sessions` 하나에 모았습니다.

```mermaid
flowchart LR
    A["Demo Customer"] --> B["Preference"]
    B --> C["Intent Profile"]
    C --> D["UNSEEN"]
    D --> E["Advisor Touch"]
    E --> F["Reveal"]
    F --> G["Feedback / Purchase"]
```

- 고객: `demo_customer_id`, `customer_name`, 선택 프로필(`phone`, `email`, `gender`), `data_consent`
- 취향: `silhouette`부터 `locked_attribute`까지
- 입력 이력: 마지막 입력 모드, 자유 텍스트, 음성 transcript
- Intent Profile: `intent_purpose`부터 `intent_summary`까지
- UNSEEN: `unseen_status`, `unseen_public_id`, 최종 이미지·프롬프트·오류와 `unseen_candidates` 최대 4개
- 오프라인: `advisor_priority`, `reveal_stage`
- 기억: Loved, Concern, Wants, 구매 결과

`contexts`, `feedback_loved`, `feedback_concern`, `feedback_wants`는 현재 파이프 문자(`|`)로 구분해 저장하고 API에서는 배열로 변환합니다.

## 4. 상태 정의

### ExperienceStatus

```mermaid
stateDiagram-v2
    [*] --> CREATED
    CREATED --> PREFERENCES_SAVED: 취향 저장
    PREFERENCES_SAVED --> INTENT_READY: Intent 생성
    INTENT_READY --> UNSEEN_PROCESSING: UNSEEN 요청
    UNSEEN_PROCESSING --> UNSEEN_READY: 생성 성공
    UNSEEN_PROCESSING --> INTENT_READY: 생성 실패
    UNSEEN_READY --> RESERVED: 예약 완료
    RESERVED --> ARRIVED: PASS 인식
    ARRIVED --> PERSONAL_EDIT_READY: Personal Edit 생성
    PERSONAL_EDIT_READY --> REVEALING: Reveal 시작
    REVEALING --> PERSONAL_EDIT_READY: Reveal 완료
    PERSONAL_EDIT_READY --> COMPLETED: 피드백 저장
    REVEALING --> COMPLETED: 피드백 저장
```

값:

- `CREATED`
- `PREFERENCES_SAVED`
- `INTENT_READY`
- `UNSEEN_PROCESSING`
- `UNSEEN_READY`
- `RESERVED`
- `ARRIVED`
- `PERSONAL_EDIT_READY`
- `REVEALING`
- `COMPLETED`

### GenerationStatus

- `NOT_STARTED`
- `PROCESSING`
- `READY`
- `FAILED`

### ReservationStatus

- `BOOKED`
- `ARRIVED`
- `COMPLETED`
- `CANCELLED`

### RevealStage

`NOT_STARTED → WELCOME → UNSEEN_REVEAL → LIFESTYLE_SCENE → FINAL_TRANSITION → COMPLETED`

### PurchaseResult

- `PURCHASED`
- `NOT_PURCHASED`
- `UNDECIDED`

### EditDirection

- `THE_EVERYDAY`
- `THE_NOMAD`
- `THE_UNEXPECTED`

## 5. 운영 전환 시 권장 정규화

MVP 이후에는 다음 테이블 분리를 권장합니다.

1. `customers`, `customer_consents`: 실제 회원·동의 이력
2. `session_preferences`, `session_contexts`: 취향 및 다중 Context
3. `intent_profiles`: AI 모델·프롬프트 버전과 생성 이력
4. `unseens`: 현재 `unseen_candidates`를 포함한 재생성·선택 이력 상위 애그리거트
5. `feedback_items`: Loved, Concern, Wants 항목별 분석
6. `inventory`: 매장별 실제 재고와 이동 상태
7. `reveal_events`: Reveal 실행·단계별 이벤트 로그

현재 구조는 standalone 해커톤 데모에는 충분하며, 위 분리는 CRM·실재고·분석 기능을 도입할 때 적용하면 됩니다.
