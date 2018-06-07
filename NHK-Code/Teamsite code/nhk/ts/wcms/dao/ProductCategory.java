package nhk.ts.wcms.dao;

/**
 * @author rmantrav
 *
 */
public class ProductCategory {
	
	private String categoryID;
	private String categoryName;
	private String flashBannerPath;
	private String iconImageMainNavigation;
	private String overviewRequired;
	private String overviewText;
	private String parentCategoryID;
	private String grandParentCategoryID;
        private String locale;
	
	public String getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getFlashBannerPath() {
		return flashBannerPath;
	}
	public void setFlashBannerPath(String flashBannerPath) {
		this.flashBannerPath = flashBannerPath;
	}
	public String getIconImageMainNavigation() {
		return iconImageMainNavigation;
	}
	public void setIconImageMainNavigation(String iconImageMainNavigation) {
		this.iconImageMainNavigation = iconImageMainNavigation;
	}
	public String getOverviewRequired() {
		return overviewRequired;
	}
	public void setOverviewRequired(String overviewRequired) {
		this.overviewRequired = overviewRequired;
	}
	public String getOverviewText() {
		return overviewText;
	}
	public void setOverviewText(String overviewText) {
		this.overviewText = overviewText;
	}
	public String getParentCategoryID() {
		return parentCategoryID;
	}
	public void setParentCategoryID(String parentCategoryID) {
		this.parentCategoryID = parentCategoryID;
	}
	public String getGrandParentCategoryID() {
		return grandParentCategoryID;
	}
	public void setGrandParentCategoryID(String grandParentCategoryID) {
		this.grandParentCategoryID = grandParentCategoryID;
	}
        public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
}
