package com.interwoven.teamsite.nikon.to;

import java.io.File;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.interwoven.livesite.common.xml.XmlEmittable;
import com.interwoven.teamsite.ext.to.TeamsiteTo;
import com.interwoven.teamsite.ext.util.FormatUtils;

public class AssetTo
implements TeamsiteTo, XmlEmittable, Comparable<AssetTo>
{

	String id;
	String uid;
	String name;
	String colourSpace;
	String imageDimensions;
	String size;
	String thumbnailURL;
	String URL;
	File file;

	public AssetTo(){}
	
	public AssetTo(String name, String colourSpace, String imageDimensions,
			String size, String thumbnailURL, String url, File file) {
		super();
		this.name = name;
		this.colourSpace = colourSpace;
		this.imageDimensions = imageDimensions;
		this.size = size;
		this.thumbnailURL = thumbnailURL;
		this.URL = url;
		this.file = file;
	}
	
	public Element toElement() {
		Element e = DocumentFactory.getInstance().createElement("Asset");
		e.addAttribute("id", id);
		e.addAttribute("uid", uid);
		e.addAttribute("name", name);
		e.addAttribute("colourSpace", colourSpace);
		e.addAttribute("imageDimensions", imageDimensions);
		e.addAttribute("size", size);
		e.addAttribute("thumbnail", thumbnailURL);
		e.setText(URL);
		return e;
	}

	public Element toElement(String arg0) {
		id = arg0;
		return toElement();
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(FormatUtils.mFormat("[id:{0}, ", id));
		sb.append(FormatUtils.mFormat("uid:{0}, ", uid));
		sb.append(FormatUtils.mFormat("name:{0}, ", name));
		sb.append(FormatUtils.mFormat("colourSpace:{0}, ", colourSpace));
		sb.append(FormatUtils.mFormat("imageDimensions:{0}, ", imageDimensions));
		sb.append(FormatUtils.mFormat("size:{0}, ", size));
		sb.append(FormatUtils.mFormat("thumbnailURL:{0}, ", thumbnailURL));
		sb.append(FormatUtils.mFormat("URL:{0}]", thumbnailURL));
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColourSpace() {
		return colourSpace;
	}

	public void setColourSpace(String colourSpace) {
		this.colourSpace = colourSpace;
	}

	public String getImageDimensions() {
		return imageDimensions;
	}

	public void setImageDimensions(String imageDimensions) {
		this.imageDimensions = imageDimensions;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getThumbnailURL() {
		return thumbnailURL;
	}

	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String url) {
		URL = url;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int compareTo(AssetTo o) {
		// TODO Auto-generated method stub
		String c1 = name!=null?name:"";
		String c2 = o.getName()!=null?o.getName():"";
		
		return c1.compareTo(c2);
	}

}
