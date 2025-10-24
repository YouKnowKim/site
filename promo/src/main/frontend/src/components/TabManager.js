import React, { useState, useCallback } from 'react';
import { Nav, Tab } from 'react-bootstrap';
import { FaTimes } from 'react-icons/fa';
import '../styles/TabManager.css';
import Home from '../pages/Home';

const TabManager = () => {
  const [tabs, setTabs] = useState([
    { id: 'home', title: '홈', path: '/', component: <Home />, closeable: false }
  ]);
  const [activeTabId, setActiveTabId] = useState('home');

  // 탭 추가 또는 기존 탭 활성화
  const addTab = useCallback((path, title, component) => {
    // 고유 ID 생성 (path 기반)
    const tabId = path.replace(/\//g, '_') || 'home';

    // 이미 열린 탭인지 확인
    const existingTab = tabs.find(tab => tab.id === tabId);

    if (existingTab) {
      // 이미 열린 탭이면 활성화만
      setActiveTabId(tabId);
    } else {
      // 새 탭 추가
      const newTab = {
        id: tabId,
        title,
        path,
        component,
        closeable: true
      };
      setTabs(prev => [...prev, newTab]);
      setActiveTabId(tabId);
    }
  }, [tabs]);

  // 탭 닫기
  const closeTab = useCallback((tabId, e) => {
    if (e) {
      e.stopPropagation();
    }

    // 닫을 수 없는 탭(홈)은 닫지 않음
    const tabToClose = tabs.find(tab => tab.id === tabId);
    if (!tabToClose || !tabToClose.closeable) {
      return;
    }

    const tabIndex = tabs.findIndex(tab => tab.id === tabId);
    const newTabs = tabs.filter(tab => tab.id !== tabId);

    setTabs(newTabs);

    // 닫힌 탭이 활성 탭이었다면 다른 탭 활성화
    if (activeTabId === tabId) {
      if (tabIndex > 0) {
        // 이전 탭 활성화
        setActiveTabId(newTabs[tabIndex - 1].id);
      } else if (newTabs.length > 0) {
        // 다음 탭 활성화
        setActiveTabId(newTabs[0].id);
      }
    }
  }, [tabs, activeTabId]);

  // TabManager에서 외부로 함수 노출 (Header에서 사용)
  React.useEffect(() => {
    window.tabManager = {
      addTab
    };
  }, [addTab]);

  return (
    <div className="tab-manager">
      <Tab.Container activeKey={activeTabId} onSelect={(k) => setActiveTabId(k)}>
        {/* 탭 헤더 */}
        <Nav variant="tabs" className="tab-headers">
          {tabs.map(tab => (
            <Nav.Item key={tab.id}>
              <Nav.Link eventKey={tab.id} className="tab-header-item">
                <span className="tab-title">{tab.title}</span>
                {tab.closeable && (
                  <button
                    className="tab-close-btn"
                    onClick={(e) => closeTab(tab.id, e)}
                    aria-label="탭 닫기"
                  >
                    <FaTimes />
                  </button>
                )}
              </Nav.Link>
            </Nav.Item>
          ))}
        </Nav>

        {/* 탭 컨텐츠 */}
        <Tab.Content className="tab-content-area">
          {tabs.map(tab => (
            <Tab.Pane key={tab.id} eventKey={tab.id}>
              {tab.component}
            </Tab.Pane>
          ))}
        </Tab.Content>
      </Tab.Container>
    </div>
  );
};

export default TabManager;
