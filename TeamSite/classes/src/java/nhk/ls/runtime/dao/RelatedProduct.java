/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.dao;

import java.io.Serializable;

/**
 *
 * @author smukherj
 */
public class RelatedProduct implements Serializable {

    private String groupCategoryId;
    private String productId;
    private String relatedGroupCategoryId;
    private String relatedProductId;

    public String getGroupCategoryId() {
        return groupCategoryId;
    }

    public void setGroupCategoryId(String groupCategoryId) {
        this.groupCategoryId = groupCategoryId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRelatedGroupCategoryId() {
        return relatedGroupCategoryId;
    }

    public void setRelatedGroupCategoryId(String relatedGroupCategoryId) {
        this.relatedGroupCategoryId = relatedGroupCategoryId;
    }

    public String getRelatedProductId() {
        return relatedProductId;
    }

    public void setRelatedProductId(String relatedProductId) {
        this.relatedProductId = relatedProductId;
    }
}
