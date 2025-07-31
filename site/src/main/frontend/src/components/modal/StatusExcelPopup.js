import React, { useEffect, useState,useRef } from 'react';
import { Modal, Button, Form, Row, Col, Table } from 'react-bootstrap';
import axios from 'axios';
import * as XLSX from 'xlsx';

const StatusExcelPopup = () => {
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
    window.addEventListener('showStatusExcelPoup', showHandler);

    // 닫기 함수도 외부에서 호출 가능하도록 등록
    window.closeSaveStatusPopup = () => setShow(false);

    return () => {
      window.removeEventListener('showStatusExcelPoup', showHandler);
      delete window.closeSaveStatusPopup;
    };
  }, []);

  const getStatusData = () => {
    // 값 초기화
    custnoRef.current = '';
    custnameRef.current = '';
    salesManRef.current = '';
    setSaveYn('');
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

  const handleClose = () => setShow(false);

  const filterStatusTable = () => {
    const custno = custnoRef.current.value.toLowerCase();
    const custname = custnameRef.current.value.toLowerCase();
    const salesMan = salesManRef.current.value.toLowerCase();
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

    // 카운트 계산
    const total = filteredData.length;
    const countY = filteredData.filter(item => item.saveYn === 'Y').length;
    const countN = total - countY;

    setSummaryCounts({ total, countY, countN });

  };

  const downloadExcel = () => {
    // 필터된 데이터가 없는 경우 체크
    if (!filterStatusData || filterStatusData.length === 0) {
        alert("다운로드할 데이터가 없습니다.");
        return;
    }

    // ✅ 필터된 상태 데이터를 엑셀 형식으로 변환 (순번 포함)
    const excelData = filterStatusData.map((row, index) => ({
        순번: index + 1,                                              // ✅ 순번 추가
        거래처코드: row.custNo,
        거래처명: row.custName,
        담당자명: row.salesMan,
        지정여부: row.saveYn === 'Y' ? '지정완료' : '미지정'
    }));

    // 워크시트 생성
    const worksheet = XLSX.utils.json_to_sheet(excelData);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "권역지정현황");

    // 파일명에 현재 날짜 포함
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(workbook, `권역지정현황_${today}.xlsx`);
  };

  const getSelectedSaveYn = () => {
    if (saveYnAllRef.current?.checked) return '';
    if (saveYnYRef.current?.checked) return 'Y';
    if (saveYnNRef.current?.checked) return 'N';
    return '';
  };

  return (
    <Modal
      size="xl"
      show={show}
      onHide={handleClose}
      backdrop={true}
      keyboard={true}
      scrollable={true}
    >
      <Modal.Header closeButton>
        <Modal.Title>
            대리점별 권역 지정 현황
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
          <Col md={3}>
            <Form.Label>거래처코드</Form.Label>
            <Form.Control type="text" id="filterCustno" ref={custnoRef} onChange={filterStatusTable} />
          </Col>
          <Col md={3}>
            <Form.Label>거래처명</Form.Label>
            <Form.Control type="text" id="filterCustname" ref={custnameRef} onChange={filterStatusTable} />
          </Col>
          <Col md={3}>
            <Form.Label>담당자명</Form.Label>
            <Form.Control type="text" id="filterSalesMan" ref={salesManRef} onChange={filterStatusTable} />
          </Col>
          <Col md={3}>
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

        <div 
          style={{ 
            maxHeight: '450px', 
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
                    zIndex: 10,
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
                  <tr key={index} style={{ cursor: 'pointer' }}>
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
      </Modal.Body>
      <Modal.Footer>
        <Button variant="success" onClick={downloadExcel}>
          엑셀 다운로드
        </Button>
        <Button variant="secondary" onClick={handleClose}>
          닫기
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default StatusExcelPopup;