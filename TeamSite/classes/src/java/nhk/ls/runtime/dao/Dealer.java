package nhk.ls.runtime.dao;
// Referenced classes of package nhk.ls.runtime.dao:
//  Dealer
public class Dealer
{
    private String dcrlink;
    private String dealername;
    private String vendorType;
    private String vendorName;
    private String country;
    private String region;
    private String city;
    private String contact;
    private String locale;
    private String pincode;
    private String fax;
    private String URL;    //added on 20140327
    private int sortOrder;
    private String serviceType; // added on 20141208
    private String openingHours; // added on 20141208
    
    // added on 20141208
    public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getOpeningHours() {
		return openingHours;
	}

	public void setOpeningHours(String openingHours) {
		this.openingHours = openingHours;
	}

	//added on 20140327 start
    public String getURL()
    {
        return URL;
    }

    public void setURL(String URL)
    {
        this.URL = URL;
    }
    //added on 20140327 end
   
    public String getDcrlink()
    {
        return dcrlink;
    }

    public void setDcrlink(String dcrlink)
    {
        this.dcrlink = dcrlink;
    }

    public String getDealername()
    {
        return dealername;
    }

    public void setDealername(String dealername)
    {
        this.dealername = dealername;
    }

    public String getVendorType()
    {
        return vendorType;
    }

    public void setVendorType(String vendorType)
    {
        this.vendorType = vendorType;
    }

    public String getVendorName()
    {
        return vendorName;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }
    public String getContact()
    {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    } 
    
     public String getPincode()
    {
        return pincode;
    }

    public void setPincode(String pincode)
    {
        this.pincode = pincode;
    }

    public String getFax()
    {
        return fax;
    }

    public void setFax(String fax)
    {
        this.fax = fax;
    }
    
    public int getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder)
    {
        this.sortOrder = sortOrder;
    }
}