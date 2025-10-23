import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, Navigate } from 'react-router-dom';
import Header from './layout/header.js';
import Footer from './layout/footer.js';
import Login from './pages/Login.js';
import Home from './pages/Home.js';
import MilkFileMng from './pages/MilkFileMng.js';
import MilkFileNotSubmit from './pages/MilkFileNotSubmit.js';
import PromotionSettle from './pages/PromotionSettle.js';
import ProtectedRoute from './components/ProtectedRoute';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/App.css';

function App() {
  const [path] = useState(false);
  const location = useLocation();
  const showHeader = location.pathname !== '/login';

  return (
    <div className="app">
      {showHeader && <Header />}
      <main className="content">
        <Routes>
          <Route path="/Login" element={<Login />} />
          <Route path="/" element={<ProtectedRoute><Home /></ProtectedRoute>} />
          <Route path="/MilkFileMng" element={<ProtectedRoute><MilkFileMng /></ProtectedRoute>} />
          <Route path="/MilkFileNotSubmit" element={<ProtectedRoute><MilkFileNotSubmit /></ProtectedRoute>} />
          <Route path="/PromotionSettle" element={<ProtectedRoute><PromotionSettle /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      {path === true ? <Footer /> : null}
    </div>
  );
}

export default App;
