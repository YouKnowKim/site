import React, { useEffect, useState } from 'react';
import {
  Container,
  Form,
  Button,
  Card,
  Row,
  Col,
  Alert
} from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/images/logo.png'; // 상대경로
import axios from 'axios';
import Swal from 'sweetalert2';

const Login = () => {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // 페이지 로딩 시 이미 로그인되어 있다면 리다이렉트
  useEffect(() => {
    if (sessionStorage.getItem('authenticated') === 'true') {
      navigate('/login');
    }
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();

    let teamPersonCd = '';
    let teamPersonNm = '';
    let teamPersonType = '';
    let managerYn = '';
    let teamCd = '';
    let agencyYn = '';
    let agencyCd = '';
    let loginYn = '';

    let gubunType = '';
    let loginData = {};
    let valid = false;

    // IP 정보 가져오기 (이게 완료될 때까지 다음 줄 실행 안 됨)
    const ipResponse = await axios.get('https://ipapi.co/json/');

    const loginResponse = await axios.get(`/api/login/getLoginInfo`, {
      params: { 
                loginId: loginId 
               ,loginPw : password
               ,loginIp : ipResponse.data.ip
               ,loginBrowser : navigator.userAgent
              }
    })
    .then(response => {
      loginData = response.data;
    })
    .catch(error => {
      Swal.fire({
        icon: 'error',
        title: '로그인 오류',
        text: error
      });
    });

    if (loginData) {

      if (loginData.status == "0") {
        setError('존재하지 않는 아이디입니다.');

      } else if (loginData.status == "1") {
        setError('비밀번호가 올바르지 않습니다.');

      } else if (loginData.status == "2") {
        sessionStorage.setItem('authenticated', 'true');
        sessionStorage.setItem('teamPersonCd', loginData.teamPersonCd);
        sessionStorage.setItem('loginId', loginData.loginId);
        sessionStorage.setItem('loginPw', loginData.loginPw);
        sessionStorage.setItem('teamPersonNm', loginData.teamPersonNm);
        sessionStorage.setItem('teamPersonType', loginData.teamPersonType);
        sessionStorage.setItem('managerYn', loginData.managerYn);
        sessionStorage.setItem('teamCd', loginData.teamCd);
        sessionStorage.setItem('agencyYn', loginData.agencyYn);
        sessionStorage.setItem('agencyCd', loginData.agencyCd);
        sessionStorage.setItem('loginYn', loginData.loginYn);
        navigate('/');
      }
    }
  };

  return (
    <div>
      {/* 로고 영역 */}
      <div style={{ 
        padding: '20px',
        display: 'flex',
        alignItems: 'center',
        gap: '20px'  // ✅ 15px에서 20px로 증가
      }}>
        <img 
          src={logo}
          alt="연세유업 로고" 
          style={{ 
            height: '50px',
            cursor: 'pointer'
          }}
          onClick={() => navigate('/')}
        />
        <span style={{
          fontSize: '24px',
          fontWeight: 'bold',
          color: '#1a5490'
        }}>
          판촉관리사이트
        </span>
      </div>
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh'}}>
        <Card style={{ width: '100%', maxWidth: '410px' }} className="p-4 shadow">
          <Card.Body>
            <h3 className="text-center mb-4">로그인</h3>

            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleLogin}>
              <Form.Group className="mb-3" controlId="formUsername">
                  <Form.Label>아이디</Form.Label>
                  <Form.Control
                      type="text"
                      placeholder="아이디 입력"
                      value={loginId}
                      onChange={(e) => setLoginId(e.target.value)}
                      required
                  />
              </Form.Group>

              <Form.Group className="mb-3" controlId="formPassword">
                <Form.Label>비밀번호</Form.Label>
                <Form.Control
                  type="password"
                  placeholder="비밀번호 입력"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </Form.Group>

              <div className="d-grid mb-3">
                <Button type="submit" variant="primary">
                  로그인
                </Button>
              </div>

              {/* <div className="d-flex justify-content-between">
                <a href="#">비밀번호 찾기</a>
                <a href="#">회원가입</a>
              </div> */}
            </Form>
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
};

export default Login;