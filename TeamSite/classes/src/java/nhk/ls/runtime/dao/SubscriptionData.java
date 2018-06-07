/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nhk.ls.runtime.dao;
import java.util.Date;

/**
 *
 * @author wxiaoxi
 */
public class SubscriptionData {
//(EMAIL,CONTACT_NUMBER, SALUTATION, LAST_NAME, FIRST_NAME,NIKON_PRODUCTS,
 //COUNTRY_RESIDENCE, COUNTRY_SUBSCRIPTION, STATUS, DATE_SUBMITTED, CONFIRM_LINK)

     /**
     * Getter/setter for property EMAIL
     **/
    protected String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

     /**
     * Getter/setter for property CONTACT_NUMBER
     **/

    protected String contactNumber;

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }


     /**
     * Getter/setter for property SALUTATION
     **/
    protected String salutation;

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

     /**
     * Getter/setter for property LAST_NAME
     **/
    protected String lastName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName= lastName;
    }

     /**
     * Getter/setter for property FIRST_NAME
     **/
    protected String firstName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


     /**
     * Getter/setter for property NIKON_PRODUCTS
     **/
    protected String nikonProdocts;

    public String getNikonProdocts() {
        return nikonProdocts;
    }

    public void setNikonProdocts(String nikonProdocts) {
        this.nikonProdocts = nikonProdocts;
    }


     /**
     * Getter/setter for property COUNTRY_RESIDENCE
     **/
    protected String countryResidence;

    public String getCountryResidence() {
        return countryResidence;
    }

    public void setCountryResidence(String countryResidence) {
        this.countryResidence = countryResidence;
    }
    

     /**
     * Getter/setter for property COUNTRY_SUBSCRIPTION
     **/
    protected String countrySubscription;

    public String getCountrySubscription() {
        return countrySubscription;
    }

    public void setCountrySubscription(String countrySubscription) {
        this.countrySubscription = countrySubscription;
    }


     /**
     * Getter/setter for property STATUS
     **/
    protected String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status= status;
    }


     /**
     * Getter/setter for property DATE_SUBMITTED
     **/
    protected Date dateSubmitted;

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }


      /**
     * Getter/setter for property CONFIRM_LINK
     **/
    protected String confirmLink;

    public String getConfirmLink() {
        return confirmLink;
    }

    public void setConfirmLink(String confirmLink) {
        this.confirmLink = confirmLink;
    }
}
