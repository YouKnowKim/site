import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, Navigate } from 'react-router-dom';
import Header from './layout/header.js';
import Footer from './layout/footer.js';
import Login from './pages/Login.js';
import Map from './pages/Map.js';
import MapEdit from './pages/MapEdit.js';
import ProtectedRoute from './components/ProtectedRoute';
// import axios from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';
// import logo from './logo.svg';
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
          <Route path="/" element={<ProtectedRoute><Map /></ProtectedRoute>} />
          <Route path="/Map" element={<ProtectedRoute><Map /></ProtectedRoute>} />
          <Route path="/MapEdit" element={<ProtectedRoute><MapEdit /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      {path === true ? <Footer /> : null}
    </div>
  );
}

export default App;
