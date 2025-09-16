package com.milk.batch.bean;

import java.util.ArrayList;

public class GoodsBean extends StoreBean {
	public long GoodsCD;

	public long GoodsCD_Origin;

	public String GoodsCD_s = "";

	public String GoodsNM = "";

	public String GoodsNM_Origin = "";

	public String GoodsNM_s = "";

	public String PB_Code = "";

	public String GoodsImg = "";

	public String GoodsIntro = "";

	public int GoodsType;

	public String BasicInfo = "";

	public String Standard = "";

	public String ShelfLife = "";

	public String Ingredient = "";

	public String NutritionFacts = "";

	public String EssentialInfo = "";

	public String Notice = "";

	public String MadeCountry = "";

	public String BestPeriod = "";

	public String ContentImg = "";

	public String ContentHTML = "";

	public String ContentHTML_Origin = "";

	public String ContentText = "";

	public String ContentText_Origin = "";

	public String ContentText_s = "";

	public int SalesYN;

	public String SalesImg = "";

	public int SoldoutYN;

	public int ShowYN;

	public int DeleteYN;

	public long CategoryCD;

	public long CategoryCD_Origin;

	public String CategoryCD_s = "";

	public int CategoryOrder;

	public int CategoryOrder_Origin;

	public long ParentCategoryCD;

	public ArrayList<CategoryBean> arrCategory = new ArrayList<>();
}