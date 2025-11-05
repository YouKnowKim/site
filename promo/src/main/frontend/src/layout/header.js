import React, { useState } from 'react';
import '../styles/Header.css';
import logo from '../assets/images/logo.png';
import 'bootstrap/dist/css/bootstrap.min.css';
import { useNavigate } from 'react-router-dom';
import { Nav, NavDropdown, Modal, Button, Form } from 'react-bootstrap';
import Swal from 'sweetalert2';
import axios from 'axios';
import { useTab } from '../components/TabContext'; // ✅ useTab Hook import

// ✅ 페이지 컴포넌트들 import (실제 경로에 맞게 수정 필요)
import Home from '../pages/Home';
import MilkFileMng from '../pages/MilkFileMng';
import MilkFileNotSubmit from '../pages/MilkFileNotSubmit';
import PromotionSettle from '../pages/PromotionSettle';
import AgencyMng from '../pages/AgencyMng';
// 추가 페이지 컴포넌트들을 import 하세요

/**
 * 헤더 컴포넌트
 * - 네비게이션 메뉴
 * - 비밀번호 변경
 * - 로그아웃
 * - 멀티탭 연동
 */
const Header = () => {

  const navigate = useNavigate();
  const teamPersonNm = '' + sessionStorage.getItem('teamPersonNm') + '님';
  
  // ✅ 탭 컨텍스트에서 addTab 함수 가져오기
  const { addTab, closeAllTabs } = useTab();

  // 비밀번호 변경 모달 상태 관리
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  /**
   * 로그아웃 버튼 클릭 핸들러
   * - 세션 스토리지 클리어
   * - 로그인 페이지로 이동
   */
  const handleLogoutClick = (e) => {
    e.preventDefault();

    if(closeAllTabs){
      closeAllTabs();
    }
    sessionStorage.clear();
    navigate('/login');
  };

  /**
   * ✅ 메뉴 클릭 핸들러 - 탭으로 페이지 추가
   * @param {Event} e - 이벤트 객체
   * @param {string} id - 탭 고유 ID
   * @param {string} title - 탭 제목
   * @param {string} path - 라우팅 경로 (사용하지 않지만 유지)
   * @param {React.Component} component - 렌더링할 컴포넌트
   */
  const handleMenuClick = (e, id, title, path, component) => {
    e.preventDefault();
    addTab(id, title, path, component);
  };

  /**
   * 사용자 이름 클릭 핸들러
   * - 비밀번호 변경 모달 열기
   */
  const handleNameClick = () => {
    setShowPasswordModal(true);
  };

  /**
   * 비밀번호 변경 모달 닫기
   * - 모달 상태 초기화
   */
  const handleCloseModal = () => {
    setShowPasswordModal(false);
    setPasswordData({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
  };

  /**
   * 비밀번호 입력 값 변경 핸들러
   * @param {Event} e - 이벤트 객체
   */
  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  /**
   * 비밀번호 변경 제출 핸들러
   * - 유효성 검사 수행
   * - API 호출하여 비밀번호 변경
   * @param {Event} e - 이벤트 객체
   */
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    
    // 필수 입력 항목 검사
    if (!passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '모든 항목을 입력해주세요.',
        confirmButtonText: '확인'
      });
      return;
    }

    // 현재 비밀번호 확인
    if (sessionStorage.getItem('loginPw') !== passwordData.currentPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '현재 비밀번호가 일치하지 않습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // 새 비밀번호 일치 확인
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '새 비밀번호가 일치하지 않습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    try {
      // 비밀번호 변경 API 호출
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
        // 세션 스토리지의 비밀번호도 업데이트
        sessionStorage.setItem('loginPw', passwordData.newPassword);
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
  };

  return (
    <>
      <header className="header p-3 bg-white text-black">
        <div className="container">
          <div className="d-flex flex-wrap align-items-center justify-content-center justify-content-lg-start">
            
            {/* 로고 - 홈 탭 열기 */}
            <a 
              href="/" 
              className="d-flex align-items-center mb-2 mb-lg-0 text-black text-decoration-none me-4"
              onClick={(e) => handleMenuClick(e, 'home', '홈', '/', Home)}
            >
              <img src={logo} alt="연세우유 로고" width="40" height="auto" className="me-2" />
            </a>

            {/* ✅ 네비게이션 메뉴 - 탭 방식으로 변경 */}
            <Nav className="me-auto">
              
              {/* 판촉파일 관리 */}
              <NavDropdown title="판촉파일 관리" id="promo-file-dropdown">
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'milk-file-mng', '밀크방 파일 관리', '/MilkFileMng', MilkFileMng)}
                >
                  밀크방 파일 관리
                </NavDropdown.Item>
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'milk-file-not-submit', '밀크방 미전송 대리점', '/MilkFileNotSubmit', MilkFileNotSubmit)}
                >
                  밀크방 미전송 대리점
                </NavDropdown.Item>
              </NavDropdown>

              {/* 판촉실적 정산 */}
              <NavDropdown title="판촉실적 정산" id="promo-settle-dropdown">
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'promotion-settle', '판촉실적 정산', '/PromotionSettle', PromotionSettle)}
                >
                  판촉실적 정산
                </NavDropdown.Item>
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'promo-close', '판촉실적 마감', '/PromoClose', Home)}
                >
                  판촉실적 마감
                </NavDropdown.Item>
              </NavDropdown>

              {/* 판촉팀별 실적 */}
              <NavDropdown title="판촉팀별 실적" id="team-performance-dropdown">
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'team-performance', '판촉팀별 실적', '/TeamPerformance', Home)}
                >
                  판촉팀별 실적
                </NavDropdown.Item>
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'weekly-performance', '판촉사원별 주간 실적', '/WeeklyPerformance', Home)}
                >
                  판촉사원별 주간 실적
                </NavDropdown.Item>
              </NavDropdown>

              {/* 판촉실적 통계 */}
              <NavDropdown title="판촉실적 통계" id="promo-stats-dropdown">
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'stat-all', '판촉 실적 보고(전체)', '/StatAll', Home)}
                >
                  판촉 실적 보고(전체)
                </NavDropdown.Item>
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'stat-team', '판촉 실적 보고(팀)', '/StatTeam', Home)}
                >
                  판촉 실적 보고(팀)
                </NavDropdown.Item>
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'agency-weekly', '대리점별 주간 실적', '/AgencyWeekly', Home)}
                >
                  대리점별 주간 실적
                </NavDropdown.Item>
              </NavDropdown>

              {/* 해피콜 관리 */}
              <NavDropdown title="해피콜 관리" id="happy-call-dropdown">
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'happy-call', '해피콜', '/HappyCall', Home)}
                >
                  해피콜
                </NavDropdown.Item>
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'happy-call-result', '해피콜 결과', '/HappyCallResult', Home)}
                >
                  해피콜 결과
                </NavDropdown.Item>
              </NavDropdown>

              {/* 설정 */}
              <NavDropdown title="설정" id="setting-dropdown">
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'agency-manage', '대리점 등록 관리', '/AgencyMng', AgencyMng)}
                >
                  대리점 등록 관리
                </NavDropdown.Item>
                <NavDropdown.Item 
                  onClick={(e) => handleMenuClick(e, 'promo-count-setting', '판촉홉수 설정', '/PromoCountSetting', Home)}
                >
                  판촉홉수 설정
                </NavDropdown.Item>
              </NavDropdown>
            </Nav>

            {/* 사용자 정보 및 로그아웃 */}
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