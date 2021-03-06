package com.interwoven.teamsite.nikon.hibernate.beans;

// Generated 18-Oct-2009 14:29:28 by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * Product generated by hbm2java
 */
public class Product implements java.io.Serializable {

	private String id;
	private String prodId;
	private String prodDevCode;
	private String upc;
	private String description;
	private String type;
	private Date wwaDate;
	private String title;
	private boolean discontinued;
	private String prodShortCode;
	private String prodShortName;
	private String prodAccessoryOf;
	private String nikonLocale;
	private String path;
	private String sortOrder;
	private boolean stillNew;
	private String localShortName;
	private boolean accessory;
	private String navCat1;
	private String navCat2;
	private String navCat3;
	private boolean kit;
	private String productCategory;
	private String prodLocaleOptOut;
	private boolean migrated;
	private boolean localProduct;

	public Product() {
	}

	public Product(String id, String type, Date wwaDate) {
		this.id = id;
		this.type = type;
		this.wwaDate = wwaDate;
	}

	public Product(String id, String prodId, String prodDevCode, String upc,
			String description, String type, Date wwaDate, String title,
			String prodShortCode, String prodShortName, String prodAccessoryOf,
			String nikonLocale, String path, String sortOrder,
			String localShortName, String navCat1, String navCat2,
			String navCat3, boolean kit, String productCategory,
			String prodLocaleOptOut, boolean migrated, boolean localProduct) {
		this.id = id;
		this.prodId = prodId;
		this.prodDevCode = prodDevCode;
		this.upc = upc;
		this.description = description;
		this.type = type;
		this.wwaDate = wwaDate;
		this.title = title;
		this.prodShortCode = prodShortCode;
		this.prodShortName = prodShortName;
		this.prodAccessoryOf = prodAccessoryOf;
		this.nikonLocale = nikonLocale;
		this.path = path;
		this.sortOrder = sortOrder;
		this.localShortName = localShortName;
		this.navCat1 = navCat1;
		this.navCat2 = navCat2;
		this.navCat3 = navCat3;
		this.kit = kit;
		this.productCategory = productCategory;
		this.prodLocaleOptOut = prodLocaleOptOut;
		this.migrated = migrated;
		this.localProduct = localProduct;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProdId() {
		return this.prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}

	public String getProdDevCode() {
		return this.prodDevCode;
	}

	public void setProdDevCode(String prodDevCode) {
		this.prodDevCode = prodDevCode;
	}

	public String getUpc() {
		return this.upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getWwaDate() {
		return this.wwaDate;
	}

	public void setWwaDate(Date wwaDate) {
		this.wwaDate = wwaDate;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isDiscontinued() {
		return this.discontinued;
	}

	public void setDiscontinued(boolean discontinued) {
		this.discontinued = discontinued;
	}

	public String getProdShortCode() {
		return this.prodShortCode;
	}

	public void setProdShortCode(String prodShortCode) {
		this.prodShortCode = prodShortCode;
	}

	public String getProdShortName() {
		return this.prodShortName;
	}

	public void setProdShortName(String prodShortName) {
		this.prodShortName = prodShortName;
	}

	public String getProdAccessoryOf() {
		return this.prodAccessoryOf;
	}

	public void setProdAccessoryOf(String prodAccessoryOf) {
		this.prodAccessoryOf = prodAccessoryOf;
	}

	public String getNikonLocale() {
		return this.nikonLocale;
	}

	public void setNikonLocale(String nikonLocale) {
		this.nikonLocale = nikonLocale;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSortOrder() {
		return this.sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public boolean isStillNew() {
		return this.stillNew;
	}

	public void setStillNew(boolean stillNew) {
		this.stillNew = stillNew;
	}

	public String getLocalShortName() {
		return this.localShortName;
	}

	public void setLocalShortName(String localShortName) {
		this.localShortName = localShortName;
	}

	public boolean isAccessory() {
		return this.accessory;
	}

	public void setAccessory(boolean accessory) {
		this.accessory = accessory;
	}

	public String getNavCat1() {
		return this.navCat1;
	}

	public void setNavCat1(String navCat1) {
		this.navCat1 = navCat1;
	}

	public String getNavCat2() {
		return this.navCat2;
	}

	public void setNavCat2(String navCat2) {
		this.navCat2 = navCat2;
	}

	public String getNavCat3() {
		return this.navCat3;
	}

	public void setNavCat3(String navCat3) {
		this.navCat3 = navCat3;
	}

	public boolean isKit() {
		return this.kit;
	}

	public void setKit(boolean kit) {
		this.kit = kit;
	}

	public String getProductCategory() {
		return this.productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}

	public String getProdLocaleOptOut() {
		return this.prodLocaleOptOut;
	}

	public void setProdLocaleOptOut(String prodLocaleOptOut) {
		this.prodLocaleOptOut = prodLocaleOptOut;
	}

	public boolean isMigrated() {
		return this.migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}

	public boolean isLocalProduct() {
		return this.localProduct;
	}

	public void setLocalProduct(boolean localProduct) {
		this.localProduct = localProduct;
	}

}
