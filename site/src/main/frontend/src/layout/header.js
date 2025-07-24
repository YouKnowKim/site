import React from 'react';
import '../styles/Header.css';
import logo from '../assets/images/logo.png'; // 상대경로
import 'bootstrap/dist/css/bootstrap.min.css'; // Bootstrap CSS import (npm 설치 시)
import { useNavigate } from 'react-router-dom';
import { Nav, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const Header = () => {

  const navigate = useNavigate(); // ✅ Hook 사용
  const name = sessionStorage.getItem('name');

  const handleLogoutClick = (e) => {

    e.preventDefault();
    sessionStorage.clear();
    navigate('/login'); // ✅ login.js 경로로 이동
  };

  return (
    <header className="header p-3 bg-white text-black">
      <div className="container">
        <div className="d-flex flex-wrap align-items-center justify-content-center justify-content-lg-start">
          
          <a href="/" className="d-flex align-items-center mb-2 mb-lg-0 text-black text-decoration-none me-4">
            {/* 로고 SVG 또는 텍스트 로고 대체 */}
            <img src={logo} alt="연세우유 로고" width="140" height="auto" className="me-2" />
          </a>

          <Nav className="me-auto">
            <NavDropdown title="메뉴" id="basic-nav-dropdown">
              <NavDropdown.Item as={Link} to="/Map">대리점별 권역 조회</NavDropdown.Item>
              <NavDropdown.Item as={Link} to="/MapEdit">대리점별 권역 지정</NavDropdown.Item>
              {/* <NavDropdown.Divider />
              <NavDropdown.Item href="#">기타 항목</NavDropdown.Item> */}
            </NavDropdown>
            {/* <Nav.Link href="#">About</Nav.Link> */}
          </Nav>

          <div className="text-start">
            {/* <button type="button" className="btn btn-outline-dark me-2" onClick={handleLoginClick}>Login</button> */}
            <span className="fw-semibold text-dark text-nowrap me-3"> {name} </span>
            <button type="button" className="btn btn-primary" onClick={handleLogoutClick}>Logout</button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;