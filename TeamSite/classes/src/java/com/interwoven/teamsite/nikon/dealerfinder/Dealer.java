package com.interwoven.teamsite.nikon.dealerfinder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has a OneToMany relation with AdditionalData
 *
 * @author Mike
 */
@Entity
@Table(name = "dealer")
public class Dealer implements Serializable
{
    static final Logger oLogger = LoggerFactory.getLogger(Dealer.class);
    // OneTomany....
    @OneToMany(mappedBy = "dealer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "dealer_with_additional", joinColumns =
    {
        @JoinColumn(name = "id")
    }, inverseJoinColumns =
    {
        @JoinColumn(name = "id")
    })
    private Set<AdditionalData> stAdditionalData = new HashSet<AdditionalData>();
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Column(name = "name")
    private String name;
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Column(name = "street")
    private String street;
    public String getStreet()
    {
        return street;
    }

    public void setStreet(String street)
    {
        this.street = street;
    }

    @Column(name = "town")
    private String town;
    @Column(name = "state")
    private String state;
    @Column(name = "country")
    private String country;
    @Column(name = "country_code")
    private String countryCode;
    @Column(name = "lon")
    private float longitude;
    @Column(name = "lat")
    private float latitude;
    @Column(name = "author")
    private String author;
    @Column(name = "modified_date")
    private long modifiedDate;
    @Column(name = "description")
    private String description;  
    
    @Column(name = "tel")
    private String tel;
    @Column(name = "fax")
    private String fax;
    @Column(name = "email")
    private String email;
    @Column(name = "url")
    private String url;
    @Column(name = "opening_hours")
    private String openingHours;
    @Column(name="post_code")
    private String postCode;
    
    @Column(name="abf")
    private Long abfDate = new Long(0);
    @Column(name="prod")
    private Long prodDate = new Long(0);
 	
    @Column(name = "status")
    private String status;
    
    // can't use group as this is a reserved PA keyword apparently
    @Column(name = "user_group")
    private String userGroup;
      
    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
       
	if (!(object instanceof Dealer))
        {
            return false;
        }
        Dealer other = (Dealer) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "com.nikon.dealerFinder.dealer[ id=" + id + " ]";
    }

    /**
     * @return the town
     */
    public String getTown()
    {
        return town;
    }

    /**
     * @param town the town to set
     */
    public void setTown(String town)
    {
        this.town = town;
    }

    /**
     * @return the state
     */
    public String getState()
    {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state)
    {
        this.state = state;
    }

    /**
     * @return the country
     */
    public String getCountry()
    {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * @return the countryCode
     */
    public String getCountryCode()
    {
        return countryCode;
    }

    /**
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    /**
     * @return the longitude
     */
    public float getLongitude()
    {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    /**
     * @return the latitude
     */
    public float getLatitude()
    {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }

    /**
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * @return the date
     */
    public long getModifiedDate()
    {
        return modifiedDate;
    }

    /**
     * @param date the date to set
     */
    public void setModifiedDate(long date)
    {
        this.modifiedDate = date;
    }

    public String toJSONString()
    {
        try
        {
            JSONObject oDealer = new JSONObject();
            oDealer.put("Name", name);
            oDealer.put("Street", street);
            oDealer.put("Town", town);
            oDealer.put("State", state);
            oDealer.put("Country", country);
            oDealer.put("ID", id);

            oDealer.put("ABF", getABFCode());
            oDealer.put("Live", getLiveCode());

            return oDealer.toString();
        }
        catch (Exception e)
        {
            oLogger.error("Error converting dealer to JSON: " + e.getMessage());
        }
        return null;
    }

    /**
     * @return the stAdditionalData
     */
    public Set<AdditionalData> getAdditionalData()
    {
        return stAdditionalData;
    }

    /**
     * @param stAdditionalData the stAdditionalData to set
     */
    public void setAdditionalData(Set<AdditionalData> stAdditionalData)
    {
        this.stAdditionalData = stAdditionalData;
    }
    
    public int getABFCode()
    {
        return (abfDate == 0 ) ? 0 : ((modifiedDate > abfDate)? 1 : 2);
        
    }

    public int getLiveCode()
    {
        return (prodDate == 0 ) ? 0 : ((modifiedDate > prodDate)? 1 : 2);
    }


    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the tel
     */
    public String getTel()
    {
        return tel;
    }

    /**
     * @param tel the tel to set
     */
    public void setTel(String tel)
    {
        this.tel = tel;
    }

    /**
     * @return the fax
     */
    public String getFax()
    {
        return fax;
    }

    /**
     * @param fax the fax to set
     */
    public void setFax(String fax)
    {
        this.fax = fax;
    }

    /**
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return the openingHours
     */
    public String getOpeningHours()
    {
        return openingHours;
    }

    /**
     * @param openingHours the openingHours to set
     */
    public void setOpeningHours(String openingHours)
    {
        this.openingHours = openingHours;
    }

    /**
     * @return the postCode
     */
    public String getPostCode()
    {
        return postCode;
    }

    /**
     * @param postCode the postCode to set
     */
    public void setPostCode(String postCode)
    {
        this.postCode = postCode;
    }

    /**
     * @return the status
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * @return the group
     */
    public String getGroup()
    {
        return userGroup;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group)
    {
        this.userGroup = group;
    }

    /**
     * @return the abfDate
     */
    public Long getAbfDate()
    {
        return abfDate;
    }

    /**
     * @param abfDate the abfDate to set
     */
    public void setAbfDate(Long abfDate)
    {
        this.abfDate = abfDate;
    }

    /**
     * @return the prodDate
     */
    public Long getProdDate()
    {
        return prodDate;
    }

    /**
     * @param prodDate the prodDate to set
     */
    public void setProdDate(Long prodDate)
    {
        this.prodDate = prodDate;
    }
}
