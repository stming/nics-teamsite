/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interwoven.teamsite.nikon.dealerfinder;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This class has a ManyToOne relation with Dealer
 *
 * @author Mike
 */
@Entity
@Table(name = "additional")
public class AdditionalData implements Serializable
{

    private static final long serialVersionUID = 1L;
    // ManyToOne
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id", insertable=false, updatable=false)
    private Dealer dealer;

    /**
     * @return the dealer
     */
    public Dealer getDealer()
    {
        return dealer;
    }

    /**
     * @param dealer the dealer to set
     */
    public void setDealer(Dealer dealer)
    {
        this.dealer = dealer;
    }

    @Id
    private String pk;
    
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }
    
    private Long id;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name = "fieldID")
    private Long fieldId;

    /**
     * @return the fieldId
     */
    public Long getFieldId()
    {
        return fieldId;
    }

    /**
     * @param fieldId the fieldId to set
     */
    public void setFieldId(Long fieldId)
    {
        this.fieldId = fieldId;
    }

    @Column(name = "fieldValue")
    private String fieldValue;

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (fieldId != null ? fieldId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AdditionalData))
        {
            return false;
        }
        AdditionalData other = (AdditionalData) object;
        if ((this.fieldId == null && other.fieldId != null) || (this.fieldId != null && !this.fieldId.equals(other.fieldId)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "com.nikon.dealerFinder.additional[ id=" + fieldId + " ]";
    }

    /**
     * @return the fieldValue
     */
    public String getFieldValue()
    {
        return fieldValue;
    }

    /**
     * @param fieldValue the fieldValue to set
     */
    public void setFieldValue(String fieldValue)
    {
        this.fieldValue = fieldValue;
    }
}
