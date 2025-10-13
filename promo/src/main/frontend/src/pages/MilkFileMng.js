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
import { CiViewList } from "react-icons/ci";
import '../styles/MilkFileMng.css';
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
    console.log('다운로드 시작:', rowData);

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
  const tableRef = useRef(null);

  // 컴포넌트 마운트 시 대리점 목록 조회
  useEffect(() => {
    fetchAgencyList();
  }, []);

  // 검색 조건 변경 시 자동 조회
  // useEffect(() => {
  //   handleSearch();
  // }, [startDate, endDate, selectedAgency]);
  
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
      headerHozAlign: 'center',
      editor: 'input'  // 텍스트 입력 가능
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
      formatter: function(cell) {
        const value = cell.getValue();
        if (value === '정상파일') {
          return `<span style="color: blue; cursor: pointer;">${value}</span>`;
        } else if (value === '파일내용없음') {
          return `<span style="color: red;">${value}</span>`;
        } else {
          return `<span style="color: red;">${value}</span>`;
        }
        return value;
      },
      cellClick: function(e, cell) {
        const value = cell.getValue();
        if (value === '정상파일') {
          handleRemarkClick(cell.getRow().getData());
        }
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
    const fileName = `밀크방파일관리_${today}.xlsx`;
    
    tabulatorInstance?.current.download("xlsx", fileName, {
      sheetName: "밀크방파일관리"
    });
  };

  // 조회 버튼 클릭
  const handleSearch = async () => {
    try {
      // 조회 API 호출
      const response = await axios.get('/api/promo/getMilkbangFileList', {
        params: { startDate : startDate,
                  endDate : endDate,
                  agencyCd: selectedAgency }
      });

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
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '데이터 조회에 실패했습니다.',
        confirmButtonText: '확인'
      });
    }
  };

  // 비고 클릭 이벤트
  const handleRemarkClick = (rowData) => {
    console.log('클릭된 행:', rowData);

    // Swal.fire({
    //     icon: 'warning',
    //     title: '오류',
    //     text: `파일명: ${rowData.fileNm}\n상세 정보를 표시합니다.`,
    //     confirmButtonText: '확인'
    //   });
  };

  // Tabulator 옵션
  const options = {
    layout: 'fitColumns',
    pagination: false,
    placeholder: '조회된 데이터가 없습니다.',
    height: '570px'
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

            {/* 조회 버튼 */}
            <Col md={1} style={{ minWidth: '90px', maxWidth: '90px' }}>
              <Button
                variant="primary"
                size="sm"
                className="w-100"
                onClick={handleSearch}
              >
                <i className="bi bi-search me-2"></i>
                조회
              </Button>
            </Col>

            {/* 엑셀 버튼 */}
            <Col md={2} style={{ minWidth: '140px', maxWidth: '140px' }}>
              <Button
                variant="secondary"
                size="sm"
                className="w-100"
                onClick={handleExcelDownload}
              >
                <i className="bi bi-search me-2"></i>
                엑셀다운로드
              </Button>
            </Col>
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
    </Container>
  );
};

export default MilkFileMng;