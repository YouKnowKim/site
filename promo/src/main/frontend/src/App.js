import React, { useState } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import Header from './layout/header.js';
import Footer from './layout/footer.js';
import Login from './pages/Login.js';
import ProtectedRoute from './components/ProtectedRoute';
import TabManager from './components/TabManager';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/App.css';

function AppContent() {
  const [path] = useState(false);
  const location = useLocation();
  const showHeader = location.pathname !== '/login';
  const isLoginPage = location.pathname === '/login';

  return (
    <div className="app">
      {showHeader && <Header />}
      <main className="content">
        {isLoginPage ? (
          <Routes>
            <Route path="/Login" element={<Login />} />
          </Routes>
        ) : (
          <ProtectedRoute>
            <TabManager />
          </ProtectedRoute>
        )}
      </main>
      {path === true ? <Footer /> : null}
    </div>
  );
}

function App() {
  return <AppContent />;
}

export default App;
