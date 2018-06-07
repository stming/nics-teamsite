package nhk.ts.wcms.dao;

import java.sql.Date;

/**
 * @author rmantrav
 * 
 */
public class Products {

    private String productID;
    private String productName;
    private ProductCategory category;
    private Date releaseDate;
    private String summary;
    private String quickViewData;
    private String features;
    private String price;
    private String image;
    private String iconImage;
    private String availableInEshop;
    private String status;
    private String archiveFlag;
    private String comingSoonFlag;
    private String dcrPath;
    private String doNotShowOnHomepage;
    private int displayOrder;
    protected String locale;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getArchiveFlag() {
        return archiveFlag;
    }

    public void setArchiveFlag(String archiveFlag) {
        this.archiveFlag = archiveFlag;
    }

    public String getComingSoonFlag() {
        return comingSoonFlag;
    }

    public void setComingSoonFlag(String comingSoonFlag) {
        this.comingSoonFlag = comingSoonFlag;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getQuickViewData() {
        return quickViewData;
    }

    public void setQuickViewData(String quickViewData) {
        this.quickViewData = quickViewData;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    public String getAvailableInEshop() {
        return availableInEshop;
    }

    public void setAvailableInEshop(String availableInEshop) {
        this.availableInEshop = availableInEshop;
    }

    public String getDcrPath() {
        return dcrPath;
    }

    public void setDcrPath(String dcrPath) {
        this.dcrPath = dcrPath;
    }

    public String getDoNotShowOnHomepage() {
        return doNotShowOnHomepage;
    }

    public void setDoNotShowOnHomepage(String doNotShowOnHomepage) {
        this.doNotShowOnHomepage = doNotShowOnHomepage;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getlocale() {
        return locale;
    }

    public void setlocale(String locale) {
        this.locale = locale;
    }
}
