import { useState, useRef, useEffect } from 'react';
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
import { FaSearch, FaSave, FaPlus, FaKey } from "react-icons/fa";
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

const TeamPersonMng = () => {
  const [selectedTeamPersonNm, setSelectedTeamPersonNm] = useState('');
  const [selectedTeamPersonCd, setSelectedTeamPersonCd] = useState('');
  const [selectedTeamPersonType, setSelectedTeamPersonType] = useState('');
  const [tableData, setTableData] = useState([]);
  const [teamList, setTeamList] = useState([]);
  const [teamOptions, setTeamOptions] = useState({});
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [originalData, setOriginalData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);  // 조회 중 상태
  const isInitialLoadRef = useRef(true);
  const tableRef = useRef(null);
  // 담당자 선택 팝업 관련 state
  const [showTeamPersonModal, setShowTeamPersonModal] = useState(false);  // 팝업 표시 여부
  const [teamPersonList, setTeamPersonList] = useState([]);               // 담당자 목록
  const [currentRowIndex, setCurrentRowIndex] = useState(null);           // 현재 선택된 행 인덱스
  const teamPersonModalTableRef = useRef(null);                           // 팝업 테이블 참조

  useEffect(() => {
    fetchTeamList();
  }, []); // 빈 배열: 컴포넌트 마운트 시 1회만 실행

  useEffect(() => {
    fetchTeamPersonList();
  });

  // 초기 로드 시 1회만 자동 조회
  useEffect(() => {
    if (isInitialLoad && teamPersonList.length > 0 && teamList.length > 0) {
      handleSearch();
      setIsInitialLoad(false);
    }
  }, [isInitialLoad, teamPersonList, teamList]);

  const fetchTeamList = async () => {
    try {
      // API 호출
      const response = await axios.get('/api/setting/getTeamList');
      
      // 응답 데이터 확인 (디버깅용)
      console.log('팀 목록 조회 결과:', response.data);
      
      // state에 원본 데이터 저장
      setTeamList(response.data || []);
      
      // ✅ editorParams의 values 형태로 변환
      // 형태: { "01": "영업1팀", "02": "영업2팀", ... }
      const options = {};
      
      // 빈 옵션 추가 (선택 안 함)
      options[''] = '- 선택 -';
      
      // 각 팀을 순회하며 옵션 객체 생성
      response.data.forEach(team => {
        options[team.teamCd] = team.teamNm;
      });
      
      // 변환된 옵션 객체를 state에 저장
      setTeamOptions(options);
      
      console.log('팀 옵션 변환 결과:', options);
      
    } catch (error) {
      console.error('팀 목록 조회 실패:', error);
      
      // 에러 알림
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '팀 목록 조회에 실패했습니다.',
        confirmButtonText: '확인'
      });
      
      // 에러 시 빈 배열/객체로 초기화
      setTeamList([]);
      setTeamOptions({ '': '- 선택 -' });
    }
  };

  // 담당자 목록 조회 함수
  const fetchTeamPersonList = async () => {
    try {
      const response = await axios.get('/api/promo/getAllTeamPerson');  // API 엔드포인트 수정 필요
      
      // API 응답 구조에 따라 수정
      // 예: response.data 또는 response.data.data
      setTeamPersonList(response.data);
      
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

  /**
   * ✅ 두 행 데이터를 비교하여 변경 여부를 판단하는 함수
   * @param {Object} original - 원본 행 데이터
   * @param {Object} current - 현재 행 데이터
   * @returns {boolean} 변경 여부 (true: 변경됨, false: 변경 안됨)
   */
  const isRowChanged = (original, current) => {
    // ✅ 비교할 필드 목록 (편집 가능한 필드들)
    const fieldsToCompare = [
      'teamPersonNm',    // 사원명
      'loginId',         // 로그인ID
      'loginPw',         // 비밀번호
      'teamCd',          // 소속팀코드
      'managerYn',       // 관리자여부
      'agencyYn',        // 대리점여부
      'teamPersonType'   // 직원타입
    ];

    // ✅ 각 필드를 비교하여 하나라도 다르면 변경된 것으로 판단
    for (const field of fieldsToCompare) {
      const originalValue = original[field];
      const currentValue = current[field];

      // null, undefined, 빈 문자열을 동일하게 취급
      const normalizedOriginal = (originalValue === null || originalValue === undefined || originalValue === '') 
        ? '' 
        : String(originalValue).trim();
      const normalizedCurrent = (currentValue === null || currentValue === undefined || currentValue === '') 
        ? '' 
        : String(currentValue).trim();

      if (normalizedOriginal !== normalizedCurrent) {
        console.log(`필드 '${field}' 변경 감지:`, {
          원본: normalizedOriginal,
          현재: normalizedCurrent
        });
        return true;  // 변경됨
      }
    }

    return false;  // 변경 안됨
  };

  /**
   * ✅ 변경된 데이터만 추출하는 함수
   * @param {Array} originalData - 원본 데이터 배열
   * @param {Array} currentData - 현재 데이터 배열
   * @returns {Array} 변경된 행들의 배열
   */
  const getChangedData = (originalData, currentData) => {
    const changedRows = [];

    // ✅ 각 행의 고유 식별자 (teamPersonCd)를 사용하여 매칭
    currentData.forEach(currentRow => {
      // 새로 추가된 행은 무조건 변경된 것으로 간주
      if (currentRow.isNew === true) {
        changedRows.push({
          ...currentRow,
          changeType: 'INSERT'  // 신규 추가 표시
        });
        return;
      }

      // 원본 데이터에서 동일한 teamPersonCd를 가진 행 찾기
      const originalRow = originalData.find(
        row => row.teamPersonCd === currentRow.teamPersonCd
      );

      // ✅ 원본 데이터에 해당 행이 있고, 변경되었다면 추가
      if (originalRow && isRowChanged(originalRow, currentRow)) {
        changedRows.push({
          ...currentRow,
          changeType: 'UPDATE'  // 수정 표시
        });
      }
    });

    console.log('=== 변경 감지 결과 ===');
    console.log('전체 행 수:', currentData.length);
    console.log('변경된 행 수:', changedRows.length);
    console.log('변경된 데이터:', changedRows);

    return changedRows;
  };

  const handleTeamPersonSelect = (selectedPerson) => {
    if (currentRowIndex === null || !tabulatorInstance?.current) {
      return;
    }

    // 테이블의 해당 행 데이터 업데이트
    const rows = tabulatorInstance.current.getRows();
    const targetRow = rows[currentRowIndex];
    
    if (targetRow) {
      // ✅ 담당코드와 담당자명을 동시에 업데이트
      targetRow.update({
        teamPersonCd: selectedPerson.teamPersonCd,
        teamPersonNm: selectedPerson.teamPersonNm,
        isTeamPersonChanged: true  // ✅ 변경 플래그 추가
      });
      
      // state도 함께 업데이트
      setTableData(prev => {
        const newData = [...prev];
        newData[currentRowIndex] = {
          ...newData[currentRowIndex],
          teamPersonCd: selectedPerson.teamPersonCd,
          teamPersonNm: selectedPerson.teamPersonNm,
          isTeamPersonChanged: true  // ✅ 변경 플래그 추가
        };
        return newData;
      });
    }
    
    // 팝업 닫기
    setShowTeamPersonModal(false);
    setCurrentRowIndex(null);
  };

  /**
   * ✅ 비밀번호 초기화 함수
   * - 선택된 행의 비밀번호를 '1234'로 초기화
   */
  const handleResetPassword = async () => {
    if (!tabulatorInstance?.current) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '테이블이 준비되지 않았습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 선택된 행 가져오기
    const selectedRows = tabulatorInstance.current.getSelectedRows();

    if (selectedRows.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '비밀번호를 초기화할 사원을 선택해주세요.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 선택된 사원 정보 추출
    const selectedData = selectedRows.map(row => row.getData());
    const selectedNames = selectedData.map(data => data.teamPersonNm || '(이름없음)').join(', ');

    // ✅ 확인 메시지
    const result = await Swal.fire({
      title: '비밀번호 초기화',
      html: `
        <div style="text-align: left;">
          <p>선택된 <strong>${selectedRows.length}명</strong>의 비밀번호를 초기화하시겠습니까?</p>
          <p style="color: #6c757d; font-size: 14px;">대상: ${selectedNames}</p>
          <p style="color: #dc3545; font-weight: bold;">초기화 비밀번호: 1234</p>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: '초기화',
      cancelButtonText: '취소',
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d'
    });

    if (!result.isConfirmed) {
      return;
    }

    try {
      // ✅ 로딩 표시
      Swal.fire({
        title: '처리 중...',
        html: '비밀번호를 초기화하고 있습니다.',
        allowOutsideClick: false,
        allowEscapeKey: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // ✅ 초기화할 사원코드 목록
      const teamPersonCdList = selectedData
        .filter(data => data.teamPersonCd)  // 신규 행 제외 (사원코드 있는 것만)
        .map(data => data.teamPersonCd);

      if (teamPersonCdList.length === 0) {
        Swal.fire({
          icon: 'warning',
          title: '알림',
          text: '저장되지 않은 신규 사원은 비밀번호 초기화가 불가능합니다.',
          confirmButtonText: '확인'
        });
        return;
      }

      // ✅ API 호출 (비밀번호 초기화)
      const response = await axios.post('/api/setting/resetTeamPersonPassword', {
        teamPersonCdList: teamPersonCdList
      });

      // ✅ 성공 처리
      Swal.fire({
        icon: 'success',
        title: '초기화 완료',
        text: `${teamPersonCdList.length}명의 비밀번호가 '1234'로 초기화되었습니다.`,
        confirmButtonText: '확인'
      });

      // ✅ 선택 해제
      tabulatorInstance.current.deselectRow();

    } catch (error) {
      console.error('비밀번호 초기화 실패:', error);

      const errorMessage = error.response?.data?.message ||
                          error.response?.data?.error ||
                          '비밀번호 초기화에 실패했습니다.';

      Swal.fire({
        icon: 'error',
        title: '초기화 실패',
        text: errorMessage,
        confirmButtonText: '확인'
      });
    }
  };

  // ✅ 담당자 선택 팝업의 컬럼 정의
  const teamPersonColumns = [
    {
      title: 'No',
      field: 'no',
      width: 70,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell) {
        return cell.getRow().getPosition();
      }
    },
    {
      title: '담당코드',
      field: 'teamPersonCd',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '담당자명',
      field: 'teamPersonNm',
      width: 150,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '선택',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      headerSort: false,
      formatter: function(cell) {
      // ✅ 버튼 스타일 최적화
      return `
        <button 
          class="btn btn-primary btn-sm team-person-select-btn"
          style="
            padding: 0px 0px;
            font-size: 12px;
            white-space: nowrap;
            min-width: 40px;
            max-width: 50px;
          ">
          선택
        </button>
      `;
    },
      cellClick: function(e, cell) {
        const rowData = cell.getRow().getData();
        handleTeamPersonSelect(rowData);
      }
    }
  ];

  const openTeamPersonModal = async (rowIndex) => {
    setCurrentRowIndex(rowIndex);  // 현재 행 인덱스 저장
    setShowTeamPersonModal(true);  // 팝업 표시
    
    // 담당자 목록 조회 API 호출
    try {
      const response = await axios.get('/api/promo/getAllTeamPerson');
      setTeamPersonList(response.data || []);
    } catch (error) {
      console.error('담당자 목록 조회 실패:', error);
      Swal.fire({
        icon: 'error',
        title: '오류',
        text: '담당자 목록 조회에 실패했습니다.',
        confirmButtonText: '확인'
      });
      setTeamPersonList([]);
    }
  };

  const handleAddRow = () => {
    if (!tabulatorInstance?.current) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '테이블이 준비되지 않았습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // 새로운 행 데이터 생성
    const newRow = {
      no: tableData.length + 1,
      managerYn: '0',
      teamPersonType: '1',
      isNew: true  // ✅ 새로 추가된 행 구분용 플래그
    };

    // 테이블 맨 위에 행 추가
    tabulatorInstance.current.addRow(newRow, true);
    
    // state도 업데이트
    setTableData(prev => [newRow, ...prev]);
  };

  // 테이블 컬럼 정의
  const columns = [
    {
      formatter: "rowSelection",
      titleFormatter: "rowSelection",
      hozAlign: "center",
      width: 50,
      download: false,  // ✅ 엑셀 다운로드 시 제외
      // ✅ 셀 클릭 시 체크박스 토글
      cellClick: function(e, cell) {
        // 이미 체크박스를 직접 클릭한 경우는 제외
        if (e.target.type === 'checkbox') {
          return;
        }
        
        const row = cell.getRow();
        
        // 현재 선택 상태 확인
        if (row.isSelected()) {
          row.deselect();
        } else {
          row.select();
        }
      }
    },
    {
      title: 'No',
      field: 'no',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell, formatterParams, onRendered) {
        // 현재 행의 위치를 가져와서 1부터 시작하는 순번 반환
        return cell.getRow().getPosition();
      }
    },
    {
      title: '사원코드',
      field: 'teamPersonCd',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      editable: false
    },
    {
      title: '사원명',
      field: 'teamPersonNm',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input'
    },
    {
      title: '로그인ID(MIS사번)',
      field: 'loginId',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      titleFormatter: function() {
        return '로그인ID<br/>(MIS사번)';  // HTML로 줄바꿈
      },
    },
    {
      title: '비밀번호',
      field: 'loginPw',
      width: 150,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      visible: false
    },
    {
      title: 'MIS 코드',
      field: 'empyId',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: 'MIS 사원명',
      field: 'empyNme',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: 'MIS 부서코드',
      field: 'deptCde',
      width: 110,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return 'MIS<br/>부서코드';  // HTML로 줄바꿈
      },
    },
    {
      title: 'MIS 부서명',
      field: 'deptNme',
      width: 150,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '관리자여부',
      field: 'managerYn',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'list',
      editorParams: {
        values: {
          "0": "관리자X",
          "1": "관리자O"
        }
      },
      // ✅ formatter 함수: 0이면 "사용", 1이면 "미사용" 표시 (미사용은 빨간색)
      formatter: function(cell) {
        const value = cell.getValue();
        
        // null, undefined 체크
        if (value === null || value === undefined) {
          return '';
        }
        
        // 문자열 또는 숫자로 변환하여 비교
        const stringValue = String(value);
        const displayValue = stringValue === '0' ? '관리자X' : '관리자O';
        
        // 미사용인 경우 빨간색 강조
        if (displayValue === '관리자O') {
          cell.getElement().style.color = '#1014eeff';
          cell.getElement().style.fontWeight = 'bold';
        } else {
          // 사용인 경우 기본 스타일로 복원
          cell.getElement().style.color = '';
          cell.getElement().style.fontWeight = '';
        }
        
        return displayValue;
      }
    },
    {
      title: '직원타입',
      field: 'teamPersonType',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'list',
      editorParams: {
        values: {
          "0": "없음",
          "1": "담당자O",
          "2": "대리점",
          "3": "판촉팀장",
          "4": "해피콜"
        }
      },
      // ✅ formatter 추가: key를 value로 변환하여 표시
      formatter: function(cell) {
        const value = cell.getValue();
        const valueMap = {
          "0": "없음",
          "1": "담당자O",
          "2": "대리점",
          "3": "판촉팀장",
          "4": "해피콜"
        };
        return valueMap[value] || value || "";
      }
    },
    {
      title: '대리점여부',
      field: 'agencyYn',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible:false,
      editor: 'list',
      editorParams: {
        values: {
          "0": "대리점X",
          "1": "대리점O"
        }
      },
      // ✅ formatter 함수: 0이면 "사용", 1이면 "미사용" 표시 (미사용은 빨간색)
      formatter: function(cell) {
        const value = cell.getValue();
        
        // null, undefined 체크
        if (value === null || value === undefined) {
          return '';
        }
        
        // 문자열 또는 숫자로 변환하여 비교
        const stringValue = String(value);
        const displayValue = stringValue === '0' ? '대리점X' : '대리점O';
        
        // 미사용인 경우 빨간색 강조
        if (displayValue === '대리점O') {
          cell.getElement().style.color = '#1014eeff';
          cell.getElement().style.fontWeight = 'bold';
        } else {
          // 사용인 경우 기본 스타일로 복원
          cell.getElement().style.color = '';
          cell.getElement().style.fontWeight = '';
        }
        
        return displayValue;
      }
    }
  ];

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
    const fileName = `대리점관리_${today}.xlsx`;
    
    tabulatorInstance?.current.download("xlsx", fileName, {
      sheetName: "대리점관리"
    });
  };

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

      // 조회 API 호출
      const response = await axios.get('/api/setting/getTeamPersonList', {
        params: {
          teamPersonCd : selectedTeamPersonCd,
          teamPersonNm : selectedTeamPersonNm,
          teamPersonType : selectedTeamPersonType
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

      // ✅ 원본 데이터를 깊은 복사로 저장 (참조 문제 방지)
      setOriginalData(JSON.parse(JSON.stringify(response.data)));

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

  /**
   * ✅ 저장 버튼 클릭 핸들러
   */
  const handleSave = async () => {
    
    // ✅ 1. 현재 테이블의 모든 데이터 가져오기
    const currentData = tabulatorInstance?.current?.getData() || [];

    if (currentData.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '알림',
        text: '저장할 데이터가 없습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 2. 유효성 검사 (신규 행만 검증)
    const invalidRows = currentData.filter(row => {
      if (row.isNew) {
        // 사원명이 비어있는 경우
        if (!row.teamPersonNm || row.teamPersonNm.trim() === '') {
          return true;
        }
      }
      return false;
    });

    if (invalidRows.length > 0) {
      // ✅ 구체적인 오류 메시지 생성
      const errors = [];
      invalidRows.forEach(row => {
        if (!row.teamPersonNm || row.teamPersonNm.trim() === '') {
          errors.push(`사원명이 비어있습니다.`);
        }
      });
      
      Swal.fire({
        icon: 'error',
        title: '저장 실패',
        html: `
          <div style="text-align: left;">
            <p><strong>다음 항목을 확인해주세요:</strong></p>
            <ul>
              ${[...new Set(errors)].map(err => `<li>${err}</li>`).join('')}
            </ul>
          </div>
        `,
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 3. 변경된 데이터만 추출
    const changedData = getChangedData(originalData, currentData);

    // ✅ 4. 변경된 데이터가 없는 경우
    if (changedData.length === 0) {
      Swal.fire({
        icon: 'info',
        title: '알림',
        text: '변경된 데이터가 없습니다.',
        confirmButtonText: '확인'
      });
      return;
    }

    // ✅ 5. 저장 확인 메시지
    const result = await Swal.fire({
      title: '저장 확인',
      html: `
        <div style="text-align: left;">
          <p><strong>저장할 데이터:</strong></p>
          <ul>
            <li>신규: ${changedData.filter(row => row.changeType === 'INSERT').length}건</li>
            <li>수정: ${changedData.filter(row => row.changeType === 'UPDATE').length}건</li>
          </ul>
          <p>총 <strong>${changedData.length}건</strong>을 저장하시겠습니까?</p>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: '저장',
      cancelButtonText: '취소',
      confirmButtonColor: '#28a745',
      cancelButtonColor: '#6c757d'
    });

    if (!result.isConfirmed) {
      return;
    }

    // ✅ 6. 서버에 저장 요청
    try {
      // 로딩 표시
      Swal.fire({
        title: '저장 중...',
        html: '데이터를 저장하고 있습니다.',
        allowOutsideClick: false,
        allowEscapeKey: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // ✅ API 호출 (사원 관리 엔드포인트)
      const response = await axios.post('/api/setting/saveTeamPersonList', changedData);

      // ✅ 7. 저장 성공 처리
      Swal.fire({
        icon: 'success',
        title: '저장 완료',
        text: `${changedData.length}건의 데이터가 성공적으로 저장되었습니다.`,
        confirmButtonText: '확인'
      });

      // ✅ 8. 저장 후 데이터 다시 조회 (최신 상태 반영)
      await handleSearch();

    } catch (error) {
      console.error('저장 실패:', error);
      
      // ✅ 에러 메시지 추출
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          '데이터 저장에 실패했습니다.';
      
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
    height: "calc(100vh - 390px)"
  };

  const modalOptions = {
    layout: 'fitColumns',
    pagination: false,
    placeholder: '조회된 데이터가 없습니다.',
    height: '500px'
  };

  return (
    <Container fluid className="mt-1">
      {/* 제목 */}
      <Row className="mb-1">
        <Col>
          <h5>
            <i className="bi bi-circle-fill text-warning me-1"></i>
            <CiViewList size={22} />
            사원 관리
          </h5>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-end">

            {/* 대리점 입력 */}
            <Col md={4} style={{ minWidth: '350px', maxWidth: '350px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    사원코드 :
                  </Form.Label>
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedTeamPersonCd}
                    onChange={(e) => setSelectedTeamPersonCd(e.target.value)}
                    placeholder="사원코드 입력"
                    style={{ width: '120px' }}
                  />
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedTeamPersonNm}
                    onChange={(e) => setSelectedTeamPersonNm(e.target.value)}
                    placeholder="사원명 입력"
                    style={{ width: '120px' }}
                  />
                </div>
              </Form.Group>
            </Col>

            <Col md={2} style={{ minWidth: '250px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    구분 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedTeamPersonType}
                    onChange={(e) => setSelectedTeamPersonType(e.target.value)}
                    style={{ width: '130px' }}
                  >
                    <option value="">전체</option>
                    <option value="1">담당자</option>
                    <option value="2">대리점</option>
                    <option value="3">판촉팀장</option>
                    <option value="4">해피콜</option>
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

            {/* 저장 버튼 */}
            <Col md={1} style={{ minWidth: '100px', maxWidth: '100px' }}>
              <Button
                variant="success"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleSave}
                disabled={isLoading}
              >
                <FaSave /> 저장
              </Button>
            </Col>

            {/* ✅ 비밀번호 초기화 버튼 */}
            <Col md={2} style={{ minWidth: '170px', maxWidth: '170px' }}>
              <Button
                variant="warning"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleResetPassword}
                disabled={isLoading}
              >
                <FaKey /> 비밀번호 초기화
              </Button>
            </Col>

            {/* 엑셀 버튼 */}
            <Col md={2} style={{ minWidth: '160px', maxWidth: '160px' }}>
              <Button
                variant="secondary"
                size="sm"
                className="w-100"
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
          {/* 행추가/행삭제 버튼 그룹 */}
          <Row className="align-items-center">
            <Col xs="auto">
              <span style={{ fontSize: '16px', fontWeight: 'bold' }}>사원 관리</span>
            </Col>
            <Col className="d-flex align-items-center gap-2">
              <Button
                variant="success"
                size="sm"
                onClick={handleAddRow}
                className="d-flex align-items-center gap-1"
              >
                <FaPlus size={12} /> 행추가
              </Button>
              {/* ✅ 초기비밀번호 안내 문구 */}
              <span 
                className="text-muted" 
                style={{ 
                  fontSize: '13px', 
                  fontStyle: 'italic',
                  color: '#6c757d'
                }}
              >
                (초기비밀번호: 1234)
              </span>
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

export default TeamPersonMng;