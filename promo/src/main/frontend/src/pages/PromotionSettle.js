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
import { FaSearch, FaSave } from "react-icons/fa";
import { RiFileExcel2Line } from "react-icons/ri";
import axios from 'axios';  // axios import 추가
import Swal from 'sweetalert2';
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

const PromotionSettle = () => {
  const [stdYear, setStdYear] = useState(getCurrentYear());
  const [stdMonth, setStdMonth] = useState(getCurrentMonth());
  const [stdWeek, setStdWeek] = useState(getCurrentWeek());  // 주차 저장
  const [selectedTeamPersonCd, setSelectedTeamPersonCd] = useState('');
  const [selectedGubun, setSelectedGubun] = useState('');
  const [isClosed, setIsClosed] = useState(false);  // 마감여부 체크박스 state 추가
  const [tableData, setTableData] = useState([]);
  const [teamPersonList, setTeamPersonList] = useState([]);  // 대리점 목록 state 추가
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [isManager, setIsManager] = useState(false);  // 매니저 여부 state 추가
  const [weekOptions, setWeekOptions] = useState([]);  // 주차 옵션 목록 state 추가
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [agencyList, setAgencyList] = useState([]);  // 대리점 목록 state 추가
  const [selectedAgency, setSelectedAgency] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [promotionEmployee, setPromotionEmployee] = useState('');
  const [selectedHcStatus, setSelectedHcStatus] = useState('');
  const [selectedHcActionStatus, setSelectedHcActionStatus] = useState('');
  const [selectedAddCondition, setSelectedAddCondition] = useState('');
  const tableRef = useRef(null);
  const fileInputRef = useRef(null);

  // 컴포넌트 마운트 시 대리점 목록 조회
  useEffect(() => {
    // 초기 주차 목록 설정
    const initialWeeks = getWeeksInMonth(stdYear, stdMonth);
    setWeekOptions(initialWeeks);
    if (initialWeeks.length > 0) {
        setStdWeek(initialWeeks[0].dateRange);
    }

    fetchTeamPersonList();
  }, []);

  // 컴포넌트 마운트 시 대리점 목록 조회
    useEffect(() => {
      fetchAgencyList();
    }, []);

  // stdWeek와 selectedTeamPersonCd가 모두 설정된 후 자동 조회
  useEffect(() => {
    // 초기 로드 시에만 실행
    if (isInitialLoad && stdWeek && weekOptions.length > 0) {
      // selectedTeamPersonCd는 빈 문자열일 수도 있으므로 undefined 체크만
      if (selectedTeamPersonCd) {
        handleSearch();
        setIsInitialLoad(false);  // 최초 1회만 실행
      }
    }
  }, [stdWeek, selectedTeamPersonCd, weekOptions, agencyList, isInitialLoad]);

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
  const fetchTeamPersonList = async () => {
    try {
      const response = await axios.get('/api/promo/getAllTeamPerson');  // API 엔드포인트 수정 필요
      
      // API 응답 구조에 따라 수정
      // 예: response.data 또는 response.data.data
      setTeamPersonList(response.data);

      // 매니저가 아니면 담당자 변경 불가
      // 매니저 여부 확인
        const managerYn = sessionStorage.getItem("managerYn");
        const isManagerUser = managerYn === "1";
        setIsManager(isManagerUser);

    // sessionStorage에서 teamPersonCd 가져오기
    const teamPersonCd = sessionStorage.getItem("teamPersonCd");
    
    // teamPersonCd 일치하는 teampersoncd 찾기
    if (teamPersonCd && response.data && response.data.length > 0) {
      const matchedPerson = response.data.find(
        person => person.teamPersonCd === teamPersonCd
      );
      
      // 일치하는 항목이 있으면 해당 값으로 설정
      if (matchedPerson) {
        setSelectedTeamPersonCd(matchedPerson.teamPersonCd);
      }
    }
      
    } catch (error) {
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '담당자 목록 조회 실패',
        confirmButtonText: '확인'
      });
      console.error('담당자 목록 조회 실패:', error);
      // 에러 시 빈 배열 설정
      setTeamPersonList([]);
    }
  };

  // 대리점 목록 조회 함수
  const fetchAgencyList = async () => {
    try {

      const response = await axios.get('/api/promo/getMyAgencyList', {
        params: {
          teamPersonCd: sessionStorage.getItem("teamPersonCd"),
          managerYn: sessionStorage.getItem("managerYn")
        }
      });  // API 엔드포인트 수정 필요
      
      // API 응답 구조에 따라 수정
      // 예: response.data 또는 response.data.data
      setAgencyList(response.data);
      
    } catch (error) {
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '대리점 목록 조회 실패',
        confirmButtonText: '확인'
      });
      console.error('대리점 목록 조회 실패:', error);
      // 에러 시 빈 배열 설정
      setAgencyList([]);
    }
  };

  // 테이블 컬럼 정의
  const columns = [
    // {
    //   formatter: "rowSelection",  // 체크박스 추가
    //   titleFormatter: "rowSelection",
    //   hozAlign: "center",
    //   headerSort: false,
    //   width: 50,
    //   cellClick: function(e, cell) {
    //     cell.getRow().toggleSelect();
    //   }
    // },
    {
      title: 'No',
      field: 'no',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '대리점코드',
      field: 'agencyCd',
      width: 140,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '대리점명',
      field: 'agencyNm',
      width: 150,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '담당자',
      field: 'teamPersonNm',
      width: 180,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '전송여부',
      field: 'sendYn',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell) {
        const value = cell.getValue();
        if (value === '전송완료') {
          return `<span style="color: blue; cursor: pointer;">${value}</span>`;
        } else if (value === '미전송') {
          return `<span style="color: red; cursor: pointer;">${value}</span>`;
        } else {
          return `<span style="color: #ff8c00; cursor: pointer;">${value}</span>`;
        }
        
      }
    }
  ];

  // 엑셀 업로드 함수
  const handleExcelUpload = () => {
    // 파일 선택 다이얼로그 열기
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  // 파일 선택 시 처리 함수
  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    
    if (!file) {
      return;
    }

    // 파일 확장자 체크
    const fileExtension = file.name.split('.').pop().toLowerCase();
    if (!['xls', 'xlsx'].includes(fileExtension)) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '엑셀 파일만 업로드 가능합니다. (.xls, .xlsx)',
        confirmButtonText: '확인'
      });
      event.target.value = ''; // input 초기화
      return;
    }

    // 파일 크기 체크 (예: 10MB 제한)
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '파일 크기는 10MB를 초과할 수 없습니다.',
        confirmButtonText: '확인'
      });
      event.target.value = ''; // input 초기화
      return;
    }

    // 업로드 확인
    const result = await Swal.fire({
      icon: 'question',
      title: '파일 업로드',
      text: `${file.name} 파일을 업로드하시겠습니까?`,
      showCancelButton: true,
      confirmButtonText: '업로드',
      cancelButtonText: '취소'
    });

    if (!result.isConfirmed) {
      event.target.value = ''; // input 초기화
      return;
    }

    try {
      // FormData 생성
      const formData = new FormData();
      formData.append('file', file);
      formData.append('stdYear', stdYear);
      formData.append('stdMonth', stdMonth);
      formData.append('stdWeek', stdWeek);
      
      const [startDate, endDate] = stdWeek.split('|');
      formData.append('startDate', startDate);
      formData.append('endDate', endDate);
      
      if (selectedTeamPersonCd) {
        formData.append('teamPersonCd', selectedTeamPersonCd);
      }

      // 로딩 표시
      Swal.fire({
        title: '업로드 중...',
        text: '파일을 업로드하고 있습니다.',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // API 호출
      // const response = await axios.post('/api/promo/uploadMilkbangFile', formData, {
      //   headers: {
      //     'Content-Type': 'multipart/form-data'
      //   }
      // });

      const response = {
        data : {
          message : '확인'
        }
      }

      setTimeout(() => {
      
    }, 1000);

      // 성공 처리
      Swal.fire({
        icon: 'success',
        title: '업로드 완료',
        text: response.data.message || '파일이 성공적으로 업로드되었습니다.',
        confirmButtonText: '확인'
      }).then(() => {
        // 업로드 후 목록 재조회
        handleSearch();
      });

    } catch (error) {
      console.error('업로드 실패:', error);
      
      // 에러 메시지 처리
      let errorMessage = '파일 업로드에 실패했습니다.';
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      Swal.fire({
        icon: 'error',
        title: '업로드 실패',
        text: errorMessage,
        confirmButtonText: '확인'
      });
    } finally {
      // input 초기화 (같은 파일 재선택 가능하도록)
      event.target.value = '';
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
    const fileName = `판촉실적 정산_${today}.xlsx`;
    
    tabulatorInstance?.current.download("xlsx", fileName, {
      sheetName: "판촉실적 정산"
    });
  };

  // 조회 버튼 클릭
  const handleSearch = async () => {

    try {

      // 로딩 placeholder 설정
      tabulatorInstance.current.setData([]);
      tabulatorInstance.current.options.placeholder = 
        '<div class="text-center py-5"><div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem; animation-duration: 0.9s;"></div><div class="fw-bold text-primary fs-5">조회 중...</div></div>';
      tabulatorInstance.current.redraw();

      const [startDate, endDate] = stdWeek.split('|');
      // 조회 API 호출
      const response = await axios.get('/api/promo/getMilkNotSubmitFileList', {
        params: { stdYear : stdYear,
                  stdMonth : stdMonth,
                  stdWeek : stdWeek,
                  startDate : startDate,
                  endDate : endDate,
                  teamPersonCd: selectedTeamPersonCd }
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
    }
  };

  // 저장 버튼 클릭 함수
  const handleSave = async () => {
    // Tabulator 인스턴스가 준비되었는지 확인
    if (!tabulatorInstance || !tabulatorInstance.current) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '테이블이 준비되지 않았습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // 테이블 데이터 가져오기
    const dataToSave = tabulatorInstance.current.getData();

    // 저장할 데이터가 없는 경우 체크
    if (!dataToSave || dataToSave.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '저장할 데이터가 없습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // 저장 확인 다이얼로그
    const result = await Swal.fire({
      icon: 'question',
      title: '저장 확인',
      text: `${dataToSave.length}건의 데이터를 저장하시겠습니까?`,
      showCancelButton: true,
      confirmButtonText: '저장',
      cancelButtonText: '취소',
      confirmButtonColor: '#28a745'
    });

    // 취소 버튼 클릭 시
    if (!result.isConfirmed) {
      return;
    }

    try {
      // 로딩 표시
      Swal.fire({
        title: '저장 중...',
        text: '잠시만 기다려주세요.',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // API 호출 - 저장
      const response = await axios.post('/api/promo/savePromotionSettle', {
        data: dataToSave,
        stdYear: stdYear,
        stdMonth: stdMonth,
        stdWeek: stdWeek,
        startDate: startDate,
        endDate: endDate,
        teamPersonCd: selectedTeamPersonCd,
        agencyCd: selectedAgency,
        gubun: selectedGubun,
        closed: isClosed ? '1' : '0',
        promotionEmployee: promotionEmployee,
        hcStatus: selectedHcStatus,
        hcActionStatus: selectedHcActionStatus,
        addCondition: selectedAddCondition
      });

      // 저장 성공
      Swal.fire({
        icon: 'success',
        title: '저장 완료',
        text: response.data.message || '데이터가 성공적으로 저장되었습니다.',
        confirmButtonText: '확인'
      }).then(() => {
        // 저장 후 목록 재조회
        handleSearch();
      });

    } catch (error) {
      console.error('저장 실패:', error);

      // 에러 메시지 처리
      let errorMessage = '데이터 저장에 실패했습니다.';
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      Swal.fire({
        icon: 'error',
        title: '저장 실패',
        text: errorMessage,
        confirmButtonText: '확인'
      });
    }
  };

  // Tabulator 옵션
  const options = {
    layout: 'fitColumns',
    pagination: false,
    placeholder: '조회된 데이터가 없습니다.',
    height: '435px'
  };

  return (
    <Container fluid className="mt-1">

      {/* 숨겨진 파일 입력 */}
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        accept=".xls,.xlsx"
        style={{ display: 'none' }}
      />

      {/* 제목 */}
      <Row className="mb-1">
        <Col>
          <h5>
            <i className="bi bi-circle-fill text-warning me-1"></i>
            <CiViewList size={22} />
            판촉실적 정산
          </h5>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-end mb-2">
            {/* 날짜 범위 */}
            <Col md={5} style={{ minWidth: '500px', maxWidth: '550px' }}>
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
                    style={{ width: '200px' }}
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

            {/* 담당자 선택 */}
            <Col md={3} style={{ minWidth: '200px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    담당자 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedTeamPersonCd}
                    onChange={(e) => {
                      setSelectedTeamPersonCd(e.target.value);
                    }}
                    disabled={!isManager}  // 매니저가 아니면 비활성화
                    style={{ width: '150px' }}  // 고정 크기
                  >
                    <option value="">= 전체 =</option>
                    {teamPersonList.map((teamPerson) => (
                      <option key={teamPerson.teamPersonCd} value={teamPerson.teamPersonCd}>
                        {teamPerson.teamPersonNm}
                      </option>
                    ))}
                  </Form.Select>
                </div>
              </Form.Group>
            </Col>

            {/* 대리점 선택 */}
            <Col md={3} style={{ minWidth: '200px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    대리점 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedAgency}
                    onChange={(e) => {
                      setSelectedAgency(e.target.value);
                    }}
                    style={{ width: '150px' }}  // 고정 크기
                  >
                    <option value="">= 전체 =</option>
                    {agencyList.map((agency) => (
                      <option key={agency.agencyCd} value={agency.agencyCd}>
                        {agency.agencyNm}
                      </option>
                    ))}
                  </Form.Select>
                </div>
              </Form.Group>
            </Col>

            {/* 판촉사원 입력 */}
            <Col md={1} style={{ minWidth: '200px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '70px' }}>
                    판촉사원 :
                  </Form.Label>
                  <Form.Control
                    type="text"
                    size="sm"
                    value={promotionEmployee}
                    onChange={(e) => setPromotionEmployee(e.target.value)}
                    placeholder="판촉사원명 입력"
                    style={{ width: '120px' }}
                  />
                </div>
              </Form.Group>
            </Col>
          </Row>

          {/* 두 번째 줄: 조회 버튼 */}
          <Row className="align-items-end mb-2">
            {/* 날짜 범위 */}
            <Col md={5} style={{ minWidth: '500px', maxWidth: '550px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '185px' , fontSize: '0.75rem'}}>
                    날짜검색(주차 검색보다 우선 적용) : 
                  </Form.Label>
                  <Form.Control
                    type="date"
                    size="sm"
                    value={startDate}
                    onChange={(e) => {
                      setStartDate(e.target.value);
                    }}
                    style={{ width: '130px' }}  // 고정 크기
                  />
                  <span className="small">~</span>
                  <Form.Control
                    type="date"
                    size="sm"
                    value={endDate}
                    onChange={(e) => {
                      setEndDate(e.target.value);
                    }}
                    style={{ width: '130px' }}  // 고정 크기
                  />
                </div>
              </Form.Group>
            </Col>

            {/* 담당자 선택 */}
            <Col md={3} style={{ minWidth: '200px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '80px' }}>
                    해피콜 결과 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedHcStatus}
                    onChange={(e) => setSelectedHcStatus(e.target.value)}
                    style={{ width: '130px' }}
                  >
                    <option value="">= 전체 =</option>
                    <option value="10">미확인</option>
                    <option value="11">정상</option>
                    <option value="12">부재중</option>
                    <option value="13">상이건</option>
                    <option value="14">결번</option>
                    <option value="15">내용변경</option>
                  </Form.Select>
                </div>
              </Form.Group>
            </Col>

            {/* 대리점 선택 */}
            <Col md={3} style={{ minWidth: '220px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '103px' }}>
                    해피콜 결과확인 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedHcActionStatus}
                    onChange={(e) => setSelectedHcActionStatus(e.target.value)}
                    style={{ width: '90px' }}
                  >
                    <option value="">= 전체 =</option>
                    <option value="10">미확인</option>
                    <option value="11">정상</option>
                    <option value="12">부재중</option>
                    <option value="13">상이건</option>
                    <option value="14">결번</option>
                    <option value="15">내용변경</option>
                  </Form.Select>
                </div>
              </Form.Group>
            </Col>
            {/* 대리점 선택 */}
            <Col md={1} style={{ minWidth: '200px', maxWidth: '220px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '70px' }}>
                    특이사항 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedAddCondition}
                    onChange={(e) => setSelectedAddCondition(e.target.value)}
                    style={{ width: '120px' }}
                  >
                    <option value="">없음</option>
                    <option value="1">이중기재</option>
                    <option value="2">유치원</option>
                    <option value="3">전화번호</option>
                  </Form.Select>
                </div>
              </Form.Group>
            </Col>
          </Row>

          {/* 세 번째 줄: 조회 버튼 */}
          <Row className="align-items-end mb-2">
            

            

            {/* 날짜 범위 */}
            <Col md={2} style={{ minWidth: '250px', maxWidth: '300px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  {/* <Form.Label className="fw-bold small mb-0" style={{ minWidth: '20px'}}>
                    구분 : 
                  </Form.Label> */}
                  {/* <Form.Select
                    size="sm"
                    value={selectedGubun}
                    onChange={(e) => setSelectedGubun(e.target.value)}
                    style={{ width: '130px' }}
                  >
                    <option value="">전체</option>
                    <option value="1">일반</option>
                    <option value="2">프리미엄</option>
                  </Form.Select> */}

                  {/* 마감여부 체크박스 */}
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '20px'}}>
                    마감 : 
                  </Form.Label>
                  <Form.Check
                    type="checkbox"
                    id="closedCheck"
                    checked={isClosed}
                    onChange={(e) => setIsClosed(e.target.checked)}
                    className="mb-0 ms-4"
                    style={{ 
                      transform: 'scale(1.2)',  // 체크박스 크기 1.2배
                      cursor: 'pointer' 
                    }}
                  />
                  <Form.Label 
                    htmlFor="closedCheck" 
                    className="mb-0 text-muted"  // 회색
                    style={{ 
                      cursor: 'pointer',
                      marginLeft: '5px'  // 체크박스와 라벨 간격
                    }}
                  >
                    마감여부
                  </Form.Label>
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

            {/* 저장 버튼 */}
            <Col md={1} style={{ minWidth: '100px', maxWidth: '100px' }}>
              <Button
                variant="success"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleSave}
              >
                <FaSave /> 저장
              </Button>
            </Col>

            {/* 엑셀 버튼 */}
            <Col md={2} style={{ minWidth: '160px', maxWidth: '160px' }}>
              <Button
                variant="secondary"
                size="sm"
                className="w-100  d-flex align-items-center justify-content-center gap-1"
                onClick={handleExcelDownload}
              >
                <i className="bi bi-search me-2">
                    <RiFileExcel2Line /> 엑셀다운로드
                </i>
                
              </Button>
            </Col>            
          </Row>
        </Card.Body>
      </Card>

      {/* Tabulator 그리드 */}
      <Card>
        <Card.Header className="bg-light text-dark fw-bold">
          판촉실적 정산
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

export default PromotionSettle;