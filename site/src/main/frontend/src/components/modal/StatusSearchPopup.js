import React, { useEffect, useState,useRef } from 'react';
import { Modal, Button, Form, Row, Col, Table, Alert } from 'react-bootstrap';
import axios from 'axios';
import * as XLSX from 'xlsx';

const StatusSearchPopup = (props) => {
  const [show, setShow] = useState(false);
  const custnoRef = useRef(null);
  const custnameRef = useRef(null);
  const salesManRef = useRef(null);
  const saveYnAllRef = useRef(null);
  const saveYnYRef = useRef(null);
  const saveYnNRef = useRef(null);
  const [saveYn, setSaveYn] = useState(null);
  const [fullStatusData, setFullStatusData] = useState([]);
  const [filterStatusData, setFilterStatusData] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [selectedRow, setSelectedRow] = useState(null); // 선택된 행 상태 추가
  const [summaryCounts, setSummaryCounts] = useState({
    total: 0,
    countY: 0,
    countN: 0
    });
  const baseURL = process.env.REACT_APP_API_URL;

  useEffect(() => {
    const showHandler = () => {
        setShow(true)
        getStatusData()};
    window.addEventListener('showStatusSearchPopup', showHandler);

    // 닫기 함수도 외부에서 호출 가능하도록 등록
    window.closeSaveStatusPopup = () => setShow(false);

    return () => {
      window.removeEventListener('showStatusSearchPopup', showHandler);
      delete window.closeSaveStatusPopup;
    };
  }, []);

  const getStatusData = () => {
    // 값 초기화
    if (custnoRef.current) custnoRef.current.value = '';
    if (custnameRef.current) custnameRef.current.value = '';
    if (salesManRef.current) salesManRef.current.value = '';
    setSaveYn('');
    setSelectedRow(null); // 선택된 행 초기화
    const gubunType = (sessionStorage.getItem("gubunType"));

    axios.get(`${baseURL}/api/region/getRegionSaveStatus`,{
        params: { gubunType: gubunType }
        })
        .then(res => {
            setFullStatusData(res.data);
            setFilterStatusData(res.data);

            // 카운트 계산
            const total = res.data.length;
            const countY = res.data.filter(item => item.saveYn === 'Y').length;
            const countN = total - countY;

            setSummaryCounts({ total, countY, countN });
        })
        .catch(err => {
            alert("조회 중 오류 발생.");
            console.error("권역 저장 여부 불러오기 실패:", err);
        });

  };

  const handleClose = () => {
    setShow(false);
    setSelectedRow(null); // 닫을 때 선택 초기화
  };

  const filterStatusTable = () => {
    const custno = custnoRef.current?.value?.toLowerCase() || '';
    const custname = custnameRef.current?.value?.toLowerCase() || '';
    const salesMan = salesManRef.current?.value?.toLowerCase() || '';
    const saveYn = getSelectedSaveYn();

    // React 의 setState는 비동기적으로 동작하므로 이걸 막아주어야 한다.
    const filteredData = fullStatusData.filter(row => {
            const matchCustno = row.custNo.toLowerCase().includes(custno);
            const matchCustname = row.custName.toLowerCase().includes(custname);
            const matchSalesMan = row.salesMan.toLowerCase().includes(salesMan);
            const matchSaveYn = !saveYn || row.saveYn === saveYn;
            return matchCustno && matchCustname && matchSaveYn && matchSalesMan;
        });

    setFilterStatusData(filteredData);
    setSelectedRow(null); // 필터링 시 선택 초기화

    // 카운트 계산
    const total = filteredData.length;
    const countY = filteredData.filter(item => item.saveYn === 'Y').length;
    const countN = total - countY;

    setSummaryCounts({ total, countY, countN });

  };

  const getSelectedSaveYn = () => {
    if (saveYnAllRef.current?.checked) return '';
    if (saveYnYRef.current?.checked) return 'Y';
    if (saveYnNRef.current?.checked) return 'N';
    return '';
  };

  // 행 클릭 시 선택
  const handleRowClick = (row, index) => {
    setSelectedRow({ ...row, index });
  };

  // 더블클릭 시 즉시 데이터 전송
  const handleRowDoubleClick = (row) => {
    sendDataToParent(row);
  };

  // 확인 버튼 클릭
  const handleConfirm = () => {
    if (!selectedRow) {
      alert("행을 선택해주세요.");
      return;
    }
    sendDataToParent(selectedRow);
  };

  const sendDataToParent = (rowData) => {
    // 데이터 유효성 검사
    if (!rowData || !rowData.custNo) {
      alert("유효하지 않은 데이터입니다.");
      return;
    }

    props.getData(rowData);

    handleClose();
  };

  return (
    <Modal
      size="lg"
      show={show}
      onHide={handleClose}
      backdrop={true}
      keyboard={true}
      scrollable={true}
    >
      <Modal.Header closeButton>
        <Modal.Title>
            거래처 검색
            <span style={{
                fontSize: '15px',
                fontWeight: 'normal',
                marginLeft: '10px',
                color: '#555'
            }}>
                ({`총 ${summaryCounts.total}건 / 지정완료 ${summaryCounts.countY}건 / 미지정 ${summaryCounts.countN}건`})
            </span>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body style={{ maxHeight: '70vh' }}>
        <Row className="mb-3">
          <Col md={2}>
            <Form.Label>거래처코드</Form.Label>
            <Form.Control 
              type="text" 
              id="filterCustno" 
              ref={custnoRef} 
              onChange={filterStatusTable} 
              placeholder="거래처코드"
            />
          </Col>
          <Col md={3}>
            <Form.Label>거래처명</Form.Label>
            <Form.Control 
              type="text" 
              id="filterCustname" 
              ref={custnameRef} 
              onChange={filterStatusTable} 
              placeholder="거래처명"
            />
          </Col>
          <Col md={2}>
            <Form.Label>담당자명</Form.Label>
            <Form.Control 
              type="text" 
              id="filterSalesMan" 
              ref={salesManRef} 
              onChange={filterStatusTable} 
              placeholder="담당자명"
            />
          </Col>
          <Col md={5}>
            <Form.Label>지정여부</Form.Label>
            <div>
              <Form.Check
                inline
                label="전체"
                name="filterSaveYn"
                type="radio"
                value=""
                ref={saveYnAllRef}
                defaultChecked
                onChange={filterStatusTable}
              />
              <Form.Check
                inline
                label="지정완료"
                name="filterSaveYn"
                type="radio"
                value="Y"
                ref={saveYnYRef}
                onChange={filterStatusTable}
              />
              <Form.Check
                inline
                label="미지정"
                name="filterSaveYn"
                type="radio"
                value="N"
                ref={saveYnNRef}
                onChange={filterStatusTable}
              />
            </div>
          </Col>
        </Row>

        {/* 선택된 행 정보 표시 */}
        <Alert variant="info" className="mb-3">
        <div style={{ fontSize: '14px' }}>
            <strong>선택된 항목:</strong>
            {selectedRow ? (
            <span className="ms-2">
                {selectedRow.custNo} - {selectedRow.custName}
                <span className="text-muted ms-2">({selectedRow.salesMan})</span>
            </span>
            ) : (
            <span className="ms-2 text-muted">선택된 항목이 없습니다.</span>
            )}
        </div>
        </Alert>

        <div 
          style={{ 
            maxHeight: '400px', 
            overflowY: 'auto',
            border: '1px solid #dee2e6',
            borderRadius: '0.375rem'
          }}
        >
          <Table striped bordered hover size="sm" className="mb-0">
            <thead 
                className="table-dark"
                style={{ 
                    position: 'sticky', 
                    top: 0, 
                    zIndex: 100,
                    backgroundColor: '#212529',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                }}
            >
              <tr>
                <th style={{ width: '10%', textAlign: 'center' }}>순번</th>
                <th style={{ width: '20%', textAlign: 'center' }}>거래처코드</th>
                <th style={{ width: '35%', textAlign: 'center' }}>거래처명</th>
                <th style={{ width: '25%', textAlign: 'center' }}>담당자명</th>
                <th style={{ width: '20%', textAlign: 'center' }}>지정여부</th>
              </tr>
            </thead>
            <tbody>
              {filterStatusData && filterStatusData.length > 0 ? (
                filterStatusData.map((row, index) => (
                  <tr 
                    key={index} 
                    onClick={() => handleRowClick(row, index)}
                    onDoubleClick={() => handleRowDoubleClick(row)}
                    style={{ 
                      cursor: 'pointer',
                      backgroundColor: selectedRow && selectedRow.index === index 
                        ? '#e3f2fd'  // 선택된 행은 파란색 배경
                        : 'transparent',
                      transition: 'background-color 0.2s ease',
                      userSelect: 'none' // 텍스트 선택 방지
                    }}
                    onMouseEnter={(e) => {
                      if (!selectedRow || selectedRow.index !== index) {
                        e.currentTarget.style.backgroundColor = '#f8f9fa';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!selectedRow || selectedRow.index !== index) {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      } else {
                        e.currentTarget.style.backgroundColor = '#e3f2fd';
                      }
                    }}
                  >
                    <td style={{ textAlign: 'center', fontWeight: 'bold', color: '#666' }}>
                      {index + 1}
                    </td>
                    <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                      {row.custNo}
                    </td>
                    <td style={{ verticalAlign: 'middle', paddingLeft: '12px' }}>
                      {row.custName}
                    </td>
                    <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                      {row.salesMan}
                    </td>
                    <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                      <span 
                        className={`badge ${
                          row.saveYn === 'Y' ? 'bg-success' : 'bg-secondary'
                        }`}
                        style={{ fontSize: '0.8rem' }}
                      >
                        {row.saveYn === 'Y' ? '지정완료' : '미지정'}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', padding: '2rem' }}>
                    <div className="text-muted">
                      <i className="fas fa-search me-2"></i>
                      검색 결과가 없습니다.
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        </div>

        {/* 사용법 안내 */}
        <div className="mt-3">
          <small className="text-muted">
            💡 <strong>사용법:</strong> 행을 <strong>클릭</strong>하여 선택하거나, <strong>더블클릭</strong>으로 바로 선택할 수 있습니다.
          </small>
        </div>
      </Modal.Body>
      <Modal.Footer>
        <Button 
          variant="success" 
          onClick={handleConfirm}
          disabled={!selectedRow} // 선택된 행이 없으면 비활성화
        >
          선택 {selectedRow && `(${selectedRow.custNo})`}
        </Button>
        <Button variant="secondary" onClick={handleClose}>
          닫기
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default StatusSearchPopup;