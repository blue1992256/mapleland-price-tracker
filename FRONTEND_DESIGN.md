# 메이플랜드 아이템 가격 추적 - 프론트엔드 디자인 명세서

## 1. 개요

메이플랜드 아이템의 가격 변동을 추적하고 시각화하는 웹 인터페이스 디자인 명세입니다.
사용자는 직관적인 UI를 통해 아이템을 검색하고, 가격 추이를 그래프로 확인하며, 통계 정보를 파악할 수 있습니다.

## 2. 기술 스택 제안

### 2.1. 프론트엔드 프레임워크
- **Thymeleaf** (서버 사이드 렌더링, 간단한 구현)
  - 장점: Spring Boot와 완벽한 통합, 초기 개발 속도 빠름, SEO 친화적
  - 단점: 동적 인터랙션 구현 시 JavaScript 추가 필요

- **React** (클라이언트 사이드 렌더링, 향후 확장성)
  - 장점: 풍부한 UI 인터랙션, 컴포넌트 재사용성, 차트 라이브러리 풍부
  - 단점: 초기 설정 복잡도 증가, 별도 빌드 프로세스 필요

**권장: MVP는 Thymeleaf로 시작, 향후 React로 마이그레이션**

### 2.2. 차트 라이브러리
- **Chart.js** (Thymeleaf 사용 시)
- **Recharts** 또는 **Victory** (React 사용 시)
- **ApexCharts** (두 환경 모두 사용 가능)

### 2.3. CSS 프레임워크
- **Bootstrap 5** 또는 **Tailwind CSS**
- 반응형 디자인 지원

## 3. 화면 설계

### 3.1. 메인 페이지 (Main Page)

#### 3.1.1. 레이아웃 구조

```
┌─────────────────────────────────────────────────────────┐
│                     Header                                │
│  [🍁 메이플랜드 가격 추적기]                            │
└─────────────────────────────────────────────────────────┘
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │     🔍 검색창 (아이템 이름을 입력하세요...)        │  │
│  └───────────────────────────────────────────────────┘  │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │            📈 인기 아이템 (최근 조회순)             ││
│  │                                                       ││
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐          ││
│  │  │ [이미지]  │  │ [이미지]  │  │ [이미지]  │          ││
│  │  │ 아이템명  │  │ 아이템명  │  │ 아이템명  │          ││
│  │  │ 평균가    │  │ 평균가    │  │ 평균가    │          ││
│  │  │ 변동 +5%  │  │ 변동 -2%  │  │ 변동 0%   │          ││
│  │  └──────────┘  └──────────┘  └──────────┘          ││
│  │                                                       ││
│  │  (6-12개 아이템 카드를 그리드로 배치)                ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
└─────────────────────────────────────────────────────────┘
```

#### 3.1.2. UI 컴포넌트

**헤더 (Header)**
- 로고 / 타이틀: "메이플랜드 가격 추적기"
- 네비게이션: 메인, 아이템 목록 (향후 확장)
- 색상: 메이플스토리 테마 (주황색, 초록색 계열)

**검색 영역 (Search Section)**
- 컴포넌트: `<input type="text">` + 돋보기 아이콘
- 기능:
  - 실시간 자동완성 (3글자 이상 입력 시)
  - Enter 키 또는 검색 버튼으로 검색
  - 검색 결과: 드롭다운 리스트 표시
- 스타일:
  - 중앙 정렬
  - 큰 검색창 (width: 60-70%)
  - 포커스 시 하이라이트 효과

**인기 아이템 그리드 (Popular Items Grid)**
- 레이아웃: CSS Grid (3-4 컬럼, 반응형)
- 각 카드 구성:
  ```
  ┌──────────────────┐
  │   [아이템 이미지]  │  (100x100px)
  ├──────────────────┤
  │  아이템 이름       │  (폰트: 14px, bold)
  ├──────────────────┤
  │  평균가: 15,000,000│  (폰트: 16px, 메인 컬러)
  │  메소              │
  ├──────────────────┤
  │  변동: ▲ +5.2%    │  (초록색) / ▼ -2.1% (빨간색)
  └──────────────────┘
  ```
- 인터랙션:
  - 카드 hover 시 그림자 효과
  - 클릭 시 상세 페이지로 이동

#### 3.1.3. 데이터 요구사항

**API 호출:**
```javascript
// 검색 자동완성
GET /api/items?query={searchText}

// 인기 아이템 목록 (조회수 기준 상위 12개)
GET /api/items/popular?limit=12
```

**응답 데이터 구조:**
```json
{
  "id": 1,
  "name": "하이퍼 부스터",
  "imageUrl": "https://...",
  "currentAvgPrice": 15000000,
  "priceChange": 5.2,  // 어제 대비 변동률 (%)
  "viewCount": 1250
}
```

### 3.2. 아이템 상세 페이지 (Item Detail Page)

#### 3.2.1. 레이아웃 구조

```
┌─────────────────────────────────────────────────────────┐
│                     Header                                │
│  [← 뒤로가기]  메이플랜드 가격 추적기                    │
└─────────────────────────────────────────────────────────┘
│                                                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │  ┌──────┐  하이퍼 부스터                             ││
│  │  │[이미지]│                                           ││
│  │  │120x120│  오늘의 평균가: 15,000,000 메소           ││
│  │  └──────┘  변동률: ▲ +5.2% (어제 대비)              ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │           가격 통계 카드 (Statistics Cards)          ││
│  │                                                       ││
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐          ││
│  │  │  최저가   │  │  최고가   │  │  중간값   │          ││
│  │  │12,000,000│  │20,000,000│  │14,500,000│          ││
│  │  │   메소    │  │   메소    │  │   메소    │          ││
│  │  └──────────┘  └──────────┘  └──────────┘          ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │           기간 선택 탭 (Period Tabs)                 ││
│  │  [ 7일 ]  [ 30일 ]  [ 90일 ]                         ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │              가격 추이 그래프 (Line Chart)            ││
│  │                                                       ││
│  │   20M ┤                               ●              ││
│  │       │                         ●                    ││
│  │   15M ┤           ●       ●                          ││
│  │       │     ●                                        ││
│  │   10M ┤                                              ││
│  │       └────────────────────────────────────────────  ││
│  │        11/01  11/08  11/15  11/22  11/29            ││
│  │                                                       ││
│  │   [범례] ─ 평균가  ··· 최저가  ··· 최고가            ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │          상세 가격 테이블 (Price History Table)       ││
│  │                                                       ││
│  │  날짜        │ 평균가      │ 최저가      │ 최고가    ││
│  │  ──────────┼──────────┼──────────┼────────────    ││
│  │  2025-11-19 │ 15,000,000 │ 12,000,000 │ 20,000,000││
│  │  2025-11-18 │ 14,250,000 │ 11,500,000 │ 18,000,000││
│  │  2025-11-17 │ 14,800,000 │ 13,000,000 │ 19,000,000││
│  │  ...        │ ...        │ ...        │ ...       ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
└─────────────────────────────────────────────────────────┘
```

#### 3.2.2. UI 컴포넌트 상세

**헤더 정보 섹션 (Item Header)**
- 아이템 이미지 (120x120px)
- 아이템 이름 (폰트: 24px, bold)
- 오늘의 평균가 강조 (폰트: 28px, 굵게, 메인 컬러)
- 변동률 배지 (▲ 초록색 / ▼ 빨간색)

**가격 통계 카드 (Statistics Cards)**
- 3개 카드: 최저가, 최고가, 중간값
- 카드 디자인:
  - 배경: 흰색 / 연한 회색
  - 테두리: 1px solid #ddd
  - 패딩: 20px
  - 그림자: box-shadow
- 레이아웃: Flexbox (균등 배치)

**기간 선택 탭 (Period Selector)**
- 버튼 형태의 탭
- 기본값: 30일
- 선택된 탭: 활성화 스타일 (배경색 변경)
- 클릭 시 그래프 데이터 갱신

**가격 추이 그래프 (Price Chart)**
- 라이브러리: Chart.js 또는 ApexCharts
- 차트 타입: Line Chart (라인 그래프)
- 데이터 시리즈:
  - 평균가 (주 라인, 굵은 선)
  - 최저가 (점선, 옅은 색)
  - 최고가 (점선, 옅은 색)
- X축: 날짜
- Y축: 가격 (메소)
- 인터랙션:
  - 데이터 포인트 hover 시 툴팁 표시
  - 툴팁 내용: 날짜, 평균가, 최저가, 최고가
- 반응형: 모바일에서는 높이 축소

**상세 가격 테이블 (Price History Table)**
- 테이블 헤더: 날짜, 평균가, 최저가, 최고가
- 데이터 포맷:
  - 날짜: YYYY-MM-DD
  - 가격: 천 단위 구분 (예: 15,000,000)
- 페이지네이션: 10개씩 표시
- 정렬: 날짜 기준 내림차순 (최신순)

#### 3.2.3. 데이터 요구사항

**API 호출:**
```javascript
// 아이템 기본 정보 + 오늘 가격
GET /api/items/{itemId}

// 특정 기간의 가격 히스토리
GET /api/items/{itemId}/prices?range=30d
```

**응답 데이터 구조:**
```json
// GET /api/items/{itemId}
{
  "id": 1,
  "name": "하이퍼 부스터",
  "imageUrl": "https://...",
  "todayPrice": {
    "date": "2025-11-19",
    "avgPrice": 15000000,
    "minPrice": 12000000,
    "maxPrice": 20000000,
    "medianPrice": 14500000,
    "count": 150,
    "changeRate": 5.2
  }
}

// GET /api/items/{itemId}/prices?range=30d
{
  "itemId": 1,
  "name": "하이퍼 부스터",
  "prices": [
    {
      "date": "2025-11-19",
      "avgPrice": 15000000,
      "minPrice": 12000000,
      "maxPrice": 20000000,
      "medianPrice": 14500000,
      "count": 150
    },
    // ... 30일치 데이터
  ]
}
```

## 4. 컴포넌트 구조 (React 기준)

### 4.1. 컴포넌트 트리

```
App
├── Header
├── MainPage
│   ├── SearchBar
│   │   └── AutoCompleteDropdown
│   └── PopularItemsGrid
│       └── ItemCard (여러 개)
└── ItemDetailPage
    ├── ItemHeader
    ├── StatisticsCards
    │   ├── StatCard (최저가)
    │   ├── StatCard (최고가)
    │   └── StatCard (중간값)
    ├── PeriodSelector
    ├── PriceChart
    └── PriceHistoryTable
```

### 4.2. 주요 컴포넌트 명세

#### 4.2.1. SearchBar 컴포넌트

```jsx
// SearchBar.jsx
import { useState, useEffect } from 'react';

function SearchBar({ onSearch }) {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  useEffect(() => {
    if (query.length >= 2) {
      // API 호출로 자동완성 결과 가져오기
      fetch(`/api/items?query=${query}`)
        .then(res => res.json())
        .then(data => {
          setSuggestions(data);
          setShowDropdown(true);
        });
    } else {
      setSuggestions([]);
      setShowDropdown(false);
    }
  }, [query]);

  const handleSearch = (itemId) => {
    onSearch(itemId);
    setShowDropdown(false);
  };

  return (
    <div className="search-bar">
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="아이템 이름을 입력하세요..."
      />
      {showDropdown && (
        <div className="autocomplete-dropdown">
          {suggestions.map(item => (
            <div
              key={item.id}
              className="suggestion-item"
              onClick={() => handleSearch(item.id)}
            >
              <img src={item.imageUrl} alt={item.name} />
              <span>{item.name}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

#### 4.2.2. ItemCard 컴포넌트

```jsx
// ItemCard.jsx
function ItemCard({ item }) {
  const priceChangeClass = item.priceChange >= 0 ? 'positive' : 'negative';
  const priceChangeIcon = item.priceChange >= 0 ? '▲' : '▼';

  return (
    <div className="item-card" onClick={() => navigateToDetail(item.id)}>
      <img src={item.imageUrl} alt={item.name} className="item-image" />
      <h3 className="item-name">{item.name}</h3>
      <div className="item-price">
        {item.currentAvgPrice.toLocaleString()} 메소
      </div>
      <div className={`price-change ${priceChangeClass}`}>
        {priceChangeIcon} {Math.abs(item.priceChange).toFixed(1)}%
      </div>
    </div>
  );
}
```

#### 4.2.3. PriceChart 컴포넌트

```jsx
// PriceChart.jsx
import { Line } from 'react-chartjs-2';

function PriceChart({ priceHistory }) {
  const chartData = {
    labels: priceHistory.map(p => p.date),
    datasets: [
      {
        label: '평균가',
        data: priceHistory.map(p => p.avgPrice),
        borderColor: 'rgb(75, 192, 192)',
        borderWidth: 3,
        tension: 0.1
      },
      {
        label: '최저가',
        data: priceHistory.map(p => p.minPrice),
        borderColor: 'rgba(54, 162, 235, 0.5)',
        borderDash: [5, 5],
        borderWidth: 2
      },
      {
        label: '최고가',
        data: priceHistory.map(p => p.maxPrice),
        borderColor: 'rgba(255, 99, 132, 0.5)',
        borderDash: [5, 5],
        borderWidth: 2
      }
    ]
  };

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: 'bottom'
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            return context.dataset.label + ': ' +
                   context.parsed.y.toLocaleString() + ' 메소';
          }
        }
      }
    },
    scales: {
      y: {
        ticks: {
          callback: function(value) {
            return (value / 1000000) + 'M';
          }
        }
      }
    }
  };

  return (
    <div className="price-chart">
      <Line data={chartData} options={options} />
    </div>
  );
}
```

#### 4.2.4. StatisticsCards 컴포넌트

```jsx
// StatisticsCards.jsx
function StatisticsCards({ minPrice, maxPrice, medianPrice }) {
  return (
    <div className="statistics-cards">
      <div className="stat-card">
        <div className="stat-label">최저가</div>
        <div className="stat-value">{minPrice.toLocaleString()}</div>
        <div className="stat-unit">메소</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">최고가</div>
        <div className="stat-value">{maxPrice.toLocaleString()}</div>
        <div className="stat-unit">메소</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">중간값</div>
        <div className="stat-value">{medianPrice.toLocaleString()}</div>
        <div className="stat-unit">메소</div>
      </div>
    </div>
  );
}
```

## 5. 상태 관리 설계

### 5.1. React Context (간단한 상태 관리)

```jsx
// AppContext.jsx
import { createContext, useState } from 'react';

export const AppContext = createContext();

export function AppProvider({ children }) {
  const [selectedItem, setSelectedItem] = useState(null);
  const [popularItems, setPopularItems] = useState([]);
  const [priceHistory, setPriceHistory] = useState([]);

  return (
    <AppContext.Provider value={{
      selectedItem,
      setSelectedItem,
      popularItems,
      setPopularItems,
      priceHistory,
      setPriceHistory
    }}>
      {children}
    </AppContext.Provider>
  );
}
```

### 5.2. 데이터 페칭 훅 (Custom Hook)

```jsx
// useItemData.js
import { useState, useEffect } from 'react';

export function useItemData(itemId) {
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!itemId) return;

    setLoading(true);
    fetch(`/api/items/${itemId}`)
      .then(res => res.json())
      .then(data => {
        setItem(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err);
        setLoading(false);
      });
  }, [itemId]);

  return { item, loading, error };
}

export function usePriceHistory(itemId, range = '30d') {
  const [priceHistory, setPriceHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!itemId) return;

    setLoading(true);
    fetch(`/api/items/${itemId}/prices?range=${range}`)
      .then(res => res.json())
      .then(data => {
        setPriceHistory(data.prices);
        setLoading(false);
      });
  }, [itemId, range]);

  return { priceHistory, loading };
}
```

## 6. 스타일링 가이드

### 6.1. 색상 팔레트

```css
:root {
  /* 메이플스토리 테마 */
  --primary-color: #FF8C00;        /* 주황색 (메이플 메인 컬러) */
  --secondary-color: #32CD32;      /* 초록색 */
  --accent-color: #FFD700;         /* 골드 */

  /* 기능적 색상 */
  --positive-color: #28a745;       /* 상승 (초록) */
  --negative-color: #dc3545;       /* 하락 (빨강) */
  --neutral-color: #6c757d;        /* 중립 (회색) */

  /* 배경 및 텍스트 */
  --bg-primary: #ffffff;
  --bg-secondary: #f8f9fa;
  --text-primary: #212529;
  --text-secondary: #6c757d;

  /* 테두리 */
  --border-color: #dee2e6;
}
```

### 6.2. 타이포그래피

```css
body {
  font-family: 'Noto Sans KR', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 16px;
  line-height: 1.5;
  color: var(--text-primary);
}

h1 { font-size: 2.5rem; font-weight: 700; }
h2 { font-size: 2rem; font-weight: 600; }
h3 { font-size: 1.5rem; font-weight: 600; }
h4 { font-size: 1.25rem; font-weight: 500; }

.price-large {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
}

.price-medium {
  font-size: 20px;
  font-weight: 600;
}

.price-small {
  font-size: 16px;
  font-weight: 500;
}
```

### 6.3. 반응형 브레이크포인트

```css
/* 모바일 우선 디자인 */
@media (min-width: 576px) { /* Small devices (landscape phones) */ }
@media (min-width: 768px) { /* Medium devices (tablets) */ }
@media (min-width: 992px) { /* Large devices (desktops) */ }
@media (min-width: 1200px) { /* Extra large devices (large desktops) */ }
```

### 6.4. 주요 컴포넌트 스타일

```css
/* 아이템 카드 */
.item-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.item-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

/* 검색창 */
.search-bar input {
  width: 100%;
  max-width: 600px;
  padding: 12px 20px;
  font-size: 18px;
  border: 2px solid var(--border-color);
  border-radius: 24px;
  outline: none;
  transition: border-color 0.3s;
}

.search-bar input:focus {
  border-color: var(--primary-color);
}

/* 통계 카드 */
.stat-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  flex: 1;
  min-width: 150px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
}

/* 가격 변동 표시 */
.price-change.positive {
  color: var(--positive-color);
}

.price-change.negative {
  color: var(--negative-color);
}
```

## 7. UX 고려사항

### 7.1. 로딩 상태
- 데이터 로딩 중: 스켈레톤 UI 또는 스피너 표시
- 그래프 로딩: 페이드인 애니메이션

### 7.2. 에러 핸들링
- API 실패 시: 사용자 친화적 에러 메시지 표시
- 아이템 없음: "검색 결과가 없습니다" 메시지

### 7.3. 접근성 (Accessibility)
- 시맨틱 HTML 태그 사용
- 이미지 alt 텍스트 제공
- 키보드 네비게이션 지원
- ARIA 레이블 적용

### 7.4. 성능 최적화
- 이미지 lazy loading
- 검색 자동완성 디바운싱 (300ms)
- 차트 데이터 메모이제이션

## 8. 테스트 시나리오

### 8.1. 메인 페이지
- [ ] 페이지 로드 시 인기 아이템 12개 표시
- [ ] 검색창에 2글자 이상 입력 시 자동완성 작동
- [ ] 아이템 카드 클릭 시 상세 페이지 이동
- [ ] 반응형: 모바일에서 1컬럼, 태블릿 2컬럼, 데스크탑 3-4컬럼

### 8.2. 아이템 상세 페이지
- [ ] 아이템 정보 및 오늘 가격 표시
- [ ] 통계 카드에 최저/최고/중간값 표시
- [ ] 기본 30일 그래프 표시
- [ ] 기간 탭 전환 시 그래프 데이터 변경
- [ ] 가격 테이블에 날짜별 가격 표시
- [ ] 툴팁 hover 시 상세 정보 표시

## 9. 향후 개선 사항

### Phase 2 (MVP 이후)
- 사용자 로그인 및 관심 아이템 저장
- 가격 알림 기능
- 아이템 비교 기능 (2-3개 아이템 동시 비교)

### Phase 3
- 다크 모드 지원
- 모바일 앱 (PWA)
- 실시간 가격 업데이트 (WebSocket)

## 10. 개발 우선순위

### 1순위 (MVP 필수)
1. 메인 페이지 - 검색 + 인기 아이템 그리드
2. 아이템 상세 페이지 - 기본 정보 + 그래프
3. API 연동

### 2순위 (MVP 완성도)
4. 반응형 디자인 구현
5. 로딩/에러 상태 처리
6. 가격 테이블 추가

### 3순위 (UX 개선)
7. 자동완성 최적화
8. 애니메이션 효과
9. 접근성 개선

---

## 부록: Thymeleaf 구현 예시

### A.1. 메인 페이지 템플릿

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>메이플랜드 가격 추적기</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <header>
        <h1>🍁 메이플랜드 가격 추적기</h1>
    </header>

    <main>
        <div class="search-section">
            <input type="text"
                   id="searchInput"
                   placeholder="아이템 이름을 입력하세요..."
                   autocomplete="off">
            <div id="autocomplete" class="autocomplete-dropdown"></div>
        </div>

        <section class="popular-items">
            <h2>📈 인기 아이템</h2>
            <div class="items-grid">
                <div th:each="item : ${popularItems}"
                     class="item-card"
                     th:onclick="'location.href=\'/items/' + ${item.id} + '\''">
                    <img th:src="${item.imageUrl}"
                         th:alt="${item.name}"
                         class="item-image">
                    <h3 th:text="${item.name}"></h3>
                    <div class="item-price"
                         th:text="${#numbers.formatInteger(item.currentAvgPrice, 0, 'COMMA')} + ' 메소'">
                    </div>
                    <div class="price-change"
                         th:classappend="${item.priceChange >= 0} ? 'positive' : 'negative'"
                         th:text="${item.priceChange >= 0 ? '▲' : '▼'} + ' ' +
                                  ${#numbers.formatDecimal(item.priceChange, 1, 1)} + '%'">
                    </div>
                </div>
            </div>
        </section>
    </main>

    <script src="/js/search.js"></script>
</body>
</html>
```

### A.2. 아이템 상세 페이지 템플릿

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${item.name} + ' - 메이플랜드 가격 추적기'"></title>
    <link rel="stylesheet" href="/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <header>
        <a href="/">← 뒤로가기</a>
        <h1>메이플랜드 가격 추적기</h1>
    </header>

    <main>
        <div class="item-header">
            <img th:src="${item.imageUrl}" th:alt="${item.name}">
            <div>
                <h2 th:text="${item.name}"></h2>
                <div class="today-price">
                    오늘의 평균가:
                    <span th:text="${#numbers.formatInteger(item.todayPrice.avgPrice, 0, 'COMMA')}"></span>
                    메소
                </div>
                <div class="price-change"
                     th:classappend="${item.todayPrice.changeRate >= 0} ? 'positive' : 'negative'"
                     th:text="'변동률: ' +
                              (${item.todayPrice.changeRate >= 0 ? '▲' : '▼'}) + ' ' +
                              ${#numbers.formatDecimal(item.todayPrice.changeRate, 1, 1)} + '% (어제 대비)'">
                </div>
            </div>
        </div>

        <div class="statistics-cards">
            <div class="stat-card">
                <div class="stat-label">최저가</div>
                <div class="stat-value"
                     th:text="${#numbers.formatInteger(item.todayPrice.minPrice, 0, 'COMMA')}">
                </div>
                <div class="stat-unit">메소</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">최고가</div>
                <div class="stat-value"
                     th:text="${#numbers.formatInteger(item.todayPrice.maxPrice, 0, 'COMMA')}">
                </div>
                <div class="stat-unit">메소</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">중간값</div>
                <div class="stat-value"
                     th:text="${#numbers.formatInteger(item.todayPrice.medianPrice, 0, 'COMMA')}">
                </div>
                <div class="stat-unit">메소</div>
            </div>
        </div>

        <div class="period-selector">
            <button class="period-tab" data-range="7">7일</button>
            <button class="period-tab active" data-range="30">30일</button>
            <button class="period-tab" data-range="90">90일</button>
        </div>

        <div class="chart-container">
            <canvas id="priceChart"></canvas>
        </div>

        <table class="price-history-table">
            <thead>
                <tr>
                    <th>날짜</th>
                    <th>평균가</th>
                    <th>최저가</th>
                    <th>최고가</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="price : ${priceHistory}">
                    <td th:text="${#temporals.format(price.date, 'yyyy-MM-dd')}"></td>
                    <td th:text="${#numbers.formatInteger(price.avgPrice, 0, 'COMMA')}"></td>
                    <td th:text="${#numbers.formatInteger(price.minPrice, 0, 'COMMA')}"></td>
                    <td th:text="${#numbers.formatInteger(price.maxPrice, 0, 'COMMA')}"></td>
                </tr>
            </tbody>
        </table>
    </main>

    <script th:inline="javascript">
        const priceData = /*[[${priceHistory}]]*/ [];
        renderChart(priceData);
    </script>
    <script src="/js/chart.js"></script>
</body>
</html>
```

### A.3. JavaScript (검색 자동완성)

```javascript
// /resources/static/js/search.js
let debounceTimer;
const searchInput = document.getElementById('searchInput');
const autocompleteDiv = document.getElementById('autocomplete');

searchInput.addEventListener('input', function() {
    const query = this.value.trim();

    clearTimeout(debounceTimer);

    if (query.length < 2) {
        autocompleteDiv.innerHTML = '';
        autocompleteDiv.style.display = 'none';
        return;
    }

    debounceTimer = setTimeout(() => {
        fetch(`/api/items?query=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(items => {
                if (items.length === 0) {
                    autocompleteDiv.innerHTML = '<div class="no-results">검색 결과가 없습니다</div>';
                } else {
                    autocompleteDiv.innerHTML = items.map(item => `
                        <div class="suggestion-item" onclick="location.href='/items/${item.id}'">
                            <img src="${item.imageUrl}" alt="${item.name}">
                            <span>${item.name}</span>
                        </div>
                    `).join('');
                }
                autocompleteDiv.style.display = 'block';
            })
            .catch(error => {
                console.error('검색 실패:', error);
            });
    }, 300);
});

// 클릭 외부 영역 클릭 시 자동완성 닫기
document.addEventListener('click', function(e) {
    if (!searchInput.contains(e.target) && !autocompleteDiv.contains(e.target)) {
        autocompleteDiv.style.display = 'none';
    }
});
```

### A.4. JavaScript (차트 렌더링)

```javascript
// /resources/static/js/chart.js
function renderChart(priceHistory) {
    const ctx = document.getElementById('priceChart').getContext('2d');

    const labels = priceHistory.map(p => p.date);
    const avgPrices = priceHistory.map(p => p.avgPrice);
    const minPrices = priceHistory.map(p => p.minPrice);
    const maxPrices = priceHistory.map(p => p.maxPrice);

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '평균가',
                    data: avgPrices,
                    borderColor: 'rgb(75, 192, 192)',
                    borderWidth: 3,
                    tension: 0.1,
                    fill: false
                },
                {
                    label: '최저가',
                    data: minPrices,
                    borderColor: 'rgba(54, 162, 235, 0.5)',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    fill: false
                },
                {
                    label: '최고가',
                    data: maxPrices,
                    borderColor: 'rgba(255, 99, 132, 0.5)',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' +
                                   context.parsed.y.toLocaleString() + ' 메소';
                        }
                    }
                }
            },
            scales: {
                y: {
                    ticks: {
                        callback: function(value) {
                            return (value / 1000000) + 'M';
                        }
                    }
                }
            }
        }
    });
}

// 기간 탭 전환
document.querySelectorAll('.period-tab').forEach(tab => {
    tab.addEventListener('click', function() {
        document.querySelectorAll('.period-tab').forEach(t => t.classList.remove('active'));
        this.classList.add('active');

        const range = this.dataset.range + 'd';
        const itemId = window.location.pathname.split('/').pop();

        fetch(`/api/items/${itemId}/prices?range=${range}`)
            .then(response => response.json())
            .then(data => {
                // 차트 업데이트
                renderChart(data.prices);
            });
    });
});
```

---

**문서 버전:** 1.0
**작성일:** 2025-11-19
**최종 수정일:** 2025-11-19
