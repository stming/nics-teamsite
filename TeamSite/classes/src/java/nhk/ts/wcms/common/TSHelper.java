package nhk.ts.wcms.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIllegalOpException;
import com.interwoven.cssdk.common.CSInstallationInfo;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.common.CSModule;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSPatch;
import com.interwoven.cssdk.common.CSProduct;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.common.CSServerBusyException;
import com.interwoven.cssdk.common.CSServicePack;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSBranch;
import com.interwoven.cssdk.filesys.CSConflictException;
import com.interwoven.cssdk.filesys.CSDir;
import com.interwoven.cssdk.filesys.CSEdition;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSFileKindMask;
import com.interwoven.cssdk.filesys.CSObjectAlreadyExistsException;
import com.interwoven.cssdk.filesys.CSPathCommentPair;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSSortKey;
import com.interwoven.cssdk.filesys.CSStaging;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.serverutils100.InstalledLocations;

import com.interwoven.livesite.model.page.Page;

public class TSHelper {

    static CSClient client = null;
    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.common.TSHelper"));

    public TSHelper(CSClient client) {
        TSHelper.client = client;
    }

    /**
     * Gets a {@link Log} object for the specified Class
     * @param clazz - class for which the {@link Log} object is created
     * @return {@link Log} for the requested Class
     */
    public static boolean isValidFile(CSFile file) {
        if (file != null && file.isValid()) {
            return true;
        }
        return false;
    }

    public static Page getPageProperties() {


        return null;
    }

    public static String getTaskVariable(CSTask task, String varName) throws CSException {
        String value = null;
        if (task != null && !StringUtils.isBlank(varName)) {
            value = task.getVariable(varName);
            if (StringUtils.isBlank(value)) {
                return value;
            }
        }
        return value;
    }

    public static String getWorkFlowVariable(CSWorkflow workflow, String varName) throws CSException {
        String value = null;
        if (workflow != null && !StringUtils.isBlank(varName)) {
            value = workflow.getVariable(varName);
            if (StringUtils.isBlank(value)) {
                return value;
            }
        }
        return value;
    }

    public static String getExtendedAttribute(CSFile file, String eakey) {
        String value = "";
        try {
            if (isValidFile(file) && file instanceof CSSimpleFile) {
                CSSimpleFile simpleFile = (CSSimpleFile) file;
                if (simpleFile.getExtendedAttribute(eakey) != null) {
                    value = simpleFile.getExtendedAttribute(eakey).getValue();
                }
            }
        } catch (CSException e) {
            mLogger.createLogDebug("Error in getExtendedAttribute::", e);
        }
        return value;
    }

    public static boolean isSubBranch(CSBranch branch, String branchName) throws CSException {
        boolean flag = true;
        CSBranch[] br = branch.getSubBranches();
        if (br.length > 0) {
            for (CSBranch eachBranch : br) {
                if (eachBranch.getName().equals(branchName)) {
                    flag = false;
                    continue;
                }
            }
        }
        return flag;
    }

    public static void getAllFilesFromEdition(ArrayList<CSSimpleFile> returnList, CSEdition Edition) throws CSException {
        CSSortKey[] sortArray = {new CSSortKey(CSSortKey.NAME, true)};
        CSIterator csFiles = Edition.getFiles(CSFileKindMask.ALLTYPES, sortArray, CSFileKindMask.ALLFILES, null, 0, -1);
        if (csFiles != null) {
            ArrayList<CSFile> files = new ArrayList<CSFile>();
            CollectionUtils.addAll(files, csFiles);
            for (CSFile file : files) {
                if (file instanceof CSDir) {
                    getAllFilesFromDir(returnList, (CSDir) file);
                }
                if (file instanceof CSSimpleFile) {
                    returnList.add((CSSimpleFile) file);
                }
            }
        }
        return;
    }

    public static void getAllFilesFromStaging(ArrayList<CSSimpleFile> returnList, CSStaging staging) throws CSException {
        CSSortKey[] sortArray = {new CSSortKey(CSSortKey.NAME, true)};
        CSIterator csFiles = staging.getFiles(CSFileKindMask.ALLTYPES, sortArray, CSFileKindMask.ALLFILES, null, 0, -1);
        if (csFiles != null) {
            ArrayList<CSFile> files = new ArrayList<CSFile>();
            CollectionUtils.addAll(files, csFiles);
            for (CSFile file : files) {
                if (file instanceof CSDir) {
                    getAllFilesFromDir(returnList, (CSDir) file);
                }
                if (file instanceof CSSimpleFile) {
                    returnList.add((CSSimpleFile) file);
                }
            }
        }
        return;
    }

    public static void getFilesFromDir(ArrayList<CSAreaRelativePath> returnList, CSDir dir) throws CSException {
        CSSortKey[] sortArray = {new CSSortKey(CSSortKey.NAME, true)};
        CSIterator csFiles = dir.getFiles(CSFileKindMask.ALLTYPES, sortArray, CSFileKindMask.ALLFILES, null, 0, -1);
        if (csFiles != null) {
            ArrayList<CSFile> files = new ArrayList<CSFile>();
            CollectionUtils.addAll(files, csFiles);
            for (CSFile file : files) {
                if (file instanceof CSDir) {
                    getFilesFromDir(returnList, (CSDir) file);
                }
                if (file instanceof CSSimpleFile) {
                    returnList.add(((CSSimpleFile) file).getVPath().getAreaRelativePath());
                }
            }
        }
        return;
    }

    public static void getAllFilesFromDir(ArrayList<CSSimpleFile> returnList, CSDir dir) throws CSException {
        CSSortKey[] sortArray = {new CSSortKey(CSSortKey.NAME, true)};
        CSIterator csFiles = dir.getFiles(CSFileKindMask.ALLTYPES, sortArray, CSFileKindMask.ALLFILES, null, 0, -1);
        if (csFiles != null) {
            ArrayList<CSFile> files = new ArrayList<CSFile>();
            CollectionUtils.addAll(files, csFiles);
            for (CSFile file : files) {
                if (file instanceof CSDir) {
                    getAllFilesFromDir(returnList, (CSDir) file);
                }
                if (file instanceof CSSimpleFile) {
                    returnList.add((CSSimpleFile) file);
                }
            }
        }
        return;
    }

    public synchronized static void createNewWorkFlow(String fileName) throws Exception {
        executeCommand(TSHelper.getIWHome() + Constants.filePathSep + IOHelper.getString("TSHelper.jobSpecCommand") + Constants.BLANK + fileName);
    }

    /**
     * Creates a list of area relative paths of all the file paths stored as comma separated values in the specified workflow variable
     * @param workFlowVariable
     * @return CSAreaRelativePath[]
     */
    public static CSAreaRelativePath[] getRelativePaths(String workFlowVariable) {
        ArrayList<CSAreaRelativePath> al = new ArrayList<CSAreaRelativePath>();
        if (!StringUtils.isBlank(workFlowVariable)) {
            String list[] = StringUtils.split(workFlowVariable.trim(), Constants.delimiter);
            for (String path : list) {
                if (!StringUtils.isBlank(path)) {
                    al.add(new CSVPath(path).getAreaRelativePath());
                }
            }
        }
        return al.toArray(new CSAreaRelativePath[0]);
    }

    public static boolean createFolderStructure(CSClient client, CSVPath vpath) throws CSAuthorizationException, CSExpiredSessionException, CSRemoteException, CSException {

        mLogger.createLogDebug("vpath---> " + vpath);
        mLogger.createLogDebug("AreaRelativePath---> " + vpath.getAreaRelativePath());
        CSVPath area = vpath.getArea();
        CSFile file = client.getFile(area);
        if (!file.isValid()) {
            return false;
        } else {
            CSWorkarea wa = client.getWorkarea(area, true);
            CSAreaRelativePath areaRelativePath = null;

            String path = vpath.toString();

            // Assumption:The path has a file reference if it has a "." in the
            // vpath and the file name will be skipped, else its considered a
            // folder and
            // the folder structure will be created
            if (StringUtils.indexOf(path, ".") != -1) {
                areaRelativePath = vpath.getAreaRelativePath().getParentPath();
            } else {
                areaRelativePath = vpath.getAreaRelativePath();
            }

            if (areaRelativePath != null && !areaRelativePath.toString().equals("")) {
                String dirs[] = areaRelativePath.toString().split(Constants.filePathSep);
                String buildPath = "";
                for (String string : dirs) {
                    mLogger.createLogDebug("dirs :" + dirs);
                    buildPath = buildPath + string + Constants.filePathSep;
                    try {
                        wa.createDirectory(new CSVPath(buildPath).getAreaRelativePath());
                    } catch (CSObjectAlreadyExistsException exp) {
                        continue;
                    }
                }
            }
        }
        return true;
    }

    public static CSSimpleFile createSimpleFileInWA(CSClient client, CSVPath vpath) throws CSException {

        CSFile file = client.getFile(vpath);
        if (file == null) {
            CSWorkarea workarea = client.getWorkarea(vpath.getArea(), true);
            file = workarea.createSimpleFile(vpath.getAreaRelativePath());
        }
        if (file instanceof CSSimpleFile) {
            return (CSSimpleFile) file;
        }
        return null;
    }

    public static byte[] getBytesFromFile(CSFile csFile) throws IOException, CSException {

        if (csFile instanceof CSSimpleFile) {
            CSSimpleFile simpleFile = (CSSimpleFile) csFile;
            InputStream is = simpleFile.getInputStream(true);

            long length = simpleFile.getSize();
            if (length > Integer.MAX_VALUE) {
                // File is too large
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new CSException(1, "Could not completely read file " + csFile.getName());
            }

            // Close the input stream and return bytes
            is.close();
            return bytes;
        }
        return null;

    }

    public static CSWorkarea getWorkarea(CSBranch branch, String wANameString) throws CSException {
        CSWorkarea[] wArea = branch.getWorkareas();
        if (wArea.length > 0) {
            for (CSWorkarea wa : wArea) {
                if (wa.getName().equals(wANameString)) {
                    return wa;
                }
            }
        }
        return null;
    }

    public static void batchSubmitFiles(CSWorkarea workArea, CSAreaRelativePath[] array) throws CSAuthorizationException, CSObjectNotFoundException, CSRemoteException, CSIllegalOpException, CSConflictException, CSServerBusyException, CSExpiredSessionException, CSException {
        String fileComment = IOHelper.getString("CreateNewBranchTask.fileComment");
        String comment = IOHelper.getString("CreateNewBranchTask.comment");
        String infoComment = IOHelper.getString("CreateNewBranchTask.infoComment");
        ArrayList<CSPathCommentPair> pathCommentPairs = new ArrayList<CSPathCommentPair>();
        for (CSAreaRelativePath csAreaRelativePath : array) {
            CSPathCommentPair pathCommentPair = new CSPathCommentPair(csAreaRelativePath, fileComment);
            pathCommentPairs.add(pathCommentPair);
        }
        if (pathCommentPairs.size() > 0) {
            workArea.submitDirect(comment, infoComment, pathCommentPairs.toArray(new CSPathCommentPair[0]), CSWorkarea.OVERWRITE_ALL);
        }
    }

    public static Document getDocument(CSSimpleFile file) {
        Document document = null;
        try {
            document = Dom4jUtils.newDocument(file.getInputStream(true));
        } catch (CSException cexp) {
            mLogger.createLogDebug("Error in getDcoument::", cexp);
        }
        return document;
    }

    public static String getIWHome() {
        return InstalledLocations.getIWHome();
    }

    public static String getODHome() throws IOException {
        String ODHOME = "";
        if (InstalledLocations.isWindows()) {
            ODHOME = System.getenv("IWOD60HOME");
            return ODHOME;
        } else if (InstalledLocations.isSolaris() || InstalledLocations.isLinux()) {
            try {
                File file = new File("/etc/defaultiwod60home");
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
                String temp = "";
                while ((temp = br.readLine()) != null) {
                    ODHOME = temp;
                }
                br.close();
                return ODHOME;
            } catch (FileNotFoundException fexp) {
                mLogger.createLogDebug("Error in getODHome::", fexp);
                return ODHOME;
            }
        }
        return ODHOME;
    }

    /**
     * Executes the iwcp copy CLT
     * @param source
     * @param destination
     */
    public synchronized static void executeCopyCommand(String source, String destination) {
        String strCmd = TSHelper.getIWHome() + Constants.filePathSep + IOHelper.getString("TSHelper.iwcopyCommand") + Constants.BLANK + source + Constants.BLANK + destination;
        executeCommand(strCmd);
    }

    /**
     * Executes the iwsubmit submit CLT
     * @param destination
     */
    public synchronized static void executeSubmitCommand(String destination) {
        String strCmd = TSHelper.getIWHome() + Constants.filePathSep + Constants.submitCommand + Constants.BLANK + Constants.hyphen + Constants.submitOverwriteFlag + Constants.BLANK + Constants.hyphen + Constants.submitUnlockFlag + Constants.BLANK + destination + Constants.BLANK + '"' + Constants.submitComment + '"';
        executeCommand(strCmd);
    }

    public static void executeCommand(String strCmd) {
        HashMap<Integer, String> resultMap = new HashMap<Integer, String>();

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(strCmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String lineRead;
            String output = "";

            while ((lineRead = br.readLine()) != null) {
                output += lineRead + Constants.BREAK;
            }
            Integer exitCode = process.waitFor();
            if (exitCode == 1) {
                throw new Exception("Command::" + strCmd + " falied ");
            }

            resultMap.put(exitCode, output);
            br.close();
        } catch (IOException e) {
            mLogger.createLogDebug("Error in executeCommand::", e);
        } catch (InterruptedException e) {
            mLogger.createLogDebug("Error in executeCommand::", e);
        } catch (Exception e) {
            mLogger.createLogDebug("Error in executeCommand::", e);
        }
    }

    public static String getIWMount() {
        return InstalledLocations.getIwmount();
    }

    /**
     * Checks if the TeamSite server platform is Solaris
     * @return
     */
    public static boolean isSolaris() {
        return InstalledLocations.isSolaris();
    }

    /**
     * Checks if the TeamSite server platform is Windows
     * @return
     */
    public static boolean isWindows() {
        return InstalledLocations.isWindows();
    }

    /**
     * Checks if the TeamSite server platform is Linux
     * @return
     */
    public static boolean isLinux() {
        return InstalledLocations.isLinux();
    }

    public static String getBranchOwnerDetails(CSTask task, String value) {
        // get branch owner
        String result = "";
        try {
            CSUser branchOwner = task.getArea().getBranch().getOwner();
            if (value.equalsIgnoreCase("name")) {
                result = branchOwner.getDisplayName();
            } else if (value.equalsIgnoreCase("email")) {
                result = branchOwner.getEmailAddress();
            }
            return result;
        } catch (CSException e) {
            mLogger.createLogDebug("Error in getBranchOwnerDetails::", e);
            return null;
        }
    }

    public static String getServerName() {
        return InstalledLocations.getServerName();
    }

    public static Matcher getPatternMatcher(String srcString, String regex) {
        Matcher matcher = null;
        Pattern pattern = Pattern.compile(regex);
        if (pattern != null) {
            matcher = pattern.matcher(srcString);
        }
        return matcher;
    }

    public static String readTSFileContents(CSSimpleFile file) throws Exception {
        StringBuffer data = new StringBuffer();
        if (file != null && file.isValid()) {
            InputStreamReader in = new InputStreamReader(file.getInputStream(true), "UTF-8");
            try {
                char[] buff = new char[1024];
                int c = in.read(buff);

                while (c != -1) {
                    data.append(buff, 0, c);
                    c = in.read(buff);
                }

            } catch (Exception e) {
                mLogger.createLogDebug("Error in readTSFileContents::", e);
            } finally {
                in.close();
            }
        }
        return data.toString();
    }

    public static void createDirectoryInTargetWA(CSWorkarea targWA, String relFileDir, String locale) {
        String waPath = targWA.getVPath().toString();
        // Create the directory if it does not exist
        int dirStart = relFileDir.lastIndexOf("/");
        relFileDir = relFileDir.substring(0, dirStart);
        String newDir = waPath + "/" + relFileDir;
        if (locale != null) {
            // Replace the locale appropriately
            int indexFromLocaleString = newDir.indexOf("/templatedata/") + 14;
            int indexAfterLocaleString = newDir.indexOf("/", indexFromLocaleString);

            newDir = newDir.substring(0, indexFromLocaleString) + locale + newDir.substring(indexAfterLocaleString);
        }
        mLogger.createLogDebug("createDirectoryInTargetWA making directory:" + newDir);
        File obfDirFile = new File(newDir);
        obfDirFile.mkdirs();
    }

    public class InstallationInfo {

        public CSInstallationInfo getInstallationInfo() throws CSException {
            return client.getInstallationInfo();
        }

        public CSModule[] getAllModules(String productName) throws CSRemoteException, CSException {
            return getInstallationInfo().getAllModules(productName);
        }

        public CSProduct[] getAllProducts() throws CSRemoteException, CSException {
            return getInstallationInfo().getAllProducts();
        }

        public CSPatch[] getAppliedPatches() throws CSRemoteException, CSException {
            return getInstallationInfo().getAppliedPatches();
        }

        public CSProduct getProduct(String productName) throws CSRemoteException, CSException {
            return getInstallationInfo().getProduct(productName);
        }

        public CSServicePack getServicePackLevel(String productName) throws CSRemoteException, CSException {
            return getInstallationInfo().getServicePackLevel(productName);
        }
    }
}
