/**
 * 가격 추이 차트 렌더링
 * Chart.js를 사용하여 아이템의 가격 변동을 시각화합니다.
 */

let priceChart = null;

// 페이지 로드 시 초기 차트 렌더링
document.addEventListener('DOMContentLoaded', function() {
    if (typeof priceHistoryData !== 'undefined' && priceHistoryData.length > 0) {
        renderChart(priceHistoryData);
    }

    // 기간 탭 이벤트 리스너 등록
    setupPeriodTabs();
});

// 차트 렌더링 함수
function renderChart(priceHistory) {
    const ctx = document.getElementById('priceChart');
    if (!ctx) {
        console.error('차트 캔버스를 찾을 수 없습니다.');
        return;
    }

    // 기존 차트가 있으면 제거
    if (priceChart) {
        priceChart.destroy();
    }

    // 데이터를 과거순으로 정렬 (과거가 왼쪽, 현재가 오른쪽)
    const reversedHistory = [...priceHistory].reverse();

    // 데이터 추출
    const labels = reversedHistory.map(p => formatDate(p.date));
    const avgPrices = reversedHistory.map(p => p.avgPrice);
    const minPrices = reversedHistory.map(p => p.minPrice);
    const maxPrices = reversedHistory.map(p => p.maxPrice);

    // 차트 생성
    priceChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '평균가',
                    data: avgPrices,
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.1)',
                    borderWidth: 3,
                    tension: 0.2,
                    fill: true,
                    pointRadius: 4,
                    pointHoverRadius: 6
                },
                {
                    label: '최저가',
                    data: minPrices,
                    borderColor: 'rgba(54, 162, 235, 0.6)',
                    backgroundColor: 'transparent',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    tension: 0.2,
                    fill: false,
                    pointRadius: 3,
                    pointHoverRadius: 5
                },
                {
                    label: '최고가',
                    data: maxPrices,
                    borderColor: 'rgba(255, 99, 132, 0.6)',
                    backgroundColor: 'transparent',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    tension: 0.2,
                    fill: false,
                    pointRadius: 3,
                    pointHoverRadius: 5
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: {
                            size: 13
                        },
                        usePointStyle: true
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    titleFont: {
                        size: 14
                    },
                    bodyFont: {
                        size: 13
                    },
                    callbacks: {
                        label: function(context) {
                            const label = context.dataset.label || '';
                            const value = context.parsed.y;
                            return label + ': ' + formatPrice(value) + ' 메소';
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        maxRotation: 45,
                        minRotation: 0
                    }
                },
                y: {
                    beginAtZero: false,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    },
                    ticks: {
                        callback: function(value) {
                            return formatPriceShort(value);
                        }
                    }
                }
            }
        }
    });
}

// 기간 선택 탭 설정
function setupPeriodTabs() {
    const tabs = document.querySelectorAll('.period-tab');

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // 활성 탭 변경
            tabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            // 선택된 기간으로 데이터 필터링
            const days = parseInt(this.dataset.range);
            filterPriceHistoryByDays(days);
        });
    });
}

// 기간별 가격 히스토리 필터링
function filterPriceHistoryByDays(days) {
    if (typeof priceHistoryData === 'undefined' || priceHistoryData.length === 0) {
        console.warn('가격 데이터가 없습니다.');
        return;
    }

    // 로딩 표시
    const chartContainer = document.querySelector('.chart-container');
    if (chartContainer) {
        chartContainer.style.opacity = '0.5';
    }

    // 최근 N일 데이터만 필터링 (데이터가 부족하면 있는 만큼만 표시)
    const filteredData = priceHistoryData.slice(0, Math.min(days, priceHistoryData.length));

    // 차트 업데이트
    if (filteredData.length > 0) {
        renderChart(filteredData);
        console.log(`${filteredData.length}일치 데이터 표시 (요청: ${days}일)`);
    } else {
        console.warn('표시할 가격 데이터가 없습니다.');
    }

    // 로딩 해제
    if (chartContainer) {
        chartContainer.style.opacity = '1';
    }
}

// 날짜 포맷팅 (YYYY-MM-DD -> MM/DD)
function formatDate(dateString) {
    if (!dateString) return '';

    // LocalDate 형식 처리
    if (Array.isArray(dateString)) {
        // [2025, 11, 19] 형식
        const [year, month, day] = dateString;
        return `${String(month).padStart(2, '0')}/${String(day).padStart(2, '0')}`;
    }

    // 문자열 형식 처리
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
        // 유효하지 않은 날짜
        return dateString;
    }

    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${month}/${day}`;
}

// 가격 포맷팅 (천 단위 구분)
function formatPrice(price) {
    if (price == null) return '0';
    return price.toLocaleString('ko-KR');
}

// 가격 짧게 포맷팅 (차트 Y축용)
function formatPriceShort(price) {
    if (price >= 1000000) {
        return (price / 1000000).toFixed(1) + 'M';
    } else if (price >= 1000) {
        return (price / 1000).toFixed(0) + 'K';
    }
    return price.toString();
}
