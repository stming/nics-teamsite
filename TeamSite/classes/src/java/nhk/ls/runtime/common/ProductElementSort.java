/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.common;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 *
 * @author smukherj
 */
public class ProductElementSort {

    private static final Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.common.ProductElementSort"));
    /**
     * This is used by Product Listing.
     *
     */
    public static final Comparator<Element> PRICE_ASCENDING =
            new Comparator<Element>() {

                public int compare(Element o1, Element o2) {
                    int result = 0;
                    double o1Price = Double.parseDouble(o1.selectSingleNode("@Price").getText());
                    double o2Price = Double.parseDouble(o2.selectSingleNode("@Price").getText());

                    if (o1Price > o2Price) {
                        result = 1;
                    } else if (o1Price < o2Price) {
                        result = -1;
                    }
                    mLogger.createLogDebug("PRICE_ASCENDING::Comparing o1Price" + o1Price + " with o2Price" + o2Price
                            + "::Returning " + result);
                    return result;
                }
            };
    /**
     * This is used by Product Listing.
     *
     */
    public static final Comparator<Element> PRICE_DESCENDING =
            new Comparator<Element>() {

                public int compare(Element o1, Element o2) {
                    int result = 0;
                    double o1Price = Double.parseDouble(o1.selectSingleNode("@Price").getText());
                    double o2Price = Double.parseDouble(o2.selectSingleNode("@Price").getText());

                    if (o2Price > o1Price) {
                        result = 1;
                    } else if (o2Price < o1Price) {
                        result = -1;
                    }
                    mLogger.createLogDebug("PRICE_DESCENDING::Comparing o1Price" + o1Price + " with o2Price" + o2Price
                            + "::Returning " + result);
                    return result;
                }
            };
    /**
     * This is used by Product Listing.
     *
     */
    public static final Comparator<Element> RELEASE_DATE = new Comparator<Element>() {

        public int compare(Element o1, Element o2) {
            Date o1Date = DateUtils.getDate(o1.selectSingleNode("@ReleaseDate").getText(), DateUtils.DATE_FORMAT_DATEONLY);
            Date o2Date = DateUtils.getDate(o2.selectSingleNode("@ReleaseDate").getText(), DateUtils.DATE_FORMAT_DATEONLY);

            int result = o2Date.compareTo(o1Date);

            mLogger.createLogDebug("RELEASE_DATE::Comparing o1Date" + o1Date + " with o2Date" + o2Date
                    + "::Returning " + result);
            return result;
        }
    };
}
