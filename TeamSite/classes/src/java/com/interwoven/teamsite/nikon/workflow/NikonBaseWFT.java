package com.interwoven.teamsite.nikon.workflow;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.sci.filesys.CSFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.to.WorkflowFileTo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Abstract class for all custom workflow tasks
 * @author nbamford
 *
 */
public abstract class NikonBaseWFT {
	
	private Log log = LogFactory.getLog(NikonBaseWFT.class);

	//Method to return all the files attached to the task to be processed/
	//TODO Change List<WorkflowFileTo> to some to which will hold more information than
	//just the File
	List<WorkflowFileTo> getFiles(CSExternalTask csExternalTask)
	{
		List<WorkflowFileTo> retList = new LinkedList<WorkflowFileTo>();

		try {
			CSAreaRelativePath[] files = csExternalTask.getFiles();

			for(com.interwoven.cssdk.filesys.CSAreaRelativePath cs:files)
			{
				String vPath = csExternalTask.getArea().getBranch().getVPath().toString();
				String parentPath = cs.getParentPath().toString();
				String fileName = cs.getName();
				
				vPath = csExternalTask.getArea().getBranch().getWorkareas()[0].getVPath().toString();
				String extension = cs.getExtension();
				
				String fullFileName = FormatUtils.mFormat("{0}/{1}/{2}", vPath, parentPath, fileName);
				String relativePath = FormatUtils.mFormat("{0}/{1}", parentPath, fileName);
				
				try
				{
					File file = new File(fullFileName);
					CSFile csFile = new CSFile(fullFileName);
					CSAreaRelativePath relPathFile = new CSAreaRelativePath(relativePath);
					CSSimpleFile csSimple = (CSSimpleFile)csExternalTask.getArea().getFile(relPathFile);
					
					
					WorkflowFileTo workflowFileTo = new WorkflowFileTo(cs, file, csFile, csSimple);  
					log.debug(FormatUtils.mFormat("X---> fullFileName:{0}", fullFileName));
					log.debug(FormatUtils.mFormat("Y---> fullFileName:{0}", file.getAbsolutePath()));
					retList.add(workflowFileTo);
				}
				catch(Exception exception)
				{
					log.error("Exception", exception);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Exception", e);
		}
		return retList;
	}
}
