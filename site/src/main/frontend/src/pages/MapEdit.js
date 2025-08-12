import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/MapPage.css';
import { FaSearch } from 'react-icons/fa';
import ReactDOM from 'react-dom/client';
import RegionOverlay from '../components/RegionOverlay';
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
import StatusExcelPopup from '../components/modal/StatusExcelPopup';
import StatusSearchPopup from '../components/modal/StatusSearchPopup';
import axios from 'axios';
const { kakao } = window;

const MapEdit = () => {
  const [showContent, setShowContent] = useState(true);
  const mapRef = useRef(null);
  const mapInstance = useRef(null);
  const custnoInputRef = useRef(null);
  const custNameInputRef = useRef(null);
  const sabunDisplayRef = useRef(null);
  const selectedPolygon = useRef(null);
  const [editingPolygon, setEditingPolygon] = useState(null);
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
  const navigate = useNavigate();
  const [selectedCustomer, setSelectedCustomer] = useState(null); // 선택된 거래처 정보
  const [regionTitle, setRegionTitle] = useState("대리점별 권역 지정");
  const [selectedRegionId, setSelectedRegionId] = useState(null);

  // 지도 관련 상태
  let [allPolygons, setAllPolygons] = useState([]);
  let [allOverlays, setAllOverlays] = useState([]);
  let [overlayObjects, setOverlayObjects] = useState([]);
  let [labelOnlyOverlays, setLabelOnlyOverlays] = useState([]);

  const [drawingPolygon, setDrawingPolygon] = useState(null);
  const [polyline, setPolyline] = useState(null);
  const [markers, setMarkers] = useState([]);
  const [polygonCoords, setPolygonCoords] = useState([]);
  const mapClickListener = useRef(null);
  const allRegionsRef = useRef([]);

  useEffect(() => {

      // 지도가 그려져 있으면 다시 작동안함
      if(mapInstance.current) return;

      checkSabun();

      const options = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 8
      };

      const map = new kakao.maps.Map(mapRef.current, options);

      mapInstance.current = map;

      getLocationByIP(map);
      loadAllRegions();

    }, []);

  // 데이터 받는 함수 (개선됨)
  const handleCustomerSelected = (selectedCustomer) => {
    const customerData = selectedCustomer; // 선택된 데이터
    
    // 데이터 유효성 검사
    if (!customerData || !customerData.custNo) {
      console.error("유효하지 않은 데이터:", customerData);
      return;
    }
    
    // 선택된 거래처 상태 업데이트
    setSelectedCustomer(customerData);
    
    // 입력 필드에 자동 입력 (안전한 방식)
    if (custnoInputRef.current) {
      custnoInputRef.current.value = customerData.custNo;
    }
    if (custNameInputRef.current) {
      custNameInputRef.current.value = customerData.custName;
    }
  };

  // 선택된 거래처 정보 초기화
  const clearSelectedCustomer = () => {
    setSelectedCustomer(null);
    if (custnoInputRef.current) {
      custnoInputRef.current.value = '';
    }
    if (custNameInputRef.current) {
      custNameInputRef.current.value = '';
    }
  };

  const checkSabun = () => {

    const sabun = sessionStorage.getItem("sabun");

    if (sabun === null || sabun === '') {
    // sabun이 없거나, 빈 문자열일 때
        const promptSabun = prompt("사번을 입력해주세요");

        axios.get(`/api/region/checkSabun?sabun=${encodeURIComponent(promptSabun)}`)
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
    return {
      __html: `<Card.Title id="headerTitle">${regionTitle} &nbsp;&nbsp; 인증사번 : ${regionInfo}</Card.Title>`
    };
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

    axios.get(`/api/region/getRegions`, {
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

      allRegionsRef.current.push({ id: region.id, custName: region.custName, custNo: region.custNo, polygon: polygon });

      // 5. 라벨 중앙 좌표 계산
      const center = path.reduce(
        (sum, latLng) => [sum[0] + latLng.getLat(), sum[1] + latLng.getLng()],
        [0, 0]
      );
      
      const centerPos = new kakao.maps.LatLng(center[0] / path.length, center[1] / path.length);

      // 오버레이 content를 위한 container 생성
      const container = document.createElement('div');

      // ReactDOM을 통해 React 컴포넌트를 container에 렌더링
      const root = ReactDOM.createRoot(container);
      root.render(
        <RegionOverlay
          region={region}
          isAdmin={sessionStorage.getItem('username')?.includes('superadmin')}
          onDelete={deleteRegion}
          onUpdate={updateRegion}
        />
      );

      // Kakao CustomOverlay에 삽입
      const overlay = new kakao.maps.CustomOverlay({
        position: centerPos,
        content: container,
        yAnchor: 1.5
      });

      const labelOnlyOverlay = new kakao.maps.CustomOverlay({
        position: centerPos,
        content: `
          <div style="background:white;padding:8px;border:1px solid #aaa;border-radius:8px;font-size:12px;line-height:1.4;min-width:100px;font-weight:bold;">
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

  const showStatusExcelPoup = () => {
    window.dispatchEvent(new Event('showStatusExcelPoup'));
  };

  const showStatusSearchPopup = () => {
    window.dispatchEvent(new Event('showStatusSearchPopup'));
  };

  // 팝업에 있는 데이터를 부모창으로 가져오는 방법
  const getData = (data) => {
    setSelectedCustomer(data);
    
    handleCustomerSelected(data);
    
  };

  const startDrawing = () => {
    const map = mapInstance.current;
    if (!map) return;

    setRegionTitle("대리점별 권역 지정 (범위지정중)");

    // 수정 중일 경우 방지 (추가 조건은 직접 구현 필요)
    if (drawingPolygon || polyline) {
      const isEditing = false; // 필요 시 editing 상태 추가
      if (isEditing) {
        alert("현재 대리점 수정 중입니다. 저장 후 다시 시도해주세요.");
        setRegionTitle("대리점별 권역 지정 (기존대리점 수정중)");
        return;
      }
    }

    // 기존 클릭 이벤트 제거
    if (mapClickListener.current) {
      kakao.maps.event.removeListener(map, 'click', mapClickListener.current);
      mapClickListener.current = null;
    }

    // 기존 객체 제거 및 상태 초기화
    if (drawingPolygon) {
      drawingPolygon.setMap(null);
      setDrawingPolygon(null);
    }
    if (polyline) {
      polyline.setMap(null);
      setPolyline(null);
    }
    markers.forEach(marker => marker.setMap(null));
    setMarkers([]);
    setPolygonCoords([]);

    // 새 Polygon 및 Polyline 생성
    const newPolygon = new kakao.maps.Polygon({
      strokeWeight: 2,
      strokeColor: '#0000FF',
      fillColor: '#AAAAFF',
      fillOpacity: 0.5,
      map: map
    });
    setDrawingPolygon(newPolygon);

    const newPolyline = new kakao.maps.Polyline({
      strokeWeight: 2,
      strokeColor: '#0000FF',
      strokeOpacity: 0.7,
      map: map
    });
    setPolyline(newPolyline);

    // 지도 클릭 핸들러 정의
    const clickHandler = (mouseEvent) => {
      const clickPosition = mouseEvent.latLng;

      const marker = new kakao.maps.Marker({
        position: clickPosition,
        map,
        draggable: true
      });

      setMarkers(prev => {
        const newMarkers = [...prev, marker];
        const updatedCoords = newMarkers.map(m => m.getPosition());
        setPolygonCoords(updatedCoords);
        newPolygon.setPath(updatedCoords);
        newPolyline.setPath(updatedCoords);
        return newMarkers;
      });

      // 마커 드래그 이벤트
      kakao.maps.event.addListener(marker, 'dragend', () => {
        setMarkers(current => {
          const updated = [...current];
          const updatedCoords = updated.map(m => m.getPosition());
          setPolygonCoords(updatedCoords);
          newPolygon.setPath(updatedCoords);
          newPolyline.setPath(updatedCoords);
          return updated;
        });
      });

      // 마커 클릭 → 삭제
      kakao.maps.event.addListener(marker, 'click', () => {
        setMarkers(current => {
          const remaining = current.filter(m => m !== marker);
          marker.setMap(null);
          const coords = remaining.map(m => m.getPosition());
          setPolygonCoords(coords);
          newPolygon.setPath(coords);
          newPolyline.setPath(coords);
          return remaining;
        });
      });
    };

    // 클릭 리스너 등록
    kakao.maps.event.addListener(map, 'click', clickHandler);
    mapClickListener.current = clickHandler;

    alert("지도를 클릭하여 권역을 지정하세요.\n마커를 다시 클릭하면 삭제됩니다.\n드래그로 위치 이동도 가능합니다.");
  };

  // 새 다각형 저장하기 (겹치는지 확인 후 저장)
  const savePolygon = () => {
    if (polygonCoords.length < 3) {
      alert("적어도 3개 이상의 점을 선택해야 합니다.");
      return;
    }

    const newPolygonCoords = markers.map(marker => [
      marker.getPosition().getLat(), 
      marker.getPosition().getLng()
    ]);
    setPolygonCoords(newPolygonCoords);
    
    if (drawingPolygon) {
      drawingPolygon.setPath(
        newPolygonCoords.map(coord => new kakao.maps.LatLng(coord[0], coord[1]))
      );
    }
    
    // polyline이 있을때만 초기화
    if (polyline) {
      polyline.setMap(null);
      setPolyline(null);
    }

    if (!drawingPolygon || polygonCoords.length < 3) {
      alert("다각형을 완성해야 저장할 수 있습니다.");
      return;
    }

    const custName = custNameInputRef.current?.value;
    const custno = custnoInputRef.current?.value;
    const sabun = sessionStorage.getItem('sabun');
    const gubunType = sessionStorage.getItem('gubunType');

    if (!custno?.trim()) {
      alert("거래처코드를 입력하세요.");
      return;
    }

    if (!custName?.trim()) {
      alert("거래처명을 입력하세요.");
      return;
    }

    const polygonCoordsArray = newPolygonCoords.map(point => [point[0], point[1]]);

    // 기존 다각형과 겹치는지 확인
    if (isOverlapping(newPolygonCoords)) {
      alert("이미 존재하는 대리점 관할구역과 겹칩니다. 다시 설정해주세요.");
      return;
    }

    axios.post(`/api/region/saveRegion`, {
      regionId: selectedRegionId,
      custNo: custno,
      custName: custName,
      polygonCoordsArray: polygonCoordsArray,
      sabun: sabun,
      gubunType: gubunType
    })
    .then(response => {
      if (response.data.success) {
        alert("대리점이 저장되었습니다.");
        window.location.reload();
      } else if (response.data.fail) {
        alert("저장 실패. 대리점 구분이 없습니다.");
      } else {
        alert("저장 실패. 다시 시도해주세요.");
      }
    })
    .catch(error => {
      console.error("저장 중 오류 발생:", error);
      alert("저장 중 오류가 발생했습니다.");
    });

    setSelectedRegionId(null);
    setRegionTitle("대리점별 권역 지정");
  };

  // 다각형이 기존 대리점 구역과 겹치는지 확인하는 함수
  const isOverlapping = (newPolygonCoords) => {
    const newPolygon = new kakao.maps.Polygon({
      path: newPolygonCoords.map(coord => new kakao.maps.LatLng(coord[0], coord[1]))
    });

    // 본인 대리점 확인
    const isRegionExists = allRegionsRef.current.some(region => region.id === selectedRegionId);

    for (let i = 0; i < allPolygons.length; i++) {
      const existingPolygonData = allPolygons[i];
      
      // ✅ 본인 대리점은 겹침 검사에서 제외
      if (selectedRegionId && isRegionExists) {
        continue;
      }

      const existingPolygon = existingPolygonData;

      if (isPolygonOverlap(newPolygon, existingPolygon)) {
        return true;
      }
    }
    return false;
  };

  // 두 개의 다각형이 겹치는지 검사 (점 포함 여부 + 선분 교차 여부)
  const isPolygonOverlap = (polygon1, polygon2) => {
    const path1 = polygon1.getPath();
    const path2 = polygon2.getPath();

    // 1. 첫 번째 다각형의 점이 두 번째 다각형 안에 포함되는지 확인
    for (let i = 0; i < path1.length; i++) {
      if (isPointInPolygon(path1[i], polygon2)) {
        return true;
      }
    }

    // 2. 두 번째 다각형의 점이 첫 번째 다각형 안에 포함되는지 확인
    for (let j = 0; j < path2.length; j++) {
      if (isPointInPolygon(path2[j], polygon1)) {
        return true;
      }
    }

    // 3. 두 다각형의 선분이 서로 교차하는지 확인
    if (isPolygonIntersect(path1, path2)) {
      return true;
    }

    return false;
  };

  // 점이 다각형 내부에 있는지 확인 (Ray-Casting 알고리즘)
  const isPointInPolygon = (point, polygon) => {
    const path = polygon.getPath();
    const x = point.getLat();
    const y = point.getLng();
    let inside = false;

    for (let i = 0, j = path.length - 1; i < path.length; j = i++) {
      const xi = path[i].getLat();
      const yi = path[i].getLng();
      const xj = path[j].getLat();
      const yj = path[j].getLng();

      const intersect = ((yi > y) !== (yj > y)) &&
        (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
      if (intersect) inside = !inside;
    }
    return inside;
  };

  // 두 다각형의 선분이 교차하는지 확인하는 함수
  const isPolygonIntersect = (path1, path2) => {
    for (let i = 0; i < path1.length - 1; i++) {
      for (let j = 0; j < path2.length - 1; j++) {
        if (doLinesIntersect(path1[i], path1[i + 1], path2[j], path2[j + 1])) {
          return true;
        }
      }
    }
    return false;
  };

  // 두 선분이 교차하는지 검사하는 함수
  const doLinesIntersect = (p1, p2, q1, q2) => {
    const ccw = (a, b, c) => {
      return (b.getLng() - a.getLng()) * (c.getLat() - a.getLat()) - 
            (b.getLat() - a.getLat()) * (c.getLng() - a.getLng());
    };

    return (ccw(p1, p2, q1) * ccw(p1, p2, q2) < 0) &&
          (ccw(q1, q2, p1) * ccw(q1, q2, p2) < 0);
  };

  // 대리점 삭제 함수
  const deleteRegion = (regionId) => {
    const confirmed = window.confirm("정말 이 대리점을 삭제하시겠습니까?");
    if (!confirmed) return;

    const sabun = sessionStorage.getItem('sabun');
    const gubunType = sessionStorage.getItem('gubunType');

    if(sabun == null || sabun === ""){
      alert("사번이 없습니다. 삭제 실패");
      return;
    }

    axios.get(`/api/region/deleteRegion`,{
        params: {
          regionId: regionId,
          sabun: sabun,
          gubunType: gubunType
        }
      })
      .then(res => {
        alert("대리점이 삭제되었습니다.");
        window.location.reload();
      })
      .catch(err => {
          alert("삭제 실패. 다시 시도해주세요.");
      });
  };

  const updateRegion = (regionId) => {
    const map = mapInstance.current;
    if (!map) return;

    setRegionTitle("대리점별 권역 지정 (기존대리점 수정중)");

    const gubunType = sessionStorage.getItem('gubunType');
    const regionData = allRegionsRef.current.find(p => p.id == regionId);

    if (!regionData) {
      alert("해당 대리점을 찾을 수 없습니다.");
      return;
    }

    if (editingPolygon) {
      alert("이미 대리점 수정 중입니다. 저장 후 다시 시도해주세요.");
      return;
    }

    // ✅ startDrawing과 동일한 초기화 패턴
    // 기존 클릭 이벤트 제거
    if (mapClickListener.current) {
      kakao.maps.event.removeListener(map, 'click', mapClickListener.current);
      mapClickListener.current = null;
    }

    // 기존 객체 제거 및 상태 초기화 (startDrawing과 동일)
    if (drawingPolygon) {
      drawingPolygon.setMap(null);
      setDrawingPolygon(null);
    }
    if (polyline) {
      polyline.setMap(null);
      setPolyline(null);
    }
    markers.forEach(marker => marker.setMap(null));
    setMarkers([]);
    setPolygonCoords([]);

    // 입력 필드 업데이트
    custnoInputRef.current.value = regionData.custNo;
    custNameInputRef.current.value = regionData.custName;

    selectedPolygon.current = regionData.polygon;
    setSelectedRegionId(regionData.id);
    setEditingPolygon(regionData.polygon);

    // ✅ 기존 폴리곤을 지도에서 숨김 (중복 방지)
    if (regionData.polygon) {
      regionData.polygon.setMap(null);
    }

    // ✅ startDrawing과 동일한 Polygon 및 Polyline 생성
    const editPolygon = new kakao.maps.Polygon({
      strokeWeight: 2,
      strokeColor: '#FF0000',  // 수정용이므로 빨간색
      fillColor: '#FFAAAA',
      fillOpacity: 0.5,
      map: map
    });
    setDrawingPolygon(editPolygon);

    const editPolyline = new kakao.maps.Polyline({
      strokeWeight: 2,
      strokeColor: '#FF0000',  // 수정용이므로 빨간색
      strokeOpacity: 0.7,
      map: map
    });
    setPolyline(editPolyline);

    // ✅ 기존 polygon 좌표로 마커들 생성 및 상태 설정
    const path = selectedPolygon.current.getPath();
    const initialMarkers = [];
    const initialCoords = [];

    for (let i = 0; i < path.length; i++) {
      const latlng = path[i];
      const marker = new kakao.maps.Marker({
        position: latlng,
        draggable: true,
        map: map
      });

      initialMarkers.push(marker);
      initialCoords.push(latlng);
    }

    // ✅ React 상태 초기화 (startDrawing과 동일한 패턴)
    setMarkers(initialMarkers);
    setPolygonCoords(initialCoords);
    
    // 초기 polygon과 polyline 경로 설정
    editPolygon.setPath(initialCoords);
    editPolyline.setPath(initialCoords);

    // ✅ 기존 마커들에 이벤트 추가 (startDrawing과 완전히 동일한 패턴)
    initialMarkers.forEach((marker) => {
      // 마커 드래그 이벤트 (startDrawing과 동일)
      kakao.maps.event.addListener(marker, 'dragend', () => {
        setMarkers(current => {
          const updated = [...current];
          const updatedCoords = updated.map(m => m.getPosition());
          setPolygonCoords(updatedCoords);
          editPolygon.setPath(updatedCoords);
          editPolyline.setPath(updatedCoords);
          return updated;
        });
      });

      // 마커 클릭 → 삭제 (startDrawing과 동일)
      kakao.maps.event.addListener(marker, 'click', () => {
        setMarkers(current => {
          const remaining = current.filter(m => m !== marker);
          marker.setMap(null);
          const coords = remaining.map(m => m.getPosition());
          setPolygonCoords(coords);
          editPolygon.setPath(coords);
          editPolyline.setPath(coords);
          return remaining;
        });
      });
    });

    // ✅ 지도 클릭 핸들러 정의 (startDrawing과 완전히 동일)
    const clickHandler = (mouseEvent) => {
      const clickPosition = mouseEvent.latLng;

      const marker = new kakao.maps.Marker({
        position: clickPosition,
        map,
        draggable: true
      });

      setMarkers(prev => {
        const newMarkers = [...prev, marker];
        const updatedCoords = newMarkers.map(m => m.getPosition());
        setPolygonCoords(updatedCoords);
        editPolygon.setPath(updatedCoords);
        editPolyline.setPath(updatedCoords);
        return newMarkers;
      });

      // 마커 드래그 이벤트 (startDrawing과 동일)
      kakao.maps.event.addListener(marker, 'dragend', () => {
        setMarkers(current => {
          const updated = [...current];
          const updatedCoords = updated.map(m => m.getPosition());
          setPolygonCoords(updatedCoords);
          editPolygon.setPath(updatedCoords);
          editPolyline.setPath(updatedCoords);
          return updated;
        });
      });

      // 마커 클릭 → 삭제 (startDrawing과 동일)
      kakao.maps.event.addListener(marker, 'click', () => {
        setMarkers(current => {
          const remaining = current.filter(m => m !== marker);
          marker.setMap(null);
          const coords = remaining.map(m => m.getPosition());
          setPolygonCoords(coords);
          editPolygon.setPath(coords);
          editPolyline.setPath(coords);
          return remaining;
        });
      });
    };

    // ✅ 클릭 리스너 등록 (startDrawing과 동일)
    kakao.maps.event.addListener(map, 'click', clickHandler);
    mapClickListener.current = clickHandler;

    alert("기존 관할구역을 불러왔습니다.\n지도를 클릭하여 마커를 추가하거나\n기존 마커를 드래그하여 수정할 수 있습니다.\n마커를 다시 클릭하면 삭제됩니다.");
  };

  return (
    <div className="mt-0 justify-content-start">
      <Card className="form-shrink px-3 pt-3 pb-1 shadow-sm" style={{ fontSize: '14px' }}>
        <Card.Body className="p-0">
          <div dangerouslySetInnerHTML={setSabun()}></div>
          <Collapse in={showContent}>
            <div>
              {/* 주소 검색 영역 */}
              <Form onSubmit={e => e.preventDefault()}>
                <Row className="align-items-center g-3 mb-3">
                  <Col xs={12} md={1}>
                    <Form.Group controlId="custnoInput" className="d-flex align-items-center gap-2">
                      <Form.Control
                        type="text"
                        placeholder="거래처 코드 입력"
                        ref={custnoInputRef}
                      />
                    </Form.Group>
                  </Col>

                  <Col xs={12} md={3}>
                    <Form.Group controlId="custNameInput" className="d-flex align-items-center gap-2">
                      <Form.Control
                        type="text"
                        placeholder="거래처명 입력"
                        ref={custNameInputRef}
                      />
                      <InputGroup.Text 
                        onClick={showStatusSearchPopup}  // 클릭 이벤트 추가
                        style={{
                          cursor: 'pointer',
                          padding: '10px 0px',
                          fontSize: '24px',
                          minWidth: '50px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          backgroundColor: '#e9ecef',  // 버튼 느낌을 위한 배경색
                          border: '1px solid #ced4da',
                          transition: 'background-color 0.2s'  // 호버 효과
                        }}
                        onMouseEnter={(e) => e.target.style.backgroundColor = '#dee2e6'}  // 호버 시 색상 변경
                        onMouseLeave={(e) => e.target.style.backgroundColor = '#e9ecef'}
                      >
                        <FaSearch />
                      </InputGroup.Text>
                    </Form.Group>
                  </Col>

                  <Col xs="auto">
                    <Button variant="primary" onClick={startDrawing}>
                      권역지정(초기화)
                    </Button>
                  </Col>

                  <Col xs="auto">
                    <Button variant="primary" onClick={savePolygon}>
                      저장
                    </Button>
                  </Col>

                  <Col xs="auto">
                    <Button variant="warning" className="text-white" onClick={() => window.dispatchEvent(new Event('showStatusExcelPoup'))}>
                      대리점별 권역 지정 여부
                    </Button>
                  </Col>

                  <Col xs="auto">
                    <Button 
                      variant={overlaysVisible ? "secondary" : "outline-secondary"}
                      onClick={toggleOverlayDetail}
                    >
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

      <StatusExcelPopup />
      <StatusSearchPopup getData={getData} />
    </div>
  );
};

export default MapEdit;
