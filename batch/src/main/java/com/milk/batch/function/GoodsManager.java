package com.milk.batch.function;

import com.milk.batch.bean.GoodsBean;
import com.milk.batch.bean.GoodsHobBean;
import com.milk.batch.bean.GoodsOptionBean;
import com.milk.batch.bean.TeamPersonBean;
import com.milk.batch.common.DBManager;
import com.milk.batch.common.GF;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class GoodsManager {
  private Connection FV_conDB;
  
  private boolean FV_blnCloseConnectYN = false;
  
  public GoodsManager(Connection p_Connection) {
    if (p_Connection == null) {
      this.FV_blnCloseConnectYN = true;
    } else {
      this.FV_conDB = p_Connection;
    } 
  }
  
  public GoodsManager() {}
  
  public void finalize() {
    if (this.FV_blnCloseConnectYN)
      DBManager.closeDB(this.FV_conDB); 
  }
  
  public ArrayList<GoodsBean> findGoodsList(GoodsBean p_Param) {
    String strError = "(GoodsManager) findGoodsList 에러 : ";
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList<GoodsBean> arrReturn = new ArrayList<>();
    GoodsBean clsReturn = null;
    try {
      String strSQL = "SELECT COUNT(*) as TotalCNT FROM tc_goods";
      String strWhere = "";
      if (!p_Param.GoodsNM.equals(""))
        strWhere = strWhere + " AND (tc_goods.GoodsNM like '%" + GF.recoverSQL(p_Param.GoodsNM) + "%')"; 
      if (p_Param.GoodsCD != 0L)
        strWhere = strWhere + " AND (tc_goods.GoodsCD like '%" + p_Param.GoodsCD + "%')"; 
      if (p_Param.ParentCategoryCD != 0L)
        strWhere = strWhere + " AND (tc_goods.GoodsCD IN (SELECT DISTINCT(tc_goodscategory.GoodsCD) FROM tc_goodscategory INNER JOIN tc_category ON (tc_category.CategoryCD = tc_goodscategory.CategoryCD) WHERE (tc_category.ParentCategoryCD=" + p_Param.ParentCategoryCD + ") OR (tc_category.CategoryCD=" + p_Param.ParentCategoryCD + ")))"; 
      if (!strWhere.equals(""))
        strWhere = strWhere.replaceFirst(" AND", " WHERE"); 
      strSQL = strSQL + strWhere;
      stmt = this.FV_conDB.createStatement(1004, 1007);
      rs = stmt.executeQuery(strSQL);
      if (rs.last() == true) {
        rs.first();
        p_Param.Search_TotalCNT = rs.getInt("TotalCNT");
      } 
      DBManager.rsClose(rs);
      if (p_Param.Search_TotalCNT > 0) {
        p_Param.Search_TotalPage = (int)Math.ceil(p_Param.Search_TotalCNT / p_Param.Search_ShowCNT);
      } else {
        p_Param.Search_TotalPage = 0;
      } 
      if (p_Param.Search_Page > p_Param.Search_TotalPage)
        p_Param.Search_Page = p_Param.Search_TotalPage; 
      int Start = p_Param.Search_ShowCNT * p_Param.Search_Page - p_Param.Search_ShowCNT;
      if (Start < 0)
        Start = 0; 
      strSQL = "SELECT tc_goods.* FROM tc_goods";
      strSQL = strSQL + strWhere;
      if (!p_Param.Search_OrderCol.equals(""))
        strSQL = strSQL + " ORDER BY " + p_Param.Search_OrderCol + " " + p_Param.Search_OrderBy; 
      if (p_Param.Search_ShowCNT > 0)
        strSQL = strSQL + " LIMIT " + Start + "," + p_Param.Search_ShowCNT; 
      rs = stmt.executeQuery(strSQL);
      if (rs.last() == true) {
        int intMaxRow = rs.getRow();
        rs.first();
        for (int i = 0; i < intMaxRow; i++) {
          clsReturn = new GoodsBean();
          clsReturn = setAllGoodsField(rs);
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
  
  public GoodsBean setAllGoodsField(ResultSet p_RS) {
    GoodsBean clsReturn;
    String strError = "(GoodsManager) setAllGoodsField 에러 : ";
    try {
      clsReturn = new GoodsBean();
      clsReturn.GoodsCD = p_RS.getLong("GoodsCD");
      clsReturn.GoodsNM = (p_RS.getString("GoodsNM") == null) ? "" : p_RS.getString("GoodsNM");
      clsReturn.GoodsImg = (p_RS.getString("GoodsImg") == null) ? "" : p_RS.getString("GoodsImg");
      clsReturn.GoodsIntro = (p_RS.getString("GoodsIntro") == null) ? "" : p_RS.getString("GoodsIntro");
      clsReturn.GoodsType = p_RS.getInt("GoodsType");
      clsReturn.Standard = (p_RS.getString("Standard") == null) ? "" : p_RS.getString("Standard");
      clsReturn.ShelfLife = (p_RS.getString("ShelfLife") == null) ? "" : p_RS.getString("ShelfLife");
      clsReturn.Ingredient = (p_RS.getString("Ingredient") == null) ? "" : p_RS.getString("Ingredient");
      clsReturn.ContentImg = (p_RS.getString("ContentImg") == null) ? "" : p_RS.getString("ContentImg");
      clsReturn.NutritionFacts = (p_RS.getString("NutritionFacts") == null) ? "" : p_RS.getString("NutritionFacts");
    } catch (Exception e) {
      System.out.println(strError + e.getMessage());
      clsReturn = null;
    } 
    return clsReturn;
  }
  
  public ArrayList<GoodsOptionBean> findGoodsOption() {
    String strError = "(GoodsManager) findGoodsOption 에러 : ";
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList<GoodsOptionBean> arrReturn = new ArrayList<>();
    try {
      String strSQL = "SELECT tc_goodsoption.* FROM ysc.tc_goodsoption";
      stmt = this.FV_conDB.createStatement(1004, 1007);
      rs = stmt.executeQuery(strSQL);
      if (rs.last() == true) {
        int intMaxRow = rs.getRow();
        rs.first();
        for (int i = 0; i < intMaxRow; i++) {
          GoodsOptionBean clsReturn = setAllField(rs);
          arrReturn.add(clsReturn);
          rs.next();
        } 
      } 
    } catch (SQLException e) {
      System.out.println(strError + e.getMessage());
      arrReturn = new ArrayList<>();
    } finally {
      DBManager.rsClose(rs);
      DBManager.stmtClose(stmt);
    } 
    return arrReturn;
  }
  
  public GoodsOptionBean setAllField(ResultSet p_RS) {
    GoodsOptionBean clsReturn;
    String strError = "(GoodsManager) setAllField 에러 : ";
    try {
      clsReturn = new GoodsOptionBean();
      clsReturn.GoodsCD = p_RS.getLong("GoodsCD");
      clsReturn.GoodsOptionCD = p_RS.getLong("GoodsOptionCD");
      clsReturn.OptionNM = p_RS.getString("OptionNM");
      clsReturn.UnitPrice = (p_RS.getBigDecimal("UnitPrice") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("UnitPrice");
      clsReturn.Capacity = (p_RS.getBigDecimal("Capacity") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("Capacity");
      clsReturn.DeleteYN = p_RS.getInt("DeleteYN");
      clsReturn.MergeCD = p_RS.getInt("MergeCD");
      clsReturn.AliasNM = (p_RS.getString("AliasNM") == null) ? "" : p_RS.getString("AliasNM");
      clsReturn.MisCD = (p_RS.getString("MisCD") == null) ? "" : p_RS.getString("MisCD");
      clsReturn.Day1 = (p_RS.getBigDecimal("Day1") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("Day1");
      clsReturn.Day2 = (p_RS.getBigDecimal("Day2") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("Day2");
      clsReturn.Day3 = (p_RS.getBigDecimal("Day3") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("Day3");
      clsReturn.Day4 = (p_RS.getBigDecimal("Day4") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("Day4");
      clsReturn.Day5 = (p_RS.getBigDecimal("Day5") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("Day5");
      clsReturn.Day6 = (p_RS.getBigDecimal("Day6") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("Day6");
    } catch (Exception e) {
      System.out.println(strError + e.getMessage());
      clsReturn = null;
    } 
    return clsReturn;
  }
  
  public ArrayList<GoodsHobBean> findGoodsHob() {
    String strError = "(GoodsManager) findGoodsHob 에러 : ";
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList<GoodsHobBean> arrReturn = new ArrayList<>();
    try {
      String strSQL = "SELECT tc_goodshob.* FROM tc_goodshob";
      stmt = this.FV_conDB.createStatement(1004, 1007);
      rs = stmt.executeQuery(strSQL);
      if (rs.last() == true) {
        int intMaxRow = rs.getRow();
        rs.first();
        for (int i = 0; i < intMaxRow; i++) {
          GoodsHobBean clsReturn = setAllField_GoodsHob(rs);
          arrReturn.add(clsReturn);
          rs.next();
        } 
      } 
    } catch (SQLException e) {
      System.out.println(strError + e.getMessage());
      arrReturn = new ArrayList<>();
    } finally {
      DBManager.rsClose(rs);
      DBManager.stmtClose(stmt);
    } 
    return arrReturn;
  }
  
  public ArrayList<GoodsHobBean> findGoodsHob(String p_MergeType) {
    String strError = "(GoodsManager) findGoodsHob 에러 : ";
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList<GoodsHobBean> arrReturn = new ArrayList<>();
    try {
      String strSQL = "SELECT tc_goodshob.* FROM tc_goodshob WHERE MergeType IN (" + p_MergeType + ")";
      stmt = this.FV_conDB.createStatement(1004, 1007);
      rs = stmt.executeQuery(strSQL);
      if (rs.last() == true) {
        int intMaxRow = rs.getRow();
        rs.first();
        for (int i = 0; i < intMaxRow; i++) {
          GoodsHobBean clsReturn = setAllField_GoodsHob(rs);
          arrReturn.add(clsReturn);
          rs.next();
        } 
      } 
    } catch (SQLException e) {
      System.out.println(strError + e.getMessage());
      arrReturn = new ArrayList<>();
    } finally {
      DBManager.rsClose(rs);
      DBManager.stmtClose(stmt);
    } 
    return arrReturn;
  }
  
  public GoodsHobBean setAllField_GoodsHob(ResultSet p_RS) {
    GoodsHobBean clsReturn;
    String strError = "(GoodsManager) setAllField_GoodsHob 에러 : ";
    try {
      clsReturn = new GoodsHobBean();
      clsReturn.MergeSEQ = p_RS.getLong("MergeSEQ");
      clsReturn.MergeCD = p_RS.getInt("MergeCD");
      clsReturn.MergeType = p_RS.getString("MergeType");
      clsReturn.GroupNM = p_RS.getString("GroupNM");
      clsReturn.MergeNM = p_RS.getString("MergeNM");
      clsReturn.WeekQty1 = (p_RS.getBigDecimal("WeekQty1") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("WeekQty1");
      clsReturn.WeekQty2 = (p_RS.getBigDecimal("WeekQty2") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("WeekQty2");
      clsReturn.WeekQty3 = (p_RS.getBigDecimal("WeekQty3") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("WeekQty3");
      clsReturn.WeekQty4 = (p_RS.getBigDecimal("WeekQty4") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("WeekQty4");
      clsReturn.WeekQty5 = (p_RS.getBigDecimal("WeekQty5") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("WeekQty5");
      clsReturn.WeekQty6 = (p_RS.getBigDecimal("WeekQty6") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("WeekQty6");
      clsReturn.WeekQty7 = (p_RS.getBigDecimal("WeekQty7") == null) ? new BigDecimal("0") : p_RS.getBigDecimal("WeekQty7");
      clsReturn.DefaultQty = p_RS.getInt("DefaultQty");
    } catch (Exception e) {
      System.out.println(strError + e.getMessage());
      clsReturn = null;
    } 
    return clsReturn;
  }
  
  public ArrayList<GoodsOptionBean> findGoodsOption_StaffSales() {
    String strError = "(GoodsManager) findGoodsOption_StaffSales 에러 : ";
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList<GoodsOptionBean> arrList = new ArrayList<>();
    GoodsOptionBean clsGoodsOption = null;
    try {
      String strSQL = "SELECT tc_goodsoption.*  FROM tc_goodsoption WHERE tc_goodsoption.DeleteYN = 0 AND tc_goodsoption.StaffSalesYN = 1 ORDER BY tc_goodsoption.GoodsCD ASC";
      stmt = this.FV_conDB.createStatement(1004, 1007);
      rs = stmt.executeQuery(strSQL);
      if (rs.last() == true) {
        int intMaxRow = rs.getRow();
        rs.first();
        for (int i = 0; i < intMaxRow; i++) {
          clsGoodsOption = new GoodsOptionBean();
          clsGoodsOption = setFieldList_GoodsOption(rs);
          arrList.add(clsGoodsOption);
          rs.next();
        } 
      } 
    } catch (SQLException e) {
      System.out.println(strError + e.getMessage());
    } finally {
      DBManager.rsClose(rs);
      DBManager.stmtClose(stmt);
    } 
    return arrList;
  }
  
  public GoodsOptionBean setFieldList_GoodsOption(ResultSet rs) {
    GoodsOptionBean clsReturn;
    String strError = "(GoodsManager) setFieldList_GoodsOption 에러 : ";
    try {
      clsReturn = new GoodsOptionBean();
      clsReturn.GoodsOptionCD = rs.getLong("GoodsOptionCD");
      clsReturn.OptionNM = rs.getString("OptionNM");
      clsReturn.MergeCD = rs.getInt("MergeCD");
      clsReturn.GoodsCD = rs.getLong("GoodsCD");
      clsReturn.OriginPrice = (rs.getBigDecimal("OriginPrice") == null) ? new BigDecimal("0") : rs.getBigDecimal("OriginPrice");
      clsReturn.UnitPrice = (rs.getBigDecimal("UnitPrice") == null) ? new BigDecimal("0") : rs.getBigDecimal("UnitPrice");
      clsReturn.UnitPrice3 = (rs.getBigDecimal("UnitPrice3") == null) ? new BigDecimal("0") : rs.getBigDecimal("UnitPrice3");
      clsReturn.UnitPrice6 = (rs.getBigDecimal("UnitPrice6") == null) ? new BigDecimal("0") : rs.getBigDecimal("UnitPrice6");
      clsReturn.UnitPrice12 = (rs.getBigDecimal("UnitPrice12") == null) ? new BigDecimal("0") : rs.getBigDecimal("UnitPrice12");
      clsReturn.UnitPrice_Alumnus = (rs.getBigDecimal("UnitPrice_Alumnus") == null) ? new BigDecimal("0") : rs.getBigDecimal("UnitPrice_Alumnus");
      clsReturn.UnitPrice_Staff = (rs.getBigDecimal("UnitPrice_Staff") == null) ? new BigDecimal("0") : rs.getBigDecimal("UnitPrice_Staff");
      clsReturn.Capacity = (rs.getBigDecimal("Capacity") == null) ? new BigDecimal("0") : rs.getBigDecimal("Capacity");
      clsReturn.AliasNM = (rs.getString("AliasNM") == null) ? "" : rs.getString("AliasNM");
      clsReturn.SalesYN = rs.getInt("SalesYN");
      clsReturn.DeleteYN = rs.getInt("DeleteYN");
    } catch (Exception e) {
      System.out.println(strError + e.getMessage());
      clsReturn = null;
    } 
    return clsReturn;
  }
  
  public ArrayList<TeamPersonBean> AllTeamPerson() {
    String strError = "(TeamPersonManager) AllTeamPerson 에러 : ";
    Statement stmt = null;
    ResultSet rs = null;
    ArrayList<TeamPersonBean> arrReturn = new ArrayList<>();
    TeamPersonBean clsTeamPerson = null;
    try {
      String strSQL = "SELECT * FROM tc_teamperson  WHERE  TeamPersonCD > 200 AND TeamPersonCD < 700 AND AgencyYN = 0  Order By TeamPersonNM ASC ";
      stmt = this.FV_conDB.createStatement(1004, 1007);
      rs = stmt.executeQuery(strSQL);
      if (rs.last() == true) {
        int intMaxRow = rs.getRow();
        rs.first();
        for (int i = 0; i < intMaxRow; i++) {
          clsTeamPerson = new TeamPersonBean();
          clsTeamPerson.TeamPersonCD = rs.getLong("TeamPersonCD");
          clsTeamPerson.TeamPersonNM = (rs.getString("TeamPersonNM") == null) ? "" : rs.getString("TeamPersonNM");
          arrReturn.add(clsTeamPerson);
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
}
