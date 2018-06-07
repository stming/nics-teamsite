package com.interwoven.teamsite.nikon.hibernate.beans;

// Generated 18-Oct-2009 14:29:28 by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * AwardTestimonial generated by hbm2java
 */
public class AwardTestimonial implements java.io.Serializable {

	private String id;
	private String awardTestId;
	private String year;
	private Date testimonialDate;
	private String prodRelated;
	private String awardAwardId;
	private String nikonLocale;
	private String path;

	public AwardTestimonial() {
	}

	public AwardTestimonial(String id) {
		this.id = id;
	}

	public AwardTestimonial(String id, String awardTestId,
			Date testimonialDate, String prodRelated, String awardAwardId,
			String nikonLocale, String path) {
		this.id = id;
		this.awardTestId = awardTestId;
		this.testimonialDate = testimonialDate;
		this.prodRelated = prodRelated;
		this.awardAwardId = awardAwardId;
		this.nikonLocale = nikonLocale;
		this.path = path;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAwardTestId() {
		return this.awardTestId;
	}

	public void setAwardTestId(String awardTestId) {
		this.awardTestId = awardTestId;
	}

	public String getYear() {
		return this.year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Date getTestimonialDate() {
		return this.testimonialDate;
	}

	public void setTestimonialDate(Date testimonialDate) {
		this.testimonialDate = testimonialDate;
	}

	public String getProdRelated() {
		return this.prodRelated;
	}

	public void setProdRelated(String prodRelated) {
		this.prodRelated = prodRelated;
	}

	public String getAwardAwardId() {
		return this.awardAwardId;
	}

	public void setAwardAwardId(String awardAwardId) {
		this.awardAwardId = awardAwardId;
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

}
