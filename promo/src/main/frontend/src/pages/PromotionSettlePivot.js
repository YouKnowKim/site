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
 */
const PromotionSettlePivot = () => {
  // ============================================
  // State 관리
  // ============================================
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [rawData, setRawData] = useState([]);
  
  const pivotRef = useRef(null);

  // ============================================
  // 필드명 매핑 정의
  // ============================================
  const fieldMapping = [
    { uniqueName: 'agencyNm', caption: '대리점명', type: 'string' },
    { uniqueName: 'agencyCdMis', caption: '대리점코드', type: 'string' },
    { uniqueName: 'promoPersonNm', caption: '판촉사원', type: 'string' },
    { uniqueName: 'goodsOptionNm', caption: '제품명', type: 'string' },
    { uniqueName: 'contractPeriod', caption: '계약기간', type: 'string' },
    { uniqueName: 'teamPersonNm', caption: '담당자명', type: 'string' },
    { uniqueName: 'misCd', caption: '제품코드', type: 'string' },
    { uniqueName: 'deptNme', caption: '부서명', type: 'string' },
    { uniqueName: 'promoTeamNm', caption: '판촉팀', type: 'string' },
    { uniqueName: 'actualHob', caption: '실적홉수', type: 'number' },
    { uniqueName: 'orderKindCdNm', caption: '계약구분', type: 'string' },
    { uniqueName: 'promoYyMm', caption: '판촉년월', type: 'string' },
    { uniqueName: 'weekCnt', caption: '주차', type: 'number' },
    { uniqueName: 'weekQty', caption: '주간수량', type: 'number' },
    { uniqueName: 'weekRemark', caption: '배송요일', type: 'string' },
    { uniqueName: 'unitPrice', caption: '단가', type: 'number' },
    { uniqueName: 'promoGiftNm', caption: '계약선물', type: 'string' }
  ];

  // ============================================
  // Flexmonster 리포트 초기 설정
  // ============================================
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
        { uniqueName: 'deptNme' },
        { uniqueName: 'teamPersonNm' },
        { uniqueName: 'agencyNm' },
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
      expandAll: true,
      drillAll: true
    },
    options: {
      grid: {
        type: 'classic',
        showGrandTotals: 'on',
        showTotals: 'off'
      },
      configuratorActive: true,
      configuratorButton: true,
      showAggregations: true,
      showCalculatedValuesButton: true,
      drillThrough: true
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
   */
  const getTodayDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  /**
   * 2개월 전 날짜 구하기
   */
  const getBeforeDate = () => {
    const today = new Date();
    today.setMonth(today.getMonth() - 2);
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  /**
   * ✅ ISO 8601 기준 주차 계산 (수요일 기준)
   * @param {number|string} year - 년도
   * @param {number|string} month - 월
   * @returns {Array} 주차 정보 배열
   */
  const getWeeksInMonth = (year, month) => {
    const weeks = [];
    const monthNum = parseInt(month);
    const yearNum = parseInt(year);
    
    // 해당 월의 1일
    const firstDate = new Date(yearNum, monthNum - 1, 1);
    
    // 해당 월 1일이 속한 주의 월요일 찾기
    const firstDayOfWeek = firstDate.getDay();
    let daysToMonday = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;
    
    const firstMonday = new Date(firstDate);
    firstMonday.setDate(firstDate.getDate() - daysToMonday);
    
    let weekCount = 0;
    let currentMonday = new Date(firstMonday);
    
    // 최대 6주까지 확인
    for (let i = 0; i < 6; i++) {
      const currentSunday = new Date(currentMonday);
      currentSunday.setDate(currentMonday.getDate() + 6);
      
      // 해당 주의 수요일 계산
      const wednesday = new Date(currentMonday);
      wednesday.setDate(currentMonday.getDate() + 2);
      
      // 수요일이 해당 월에 속하는지 확인
      if (wednesday.getMonth() + 1 === monthNum && wednesday.getFullYear() === yearNum) {
        weekCount++;
        
        const startYear = currentMonday.getFullYear();
        const startMonth = currentMonday.getMonth() + 1;
        const startDay = currentMonday.getDate();
        const endYear = currentSunday.getFullYear();
        const endMonth = currentSunday.getMonth() + 1;
        const endDay = currentSunday.getDate();
        
        const startDateStr = `${String(startMonth).padStart(2, '0')}-${String(startDay).padStart(2, '0')}`;
        const endDateStr = `${String(endMonth).padStart(2, '0')}-${String(endDay).padStart(2, '0')}`;

        // value용 전체 날짜 형식 (YYYY-MM-DD)
        const startDateFull = `${startYear}-${String(startMonth).padStart(2, '0')}-${String(startDay).padStart(2, '0')}`;
        const endDateFull = `${endYear}-${String(endMonth).padStart(2, '0')}-${String(endDay).padStart(2, '0')}`;
        const dateRangeValue = `${startDateFull}|${endDateFull}`;
        
        weeks.push({
          weekNum: weekCount,
          startDate: startDateStr,
          endDate: endDateStr,
          startDateFull: startDateFull,  // ✅ 전체 날짜 형식 추가
          endDateFull: endDateFull,      // ✅ 전체 날짜 형식 추가
          dateRange: dateRangeValue,
          label: `${weekCount}주차: ${startDateStr} ~ ${endDateStr}`
        });
      }
      
      // 다음 주 월요일로 이동
      currentMonday.setDate(currentMonday.getDate() + 7);
      
      // 조기 종료 조건: 수요일이 다음 월을 넘어가면
      const nextWednesday = new Date(currentMonday);
      nextWednesday.setDate(currentMonday.getDate() + 2);
      if (nextWednesday.getFullYear() > yearNum || 
          (nextWednesday.getFullYear() === yearNum && nextWednesday.getMonth() + 1 > monthNum)) {
        break;
      }
    }
    
    return weeks;
  };

  /**
   * ✅ 전월 날짜 범위 설정
   * 전월 1주차 시작일 ~ 마지막 주차 종료일
   */
  const handlePreviousMonth = () => {
    const today = new Date();
    // 전월 계산
    const prevMonth = today.getMonth();  // 0-based, 현재 달 -1 = 전월
    const year = prevMonth === 0 ? today.getFullYear() - 1 : today.getFullYear();
    const month = prevMonth === 0 ? 12 : prevMonth;
    
    // 전월의 주차 정보 가져오기
    const weeks = getWeeksInMonth(year, String(month).padStart(2, '0'));
    
    if (weeks.length > 0) {
      // 1주차 시작일
      const firstWeekStart = weeks[0].startDateFull;
      // 마지막 주차 종료일
      const lastWeekEnd = weeks[weeks.length - 1].endDateFull;
      
      setStartDate(firstWeekStart);
      setEndDate(lastWeekEnd);
      
      console.log(`📅 전월 설정: ${firstWeekStart} ~ ${lastWeekEnd}`);
    }
  };

  /**
   * ✅ 당월 날짜 범위 설정
   * 당월 1주차 시작일 ~ 마지막 주차 종료일
   */
  const handleCurrentMonth = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth() + 1;  // 1-based
    
    // 당월의 주차 정보 가져오기
    const weeks = getWeeksInMonth(year, String(month).padStart(2, '0'));
    
    if (weeks.length > 0) {
      // 1주차 시작일
      const firstWeekStart = weeks[0].startDateFull;
      // 마지막 주차 종료일
      const lastWeekEnd = weeks[weeks.length - 1].endDateFull;
      
      setStartDate(firstWeekStart);
      setEndDate(lastWeekEnd);
      
      console.log(`📅 당월 설정: ${firstWeekStart} ~ ${lastWeekEnd}`);
    }
  };

  // ============================================
  // 초기화 Effect
  // ============================================
  useEffect(() => {
    const today = getTodayDate();
    const beforeday = getBeforeDate();
    setStartDate(beforeday);
    setEndDate(today);
  }, []);

  // ============================================
  // 데이터 조회 함수
  // ============================================
  const handleSearch = async () => {
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

    setIsLoading(true);
    console.log("📊 데이터 조회 시작");
    console.log(`📅 조회 기간: ${startDate} ~ ${endDate}`);

    try {
      const response = await axios.get('/api/promo/getMilkbangDetailListPivot', {
        params: {
          startDate: startDate,
          endDate: endDate
        }
      });

      const data = response.data;
      console.log("✅ 데이터 로드 완료:", data);
      console.log(`📊 조회된 데이터 건수: ${data.length}건`);

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

      setRawData(data);
      
      if (pivotRef.current) {
        const newReport = getInitialReport(data);
        pivotRef.current.flexmonster.setReport(newReport);
        console.log("✅ Flexmonster 리포트 업데이트 완료");
      }

      setIsLoading(false);

      Swal.fire({
        icon: 'success',
        title: '조회 완료',
        text: `${data.length}건의 데이터를 불러왔습니다.`,
        timer: 1500,
        showConfirmButton: false
      });

    } catch (error) {
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
  const handleExportExcel = () => {
    if (!rawData || rawData.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '데이터 없음',
        text: '내보낼 데이터가 없습니다. 먼저 조회를 해주세요.'
      });
      return;
    }

    if (!pivotRef.current || !pivotRef.current.flexmonster) {
      Swal.fire({
        icon: 'error',
        title: '내보내기 실패',
        text: '피벗 테이블을 찾을 수 없습니다.'
      });
      return;
    }

    try {
      const today = new Date().toISOString().split('T')[0];
      const fileName = `판촉정산피벗_${today}`;

      console.log(`📥 Excel 내보내기 시작: ${fileName}.xlsx`);

      pivotRef.current.flexmonster.exportTo('excel', {
        filename: fileName,
        excelSheetName: '판촉정산피벗',
        header: '판촉정산피벗',
        footer: `작성일: ${today}`,
        pageOrientation: 'landscape',
        destinationType: 'file'
      }, () => {
        console.log("✅ Excel 내보내기 완료");
        
        Swal.fire({
          icon: 'success',
          title: 'Excel 내보내기',
          text: 'Excel 파일이 다운로드되었습니다.',
          timer: 1500,
          showConfirmButton: false
        });
      }, (error) => {
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
  // PDF 내보내기 함수
  // ============================================
  const handleExportPDF = () => {
    if (!rawData || rawData.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '데이터 없음',
        text: '내보낼 데이터가 없습니다.'
      });
      return;
    }

    if (!pivotRef.current || !pivotRef.current.flexmonster) {
      return;
    }

    const today = new Date().toISOString().split('T')[0];
    const fileName = `판촉정산피벗_${today}`;

    pivotRef.current.flexmonster.exportTo('pdf', {
      filename: fileName,
      header: '판촉정산피벗',
      footer: `작성일: ${today}`,
      pageOrientation: 'landscape'
    });
  };

  // ============================================
  // Flexmonster 이벤트 핸들러
  // ============================================
  const onReportComplete = () => {
    console.log("✅ Flexmonster 리포트 로드 완료");
    
    if (pivotRef.current && pivotRef.current.flexmonster) {
      console.log("📊 Flexmonster 인스턴스 준비 완료");
    }
  };

  const onDataLoaded = () => {
    console.log("✅ 데이터 로드 완료");
  };

  const onUpdate = (params) => {
    console.log("🔄 피벗 테이블 업데이트:", params);
  };

  // ============================================
  // 렌더링
  // ============================================
  return (
    <Container fluid className="mt-1">
      {/* 제목 영역 */}
      <Row className="mb-1">
        <Col>
          <h5>
            <i className="bi bi-circle-fill text-warning me-1"></i>
            <CiViewList size={22} />
            판촉실적 정산 (피벗)
          </h5>
        </Col>
      </Row>

      {/* 상단 Card - 검색 및 버튼 영역 */}
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

            {/* ✅ 전월 버튼 */}
            <Col md={1} style={{ minWidth: '80px', maxWidth: '80px' }}>
              <Button
                variant="outline-secondary"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center"
                onClick={handlePreviousMonth}
                disabled={isLoading}
                title="전월 1주차 시작일 ~ 마지막 주차 종료일"
              >
                전월
              </Button>
            </Col>

            {/* ✅ 당월 버튼 */}
            <Col md={1} style={{ minWidth: '80px', maxWidth: '80px' }}>
              <Button
                variant="outline-secondary"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center"
                onClick={handleCurrentMonth}
                disabled={isLoading}
                title="당월 1주차 시작일 ~ 마지막 주차 종료일"
              >
                당월
              </Button>
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
      
      {/* 하단 Card - 피벗 테이블 영역 */}
      <Card style={{ height: 'calc(100vh - 270px)' }}>
        <Card.Body className="p-2" style={{ height: '100%', overflow: 'auto', position: 'relative' }}>
          {/* 로딩 표시 */}
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

          {/* Flexmonster Pivot 컴포넌트 */}
          {!isLoading && rawData && rawData.length > 0 && (
            <FlexmonsterReact.Pivot
              ref={pivotRef}
              toolbar={true}
              width="100%"
              height="100%"
              report={getInitialReport(rawData)}
              licenseKey="Z7HJ-XIHH45-5F4W41-6L414O-2F395B-2H6K5G-1Z665L-24012U-675R0L-4Y3L19-2K"
              reportcomplete={onReportComplete}
              datachanged={onDataLoaded}
              update={onUpdate}
            />
          )}

          {/* 데이터 없음 메시지 */}
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

export default PromotionSettlePivot;