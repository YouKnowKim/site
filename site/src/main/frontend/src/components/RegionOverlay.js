import React from 'react';

const RegionOverlay = ({ region, isAdmin, onDelete, onUpdate }) => {
  return (
    <div style={{
      background: 'white',
      padding: '8px',
      border: '1px solid #aaa',
      borderRadius: '8px',
      fontSize: '12px',
      lineHeight: '1.4',
      minWidth: '100px'
    }}>
    <div style={{ display: 'flex', gap: '6px', justifyContent: 'center', alignItems: 'center' }}>
        {region.custNo} {region.custName}
        <button
            style={{
            backgroundColor: '#e74c3c',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            padding: '4px 10px',
            fontSize: '12px',
            cursor: 'pointer'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#c0392b'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#e74c3c'}
            onClick={() => onDelete(region.id)}
        >
            🗑 삭제
        </button>
        <button
            style={{
            backgroundColor: '#3498db',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            padding: '4px 10px',
            fontSize: '12px',
            cursor: 'pointer'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#2980b9'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#3498db'}
            onClick={() => onUpdate(region.id)}
        >
            ✏ 수정
        </button>
    </div>

      {isAdmin && (
        <>
          <div>☎ {region.tel || '-'}</div>
          <div>대리점 핸드폰1: {region.phoneMobile1 || '-'}</div>
          <div>대리점 핸드폰2: {region.phoneMobile2 || '-'}</div>
        </>
      )}
      <div>담당자: {region.salesMan || '-'}</div>
      <div>담당자 연락처: {region.mobileNbr || '-'}</div>
      <div>팀: {region.deptName || '-'}</div>
      <div>지역대분류: {region.category1 || '미지정'}</div>
      <div>지역중분류: {region.category2 || '미지정'}</div>
      {region.openTime && <div>상담시간: {region.openTime}</div>}
    </div>
  );
};

export default RegionOverlay;
