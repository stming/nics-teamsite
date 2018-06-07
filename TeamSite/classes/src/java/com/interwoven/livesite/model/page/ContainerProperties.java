package com.interwoven.livesite.model.page;

import com.interwoven.livesite.common.xml.XmlEmittable;
import com.interwoven.livesite.common.xml.XmlParseable;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.model.ContainerIfc;
import com.interwoven.livesite.system.Constants;
import java.io.Serializable;
import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.*;

public class ContainerProperties
    implements ContainerIfc, Serializable, XmlEmittable, XmlParseable
{
    public static class FixedLayoutIndexComparator
        implements Comparator
    {

        public int compare(Object o1, Object o2)
        {
            int ret = -1;
            if((o1 instanceof ContainerProperties) && (o2 instanceof ContainerProperties))
            {
                ContainerProperties props1 = (ContainerProperties)o1;
                ContainerProperties props2 = (ContainerProperties)o2;
                ret = props1.getFixedLayoutAreaIndex() - props2.getFixedLayoutAreaIndex();
            } else
            {
                throw new RuntimeException((new StringBuilder()).append("One or more invalid object types found: object1: ").append(o1.getClass().getName()).append(", object2: ").append(o2.getClass().getName()).append(". This comparator only supports ContainerProperties.").toString());
            }
            return ret;
        }

        public FixedLayoutIndexComparator()
        {
        }
    }


    public ContainerProperties(String xml)
    {
        mProperties = null;
        mID = "0";
        mPageEditVisibility = "";
        mLogger = LogFactory.getLog(getClass());
        mProperties = Dom4jUtils.newDocument(xml).getRootElement();
        mID = mProperties.attributeValue("ID", "0");
        fixRenderInRuntimeElement(mProperties);
        fixFixedLayoutAreaElement(mProperties);
        initDimensions();
    }

    public ContainerProperties(Element xml)
    {
        mProperties = null;
        mID = "0";
        mPageEditVisibility = "";
        mLogger = LogFactory.getLog(getClass());
        mProperties = xml;
        mID = mProperties.attributeValue("ID", "0");
        fixRenderInRuntimeElement(mProperties);
        fixFixedLayoutAreaElement(mProperties);
        initDimensions();
    }

    public ContainerProperties(ContainerProperties cp)
    {
        this(cp.getRootElement().createCopy());
        mID = cp.mID;
        fixRenderInRuntimeElement(mProperties);
        fixFixedLayoutAreaElement(mProperties);
    }

    private void initDimensions()
    {
        mTop = Long.parseLong(mProperties.element("Top").getText());
        mLeft = Long.parseLong(mProperties.element("Left").getText());
        mWidth = Long.parseLong(mProperties.element("Width").getText());
        mHeight = Long.parseLong(mProperties.element("Height").getText());
    }

    public static void emitDefaultModel(Element base)
    {
        base.addAttribute("ID", "");
        base.addAttribute("locked", "false");
        Element eLayout = base.addElement("FixedLayoutArea");
        eLayout.addAttribute("ID", "");
        eLayout.addAttribute("index", "0");
        base.addElement("CacheTime").addText("0");
        base.addElement("BGColor");
        base.addElement("Width").addText("100");
        base.addElement("Height").addText("100");
        base.addElement("Top").addText("100");
        base.addElement("Left").addText("100");
        base.addElement("ZIndex").addText("0");
        base.addElement("RenderInRuntime").addText("true");
    }

    public ContainerProperties(long cacheTime, String backgroundColor, long width, long height, 
            long top, long left, long zIndex, boolean renderInRuntime)
    {
        mProperties = null;
        mID = "0";
        mPageEditVisibility = "";
        mLogger = LogFactory.getLog(getClass());
        mProperties = DocumentHelper.createElement("ContainerProperties");
        mProperties.addAttribute("ID", "");
        mProperties.addAttribute("locked", "false");
        Element eLayout = mProperties.addElement("FixedLayoutArea");
        eLayout.addAttribute("ID", "");
        eLayout.addAttribute("index", "0");
        mProperties.addElement("CacheTime").addText(Long.toString(cacheTime));
        mProperties.addElement("BGColor").addText(backgroundColor);
        mProperties.addElement("Width").addText(Long.toString(width));
        mProperties.addElement("Height").addText(Long.toString(height));
        mProperties.addElement("Top").addText(Long.toString(top));
        mProperties.addElement("Left").addText(Long.toString(left));
        mProperties.addElement("ZIndex").addText(Long.toString(zIndex));
        mProperties.addElement("RenderInRuntime").addText(String.valueOf(renderInRuntime));
        mTop = top;
        mLeft = left;
        mWidth = width;
        mHeight = height;
    }

    public Element getProperties()
    {
        return mProperties;
    }

    public Element getRootElement()
    {
        return mProperties;
    }

    public void setID(String id)
    {
        mID = id;
    }

    public String getID()
    {
        return mID;
    }

    public void setFixedLayoutAreaIndex(int index)
    {
        mProperties.element("FixedLayoutArea").addAttribute("index", String.valueOf(index));
    }

    public int getFixedLayoutAreaIndex()
    {
        return Integer.parseInt(mProperties.element("FixedLayoutArea").attributeValue("index", "0"));
    }

    public void setFixedLayoutAreaId(String areaId)
    {
        mProperties.element("FixedLayoutArea").addAttribute("ID", areaId);
    }

    public String getFixedLayoutAreaId()
    {
        return mProperties.element("FixedLayoutArea").attributeValue("ID", "");
    }

    public void setCacheTime(long cacheTime)
    {
        mProperties.element("CacheTime").setText(String.valueOf(cacheTime));
    }

    public long getCacheTime()
    {
        return Long.parseLong(mProperties.element("CacheTime").getText());
    }

    public void setBackgroundColor(String backgroundColor)
    {
        mProperties.element("BGColor").setText(backgroundColor);
    }

    public String getBackgroundColor()
    {
        return mProperties.element("BGColor").getText();
    }

    public void setWidth(long width)
    {
        mProperties.element("Width").setText(String.valueOf(width));
        mWidth = width;
    }

    public long getWidth()
    {
        return mWidth;
    }

    public void setHeight(long height)
    {
        mProperties.element("Height").setText(String.valueOf(height));
        mHeight = height;
    }

    public long getHeight()
    {
        return mHeight;
    }

    public void setTop(long top)
    {
        mProperties.element("Top").setText(String.valueOf(top));
        mTop = top;
    }

    public long getTop()
    {
        return mTop;
    }

    public void setLeft(long left)
    {
        mProperties.element("Left").setText(String.valueOf(left));
        mLeft = left;
    }

    public long getLeft()
    {
        return mLeft;
    }

    public void setZIndex(long zIndex)
    {
        mProperties.element("ZIndex").setText(String.valueOf(zIndex));
    }

    public long getZIndex()
    {
        return Long.parseLong(mProperties.element("ZIndex").getText());
    }

    public void setRenderInRuntime(boolean render)
    {
        mProperties.element("RenderInRuntime").setText(String.valueOf(render));
    }

    public boolean getRenderInRuntime()
    {
        return !Constants.FALSE.equals(mProperties.element("RenderInRuntime").getText());
    }

    public String getPageEditVisibility()
    {
        return mPageEditVisibility;
    }

    public void setPageEditVisibility(String visibility)
    {
        mPageEditVisibility = visibility;
    }

    public void setWidthChanged(boolean changed)
    {
        mProperties.element("Width").addAttribute("Changed", String.valueOf(changed));
    }

    public boolean getWidthChanged()
    {
        return Boolean.valueOf(mProperties.element("Width").attributeValue("Changed")).booleanValue();
    }

    public void setHeightChanged(boolean changed)
    {
        mProperties.element("Height").addAttribute("Changed", String.valueOf(changed));
    }

    public boolean getHeightChanged()
    {
        return Boolean.valueOf(mProperties.element("Height").attributeValue("Changed")).booleanValue();
    }

    public void setTopChanged(boolean changed)
    {
        mProperties.element("Top").addAttribute("Changed", String.valueOf(changed));
    }

    public boolean getTopChanged()
    {
        return Boolean.valueOf(mProperties.element("Top").attributeValue("Changed")).booleanValue();
    }

    public void setLeftChanged(boolean changed)
    {
        mProperties.element("Left").addAttribute("Changed", String.valueOf(changed));
    }

    public boolean getLeftChanged()
    {
        return Boolean.valueOf(mProperties.element("Left").attributeValue("Changed")).booleanValue();
    }

    public void setCacheTimeChanged(boolean changed)
    {
        mProperties.element("CacheTime").addAttribute("Changed", String.valueOf(changed));
    }

    public boolean getCacheTimeChanged()
    {
        return Boolean.valueOf(mProperties.element("CacheTime").attributeValue("Changed")).booleanValue();
    }

    public void setBackgroundColorChanged(boolean changed)
    {
        mProperties.element("BGColor").addAttribute("Changed", String.valueOf(changed));
    }

    public boolean getBackgroundColorChanged()
    {
        return Boolean.valueOf(mProperties.element("BGColor").attributeValue("Changed")).booleanValue();
    }

    public void setRenderInRuntimeChanged(boolean changed)
    {
        mProperties.element("RenderInRuntime").addAttribute("Changed", String.valueOf(changed));
    }

    public boolean getRenderInRuntimeChanged()
    {
        return Boolean.valueOf(mProperties.element("RenderInRuntime").attributeValue("Changed")).booleanValue();
    }

    public void clearChanged()
    {
        mProperties.element("Top").addAttribute("Changed", null);
        mProperties.element("Left").addAttribute("Changed", null);
        mProperties.element("Width").addAttribute("Changed", null);
        mProperties.element("Height").addAttribute("Changed", null);
        mProperties.element("CacheTime").addAttribute("Changed", null);
        mProperties.element("BGColor").addAttribute("Changed", null);
        Element renderInRuntime = mProperties.element("RenderInRuntime");
        if(null != renderInRuntime)
            renderInRuntime.addAttribute("Changed", null);
    }

    public String getXML()
    {
        return mProperties.toString();
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        buff.append((new StringBuilder()).append("ID[").append(mID).append("]\n").toString());
        buff.append((new StringBuilder()).append("props[\n").append(mProperties.asXML()).append("\n]\n").toString());
        return buff.toString();
    }

    public void updateFromBase(ContainerProperties baseContainerProperties, boolean overwrite)
    {
        if(overwrite)
            clearChanged();
        if(!getBackgroundColorChanged())
            setBackgroundColor(baseContainerProperties.getBackgroundColor());
        if(!getCacheTimeChanged())
            setCacheTime(baseContainerProperties.getCacheTime());
        if(!getHeightChanged())
            setHeight(baseContainerProperties.getHeight());
        if(!getWidthChanged())
            setWidth(baseContainerProperties.getWidth());
        if(!getLeftChanged())
            setLeft(baseContainerProperties.getLeft());
        if(!getTopChanged())
            setTop(baseContainerProperties.getTop());
        if(!getRenderInRuntimeChanged())
            setRenderInRuntime(baseContainerProperties.getRenderInRuntime());
        //setFixedLayoutAreaId(baseContainerProperties.getFixedLayoutAreaId());
    }

    public Element toElement()
    {
        Element containerProperties = mProperties.createCopy();
        return containerProperties.addAttribute("ID", mID);
    }

    public Element toElement(String elementName)
    {
        mLogger.warn("Renaming the root element is not supported.");
        return toElement();
    }

    public void parse(Element root)
    {
        mProperties = root;
        mID = root.attributeValue("ID", "0");
    }

    private void fixRenderInRuntimeElement(Element root)
    {
        if(root.element("RenderInRuntime") == null)
            root.addElement("RenderInRuntime").addText("true");
    }

    private void fixFixedLayoutAreaElement(Element root)
    {
        if(root.element("FixedLayoutArea") == null)
        {
            Element eLayout = root.addElement("FixedLayoutArea");
            eLayout.addAttribute("ID", "");
            eLayout.addAttribute("index", "0");
        }
    }

    static final long serialVersionUID = 0xb232351dL;
    public static final String ELEMENT = "ContainerProperties";
    private static final String CHANGED = "Changed";
    private static final String ZINDEX = "ZIndex";
    private static final String LEFT = "Left";
    private static final String TOP = "Top";
    private static final String WIDTH = "Width";
    private static final String HEIGHT = "Height";
    private static final String BACKGROUND_COLOR = "BGColor";
    public static final String CACHE_TIME = "CacheTime";
    private static final String FIXED_LAYOUT_AREA = "FixedLayoutArea";
    private static final String RENDER_IN_RUNTIME = "RenderInRuntime";
    private Element mProperties;
    private String mID;
    private long mTop;
    private long mLeft;
    private long mWidth;
    private long mHeight;
    private String mPageEditVisibility;
    protected final Log mLogger;
}

