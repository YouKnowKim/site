package com.milk.batch.function;

import com.milk.batch.bean.CommonBean;
import com.milk.batch.bean.GoodsHobBean;
import com.milk.batch.bean.GoodsOptionBean;
import com.milk.batch.bean.MilkbangBean;
import com.milk.batch.bean.ZipCDBean;
import com.milk.batch.common.DBManager;
import com.milk.batch.common.GF;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MilkbangManager {
	private Connection FV_conDB;

	private boolean FV_blnCloseConnectYN = false;

	public MilkbangManager(Connection p_Connection) {
		if (p_Connection == null) {
			this.FV_blnCloseConnectYN = true;
		} else {
			this.FV_conDB = p_Connection;
		}
	}

	public MilkbangManager() {
	}

	public void finalize() {
		if (this.FV_blnCloseConnectYN)
			DBManager.closeDB(this.FV_conDB);
	}
	
	public ArrayList<MilkbangBean> findFileSummary(MilkbangBean p_Param) {
	    String strError = "(MilkbangManager) findFileSummary 에러 : ";
	    Statement stmt = null;
	    ResultSet rs = null;
	    ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
	    
	    try {
	        // 페이징 없이 단순 조회
	        String strSQL = "SELECT f.*, a.AgencyNM " +
	                       "FROM ysc.tc_milkbang_file f " +
	                       "LEFT OUTER JOIN ysc.tc_agency a ON f.AgencyCD = a.AgencyCD " +
	                       "WHERE f.UploadYN = -1 " +  // 미처리 파일만
	                       "ORDER BY f.FileNM ASC";
	        
	        stmt = this.FV_conDB.createStatement();
	        rs = stmt.executeQuery(strSQL);
	        
	        while (rs.next()) {
	            MilkbangBean clsReturn = setAllField_File(rs);
	            clsReturn.AgencyNM = GF.getString(rs, "AgencyNM");
	            arrReturn.add(clsReturn);
	        }
	        
	    } catch (SQLException e) {
	        System.out.println(strError + e.getMessage());
	    } finally {
	        DBManager.rsClose(rs);
	        DBManager.stmtClose(stmt);
	    }
	    
	    return arrReturn;
	}
	
	public ArrayList<MilkbangBean> findFileSummary0(MilkbangBean p_Param) {
	    String strError = "(MilkbangManager) findFileSummary 에러 : ";
	    Statement stmt = null;
	    ResultSet rs = null;
	    ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
	    
	    try {
	        // COUNT 쿼리 구성
	        String strSelect = "SELECT COUNT(*) as TotalCNT";
	        String strFrom = " FROM ysc.tc_milkbang_file " +
	                        "LEFT OUTER JOIN ysc.tc_agency ON " +
	                        "(ysc.tc_milkbang_file.AgencyCD = ysc.tc_agency.AgencyCD)";
	        String strWhere = "";
	        
	        // WHERE 조건 구성
	        if (!p_Param.SearchDT_Type.equals("")) {
	            if (!p_Param.SearchFromDT.equals("")) {
	                strWhere = strWhere + " AND (TO_DATE('" + p_Param.SearchFromDT + 
	                          "', 'YYYY-MM-DD') <= tc_milkbang_file." + p_Param.SearchDT_Type + ")";
	            }
	            if (!p_Param.SearchToDT.equals("")) {
	                // Oracle 날짜 연산 사용
	                strWhere = strWhere + " AND (tc_milkbang_file." + p_Param.SearchDT_Type + 
	                          " < TO_DATE('" + p_Param.SearchToDT + "', 'YYYY-MM-DD') + 1)";
	            }
	        }
	        
	        if (p_Param.UploadYN != 0) {
	            strWhere = strWhere + " AND (tc_milkbang_file.UploadYN = " + p_Param.UploadYN + ")";
	        }
	        
	        if (p_Param.AgencyCD != 0L) {
	            strWhere = strWhere + " AND (tc_milkbang_file.AgencyCD = " + p_Param.AgencyCD + ")";
	        }
	        
	        // WHERE 절 처리 개선
	        if (!strWhere.equals("")) {
	            strWhere = " WHERE " + strWhere.substring(5);
	        }
	        
	        // 전체 건수 조회
	        String strSQL = strSelect + strFrom + strWhere;
	        stmt = this.FV_conDB.createStatement();
	        rs = stmt.executeQuery(strSQL);
	        
	        if (rs.next()) {
	            p_Param.Search_TotalCNT = rs.getInt("TotalCNT");
	        }
	        DBManager.rsClose(rs);
	        
	        // 페이징 계산
	        if (p_Param.Search_TotalCNT > 0) {
	            p_Param.Search_TotalPage = (int) Math.ceil((double)p_Param.Search_TotalCNT / p_Param.Search_ShowCNT);
	        } else {
	            p_Param.Search_TotalPage = 0;
	        }
	        
	        if (p_Param.Search_Page > p_Param.Search_TotalPage) {
	            p_Param.Search_Page = p_Param.Search_TotalPage;
	        }
	        
	        int Start = p_Param.Search_ShowCNT * p_Param.Search_Page - p_Param.Search_ShowCNT;
	        if (Start < 0) Start = 0;
	        int End = Start + p_Param.Search_ShowCNT;
	        
	        // 데이터 조회 - Oracle ROWNUM 사용
	        strSelect = "SELECT * FROM (" +
	                   "  SELECT ROWNUM AS RN, T.* FROM (" +
	                   "    SELECT ysc.tc_milkbang_file.*, ysc.tc_agency.AgencyNM" +
	                   strFrom + strWhere;
	        
	        // ORDER BY 처리
	        if (!p_Param.Search_OrderCol.equals("")) {
	            strSelect += " ORDER BY " + p_Param.Search_OrderCol + " " + p_Param.Search_OrderBy;
	        } else {
	            strSelect += " ORDER BY FileNM ASC";
	        }
	        
	        strSelect += "  ) T WHERE ROWNUM <= " + End + 
	                    ") WHERE RN > " + Start;
	        
	        rs = stmt.executeQuery(strSelect);
	        
	        while (rs.next()) {
	            MilkbangBean clsReturn = setAllField_File(rs);
	            clsReturn.AgencyNM = GF.getString(rs, "AgencyNM");
	            arrReturn.add(clsReturn);
	        }
	        
	    } catch (SQLException e) {
	        System.out.println(strError + e.getMessage());
	    } finally {
	        DBManager.rsClose(rs);
	        DBManager.stmtClose(stmt);
	    }
	    
	    return arrReturn;
	}

	public ArrayList<MilkbangBean> findFileSummary2(MilkbangBean p_Param) {
		String strError = "(MilkbangManager) findFileSummary 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSelect = "SELECT COUNT(*) as TotalCNT";
			String strFrom = " FROM ysc.tc_milkbang_file LEFT OUTER JOIN ysc.tc_agency ON (ysc.tc_milkbang_file.AgencyCD = ysc.tc_agency.AgencyCD)";
			String strWhere = "";
			if (!p_Param.SearchDT_Type.equals("")) {
				if (!p_Param.SearchFromDT.equals(""))
					strWhere = strWhere + " AND ('" + p_Param.SearchFromDT + "' <= tc_milkbang_file."
							+ p_Param.SearchDT_Type + ")";
				if (!p_Param.SearchToDT.equals(""))
					strWhere = strWhere + " AND (tc_milkbang_file." + p_Param.SearchDT_Type + " < date_add('"
							+ p_Param.SearchToDT + "',interval 1 DAY))";
			}
			if (p_Param.UploadYN != 0)
				strWhere = strWhere + " AND (tc_milkbang_file.UploadYN = " + p_Param.UploadYN + ")";
			if (p_Param.AgencyCD != 0L)
				strWhere = strWhere + " AND (tc_milkbang_file.AgencyCD = " + p_Param.AgencyCD + ")";
			strWhere = " WHERE " + strWhere.substring(5);
			String strSQL = strSelect + strFrom + strWhere;
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				rs.first();
				p_Param.Search_TotalCNT = rs.getInt("TotalCNT");
			}
			DBManager.rsClose(rs);
			if (p_Param.Search_TotalCNT > 0) {
				p_Param.Search_TotalPage = (int) Math.ceil(p_Param.Search_TotalCNT / p_Param.Search_ShowCNT);
			} else {
				p_Param.Search_TotalPage = 0;
			}
			if (p_Param.Search_Page > p_Param.Search_TotalPage)
				p_Param.Search_Page = p_Param.Search_TotalPage;
			int Start = p_Param.Search_ShowCNT * p_Param.Search_Page - p_Param.Search_ShowCNT;
			if (Start < 0)
				Start = 0;
			strSelect = "SELECT ysc.tc_milkbang_file.* , ysc.tc_agency.AgencyNM";
			strSQL = strSelect + strFrom + strWhere;
			if (!p_Param.Search_OrderCol.equals("")) {
				strSQL = strSQL + " ORDER BY " + p_Param.Search_OrderCol + " " + p_Param.Search_OrderBy;
			} else {
				strSQL = strSQL + " ORDER BY FileNM ASC";
			}
			if (p_Param.Search_ShowCNT > 0)
				strSQL = strSQL + " LIMIT " + Start + "," + p_Param.Search_ShowCNT;
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					MilkbangBean clsReturn = setAllField_File(rs);
					clsReturn.AgencyNM = GF.getString(rs, "AgencyNM");
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findAllFile() {
		String strError = "(MilkbangManager) findAllFile 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSelect = "SELECT ysc.tc_milkbang_file.*";
			String strFrom = " FROM ysc.tc_milkbang_file";
			String strWhere = "";
			String strSQL = strSelect + strFrom + strWhere;
			strSQL = strSQL + " ORDER BY FileNM ASC";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					MilkbangBean clsReturn = setAllField_File(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public MilkbangBean setAllField_File(ResultSet rs) {
		MilkbangBean clsReturn;
		String strError = "(MilkbangManager) setAllField_File 에러 : ";
		try {
			clsReturn = new MilkbangBean();
			clsReturn.FileNM = GF.getString(rs, "FileNM");
			clsReturn.FileURL = GF.getString(rs, "FileURL");
			clsReturn.DownloadDT = rs.getTimestamp("DownloadDT");
			clsReturn.AgencyCD = rs.getLong("AgencyCD");
			clsReturn.UploadYN = rs.getInt("UploadYN");
			clsReturn.UploadDT = rs.getTimestamp("UploadDT");
			clsReturn.FileStatus = rs.getInt("FileStatus");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		}
		return clsReturn;
	}

	/**
	 * 파일 정보를 DB에 저장 (PreparedStatement 버전)
	 * 
	 * @param p_Param 저장할 파일 정보
	 * @return boolean 저장 성공 여부
	 */
	public boolean insertFileList(MilkbangBean p_Param) {
	    String strError = "(MilkbangManager) insertFileList 에러 : ";
	    PreparedStatement pstmt = null;
	    boolean blnReturn = false;
	    
	    String strSQL = "INSERT INTO ysc.tc_milkbang_file " +
	                   "(FileNM, FileURL, DownloadDT, AgencyCD, UploadYN, UploadDT) " +
	                   "VALUES (?, ?, ?, ?, ?, ?)";
	    
	    try {
	        this.FV_conDB.setAutoCommit(false);
	        pstmt = this.FV_conDB.prepareStatement(strSQL);
	        
	        // 파라미터 설정
	        pstmt.setString(1, p_Param.FileNM);
	        pstmt.setString(2, p_Param.FileURL);
	        pstmt.setTimestamp(3, p_Param.DownloadDT);
	        
	        // AgencyCD null 처리
	        if (p_Param.AgencyCD > 0L) {
	            pstmt.setLong(4, p_Param.AgencyCD);
	        } else {
	            pstmt.setNull(4, java.sql.Types.BIGINT);
	        }
	        
	        pstmt.setInt(5, p_Param.UploadYN);
	        pstmt.setTimestamp(6, p_Param.UploadDT);
	        
	        // 실행
	        int rowCount = pstmt.executeUpdate();
	        this.FV_conDB.commit();
	        
	        blnReturn = (rowCount > 0);
	        
	    } catch (SQLException e) {
	        blnReturn = false;
	        System.out.println(strError + e.getMessage());
	        
	        try {
	            this.FV_conDB.rollback();
	        } catch (SQLException ex) {
	            System.out.println(strError + ex.getMessage());
	        }
	    } finally {
	        DBManager.stmtClose(pstmt);
	    }
	    
	    return blnReturn;
	}

	/**
	 * 파일 상태를 업데이트 (AutoCommit 항상 false)
	 * 
	 * @param p_FileNM 파일명
	 * @param p_FileStatus 파일 상태 코드
	 * @return boolean 업데이트 성공 여부
	 */
	public boolean updateFileStatus(String p_FileNM, int p_FileStatus) {
	    String strError = "(MilkbangManager) updateFileStatus 에러 : ";
	    PreparedStatement pstmt = null;
	    boolean blnReturn = false;
	    
	    // 입력값 검증
	    if (p_FileNM == null || p_FileNM.trim().isEmpty()) {
	        System.out.println(strError + "파일명이 없습니다");
	        return false;
	    }
	    
	    String strSQL = "UPDATE ysc.tc_milkbang_file " +
	                   "SET FileStatus = ?, " +
	                   "    UploadYN = 1, " +
	                   "    UploadDT = SYSDATE " +
	                   "WHERE FileNM = ?";
	    
	    try {
	        // 무조건 AutoCommit을 false로 설정
	        this.FV_conDB.setAutoCommit(false);
	        
	        pstmt = this.FV_conDB.prepareStatement(strSQL);
	        
	        // 파라미터 설정
	        pstmt.setInt(1, p_FileStatus);
	        pstmt.setString(2, p_FileNM);
	        
	        // 실행
	        int rowCount = pstmt.executeUpdate();
	        
	        if (rowCount > 0) {
	            blnReturn = true;
	            // 커밋은 호출하는 쪽에서 처리
	            System.out.println("파일 상태 업데이트 완료 (커밋 대기): " + p_FileNM + " → " + p_FileStatus);
	        } else {
	            System.out.println(strError + "업데이트할 파일 없음: " + p_FileNM);
	        }
	        
	    } catch (SQLException e) {
	        blnReturn = false;
	        System.out.println(strError + e.getMessage());
	    } finally {
	        DBManager.stmtClose(pstmt);
	    }
	    
	    return blnReturn;
	}

	/**
	 * 파일 정보를 업데이트 (PreparedStatement 버전)
	 * 
	 * @param p_Param 업데이트할 파일 정보
	 * @return boolean 업데이트 성공 여부
	 */
	public boolean updateFileList(MilkbangBean p_Param) {
	    String strError = "(MilkbangManager) updateFileList 에러 : ";
	    PreparedStatement pstmt = null;
	    boolean blnReturn = false;
	    
	    // 입력값 검증
	    if (p_Param == null || p_Param.FileNM == null || p_Param.FileNM.trim().isEmpty()) {
	        System.out.println(strError + "필수 파라미터(FileNM) 누락");
	        return false;
	    }
	    
	    try {
	        this.FV_conDB.setAutoCommit(false);
	        
	        // 동적 SQL 구성
	        StringBuilder sql = new StringBuilder();
	        sql.append("UPDATE ysc.tc_milkbang_file SET ");
	        sql.append("DownloadDT = ?");
	        
	        // UploadYN 조건부 추가
	        boolean updateUploadYN = (p_Param.UploadYN != 0);
	        if (updateUploadYN) {
	            sql.append(", UploadYN = ?");
	        }
	        
	        sql.append(" WHERE FileNM = ?");
	        
	        pstmt = this.FV_conDB.prepareStatement(sql.toString());
	        
	        // 파라미터 바인딩
	        int paramIndex = 1;
	        
	        // DownloadDT 설정
	        if (p_Param.DownloadDT != null) {
	            pstmt.setTimestamp(paramIndex++, p_Param.DownloadDT);
	        } else {
	            pstmt.setTimestamp(paramIndex++, new Timestamp(System.currentTimeMillis()));
	        }
	        
	        // UploadYN 설정 (조건부)
	        if (updateUploadYN) {
	            pstmt.setInt(paramIndex++, p_Param.UploadYN);
	        }
	        
	        // WHERE 조건 - FileNM
	        pstmt.setString(paramIndex, p_Param.FileNM);
	        
	        // 실행
	        int rowCount = pstmt.executeUpdate();
	        
	        if (rowCount > 0) {
	            this.FV_conDB.commit();
	            blnReturn = true;
	            System.out.println("파일 정보 업데이트 성공: " + p_Param.FileNM + " (수정된 행: " + rowCount + ")");
	        } else {
	            this.FV_conDB.rollback();
	            System.out.println(strError + "업데이트할 파일을 찾을 수 없음: " + p_Param.FileNM);
	        }
	        
	    } catch (SQLException e) {
	        blnReturn = false;
	        System.out.println(strError + e.getMessage());
	        
	        try {
	            if (this.FV_conDB != null && !this.FV_conDB.getAutoCommit()) {
	                this.FV_conDB.rollback();
	            }
	        } catch (SQLException ex) {
	            System.out.println(strError + "롤백 실패: " + ex.getMessage());
	        }
	    } finally {
	        // PreparedStatement 정리
	        if (pstmt != null) {
	            try {
	                pstmt.close();
	            } catch (SQLException e) {
	                System.out.println(strError + "PreparedStatement close 실패");
	            }
	        }
	    }
	    
	    return blnReturn;
	}

	/**
	 * Excel 파일(.xls)을 새로운 형식(.xlsx)으로 변환
	 * VBS 스크립트를 사용하여 변환 작업 수행
	 * 
	 * @param p_Origin_FilePath 원본 파일이 있는 디렉토리 경로
	 * @param p_Origin_FileNM   원본 파일명 (.xls)
	 * @param p_Target_FilePath 변환된 파일을 저장할 디렉토리 경로
	 * @return 변환된 파일의 전체 경로 (실패시 빈 문자열)
	 */
	public String convertExcel(String p_Origin_FilePath, String p_Origin_FileNM, String p_Target_FilePath) {
	    String strError = "(MilkbangManager) convertExcel 에러 : ";
	    String strResult = "";
	    
	    try {
	        // ======== 1. 대상 파일명 생성 ========
	        // .xls, .XLS 확장자를 제거하고 .xlsx 추가
	        String strTarget_FileNM = p_Origin_FileNM.replace(".xls", "").replace(".XLS", "") + ".xlsx";
	        
	        // ======== 2. 대상 디렉토리 설정 ========
	        // 기본값: 원본 경로/temp
	        String strTarget_Path = p_Origin_FilePath + "/temp";
	        // 대상 경로가 지정된 경우 해당 경로 사용
	        if (!p_Target_FilePath.equals(""))
	            strTarget_Path = p_Target_FilePath;
	        
	        // ======== 3. 대상 디렉토리 생성 ========
	        File file = new File(strTarget_Path);
	        if (!file.exists())
	            file.mkdirs();  // 디렉토리가 없으면 생성
	        
	        // ======== 4. 기존 파일 삭제 ========
	        // 동일한 이름의 파일이 있으면 삭제
	        file = new File(strTarget_Path + "/" + strTarget_FileNM);
	        if (file.exists() == true)
	            file.delete();
	        
	        // ======== 5. VBS 스크립트 실행 ========
	        // Excel COM 객체를 사용하여 파일 변환
	        String vbsPath = "c:/develop/FILES/Milkbang/util/XLStoXLSX.vbs";  // VBS 스크립트 경로
	        
	        // cscript 명령으로 VBS 실행
	        // 인자: VBS경로, 원본파일경로, 대상파일경로
	        Process p = Runtime.getRuntime().exec("cscript \"" + vbsPath + "\" \"" + 
	                p_Origin_FilePath + "/" + p_Origin_FileNM + "\" \"" + 
	                strTarget_Path + "/" + strTarget_FileNM + "\"");
	        
	        // 프로세스 완료 대기
	        p.waitFor();
	        
	        // 프로세스 강제 종료 (리소스 해제)
	        p.destroyForcibly();
	        
	        // ======== 6. 결과 경로 설정 ========
	        file = null;  // 파일 객체 해제
	        strResult = strTarget_Path + "/" + strTarget_FileNM;  // 변환된 파일 경로
	        
	        // TODO: 실제 파일 생성 여부 확인 필요
	         File resultFile = new File(strResult);
	         if (!resultFile.exists()) {
	             System.out.println("변환 실패: 파일이 생성되지 않음");
	             return "";
	         }
	        
	    } catch (Exception e) {
	        System.out.println(strError + e.getMessage());
	        strResult = "";  // 오류 발생시 빈 문자열 반환
	    }
	    
	    return strResult;
	}

	/**
	 * 밀크방 주문 코드 생성 (Oracle 버전)
	 * 날짜 기반으로 순차적인 고유 코드 생성
	 * 
	 * @param p_Date 기준 날짜 (yyyy-MM-dd 형식)
	 * @return 생성된 주문 코드
	 */
	public long makeMilkbangCD(String p_Date) {
	    String strError = "(MilkbangManager) makeMilkbangCD 에러 : ";
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    long lngReturn = 0L;
	    
	    try {
	        // 날짜 처리
	        Calendar cal = Calendar.getInstance();
	        String strDate = (p_Date == null) ? "" : p_Date;
	        strDate = strDate.replaceAll("-", "").replaceAll("\\.", "").replaceAll(" ", "");
	        String temp = "300";
	        
	        if (strDate.equals("")) {
	            strDate = cal.get(Calendar.YEAR) + 
	                     GF.right("0" + (cal.get(Calendar.MONTH) + 1), 2) + 
	                     GF.right("0" + cal.get(Calendar.DAY_OF_MONTH), 2);
	        }
	        
	        // 트랜잭션 시작
	        this.FV_conDB.setAutoCommit(false);
	        
	        // Oracle: NVL 사용 (IFNULL 대체)
	        String strSQL = "SELECT NVL(MAX(tc_code.CodeSEQ), 0) as CodeSEQ " +
	                       "FROM ysc.tc_code " +
	                       "WHERE tc_code.CodeDate = ? " +
	                       "AND tc_code.CodeType = ?";
	        
	        pstmt = this.FV_conDB.prepareStatement(strSQL);
	        pstmt.setString(1, "300" + strDate);
	        pstmt.setString(2, temp);
	        rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            long currentSeq = rs.getLong("CodeSEQ");
	            
	            if (currentSeq > 0L) {
	                // 기존 코드 시퀀스 증가
	                lngReturn = currentSeq + 1L;
	                
	                rs.close();
	                pstmt.close();
	                
	                strSQL = "UPDATE ysc.tc_code " +
	                        "SET CodeSEQ = CodeSEQ + 1 " +
	                        "WHERE CodeDate = ? " +
	                        "AND CodeType = ?";
	                
	                pstmt = this.FV_conDB.prepareStatement(strSQL);
	                pstmt.setString(1, "300" + strDate);
	                pstmt.setString(2, temp);
	                pstmt.executeUpdate();
	            } else {
	                // 신규 코드 생성
	                rs.close();
	                pstmt.close();
	                
	                strSQL = "INSERT INTO ysc.tc_code (CodeDate, CodeType, CodeSEQ) " +
	                        "VALUES (?, ?, ?)";
	                
	                pstmt = this.FV_conDB.prepareStatement(strSQL);
	                pstmt.setString(1, "300" + strDate);
	                pstmt.setString(2, temp);
	                pstmt.setInt(3, 1);
	                pstmt.executeUpdate();
	                
	                lngReturn = 1L;
	            }
	        }
	        
	        // 최종 코드 생성
	        if (99999L < lngReturn) {
	            lngReturn = Long.parseLong(temp + strDate + String.valueOf(lngReturn));
	        } else {
	            String strTemp = temp + strDate + GF.right(String.valueOf(100000), 
	                                                       String.valueOf(99999).length());
	            lngReturn = Long.parseLong(strTemp) + lngReturn;
	        }
	        
	        // 커밋
	        //this.FV_conDB.commit();
	        
	    } catch (SQLException e) {
	        System.out.println(strError + e.getMessage());
	        try {
	            this.FV_conDB.rollback();
	        } catch (SQLException ex) {
	            System.out.println(strError + "롤백 실패: " + ex.getMessage());
	        }
	    } finally {
	        DBManager.rsClose(rs);
	        DBManager.stmtClose(pstmt);
	    }
	    
	    return lngReturn;
	}

	public GoodsOptionBean checkGoodsOptionCD(ArrayList<GoodsOptionBean> p_arrParam, long p_GoodsOptionCD) {
		String strError = "(MilkbangManager) checkGoodsOptionCD 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		GoodsOptionBean clsReturn = new GoodsOptionBean();
		clsReturn.GoodsCD = -1L;
		clsReturn.GoodsOptionCD = -1L;
		clsReturn.OptionNM = "매칭안됨";
		try {
			for (int i = 0; i < p_arrParam.size(); i++) {
				if (((GoodsOptionBean) p_arrParam.get(i)).GoodsOptionCD == p_GoodsOptionCD) {
					clsReturn = p_arrParam.get(i);
					break;
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return clsReturn;
	}

	public GoodsOptionBean checkGoodsOptionNM(ArrayList<GoodsOptionBean> p_arrParam, String p_GoodsOptionNM) {
		String strError = "(MilkbangManager) checkGoodsOptionNM 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		GoodsOptionBean clsReturn = new GoodsOptionBean();
		clsReturn.GoodsCD = -1L;
		clsReturn.GoodsOptionCD = -1L;
		clsReturn.OptionNM = "매칭안됨";
		ArrayList<GoodsOptionBean> arrGoodsOption = new ArrayList<>();
		String strParam = (p_GoodsOptionNM == null) ? "" : p_GoodsOptionNM.replaceAll(" ", "");
		try {
			if (!strParam.equals("")) {
				boolean blnExistYN = false;
				for (int i = 0; i < p_arrParam.size(); i++) {
					GoodsOptionBean clsGoodsOption = p_arrParam.get(i);
					blnExistYN = false;
					if (clsGoodsOption.OptionNM.replaceAll(" ", "").equals(strParam)) {
						clsReturn = clsGoodsOption;
						blnExistYN = true;
						break;
					}
					String[] arrAliasNM = clsGoodsOption.AliasNM.split("\\|");
					for (int j = 0; j < arrAliasNM.length; j++) {
						if (!arrAliasNM[j].replaceAll(" ", "").equals("")
								&& arrAliasNM[j].replaceAll(" ", "").equals(strParam)) {
							clsReturn = clsGoodsOption;
							blnExistYN = true;
							break;
						}
					}
					if (blnExistYN == true)
						break;
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return clsReturn;
	}

	public GoodsOptionBean checkMisGoodsOptionCD(ArrayList<GoodsOptionBean> p_arrParam, String p_GoodsOptionCD) {
		String strError = "(MilkbangManager) checkMisGoodsOptionCD 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		GoodsOptionBean clsReturn = new GoodsOptionBean();
		clsReturn.GoodsCD = -1L;
		clsReturn.GoodsOptionCD = -1L;
		clsReturn.OptionNM = "매칭안됨";
		ArrayList<GoodsOptionBean> arrGoodsOption = new ArrayList<>();
		String strParam = (p_GoodsOptionCD == null) ? "" : p_GoodsOptionCD.replaceAll(" ", "");
		try {
			if (!strParam.equals("")) {
				boolean blnExistYN = false;
				for (int i = 0; i < p_arrParam.size(); i++) {
					GoodsOptionBean clsGoodsOption = p_arrParam.get(i);
					blnExistYN = false;
					if (!clsGoodsOption.MisCD.replaceAll(" ", "").equals("")
							&& clsGoodsOption.MisCD.replaceAll(" ", "").equals(strParam)) {
						clsReturn = clsGoodsOption;
						blnExistYN = true;
						break;
					}
					if (blnExistYN == true)
						break;
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return clsReturn;
	}
	
	/**
	 * 밀크방(유제품 배송) 엑셀 파일을 읽어서 MilkbangBean 객체 리스트로 변환
	 * 
	 * @param p_OriginFileNM   원본 파일명 (DB 상태 업데이트용)
	 * @param p_TargerFilePath 읽을 엑셀 파일의 전체 경로
	 * @return ArrayList<MilkbangBean> 파싱된 데이터 리스트 (오류 시 빈 리스트)
	 */
	public ArrayList<MilkbangBean> readExcel_Milkbang(String p_OriginFileNM, String p_TargerFilePath) {
	    String strError = "(MilkbangManager) readExcel_Milkbang 에러 : ";
	    
	    // POI 라이브러리 워크북 객체 (.xls용 HSSF, .xlsx용 XSSF)
	    HSSFWorkbook workbook_h = null;
	    HSSFSheet sheet_h = null;
	    XSSFWorkbook workbook_x = null;
	    XSSFSheet sheet_x = null;
	    
	    boolean blnExceFilelYN = true;   // 엑셀 파일 여부
	    boolean blnOldExcelYN = true;    // 구형 엑셀(.xls) 여부
	    
	    // 반환할 데이터 리스트
	    ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
	    MilkbangBean clsReturn = new MilkbangBean();
	    
	    // [미사용 변수들 - 추후 기능 확장용으로 추정]
	    ArrayList<String> arrListIncreaseNM = new ArrayList<>();  // 증가 목록
	    ArrayList<String> arrListDecreaseNM = new ArrayList<>();  // 감소 목록
	    String strPromoTeamNM = "";        // 판촉팀명
	    String strTemp = "", strDate = "", strYear = "", strMonth = "", strDay = "";
	    String strBeforeAgencyNM = "";    // 이전 대리점명
	    boolean blnKindergartenYN = false; // 유치원 여부
	    
	    // 엑셀에서 읽을 주요 필드
	    String strAgencyCD = "";   // 대리점 코드
	    String strPromoDT = "";    // 판촉일자
	    String strPutDT = "";      // 투입일자
	    String strAgencyNM = ""; // 대리점 명
	    
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	    ZipCDBean clsZipCD = null;
	    
	    try {
	        // ======== 1. 파일 확장자로 엑셀 형식 판별 ========
	        if (GF.right(p_TargerFilePath, 4).toLowerCase().equals(".xls") == true) {
	            blnOldExcelYN = true;   // Excel 97-2003 형식
	        } else if (GF.right(p_TargerFilePath, 5).toLowerCase().equals(".xlsx") == true) {
	            blnOldExcelYN = false;  // Excel 2007+ 형식
	        } else {
	            blnExceFilelYN = false; // 엑셀 파일이 아님
	        }
	        
	        // ======== 2. 엑셀 파일 읽기 ========
	        if (blnExceFilelYN == true) {
	            int intlastRow;
	            
	            // 파일 형식에 맞는 워크북 생성
	            if (blnOldExcelYN == true) {
	                // .xls 파일 처리 (HSSF 사용)
	                workbook_h = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(p_TargerFilePath)));
	            } else {
	                // .xlsx 파일 처리 (XSSF 사용)
	                workbook_x = new XSSFWorkbook(OPCPackage.open(new FileInputStream(p_TargerFilePath)));
	            }
	            
	            // 첫 번째 시트 선택 및 마지막 행 번호 확인
	            if (blnOldExcelYN == true) {
	                sheet_h = workbook_h.getSheetAt(0);
	                intlastRow = sheet_h.getLastRowNum();
	            } else {
	                sheet_x = workbook_x.getSheetAt(0);
	                intlastRow = sheet_x.getLastRowNum();
	            }
	            
	            // ======== 3. 데이터 행 처리 (1행부터 시작 - 0행은 헤더) ========
	            if (intlastRow >= 1) {
	                int intFirstRow = 1;  // 데이터 시작 행
	                
	                for (int intRow = intFirstRow; intRow <= intlastRow; intRow++) {
	                    XSSFRow xSSFRow = null;
	                    
	                    // [주의: 버그 있음 - hSSFRow는 지역변수로만 선언되고 사용 안됨]
	                    if (blnOldExcelYN == true) {
	                        HSSFRow hSSFRow = sheet_h.getRow(intRow);  // 버그: 사용되지 않음
	                    } else {
	                        xSSFRow = sheet_x.getRow(intRow);
	                    }
	                    
	                    int intCol = 0;  // 컬럼 인덱스
	                    clsReturn = new MilkbangBean();
	                    
	                    // 파일명 저장 (경로에서 파일명만 추출)
	                    clsReturn.FileNM = p_TargerFilePath.substring(p_TargerFilePath.lastIndexOf("/") + 1);
	                    
	                    // ======== 4. 필수 필드 검증 및 데이터 추출 ========
	                    if (xSSFRow.getCell(intCol) != null) {
	                        // 대리점코드(0열), 판촉일(10열), 투입일(11열) 추출 및 공백 제거
	                        strAgencyCD = GF.excelString((Row) xSSFRow, intCol).replaceAll(" ", "");
	                        strPromoDT = GF.excelString((Row) xSSFRow, intCol + 10)
	                                    .replaceAll(" ", "").replaceAll("\\.", "");
	                        strPutDT = GF.excelString((Row) xSSFRow, intCol + 11)
	                                  .replaceAll(" ", "").replaceAll("\\.", "");
	                        
	                        // 날짜 상호 보완 처리: 판촉일이 없으면 투입일 사용, 반대도 동일
	                        if (strPromoDT.equals("")) {
	                            if (!strPutDT.equals(""))
	                                strPromoDT = strPutDT;
	                        } else if (strPutDT.equals("")) {
	                            strPutDT = strPromoDT;
	                        }
	                        
	                        // ======== 5. 필수 데이터가 모두 있는 경우만 처리 ========
	                        if (!strAgencyCD.equals("") && !strPromoDT.equals("") && !strPutDT.equals("")) {
	                            intCol++;  // 1열로 이동
	                            
	                            // 대리점 코드 변환 (뒤 3자리만 추출 후 10000번대로 변환)
	                            strAgencyCD = GF.right(strAgencyCD, 3);
	                            clsReturn.AgencyCD = Long.parseLong(strAgencyCD);
	                            if (clsReturn.AgencyCD < 10000L)
	                                clsReturn.AgencyCD = 10000L + clsReturn.AgencyCD;  // 예: 123 → 10123
	                            
	                            intCol++;  // 2열로 이동
	                            
	                            // ======== 6. 고객 정보 매핑 ========
	                            // 판촉 담당자명 (백슬래시 제거)
	                            clsReturn.PromoPersonNM = GF.excelString((Row) xSSFRow, intCol++)
	                                                       .replaceAll(" ", "").replaceAll("\\\\", "");
	                            clsReturn.PromoPersonNM_Origin = clsReturn.PromoPersonNM;  // 원본 보관
	                            
	                            // 주소 정보
	                            clsReturn.PostArea = GF.excelString((Row) xSSFRow, intCol++);        // 우편 지역
	                            clsReturn.OrderZipCD = GF.excelString((Row) xSSFRow, intCol++);      // 우편번호
	                            clsReturn.ReceiveZipCD = clsReturn.OrderZipCD;  // 수령지 = 주문지 복사
	                            
	                            clsReturn.OrderAddress1 = GF.excelString((Row) xSSFRow, intCol++)
	                                                      .replaceAll("\\\\", "");  // 주문 주소
	                            clsReturn.ReceiveAddress1 = clsReturn.OrderAddress1;  // 수령 주소 복사
	                            
	                            intCol++;  // 6열 건너뜀
	                            clsReturn.AddressType = "";  // 주소 타입 (미사용)
	                            
	                            // 주문자 정보
	                            clsReturn.OrderUserNM = GF.excelString((Row) xSSFRow, intCol++)
	                                                     .replaceAll(" ", "").replaceAll("\\\\", "");
	                            clsReturn.ReceiveUserNM = clsReturn.OrderUserNM;  // 수령자 = 주문자
	                            
	                            // 연락처 정보
	                            clsReturn.OrderHomePhone = GF.excelString((Row) xSSFRow, intCol++)
	                                                        .replaceAll(" ", "").replaceAll("\\\\", "");
	                            clsReturn.ReceiveHomePhone = clsReturn.OrderHomePhone;
	                            
	                            clsReturn.OrderCellPhone = GF.excelString((Row) xSSFRow, intCol++)
	                                                        .replaceAll(" ", "").replaceAll("\\\\", "");
	                            clsReturn.ReceiveCellPhone = clsReturn.OrderCellPhone;
	                            
	                            // ======== 7. 날짜 정보 처리 ========
	                            clsReturn.PromoDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));  // 판촉일
	                            clsReturn.PutDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));    // 투입일
	                            
	                            // 날짜 상호 보완 (null 체크)
	                            if (clsReturn.PromoDT == null) {
	                                if (clsReturn.PutDT != null)
	                                    clsReturn.PromoDT = clsReturn.PutDT;
	                            } else if (clsReturn.PutDT == null) {
	                                clsReturn.PutDT = clsReturn.PromoDT;
	                            }
	                            
	                            clsReturn.OrderDT = clsReturn.PromoDT;  // 주문일 = 판촉일
	                            
	                            // ======== 8. 주문일이 유효한 경우 상품 정보 처리 ========
	                            if (clsReturn.OrderDT != null) {
	                                // 상품 정보
	                                clsReturn.GoodsOptionCD_Origin = GF.excelString((Row) xSSFRow, intCol++)
	                                                                   .replaceAll(" ", "").replaceAll("\\\\", "");
	                                clsReturn.GoodsOptionNM_Origin = GF.excelString((Row) xSSFRow, intCol++)
	                                                                   .replaceAll("\\\\", "");
	                                
	                                // 수량 (빈값은 0으로 처리)
	                                strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
	                                strTemp = strTemp.equals("") ? "0" : strTemp;
	                                clsReturn.Quantity = Integer.parseInt(strTemp);
	                                
	                                // 단가
	                                clsReturn.UnitPrice = GF.toBigDecimal(GF.excelString((Row) xSSFRow, intCol++));
	                                
	                                // 주문 부가 정보
	                                clsReturn.WeekRemark = GF.excelString((Row) xSSFRow, intCol++);        // 주간 비고
	                                clsReturn.OrderKind = GF.excelString((Row) xSSFRow, intCol++)
	                                                       .replaceAll("\\\\", "");  // 주문 종류
	                                
	                                // 할인 정보 (대리점 할인)
	                                strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
	                                strTemp = strTemp.equals("") ? "0" : strTemp;
	                                clsReturn.AgencyHob = new BigDecimal(strTemp);
	                                
	                                // 본사 할인
	                                strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
	                                strTemp = strTemp.equals("") ? "0" : strTemp;
	                                clsReturn.HQHob = GF.toBigDecimal(strTemp);
	                                
	                                // 주간 수량
	                                strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
	                                strTemp = strTemp.equals("") ? "0" : strTemp;
	                                clsReturn.WeekQty = Integer.parseInt(strTemp);
	                                
	                                // 판촉 정보
	                                clsReturn.PromoGiftNM = GF.excelString((Row) xSSFRow, intCol++)
	                                                          .replaceAll("\\\\", "");  // 판촉 선물명
	                                
	                                // 계약 기간
	                                strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
	                                strTemp = strTemp.equals("") ? "0" : strTemp;
	                                clsReturn.ContractPeriod = Integer.parseInt(strTemp);
	                                
	                                // 지급 정보
	                                clsReturn.GiveDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));  // 지급일
	                                clsReturn.GivePersonNM = GF.excelString((Row) xSSFRow, intCol++)
	                                                          .replaceAll(" ", "").replaceAll("\\\\", "");  // 지급자
	                                
	                                // 중지 정보
	                                clsReturn.StopDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));  // 중지일
	                                clsReturn.StopReason = GF.excelString((Row) xSSFRow, intCol++)
	                                                        .replaceAll("\\\\", "");  // 중지 사유
	                                
	                                // 유효한 데이터를 리스트에 추가
	                                arrReturn.add(clsReturn);
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        
	        // ======== 9. 처리 결과에 따른 파일 상태 업데이트 ========
	        if (arrReturn.size() <= 0)
	            updateFileStatus(p_OriginFileNM, 2);  // 상태 2: 데이터 없음
	            
	    } catch (Exception e) {
	        System.out.println(strError + e.getMessage());
	        arrReturn = new ArrayList<>();  // 오류 시 빈 리스트 반환
	        updateFileStatus(p_OriginFileNM, 3);  // 상태 3: 처리 오류
	    } finally {
	        // 리소스 정리
	        sheet_h = null;
	        sheet_x = null;
	        workbook_h = null;
	        workbook_x = null;
	    }
	    
	    return arrReturn;
	}

	public ArrayList<MilkbangBean> readExcel_Milkbang2(String p_OriginFileNM, String p_TargerFilePath) {
		String strError = "(MilkbangManager) readExcel_Milkbang 에러 : ";
		HSSFWorkbook workbook_h = null;
		HSSFSheet sheet_h = null;
		XSSFWorkbook workbook_x = null;
		XSSFSheet sheet_x = null;
		boolean blnExceFilelYN = true;
		boolean blnOldExcelYN = true;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		MilkbangBean clsReturn = new MilkbangBean();
		ArrayList<String> arrListIncreaseNM = new ArrayList<>();
		ArrayList<String> arrListDecreaseNM = new ArrayList<>();
		String strPromoTeamNM = "";
		String strTemp = "", strDate = "", strYear = "", strMonth = "", strDay = "";
		String strBeforeAgencyNM = "";
		boolean blnKindergartenYN = false;
		String strAgencyCD = "";
		String strPromoDT = "";
		String strPutDT = "";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ZipCDBean clsZipCD = null;
		try {
			if (GF.right(p_TargerFilePath, 4).toLowerCase().equals(".xls") == true) {
				blnOldExcelYN = true;
			} else if (GF.right(p_TargerFilePath, 5).toLowerCase().equals(".xlsx") == true) {
				blnOldExcelYN = false;
			} else {
				blnExceFilelYN = false;
			}
			if (blnExceFilelYN == true) {
				int intlastRow;
				if (blnOldExcelYN == true) {
					workbook_h = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(p_TargerFilePath)));
				} else {
					workbook_x = new XSSFWorkbook(OPCPackage.open(new FileInputStream(p_TargerFilePath)));
				}
				if (blnOldExcelYN == true) {
					sheet_h = workbook_h.getSheetAt(0);
					intlastRow = sheet_h.getLastRowNum();
				} else {
					sheet_x = workbook_x.getSheetAt(0);
					intlastRow = sheet_x.getLastRowNum();
				}
				if (intlastRow >= 1) {
					int intFirstRow = 1;
					for (int intRow = intFirstRow; intRow <= intlastRow; intRow++) {
						XSSFRow xSSFRow = null;
						if (blnOldExcelYN == true) {
							HSSFRow hSSFRow = sheet_h.getRow(intRow);
						} else {
							xSSFRow = sheet_x.getRow(intRow);
						}
						int intCol = 0;
						clsReturn = new MilkbangBean();
						clsReturn.FileNM = p_TargerFilePath.substring(p_TargerFilePath.lastIndexOf("/") + 1);
						if (xSSFRow.getCell(intCol) != null) {
							strAgencyCD = GF.excelString((Row) xSSFRow, intCol).replaceAll(" ", "");
							strPromoDT = GF.excelString((Row) xSSFRow, intCol + 10).replaceAll(" ", "")
									.replaceAll("\\.", "");
							strPutDT = GF.excelString((Row) xSSFRow, intCol + 11).replaceAll(" ", "").replaceAll("\\.",
									"");
							if (strPromoDT.equals("")) {
								if (!strPutDT.equals(""))
									strPromoDT = strPutDT;
							} else if (strPutDT.equals("")) {
								strPutDT = strPromoDT;
							}
							if (!strAgencyCD.equals("") && !strPromoDT.equals("") && !strPutDT.equals("")) {
								intCol++;
								strAgencyCD = GF.right(strAgencyCD, 3);
								clsReturn.AgencyCD = Long.parseLong(strAgencyCD);
								if (clsReturn.AgencyCD < 10000L)
									clsReturn.AgencyCD = 10000L + clsReturn.AgencyCD;
								intCol++;
								clsReturn.PromoPersonNM = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "")
										.replaceAll("\\\\", "");
								clsReturn.PromoPersonNM_Origin = clsReturn.PromoPersonNM;
								clsReturn.PostArea = GF.excelString((Row) xSSFRow, intCol++);
								clsReturn.OrderZipCD = GF.excelString((Row) xSSFRow, intCol++);
								clsReturn.ReceiveZipCD = clsReturn.OrderZipCD;
								clsReturn.OrderAddress1 = GF.excelString((Row) xSSFRow, intCol++).replaceAll("\\\\",
										"");
								clsReturn.ReceiveAddress1 = clsReturn.OrderAddress1;
								intCol++;
								clsReturn.AddressType = "";
								clsReturn.OrderUserNM = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "")
										.replaceAll("\\\\", "");
								clsReturn.ReceiveUserNM = clsReturn.OrderUserNM;
								clsReturn.OrderHomePhone = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "")
										.replaceAll("\\\\", "");
								clsReturn.ReceiveHomePhone = clsReturn.OrderHomePhone;
								clsReturn.OrderCellPhone = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "")
										.replaceAll("\\\\", "");
								clsReturn.ReceiveCellPhone = clsReturn.OrderCellPhone;
								clsReturn.PromoDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));
								clsReturn.PutDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));
								if (clsReturn.PromoDT == null) {
									if (clsReturn.PutDT != null)
										clsReturn.PromoDT = clsReturn.PutDT;
								} else if (clsReturn.PutDT == null) {
									clsReturn.PutDT = clsReturn.PromoDT;
								}
								clsReturn.OrderDT = clsReturn.PromoDT;
								if (clsReturn.OrderDT != null) {
									clsReturn.GoodsOptionCD_Origin = GF.excelString((Row) xSSFRow, intCol++)
											.replaceAll(" ", "").replaceAll("\\\\", "");
									clsReturn.GoodsOptionNM_Origin = GF.excelString((Row) xSSFRow, intCol++)
											.replaceAll("\\\\", "");
									strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
									strTemp = strTemp.equals("") ? "0" : strTemp;
									clsReturn.Quantity = Integer.parseInt(strTemp);
									clsReturn.UnitPrice = GF.toBigDecimal(GF.excelString((Row) xSSFRow, intCol++));
									clsReturn.WeekRemark = GF.excelString((Row) xSSFRow, intCol++);
									clsReturn.OrderKind = GF.excelString((Row) xSSFRow, intCol++).replaceAll("\\\\",
											"");
									strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
									strTemp = strTemp.equals("") ? "0" : strTemp;
									clsReturn.AgencyHob = new BigDecimal(strTemp);
									strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
									strTemp = strTemp.equals("") ? "0" : strTemp;
									clsReturn.HQHob = GF.toBigDecimal(strTemp);
									strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
									strTemp = strTemp.equals("") ? "0" : strTemp;
									clsReturn.WeekQty = Integer.parseInt(strTemp);
									clsReturn.PromoGiftNM = GF.excelString((Row) xSSFRow, intCol++).replaceAll("\\\\",
											"");
									strTemp = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "");
									strTemp = strTemp.equals("") ? "0" : strTemp;
									clsReturn.ContractPeriod = Integer.parseInt(strTemp);
									clsReturn.GiveDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));
									clsReturn.GivePersonNM = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "")
											.replaceAll("\\\\", "");
									clsReturn.StopDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));
									clsReturn.StopReason = GF.excelString((Row) xSSFRow, intCol++).replaceAll("\\\\",
											"");
									arrReturn.add(clsReturn);
								}
							}
						}
					}
				}
			}
			if (arrReturn.size() <= 0)
				updateFileStatus(p_OriginFileNM, 2);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			arrReturn = new ArrayList<>();
			updateFileStatus(p_OriginFileNM, 3);
		} finally {
			sheet_h = null;
			sheet_x = null;
			workbook_h = null;
			workbook_x = null;
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findMilkbangTemp(String p_StartDT, String p_EndDT) {
		String strError = "(MilkbangManager) findMilkbangTemp : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		MilkbangBean clsReturn = new MilkbangBean();
		ArrayList<String> arrListIncreaseNM = new ArrayList<>();
		ArrayList<String> arrListDecreaseNM = new ArrayList<>();
		String strPromoTeamNM = "";
		String strTemp = "", strDate = "", strYear = "", strMonth = "", strDay = "";
		String strBeforeAgencyNM = "";
		boolean blnKindergartenYN = false;
		String strAgencyCD = "";
		String strPromoDT = "";
		String strPutDT = "";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ZipCDBean clsZipCD = null;
		try {
			String strSQL = "SELECT temp_milkbang.* FROM temp_milkbang WHERE temp_milkbang.OrderDT >= '" + p_StartDT
					+ "' AND temp_milkbang.OrderDT <= '" + p_EndDT + "'";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				boolean blnExistYN = false;
				for (int i = 0; i < intMaxRow; i++) {
					clsReturn = new MilkbangBean();
					clsReturn.FileNM = "2017년9월_수동복구";
					strAgencyCD = GF.getString(rs, "AgencyCD");
					strPromoDT = GF.getString(rs, "PromoDT");
					strPutDT = GF.getString(rs, "PutDT");
					if (strPromoDT.equals("")) {
						if (!strPutDT.equals(""))
							strPromoDT = strPutDT;
					} else if (strPutDT.equals("")) {
						strPutDT = strPromoDT;
					}
					if (!strAgencyCD.equals("") && !strPromoDT.equals("") && !strPutDT.equals("")) {
						strAgencyCD = GF.right(strAgencyCD, 3);
						clsReturn.AgencyCD = Long.parseLong(strAgencyCD);
						if (clsReturn.AgencyCD < 10000L)
							clsReturn.AgencyCD = 10000L + clsReturn.AgencyCD;
						clsReturn.PromoPersonNM = GF.getString(rs, "PromoPersonNM");
						clsReturn.PromoPersonNM_Origin = clsReturn.PromoPersonNM;
						clsReturn.PostArea = "";
						clsReturn.OrderZipCD = "";
						clsReturn.ReceiveZipCD = "";
						clsReturn.OrderAddress1 = GF.getString(rs, "OrderAddress1");
						clsReturn.ReceiveAddress1 = clsReturn.OrderAddress1;
						clsReturn.AddressType = "";
						clsReturn.OrderUserNM = GF.getString(rs, "OrderUserNM");
						clsReturn.ReceiveUserNM = clsReturn.OrderUserNM;
						clsReturn.OrderHomePhone = GF.getString(rs, "OrderHomePhone");
						clsReturn.ReceiveHomePhone = clsReturn.OrderHomePhone;
						clsReturn.OrderCellPhone = GF.getString(rs, "OrderCellPhone");
						clsReturn.ReceiveCellPhone = clsReturn.OrderCellPhone;
						clsReturn.PromoDT = rs.getTimestamp("PromoDT");
						clsReturn.PutDT = rs.getTimestamp("PromoDT");
						if (clsReturn.PromoDT == null) {
							if (clsReturn.PutDT != null)
								clsReturn.PromoDT = clsReturn.PutDT;
						} else if (clsReturn.PutDT == null) {
							clsReturn.PutDT = clsReturn.PromoDT;
						}
						clsReturn.OrderDT = clsReturn.PromoDT;
						clsReturn.GoodsOptionCD_Origin = GF.getString(rs, "GoodsOptionCD");
						clsReturn.GoodsOptionNM_Origin = GF.getString(rs, "GoodsOptionNM");
						clsReturn.GoodsCD = rs.getLong("GoodsCD");
						clsReturn.GoodsOptionCD = rs.getLong("GoodsOptionCD");
						clsReturn.GoodsOptionNM = GF.getString(rs, "GoodsOptionNM");
						strTemp = GF.getString(rs, "Quantity");
						strTemp = strTemp.equals("") ? "0" : strTemp;
						clsReturn.Quantity = Integer.parseInt(strTemp);
						clsReturn.UnitPrice = rs.getBigDecimal("UnitPrice");
						clsReturn.WeekRemark = GF.getString(rs, "WeekRemark");
						clsReturn.OrderKind = GF.getString(rs, "OrderKind");
						clsReturn.OrderKindCD = rs.getInt("OrderKindCD");
						strTemp = GF.getString(rs, "AgencyHob");
						strTemp = strTemp.equals("") ? "0" : strTemp;
						clsReturn.AgencyHob = new BigDecimal(strTemp);
						strTemp = GF.getString(rs, "HQHob");
						strTemp = strTemp.equals("") ? "0" : strTemp;
						clsReturn.HQHob = GF.toBigDecimal(strTemp);
						strTemp = GF.getString(rs, "WeekQty");
						strTemp = strTemp.equals("") ? "0" : strTemp;
						clsReturn.WeekQty = Integer.parseInt(strTemp);
						clsReturn.PromoGiftNM = GF.getString(rs, "PromoGiftNM");
						strTemp = GF.getString(rs, "ContractPeriod");
						strTemp = strTemp.equals("") ? "0" : strTemp;
						clsReturn.ContractPeriod = Integer.parseInt(strTemp);
						clsReturn.StopDT = rs.getTimestamp("StopDT");
						clsReturn.StopReason = GF.getString(rs, "StopReason");
						clsReturn.PromoTeamNM = GF.getString(rs, "PromoTeamNM");
						clsReturn.PromoTeamCD = rs.getLong("PromoTeamCD");
						strTemp = GF.getString(rs, "ActualHob");
						strTemp = strTemp.equals("") ? "0" : strTemp;
						clsReturn.ActualHob = GF.toBigDecimal(strTemp);
						strTemp = GF.getString(rs, "HCHob");
						strTemp = strTemp.equals("") ? "0" : strTemp;
						clsReturn.HCHob = GF.toBigDecimal(strTemp);
						clsReturn.SaveRemark = GF.getString(rs, "SaveRemark");
						clsReturn.TeamPersonNM = GF.getString(rs, "TeamPersonNM");
						clsReturn.TeamPersonCD = rs.getLong("TeamPersonCD");
						clsReturn.TeamNM = GF.getString(rs, "TeamNM");
						clsReturn.TeamCD = rs.getLong("TeamCD");
						arrReturn.add(clsReturn);
					}
					rs.next();
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			arrReturn = new ArrayList<>();
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}
	
	/**
	 * 밀크방(유제품 배송) 주문 데이터를 DB에 저장 (Oracle PreparedStatement 버전)
	 * 중복 체크, 원클릭 체크를 수행하고 주문 마스터와 상품 상세를 저장
	 * 
	 * @param p_Param 저장할 주문 정보 (주문자, 상품 목록 등 포함)
	 * @return boolean 저장 성공 여부
	 */
	public boolean insertMilkbang(MilkbangBean p_Param) {
	    String strError = "(MilkbangManager) insertMilkbang 에러 : ";
	    PreparedStatement pstmt = null;
	    Statement stmt = null;
	    boolean blnReturn = false;
	    boolean SaveYN = false;
	    
	    try {
	        // ======== 1. 트랜잭션 시작 ========
	    	this.FV_conDB.setAutoCommit(false);
	        
	        // ======== 2. 기존 주문 존재 여부 확인 ========
	        MilkbangBean clsParam = new MilkbangBean();
	        clsParam.OrderDT = p_Param.OrderDT;
	        clsParam.AgencyCD = p_Param.AgencyCD;
	        clsParam.OrderUserNM = p_Param.OrderUserNM;
	        clsParam.OrderAddress1 = p_Param.OrderAddress1;
	        
	        ArrayList<MilkbangBean> arrOrder = findMilkbang(clsParam);
	        
	        // ======== 3. 기존 주문이 1건 존재하는 경우 ========
	        if (arrOrder.size() == 1) {
	            MilkbangBean clsOrder = arrOrder.get(0);
	            
	            for (int i = 0; i < clsOrder.arrMilkbangGoods.size(); i++) {
	                MilkbangBean clsMilkbangGoods = clsOrder.arrMilkbangGoods.get(i);
	                if (clsMilkbangGoods.SaveYN == 1) {
	                    SaveYN = true;
	                    break;
	                }
	            }
	            
	            if (!SaveYN) {
	                String sql = "DELETE FROM ysc.tc_milkbang WHERE OrderCD = ?";
	                pstmt = this.FV_conDB.prepareStatement(sql);
	                pstmt.setLong(1, clsOrder.OrderCD);
	                pstmt.executeUpdate();
	                pstmt.close();
	            }
	            
	        } else {
	            // ======== 4. 신규 주문인 경우 중복/원클릭 체크 ========
	            clsParam = new MilkbangBean();
	            clsParam.AgencyCD = p_Param.AgencyCD;
	            clsParam.OrderUserNM = p_Param.OrderUserNM;
	            clsParam.OrderAddress1 = p_Param.OrderAddress1;
	            clsParam.OrderCellPhone = p_Param.OrderCellPhone;
	            clsParam.SearchFromDT = GF.addTime_Year(p_Param.OrderDT.toString(), -1);
	            
	            ArrayList<MilkbangBean> arrUploaded = findDuplicate(clsParam);
	            if (arrUploaded.size() > 0) {
	                MilkbangBean clsUploaded = arrUploaded.get(0);
	                p_Param.DuplicateYN = 1;
	                p_Param.DuplOrderCD = clsUploaded.OrderCD;
	            }
	            
	            arrUploaded = findOneclick(clsParam);
	            if (arrUploaded.size() > 0) {
	                MilkbangBean clsUploaded = arrUploaded.get(0);
	                p_Param.OneclickYN = 1;
	                p_Param.OneclickOrderCD = clsUploaded.OrderCD;
	            }
	        }
	        
	        // ======== 5. 새로운 주문 데이터 저장 ========
	        if (!SaveYN) {
	            clsParam = p_Param;
	            
	            // 5-1. 신규 주문번호 생성
	            clsParam.OrderCD = makeMilkbangCD(GF.left(clsParam.OrderDT.toString(), 10));
	            
	            // 5-2. 주문 마스터 데이터 저장
	            String sql = "INSERT INTO ysc.tc_milkbang (" +
	                    "OrderCD, OrderDT, OrderType, OrderUserCD, OrderUserNM, " +
	                    "OrderHomePhone, OrderCellPhone, OrderEmail, OrderZipCD, " +
	                    "OrderAddress1, OrderAddress2, OrderRemark, " +
	                    "ReceiveUserNM, ReceiveHomePhone, ReceiveCellPhone, ReceiveEmail, " +
	                    "ReceiveZipCD, ReceiveAddress1, ReceiveAddress2, StaffRemark, " +
	                    "TotalOrderPrice, TotalPayPrice, PG_Type, PG_PayYN, PG_PayType, PG_PayDT, " +
	                    "AgencyCD, AgencyNM, AgencyTel, AgencyDeliveryYN, DeleteYN, " +
	                    "MilkbangFileNM, PromoPersonNM, PromoPersonNM_Origin, PostArea, AddressType, " +
	                    "ForceAddYN, DuplicateYN, DuplOrderCD, OneclickYN, OneclickOrderCD" +
	                    ") VALUES( " +
	                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
	                    "?, ?, ?, ?, ?, ?, ?, ?, " +
	                    "?, ?, ?, ?, ?, ?, " +
	                    "?, ?, ?, ?, ?, " +
	                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
	            
	            pstmt = this.FV_conDB.prepareStatement(sql);
	            
	            int idx = 1;
	            pstmt.setLong(idx++, clsParam.OrderCD);
	            pstmt.setTimestamp(idx++, clsParam.OrderDT);  // Timestamp 직접 사용
	            pstmt.setInt(idx++, clsParam.OrderType);
	            pstmt.setLong(idx++, clsParam.OrderUserCD);
	            pstmt.setString(idx++, clsParam.OrderUserNM);
	            pstmt.setString(idx++, clsParam.OrderHomePhone);
	            pstmt.setString(idx++, clsParam.OrderCellPhone);
	            pstmt.setString(idx++, clsParam.OrderEmail);
	            pstmt.setString(idx++, clsParam.OrderZipCD != null ? 
	                          clsParam.OrderZipCD.replaceAll("-", "") : "");
	            pstmt.setString(idx++, clsParam.OrderAddress1);
	            pstmt.setString(idx++, clsParam.OrderAddress2);
	            pstmt.setString(idx++, clsParam.OrderRemark);
	            pstmt.setString(idx++, clsParam.ReceiveUserNM);
	            pstmt.setString(idx++, clsParam.ReceiveHomePhone);
	            pstmt.setString(idx++, clsParam.ReceiveCellPhone);
	            pstmt.setString(idx++, clsParam.ReceiveEmail);
	            pstmt.setString(idx++, clsParam.ReceiveZipCD != null ? 
	                          clsParam.ReceiveZipCD.replaceAll("-", "") : "");
	            pstmt.setString(idx++, clsParam.ReceiveAddress1);
	            pstmt.setString(idx++, clsParam.ReceiveAddress2);
	            pstmt.setString(idx++, clsParam.StaffRemark);
	            pstmt.setBigDecimal(idx++, clsParam.TotalOrderPrice);
	            pstmt.setBigDecimal(idx++, clsParam.TotalPayPrice);
	            pstmt.setInt(idx++, clsParam.PG_Type);
	            pstmt.setInt(idx++, clsParam.PG_PayYN);
	            pstmt.setInt(idx++, clsParam.PG_PayType);
	            pstmt.setTimestamp(idx++, clsParam.PG_PayDT);  // null도 자동 처리
	            pstmt.setLong(idx++, clsParam.AgencyCD);
	            pstmt.setString(idx++, clsParam.AgencyNM); //AgencyNM
	            pstmt.setString(idx++, clsParam.AgencyTel); //AgencyTel
	            pstmt.setInt(idx++, clsParam.AgencyDeliveryYN);
	            pstmt.setInt(idx++, clsParam.DeleteYN);
	            pstmt.setString(idx++, clsParam.FileNM);
	            pstmt.setString(idx++, clsParam.PromoPersonNM);
	            pstmt.setString(idx++, clsParam.PromoPersonNM_Origin);
	            pstmt.setString(idx++, clsParam.PostArea);
	            pstmt.setString(idx++, clsParam.AddressType);
	            pstmt.setInt(idx++, clsParam.ForceAddYN);
	            pstmt.setInt(idx++, clsParam.DuplicateYN);
	            pstmt.setLong(idx++, clsParam.DuplOrderCD);
	            pstmt.setInt(idx++, clsParam.OneclickYN);
	            pstmt.setLong(idx++, clsParam.OneclickOrderCD);
	            
	            pstmt.executeUpdate();
	            pstmt.close();
	            
	            // 5-3. 주문 상품 상세 데이터 저장
//	            sql = "INSERT INTO ysc.tc_milkbanggoods (" +
//	                    "OrderCD, OrderSEQ, GoodsCD, GoodsOptionCD, GoodsOptionNM, " +
//	                    "Quantity, ContractPeriod, WeekQty, WeekRemark, " +
//	                    "UnitPrice, OrderPrice, FactoryPrice, DeliveryFee, " +
//	                    "OrderStatus, DeleteYN, " +
//	                    "PromoDT, PutDT, ExpireDT, " +
//	                    "GoodsOptionCD_Origin, GoodsOptionNM_Origin, " +
//	                    "OrderKindCD, OrderKind, " +
//	                    "AgencyHob, HQHob, PromoGiftNM, " +
//	                    "GiveDT, GivePersonNM, StopDT, StopReason, SaveYN, " +
//	                    "TeamPersonCD, TeamPersonNM, TeamCD, TeamNM" +
//	                    ") VALUES( " +
//	                    "?, " +
//	                    "(SELECT NVL(MAX(OrderSEQ),0) + 1 FROM ysc.tc_milkbanggoods WHERE OrderCD = ?), " +
//	                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
//	                    "?, ?, " +
//	                    "ADD_MONTHS(?, ?), " +  // ExpireDT = PromoDT + ContractPeriod
//	                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
//	                    "0, '사원명', " +
//	                    "0, '부서명' )";
	            
	            sql = "INSERT INTO ysc.tc_milkbanggoods (" +
	                    "OrderCD, OrderSEQ, GoodsCD, GoodsOptionCD, GoodsOptionNM, " +
	                    "Quantity, ContractPeriod, WeekQty, WeekRemark, " +
	                    "UnitPrice, OrderPrice, FactoryPrice, DeliveryFee, " +
	                    "OrderStatus, DeleteYN, " +
	                    "PromoDT, PutDT, ExpireDT, " +
	                    "GoodsOptionCD_Origin, GoodsOptionNM_Origin, " +
	                    "OrderKindCD, OrderKind, " +
	                    "AgencyHob, HQHob, PromoGiftNM, " +
	                    "GiveDT, GivePersonNM, StopDT, StopReason, SaveYN, " +
	                    "TeamPersonCD, TeamPersonNM, TeamCD, TeamNM, ActualHob, PromoTeamCd" +
	                    ") SELECT " +
	                    "?, " +
	                    "(SELECT NVL(MAX(OrderSEQ),0) + 1 FROM ysc.tc_milkbanggoods WHERE OrderCD = ?), " +
	                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
	                    "?, ?, " +
	                    "ADD_MONTHS(?, ?), " +  // ExpireDT = PromoDT + ContractPeriod
	                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
	                    "tc_teamperson.TeamPersonCD, tc_teamperson.TeamPersonNM, " +
	                    "tc_team.TeamCD, tc_team.TeamNM, ?, (SELECT nvl(max(b.PROMOTEAMCD), -1) FROM ysc.tc_milkbang a INNER JOIN ysc.TC_MILKBANGGOODS b ON a.ORDERCD = b.ORDERCD WHERE a.PROMOPERSONNM = ? AND a.ORDERDT >= a.ORDERDT - 100) " +
	                    "FROM ysc.tc_agency " +
	                    "LEFT OUTER JOIN ysc.tc_teamperson ON (tc_agency.TeamPersonCD = tc_teamperson.TeamPersonCD) " +
	                    "LEFT OUTER JOIN ysc.tc_team ON (tc_team.TeamCD = tc_teamperson.TeamCD) " +
	                    "WHERE tc_agency.AgencyCD = ?";
	            
	            pstmt = this.FV_conDB.prepareStatement(sql);
	            
	            for (int j = 0; j < clsParam.arrMilkbangGoods.size(); j++) {
	                MilkbangBean clsMilkbangGoods = clsParam.arrMilkbangGoods.get(j);
	                
	                idx = 1;
	                pstmt.setLong(idx++, clsParam.OrderCD);
	                pstmt.setLong(idx++, clsParam.OrderCD);  // for OrderSEQ subquery
	                pstmt.setLong(idx++, clsMilkbangGoods.GoodsCD);
	                pstmt.setLong(idx++, clsMilkbangGoods.GoodsOptionCD);
	                pstmt.setString(idx++, clsMilkbangGoods.GoodsOptionNM);
	                pstmt.setInt(idx++, clsMilkbangGoods.Quantity);
	                pstmt.setInt(idx++, clsMilkbangGoods.ContractPeriod);
	                pstmt.setInt(idx++, clsMilkbangGoods.WeekQty);
	                pstmt.setString(idx++, clsMilkbangGoods.WeekRemark);
	                pstmt.setBigDecimal(idx++, clsMilkbangGoods.UnitPrice);
	                pstmt.setBigDecimal(idx++, clsMilkbangGoods.OrderPrice);
	                pstmt.setBigDecimal(idx++, new BigDecimal("0"));  // FactoryPrice
	                pstmt.setBigDecimal(idx++, new BigDecimal("0"));  // DeliveryFee
	                pstmt.setInt(idx++, clsMilkbangGoods.OrderStatus);
	                pstmt.setInt(idx++, clsMilkbangGoods.DeleteYN);
	                pstmt.setTimestamp(idx++, clsMilkbangGoods.PromoDT);
	                pstmt.setTimestamp(idx++, clsMilkbangGoods.PutDT);
	                // ExpireDT 계산용
	                pstmt.setTimestamp(idx++, clsMilkbangGoods.PromoDT);
	                pstmt.setInt(idx++, clsMilkbangGoods.ContractPeriod);
	                pstmt.setString(idx++, clsMilkbangGoods.GoodsOptionCD_Origin);
	                pstmt.setString(idx++, clsMilkbangGoods.GoodsOptionNM_Origin);
	                pstmt.setInt(idx++, clsMilkbangGoods.OrderKindCD);
	                pstmt.setString(idx++, clsMilkbangGoods.OrderKind);
	                pstmt.setBigDecimal(idx++, clsMilkbangGoods.AgencyHob);
	                pstmt.setBigDecimal(idx++, clsMilkbangGoods.HQHob);
	                pstmt.setString(idx++, clsMilkbangGoods.PromoGiftNM);
	                pstmt.setTimestamp(idx++, clsMilkbangGoods.GiveDT);
	                pstmt.setString(idx++, clsMilkbangGoods.GivePersonNM);
	                pstmt.setTimestamp(idx++, clsMilkbangGoods.StopDT);
	                pstmt.setString(idx++, clsMilkbangGoods.StopReason);
	                pstmt.setInt(idx++, clsMilkbangGoods.SaveYN);
	                pstmt.setBigDecimal(idx++, clsMilkbangGoods.ActualHob);
	                pstmt.setString(idx++, clsParam.PromoPersonNM);
	                pstmt.setLong(idx++, clsMilkbangGoods.AgencyCD);
	                
	                pstmt.executeUpdate();
	            }
	            pstmt.close();
	            
	            // 5-4. 파일 상태 업데이트
	            sql = "UPDATE ysc.tc_milkbang_file " +
	                    "SET UploadYN = 1, " +
	                    "    UploadDT = SYSDATE, " +
	                    "    AgencyCD = ?, " +
	                    "    FileStatus = 1 " +
	                    "WHERE FileNM = ?";
	            
	            pstmt = this.FV_conDB.prepareStatement(sql);
	            pstmt.setLong(1, clsParam.AgencyCD);
	            pstmt.setString(2, clsParam.FileNM);
	            pstmt.executeUpdate();
	            pstmt.close();
	            
	            // ======== 6. 트랜잭션 커밋 ========
	            //this.FV_conDB.commit();
	            
	        } else {
	            updateFileStatus(p_Param.FileNM, 1);
	        }
	        
	        // ======== 6. 트랜잭션 커밋 ========
            this.FV_conDB.commit();
	        
	        blnReturn = true;
	        
	    } catch (SQLException e) {
	        blnReturn = false;
	        System.out.println(strError + e.getMessage());
	        
	        try {
	        	this.FV_conDB.rollback();
	            
	        } catch (SQLException ex) {
	            System.out.println(strError + ex.getMessage());
	        }
	    } finally {
	        DBManager.stmtClose(pstmt);
	        DBManager.stmtClose(stmt);
	    }
	    
	    return blnReturn;
	}

	public boolean insertMilkbang2(MilkbangBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) insertMilkbang : ";
		Statement stmt = null;
		boolean blnReturn = false;
		boolean SaveYN = false;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			MilkbangBean clsParam = new MilkbangBean();
			clsParam.OrderDT = p_Param.OrderDT;
			clsParam.AgencyCD = p_Param.AgencyCD;
			clsParam.OrderUserNM = p_Param.OrderUserNM;
			clsParam.OrderAddress1 = p_Param.OrderAddress1;
			ArrayList<MilkbangBean> arrOrder = findMilkbang(clsParam);
			if (arrOrder.size() == 1) {
				MilkbangBean clsOrder = arrOrder.get(0);
				for (int i = 0; i < clsOrder.arrMilkbangGoods.size(); i++) {
					MilkbangBean clsMilkbangGoods = clsOrder.arrMilkbangGoods.get(i);
					if (clsMilkbangGoods.SaveYN == 1) {
						SaveYN = true;
						break;
					}
				}
				if (!SaveYN) {
					strSQL = " DELETE FROM tc_milkbang WHERE tc_milkbang.OrderCD=" + clsOrder.OrderCD + ";\n";
					stmt.executeUpdate(strSQL);
				}
			} else {
				clsParam = new MilkbangBean();
				clsParam.AgencyCD = p_Param.AgencyCD;
				clsParam.OrderUserNM = p_Param.OrderUserNM;
				clsParam.OrderAddress1 = p_Param.OrderAddress1;
				clsParam.OrderCellPhone = p_Param.OrderCellPhone;
				clsParam.SearchFromDT = GF.addTime_Year(p_Param.OrderDT.toString(), -1);
				ArrayList<MilkbangBean> arrUploaded = findDuplicate(clsParam);
				if (arrUploaded.size() > 0) {
					int i = 0;
					if (i < arrUploaded.size()) {
						MilkbangBean clsUploaded = new MilkbangBean();
						clsUploaded = arrUploaded.get(i);
						p_Param.DuplicateYN = 1;
						p_Param.DuplOrderCD = clsUploaded.OrderCD;
					}
				}
				arrUploaded = findOneclick(clsParam);
				if (arrUploaded.size() > 0) {
					int i = 0;
					if (i < arrUploaded.size()) {
						MilkbangBean clsUploaded = new MilkbangBean();
						clsUploaded = arrUploaded.get(i);
						p_Param.OneclickYN = 1;
						p_Param.OneclickOrderCD = clsUploaded.OrderCD;
					}
				}
			}
			if (!SaveYN) {
				clsParam = p_Param;
				clsParam.OrderCD = makeMilkbangCD(GF.left(clsParam.OrderDT.toString(), 10));
				strSQL = " INSERT INTO tc_milkbang (OrderCD, OrderDT, OrderType, OrderUserCD, OrderUserNM, OrderHomePhone, OrderCellPhone, OrderEmail, OrderZipCD, OrderAddress1, OrderAddress2, OrderRemark, ReceiveUserNM, ReceiveHomePhone, ReceiveCellPhone, ReceiveEmail, ReceiveZipCD, ReceiveAddress1, ReceiveAddress2, StaffRemark, TotalOrderPrice, TotalPayPrice, PG_Type, PG_PayYN, PG_PayType, PG_PayDT, AgencyCD, AgencyNM, AgencyTel, AgencyDeliveryYN, DeleteYN, MilkbangFileNM, PromoPersonNM, PromoPersonNM_Origin, PostArea, AddressType, ForceAddYN, DuplicateYN, DuplOrderCD, OneclickYN, OneclickOrderCD) SELECT "
						+ clsParam.OrderCD + ", '" + clsParam.OrderDT + "', " + clsParam.OrderType + ", "
						+ clsParam.OrderUserCD + ", '" + GF.recoverSQL(clsParam.OrderUserNM) + "', '"
						+ GF.recoverSQL(clsParam.OrderHomePhone) + "', '" + GF.recoverSQL(clsParam.OrderCellPhone)
						+ "', '" + GF.recoverSQL(clsParam.OrderEmail) + "', '" + clsParam.OrderZipCD.replaceAll("-", "")
						+ "', '" + GF.recoverSQL(clsParam.OrderAddress1) + "', '"
						+ GF.recoverSQL(clsParam.OrderAddress2) + "', '" + GF.recoverSQL(clsParam.OrderRemark) + "', '"
						+ GF.recoverSQL(clsParam.ReceiveUserNM) + "', '" + GF.recoverSQL(clsParam.ReceiveHomePhone)
						+ "', '" + GF.recoverSQL(clsParam.ReceiveCellPhone) + "', '"
						+ GF.recoverSQL(clsParam.ReceiveEmail) + "', '" + clsParam.ReceiveZipCD.replaceAll("-", "")
						+ "', '" + GF.recoverSQL(clsParam.ReceiveAddress1) + "', '"
						+ GF.recoverSQL(clsParam.ReceiveAddress2) + "', '" + GF.recoverSQL(clsParam.StaffRemark) + "', "
						+ clsParam.TotalOrderPrice + ", " + clsParam.TotalPayPrice + ", " + clsParam.PG_Type + ", "
						+ clsParam.PG_PayYN + ", " + clsParam.PG_PayType + ", "
						+ ((clsParam.PG_PayDT == null) ? "null" : ("'" + clsParam.PG_PayDT + "'")) + ", "
						+ clsParam.AgencyCD + ", tc_agency.AgencyNM, tc_agency.AgencyTel, " + clsParam.AgencyDeliveryYN
						+ ", " + clsParam.DeleteYN + ", '" + GF.recoverSQL(clsParam.FileNM) + "', '"
						+ GF.recoverSQL(clsParam.PromoPersonNM) + "', '" + GF.recoverSQL(clsParam.PromoPersonNM_Origin)
						+ "', '" + GF.recoverSQL(clsParam.PostArea) + "', '" + GF.recoverSQL(clsParam.AddressType)
						+ "', " + clsParam.ForceAddYN + ", " + clsParam.DuplicateYN + ", " + clsParam.DuplOrderCD + ", "
						+ clsParam.OneclickYN + ", " + clsParam.OneclickOrderCD
						+ " FROM tc_agency WHERE (tc_agency.AgencyCD = " + clsParam.AgencyCD + ");\n";
				stmt.executeUpdate(strSQL);
				for (int j = 0; j < clsParam.arrMilkbangGoods.size(); j++) {
					MilkbangBean clsMilkbangGoods = clsParam.arrMilkbangGoods.get(j);
					strSQL = " INSERT INTO tc_milkbanggoods (OrderCD, OrderSEQ, GoodsCD, GoodsOptionCD, GoodsOptionNM, Quantity, ContractPeriod, WeekQty, WeekRemark, UnitPrice, OrderPrice, FactoryPrice, DeliveryFee, OrderStatus, DeleteYN, PromoDT, PutDT, ExpireDT, GoodsOptionCD_Origin, GoodsOptionNM_Origin, OrderKindCD, OrderKind, AgencyHob, HQHob, PromoGiftNM, GiveDT, GivePersonNM, StopDT, StopReason, SaveYN, TeamPersonCD, TeamPersonNM, TeamCD, TeamNM) SELECT "
							+ clsParam.OrderCD
							+ ", (SELECT (IFNULL(MAX(OrderSEQ),0) + 1) FROM tc_milkbanggoods WHERE OrderCD="
							+ clsParam.OrderCD + "), " + clsMilkbangGoods.GoodsCD + ", "
							+ clsMilkbangGoods.GoodsOptionCD + ", '" + GF.recoverSQL(clsMilkbangGoods.GoodsOptionNM)
							+ "', " + clsMilkbangGoods.Quantity + ", " + clsMilkbangGoods.ContractPeriod + ", "
							+ clsMilkbangGoods.WeekQty + ", '" + GF.recoverSQL(clsMilkbangGoods.WeekRemark) + "', "
							+ clsMilkbangGoods.UnitPrice + ", " + clsMilkbangGoods.OrderPrice + ", 0, 0, "
							+ clsMilkbangGoods.OrderStatus + ", " + clsMilkbangGoods.DeleteYN + ", "
							+ ((clsMilkbangGoods.PromoDT == null) ? "null" : ("'" + clsMilkbangGoods.PromoDT + "'"))
							+ ", " + ((clsMilkbangGoods.PutDT == null) ? "null" : ("'" + clsMilkbangGoods.PutDT + "'"))
							+ ", "
							+ ((clsMilkbangGoods.PromoDT == null) ? "null"
									: ("ADDDATE('" + clsMilkbangGoods.PromoDT + "', INTERVAL "
											+ clsMilkbangGoods.ContractPeriod + " MONTH)"))
							+ ", '" + GF.recoverSQL(clsMilkbangGoods.GoodsOptionCD_Origin) + "', '"
							+ GF.recoverSQL(clsMilkbangGoods.GoodsOptionNM_Origin) + "', "
							+ clsMilkbangGoods.OrderKindCD + ", '" + GF.recoverSQL(clsMilkbangGoods.OrderKind) + "', "
							+ clsMilkbangGoods.AgencyHob + ", " + clsMilkbangGoods.HQHob + ", '"
							+ GF.recoverSQL(clsMilkbangGoods.PromoGiftNM) + "', "
							+ ((clsMilkbangGoods.GiveDT == null) ? "null" : ("'" + clsMilkbangGoods.GiveDT + "'"))
							+ ", '" + GF.recoverSQL(clsMilkbangGoods.GivePersonNM) + "', "
							+ ((clsMilkbangGoods.StopDT == null) ? "null" : ("'" + clsMilkbangGoods.StopDT + "'"))
							+ ", '" + GF.recoverSQL(clsMilkbangGoods.StopReason) + "', " + clsMilkbangGoods.SaveYN
							+ ", tc_teamperson.TeamPersonCD, tc_teamperson.TeamPersonNM, tc_team.TeamCD, tc_team.TeamNM FROM tc_agency LEFT OUTER JOIN tc_teamperson ON (tc_agency.TeamPersonCD = tc_teamperson.TeamPersonCD) LEFT OUTER JOIN tc_team ON (tc_team.TeamCD = tc_teamperson.TeamCD) WHERE (tc_agency.AgencyCD = "
							+ clsParam.AgencyCD + ");\n";
					stmt.executeUpdate(strSQL);
				}
				strSQL = " UPDATE tc_milkbang_file    SET UploadYN = 1,       UploadDT = now(),       AgencyCD = "
						+ clsParam.AgencyCD + ",     FileStatus = " + '\001' + "  WHERE FileNM = "
						+ GF.sqlString(clsParam.FileNM) + ";\n";
				stmt.executeUpdate(strSQL);
				this.FV_conDB.commit();
			} else {
				updateFileStatus(p_Param.FileNM, 1);
			}
			blnReturn = true;
		} catch (SQLException e) {
			blnReturn = false;
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public boolean updateMilkbang(MilkbangBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) updateMilkbang : ";
		Statement stmt = null;
		boolean blnReturn = false;
		boolean SaveYN = false;
		try {
			MilkbangBean clsParam = new MilkbangBean();
			clsParam.OrderDT = p_Param.OrderDT;
			clsParam.AgencyCD = p_Param.AgencyCD;
			clsParam.OrderUserNM = p_Param.OrderUserNM;
			clsParam.OrderAddress1 = p_Param.OrderAddress1;
			ArrayList<MilkbangBean> arrOrder = findMilkbang(clsParam);
			if (arrOrder.size() == 1) {
				MilkbangBean clsOrder = arrOrder.get(0);
				strSQL = strSQL
						+ " UPDATE tc_milkbang SET   tc_milkbang.SaveYN = 1, tc_milkbang.UpdateDT = now() WHERE tc_milkbang.OrderCD = "
						+ clsOrder.OrderCD + ";";
				for (int i = 0; i < clsOrder.arrMilkbangGoods.size(); i++) {
					MilkbangBean clsMilkbangGoods = clsOrder.arrMilkbangGoods.get(i);
					MilkbangBean clsMilkbangGoods_param = new MilkbangBean();
					clsMilkbangGoods_param.OrderCD = clsMilkbangGoods.OrderCD;
					clsMilkbangGoods_param.GoodsOptionNM = clsMilkbangGoods.GoodsOptionNM;
					ArrayList<MilkbangBean> arrMilkbangGoods = findMilkbangGoods(clsMilkbangGoods_param);
					if (arrMilkbangGoods.size() > 0) {
						clsMilkbangGoods_param = arrMilkbangGoods.get(arrMilkbangGoods.size() - 1);
						strSQL = strSQL + " UPDATE tc_milkbanggoods SET   tc_milkbanggoods.OrderKindCD = "
								+ clsMilkbangGoods.OrderKindCD + ", tc_milkbanggoods.PromoTeamNM = "
								+ GF.sqlString(clsMilkbangGoods.PromoTeamNM) + ", tc_milkbanggoods.PromoTeamCD = "
								+ clsMilkbangGoods.PromoTeamCD + ", tc_milkbanggoods.ActualHob = "
								+ clsMilkbangGoods.ActualHob + ", tc_milkbanggoods.HCHob = " + clsMilkbangGoods.HCHob
								+ ", tc_milkbanggoods.SaveRemark = " + GF.sqlString(clsMilkbangGoods.SaveRemark)
								+ ", tc_milkbanggoods.TeamPersonNM = " + GF.sqlString(clsMilkbangGoods.TeamPersonNM)
								+ ", tc_milkbanggoods.TeamPersonCD = " + clsMilkbangGoods.TeamPersonCD
								+ ", tc_milkbanggoods.TeamNM = " + GF.sqlString(clsMilkbangGoods.TeamNM)
								+ ", tc_milkbanggoods.TeamCD = " + clsMilkbangGoods.TeamCD
								+ ", tc_milkbanggoods.SaveYN = 1, tc_milkbanggoods.SaveDT = now(), tc_milkbanggoods.MasterCloseYN = 1, tc_milkbanggoods.MasterCloseDT = now(), tc_milkbanggoods.UpdateDT = now() WHERE tc_milkbanggoods.OrderCD = "
								+ clsMilkbangGoods_param.OrderCD + " AND tc_milkbanggoods.OrderSEQ = "
								+ clsMilkbangGoods_param.OrderSEQ + ";\n";
					} else {
						strSQL = strSQL
								+ " INSERT INTO tc_milkbanggoods (OrderCD, OrderSEQ,GoodsCD,GoodsOptionCD, GoodsOptionNM,Quantity,ContractPeriod,WeekQty,WeekRemark,UnitPrice,OrderPrice,FactoryPrice,DeliveryFee,OrderStatus,DeleteYN,PromoDT,PutDT,ExpireDT,GoodsOptionCD_Origin,GoodsOptionNM_Origin,OrderKindCD,OrderKind,AgencyHob,HQHob,PromoGiftNM,GiveDT,GivePersonNM,StopDT,StopReason,PromoTeamNM,PromoTeamCD,ActualHob,HCHob,SaveRemark,TeamPersonCD,TeamPersonNM,TeamCD,TeamNM,SaveYN,SaveDT,MasterCloseYN,MasterCloseDT) SELECT "
								+ clsParam.OrderCD
								+ ", (SELECT (IFNULL(MAX(OrderSEQ),0) + 1) FROM tc_milkbanggoods WHERE tc_milkbanggoods.OrderCD="
								+ clsParam.OrderCD + ")," + clsMilkbangGoods.GoodsCD + ","
								+ clsMilkbangGoods.GoodsOptionCD + "," + GF.sqlString(clsMilkbangGoods.GoodsOptionNM)
								+ "," + clsMilkbangGoods.Quantity + "," + clsMilkbangGoods.ContractPeriod + ","
								+ clsMilkbangGoods.WeekQty + "," + GF.sqlString(clsMilkbangGoods.WeekRemark) + ","
								+ clsMilkbangGoods.UnitPrice + "," + clsMilkbangGoods.OrderPrice + ", 0, 0,"
								+ clsMilkbangGoods.OrderStatus + "," + clsMilkbangGoods.DeleteYN + ","
								+ ((clsMilkbangGoods.PromoDT == null) ? "null" : ("'" + clsMilkbangGoods.PromoDT + "'"))
								+ ","
								+ ((clsMilkbangGoods.PutDT == null) ? "null" : ("'" + clsMilkbangGoods.PutDT + "'"))
								+ ","
								+ ((clsMilkbangGoods.PromoDT == null) ? "null"
										: ("ADDDATE('" + clsMilkbangGoods.PromoDT + "', INTERVAL "
												+ clsMilkbangGoods.ContractPeriod + " MONTH)"))
								+ "," + GF.sqlString(clsMilkbangGoods.GoodsOptionCD_Origin) + ","
								+ GF.sqlString(clsMilkbangGoods.GoodsOptionNM_Origin) + ","
								+ clsMilkbangGoods.OrderKindCD + "," + GF.sqlString(clsMilkbangGoods.OrderKind) + ","
								+ clsMilkbangGoods.AgencyHob + "," + clsMilkbangGoods.HQHob + ","
								+ GF.sqlString(clsMilkbangGoods.PromoGiftNM) + ","
								+ ((clsMilkbangGoods.GiveDT == null) ? "null" : ("'" + clsMilkbangGoods.GiveDT + "'"))
								+ "," + GF.sqlString(clsMilkbangGoods.GivePersonNM) + ","
								+ ((clsMilkbangGoods.StopDT == null) ? "null" : ("'" + clsMilkbangGoods.StopDT + "'"))
								+ "," + GF.sqlString(clsMilkbangGoods.StopReason) + ","
								+ GF.sqlString(clsMilkbangGoods.PromoTeamNM) + "," + clsMilkbangGoods.PromoTeamCD + ","
								+ clsMilkbangGoods.ActualHob + "," + clsMilkbangGoods.HCHob + ","
								+ GF.sqlString(clsMilkbangGoods.SaveRemark) + "," + clsMilkbangGoods.TeamPersonCD + ","
								+ GF.sqlString(clsMilkbangGoods.TeamPersonNM) + "," + clsMilkbangGoods.TeamCD + ","
								+ GF.sqlString(clsMilkbangGoods.TeamNM) + ", 1, now(), 1, now();\n";
					}
				}
			} else {
				clsParam = p_Param;
				clsParam.OrderCD = makeMilkbangCD(GF.left(clsParam.OrderDT.toString(), 10));
				strSQL = strSQL
						+ " INSERT INTO tc_milkbang (OrderCD,OrderDT,OrderType,OrderUserCD, OrderUserNM,OrderHomePhone, OrderCellPhone,OrderEmail,OrderZipCD, OrderAddress1, OrderAddress2,OrderRemark,ReceiveUserNM,ReceiveHomePhone, ReceiveCellPhone,ReceiveEmail,ReceiveZipCD, ReceiveAddress1, ReceiveAddress2,StaffRemark,TotalOrderPrice,TotalPayPrice,PG_Type,PG_PayYN,PG_PayType,PG_PayDT,AgencyCD,AgencyNM,AgencyTel,AgencyDeliveryYN,DeleteYN,MilkbangFileNM,PromoPersonNM,PromoPersonNM_Origin,PostArea,AddressType,ForceAddYN,SaveYN) SELECT "
						+ clsParam.OrderCD + ", '" + clsParam.OrderDT + "'," + clsParam.OrderType + ","
						+ clsParam.OrderUserCD + ",'" + GF.recoverSQL(clsParam.OrderUserNM) + "','"
						+ GF.recoverSQL(clsParam.OrderHomePhone) + "','" + GF.recoverSQL(clsParam.OrderCellPhone)
						+ "','" + GF.recoverSQL(clsParam.OrderEmail) + "','" + clsParam.OrderZipCD + "','"
						+ GF.recoverSQL(clsParam.OrderAddress1) + "','" + GF.recoverSQL(clsParam.OrderAddress2) + "','"
						+ GF.recoverSQL(clsParam.OrderRemark) + "','" + GF.recoverSQL(clsParam.ReceiveUserNM) + "','"
						+ GF.recoverSQL(clsParam.ReceiveHomePhone) + "','" + GF.recoverSQL(clsParam.ReceiveCellPhone)
						+ "','" + GF.recoverSQL(clsParam.ReceiveEmail) + "','" + clsParam.ReceiveZipCD + "','"
						+ GF.recoverSQL(clsParam.ReceiveAddress1) + "','" + GF.recoverSQL(clsParam.ReceiveAddress2)
						+ "','" + GF.recoverSQL(clsParam.StaffRemark) + "'," + clsParam.TotalOrderPrice + ","
						+ clsParam.TotalPayPrice + "," + clsParam.PG_Type + "," + clsParam.PG_PayYN + ","
						+ clsParam.PG_PayType + ","
						+ ((clsParam.PG_PayDT == null) ? "null" : ("'" + clsParam.PG_PayDT + "'")) + ","
						+ clsParam.AgencyCD + ",tc_agency.AgencyNM,tc_agency.AgencyTel," + clsParam.AgencyDeliveryYN
						+ "," + clsParam.DeleteYN + ", '" + GF.recoverSQL(clsParam.FileNM) + "', '"
						+ GF.recoverSQL(clsParam.PromoPersonNM) + "', '" + GF.recoverSQL(clsParam.PromoPersonNM_Origin)
						+ "', '" + GF.recoverSQL(clsParam.PostArea) + "', '" + GF.recoverSQL(clsParam.AddressType)
						+ "'," + clsParam.ForceAddYN + ",1 FROM tc_agency WHERE (tc_agency.AgencyCD = "
						+ clsParam.AgencyCD + ");\n";
				for (int j = 0; j < clsParam.arrMilkbangGoods.size(); j++) {
					MilkbangBean clsMilkbangGoods = clsParam.arrMilkbangGoods.get(j);
					strSQL = strSQL
							+ " INSERT INTO tc_milkbanggoods (OrderCD, OrderSEQ,GoodsCD,GoodsOptionCD, GoodsOptionNM,Quantity,ContractPeriod,WeekQty,WeekRemark,UnitPrice,OrderPrice,FactoryPrice,DeliveryFee,OrderStatus,DeleteYN,PromoDT,PutDT,ExpireDT,GoodsOptionCD_Origin,GoodsOptionNM_Origin,OrderKindCD,OrderKind,AgencyHob,HQHob,PromoGiftNM,GiveDT,GivePersonNM,StopDT,StopReason,PromoTeamNM,PromoTeamCD,ActualHob,HCHob,SaveRemark,TeamPersonCD,TeamPersonNM,TeamCD,TeamNM,SaveYN,SaveDT,MasterCloseYN,MasterCloseDT) SELECT "
							+ clsParam.OrderCD
							+ ", (SELECT (IFNULL(MAX(OrderSEQ),0) + 1) FROM tc_milkbanggoods WHERE tc_milkbanggoods.OrderCD="
							+ clsParam.OrderCD + ")," + clsMilkbangGoods.GoodsCD + "," + clsMilkbangGoods.GoodsOptionCD
							+ "," + GF.sqlString(clsMilkbangGoods.GoodsOptionNM) + "," + clsMilkbangGoods.Quantity + ","
							+ clsMilkbangGoods.ContractPeriod + "," + clsMilkbangGoods.WeekQty + ","
							+ GF.sqlString(clsMilkbangGoods.WeekRemark) + "," + clsMilkbangGoods.UnitPrice + ","
							+ clsMilkbangGoods.OrderPrice + ", 0, 0," + clsMilkbangGoods.OrderStatus + ","
							+ clsMilkbangGoods.DeleteYN + ","
							+ ((clsMilkbangGoods.PromoDT == null) ? "null" : ("'" + clsMilkbangGoods.PromoDT + "'"))
							+ "," + ((clsMilkbangGoods.PutDT == null) ? "null" : ("'" + clsMilkbangGoods.PutDT + "'"))
							+ ","
							+ ((clsMilkbangGoods.PromoDT == null) ? "null"
									: ("ADDDATE('" + clsMilkbangGoods.PromoDT + "', INTERVAL "
											+ clsMilkbangGoods.ContractPeriod + " MONTH)"))
							+ "," + GF.sqlString(clsMilkbangGoods.GoodsOptionCD_Origin) + ","
							+ GF.sqlString(clsMilkbangGoods.GoodsOptionNM_Origin) + "," + clsMilkbangGoods.OrderKindCD
							+ "," + GF.sqlString(clsMilkbangGoods.OrderKind) + "," + clsMilkbangGoods.AgencyHob + ","
							+ clsMilkbangGoods.HQHob + "," + GF.sqlString(clsMilkbangGoods.PromoGiftNM) + ","
							+ ((clsMilkbangGoods.GiveDT == null) ? "null" : ("'" + clsMilkbangGoods.GiveDT + "'")) + ","
							+ GF.sqlString(clsMilkbangGoods.GivePersonNM) + ","
							+ ((clsMilkbangGoods.StopDT == null) ? "null" : ("'" + clsMilkbangGoods.StopDT + "'")) + ","
							+ GF.sqlString(clsMilkbangGoods.StopReason) + ","
							+ GF.sqlString(clsMilkbangGoods.PromoTeamNM) + "," + clsMilkbangGoods.PromoTeamCD + ","
							+ clsMilkbangGoods.ActualHob + "," + clsMilkbangGoods.HCHob + ","
							+ GF.sqlString(clsMilkbangGoods.SaveRemark) + "," + clsMilkbangGoods.TeamPersonCD + ","
							+ GF.sqlString(clsMilkbangGoods.TeamPersonNM) + "," + clsMilkbangGoods.TeamCD + ","
							+ GF.sqlString(clsMilkbangGoods.TeamNM) + ", 1, now(), 1, now();\n";
				}
			}
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			this.FV_conDB.commit();
			blnReturn = true;
		} catch (SQLException e) {
			blnReturn = false;
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public boolean insertAgencyMilkbangNew(MilkbangBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) insertAgencyMilkbangNew : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			strSQL = strSQL + " DELETE FROM tc_agencymilkbang WHERE AgencyCD=" + p_Param.AgencyCD
					+ " AND MilkbangFileNM='" + GF.recoverSQL(p_Param.MilkbangFileNM) + "';\n";
			strSQL = strSQL
					+ " INSERT INTO tc_agencymilkbang (AgencyCD,MilkbangFileNM,MilkbangType,UploadDT,UploadYN) SELECT "
					+ p_Param.AgencyCD + ", '" + GF.recoverSQL(p_Param.MilkbangFileNM) + "'," + '\001'
					+ ", now(), 1;\n";
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			this.FV_conDB.commit();
			blnReturn = true;
		} catch (SQLException e) {
			blnReturn = false;
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public ArrayList<MilkbangBean> readExcel_MilkbangStop(String p_FilePath) {
		String strError = "(MilkbangManager) readExcel_MilkbangStop 에러 : ";
		HSSFWorkbook workbook_h = null;
		HSSFSheet sheet_h = null;
		XSSFWorkbook workbook_x = null;
		XSSFSheet sheet_x = null;
		boolean blnExceFilelYN = true;
		boolean blnOldExcelYN = true;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		MilkbangBean clsReturn = new MilkbangBean();
		ArrayList<String> arrListIncreaseNM = new ArrayList<>();
		ArrayList<String> arrListDecreaseNM = new ArrayList<>();
		String strPromoTeamNM = "";
		String strTemp = "", strDate = "", strYear = "", strMonth = "", strDay = "";
		String strBeforeAgencyNM = "";
		boolean blnKindergartenYN = false;
		String strAgencyCD = "";
		String strPromoDT = "";
		String strPutDT = "";
		String strStopDT = "";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ZipCDBean clsZipCD = null;
		try {
			if (GF.right(p_FilePath, 4).toLowerCase().equals(".xls") == true) {
				blnOldExcelYN = true;
			} else if (GF.right(p_FilePath, 5).toLowerCase().equals(".xlsx") == true) {
				blnOldExcelYN = false;
			} else {
				blnExceFilelYN = false;
			}
			if (blnExceFilelYN == true) {
				int intSheetCNT;
				if (blnOldExcelYN == true) {
					workbook_h = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(p_FilePath)));
					intSheetCNT = workbook_h.getNumberOfSheets();
				} else {
					workbook_x = new XSSFWorkbook(OPCPackage.open(new FileInputStream(p_FilePath)));
					intSheetCNT = workbook_x.getNumberOfSheets();
				}
				for (int intSheet = 0; intSheet < intSheetCNT; intSheet++) {
					int intlastRow;
					if (blnOldExcelYN == true) {
						sheet_h = workbook_h.getSheetAt(intSheet);
						intlastRow = sheet_h.getLastRowNum();
					} else {
						sheet_x = workbook_x.getSheetAt(intSheet);
						intlastRow = sheet_x.getLastRowNum();
					}
					if (intSheet != 0 || intlastRow >= 1) {
						int intFirstRow = 1;
						for (int intRow = intFirstRow; intRow <= intlastRow; intRow++) {
							XSSFRow xSSFRow = null;
							if (blnOldExcelYN == true) {
								HSSFRow hSSFRow = sheet_h.getRow(intRow);
							} else {
								xSSFRow = sheet_x.getRow(intRow);
							}
							int intCol = 0;
							clsReturn = new MilkbangBean();
							clsReturn.MilkbangFileNM = p_FilePath.substring(p_FilePath.lastIndexOf("\\") + 1);
							if (xSSFRow.getCell(intCol) != null) {
								strAgencyCD = GF.excelString((Row) xSSFRow, intCol).replaceAll(" ", "");
								strStopDT = GF.excelString((Row) xSSFRow, intCol + 25).replaceAll(" ", "")
										.replaceAll("\\.", "");
								if (!strAgencyCD.equals("") && !strStopDT.equals("")) {
									clsReturn.AgencyCD = Long.parseLong(GF.excelString((Row) xSSFRow, intCol++));
									intCol++;
									intCol++;
									intCol++;
									intCol++;
									clsReturn.OrderAddress1 = GF.excelString((Row) xSSFRow, intCol++).replaceAll("\\\\",
											"");
									clsReturn.ReceiveAddress1 = clsReturn.OrderAddress1;
									intCol++;
									clsReturn.AddressType = "";
									clsReturn.OrderUserNM = GF.excelString((Row) xSSFRow, intCol++).replaceAll(" ", "")
											.replaceAll("\\\\", "");
									clsReturn.ReceiveUserNM = clsReturn.OrderUserNM;
									clsReturn.OrderHomePhone = GF.excelString((Row) xSSFRow, intCol++)
											.replaceAll("\\\\", "");
									clsReturn.ReceiveHomePhone = clsReturn.OrderHomePhone;
									clsReturn.OrderCellPhone = GF.excelString((Row) xSSFRow, intCol++)
											.replaceAll("\\\\", "");
									clsReturn.ReceiveCellPhone = clsReturn.OrderCellPhone;
									clsReturn.PromoDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));
									clsReturn.PutDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));
									if (clsReturn.PromoDT == null) {
										if (clsReturn.PutDT != null)
											clsReturn.PromoDT = clsReturn.PutDT;
									} else if (clsReturn.PutDT == null) {
										clsReturn.PutDT = clsReturn.PromoDT;
									}
									clsReturn.OrderDT = clsReturn.PromoDT;
									if (clsReturn.OrderDT != null) {
										clsReturn.GoodsOptionCD_Origin = GF.excelString((Row) xSSFRow, intCol++)
												.replaceAll(" ", "").replaceAll("\\\\", "");
										clsReturn.GoodsOptionNM_Origin = GF.excelString((Row) xSSFRow, intCol++)
												.replaceAll("\\\\", "");
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										intCol++;
										clsReturn.StopDT = GF.toDate(GF.excelString((Row) xSSFRow, intCol++));
										clsReturn.StopReason = GF.excelString((Row) xSSFRow, intCol++)
												.replaceAll("\\\\", "");
										arrReturn.add(clsReturn);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			arrReturn = new ArrayList<>();
		} finally {
			sheet_h = null;
			sheet_x = null;
			workbook_h = null;
			workbook_x = null;
		}
		return arrReturn;
	}

	public boolean stopMilkbang(MilkbangBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) stopMilkbang : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			if (p_Param.AgencyCD > 0L && p_Param.PromoDT != null &&

					!p_Param.OrderUserNM.equals("") && !p_Param.OrderAddress1.equals(""))
				if (!p_Param.GoodsOptionCD_Origin.equals("") || !p_Param.GoodsOptionNM_Origin.equals("")) {
					strSQL = strSQL
							+ " UPDATE tc_milkbanggoods INNER JOIN tc_milkbang on tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD SET StopDT = "
							+ ((p_Param.StopDT == null) ? "null" : ("'" + p_Param.StopDT + "'")) + ",StopReason = '"
							+ GF.recoverSQL(p_Param.StopReason) + "' WHERE (tc_milkbang.AgencyCD = " + p_Param.AgencyCD
							+ ") AND (tc_milkbang.OrderDT = '" + p_Param.OrderDT + "') AND (tc_milkbang.OrderUserNM = '"
							+ GF.recoverSQL(p_Param.OrderUserNM) + "') AND (tc_milkbang.OrderAddress1 = '"
							+ GF.recoverSQL(p_Param.OrderAddress1) + "') AND (tc_milkbang.OrderType = "
							+ p_Param.OrderType + ")";
					if (!p_Param.GoodsOptionCD_Origin.equals(""))
						strSQL = strSQL + " AND (tc_milkbanggoods.GoodsOptionCD_Origin = '"
								+ GF.recoverSQL(p_Param.GoodsOptionCD_Origin) + "')";
					if (!p_Param.GoodsOptionNM_Origin.equals(""))
						strSQL = strSQL + " AND (tc_milkbanggoods.GoodsOptionNM_Origin = '"
								+ GF.recoverSQL(p_Param.GoodsOptionNM_Origin) + "')";
					strSQL = strSQL + ";\n";
					this.FV_conDB.setAutoCommit(false);
					stmt = this.FV_conDB.createStatement(1004, 1007);
					stmt.executeUpdate(strSQL);
					this.FV_conDB.commit();
					blnReturn = true;
				}
		} catch (SQLException e) {
			blnReturn = false;
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public ArrayList<MilkbangBean> findPeriod(String p_StartDT, String p_EndDT, long p_TeamPersonCD) {
		String strError = "(MilkbangManager) findPeriod 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		ArrayList<MilkbangBean> arrMilkbangGoods = new ArrayList<>();
		try {
			String strSQL = "SELECT tc_milkbang.* FROM tc_milkbang INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) WHERE (tc_milkbang.ordertype = 90) AND ('"
					+ p_StartDT + "' <= tc_milkbang.OrderDT) AND (tc_milkbang.OrderDT < ADDDATE('" + p_EndDT
					+ "', INTERVAL 1 DAY))"
					+ ((p_TeamPersonCD > 0L) ? (" AND (tc_agency.TeamPersonCD =" + p_TeamPersonCD + ")") : "")
					+ " AND (tc_milkbang.DeleteYN = 0) ORDER BY tc_milkbang.AgencyCD ASC, tc_milkbang.OrderDT ASC, tc_milkbang.OrderUserNM ASC, tc_milkbang.OrderAddress1 ASC";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int j = 0; j < intMaxRow; j++) {
					MilkbangBean clsReturn = setOrderField(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
			DBManager.rsClose(rs);
			strSQL = "SELECT tc_milkbanggoods.*, tc_teamperson.TeamPersonCD as AgencyTeamPersonCD, tc_teamperson.TeamPersonNM as AgencyTeamPersonNM, tc_team.TeamCD as AgencyTeamCD, tc_team.TeamNM as AgencyTeamNM, tc_goodsoption.MergeCD FROM tc_milkbanggoods INNER JOIN tc_milkbang ON (tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD) INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) LEFT OUTER JOIN tc_teamperson ON (tc_teamperson.TeamPersonCD = tc_agency.TeamPersonCD) LEFT OUTER JOIN tc_team ON (tc_team.TeamCD = tc_teamperson.TeamCD) LEFT OUTER JOIN tc_goodsoption ON (tc_milkbanggoods.GoodsOptionCD = tc_goodsoption.GoodsOptionCD) WHERE (tc_milkbang.ordertype = 90) AND ('"
					+ p_StartDT + "' <= tc_milkbang.OrderDT) AND (tc_milkbang.OrderDT <= '" + p_EndDT + "')"
					+ ((p_TeamPersonCD > 0L) ? (" AND (tc_agency.TeamPersonCD =" + p_TeamPersonCD + ")") : "")
					+ " AND (tc_milkbang.DeleteYN = 0)";
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int j = 0; j < intMaxRow; j++) {
					MilkbangBean clsMilkbangGoods = setMilkbangGoodsField(rs);
					clsMilkbangGoods.TeamPersonCD = rs.getLong("AgencyTeamPersonCD");
					clsMilkbangGoods.TeamPersonNM = (rs.getString("AgencyTeamPersonNM") == null) ? ""
							: rs.getString("AgencyTeamPersonNM");
					clsMilkbangGoods.TeamCD = rs.getLong("AgencyTeamCD");
					clsMilkbangGoods.TeamNM = (rs.getString("AgencyTeamNM") == null) ? ""
							: rs.getString("AgencyTeamNM");
					clsMilkbangGoods.MergeCD = rs.getInt("MergeCD");
					arrMilkbangGoods.add(clsMilkbangGoods);
					rs.next();
				}
			}
			for (int i = 0; i < arrReturn.size(); i++) {
				MilkbangBean clsReturn = arrReturn.get(i);
				for (int j = 0; j < arrMilkbangGoods.size(); j++) {
					MilkbangBean clsMilkbangGoods = arrMilkbangGoods.get(j);
					if (clsReturn.OrderCD == clsMilkbangGoods.OrderCD)
						clsReturn.arrMilkbangGoods.add(clsMilkbangGoods);
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findPeriod(MilkbangBean p_Param) {
		String strError = "(MilkbangManager) findPeriod 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		ArrayList<MilkbangBean> arrMilkbangGoods = new ArrayList<>();
		try {
			String strSelect = "SELECT COUNT(*) as TotalCNT";
			String strFrom = " FROM tc_milkbanggoods INNER JOIN tc_milkbang ON (tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD)";
			if (!p_Param.SearchToDT.equals("")) {
				if (p_Param.SearchToDT.compareTo("2016-04-18") < 0) {
					strFrom = strFrom
							+ " INNER JOIN tc_agency_old as tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) LEFT OUTER JOIN tc_teamperson_old as tc_teamperson ON (tc_teamperson.TeamPersonCD = tc_agency.TeamPersonCD)";
				} else {
					strFrom = strFrom
							+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) LEFT OUTER JOIN tc_teamperson ON (tc_teamperson.TeamPersonCD = tc_agency.TeamPersonCD)";
				}
			} else {
				strFrom = strFrom
						+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) LEFT OUTER JOIN tc_teamperson ON (tc_teamperson.TeamPersonCD = tc_agency.TeamPersonCD)";
			}
			strFrom = strFrom
					+ " LEFT OUTER JOIN tc_team ON (tc_team.TeamCD = tc_teamperson.TeamCD) LEFT OUTER JOIN tc_goodsoption ON (tc_milkbanggoods.GoodsOptionCD = tc_goodsoption.GoodsOptionCD) LEFT OUTER JOIN tc_promoteam ON (tc_promoteam.PromoTeamCD = tc_milkbanggoods.PromoTeamCD)";
			String strWhere = "", strMilkbangWhere = "";
			if (!p_Param.OrderCD_S.equals(""))
				if (p_Param.OrderCD_S.indexOf(",") > 0) {
					strWhere = strWhere + " AND (tc_milkbang.OrderCD IN (" + p_Param.OrderCD_S + ")";
				} else {
					strWhere = strWhere + " AND (tc_milkbang.OrderCD = " + p_Param.OrderCD_S + ")";
				}
			if (!p_Param.SearchFromDT.equals(""))
				strWhere = strWhere + " AND ('" + p_Param.SearchFromDT + "' <= tc_milkbang.OrderDT)";
			if (!p_Param.SearchToDT.equals(""))
				strWhere = strWhere + " AND (tc_milkbang.OrderDT < date_add('" + p_Param.SearchToDT
						+ "',interval 1 DAY))";
			if (p_Param.TeamPersonCD > 0L)
				strWhere = strWhere + " AND (tc_agency.TeamPersonCD = " + p_Param.TeamPersonCD + ")";
			if (p_Param.AgencyCD > 0L)
				strWhere = strWhere + " AND (tc_milkbang.AgencyCD = " + p_Param.AgencyCD + ")";
			if (p_Param.OrderType > 0)
				strWhere = strWhere + " AND (tc_milkbang.OrderType = " + p_Param.OrderType + ")";
			strMilkbangWhere = strWhere;
			if (p_Param.TeamCD > 0L)
				strWhere = strWhere + " AND (tc_team.TeamCD = " + p_Param.TeamCD + ")";
			if (p_Param.HCStatus > 0) {
				strMilkbangWhere = strMilkbangWhere
						+ " AND (tc_milkbang.ordercd in (select tc_milkbanggoods.ordercd from tc_milkbanggoods where (tc_milkbanggoods.OrderCD = tc_milkbang.ordercd)AND (tc_milkbanggoods.HCStatus = "
						+ p_Param.HCStatus + ")))";
				strWhere = strWhere + " AND (tc_milkbanggoods.HCStatus = " + p_Param.HCStatus + ")";
			}
			if (!p_Param.HCStatus_s.equals("")) {
				strMilkbangWhere = strMilkbangWhere
						+ " AND (tc_milkbang.ordercd in (select tc_milkbanggoods.ordercd from tc_milkbanggoods where (tc_milkbanggoods.OrderCD = tc_milkbang.ordercd)AND (tc_milkbanggoods.HCStatus in ("
						+ p_Param.HCStatus_s + "))))";
				strWhere = strWhere + " AND (tc_milkbanggoods.HCStatus in (" + p_Param.HCStatus_s + "))";
			}
			if (p_Param.HCActionStatus > 0) {
				strMilkbangWhere = strMilkbangWhere
						+ " AND (tc_milkbang.ordercd in (select tc_milkbanggoods.ordercd from tc_milkbanggoods where (tc_milkbanggoods.OrderCD = tc_milkbang.ordercd)AND (tc_milkbanggoods.HCActionStatus = "
						+ p_Param.HCActionStatus + ")))";
				strWhere = strWhere + " AND (tc_milkbanggoods.HCActionStatus = " + p_Param.HCActionStatus + ")";
			}
			if (p_Param.HCTeamPersonCD > 0L) {
				strMilkbangWhere = strMilkbangWhere
						+ " AND (tc_milkbang.ordercd in (select tc_milkbanggoods.ordercd from tc_milkbanggoods where (tc_milkbanggoods.OrderCD = tc_milkbang.ordercd)AND (tc_milkbanggoods.HCTeamPersonCD = "
						+ p_Param.HCTeamPersonCD + ")))";
				strWhere = strWhere + " AND (tc_milkbanggoods.HCTeamPersonCD =" + p_Param.HCTeamPersonCD + ")";
			}
			if (p_Param.MilkMethod > 0)
				if (p_Param.MilkMethod == 1) {
					strWhere = strWhere
							+ " AND (tc_milkbanggoods.GoodsOptionCD NOT IN (410790, 410800, 111501, 111502, 111503, 111504))";
				} else if (p_Param.MilkMethod == 2) {
					strWhere = strWhere
							+ " AND (tc_milkbanggoods.GoodsOptionCD IN (410790, 410800, 111501, 111502, 111503, 111504))";
				}
			if (!p_Param.PromoPersonNM.equals(""))
				strWhere = strWhere + " AND (tc_milkbang.PromoPersonNM = '" + p_Param.PromoPersonNM + "')";
			if (!p_Param.AddressType.equals(""))
				if (p_Param.AddressType.equals("1")) {
					strWhere = strWhere + " AND (tc_milkbang.DuplicateYN = 1)";
				} else if (p_Param.AddressType.equals("2")) {
					strWhere = strWhere
							+ " AND ((tc_milkbang.OrderAddress1 like '%유치원%') OR (tc_milkbang.OrderAddress1 like '%어린이집%') OR (tc_milkbang.OrderAddress1 like '%유아원%') OR (tc_milkbang.OrderAddress1 like '%커피숍%'))";
				} else if (p_Param.AddressType.equals("3")) {
					strWhere = strWhere
							+ " AND ((tc_milkbang.OrderHomePhone = '') AND (tc_milkbang.OrderCellPhone = ''))";
				}
			if (p_Param.MasterCloseYN == 1) {
				strMilkbangWhere = strMilkbangWhere
						+ " AND (tc_milkbang.ordercd in (select tc_milkbanggoods.ordercd from tc_milkbanggoods where (tc_milkbanggoods.OrderCD = tc_milkbang.ordercd)AND (tc_milkbanggoods.MasterCloseYN = 1)))";
				strWhere = strWhere + " AND (tc_milkbanggoods.MasterCloseYN = 1)";
			}
			strWhere = strWhere + " AND (tc_milkbang.DeleteYN = 0)";
			if (!strMilkbangWhere.equals(""))
				strMilkbangWhere = " WHERE " + strMilkbangWhere.substring(5);
			strWhere = " WHERE " + strWhere.substring(5);
			String strSQL = strSelect + strFrom + strWhere;
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				rs.first();
				p_Param.Search_TotalCNT = rs.getInt("TotalCNT");
			}
			DBManager.rsClose(rs);
			if (p_Param.Search_TotalCNT > 0) {
				p_Param.Search_TotalPage = (int) Math.ceil(p_Param.Search_TotalCNT / p_Param.Search_ShowCNT);
			} else {
				p_Param.Search_TotalPage = 0;
			}
			if (p_Param.Search_TotalPage < 0)
				p_Param.Search_TotalPage = 1;
			if (p_Param.Search_Page > p_Param.Search_TotalPage)
				p_Param.Search_Page = p_Param.Search_TotalPage;
			if (p_Param.Search_Page < 0)
				p_Param.Search_Page = 1;
			int Start = p_Param.Search_ShowCNT * p_Param.Search_Page - p_Param.Search_ShowCNT;
			if (Start < 0)
				Start = 0;
			String strMilkbangSelect = "SELECT tc_milkbang.*";
			String strMilkbangFrom = " FROM tc_milkbang";
			if (!p_Param.SearchToDT.equals("")) {
				if (p_Param.SearchToDT.compareTo("2016-04-18") < 0) {
					strMilkbangFrom = strMilkbangFrom
							+ " INNER JOIN tc_agency_old as tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD)";
				} else {
					strMilkbangFrom = strMilkbangFrom
							+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD)";
				}
			} else {
				strMilkbangFrom = strMilkbangFrom
						+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD)";
			}
			String strOrderBy = "";
			if (!p_Param.Search_OrderCol.equals("")) {
				strOrderBy = strOrderBy + " ORDER BY " + p_Param.Search_OrderCol + " " + p_Param.Search_OrderBy
						+ ",tc_milkbang.AgencyCD ASC, tc_milkbang.OrderDT ASC, tc_milkbang.OrderUserNM ASC, tc_milkbang.OrderAddress1 ASC";
			} else {
				strOrderBy = strOrderBy
						+ " ORDER BY tc_milkbang.AgencyCD ASC, tc_milkbang.OrderDT ASC, tc_milkbang.OrderUserNM ASC, tc_milkbang.OrderAddress1 ASC";
			}
			strSQL = strMilkbangSelect + strMilkbangFrom + strMilkbangWhere + strOrderBy;
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int j = 0; j < intMaxRow; j++) {
					MilkbangBean clsReturn = setAllField_Milkbang(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
			DBManager.rsClose(rs);
			strSelect = "SELECT tc_milkbanggoods.*, tc_teamperson.TeamPersonCD as AgencyTeamPersonCD, tc_teamperson.TeamPersonNM as AgencyTeamPersonNM, tc_team.TeamCD as AgencyTeamCD, tc_team.TeamNM as AgencyTeamNM, tc_promoteam.PromoTeamNM as PromoTeamNM_origin, tc_goodsoption.MergeCD";
			strWhere = strWhere + " AND (tc_milkbanggoods.DeleteYN = 0)";
			strSQL = strSelect + strFrom + strWhere;
			if (p_Param.Search_ShowCNT > 0)
				strSQL = strSQL + " LIMIT " + Start + "," + p_Param.Search_ShowCNT;
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int j = 0; j < intMaxRow; j++) {
					MilkbangBean clsMilkbangGoods = setMilkbangGoodsField(rs);
					clsMilkbangGoods.TeamPersonCD = rs.getLong("AgencyTeamPersonCD");
					clsMilkbangGoods.TeamPersonNM = (rs.getString("AgencyTeamPersonNM") == null) ? ""
							: rs.getString("AgencyTeamPersonNM");
					clsMilkbangGoods.TeamCD = rs.getLong("AgencyTeamCD");
					clsMilkbangGoods.TeamNM = (rs.getString("AgencyTeamNM") == null) ? ""
							: rs.getString("AgencyTeamNM");
					clsMilkbangGoods.PromoTeamNM = GF.getString(rs, "PromoTeamNM_origin");
					clsMilkbangGoods.MergeCD = rs.getInt("MergeCD");
					arrMilkbangGoods.add(clsMilkbangGoods);
					rs.next();
				}
			}
			for (int i = 0; i < arrReturn.size(); i++) {
				MilkbangBean clsReturn = arrReturn.get(i);
				for (int j = 0; j < arrMilkbangGoods.size(); j++) {
					MilkbangBean clsMilkbangGoods = arrMilkbangGoods.get(j);
					if (clsReturn.OrderCD == clsMilkbangGoods.OrderCD)
						clsReturn.arrMilkbangGoods.add(clsMilkbangGoods);
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findPeriodHC(MilkbangBean p_Param) {
		String strError = "(MilkbangManager) findPeriodHC 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		ArrayList<MilkbangBean> arrMilkbangGoods = new ArrayList<>();
		try {
			String strSelect = "SELECT COUNT(*) as TotalCNT";
			String strFrom = " FROM tc_milkbanggoods INNER JOIN tc_milkbang ON (tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD)";
			if (!p_Param.SearchToDT.equals("")) {
				if (p_Param.SearchToDT.compareTo("2016-04-18") < 0) {
					strFrom = strFrom
							+ " INNER JOIN tc_agency_old as tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) LEFT OUTER JOIN tc_teamperson_old as tc_teamperson ON (tc_teamperson.TeamPersonCD = tc_agency.TeamPersonCD)";
				} else {
					strFrom = strFrom
							+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) LEFT OUTER JOIN tc_teamperson ON (tc_teamperson.TeamPersonCD = tc_agency.TeamPersonCD)";
				}
			} else {
				strFrom = strFrom
						+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD) LEFT OUTER JOIN tc_teamperson ON (tc_teamperson.TeamPersonCD = tc_agency.TeamPersonCD)";
			}
			strFrom = strFrom
					+ " LEFT OUTER JOIN tc_team ON (tc_team.TeamCD = tc_teamperson.TeamCD) LEFT OUTER JOIN tc_goodsoption ON (tc_milkbanggoods.GoodsOptionCD = tc_goodsoption.GoodsOptionCD) LEFT OUTER JOIN tc_promoteam ON (tc_promoteam.PromoTeamCD = tc_milkbanggoods.PromoTeamCD)";
			String strWhere = "", strMilkbangWhere = "";
			if (!p_Param.OrderCD_S.equals(""))
				if (p_Param.OrderCD_S.indexOf(",") > 0) {
					strWhere = strWhere + " AND (tc_milkbang.OrderCD IN (" + p_Param.OrderCD_S + ")";
				} else {
					strWhere = strWhere + " AND (tc_milkbang.OrderCD = " + p_Param.OrderCD_S + ")";
				}
			if (!p_Param.SearchFromDT.equals(""))
				strWhere = strWhere + " AND ('" + p_Param.SearchFromDT + "' <= tc_milkbang.OrderDT)";
			if (!p_Param.SearchToDT.equals(""))
				strWhere = strWhere + " AND (tc_milkbang.OrderDT < date_add('" + p_Param.SearchToDT
						+ "',interval 1 DAY))";
			if (p_Param.HCTeamPersonCD > 0L)
				strWhere = strWhere + " AND (tc_agency.HC_TeamPersonCD = " + p_Param.HCTeamPersonCD + ")";
			if (p_Param.OrderType > 0)
				strWhere = strWhere + " AND (tc_milkbang.OrderType = " + p_Param.OrderType + ")";
			strMilkbangWhere = strWhere;
			if (p_Param.MilkMethod > 0)
				if (p_Param.MilkMethod == 1) {
					strWhere = strWhere
							+ " AND (tc_milkbanggoods.GoodsOptionCD NOT IN (410790, 410800, 111501, 111502, 111503, 111504))";
				} else if (p_Param.MilkMethod == 2) {
					strWhere = strWhere
							+ " AND (tc_milkbanggoods.GoodsOptionCD IN (410790, 410800, 111501, 111502, 111503, 111504))";
				}
			strWhere = strWhere + " AND (tc_milkbanggoods.MasterCloseYN = 1)";
			strWhere = strWhere + " AND (tc_milkbang.DeleteYN = 0)";
			strMilkbangWhere = " WHERE " + strMilkbangWhere.substring(5);
			strWhere = " WHERE " + strWhere.substring(5);
			String strSQL = strSelect + strFrom + strWhere;
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				rs.first();
				p_Param.Search_TotalCNT = rs.getInt("TotalCNT");
			}
			DBManager.rsClose(rs);
			if (p_Param.Search_TotalCNT > 0) {
				p_Param.Search_TotalPage = (int) Math.ceil(p_Param.Search_TotalCNT / p_Param.Search_ShowCNT);
			} else {
				p_Param.Search_TotalPage = 0;
			}
			if (p_Param.Search_TotalPage < 0)
				p_Param.Search_TotalPage = 1;
			if (p_Param.Search_Page > p_Param.Search_TotalPage)
				p_Param.Search_Page = p_Param.Search_TotalPage;
			if (p_Param.Search_Page < 0)
				p_Param.Search_Page = 1;
			int Start = p_Param.Search_ShowCNT * p_Param.Search_Page - p_Param.Search_ShowCNT;
			if (Start < 0)
				Start = 0;
			String strMilkbangSelect = "SELECT tc_milkbang.*";
			String strMilkbangFrom = " FROM tc_milkbang";
			if (!p_Param.SearchToDT.equals("")) {
				if (p_Param.SearchToDT.compareTo("2016-04-18") < 0) {
					strMilkbangFrom = strMilkbangFrom
							+ " INNER JOIN tc_agency_old as tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD)";
				} else {
					strMilkbangFrom = strMilkbangFrom
							+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD)";
				}
			} else {
				strMilkbangFrom = strMilkbangFrom
						+ " INNER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD)";
			}
			String strOrderBy = "";
			if (!p_Param.Search_OrderCol.equals("")) {
				strOrderBy = strOrderBy + " ORDER BY " + p_Param.Search_OrderCol + " " + p_Param.Search_OrderBy
						+ ",tc_milkbang.AgencyCD ASC, tc_milkbang.OrderDT ASC, tc_milkbang.OrderUserNM ASC, tc_milkbang.OrderAddress1 ASC";
			} else {
				strOrderBy = strOrderBy
						+ " ORDER BY tc_milkbang.AgencyCD ASC, tc_milkbang.OrderDT ASC, tc_milkbang.OrderUserNM ASC, tc_milkbang.OrderAddress1 ASC";
			}
			strSQL = strMilkbangSelect + strMilkbangFrom + strMilkbangWhere + strOrderBy;
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int j = 0; j < intMaxRow; j++) {
					MilkbangBean clsReturn = setAllField_Milkbang(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
			DBManager.rsClose(rs);
			strSelect = "SELECT tc_milkbanggoods.*, tc_teamperson.TeamPersonCD as AgencyTeamPersonCD, tc_teamperson.TeamPersonNM as AgencyTeamPersonNM, tc_team.TeamCD as AgencyTeamCD, tc_team.TeamNM as AgencyTeamNM, tc_promoteam.PromoTeamNM as PromoTeamNM_origin, tc_goodsoption.MergeCD";
			strSQL = strSelect + strFrom + strWhere;
			if (p_Param.Search_ShowCNT > 0)
				strSQL = strSQL + " LIMIT " + Start + "," + p_Param.Search_ShowCNT;
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int j = 0; j < intMaxRow; j++) {
					MilkbangBean clsMilkbangGoods = setMilkbangGoodsField(rs);
					clsMilkbangGoods.TeamPersonCD = rs.getLong("AgencyTeamPersonCD");
					clsMilkbangGoods.TeamPersonNM = (rs.getString("AgencyTeamPersonNM") == null) ? ""
							: rs.getString("AgencyTeamPersonNM");
					clsMilkbangGoods.TeamCD = rs.getLong("AgencyTeamCD");
					clsMilkbangGoods.TeamNM = (rs.getString("AgencyTeamNM") == null) ? ""
							: rs.getString("AgencyTeamNM");
					clsMilkbangGoods.PromoTeamNM = GF.getString(rs, "PromoTeamNM_origin");
					clsMilkbangGoods.MergeCD = rs.getInt("MergeCD");
					arrMilkbangGoods.add(clsMilkbangGoods);
					rs.next();
				}
			}
			for (int i = 0; i < arrReturn.size(); i++) {
				MilkbangBean clsReturn = arrReturn.get(i);
				for (int j = 0; j < arrMilkbangGoods.size(); j++) {
					MilkbangBean clsMilkbangGoods = arrMilkbangGoods.get(j);
					if (clsReturn.OrderCD == clsMilkbangGoods.OrderCD)
						clsReturn.arrMilkbangGoods.add(clsMilkbangGoods);
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findPromoTeam(String p_StartDT, String p_EndDT, int p_PromoTeamCD) {
		String strError = "(MilkbangManager) findPromoTeam 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSQL = "SELECT tc_milkbanggoods.*,tc_milkbang.AgencyCD, tc_milkbang.AgencyNM,tc_milkbang.OrderUserNM, tc_milkbang.PromoPersonNM , tc_goodsoption.MergeCD FROM tc_milkbanggoods INNER JOIN tc_milkbang ON (tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD) LEFT OUTER JOIN tc_goodsoption ON (tc_milkbanggoods.GoodsOptionCD = tc_goodsoption.GoodsOptionCD) WHERE ('"
					+ p_StartDT + "' <= tc_milkbanggoods.PromoDT) AND (tc_milkbanggoods.PromoDT <= '" + p_EndDT
					+ "') AND (tc_milkbanggoods.PromoTeamCD =" + p_PromoTeamCD
					+ ") AND (tc_milkbang.DeleteYN = 0) ORDER BY tc_milkbang.AgencyCD ASC, tc_milkbang.PromoPersonNM ASC, tc_milkbanggoods.PromoDT ASC";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					MilkbangBean clsReturn = setMilkbangGoodsField(rs);
					clsReturn.MergeCD = rs.getInt("MergeCD");
					clsReturn.AgencyCD = rs.getLong("AgencyCD");
					clsReturn.AgencyNM = (rs.getString("AgencyNM") == null) ? "" : rs.getString("AgencyNM");
					clsReturn.OrderUserNM = (rs.getString("OrderUserNM") == null) ? "" : rs.getString("OrderUserNM");
					clsReturn.PromoPersonNM = (rs.getString("PromoPersonNM") == null) ? ""
							: rs.getString("PromoPersonNM");
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findMismatchGoods(String p_StartDT, String p_EndDT) {
		String strError = "(MilkbangManager) findMismatchGoods 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSQL = "SELECT tc_milkbanggoods.* FROM tc_milkbanggoods INNER JOIN tc_milkbang ON (tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD) WHERE (tc_milkbang.ordertype = 90) AND ('"
					+ p_StartDT + "' <= tc_milkbang.OrderDT) AND (tc_milkbang.OrderDT <= '" + p_EndDT
					+ "') AND tc_milkbanggoods.GoodsOptionCD = -1 AND (tc_milkbang.DeleteYN = 0)";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int j = 0; j < intMaxRow; j++) {
					MilkbangBean clsReturn = setMilkbangGoodsField(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public boolean updateMilkbangGoods(MilkbangBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) updateMilkbangGoods 에러 : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			strSQL = "UPDATE tc_milkbanggoods";
			String strSET = "";
			if (p_Param.GoodsCD > 0L)
				strSET = strSET + ",GoodsCD = " + p_Param.GoodsCD;
			if (p_Param.GoodsOptionCD > 0L)
				strSET = strSET + ",GoodsOptionCD = " + p_Param.GoodsOptionCD;
			if (!p_Param.GoodsOptionNM.equals(""))
				strSET = strSET + ",GoodsOptionNM = " + GF.sqlString(p_Param.GoodsOptionNM);
			String strWHERE = "";
			if (p_Param.OrderCD > 0L)
				strWHERE = strWHERE + " AND (tc_milkbanggoods.OrderCD = " + p_Param.OrderCD + ")";
			if (p_Param.OrderSEQ > 0)
				strWHERE = strWHERE + " AND (tc_milkbanggoods.OrderSEQ = " + p_Param.OrderSEQ + ")";
			if (p_Param.TeamPersonCD > 0L)
				strWHERE = strWHERE + " AND (tc_milkbanggoods.TeamPersonCD = " + p_Param.TeamPersonCD + ")";
			if (!p_Param.SearchFromDT.equals(""))
				strWHERE = strWHERE + " AND (tc_milkbanggoods.PromoDT >= '" + p_Param.SearchFromDT + "')";
			if (!p_Param.SearchToDT.equals(""))
				strWHERE = strWHERE + " AND (tc_milkbanggoods.PromoDT < ADDDATE('" + p_Param.SearchToDT
						+ "', INTERVAL 1 DAY))";
			if (!strWHERE.equals(""))
				strWHERE = " WHERE " + strWHERE.substring(4);
			strSQL = strSQL + strSET + strWHERE;
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			this.FV_conDB.commit();
			blnReturn = true;
		} catch (SQLException e) {
			blnReturn = false;
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public boolean closeMilkbangGoods(MilkbangBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) closeMilkbangGoods 에러 : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			strSQL = "UPDATE tc_milkbanggoods";
			String strSET = "";
			if (p_Param.GoodsCD > 0L)
				strSET = strSET + ",GoodsCD = " + p_Param.GoodsCD;
			if (p_Param.GoodsOptionCD > 0L)
				strSET = strSET + ",GoodsOptionCD = " + p_Param.GoodsOptionCD;
			if (!p_Param.GoodsOptionNM.equals(""))
				strSET = strSET + ",GoodsOptionNM = " + GF.sqlString(p_Param.GoodsOptionNM);
			if (p_Param.MasterCloseYN == 1) {
				strSET = strSET + ",MasterCloseYN = 1";
				strSET = strSET + ",MasterCloseDT = now()";
			} else if (p_Param.MasterCloseYN == -1) {
				strSET = strSET + ",MasterCloseYN = 0";
				strSET = strSET + ",MasterCloseDT = null";
			}
			strSET = strSET + ",MasterCloseRemark = " + GF.sqlString(p_Param.MasterCloseRemark);
			if (!strSET.equals(""))
				strSET = " SET " + strSET.substring(1);
			String strWHERE = "";
			if (p_Param.OrderCD > 0L)
				strWHERE = strWHERE + " AND (tc_milkbanggoods.OrderCD = " + p_Param.OrderCD + ")";
			if (p_Param.OrderSEQ > 0)
				strWHERE = strWHERE + " AND (tc_milkbanggoods.OrderSEQ = " + p_Param.OrderSEQ + ")";
			if (p_Param.TeamPersonCD > 0L)
				strWHERE = strWHERE + " AND (tc_milkbanggoods.TeamPersonCD = " + p_Param.TeamPersonCD + ")";
			if (!p_Param.SearchFromDT.equals(""))
				strWHERE = strWHERE + " AND (tc_milkbanggoods.PromoDT >= '" + p_Param.SearchFromDT + "')";
			if (!p_Param.SearchToDT.equals(""))
				strWHERE = strWHERE + " AND (tc_milkbanggoods.PromoDT < ADDDATE('" + p_Param.SearchToDT
						+ "', INTERVAL 1 DAY))";
			if (!strWHERE.equals(""))
				strWHERE = " WHERE " + strWHERE.substring(4);
			strSQL = strSQL + strSET + strWHERE;
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			this.FV_conDB.commit();
			blnReturn = true;
		} catch (SQLException e) {
			blnReturn = false;
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public ArrayList<MilkbangBean> findFailSummary(MilkbangBean p_Param) {
		String strError = "(MilkbangManager) findFailSummary 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSelect = "SELECT COUNT(*) as TotalCNT";
			String strFrom = " FROM tc_milkbang LEFT OUTER JOIN tc_agency ON (tc_milkbang.AgencyCD = tc_agency.AgencyCD)";
			String strWhere = "";
			if (!p_Param.SearchFromDT.equals(""))
				strWhere = strWhere + " AND ('" + p_Param.SearchFromDT + "' <= tc_milkbang.OrderDT)";
			if (!p_Param.SearchToDT.equals(""))
				strWhere = strWhere + " AND (tc_milkbang.OrderDT < date_add('" + p_Param.SearchToDT
						+ "',interval 1 DAY))";
			if (p_Param.TeamPersonCD > 0L)
				strWhere = strWhere + " AND (tc_agency.TeamPersonCD = " + p_Param.TeamPersonCD + ")";
			if (p_Param.AgencyCD > 0L)
				strWhere = strWhere + " AND (tc_milkbang.AgencyCD = " + p_Param.AgencyCD + ")";
			strWhere = strWhere + " AND (tc_milkbang.DeleteYN = 0)";
			strWhere = " WHERE " + strWhere.substring(5);
			String strSQL = strSelect + strFrom + strWhere;
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				rs.first();
				p_Param.Search_TotalCNT = rs.getInt("TotalCNT");
			}
			DBManager.rsClose(rs);
			if (p_Param.Search_TotalCNT > 0) {
				p_Param.Search_TotalPage = (int) Math.ceil(p_Param.Search_TotalCNT / p_Param.Search_ShowCNT);
			} else {
				p_Param.Search_TotalPage = 0;
			}
			if (p_Param.Search_Page > p_Param.Search_TotalPage)
				p_Param.Search_Page = p_Param.Search_TotalPage;
			int Start = p_Param.Search_ShowCNT * p_Param.Search_Page - p_Param.Search_ShowCNT;
			if (Start < 0)
				Start = 0;
			strSelect = "SELECT tc_milkbang.*";
			strSQL = strSelect + strFrom + strWhere;
			if (!p_Param.Search_OrderCol.equals("")) {
				strSQL = strSQL + " ORDER BY " + p_Param.Search_OrderCol + " " + p_Param.Search_OrderBy;
			} else {
				strSQL = strSQL + " ORDER BY OrderCD ASC";
			}
			if (p_Param.Search_ShowCNT > 0)
				strSQL = strSQL + " LIMIT " + Start + "," + p_Param.Search_ShowCNT;
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					MilkbangBean clsReturn = setAllField_Milkbang(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public MilkbangBean setAllField_Milkbang(ResultSet rs) {
		MilkbangBean clsReturn;
		String strError = "(MilkbangManager) setAllField_Milkbang 에러 : ";
		try {
			clsReturn = new MilkbangBean();
			clsReturn.OrderCD = rs.getLong("OrderCD");
			clsReturn.OrderDT = rs.getTimestamp("OrderDT");
			clsReturn.OrderType = rs.getInt("OrderType");
			clsReturn.OrderUserCD = rs.getLong("OrderUserCD");
			clsReturn.OrderUserNM = GF.getString(rs, "OrderUserNM");
			clsReturn.OrderHomePhone = GF.getString(rs, "OrderHomePhone");
			clsReturn.OrderCellPhone = GF.getString(rs, "OrderCellPhone");
			clsReturn.OrderEmail = GF.getString(rs, "OrderEmail");
			clsReturn.OrderZipCD = GF.getString(rs, "OrderZipCD");
			clsReturn.OrderAddress1 = GF.getString(rs, "OrderAddress1");
			clsReturn.OrderAddress2 = GF.getString(rs, "OrderAddress2");
			clsReturn.OrderRemark = GF.getString(rs, "OrderRemark");
			clsReturn.ReceiveUserNM = GF.getString(rs, "ReceiveUserNM");
			clsReturn.ReceiveHomePhone = GF.getString(rs, "ReceiveHomePhone");
			clsReturn.ReceiveCellPhone = GF.getString(rs, "ReceiveCellPhone");
			clsReturn.ReceiveEmail = GF.getString(rs, "ReceiveEmail");
			clsReturn.ReceiveZipCD = GF.getString(rs, "ReceiveZipCD");
			clsReturn.ReceiveAddress1 = GF.getString(rs, "ReceiveAddress1");
			clsReturn.ReceiveAddress2 = GF.getString(rs, "ReceiveAddress2");
			clsReturn.StaffRemark = GF.getString(rs, "StaffRemark");
			clsReturn.TotalOrderPrice = GF.getBigDecimal(rs, "TotalOrderPrice");
			clsReturn.TotalPayPrice = GF.getBigDecimal(rs, "TotalPayPrice");
			clsReturn.TotalUseHomePoint = GF.getBigDecimal(rs, "TotalUseHomePoint");
			clsReturn.TotalUseHomeEventPoint = GF.getBigDecimal(rs, "TotalUseHomeEventPoint");
			clsReturn.TotalUseShopPoint = GF.getBigDecimal(rs, "TotalUseShopPoint");
			clsReturn.TotalUseShopEventPoint = GF.getBigDecimal(rs, "TotalUseShopEventPoint");
			clsReturn.DeleteYN = rs.getInt("DeleteYN");
			clsReturn.MilkbangFileNM = GF.getString(rs, "MilkbangFileNM");
			clsReturn.AgencyCD = rs.getLong("AgencyCD");
			clsReturn.AgencyNM = GF.getString(rs, "AgencyNM");
			clsReturn.AgencyTel = GF.getString(rs, "AgencyTel");
			clsReturn.AgencyDeliveryYN = rs.getInt("AgencyDeliveryYN");
			clsReturn.PromoPersonNM = GF.getString(rs, "PromoPersonNM");
			clsReturn.PromoPersonNM_Origin = GF.getString(rs, "PromoPersonNM_Origin");
			clsReturn.PostArea = GF.getString(rs, "PostArea");
			clsReturn.AddressType = GF.getString(rs, "AddressType");
			clsReturn.ForceAddYN = rs.getInt("ForceAddYN");
			clsReturn.DuplicateYN = rs.getInt("DuplicateYN");
			clsReturn.DuplOrderCD = rs.getLong("DuplOrderCD");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		}
		return clsReturn;
	}

	public MilkbangBean setOrderField_MilkbangGoods(ResultSet rs) {
		MilkbangBean clsReturn;
		String strError = "(UserManager) setOrderField_MilkbangGoods 에러 : ";
		MilkbangBean clsOrder = setOrderField(rs);
		try {
			clsReturn = new MilkbangBean();
			clsReturn.OrderCD = clsOrder.OrderCD;
			clsReturn.OrderDT = clsOrder.OrderDT;
			clsReturn.OrderType = clsOrder.OrderType;
			clsReturn.OrderUserCD = clsOrder.OrderUserCD;
			clsReturn.OrderUserNM = clsOrder.OrderUserNM;
			clsReturn.OrderHomePhone = clsOrder.OrderHomePhone;
			clsReturn.OrderCellPhone = clsOrder.OrderCellPhone;
			clsReturn.OrderEmail = clsOrder.OrderEmail;
			clsReturn.OrderZipCD = clsOrder.OrderZipCD;
			clsReturn.OrderAddress1 = clsOrder.OrderAddress1;
			clsReturn.OrderAddress2 = clsOrder.OrderAddress2;
			clsReturn.OrderRemark = clsOrder.OrderRemark;
			clsReturn.ReceiveUserNM = clsOrder.ReceiveUserNM;
			clsReturn.ReceiveHomePhone = clsOrder.ReceiveHomePhone;
			clsReturn.ReceiveCellPhone = clsOrder.ReceiveCellPhone;
			clsReturn.ReceiveEmail = clsOrder.ReceiveEmail;
			clsReturn.ReceiveZipCD = clsOrder.ReceiveZipCD;
			clsReturn.ReceiveAddress1 = clsOrder.ReceiveAddress1;
			clsReturn.ReceiveAddress2 = clsOrder.ReceiveAddress2;
			clsReturn.StaffRemark = clsOrder.StaffRemark;
			clsReturn.TotalOrderPrice = clsOrder.TotalOrderPrice;
			clsReturn.TotalPayPrice = clsOrder.TotalPayPrice;
			clsReturn.TotalUseHomePoint = clsOrder.TotalUseHomePoint;
			clsReturn.TotalUseHomeEventPoint = clsOrder.TotalUseHomeEventPoint;
			clsReturn.TotalUseShopPoint = clsOrder.TotalUseShopPoint;
			clsReturn.TotalUseShopEventPoint = clsOrder.TotalUseShopEventPoint;
			clsReturn.DeleteYN = clsOrder.DeleteYN;
			clsReturn.MilkbangFileNM = clsOrder.MilkbangFileNM;
			clsReturn.AgencyCD = clsOrder.AgencyCD;
			clsReturn.AgencyNM = clsOrder.AgencyNM;
			clsReturn.AgencyTel = clsOrder.AgencyTel;
			clsReturn.AgencyDeliveryYN = clsOrder.AgencyDeliveryYN;
			clsReturn.PromoPersonNM = clsOrder.PromoPersonNM;
			clsReturn.PromoPersonNM_Origin = clsOrder.PromoPersonNM_Origin;
			clsReturn.PostArea = clsOrder.PostArea;
			clsReturn.AddressType = clsOrder.AddressType;
			clsReturn.ForceAddYN = clsOrder.ForceAddYN;
			clsReturn.GiftCD = clsOrder.GiftCD;
			clsReturn.MessageCD = clsOrder.MessageCD;
			clsReturn.PG_Type = clsOrder.PG_Type;
			clsReturn.PG_PayYN = clsOrder.PG_PayYN;
			clsReturn.PG_PayType = clsOrder.PG_PayType;
			clsReturn.PG_PayDT = clsOrder.PG_PayDT;
			clsReturn.PG_TID = clsOrder.PG_TID;
			clsReturn.PG_CardNM = clsOrder.PG_CardNM;
			clsReturn.PG_CardAuthNO = clsOrder.PG_CardAuthNO;
			clsReturn.PG_CardNO = clsOrder.PG_CardNO;
			clsReturn.PG_CardMonth = clsOrder.PG_CardMonth;
			clsReturn.PG_CardInterest = clsOrder.PG_CardInterest;
			clsReturn.PG_BankNM = clsOrder.PG_BankNM;
			clsReturn.PG_BankNO = clsOrder.PG_BankNO;
			clsReturn.PG_BankReceiver = clsOrder.PG_BankReceiver;
			clsReturn.PG_BankSender = clsOrder.PG_BankSender;
			clsReturn.PG_BankLimitDT = clsOrder.PG_BankLimitDT;
			clsReturn.PG_CashReceiptNO = clsOrder.PG_CashReceiptNO;
			clsReturn.PG_CashReceiptType = clsOrder.PG_CashReceiptType;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		}
		return clsReturn;
	}

	public MilkbangBean setMilkbangGoodsField(ResultSet rs) {
		MilkbangBean clsReturn;
		String strError = "(MilkbangManager) setMilkbangGoodsField 에러 : ";
		try {
			clsReturn = new MilkbangBean();
			clsReturn.OrderCD = rs.getLong("OrderCD");
			clsReturn.OrderSEQ = rs.getInt("OrderSEQ");
			clsReturn.GoodsCD = rs.getLong("GoodsCD");
			clsReturn.GoodsOptionCD = rs.getLong("GoodsOptionCD");
			clsReturn.GoodsOptionNM = (rs.getString("GoodsOptionNM") == null) ? "" : rs.getString("GoodsOptionNM");
			clsReturn.Quantity = rs.getInt("Quantity");
			clsReturn.ContractPeriod = rs.getInt("ContractPeriod");
			clsReturn.WeekQty = rs.getInt("WeekQty");
			clsReturn.WeekRemark = (rs.getString("WeekRemark") == null) ? "" : rs.getString("WeekRemark");
			clsReturn.UnitPrice = (rs.getBigDecimal("UnitPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("UnitPrice");
			clsReturn.OrderPrice = (rs.getBigDecimal("OrderPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("OrderPrice");
			clsReturn.FactoryPrice = (rs.getBigDecimal("FactoryPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("FactoryPrice");
			clsReturn.DeliveryFee = (rs.getBigDecimal("DeliveryFee") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("DeliveryFee");
			clsReturn.TMFee = (rs.getBigDecimal("TMFee") == null) ? new BigDecimal("0") : rs.getBigDecimal("TMFee");
			clsReturn.DMFee = (rs.getBigDecimal("DMFee") == null) ? new BigDecimal("0") : rs.getBigDecimal("DMFee");
			clsReturn.OrderStatus = rs.getInt("OrderStatus");
			clsReturn.ProcessStatus = rs.getInt("ProcessStatus");
			clsReturn.DeliveryCompanyNM = (rs.getString("DeliveryCompanyNM") == null) ? ""
					: rs.getString("DeliveryCompanyNM");
			clsReturn.DeliveryNO = (rs.getString("DeliveryNO") == null) ? "" : rs.getString("DeliveryNO");
			clsReturn.OrderGoodsRemark = (rs.getString("OrderGoodsRemark") == null) ? ""
					: rs.getString("OrderGoodsRemark");
			clsReturn.DeleteYN = rs.getInt("DeleteYN");
			clsReturn.PromoDT = rs.getTimestamp("PromoDT");
			clsReturn.PutDT = rs.getTimestamp("PutDT");
			clsReturn.ExpireDT = rs.getTimestamp("ExpireDT");
			clsReturn.GoodsOptionCD_Origin = (rs.getString("GoodsOptionCD_Origin") == null) ? ""
					: rs.getString("GoodsOptionCD_Origin");
			clsReturn.GoodsOptionNM_Origin = (rs.getString("GoodsOptionNM_Origin") == null) ? ""
					: rs.getString("GoodsOptionNM_Origin");
			clsReturn.OrderKindCD = rs.getInt("OrderKindCD");
			clsReturn.OrderKind = (rs.getString("OrderKind") == null) ? "" : rs.getString("OrderKind");
			clsReturn.AgencyHob = (rs.getBigDecimal("AgencyHob") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("AgencyHob");
			clsReturn.HQHob = (rs.getBigDecimal("HQHob") == null) ? new BigDecimal("0") : rs.getBigDecimal("HQHob");
			clsReturn.PromoGiftNM = (rs.getString("PromoGiftNM") == null) ? "" : rs.getString("PromoGiftNM");
			clsReturn.GiveDT = rs.getTimestamp("GiveDT");
			clsReturn.GivePersonNM = (rs.getString("GivePersonNM") == null) ? "" : rs.getString("GivePersonNM");
			clsReturn.StopDT = rs.getTimestamp("StopDT");
			clsReturn.StopReason = (rs.getString("StopReason") == null) ? "" : rs.getString("StopReason");
			clsReturn.SaveYN = rs.getInt("SaveYN");
			clsReturn.SaveDT = rs.getTimestamp("SaveDT");
			clsReturn.SaveRemark = (rs.getString("SaveRemark") == null) ? "" : rs.getString("SaveRemark");
			clsReturn.MasterCloseYN = rs.getInt("MasterCloseYN");
			clsReturn.MasterCloseDT = rs.getTimestamp("MasterCloseDT");
			clsReturn.MasterCloseRemark = (rs.getString("MasterCloseRemark") == null) ? ""
					: rs.getString("MasterCloseRemark");
			clsReturn.PromoType = rs.getInt("PromoType");
			clsReturn.PromoTeamCD = rs.getLong("PromoTeamCD");
			clsReturn.PromoTeamNM = (rs.getString("PromoTeamNM") == null) ? "" : rs.getString("PromoTeamNM");
			clsReturn.TeamPersonCD = rs.getLong("TeamPersonCD");
			clsReturn.TeamPersonNM = (rs.getString("TeamPersonNM") == null) ? "" : rs.getString("TeamPersonNM");
			clsReturn.TeamCD = rs.getLong("TeamCD");
			clsReturn.TeamNM = (rs.getString("TeamNM") == null) ? "" : rs.getString("TeamNM");
			clsReturn.ActualHob = GF.getBigDecimal(rs, "ActualHob");
			clsReturn.ForceAddYN = rs.getInt("ForceAddYN");
			clsReturn.HCDT = rs.getTimestamp("HCDT");
			clsReturn.HCStatus = rs.getInt("HCStatus");
			clsReturn.HCContent = GF.getString(rs, "HCContent");
			clsReturn.HCActionStatus = rs.getInt("HCActionStatus");
			clsReturn.HCAction = GF.getString(rs, "HCAction");
			clsReturn.HCHob = GF.getBigDecimal(rs, "HCHob");
			clsReturn.HCCheckHob = GF.getBigDecimal(rs, "HCCheckHob");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		}
		return clsReturn;
	}

	public boolean update(ArrayList<MilkbangBean> p_arrMilkbang) {
		String strSQL = "", strError = "(MilkbangManager) update 에러 : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			strSQL = "";
			for (int i = 0; i < p_arrMilkbang.size(); i++) {
				MilkbangBean clsResult = p_arrMilkbang.get(i);
				strSQL = strSQL + "UPDATE tc_milkbang SET PromoPersonNM = '" + GF.recoverSQL(clsResult.PromoPersonNM)
						+ "' WHERE (OrderCD = " + clsResult.OrderCD + ");\n";
			}
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			blnReturn = true;
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public boolean updateTM_SMS(MilkbangBean p_clsResult) {
		String strSQL = "", strError = "(MilkbangManager) updateTM_SMS 에러 : ";
		Statement stmt = null;
		boolean blnReturn = false;
		MilkbangBean clsResult = p_clsResult;
		try {
			long MessageCD = makeMessageCD();
			strSQL = "Insert tc_message (MessageCD,MessageType,AgencyCD,UserCD,UserNM,CellPhoneNO,SendDT,OpenDT,OpenYN,OrderDT,OrderYN,OrderCD,Content,TemplateCD,TemplateType) SELECT "
					+ MessageCD + "," + p_clsResult.TM_MessageType
					+ ",AgencyCD, null, OrderUserNM, OrderCellPhone, now(), null, 0, null, 0, null,null,"
					+ p_clsResult.TM_SMS_TemplateCD + "," + p_clsResult.TM_SMS_TemplateType
					+ " FROM tc_milkbang WHERE (OrderCD = " + clsResult.OrderCD + ");\n";
			strSQL = strSQL + "UPDATE tc_milkbang SET TM_SMS_MessageCD = " + MessageCD + ", TM_SMS_SendYN = "
					+ clsResult.TM_SMS_SendYN + " WHERE (OrderCD = " + clsResult.OrderCD + ");\n";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			blnReturn = true;
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public long makeMessageCD() {
		String strError = "(UserManager) makeMessageCD 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		long lngReturn = 0L;
		try {
			String strSQL = "SELECT (IFNULL(MAX(MessageCD),0) + 1) as MessageCD FROM tc_message";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true)
				lngReturn = rs.getLong("MessageCD");
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return lngReturn;
	}

	public ArrayList<MilkbangBean> computeActualHob(ArrayList<MilkbangBean> p_Milkbang,
			ArrayList<GoodsHobBean> p_GoodsHob) {
		String strError = "(MilkbangManager) computeActualHob : ";
		try {
			for (int i = 0; i < p_Milkbang.size(); i++) {
				MilkbangBean clsMilkbang = p_Milkbang.get(i);
				ArrayList<MilkbangBean> arrContract = new ArrayList<>();
				int[] arrWeekQty_Contract = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
				ArrayList<MilkbangBean> arrNoContract = new ArrayList<>();
				int[] arrWeekQty_NoContract = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
				int intSaveCNT = 0;
				if (clsMilkbang.PromoPersonNM.replaceAll(" ", "").equals("이사연결") == true
						|| clsMilkbang.PromoPersonNM.replaceAll(" ", "").equals("온라인") == true
						|| clsMilkbang.PromoPersonNM.replaceAll(" ", "").equals("직원판촉") == true) {
					clsMilkbang.ComputeHobCause += "\n" + clsMilkbang.PromoPersonNM.replaceAll(" ", "") + " : 0홉";
				} else {
					for (int j = 0; j < clsMilkbang.arrMilkbangGoods.size(); j++) {
						MilkbangBean clsMilkbangGoods = clsMilkbang.arrMilkbangGoods.get(j);
						clsMilkbang.WeekQty += clsMilkbangGoods.WeekQty;
						if (clsMilkbangGoods.StopDT != null) {
							clsMilkbang.ComputeHobCause += "\n" + clsMilkbangGoods.GoodsOptionNM + " (중단일:"
									+ GF.formatDate(clsMilkbangGoods.StopDT) + ") - " + clsMilkbangGoods.StopReason
									+ " : 0홉";
						} else if (clsMilkbangGoods.OrderKind.equals("신청") || clsMilkbangGoods.OrderKind.equals("추가")
								|| clsMilkbangGoods.OrderKind.equals("판촉")) {
							if (clsMilkbangGoods.SaveYN == 0) {
								clsMilkbangGoods.OrderKindCD = 1;
								if (clsMilkbangGoods.ContractPeriod == 0) {
									clsMilkbangGoods.OrderKindCD = 3;
								} else if (clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("추후")
										|| clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("지급")
										|| clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("지급예정")
										|| clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("차후")
										|| clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("선택")
										|| clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("미정")
										|| clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("")) {
									clsMilkbangGoods.OrderKindCD = 3;
								}
							}
							int intMergeCD = clsMilkbangGoods.MergeCD;
							if (clsMilkbangGoods.OrderKindCD == 1) {
								arrContract.add(clsMilkbangGoods);
								arrWeekQty_Contract[intMergeCD] = arrWeekQty_Contract[intMergeCD]
										+ clsMilkbangGoods.WeekQty;
							} else {
								arrNoContract.add(clsMilkbangGoods);
								arrWeekQty_NoContract[intMergeCD] = arrWeekQty_NoContract[intMergeCD]
										+ clsMilkbangGoods.WeekQty;
							}
						} else if (clsMilkbangGoods.OrderKind.equals("재계") || clsMilkbangGoods.OrderKind.equals("재투입")
								|| clsMilkbangGoods.OrderKind.equals("재계약")
								|| clsMilkbangGoods.OrderKind.equals("재판촉")) {
							if (clsMilkbangGoods.SaveYN == 0)
								clsMilkbangGoods.OrderKindCD = 2;
							if (!clsMilkbangGoods.PromoGiftNM.replaceAll(" ", "").equals("")) {
								if (clsMilkbangGoods.SaveYN == 0)
									clsMilkbangGoods.ActualHob = new BigDecimal("1");
								clsMilkbang.ReContractHob = clsMilkbang.ReContractHob.add(clsMilkbangGoods.ActualHob);
								clsMilkbang.ComputeHobCause += "\n" + clsMilkbangGoods.GoodsOptionNM + " - 재계약 : 1홉";
							} else {
								clsMilkbang.ComputeHobCause += "\n" + clsMilkbangGoods.GoodsOptionNM + " - 계약선물 없음";
							}
						} else {
							if (clsMilkbangGoods.SaveYN == 0)
								clsMilkbangGoods.OrderKindCD = 3;
							clsMilkbang.ComputeHobCause += "\n" + clsMilkbangGoods.GoodsOptionNM + " - "
									+ clsMilkbangGoods.OrderKind + " : 0홉";
						}
						if (clsMilkbangGoods.SaveYN == 1)
							intSaveCNT++;
					}
				}
				if (arrContract.size() > 0) {
					clsMilkbang.ComputeHobCause += "\n신규 " + arrContract.size() + " 건 : ";
					for (int j = 1; j < arrWeekQty_Contract.length; j++) {
						int iWeekQty = arrWeekQty_Contract[j];
						int intMergeCD = j;
						if (clsMilkbang.OrderCD == Long.parseLong("3002018120305684"))
							;
						if (iWeekQty > 0) {
							GoodsHobBean clsHobParam = new GoodsHobBean();
							clsHobParam.MergeCD = intMergeCD;
							clsHobParam.WeekQty = iWeekQty;
							clsHobParam.Cause = clsMilkbang.ComputeHobCause;
							clsMilkbang.ContractHob = clsMilkbang.ContractHob
									.add(computeHob(arrContract, p_GoodsHob, clsHobParam));
							clsMilkbang.ComputeHobCause = clsHobParam.Cause;
							if (clsMilkbang.OrderCD == Long.parseLong("3002018120305684")) {
								System.out.println("WeekQty : " + clsHobParam.WeekQty);
								System.out.println("Cause : " + clsHobParam.Cause);
								System.out.println("ContractHob : " + clsMilkbang.ContractHob);
								System.out.println("ComputeHobCause : " + clsMilkbang.ComputeHobCause);
							}
						}
					}
				}
				if (arrNoContract.size() > 0) {
					clsMilkbang.ComputeHobCause += "\n무계약" + arrNoContract.size() + " 건 : ";
					clsMilkbang.ComputeHobCause += "\n(무계약은 0홉으로 인정)";
				}
				clsMilkbang.ActualHob = clsMilkbang.ContractHob.add(clsMilkbang.ReContractHob)
						.add(clsMilkbang.NoContractHob);
				clsMilkbang.ComputeHobCause += "\n------------------------------\n계산된 홉수 : "
						+ GF.formatNumber(clsMilkbang.ActualHob, 1) + "홉";
				if (intSaveCNT != clsMilkbang.arrMilkbangGoods.size()) {
					int j;
					for (j = 0; j < clsMilkbang.arrMilkbangGoods.size(); j++) {
						MilkbangBean clsMilkbangGoods = clsMilkbang.arrMilkbangGoods.get(j);
						if (clsMilkbangGoods.OrderKindCD == 1) {
							clsMilkbangGoods.ActualHob = clsMilkbang.ContractHob;
							break;
						}
					}
					for (j = 0; j < clsMilkbang.arrMilkbangGoods.size(); j++) {
						MilkbangBean clsMilkbangGoods = clsMilkbang.arrMilkbangGoods.get(j);
						if (clsMilkbangGoods.OrderKindCD == 3) {
							clsMilkbangGoods.ActualHob = clsMilkbang.NoContractHob;
							break;
						}
					}
				}
				if (clsMilkbang.ActualHob.compareTo(new BigDecimal("1")) < 0) {
					clsMilkbang.ActualHob = new BigDecimal("0");
					clsMilkbang.ComputeHobCause += "\n▶▶▶ 총홉수가 1홉 미만 이므로 홉수 인정안함";
					if (intSaveCNT != clsMilkbang.arrMilkbangGoods.size())
						for (int j = 0; j < clsMilkbang.arrMilkbangGoods.size(); j++) {
							MilkbangBean clsMilkbangGoods = clsMilkbang.arrMilkbangGoods.get(j);
							clsMilkbangGoods.ActualHob = new BigDecimal("0");
						}
				}
				if (clsMilkbang.OrderHomePhone.equals("") == true && clsMilkbang.OrderCellPhone.equals("") == true) {
					clsMilkbang.ActualHob = new BigDecimal("0");
					clsMilkbang.ComputeHobCause += "\n▶▶▶ 고객 연락처 누락으로 홉수 인정안함";
					if (intSaveCNT != clsMilkbang.arrMilkbangGoods.size())
						for (int j = 0; j < clsMilkbang.arrMilkbangGoods.size(); j++) {
							MilkbangBean clsMilkbangGoods = clsMilkbang.arrMilkbangGoods.get(j);
							clsMilkbangGoods.ActualHob = new BigDecimal("0");
						}
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return p_Milkbang;
	}

	public BigDecimal computeHob(ArrayList<MilkbangBean> p_Milkbang, ArrayList<GoodsHobBean> p_arrGoodsHob,
			GoodsHobBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) computeHob : ";
		Statement stmt = null;
		BigDecimal bdcReturn = new BigDecimal("0");
		try {
			int iMergeCD = p_Param.MergeCD;
			int iWeekQty = p_Param.WeekQty;
			if (iMergeCD > 0 && iWeekQty > 0)
				for (int i = 0; i < p_arrGoodsHob.size(); i++) {
					GoodsHobBean clsReturn = p_arrGoodsHob.get(i);
					if (clsReturn.MergeCD == iMergeCD) {
						if (iWeekQty > 7) {
							for (int j = 0; j < p_Milkbang.size(); j++) {
								MilkbangBean clsMilkbang = p_Milkbang.get(j);
								if (clsMilkbang.MergeCD == iMergeCD) {
									p_Param.Cause += "\n" + clsReturn.GroupNM + " " + clsReturn.MergeNM;
									if (clsMilkbang.WeekQty <= clsReturn.DefaultQty * 1) {
										bdcReturn = clsReturn.WeekQty1;
										p_Param.Cause += " - 주1회: " + clsReturn.WeekQty1 + "홉";
										p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
									} else if (clsMilkbang.WeekQty <= clsReturn.DefaultQty * 2) {
										bdcReturn = clsReturn.WeekQty2;
										p_Param.Cause += " - 주2회: " + clsReturn.WeekQty2 + "홉";
										p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
									} else if (clsMilkbang.WeekQty <= clsReturn.DefaultQty * 3) {
										bdcReturn = clsReturn.WeekQty3;
										p_Param.Cause += " - 주3회: " + clsReturn.WeekQty3 + "홉";
										p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
									} else if (clsMilkbang.WeekQty <= clsReturn.DefaultQty * 4) {
										bdcReturn = clsReturn.WeekQty4;
										p_Param.Cause += " - 주4회: " + clsReturn.WeekQty4 + "홉";
										p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
									} else if (clsMilkbang.WeekQty <= clsReturn.DefaultQty * 5) {
										bdcReturn = clsReturn.WeekQty5;
										p_Param.Cause += " - 주5회: " + clsReturn.WeekQty5 + "홉";
										p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
									} else if (clsMilkbang.WeekQty <= clsReturn.DefaultQty * 6) {
										bdcReturn = clsReturn.WeekQty6;
										p_Param.Cause += " - 주6회: " + clsReturn.WeekQty6 + "홉";
										p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
									} else {
										bdcReturn = clsReturn.WeekQty7;
										p_Param.Cause += " - 주7회: " + clsReturn.WeekQty7 + "홉";
										p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
									}
								}
							}
							break;
						}
						p_Param.Cause += "\n" + clsReturn.GroupNM + " " + clsReturn.MergeNM;
						if (iWeekQty <= clsReturn.DefaultQty * 1) {
							bdcReturn = clsReturn.WeekQty1;
							p_Param.Cause += " - 주1회 : " + clsReturn.WeekQty1 + "홉";
							p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
							break;
						}
						if (iWeekQty <= clsReturn.DefaultQty * 2) {
							bdcReturn = clsReturn.WeekQty2;
							p_Param.Cause += " - 주2회 : " + clsReturn.WeekQty2 + "홉";
							p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
							break;
						}
						if (iWeekQty <= clsReturn.DefaultQty * 3) {
							bdcReturn = clsReturn.WeekQty3;
							p_Param.Cause += " - 주3회 : " + clsReturn.WeekQty3 + "홉";
							p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
							break;
						}
						if (iWeekQty <= clsReturn.DefaultQty * 4) {
							bdcReturn = clsReturn.WeekQty4;
							p_Param.Cause += " - 주4회 : " + clsReturn.WeekQty4 + "홉";
							p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
							break;
						}
						if (iWeekQty <= clsReturn.DefaultQty * 5) {
							bdcReturn = clsReturn.WeekQty5;
							p_Param.Cause += " - 주5회 : " + clsReturn.WeekQty5 + "홉";
							p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
							break;
						}
						if (iWeekQty <= clsReturn.DefaultQty * 6) {
							bdcReturn = clsReturn.WeekQty6;
							p_Param.Cause += " - 주6회 : " + clsReturn.WeekQty6 + "홉";
							p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
							break;
						}
						bdcReturn = clsReturn.WeekQty7;
						p_Param.Cause += " - 주7회 : " + clsReturn.WeekQty7 + "홉";
						p_Param.HobSum = p_Param.HobSum.add(bdcReturn);
						break;
					}
				}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			bdcReturn = new BigDecimal("0");
		}
		return bdcReturn;
	}

	public boolean updateMilkbangSummary(ArrayList<MilkbangBean> p_arrParam) {
		String strSQL = "", strError = "MilkbangManager) updateMilkbangSummary : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			strSQL = "";
			for (int i = 0; i < p_arrParam.size(); i++) {
				MilkbangBean clsResult = p_arrParam.get(i);
				strSQL = strSQL + " UPDATE tc_milkbang SET PromoPersonNM = " + GF.sqlString(clsResult.PromoPersonNM)
						+ ", SaveYN = 1 WHERE (OrderCD = " + clsResult.OrderCD
						+ ");\n UPDATE tc_milkbanggoods SET SaveYN = 1, SaveDT = now(), SaveRemark = "
						+ GF.sqlString(clsResult.SaveRemark) + ", ActualHob = " + clsResult.ActualHob
						+ ", OrderKindCD = " + clsResult.OrderKindCD + ", PromoType = " + clsResult.PromoType
						+ ", PromoTeamCD = " + clsResult.PromoTeamCD + ", HCActionStatus = " + clsResult.HCActionStatus
						+ ", HCAction = " + GF.sqlString(clsResult.HCAction) + " WHERE (OrderCD = " + clsResult.OrderCD
						+ ") AND (OrderSEQ = " + clsResult.OrderSEQ + ") AND (MasterCloseYN <> 1);\n";
			}
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			this.FV_conDB.commit();
			blnReturn = true;
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public boolean updateMilkbangSummaryPopup(MilkbangBean p_Param) {
		String strSQL = "", strError = "(MilkbangManager) updateMilkbangSummaryPopup : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			if (p_Param.MasterCloseYN == 1) {
				for (int i = 0; i < p_Param.arrMilkbangGoods.size(); i++) {
					MilkbangBean clsMilkbangGoods = p_Param.arrMilkbangGoods.get(i);
					strSQL = strSQL + " UPDATE tc_milkbanggoods SET HCActionStatus = " + clsMilkbangGoods.HCActionStatus
							+ ", HCAction = " + GF.sqlString(clsMilkbangGoods.HCAction) + ", HCCheckHob = "
							+ clsMilkbangGoods.HCCheckHob + " WHERE (OrderCD = " + p_Param.OrderCD
							+ ") AND (OrderSEQ = " + clsMilkbangGoods.OrderSEQ + ");\n";
				}
			} else {
				strSQL = " UPDATE tc_milkbang SET tc_milkbang.PromoPersonNM = " + GF.sqlString(p_Param.PromoPersonNM)
						+ ", tc_milkbang.SaveYN = 1 WHERE (tc_milkbang.OrderCD = " + p_Param.OrderCD + ");\n";
				if (!p_Param.PromoPersonNM.equals(p_Param.PromoPersonNM_Before)) {
					if (!p_Param.SearchFromDT.equals("") && !p_Param.SearchToDT.equals("") && p_Param.AgencyCD > 0L) {
						strSQL = strSQL + " UPDATE tc_milkbang SET tc_milkbang.PromoPersonNM = "
								+ GF.sqlString(p_Param.PromoPersonNM) + " WHERE (tc_milkbang.PromoPersonNM = "
								+ GF.sqlString(p_Param.PromoPersonNM_Before) + ")";
						strSQL = strSQL + " AND ('" + p_Param.SearchFromDT + "' <= tc_milkbang.OrderDT)";
						strSQL = strSQL + " AND (tc_milkbang.OrderDT < date_add('" + p_Param.SearchToDT
								+ "',interval 1 DAY))";
					}
					strSQL = strSQL + " AND (tc_milkbang.AgencyCD = " + p_Param.AgencyCD + ")";
					strSQL = strSQL + ";\n";
				}
				for (int i = 0; i < p_Param.arrMilkbangGoods.size(); i++) {
					MilkbangBean clsMilkbangGoods = p_Param.arrMilkbangGoods.get(i);
					strSQL = strSQL + " UPDATE tc_milkbanggoods SET SaveYN = 1, SaveDT = now(), SaveRemark = "
							+ GF.sqlString(clsMilkbangGoods.SaveRemark) + ", ActualHob = " + clsMilkbangGoods.ActualHob
							+ ", OrderKindCD = " + clsMilkbangGoods.OrderKindCD + ", PromoType = "
							+ clsMilkbangGoods.PromoType + ", PromoTeamCD = " + p_Param.PromoTeamCD
							+ ", HCActionStatus = " + clsMilkbangGoods.HCActionStatus + ", HCAction = "
							+ GF.sqlString(clsMilkbangGoods.HCAction) + ", HCCheckHob = " + clsMilkbangGoods.HCCheckHob
							+ " WHERE (OrderCD = " + p_Param.OrderCD + ") AND (OrderSEQ = " + clsMilkbangGoods.OrderSEQ
							+ ") AND (MasterCloseYN <> 1);\n";
					strSQL = strSQL + " UPDATE tc_milkbanggoods SET HCHob = " + clsMilkbangGoods.ActualHob
							+ " WHERE (OrderCD = " + p_Param.OrderCD + ") AND (OrderSEQ = " + clsMilkbangGoods.OrderSEQ
							+ ") AND (HCStatus not in (" + '\r' + "," + '\016' + "," + '\017' + "));\n";
					if (p_Param.PromoTeamCD != p_Param.PromoTeamCD_Before && !p_Param.SearchFromDT.equals("")
							&& !p_Param.SearchToDT.equals("") && p_Param.AgencyCD > 0L) {
						strSQL = strSQL
								+ " UPDATE tc_milkbanggoods INNER JOIN tc_milkbang on (tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD) AND (tc_milkbang.PromoPersonNM = "
								+ GF.sqlString(p_Param.PromoPersonNM_Before) + ") AND ('" + p_Param.SearchFromDT
								+ "' <= tc_milkbang.OrderDT) AND (tc_milkbang.OrderDT < date_add('" + p_Param.SearchToDT
								+ "',interval 1 DAY)) AND (tc_milkbang.AgencyCD = " + p_Param.AgencyCD
								+ ") SET tc_milkbanggoods.PromoTeamCD = " + p_Param.PromoTeamCD
								+ " WHERE (tc_milkbanggoods.MasterCloseYN <> 1)";
						if (p_Param.PromoTeamCD_Before > 0L) {
							strSQL = strSQL + " AND (tc_milkbanggoods.PromoTeamCD = " + p_Param.PromoTeamCD_Before
									+ ")";
						} else {
							strSQL = strSQL
									+ " AND ( (tc_milkbanggoods.PromoTeamCD is null) OR (tc_milkbanggoods.PromoTeamCD = -1) OR (tc_milkbanggoods.PromoTeamCD = 0))";
						}
						strSQL = strSQL + ";\n";
					}
				}
			}
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			this.FV_conDB.commit();
			blnReturn = true;
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public boolean updateHappycallSummary(ArrayList<MilkbangBean> p_arrParam) {
		String strSQL = "", strError = "MilkbangManager) updateHappycallSummary 에러 : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			strSQL = "";
			for (int i = 0; i < p_arrParam.size(); i++) {
				MilkbangBean clsResult = p_arrParam.get(i);
				strSQL = strSQL + " UPDATE tc_milkbanggoods SET  HCDT = " + GF.sqlDate(clsResult.HCDT)
						+ ", HCTeamPersonCD = " + clsResult.HCTeamPersonCD + ", HCStatus = " + clsResult.HCStatus
						+ ", HCContent = " + GF.sqlString(clsResult.HCContent) + ", HCHob = " + clsResult.HCHob
						+ ", HCCheckHob = " + clsResult.HCCheckHob;
				strSQL = strSQL + " WHERE (OrderCD = " + clsResult.OrderCD + ") AND (OrderSEQ = " + clsResult.OrderSEQ
						+ ");\n";
			}
			this.FV_conDB.setAutoCommit(false);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			stmt.executeUpdate(strSQL);
			this.FV_conDB.commit();
			blnReturn = true;
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public ArrayList<MilkbangBean> findMilkbang(MilkbangBean p_Param) {
		String strError = "(OrderManager) findMilkbang 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		MilkbangBean clsOrder = null;
		try {
			String strSQL = "SELECT tc_milkbang.*," + selectMilkbangGoods()
			+ ", tc_user.UserID as OrderUserID FROM ysc.tc_milkbang INNER JOIN ysc.tc_milkbanggoods ON (tc_milkbanggoods.OrderCD = tc_milkbang.OrderCD) LEFT OUTER JOIN ysc.tc_user ON (tc_user.UserCD = tc_milkbang.OrderUserCD)";
			String strWhere = " AND (tc_milkbang.DeleteYN=0)";
			if (p_Param.AgencyCD > 0L)
				strWhere = strWhere + " AND (tc_milkbang.AgencyCD = " + p_Param.AgencyCD + ")";
			if (p_Param.OrderDT != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String dateStr = sdf.format(p_Param.OrderDT);
				strWhere = strWhere + " AND (tc_milkbang.OrderDT = TO_DATE('" + dateStr + "', 'YYYY-MM-DD'))";
			}
			if (!p_Param.OrderUserNM.equals(""))
				strWhere = strWhere + " AND (tc_milkbang.OrderUserNM = '" + GF.recoverSQL(p_Param.OrderUserNM) + "')";
			if (!p_Param.OrderAddress1.equals(""))
				strWhere = strWhere + " AND (tc_milkbang.OrderAddress1 = '" + GF.recoverSQL(p_Param.OrderAddress1)
						+ "')";
			if (!p_Param.OrderCD_S.equals(""))
				if (p_Param.OrderCD_S.indexOf(",") > 0) {
					strWhere = strWhere + " AND (tc_milkbang.OrderCD IN (" + p_Param.OrderCD_S + ")";
				} else {
					strWhere = strWhere + " AND (tc_milkbang.OrderCD = " + p_Param.OrderCD_S + ")";
				}
			if (!strWhere.equals(""))
				strWhere = strWhere.replaceFirst(" AND", " WHERE");
			strSQL = strSQL + strWhere + " ORDER BY tc_milkbang.OrderCD ASC";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				long BeforeOrderCD = 0L;
				for (int i = 0; i < intMaxRow; i++) {
					if (BeforeOrderCD != rs.getLong("OrderCD")) {
						clsOrder = setOrderField(rs);
						clsOrder.OrderUserID = (rs.getString("OrderUserID") == null) ? "" : rs.getString("OrderUserID");
						arrReturn.add(clsOrder);
						BeforeOrderCD = clsOrder.OrderCD;
					}
					MilkbangBean clsMilkbangGoods = new MilkbangBean();
					setMilkbangGoodsField(rs, clsMilkbangGoods);
					clsOrder.arrMilkbangGoods.add(clsMilkbangGoods);
					rs.next();
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findMilkbangGoods(MilkbangBean p_Param) {
		String strError = "(OrderManager) findMilkbangGoods 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		MilkbangBean clsReturn = null;
		try {
			String strSQL = "SELECT tc_milkbanggoods.* FROM tc_milkbanggoods WHERE tc_milkbanggoods.OrderCD="
					+ p_Param.OrderCD;
			if (p_Param.OrderSEQ != 0)
				strSQL = strSQL + " AND tc_milkbanggoods.OrderSEQ=" + p_Param.OrderSEQ;
			if (!p_Param.GoodsOptionNM.equals(""))
				strSQL = strSQL + " AND tc_milkbanggoods.GoodsOptionNM = " + GF.sqlString(p_Param.GoodsOptionNM);
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					clsReturn = setMilkbangGoodsField(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			arrReturn = new ArrayList<>();
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public MilkbangBean setOrderField(ResultSet rs) {
		MilkbangBean clsReturn;
		String strError = "(UserManager) setOrderField 에러 : ";
		try {
			clsReturn = new MilkbangBean();
			clsReturn.OrderCD = rs.getLong("OrderCD");
			clsReturn.OrderDT = rs.getTimestamp("OrderDT");
			clsReturn.OrderType = rs.getInt("OrderType");
			clsReturn.OrderUserCD = rs.getLong("OrderUserCD");
			clsReturn.OrderUserNM = (rs.getString("OrderUserNM") == null) ? "" : rs.getString("OrderUserNM");
			clsReturn.OrderHomePhone = (rs.getString("OrderHomePhone") == null) ? "" : rs.getString("OrderHomePhone");
			clsReturn.OrderCellPhone = (rs.getString("OrderCellPhone") == null) ? "" : rs.getString("OrderCellPhone");
			clsReturn.OrderEmail = (rs.getString("OrderEmail") == null) ? "" : rs.getString("OrderEmail");
			clsReturn.OrderZipCD = (rs.getString("OrderZipCD") == null) ? "" : rs.getString("OrderZipCD");
			clsReturn.OrderAddress1 = (rs.getString("OrderAddress1") == null) ? "" : rs.getString("OrderAddress1");
			clsReturn.OrderAddress2 = (rs.getString("OrderAddress2") == null) ? "" : rs.getString("OrderAddress2");
			clsReturn.OrderRemark = (rs.getString("OrderRemark") == null) ? "" : rs.getString("OrderRemark");
			clsReturn.ReceiveUserNM = (rs.getString("ReceiveUserNM") == null) ? "" : rs.getString("ReceiveUserNM");
			clsReturn.ReceiveHomePhone = (rs.getString("ReceiveHomePhone") == null) ? ""
					: rs.getString("ReceiveHomePhone");
			clsReturn.ReceiveCellPhone = (rs.getString("ReceiveCellPhone") == null) ? ""
					: rs.getString("ReceiveCellPhone");
			clsReturn.ReceiveEmail = (rs.getString("ReceiveEmail") == null) ? "" : rs.getString("ReceiveEmail");
			clsReturn.ReceiveZipCD = (rs.getString("ReceiveZipCD") == null) ? "" : rs.getString("ReceiveZipCD");
			clsReturn.ReceiveAddress1 = (rs.getString("ReceiveAddress1") == null) ? ""
					: rs.getString("ReceiveAddress1");
			clsReturn.ReceiveAddress2 = (rs.getString("ReceiveAddress2") == null) ? ""
					: rs.getString("ReceiveAddress2");
			clsReturn.StaffRemark = (rs.getString("StaffRemark") == null) ? "" : rs.getString("StaffRemark");
			clsReturn.TotalOrderPrice = (rs.getBigDecimal("TotalOrderPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("TotalOrderPrice");
			clsReturn.TotalPayPrice = (rs.getBigDecimal("TotalPayPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("TotalPayPrice");
			clsReturn.TotalUseHomePoint = (rs.getBigDecimal("TotalUseHomePoint") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("TotalUseHomePoint");
			clsReturn.TotalUseHomeEventPoint = (rs.getBigDecimal("TotalUseHomeEventPoint") == null)
					? new BigDecimal("0")
					: rs.getBigDecimal("TotalUseHomeEventPoint");
			clsReturn.TotalUseShopPoint = (rs.getBigDecimal("TotalUseShopPoint") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("TotalUseShopPoint");
			clsReturn.TotalUseShopEventPoint = (rs.getBigDecimal("TotalUseShopEventPoint") == null)
					? new BigDecimal("0")
					: rs.getBigDecimal("TotalUseShopEventPoint");
			clsReturn.PG_Type = rs.getInt("PG_Type");
			clsReturn.PG_PayYN = rs.getInt("PG_PayYN");
			clsReturn.PG_PayType = rs.getInt("PG_PayType");
			clsReturn.PG_PayDT = rs.getTimestamp("PG_PayDT");
			clsReturn.PG_TID = (rs.getString("PG_TID") == null) ? "" : rs.getString("PG_TID");
			clsReturn.PG_CardNM = (rs.getString("PG_CardNM") == null) ? "" : rs.getString("PG_CardNM");
			clsReturn.PG_CardAuthNO = (rs.getString("PG_CardAuthNO") == null) ? "" : rs.getString("PG_CardAuthNO");
			clsReturn.PG_CardNO = (rs.getString("PG_CardNO") == null) ? "" : rs.getString("PG_CardNO");
			clsReturn.PG_CardMonth = (rs.getString("PG_CardMonth") == null) ? "" : rs.getString("PG_CardMonth");
			clsReturn.PG_CardInterest = (rs.getString("PG_CardInterest") == null) ? ""
					: rs.getString("PG_CardInterest");
			clsReturn.PG_BankNM = (rs.getString("PG_BankNM") == null) ? "" : rs.getString("PG_BankNM");
			clsReturn.PG_BankNO = (rs.getString("PG_BankNO") == null) ? "" : rs.getString("PG_BankNO");
			clsReturn.PG_BankReceiver = (rs.getString("PG_BankReceiver") == null) ? ""
					: rs.getString("PG_BankReceiver");
			clsReturn.PG_BankSender = (rs.getString("PG_BankSender") == null) ? "" : rs.getString("PG_BankSender");
			clsReturn.PG_BankLimitDT = (rs.getString("PG_BankLimitDT") == null) ? "" : rs.getString("PG_BankLimitDT");
			clsReturn.PG_CashReceiptNO = (rs.getString("PG_CashReceiptNO") == null) ? ""
					: rs.getString("PG_CashReceiptNO");
			clsReturn.PG_CashReceiptType = (rs.getString("PG_CashReceiptType") == null) ? ""
					: rs.getString("PG_CashReceiptType");
			clsReturn.MilkbangFileNM = (rs.getString("MilkbangFileNM") == null) ? "" : rs.getString("MilkbangFileNM");
			clsReturn.AgencyCD = rs.getLong("AgencyCD");
			clsReturn.AgencyNM = (rs.getString("AgencyNM") == null) ? "" : rs.getString("AgencyNM");
			clsReturn.PromoPersonNM = (rs.getString("PromoPersonNM") == null) ? "" : rs.getString("PromoPersonNM");
			clsReturn.PromoPersonNM_Origin = (rs.getString("PromoPersonNM_Origin") == null) ? ""
					: rs.getString("PromoPersonNM_Origin");
			clsReturn.PostArea = (rs.getString("PostArea") == null) ? "" : rs.getString("PostArea");
			clsReturn.AddressType = (rs.getString("AddressType") == null) ? "" : rs.getString("AddressType");
			clsReturn.ForceAddYN = rs.getInt("ForceAddYN");
			clsReturn.DuplicateYN = rs.getInt("DuplicateYN");
			clsReturn.DuplOrderCD = rs.getLong("DuplOrderCD");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		}
		return clsReturn;
	}

	public String selectMilkbangGoods() {
		String strReturn = " tc_milkbanggoods.OrderCD as OG_OrderCD, tc_milkbanggoods.OrderSEQ, tc_milkbanggoods.GoodsCD, tc_milkbanggoods.GoodsOptionCD, tc_milkbanggoods.GoodsOptionNM, tc_milkbanggoods.Quantity, tc_milkbanggoods.ContractPeriod, tc_milkbanggoods.WeekQty, tc_milkbanggoods.WeekRemark, tc_milkbanggoods.UnitPrice, tc_milkbanggoods.OrderPrice, tc_milkbanggoods.FactoryPrice, tc_milkbanggoods.DeliveryFee, tc_milkbanggoods.TMFee, tc_milkbanggoods.DMFee, tc_milkbanggoods.OrderStatus, tc_milkbanggoods.ProcessStatus, tc_milkbanggoods.DeliveryCompanyNM, tc_milkbanggoods.DeliveryNO, tc_milkbanggoods.OrderGoodsRemark, tc_milkbanggoods.DeleteYN as OG_DeleteYN, tc_milkbanggoods.PromoDT, tc_milkbanggoods.PutDT, tc_milkbanggoods.ExpireDT, tc_milkbanggoods.GoodsOptionCD_Origin, tc_milkbanggoods.GoodsOptionNM_Origin, tc_milkbanggoods.OrderKindCD, tc_milkbanggoods.OrderKind, tc_milkbanggoods.AgencyHob, tc_milkbanggoods.HQHob, tc_milkbanggoods.PromoGiftNM, tc_milkbanggoods.GiveDT, tc_milkbanggoods.GivePersonNM, tc_milkbanggoods.StopDT, tc_milkbanggoods.StopReason, tc_milkbanggoods.SaveYN, tc_milkbanggoods.SaveDT, tc_milkbanggoods.SaveRemark, tc_milkbanggoods.MasterCloseYN, tc_milkbanggoods.MasterCloseDT, tc_milkbanggoods.MasterCloseRemark, tc_milkbanggoods.PromoType, tc_milkbanggoods.PromoTeamCD, tc_milkbanggoods.PromoTeamNM, tc_milkbanggoods.TeamPersonCD, tc_milkbanggoods.TeamPersonNM, tc_milkbanggoods.TeamCD, tc_milkbanggoods.TeamNM, tc_milkbanggoods.ActualHob, tc_milkbanggoods.ForceAddYN as OG_ForceAddYN, tc_milkbanggoods.TM_SMS_MessageCD, tc_milkbanggoods.TM_SMS_SendYN, tc_milkbanggoods.TM_Mail_MessageCD, tc_milkbanggoods.TM_Mail_SendYN, tc_milkbanggoods.TM_OrderYN, tc_milkbanggoods.TM_Tel_Status, tc_milkbanggoods.TM_Tel_Result, tc_milkbanggoods.TM_Tel_Remark, tc_milkbanggoods.TM_Tel_DT, tc_milkbanggoods.HCDT, tc_milkbanggoods.HCStatus, tc_milkbanggoods.HCContent, tc_milkbanggoods.HCActionStatus, tc_milkbanggoods.HCAction, tc_milkbanggoods.HCHob, tc_milkbanggoods.HCCheckHob";
		return strReturn;
	}

	public void setMilkbangGoodsField(ResultSet rs, MilkbangBean clsReturn) {
		String strError = "(MilkbangManager) setMilkbangGoodsField 에러 : ";
		try {
			if (clsReturn == null)
				clsReturn = new MilkbangBean();
			clsReturn.OrderCD = rs.getLong("OG_OrderCD");
			clsReturn.OrderSEQ = rs.getInt("OrderSEQ");
			clsReturn.GoodsCD = rs.getLong("GoodsCD");
			clsReturn.GoodsOptionCD = rs.getLong("GoodsOptionCD");
			clsReturn.GoodsOptionNM = (rs.getString("GoodsOptionNM") == null) ? "" : rs.getString("GoodsOptionNM");
			clsReturn.Quantity = rs.getInt("Quantity");
			clsReturn.ContractPeriod = rs.getInt("ContractPeriod");
			clsReturn.WeekQty = rs.getInt("WeekQty");
			clsReturn.WeekRemark = (rs.getString("WeekRemark") == null) ? "" : rs.getString("WeekRemark");
			clsReturn.UnitPrice = (rs.getBigDecimal("UnitPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("UnitPrice");
			clsReturn.OrderPrice = (rs.getBigDecimal("OrderPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("OrderPrice");
			clsReturn.FactoryPrice = (rs.getBigDecimal("FactoryPrice") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("FactoryPrice");
			clsReturn.DeliveryFee = (rs.getBigDecimal("DeliveryFee") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("DeliveryFee");
			clsReturn.TMFee = (rs.getBigDecimal("TMFee") == null) ? new BigDecimal("0") : rs.getBigDecimal("TMFee");
			clsReturn.DMFee = (rs.getBigDecimal("DMFee") == null) ? new BigDecimal("0") : rs.getBigDecimal("DMFee");
			clsReturn.OrderStatus = rs.getInt("OrderStatus");
			clsReturn.ProcessStatus = rs.getInt("ProcessStatus");
			clsReturn.DeliveryCompanyNM = (rs.getString("DeliveryCompanyNM") == null) ? ""
					: rs.getString("DeliveryCompanyNM");
			clsReturn.DeliveryNO = (rs.getString("DeliveryNO") == null) ? "" : rs.getString("DeliveryNO");
			clsReturn.OrderGoodsRemark = (rs.getString("OrderGoodsRemark") == null) ? ""
					: rs.getString("OrderGoodsRemark");
			clsReturn.DeleteYN = rs.getInt("OG_DeleteYN");
			clsReturn.PromoDT = rs.getTimestamp("PromoDT");
			clsReturn.PutDT = rs.getTimestamp("PutDT");
			clsReturn.ExpireDT = rs.getTimestamp("ExpireDT");
			clsReturn.GoodsOptionCD_Origin = (rs.getString("GoodsOptionCD_Origin") == null) ? ""
					: rs.getString("GoodsOptionCD_Origin");
			clsReturn.GoodsOptionNM_Origin = (rs.getString("GoodsOptionNM_Origin") == null) ? ""
					: rs.getString("GoodsOptionNM_Origin");
			clsReturn.OrderKindCD = rs.getInt("OrderKindCD");
			clsReturn.OrderKind = (rs.getString("OrderKind") == null) ? "" : rs.getString("OrderKind");
			clsReturn.AgencyHob = (rs.getBigDecimal("AgencyHob") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("AgencyHob");
			clsReturn.HQHob = (rs.getBigDecimal("HQHob") == null) ? new BigDecimal("0") : rs.getBigDecimal("HQHob");
			clsReturn.PromoGiftNM = (rs.getString("PromoGiftNM") == null) ? "" : rs.getString("PromoGiftNM");
			clsReturn.GiveDT = rs.getTimestamp("GiveDT");
			clsReturn.GivePersonNM = (rs.getString("GivePersonNM") == null) ? "" : rs.getString("GivePersonNM");
			clsReturn.StopDT = rs.getTimestamp("StopDT");
			clsReturn.StopReason = (rs.getString("StopReason") == null) ? "" : rs.getString("StopReason");
			clsReturn.SaveYN = rs.getInt("SaveYN");
			clsReturn.SaveDT = rs.getTimestamp("SaveDT");
			clsReturn.SaveRemark = GF.getString(rs, "SaveRemark");
			clsReturn.MasterCloseYN = rs.getInt("MasterCloseYN");
			clsReturn.MasterCloseDT = rs.getTimestamp("MasterCloseDT");
			clsReturn.MasterCloseRemark = GF.getString(rs, "MasterCloseRemark");
			clsReturn.PromoType = rs.getInt("PromoType");
			clsReturn.PromoTeamCD = rs.getLong("PromoTeamCD");
			clsReturn.PromoTeamNM = (rs.getString("PromoTeamNM") == null) ? "" : rs.getString("PromoTeamNM");
			clsReturn.TeamPersonCD = rs.getLong("TeamPersonCD");
			clsReturn.TeamPersonNM = (rs.getString("TeamPersonNM") == null) ? "" : rs.getString("TeamPersonNM");
			clsReturn.TeamCD = rs.getLong("TeamCD");
			clsReturn.TeamNM = (rs.getString("TeamNM") == null) ? "" : rs.getString("TeamNM");
			clsReturn.ActualHob = (rs.getBigDecimal("ActualHob") == null) ? new BigDecimal("0")
					: rs.getBigDecimal("ActualHob");
			clsReturn.ForceAddYN = rs.getInt("OG_ForceAddYN");
			clsReturn.TM_SMS_MessageCD = rs.getInt("TM_SMS_MessageCD");
			clsReturn.TM_SMS_SendYN = rs.getInt("TM_SMS_SendYN");
			clsReturn.TM_Mail_MessageCD = rs.getInt("TM_Mail_MessageCD");
			clsReturn.TM_Mail_SendYN = rs.getInt("TM_Mail_SendYN");
			clsReturn.TM_OrderYN = rs.getInt("TM_OrderYN");
			clsReturn.TM_Tel_Status = rs.getInt("TM_Tel_Status");
			clsReturn.TM_Tel_Result = rs.getInt("TM_Tel_Result");
			clsReturn.TM_Tel_Remark = (rs.getString("TM_Tel_Remark") == null) ? "" : rs.getString("TM_Tel_Remark");
			clsReturn.TM_Tel_DT = rs.getTimestamp("TM_Tel_DT");
			clsReturn.HCDT = rs.getTimestamp("HCDT");
			clsReturn.HCStatus = rs.getInt("HCStatus");
			clsReturn.HCContent = GF.getString(rs, "HCContent");
			clsReturn.HCActionStatus = rs.getInt("HCActionStatus");
			clsReturn.HCAction = GF.getString(rs, "HCAction");
			clsReturn.HCHob = GF.getBigDecimal(rs, "HCHob");
			clsReturn.HCCheckHob = GF.getBigDecimal(rs, "HCCheckHob");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			clsReturn = null;
		}
	}

	public ArrayList<MilkbangBean> findForceAdd(String p_StartDT, String p_EndDT, long p_AgencyCD) {
		String strError = "(OrderManager) findForceAdd 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSQL = "SELECT tc_milkbang.*," + selectMilkbangGoods()
					+ " FROM tc_milkbanggoods INNER JOIN tc_milkbang ON (tc_milkbang.OrderCD = tc_milkbanggoods.OrderCD) WHERE ('"
					+ p_StartDT + "' <= tc_milkbang.OrderDT) AND (tc_milkbang.OrderDT < ADDDATE('" + p_EndDT
					+ "', INTERVAL 1 DAY)) AND (tc_milkbang.OrderType =" + 'Z' + ") AND (tc_milkbang.AgencyCD ="
					+ p_AgencyCD + ") AND (tc_milkbanggoods.ForceAddYN = 1) AND (tc_milkbang.DeleteYN = 0)";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					MilkbangBean clsReturn = setOrderField_MilkbangGoods(rs);
					setMilkbangGoodsField(rs, clsReturn);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public boolean forceAddMilkbang(ArrayList<MilkbangBean> p_arrMilkbang) {
		String strSQL = "", strError = "(OrderManager) forceAddMilkbang 에러 : ";
		Statement stmt = null;
		boolean blnReturn = false;
		try {
			if (p_arrMilkbang.size() > 0) {
				for (int i = 0; i < p_arrMilkbang.size(); i++) {
					MilkbangBean clsReturn = p_arrMilkbang.get(i);
					if (clsReturn.OrderCD > 0L)
						strSQL = strSQL + "DELETE FROM tc_milkbang WHERE OrderCD = " + clsReturn.OrderCD + ";\n";
					if (!clsReturn.PromoPersonNM.equals("") && clsReturn.ActualHob.compareTo(new BigDecimal("0")) > 0) {
						long lngOrderCD = makeMilkbangCD(GF.left(clsReturn.OrderDT.toString(), 10));
						strSQL = strSQL
								+ "INSERT INTO tc_milkbang (OrderCD, OrderType, OrderDT, AgencyCD, AgencyNM, PromoPersonNM) SELECT "
								+ lngOrderCD + "," + 'Z' + ",'" + clsReturn.OrderDT + "'," + clsReturn.AgencyCD + ",'"
								+ clsReturn.AgencyNM + "','" + GF.recoverSQL(clsReturn.PromoPersonNM) + "';\n";
						strSQL = strSQL
								+ "INSERT INTO tc_milkbanggoods (OrderCD, OrderSEQ, PromoDT, SaveYN, SaveDT, ActualHob, OrderKindCD, PromoType, PromoTeamCD, PromoTeamNM, TeamPersonCD, TeamPersonNM, TeamCD, TeamNM, ForceAddYN) SELECT "
								+ lngOrderCD + ", 1,'" + clsReturn.PromoDT + "',1, now()," + clsReturn.ActualHob + ","
								+ clsReturn.OrderKindCD + "," + clsReturn.PromoType + "," + clsReturn.PromoTeamCD + ",'"
								+ clsReturn.PromoTeamNM + "'," + clsReturn.TeamPersonCD + ",'" + clsReturn.TeamPersonNM
								+ "'," + clsReturn.TeamCD + ",'" + clsReturn.TeamNM + "',1;\n";
					}
				}
				this.FV_conDB.setAutoCommit(false);
				stmt = this.FV_conDB.createStatement(1004, 1007);
				stmt.executeUpdate(strSQL);
				this.FV_conDB.commit();
				blnReturn = true;
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
			try {
				this.FV_conDB.rollback();
			} catch (SQLException ex) {
				System.out.println(strError + ex.getMessage());
			}
		} finally {
			DBManager.stmtClose(stmt);
		}
		return blnReturn;
	}

	public CommonBean findWeek(Timestamp p_Time) {
		String strSQL = "", strError = "(MilkbangManager) findWeek : ";
		CommonBean clsReturn = new CommonBean();
		int year = Integer.parseInt(GF.left(p_Time.toString(), 4));
		int month = Integer.parseInt(GF.mid(p_Time.toString(), 6, 2));
		int day = Integer.parseInt(GF.mid(p_Time.toString(), 9, 2));
		Calendar today = Calendar.getInstance();
		today.set(year, month - 1, day);
		Calendar sdate = Calendar.getInstance();
		sdate.set(year, month - 1, 1);
		Calendar edate = Calendar.getInstance();
		edate.set(year, month, 1);
		int week = sdate.get(7);
		if (week == 1) {
			sdate.add(5, 1);
		} else if (week > 4) {
			sdate.add(5, 9 - week);
		} else {
			sdate.add(5, -(week - 2));
		}
		if (today.compareTo(sdate) <= 0) {
			sdate.set(year, month - 2, 1);
			week = sdate.get(7);
			if (week == 1) {
				sdate.add(5, 1);
			} else if (week > 4) {
				sdate.add(5, 9 - week);
			} else {
				sdate.add(5, -(week - 2));
			}
			edate.set(year, month - 1, 1);
		}
		week = edate.get(7);
		if (week == 1) {
			edate.add(5, 1);
		} else if (week > 4) {
			edate.add(5, 9 - week);
		} else {
			edate.add(5, -(week - 2));
		}
		edate.add(5, -1);
		int weekCNT = 1;
		clsReturn.SearchFromDT = sdate.get(1) + "-" + GF.right("0" + (sdate.get(2) + 1), 2) + "-"
				+ GF.right("0" + sdate.get(5), 2);
		clsReturn.SearchToDT = edate.get(1) + "-" + GF.right("0" + (edate.get(2) + 1), 2) + "-"
				+ GF.right("0" + edate.get(5), 2);
		String sYear = GF.left(clsReturn.SearchFromDT, 4);
		String sMonth = GF.mid(clsReturn.SearchFromDT, 6, 2);
		String sDay = GF.mid(clsReturn.SearchFromDT, 9, 2);
		return clsReturn;
	}

	public ArrayList<MilkbangBean> findDuplicate(MilkbangBean clsParam) {
		String strError = "(MilkbangManager) findDuplicate 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSQL = "SELECT tc_milkbang.* FROM ysc.tc_milkbang " +
	                "WHERE (tc_milkbang.ordertype = 90) " +
	                "AND ((tc_milkbang.OrderAddress1 = '" + clsParam.OrderAddress1 + "') " +
	                "OR ((tc_milkbang.OrderCellPhone = '" + clsParam.OrderCellPhone + "') " +
	                "AND (tc_milkbang.OrderCellPhone != ''))) " +
	                "AND (TO_DATE('" + clsParam.SearchFromDT + "', 'YYYY-MM-DD HH24:MI:SS') <= tc_milkbang.OrderDT) " +
	                "AND (TO_DATE('" + clsParam.SearchFromDT + "', 'YYYY-MM-DD HH24:MI:SS') >= ADD_MONTHS(tc_milkbang.OrderDT, -14)) " +
	                "AND (tc_milkbang.DeleteYN = 0) " +
	                "ORDER BY tc_milkbang.OrderDT DESC";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					MilkbangBean clsReturn = setOrderField(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public ArrayList<MilkbangBean> findOneclick(MilkbangBean clsParam) {
		String strError = "(MilkbangManager) findOneclick 에러 : ";
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<MilkbangBean> arrReturn = new ArrayList<>();
		try {
			String strSQL = "SELECT tc_order.* FROM ysc.tc_order WHERE (tc_order.ordertype = 44) AND (tc_order.OrderCellPhone = '"
					+ clsParam.OrderCellPhone
					+ "') AND (tc_order.OrderCellPhone != '') AND (tc_order.OrderDT > '2019-04-25 00:00:00') AND (tc_order.DeleteYN = 0) ORDER BY tc_order.OrderDT DESC";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				int intMaxRow = rs.getRow();
				rs.first();
				for (int i = 0; i < intMaxRow; i++) {
					MilkbangBean clsReturn = setOrderField(rs);
					arrReturn.add(clsReturn);
					rs.next();
				}
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return arrReturn;
	}

	public String now() {
		String strError = "(MilkbangManager) now 에러: ";
		Statement stmt = null;
		ResultSet rs = null;
		String clsReturn = "";
		try {
			String strSQL = "select sysdate as now_time from dual";
			stmt = this.FV_conDB.createStatement(1004, 1007);
			rs = stmt.executeQuery(strSQL);
			if (rs.last() == true) {
				rs.first();
				clsReturn = GF.formatDateTime(rs.getTimestamp("now_time"));
			}
		} catch (SQLException e) {
			System.out.println(strError + e.getMessage());
			clsReturn = "";
		} finally {
			DBManager.rsClose(rs);
			DBManager.stmtClose(stmt);
		}
		return clsReturn;
	}
}
