package com.interwoven.teamsite.nikon.workflow;

import java.util.*;
import org.apache.log4j.Logger;
import java.util.regex.Pattern;
import com.interwoven.modeler.workflow.*;
import com.interwoven.modeler.workflow.commands.InProcessJavaCommand;

public class InProcessCheckSubmitFiles implements InProcessJavaCommand{
	private static final Logger logger = Logger.getLogger(InProcessCheckSubmitFiles.class);
	
    public WFMWorkflow execute(WFMWorkflow workflow, Map<String,String> params){
    	//get all files submitted
        WFMFile[] arrFiles = workflow.getFiles();
        int arrsize = arrFiles.length;
        boolean containProdNewsDCR = false;
        
        
        for (int i=0;i<arrsize;i++){
        	logger.debug("Submitted File: "+arrFiles[i].getFilePath());
        	if(Pattern.matches(".+templatedata\\/[^\\/]+\\/(product_information_container|taggable_content|main_feature)\\/.+", arrFiles[i].getFilePath())){
        		containProdNewsDCR = true;
        		logger.debug("set containProdNewsDCR=true");
        	}
        }
        try{
        	if(containProdNewsDCR){
            	workflow.updateVariable("containProductNewsDCR", "yes");
            }else{
            	workflow.updateVariable("containProductNewsDCR", "no");
            }
        }catch(Exception e){
        	logger.error(e.getMessage());
        }
        return workflow;
    }
}
