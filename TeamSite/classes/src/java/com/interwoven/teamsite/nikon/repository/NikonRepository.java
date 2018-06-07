package com.interwoven.teamsite.nikon.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.dom4j.Document;

public interface NikonRepository {
	
	public InputStream retrieveContent (String type, String name, String locale) throws IOException;
	public OutputStream writeContent (String type, String name, String locale) throws IOException;
	public InputStream retreieveContentWithFallbackSupport(String type, String name,
			Collection<String> locales) throws FileNotFoundException;
	public Document retrieveLocaleFallbackDocument ();
	public boolean isEnable();
	public boolean isCacheEnable();
	public boolean isCommerceCacheEnable();

}
