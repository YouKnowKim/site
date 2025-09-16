package com.milk.batch.common;

import java.math.BigDecimal;
import java.math.MathContext;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

public class GF {
	public static String getString(ResultSet rs, String p_Field) {
		String strError = "(GF) getString : ";
		String strReturn = "";
		try {
			strReturn = (rs.getString(p_Field) == null) ? "" : rs.getString(p_Field);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = "";
		}
		return strReturn;
	}

	public static String getString(String p_Field) {
		String strError = "(GF) getString : ";
		String strReturn = "";
		try {
			strReturn = (p_Field == null) ? "" : p_Field;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = "";
		}
		return strReturn;
	}

	public static BigDecimal getBigDecimal(ResultSet rs, String p_Field) {
		String strError = "(GF) getBigDecimal : ";
		BigDecimal strReturn = new BigDecimal("0");
		try {
			strReturn = (rs.getBigDecimal(p_Field) == null) ? new BigDecimal("0") : rs.getBigDecimal(p_Field);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = new BigDecimal("0");
		}
		return strReturn;
	}

	public static BigDecimal getBigDecimal(String p_Field) {
		String strError = "(GF) getBigDecimal : ";
		BigDecimal strReturn = new BigDecimal("0");
		try {
			if (p_Field == null) {
				strReturn = new BigDecimal("0");
			} else if (!isNumber(p_Field)) {
				strReturn = new BigDecimal("0");
			} else {
				strReturn = new BigDecimal(p_Field);
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = new BigDecimal("0");
		}
		return strReturn;
	}

	public static String getLongDate(Timestamp p_Field) {
		String strError = "(GF) getLongDate : ";
		String strReturn = "";
		try {
			strReturn = (p_Field == null) ? "" : left(p_Field.toString(), 19);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = "";
		}
		return strReturn;
	}

	public static String getShortDate(Timestamp p_Field) {
		String strError = "(GF) getShortDate : ";
		String strReturn = "";
		try {
			strReturn = (p_Field == null) ? "" : left(p_Field.toString(), 10);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = "";
		}
		return strReturn;
	}

	public static long getLong(String p_Field) {
		String strError = "(GF) getLong : ";
		long lngReturn = 0L;
		try {
			lngReturn = Long.parseLong((p_Field == null) ? "0" : (p_Field.equals("") ? "0" : p_Field));
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			lngReturn = 0L;
		}
		return lngReturn;
	}

	public static int getInt(String p_Field) {
		String strError = "(GF) getInt : ";
		int intReturn = 0;
		try {
			intReturn = Integer.parseInt((p_Field == null) ? "0" : (p_Field.equals("") ? "0" : p_Field));
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			intReturn = 0;
		}
		return intReturn;
	}

	public static String sqlString(String p_Param) {
		return (p_Param == null) ? "''" : ("'" + recoverSQL(p_Param) + "'");
	}

	public static String sqlDate(Timestamp p_Param) {
		return (p_Param == null) ? "null" : ("'" + p_Param + "'");
	}

	public static String sqlMoney(BigDecimal p_Param) {
		return (p_Param == null) ? "0" : p_Param.toString();
	}

	public static String excelString(Row p_Row, int p_Col) {
    String strError = "(GF) excelString : ";
    String strReturn = "";
    try {
      if (p_Row != null && 
        p_Row.getCell(p_Col) != null) {
        DataFormatter formatter = new DataFormatter();
        strReturn = formatter.formatCellValue(p_Row.getCell(p_Col)).trim();
        strReturn = strReturn.replaceAll(" ", "");
      } 
    } catch (Exception e) {
      System.out.println(strError + e.getMessage());
    } 
    return strReturn;
  }

	public BigDecimal excelNumeric(Row p_Row, int p_Col) {
		String strError = "(GF) excelNumeric : ";
		BigDecimal bdcReturn = new BigDecimal(0);
		try {
			if (p_Row != null)
				if (p_Row.getCell(p_Col) != null)
					bdcReturn = new BigDecimal(p_Row.getCell(p_Col).getNumericCellValue());
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return bdcReturn;
	}

	public String excelDate(Row p_Row, int p_Col) {
		String strError = "(GF) excelDate : ";
		String strDate = "";
		try {
			if (p_Row != null && p_Row.getCell(p_Col) != null) {
				SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
				strDate = sdfDate.format(p_Row.getCell(p_Col).getDateCellValue());
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strDate;
	}

	public static Timestamp toDate(String p_Date) {
    String strError = "(GF) toDate : ";
    String strTemp = "";
    Timestamp timReturn = null;
    try {
      strTemp = left(p_Date, 10).trim().replaceAll(" ", "").replaceAll(" ", "").replaceAll("\\.", "-");
      if (!strTemp.equals("") && !strTemp.equals("--"))
        timReturn = Timestamp.valueOf(strTemp + " 00:00:00"); 
    } catch (Exception e) {
      System.out.println(strError + e.getMessage());
      timReturn = null;
    } 
    return timReturn;
  }

	public static BigDecimal toBigDecimal(String p_Number) {
		String strError = "(GF) toDate : ";
		BigDecimal bdcReturn = null;
		try {
			p_Number = p_Number.trim().replaceAll(" ", "");
			if (p_Number == null) {
				bdcReturn = new BigDecimal("0");
			} else if (p_Number.equals("") == true) {
				bdcReturn = new BigDecimal("0");
			} else {
				bdcReturn = new BigDecimal(p_Number);
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			bdcReturn = new BigDecimal("0");
		}
		return bdcReturn;
	}

	public static String left(String p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) left : ";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				int intLength = strReturn.length();
				if (p_Count > 0) {
					strReturn = strReturn.substring(0, (intLength < p_Count) ? intLength : p_Count);
				} else {
					strReturn = "";
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String left(int p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) left : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (p_Count > 0) {
				strReturn = strReturn.substring(0, (intLength < p_Count) ? intLength : p_Count);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String left(long p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) left : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (p_Count > 0) {
				strReturn = strReturn.substring(0, (intLength < p_Count) ? intLength : p_Count);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String left(BigDecimal p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) left : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (p_Count > 0) {
				strReturn = strReturn.substring(0, (intLength < p_Count) ? intLength : p_Count);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String right(String p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) right : ";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				int intLength = strReturn.length();
				if (p_Count > 0) {
					strReturn = (intLength < p_Count) ? strReturn : strReturn.substring(intLength - p_Count);
				} else {
					strReturn = "";
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String right(int p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) right : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (p_Count > 0) {
				strReturn = (intLength < p_Count) ? strReturn : strReturn.substring(intLength - p_Count);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String right(long p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) right : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (p_Count > 0) {
				strReturn = (intLength < p_Count) ? strReturn : strReturn.substring(intLength - p_Count);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String right(BigDecimal p_Original, int p_Count) {
		String strReturn = "";
		String strError = "(GF) right : ";
		try {
			if (p_Original != null) {
				strReturn = String.valueOf(p_Original);
				int intLength = strReturn.length();
				if (p_Count > 0) {
					strReturn = (intLength < p_Count) ? strReturn : strReturn.substring(intLength - p_Count);
				} else {
					strReturn = "";
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(String p_Original, int p_StartPosition, int p_Count) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				int intLength = strReturn.length();
				if (intLength < p_StartPosition || p_Count < 0) {
					strReturn = "";
				} else if (p_Count > intLength - p_StartPosition) {
					strReturn = strReturn.substring(p_StartPosition - 1);
				} else {
					strReturn = strReturn.substring(p_StartPosition - 1, p_StartPosition + p_Count - 1);
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(int p_Original, int p_StartPosition, int p_Count) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (intLength < p_StartPosition || p_Count < 0) {
				strReturn = "";
			} else if (p_Count > intLength - p_StartPosition) {
				strReturn = strReturn.substring(p_StartPosition - 1);
			} else {
				strReturn = strReturn.substring(p_StartPosition - 1, p_StartPosition + p_Count - 1);
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(long p_Original, int p_StartPosition, int p_Count) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (intLength < p_StartPosition || p_Count < 0) {
				strReturn = "";
			} else if (p_Count > intLength - p_StartPosition) {
				strReturn = strReturn.substring(p_StartPosition - 1);
			} else {
				strReturn = strReturn.substring(p_StartPosition - 1, p_StartPosition + p_Count - 1);
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(BigDecimal p_Original, int p_StartPosition, int p_Count) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			if (p_Original != null) {
				strReturn = String.valueOf(p_Original);
				int intLength = strReturn.length();
				if (intLength < p_StartPosition || p_Count < 0) {
					strReturn = "";
				} else if (p_Count > intLength - p_StartPosition) {
					strReturn = strReturn.substring(p_StartPosition - 1);
				} else {
					strReturn = strReturn.substring(p_StartPosition - 1, p_StartPosition + p_Count - 1);
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(String p_Original, int p_StartPosition) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				int intLength = strReturn.length();
				if (p_StartPosition > 0)
					strReturn = (intLength < p_StartPosition) ? "" : strReturn.substring(p_StartPosition - 1);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(int p_Original, int p_StartPosition) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (p_StartPosition > 0)
				strReturn = (intLength < p_StartPosition) ? "" : strReturn.substring(p_StartPosition - 1);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(long p_Original, int p_StartPosition) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			strReturn = String.valueOf(p_Original);
			int intLength = strReturn.length();
			if (p_StartPosition > 0)
				strReturn = (intLength < p_StartPosition) ? "" : strReturn.substring(p_StartPosition - 1);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String mid(BigDecimal p_Original, int p_StartPosition) {
		String strReturn = "";
		String strError = "(GF) mid : ";
		try {
			if (p_Original != null) {
				strReturn = String.valueOf(p_Original);
				int intLength = strReturn.length();
				if (p_StartPosition > 0)
					strReturn = (intLength < p_StartPosition) ? "" : strReturn.substring(p_StartPosition - 1);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String cutTitle(String p_Original, int p_Count) {
		String strError = "(GF) cutTitle : ";
		String strReturn = "";
		try {
			strReturn = p_Original;
			byte[] bytTemp = strReturn.getBytes();
			if (p_Count > 0) {
				if (p_Count < bytTemp.length) {
					int intCount = 0;
					for (int i = p_Count - 1; i >= 0; i--) {
						if ((bytTemp[i] & 0x80) != 0)
							intCount++;
					}
					strReturn = new String(bytTemp, 0, p_Count - intCount % 2);
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String removeBanWord(String p_Original) {
		String strError = "(GF) removeBanWord : ";
		String strReturn = "";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				strReturn = strReturn.replaceAll("'", "''");
				strReturn = strReturn.replaceAll("\r\n", "");
				strReturn = strReturn.replaceAll("\r", "");
				strReturn = strReturn.replaceAll("\n", "");
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String remove11stBanWord(String p_Original) {
    String strError = "(GF) remove11stBanWord : ";
    String strReturn = "";
    try {
      if (p_Original != null) {
        strReturn = p_Original;
        strReturn = strReturn.replaceAll("'", "");
        strReturn = strReturn.replaceAll("\"", "");
        strReturn = strReturn.replaceAll("%", "");
        strReturn = strReturn.replaceAll("&", "");
        strReturn = strReturn.replaceAll("<", "");
        strReturn = strReturn.replaceAll(">", "");
        strReturn = strReturn.replaceAll("#", "");
      } 
    } catch (Exception e) {
      System.out.println(strError + e.getMessage());
    } 
    return strReturn;
  }

	public static String recoverSQL(String p_Original) {
		String strError = "(GF) recoverSQL : ";
		String strReturn = "";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				strReturn = strReturn.replaceAll("'", "''");
				strReturn = strReturn.replaceAll("\r\n", "");
				strReturn = strReturn.replaceAll("\r", "");
				strReturn = strReturn.replaceAll("\n", "");
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverSQL_Quotation(String p_Original) {
		String strError = "(GF) recoverSQL_Quotation : ";
		String strReturn = "";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				strReturn = strReturn.replaceAll("'", "''");
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverLikeSQL(String p_Original) {
		String strError = "(GF) recoverLikeSQL : ";
		String strReturn = "";
		try {
			strReturn = p_Original;
			strReturn = strReturn.replaceAll("\\[", "\\[[]").replaceAll("_", "[_]").replaceAll("%", "[%]")
					.replaceAll("'", "''").replaceAll(" ", "_");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String removeQuotation(String p_Original) {
		String strError = "(GF) removeQuotation : ";
		String strReturn = "";
		try {
			strReturn = p_Original;
			strReturn = strReturn.replaceAll("\r\n", "");
			strReturn = strReturn.replaceAll("\r", "");
			strReturn = strReturn.replaceAll("\n", "");
			strReturn = strReturn.replaceAll("'", "\\\\'");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String removeDblQuotation(String p_Original) {
		String strError = "(GF) removeDblQuotation : ";
		String strReturn = "";
		try {
			strReturn = p_Original;
			strReturn = strReturn.replaceAll("\r\n", "");
			strReturn = strReturn.replaceAll("\r", "");
			strReturn = strReturn.replaceAll("\n", "");
			strReturn = strReturn.replaceAll("\"", "\\\\\"");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverETCWord(String p_Original) {
		String strError = "(GF) recoverETCWord : ";
		String strReturn = "";
		try {
			strReturn = p_Original;
			strReturn = strReturn.replaceAll("\r\n", "");
			strReturn = strReturn.replaceAll("\r", "");
			strReturn = strReturn.replaceAll("\n", "");
			strReturn = strReturn.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\\\'").replaceAll("\"",
					"\\\\\\\"");
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverToScript(String p_Original) {
		String strError = "(GF) recoverToScript : ";
		String strReturn = "";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				strReturn = strReturn.replaceAll("'", "\\\\\\\\\\\\'");
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverToJSON(String p_Original) {
		String strError = "(GF) recoverToJSON : ";
		String strReturn = "";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				strReturn = strReturn.replaceAll("&", "\\&").replaceAll("\"", "\\\\\\\"").replaceAll("\t", "")
						.replaceAll("\n", " ");
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverToHTML(String p_Original) {
		String strError = "(GF) recoverToHTML : ";
		String strReturn = "";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				strReturn = strReturn.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
						.replaceAll("\"", "&quot;");
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverToHTML_Quotation(String p_Original) {
		String strError = "(GF) recoverToHTML_Quotation : ";
		String strReturn = "";
		try {
			if (p_Original != null) {
				strReturn = p_Original;
				strReturn = strReturn.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
						.replaceAll("\"", "&quot;").replaceAll("'", "\\\\'");
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String recoverToHTML_Keyword(String p_Origin, String p_Keyword, String p_Color) {
		String strReturn, strError = "(GF) recoverToHTML_Keyword : ";
		try {
			if (p_Origin == null || p_Origin.equals("")) {
				strReturn = "";
			} else {
				int intStart = p_Origin.toUpperCase().indexOf(p_Keyword.toUpperCase());
				if (intStart >= 0) {
					strReturn = recoverToHTML(p_Origin.substring(0, intStart)) + "<span style='color:" + p_Color + "'>"
							+ recoverToHTML(p_Origin.substring(intStart, intStart + p_Keyword.length())) + "</span>"
							+ recoverToHTML(p_Origin.substring(intStart + p_Keyword.length()));
				} else {
					strReturn = p_Origin;
				}
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = "";
		}
		return strReturn;
	}

	public static String pickupImageURL(String p_Original) {
		String strError = "(GF) pickupImageURL : ";
		String strReturn = "";
		try {
			strReturn = p_Original.toUpperCase();
			int intStart = strReturn.indexOf("<IMG");
			if (intStart >= 0) {
				intStart += 4;
				intStart = strReturn.indexOf("SRC", intStart);
				intStart += 5;
				int intEnd = strReturn.indexOf("\"", intStart);
				strReturn = p_Original.substring(intStart, intEnd);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String getFullDomain(String p_URL) {
		String strError = "(GF) getFullDomain : ";
		String strReturn = "";
		try {
			strReturn = p_URL;
			int intStart = strReturn.indexOf("//") + 2;
			int intEnd = strReturn.indexOf(".") + strReturn.substring(strReturn.indexOf(".")).indexOf("/");
			strReturn = strReturn.substring(intStart, intEnd);
			if (strReturn.indexOf("/") > 0)
				strReturn = strReturn.substring(strReturn.indexOf("/") + 1);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String getFrontDomain(String p_URL) {
		String strError = "(GF) getFrontDomain : ";
		String strReturn = "";
		try {
			strReturn = p_URL;
			int intStart = strReturn.indexOf("//") + 2;
			int intEnd = strReturn.indexOf(".") + strReturn.substring(strReturn.indexOf(".")).indexOf("/");
			strReturn = strReturn.substring(intStart, intEnd);
			if (strReturn.indexOf("/") > 0) {
				strReturn = strReturn.substring(0, strReturn.indexOf("/"));
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String formatRemoveZero(BigDecimal p_Money) {
		String strError = "(GF) formatRemoveZero : ";
		String strTemp = "";
		try {
			if (p_Money == null) {
				strTemp = "0";
			} else if (p_Money.compareTo(new BigDecimal("0")) == 0) {
				strTemp = "0";
			} else {
				String strFront, strTail;
				strTemp = p_Money.toString();
				if (strTemp.indexOf(".") >= 0) {
					strFront = strTemp.substring(0, strTemp.indexOf("."));
					strTail = strTemp.substring(strTemp.indexOf(".") + 1);
					int intCNT = strTail.length();
					for (int i = 0; i < intCNT
							&& strTail.substring(strTail.length() - 1, strTail.length()).equals("0"); i++)
						strTail = strTail.substring(0, strTail.length() - 1);
					if (strTail.equals(""))
						strTail = "0";
				} else {
					strFront = strTemp;
					strTail = "0";
				}
				if (Integer.parseInt(strTail) == 0) {
					strTemp = strFront;
				} else if (strFront.equals("")) {
					strTemp = "0." + strTail;
				} else {
					strTemp = strFront + "." + strTail;
				}
			}
			return strTemp;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static BigDecimal roundMoney(BigDecimal p_Money, int p_Decimal) {
		String strError = "(GF) roundMoney : ";
		String strTemp = "0";
		try {
			if (p_Money != null && p_Money.compareTo(new BigDecimal("0")) != 0) {
				String strFront, strTail;
				strTemp = p_Money.toString();
				if (strTemp.indexOf(".") >= 0) {
					if (p_Decimal > 0) {
						strTemp = p_Money.multiply((new BigDecimal("10")).pow(p_Decimal)).toString();
						strTemp = strTemp.substring(0, strTemp.indexOf("."));
						strFront = left(strTemp, strTemp.length() - p_Decimal);
						strTail = right(strTemp, p_Decimal);
					} else if (p_Decimal == 0) {
						strTemp = strTemp.substring(0, strTemp.indexOf("."));
						strFront = strTemp;
						strTail = "0";
					} else {
						strTemp = strTemp.substring(0, strTemp.indexOf("."));
						strTemp = strTemp.substring(0, strTemp.length() + p_Decimal);
						for (int i = 0; i < -p_Decimal; i++)
							strTemp = strTemp + "0";
						strFront = strTemp;
						strTail = "0";
					}
				} else {
					strFront = strTemp;
					strTail = "0";
				}
				if (Integer.parseInt(strTail) == 0) {
					strTemp = strFront;
				} else if (strFront.equals("")) {
					strTemp = "0." + strTail;
				} else {
					strTemp = strFront + "." + strTail;
				}
			}
			return new BigDecimal(strTemp);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return new BigDecimal("0");
		}
	}

	public static BigDecimal roundMoney(BigDecimal p_Money, int p_Decimal, int p_RoundMethod) {
		String strError = "(GF) roundMoney : ";
		String strTemp = "0";
		try {
			if (p_Money != null && p_Money.compareTo(new BigDecimal("0")) != 0) {
				strTemp = p_Money.toString();
				BigDecimal bdcMoney = new BigDecimal(strTemp);
				if (p_Decimal > 0) {
					if (p_RoundMethod == 4) {
						bdcMoney = bdcMoney.setScale(p_Decimal - 1, p_RoundMethod);
					} else if (p_RoundMethod == 0) {
						bdcMoney = bdcMoney.setScale(p_Decimal - 1, p_RoundMethod);
					} else {
						bdcMoney = bdcMoney.setScale(p_Decimal - 1, 1);
					}
				} else if (p_Decimal < 0) {
					if (p_RoundMethod == 4) {
						bdcMoney = bdcMoney.divide((new BigDecimal("10")).pow(-p_Decimal), MathContext.DECIMAL32);
						bdcMoney = bdcMoney.setScale(0, p_RoundMethod);
					} else if (p_RoundMethod == 0) {
						bdcMoney = bdcMoney.divide((new BigDecimal("10")).pow(-p_Decimal), MathContext.DECIMAL32);
						bdcMoney = bdcMoney.setScale(0, p_RoundMethod);
					} else {
						bdcMoney = bdcMoney.divide((new BigDecimal("10")).pow(-p_Decimal), MathContext.DECIMAL32);
						bdcMoney = bdcMoney.setScale(0, 1);
					}
					bdcMoney = bdcMoney.multiply((new BigDecimal("10")).pow(-p_Decimal));
				}
				strTemp = bdcMoney.toString();
			}
			return new BigDecimal(strTemp);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return new BigDecimal("0");
		}
	}

	public static String formatMoney(int p_Money) {
		String strError = "(GF) formatMoney : ";
		try {
			DecimalFormat dcfNumber = new DecimalFormat("#,###");
			return dcfNumber.format(p_Money);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatMoney(long p_Money) {
		String strError = "(GF) formatMoney : ";
		try {
			DecimalFormat dcfNumber = new DecimalFormat("#,###");
			return dcfNumber.format(p_Money);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatMoney(BigDecimal p_Money) {
		String strError = "(GF) formatMoney : ";
		DecimalFormat dcfNumber = new DecimalFormat("#,###");
		String strTemp = "";
		try {
			if (p_Money == null) {
				strTemp = "0";
			} else if (p_Money.compareTo(new BigDecimal("0")) == 0) {
				strTemp = "0";
			} else {
				String strFront, strTail;
				strTemp = p_Money.toString();
				if (strTemp.indexOf(".") >= 0) {
					strFront = strTemp.substring(0, strTemp.indexOf("."));
					strTail = strTemp.substring(strTemp.indexOf(".") + 1);
					int intCNT = strTail.length();
					for (int i = 0; i < intCNT
							&& strTail.substring(strTail.length() - 1, strTail.length()).equals("0"); i++)
						strTail = strTail.substring(0, strTail.length() - 1);
					if (strTail.equals(""))
						strTail = "0";
				} else {
					strFront = strTemp;
					strTail = "0";
				}
				if (strTail.equals("0") == true) {
					strTemp = dcfNumber.format(new BigDecimal(strFront));
				} else if (strFront.equals("")) {
					strTemp = "0." + strTail;
				} else {
					strTemp = dcfNumber.format(new BigDecimal(strFront)) + "." + strTail;
				}
			}
			return strTemp;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatMoney(BigDecimal p_Money, int p_Decimal) {
		String strError = "(GF) formatMoney : ";
		DecimalFormat dcfNumber = new DecimalFormat("#,###");
		String strTemp = "";
		try {
			if (p_Money == null) {
				strTemp = "0";
				if (p_Decimal > 0) {
					strTemp = strTemp + ".";
					for (int i = 0; i < p_Decimal; i++)
						strTemp = strTemp + "0";
				}
			} else if (p_Money.compareTo(new BigDecimal("0")) == 0) {
				strTemp = "0";
				if (p_Decimal > 0) {
					strTemp = strTemp + ".";
					for (int i = 0; i < p_Decimal; i++)
						strTemp = strTemp + "0";
				}
			} else {
				String strFront, strTail;
				strTemp = p_Money.toString();
				if (strTemp.indexOf(".") >= 0) {
					if (p_Decimal > 0) {
						strTemp = p_Money.multiply((new BigDecimal("10")).pow(p_Decimal)).toString();
						if (strTemp.indexOf(".") > 0)
							strTemp = strTemp.substring(0, strTemp.indexOf("."));
						strTemp = (new BigDecimal(strTemp))
								.divide((new BigDecimal("10")).pow(p_Decimal), MathContext.DECIMAL32).toString();
						if (strTemp.indexOf(".") > 0) {
							strFront = strTemp.substring(0, strTemp.indexOf("."));
							strTail = strTemp.substring(strTemp.indexOf(".") + 1);
						} else {
							strFront = strTemp;
							strTail = "0";
						}
					} else if (p_Decimal == 0) {
						strTemp = strTemp.substring(0, strTemp.indexOf("."));
						strFront = strTemp;
						strTail = "0";
					} else {
						strTemp = strTemp.substring(0, strTemp.indexOf("."));
						strTemp = strTemp.substring(0, strTemp.length() + p_Decimal);
						for (int i = 0; i < -p_Decimal; i++)
							strTemp = strTemp + "0";
						strFront = strTemp;
						strTail = "0";
					}
				} else if (p_Decimal >= 0) {
					strFront = strTemp;
					strTail = "0";
				} else {
					strTemp = strTemp.substring(0, strTemp.length() + p_Decimal);
					for (int i = 0; i < -p_Decimal; i++)
						strTemp = strTemp + "0";
					strFront = strTemp;
					strTail = "0";
				}
				if (strTail.equals("0") == true) {
					strTemp = dcfNumber.format(new BigDecimal(strFront));
					if (p_Decimal > 0) {
						strTemp = strTemp + ".";
						for (int i = 0; i < p_Decimal; i++)
							strTemp = strTemp + "0";
					}
				} else if (strFront.equals("")) {
					strTemp = "0." + strTail;
					if (p_Decimal > 0 && strTail.length() < p_Decimal)
						for (int i = 0; i < p_Decimal - strTail.length(); i++)
							strTemp = strTemp + "0";
				} else {
					strTemp = dcfNumber.format(new BigDecimal(strFront)) + "." + strTail;
					if (p_Decimal > 0 && strTail.length() < p_Decimal)
						for (int i = 0; i < p_Decimal - strTail.length(); i++)
							strTemp = strTemp + "0";
				}
			}
			return strTemp;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatNumber(BigDecimal p_Money) {
		String strError = "(GF) formatNumber : ";
		String strTemp = "";
		try {
			if (p_Money == null) {
				strTemp = "0";
			} else if (p_Money.compareTo(new BigDecimal("0")) == 0) {
				strTemp = "0";
			} else {
				String strFront, strTail;
				strTemp = p_Money.toString();
				if (strTemp.indexOf(".") >= 0) {
					strFront = strTemp.substring(0, strTemp.indexOf("."));
					strTail = strTemp.substring(strTemp.indexOf(".") + 1);
					int intCNT = strTail.length();
					for (int i = 0; i < intCNT
							&& strTail.substring(strTail.length() - 1, strTail.length()).equals("0"); i++)
						strTail = strTail.substring(0, strTail.length() - 1);
					if (strTail.equals(""))
						strTail = "0";
				} else {
					strFront = strTemp;
					strTail = "0";
				}
				if (strTail.equals("0") == true) {
					strTemp = strFront;
				} else if (strFront.equals("")) {
					strTemp = "0." + strTail;
				} else {
					strTemp = strFront + "." + strTail;
				}
			}
			return strTemp;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatNumber(BigDecimal p_Money, int p_Decimal) {
		String strError = "(GF) formatNumber : ";
		String strTemp = "";
		try {
			String strFront;
			String strTail;
			if (p_Money == null) {
				strTemp = "0";
			} else if (p_Money.compareTo(new BigDecimal("0")) == 0) {
				strTemp = "0";
			} else {
				strTemp = p_Money.toString();
			}
			if (strTemp.indexOf(".") >= 0) {
				if (p_Decimal > 0) {
					strTemp = p_Money.multiply((new BigDecimal("10")).pow(p_Decimal)).toString();
					if (strTemp.indexOf(".") > 0)
						strTemp = strTemp.substring(0, strTemp.indexOf("."));
					strTemp = (new BigDecimal(strTemp))
							.divide((new BigDecimal("10")).pow(p_Decimal), MathContext.DECIMAL32).toString();
					if (strTemp.indexOf(".") > 0) {
						strFront = strTemp.substring(0, strTemp.indexOf("."));
						strTail = strTemp.substring(strTemp.indexOf(".") + 1);
					} else {
						strFront = strTemp;
						strTail = "0";
					}
				} else if (p_Decimal == 0) {
					strTemp = strTemp.substring(0, strTemp.indexOf("."));
					strFront = strTemp;
					strTail = "0";
				} else {
					strTemp = strTemp.substring(0, strTemp.indexOf("."));
					strTemp = strTemp.substring(0, strTemp.length() + p_Decimal);
					for (int i = 0; i < -p_Decimal; i++)
						strTemp = strTemp + "0";
					strFront = strTemp;
					strTail = "0";
				}
			} else if (p_Decimal >= 0) {
				strFront = strTemp;
				strTail = "0";
			} else {
				strTemp = strTemp.substring(0, strTemp.length() + p_Decimal);
				for (int i = 0; i < -p_Decimal; i++)
					strTemp = strTemp + "0";
				strFront = strTemp;
				strTail = "0";
			}
			if (strTail.equals("0") == true) {
				strTemp = strFront;
				if (p_Decimal > 0) {
					strTemp = strTemp + ".";
					for (int i = 0; i < p_Decimal; i++)
						strTemp = strTemp + "0";
				}
			} else if (strFront.equals("")) {
				strTemp = "0." + strTail;
				if (p_Decimal > 0 && strTail.length() < p_Decimal)
					for (int i = 0; i < p_Decimal - strTail.length(); i++)
						strTemp = strTemp + "0";
			} else {
				strTemp = strFront + "." + strTail;
				if (p_Decimal > 0 && strTail.length() < p_Decimal)
					for (int i = 0; i < p_Decimal - strTail.length(); i++)
						strTemp = strTemp + "0";
			}
			return strTemp;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatLastZero(BigDecimal p_Money, int p_Digit) {
		String strError = "(GF) formatNumber : ";
		String strTemp = "";
		try {
			if (p_Money == null) {
				strTemp = "0.";
				for (int i = 0; i < p_Digit; i++)
					strTemp = strTemp + "0";
			} else if (p_Money.compareTo(new BigDecimal("0")) == 0) {
				strTemp = "0.";
				for (int i = 0; i < p_Digit; i++)
					strTemp = strTemp + "0";
			} else {
				strTemp = p_Money.toString();
				String strFront = strTemp.substring(0, strTemp.indexOf(".") + 1);
				String strTail = strTemp.substring(strTemp.indexOf(".") + 1);
				int intTemp = Integer.parseInt(strTail);
				if (intTemp == 0) {
					strTemp = strFront;
					for (int i = 0; i < p_Digit; i++)
						strTemp = strTemp + "0";
				} else {
					for (int i = 0; i < p_Digit; i++)
						strTail = strTail + "0";
					strTemp = strFront + strTail.substring(0, p_Digit);
				}
			}
			return strTemp;
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatDate(Timestamp p_Time) {
		String strError = "(GF) formatDate : ";
		try {
			if (p_Time == null)
				return "";
			return String.valueOf(p_Time).substring(0, 10);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatDate(String p_Time) {
		String strError = "(GF) formatDate : ";
		String strReturn = "";
		try {
			strReturn = p_Time.replaceAll("-", "").replaceAll("/", "");
			if (strReturn.length() >= 8) {
				strReturn = strReturn.substring(0, 4) + "-" + strReturn.substring(4, 6) + "-"
						+ strReturn.substring(6, 8);
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			strReturn = "";
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static String formatDateTime(Timestamp p_Time) {
		String strError = "(GF) formatDateTime : ";
		try {
			if (p_Time == null)
				return "";
			return String.valueOf(p_Time).substring(0, 19);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatDateTime(String p_Time) {
		String strError = "(GF) formatDateTime : ";
		try {
			return p_Time.substring(0, 19);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatTime(Timestamp p_Time) {
		String strError = "(GF) formatTime : ";
		try {
			if (p_Time == null)
				return "";
			return String.valueOf(p_Time).substring(11, 19);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatTime(String p_Time) {
		String strError = "(GF) formatTime : ";
		try {
			return p_Time.substring(11, 19);
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			return "";
		}
	}

	public static String formatAlphaNumber(String p_String) {
		String strError = "(GF) formatAlphaNumber : ";
		StringBuffer strResult = new StringBuffer();
		String strTemp = "";
		try {
			if (p_String != null && !p_String.equals(""))
				for (int i = 0; i < p_String.length(); i++) {
					strTemp = p_String.substring(i, i + 1);
					if ("1234567890 abcdefghijklmnopqrstuvwxyz".indexOf(strTemp.toLowerCase()) >= 0)
						strResult.append(strTemp);
				}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strResult.toString();
	}

	public static int getDifferenceOfDate(Calendar p_StartDay, Calendar p_EndDay) {
		return (int) ((p_StartDay.getTimeInMillis() - p_EndDay.getTimeInMillis()) / 86400000L);
	}

	public static int getDifferenceOfHour(Calendar p_StartDay, Calendar p_EndDay) {
		return (int) ((p_StartDay.getTimeInMillis() - p_EndDay.getTimeInMillis()) / 3600000L);
	}

	public static boolean isNumber(String p_Number) {
		boolean result = true;
		String strCheck = p_Number;
		if (strCheck == null || strCheck.equals("")) {
			result = false;
		} else {
			strCheck = strCheck.replace(",", "");
			strCheck = strCheck.replace(".", "");
			if (strCheck.substring(0, 1).equals("-") == true)
				strCheck = strCheck.substring(1);
			for (int i = 0; i < strCheck.length(); i++) {
				char c = strCheck.charAt(i);
				if (c < '0' || c > ';') {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	public static boolean isAlpha(String p_Alpha) {
		boolean result = true;
		if (p_Alpha == null || p_Alpha.equals(""))
			result = false;
		for (int i = 0; i < p_Alpha.length(); i++) {
			char c = p_Alpha.toLowerCase().charAt(i);
			if (c < 'a' || c > 'z') {
				result = false;
				break;
			}
		}
		return result;
	}

	public static String NVL(String str) {
		return (str != null) ? str.trim() : "";
	}

	public static String nullcheck(String p_Data, String p_DefaultValue) throws Exception {
		String ReturnDefault = "";
		if (p_Data == null) {
			ReturnDefault = p_DefaultValue;
		} else if (p_Data == "") {
			ReturnDefault = p_DefaultValue;
		} else {
			ReturnDefault = p_Data;
		}
		return ReturnDefault;
	}

	public static String getSYSTime() {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(1);
    int month = calendar.get(2);
    int day = calendar.get(5);
    calendar.set(year, month, day);
    int dayofweek = calendar.get(7);
    Date time = calendar.getTime();
    SimpleDateFormat simpleformat2 = new SimpleDateFormat("yyyyMMdd", Locale.US);
    SimpleDateFormat simpleformat3 = new SimpleDateFormat("HHmmss", Locale.US);
    String sformat2 = simpleformat2.format(time);
    String sformat3 = simpleformat3.format(time);
    String hday = "";
    switch (dayofweek) {
      case 1:
        hday = "일";
        break;
      case 2:
        hday = "월";
        break;
      case 3:
        hday = "화";
        break;
      case 4:
        hday = "수";
        break;
      case 5:
        hday = "목";
        break;
      case 6:
        hday = "금";
        break;
      case 7:
        hday = "토";
        break;
    } 
    return sformat2 + sformat3 + dayofweek + hday;
  }

	public static String toMonthLater(String p_Date, int p_AddMonth) {
		Calendar calDate = Calendar.getInstance();
		String strDate = "";
		String toDate = "";
		if (p_Date != null && !p_Date.trim().equals("")) {
			strDate = p_Date.replace("-", "").trim();
			int intYear = Integer.parseInt(strDate.substring(0, 4));
			int intMonth = Integer.parseInt(strDate.substring(4, 6)) - 1;
			int intDay = Integer.parseInt(strDate.substring(6));
			calDate.set(intYear, intMonth, intDay);
			calDate.add(2, p_AddMonth);
			intYear = calDate.get(1);
			intMonth = calDate.get(2) + 1;
			intDay = calDate.get(5);
			toDate = Integer.toString(intYear) + "-"
					+ ((Integer.toString(intMonth).length() < 2) ? ("0" + Integer.toString(intMonth))
							: Integer.toString(intMonth))
					+ "-" + ((Integer.toString(intDay).length() < 2) ? ("0" + Integer.toString(intDay))
							: Integer.toString(intDay));
		} else {
			toDate = "";
		}
		return toDate;
	}

	public static String toDayLater(String p_Date, int p_AddDay) {
		Calendar calDate = Calendar.getInstance();
		String strDate = "";
		String toDate = "";
		if (p_Date != null && !p_Date.trim().equals("")) {
			strDate = p_Date.replace("-", "").trim();
			int intYear = Integer.parseInt(strDate.substring(0, 4));
			int intMonth = Integer.parseInt(strDate.substring(4, 6)) - 1;
			int intDay = Integer.parseInt(strDate.substring(6));
			calDate.set(intYear, intMonth, intDay);
			calDate.add(5, p_AddDay);
			intYear = calDate.get(1);
			intMonth = calDate.get(2) + 1;
			intDay = calDate.get(5);
			toDate = Integer.toString(intYear) + "-"
					+ ((Integer.toString(intMonth).length() < 2) ? ("0" + Integer.toString(intMonth))
							: Integer.toString(intMonth))
					+ "-" + ((Integer.toString(intDay).length() < 2) ? ("0" + Integer.toString(intDay))
							: Integer.toString(intDay));
		} else {
			toDate = "";
		}
		return toDate;
	}

	public static String toDayBefore(String p_Date, int p_AddDay) {
		Calendar calDate = Calendar.getInstance();
		String strDate = "";
		String toDate = "";
		if (p_Date != null && !p_Date.trim().equals("")) {
			strDate = p_Date.replace("-", "").trim();
			int intYear = Integer.parseInt(strDate.substring(0, 4));
			int intMonth = Integer.parseInt(strDate.substring(4, 6)) - 1;
			int intDay = Integer.parseInt(strDate.substring(6));
			calDate.set(intYear, intMonth, intDay);
			calDate.add(5, -p_AddDay);
			intYear = calDate.get(1);
			intMonth = calDate.get(2) + 1;
			intDay = calDate.get(5);
			toDate = Integer.toString(intYear) + "-"
					+ ((Integer.toString(intMonth).length() < 2) ? ("0" + Integer.toString(intMonth))
							: Integer.toString(intMonth))
					+ "-" + ((Integer.toString(intDay).length() < 2) ? ("0" + Integer.toString(intDay))
							: Integer.toString(intDay));
		} else {
			toDate = "";
		}
		return toDate;
	}

	public static String formatDateReplace(String date, String str) {
		String sformat = "";
		if (date != null && date.trim().length() == 8)
			sformat = date.substring(0, 4) + str + date.substring(4, 6) + str + date.substring(6);
		return sformat;
	}

	public static String NoComma(String str) {
		while (str.indexOf(",") != -1)
			str = str.substring(0, str.indexOf(",")) + str.substring(str.indexOf(",") + 1, str.length());
		return str;
	}

	public static String encodeImageURL(String p_URL) {
		String strError = "(GF) encodeImageURL : ";
		String strReturn = "";
		try {
			if (p_URL != null) {
				strReturn = p_URL;
				byte[] byteImage = strReturn.getBytes();
				for (int i = 0; i < byteImage.length; i++)
					byteImage[i] = (byte) (byteImage[i] - 9);
				byteImage = encodeBase64(byteImage);
				strReturn = new String(byteImage);
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return strReturn;
	}

	public static byte[] encodeBase64(byte[] DataByte) {
		int numberDataBit = DataByte.length * 8;
		int numberDataBitMODULO24 = numberDataBit % 24;
		int numberDataBitBY24 = numberDataBit / 24;
		byte[] EncodeData = null;
		int SIGN = -128;
		byte PADDING = 61;
		byte[] Encodebase64Alphabet = new byte[64];
		int i;
		for (i = 0; i <= 25; i++)
			Encodebase64Alphabet[i] = (byte) (65 + i);
		int j;
		for (i = 26, j = 0; i <= 51; i++, j++)
			Encodebase64Alphabet[i] = (byte) (97 + j);
		for (i = 52, j = 0; i <= 61; i++, j++)
			Encodebase64Alphabet[i] = (byte) (48 + j);
		Encodebase64Alphabet[62] = 43;
		Encodebase64Alphabet[63] = 47;
		if (numberDataBitMODULO24 == 0) {
			EncodeData = new byte[numberDataBitBY24 * 4];
		} else {
			EncodeData = new byte[(numberDataBitBY24 + 1) * 4];
		}
		int k;
		for (k = 0; k < numberDataBitBY24; k++) {
			int m = 3 * k;
			int n = 4 * k;
			byte b1 = DataByte[m];
			byte b2 = DataByte[m + 1];
			byte b3 = DataByte[m + 2];
			byte Rshift2_b1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) (b1 >>> 2);
			byte High_b2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) (b2 >>> 4);
			byte Rshift6_b3 = ((b2 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) (b3 >>> 6);
			byte e2 = (byte) ((b1 & 0x3) << 4 | High_b2);
			byte e3 = (byte) ((b2 & 0xF) << 2 | Rshift6_b3);
			byte e4 = (byte) (b3 & 0x3F);
			EncodeData[n] = Encodebase64Alphabet[Rshift2_b1];
			EncodeData[n + 1] = Encodebase64Alphabet[e2];
			EncodeData[n + 2] = Encodebase64Alphabet[e3];
			EncodeData[n + 3] = Encodebase64Alphabet[e4];
		}
		int IndexDataByte = 3 * k;
		int IndexEncodeData = 4 * k;
		if (numberDataBitMODULO24 == 16) {
			byte b1 = DataByte[IndexDataByte];
			byte b2 = DataByte[IndexDataByte + 1];
			byte Rshift2_b1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) (b1 >>> 2);
			byte High_b2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) (b2 >>> 4);
			byte e2 = (byte) ((b1 & 0x3) << 4 | High_b2);
			byte e3 = (byte) ((b2 & 0xF) << 2);
			byte e4 = PADDING;
			EncodeData[IndexEncodeData] = Encodebase64Alphabet[Rshift2_b1];
			EncodeData[IndexEncodeData + 1] = Encodebase64Alphabet[e2];
			EncodeData[IndexEncodeData + 2] = Encodebase64Alphabet[e3];
			EncodeData[IndexEncodeData + 3] = e4;
		} else if (numberDataBitMODULO24 == 8) {
			byte b1 = DataByte[IndexDataByte];
			byte Rshift2_b1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) (b1 >>> 2);
			byte e2 = (byte) ((b1 & 0x3) << 4);
			byte e3 = PADDING;
			byte e4 = PADDING;
			EncodeData[IndexEncodeData] = Encodebase64Alphabet[Rshift2_b1];
			EncodeData[IndexEncodeData + 1] = Encodebase64Alphabet[e2];
			EncodeData[IndexEncodeData + 2] = e3;
			EncodeData[IndexEncodeData + 3] = e4;
		}
		return EncodeData;
	}

	public static String encodeMD5(String p_Origin) {
		String strReturn = "";
		byte[] bResult = null;
		if (p_Origin != null)
			try {
				bResult = MessageDigest.getInstance("MD5").digest(p_Origin.getBytes());
				StringBuffer sbTemp = new StringBuffer();
				for (int i = 0; i < bResult.length; i++) {
					sbTemp.append(Integer.toString((bResult[i] & 0xF0) >> 4, 16));
					sbTemp.append(Integer.toString(bResult[i] & 0xF, 16));
				}
				strReturn = sbTemp.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				strReturn = "";
			}
		return strReturn;
	}

	public static String cutOff(String p_str, int p_len) {
		if (p_str != null && !"".equals(p_str)) {
			boolean chkFlag = false;
			String strName = p_str.trim();
			byte[] arName = strName.getBytes();
			String p_tailstr = "...";
			if (arName.length > p_len) {
				for (int idx = 0; idx < p_len; idx++) {
					if (arName[idx] < 0) {
						chkFlag = !chkFlag;
					} else {
						chkFlag = false;
					}
				}
				if (chkFlag) {
					strName = new String(arName, 0, p_len + 1) + p_tailstr;
				} else {
					strName = new String(arName, 0, p_len) + p_tailstr;
				}
			} else {
				strName = new String(arName, 0, arName.length);
			}
			return strName;
		}
		return p_str;
	}

	public static String replaceKorean(String p_Original) {
		int i = 0, intCount = 0;
		String strEnglish = "";
		intCount = p_Original.length();
		strEnglish = p_Original;
		for (i = 0; i < intCount; i++) {
			char c = p_Original.charAt(i);
			if (IsLeadByte(c))
				strEnglish.replace(c, '%');
		}
		return strEnglish;
	}

	public static boolean IsLeadByte(char c) {
		return (Character.MIN_VALUE > c || '' < c);
	}

	public static String replaceAlpha(String p_Alpha) {
		String strEnglish = "";
		for (int i = 0; i < p_Alpha.length(); i++) {
			char c = p_Alpha.toLowerCase().charAt(i);
			if (c >= 'a' && c <= 'z') {
				strEnglish = strEnglish + c;
			} else if (c == ' ') {
				strEnglish = strEnglish + " ";
			} else if (c == '-') {
				strEnglish = strEnglish + "-";
			} else if (c == '(') {
				strEnglish = strEnglish + "(";
			} else if (c == ')') {
				strEnglish = strEnglish + ")";
			} else if (c == '/') {
				strEnglish = strEnglish + "/";
			} else if (c >= '0' && c <= '9') {
				strEnglish = strEnglish + c;
			}
		}
		return strEnglish;
	}

	public static String quoted(String in) {
		return "\"" + in.replace("\"", "\"\"") + "\"";
	}

	public static String sqlPaging(StringBuffer p_Select, StringBuffer p_From, StringBuffer p_Where,
			String p_PrimaryKeyTable, ArrayList<String> p_PrimaryKeyColumn, ArrayList<String> p_PrimaryKeyDataType,
			ArrayList<String> p_PrimaryKeyOrder, ArrayList<String> p_OrderByTable, ArrayList<String> p_OrderByColumn,
			ArrayList<String> p_OrderByDataType, ArrayList<String> p_OrderByOrder, int p_ShowCNT, int p_Page,
			boolean p_ExcuteYN) {
		String strSQL = "", strError = "(GF) sqlPaging : ";
		int intTop = -1;
		if (p_ShowCNT > 0 && p_Page > 0)
			intTop = p_ShowCNT * p_Page;
		StringBuffer sbSQL = new StringBuffer();
		StringBuffer strOrderBy = new StringBuffer(), strOrderByReverse = new StringBuffer();
		StringBuffer strKeyTable = new StringBuffer(), str1 = new StringBuffer(), str2 = new StringBuffer(),
				str3 = new StringBuffer(), str4 = new StringBuffer();
		boolean blnPrimaryKeyYN = false;
		try {
			strOrderBy.append("\nORDER BY " + (String) p_OrderByColumn.get(0) + " " + (String) p_OrderByOrder.get(0));
			int i;
			for (i = 1; i < p_OrderByColumn.size(); i++)
				strOrderBy.append("," + (String) p_OrderByColumn.get(i) + " " + (String) p_OrderByOrder.get(i));
			if (((String) p_OrderByOrder.get(0)).toUpperCase().equals("ASC") == true) {
				strOrderByReverse.append("\nORDER BY " + (String) p_OrderByColumn.get(0) + " DESC");
			} else {
				strOrderByReverse.append("\nORDER BY " + (String) p_OrderByColumn.get(0) + " ASC");
			}
			for (i = 1; i < p_OrderByColumn.size(); i++) {
				if (((String) p_OrderByOrder.get(i)).toUpperCase().equals("ASC") == true) {
					strOrderByReverse.append("," + (String) p_OrderByColumn.get(i) + " DESC");
				} else {
					strOrderByReverse.append("," + (String) p_OrderByColumn.get(i) + " ASC");
				}
			}
			strKeyTable.append("DECLARE @KeyTable Table (");
			strKeyTable.append(
					(String) p_PrimaryKeyColumn.get(0) + " " + (String) p_PrimaryKeyDataType.get(0) + " NOT NULL");
			for (i = 1; i < p_PrimaryKeyColumn.size(); i++)
				strKeyTable.append("," + (String) p_PrimaryKeyColumn.get(i) + " " + (String) p_PrimaryKeyDataType.get(i)
						+ " NOT NULL");
			for (i = 0; i < p_OrderByColumn.size(); i++) {
				blnPrimaryKeyYN = false;
				for (int j = 0; j < p_PrimaryKeyColumn.size(); j++) {
					if (((String) p_PrimaryKeyColumn.get(j)).equals(p_OrderByColumn.get(i)) == true) {
						blnPrimaryKeyYN = true;
						break;
					}
				}
				if (!blnPrimaryKeyYN)
					strKeyTable.append("," + (String) p_OrderByColumn.get(i) + " " + (String) p_OrderByDataType.get(i));
			}
			strKeyTable.append(",PRIMARY KEY CLUSTERED (" + (String) p_PrimaryKeyColumn.get(0) + " "
					+ (String) p_PrimaryKeyOrder.get(0));
			for (i = 1; i < p_PrimaryKeyColumn.size(); i++)
				strKeyTable.append("," + (String) p_PrimaryKeyColumn.get(i) + " " + (String) p_PrimaryKeyOrder.get(i));
			strKeyTable.append(") )");
			strKeyTable.append("INSERT INTO @KeyTable");
			strKeyTable.append("\nSELECT " + p_PrimaryKeyTable + "." + (String) p_PrimaryKeyColumn.get(0));
			for (i = 1; i < p_PrimaryKeyColumn.size(); i++)
				strKeyTable.append("," + p_PrimaryKeyTable + "." + (String) p_PrimaryKeyColumn.get(i));
			for (i = 0; i < p_OrderByColumn.size(); i++) {
				blnPrimaryKeyYN = false;
				for (int j = 0; j < p_PrimaryKeyColumn.size(); j++) {
					if (((String) p_PrimaryKeyColumn.get(j)).equals(p_OrderByColumn.get(i)) == true) {
						blnPrimaryKeyYN = true;
						break;
					}
				}
				if (!blnPrimaryKeyYN)
					strKeyTable.append("," + (String) p_OrderByTable.get(i) + "." + (String) p_OrderByColumn.get(i));
			}
			strKeyTable.append("" + p_From + p_Where + ";");
			strKeyTable.append("\nDECLARE @TotalCNT bigint = (SELECT COUNT(*) FROM @KeyTable);");
			strKeyTable.append("\nDECLARE @TopCNT bigint = @TotalCNT;");
			strKeyTable.append("\nDECLARE @ShowCNT bigint = @TotalCNT;");
			if (intTop > 0) {
				strKeyTable.append("\nSET @TopCNT = (CASE WHEN " + intTop + " > @TotalCNT THEN @TotalCNT ELSE " + intTop
						+ " END);");
				strKeyTable.append("\nSET @ShowCNT = (CASE WHEN " + intTop + " > @TotalCNT THEN @TotalCNT - ("
						+ (p_ShowCNT * (p_Page - 1)) + ") ELSE " + p_ShowCNT + " END);");
			}
			str1.append("SELECT TOP (@TopCNT) " + (String) p_PrimaryKeyColumn.get(0));
			for (i = 1; i < p_PrimaryKeyColumn.size(); i++)
				str1.append("," + (String) p_PrimaryKeyColumn.get(i));
			for (i = 0; i < p_OrderByColumn.size(); i++) {
				blnPrimaryKeyYN = false;
				for (int j = 0; j < p_PrimaryKeyColumn.size(); j++) {
					if (((String) p_PrimaryKeyColumn.get(j)).equals(p_OrderByColumn.get(i)) == true) {
						blnPrimaryKeyYN = true;
						break;
					}
				}
				if (!blnPrimaryKeyYN)
					str1.append("," + (String) p_OrderByColumn.get(i));
			}
			str1.append(" FROM @KeyTable as " + p_PrimaryKeyTable + strOrderBy);
			str2.append(" SELECT TOP (@ShowCNT) " + p_PrimaryKeyTable + "." + (String) p_PrimaryKeyColumn.get(0));
			for (i = 1; i < p_PrimaryKeyColumn.size(); i++)
				str2.append("," + p_PrimaryKeyTable + "." + (String) p_PrimaryKeyColumn.get(i));
			str2.append("\nFROM (" + str1 + ") as " + p_PrimaryKeyTable + strOrderByReverse);
			str3.append(p_Select);
			str3.append(p_From);
			str3.append("\nINNER JOIN (" + str2 + ") as keyTable ON ");
			str3.append("(keyTable." + (String) p_PrimaryKeyColumn.get(0) + "=" + p_PrimaryKeyTable + "."
					+ (String) p_PrimaryKeyColumn.get(0) + ")");
			for (i = 1; i < p_PrimaryKeyColumn.size(); i++)
				str3.append(" AND (keyTable." + (String) p_PrimaryKeyColumn.get(i) + "=" + p_PrimaryKeyTable + "."
						+ (String) p_PrimaryKeyColumn.get(i) + ")");
			str4.append("\nSELECT *, (@TotalCNT) as SearchResultCNT FROM (" + str3 + ") as " + p_PrimaryKeyTable
					+ strOrderBy);
			sbSQL.append(strKeyTable);
			sbSQL.append(str4);
			if (p_ExcuteYN == true) {
				strSQL = "EXEC SP_EXCUTE_SELECT_SQL '" + recoverSQL(sbSQL.toString().replaceAll("\n", " ")) + "'";
			} else {
				strSQL = sbSQL.toString();
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strSQL = "";
		}
		return strSQL;
	}

	public static Timestamp now() {
		Calendar cal = Calendar.getInstance();
		return Timestamp.valueOf(cal.get(1) + "-" + right("0" + (cal.get(2) + 1), 2) + "-" + right("0" + cal.get(5), 2)
				+ " " + right("0" + cal.get(11), 2) + ":" + right("0" + cal.get(12), 2) + ":"
				+ right("0" + cal.get(13), 2));
	}

	public static String formatCellPhoneNO(String p_CellPhoneNO) {
		String strError = "(GF) formatCellPhoneNO : ";
		String strReturn = "";
		try {
			if (p_CellPhoneNO != null) {
				strReturn = p_CellPhoneNO.replaceAll(" ", "").replaceAll("-", "");
				if (strReturn.length() >= 10) {
					if (left(strReturn, 3).equals("+82")) {
						strReturn = strReturn.substring(3);
						if (!left(strReturn, 2).equals("01"))
							strReturn = "0" + strReturn;
					} else if (left(strReturn, 2).equals("82")) {
						strReturn = strReturn.substring(2);
						if (!left(strReturn, 2).equals("01"))
							strReturn = "0" + strReturn;
					}
					if (left(strReturn, 3).equals("010") || left(strReturn, 3).equals("011")
							|| left(strReturn, 3).equals("016") || left(strReturn, 3).equals("017")
							|| left(strReturn, 3).equals("018") || left(strReturn, 3).equals("019")) {
						if (left(strReturn, 3).equals("010") == true && strReturn.length() != 11)
							strReturn = "";
					} else {
						strReturn = "";
					}
					if (isNumber(strReturn) == true) {
						if (strReturn.length() == 10) {
							strReturn = left(strReturn, 3) + "-" + mid(strReturn, 4, 3) + "-" + right(strReturn, 4);
						} else if (strReturn.length() == 11) {
							strReturn = left(strReturn, 3) + "-" + mid(strReturn, 4, 4) + "-" + right(strReturn, 4);
						} else {
							strReturn = "";
						}
					} else {
						strReturn = "";
					}
				} else {
					strReturn = "";
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = "";
		}
		return strReturn;
	}

	public static String formatEmail(String p_Email) {
		String strError = "(GF) formatEmail : ";
		String strReturn = "";
		try {
			if (p_Email != null) {
				strReturn = p_Email.replaceAll(" ", "");
				if (strReturn.indexOf("@") > 0) {
					if (strReturn.lastIndexOf(".") > 2) {
						if (strReturn.length() <= strReturn.lastIndexOf(".") + 1)
							strReturn = "";
					} else {
						strReturn = "";
					}
				} else {
					strReturn = "";
				}
			} else {
				strReturn = "";
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
			strReturn = "";
		}
		return strReturn;
	}

	public static String addTime(String p_Date, int p_AddType, int p_AddTime) {
		Calendar calDate = Calendar.getInstance();
		String strReturn = "";
		if (p_Date != null && !p_Date.trim().equals("")) {
			strReturn = p_Date.replaceAll("-", "").replaceAll("\\.", "").replaceAll("/", "").replaceAll(":", "")
					.replaceAll(" ", "");
			if (strReturn.length() == 8)
				strReturn = strReturn + "000000";
			strReturn = left(strReturn, 14);
			if (strReturn.length() == 14) {
				int intYear = Integer.parseInt(strReturn.substring(0, 4));
				int intMonth = Integer.parseInt(strReturn.substring(4, 6)) - 1;
				int intDay = Integer.parseInt(strReturn.substring(6, 8));
				int intHour = Integer.parseInt(strReturn.substring(8, 10));
				int intMinute = Integer.parseInt(strReturn.substring(10, 12));
				int intSecond = Integer.parseInt(strReturn.substring(12, 14));
				calDate.set(intYear, intMonth, intDay, intHour, intMinute, intSecond);
				calDate.add(p_AddType, p_AddTime);
				intYear = calDate.get(1);
				intMonth = calDate.get(2) + 1;
				intDay = calDate.get(5);
				intHour = calDate.get(11);
				intMinute = calDate.get(12);
				intSecond = calDate.get(13);
				strReturn = Integer.toString(intYear) + "-" + right("0" + intMonth, 2) + "-" + right("0" + intDay, 2)
						+ " " + right("0" + intHour, 2) + ":" + right("0" + intMinute, 2) + ":"
						+ right("0" + intSecond, 2);
			} else {
				strReturn = "";
			}
		} else {
			strReturn = "";
		}
		return strReturn;
	}

	public static String addTime_Year(String p_Date, int p_AddTime) {
		return addTime(p_Date, 1, p_AddTime);
	}

	public static String addTime_Month(String p_Date, int p_AddTime) {
		return addTime(p_Date, 2, p_AddTime);
	}

	public static String addTime_Day(String p_Date, int p_AddTime) {
		return addTime(p_Date, 5, p_AddTime);
	}

	public static String addTime_Second(String p_Date, int p_AddTime) {
		return addTime(p_Date, 13, p_AddTime);
	}
}
