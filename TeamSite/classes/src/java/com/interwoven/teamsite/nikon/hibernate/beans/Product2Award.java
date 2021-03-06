package com.interwoven.teamsite.nikon.hibernate.beans;

// Generated 18-Oct-2009 14:29:28 by Hibernate Tools 3.2.4.GA

/**
 * Product2Award generated by hbm2java
 */
public class Product2Award implements java.io.Serializable {

	private Product2AwardPK id;
	private String nikonLocale;
	private String prodProdId;
	private String prodProdDevCode;
	private String awardTestId;

	public Product2Award() {
	}

	public Product2Award(Product2AwardPK id) {
		this.id = id;
	}

	public Product2Award(Product2AwardPK id, String nikonLocale,
			String prodProdId, String prodProdDevCode, String awardTestId) {
		this.id = id;
		this.nikonLocale = nikonLocale;
		this.prodProdId = prodProdId;
		this.prodProdDevCode = prodProdDevCode;
		this.awardTestId = awardTestId;
	}

	public Product2AwardPK getId() {
		return this.id;
	}

	public void setId(Product2AwardPK id) {
		this.id = id;
	}

	public String getNikonLocale() {
		return this.nikonLocale;
	}

	public void setNikonLocale(String nikonLocale) {
		this.nikonLocale = nikonLocale;
	}

	public String getProdProdId() {
		return this.prodProdId;
	}

	public void setProdProdId(String prodProdId) {
		this.prodProdId = prodProdId;
	}

	public String getProdProdDevCode() {
		return this.prodProdDevCode;
	}

	public void setProdProdDevCode(String prodProdDevCode) {
		this.prodProdDevCode = prodProdDevCode;
	}

	public String getAwardTestId() {
		return this.awardTestId;
	}

	public void setAwardTestId(String awardTestId) {
		this.awardTestId = awardTestId;
	}

}
