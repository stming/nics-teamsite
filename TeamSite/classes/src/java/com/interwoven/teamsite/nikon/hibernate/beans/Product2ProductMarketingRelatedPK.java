package com.interwoven.teamsite.nikon.hibernate.beans;

// Generated 18-Oct-2009 14:29:28 by Hibernate Tools 3.2.4.GA

/**
 * Product2ProductMarketingRelatedPK generated by hbm2java
 */
public class Product2ProductMarketingRelatedPK implements java.io.Serializable {

	private String id;
	private String prodId;
	private String prodProdId;

	public Product2ProductMarketingRelatedPK() {
	}

	public Product2ProductMarketingRelatedPK(String id, String prodId,
			String prodProdId) {
		this.id = id;
		this.prodId = prodId;
		this.prodProdId = prodProdId;
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

	public String getProdProdId() {
		return this.prodProdId;
	}

	public void setProdProdId(String prodProdId) {
		this.prodProdId = prodProdId;
	}

}
