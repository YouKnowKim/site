package com.milk.batch.bean;

public class CategoryBean extends CommonBean {
	public long CategoryCD;

	public int CategoryOrder;

	public int CategoryLevel;

	public String CategoryNM = "";

	public long ParentCategoryCD;

	public String ParentCategoryNM = "";

	public int ParentCategoryOrder;

	public int ParentCategoryLevel;

	public long RootCategoryCD;

	public String RootCategoryNM = "";

	public int RootCategoryOrder;

	public int RootCategoryLevel;
}