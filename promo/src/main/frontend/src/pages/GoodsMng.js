import React, { useState, useRef, useEffect } from 'react';
import {
  Container,
  Row,
  Col,
  Form,
  Button,
  Card,
  Modal
} from 'react-bootstrap';
import { ReactTabulator } from 'react-tabulator';
import 'tabulator-tables/dist/css/tabulator.min.css';
import 'tabulator-tables/dist/css/tabulator_bootstrap4.min.css';
import 'bootstrap/dist/css/bootstrap.min.css'; // Bootstrap CSS import (npm 설치 시)
import { CiViewList} from "react-icons/ci";
import { FaSearch, FaSave, FaPlus, FaTrashAlt } from "react-icons/fa";
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

const GoodsMng = () => {
  const [selectedOptionNm, setSelectedOptionNm] = useState('');
  const [selectedGoodsOptionCd, setSelectedGoodsOptionCd] = useState('');
  const [selectedMisNm, setSelectedMisNm] = useState('');
  const [selectedMisCd, setSelectedMisCd] = useState('');
  const [selectedDeleteYn, setSelectedDeleteYn] = useState(0);  // ✅ 미사용 포함 여부
  const [agencyList, setAgencyList] = useState([]);  // 대리점 목록 state 추가
  const [tableData, setTableData] = useState([]);
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [originalData, setOriginalData] = useState([]);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [isLoading, setIsLoading] = useState(false);  // 조회 중 상태
  const tableRef = useRef(null);

  useEffect(() => {
    fetchAgencyList();
  });

  // 초기 로드 시 1회만 자동 조회
    useEffect(() => {
      if (isInitialLoad && agencyList.length > 0) {
        handleSearch();
        setIsInitialLoad(false);
      }
    }, [isInitialLoad, agencyList]);

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

  /**
   * ✅ 두 행 데이터를 비교하여 변경 여부를 판단하는 함수
   * @param {Object} original - 원본 행 데이터
   * @param {Object} current - 현재 행 데이터
   * @returns {boolean} 변경 여부 (true: 변경됨, false: 변경 안됨)
   */
  const isRowChanged = (original, current) => {
    // ✅ 비교할 필드 목록 (편집 가능한 필드들)
    // 새로 추가된 행(isNew)은 이 함수를 거치지 않으므로 goodsOptionCd는 제외
    const fieldsToCompare = [
      'optionNm',        // 제품명(대리점)
      'misCd',           // 제품코드연결(본사)
      'day1',            // day1
      'day2',            // day2
      'day3',            // day3
      'day4',            // day4
      'day5',            // day5
      'day6',            // day6
      'deleteYn'         // 사용여부
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

    // ✅ 각 행의 고유 식별자 (goodsOptionCd)를 사용하여 매칭
    currentData.forEach(currentRow => {
      // 새로 추가된 행은 무조건 변경된 것으로 간주
      if (currentRow.isNew === true) {
        changedRows.push({
          ...currentRow,
          changeType: 'INSERT'  // 신규 추가 표시
        });
        return;
      }

      // 원본 데이터에서 동일한 goodsOptionCd를 가진 행 찾기
      const originalRow = originalData.find(
        row => row.goodsOptionCd === currentRow.goodsOptionCd
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
      goodsOptionCd: '',
      optionNm: '',
      misCd: "",
      day1: "0",
      day2: "0",
      day3: "0",
      day4: "0",
      day5: "0",
      day6: "0",
      deleteYn: '0',
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
      title: 'No',
      field: 'no',
      width: 70,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '제품코드(대리점)',
      field: 'goodsOptionCd',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      editorParams: {
        elementAttributes: {
          inputMode: "numeric",  // 모바일에서 숫자 키패드 표시
          pattern: "[0-9]*"      // 숫자만 입력 가능 (브라우저 힌트)
        }
      },
      // ✅ 새로 추가된 행(isNew=true)에서만 편집 가능
      editable: function(cell) {
        const rowData = cell.getRow().getData();
        return rowData.isNew === true;
      },
      titleFormatter: function() {
        return '제품코드<br/>(대리점)';
      },
      // ✅ 입력값 유효성 검사 - 숫자만 허용
      cellEdited: function(cell) {
        const value = cell.getValue();
        
        // 1. 빈 값 체크
        if (!value || value.trim() === '') {
          Swal.fire({
            icon: 'warning',
            title: '입력 오류',
            text: '제품코드는 필수 입력 항목입니다.',
            confirmButtonText: '확인'
          });
          // 빈 값으로 초기화
          cell.setValue('');
          return;
        }
        
        // 2. 숫자가 아닌 문자 제거
        const cleanedValue = value.replace(/\D/g, '');
        
        // 3. 숫자가 아닌 문자가 포함되어 있었다면 경고 후 정제된 값으로 업데이트
        if (value !== cleanedValue) {
          Swal.fire({
            icon: 'warning',
            title: '입력 오류',
            text: '제품코드는 숫자만 입력 가능합니다.',
            confirmButtonText: '확인'
          });
          cell.setValue(cleanedValue);
          return;
        }
        
        // 4. 정제 후 빈 값이 된 경우 (숫자가 하나도 없었던 경우)
        if (cleanedValue === '') {
          Swal.fire({
            icon: 'warning',
            title: '입력 오류',
            text: '제품코드는 숫자만 입력 가능합니다.',
            confirmButtonText: '확인'
          });
          cell.setValue('');
          return;
        }
      }
    },
    {
      title: '제품명(대리점)',
      field: 'optionNm',
      width: 250,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      titleFormatter: function() {
        return '제품명<br/>(대리점)';  // HTML로 줄바꿈
      },
    },
    {
      title: '제품코드연결(대리점)',
      field: 'misCd',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      visible: false,
      titleFormatter: function() {
        return '제품코드연결<br/>(대리점)';  // HTML로 줄바꿈
      },
    },
    {
      title: '제품코드(본사)',
      field: 'matcode',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '제품코드<br/>(본사)';  // HTML로 줄바꿈
      },
    },
    {
      title: '제품명(본사)',
      field: 'misNm',
      width: 300,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '제품명<br/>(본사)';  // HTML로 줄바꿈
      },
    },
    {
      title: 'day1',
      field: 'day1',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input'
    },
    {
      title: 'day2',
      field: 'day2',
      width: 80,
      hozAlign: 'center',
      editor: 'input',
      headerHozAlign: 'center'
    },
    {
      title: 'day3',
      field: 'day3',
      width: 80,
      hozAlign: 'center',
      editor: 'input',
      headerHozAlign: 'center'
    },
    {
      title: 'day4',
      field: 'day4',
      width: 80,
      hozAlign: 'center',
      editor: 'input',
      headerHozAlign: 'center'
    },
    {
      title: 'day5',
      field: 'day5',
      width: 80,
      hozAlign: 'center',
      editor: 'input',
      headerHozAlign: 'center'
    },
    {
      title: 'day6',
      field: 'day6',
      width: 80,
      hozAlign: 'center',
      editor: 'input',
      headerHozAlign: 'center'
    },
    {
      title: '사용여부',
      field: 'deleteYn',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'list',
      editorParams: {
        values: {
          "0": "사용",
          "1": "미사용"
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
        const displayValue = stringValue === '0' ? '사용' : '미사용';
        
        // 미사용인 경우 빨간색 강조
        if (displayValue === '미사용') {
          cell.getElement().style.color = '#ee1010ff';
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
    const numberFields = ['day1', 'day2', 'day3', 'day4', 'day5', 'day6'];
    
    // 숫자 필드면 numberDownloadFormatter, 아니면 textDownloadFormatter 적용
    if (numberFields.includes(col.field)) {
      col.accessorDownload = numberDownloadFormatter;
    } else {
      col.accessorDownload = textDownloadFormatter;
    }
  });

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
    const fileName = `판촉제품관리${today}.xlsx`;
    
    tabulatorInstance?.current.download("xlsx", fileName, {
      sheetName: "판촉제품관리"
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
      const response = await axios.get('/api/setting/getGoodsList', {
        params: {
          optionNm : selectedOptionNm,
          goodsOptionCd : selectedGoodsOptionCd,
          misNm : selectedMisNm,
          misCd : selectedMisCd,
          deleteYn : selectedDeleteYn
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

    // ✅ 2. 유효성 검사 - 제품코드 필수 입력 및 숫자 검사
    const invalidRows = currentData.filter(row => {
      if (row.isNew) {  // 새로 추가된 행만 검사
        const goodsOptionCd = row.goodsOptionCd;
        
        // 빈 값 체크
        if (!goodsOptionCd || goodsOptionCd.trim() === '') {
          return true;
        }
        
        // 숫자가 아닌 문자가 포함된 경우
        if (!/^\d+$/.test(goodsOptionCd)) {
          return true;
        }
      }
      return false;
    });

    if (invalidRows.length > 0) {
      // ✅ 에러 메시지 상세화
      const hasEmptyCode = invalidRows.some(row => !row.goodsOptionCd || row.goodsOptionCd.trim() === '');
      const hasNonNumeric = invalidRows.some(row => row.goodsOptionCd && !/^\d+$/.test(row.goodsOptionCd));
      
      let errorMessage = '제품코드 입력 오류:\n';
      if (hasEmptyCode) {
        errorMessage += '- 제품코드가 입력되지 않은 행이 있습니다.\n';
      }
      if (hasNonNumeric) {
        errorMessage += '- 제품코드는 숫자만 입력 가능합니다.\n';
      }
      
      Swal.fire({
        icon: 'error',
        title: '저장 실패',
        text: errorMessage,
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

      // ✅ API 호출 (제품 관리용 엔드포인트)
      const response = await axios.post('/api/setting/saveGoodsList', changedData);

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
      
      // 에러 메시지 추출
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
            판촉 제품 관리
          </h5>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-end">

            {/* 대리점 입력 */}
            <Col md={3} style={{ minWidth: '460px', maxWidth: '460px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    제품(대리점) :
                  </Form.Label>
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedOptionNm}
                    onChange={(e) => setSelectedOptionNm(e.target.value)}
                    placeholder="제품명(대리점) 입력"
                    style={{ width: '180px' }}
                  />
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedGoodsOptionCd}
                    onChange={(e) => setSelectedGoodsOptionCd(e.target.value)}
                    placeholder="제품코드(대리점) 입력"
                    style={{ width: '150px' }}
                  />
                </div>
              </Form.Group>
            </Col>

            {/* 대리점 입력 */}
            <Col md={3} style={{ minWidth: '460px', maxWidth: '460px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    제품(대리점) :
                  </Form.Label>
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedMisNm}
                    onChange={(e) => setSelectedMisNm(e.target.value)}
                    placeholder="제품명(본사) 입력"
                    style={{ width: '180px' }}
                  />
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedMisCd}
                    onChange={(e) => setSelectedMisCd(e.target.value)}
                    placeholder="제품코드(본사) 입력"
                    style={{ width: '150px' }}
                  />
                </div>
              </Form.Group>
            </Col>

            {/* 담당자 입력 */}
            <Col md={2} style={{ minWidth: '250px', maxWidth: '250px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    미사용 :
                  </Form.Label>
                  <Form.Select
                    size="sm"
                    value={selectedDeleteYn}
                    onChange={(e) => setSelectedDeleteYn(e.target.value)}
                    style={{ width: '130px' }}
                  >
                    <option value="0">미포함</option>
                    <option value="1">포함</option>
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
              <span style={{ fontSize: '16px', fontWeight: 'bold' }}>판촉 제품 관리</span>
            </Col>
            <Col>
              <Button
                variant="success"
                size="sm"
                onClick={handleAddRow}
                className="d-flex align-items-center gap-1"
                disabled={isLoading}
              >
                <FaPlus size={12} /> 행추가
              </Button>
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

export default GoodsMng;