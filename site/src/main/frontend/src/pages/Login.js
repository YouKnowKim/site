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

const Login = () => {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // 페이지 로딩 시 이미 로그인되어 있다면 리다이렉트
  useEffect(() => {
    if (sessionStorage.getItem('authenticated') === 'true') {
      navigate('/login');
    }
  }, []);

  const handleLogin = (e) => {
    e.preventDefault();

    let gubunType = '';
    let name = '';
    let valid = false;

    if (userId === 'admin' && password === '1q2w3e4r!!') {
      gubunType = '10';
      name = '대리점';
      valid = true;
    } else if (userId === 'admin02' && password === '1q2w3e4r!!') {
      gubunType = '20';
      name = '유통';
      valid = true;
    } else if (userId === 'superadmin' && password === 'ys1234') {
      gubunType = '10';
      name = '대리점';
      valid = true;
    } else if (userId === 'superadmin02' && password === 'ys1234') {
      gubunType = '20';
      name = '유통';
      valid = true;
    }

    if (valid) {
      sessionStorage.setItem('authenticated', 'true');
      sessionStorage.setItem('username', userId.startsWith('superadmin') ? 'superadmin' : userId);
      sessionStorage.setItem('name', name);
      sessionStorage.setItem('gubunType', gubunType);
      navigate('/');
    } else {
      setError('아이디 또는 비밀번호가 올바르지 않습니다.');
    }
  };

  return (
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
                    value={userId}
                    onChange={(e) => setUserId(e.target.value)}
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
  );
};

export default Login;