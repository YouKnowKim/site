import React, { useState, useEffect, useMemo} from 'react';
import { Modal, Button, Form, Row, Col, Table} from 'react-bootstrap';
import axios from 'axios';
import Swal from 'sweetalert2';

/**
 * 판촉실적 상세 팝업 컴포넌트
 * @param {boolean} show - 모달 표시 여부
 * @param {function} onHide - 모달 닫기 함수
 * @param {object} rowData - 선택된 행 데이터
 * @param {array} originalData - 전체 원본 데이터 (no, orderCd 정렬됨)
 * @param {function} onSave - 저장 후 콜백 함수
 */
const PromotionDuplModal = ({ show, onHide, rowData }) => {
  // ✅ 상태 관리
  const [detailData, setDetailData] = useState([]);
  const [subDetailData, setSubDetailData] = useState({});
  const [loading, setLoading] = useState(false);
  const [promoTeamList, setPromoTeamList] = useState([]);  // 판촉팀 목록 state 추가
  const [selectedPromoTeamCd, setSelectedPromoTeamCd] = useState('');
  const [selectedPromoPersonNm, setSelectedPromoPersonNm] = useState('');

  // 컴포넌트 마운트 시 대리점 목록 조회
  useEffect(() => {
    fetchPromoTeamList();
  }, []);

  /**
   * ✅ promoTeamList가 로드된 후 판촉팀 코드 설정
   * detailData가 있고, promoTeamList가 로드되었을 때만 실행
   */
  useEffect(() => {
    // ✅ 판촉팀 목록과 상세 데이터가 모두 준비되었을 때
    if (promoTeamList.length > 0 && detailData.length > 0) {
      const promoTeamCd = detailData?.[0]?.promoTeamCd;
      
      // ✅ null, undefined, 빈 문자열인 경우 '-1'(배치X)로 처리
      if (!promoTeamCd || promoTeamCd === '' || promoTeamCd === null || promoTeamCd === undefined) {
        setSelectedPromoTeamCd('-1');
      } else {
        setSelectedPromoTeamCd(promoTeamCd);
      }
    }
  }, [promoTeamList, detailData]);  // ✅ 두 값이 변경될 때마다 실행

  // 대리점 목록 조회 함수
  const fetchPromoTeamList = async () => {
    try {

      const response = await axios.get('/api/promo/getAllPromoTeam', {
        params: {
          teamPersonCd: sessionStorage.getItem("teamPersonCd"),
          managerYn: sessionStorage.getItem("managerYn")
        }
      });  // API 엔드포인트 수정 필요
      
      // API 응답 구조에 따라 수정
      // 예: response.data 또는 response.data.data
      setPromoTeamList(response.data);
      
    } catch (error) {
      Swal.fire({
        icon: 'warning',
        title: '오류',
        text: '판촉팀 목록 조회 실패',
        confirmButtonText: '확인'
      });
      console.error('판촉팀 목록 조회 실패:', error);
      // 에러 시 빈 배열 설정
      setPromoTeamList([]);
    }
  };

  /**
     * ✅ 모달이 열릴 때 초기 데이터 로드 및 인덱스 설정
     */
    useEffect(() => {
      if (show && rowData) {
        
        // 상세 데이터 조회
        fetchDetailData(rowData);
      }
    }, [show, rowData]);

  /**
   * 상세 데이터 조회 API
   * @param {object} targetRowData - 조회할 행 데이터
   */
  const fetchDetailData = async (targetRowData) => {
    try {
      setLoading(true);
      
      const response = await axios.get('/api/promo/getMilkbangDetail', {
        params: {
          orderCd: targetRowData.duplOrderCd
        }
      });
      
      const dataArray = Array.isArray(response.data) ? response.data : [response.data];
      setDetailData(dataArray);

      // ✅ 첫 번째 데이터에서 판촉팀 코드 추출하여 select에 세팅
      if (dataArray.length > 0 && dataArray[0].promoPersonNm) {
        setSelectedPromoPersonNm(dataArray[0].promoPersonNm);
      } else {
        // 데이터가 없거나 판촉팀 정보가 없으면 초기화
        setSelectedPromoPersonNm('');
      }
      
    } catch (error) {
      console.error('상세 데이터 조회 실패:', error);
      Swal.fire({
        icon: 'error',
        title: '조회 실패',
        text: '상세 정보를 조회하는 중 오류가 발생했습니다.',
        confirmButtonText: '확인'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal 
      show={show} 
      onHide={onHide} 
      size="xl" 
      centered
      backdrop="static"
    >
      <Modal.Header closeButton className="bg-light">
        <Modal.Title style={{ fontSize: '1.3rem' }}>
          <i className="bi bi-card-checklist me-2"></i>
          이중기재 내역
          {/* ✅ 조회 중 표시 - 로딩 상태일 때만 표시 */}
          {loading && (
            <small className="ms-3 text-primary" style={{ fontSize: '14px' }}>
              <span 
                className="spinner-border spinner-border-sm me-1" 
                role="status" 
                style={{ 
                  width: '0.9rem', 
                  height: '0.9rem',
                  borderWidth: '0.15em',
                  verticalAlign: 'middle'
                }}
              />
              <span style={{ verticalAlign: 'middle' }}>조회 중...</span>
            </small>
          )}
        </Modal.Title>
      </Modal.Header>

      <Modal.Body style={{ maxHeight: '75vh', overflowY: 'auto' }}>
        {/* ✅ 대리점 및 판촉팀 정보 */}
        <div className="border rounded p-3 mb-3 bg-light">
          <h6 className="fw-bold mb-3">
            대리점 및 판촉팀 정보
          </h6>
          <Row>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">대리점</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={`${detailData?.[0]?.agencyNm || ''} (${detailData?.[0]?.agencyCd || ''})`}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">담당자</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={detailData?.[0]?.teamPersonNm || ''}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">판촉팀</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={detailData?.[0]?.promoTeamNm || ''}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">판촉사원</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={selectedPromoPersonNm}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
          </Row>
        </div>

        {/* ✅ 고객 정보 */}
        <div className="border rounded p-3 mb-3">
          <h6 className="fw-bold mb-3">
            <i className="bi bi-person me-2"></i>
            고객 정보
          </h6>
          <Row>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">주문번호</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={detailData?.[0]?.orderCd || ''}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">주문자명</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={detailData?.[0]?.orderUserNm || ''}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">주소</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={detailData?.[0]?.orderAddress1 || ''}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
            <Col md={2}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">전화번호</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  style={{
                    borderColor: (detailData?.[0]?.orderCellPhone) ? '' : '#dc3545',
                    color: (detailData?.[0]?.orderCellPhone) ? '' : '#dc3545'
                  }}
                  value={ (detailData?.[0]?.orderCellPhone) ? detailData?.[0]?.orderCellPhone || '' : '전화번호 없음'}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
          </Row>
          <Row>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">주문일자</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={detailData?.[0]?.orderDt || ''}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
          </Row>
        </div>

        {/* ✅ 제품정보 테이블 */}
        <div className="border rounded p-3 mb-3">
          <h6 className="fw-bold mb-3">
            <i className="bi bi-box-seam me-2"></i>
            제품 정보
          </h6>
          <div className="table-responsive">
            <Table bordered hover size="sm" className="mb-0">
              <thead className="table-light">
                <tr className="text-center align-middle small">
                  <th style={{ width: '200px' }}>상품</th>
                  <th style={{ width: '50px' }}>1회투입<br/>수량</th>
                  <th style={{ width: '80px' }}>배송요일</th>
                  <th style={{ width: '50px' }}>주간<br/>총수량</th>
                  <th style={{ width: '70px' }}>음용기간</th>
                  <th style={{ width: '60px' }}>계약구분</th>
                  <th style={{ width: '140px' }}>판촉물 / 중단일<br/>(중단사유)</th>
                  <th style={{ width: '80px' }}>마감홉수</th>
                  
                </tr>
              </thead>
              <tbody class="align-middle">
                {/* ✅ detailData 배열을 map으로 반복 렌더링 */}
                {detailData.map((item, index) => (
                  <tr key={index}>
                  <td className="text-center" style={{color: (item.goodsOptionNm.includes('매칭안됨')) ? '#dc3545' : ''}}>{item.goodsOptionNm || ''}</td>
                  <td className="text-center">{item.quantity || ''}</td>
                  <td className="text-center">{item.weekRemark || ''}</td>
                  <td className="text-center">{item.weekQty || ''}</td>
                  <td className="text-center" style={{color: (Number(item.contractPeriod) < 12) ? '#dc3545' : ''}}>{item.contractPeriod || ''} 개월</td>
                  <td className="text-center">{item.orderKindCdNm || ''}</td>
                  {/* <td className="text-center">
                    {item.stopReason && item.promoGiftNm ? `${item.stopReason} / ${item.promoGiftNm}` : item.stopReason || item.promoGiftNm || ''}
                  </td> */}
                  <td className="text-center">
                    {
                      // ✅ promoGiftNm / stopDt / stopReason 을 배열로 만들고
                      // 값이 있는 것만 필터링한 후 / 로 연결
                      [
                        item.promoGiftNm,
                        item.stopDt,
                        item.stopReason
                      ]
                      .filter(value => value)  // 빈 값 제거 (null, undefined, '' 제외)
                      .join(' / ')             // / 로 연결
                      || ''                     // 모두 없으면 빈 문자열
                    }
                  </td>
                  <td className="text-center" style={{textAlign: 'center'}}>{item.actualHob || ''}</td>
                </tr>
                ))}
                
              </tbody>
            </Table>
          </div>
        </div>
      </Modal.Body>

      <Modal.Footer className="bg-light">
        <div className="d-flex justify-content-between align-items-center w-100">
          {/* 왼쪽: 이전/다음 버튼 */}
          <div className="d-flex gap-2">
            
            
          </div>
          
          {/* 오른쪽: 닫기/저장 버튼 */}
          <div className="d-flex gap-2">
            <Button 
              variant="secondary" 
              onClick={onHide}
              style={{ width: '120px' }}
            >
              <i className="bi bi-x-circle me-1"></i>
              닫기
            </Button>
            
          </div>
        </div>
      </Modal.Footer>
    </Modal>

  );
};

export default PromotionDuplModal;