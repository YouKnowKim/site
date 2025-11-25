import {useState } from 'react';
import { Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { TabProvider } from './components/TabContext'; // ✅ TabProvider import
import Header from './layout/header.js';
import Footer from './layout/footer.js';
import Login from './pages/Login.js';
import TabManager from './components/TabManager'; // ✅ TabManager import
import ProtectedRoute from './components/ProtectedRoute';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/App.css';

/**
 * 메인 App 컴포넌트
 * - 로그인 페이지는 기존 라우팅 방식 유지
 * - 로그인 후에는 멀티탭 시스템 사용
 */
function App() {
  const [path] = useState(false);
  const location = useLocation();
  
  // 로그인 페이지에서는 헤더 숨김
  const showHeader = location.pathname !== '/login';
  
  // 로그인 페이지 여부 확인
  const isLoginPage = location.pathname === '/login';

  return (
    <TabProvider>
      <div className="app">
        {/* 헤더는 로그인 페이지가 아닐 때만 표시 */}
        {showHeader && <Header />}
        
        <main className="content">
          {isLoginPage ? (
            // 로그인 페이지는 기존 라우팅 방식 사용
            <Routes>
              <Route path="/Login" element={<Login />} />
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          ) : (
            // 로그인 후에는 멀티탭 시스템 사용
            <ProtectedRoute>
              <TabManager />
            </ProtectedRoute>
          )}
        </main>
        
        {/* Footer는 필요 시 표시 */}
        {path === true ? <Footer /> : null}
      </div>
    </TabProvider>
  );
}

export default App;