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
        
        <section className="menu-section">
          <div className="menu-card">
            <h3>프로모션 조회</h3>
            <p>진행 중인 프로모션을 확인하세요</p>
            <button>바로가기</button>
          </div>
          
          <div className="menu-card">
            <h3>프로모션 등록</h3>
            <p>새로운 프로모션을 등록하세요</p>
            <button>바로가기</button>
          </div>
          
          <div className="menu-card">
            <h3>통계 관리</h3>
            <p>프로모션 통계를 확인하세요</p>
            <button>바로가기</button>
          </div>
        </section>
      </main>
    </div>
  );
}

export default Home;