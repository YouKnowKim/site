package com.milk.batch.bean;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CartBean extends GoodsOptionBean {
	public long UserCD;

	public int Quantity;

	public int OrderSEQ;

	public int WeekQty;

	public int ContractPeriod;

	public String WeekRemark = "";

	public BigDecimal OrderPrice = new BigDecimal("0");

	public BigDecimal SubTotal = new BigDecimal("0");

	public long GoodsOptionCD;

	public BigDecimal UserPrice = new BigDecimal("0");

	public long AgencyCD;

	public String AgencyTel = "";

	public ArrayList<com.milk.batch.bean.CartBean> arrList = new ArrayList<>();

	public static com.milk.batch.bean.CartBean[] convertToArray(ArrayList<com.milk.batch.bean.CartBean> p_Cart) {
		String strError = "(CartBean) convertToArray : ";
		com.milk.batch.bean.CartBean[] arrList = null;
		try {
			if (p_Cart != null) {
				arrList = new com.milk.batch.bean.CartBean[p_Cart.size()];
				for (int i = 0; i < p_Cart.size(); i++)
					arrList[i] = p_Cart.get(i);
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return arrList;
	}

	public static ArrayList<com.milk.batch.bean.CartBean> convertToArrayList(com.milk.batch.bean.CartBean[] p_Cart) {
		String strError = "(CartBean) convertToArrayList : ";
		ArrayList<com.milk.batch.bean.CartBean> arrList = null;
		try {
			if (p_Cart != null) {
				arrList = new ArrayList<>();
				for (int i = 0; i < p_Cart.length; i++)
					arrList.add(p_Cart[i]);
			}
		} catch (Exception e) {
			System.out.println(strError + e.getMessage());
		}
		return arrList;
	}
}
