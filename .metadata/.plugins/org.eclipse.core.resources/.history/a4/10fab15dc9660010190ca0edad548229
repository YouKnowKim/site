package com.yonseidairy.site.dao;

import java.util.List;

import lombok.Data;

@Data
public class RegionDao {
	private String code;
    private String name;
    private String addr;
    private String gubun;
    private String tel;
    private String gubun_type;
    private String polygon_coords; // CLOB → String 으로 읽을 경우
    private List<double[]> polygon;  // [lat, lng] 쌍의 리스트
}
