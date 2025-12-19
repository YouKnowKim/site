import React, { useEffect, useState } from 'react';
import {
  Form,
  Button,
  Card,
  Alert,
  Modal          // ✅ Modal 추가
} from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/images/logo.png';
import axios from 'axios';
import Swal from 'sweetalert2';
import { useTab } from '../components/TabContext';

const Login = () => {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { initializeTabs } = useTab();

  // ============================================
  // ✅ 비밀번호 강제 변경 모달 관련 상태
  // ============================================
  const [showPasswordModal, setShowPasswordModal] = useState(false);  // 모달 표시 여부
  const [passwordData, setPasswordData] = useState({
    newPassword: '',       // 새 비밀번호
    confirmPassword: ''    // 새 비밀번호 확인
  });
  const [tempLoginData, setTempLoginData] = useState(null);  // 로그인 성공 데이터 임시 저장

  // 페이지 로딩 시 이미 로그인되어 있다면 리다이렉트
  useEffect(() => {
    if (sessionStorage.getItem('authenticated') === 'true') {
      navigate('/');
    }
  }, [navigate]);

  /**
   * ============================================
   * 세션 스토리지에 로그인 정보 저장 및 페이지 이동
   * ============================================
   * @param {Object} loginData - 로그인 API 응답 데이터
   * @param {string} newPassword - 변경된 비밀번호 (선택적, 비밀번호 변경 시 사용)
   */
  const completeLogin = (loginData, newPassword = null) => {
    sessionStorage.setItem('authenticated', 'true');
    sessionStorage.setItem('teamPersonCd', loginData.teamPersonCd || '');
    sessionStorage.setItem('loginId', loginData.loginId || '');
    // ✅ 비밀번호 변경 시 새 비밀번호 저장, 아니면 기존 비밀번호 저장
    sessionStorage.setItem('loginPw', newPassword || loginData.loginPw || '');
    sessionStorage.setItem('teamPersonNm', loginData.teamPersonNm || '');
    sessionStorage.setItem('teamPersonType', loginData.teamPersonType || '');
    sessionStorage.setItem('managerYn', loginData.managerYn || '');
    sessionStorage.setItem('teamCd', loginData.teamCd || '');
    sessionStorage.setItem('promoTeamCd', loginData.promoTeamCd || '');
    sessionStorage.setItem('agencyYn', loginData.agencyYn || '');
    sessionStorage.setItem('agencyCd', loginData.agencyCd || '');
    sessionStorage.setItem('loginYn', loginData.loginYn || '');

    initializeTabs();
    navigate('/');
  };

  // ✅ 로그인 처리 함수
  const handleLogin = async () => {
    // 중복 제출 방지
    if (isLoading) return;
    
    // 입력값 검증
    if (!loginId.trim()) {
      setError('아이디를 입력해주세요.');
      return;
    }
    if (!password.trim()) {
      setError('비밀번호를 입력해주세요.');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      // IP 조회 (실패해도 로그인 진행)
      let clientIp = '';
      try {
        const ipResponse = await axios.get('https://ipapi.co/json/', {
          timeout: 3000
        });
        clientIp = ipResponse.data?.ip || '';
      } catch (ipError) {
        console.warn('IP 조회 실패 (무시):', ipError.message);
      }

      // 로그인 API 호출
      const loginResponse = await axios.get('/api/login/getLoginInfo', {
        params: { 
          loginId: loginId,
          loginPw: password,
          loginIp: clientIp,
          loginBrowser: navigator.userAgent
        },
        timeout: 10000
      });

      const loginData = loginResponse.data;

      // 상태별 처리
      switch (loginData.status) {
        case "0":
          setError('존재하지 않는 아이디입니다.');
          break;
        case "1":
          setError('비밀번호가 올바르지 않습니다.');
          break;
        case "2":
          // ============================================
          // ✅ 로그인 성공 - 비밀번호 '1234' 체크
          // ============================================
          if (loginData.loginPw === '1234') {
            // 비밀번호가 1234인 경우: 강제 변경 모달 표시
            setTempLoginData(loginData);  // 로그인 데이터 임시 저장
            setShowPasswordModal(true);   // 비밀번호 변경 모달 열기
            
            // 안내 메시지 표시
            Swal.fire({
              icon: 'warning',
              title: '비밀번호 변경 필요',
              text: '초기 비밀번호 사용 중입니다. 보안을 위해 비밀번호를 변경해주세요.',
              confirmButtonText: '확인'
            });
          } else {
            // 정상 로그인 처리
            completeLogin(loginData);
          }
          break;
        default:
          setError('알 수 없는 응답입니다. 관리자에게 문의하세요.');
      }

    } catch (error) {
      console.error('로그인 오류:', error);
      
      if (error.code === 'ECONNABORTED') {
        setError('서버 응답 시간이 초과되었습니다. 다시 시도해주세요.');
      } else if (error.response) {
        setError(`서버 오류: ${error.response.status}`);
      } else if (error.request) {
        setError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.');
      } else {
        setError('로그인 처리 중 오류가 발생했습니다.');
      }

    } finally {
      setIsLoading(false);
    }
  };

  // ✅ Enter 키 핸들러
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !isLoading) {
      handleLogin();
    }
  };

  /**
   * ============================================
   * 비밀번호 변경 모달 관련 핸들러
   * ============================================
   */

  /**
   * 비밀번호 변경 모달 닫기 시도 핸들러
   * - 초기 비밀번호 변경은 필수이므로 닫기 불가
   */
  const handleCloseModal = () => {
    Swal.fire({
      icon: 'warning',
      title: '비밀번호 변경 필수',
      text: '초기 비밀번호는 반드시 변경해야 합니다.',
      confirmButtonText: '확인'
    });
    // 모달을 닫지 않음 (강제 변경 필수)
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
   * ============================================
   * 비밀번호 유효성 검사 함수
   * ============================================
   * @param {string} password - 검사할 비밀번호
   * @returns {Object} { isValid: boolean, message: string }
   */
  const validatePassword = (password) => {
    // 최소 길이 검사 (8자 이상 권장)
    if (password.length < 8) {
      return { 
        isValid: false, 
        message: '비밀번호는 8자 이상이어야 합니다.' 
      };
    }

    // 영문자 포함 여부 검사 (대문자 또는 소문자)
    const hasLetter = /[a-zA-Z]/.test(password);
    if (!hasLetter) {
      return { 
        isValid: false, 
        message: '비밀번호에 영문자를 포함해야 합니다.' 
      };
    }

    // 숫자 포함 여부 검사
    const hasNumber = /[0-9]/.test(password);
    if (!hasNumber) {
      return { 
        isValid: false, 
        message: '비밀번호에 숫자를 포함해야 합니다.' 
      };
    }

    // 특수문자 포함 여부 검사
    const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?`~]/.test(password);
    if (!hasSpecialChar) {
      return { 
        isValid: false, 
        message: '비밀번호에 특수문자를 포함해야 합니다.' 
      };
    }

    return { isValid: true, message: '' };
  };

  /**
   * 비밀번호 변경 제출 핸들러
   * - 유효성 검사 수행
   * - API 호출하여 비밀번호 변경
   * - 성공 시 로그인 완료 처리
   * @param {Event} e - 이벤트 객체
   */
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    
    const { newPassword, confirmPassword } = passwordData;

    // ✅ 필수 입력 항목 검사
    if (!newPassword || !confirmPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '모든 항목을 입력해주세요.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 새 비밀번호가 1234와 같은지 검사
    if (newPassword === '1234') {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '초기 비밀번호(1234)와 다른 비밀번호를 입력해주세요.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 새 비밀번호 일치 확인
    if (newPassword !== confirmPassword) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: '새 비밀번호가 일치하지 않습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 비밀번호 복잡도 검사 (영문 + 숫자 + 특수문자)
    const validation = validatePassword(newPassword);
    if (!validation.isValid) {
      Swal.fire({
        icon: 'warning',
        title: '입력 오류',
        text: validation.message,
        confirmButtonText: '확인'
      });
      return;
    }

    try {
      // 비밀번호 변경 API 호출
      const response = await axios.post('/api/login/changePassword', {
        loginId: tempLoginData.loginId,
        loginPw: newPassword
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
        
        // ✅ 비밀번호 변경 완료 후 로그인 완료 처리
        setShowPasswordModal(false);
        completeLogin(tempLoginData, newPassword);
        
      } else {
        Swal.fire({
          icon: 'error',
          title: '변경 실패',
          text: response.data.message || '비밀번호 변경에 실패했습니다.',
          confirmButtonText: '확인'
        });
      }
    } catch (error) {
      console.error('비밀번호 변경 오류:', error);
      
      if (error.response) {
        Swal.fire({
          icon: 'error',
          title: '변경 실패',
          text: error.response.data.message || '비밀번호 변경에 실패했습니다.',
          confirmButtonText: '확인'
        });
      } else if (error.request) {
        Swal.fire({
          icon: 'error',
          title: '네트워크 오류',
          text: '서버와 통신할 수 없습니다. 네트워크 연결을 확인해주세요.',
          confirmButtonText: '확인'
        });
      } else {
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

            <div>
              <Form.Group className="mb-3" controlId="formUsername">
                <Form.Label>아이디</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="아이디 입력"
                  value={loginId}
                  onChange={(e) => setLoginId(e.target.value)}
                  onKeyDown={handleKeyDown}
                  disabled={isLoading}
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
                  onKeyDown={handleKeyDown}
                  disabled={isLoading}
                  autoComplete="current-password"
                  style={{ padding: '10px 12px' }}
                />
              </Form.Group>

              <div className="d-grid">
                <Button 
                  type="button"
                  variant="primary"
                  size="lg"
                  onClick={handleLogin}
                  disabled={isLoading}
                  style={{ 
                    padding: '12px',
                    fontWeight: '500'
                  }}
                >
                  {isLoading ? '로그인 중...' : '로그인'}
                </Button>
              </div>
            </div>
          </Card.Body>
        </Card>
      </div>

      {/* ============================================
          ✅ 비밀번호 강제 변경 모달
          - backdrop="static": 모달 외부 클릭으로 닫기 방지
          - keyboard={false}: ESC 키로 닫기 방지
          ============================================ */}
      <Modal 
        show={showPasswordModal} 
        onHide={handleCloseModal} 
        centered
        backdrop="static"
        keyboard={false}
      >
        <Modal.Header>
          {/* ✅ closeButton 제거 - 강제 변경이므로 닫기 버튼 없음 */}
          <Modal.Title>비밀번호 변경 (필수)</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Alert variant="info" className="mb-3">
            <small>
              <strong>안내:</strong> 초기 비밀번호(1234) 사용 중입니다.<br />
              보안을 위해 새로운 비밀번호로 변경해주세요.<br /><br />
              <strong>비밀번호 규칙:</strong> 8자 이상, 영문 + 숫자 + 특수문자 조합
            </small>
          </Alert>
          
          <Form onSubmit={handlePasswordSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>새 비밀번호</Form.Label>
              <Form.Control
                type="password"
                name="newPassword"
                value={passwordData.newPassword}
                onChange={handlePasswordChange}
                placeholder="새 비밀번호를 입력하세요"
                required
                autoFocus
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

            <div className="d-grid">
              {/* ✅ 취소 버튼 없음 - 강제 변경 필수 */}
              <Button variant="primary" type="submit">
                비밀번호 변경
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>
    </div>
  );
};

export default Login;