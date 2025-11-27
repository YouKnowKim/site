import React, { useEffect, useState, useRef } from 'react';
import * as FlexmonsterReact from "react-flexmonster";
import 'flexmonster/flexmonster.min.css';
import { CiViewList } from "react-icons/ci";
import Swal from 'sweetalert2';
import { RiFileExcel2Line } from "react-icons/ri";
import { FaSearch } from "react-icons/fa";
import axios from 'axios';
import "../styles/PromotionSettlePivot.css";
import {
  Container,
  Row,
  Col,
  Form,
  Button,
  Card
} from 'react-bootstrap';

/**
 * 촉진정산 피벗 분석 컴포넌트 (Flexmonster 사용)
 * - Flexmonster 피벗 테이블 라이브러리 사용
 * - 데이터 분석, 차트 시각화 및 다양한 형식 내보내기 기능
 * 
 * @component
 * @description
 * Flexmonster는 react-pivottable보다 향상된 기능을 제공합니다:
 * - 더 빠른 대용량 데이터 처리
 * - 내장된 Excel/PDF/CSV/HTML 내보내기
 * - 드릴다운 및 필터링 기능
 * - 다양한 집계 함수 (합계, 평균, 최소, 최대, 개수 등)
 * - 조건부 서식 및 커스터마이징
 */
const PromotionTeamSettlePivot = () => {
  // ============================================
  // State 관리
  // ============================================
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [rawData, setRawData] = useState([]);
  const [promoTeamList, setPromoTeamList] = useState([]); // 판촉팀 목록 state 추가
  const [isManager, setIsManager] = useState(false);  // 매니저 여부 state 추가
  const [selectedPromoTeamCd, setSelectedPromoTeamCd] = useState('');

  // 컴포넌트 마운트 시 대리점 목록 조회
  useEffect(() => {

    fetchPromoTeamList();
  }, []);

  // 판촉팀 목록 조회 함수
  const fetchPromoTeamList = async () => {
    try {
      const response = await axios.get('/api/promo/getValidPromoTeam');  // API 엔드포인트 수정 필요
      
      // API 응답 구조에 따라 수정
      // 예: response.data 또는 response.data.data
      setPromoTeamList(response.data);

      // 매니저가 아니면 판촉팀 변경 불가
      // 매니저 여부 확인
        const managerYn = sessionStorage.getItem("managerYn");
        const isManagerUser = managerYn === "1";
        setIsManager(isManagerUser);

    // sessionStorage에서 promoTeamCd 가져오기
    const promoTeamCd = sessionStorage.getItem("promoTeamCd");
    
    // promoTeamCd 일치하는 promoTeamCd 찾기
    if (promoTeamCd && response.data && response.data.length > 0) {
      const matchedPromoTeam = response.data.find(
        promoTeam => promoTeam.promoTeamCd === promoTeamCd
      );
      
      // 일치하는 항목이 있으면 해당 값으로 설정
      if (matchedPromoTeam) {
        setSelectedPromoTeamCd(matchedPromoTeam.promoTeamCd);
      } else {
        setSelectedPromoTeamCd(response.data[0].promoTeamCd);
      }
    }
      
    } catch (error) {
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '판촉팀 목록 조회 실패',
        confirmButtonText: '확인'
      });
      console.error('판촉팀 목록 조회 실패:', error);
      // 에러 시 빈 배열 설정
      setPromoTeamList([]);
    }
  };
  
  /**
   * Flexmonster 인스턴스 참조
   * - API 메서드 호출 및 제어를 위한 ref
   */
  const pivotRef = useRef(null);

  // ============================================
  // 필드명 매핑 정의
  // ============================================
  
  /**
   * ✅ 영문 필드명 -> 한글 필드명 매핑
   * Flexmonster의 mapping 기능을 위한 구조
   * 
   * @description
   * - uniqueName: 데이터의 실제 필드명 (영문)
   * - caption: 화면에 표시될 필드명 (한글)
   * - type: 데이터 타입 (string, number, date 등)
   */
  const fieldMapping = [
    { uniqueName: 'agencyNm', caption: '대리점명', type: 'string' },
    { uniqueName: 'promoPersonNm', caption: '판촉사원', type: 'string' },
    { uniqueName: 'goodsOptionNm', caption: '제품명', type: 'string' },
    { uniqueName: 'actualHob', caption: '실적홉수', type: 'number' },
    { uniqueName: 'promoYyMm', caption: '판촉년월', type: 'string' },
    { uniqueName: 'weekCnt', caption: '주차', type: 'number' }
  ];

  // ============================================
  // Flexmonster 리포트 초기 설정
  // ============================================
  
  /**
   * ✅ Flexmonster 초기 리포트 구조
   * 
   * @description
   * - dataSource: 데이터 소스 및 필드 매핑 정보
   * - slice: 피벗 테이블 구조 정의 (행, 열, 측정값)
   * - options: 그리드 및 차트 옵션
   * - formats: 숫자 및 날짜 형식 정의
   */
  const getInitialReport = (data) => ({
    dataSource: {
      data: data,
      mapping: fieldMapping.reduce((acc, field) => {
        acc[field.uniqueName] = {
          caption: field.caption,
          type: field.type
        };
        return acc;
      }, {})
    },
    localization: "/locales/ko.json?v=1.01",
    slice: {
      rows: [
        {
          uniqueName: 'promoPersonNm'
        },
        {
          uniqueName: 'goodsOptionNm'
        },
      ],
      columns: [
        { uniqueName: '[Measures]' },
        { uniqueName: 'promoYyMm' },
        { uniqueName: 'weekCnt' }
      ],
      measures: [
        {
          uniqueName: 'actualHob',
          aggregation: 'sum',
          format: 'currency'
        }
      ],
      expandAll: true,  // ✅ 모든 데이터 펼치기
      drillAll: true    // ✅ 모든 레벨 드릴다운
    },
    options: {
      grid: {
        type: 'classic',  // 'compact', 'classic', 'flat'
        showGrandTotals: 'on',
        showTotals: 'off'
      },
      configuratorActive: true,  // 필드 목록 표시 여부
      configuratorButton: true,  // 필드 목록 버튼 표시
      showAggregations: true,
      showCalculatedValuesButton: true,
      drillThrough: true  // 드릴스루 활성화
    },
    formats: [
      {
        name: 'currency',
        thousandsSeparator: ',',
        decimalSeparator: '.',
        decimalPlaces: 1,
        currencySymbol: '',
        nullValue: '0',
        infinityValue: 'Infinity',
        divideByZeroValue: 'Infinity'
      }
    ]
  });

  // ============================================
  // 날짜 관련 함수
  // ============================================
  
  /**
   * 오늘 날짜 구하기 (로컬 시간대)
   * 
   * @returns {string} YYYY-MM-DD 형식의 날짜 문자열
   */
  const getTodayDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const getBeforeDate = () => {
    const today = new Date();
    today.setMonth(today.getMonth() - 2);
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  // ============================================
  // 초기화 Effect
  // ============================================
  
  /**
   * 컴포넌트 마운트 시 초기 날짜 설정
   * - 시작일과 종료일을 오늘 날짜로 설정
   */
  useEffect(() => {
    const today = getTodayDate();
    const beforeday = getBeforeDate();
    setStartDate(beforeday);
    setEndDate(today);
  }, []);

  // ============================================
  // 데이터 조회 함수
  // ============================================
  
  /**
   * 데이터 조회 처리
   * 
   * @description
   * 1. 날짜 유효성 검사
   * 2. API 호출로 데이터 조회
   * 3. Flexmonster에 데이터 로드
   * 
   * @async
   */
  const handleSearch = async () => {
    // ========================================
    // 1. 날짜 유효성 검사
    // ========================================
    if (!startDate || !endDate) {
      Swal.fire({
        icon: 'warning',
        title: '날짜를 입력해주세요',
        text: '시작일과 종료일을 모두 입력해주세요.'
      });
      return;
    }

    if (startDate > endDate) {
      Swal.fire({
        icon: 'warning',
        title: '날짜 오류',
        text: '시작일이 종료일보다 늦을 수 없습니다.'
      });
      return;
    }

    // ========================================
    // 2. 로딩 시작 및 로그
    // ========================================
    setIsLoading(true);
    console.log("📊 데이터 조회 시작");
    console.log(`📅 조회 기간: ${startDate} ~ ${endDate}`);

    try {
      // ========================================
      // 3. API 호출
      // ========================================
      const response = await axios.get('/api/promo/getMilkbangDetailListTeamPivot', {
        params: {
          startDate: startDate,
          endDate: endDate,
          promoTeamCd: selectedPromoTeamCd
        }
      });

      const data = response.data;
      console.log("✅ 데이터 로드 완료:", data);
      console.log(`📊 조회된 데이터 건수: ${data.length}건`);

      // ========================================
      // 4. 데이터 없음 처리
      // ========================================
      if (!data || data.length === 0) {
        Swal.fire({
          icon: 'info',
          title: '조회 결과 없음',
          text: '해당 기간에 데이터가 없습니다.'
        });
        setRawData([]);
        setIsLoading(false);
        return;
      }

      // ========================================
      // 5. 데이터 저장 및 Flexmonster 업데이트
      // ========================================
      setRawData(data);
      
      // Flexmonster에 새 리포트 설정
      if (pivotRef.current) {
        const newReport = getInitialReport(data);
        pivotRef.current.flexmonster.setReport(newReport);
        console.log("✅ Flexmonster 리포트 업데이트 완료");
      }

      setIsLoading(false);

      // ========================================
      // 6. 성공 메시지
      // ========================================
      Swal.fire({
        icon: 'success',
        title: '조회 완료',
        text: `${data.length}건의 데이터를 불러왔습니다.`,
        timer: 1500,
        showConfirmButton: false
      });

    } catch (error) {
      // ========================================
      // 7. 오류 처리
      // ========================================
      console.error('❌ 데이터 로드 실패:', error);
      setIsLoading(false);

      Swal.fire({
        icon: 'error',
        title: '데이터 로드 실패',
        text: error.response?.data?.message || '데이터를 불러오는 중 오류가 발생했습니다.'
      });
    }
  };

  // ============================================
  // Excel 내보내기 함수
  // ============================================
  
  /**
   * Excel 내보내기 처리
   * 
   * @description
   * Flexmonster의 내장 exportTo 메서드 사용
   * - 현재 피벗 테이블의 상태를 그대로 Excel로 내보냄
   * - 서식, 집계, 필터링이 모두 반영됨
   * 
   * @see https://www.flexmonster.com/api/export/
   */
  const handleExportExcel = () => {
    // ========================================
    // 1. 데이터 존재 여부 확인
    // ========================================
    if (!rawData || rawData.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '데이터 없음',
        text: '내보낼 데이터가 없습니다. 먼저 조회를 해주세요.'
      });
      return;
    }

    // ========================================
    // 2. Flexmonster 인스턴스 확인
    // ========================================
    if (!pivotRef.current || !pivotRef.current.flexmonster) {
      Swal.fire({
        icon: 'error',
        title: '내보내기 실패',
        text: '피벗 테이블을 찾을 수 없습니다.'
      });
      return;
    }

    try {
      // ========================================
      // 3. Excel 내보내기 실행
      // ========================================
      const today = new Date().toISOString().split('T')[0];
      const fileName = `판촉정산피벗_${today}`;

      console.log(`📥 Excel 내보내기 시작: ${fileName}.xlsx`);

      // Flexmonster의 exportTo 메서드 호출
      // - 'excel' 외에도 'pdf', 'csv', 'html', 'image' 지원
      pivotRef.current.flexmonster.exportTo('excel', {
        filename: fileName,
        // Excel 내보내기 옵션
        excelSheetName: '판촉정산피벗',
        header: '판촉정산피벗',
        footer: `작성일: ${today}`,
        pageOrientation: 'landscape',  // 'portrait' or 'landscape'
        destinationType: 'file'  // 'file' or 'server'
      }, () => {
        // ========================================
        // 4. 내보내기 완료 콜백
        // ========================================
        console.log("✅ Excel 내보내기 완료");
        
        Swal.fire({
          icon: 'success',
          title: 'Excel 내보내기',
          text: 'Excel 파일이 다운로드되었습니다.',
          timer: 1500,
          showConfirmButton: false
        });
      }, (error) => {
        // ========================================
        // 5. 내보내기 실패 처리
        // ========================================
        console.error('❌ Excel 내보내기 실패:', error);
        
        Swal.fire({
          icon: 'error',
          title: '내보내기 실패',
          text: '파일 내보내기 중 오류가 발생했습니다.'
        });
      });

    } catch (error) {
      console.error('❌ Excel 내보내기 실패:', error);
      
      Swal.fire({
        icon: 'error',
        title: '내보내기 실패',
        text: '파일 내보내기 중 오류가 발생했습니다.'
      });
    }
  };

  // ============================================
  // Flexmonster 이벤트 핸들러
  // ============================================
  
  /**
   * Flexmonster 준비 완료 이벤트
   * 
   * @description
   * 피벗 테이블이 완전히 로드되고 사용 가능한 상태가 되면 호출
   */
  const onReportComplete = () => {
    console.log("✅ Flexmonster 리포트 로드 완료");
    
    // 초기 설정이나 커스터마이징이 필요한 경우 여기서 처리
    if (pivotRef.current && pivotRef.current.flexmonster) {
      // 예: 특정 필드에 색상 적용, 조건부 서식 등
      console.log("📊 Flexmonster 인스턴스 준비 완료");
    }
  };

  /**
   * 데이터 로드 완료 이벤트
   */
  const onDataLoaded = () => {
    console.log("✅ 데이터 로드 완료");
  };

  /**
   * 데이터 변경 이벤트
   */
  const onUpdate = (params) => {
    console.log("🔄 피벗 테이블 업데이트:", params);
  };

  // ============================================
  // 렌더링
  // ============================================
  
  return (
    <Container fluid className="mt-1">
      {/* ========================================
          제목 영역
      ======================================== */}
      <Row className="mb-1">
        <Col>
          <h5>
            <i className="bi bi-circle-fill text-warning me-1"></i>
            <CiViewList size={22} />
            판촉사원 제품별 홉수 (피벗)
          </h5>
        </Col>
      </Row>

      {/* ========================================
          상단 Card - 검색 및 버튼 영역
      ======================================== */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-center">
            {/* 날짜 검색 */}
            <Col md={4} style={{ minWidth: '400px', maxWidth: '400px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '80px', maxWidth: '80px' }}>
                    날짜검색 : 
                  </Form.Label>
                  <Form.Control
                    type="date"
                    size="sm"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    style={{ width: '130px' }}
                  />
                  <span className="small">~</span>
                  <Form.Control
                    type="date"
                    size="sm"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    style={{ width: '130px' }}
                  />
                </div>
              </Form.Group>
            </Col>

            {/* 판촉팀 선택 */}
            <Col md={3} style={{ minWidth: '200px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    판촉팀 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedPromoTeamCd}
                    onChange={(e) => {
                      setSelectedPromoTeamCd(e.target.value);
                    }}
                    disabled={!isManager}  // 매니저가 아니면 비활성화
                    style={{ width: '150px' }}  // 고정 크기
                  >
                    {promoTeamList.map((promoTeam) => (
                      <option key={promoTeam.promoTeamCd} value={promoTeam.promoTeamCd}>
                        {promoTeam.promoTeamNm}
                      </option>
                    ))}
                  </Form.Select>
                </div>
              </Form.Group>
            </Col>

            {/* 조회 버튼 */}
            <Col md={1} style={{ minWidth: '100px', maxWidth: '100px' }}>
              <Button
                variant="primary"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleSearch}
                disabled={isLoading}
              >
                <FaSearch /> 조회
              </Button>
            </Col>
            
            {/* 엑셀 다운로드 버튼 */}
            <Col md={1} style={{ minWidth: '150px', maxWidth: '150px' }}>
              <Button
                variant="success"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleExportExcel}
                disabled={!rawData || rawData.length === 0}
              >
                <RiFileExcel2Line /> 엑셀 다운로드
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>
      
      {/* ========================================
          하단 Card - 피벗 테이블 영역
      ======================================== */}
      <Card style={{ height: 'calc(100vh - 270px)' }}>
        <Card.Body className="p-2" style={{ height: '100%', overflow: 'auto', position: 'relative' }}>
          {/* ========================================
              로딩 표시
          ======================================== */}
          {isLoading && (
            <div style={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              textAlign: 'center',
              zIndex: 1000,
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              padding: '30px',
              borderRadius: '12px',
              boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)'
            }}>
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">로딩중...</span>
              </div>
              <p style={{ marginTop: '15px', fontSize: '14px', color: '#666' }}>
                데이터를 불러오는 중...
              </p>
            </div>
          )}

          {/* ========================================
              Flexmonster Pivot 컴포넌트
          ======================================== */}
          {!isLoading && rawData && rawData.length > 0 && (
            <FlexmonsterReact.Pivot
              ref={pivotRef}
              toolbar={true}  // 툴바 표시 (저장, 열기, 내보내기 등)
              width="100%"
              height="100%"
              report={getInitialReport(rawData)}
              licenseKey=""  // ⚠️ 라이센스 키 필요 (개발용은 빈 문자열)
              // 이벤트 핸들러
              reportcomplete={onReportComplete}
              datachanged={onDataLoaded}
              update={onUpdate}
            />
          )}

          {/* ========================================
              데이터 없음 메시지
          ======================================== */}
          {!isLoading && (!rawData || rawData.length === 0) && (
            <div style={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              height: '100%',
              color: '#999',
              fontSize: '16px',
              flexDirection: 'column',
              gap: '10px'
            }}>
              <div style={{ fontSize: '48px' }}>📊</div>
              <div>조회 버튼을 클릭하여 데이터를 불러오세요.</div>
            </div>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default PromotionTeamSettlePivot;