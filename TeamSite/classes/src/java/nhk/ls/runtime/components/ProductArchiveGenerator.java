/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.PropertyContext;

import nhk.ls.runtime.common.Logger;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author smukherj
 */
public class ProductArchiveGenerator {

    private Logger mLogger = new Logger(LogFactory.getLog(this.getClass()));

    public Document getCategory1Options(PropertyContext context) {

        Document doc = Dom4jUtils.newDocument();
        Element optionsBase = doc.addElement("Options");

        //a_Add N options

        // 1st option

        Element option = optionsBase.addElement("Option");
        option.addAttribute("Selected", "true");

        option.addElement("Display").addText("DX Format");
        option.addElement("Value").addText("DX Format");

        // 2nd option

        option = optionsBase.addElement("Option");
        option.addAttribute("Selected", "true");

        option.addElement("Display").addText("FX Format");
        option.addElement("Value").addText("FX Format");

        return doc;
    }

    public Document getCategory2Options(PropertyContext context) {

        if (context != null && context.getParameters() != null) {
        }

        Document doc = Dom4jUtils.newDocument();
        Element optionsBase = doc.addElement("Options");

        //a_Add N options

        // 1st option

        Element option = optionsBase.addElement("Option");
        option.addAttribute("Selected", "true");

        option.addElement("Display").addText("Zoom");
        option.addElement("Value").addText("Zoom");

        // 2nd option

        option = optionsBase.addElement("Option");
        option.addAttribute("Selected", "true");

        option.addElement("Display").addText("Single Focal Length");
        option.addElement("Value").addText("Single Focal Length");

        return doc;
    }

    public Document getProductOptions(PropertyContext context) {

        if (context != null && context.getParameters() != null) {
        }

        Document doc = Dom4jUtils.newDocument();
        Element optionsBase = doc.addElement("Options");

        //a_Add N options

        // 1st option

        Element option = optionsBase.addElement("Option");
        option.addAttribute("Selected", "true");

        option.addElement("Display").addText("500 mm f/4");
        option.addElement("Value").addText("500 mm f/4");

        // 2nd option

        option = optionsBase.addElement("Option");
        option.addAttribute("Selected", "true");

        option.addElement("Display").addText("f35 mm");
        option.addElement("Value").addText("f35 mm");
        return doc;
    }
}
