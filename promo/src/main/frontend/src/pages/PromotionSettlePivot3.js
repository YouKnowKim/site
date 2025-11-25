import React, { useEffect, useState, useMemo } from 'react';
import PivotTableUI from 'react-pivottable/PivotTableUI';
import 'react-pivottable/pivottable.css';
import TableRenderers from 'react-pivottable/TableRenderers';
import Plot from 'react-plotly.js';
import createPlotlyRenderers from 'react-pivottable/PlotlyRenderers';
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

// Plotly 렌더러 생성
const PlotlyRenderers = createPlotlyRenderers(Plot);

/**
 * 촉진정산 피벗 분석 컴포넌트
 * - react-pivottable 사용
 * - 데이터 분석 및 내보내기 기능
 * 
 * @component
 */
const PromotionSettlePivot3 = () => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [rawData, setRawData] = useState([]);
  
  /**
   * ✅ 영문 필드명 -> 한글 필드명 매핑
   */
  const fieldMapping = {
    // 기본 필드
    'agencyNm': '대리점명',
    'agencyCdMis': '대리점코드',
    'promoPersonNm': '판촉사원',
    'goodsOptionNm': '제품명',
    'weekQty': '주간홉수',
    'contractPeriod': '계약기간',
    'teamPersonNm': '담당자명',
    'misCd': '제품코드',
    'deptNme': '부서명',
    'promoTeamNm': '판촉팀',
    'actualHob': '실적홉수'
  };

  /**
   * ✅ 영문 데이터를 한글 키로 변환하는 함수
   * @param {Array} data - 원본 데이터
   * @returns {Array} - 한글 키로 변환된 데이터
   */
  const convertToKoreanKeys = (data) => {
    return data.map(item => {
      const convertedItem = {};
      
      Object.keys(item).forEach(key => {
        // 매핑된 한글명이 있으면 한글로, 없으면 원래 키 사용
        const koreanKey = fieldMapping[key] || key;
        convertedItem[koreanKey] = item[key];
      });
      
      return convertedItem;
    });
  };

  /**
   * ✅ 피벗 테이블 초기 상태
   */
  const defaultPivotState = useMemo(() => ({
    rows: [], // ✅ 한글로 변경
    cols: [], // ✅ 한글로 변경
    aggregatorName: 'Sum',
    vals: [], // ✅ 한글로 변경
    rendererName: 'Table'
  }), []);

  const [pivotState, setPivotState] = useState(defaultPivotState);

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
   * 컴포넌트 마운트 시 초기 날짜 설정
   */
  useEffect(() => {
    const today = getTodayDate();
    setStartDate(today);
    setEndDate(today);
  }, []);

  /**
   * 데이터 조회
   */
  const handleSearch = async () => {
    // 날짜 유효성 검사
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

    try {
      // API 호출
      const response = await axios.get('/api/promo/getMilkbangDetailListPivot', {
        params: {
          startDate: startDate,
          endDate: endDate
        }
      });

      const data = response.data;
      console.log("✅ 데이터 로드 완료 (원본):", data);

      // 데이터가 없는 경우
      if (!data || data.length === 0) {
        Swal.fire({
          icon: 'info',
          title: '조회 결과 없음',
          text: '해당 기간에 데이터가 없습니다.'
        });
        setRawData([]);
        setPivotState(defaultPivotState);
        setIsLoading(false);
        return;
      }

      // ✅ 한글 키로 변환
      const koreanData = convertToKoreanKeys(data);
      console.log("✅ 데이터 변환 완료 (한글):", koreanData);

      // 변환된 데이터 저장
      setRawData(koreanData);
      setPivotState(defaultPivotState);
      setIsLoading(false);

      Swal.fire({
        icon: 'success',
        title: '조회 완료',
        text: `${koreanData.length}건의 데이터를 불러왔습니다.`,
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

  /**
   * Excel 내보내기
   */
  const handleExportExcel = () => {
    if (!rawData || rawData.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '데이터 없음',
        text: '내보낼 데이터가 없습니다. 먼저 조회를 해주세요.'
      });
      return;
    }

    try {
      const pivotTable = document.querySelector('.pvtTable');
      
      if (!pivotTable) {
        Swal.fire({
          icon: 'error',
          title: '내보내기 실패',
          text: '피벗 테이블을 찾을 수 없습니다.'
        });
        return;
      }

      // HTML 테이블을 CSV 형식으로 변환
      let csv = [];
      const rows = pivotTable.querySelectorAll('tr');
      
      for (let i = 0; i < rows.length; i++) {
        const row = [];
        const cols = rows[i].querySelectorAll('td, th');
        
        for (let j = 0; j < cols.length; j++) {
          let data = cols[j].innerText.replace(/(\r\n|\n|\r)/gm, '').replace(/(\s\s)/gm, ' ');
          data = data.replace(/"/g, '""');
          row.push('"' + data + '"');
        }
        
        csv.push(row.join(','));
      }

      // CSV 다운로드
      const csvString = '\uFEFF' + csv.join('\n');
      const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const today = new Date().toISOString().split('T')[0];
      
      link.href = URL.createObjectURL(blob);
      link.download = `촉진정산_피벗분석_${today}.csv`;
      link.click();

      Swal.fire({
        icon: 'success',
        title: 'Excel 내보내기',
        text: 'CSV 파일이 다운로드되었습니다.',
        timer: 1500,
        showConfirmButton: false
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

  return (
    <Container fluid className="mt-1">
      {/* 제목 */}
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

            {/* 조회 버튼 */}
            <Col md={1} style={{ minWidth: '100px', maxWidth: '100px' }}>
              <Button
                variant="primary"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleSearch}
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
        <Card.Body className="p-2" style={{ height: '100%', overflow: 'auto' }}>
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

          {/* Pivot Table UI - 한글 컬럼명으로 표시 */}
          {!isLoading && rawData && rawData.length > 0 && (
            <PivotTableUI
              data={rawData}
              onChange={(s) => setPivotState(s)}
              renderers={Object.assign({}, TableRenderers, PlotlyRenderers)}
              {...pivotState}
              unusedOrientationCutoff={Infinity}
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
              fontSize: '16px'
            }}>
              조회 버튼을 클릭하여 데이터를 불러오세요.
            </div>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default PromotionSettlePivot3;