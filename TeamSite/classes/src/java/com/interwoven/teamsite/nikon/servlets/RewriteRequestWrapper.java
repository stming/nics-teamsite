package com.interwoven.teamsite.nikon.servlets;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RewriteRequestWrapper extends HttpServletRequestWrapper {

	private String requestURI;
	private Hashtable params;
	public RewriteRequestWrapper(HttpServletRequest request) {
		super(request);
	}
	
	public RewriteRequestWrapper(HttpServletRequest request, String requestURI, Hashtable params) {
		super(request);
		this.requestURI = requestURI;
		this.params = params;
	}
	
	

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getScheme());
		buffer.append("://");
		buffer.append(this.getServerName());
		buffer.append(this.requestURI);
		return buffer;
	}

	@Override
	public String getRequestURI() {
		return this.requestURI;
	}

	@Override
	public String getParameter(String name) {
		Object values = params.get(name);
		if (values == null){
			return null;
		}else if (values instanceof String[]){
			return ((String[]) values)[0];
		}else if (values instanceof String){
			return (String) values;
		}else{
			return null;
		}
	}
	
	@Override
	public Enumeration getParameterNames() {
		return params.keys();
	}
	@Override
	public Map getParameterMap() {
		return params;
	}



	@Override
	public String[] getParameterValues(String name) {
		Object values = params.get(name);
		if (values == null){
			return null;
		}else if (values instanceof String[]){
			return (String[]) values;
		}else if (values instanceof String){
			return new String[]{(String) values};
		}else{
			return null;
		}
	}
	

}
