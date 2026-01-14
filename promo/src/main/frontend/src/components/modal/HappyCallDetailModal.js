// components/PromotionDetailModal.jsx
import React, { useState, useEffect, useMemo } from 'react';
import { Modal, Button, Form, Row, Col, Table } from 'react-bootstrap';
import axios from 'axios';
import Swal from 'sweetalert2';
import { FaAngleDoubleLeft, FaAngleDoubleRight } from "react-icons/fa";
import { IoClose } from "react-icons/io5";
import { FaSave } from "react-icons/fa";

/**
 * 판촉실적 상세 팝업 컴포넌트
 * @param {boolean} show - 모달 표시 여부
 * @param {function} onHide - 모달 닫기 함수
 * @param {object} rowData - 선택된 행 데이터
 * @param {array} originalData - 전체 원본 데이터 (no, orderCd 정렬됨)
 * @param {function} onSave - 저장 후 콜백 함수
 */
const PromotionDetailModal = ({ show, onHide, rowData, originalData = [], onSave, showSaveButton }) => {
  // ✅ 상태 관리
  const [detailData, setDetailData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentOrderCd, setCurrentOrderCd] = useState('');  // ✅ 현재 조회 중인 orderCd
  const [currentIndex, setCurrentIndex] = useState(-1);  // ✅ orderCdList에서의 현재 인덱스
  const [promoTeamList, setPromoTeamList] = useState([]);  // 판촉팀 목록 state 추가
  const [selectedPromoTeamCd, setSelectedPromoTeamCd] = useState('');
  const [selectedPromoPersonNm, setSelectedPromoPersonNm] = useState('');
  const [selectedOrderCellPhone, setSelectedOrderCellPhone] = useState('');

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
      
      const dataArray = Array.isArray(response.data) ? response.data : [response.data];
      setDetailData(dataArray);


      // ✅ 첫 번째 데이터에서 판촉팀 코드 추출하여 select에 세팅
      if (dataArray.length > 0 && dataArray[0].promoPersonNm) {
        setSelectedPromoPersonNm(dataArray[0].promoPersonNm);
      } else {
        // 데이터가 없거나 판촉팀 정보가 없으면 초기화
        setSelectedPromoPersonNm('');
      }

      if (dataArray.length > 0 && dataArray[0].orderCellPhone) {
        setSelectedOrderCellPhone(dataArray[0].orderCellPhone);
      } else {
        // 데이터가 없거나 전화번호 정보가 없으면 초기화
        setSelectedOrderCellPhone('');
      }
      
      // ✅ 편집 가능한 필드 초기화 - 배열의 각 항목에 대해
      const initialEditableData = dataArray.map(item => ({
        orderCd: item.orderCd,           // ✅ 필수: 주문코드
        orderSeq: item.orderSeq,         // ✅ 필수: 주문순번
        actualHob: item.actualHob || '',
        orderKindCd: item.orderKindCd || '1',
        saveRemark: item.saveRemark || '',
        hcHob: item.hcHob || '',
        hcStatus: item.hcStatus || '',
        hcContent: item.hcContent || '',
        hcActionStatus: item.hcActionStatus || '',
        hcAction: item.hcAction || ''
      }));
      
      setEditableData(initialEditableData);
      
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
    
    // originalData에서 해당 orderCd의 첫 번째 데이터 찾기
    const nextRowData = originalData.find(item => item.orderCd === nextOrderCd);
    
    if (nextRowData) {
      setCurrentOrderCd(nextOrderCd);
      setCurrentIndex(nextIndex);
      fetchDetailData(nextRowData);
    }
  };

  /**
   * ✅ 마감홉수 입력값 변경 핸들러 (숫자만 허용)
   * @param {number} index - 배열 인덱스
   * @param {string} field - 필드명
   * @param {string} value - 입력값
   */
  const handleInputChange = (index, field, value) => {
    // ✅ actualHob 필드인 경우 숫자 검증
    if (field === 'actualHob') {
      // 빈 값은 허용
      if (value === '') {
        setEditableData(prev => {
          const newData = [...prev];
          newData[index] = {
            ...newData[index],
            [field]: ''
          };
          return newData;
        });
        return;
      }

      // ✅ 숫자와 소수점만 허용하는 정규식
      const numberRegex = /^[0-9]*\.?[0-9]*$/;
      
      // 정규식 통과하지 못하면 무시
      if (!numberRegex.test(value)) {
        return;
      }

      // ✅ 소수점 첫째자리까지만 허용
      const parts = value.split('.');
      if (parts.length > 2) {
        return; // 소수점이 2개 이상이면 무시
      }
      if (parts[1] && parts[1].length > 1) {
        return; // 소수점 둘째자리 입력 시도 무시
      }

      // ✅ 최대값 검증 (999.9)
      const numValue = parseFloat(value);
      if (!isNaN(numValue) && numValue > 999.9) {
        Swal.fire({
          icon: 'warning',
          title: '입력 오류',
          text: '마감홉수는 999.9를 초과할 수 없습니다.',
          confirmButtonText: '확인'
        });
        return;
      }
    }

    // ✅ 값 업데이트
    setEditableData(prev => {
      const newData = [...prev];
      newData[index] = {
        ...newData[index],
        [field]: value
      };
      return newData;
    });
  };

  /**
   * 저장 버튼 클릭
   */
  const handleSave = async () => {
    try {

      // ✅ 로딩 표시
      Swal.fire({
        title: '저장 중...',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      const happyCallPersonCd = sessionStorage.getItem("teamPersonCd");

      // ✅ editableData 배열의 각 항목에 판촉팀 정보 추가
      // 모든 제품에 대해 동일한 판촉팀과 판촉사원 정보가 적용됨
      const saveData = editableData.map(item => ({
        ...item,                                    // 기존 편집 데이터 (orderCd, orderSeq, actualHob 등)
        hcContent: item.hcContent ? item.hcContent.replace(/(?:\r\n|\r|\n)/g, '<br>') : item.hcContent,
        teamPersonCd: happyCallPersonCd,
        promoTeamCd: selectedPromoTeamCd,            // 선택된 판촉팀 코드
        promoPersonNm: selectedPromoPersonNm,        // 입력된 판촉사원 이름
        orderCellPhone : selectedOrderCellPhone
      }));

      

      await axios.post('/api/promo/saveHappyDetail', saveData);

      // ✅ 성공 메시지
      Swal.fire({
        icon: 'success',
        title: '저장 완료',
        text: '판촉실적이 성공적으로 저장되었습니다.',
        confirmButtonText: '확인',
        timer: 2000,  // ✅ 0.5초 후 자동 종료 (밀리초 단위)
        timerProgressBar: true  // ✅ 타이머 진행 바 표시 (선택사항)
      }).then(() => {
        //onSave(); // 목록 재조회
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
          판촉실적 내역
          {/* ✅ 현재 위치 표시 */}
          <small className="text-muted ms-3" style={{ fontSize: '14px' }}>
            ({currentIndex + 1} / {orderCdList.length})
          </small>
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
            <i className="bi bi-building me-2"></i>
            대리점 및 판촉팀 정보
            {/* ✅ 저장/마감 상태 표시 */}
            {detailData && detailData.length > 0 && (
              <>
                {/* 저장완료 표시 */}
                {detailData?.[0]?.hcSaveYn === '1' && (
                  <span className="badge bg-success ms-2" style={{ fontSize: '12px' }}>
                    저장완료
                  </span>
                )}
                {detailData?.[0]?.hcSaveYn !== '1' && (
                  <span className="badge bg-warning ms-2" style={{ fontSize: '12px' }}>
                    미저장
                  </span>
                )}
              </>
            )}
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
                  onChange={(e) => {
                    setSelectedPromoPersonNm(e.target.value);
                  }}
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
                  onChange={(e) => {
                    setSelectedOrderCellPhone(e.target.value);
                  }}
                  style={{
                    borderColor: (detailData?.[0]?.orderCellPhone) ? '' : '#dc3545',
                    color: (detailData?.[0]?.orderCellPhone) ? '' : '#dc3545',
                    borderWidth: '2px'
                  }}
                  value={selectedOrderCellPhone}
                  readOnly
                  placeholder="전화번호 없음"
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
          <div className="table-responsive" style={{ fontSize: '13px'}}>
            <Table bordered hover size="sm" className="mb-0">
              <thead className="table-light">
                <tr className="text-center align-middle small">
                  <th style={{ width: '100px' }}>상품</th>
                  <th style={{ width: '45px' }}>1회투입<br/>수량</th>
                  <th style={{ width: '50px' }}>배송요일</th>
                  <th style={{ width: '35px' }}>주간<br/>총수량</th>
                  <th style={{ width: '50px' }}>음용기간</th>
                  <th style={{ width: '50px' }}>계약구분</th>
                  <th style={{ width: '80px' }}>판촉물 / 중단일<br/>(중단사유)</th>
                  <th style={{ width: '50px' }}>마감홉수</th>
                  <th style={{ width: '60px' }}>해피콜<br/>조정홉수</th>
                  <th style={{ width: '100px' }}>해피콜 날짜<br/>해피콜 결과</th>
                  <th style={{ width: '280px' }}>해피콜<br/>상담내용</th>
                </tr>
              </thead>
              <tbody class="align-middle" style={{ fontSize: '13px'}}>
                {/* ✅ detailData 배열을 map으로 반복 렌더링 */}
                {detailData.map((item, index) => (
                  <tr key={index}>
                  <td className="text-center" style={{color: (item.goodsOptionNm.includes('매칭안됨')) ? '#dc3545' : ''}}>{item.goodsOptionNm || ''}</td>
                  <td className="text-center">{item.quantity || ''}</td>
                  <td className="text-center">{item.weekRemark || ''}</td>
                  <td className="text-center">{item.weekQty || ''}</td>
                  <td className="text-center" style={{color: (Number(item.contractPeriod) < 12) ? '#dc3545' : ''}}>{item.contractPeriod || ''} 개월</td>
                  <td className="text-center">{item.orderKindCdNm || ''}</td>
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
                  <td className="text-center">{item.hcHob || ''}</td>
                  <td>
                    <Form.Control
                      rows={1}
                      size="sm"
                      value={editableData[index]?.hcHob || ''}
                      onChange={(e) => handleInputChange(index, 'hcHob', e.target.value)}
                      onFocus={(e) => e.target.select()}  // ✅ 포커스 시 전체 선택
                      type="number"
                      step="0.5"      // ✅ 0.1 단위로 증감
                      min="0"         // ✅ 최소값 0
                      max="999.9"     // ✅ 최대값 999.9
                      className="text-center"
                      placeholder="0.0"
                      style={{ 
                        textAlign: 'center',
                        // ✅ MozAppearance 제거 - Firefox에서도 스피너 표시
                        WebkitAppearance: 'auto',  // Chrome/Safari 스피너 표시
                        appearance: 'auto'          // 표준 속성
                      }}
                    />
                  </td>
                  <td className="text-center">
                    {item.hcDt || ''}
                    <br/>
                    <Form.Select
                      size="sm"
                      value={editableData[index]?.hcStatus || '10'}
                      onChange={(e) => handleInputChange(index, 'hcStatus', e.target.value)}
                      style={{ 
                        fontSize: '14px',
                        padding: '0px 20px'
                      }}
                    >
                      <option value="10">미확인</option>
                      <option value="11">정상</option>
                      <option value="12">부재중</option>
                      <option value="13">상이건</option>
                      <option value="14">결번</option>
                      <option value="15">내용변경</option>
                    </Form.Select>
                  </td>
                  <td class="text-center">
                    <textarea 
                      style={{ fontSize: '13px', whiteSpace: "pre-wrap"}}
                      size="sm"
                      value={editableData[index]?.hcContent.replace(/<br\s*\/?>/gi, '\r\n') || ''}
                      class="form-control" 
                      onChange={(e) => handleInputChange(index, 'hcContent', e.target.value)}
                      rows="4">
                    </textarea>
                  </td>
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
            <Button 
              variant="outline-secondary" 
              onClick={handlePrevious}
              disabled={isPreviousDisabled}
              title="이전 주문건"
              style={{ width: '120px' }}
            >
              <i className="bi bi-chevron-double-left me-1">
                <FaAngleDoubleLeft className="me-1" />
                이전
              </i>
            </Button>
            <Button 
              variant="outline-secondary" 
              onClick={handleNext}
              disabled={isNextDisabled}
              title="다음 주문건"
              style={{ width: '120px' }}
            >
              
              <i className="bi bi-chevron-double-right ms-1">
                <FaAngleDoubleRight className="me-1" />
                다음 
              </i>
            </Button>
          </div>
          
          {/* 오른쪽: 닫기/저장 버튼 */}
          <div className="d-flex gap-2">
            <Button 
              variant="secondary" 
              onClick={onHide}
              style={{ width: '120px' }}
            >
              <IoClose /> 닫기
            </Button>

            {showSaveButton && (
              <Button 
                variant="primary" 
                onClick={handleSave}
                style={{ width: '120px' }}
              >
                <FaSave /> 저장
              </Button>
            )}
          </div>
        </div>
      </Modal.Footer>
      
    </Modal>

  );
};

export default PromotionDetailModal;