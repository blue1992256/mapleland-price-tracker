/**
 * 검색 자동완성 기능
 * 사용자가 입력한 검색어에 따라 실시간으로 아이템 목록을 보여줍니다.
 * JSON 파일을 미리 로드하여 클라이언트 측에서 자동완성을 처리합니다.
 */

let debounceTimer;
let itemsData = []; // 아이템 목록 캐시
const searchInput = document.getElementById('searchInput');
const autocompleteDiv = document.getElementById('autocomplete');

// 페이지 로드 시 아이템 목록 로드
document.addEventListener('DOMContentLoaded', function() {
    loadItemsData();
});

// JSON 파일에서 아이템 목록 로드
function loadItemsData() {
    fetch('/files/items.json')
        .then(response => {
            if (!response.ok) {
                throw new Error('아이템 데이터 로드 실패');
            }
            return response.json();
        })
        .then(items => {
            itemsData = items;
            console.log(`아이템 ${items.length}개 로드 완료`);
        })
        .catch(error => {
            console.error('아이템 데이터 로드 중 오류:', error);
        });
}

// 입력 이벤트 리스너
searchInput.addEventListener('input', function() {
    const query = this.value.trim();

    // 이전 타이머 취소
    clearTimeout(debounceTimer);

    // 검색어가 1글자 미만이면 자동완성 숨김
    if (query.length < 1) {
        hideAutocomplete();
        return;
    }

    // 300ms 디바운싱
    debounceTimer = setTimeout(() => {
        searchItems(query);
    }, 300);
});

// 클라이언트 측에서 검색 수행
function searchItems(query) {
    if (itemsData.length === 0) {
        showError('아이템 데이터를 로드 중입니다...');
        return;
    }

    // 검색어가 포함된 아이템 찾기 (대소문자 구분 없음)
    const lowerQuery = query.toLowerCase();
    const results = itemsData
        .filter(item => item.name.toLowerCase().includes(lowerQuery))
        .slice(0, 10); // 최대 10개만 표시

    displaySuggestions(results);
}

// 자동완성 결과 표시
function displaySuggestions(items) {
    if (items.length === 0) {
        autocompleteDiv.innerHTML = '<div class="no-results">검색 결과가 없습니다</div>';
    } else {
        autocompleteDiv.innerHTML = items.map(item => {
            const priceHtml = item.averagePrice
                ? `<span class="item-price">${formatPrice(item.averagePrice)} 메소</span>`
                : '<span class="item-price no-price">가격 정보 없음</span>';

            return `
                <div class="suggestion-item" onclick="navigateToItem('${item.itemCode}')">
                    <img
                        src="${item.imageUrl || '/images/default-item.png'}"
                        alt="${escapeHtml(item.name)}"
                        onerror="this.src='/images/default-item.png'">
                    <div class="item-info">
                        <span class="item-name">${escapeHtml(item.name)}</span>
                        ${priceHtml}
                    </div>
                </div>
            `;
        }).join('');
    }
    autocompleteDiv.classList.add('show');
}

// 에러 메시지 표시
function showError(message) {
    autocompleteDiv.innerHTML = `<div class="no-results">${escapeHtml(message)}</div>`;
    autocompleteDiv.classList.add('show');
}

// 자동완성 숨기기
function hideAutocomplete() {
    autocompleteDiv.innerHTML = '';
    autocompleteDiv.classList.remove('show');
}

// 아이템 상세 페이지로 이동 (itemCode 사용)
function navigateToItem(itemCode) {
    window.location.href = `/items/${itemCode}`;
}

// HTML 이스케이프 (XSS 방지)
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

// 가격 포맷팅 (천 단위 콤마)
function formatPrice(price) {
    if (price === null || price === undefined) {
        return '-';
    }
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

// Enter 키 처리
searchInput.addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        const firstItem = autocompleteDiv.querySelector('.suggestion-item');
        if (firstItem) {
            firstItem.click();
        }
    }
});

// 외부 클릭 시 자동완성 닫기
document.addEventListener('click', function(e) {
    if (!searchInput.contains(e.target) && !autocompleteDiv.contains(e.target)) {
        hideAutocomplete();
    }
});

// ESC 키로 자동완성 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        hideAutocomplete();
        searchInput.blur();
    }
});
