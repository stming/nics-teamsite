package com.interwoven.teamsite.nikon.hibernate.beans;

// Generated 18-Oct-2009 14:29:28 by Hibernate Tools 3.2.4.GA

/**
 * AwardTest2Award generated by hbm2java
 */
public class AwardTest2Award implements java.io.Serializable {

	private AwardTest2AwardPK id;
	private String awardAwardId;
	private String awardTestId;
	private String nikonLocale;

	public AwardTest2Award() {
	}

	public AwardTest2Award(AwardTest2AwardPK id) {
		this.id = id;
	}

	public AwardTest2Award(AwardTest2AwardPK id, String awardAwardId,
			String awardTestId, String nikonLocale) {
		this.id = id;
		this.awardAwardId = awardAwardId;
		this.awardTestId = awardTestId;
		this.nikonLocale = nikonLocale;
	}

	public AwardTest2AwardPK getId() {
		return this.id;
	}

	public void setId(AwardTest2AwardPK id) {
		this.id = id;
	}

	public String getAwardAwardId() {
		return this.awardAwardId;
	}

	public void setAwardAwardId(String awardAwardId) {
		this.awardAwardId = awardAwardId;
	}

	public String getAwardTestId() {
		return this.awardTestId;
	}

	public void setAwardTestId(String awardTestId) {
		this.awardTestId = awardTestId;
	}

	public String getNikonLocale() {
		return this.nikonLocale;
	}

	public void setNikonLocale(String nikonLocale) {
		this.nikonLocale = nikonLocale;
	}

}
