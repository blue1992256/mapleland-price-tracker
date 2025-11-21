# 📌 PRD — Mapleland Item Price Tracker

## 1. 개요 (Overview)

Mapleland Item Price Tracker는 메이플스토리월드 내 게임 '메이플랜드'의 아이템 시장 가격을 자동으로 수집·분석하여 매일 평균 가격을 사용자에게 제공하는 웹 서비스이다.
사용자는 사이트에 접속하여 특정 아이템의 일간 가격 변화, 최근 평균 가격, 가격 그래프, 가격 급변 알림 등을 확인할 수 있다.

## 2. 목적 (Purpose)

- 메이플랜드 내 아이템 거래 가격이 사용자 간에 분산되어 있어 가격 파악이 어려운 문제 해결
- 특정 아이템의 가격 변동을 쉽게 추적하여 거래 시 적정 가격 판단에 도움 제공
- 유저들이 실시간 가격 정보 수집을 자동화하여 반복 작업을 줄임

## 3. 핵심 기능 (Key Features)

### 3.1. 아이템 가격 수집 (크롤링/스크래핑)

**대상:** https://mapleland.gg 의 아이템 거래 페이지(상품 리스트)

**수집 데이터:**
- 아이템 이름
- 판매자(선택)
- 등록 가격 (메소)
- 등록 시간
- 이미지 URL (있다면)

**주기:** 매일 1회(00:00 ~ 05:00 사이) 예약 실행

**수집 방식:**
- 공개된 HTML 기반 크롤링(비로그인 기준)
- API가 있을 경우 API 사용 고려
- HTML 구조 변경 시 예외 처리 필요

### 3.2. 가격 통계 저장

**저장 항목:**
- item_id
- date
- min_price
- max_price
- avg_price
- median_price
- count(등록 수)

### 3.3. 아이템별 가격 조회 기능

- 오늘/과거 n일의 가격 리스트 조회
- 일별 평균 가격 그래프 제공
- 가장 최근 가격 정보 제공

### 3.4. 가격 변동 알림 (옵션 / MVP 제외 가능)

- 특정 아이템 저장(watchlist)
- 평균가가 특정 임계값 이하 혹은 이상일 때 알림

## 4. 사용자 시나리오 (User Scenarios)

### 4.1. 일반 사용자 (로그인 없음)

1. 사이트 접속
2. 검색창에서 아이템 이름 입력
3. 해당 아이템의 최근 평균가와 지난 30일 그래프 확인
4. 최저가/최고가 판매 기록 확인
5. 가격이 적정한지 판단 후 메이플랜드에서 구매

### 4.2. 등록 유저 (선택 기능)

- 관심 아이템 목록 저장
- 가격 변동 알림 받기

## 5. 시스템 구조 (System Architecture)

### 5.1. 구성

- Spring Boot 3.5.7 (Java 17)
- H2 Database (File Mode 사용)
- Cron 기반 예약 작업
- HTML 크롤러(Jsoup) 사용
- REST API + Thymeleaf (또는 React, 선택)

### 5.2. 모듈 구조

```
/crawler
   - MaplelandCrawler.java
   - HTMLParser.java

/service
   - ItemService.java
   - PriceHistoryService.java

/repository
   - ItemRepository.java
   - PriceHistoryRepository.java

/controller
   - ItemController.java
   - ViewController.java

/domain
  - Item
  - PriceHistory

/scheduler
  - PriceFetchScheduler.java
```

## 6. DB 설계 (H2)

### 6.1. Item 테이블

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 자동 증가 |
| name | VARCHAR(255) | 아이템 이름 |
| image_url | VARCHAR(500) | 이미지 URL |
| created_at | DATETIME | 최초 등록 |

### 6.2. PriceHistory 테이블

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | |
| item_id | BIGINT (FK: item.id) | |
| date | DATE | 가격 수집 날짜 |
| min_price | BIGINT | |
| max_price | BIGINT | |
| avg_price | BIGINT | |
| median_price | BIGINT | |
| count | INT | |
| created_at | DATETIME | |

## 7. API 명세 (REST)

### 7.1. 아이템 검색

**요청:** `GET /api/items?query={name}`

**응답:**
```json
[
  { "id": 1, "name": "하이퍼 부스터", "imageUrl": "" }
]
```

### 7.2. 아이템 가격 히스토리 조회

**요청:** `GET /api/items/{itemId}/prices?range=30d`

**응답:**
```json
{
  "itemId": 1,
  "name": "하이퍼 부스터",
  "prices": [
    {"date": "2025-11-01", "avg": 15000000, "min": 12000000, "max": 20000000}
  ]
}
```

### 7.3. 특정 날짜의 가격 조회

**요청:** `GET /api/items/{id}/prices/today`

## 8. 크롤링 로직 상세

### 8.1. 실행 주기

- Spring Scheduler
- 매일 01:00 실행

### 8.2. 처리 과정

1. https://mapleland.gg 접속 → 아이템 리스트 추출
2. 페이지 전체 아이템 데이터 파싱
3. 아이템 이름으로 DB 확인
4. 없으면 Item 테이블에 신규 저장
5. 가격만 추출하여 가격 리스트 생성
6. min/max/avg/median 계산
7. PriceHistory 테이블에 Insert

### 8.3. 실패 처리

- 사이트 비가용 → "크롤 실패 로그" 저장
- HTML 형식 변화로 파싱 실패 → Error Log 기록

## 9. 프론트엔드 요구사항

### 9.1. 화면 구성

**메인 페이지**
- 검색창 + 인기 아이템 리스트(최근 조회순)

**아이템 상세 페이지**
- 아이템 이미지/이름
- 오늘의 평균가
- 지난 7일 · 30일 그래프
- 가격 통계 카드 (min/max/average)

**관리자 페이지 (선택)**
- 수집 로그 확인
- 수집된 아이템 리스트 확인

## 10. 비기능 요구사항

### 10.1. 성능

- 크롤링 작업은 10초 이내 완료
- DB 저장은 1000건 이하 처리 기준 3초 이내

### 10.2. 보안

- 관리자 API는 인증 필요
- 공개 API는 read-only

### 10.3. 확장성

- 향후 메이플랜드 이외의 거래 사이트도 동일 구조로 확장 가능
- H2 → MySQL 전환을 고려한 엔티티 구조

## 11. MVP 범위 (최소 기능)

### 포함

- ✔ 크롤러 실행 + 가격 수집
- ✔ Item 및 PriceHistory 저장
- ✔ 아이템 검색 및 상세 페이지
- ✔ 가격 그래프 표시

### 제외 (차후 개발)

- ✖ 유저 회원가입
- ✖ 알림 기능
- ✖ 관리자 페이지
