import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/MapPage.css';
import { FaSearch } from 'react-icons/fa';
import {
  Row,
  Col,
  Button,
  Form,
  Card,
  InputGroup,
  Collapse,
  FormControl, Modal, ListGroup
} from 'react-bootstrap';
import axios from 'axios';
const { kakao, XLSX } = window;

const MapEdit = () => {
  const [showContent, setShowContent] = useState(true);
  const mapRef = useRef(null);
  const mapInstance = useRef(null);
  const custnoInputRef = useRef(null);
  const custNameInputRef = useRef(null);
  const sabunDisplayRef = useRef(null);
  let [overlaysVisible, setOverlaysVisible] = useState(true);
  let [regionInfo, setRegionInfo] = useState('');
  let [categoryColors, setCategoryColors] = useState({
        "서울특별시": "#FF9999", 	// 연한 빨강빛 (부드러운 핑크톤)
        "경기도": "#99FF99",		// 연한 연두색 (맑고 부드러운 초록)
        "인천광역시": "#9999FF",		// 연한 파란색 (라벤더 느낌의 파랑)
        "부산광역시": "#FFCC99",		// 살구색 계열 (밝은 주황빛)
        "대구광역시": "#CCFFCC",		// 연녹색 (상쾌하고 은은한 민트 느낌)
        "대전광역시": "#CCCCFF",		// 연보라색 (청보라 계열의 연한 톤)
        "광주광역시": "#FFFF99",		// 연한 노랑색 (레몬처럼 밝고 상큼한 색)
        "울산광역시": "#FF99FF",		// 연보라핑크 (핑크와 라벤더 중간 느낌)
        "세종특별자치시": "#99FFFF",	// 연한 하늘색 (민트 하늘색 느낌)
        "강원도": "#FFD700",		// 선명한 금색 (골드톤, 눈에 띔)
        "충청남도": "#87CEEB",		// 하늘색 (SkyBlue – 파란 하늘 느낌)
        "충청북도": "#B0E0E6",		// 페일블루 (파란 회색빛 섞인 느낌, 고요한 느낌)
        "전라남도": "#F4A460",		// 연한 갈색 (사암색 – 자연스러운 느낌)
        "전라북도": "#FFA07A",		// 연한 살색 (살구빛 계열, 따뜻함)
        "경상남도": "#E6E6FA",		// 라벤더 (연보라색, 아주 부드러운 느낌)
        "경상북도": "#D8BFD8",		// 옅은 자주색 (연한 자색/보라, 부드럽고 은은함)
        "제주특별자치도": "#90EE90"	// 연한 초록색 (LimeGreen – 상큼한 자연 느낌)
      });
  const [filterCategory1Options, setFilterCategory1Options] = useState([]);
  const [filterCategory2Options, setFilterCategory2Options] = useState([]);
  const navigate = useNavigate();

  // 지도 관련 상태
  let [allPolygons, setAllPolygons] = useState([]);
  let [allOverlays, setAllOverlays] = useState([]);
  let [overlayObjects, setOverlayObjects] = useState([]);
  let [labelOnlyOverlays, setLabelOnlyOverlays] = useState([]);
  const baseURL = process.env.REACT_APP_API_URL;

  useEffect(() => {

      // 지도가 그려져 있으면 다시 작동안함
      if(mapInstance.current) return;

      checkSabun();

      const options = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 6
      };

      const map = new kakao.maps.Map(mapRef.current, options);

      mapInstance.current = map;

      getLocationByIP(map);
      loadAllRegions();

    }, []);

  const checkSabun = () => {

    const sabun = sessionStorage.getItem("sabun");

    if (sabun === null || sabun === '') {
    // sabun이 없거나, 빈 문자열일 때
        const promptSabun = prompt("사번을 입력해주세요");

        axios.get(`${baseURL}/api/region/checkSabun?sabun=${encodeURIComponent(promptSabun)}`)
            .then(res => {
                if (res.data.valid) {
                    sessionStorage.setItem('sabun', promptSabun);
                    setRegionInfo(promptSabun);
                } else {
                    alert("유효하지 않은 사번입니다.");
                    if (window.history.length > 2) {
                        navigate(-1);
                    } else {
                        navigate('/login');
                    }
                }
            })
            .catch(err => {
                console.error("사번 확인 중 오류:", err);
                alert("사번 확인 중 오류가 발생했습니다.");
            });
    } else {
        setRegionInfo(sabun);
    }

  };

  const setSabun = () => {
    return {__html: `<Card.Title>대리점별 권역 지정 &nbsp;&nbsp; 인증사번 : ${regionInfo}</Card.Title>`};
  };

  const toggleOverlayDetail = () => {

    const map = mapInstance.current;

    if(!map) return;

    const newVisible = !overlaysVisible;
    setOverlaysVisible(newVisible);

    overlayObjects.forEach(o => o.setMap(newVisible ? map : null));
    labelOnlyOverlays.forEach(o => o.setMap(newVisible ? null : map));
  };

  const getLocationByIP = (mapInstance) => {
    // 2. IP 기반 위치 조회
    axios.get('https://ip-api.io/json/')
      .then(res => {
        const { latitude, longitude } = res.data;
        const userLoc = new window.kakao.maps.LatLng(latitude, longitude);
        mapInstance.setCenter(userLoc);
      })
      .catch(err => {
        console.warn("IP 위치 조회 실패:", err);
        const fallback = new kakao.maps.LatLng(37.5561, 126.9369);
        mapInstance.setCenter(fallback); // fallback
      });
  };

  const loadAllRegions = () => {

    const gubunType = sessionStorage.getItem('gubunType');

    axios.get(`${baseURL}/api/region/getRegions`, {
      params: { gubunType: gubunType }
    })
    .then(response => {
      drawRegions(response.data);
    })
    .catch(error => {
      console.error('대리점 데이터를 불러오는 중 오류:', error);
    });
  };

  const drawRegions = (data, filter = {}) => {

    const map = mapInstance.current;
    if (!map) return;
    
    // 1. overlay 초기화
    clearMap();

    const newPolygons = [];
    const newOverlays = [];
    const newOverlayObjects = [];
    const newLabelOnlyOverlays = [];

    data.forEach(region => {
      if (!region.polygon.length) return;

      // 3. 색상 지정
      
      const fillColor = categoryColors[region.category1] || "#FFAAAA";

      // 4. 경로 및 폴리곤 생성
      const polygonPaths = region.polygon;

      const path = polygonPaths.map(([lat, lng]) =>
        new kakao.maps.LatLng(lat, lng)
      );

      const polygon = new kakao.maps.Polygon({
        map,
        path,
        strokeWeight: 2,
        strokeColor: '#FF0000',
        fillColor,
        fillOpacity: 0.5
      });

      // 5. 라벨 중앙 좌표 계산
      const center = path.reduce(
        (sum, latLng) => [sum[0] + latLng.getLat(), sum[1] + latLng.getLng()],
        [0, 0]
      );
      
      const centerPos = new kakao.maps.LatLng(center[0] / path.length, center[1] / path.length);

      // 6. 상세 오버레이 content
      let label;
      if (sessionStorage.getItem('username') === 'superadmin') {
        label = `
          <div style="background:white;padding:5px;border:1px solid black;border-radius:5px;font-size:12px;">
            ${region.custNo} ${region.custName}<br/>
            ${region.tel}<br/>
            대리점 핸드폰1 : ${region.phoneMobile1 || '-'}<br/>
            대리점 핸드폰2 : ${region.phoneMobile2 || '-'}<br/>
            담당자 : ${region.salesMan}<br/>
            담당자 핸드폰 : ${region.mobileNbr || '-'}<br/>
            팀 : ${region.deptName}<br/>
            지역대분류 : ${region.category1}<br/>
            지역중분류 : ${region.category2}<br/>
            상담시간 : ${region.openTime}
          </div>`;
      } else {
        label = `
          <div style="background:white;padding:5px;border:1px solid black;border-radius:5px;font-size:12px;">
            ${region.custNo} ${region.custName}<br/>
            ${region.tel}<br/>
            담당자 : ${region.salesMan}<br/>
            팀 : ${region.deptName}<br/>
            지역대분류 : ${region.category1}<br/>
            지역중분류 : ${region.category2}
          </div>`;
      }

      const overlay = new kakao.maps.CustomOverlay({
        position: centerPos,
        content: label,
        yAnchor: 1.5
      });

      const labelOnlyOverlay = new kakao.maps.CustomOverlay({
        position: centerPos,
        content: `
          <div style="background:white;padding:3px 7px;border:1px solid black;border-radius:3px;font-size:12px;font-weight:bold;">
            ${region.custNo} ${region.custName}
          </div>`,
        yAnchor: 1.5
      });

      newPolygons.push(polygon);
      newOverlays.push(overlay);
      newOverlayObjects.push(overlay);
      newLabelOnlyOverlays.push(labelOnlyOverlay);

      // ✅ 현재 상태에 따라 표시
      if (overlaysVisible) {
        overlay.setMap(map);
        labelOnlyOverlay.setMap(null);
      } else {
        overlay.setMap(null);
        labelOnlyOverlay.setMap(map);
      }

      // 상태를 한 번에 갱신
      setAllPolygons(newPolygons);
      setAllOverlays(newOverlays);
      setOverlayObjects(newOverlayObjects);
      setLabelOnlyOverlays(newLabelOnlyOverlays);
    });
};

  const clearMap = () => {

    console.log(allPolygons);

    // 상태로 선언한 각 배열을 비우고 map 객체에서 제거
    allPolygons.forEach(polygon => polygon.setMap(null));
    setAllPolygons([]); // 상태 초기화

    allOverlays.forEach(overlay => overlay.setMap(null));
    setAllOverlays([]); // 상태 초기화

    overlayObjects.forEach(obj => obj.setMap(null));
    setOverlayObjects([]); // 상태 초기화

    labelOnlyOverlays.forEach(label => label.setMap(null));
    setLabelOnlyOverlays([]); // 상태 초기화

  };

  return (
    <div className="mt-0 justify-content-start">
      <Card className="form-shrink px-3 pt-3 pb-1 shadow-sm" style={{ fontSize: '13px' }}>
        <Card.Body className="p-0">
          <div dangerouslySetInnerHTML={setSabun()}></div>
          <Collapse in={showContent}>
            <div>
              {/* 주소 검색 영역 */}
              <Form onSubmit={e => e.preventDefault()}>
                <Row className="align-items-center g-3 mb-3">
                  <Col xs={12} md={2}>
                    <Form.Group controlId="custnoInput" className="d-flex align-items-center gap-2">
                      <Form.Control
                        type="text"
                        placeholder="거래처 코드 입력"
                        ref={custnoInputRef}
                      />
                    </Form.Group>
                  </Col>

                  <Col xs={12} md={2}>
                    <Form.Group controlId="custNameInput" className="d-flex align-items-center gap-2">
                      <Form.Control
                        type="text"
                        placeholder="거래처명 입력"
                        ref={custNameInputRef}
                      />
                      <InputGroup.Text 
                        style={{
                            cursor: 'pointer',
                            padding: '10px 0px',  // 더 큰 패딩
                            fontSize: '24px',      // 더 큰 폰트 크기
                            minWidth: '50px',      // 최소 너비 지정
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center' 
                         }}
                      >
                        <FaSearch />
                      </InputGroup.Text>
                    </Form.Group>
                  </Col>

                  <Col xs="auto">
                    <Button variant="primary">
                      권역지정(초기화)
                    </Button>
                  </Col>

                  <Col xs="auto">
                    <Button variant="primary" >
                      저장
                    </Button>
                  </Col>

                  <Col xs="auto">
                    <Button variant="warning" className="text-white">
                      대리점별 권역 지정 여부
                    </Button>
                  </Col>

                  <Col xs="auto">
                    <Button variant="secondary" style={{ backgroundColor: '#6c757d' }} onClick={toggleOverlayDetail}>
                      거래처정보 {overlaysVisible ? '숨기기' : '보이기'}
                    </Button>
                  </Col>

                  <Col xs="auto">
                    <span>권역 지정시 마커를 다시 클릭하면 지정된 마커를 지울 수 있습니다.</span>
                  </Col>
                </Row>
              </Form>              
            </div> 
          </Collapse>
          {/* 카드 바닥의 토글 버튼 */}
          <div className="text-center py-2">
            <Button
              variant="light"
              size="sm"
              onClick={() => setShowContent(v => !v)}
            >
              {showContent ? '내용 숨기기' : '내용 보이기'}{' '}
              <span
                style={{
                  display: 'inline-block',
                  transition: 'transform .2s',
                  transform: showContent ? 'rotate(180deg)' : 'rotate(0deg)',
                }}
              >
                ▼
              </span>
            </Button>
          </div>
        </Card.Body>
      </Card>

      {/* 지도 표시 영역 */}
      <div ref={mapRef} style={{ width: '100%', height: '800px', marginTop: '20px' }}></div>
    </div>
  );
};

export default MapEdit;
