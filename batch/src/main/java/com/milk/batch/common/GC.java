package com.milk.batch.common;

public class GC {
	public static final String DB_NAME = "RFLOGIX";

	public static final int SESSION_TIME = 300;

	public static final int SESSION_TIME_B2C = 3600;

	public static final int SESSION_TIME_ADMIN = 14400;

	public static final int COOKIE_TIME = 604800;

	public static final int COOKIE_TIME_DAY = 86400;

	public static final int COOKIE_TIME_WEEK = 604800;

	public static final int COOKIE_TIME_MONTH = 2592000;

	public static final String REAL_UPLOAD_PATH = "//RFLOGIX-1/host_upload";

	public static final String REAL_UPLOAD_FILE_PATH = "//RFLOGIX-1/host_upload/file";

	public static final String REAL_UPLOAD_TEMP_PATH = "//RFLOGIX-1/host_upload/temp";

	public static final int MAX_FILE_SIZE = 1048576000;

	public static final int USER_JOIN_TYPE_MILK = 1;

	public static final int USER_JOIN_TYPE_LIFE = 2;

	public static final int USER_JOIN_TYPE_HOME = 10;

	public static final int USER_JOIN_TYPE_HOME_MOBILE = 20;

	public static final int USER_JOIN_TYPE_SHOP = 50;

	public static final int USER_JOIN_TYPE_SHOP_MOBILE = 60;

	public static final int USER_JOIN_TYPE_MONEYCON_COUPON = 70;

	public static final int[] USER_JOIN_TYPE_CD = new int[] { 1, 2, 10, 20, 50, 60, 70 };

	public static final String[] USER_JOIN_TYPE_NM = new String[] { "연세우유", "연세생활건강", "홈딜리버리(모바일)", "연세샵", "연세샵(모바일)", "머니콘쿠폰"};

	public static final int SHOP_SHOWTYPE_NORMAL = 1;

	public static final int SHOP_SHOWTYPE_STAFF = 2;

	public static final int SHOP_SHOWTYPE_ALUMNUS = 3;

	public static final int SHOP_SHOWTYPE_CRISTIAN = 4;

	public static final int TEAM_TYPE_MARKETING = 1;

	public static final int TEAM_TYPE_SALES = 2;

	public static final int TEAM_TYPE_ETC = 3;

	public static final int[] TEAM_TYPE_CD = new int[] { 1, 2, 3 };

	public static final String[] TEAM_TYPE_NM = new String[] { "마케팅", "영업", "기타"};

	public static final int TEAMPERSON_TYPE_SALES = 1;

	public static final int TEAMPERSON_TYPE_CRM = 2;

	public static final int TEAMPERSON_TYPE_HOME = 3;

	public static final int TEAMPERSON_TYPE_SHOP = 4;

	public static final int TEAMPERSON_TYPE_AGENCY_OFFLINE = 5;

	public static final int TEAMPERSON_TYPE_AGENCY_ONLINE = 6;

	public static final int TEAMPERSON_TYPE_PROMO = 7;

	public static final int[] TEAMPERSON_TYPE_CD = new int[] { 1, 2, 3, 4, 5, 6, 7 };

	public static final String[] TEAMPERSON_TYPE_NM = new String[] { "영업관리자", "CRM관리자", "홈관리자", "샵관리자", "오프라인대리점", "온라인대리점", "판촉팀장"};

	public static final int PROMO_TYPE_PERSON = 1;

	public static final int PROMO_TYPE_AGENCY_TEAM = 2;

	public static final int PROMO_TYPE_AGENCY = 3;

	public static final int PROMO_TYPE_NOMATCHING = -1;

	public static final int[] PROMO_TYPE_CD = new int[] { 1, 2, 3 };

	public static final String[] PROMO_TYPE_NM = new String[] { "일반", "특별", "유치원"};

	public static final int CHANGE_TYPE_INCREASE = 1;

	public static final int CHANGE_TYPE_DECREASE = 2;

	public static final int CHANGE_TYPE_MOVE = 3;

	public static final int CHANGE_REASON_SCOUT = 1;

	public static final int CHANGE_REASON_RETURN = 2;

	public static final int CHANGE_REASON_NEW = 3;

	public static final int CHANGE_REASON_ETC_IN = 4;

	public static final int CHANGE_REASON_MOVE_IN = 5;

	public static final int CHANGE_REASON_CHG_CORP = 6;

	public static final int CHANGE_REASON_VACATION = 7;

	public static final int CHANGE_REASON_STOP = 8;

	public static final int CHANGE_REASON_ETC_OUT = 9;

	public static final int CHANGE_REASON_MOVE_OUT = 10;

	public static final int[] CHANGE_REASON_INCREASE_CD = new int[] { 0, 1, 2, 3, 4, 5 };

	public static final String[] CHANGE_REASON_INCREASE_NM = new String[] { "", "타사영입", "임시휴무복귀", "신규영입", "기타", "팀내이동"};

	public static final int[] CHANGE_REASON_DECREASE_CD = new int[] { 0, 6, 7, 8, 9, 10 };

	public static final String[] CHANGE_REASON_DECREASE_NM = new String[] { "", "타사이동", "임시휴무", "퇴사", "기타", "팀내이동"};

	public static final int AGENCY_TYPE_NORMAL = 1;

	public static final int AGENCY_TYPE_SPECIAL = 2;

	public static final int[] AGENCY_TYPE_CD = new int[] { 1, 2 };

	public static final String[] AGENCY_TYPE_NM = new String[] { "일반", "특판" };

	public static final int PROMOTEAM_CD_CHUNGMOK = 1;

	public static final int PROMOTEAM_CD_DOYAK = 2;

	public static final int PROMOTEAM_CD_CHUNGRYOUNG = 3;

	public static final int PROMOTEAM_CD_DONGBAEK = 4;

	public static final int PROMOTEAM_CD_WOOJOO = 5;

	public static final int PROMOTEAM_CD_HONAM = 6;

	public static final int PROMOTEAM_CD_YOOCHIWON = 7;

	public static final int PROMOTEAM_CD_AGENCY_TEAM = 8;

	public static final int PROMOTEAM_CD_AGENCY = 9;

	public static final int PROMOTEAM_CD_DREAM = 10;

	public static final int PROMOTEAM_CD_NOMATCHING = -1;

	public static final int[] PROMOTEAM_CD = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 10 };

	public static final String[] PROMOTEAM_NM = new String[] { "청목", "도약", "청룡", "동백", "우주", "호남", "유치원", "회개", "드림"};

	public static final long GOODS_CD_NOMATCHING = -1L;

	public static final long GOODSOPTION_CD_NOMATCHING = -1L;

	public static final int ORDER_KIND_CD_CONTRACT = 1;

	public static final int ORDER_KIND_CD_RECONTRACT = 2;

	public static final int ORDER_KIND_CD_NOCONTRACT = 3;

	public static final int RECEIVE_TYPE_SELF = 1;

	public static final int RECEIVE_TYPE_PRESENT = 2;

	public static final int RECEIVE_TYPE_CHANGE = 3;

	public static final int ORDER_TYPE_YM = 10;

	public static final int ORDER_TYPE_YM_MOBILE = 20;

	public static final int ORDER_TYPE_YM_TM = 30;

	public static final int ORDER_TYPE_YM_DM = 31;

	public static final int ORDER_TYPE_YM_OM_11 = 40;

	public static final int ORDER_TYPE_YM_OM_GM = 41;

	public static final int ORDER_TYPE_YM_OM_AU = 42;

	public static final int ORDER_TYPE_YM_OM_IN = 43;

	public static final int ORDER_TYPE_YM_STAFF = 44;

	public static final int ORDER_TYPE_SHOP = 50;

	public static final int ORDER_TYPE_SHOP_STAFF = 51;

	public static final int ORDER_TYPE_SHOP_ALUMNUS = 52;

	public static final int ORDER_TYPE_SHOP_CRISTIAN = 53;

	public static final int ORDER_TYPE_SHOP_MOBILE = 60;

	public static final int ORDER_TYPE_SHOP_MOBILE_STAFF = 61;

	public static final int ORDER_TYPE_SHOP_MOBILE_ALUMNUS = 62;

	public static final int ORDER_TYPE_SHOP_MOBILE_CRISTIAN = 63;

	public static final int ORDER_TYPE_MONEYCON_DELIVERY = 71;

	public static final int ORDER_TYPE_MONEYCON_SHOP = 72;

	public static final int ORDER_TYPE_MONEYCON_PRICE_DELIVERY = 73;

	public static final int ORDER_TYPE_MONEYCON_PRICE_SHOP = 74;

	public static final int ORDER_TYPE_EVENT_HEALTH = 80;

	public static final int ORDER_TYPE_EVENT_HEALTH_MOBILE = 81;

	public static final int ORDER_TYPE_MILKBANG = 90;

	public static final int[] ORDER_TYPE_CD = new int[] { 10, 20, 30, 31, 40, 41, 42, 43, 44, 50, 51, 52, 53, 60, 61,
			62, 63, 71, 72, 73, 74, 80, 81, 90 };

	public static final String[] ORDER_TYPE_NM = new String[] { 
			"홈페이지", "모바일", "TM", "DM", "오픈마켓(11번가)", "오픈마켓(G마켓)", "오픈마켓(옥션)", "오픈마켓(인터파크)", "직원판촉", "연세SHOP 홈페이지", 
			"연세 교직원SHOP", "연세 동문SHOP", "연세 크리스천SHOP", "연세SHOP 모바일", "연세 모바일 교직원SHOP", "연세 모바일 동문SHOP", "연세 모바일 크리스천SHOP", "머니콘 배달쿠폰", "머니콘 배송쿠폰", "머니콘 금액권(배달)", 
			"머니콘 금액권(배송)", "대리점이벤트(웹)", "대리점이벤트(모바일)", "대리점" };

	public static final int ORDER_STATUS_UNCONFIRM = 1;

	public static final int ORDER_STATUS_CONFIRM = 2;

	public static final int ORDER_STATUS_CUSTOMER = 3;

	public static final int ORDER_STATUS_DENY = 4;

	public static final int ORDER_STATUS_CALLING = 5;

	public static final int ORDER_STATUS_CHANGE = 6;

	public static final int ORDER_STATUS_CANCEL = 90;

	public static final int[] ORDER_STATUS_CD = new int[] { 1, 2, 3, 4, 5, 6, 90 };

	public static final String[] ORDER_STATUS_NM = new String[] { "미확인", "계약완료", "고객변심", "배달불가", "고객확인중", "대리점변경", "주문취소"};

	public static final int COMPLETE_STATUS_CONFIRM = 1;

	public static final int COMPLETE_STATUS_PROCESSING = 2;

	public static final int[] COMPLETE_STATUS_CD = new int[] { 1, 2 };

	public static final String[] COMPLETE_STATUS_NM = new String[] { "처리완료", "처리중" };

	public static final int PROCESS_STATUS_PAY_STANDBY = 100;

	public static final int PROCESS_STATUS_PAY_COMPLETE = 200;

	public static final int PROCESS_STATUS_ORDER_CHECK = 300;

	public static final int PROCESS_STATUS_DELIVERY = 400;

	public static final int PROCESS_STATUS_DELIVERY_COMPLETE = 500;

	public static final int PROCESS_STATUS_ORDER_CANCEL = 600;

	public static final int PROCESS_STATUS_PROBLEM = 999;

	public static final int[] PROCESS_STATUS_CD = new int[] { 100, 200, 300, 400, 500, 600, 999 };

	public static final String[] PROCESS_STATUS_NM = new String[] { "입금대기중", "결제완료", "주문확인", "배송중", "배송완료", "주문취소", "문제처리중"};

	public static final int TM_TEL_STATUS_READY = 1;

	public static final int TM_TEL_STATUS_NOCONNECT = 2;

	public static final int TM_TEL_STATUS_CONNECT = 3;

	public static final int TM_TEL_STATUS_CHANGE = 4;

	public static final int[] TM_TEL_STATUS_CD = new int[] { 1, 2, 3, 4 };

	public static final String[] TM_TEL_STATUS_NM = new String[] { "미통화", "연결안됨", "통화완료", "결번" };

	public static final int TM_TEL_RESULT_CONTRACT = 31;

	public static final int TM_TEL_RESULT_NOCONTRACT = 32;

	public static final int TM_TEL_RESULT_RECALL = 33;

	public static final int TM_TEL_RESULT_EXCEPT = 34;

	public static final int[] TM_TEL_RESULT_CD = new int[] { 31, 32, 33, 34 };

	public static final String[] TM_TEL_RESULT_NM = new String[] { "계약의사 있음", "계약의사 없음", "재통화 요청", "TM제외 요청"};

	public static final int CLAIM_TYPE_101 = 101;

	public static final int CLAIM_TYPE_102 = 102;

	public static final int CLAIM_TYPE_103 = 103;

	public static final int CLAIM_TYPE_104 = 104;

	public static final int CLAIM_TYPE_105 = 105;

	public static final int CLAIM_TYPE_201 = 201;

	public static final int CLAIM_TYPE_202 = 202;

	public static final int CLAIM_TYPE_203 = 203;

	public static final int CLAIM_TYPE_204 = 204;

	public static final int CLAIM_TYPE_205 = 205;

	public static final int[] CLAIM_TYPE_CD = new int[] { 101, 102, 103, 104, 105, 201, 202, 203, 204, 205 };

	public static final String[] CLAIM_TYPE_NM = new String[] { "위약금", "계약기간 임의연장/해지", "불친절", "기타1", "기타2", "변질", "포장/용기불량", "탄화물", "기타3", "기타4" };

	public static final int MILKBANG_TYPE_NEW = 1;

	public static final int MILKBANG_TYPE_STOP = 2;

	public static final int MILKBANG_FILESTATUS_OK = 1;

	public static final int MILKBANG_FILESTATUS_EMPTY = 2;

	public static final int MILKBANG_FILESTATUS_BREAK = 3;

	public static final int[] MILKBANG_FILESTATUS_CD = new int[] { 1, 2, 3 };

	public static final String[] MILKBANG_FILESTATUS_NM = new String[] { "정상파일", "파일내용없음", "파일깨짐"};

	public static final int PG_TYPE_LG = 1;

	public static final int PG_TYPE_INI = 2;

	public static final int PG_TYPE_OPENMARKET = 3;

	public static final int PG_TYPE_SALES_STAFF = 4;

	public static final int PG_TYPE_MILKBANG = 9;

	public static final int PG_PAYTYPE_CARD = 1;

	public static final int PG_PAYTYPE_BANK = 2;

	public static final int PG_PAYTYPE_VBANK = 3;

	public static final int PG_PAYTYPE_POINT = 4;

	public static final int PG_PAYTYPE_MONEYCON_COUPON = 5;

	public static final int PG_PAYTYPE_MONEYCON_CARD = 6;

	public static final int PG_PAYTYPE_MONEYCON_BANK = 7;

	public static final int PG_PAYTYPE_MONEYCON_VBANK = 8;

	public static final int[] PG_PAYTYPE_CD = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };

	public static final String[] PG_PAYTYPE_NM = new String[] { "카드결제", "계좌이체", "무통장입금", "포인트결제", "머니콘 쿠폰", "머니폰 쿠폰(카드)", "머니콘 쿠폰(계좌)", "머니콘 쿠폰(가상계좌)" };

	public static final String[] PG_CARD_CD_INI = new String[] { "01", "03", "04", "06", "11", "12", "14", "15", "16",
			"17", "21", "22", "23", "24", "25" };

	public static final String[] PG_CARD_NM_INI = new String[] { 
	      "외환", "롯데", "현대", "국민", "BC", "삼성", "신한", "한미", "NH", "하나SK", 
	      "해외비자", "해외마스터", "JCB", "해외아멕스", "해외다이너스"};

	public static final String[] PG_BANK_CD_INI = new String[] { "03", "04", "05", "07", "11", "20", "23", "31", "32",
			"34", "37", "39", "53", "71", "81", "88", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB",
			"DC", "DD", "DE", "DF" };

	public static final String[] PG_BANK_NM_INI = new String[] { 
	      "기업은행", "국민은행", "외환은행", "수협중앙회", "농협중앙회", "우리은행", "SC제일은행", "대구은행", "부산은행", "광주은행", 
	      "전북은행", "경남은행", "한국씨티은행", "우체국", "하나은행", "통합신한은행(신한,조흥은행)", "동양종합금융증권", "현대증권", "미래에셋증권", "한국투자증권", 
	      "우리투자증권", "하이투자증권", "HMC투자증권", "SK증권", "대신증권", "하나대투증권", "굿모닝신한증권", "동부증권", "유진투자증권", "메리츠증권", 
	      "신영증권" };

	public static final int USER_TYPE_DEFAULT = -1;

	public static final int USER_TYPE_JOIN_USER = 1;

	public static final int USER_TYPE_NON_USER = 2;

	public static final String DEFAULT_ID = "gue$t";

	public static final String DEFAULT_NM = "고객";

	public static final String DEFAULT_NICKNM = "고객";

	public static final int STAFF_TYPE_MILK = 1;

	public static final int STAFF_TYPE_LIFE = 2;

	public static final int STAFF_TYPE_SHOP = 3;

	public static final int STAFF_TYPE_AGENCY = 4;

	public static final int STAFF_TYPE_ONLINE = 5;

	public static final int POINT_TYPE_NORMAL = 1;

	public static final int COUPON_TYPE_HOME_GOODS = 1;

	public static final int COUPON_TYPE_HOME_ALL = 2;

	public static final int COUPON_TYPE_SHOP_GOODS = 3;

	public static final int COUPON_TYPE_SHOP_ALL = 4;

	public static final int COUPON_TYPE_MONEYCON_DELIVERY = 10;

	public static final int COUPON_TYPE_MONEYCON_SHOP = 11;

	public static final int COUPON_TYPE_MONEYCON_PRICE = 12;

	public static final int COUPON_TYPE_HEALTH = 20;

	public static final int GOODS_TYPE_MILK = 1;

	public static final int GOODS_TYPE_SOYMILK = 2;

	public static final int GOODS_TYPE_FERMENTED = 3;

	public static final int GOODS_TYPE_DRINK = 8;

	public static final int GOODS_TYPE_ARYALAYA = 4;

	public static final int GOODS_TYPE_FOERSTER = 5;

	public static final int GOODS_TYPE_JEJUNGWON = 6;

	public static final int GOODS_TYPE_YONSEILIFE = 10;

	public static final int GOODS_TYPE_MONEYCON_DELIVERY = 20;

	public static final int GOODS_TYPE_MONEYCON_SHOP = 21;

	public static final int GOODS_TYPE_BRAND_SHOP = 30;

	public static final int[] GOODS_TYPE_CD = new int[] { 1, 2, 3 };

	public static final String[] GOODS_TYPE_NM = new String[] { "우유", "두유 및 음료", "발효유"};

	public static final int ROUND_TYPE_DOWN = 1;

	public static final int ROUND_TYPE_UP = 0;

	public static final int ROUND_TYPE_HALFUP = 4;

	public static final int[] ROUND_TYPE_CD = new int[] { 1, 0, 4 };

	public static final String[] ROUND_TYPE_NM = new String[] { "절삭", "올림", "반올림"};

	public static final int BOARD_ETC_NOTICE = 1;

	public static final int BOARD_ETC_ONEONONE = 2;

	public static final int BOARD_GOODS_QNA = 3;

	public static final int BOARD_GOODS_REVIEW = 4;

	public static final int BOARD_PARENT_CD_DEFAULT = 0;

	public static final int BOARD_LEVEL_DEFAULT = 0;

	public static final int NEW_BOARD_DATE = 3;

	public static final int BOARD_READ_LEVEL_DEFAULT = 0;

	public static final int CODE_TYPE_ORDER = 100;

	public static final int CODE_MAX_ORDER = 99999;

	public static final int CODE_TYPE_MESSAGE = 200;

	public static final int CODE_MAX_MESSAGE = 99999;

	public static final int CODE_TYPE_MILKBANG = 300;

	public static final int CODE_MAX_MILKBANG = 99999;

	public static final int CODE_TYPE_VISIT = 100;

	public static final int CODE_MAX_VISIT = 99999;

	public static final int CODE_TYPE_COUPON = 500;

	public static final int CODE_MAX_COUPON = 99999;

	public static final int MESSAGE_TYPE_SMS = 1;

	public static final int MESSAGE_TYPE_MAIL = 2;

	public static final int TEMPLATE_TYPE_EDELIVERY = 10;

	public static final int TEMPLATE_TYPE_AGENCY = 20;

	public static final int TEMPLATE_TYPE_STOP = 30;

	public static final int TEMPLATE_TYPE_GMARKET = 40;

	public static final int TEMPLATE_TYPE_11ST = 41;

	public static final int TEMPLATE_TYPE_AUCTION = 42;

	public static final int TEMPLATE_TYPE_INTERPARK = 43;

	public static final int CAPACITY_TYPE_SMALL = 1;

	public static final int CAPACITY_TYPE_MEDIUM = 2;

	public static final int CAPACITY_TYPE_LARGE = 3;

	public static final int HC_STATUS_DEFAULT = 10;

	public static final int HC_STATUS_OK = 11;

	public static final int HC_STATUS_NOSHOW = 12;

	public static final int HC_STATUS_DIFFER = 13;

	public static final int HC_STATUS_NONUMBER = 14;

	public static final int HC_STATUS_CHANGE = 15;

	public static final int[] HC_STATUS_CD = new int[] { 10, 11, 12, 13, 14, 15 };

	public static final String[] HC_STATUS_NM = new String[] { "미확인", "정상", "부재중", "상이건", "결번", "내용변경" };

	public static final int PLACE_KINDERGARTEN_CD = 1;

	public static final int PLACE_DAYCARE_CENTER_CD = 2;

	public static final int PLACE_COFFEE_SHOP_CD = 3;

	public static final int PLACE_FACTORY_CD = 4;

	public static final int PLACE_ETC_CD = 5;

	public static final int[] PLACE_CUSTOMER_CD = new int[] { 1, 2, 3, 4, 5 };

	public static final String[] PLACE_CUSTOMER_NM = new String[] { "유치원", "어린이집", "커피숍", "공장", "기타" };

	public static final int MILEAGE_REQUEST_STATUS_UNCONFIRM = 100;

	public static final int MILEAGE_REQUEST_STATUS_CONFIRM = 200;

	public static final int MILEAGE_REQUEST_STATUS_DELIVERY = 300;

	public static final int MILEAGE_REQUEST_STATUS_DELIVERY_COMPLETE = 400;

	public static final int[] MILEAGE_REQUEST_STATUS_CD = new int[] { 100, 200, 300, 400 };

	public static final String[] MILEAGE_REQUEST_STATUS_NM = new String[] { "미확인", "접수완료", "배송중", "배송완료" };

	public static final int CHURCH_ROLE_MINISTER = 1;

	public static final int CHURCH_ROLE_ASSISTANT = 2;

	public static final int CHURCH_ROLE_ADMIN = 3;

	public static final int CHURCH_ROLE_BUTLER = 4;

	public static final int CHURCH_ROLE_NORMAL = 5;

	public static final int[] CHURCH_ROLE_CD = new int[] { 1, 2, 3, 4, 5 };

	public static final String[] CHURCH_ROLE_NM = new String[] { "목사", "행정간사", "행정담당자", "집사", "일반교인"};

	public static final int CHURCH_STATUS_REQUEST = 0;

	public static final int CHURCH_STATUS_COMPLETE = 1;

	public static final int[] CHURCH_STATUS_CD = new int[] { 0, 1 };

	public static final String[] CHURCH_STATUS_NM = new String[] {"승인대기", "승인완료"};

	public static final int MONEYCON_MSG_SUB_CD_AUTH = 100;

	public static final int MONEYCON_MSG_SUB_CD_ACCEPT = 101;

	public static final int MONEYCON_MSG_SUB_CD_CANCEL = 102;

	public static final String MONEYCON_COUPON_TYPE_SHOP = "01";

	public static final String MONEYCON_COUPON_TYPE_DELIVERY = "02";

	public static final String MONEYCON_COUPON_TYPE_PRICE = "03";

	public static final String MONEYCON_STATUS_CD_OK = "000";

	public static final String MONEYCON_STATUS_CD_FAIL = "001";

	public static final String MONEYCON_ERROR_CD_OK = "E0000";
}
