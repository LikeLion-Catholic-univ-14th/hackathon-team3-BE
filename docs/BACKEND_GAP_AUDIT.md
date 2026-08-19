# Re:SENSE Backend 누락 점검 결과

PDF/유저플로우와 현재 `main` 구현을 대조한 결과입니다.

## 이번에 보완한 항목

| 유저플로우 요구 | 기존 상태 | 보완 결과 |
|---|---|---|
| 선택지 기반 취향 조율 | 일괄 `PUT`만 존재 | 7단계 선택지 카탈로그, 단계 저장, 진행률/다음 질문/권장 전환 지연 제공 |
| 텍스트 확장 | 없음 | 한·영 키워드 해석 후 비어 있는 취향만 병합 |
| 음성 인식 확장 | 없음 | 음성 파일 STT 제공자 호출 + 브라우저 transcript 대체 경로 |
| 선택지 우선 정책 | 없음 | 텍스트·음성은 사용자가 이미 고른 필드를 덮어쓰지 않음 |
| 취향 이어가기 | Memory 조회만 존재 | 같은 고객이며 동의된 이전 세션을 새 세션으로 복사 |
| 진행 속도 맞춤 | 없음 | 매 응답에 진행률, 다음 단계, 안내 문구, 권장 지연(ms) 포함 |
| 예약 변경/취소 | 생성/조회만 존재 | 방문 전 변경·취소, 취소 후 재예약 및 슬롯 재사용 지원 |
| 동의 없는 장기 기억 | 조회 시 구분 없음 | Memory 조회에서 동의 세션만 반환 |
| 결과 생성 후 취향 수정 | 오래된 결과 잔존 가능 | Intent/UNSEEN/Advisor/Reveal 결과를 무효화하고 재생성하도록 상태 정리 |
| 피그마 고객 프로필 | 이름 외 필드 없음 | 선택 전화번호·이메일·성별 저장 및 세션 응답 제공 |
| 피그마 LOCK IT | 서버 선택지와 화면 문구 불일치 | `Shape / Color / Space / Attitude`로 일치시키고 Intent concern에 반영 |
| 해커톤 이미지 생성 한도 | 동적 SVG 한 장 | 제공된 PNG 8개에서 최대 4개 후보 생성, 후보 속성·순위·최종 선택 저장 |
| 위치 기반 매장 탐색 | 전체 매장 목록만 제공 | `GET /stores?city=Seoul` 선택 필터 추가, 권한 거부 시 기존 전체 목록 유지 |
| 피그마 결과 카드 | 단일 `imageUrl`만 존재 | `candidates` 4개 응답과 `PATCH /unseen/selection`, 예약 전 선택 검증 추가 |
| 예약 확정 Add to Calendar | API 없음 | `GET /reservations/{id}/calendar.ics`로 1시간 일정·매장·PASS 제공 |
| 잘못된 날짜/UUID·대용량 음성 | 공통 500 가능 | 형식 오류 400, 15MB 초과 413 공통 오류 응답 추가 |

## 외부 연동이 필요한 운영 항목

- 실제 STT 사용 시 `SPEECH_API_URL`, `SPEECH_API_KEY`, `SPEECH_MODEL` 설정
- 실제 이미지 생성 모델 사용 시 `UnseenGenerationService`의 랜덤 데모 이미지 선택부 교체
- 운영 DB 전환 시 H2 대신 PostgreSQL과 Flyway 마이그레이션 적용
- 정식 회원 인증이 확정되면 `demoCustomerId`를 인증 주체 ID로 교체
- 운영 환경의 개인정보 보존 기간, 삭제 API, 감사 로그 정책 확정

해커톤 데모 범위에서는 브라우저 Web Speech API의 transcript를 `browserTranscript`로 보내면 외부 STT 키 없이도 전체 흐름을 시연할 수 있습니다.
