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
import { FaLock, FaLockOpen } from "react-icons/fa";
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

const MilkFileNotSubmit = () => {
  const [stdYear, setStdYear] = useState(getCurrentYear());
  const [stdMonth, setStdMonth] = useState(getCurrentMonth());
  const [stdWeek, setStdWeek] = useState(getCurrentWeek());  // 주차 저장
  const [selectedTeamCd, setSelectedTeamCd] = useState('');
  const [tableData, setTableData] = useState([]);
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [weekOptions, setWeekOptions] = useState([]);  // 주차 옵션 목록 state 추가
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [agencyList, setAgencyList] = useState([]);  // 대리점 목록 state 추가
  const [isLoading, setIsLoading] = useState(false);  // 조회 중 상태
  const tableRef = useRef(null);

  useEffect(() => {
    fetchAgencyList();
  }, []);

  // 컴포넌트 마운트 시 대리점 목록 조회
  useEffect(() => {
    // 초기 주차 목록 설정
    const initialWeeks = getWeeksInMonth(stdYear, stdMonth);
    setWeekOptions(initialWeeks);
    if (initialWeeks.length > 0) {
        setStdWeek(initialWeeks[0].dateRange);
    }
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

  // stdWeek와 selectedTeamPersonCd가 모두 설정된 후 자동 조회
  useEffect(() => {
    // 초기 로드 시에만 실행
    if (isInitialLoad && stdWeek && weekOptions.length > 0 && agencyList.length > 0) {
      handleSearch();
      setIsInitialLoad(false);  // 최초 1회만 실행
    }
  }, [stdWeek, weekOptions, isInitialLoad, agencyList]);

  // 대리점 목록 조회 함수
  const fetchAgencyList = async () => {
    try {
      const response = await axios.get('/api/promo/getAllAgency');  // API 엔드포인트 수정 필요
      
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

  // 마감 버튼 클릭 핸들러
  const handleClose = async () => {
    
    // 선택된 행 가져오기
    const selectedRows = tabulatorInstance.current.getSelectedData();
    
    if (!selectedRows || selectedRows.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '마감 처리할 담당자를 선택해주세요.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ========================================
    // 2. 마감 가능 여부 검증 (프론트엔드)
    // ========================================
    // 2-1. 미저장 건수가 있는 담당자 필터링
    const unsavedRows = selectedRows.filter(row => {
      const unSavedCnt = parseInt(row.unSavedCnt) || 0;
      return unSavedCnt > 0;
    });

    // 2-2. 미저장 건이 있는 경우 경고
    if (unsavedRows.length > 0) {
      const unsavedList = unsavedRows.map(row => 
        `- ${row.teamPersonNm}: ${row.unSavedCnt}건`
      ).join('\n');
      
      Swal.fire({
        icon: 'warning',
        title: '마감 불가',
        html: `
          <div class="text-start">
            <p class="mb-3">다음 담당자는 미저장 건이 있어 마감할 수 없습니다:</p>
            <pre class="bg-light p-3 rounded" style="max-height: 300px; overflow-y: auto;">${unsavedList}</pre>
            <p class="text-danger small mt-3 mb-0">※ 해당 담당자의 판촉실적을 모두 저장한 후 마감해주세요.</p>
          </div>
        `,
        confirmButtonText: '확인',
        width: '600px'
      });
      return;
    }

    // 2-3. 이미 마감완료된 담당자 필터링
    const alreadyClosedRows = selectedRows.filter(row => 
      row.masterCloseNm === '마감완료' || row.masterCloseNm === '마감후 추가건'
    );

    // 2-4. 이미 마감완료된 담당자가 있는 경우 확인
    if (alreadyClosedRows.length > 0) {
      const closedList = alreadyClosedRows.map(row => 
        `- ${row.teamPersonNm}`
      ).join('\n');
      
      const confirmResult = await Swal.fire({
        icon: 'question',
        title: '확인',
        html: `
          <div class="text-start">
            <p class="mb-3">다음 담당자는 이미 마감완료 상태입니다:</p>
            <pre class="bg-light p-3 rounded" style="max-height: 200px; overflow-y: auto;">${closedList}</pre>
            <p class="text-info small mt-3 mb-0">※ 해당 담당자는 마감 처리에서 제외하고 진행하시겠습니까?</p>
          </div>
        `,
        showCancelButton: true,
        confirmButtonText: '제외하고 진행',
        cancelButtonText: '취소',
        confirmButtonColor: '#198754',
        width: '600px'
      });

      // 취소한 경우
      if (!confirmResult.isConfirmed) {
        return;
      }

      // 이미 마감완료된 담당자를 제외한 행만 선택
      const filteredRows = selectedRows.filter(row => 
        row.masterCloseNm !== '마감완료'
      );

      // 제외 후 처리할 행이 없는 경우
      if (filteredRows.length === 0) {
        Swal.fire({
          icon: 'info',
          title: '알림',
          text: '마감 처리할 담당자가 없습니다.',
          confirmButtonText: '확인'
        });
        return;
      }

      // 필터링된 행으로 계속 진행
      selectedRows.length = 0;
      selectedRows.push(...filteredRows);
    }

    try {
      // ✅ 마감 사유 입력 받기
      const { value: closeRemark } = await Swal.fire({
        title: '판촉실적 마감',
        html: `
          <div class="text-start">
            <p class="mb-3">선택한 <strong>${selectedRows.length}명</strong>의 판촉실적을 마감하시겠습니까?</p>
            <p class="text-danger small mb-3">※ 미저장 건이 있는 담당자는 마감할 수 없습니다.</p>
            <label for="closeRemark" class="form-label fw-bold">마감 사유:</label>
            <textarea 
              id="closeRemark" 
              class="form-control" 
              rows="3"
              placeholder="마감 사유를 입력해주세요 (선택사항)"
            ></textarea>
          </div>
        `,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '마감',
        cancelButtonText: '취소',
        confirmButtonColor: '#198754',
        preConfirm: () => {
          return document.getElementById('closeRemark').value;
        }
      });

      // 취소한 경우
      if (closeRemark === undefined) {
        return;
      }

      // ✅ 로딩 표시
      Swal.fire({
        title: '처리 중...',
        html: '판촉실적을 마감하고 있습니다.',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // 선택된 행에서 필요한 데이터만 추출
      const [startDate, endDate] = stdWeek.split('|');
      const closeData = selectedRows.map(row => ({
        teamPersonCd: row.teamPersonCd,
        teamPersonNm: row.teamPersonNm,
        startDate: startDate,
        endDate: endDate,
        teamCd: selectedTeamCd || null,
        masterCloseRemark: closeRemark || null
      }));

      // ✅ API 호출
      const response = await axios.post('/api/promo/closePromo', closeData);

      // ✅ 성공 처리
      if (response.data.success) {
        await Swal.fire({
          icon: 'success',
          title: '마감 완료',
          html: response.data.message,
          confirmButtonText: '확인'
        });

        // 재조회
        handleSearch();
      } else {
        Swal.fire({
          icon: 'error',
          title: '마감 실패',
          text: response.data.message || '마감 처리에 실패했습니다.',
          confirmButtonText: '확인'
        });
      }

    } catch (error) {
      console.error('마감 처리 실패:', error);
      
      // 서버에서 받은 에러 메시지 표시
      const errorMessage = error.response?.data?.message || 
                          '마감 처리 중 오류가 발생했습니다.';
      
      Swal.fire({
        icon: 'error',
        title: '오류',
        html: errorMessage.replace(/\n/g, '<br>'),
        confirmButtonText: '확인',
        width: '600px'
      });
    }
  };

  // 마감해제 버튼 클릭 핸들러
  const handleUnclose = async () => {
    
    // 선택된 행 가져오기
    const selectedRows = tabulatorInstance.current.getSelectedData();
    
    if (!selectedRows || selectedRows.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '마감해제할 담당자를 선택해주세요.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ========================================
    // 2. 마감해제 가능 여부 검증 (프론트엔드)
    // ========================================
    // 2-1. 마감완료 상태가 아닌 담당자 필터링
    const notClosedRows = selectedRows.filter(row => 
      row.masterCloseNm !== '마감완료' && row.masterCloseNm !== '마감후 추가건'
    );

    // 2-2. 마감완료 상태가 아닌 담당자가 있는 경우 경고
    if (notClosedRows.length > 0) {
      const notClosedList = notClosedRows.map(row => 
        `- ${row.teamPersonNm}: ${row.masterCloseNm || '마감전'}`
      ).join('\n');
      
      const confirmResult = await Swal.fire({
        icon: 'warning',
        title: '확인',
        html: `
          <div class="text-start">
            <p class="mb-3">다음 담당자는 마감완료 상태가 아닙니다:</p>
            <pre class="bg-light p-3 rounded" style="max-height: 300px; overflow-y: auto;">${notClosedList}</pre>
            <p class="text-info small mt-3 mb-0">※ 해당 담당자는 마감해제 처리에서 제외하고 진행하시겠습니까?</p>
          </div>
        `,
        showCancelButton: true,
        confirmButtonText: '제외하고 진행',
        cancelButtonText: '취소',
        confirmButtonColor: '#dc3545',
        width: '600px'
      });

      // 취소한 경우
      if (!confirmResult.isConfirmed) {
        return;
      }

      // 마감완료 상태인 담당자만 선택
      const filteredRows = selectedRows.filter(row => 
        row.masterCloseNm === '마감완료'
      );

      // 제외 후 처리할 행이 없는 경우
      if (filteredRows.length === 0) {
        Swal.fire({
          icon: 'info',
          title: '알림',
          text: '마감해제할 담당자가 없습니다. (마감완료 상태인 담당자만 해제 가능)',
          confirmButtonText: '확인'
        });
        return;
      }

      // 필터링된 행으로 계속 진행
      selectedRows.length = 0;
      selectedRows.push(...filteredRows);
    }

    try {
      // ✅ 확인 메시지
      const result = await Swal.fire({
        title: '판촉실적 마감해제',
        html: `
          <div class="text-start">
            <p class="mb-3">선택한 <strong>${selectedRows.length}명</strong>의 판촉실적 마감을 해제하시겠습니까?</p>
            <p class="text-danger small mb-0">※ 마감된 건만 해제할 수 있습니다.</p>
          </div>
        `,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '해제',
        cancelButtonText: '취소',
        confirmButtonColor: '#dc3545'
      });

      // 취소한 경우
      if (!result.isConfirmed) {
        return;
      }

      // ✅ 로딩 표시
      Swal.fire({
        title: '처리 중...',
        html: '판촉실적 마감을 해제하고 있습니다.',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // 선택된 행에서 필요한 데이터만 추출
      const [startDate, endDate] = stdWeek.split('|');
      const uncloseData = selectedRows.map(row => ({
        teamPersonCd: row.teamPersonCd,
        teamPersonNm: row.teamPersonNm,
        startDate: startDate,
        endDate: endDate,
        teamCd: selectedTeamCd || null
      }));

      // ✅ API 호출
      const response = await axios.post('/api/promo/unclosePromo', uncloseData);

      // ✅ 성공 처리
      if (response.data.success) {
        await Swal.fire({
          icon: 'success',
          title: '마감해제 완료',
          html: response.data.message,
          confirmButtonText: '확인'
        });

        // 재조회
        handleSearch();
      } else {
        Swal.fire({
          icon: 'error',
          title: '마감해제 실패',
          text: response.data.message || '마감해제 처리에 실패했습니다.',
          confirmButtonText: '확인'
        });
      }

    } catch (error) {
      console.error('마감해제 처리 실패:', error);
      
      // 서버에서 받은 에러 메시지 표시
      const errorMessage = error.response?.data?.message || 
                          '마감해제 처리 중 오류가 발생했습니다.';
      
      Swal.fire({
        icon: 'error',
        title: '오류',
        html: errorMessage.replace(/\n/g, '<br>'),
        confirmButtonText: '확인',
        width: '600px'
      });
    }
  };

  // 테이블 컬럼 정의
  const columns = [
    {
      formatter: "rowSelection",  // 체크박스 추가
      titleFormatter: "rowSelection",
      hozAlign: "center",
      headerSort: false,
      width: 50,
      cellClick: function(e, cell) {
        cell.getRow().toggleSelect();
      }
    },
    {
      title: 'No',
      field: 'no',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      // ✅ formatter로 순번 자동 생성
      formatter: function(cell, formatterParams, onRendered) {
        // 현재 행의 위치를 가져와서 1부터 시작하는 순번 반환
        return cell.getRow().getPosition();
      }
    },
    {
      title: '담당자코드',
      field: 'teamPersonCd',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '담당자',
      field: 'teamPersonNm',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '전체건수',
      field: 'totalCnt',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '마감건수',
      field: 'closeCnt',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '미저장건수',
      field: 'unSavedCnt',
      width: 110,
      hozAlign: 'center',
      headerHozAlign: 'center',
      // ✅ HTML을 직접 반환하는 방식
      formatter: function(cell) {
        const value = cell.getValue();
        const numValue = parseInt(value) || 0;
        
        // null 또는 undefined 체크
        if (value === null || value === undefined) {
          return '';
        }
        
        // 0이 아니면 빨간색, 굵게 표시
        if (numValue !== 0) {
          return `<span style="color: #dc3545; font-weight: bold;">${value}</span>`;
        }
        
        return value;
      }
    },
    {
      title: '마감여부',
      field: 'masterCloseNm',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center',
      // ✅ 마감 상태별 색상 구분
      formatter: function(cell) {
        const value = cell.getValue();
        
        // null 또는 undefined 체크
        if (!value) {
          return '';
        }
        
        // 상태별 색상 설정
        let color = '#000000';  // 기본값: black
        let fontWeight = 'bold';  // 모든 상태 굵게
        
        if (value === '미저장') {
          color = '#dc3545';  // red
        } else if (value === '마감전') {
          color = '#000000';  // black
        } else if (value === '마감완료') {
          color = '#0d6efd';  // blue
        } else if (value === '마감후 추가건') {
          color = '#dc3545';  // red
        }
        
        return `<span style="color: ${color}; font-weight: ${fontWeight}; font-size: 1.15em;">${value}</span>`;
      }
    },
    {
      title: '마감일자',
      field: 'masterCloseDt',
      width: 140,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '마감사유',
      field: 'masterCloseRemark',
      width: 200,
      hozAlign: 'center',
      headerHozAlign: 'center'
    }
  ];

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

      const [startDate, endDate] = stdWeek.split('|');
      // 조회 API 호출
      const response = await axios.get('/api/promo/getCloseList', {
        params: { startDate : startDate,
                  endDate : endDate,
                  teamCd: selectedTeamCd }
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
            판촉실적 마감
          </h5>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-end">
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
                    영업팀 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedTeamCd}
                    onChange={(e) => {
                      setSelectedTeamCd(e.target.value);
                    }}
                    style={{ width: '150px' }}  // 고정 크기
                  >
                    <option value="">= 전체 =</option>
                    <option value="1">가정대리점1팀</option>
                    <option value="2">가정대리점2팀</option>
                    <option value="3">가정대리점3팀</option>
                    <option value="4">가정대리점4팀</option>
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

            {/* 마감 버튼 */}
            <Col md={1} style={{ minWidth: '100px', maxWidth: '100px' }}>
              <Button
                variant="success"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleClose}
                disabled={isLoading}
              >
                <FaLock /> 마감
              </Button>
            </Col>

            {/* 마감해제 버튼 */}
            <Col md={1} style={{ minWidth: '130px', maxWidth: '130px' }}>
              <Button
                variant="danger"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleUnclose}
                disabled={isLoading}
              >
                <FaLockOpen /> 마감해제
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Tabulator 그리드 */}
      <Card>
        <Card.Header className="bg-light text-dark fw-bold">
          판촉실적 마감
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

export default MilkFileNotSubmit;