import React, { useState, useRef, useEffect } from 'react';
import {
  Container,
  Row,
  Col,
  Form,
  Button,
  Card
} from 'react-bootstrap';
import { ReactTabulator } from 'react-tabulator';
import 'tabulator-tables/dist/css/tabulator.min.css';
import 'tabulator-tables/dist/css/tabulator_bootstrap4.min.css';
import 'bootstrap/dist/css/bootstrap.min.css'; // Bootstrap CSS import (npm 설치 시)
import { CiViewList} from "react-icons/ci";
import { FaSearch } from "react-icons/fa";
import axios from 'axios';  // axios import 추가
import Swal from 'sweetalert2';
import { RiFileExcel2Line } from "react-icons/ri";
import * as XLSX from 'xlsx';  // 이 줄 추가

// window.XLSX에 할당 (Tabulator가 사용할 수 있도록)
window.XLSX = XLSX;

// 오늘 날짜 구하기 (로컬 시간대)
const getTodayDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 현재 년도 구하기
const getCurrentYear = () => {
  const today = new Date();
  return today.getFullYear().toString();
};

// 년도 목록 생성 (2014 ~ 현재년도)
const generateYearOptions = () => {
  const currentYear = new Date().getFullYear();
  const years = [];
  for (let year = 2014; year <= currentYear; year++) {
    years.push(year);
  }
  return years;
};

// 현재 월 구하기
const getCurrentMonth = () => {
  const today = new Date();
  return String(today.getMonth() + 1).padStart(2, '0');
};

// 월 목록 생성 (1월 ~ 12월)
const generateMonthOptions = () => {
  const months = [];
  for (let month = 1; month <= 12; month++) {
    months.push(String(month).padStart(2, '0'));
  }
  return months;
};

// 현재 주차 구하기 (기본값 1주차)
const getCurrentWeek = () => {
  return '01';
};

// ISO 8601 기준 주차 계산 (수요일 기준)
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
      
      const startDate = `${String(startMonth).padStart(2, '0')}-${String(startDay).padStart(2, '0')}`;
      const endDate = `${String(endMonth).padStart(2, '0')}-${String(endDay).padStart(2, '0')}`;

      // value용 전체 날짜 형식 (YYYY-MM-DD)
      const startDateFull = `${startYear}-${String(startMonth).padStart(2, '0')}-${String(startDay).padStart(2, '0')}`;
      const endDateFull = `${endYear}-${String(endMonth).padStart(2, '0')}-${String(endDay).padStart(2, '0')}`;
      const dateRangeValue = `${startDateFull}|${endDateFull}`;
      
      weeks.push({
        weekNum: weekCount,
        startDate: startDate,
        endDate: endDate,
        dateRange: dateRangeValue,
        label: `${weekCount}주차: ${startDate} ~ ${endDate}`
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

const PromoPersonPerf = () => {
  const [stdYear, setStdYear] = useState(getCurrentYear());
  const [stdMonth, setStdMonth] = useState(getCurrentMonth());
  const [stdWeek, setStdWeek] = useState(getCurrentWeek());  // 주차 저장
  const [selectedPromoTeamCd, setSelectedPromoTeamCd] = useState('');
  const [tableData, setTableData] = useState([]);
  const [promoTeamList, setPromoTeamList] = useState([]); // 판촉팀 목록 state 추가
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [isManager, setIsManager] = useState(false);  // 매니저 여부 state 추가
  const [weekOptions, setWeekOptions] = useState([]);  // 주차 옵션 목록 state 추가
  const [headerStartDate, setHeaderStartDate] = useState('');
  const [headerEndDate, setHeaderEndDate] = useState('');
  const [isLoading, setIsLoading] = useState(false);  // 조회 중 상태
  const tableRef = useRef(null);

  // 컴포넌트 마운트 시 대리점 목록 조회
  useEffect(() => {
    // 초기 주차 목록 설정
    const initialWeeks = getWeeksInMonth(stdYear, stdMonth);
    setWeekOptions(initialWeeks);
    if (initialWeeks.length > 0) {
        setStdWeek(initialWeeks[0].dateRange);
    }

    fetchPromoTeamList();
  }, []);

  // stdYear 또는 stdMonth가 변경될 때마다 주차 목록 업데이트
  useEffect(() => {
    if (stdYear && stdMonth) {
        const weeks = getWeeksInMonth(stdYear, stdMonth);
        setWeekOptions(weeks);
        // 첫 번째 주차로 자동 선택
        if (weeks.length > 0) {
        setStdWeek(weeks[0].dateRange);
        }
    }
  }, [stdYear, stdMonth]);
  
  // 담당자 목록 조회 함수
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

  // 엑셀 다운로드 함수
  const handleExcelDownload = () => { 

    if (!tabulatorInstance) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '테이블이 준비되지 않았습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    if (tableData.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '다운로드할 데이터가 없습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    const today = getTodayDate().replace(/-/g, '');
    const fileName = `판촉사원별 주간 실적_${today}.xlsx`;
    
    tabulatorInstance?.current.download("xlsx", fileName, {
      sheetName: "판촉사원별 주간 실적"
    });
  };

  // 테이블 컬럼 정의
  const columns = [
    {
      title: '판촉사원',
      field: 'promoPersonNm',
      width: 140,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '배치대리점',
      field: 'agencyNm',
      width: 140,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '1주',
      field: 'week1',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '2주',
      field: 'week2',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '3주',
      field: 'week3',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '4주',
      field: 'week4',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '5주',
      field: 'week5',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '합계',
      field: 'sumWeek',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    }
  ];

  // ✅ 공통 formatter 함수들
  const textDownloadFormatter = (value) => {
    return (value === null || value === undefined || value === '') ? '' : value;
  };

  const numberDownloadFormatter = (value) => {
    if (value === null || value === undefined || value === '') return 0;
    const number = parseFloat(value);
    return isNaN(number) ? 0 : number;
  };

  // ✅ accessorDownload가 없는 컬럼에 자동으로 추가
  columns.forEach(col => {
    // 이미 accessorDownload가 있거나, formatter가 rowSelection인 경우 스킵
    if (col.accessorDownload || col.formatter === "rowSelection" || !col.field) {
      return;
    }
    
    // 숫자 필드 목록 (필요에 따라 추가)
    const numberFields = ['week1', 'week2', 'week3', 'week4', 'week5', 'sumWeek'];
    
    // 숫자 필드면 numberDownloadFormatter, 아니면 textDownloadFormatter 적용
    if (numberFields.includes(col.field)) {
      col.accessorDownload = numberDownloadFormatter;
    } else {
      col.accessorDownload = textDownloadFormatter;
    }
  });

  const totalBackgroundFormatter = (cell) => {

    let row = cell.getRow().getData();
    const value = cell.getValue();

    if(row.agencyNm === '합계'){
      cell.getElement().style.color = '#ebf3edff';
      cell.getElement().style.fontWeight = 'bold';
      cell.getElement().style.backgroundColor = '#878a87ff';
      return value;
    } else {
      return value;
    }
  };

  columns.forEach(col => {
    
    col.formatter = totalBackgroundFormatter;
  });

  // 조회 버튼 클릭
  const handleSearch = async () => {

    if (isLoading) return;

    try {

      setIsLoading(true);

      // ✅ 1. 테이블 alert 표시 (조회 중 메시지)
      if (tabulatorInstance && tabulatorInstance.current) {
        tabulatorInstance.current.alert(
          '<div class="text-center py-4">' +
            '<div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;"></div>' +
            '<div class="fw-bold text-primary fs-5">데이터 조회 중...</div>' +
          '</div>'
        );
      }

      const stdDate = String(stdYear) + String(stdMonth) + '01';

      // 조회 API 호출
      const response = await axios.get('/api/extPromo/getPromoPersonPerf', {
        params: { stdDate : stdDate,
                  promoTeamCd: selectedPromoTeamCd }
      });

      // placeholder 원래대로 복원
      tabulatorInstance.current.options.placeholder = '조회된 데이터가 없습니다.';

      // 데이터가 없는 경우 체크
      if (!response.data || response.data.length === 0) {
        Swal.fire({
          icon: 'info',
          title: '알림',
          text: '조회된 데이터가 없습니다.',
          confirmButtonText: '확인'
        });
        setTableData([]);  // 빈 배열로 설정
        return;
      }

      // 합계 계산
      let sumTot = {
        agencyNm : "합계",
        week1 : 0,
        week2 : 0,
        week3 : 0,
        week4 : 0,
        week5 : 0,
        sumWeek : 0
      }

      for(var i=0; i<response.data.length; i++){
        sumTot.week1 += Number(response.data[i].week1);
        sumTot.week2 += Number(response.data[i].week2);
        sumTot.week3 += Number(response.data[i].week3);
        sumTot.week4 += Number(response.data[i].week4);
        sumTot.week5 += Number(response.data[i].week5);
        sumTot.sumWeek += Number(response.data[i].sumWeek);

        // 시작일 종료일 header 표시
        setHeaderStartDate(response.data[i].startDate);
        setHeaderEndDate(response.data[i].endDate);
      }

      sumTot.week1 = sumTot.week1.toFixed(1);
      sumTot.week2 = sumTot.week2.toFixed(1);
      sumTot.week3 = sumTot.week3.toFixed(1);
      sumTot.week4 = sumTot.week4.toFixed(1);
      sumTot.week5 = sumTot.week5.toFixed(1);
      sumTot.sumWeek = sumTot.sumWeek.toFixed(1);

      response.data.push(sumTot);

      setTableData(response.data);

    } catch (error) {
      console.error('조회 실패:', error);

      // placeholder 원래대로 복원
      tabulatorInstance.current.options.placeholder = '조회된 데이터가 없습니다.';
      
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '데이터 조회에 실패했습니다.',
        confirmButtonText: '확인'
      });
    } finally {
      // ✅ 3. alert 제거
      if (tabulatorInstance && tabulatorInstance.current) {
        tabulatorInstance.current.clearAlert();
      }

      setIsLoading(false);
    }
  };

  // Tabulator 옵션
  const options = {
    layout: 'fitColumns',
    pagination: false,
    placeholder: '조회된 데이터가 없습니다.',
    height: "calc(100vh - 380px)"
  };

  return (
    <Container fluid className="mt-1">

      {/* 제목 */}
      <Row className="mb-1">
        <Col>
          <h5>
            <i className="bi bi-circle-fill text-warning me-1"></i>
            <CiViewList size={22} />
            판촉사원별 주간 실적
          </h5>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-end">
            {/* 날짜 범위 */}
            <Col md={5} style={{ minWidth: '350px', maxWidth: '350px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    주차 : 
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={stdYear}
                    onChange={(e) => {
                        setStdYear(e.target.value);
                    }}
                    style={{ width: '140px' }}
                    >
                    {generateYearOptions().map((year) => (
                        <option key={year} value={year}>
                        {year}년
                        </option>
                    ))}
                  </Form.Select>

                  <Form.Select
                    size="sm"
                    value={stdMonth}
                    onChange={(e) => {
                        setStdMonth(e.target.value);
                    }}
                    style={{ width: '90px' }}
                    >
                    {generateMonthOptions().map((month) => (
                        <option key={month} value={month}>
                        {parseInt(month)}월
                        </option>
                    ))}
                  </Form.Select>

                  <Form.Select
                    size="sm"
                    value={stdWeek}
                    onChange={(e) => {
                        setStdWeek(e.target.value);
                    }}
                    style={{ width: '200px', display: "none" }}
                    >
                    {weekOptions.map((week) => (
                        <option key={week.weekNum} value={week.dateRange}>
                        {week.label}
                        </option>
                    ))}
                  </Form.Select>
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
            {/* 엑셀 버튼 */}
            <Col md={2} style={{ minWidth: '150px', maxWidth: '150px' }}>
              <Button
                variant="secondary"
                size="sm"
                className="w-100  d-flex align-items-center justify-content-center gap-1"
                onClick={handleExcelDownload}
                disabled={isLoading}
              >
                <RiFileExcel2Line /> 엑셀다운로드
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Tabulator 그리드 */}
      <Card>
        <Card.Header className="bg-light text-dark fw-bold">
          판촉사원별 주간 실적
        </Card.Header>
        <Card.Body>
          <ReactTabulator
            ref={tableRef}
            onRef={(ref) => setTabulatorInstance(ref)}  // 이 줄 추가
            data={tableData}
            columns={columns}
            options={options}
            layout="fitColumns"
          />
        </Card.Body>
        {/* Footer 추가 */}
        <Card.Footer className="text-muted">
          <small>총 <strong>{tableData.length}</strong>건의 데이터가 조회되었습니다.</small>
        </Card.Footer>
      </Card>
    </Container>
  );
};

export default PromoPersonPerf;