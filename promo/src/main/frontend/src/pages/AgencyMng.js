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

const AgencyMng = () => {
  const [selectedAgencyCd, setSelectedAgencyCd] = useState('');
  const [selectedAgencyNm, setSelectedAgencyNm] = useState('');
  const [selectedTeamPersonNm, setSelectedTeamPersonNm] = useState('');
  const [selectedDeleteYn, setSelectedDeleteYn] = useState(0);  // ✅ 미사용 포함 여부
  const [tableData, setTableData] = useState([]);
  const [agencyList, setAgencyList] = useState([]);  // 대리점 목록 state 추가
  const [tabulatorInstance, setTabulatorInstance] = useState(null);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const isInitialLoadRef = useRef(true);
  const tableRef = useRef(null);
  // 담당자 선택 팝업 관련 state
  const [showTeamPersonModal, setShowTeamPersonModal] = useState(false);  // 팝업 표시 여부
  const [teamPersonList, setTeamPersonList] = useState([]);               // 담당자 목록
  const [currentRowIndex, setCurrentRowIndex] = useState(null);           // 현재 선택된 행 인덱스
  const teamPersonModalTableRef = useRef(null);                           // 팝업 테이블 참조

  useEffect(() => {
    /**
     * tabulatorInstance가 생성되고, 아직 초기 조회를 하지 않았다면 조회 실행
     */
    if (tabulatorInstance && isInitialLoadRef.current) {
      console.log('Tabulator 인스턴스가 생성되었습니다. 초기 조회를 시작합니다.');
      handleSearch();
      isInitialLoadRef.current = false;  // 초기 조회 완료 플래그 설정
    }
  }, [tabulatorInstance]);  // ✅ tabulatorInstance가 변경될 때마다 실행

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
      agencyCd: '',
      agencyType: '1',
      deleteYn: '0',
      agencyNm: '',
      custno: '',
      agencyNmHq: '',
      teamPersonCd: '',
      teamPersonNm: '',
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
      headerSort : false,
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
      headerHozAlign: 'center'
    },
    {
      title: '대리점코드',
      field: 'agencyCd',
      width: 140,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      editorParams: {
        elementAttributes: {
          maxlength: "5",        // 최대 5자리까지만 입력 가능
          inputMode: "numeric",  // 모바일에서 숫자 키패드 표시
          pattern: "[0-9]*"      // 숫자만 입력 가능 (브라우저 힌트)
        }
      },
      // ✅ 새로 추가된 행(isNew=true)에서만 편집 가능
      editable: function(cell) {
        const rowData = cell.getRow().getData();
        return rowData.isNew === true;  // isNew가 true일 때만 편집 가능
      },
      titleFormatter: function() {
        return '대리점코드<br/>(대리점)';  // HTML로 줄바꿈
      },
      cellEdited: function(cell) {
        const value = cell.getValue();
        
        // 숫자가 아닌 문자 제거
        const cleanedValue = value.replace(/\D/g, '');
        
        // 5자리로 제한
        const limitedValue = cleanedValue.substring(0, 5);
        
        // 값이 변경되었으면 업데이트
        if (value !== limitedValue) {
          cell.setValue(limitedValue);
        }
        
        // 5자리가 아니면 경고
        if (limitedValue.length !== 5) {
          Swal.fire({
            icon: 'warning',
            title: '입력 오류',
            text: '대리점코드는 숫자 5자리를 입력해야 합니다.',
            confirmButtonText: '확인'
          });
        }
      }
    },
    {
      title: '대리점명',
      field: 'agencyNm',
      width: 150,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editor: 'input',
      titleFormatter: function() {
        return '대리점명<br/>(대리점)';  // HTML로 줄바꿈
      },
    },
    {
      title: '대리점코드(본사)',
      field: 'custno',
      width: 140,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '대리점코드<br/>(본사)';  // HTML로 줄바꿈
      },
    },
    {
      title: '대리점명(본사)',
      field: 'agencyNm',
      width: 150,
      hozAlign: 'center',
      headerHozAlign: 'center',
      titleFormatter: function() {
        return '대리점명<br/>(본사)';  // HTML로 줄바꿈
      },
    },
    {
      title: '담당코드',
      field: 'teamPersonCd',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      editable: false,
      formatter: function(cell) {
        const value = cell.getValue() || '';
        const rowIndex = cell.getRow().getPosition(true) - 1;
        const rowData = cell.getRow().getData();
        
        // ✅ 변경된 경우 배경색 적용
        const backgroundColor = rowData.isTeamPersonChanged ? '#fff3cd' : 'transparent';
        
        return `
          <div style="
            display: flex; 
            align-items: center; 
            justify-content: center; 
            gap: 8px; 
            height: 100%;
            background-color: ${backgroundColor};
            margin: -4px;
            padding: 4px;
            transition: background-color 0.3s ease;
          ">
            <span style="
              min-width: 60px;
              text-align: center;
            ">${value}</span>
            <button 
              class="search-team-person-btn" 
              data-row-index="${rowIndex}"
              style="
                background: none;
                border: none;
                padding: 2px;
                cursor: pointer;
                font-size: 16px;
                line-height: 1;
                color: #0d6efd;
                transition: transform 0.1s ease;
              "
              onmouseover="this.style.transform='scale(1.2)'"
              onmouseout="this.style.transform='scale(1)'"
              title="담당자 선택">
              🔍
            </button>
          </div>
        `;
      },
      cellClick: function(e, cell) {
        if (e.target.classList.contains('search-team-person-btn') || 
            e.target.closest('.search-team-person-btn')) {
          
          e.stopPropagation();
          e.preventDefault();
          
          const button = e.target.classList.contains('search-team-person-btn') 
            ? e.target 
            : e.target.closest('.search-team-person-btn');
          const rowIndex = parseInt(button.dataset.rowIndex);
          
          openTeamPersonModal(rowIndex);
        }
      }
    },
    {
      title: '담당자',
      field: 'teamPersonNm',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      // ✅ formatter 추가하여 변경된 경우 배경색 적용
      formatter: function(cell) {
        const value = cell.getValue() || '';
        const rowData = cell.getRow().getData();
        
        // ✅ 변경된 경우 배경색 적용
        const backgroundColor = rowData.isTeamPersonChanged ? '#fff3cd' : 'transparent';
        
        return `
          <div style="
            background-color: ${backgroundColor};
            margin: -4px;
            padding: 4px;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background-color 0.3s ease;
          ">
            ${value}
          </div>
        `;
      }
    },
    {
      title: '사용여부',
      field: 'deleteYn',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible:false
    },
    {
      title: '사용여부',
      field: 'deleteNm',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center',
      // ✅ formatter 함수 추가: 이중기재가 포함되면 빨간색 표시
      formatter: function(cell, formatterParams, onRendered) {
        const value = cell.getValue();
        
        // null 또는 undefined 체크
        if (!value) {
          return '';
        }
        
        if (value === '미사용') {
          cell.getElement().style.color = '#ee1010ff';
          cell.getElement().style.fontWeight = 'bold';
          return value;
        }
        
        return value;
      }
    },
    {
      title: 'agencyType',
      field: 'agencyType',
      width: 80,
      hozAlign: 'center',
      headerHozAlign: 'center',
      visible:false
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
    
    try {

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
      const response = await axios.get('/api/setting/getAgencyList', {
        params: {
          agencyCd : selectedAgencyCd,
          agencyNm : selectedAgencyNm,
          teamPersonNm : selectedTeamPersonNm,
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
    }
  };

  const handleSave = async () => {
    // 유효성 검사
    const invalidRows = tableData.filter(row => {
      if (row.isNew) {  // 새로 추가된 행만 검사
        return !row.agencyCd || !/^\d{5}$/.test(row.agencyCd);
      }
      return false;
    });

    if (invalidRows.length > 0) {
      Swal.fire({
        icon: 'error',
        title: '저장 실패',
        text: '대리점코드가 올바르지 않은 행이 있습니다. (숫자 5자리 필수)',
        confirmButtonText: '확인'
      });
      return;
    }

    // 저장 로직...
  };

  const handleDelete = async () => {};

  // Tabulator 옵션
  const options = {
    layout: 'fitColumns',
    pagination: false,
    placeholder: '조회된 데이터가 없습니다.',
    height: '530px'
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
            대리점 등록 관리
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
                    대리점 :
                  </Form.Label>
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedAgencyNm}
                    onChange={(e) => setSelectedAgencyNm(e.target.value)}
                    placeholder="대리점명 입력"
                    style={{ width: '120px' }}
                  />
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedAgencyCd}
                    onChange={(e) => setSelectedAgencyCd(e.target.value)}
                    placeholder="대리점코드 입력"
                    style={{ width: '120px' }}
                  />
                </div>
              </Form.Group>
            </Col>

            {/* 담당자 입력 */}
            <Col md={2} style={{ minWidth: '230px', maxWidth: '230px' }}>
              <Form.Group>
                <div className="d-flex align-items-center gap-2">
                  <Form.Label className="fw-bold small mb-0" style={{ minWidth: '50px' }}>
                    담당자 :
                  </Form.Label>
                  <Form.Control
                    type="text"
                    size="sm"
                    value={selectedTeamPersonNm}
                    onChange={(e) => setSelectedTeamPersonNm(e.target.value)}
                    placeholder="담당자명 입력"
                    style={{ width: '120px' }}
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

            {/* ✅ 삭제 버튼 추가 */}
            <Col md={1} style={{ minWidth: '115px', maxWidth: '115px' }}>
              <Button
                variant="danger"
                size="sm"
                className="w-100 d-flex align-items-center justify-content-center gap-1"
                onClick={handleDelete}
              >
                <FaTrashAlt /> 미사용
              </Button>
            </Col>

            {/* 엑셀 버튼 */}
            <Col md={2} style={{ minWidth: '160px', maxWidth: '160px' }}>
              <Button
                variant="secondary"
                size="sm"
                className="w-100"
                onClick={handleExcelDownload}
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
              <span style={{ fontSize: '16px', fontWeight: 'bold' }}>대리점 등록 관리</span>
            </Col>
            <Col>
              <Button
                variant="success"
                size="sm"
                onClick={handleAddRow}
                className="d-flex align-items-center gap-1"
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

      {/* 담당자 선택 팝업 */}
      <Modal 
        show={showTeamPersonModal} 
        onHide={() => setShowTeamPersonModal(false)}
      >
        <Modal.Header closeButton>
          <Modal.Title>담당자 선택</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <ReactTabulator
            data={teamPersonList}
            columns={teamPersonColumns}
            options={modalOptions}
            events={{
              rowDblClick: (e, row) => {
                handleTeamPersonSelect(row.getData());
              }
            }}
          />
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default AgencyMng;