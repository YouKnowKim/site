// components/PromotionDetailModal.jsx
import React, { useState, useEffect, useMemo } from 'react';
import { Modal, Button, Form, Row, Col, Table, ButtonGroup } from 'react-bootstrap';
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
const PromotionDetailModal = ({ show, onHide, rowData, originalData = [], onSave }) => {
  // ✅ 상태 관리
  const [detailData, setDetailData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [currentOrderCd, setCurrentOrderCd] = useState('');  // ✅ 현재 조회 중인 orderCd
  const [currentIndex, setCurrentIndex] = useState(-1);  // ✅ orderCdList에서의 현재 인덱스
  
  // ✅ 편집 가능한 필드 상태
  const [editableData, setEditableData] = useState({
    actualHob: '',
    saveRemark: '',
    hcHob: '',
    hcStatus: '',
    hcContent: '',
    hcActionStatus: '',
    hcAction: ''
  });

  /**
   * ✅ originalData에서 orderCd만 추출하여 중복 제거한 리스트 생성
   * useMemo로 최적화 (originalData가 변경될 때만 재계산)
   */
  const orderCdList = useMemo(() => {
    if (!originalData || originalData.length === 0) {
      return [];
    }

    // orderCd만 추출
    const orderCds = originalData.map(item => item.orderCd);
    
    // 중복 제거 (순서 유지)
    const uniqueOrderCds = [...new Set(orderCds)];
    
    console.log('✅ orderCdList 생성:', uniqueOrderCds);
    return uniqueOrderCds;
  }, [originalData]);

  /**
   * ✅ 모달이 열릴 때 초기 데이터 로드 및 인덱스 설정
   */
  useEffect(() => {
    if (show && rowData) {
      const orderCd = rowData.orderCd;
      setCurrentOrderCd(orderCd);
      
      // orderCdList에서 현재 orderCd의 인덱스 찾기
      const index = orderCdList.findIndex(cd => cd === orderCd);
      setCurrentIndex(index);
      
      console.log(`✅ 현재 orderCd: ${orderCd}, 인덱스: ${index}/${orderCdList.length}`);
      
      // 상세 데이터 조회
      fetchDetailData(rowData);
    }
  }, [show, rowData, orderCdList]);

  /**
   * 상세 데이터 조회 API
   * @param {object} targetRowData - 조회할 행 데이터
   */
  const fetchDetailData = async (targetRowData) => {
    try {
      setLoading(true);
      
      const response = await axios.get('/api/promo/getMilkbangDetail', {
        params: {
          orderCd: targetRowData.orderCd,
          orderSeq: targetRowData.orderSeq,
          promoDt: targetRowData.promoDt,
          teamPersonCd: targetRowData.teamPersonCd
        }
      });
      
      setDetailData(response.data);
      
      // ✅ 편집 가능한 필드 초기화
      setEditableData({
        actualHob: response.data.actualHob || '',
        saveRemark: response.data.saveRemark || '',
        hcHob: response.data.hcHob || '',
        hcStatus: response.data.hcStatus || '',
        hcContent: response.data.hcContent || '',
        hcActionStatus: response.data.hcActionStatus || '',
        hcAction: response.data.hcAction || ''
      });
      
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

  /**
   * ✅ 이전 버튼 클릭 핸들러
   */
  const handlePrevious = () => {
    if (currentIndex <= 0) {
      return;  // 첫 번째 항목이면 동작하지 않음
    }

    const prevIndex = currentIndex - 1;
    const prevOrderCd = orderCdList[prevIndex];
    
    console.log(`⬅️ 이전 버튼: ${currentIndex} → ${prevIndex}, orderCd: ${prevOrderCd}`);
    
    // originalData에서 해당 orderCd의 첫 번째 데이터 찾기
    const prevRowData = originalData.find(item => item.orderCd === prevOrderCd);
    
    if (prevRowData) {
      setCurrentOrderCd(prevOrderCd);
      setCurrentIndex(prevIndex);
      fetchDetailData(prevRowData);
    }
  };

  /**
   * ✅ 다음 버튼 클릭 핸들러
   */
  const handleNext = () => {
    if (currentIndex >= orderCdList.length - 1) {
      return;  // 마지막 항목이면 동작하지 않음
    }

    const nextIndex = currentIndex + 1;
    const nextOrderCd = orderCdList[nextIndex];
    
    console.log(`➡️ 다음 버튼: ${currentIndex} → ${nextIndex}, orderCd: ${nextOrderCd}`);
    
    // originalData에서 해당 orderCd의 첫 번째 데이터 찾기
    const nextRowData = originalData.find(item => item.orderCd === nextOrderCd);
    
    if (nextRowData) {
      setCurrentOrderCd(nextOrderCd);
      setCurrentIndex(nextIndex);
      fetchDetailData(nextRowData);
    }
  };

  /**
   * 입력값 변경 핸들러
   */
  const handleInputChange = (field, value) => {
    setEditableData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  /**
   * 저장 버튼 클릭
   */
  const handleSave = async () => {
    try {
      // ✅ 저장 확인
      const result = await Swal.fire({
        icon: 'question',
        title: '저장 확인',
        text: '수정된 내용을 저장하시겠습니까?',
        showCancelButton: true,
        confirmButtonText: '저장',
        cancelButtonText: '취소',
        confirmButtonColor: '#28a745'
      });

      if (!result.isConfirmed) {
        return;
      }

      // ✅ 로딩 표시
      Swal.fire({
        title: '저장 중...',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // ✅ API 호출
      const saveData = {
        orderCd: detailData.orderCd,  // ✅ detailData 사용
        orderSeq: detailData.orderSeq,
        ...editableData
      };

      await axios.post('/api/promo/savePromoDetail', saveData);

      // ✅ 성공 메시지
      Swal.fire({
        icon: 'success',
        title: '저장 완료',
        text: '판촉실적이 성공적으로 저장되었습니다.',
        confirmButtonText: '확인'
      }).then(() => {
        onSave(); // 목록 재조회
        // ✅ 모달을 닫지 않고 현재 데이터만 다시 조회
        const currentRowData = originalData.find(item => item.orderCd === currentOrderCd);
        if (currentRowData) {
          fetchDetailData(currentRowData);
        }
      });

    } catch (error) {
      console.error('저장 실패:', error);
      
      let errorMessage = '저장 중 오류가 발생했습니다.';
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }

      Swal.fire({
        icon: 'error',
        title: '저장 실패',
        text: errorMessage,
        confirmButtonText: '확인'
      });
    }
  };

  /**
   * ✅ 이전/다음 버튼 활성화 여부 계산
   */
  const isPreviousDisabled = currentIndex <= 0 || loading;
  const isNextDisabled = currentIndex >= orderCdList.length - 1 || loading;

  // ✅ 데이터가 없으면 로딩 표시
  if (loading || !detailData) {
    return (
      <Modal show={show} onHide={onHide} size="xl" centered>
        <Modal.Body className="text-center py-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <div>데이터를 불러오는 중...</div>
        </Modal.Body>
      </Modal>
    );
  }

  return (
    <Modal 
      show={show} 
      onHide={onHide} 
      size="xl" 
      centered
      backdrop="static"
    >
      <Modal.Header closeButton className="bg-light">
        <Modal.Title>
          <i className="bi bi-card-checklist me-2"></i>
          판촉실적 내역
          {/* ✅ 현재 위치 표시 */}
          <small className="text-muted ms-3" style={{ fontSize: '14px' }}>
            ({currentIndex + 1} / {orderCdList.length})
          </small>
        </Modal.Title>
        
        {/* ✅ 이전/다음 버튼 추가 */}
        <div className="ms-auto me-3">
          <ButtonGroup size="sm">
            <Button 
              variant="outline-primary" 
              onClick={handlePrevious}
              disabled={isPreviousDisabled}
              title="이전 주문건"
            >
              <i className="bi bi-chevron-left"></i> 이전
            </Button>
            <Button 
              variant="outline-primary" 
              onClick={handleNext}
              disabled={isNextDisabled}
              title="다음 주문건"
            >
              다음 <i className="bi bi-chevron-right"></i>
            </Button>
          </ButtonGroup>
        </div>
      </Modal.Header>

      <Modal.Body style={{ maxHeight: '70vh', overflowY: 'auto' }}>
        {/* ✅ 대리점 및 판촉팀 정보 */}
        <div className="border rounded p-3 mb-3 bg-light">
          <h6 className="fw-bold mb-3">
            <i className="bi bi-building me-2"></i>
            대리점 및 판촉팀 정보
          </h6>
          <Row>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">대리점</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={`${detailData.agencyNm || ''} (${detailData.agencyCd || ''})`}
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
                  value={detailData.teamPersonNm || ''}
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
                  value={detailData.promoTeamNm || ''}
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
                  value={detailData.promoPersonNm || ''}
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
                  value={detailData.orderCd || ''}
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
                  value={detailData.orderUserNm || ''}
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
                  value={detailData.orderAddress1 || ''}
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
                  value={detailData.orderCellPhone || ''}
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
                <tr className="text-center small">
                  <th style={{ width: '150px' }}>상품</th>
                  <th style={{ width: '80px' }}>1회<br/>투입<br/>수량</th>
                  <th style={{ width: '80px' }}>주간<br/>총수량</th>
                  <th style={{ width: '100px' }}>용품기간</th>
                  <th style={{ width: '100px' }}>계약구분</th>
                  <th style={{ width: '80px' }}>판촉홉<br/>/중담품</th>
                  <th style={{ width: '80px' }}>본사홉<br/>/대리점홉</th>
                  <th style={{ width: '100px' }}>마감홉수</th>
                  <th style={{ width: '80px' }}>해피콜<br/>조정홉수</th>
                  <th style={{ width: '120px' }}>수정사유</th>
                  <th style={{ width: '100px' }}>해피콜<br/>날짜/결과</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{detailData.goodsOptionNm || ''}</td>
                  <td className="text-center">{detailData.quantity || ''}</td>
                  <td className="text-center">{detailData.weekQty || ''}</td>
                  <td className="text-center">{detailData.contractPeriod || ''}개월</td>
                  <td className="text-center">{detailData.orderKindCd || ''}</td>
                  <td className="text-center">{detailData.agencyHob || ''}</td>
                  <td className="text-center">{detailData.hqHob || ''}</td>
                  <td>
                    <Form.Control
                      type="number"
                      step="0.1"
                      size="sm"
                      value={editableData.actualHob}
                      onChange={(e) => handleInputChange('actualHob', e.target.value)}
                      className="text-center"
                    />
                  </td>
                  <td>
                    <Form.Control
                      type="number"
                      step="0.1"
                      size="sm"
                      value={editableData.hcHob}
                      onChange={(e) => handleInputChange('hcHob', e.target.value)}
                      className="text-center"
                    />
                  </td>
                  <td>
                    <Form.Control
                      as="textarea"
                      rows={1}
                      size="sm"
                      value={editableData.saveRemark}
                      onChange={(e) => handleInputChange('saveRemark', e.target.value)}
                      placeholder="수정사유 입력"
                    />
                  </td>
                  <td className="text-center small">
                    {detailData.hcDt || ''}<br/>
                    <Form.Select
                      size="sm"
                      value={editableData.hcStatus}
                      onChange={(e) => handleInputChange('hcStatus', e.target.value)}
                      style={{ fontSize: '11px' }}
                    >
                      <option value="">선택</option>
                      <option value="10">미확인</option>
                      <option value="11">정상</option>
                      <option value="12">부재중</option>
                      <option value="13">상이건</option>
                      <option value="14">결번</option>
                      <option value="15">내용변경</option>
                    </Form.Select>
                  </td>
                </tr>
              </tbody>
            </Table>
          </div>
        </div>

        {/* ✅ 해피콜 상세 정보 */}
        <div className="border rounded p-3">
          <h6 className="fw-bold mb-3">
            <i className="bi bi-telephone me-2"></i>
            해피콜 상세 정보
          </h6>
          <Row>
            <Col md={6}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">해피콜 상담내용</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  size="sm"
                  value={editableData.hcContent}
                  onChange={(e) => handleInputChange('hcContent', e.target.value)}
                  placeholder="해피콜 상담내용 입력"
                />
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">담당 해피콜 결과확인</Form.Label>
                <Form.Select
                  size="sm"
                  value={editableData.hcActionStatus}
                  onChange={(e) => handleInputChange('hcActionStatus', e.target.value)}
                >
                  <option value="">선택</option>
                  <option value="10">미확인</option>
                  <option value="11">정상</option>
                  <option value="12">부재중</option>
                  <option value="13">상이건</option>
                  <option value="14">결번</option>
                  <option value="15">내용변경</option>
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={3}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-bold mb-1">중단일</Form.Label>
                <Form.Control
                  type="text"
                  size="sm"
                  value={detailData.stopDt || ''}
                  readOnly
                  className="bg-white"
                />
              </Form.Group>
            </Col>
          </Row>
          <Row>
            <Col md={12}>
              <Form.Group>
                <Form.Label className="small fw-bold mb-1">담당 의견/대리점 소명</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  size="sm"
                  value={editableData.hcAction}
                  onChange={(e) => handleInputChange('hcAction', e.target.value)}
                  placeholder="담당 의견 또는 대리점 소명 입력"
                />
              </Form.Group>
            </Col>
          </Row>
        </div>
      </Modal.Body>

      <Modal.Footer className="bg-light">
        <Button variant="secondary" onClick={onHide}>
          <i className="bi bi-x-circle me-1"></i>
          닫기
        </Button>
        <Button variant="primary" onClick={handleSave}>
          <i className="bi bi-save me-1"></i>
          저장
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default PromotionDetailModal;