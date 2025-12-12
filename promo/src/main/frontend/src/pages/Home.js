import React, { useMemo } from 'react';
import { Container, Row, Col, Card } from 'react-bootstrap';
import { useTab } from '../components/TabContext';

// ✅ 페이지 컴포넌트 import (권한별 퀵메뉴용)
import MilkFileMng from './MilkFileMng';
import PromotionSettle from './PromotionSettle';
import PromotionSettlePivot from './PromotionSettlePivot';
import PromoTeamPerf from './PromoTeamPerf';
import AgencyMangement from './AgencyMangement';
import HappyCall from './HappyCall';
import AgencyMng from './AgencyMng';
import TeamPersonMng from './TeamPersonMng';
import MilkFileNotSubmit from './MilkFileNotSubmit';
import PromotionClose from './PromotionClose';
import PromoPersonPerf from './PromoPersonPerf';
import PromotionTeamSettlePivot from './PromotionTeamSettlePivot';
import HappyCallResult from './HappyCallResult';
import GoodsMng from './GoodsMng';

/**
 * ============================================
 * 홈 화면 컴포넌트
 * ============================================
 * - 사용자 환영 메시지
 * - 권한별 퀵 메뉴 카드
 * - 시스템 안내 정보
 */
function Home() {
  
  // ✅ 탭 컨텍스트
  const { addTab } = useTab();

  // ✅ 세션에서 사용자 정보 가져오기
  const userInfo = useMemo(() => ({
    name: sessionStorage.getItem('teamPersonNm') || '사용자',
    managerYn: sessionStorage.getItem('managerYn'),
    teamPersonType: sessionStorage.getItem('teamPersonType')
  }), []);

  /**
   * 메뉴 접근 권한 체크 함수
   * @param {string|Array} permission - 권한 값
   * @returns {boolean} - 접근 가능 여부
   */
  const hasPermission = (permission) => {
    const { managerYn, teamPersonType } = userInfo;
    if (managerYn === '1') return true;
    if (permission === 'ALL') return true;
    if (permission === 'MANAGER_ONLY') return false;
    if (Array.isArray(permission)) return permission.includes(teamPersonType);
    return false;
  };

  /**
   * 퀵 메뉴 클릭 핸들러
   * @param {string} id - 탭 ID
   * @param {string} title - 탭 제목
   * @param {string} path - 경로
   * @param {React.Component} component - 컴포넌트
   */
  const handleQuickMenu = (id, title, path, component) => {
    addTab(id, title, path, component);
  };

  /**
   * ============================================
   * 퀵 메뉴 정의
   * ============================================
   * - icon: 아이콘 (emoji 또는 SVG)
   * - title: 메뉴 제목
   * - description: 메뉴 설명
   * - color: 카드 상단 색상
   * - permission: 접근 권한
   * - action: 클릭 시 실행할 함수
   */
  /**
   * ============================================
   * 퀵 메뉴 정의 (전체 14개)
   * ============================================
   */
  const quickMenus = [
    // ========== 판촉파일 관리 (teamPersonType: 1) ==========
    {
      icon: '📁',
      title: '밀크방 파일 관리',
      description: '밀크방 주문 파일 업로드 및 관리',
      color: '#4A90D9',
      permission: ['1'],
      action: () => handleQuickMenu('milk-file-mng', '밀크방 파일 관리', '/MilkFileMng', MilkFileMng)
    },
    {
      icon: '📋',
      title: '밀크방 미전송 대리점',
      description: '파일 미전송 대리점 현황 조회',
      color: '#5DADE2',
      permission: ['1'],
      action: () => handleQuickMenu('milk-file-not-submit', '밀크방 미전송 대리점', '/MilkFileNotSubmit', MilkFileNotSubmit)
    },

    // ========== 판촉실적 정산 (teamPersonType: 1) ==========
    {
      icon: '📊',
      title: '판촉실적 정산',
      description: '판촉 실적 조회 및 정산 처리',
      color: '#5B9BD5',
      permission: ['1'],
      action: () => handleQuickMenu('promotion-settle', '판촉실적 정산', '/PromotionSettle', PromotionSettle)
    },
    {
      icon: '🔒',
      title: '판촉실적 마감',
      description: '판촉 실적 마감 처리',
      color: '#3498DB',
      permission: ['1'],
      action: () => handleQuickMenu('promo-close', '판촉실적 마감', '/PromotionClose', PromotionClose)
    },

    // ========== 판촉실적 통계 (teamPersonType: 1) ==========
    {
      icon: '📈',
      title: '판촉분석 (피벗)',
      description: '판촉 실적 다차원 분석',
      color: '#70AD47',
      permission: ['1'],
      action: () => handleQuickMenu('promotion-settle-pivot', '판촉분석 (피벗)', '/PromotionSettlePivot', PromotionSettlePivot)
    },

    // ========== 판촉팀별 실적 (teamPersonType: 3) ==========
    {
      icon: '👥',
      title: '판촉팀별 실적',
      description: '판촉팀별 실적 현황 조회',
      color: '#ED7D31',
      permission: ['1', '3'],
      action: () => handleQuickMenu('team-performance', '판촉팀별 실적', '/PromoTeamPerf', PromoTeamPerf)
    },
    {
      icon: '📅',
      title: '판촉사원별 주간 실적',
      description: '판촉사원 주간 실적 조회',
      color: '#F39C12',
      permission: ['1', '3'],
      action: () => handleQuickMenu('weekly-performance', '판촉사원별 주간 실적', '/PromoPersonPerf', PromoPersonPerf)
    },
    {
      icon: '🧮',
      title: '판촉사원 제품별 홉수 (피벗)',
      description: '판촉사원 제품별 홉수 분석',
      color: '#E67E22',
      permission: ['1', '3'],
      action: () => handleQuickMenu('promotion-team-settle-pivot', '판촉사원 제품별 홉수 (피벗)', '/PromotionTeamSettlePivot', PromotionTeamSettlePivot)
    },

    // ========== 대리점 (teamPersonType: 2) ==========
    {
      icon: '🏪',
      title: '마감실적관리',
      description: '대리점 마감 실적 관리',
      color: '#9E7CC3',
      permission: ['2'],
      action: () => handleQuickMenu('close-manage', '마감실적관리', '/AgencyMangement', AgencyMangement)
    },

    // ========== 해피콜 관리 (teamPersonType: 4) ==========
    {
      icon: '📞',
      title: '해피콜',
      description: '고객 해피콜 등록 및 관리',
      color: '#E74C3C',
      permission: ['4'],
      action: () => handleQuickMenu('happy-call', '해피콜', '/HappyCall', HappyCall)
    },
    {
      icon: '📝',
      title: '해피콜 결과',
      description: '해피콜 결과 조회 및 분석',
      color: '#C0392B',
      permission: ['4'],
      action: () => handleQuickMenu('happy-call-result', '해피콜 결과', '/HappyCallResult', HappyCallResult)
    },

    // ========== 설정 (관리자 전용) ==========
    {
      icon: '🏢',
      title: '대리점 등록 관리',
      description: '대리점 정보 등록 및 관리',
      color: '#85C1E9',
      permission: 'MANAGER_ONLY',
      action: () => handleQuickMenu('agency-manage', '대리점 등록 관리', '/AgencyMng', AgencyMng)
    },
    {
      icon: '🥛',
      title: '판촉 제품 관리',
      description: '판촉 제품 등록 및 관리',
      color: '#76D7C4',
      permission: 'MANAGER_ONLY',
      action: () => handleQuickMenu('promo-count-setting', '판촉 제품 관리', '/GoodsMng', GoodsMng)
    },
    {
      icon: '👤',
      title: '사원 관리',
      description: '사원 정보 등록 및 권한 관리',
      color: '#82E0AA',
      permission: 'MANAGER_ONLY',
      action: () => handleQuickMenu('teamperson-manage', '사원 관리', '/TeamPersonMng', TeamPersonMng)
    }
  ];

  // ✅ 권한에 따라 표시할 퀵 메뉴 필터링
  const visibleMenus = quickMenus.filter(menu => hasPermission(menu.permission));

  /**
   * 현재 시간에 따른 인사말 반환
   * - 업무 환경에 적합한 격식체
   * - 특별 요일 (월/금) 인사 포함
   * @returns {string} - 인사말
   */
  const getGreeting = () => {
    const now = new Date();
    const hour = now.getHours();
    const day = now.getDay(); // 0: 일, 1: 월, ..., 6: 토
    
    // 월요일 오전: 새 주 시작 인사
    if (day === 1 && hour >= 6 && hour < 12) {
      return '새로운 한 주의 시작입니다';
    }
    
    // 금요일 오후: 주말 앞둔 인사
    if (day === 5 && hour >= 14) {
      return '즐거운 금요일입니다';
    }
    
    // 일반 시간대별 인사
    if (hour >= 5 && hour < 9) return '상쾌한 아침입니다';
    if (hour >= 9 && hour < 12) return '좋은 오전입니다';
    if (hour >= 12 && hour < 14) return '점심 식사는 하셨나요?';
    if (hour >= 14 && hour < 18) return '좋은 오후입니다';
    if (hour >= 18 && hour < 21) return '좋은 저녁입니다';
    return '늦은 시간까지 수고 많으십니다';
  };

  return (
    <div style={styles.container}>
      {/* ============================================
          상단 히어로 섹션
          ============================================ */}
      <div style={styles.heroSection}>
        <Container>
          <Row className="align-items-center">
            <Col md={8}>
              {/* 환영 메시지 */}
              <p style={styles.greeting}>{getGreeting()}</p>
              <h1 style={styles.welcomeTitle}>
                <span style={styles.userName}>{userInfo.name}</span>님, 환영합니다
              </h1>
              <p style={styles.subtitle}>
                연세유업 프로모션 관리 시스템에서 효율적인 업무를 시작하세요.
              </p>
            </Col>
            <Col md={4} className="text-end d-none d-md-block">
              {/* 우유 아이콘 또는 로고 이미지 */}
              <div style={styles.heroIcon}>🥛</div>
            </Col>
          </Row>
        </Container>
      </div>

      {/* ============================================
          퀵 메뉴 섹션
          ============================================ */}
      <Container style={styles.mainContent}>
        <h2 style={styles.sectionTitle}>
          <span style={styles.sectionIcon}>⚡</span> 메뉴 선택
        </h2>
        <p style={styles.sectionSubtitle}>사용하는 메뉴에 빠르게 접근하세요</p>
        
        <Row className="g-4">
          {visibleMenus.map((menu, index) => (
            <Col key={index} xs={12} sm={6} lg={4} xl={3}>
              <Card 
                style={styles.menuCard}
                className="h-100 quick-menu-card"
                onClick={menu.action}
              >
                {/* 카드 상단 색상 바 */}
                <div style={{ ...styles.cardColorBar, backgroundColor: menu.color }} />
                
                <Card.Body style={styles.cardBody}>
                  {/* 아이콘 */}
                  <div style={styles.menuIcon}>{menu.icon}</div>
                  
                  {/* 제목 */}
                  <h5 style={styles.menuTitle}>{menu.title}</h5>
                  
                  {/* 설명 */}
                  <p style={styles.menuDescription}>{menu.description}</p>
                </Card.Body>
                
                {/* 화살표 아이콘 */}
                <div style={styles.cardArrow}>→</div>
              </Card>
            </Col>
          ))}
        </Row>
      </Container>

      {/* ============================================
          푸터
          ============================================ */}
      <footer style={styles.footer}>
        <Container>
          <p style={styles.footerText}>
            © 2025 연세유업 경영정보팀. All Rights Reserved.
          </p>
        </Container>
      </footer>

      {/* ============================================
          스타일 (hover 효과 등)
          ============================================ */}
      <style>{`
        .quick-menu-card {
          cursor: pointer;
          transition: all 0.3s ease;
          border: 1px solid #e9ecef;
        }
        
        .quick-menu-card:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
          border-color: #4A90D9;
        }
        
        .quick-menu-card:hover .card-arrow {
          opacity: 1;
          transform: translateX(5px);
        }
      `}</style>
    </div>
  );
}

/**
 * ============================================
 * 스타일 정의
 * ============================================
 * 연세유업 브랜드 컬러 기반
 * - Primary: #0066B3 (연세 블루)
 * - Secondary: #4A90D9 (밝은 블루)
 * - Accent: #E8F4FC (연한 블루 배경)
 */
const styles = {
  // 전체 컨테이너
  container: {
    minHeight: '100%',
    backgroundColor: '#f8f9fa',
    display: 'flex',
    flexDirection: 'column'
  },

  // 히어로 섹션 (상단 배너)
  heroSection: {
    background: 'linear-gradient(135deg, #0066B3 0%, #4A90D9 50%, #70B8FF 100%)',
    padding: '15px 0',
    color: 'white',
    borderRadius: '0 0 30px 30px',
    boxShadow: '0 4px 20px rgba(0, 102, 179, 0.3)'
  },

  // 인사말
  greeting: {
    fontSize: '16px',
    opacity: 0.9,
    marginBottom: '8px',
    fontWeight: '400'
  },

  // 환영 타이틀
  welcomeTitle: {
    fontSize: '30px',
    fontWeight: '700',
    marginBottom: '12px'
  },

  // 사용자 이름 강조
  userName: {
    color: '#FFE066',
    fontWeight: '800'
  },

  // 부제목
  subtitle: {
    fontSize: '16px',
    opacity: 0.85,
    marginBottom: '0'
  },

  // 히어로 아이콘
  heroIcon: {
    fontSize: '80px',
    opacity: 0.9
  },

  // 메인 콘텐츠
  mainContent: {
    padding: '30px 15px',
    flex: 1
  },

  // 섹션 타이틀
  sectionTitle: {
    fontSize: '22px',
    fontWeight: '700',
    color: '#333',
    marginBottom: '8px',
    display: 'flex',
    alignItems: 'center',
    gap: '8px'
  },

  sectionIcon: {
    fontSize: '24px'
  },

  // 섹션 부제목
  sectionSubtitle: {
    fontSize: '14px',
    color: '#6c757d',
    marginBottom: '24px'
  },

  // 퀵 메뉴 카드
  menuCard: {
    borderRadius: '12px',
    overflow: 'hidden',
    position: 'relative',
    border: 'none',
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.08)'
  },

  // 카드 상단 색상 바
  cardColorBar: {
    height: '4px',
    width: '100%'
  },

  // 카드 본문
  cardBody: {
    padding: '20px',
    textAlign: 'center'
  },

  // 메뉴 아이콘
  menuIcon: {
    fontSize: '40px',
    marginBottom: '12px'
  },

  // 메뉴 제목
  menuTitle: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#333',
    marginBottom: '8px'
  },

  // 메뉴 설명
  menuDescription: {
    fontSize: '13px',
    color: '#6c757d',
    marginBottom: '0',
    lineHeight: '1.5'
  },

  // 카드 화살표
  cardArrow: {
    position: 'absolute',
    bottom: '15px',
    right: '15px',
    fontSize: '18px',
    color: '#4A90D9',
    opacity: 0,
    transition: 'all 0.3s ease'
  },

  // 정보 카드
  infoCard: {
    borderRadius: '12px',
    border: 'none',
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.08)',
    overflow: 'hidden'
  },

  // 정보 카드 헤더
  infoCardHeader: {
    backgroundColor: '#f8f9fa',
    borderBottom: '1px solid #e9ecef',
    padding: '15px 20px',
    fontWeight: '600',
    fontSize: '15px',
    color: '#333',
    display: 'flex',
    alignItems: 'center',
    gap: '8px'
  },

  infoIcon: {
    fontSize: '18px'
  },

  // 공지사항 리스트
  noticeList: {
    listStyle: 'none',
    padding: 0,
    margin: 0
  },

  // 공지사항 아이템
  noticeItem: {
    padding: '12px 0',
    borderBottom: '1px solid #f0f0f0',
    display: 'flex',
    flexDirection: 'column',
    gap: '4px'
  },

  // 공지 날짜
  noticeDate: {
    fontSize: '12px',
    color: '#4A90D9',
    fontWeight: '600'
  },

  // 공지 텍스트
  noticeText: {
    fontSize: '14px',
    color: '#333'
  },

  // 시스템 정보
  systemInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px'
  },

  // 시스템 정보 아이템
  systemInfoItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '8px 0',
    borderBottom: '1px solid #f0f0f0'
  },

  // 시스템 정보 라벨
  systemInfoLabel: {
    fontSize: '14px',
    color: '#6c757d',
    fontWeight: '500'
  },

  // 시스템 정보 값
  systemInfoValue: {
    fontSize: '14px',
    color: '#333',
    fontWeight: '600'
  },

  // 푸터
  footer: {
    backgroundColor: '#f1f3f4',
    padding: '10px 0',
    marginTop: 'auto'
  },

  // 푸터 텍스트
  footerText: {
    fontSize: '15px',
    color: '#6c757d',
    textAlign: 'center',
    marginBottom: 0
  }
};

export default Home;