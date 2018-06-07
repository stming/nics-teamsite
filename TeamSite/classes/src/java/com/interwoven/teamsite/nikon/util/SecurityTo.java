package com.interwoven.teamsite.nikon.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SecurityTo {
	
	private Map<String, String> mapOfFilesSuccessfullyCopied = new LinkedHashMap<String, String>();
	private List<String> listOfFilesNotCopied = new LinkedList<String>();
	
	public Map<String, String> getMapOfFilesSuccessfullyCopied() {
		return mapOfFilesSuccessfullyCopied;
	}
	
	public void setMapOfFilesSuccessfullyCopied(
			Map<String, String> mapOfFilesSuccessfullyCopied) {
		this.mapOfFilesSuccessfullyCopied = mapOfFilesSuccessfullyCopied;
	}
	
	public List<String> getListOfFilesNotCopied() {
		return listOfFilesNotCopied;
	}
	
	public void setListOfFilesNotCopied(List<String> listOfFilesNotCopied) {
		this.listOfFilesNotCopied = listOfFilesNotCopied;
	}
}
