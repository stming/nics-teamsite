package com.interwoven.teamsite.nikon.hibernate.beans;

// Generated 18-Oct-2009 14:29:28 by Hibernate Tools 3.2.4.GA

/**
 * AwardTest2AwardPK generated by hbm2java
 */
public class AwardTest2AwardPK implements java.io.Serializable {

	private String id;
	private String awardAwardId;
	private String awardTestId;

	public AwardTest2AwardPK() {
	}

	public AwardTest2AwardPK(String id, String awardAwardId, String awardTestId) {
		this.id = id;
		this.awardAwardId = awardAwardId;
		this.awardTestId = awardTestId;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
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

}