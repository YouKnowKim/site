import React, { useEffect, useState } from 'react';
import {
  Form,
  Button,
  Card,
  Alert
} from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/images/logo.png';
import axios from 'axios';
import Swal from 'sweetalert2';
import { useTab } from '../components/TabContext'; // ✅ useTab Hook import

const Login = () => {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // ✅ 탭 컨텍스트에서 initializeTabs 함수 가져오기
  const { initializeTabs } = useTab();

  // 페이지 로딩 시 이미 로그인되어 있다면 리다이렉트
  useEffect(() => {
    if (sessionStorage.getItem('authenticated') === 'true') {
      navigate('/');
    }
  }, [navigate]);

  // 페이지 로딩 시 이미 로그인되어 있다면 리다이렉트
  useEffect(() => {
    if (sessionStorage.getItem('authenticated') === 'true') {
      navigate('/');
    }
  }, [navigate]);

  const handleLogin = async (e) => {
    e.preventDefault();

    let loginData = {};

    try {
      // IP 정보 가져오기
      const ipResponse = await axios.get('https://ipapi.co/json/');

      // 로그인 정보 요청
      const loginResponse = await axios.get(`/api/login/getLoginInfo`, {
        params: { 
          loginId: loginId,
          loginPw: password,
          loginIp: ipResponse.data.ip,
          loginBrowser: navigator.userAgent
        }
      });

      loginData = loginResponse.data;

      if (loginData.status === "0") {
        setError('존재하지 않는 아이디입니다.');
      } else if (loginData.status === "1") {
        setError('비밀번호가 올바르지 않습니다.');
      } else if (loginData.status === "2") {
        sessionStorage.setItem('authenticated', 'true');
        sessionStorage.setItem('teamPersonCd', loginData.teamPersonCd);
        sessionStorage.setItem('loginId', loginData.loginId);
        sessionStorage.setItem('loginPw', loginData.loginPw);
        sessionStorage.setItem('teamPersonNm', loginData.teamPersonNm);
        sessionStorage.setItem('teamPersonType', loginData.teamPersonType);
        sessionStorage.setItem('managerYn', loginData.managerYn);
        sessionStorage.setItem('teamCd', loginData.teamCd);
        sessionStorage.setItem('promoTeamCd', loginData.promoTeamCd);
        sessionStorage.setItem('agencyYn', loginData.agencyYn);
        sessionStorage.setItem('agencyCd', loginData.agencyCd);
        sessionStorage.setItem('loginYn', loginData.loginYn);

        // ✅ 탭 초기화 (홈 화면으로)
        initializeTabs();

        navigate('/');
      }
    } catch (error) {
      console.error('로그인 오류:', error);
      Swal.fire({
        icon: 'error',
        title: '로그인 오류',
        text: '로그인 처리 중 오류가 발생했습니다.'
      });
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      width: '100%',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'white',
      padding: '20px'
    }}>
      {/* ✅ 로고와 로그인 카드를 하나의 컨테이너로 묶음 */}
      <div style={{
        width: '100%',
        maxWidth: '420px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center'
      }}>
        {/* 로고 영역 */}
        <div style={{ 
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '15px',
          marginBottom: '30px'
        }}>
          <img 
            src={logo}
            alt="연세유업 로고" 
            style={{ 
              height: '60px',
              cursor: 'pointer'
            }}
            onClick={() => navigate('/')}
          />
        </div>

        {/* 로그인 카드 */}
        <Card 
          style={{ 
            width: '100%',
            border: 'none',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}
        >
          <Card.Body className="p-4">
            <h3 className="text-center mb-4" style={{ fontWeight: '600' }}>로그인</h3>

            {error && <Alert variant="danger" className="mb-3">{error}</Alert>}

            <Form onSubmit={handleLogin}>
              <Form.Group className="mb-3" controlId="formUsername">
                <Form.Label>아이디</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="아이디 입력"
                  value={loginId}
                  onChange={(e) => setLoginId(e.target.value)}
                  required
                  autoComplete="username"
                  style={{ padding: '10px 12px' }}
                />
              </Form.Group>

              <Form.Group className="mb-4" controlId="formPassword">
                <Form.Label>비밀번호</Form.Label>
                <Form.Control
                  type="password"
                  placeholder="비밀번호 입력"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="current-password"
                  style={{ padding: '10px 12px' }}
                />
              </Form.Group>

              <div className="d-grid">
                <Button 
                  type="submit" 
                  variant="primary"
                  size="lg"
                  style={{ 
                    padding: '12px',
                    fontWeight: '500'
                  }}
                >
                  로그인
                </Button>
              </div>
            </Form>
          </Card.Body>
        </Card>
      </div>

      {/* ✅ 모바일 반응형 스타일 */}
      <style>{`
        @media (max-width: 576px) {
          .card-body {
            padding: 1.5rem !important;
          }
          
          h3 {
            font-size: 1.5rem !important;
          }
        }
        
        @media (max-width: 400px) {
          /* 로고 크기 조정 */
          img[alt="연세유업 로고"] {
            height: 50px !important;
          }
          
          /* 타이틀 크기 조정 */
          span {
            font-size: 20px !important;
          }
        }
      `}</style>
    </div>
  );
};

export default Login;