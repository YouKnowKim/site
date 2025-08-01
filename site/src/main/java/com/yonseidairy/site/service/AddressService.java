package com.yonseidairy.site.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yonseidairy.site.dao.AddressDao;
import com.yonseidairy.site.dao.RegionDao;
import com.yonseidairy.site.mapper.AddressMapper;

@Service
public class AddressService {
	private static final String KAKAO_REST_API_KEY = "KakaoAK 70f55076ab1f80b4e69e90f9ec18c1b5";

	@Autowired
	AddressMapper addressMapper;

	public HashMap<String, String> getRegionByAddress(AddressDao inAddressDao) {

		HashMap<String, String> result = new HashMap<>();

		String address = inAddressDao.getAddress();

		if (address == null || address.trim().isEmpty()) {
			result.put("error", "해당 주소에 매핑된 대리점이 없습니다.");
			return result;
		}

		try {
			// 1. 주소 → 좌표 변환 (Kakao REST API 호출)
			double[] coordinates = getCoordinatesFromAddress(address);
			double lat = coordinates[0];
			double lng = coordinates[1];

			// 2. 좌표가 포함된 Region 찾기 (메모리 내 판단)
			List<RegionDao> regions = new ArrayList<>();
			List<RegionDao> tempRegions = addressMapper.selectAllRegionsForAddress(); // 모든 Region 리스트 가져오기
			
			for (RegionDao raw : tempRegions) {
				raw.setPolygon(parsePolygon(raw.getPolygon_coords()));

				regions.add(raw);
			}
			
			RegionDao matched = null;
			
			for (RegionDao region : regions) {
				if (pointInPolygon(lat, lng, region.getPolygon())) {
					matched = region;
					break;
				}
			}
			
			if (matched != null) {
				result.put("custNo", matched.getCode());
				result.put("custName", matched.getName());
				result.put("addr", matched.getAddr());
				result.put("gubun", matched.getGubun());
				result.put("tel", matched.getTel());
				result.put("result", "1");
			} else {
				result.put("error", "해당 주소가 포함되는 대리점이 없습니다.");
				result.put("result", "0");
			}

		} catch (Exception e) {
			result.put("error", "입력한 주소가 검색되지 않습니다. 주소를 확인해 주시기 바랍니다.");
			result.put("result", "-1");
		}

		return result;
	}

	// 🧭 Kakao 주소 → 좌표 변환
	private double[] getCoordinatesFromAddress(String address) throws IOException {
		String encoded = URLEncoder.encode(address, "UTF-8");
		URL url = new URL("https://dapi.kakao.com/v2/local/search/address.json?query=" + encoded);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", KAKAO_REST_API_KEY);
		conn.setRequestMethod("GET");

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder json = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			json.append(line);
		}
		br.close();

		JsonObject result = JsonParser.parseString(json.toString()).getAsJsonObject();
		if (result.getAsJsonArray("documents").size() == 0)
			throw new IllegalArgumentException("주소 변환 결과 없음");

		JsonObject pos = result.getAsJsonArray("documents").get(0).getAsJsonObject();
		double lat = pos.get("y").getAsDouble();
		double lng = pos.get("x").getAsDouble();

		return new double[] { lat, lng };
	}

	// 📌 점이 polygon에 포함됐는지 판단하는 알고리즘
	private boolean pointInPolygon(double lat, double lng, List<double[]> polygon) {
		boolean inside = false;
		int j = polygon.size() - 1;

		for (int i = 0; i < polygon.size(); i++) {
			double xi = polygon.get(i)[0], yi = polygon.get(i)[1];
			double xj = polygon.get(j)[0], yj = polygon.get(j)[1];

			boolean intersect = ((yi > lng) != (yj > lng)) && (lat < (xj - xi) * (lng - yi) / (yj - yi + 1e-10) + xi);
			if (intersect)
				inside = !inside;
			j = i;
		}

		return inside;
	}

	// String 위치 정보를 JsonArray로 변경하는 메소드
	private List<double[]> parsePolygon(String json) {
		List<double[]> list = new ArrayList<>();
		JsonArray array = JsonParser.parseString(json).getAsJsonArray();

		for (JsonElement elem : array) {
			JsonArray pair = elem.getAsJsonArray();
			double lat = pair.get(0).getAsDouble();
			double lng = pair.get(1).getAsDouble();
			list.add(new double[] { lat, lng });
		}

		return list;
	}

}
