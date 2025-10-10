import React, { useState, useRef } from 'react';
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


const MilkFileMng = () => {
  const [startDate, setStartDate] = useState('2025-10-01');
  const [endDate, setEndDate] = useState('2025-10-10');
  const [selectedAgency, setSelectedAgency] = useState('');
  const [tableData, setTableData] = useState([]);
  const tableRef = useRef(null);

  // 테이블 컬럼 정의
  const columns = [
    {
      title: 'No',
      field: 'no',
      width: 100,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '대리점코드',
      field: 'agencyCode',
      width: 140,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '대리점명',
      field: 'agencyName',
      width: 150,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '파일명',
      field: 'fileName',
      width: 300,
      hozAlign: 'left',
      headerHozAlign: 'center'
    },
    {
      title: '대리점 전송일',
      field: 'sendDate',
      width: 180,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '업로드여부',
      field: 'uploadYn',
      width: 130,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '업로드일',
      field: 'uploadDate',
      width: 180,
      hozAlign: 'center',
      headerHozAlign: 'center'
    },
    {
      title: '비고',
      field: 'remark',
      width: 120,
      hozAlign: 'center',
      headerHozAlign: 'center',
      formatter: function(cell) {
        const value = cell.getValue();
        if (value === '정상반영') {
          return `<span style="color: blue; cursor: pointer; text-decoration: underline;">${value}</span>`;
        } else if (value === '파일내용없음') {
          return `<span style="color: red;">${value}</span>`;
        }
        return value;
      },
      cellClick: function(e, cell) {
        const value = cell.getValue();
        if (value === '정상반영') {
          handleRemarkClick(cell.getRow().getData());
        }
      }
    }
  ];

  // 조회 버튼 클릭
  const handleSearch = async () => {
    try {
      console.log('검색 조건:', {
        startDate,
        endDate,
        selectedAgency
      });

      // TODO: 실제 API 호출
      // const response = await axios.get('/api/promo-files', {
      //   params: { startDate, endDate, agencyCode: selectedAgency }
      // });

      // 샘플 데이터
      const sampleData = [
        {
          no: 1,
          agencyCode: '10011',
          agencyName: '은평',
          fileName: '10011_은평_250901_0930.xls',
          sendDate: '2025-10-10 10:46:23',
          uploadYn: 'O',
          uploadDate: '2025-10-10 12:05:44',
          remark: '정상반영'
        },
        {
          no: 2,
          agencyCode: '10020',
          agencyName: '관악',
          fileName: '10020_관악_250901_0907.xls',
          sendDate: '2025-10-01 13:18:42',
          uploadYn: 'O',
          uploadDate: '2025-10-05 23:24:24',
          remark: '정상반영'
        },
        {
          no: 3,
          agencyCode: '10020',
          agencyName: '관악',
          fileName: '10020_관악_250908_0914.xls',
          sendDate: '2025-10-01 13:19:06',
          uploadYn: 'O',
          uploadDate: '2025-10-05 23:24:28',
          remark: '정상반영'
        },
        {
          no: 9,
          agencyCode: '10034',
          agencyName: '',
          fileName: '10034_경남_250901_0930.xls',
          sendDate: '2025-10-02 14:51:05',
          uploadYn: '',
          uploadDate: '2025-10-06 23:33:51',
          remark: '파일내용없음'
        }
      ];

      setTableData(sampleData);

    } catch (error) {
      console.error('조회 실패:', error);
      alert('데이터 조회에 실패했습니다.');
    }
  };

  // 비고 클릭 이벤트
  const handleRemarkClick = (rowData) => {
    console.log('클릭된 행:', rowData);
    alert(`파일명: ${rowData.fileName}\n상세 정보를 표시합니다.`);
  };

  // Tabulator 옵션
  const options = {
    layout: 'fitColumns',
    pagination: false,
    placeholder: '조회된 데이터가 없습니다.',
    height: '530px'
  };

  return (
    <Container fluid className="mt-1">
      {/* 제목 */}
      <Row className="mb-3">
        <Col>
          <h4>
            <i className="bi bi-circle-fill text-warning me-2"></i>
            <CiViewList />
            밀크방 파일 관리
          </h4>
        </Col>
      </Row>

      {/* 검색 조건 */}
      <Card className="mb-3">
        <Card.Body>
          <Row className="align-items-end">
            {/* 날짜 범위 */}
            <Col md={4}>
              <Form.Group>
                <Form.Label className="fw-bold">대리점 전송일</Form.Label>
                <div className="d-flex align-items-center gap-2">
                  <Form.Control
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                  />
                  <span>~</span>
                  <Form.Control
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                  />
                </div>
              </Form.Group>
            </Col>

            {/* 대리점 선택 */}
            <Col md={3}>
              <Form.Group>
                <Form.Label className="fw-bold">대리점</Form.Label>
                <Form.Select
                  value={selectedAgency}
                  onChange={(e) => setSelectedAgency(e.target.value)}
                >
                  <option value="">= 전체 =</option>
                  <option value="10011">10011 - 은평</option>
                  <option value="10020">10020 - 관악</option>
                  <option value="10033">10033 - 연동</option>
                  <option value="10034">10034 - 신정남</option>
                  <option value="10035">10035 - 인천</option>
                </Form.Select>
              </Form.Group>
            </Col>

            {/* 조회 버튼 */}
            <Col md={2}>
              <Button
                variant="primary"
                className="w-100"
                onClick={handleSearch}
              >
                <i className="bi bi-search me-2"></i>
                조회
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
            data={tableData}
            columns={columns}
            options={options}
            layout="fitColumns"
          />
        </Card.Body>
      </Card>
    </Container>
  );
};

export default MilkFileMng;