import React, { useState } from 'react';
import '../styles/Header.css';
import logo from '../assets/images/logo.png'; // 상대경로
import 'bootstrap/dist/css/bootstrap.min.css'; // Bootstrap CSS import (npm 설치 시)
import { useNavigate } from 'react-router-dom';
import { Nav, NavDropdown, Modal, Button, Form } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Swal from 'sweetalert2';
import axios from 'axios';

const Header = () => {

  const navigate = useNavigate(); // ✅ Hook 사용
  const teamPersonNm = '' + sessionStorage.getItem('teamPersonNm') + '님';

  // 비밀번호 변경 모달 상태 관리
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const handleLogoutClick = (e) => {

    e.preventDefault();
    sessionStorage.clear();
    navigate('/login'); // ✅ login.js 경로로 이동
  };

  // 비밀번호 변경 모달 열기
  const handleNameClick = () => {
    setShowPasswordModal(true);
  };

  // 비밀번호 변경 모달 닫기
  const handleCloseModal = () => {
    setShowPasswordModal(false);
    setPasswordData({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
  };

  // 비밀번호 입력 처리
  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // 비밀번호 변경 제출
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    
    // 유효성 검사
    if (!passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '모든 항목을 입력해주세요.',
        confirmButtonText: '확인'
      });
      return;
    }

    if (sessionStorage.getItem('loginPw') !== passwordData.currentPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '현재 비밀번호가 일치하지 않습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '새 비밀번호가 일치하지 않습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // API 호출 예시 (실제 구현 시 사용)
    
    try {
      // axios를 사용한 API 호출
      const response = await axios.post('/api/login/changePassword', {
        loginId: sessionStorage.getItem('loginId'),
        loginPw: passwordData.newPassword
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      // 성공 처리
      if (response.data.msg !== "f") {
        await Swal.fire({
          icon: 'success',
          title: '변경 완료',
          text: '비밀번호가 성공적으로 변경되었습니다.',
          confirmButtonText: '확인'
        });
        handleCloseModal();
      } else {
        Swal.fire({
          icon: 'error',
          title: '변경 실패',
          text: response.data.message || '비밀번호 변경에 실패했습니다.',
          confirmButtonText: '확인'
        });
      }
    } catch (error) {
      // 에러 처리
      console.error('비밀번호 변경 오류:', error);
      
      // 서버 응답이 있는 경우
      if (error.response) {
        Swal.fire({
          icon: 'error',
          title: '변경 실패',
          text: error.response.data.message || '비밀번호 변경에 실패했습니다.',
          confirmButtonText: '확인'
        });
      } 
      // 네트워크 오류
      else if (error.request) {
        Swal.fire({
          icon: 'error',
          title: '네트워크 오류',
          text: '서버와 통신할 수 없습니다. 네트워크 연결을 확인해주세요.',
          confirmButtonText: '확인'
        });
      } 
      // 기타 오류
      else {
        Swal.fire({
          icon: 'error',
          title: '오류 발생',
          text: '비밀번호 변경 중 오류가 발생했습니다.',
          confirmButtonText: '확인'
        });
      }
    }

    handleCloseModal();
  };

return (
    <>
      <header className="header p-3 bg-white text-black">
        <div className="container">
          <div className="d-flex flex-wrap align-items-center justify-content-center justify-content-lg-start">
            
            <a href="/" className="d-flex align-items-center mb-2 mb-lg-0 text-black text-decoration-none me-4">
              <img src={logo} alt="연세우유 로고" width="140" height="auto" className="me-2" />
            </a>

            <Nav className="me-auto">
              <NavDropdown title="판촉실적관리" id="basic-nav-dropdown">
                <NavDropdown.Item as={Link} to="/MilkFileMng">밀크방 파일 관리</NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/MapEdit">밀크방 미전송 대리점</NavDropdown.Item>
              </NavDropdown>
              <NavDropdown title="대리점" id="basic-nav-dropdown">
                <NavDropdown.Item as={Link} to="/Map">밀크방 파일 관리</NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/MapEdit">밀크방 미전송 대리점</NavDropdown.Item>
              </NavDropdown>
            </Nav>

            <div className="text-start">
              <span 
                className="fw-semibold text-dark text-nowrap me-3" 
                onClick={handleNameClick}
                style={{ cursor: 'pointer', textDecoration: 'underline' }}
                title="클릭하여 비밀번호 변경"
              >
                {teamPersonNm}
              </span>
              <button type="button" className="btn btn-primary" onClick={handleLogoutClick}>Logout</button>
            </div>
          </div>
        </div>
      </header>

      {/* 비밀번호 변경 모달 */}
      <Modal show={showPasswordModal} onHide={handleCloseModal} centered>
        <Modal.Header closeButton>
          <Modal.Title>비밀번호 변경</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handlePasswordSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>현재 비밀번호</Form.Label>
              <Form.Control
                type="password"
                name="currentPassword"
                value={passwordData.currentPassword}
                onChange={handlePasswordChange}
                placeholder="현재 비밀번호를 입력하세요"
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>새 비밀번호</Form.Label>
              <Form.Control
                type="password"
                name="newPassword"
                value={passwordData.newPassword}
                onChange={handlePasswordChange}
                placeholder="새 비밀번호를 입력하세요"
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>새 비밀번호 확인</Form.Label>
              <Form.Control
                type="password"
                name="confirmPassword"
                value={passwordData.confirmPassword}
                onChange={handlePasswordChange}
                placeholder="새 비밀번호를 다시 입력하세요"
                required
              />
            </Form.Group>

            <div className="d-flex justify-content-end gap-2">
              <Button variant="secondary" onClick={handleCloseModal}>
                취소
              </Button>
              <Button variant="primary" type="submit">
                변경
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>
    </>
  );
};

export default Header;