package com.interwoven.teamsite.nikon.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.dom4j.Document;

import com.interwoven.livesite.dom4j.Dom4jUtils;

public class NikonStaticFileRepository implements NikonRepository {
	
	private String basePath;
	private String localeFallbackDCRPath;
	private boolean enable;
	private boolean cacheEnable;
	private boolean commerceCacheEnable;

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getLocaleFallbackDCRPath() {
		return localeFallbackDCRPath;
	}

	public void setLocaleFallbackDCRPath(String localeFallbackDCRPath) {
		this.localeFallbackDCRPath = localeFallbackDCRPath;
	}

	public InputStream retrieveContent(String type, String name, String locale) throws FileNotFoundException {
		File file = new File(this.basePath + "/" + locale + "/" + type + "/" + name);
		if (file.exists()){
			return new FileInputStream(file);
		}else{
			return null;
		}
	}
	
	public InputStream retreieveContentWithFallbackSupport(String type, String name, 
			Collection<String> locales) throws FileNotFoundException {
		for (String l: locales){
			File file = new File(this.basePath + "/" + l + "/" + type + "/" + name);
			if (file.exists()){
				return new FileInputStream(file);
			}
		}
		return null;
	}

	public OutputStream writeContent(String type, String name, String locale) throws IOException {
		File file = new File(this.basePath + "/" + locale + "/" + type + "/" + name);
		if (!file.exists()){
			if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return new FileOutputStream(file);
	}
	
	
	public Document retrieveLocaleFallbackDocument() {
		try {
			return Dom4jUtils.newDocument(new FileInputStream( new File(this.localeFallbackDCRPath)));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public void setCacheEnable(boolean cacheEnable) {
		this.cacheEnable = cacheEnable;
	}

	public boolean isCacheEnable() {
		return cacheEnable;
	}

	public boolean isCommerceCacheEnable() {
		return commerceCacheEnable;
	}

	public void setCommerceCacheEnable(boolean commerceCacheEnable) {
		this.commerceCacheEnable = commerceCacheEnable;
	}





}
