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
import '../styles/PromotionSettle.css'
import 'bootstrap/dist/css/bootstrap.min.css'; // Bootstrap CSS import (npm 설치 시)
import { CiViewList} from "react-icons/ci";
import { FaSearch, FaSave, FaTrashAlt } from "react-icons/fa";
import { RiFileExcel2Line } from "react-icons/ri";
import axios from 'axios';  // axios import 추가
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';  // 이 줄 추가
import PromotionDetailModal from '../components/modal/PromotionDetailModal.js';
import PromotionDuplModal from '../components/modal/PromotionDuplModal.js';

// window.XLSX에 할당 (Tabulator가 사용할 수 있도록)
window.XLSX = XLSX;

/**
 * 해피콜 상담내용 상세보기 팝업
 * @param {Object} rowData - 클릭한 행의 데이터
 */
const handleHcContentDetail = (rowData) => {
  const content = rowData.hcContent || '';
  
  // HTML 태그 제거 (필요시)
  // const cleanContent = content.replace(/<br\s*\/?>/gi, '\n').replace(/<[^>]*>/g, '');
  
  Swal.fire({
    title: '해피콜 상담내용',
    html: `
      <div style="
        text-align: left; 
        padding: 20px; 
        font-family: 'Malgun Gothic', sans-serif;
        line-height: 1.8;
        font-size: 14px;
        max-height: 400px;
        overflow-y: auto;
        white-space: pre-wrap;
        word-break: break-all;
      ">
        ${content || '<span style="color: #999;">내용이 없습니다.</span>'}
      </div>
    `,
    icon: 'info',
    confirmButtonText: '확인',
    confirmButtonColor: '#0d6efd',
    width: '600px',
    customClass: {
      popup: 'hc-content-detail-popup'
    }
  });
};

/**
 * 담당 의견/대리점 소명 상세보기 팝업
 * @param {Object} rowData - 클릭한 행의 데이터
 */
const handleHcActionDetail = (rowData) => {
  const content = rowData.hcAction || '';
  
  // HTML 태그 제거 (필요시)
  // const cleanContent = content.replace(/<br\s*\/?>/gi, '\n').replace(/<[^>]*>/g, '');
  
  Swal.fire({
    title: '담당 의견/대리점 소명',
    html: `
      <div style="
        text-align: left; 
        padding: 20px; 
        font-family: 'Malgun Gothic', sans-serif;
        line-height: 1.8;
        font-size: 14px;
        max-height: 400px;
        overflow-y: auto;
        white-space: pre-wrap;
        word-break: break-all;
      ">
        ${content || '<span style="color: #999;">내용이 없습니다.</span>'}
      </div>
    `,
    icon: 'info',
    confirmButtonText: '확인',
    confirmButtonColor: '#0d6efd',
    width: '600px',
    customClass: {
      popup: 'hc-action-detail-popup'
    }
  });
};

// 숫자 천단위 콤마 포맷 함수
const formatNumberWithComma = (value) => {
  if (value == null || value === '') return '';
  const number = Number(value);
  if (isNaN(number)) return value;
  return number.toLocaleString('ko-KR');
};

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

const AgencyMangement = () => {
  const [stdYear, setStdYear] = useState(getCurrentYear());
  const [stdMonth, setStdMonth] = useState(getCurrentMonth());
  const [stdWeek, setStdWeek] = useState(getCurrentWeek());  // 주차 저장
  const [selectedTeamPersonCd, setSelectedTeamPersonCd] = useState('');
  const [isClosed, setIsClosed] = useState('');  // 마감여부 체크박스 state 추가
  const [tableData, setTableData] = useState([]);
  const [teamPersonList, setTeamPersonList] = useState([]);  // 대리점 목록 state 추가
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [isManager, setIsManager] = useState(false);  // 매니저 여부 state 추가
  const [weekOptions, setWeekOptions] = useState([]);  // 주차 옵션 목록 state 추가
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [agencyList, setAgencyList] = useState([]);  // 대리점 목록 state 추가
  const [selectedAgency, setSelectedAgency] = useState('');
  const [subEndDate, setSubEndDate] = useState('');
  const [subStartDate, setSubStartDate] = useState('');
  const [promotionEmployee, setPromotionEmployee] = useState('');
  const [selectedHcStatus, setSelectedHcStatus] = useState('');
  const [selectedIssueStatus, setSelectedIssueStatus] = useState('');
  const [selectedHcActionStatus, setSelectedHcActionStatus] = useState('');
  const [originalData, setOriginalData] = useState([]);  // 조회 시점의 원본 데이터 저장
  const [isLoading, setIsLoading] = useState(false);  // 조회 중 상태
  // ✅ 1. 현재 선택된 주차의 label을 저장할 state 추가
  const [currentWeekLabel, setCurrentWeekLabel] = useState('');

  // ✅ 모달 관련 state 추가
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showDuplModal, setShowDuplModal] = useState(false);
  const [selectedRowData, setSelectedRowData] = useState(null);
  const [selectedDuplRowData, setSelectedDuplRowData] = useState(null);
  const tableRef = useRef(null);
  const fileInputRef = useRef(null);

  // ✅ 합계 state 추가
  const [summaryData, setSummaryData] = useState({
    totalHob: 0,        // 전체홉수
    contractHob: 0,     // 계약홉수
    noContractHob: 0,   // 무계약홉수
    reContractHob: 0,   // 재계약홉수
    totalCount: 0,      // ✅ 전체 건수
    unSavedCount: 0,      // ✅ 저장 건수
    savedCount: 0,      // ✅ 저장 건수
    closedCount: 0,      // ✅ 마감 건수
    abnormalCount: 0,        // 전체 이상 건수 (중복 제거)
    abnormalBaechiX: 0,      // 배치X 건수
    abnormalDuplicate: 0,    // 이중기재 건수
    abnormalUnmatched: 0     // 매칭안됨 건수
  });

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

  // ✅ tableData 변경 시 합계 계산
  useEffect(() => {
    calculateSummary();
  }, [tableData]);

  // ✅ stdWeek 변경 시 해당 label 찾아서 저장
  useEffect(() => {
    // ✅ 날짜 검색을 사용하는 경우 label 비우기
    if (subStartDate || subEndDate) {
      setCurrentWeekLabel('');
      return;
    }
    
    if (stdWeek && weekOptions.length > 0) {
      const selectedWeek = weekOptions.find(week => week.dateRange === stdWeek);
      if (selectedWeek) {
        setCurrentWeekLabel(selectedWeek.label);
      }
    }
  }, [stdWeek, weekOptions, subStartDate, subEndDate]);  // ✅ 의존성 배열에 추가

  /**
   * 홉수 합계 계산 함수
   */
  const calculateSummary = () => {
    if (!tableData || tableData.length === 0) {
      setSummaryData({
        totalHob: 0,
        contractHob: 0,
        noContractHob: 0,
        reContractHob: 0,
        totalCount: 0,      // ✅ 추가
        unSavedCount: 0,      // ✅ 추가
        savedCount: 0,      // ✅ 추가
        closedCount: 0,      // ✅ 추가
        abnormalCount: 0,        // 전체 이상 건수 (중복 제거)
        abnormalBaechiX: 0,      // 배치X 건수
        abnormalDuplicate: 0,    // 이중기재 건수
        abnormalUnmatched: 0     // 매칭안됨 건수
      });
      return;
    }

    let totalHob = 0;
    let contractHob = 0;      // 신규 계약
    let noContractHob = 0;    // 무계약
    let reContractHob = 0;    // 재계약
    let unSavedCount = 0;       // ✅ 저장 건수
    let savedCount = 0;       // ✅ 저장 건수
    let closedCount = 0;      // ✅ 마감 건수
    let abnormalCount = 0;        // 전체 이상 건수 (중복 제거)
    let abnormalBaechiX = 0;      // 배치X 건수
    let abnormalDuplicate = 0;    // 이중기재 건수
    let abnormalUnmatched = 0;     // 매칭안됨 건수

    tableData.forEach(row => {
      // actualHob (마감홉수)를 기준으로 합계 계산
      const hob = parseFloat(row.actualHob) || 0;
      let hasAbnormal = false;
      totalHob += hob;

      // orderKind 또는 orderKindCd 기준으로 분류
      // 실제 데이터 구조에 맞게 조건 수정 필요
      const orderKind = row.orderKind || '';
      const orderKindCdNm = row.orderKindCdNm || '';

      if (orderKind === '신규' || orderKindCdNm === '신규') {
        contractHob += hob;
      } else if (orderKind === '무계약' || orderKindCdNm === '무계약') {
        noContractHob += hob;
      } else if (orderKind === '재계약' || orderKindCdNm === '재계약') {
        reContractHob += hob;
      }

      // ✅ 마감, 저장, 미저장 건수 계산
      if (row.masterCloseYn === '1' || row.masterCloseYn === 1) {
        closedCount++;
      } else if (row.saveYn === '1' || row.saveYn === 1) {
        savedCount++;
      } else {
        unSavedCount++;
      }

      // ✅ 각 유형별로 개별 집계
      if (row.promoTeamNm && row.promoTeamNm.includes('배치X')) {
        abnormalBaechiX++;
        hasAbnormal = true;
      }

      if (row.orderUserNm && row.orderUserNm.includes('이중기재')) {
        abnormalDuplicate++;
        hasAbnormal = true;
      }

      if (row.goodsOptionNm && row.goodsOptionNm.includes('매칭안됨')) {
        abnormalUnmatched++;
        hasAbnormal = true;
      }

      // 전체 이상 건수 (한 행에 여러 이상이 있어도 1건으로만 카운트)
      if (hasAbnormal) {
        abnormalCount++;
      }

    });

    setSummaryData({
      totalHob: totalHob,
      contractHob: contractHob,
      noContractHob: noContractHob,
      reContractHob: reContractHob,
      totalCount: tableData.length,  // ✅ 전체 건수
      unSavedCount: unSavedCount,        // ✅ 저장 건수
      savedCount: savedCount,        // ✅ 저장 건수
      closedCount: closedCount,       // ✅ 마감 건수
      abnormalCount: abnormalCount,        // 전체 이상 건수 (중복 제거)
      abnormalBaechiX: abnormalBaechiX,      // 배치X 건수
      abnormalDuplicate: abnormalDuplicate,    // 이중기재 건수
      abnormalUnmatched: abnormalUnmatched     // 매칭안됨 건수
    });
  };
  
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

      // 매니저가 아니면 담당자 변경 불가
      // 매니저 여부 확인
      const managerYn = sessionStorage.getItem("managerYn");
      const isManagerUser = managerYn === "1";
      setIsManager(isManagerUser);

      // sessionStorage에서 teamPersonCd 가져오기
      const agencyCd = sessionStorage.getItem("loginId");

      // teamPersonCd 일치하는 teampersoncd 찾기
      if (agencyCd && response.data && response.data.length > 0) {
        const matchedAgency = response.data.find(
          agency => agency.agencyCd === agencyCd
        );
        
        // 일치하는 항목이 있으면 해당 값으로 설정
        if (matchedAgency) {
          setSelectedAgency(matchedAgency.agencyCd);
        }
      }
      
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

  /**
   * ✅ 판촉일 셀 클릭 핸들러
   */
  const handlePromoDtClick = (rowData) => {
    setSelectedRowData(rowData);
    setShowDetailModal(true);
  };

  const handleDuplClick = (rowData) => {
    setSelectedDuplRowData(rowData);
    setShowDuplModal(true);
  };

  /**
   * ✅ 모달 저장 후 콜백
   */
  const handleModalSave = () => {
    handleSearch();  // 목록 재조회
  };

  /**
   * orderCd별로 첫 번째 행인지 확인하는 함수
   * @param {Object} rowData - 현재 행 데이터
   * @param {Array} allData - 전체 테이블 데이터
   * @returns {boolean} 첫 번째 행 여부
   */
  const isFirstRowInGroup = (rowData, allData) => {
    const currentOrderCd = rowData.orderCd;
    const currentIndex = allData.findIndex(row => 
      row.orderCd === rowData.orderCd && row.orderSeq === rowData.orderSeq
    );
    
    // 같은 orderCd를 가진 첫 번째 행인지 확인
    const firstIndex = allData.findIndex(row => row.orderCd === currentOrderCd);
    return currentIndex === firstIndex;
  };

  /**
   * orderCd별로 마지막 행인지 확인하는 함수
   * @param {Object} rowData - 현재 행 데이터
   * @param {Array} allData - 전체 테이블 데이터
   * @returns {boolean} 마지막 행 여부
   */
  const isLastRowInGroup = (rowData, allData) => {
    const currentOrderCd = rowData.orderCd;
    const currentIndex = allData.findIndex(row => 
      row.orderCd === rowData.orderCd && row.orderSeq === rowData.orderSeq
    );
    
    // 같은 orderCd를 가진 마지막 행인지 확인
    const lastIndex = allData.map((row, idx) => row.orderCd === currentOrderCd ? idx : -1)
      .filter(idx => idx !== -1)
      .pop();
    
    return currentIndex === lastIndex;
  };

  // 테이블 컬럼 정의
  const columns = [
    {
      title: 'No',
      field: 'no',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '상태',
      field: 'totStatus',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible:false,
      // ✅ formatter 함수 추가: 이중기재가 포함되면 빨간색 표시
      formatter: function(cell, formatterParams, onRendered) {
        const value = cell.getValue();
        
        // null 또는 undefined 체크
        if (!value) {
          return '';
        }
        
        if (value === '저장') {
          cell.getElement().style.color = '#28a745';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }

        if (value === '마감') {
          cell.getElement().style.color = '#6c757d';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }

        if (value === '미저장') {
          cell.getElement().style.color = '#fd7e14';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }
        
        return value;
      }
    },
    {
      title: '대리점',
      field: 'agencyNm',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell) {
        const value = cell.getValue() || '';
        const rowData = cell.getRow().getData();
        
        // ✅ 이상 데이터 확인
        const hasAbnormal = 
          (rowData.promoTeamNm && rowData.promoTeamNm.includes('배치X')) ||
          (rowData.orderUserNm && rowData.orderUserNm.includes('이중기재')) ||
          (rowData.goodsOptionNm && rowData.goodsOptionNm.includes('매칭안됨'));
        
        if (hasAbnormal) {
          return `
            <div style="
              cursor: pointer;
              color: #dc3545;
              text-decoration: underline;
              font-weight: bold;
            " 
            class="promo-dt-cell"
            title="클릭하여 상세 보기 (이상 데이터)">
              ${value}
            </div>
          `;
        }
        
        return `
          <div style="
            cursor: pointer;
            color: #0d6efd;
            text-decoration: underline;
            font-weight: 500;
          " 
          class="promo-dt-cell"
          title="클릭하여 상세 보기">
            ${value}
          </div>
        `;
      },
      cellClick: function(e, cell) {
        const rowData = cell.getRow().getData();
        handlePromoDtClick(rowData);
      }
    },
    {
      title: '판촉팀',
      field: 'promoTeamNm',
      width: 85,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell, formatterParams, onRendered) {
        const value = cell.getValue();
        
        // null 또는 undefined 체크
        if (!value) {
          return '';
        }
        
        // "이중기재" 텍스트가 포함되어 있는지 확인
        if (value.includes('배치X')) {
          cell.getElement().style.color = '#dc3545';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }
        
        return value;
      }
    },
    {
      title: '판촉사원',
      field: 'promoPersonNm',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '고객',
      field: 'orderUserNm',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      // ✅ formatter 함수 추가: 이중기재가 포함되면 빨간색 표시
      formatter: function(cell, formatterParams, onRendered) {
        const value = cell.getValue();
        
        // null 또는 undefined 체크
        if (!value) {
          return '';
        }
        
        // "이중기재" 텍스트가 포함되어 있는지 확인
        if (value.includes('이중기재')) {
          // ✅ 방법 1: HTML 반환 (권장)
          // return `<span style="color: #dc3545; font-weight: bold;">${value}</span>`;
          
          // ✅ 방법 2: cell element의 스타일 직접 수정 (대안)
          cell.getElement().style.color = '#dc3545';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }
        
        return value;
      },
      cellClick: function(e, cell) {
        const value = cell.getValue();

        if (!value) {
          return '';
        }

        if (value.includes('이중기재')) {
          const rowData = cell.getRow().getData();
          handleDuplClick(rowData);
        }
      }
    },
    {
      title: '상품',
      field: 'goodsOptionNm',
      width: 220,
      hozAlign: 'center',
      headerHozAlign: 'center',
      // ✅ formatter 함수 추가: 이중기재가 포함되면 빨간색 표시
      formatter: function(cell, formatterParams, onRendered) {
        const value = cell.getValue();
        
        // null 또는 undefined 체크
        if (!value) {
          return '';
        }
        
        // "이중기재" 텍스트가 포함되어 있는지 확인
        if (value.includes('매칭안됨')) {
          cell.getElement().style.color = '#dc3545';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }
        
        return value;
      }
    },
    {
      title: '주간수량',
      field: 'weekQty',
      width: 85,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '주간<br/>총수량';  // HTML로 줄바꿈
      },
      // ✅ 엑셀 다운로드 시 숫자로 저장
      accessorDownload: function(value) {
        if (value === null || value === undefined || value === '') return 0;
        const number = parseFloat(value);
        return isNaN(number) ? 0 : number;
      }
    },
    {
      title: '계약구분',
      field: 'orderKindCdNm',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '계약<br/>구분';  // HTML로 줄바꿈
      }
    },
    {
      title: '개월',
      field: 'contractPeriod',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell, formatterParams, onRendered) {
        const value = cell.getValue();
        
        // null 또는 undefined 체크
        if (!value) {
          return '0';
        }
        
        // 12개월 미만은 붉은색 표시
        if (Number(value) < 12) {
          cell.getElement().style.color = '#dc3545';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }
        
        return value;
      },
      // ✅ 엑셀 다운로드 시 숫자로 저장
      accessorDownload: function(value) {
        if (value === null || value === undefined || value === '') return 0;
        const number = parseFloat(value);
        return isNaN(number) ? 0 : number;
      }
    },
    {
      title: '계약선물',
      field: 'promoGiftNm',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '마감홉수',
      field: 'actualHob',
      width: 110,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'number',  // ✅ 'input' 대신 'number' 사용
      // ✅ 조건부 편집 가능 여부 설정
      editable: function(cell) {
        const rowData = cell.getRow().getData();
        // masterCloseYn이 1이면 편집 불가 (마감 상태)
        const isClosed = rowData.masterCloseYn === '1' || rowData.masterCloseYn === 1;
        return !isClosed;  // 마감이 아닐 때만 편집 가능
      },
      // ✅ editorParams로 소수점 자리수 제한
      editorParams: {
        min: 0,
        max: 999.9,
        step: 0.1,  // ✅ 0.1 단위로만 입력 가능
        selectContents: true,
        elementAttributes: {
          maxlength: "5"  // 최대 5자리 (999.9)
        }
      },
      // ✅ 입력값 검증 및 반올림
      cellEdited: function(cell) {
        let value = cell.getValue();
        if (value !== null && value !== undefined && value !== '') {
          let numValue = parseFloat(value);
          if (!isNaN(numValue)) {
            // ✅ 소수점 1자리로 반올림
            numValue = Math.round(numValue * 10) / 10;
            cell.setValue(numValue);
          }
        }
      },
      validator: [
        "numeric",
        "min:0",
        "max:999.9"
      ],
      formatter: function(cell) {
        const value = cell.getValue();
        if (value === null || value === undefined || value === '') {
          return '';
        }
        const number = parseFloat(value);
        if (isNaN(number)) {
          return value;
        }
        return number.toFixed(1);
      },
      accessorDownload: function(value) {
        if (value === null || value === undefined || value === '') return 0;
        const number = parseFloat(value);
        return isNaN(number) ? 0 : number;
      }
    },
    {
      title: '판촉일',
      field: 'promoDt',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '판촉일';
      },
      formatter: function(cell) {
        // ✅ 클릭 가능한 스타일 적용
        const value = cell.getValue() || '';
        return `
          <div style="
            cursor: pointer;
            color: #0d6efd;
            text-decoration: underline;
            font-weight: 500;
          " 
          class="promo-dt-cell"
          title="클릭하여 상세 보기">
            ${value}
          </div>
        `;
      },
      cellClick: function(e, cell) {
        // ✅ 판촉일 클릭 시 상세 팝업 열기
        const rowData = cell.getRow().getData();
        handlePromoDtClick(rowData);
      },
      accessorDownload: (value) => value ?? ''
    },
    {
      title: '투입일',
      field: 'putDt',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '제품코드(본사)',
      field: 'misCd',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '제품코드<br/>(본사)';  // HTML로 줄바꿈
      }
    },
    {
      title: '제품코드(대리점)',
      field: 'goodsOptionCdOrigin',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '제품코드<br/>(대리점)';  // HTML로 줄바꿈
      }
    },
    {
      title: '단가',
      field: 'unitPrice',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell) {  // ✅ formatter 추가
        const value = cell.getValue();
        return formatNumberWithComma(value);
      },
      // ✅ 엑셀 다운로드 시 숫자로 저장
      accessorDownload: function(value) {
        if (value === null || value === undefined || value === '') return 0;
        const number = parseFloat(value);
        return isNaN(number) ? 0 : number;
      }
    },
    {
      title: '배송요일',
      field: 'weekRemark',
      width: 95,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '계약',
      field: 'orderKind',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible:false
    },
    {
      title: '전화',
      field: 'orderCellPhone',
      width: 105,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '주소',
      field: 'orderAddress1',
      width: 250,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '수정사유',
      field: 'SaveRemark',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '중단일',
      field: 'stopDt',
      width: 90,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '중단사유',
      field: 'stopReason',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '해피콜 조정홉수',
      field: 'hcHob',
      width: 95,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '해피콜<br/>조정홉수';  // HTML로 줄바꿈
      },
      // ✅ 엑셀 다운로드 시 숫자로 저장
      accessorDownload: function(value) {
        if (value === null || value === undefined || value === '') return 0;
        const number = parseFloat(value);
        return isNaN(number) ? 0 : number;
      }
    },
    {
      title: '해피콜 날짜',
      field: 'hcDt',
      width: 90,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '해피콜<br/>날짜';  // HTML로 줄바꿈
      }
    },
    {
      title: '해피콜 결과',
      field: 'hcStatusNm',
      width: 90,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '해피콜<br/>결과';  // HTML로 줄바꿈
      }
    },
    {
      title: '해피콜 상담내용',
      field: 'hcContent',
      width: 200,
      hozAlign: 'left',
      headerHozAlign: 'center',
      formatter: function(cell) {
        const value = cell.getValue() || '';
        // HTML 태그 제거하여 순수 텍스트만 표시
        const cleanText = value.replace(/<[^>]*>/g, '');
        
        // 30자 이상이면 ... 표시
        const displayText = cleanText.length > 30 
          ? cleanText.substring(0, 30) + '...' 
          : cleanText;
        
        return `
          <div style="
            cursor: pointer;
            color: #0d6efd;
            text-decoration: underline;
            padding: 8px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          " 
          class="hc-content-cell"
          title="클릭하여 전체 내용 보기">
            ${displayText || '<span style="color: #999;">내용 없음</span>'}
          </div>
        `;
      },
      cellClick: function(e, cell) {
        const rowData = cell.getRow().getData();
        handleHcContentDetail(rowData);
      }
    },
    {
      title: '담당 해피콜 결과확인',
      field: 'hcActionStatusNm',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible: false,
      titleFormatter: function() {
        return '담당 해피콜<br/>결과확인';  // HTML로 줄바꿈
      }
    },
    {
      title: '담당 의견/대리점 소명',
      field: 'hcAction',
      width: 200,
      hozAlign: 'left',
      headerHozAlign: 'center',
      visible: false,
      formatter: function(cell) {
        const value = cell.getValue() || '';
        // HTML 태그 제거하여 순수 텍스트만 표시
        const cleanText = value.replace(/<[^>]*>/g, '');
        
        // 30자 이상이면 ... 표시
        const displayText = cleanText.length > 30 
          ? cleanText.substring(0, 30) + '...' 
          : cleanText;
        
        return `
          <div style="
            cursor: pointer;
            color: #0d6efd;
            text-decoration: underline;
            padding: 8px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          " 
          class="hc-action-cell"
          title="클릭하여 전체 내용 보기">
            ${displayText || '<span style="color: #999;">내용 없음</span>'}
          </div>
        `;
      },
      cellClick: function(e, cell) {
        const rowData = cell.getRow().getData();
        handleHcActionDetail(rowData);
      }
    },
    {
      title: '해피콜 담당확인 홉수',
      field: 'hcCheckHob',
      width: 110,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible: false,
      titleFormatter: function() {
        return '해피콜 담당<br/>확인 홉수';  // HTML로 줄바꿈
      },
      // ✅ 엑셀 다운로드 시 숫자로 저장
      accessorDownload: function(value) {
        if (value === null || value === undefined || value === '') return 0;
        const number = parseFloat(value);
        return isNaN(number) ? 0 : number;
      }
    },
    {
      title: '담당자',
      field: 'teamPersonNm',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '팀',
      field: 'deptNme',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '대리점코드(대리점)',
      field: 'agencyCd',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '대리점코드<br/>(대리점)';  // HTML로 줄바꿈
      },
    },
    {
      title: '대리점코드MIS',
      field: 'agencyCdMis',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '대리점코드<br/>(본사)';  // HTML로 줄바꿈
      },
    },
    {
      title: '주차',
      field: 'weekNum',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell) {
        // ✅ 현재 선택된 주차 label 표시
        return currentWeekLabel ? currentWeekLabel.substring(0, 3) : '';
      },
      // ✅ 엑셀 다운로드 시에도 동일한 형식으로 출력
      accessorDownload: function(value, data, type, params, column) {
        // 현재 선택된 주차 label의 앞 3글자 반환 (예: "1주차")
        return currentWeekLabel ? currentWeekLabel.substring(0, 3) : '';
      }
    },
    {
      title: '1회투입수량',
      field: 'quantity',
      width: 90,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '1회투입<br/>수량';  // HTML로 줄바꿈
      },
      visible: false
    },
    {
      title: '대리점홉',
      field: 'agencyHob',
      width: 95,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible: false
    },
    {
      title: '본사홉',
      field: 'hqHob',
      width: 85,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible: false
    },
    {
      title: 'duplOrderCd',
      field: 'duplOrderCd',
      visible: false
    },
    {
      title: 'orderCd',
      field: 'orderCd',
      visible: false
    },
    {
      title: 'orderSeq',
      field: 'orderSeq',
      visible: false
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
    const numberFields = ['weekQty', 'contractPeriod', 'actualHob', 'unitPrice', 
                          'hcHob', 'quantity', 'agencyHob', 'hqHob', 'hcCheckHob'];
    
    // 숫자 필드면 numberDownloadFormatter, 아니면 textDownloadFormatter 적용
    if (numberFields.includes(col.field)) {
      col.accessorDownload = numberDownloadFormatter;
    } else {
      col.accessorDownload = textDownloadFormatter;
    }
  });

  // ✅ 한 줄로 모든 컬럼의 정렬 비활성화
  columns.forEach(col => col.headerSort = false);

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
    const fileName = `마감실적관리_${today}.xlsx`;
    
    tabulatorInstance?.current.download("xlsx", fileName, {
      sheetName: "마감실적관리"
    });
  };

  // 조회 버튼 클릭
  const handleSearch = async () => {

    // ✅ 이미 로딩 중이면 중복 실행 방지
    if (isLoading) return;

    try {

      // ✅ 로딩 상태 시작
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
      let [startDate, endDate] = stdWeek.split('|');

      if (subStartDate !== '' || subEndDate !== '') {
        startDate = '';
        endDate = '';
      }

      const tempAgencyCd = sessionStorage.getItem("loginId");

      // const [startDate, endDate] = stdWeek.split('|');
      // 조회 API 호출
      const response = await axios.get('/api/promo/getHappyMilkbangDetailList', {
        params: {
          // 기본 년월주차
          stdYear: stdYear,
          stdMonth: stdMonth,
          stdWeek: stdWeek,
          
          // 날짜 범위 (우선 적용)
          startDate: startDate,
          endDate: endDate,
          
          // 대리점
          agencyCd: tempAgencyCd
        }
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
        setOriginalData([]);  // ✅ 원본 데이터도 초기화
        return;
      }

      setTableData(response.data);
      setOriginalData(JSON.parse(JSON.stringify(response.data)));  // ✅ 원본 데이터도 초기화

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

      // ✅ 로딩 상태 종료
      setIsLoading(false);
    }
  };

  const options = {
    layout: 'fitColumns',
    pagination: false,
    placeholder: '조회된 데이터가 없습니다.',
    height: "calc(100vh - 380px)",
    rowFormatter: function(row) {
      const rowData = row.getData();
      const table = row.getTable();
      const allData = table.getData();
      
      // ✅ 병합 대상 필드
      const mergeFields = ['agencyNm', 'promoTeamNm', 'promoPersonNm', 'orderUserNm', 'goodsOptionNm', 'totStatus'];
      
      // ✅ 같은 orderCd를 가진 행이 여러 개인지 확인
      const groupCount = allData.filter(r => r.orderCd === rowData.orderCd).length;
      
      // ✅ 병합 그룹인 경우에만 처리
      if (groupCount > 1) {
        // 첫 번째 행인지 확인
        const isFirst = isFirstRowInGroup(rowData, allData);
        
        // 마지막 행인지 확인
        const isLast = isLastRowInGroup(rowData, allData);
        
        mergeFields.forEach(field => {
          const cell = row.getCell(field);
          if (cell) {
            const cellElement = cell.getElement();

            if(field === "agencyNm"){
              cellElement.style.borderRight = '0px';
            }

            if(field === "promoTeamNm"){
              cellElement.style.borderRight = '0px';
            }

            if(field === "promoPersonNm"){
              cellElement.style.borderRight = '0px';
            }

            if(field === "orderUserNm"){
              cellElement.style.borderRight = '0px';
            }

            if(field === "totStatus"){
              cellElement.style.borderRight = '2px solid #2f89e4ff';
            }

            if(field === "goodsOptionNm"){
              cellElement.style.borderRight = '2px solid #2f89e4ff';
            }
            // ✅ 첫 번째 행: 위쪽 테두리 진하게
            if (isFirst && field !== "totStatus") {
              cellElement.style.borderTop = '2px solid #2f89e4ff';  // 진한 회색 테두리
            }
            
            // ✅ 마지막 행: 아래쪽 테두리 진하게
            if (isLast && field !== "totStatus") {
              cellElement.style.borderBottom = '2px solid #2f89e4ff';  // 진한 회색 테두리
            }
          }
        });
      }
    }
  };

  return (
    <Container fluid className="mt-1">
      {/* ✅ 판촉실적 상세 모달 추가 */}
      <PromotionDetailModal
        show={showDetailModal}
        onHide={() => setShowDetailModal(false)}
        rowData={selectedRowData}
        originalData={originalData}
        onSave={handleModalSave}
      />

      {/* ✅ 이중기재 모달 추가 */}
      <PromotionDuplModal
        show={showDuplModal}
        onHide={() => setShowDuplModal(false)}
        rowData={selectedDuplRowData}
        originalData={originalData}
      />

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
            마감실적관리
          </h5>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-end">
            {/* 날짜 범위 */}
            <Col md={5} style={{ minWidth: '500px', maxWidth: '500px' }}>
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
        <Card.Header className="bg-light text-dark fw-bold" style={{ padding: '9px 15px' }}>
          <Row className="align-items-center">
            {/* 왼쪽: 제목 */}
            <Col xs="auto">
              <span style={{ fontSize: '16px', fontWeight: 'bold' }}>마감실적관리</span>
            </Col>
            
            {/* 오른쪽: 합계 정보 */}
            <Col>
              <div style={{ 
                display: 'inline-flex',
                alignItems: 'center',
                gap: '10px',
                fontSize: '14px'
              }}>
                <span style={{ 
                  color: '#6c757d', 
                  fontWeight: 'bold'
                }}>
                  |
                </span>
                
                {/* 홉수 정보 */}
                <span style={{ color: '#0d6efd', fontWeight: '500' }}>
                  전체홉수: <strong>{summaryData.totalHob.toFixed(1)}</strong> 홉
                </span>

                <span style={{fontWeight: '500' }}>
                  <strong>{'('}</strong>
                </span>
                
                <span style={{fontWeight: '500' }}>
                  신규: <strong>{summaryData.contractHob.toFixed(1)}</strong> 홉
                </span>

                <span style={{fontWeight: '500' }}>
                  재계약: <strong>{summaryData.reContractHob.toFixed(1)}</strong> 홉
                </span>

                <span style={{fontWeight: '500' }}>
                  무계약: <strong>{summaryData.noContractHob.toFixed(1)}</strong> 홉
                </span>

                <span style={{fontWeight: '500' }}>
                  <strong>{')'}</strong>
                </span>

              </div>
            </Col>
          </Row>
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

export default AgencyMangement;