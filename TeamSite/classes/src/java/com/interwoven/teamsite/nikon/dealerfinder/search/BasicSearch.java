package com.interwoven.teamsite.nikon.dealerfinder.search;

/**
 *
 * @author Mike
 */
public class BasicSearch extends AbstractSearch
{
    public BasicSearch(){};
    
    // Not sure this makes sense in SQL context but just placeholder for now
    private String sSearchText;
    
    private String sFieldName;
	
    /**
     * @return the sSearchText
     */
    public String getSearchText()
    {
        return sSearchText;
    }

    /**
     * @param sSearchText the sSearchText to set
     */
    public void setSearchText(String sSearchText)
    {
        this.sSearchText = sSearchText;
    }

    /**
     * @return the sFieldName
     */
    public String getFieldName()
    {
        return sFieldName;
    }

    /**
     * @param sFieldName the sFieldName to set
     */
    public void setFieldName(String sFieldName)
    {
        this.sFieldName = sFieldName;
    }
}

