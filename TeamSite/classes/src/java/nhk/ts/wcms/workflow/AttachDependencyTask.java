package nhk.ts.wcms.workflow;import java.io.FileOutputStream;import java.io.PrintStream;import java.util.Hashtable;import nhk.ts.wcms.common.Logger;import nhk.ts.wcms.common.TSHelper;import nhk.ts.wcms.common.URLTasksHelper;import org.apache.commons.logging.LogFactory;import com.interwoven.cssdk.common.CSClient;import com.interwoven.cssdk.common.CSException;import com.interwoven.cssdk.filesys.CSAreaRelativePath;import com.interwoven.cssdk.filesys.CSFile;import com.interwoven.cssdk.filesys.CSSimpleFile;import com.interwoven.cssdk.workflow.CSExternalTask;import com.interwoven.cssdk.workflow.CSURLExternalTask;import com.interwoven.cssdk.workflow.CSWorkflow;public class AttachDependencyTask implements CSURLExternalTask {    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.AttachDependencyTask"));        @SuppressWarnings({ "rawtypes" })    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {            	try {                    	CSWorkflow workflow = task.getWorkflow();                        String TSFileList = TSHelper.getIWHome() + "/nikon_custom/TSFileList_" + workflow.getId() + ".txt";            String DCRFileList = TSHelper.getIWHome() + "/nikon_custom/DCRFileList_" + workflow.getId() + ".txt";                        CSAreaRelativePath[] areaRelativePaths = task.getFiles();            CSAreaRelativePath[] depFilesRelPaths = null;                        for (CSAreaRelativePath areaRelativePath : areaRelativePaths) {                            	CSFile tmp = task.getArea().getFile(areaRelativePath);                CSSimpleFile sourceSimpleFile = null;                                if (tmp instanceof CSSimpleFile) {                                    	sourceSimpleFile = (CSSimpleFile) tmp;                    depFilesRelPaths = URLTasksHelper.addDependentFiles(sourceSimpleFile, task, client);                    task.attachFiles(depFilesRelPaths);                                        mLogger.createLogDebug("Creating File List text file :: ");                                        try {                                            	FileOutputStream out = new FileOutputStream(TSFileList);                        PrintStream p = new PrintStream(out);                        FileOutputStream dout = new FileOutputStream(                                DCRFileList);                        PrintStream dp = new PrintStream(dout);                        CSAreaRelativePath[] myfiles = task.getFiles();                        for (int j = 0; j < myfiles.length; j++) {                            if (myfiles[j].toString().contains("templatedata")) {                                dp.println(myfiles[j].toString());                            }                            p.println(myfiles[j].toString());                        }                        p.close();                                        } catch (Exception e) {                                        	mLogger.createLogDebug("Error in creating file list:", e);                    }                }            }                        // choose transition from WF variable            task.chooseTransition(task.getTransitions()[0], "successfully attached the files");                    } catch (CSException exception) {                    	mLogger.createLogDebug("Error in Attach Dependency task: ", exception);        }    }}