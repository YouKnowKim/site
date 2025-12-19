package com.milk.batch.controller;

import com.milk.batch.bean.GoodsOptionBean;
import com.milk.batch.bean.MilkbangBean;
import com.milk.batch.common.DBManager;
import com.milk.batch.common.GF;
import com.milk.batch.function.GoodsManager;
import com.milk.batch.function.MilkbangManager;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchMainController {
    String strError = "(_milkbang.jsp) : ";
    Connection conDB = null;

    /**
     * 밀크방(유제품 배송) 파일 배치 처리 메서드
     * - REST API 엔드포인트: /batch/_milkbang_file
     * - 스케줄러: 50초마다 자동 실행
     * 
     * 주요 기능:
     * 1. FTP 서버의 판촉자료 폴더에서 신규 엑셀 파일 검색
     * 2. 파일 정보를 DB에 등록/업데이트
     * 3. 엑셀 파일을 읽어 주문 데이터 추출
     * 4. 주문 데이터를 DB에 저장
     */
    @GetMapping({ "/batch/_milkbang_file" })
    @Scheduled(fixedDelay = 3600000L)
    public void _milkbang_file() {
    	
    	Connection conDB1 = null;  // 첫 번째 커넥션 (파일 처리용)
        Connection conDB2 = null;  // 두 번째 커넥션 (엑셀 처리용)
        
        try {
            // ======== 1단계: 초기 설정 ========
            String ROOT_URL = "http://ftp.yonseidairy.com/판촉자료";  // FTP 서버 URL
            String ROOT_DIR = "c:/develop/FILES/Milkbang/판촉자료";   // 로컬 파일 저장 경로
            SimpleDateFormat fullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // DB 연결 및 매니저 객체 생성
            conDB1 = DBManager.openDB();
            MilkbangManager clsMilkbangManager = new MilkbangManager(conDB1);
            
            // DB에 이미 등록된 파일 목록 조회
            ArrayList<MilkbangBean> arrResult = clsMilkbangManager.findAllFile();
            
            // 현재 시간 및 검색 기준 시간 설정 (4일 전부터)
            String nowDT = clsMilkbangManager.now();
            String SearchDT_Start = GF.addTime_Day(nowDT, -4);
            
            // ======== 2단계: 로컬 폴더에서 파일 검색 ========
            ArrayList<MilkbangBean> arrFileList = new ArrayList<>();
            File clsFolder = new File(ROOT_DIR);
            File[] arrFile = clsFolder.listFiles();  // 폴더 내 모든 파일 목록
            
            int i;
            // 각 파일 정보를 MilkbangBean에 저장
            for (i = 0; i < arrFile.length; i++) {
                File file = arrFile[i];
                MilkbangBean clsFile = new MilkbangBean();
                clsFile.FileNM = file.getName();                    // 파일명
                clsFile.FileURL = ROOT_URL + "/" + clsFile.FileNM;  // 파일 URL
                clsFile.DownloadDT = Timestamp.valueOf(fullDate.format(new Date(file.lastModified())));  // 수정일시
                clsFile.UploadYN = -1;  // 미처리 상태
                
                // 파일이고, 기준 시간 이후에 생성/수정된 파일만 처리 대상에 추가
                if (file.isFile() == true && 
                    Timestamp.valueOf(GF.left(SearchDT_Start, 10) + " 00:00:00").compareTo(clsFile.DownloadDT) <= 0)
                    arrFileList.add(clsFile);
            }
            
            System.out.println("밀크방파일 시작시간 : " + nowDT);
            System.out.println("밀크방파일 기준시간: " + GF.left(SearchDT_Start, 10) + " 00:00:00 이후");
            
            // ======== 3단계: 파일 정보 DB 등록/업데이트 ========
            for (i = 0; i < arrFileList.size(); i++) {
                MilkbangBean clsFile = arrFileList.get(i);
                boolean blnExsist = false;
                
                // DB에 이미 등록된 파일인지 확인
                for (int m = 0; m < arrResult.size(); m++) {
                    MilkbangBean clsResult = arrResult.get(m);
                    if (clsFile.FileNM.equals(clsResult.FileNM)) {
                        blnExsist = true;
                        break;
                    }
                }
                
                if (!blnExsist) {
                    // 신규 파일: DB에 INSERT
                    clsMilkbangManager.insertFileList(clsFile);
                    System.out.println("밀크방파일 새로추가 (" + (i + 1) + "/" + arrFileList.size() + ") : " + clsFile.FileNM);
                } else {
                    // 기존 파일: 정보 UPDATE
                    clsMilkbangManager.updateFileList(clsFile);
                    System.out.println("밀크방파일 업데이트 (" + (i + 1) + "/" + arrFileList.size() + ") : " + clsFile.FileNM);
                }
            }
            
            // ======== 4단계: 엑셀 파일 처리 준비 ========
            i = 0;
            int j = 0;
            int k = 0;
            String ORIGIN_DIR = "c:\\develop\\FILES\\Milkbang\\판촉자료";  // 원본 엑셀 경로
            String TARGET_DIR = "c:\\operation\\temp";     // 변환된 엑셀 저장 경로
            
            // 새 DB 연결 및 상품 정보 조회
            conDB2 = DBManager.openDB();
            GoodsManager clsGoodsManager = new GoodsManager(conDB2);
            ArrayList<GoodsOptionBean> arrGoodsOption = clsGoodsManager.findGoodsOption();  // 상품 옵션 목록 조회
            
            // 미처리 파일 목록 조회 (UploadYN = -1)
            MilkbangBean clsParam_Files = new MilkbangBean();
            clsParam_Files.UploadYN = -1;
            ArrayList<MilkbangBean> arrFiles = clsMilkbangManager.findFileSummary(clsParam_Files);
            
            System.out.println("밀크방파일 업로드시작: " + nowDT);
            
            // ======== 5단계: 각 엑셀 파일 처리 ========
            for (k = 0; k < arrFiles.size(); k++) {
                MilkbangBean clsFiles = arrFiles.get(k);
                System.out.println("밀크방파일 분석 (" + (k + 1) + "/" + arrFiles.size() + ") : " + clsFiles.FileNM);
                
                // Excel 파일 형식 변환 (.xls → .xlsx)
                String strTargetFilePath = clsMilkbangManager.convertExcel(ORIGIN_DIR, clsFiles.FileNM, TARGET_DIR);
                
                // Excel 파일에서 주문 데이터 읽기
                ArrayList<MilkbangBean> arrMlikbangGoods = clsMilkbangManager.readExcel_Milkbang(clsFiles.FileNM, strTargetFilePath);
                
                if (arrMlikbangGoods.size() > 0) {
                    // 최신 상품 옵션 목록 재조회
                    arrGoodsOption = clsGoodsManager.findGoodsOption();
                    ArrayList<MilkbangBean> arrMlikbang = new ArrayList<>();  // 최종 주문 목록
                    
                    // ======== 6단계: 각 상품 데이터 처리 ========
                    for (i = 0; i < arrMlikbangGoods.size(); i++) {
                        MilkbangBean clsMilkbangGoods = arrMlikbangGoods.get(i);
                        
                        // 상품 코드 초기화
                        clsMilkbangGoods.GoodsCD = -1L;
                        clsMilkbangGoods.GoodsOptionCD = -1L;
                        clsMilkbangGoods.GoodsOptionNM = "매칭안됨";
                        
                        // 상품 옵션 코드 매칭 (MIS 코드로 실제 상품 코드 찾기)
                        if (clsMilkbangGoods.GoodsOptionCD == -1L) {
                            GoodsOptionBean clsGoodsOption = clsMilkbangManager.checkMisGoodsOptionCD(arrGoodsOption,
                                    clsMilkbangGoods.GoodsOptionCD_Origin);
                            if (clsGoodsOption != null) {
                                clsMilkbangGoods.GoodsCD = clsGoodsOption.GoodsCD;
                                clsMilkbangGoods.GoodsOptionCD = clsGoodsOption.GoodsOptionCD;
                                clsMilkbangGoods.GoodsOptionNM = clsGoodsOption.OptionNM;
                                clsMilkbangGoods.Day1 = clsGoodsOption.Day1;
                                clsMilkbangGoods.Day2 = clsGoodsOption.Day2;
                                clsMilkbangGoods.Day3 = clsGoodsOption.Day3;
                                clsMilkbangGoods.Day4 = clsGoodsOption.Day4;
                                clsMilkbangGoods.Day5 = clsGoodsOption.Day5;
                                clsMilkbangGoods.Day6 = clsGoodsOption.Day6;
                            }
                        }
                        
                        // 주문 가격 계산
                        if (clsMilkbangGoods.UnitPrice.compareTo(new BigDecimal("0")) > 0) {
                            if (clsMilkbangGoods.ContractPeriod == 0) {
                                // 계약 기간이 없는 경우: 단가 × 수량 × 주간수량
                                clsMilkbangGoods.OrderPrice = clsMilkbangGoods.UnitPrice
                                        .multiply(new BigDecimal(String.valueOf(clsMilkbangGoods.Quantity)))
                                        .multiply(new BigDecimal(String.valueOf(clsMilkbangGoods.WeekQty)));
                            } else {
                                // 계약 기간이 있는 경우: 단가 × 수량 × 주간수량 × 계약기간(월) × 4(주)
                                clsMilkbangGoods.OrderPrice = clsMilkbangGoods.UnitPrice
                                        .multiply(new BigDecimal(String.valueOf(clsMilkbangGoods.Quantity)))
                                        .multiply(new BigDecimal(String.valueOf(clsMilkbangGoods.WeekQty)))
                                        .multiply(new BigDecimal(String.valueOf(clsMilkbangGoods.ContractPeriod)))
                                        .multiply(new BigDecimal(String.valueOf("4")));
                            }
                        }
                        
                        // 주문 상태 설정
                        clsMilkbangGoods.OrderStatus = 2;  // 주문 상태
                        clsMilkbangGoods.DeleteYN = 0;     // 삭제 안됨
                        clsMilkbangGoods.SaveYN = 0;       // 저장 안됨
                        
                        // 주문 종류 판별
                        if (clsMilkbangGoods.OrderKind.equals("재계") || clsMilkbangGoods.OrderKind.equals("재계약")
                                || clsMilkbangGoods.OrderKind.equals("재판촉") || clsMilkbangGoods.OrderKind.equals("재투입")) {
                            clsMilkbangGoods.OrderKindCD = 2;  // 재계약
                        } else if (clsMilkbangGoods.ContractPeriod == 0) {
                            clsMilkbangGoods.OrderKindCD = 3;  // 무계약
                        } else {
                            clsMilkbangGoods.OrderKindCD = 1;  // 신규계약
                        }
                        
                        BigDecimal weekQty = new BigDecimal(String.valueOf(clsMilkbangGoods.WeekQty));
                        BigDecimal one = new BigDecimal("1");
                        BigDecimal two = new BigDecimal("2");
                        BigDecimal three = new BigDecimal("3");
                        BigDecimal four = new BigDecimal("4");
                        BigDecimal five = new BigDecimal("5");
                        BigDecimal six = new BigDecimal("6");
                        
                        // 본사 홉 자동 설정
                        if(clsMilkbangGoods.OrderKindCD == 1) {
                            
                        	if(weekQty.compareTo(one) == 0) {
                        		clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day1;
                        	} else if (weekQty.compareTo(two) == 0) {
                        		clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day2;
                        	} else if (weekQty.compareTo(three) == 0) {
                        		clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day3;
                        	} else if (weekQty.compareTo(four) == 0) {
                        		clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day4;
                        	} else if (weekQty.compareTo(five) == 0) {
                        		clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day5;
                        	} else if (weekQty.compareTo(six) >= 0) {
                        		clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day6;
                        	} else {
                        		clsMilkbangGoods.ActualHob = new BigDecimal("0");
                        	}
                        	
                        } else if (clsMilkbangGoods.OrderKindCD == 2) {
                        	if (weekQty.compareTo(one) == 0) {
                                // weekQty가 1인 경우: Day1이 0보다 크면 1, 아니면 0
                                clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day1.compareTo(BigDecimal.ZERO) > 0 
                                    ? one : BigDecimal.ZERO;
                            } else if (weekQty.compareTo(two) == 0) {
                                // weekQty가 2인 경우: Day2가 0보다 크면 1, 아니면 0
                                clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day2.compareTo(BigDecimal.ZERO) > 0 
                                    ? one : BigDecimal.ZERO;
                            } else if (weekQty.compareTo(three) == 0) {
                                // weekQty가 3인 경우: Day3가 0보다 크면 1, 아니면 0
                                clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day3.compareTo(BigDecimal.ZERO) > 0 
                                    ? one : BigDecimal.ZERO;
                            } else if (weekQty.compareTo(four) == 0) {
                                // weekQty가 4인 경우: Day4가 0보다 크면 1, 아니면 0
                                clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day4.compareTo(BigDecimal.ZERO) > 0 
                                    ? one : BigDecimal.ZERO;
                            } else if (weekQty.compareTo(five) == 0) {
                                // weekQty가 5인 경우: Day5가 0보다 크면 1, 아니면 0
                                clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day5.compareTo(BigDecimal.ZERO) > 0 
                                    ? one : BigDecimal.ZERO;
                            } else if (weekQty.compareTo(six) >= 0) {
                                // weekQty가 6 이상인 경우: Day6가 0보다 크면 1, 아니면 0
                                clsMilkbangGoods.ActualHob = clsMilkbangGoods.Day6.compareTo(BigDecimal.ZERO) > 0 
                                    ? one : BigDecimal.ZERO;
                            } else {
                                // weekQty가 1 미만인 경우: ActualHob = 0
                                clsMilkbangGoods.ActualHob = BigDecimal.ZERO;
                            }
                        } else if (clsMilkbangGoods.OrderKindCD == 3) {
                        	clsMilkbangGoods.ActualHob = new BigDecimal("0");
                        } else {
                        	clsMilkbangGoods.ActualHob = new BigDecimal("0");
                        }
                        
                        // ======== 7단계: 동일 주문 묶기 ========
                        boolean blnMultiOrderYN = false;
                        MilkbangBean clsMlikbang = null;
                        
                        // 같은 날짜, 같은 대리점, 같은 고객의 주문 찾기
                        for (j = 0; j < arrMlikbang.size(); j++) {
                            clsMlikbang = arrMlikbang.get(j);
                            if (clsMilkbangGoods.OrderDT.compareTo(clsMlikbang.OrderDT) == 0
                                    && clsMilkbangGoods.AgencyCD == clsMlikbang.AgencyCD 
                                    && clsMilkbangGoods.OrderUserNM.equals(clsMlikbang.OrderUserNM)
                                    && clsMilkbangGoods.OrderAddress1.equals(clsMlikbang.OrderAddress1)) {
                                blnMultiOrderYN = true;
                                break;
                            }
                        }
                        
                        if (!blnMultiOrderYN) {
                            // 새로운 주문 생성
                            clsMlikbang = new MilkbangBean();
                            clsMlikbang.FileNM = clsFiles.FileNM;
                            clsMlikbang.OrderDT = clsMilkbangGoods.OrderDT;
                            clsMlikbang.OrderType = 90;  // 밀크방 주문 타입
                            
                            // 주문자 정보 복사
                            clsMlikbang.OrderUserNM = clsMilkbangGoods.OrderUserNM;
                            clsMlikbang.OrderHomePhone = clsMilkbangGoods.OrderHomePhone;
                            clsMlikbang.OrderCellPhone = clsMilkbangGoods.OrderCellPhone;
                            clsMlikbang.OrderZipCD = clsMilkbangGoods.OrderZipCD;
                            clsMlikbang.OrderAddress1 = clsMilkbangGoods.OrderAddress1;
                            
                            // 수령자 정보 복사
                            clsMlikbang.ReceiveUserNM = clsMilkbangGoods.ReceiveUserNM;
                            clsMlikbang.ReceiveHomePhone = clsMilkbangGoods.ReceiveHomePhone;
                            clsMlikbang.ReceiveCellPhone = clsMilkbangGoods.ReceiveCellPhone;
                            clsMlikbang.ReceiveZipCD = clsMilkbangGoods.ReceiveZipCD;
                            clsMlikbang.ReceiveAddress1 = clsMilkbangGoods.ReceiveAddress1;
                            
                            // 금액 정보
                            clsMlikbang.TotalOrderPrice = clsMilkbangGoods.OrderPrice;
                            clsMlikbang.TotalPayPrice = clsMlikbang.TotalOrderPrice;
                            
                            // 결제 정보 (밀크방은 무통장입금으로 처리)
                            clsMlikbang.PG_Type = 9;       // 결제 타입
                            clsMlikbang.PG_PayYN = 1;      // 결제 완료
                            clsMlikbang.PG_PayType = 2;    // 무통장입금
                            clsMlikbang.PG_PayDT = clsMilkbangGoods.OrderDT;
                            
                            // 대리점 정보
                            clsMlikbang.AgencyCD = clsMilkbangGoods.AgencyCD;
                            clsMlikbang.AgencyDeliveryYN = 1;  // 대리점 배송
                            clsMlikbang.DeleteYN = 0;
                            
                            // 판촉 담당자 정보
                            clsMlikbang.PromoPersonNM = clsMilkbangGoods.PromoPersonNM;
                            clsMlikbang.PromoPersonNM_Origin = clsMilkbangGoods.PromoPersonNM_Origin;
                            clsMlikbang.PostArea = clsMilkbangGoods.PostArea;
                            clsMlikbang.AddressType = clsMilkbangGoods.AddressType;
                            
                            // 기타 플래그
                            clsMlikbang.ForceAddYN = 0;
                            clsMlikbang.DuplicateYN = -1;
                            clsMlikbang.DuplOrderCD = -1L;
                            
                            arrMlikbang.add(clsMlikbang);
                        } else {
                            // 기존 주문에 금액 합산
                            clsMlikbang.TotalOrderPrice = clsMlikbang.TotalOrderPrice.add(clsMilkbangGoods.OrderPrice);
                            clsMlikbang.TotalPayPrice = clsMlikbang.TotalOrderPrice;
                        }
                        
                        // 주문에 상품 추가
                        clsMlikbang.arrMilkbangGoods.add(clsMilkbangGoods);
                    }
                    
                    // ======== 8단계: DB에 저장 ========
                    System.out.print(" 총 " + arrMlikbang.size() + " 건 DB에 업로드 중...");
                    for (i = 0; i < arrMlikbang.size(); i++) {
                        clsMilkbangManager.insertMilkbang(arrMlikbang.get(i));
                        System.out.print(" " + (i + 1) + "/" + arrMlikbang.size());
                    }
                    System.out.println(" 업로드 완료");
                }
            }
        } catch (Exception e) {
            System.out.println(this.strError + e);
        } finally {
            // DB 연결 해제
        	closeConnectionSafely(conDB1);
            closeConnectionSafely(conDB2);
        }
    }
    
    /**
     * 커넥션을 안전하게 닫는 헬퍼 메서드
     * - null 체크 및 예외 처리 포함
     * @param conn 닫을 커넥션 객체
     */
    private void closeConnectionSafely(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    DBManager.closeDB(conn);
                }
            } catch (Exception e) {
                System.out.println("커넥션 닫기 실패: " + e.getMessage());
            }
        }
    }
}
