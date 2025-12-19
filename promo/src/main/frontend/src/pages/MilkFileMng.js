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
import { RiFileExcel2Line } from "react-icons/ri";
import '../styles/MilkFileMng.css';
import axios from 'axios';  // axios import 추가
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';  // 이 줄 추가
import Select from 'react-select';

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

// 현재 월의 1일 구하기 (로컬 시간대)
const getFirstDayOfMonth = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}-01`;
};

// 파일 다운로드 함수
const handleFileDownload = async (rowData) => {
  try {

    // axios로 파일 다운로드
    const response = await axios.get('/api/promo/downloadFile', {
      params: { 
        fileName: rowData.fileNm,
        agencyCode: rowData.agencyCd 
      },
      responseType: 'blob'  // 중요: blob 타입으로 받기
    });

    // Blob을 이용한 파일 다운로드
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.download = rowData.fileNm;  // 다운로드될 파일명
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

  } catch (error) {
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '파일 다운로드 실패.',
        confirmButtonText: '확인'
      });
    
    if (error.response?.status === 404) {
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '파일을 찾을 수 없습니다.',
        confirmButtonText: '확인'
      });
    } else {
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '파일 다운로드에 실패했습니다.',
        confirmButtonText: '확인'
      });
    }
  }
};


const MilkFileMng = () => {
  const [startDate, setStartDate] = useState(getFirstDayOfMonth());
  const [endDate, setEndDate] = useState(getTodayDate());
  const [selectedAgency, setSelectedAgency] = useState('');
  const [tableData, setTableData] = useState([]);
  const [agencyList, setAgencyList] = useState([]);  // 대리점 목록 state 추가
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [isLoading, setIsLoading] = useState(false);  // 조회 중 상태
  const tableRef = useRef(null);
  const fileInputRef = useRef(null);
  const uploadRowDataRef = useRef(null);

  // 컴포넌트 마운트 시 대리점 목록 조회
  useEffect(() => {
    fetchAgencyList();
    //handleSearch();
  }, []);

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
      title: '파일명',
      field: 'fileNm',
      width: 250,
      hozAlign: 'left',
      headerHozAlign: 'center',
      formatter: function(cell) {
        const value = cell.getValue();
        return `<span style="color: #0066cc; cursor: pointer;" 
                      class="file-download-link">${value}</span>`;
      },
      cellClick: function(e, cell) {
        handleFileDownload(cell.getRow().getData());
      }
    },
    {
      title: '대리점 전송일',
      field: 'downloadDt',
      width: 180,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '업로드여부',
      field: 'uploadYnNm',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '업로드일',
      field: 'uploadDt',
      width: 180,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '비고',
      field: 'fileStatusNm',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      vertAlign: 'middle',
      formatter: function(cell) {
        const value = cell.getValue();
        if (value === '정상파일') {
          return `<span style="color: blue; cursor: pointer;">${value}</span>`;
        } else if (value === '파일내용없음' || value === '파일깨짐') {
          return `<span style="color: red; cursor: pointer;">${value}</span>`;
        } else {
          return `<span style="color: #ff8c00; cursor: pointer;">${value}</span>`;
        }
        
      },
      cellClick: function(e, cell) {
        const value = cell.getValue();
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
      const response = await axios.post('/api/promo/uploadMilkbangFile', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

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
    const fileName = `밀크방파일관리_${today}.xlsx`;
    
    tabulatorInstance?.current.download("xlsx", fileName, {
      sheetName: "밀크방파일관리"
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
      const response = await axios.get('/api/promo/getMilkbangFileList', {
        params: { startDate : startDate,
                  endDate : endDate,
                  agencyCd: selectedAgency }
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
            밀크방 파일 관리
          </h5>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-2">
        <Card.Body className="py-2">
          <Row className="align-items-end">
            {/* 날짜 범위 */}
            <Col md={5} style={{ minWidth: '400px', maxWidth: '450px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '90px' }}>
                    대리점 전송일 :
                  </Form.Label>
                  <Form.Control
                    type="date"
                    size="sm"
                    value={startDate}
                    onChange={(e) => {
                      setStartDate(e.target.value);
                    }}
                    style={{ width: '140px' }}  // 고정 크기
                  />
                  <span className="small">~</span>
                  <Form.Control
                    type="date"
                    size="sm"
                    value={endDate}
                    onChange={(e) => {
                      setEndDate(e.target.value);
                    }}
                    style={{ width: '140px' }}  // 고정 크기
                  />
                </div>
              </Form.Group>
            </Col>

            {/* 대리점 선택 */}
            <Col md={3} style={{ minWidth: '230px', maxWidth: '230px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    대리점 :
                  </Form.Label>
                  <Select
                    value={
                      selectedAgency
                        ? {
                            value: selectedAgency,
                            label: agencyList.find(a => a.agencyCd === selectedAgency)?.agencyNm || selectedAgency
                          }
                        : null
                    }
                    onChange={(selected) => {
                      setSelectedAgency(selected ? selected.value : '');
                    }}
                    options={[
                      { value: '', label: '= 전체 =' },
                      ...agencyList.map(agency => ({
                        value: agency.agencyCd,
                        label: agency.agencyNm
                      }))
                    ]}
                    placeholder="대리점 검색"
                    isClearable
                    isSearchable
                    noOptionsMessage={() => "검색 결과가 없습니다"}
                    styles={{
                      // ✅ 컨테이너 전체 너비
                      container: (base) => ({
                        ...base,
                        width: '180px'
                      }),
                      // ✅ 컨트롤 (입력 영역)
                      control: (base, state) => ({
                        ...base,
                        minHeight: '31px',
                        height: '31px',
                        fontSize: '14px',
                        borderColor: state.isFocused ? '#86b7fe' : '#ced4da',
                        boxShadow: state.isFocused ? '0 0 0 0.25rem rgba(13, 110, 253, 0.25)' : 'none',
                        '&:hover': {
                          borderColor: state.isFocused ? '#86b7fe' : '#ced4da'
                        }
                      }),
                      // ✅ 값 컨테이너
                      valueContainer: (base) => ({
                        ...base,
                        height: '29px',
                        padding: '0 8px'
                      }),
                      // ✅ 입력 필드
                      input: (base) => ({
                        ...base,
                        margin: '0',
                        padding: '0'
                      }),
                      // ✅ 인디케이터 (화살표, X 버튼)
                      indicatorsContainer: (base) => ({
                        ...base,
                        height: '29px'
                      }),
                      // ✅ 드롭다운 인디케이터 (화살표)
                      dropdownIndicator: (base) => ({
                        ...base,
                        padding: '4px'
                      }),
                      // ✅ 클리어 인디케이터 (X 버튼)
                      clearIndicator: (base) => ({
                        ...base,
                        padding: '4px'
                      }),
                      // ✅ 옵션 (드롭다운 항목)
                      option: (base, state) => ({
                        ...base,
                        fontSize: '14px',
                        padding: '8px 12px',
                        backgroundColor: state.isSelected 
                          ? '#0d6efd' 
                          : state.isFocused ? '#e9ecef' : 'white',
                        color: state.isSelected ? 'white' : '#212529',
                        cursor: 'pointer',
                        '&:active': {
                          backgroundColor: '#0d6efd'
                        }
                      }),
                      // ✅ 메뉴 (드롭다운 전체)
                      menu: (base) => ({
                        ...base,
                        zIndex: 9999,
                        marginTop: '2px'
                      }),
                      // ✅ 메뉴 리스트 (스크롤 영역)
                      menuList: (base) => ({
                        ...base,
                        maxHeight: '400px'
                      }),
                      // ✅ 선택된 값 표시
                      singleValue: (base) => ({
                        ...base,
                        fontSize: '14px'
                      }),
                      // ✅ 플레이스홀더
                      placeholder: (base) => ({
                        ...base,
                        fontSize: '14px',
                        color: '#6c757d'
                      })
                    }}
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
                disabled={isLoading}
              >
                  <FaSearch /> 조회
                
              </Button>
            </Col>

            {/* 엑셀 업로드 버튼 */}
            <Col md={2} style={{ minWidth: '170px', maxWidth: '170px' }}>
              <Button
                variant="success"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleExcelUpload}
                disabled={isLoading}
              >
                  <RiFileExcel2Line /> 개별파일 업로드
                
              </Button>
            </Col>

            {/* 엑셀 버튼 */}
            {/* <Col md={2} style={{ minWidth: '160px', maxWidth: '160px' }}>
              <Button
                variant="secondary"
                size="sm"
                className="w-100"
                onClick={handleExcelDownload}
              >
                <i className="bi bi-search me-2">
                    <RiFileExcel2Line /> 엑셀다운로드
                </i>
                
              </Button>
            </Col> */}
          </Row>
        </Card.Body>
      </Card>

      {/* Tabulator 그리드 */}
      <Card>
        <Card.Header className="bg-light text-dark fw-bold">
          <i className="bi bi-list-ul me-2"></i>
          밀크방 파일 관리
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

      {/* 숨겨진 파일 input */}
      <input
        type="file"
        ref={fileInputRef}
        style={{ display: 'none' }}
        accept=".xlsx,.xls"
        onChange={handleFileChange}
      />
    </Container>
  );
};

export default MilkFileMng;