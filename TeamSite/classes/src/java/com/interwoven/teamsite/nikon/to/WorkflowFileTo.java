package com.interwoven.teamsite.nikon.to;

import java.io.File;

import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.sci.filesys.CSFile;
import com.interwoven.teamsite.ext.to.TeamsiteTo;


/**
 * Transfer Object class for Workflow files
 * @author nbamford
 *
 */
public class WorkflowFileTo 
implements TeamsiteTo {
	

	File file;
    CSFile csFile;
    CSSimpleFile csSimpleFile;
    CSAreaRelativePath csAreaRelativePath;
    
    public WorkflowFileTo(CSAreaRelativePath csAreaRelativePath, File file, CSFile csFile, CSSimpleFile csSimpleFile)
    {
    	super();
    	this.file = file;
    	this.csFile = csFile;
    	this.csSimpleFile = csSimpleFile;
    	this.csAreaRelativePath = csAreaRelativePath;
    }
	
    public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public CSFile getCsFile() {
		return csFile;
	}
	public void setCsFile(CSFile csFile) {
		this.csFile = csFile;
	}
	public CSSimpleFile getCsSimpleFile() {
		return csSimpleFile;
	}
	public void setCsSimpleFile(CSSimpleFile csSimpleFile) {
		this.csSimpleFile = csSimpleFile;
	}
	public String getExtension()
	{
		return csAreaRelativePath.getExtension();
	}
	
	public CSAreaRelativePath getCsAreaRelativePath() {
		return csAreaRelativePath;
	}
	public void setCsAreaRelativePath(CSAreaRelativePath csAreaRelativePath) {
		this.csAreaRelativePath = csAreaRelativePath;
	}
    
}
