import React, { createContext, useContext, useState, useCallback } from 'react';
import Home from '../pages/Home'; // ✅ Home 컴포넌트 import 추가

/**
 * 탭 컨텍스트 생성
 * - 전역적으로 탭 목록과 활성 탭을 관리
 */
const TabContext = createContext();

/**
 * 탭 컨텍스트 Hook
 * @returns {Object} 탭 관련 상태 및 함수들
 */
export const useTab = () => {
  const context = useContext(TabContext);
  if (!context) {
    throw new Error('useTab은 TabProvider 내부에서만 사용 가능합니다.');
  }
  return context;
};

/**
 * 탭 프로바이더 컴포넌트
 * @param {Object} props - children 컴포넌트
 */
export const TabProvider = ({ children }) => {
  
  // 열려있는 탭 목록 상태
  // 각 탭 구조: { id, title, path, component }
  const [tabs, setTabs] = useState([]);
  
  // 현재 활성화된 탭의 ID
  const [activeTabId, setActiveTabId] = useState(null);

  /**
   * 새로운 탭 추가 또는 기존 탭 활성화
   * @param {string} id - 탭의 고유 식별자 (중복 방지용)
   * @param {string} title - 탭에 표시될 제목
   * @param {string} path - 라우팅 경로
   * @param {React.Component} component - 렌더링할 컴포넌트
   */
  const addTab = useCallback((id, title, path, component) => {
    setTabs(prevTabs => {
      // 이미 해당 ID의 탭이 존재하는지 확인
      const existingTab = prevTabs.find(tab => tab.id === id);
      
      if (existingTab) {
        // 기존 탭이 있으면 해당 탭을 활성화만 함
        setActiveTabId(id);
        return prevTabs;
      }
      
      // 새로운 탭 추가
      const newTab = { id, title, path, component };
      setActiveTabId(id); // 새 탭을 활성화
      return [...prevTabs, newTab];
    });
  }, []);

  /**
   * 탭 제거
   * @param {string} idToRemove - 제거할 탭의 ID
   */
  const removeTab = useCallback((idToRemove) => {
    setTabs(prevTabs => {
      const filteredTabs = prevTabs.filter(tab => tab.id !== idToRemove);
      
      // 제거하려는 탭이 현재 활성화된 탭인 경우
      if (activeTabId === idToRemove) {
        // 남은 탭이 있으면 마지막 탭을 활성화, 없으면 null
        if (filteredTabs.length > 0) {
          setActiveTabId(filteredTabs[filteredTabs.length - 1].id);
        } else {
          setActiveTabId(null);
        }
      }
      
      return filteredTabs;
    });
  }, [activeTabId]);

  /**
   * 특정 탭 활성화
   * @param {string} id - 활성화할 탭의 ID
   */
  const setActiveTab = useCallback((id) => {
    setActiveTabId(id);
  }, []);

  /**
   * 모든 탭 닫기
   */
  const closeAllTabs = useCallback(() => {
    setTabs([]);
    setActiveTabId(null);
  }, []);

  /**
   * ✅ 탭 초기화 함수 (로그인 시 호출)
   * - 홈 탭을 기본으로 설정
   */
  const initializeTabs = useCallback(() => {
    const homeTab = {
      id: 'home',
      title: '홈',
      path: '/',
      component: Home
    };
    setTabs([homeTab]);
    setActiveTabId('home');
  }, []);

  /**
   * 현재 활성화된 탭 정보 조회
   */
  const getActiveTab = useCallback(() => {
    return tabs.find(tab => tab.id === activeTabId);
  }, [tabs, activeTabId]);

  // Context에 제공할 값
  const value = {
    tabs,           // 모든 탭 목록
    activeTabId,    // 현재 활성 탭 ID
    addTab,         // 탭 추가 함수
    removeTab,      // 탭 제거 함수
    setActiveTab,   // 탭 활성화 함수
    closeAllTabs,   // 모든 탭 닫기 함수
    getActiveTab,   // 활성 탭 조회 함수
    initializeTabs  // ✅ 탭 초기화 함수 추가
  };

  return (
    <TabContext.Provider value={value}>
      {children}
    </TabContext.Provider>
  );
};