package com.milk.batch.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
	public static Connection openDB(String p_DBName) {
		String strError = "(DBManager) openDB 에러: ";
		Connection con = null;
		try {
			
			// 운영
			Class.forName("oracle.jdbc.OracleDriver");
			System.out.println("==== 오라클 드라이버 로드 성공 ====");
			con = DriverManager.getConnection(
				    "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=165.132.200.104)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=YSMT)))",
				    "apps",
				    "apps21"
				);
			System.out.println("==== 오라클 DB 연결 성공 ====");

		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return con;
	}

	public static Connection openDB() {
		String strError = "(DBManager) openDB 에러 : ";
		Connection con = null;
		try {
			con = openDB("");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return con;
	}

	public static void closeDB(Connection p_con) {
		String strError = "(DBManager) closeDB 에러 : ";
		if (p_con != null)
			try {
				if (!p_con.isClosed()) {
					p_con.close();
					p_con = null;
					System.out.println("==== closeDB 성공!!! ====");
				}
			} catch (SQLException e) {
				System.out.println(strError + e.getMessage());
			}
	}

	public static void stmtClose(Statement p_stmt) {
		String strError = "(DBManager) stmtClose 에러 :";
		if (p_stmt != null)
			try {
				p_stmt.close();
			} catch (SQLException e) {
				System.out.println(strError + e.getMessage());
			}
	}

	public static void pstmtClose(PreparedStatement p_pstmt) {
		String strError = "(DBManager) stmtClose 에러 :";
		if (p_pstmt != null)
			try {
				p_pstmt.close();
			} catch (SQLException e) {
				System.out.println(strError + e.getMessage());
			}
	}

	public static void rsClose(ResultSet p_rs) {
		String strError = "(DBManager) rsClose 에러 :";
		if (p_rs != null)
			try {
				p_rs.close();
			} catch (SQLException e) {
				System.out.println(strError + e.getMessage());
			}
	}
}
