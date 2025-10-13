import React from 'react';
import { useNavigate } from 'react-router-dom';

function Home() {
  return (
    <div className="home-container">
      <header className="home-header">
        <h1>연세유업 프로모션 관리 시스템</h1>
        <p>Promotion Management System</p>
      </header>
      
      <main className="home-content">
        <section className="welcome-section">
          <h2>환영합니다</h2>
          <p>프로모션 관리 시스템에 오신 것을 환영합니다.</p>
        </section>
      </main>
    </div>
  );
}

export default Home;