import React from 'react';
import { useTab } from './TabContext';
import '../styles/TabManager.css';
import Swal from 'sweetalert2'; // ✅ SweetAlert2 import

/**
 * 탭 매니저 컴포넌트
 * - 탭 목록 표시
 * - 탭 전환 및 닫기 기능
 * - ✅ 모두 닫기 기능 추가
 * - 모든 탭을 마운트 상태로 유지하여 상태 보존
 */
const TabManager = () => {
  const { tabs, activeTabId, setActiveTab, removeTab, closeAllTabs } = useTab();

  /**
   * 탭 클릭 핸들러
   * @param {string} tabId - 클릭한 탭의 ID
   */
  const handleTabClick = (tabId) => {
    setActiveTab(tabId);
  };

  /**
   * 탭 닫기 버튼 클릭 핸들러
   * @param {Event} e - 이벤트 객체
   * @param {string} tabId - 닫을 탭의 ID
   */
  const handleCloseTab = (e, tabId) => {
    e.stopPropagation(); // 탭 선택 이벤트 전파 방지
    removeTab(tabId);
  };

  /**
   * ✅ 모두 닫기 버튼 클릭 핸들러
   * - 확인 메시지 후 모든 탭 닫기
   */
  const handleCloseAllTabs = async () => {
    // 열린 탭이 없으면 무시
    if (tabs.length === 0) {
      return;
    }

    // 확인 메시지 표시
    const result = await Swal.fire({
      title: '모든 탭 닫기',
      text: `${tabs.length}개의 열린 탭을 모두 닫으시겠습니까?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: '모두 닫기',
      cancelButtonText: '취소',
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d'
    });

    // 확인 버튼을 눌렀을 때만 실행
    if (result.isConfirmed) {
      closeAllTabs();
      
      // 성공 메시지 (선택사항)
      Swal.fire({
        icon: 'success',
        title: '완료',
        text: '모든 탭이 닫혔습니다.',
        timer: 1500,
        showConfirmButton: false
      });
    }
  };

  return (
    <div className="tab-manager">
      {/* 탭 헤더 영역 */}
      {tabs.length > 0 && (
        <div className="tab-header">
          <div className="tab-header-wrapper">
            {/* 탭 목록 */}
            <div className="tab-list">
              {tabs.map(tab => (
                <div
                  key={tab.id}
                  className={`tab-item ${tab.id === activeTabId ? 'active' : ''}`}
                  onClick={() => handleTabClick(tab.id)}
                >
                  {/* 탭 제목 */}
                  <span className="tab-title">{tab.title}</span>
                  
                  {/* 탭 닫기 버튼 */}
                  <button
                    className="tab-close-btn"
                    onClick={(e) => handleCloseTab(e, tab.id)}
                    title="탭 닫기"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>

            {/* ✅ 모두 닫기 버튼 */}
            <button
              className="close-all-btn"
              onClick={handleCloseAllTabs}
              title="모든 탭 닫기"
            >
              <span className="close-all-icon">×</span>
              <span className="close-all-text">모두 닫기</span>
            </button>
          </div>
        </div>
      )}

      {/* 탭 컨텐츠 컨테이너 */}
      <div className="tab-content-container">
        {tabs.length > 0 ? (
          // 모든 탭의 컴포넌트를 렌더링
          tabs.map(tab => (
            <div
              key={tab.id}
              className={`tab-content ${tab.id === activeTabId ? 'active' : 'hidden'}`}
            >
              {/* 각 탭의 컴포넌트 렌더링 */}
              <tab.component />
            </div>
          ))
        ) : (
          // 열린 탭이 없을 때 표시할 메시지
          <div className="tab-content no-tabs">
            <div className="no-tab-message">
              <h3>열린 탭이 없습니다</h3>
              <p>메뉴에서 항목을 선택하여 새 탭을 열어보세요.</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TabManager;