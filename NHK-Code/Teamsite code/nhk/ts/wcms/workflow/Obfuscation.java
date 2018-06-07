/**
 * 
 */
package nhk.ts.wcms.workflow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSPathStatus;
import com.interwoven.cssdk.filesys.CSReadOnlyFileSystemException;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.FileNotFoundException;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

/**
 * @author Arnout Cator Obfuscation URL external task implemented for Digital
 *         Asset Obfuscation in runtime LiveSite: Business Case: No digital
 *         asset can have a recognisable path in the customers web site.
 */
public class Obfuscation implements CSURLExternalTask {

    /**
     * Declare Instance Variables.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Log4J field.
     */
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.Obfuscation"));
    /**
     * MessageDigest field. Used for obfuscating file path and name
     */
    private MessageDigest md;
    /**
     * int B64_SIMPLE_STRING final constant.
     */
    private static final int B64_SIMPLE_STRING = 0;
    /**
     * int B64_NOT_PATH_AND_FILE_NAME final constant.
     *
     * If we send in /a/b/c/d/someFile.txt or /a/b/c/d/someFile then obfuscate
     * someFile.txt or someFile only and return it prepended with /a/b/c/d/
     * Similary if just the filename no path and extension then obfuscate that
     * and return
     */
    private static final int B64_NOT_PATH_AND_FILE_NAME = 1;
    /**
     * int B64_PATH_AND_FILE_NAME final constant. If we send in
     * /a/b/c/d/someFile.txt or /a/b/c/d/someFile then obfuscate
     * /a/b/c/d/someFile.txt or /a/b/c/d/someFile only and return it
     */
    private static final int B64_PATH_AND_FILE_NAME = 2;
    /**
     * int B64_FILE_NAME_ONLY field final constant. If we send in
     * /a/b/c/d/someFile.txt or /a/b/c/d/someFile then obfuscate someFile.txt or
     * someFile only and return it
     *
     */
    private static final int B64_FILE_NAME_ONLY = 3;
    /**
     * String file field. Holds workflow file
     *
     */
    private String file;
    /**
     * String initDir field. Initial Directory in workarea root "tmp" used for
     * creating temporary files for attachment to workflow and for OpenDeploy to
     * deploy
     */
    private String initDir;
    /**
     * ArrayList to hold the obfuscated files for attaching to workflow.
     */
    private List<CSAreaRelativePath> arrayList;
    /**
     * An array of obfuscated files
     */
    private CSAreaRelativePath[] obfusFiles;
    /**
     * List holding dcrs
     */
    List dcrList = new ArrayList();
    /**
     * List holding sitepublisher files
     */
    List sitepubList = new ArrayList();
    /**
     * List holding other unaffected files like pdf,doc, xls
     */
    List otherList = new ArrayList();
    /**
     * List holding other html files
     */
    List htmlFilesList = new ArrayList();
    /**
     * Map
     *
     * @params String K: original file path, V: obfuscated paths
     */
    Map imageMap = new HashMap<String, String>();
    /**
     * Obfuscation Workarea
     */
    CSWorkarea obfuscationWorkarea;
    private CSAreaRelativePath[] obfusDCRRelPathArray;
    private CSAreaRelativePath[] imageObfusFilesArray;
    private CSAreaRelativePath[] sitepubFilesArray;
    private CSAreaRelativePath[] otherFilesArray;
    private CSAreaRelativePath[] htmlFilesArray;
    /**
     * Simple File
     */
    CSSimpleFile simpleFile;

    /**
     * Constructor of the object.
     *
     * @return
     */
    public Obfuscation() {
        super();
    }

    /**
     * execute method implemented from Interface URLExternalTask.
     *
     * @params CSClient client, CSExternalTask task, Hashtable params
     * @throws CSException
     */
    public void execute(CSClient client, CSExternalTask task, Hashtable params)
            throws CSException {

        // Step 1. Instantiate a MessageDigest object for B64DigestEncoder

        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

        // Step 2. Instantiate an ArrayList object for obfuscated files
        List list = Collections.synchronizedList(new ArrayList<CSAreaRelativePath>());
        setArrayList(list);

        CSAreaRelativePath[] files = task.getFiles();

        // TODO make the following Array List for digital assets a DCT
        ArrayList extList = new ArrayList();
        extList.add("jpg");
        extList.add("jpeg");
        extList.add("gif");
        extList.add("swf");
        extList.add("png");
        extList.add("css");
        extList.add("flv");

        // TODO make the sitepublish assets a DCT
        ArrayList miscList = new ArrayList();
        miscList.add("page");
        miscList.add("site");
        miscList.add("sitemap");

        // HTML files whose image paths have to be altered.
        ArrayList htmlList = new ArrayList();
        htmlList.add("html");


        // Step 2. Create a copy of DCR templatedata folder structure for later
        // DCR xml processing
        // Get the current workarea vpath from task
        String waPath = getObfuscationWorkarea(task).getVPath().toString();

        // Create the directory if it does not exist
        String assetDir = waPath + "/tmp";
        mLogger.createLogDebug("execute making directory:" + assetDir);
        File assetFolderStr = new File(assetDir);
        assetFolderStr.mkdirs();

        // Step 3. Encode the attached files
        for (int i = 0; i < files.length; i++) {

            String thisExt = files[i].getExtension();

            if (thisExt == null) {
                thisExt = "";
            }

            String string = "/";
            setFile(string + files[i].toString());

            //TODO PERFORMANCE ISSUE: we do updates every time we find a file in the filelist
            // Step 3a. put the attached DCRs in an array
            if (task.getArea().getFile(files[i]).getKind() == CSSimpleFile.KIND) {

                simpleFile = (CSSimpleFile) task.getArea().getFile(files[i]);

                mLogger.createLogDebug("simple file Name: " + " "
                        + simpleFile.getName() + " Kind: "
                        + simpleFile.getContentKind());

                // If it is a DCR process asset
                if (simpleFile.getContentKind() == CSSimpleFile.kDCR) {
                    mLogger.createLogDebug("Simple Files: "
                            + simpleFile.getVPath().toString());
                    // adding DCRS to List
                    dcrList.add(files[i]);

                    String relDir = task.getArea().getFile(files[i]).getVPath().getAreaRelativePath().toString();
                    int dirStart = relDir.lastIndexOf("/");
                    relDir = relDir.substring(0, dirStart);

                    // Create the directory if it does not exist
                    String obfDCRDir = waPath + "/" + relDir;
                    mLogger.createLogDebug("execute making the directory:" + obfDCRDir);
                    File obfDCRDirFile = new File(obfDCRDir);
                    obfDCRDirFile.mkdirs();

                    CSAreaRelativePath origDCRRelPath = new CSAreaRelativePath(
                            relDir.substring(1));
                    CSAreaRelativePath obfusDCRRelPath = new CSAreaRelativePath(
                            simpleFile.getVPath().getAreaRelativePath().toString());
                    mLogger.createLogDebug("obfusDCRRelPath: "
                            + obfusDCRRelPath);
                    mLogger.createLogDebug("DCR List Size: " + dcrList.size());

                    // create array of obfuscated relative area DCR paths
                    // from List
                    obfusDCRRelPathArray = (CSAreaRelativePath[]) dcrList.toArray(new CSAreaRelativePath[0]);


                    // delete obfuscated DCRs and sitepublisher files
                    //     deleteObfuscationWorkareaFiles(task, obfusDCRRelPathArray, sitepubFilesArray);

                    // copy original digital assets and DCRs to obfuscated
                    // file system using an update
                    mLogger.createLogDebug("Creating the following files");
                    for (Object object : obfusDCRRelPathArray) {
                        CSAreaRelativePath fileToBeCreated = (CSAreaRelativePath) object;
                        mLogger.createLogDebug("file created in obfuscation workarea:" + fileToBeCreated.getParentPath() + "/" + fileToBeCreated.getName());
                    }
                    getObfuscationWorkarea(task).update(
                            obfusDCRRelPathArray, task.getArea(), 1);
                } // if it is a miscellaneous file process just a copy to
                // obfuscation_wa
                else if (miscList.contains(thisExt.trim().toLowerCase())) {

                    mLogger.createLogDebug("Within the MISC LOOP!!");

                    sitepubList.add(files[i]);

                    String relPageDir = task.getArea().getFile(files[i]).getVPath().getAreaRelativePath().toString();
                    int dirStart = relPageDir.lastIndexOf("/");
                    relPageDir = relPageDir.substring(0, dirStart);

                    // Create the directory if it does not exist
                    String obfPageDir = waPath + "/" + relPageDir;
                    mLogger.createLogDebug("execute making directory:" + obfPageDir);
                    File obfPageDirFile = new File(obfPageDir);
                    obfPageDirFile.mkdirs();
                    CSAreaRelativePath origPageRelPath = new CSAreaRelativePath(
                            relPageDir.substring(1));
                    CSAreaRelativePath targetPageRelPath = new CSAreaRelativePath(
                            simpleFile.getVPath().getAreaRelativePath().toString());
                    mLogger.createLogDebug("obfusPageRelPath: "
                            + targetPageRelPath);
                    mLogger.createLogDebug("Page List Size: "
                            + sitepubList.size());

                    // create array of obfuscated relative area DCR
                    // paths
                    // from List
                    sitepubFilesArray = (CSAreaRelativePath[]) sitepubList.toArray(new CSAreaRelativePath[0]);

                    //update attached sitepublisher files in obfuscation_wa
                    getObfuscationWorkarea(task).update(
                            sitepubFilesArray, task.getArea(), 1);


                } else if (extList.contains(thisExt.trim().toLowerCase())) {
                    // Step 3b. just do the extensions of type digital
                    // images in extList
                    try {
                        mLogger.createLogDebug("This Extension within Loop: " + thisExt);
                        String srcImgPath = files[i].toString();
                        String obfImgPath = b64DigestEncoder(client, task, string + files[i].toString(), 2);
                        // Create a map of image paths for replacement in
                        // the DCR
                        imageMap.put(srcImgPath, obfImgPath);
                        mLogger.createLogDebug("Source Image Path: " + srcImgPath);
                        mLogger.createLogDebug("Obfuscated Image Path: " + obfImgPath);
                        mLogger.createLogDebug("Image Map Size: " + imageMap.size());
                    } catch (NoSuchAlgorithmException ex) {
                        mLogger.createLogDebug("Error in encoding algorithm:", ex);
                    } catch (UnsupportedEncodingException ex) {
                        mLogger.createLogDebug("Error in encoding which is unsupported:", ex);
                    }
                } else {
                    mLogger.createLogDebug("Within the Other files loop!! Extension is " + thisExt);

                    otherList.add(files[i]);

                    if (htmlList.contains(thisExt.trim().toLowerCase())) {
                        htmlFilesList.add(files[i]);
                    }
                    String relPageDir = task.getArea().getFile(files[i]).getVPath().getAreaRelativePath().toString();
                    int dirStart = relPageDir.lastIndexOf("/");
                    relPageDir = relPageDir.substring(0, dirStart);

                    // Create the directory if it does not exist
                    String obfPageDir = waPath + "/" + relPageDir;
                    mLogger.createLogDebug("execute making directory:" + obfPageDir);
                    File obfPageDirFile = new File(obfPageDir);
                    obfPageDirFile.mkdirs();
                    CSAreaRelativePath origPageRelPath = new CSAreaRelativePath(
                            relPageDir.substring(1));
                    CSAreaRelativePath targetPageRelPath = new CSAreaRelativePath(
                            simpleFile.getVPath().getAreaRelativePath().toString());
                    mLogger.createLogDebug("obfusOtherFileRelPath: "
                            + targetPageRelPath);
                    mLogger.createLogDebug("Other file list Size: "
                            + otherList.size());

                    // create array of obfuscated relative area DCR
                    // paths
                    // from List
                    otherFilesArray = (CSAreaRelativePath[]) otherList.toArray(new CSAreaRelativePath[0]);

                    //update attached unaffected files (like pdf,xls,doc) in obfuscation_wa
                    getObfuscationWorkarea(task).update(
                            otherFilesArray, task.getArea(), 1);
                }
            }
        }

        // Step 4. Detach Original Files from Task


        CSPathStatus[] statusArray = task.detachFiles(files);

        CSAreaRelativePath[] afterRemFiles = task.getFiles();
        if (afterRemFiles != null) {
            mLogger.createLogDebug("afterRemFiles.length:"
                    + afterRemFiles.length);
        }

        // Step 5. Update Image paths in the DCRs (if any)
        String vpath = task.getArea().getBranch().getVPath().toString() + "/WORKAREA/obfuscation_wa/";
        mLogger.createLogDebug("vpath: " + vpath);

        if (obfusDCRRelPathArray != null) {
            mLogger.createLogDebug("NUMBER OF INPUT DCRs to obfuscate" + obfusDCRRelPathArray.length);
            for (int j = 0; j < obfusDCRRelPathArray.length; j++) {

                String obfusDCRPath = vpath
                        + obfusDCRRelPathArray[j].toString();
                mLogger.createLogDebug("InputDCR file name" + j + ":" + obfusDCRPath);
                try {
                    File newFile = new File(obfusDCRPath);
                    updateImagePaths(newFile);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
                    mLogger.createLogDebug("could not obfuscate dcr" + obfusDCRPath);
                }
            }
        }
        try {
            htmlFilesArray = (CSAreaRelativePath[]) htmlFilesList.toArray(new CSAreaRelativePath[0]);
            mLogger.createLogDebug("Number of HTML files that would be modified with obfuscation paths::" + htmlFilesArray.length);
            // Updating the obfuscated paths on the html files.
            if (htmlFilesArray != null) {
                for (int j = 0; j < htmlFilesArray.length; j++) {
                    String otherFilePathName = vpath
                            + htmlFilesArray[j].toString();
                    File newFile = new File(otherFilePathName);
                    updateHTMLWithImagePath(newFile);
                }
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error in html file updation:", e);
        }

        // Step 6. Switch to obfuscation workarea
        setObfuscationWorkarea(task);
        mLogger.createLogDebug("current workarea: "
                + task.getArea().getVPath().toString());

        // Step 7. Attach Obfuscated Files to Task
        mLogger.createLogDebug("attaching files from arrayList with size:"
                + arrayList.size());

        obfusFiles = new CSAreaRelativePath[arrayList.size()];
        mLogger.createLogDebug("obfusFiles[] with size:" + obfusFiles.length);

        imageObfusFilesArray = (CSAreaRelativePath[]) arrayList.toArray(obfusFiles);

        try {

            CSPathStatus[] dcrStatusArray;
            CSPathStatus[] imageStatusArray;
            CSPathStatus[] sitepubStatusArray;
            CSPathStatus[] otherFilesStatusArray;

            //TODO check for null in another way

            if (obfusDCRRelPathArray != null) {
                //DCRs to attach
                dcrStatusArray = task.attachFiles(obfusDCRRelPathArray);
            }
            if (imageObfusFilesArray != null) {
                //Digital assets to attach
                imageStatusArray = task.attachFiles(imageObfusFilesArray);
            }
            if (sitepubFilesArray != null) {
                //SitePublisher assets to attach
                sitepubStatusArray = task.attachFiles(sitepubFilesArray);
            }
            if (otherFilesArray != null) {
                //SitePublisher assets to attach
                otherFilesStatusArray = task.attachFiles(otherFilesArray);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        }

        // Finally attached files

        CSAreaRelativePath[] finallyAttachedFiles = task.getFiles();
        mLogger.createLogDebug("The following " + finallyAttachedFiles.length + " files have been re-attached from obfuscation_wa.");
        for (int j = 0; j < finallyAttachedFiles.length; j++) {
            CSAreaRelativePath cSAreaRelativePath = finallyAttachedFiles[j];
            mLogger.createLogDebug("File re-attached:" + task.getArea().getVPath().toString() + "/" + cSAreaRelativePath.getParentPath() + "/" + cSAreaRelativePath.getName());
        }

        // Step 8. Go to Deployment Task transition.

        String nextTransition = task.getTransitions()[0];
        mLogger.createLogDebug("Moving to next transition name:" + nextTransition);
        task.chooseTransition(nextTransition, "Image files have been obfuscated");



    }

    /**
     * Gets the workarea holding obfuscated images and DCRs with obfuscated
     * image paths
     *
     * @param task
     * @return
     * @throws CSAuthorizationException
     * @throws CSObjectNotFoundException
     * @throws CSExpiredSessionException
     * @throws CSRemoteException
     * @throws CSException
     */
    public CSWorkarea getObfuscationWorkarea(final CSExternalTask task)
            throws CSAuthorizationException, CSObjectNotFoundException,
            CSExpiredSessionException, CSRemoteException, CSException {
        CSWorkarea[] allworkarea = task.getArea().getBranch().getWorkareas();
        for (int w = 0; w < allworkarea.length; w++) {
            if (allworkarea[w].getName().matches("obfuscation_wa")) {
                obfuscationWorkarea = allworkarea[w];
                return obfuscationWorkarea;
            }
        }
        throw new CSException(CSException.FAILED_TO_GET_FILES_ON_TASK_ERR,
                "No obufscation workarea found");
    }

    /**
     * Sets the workarea holding obfuscated images and DCRs with obfuscated
     * image paths
     *
     * @param task
     * @return
     * @throws CSAuthorizationException
     * @throws CSObjectNotFoundException
     * @throws CSExpiredSessionException
     * @throws CSRemoteException
     * @throws CSException
     */
    public CSWorkarea setObfuscationWorkarea(final CSExternalTask task)
            throws CSAuthorizationException, CSObjectNotFoundException,
            CSExpiredSessionException, CSRemoteException, CSException {
        CSWorkarea[] allworkarea = task.getArea().getBranch().getWorkareas();
        for (int w = 0; w < allworkarea.length; w++) {
            if (allworkarea[w].getName().matches("obfuscation_wa")) {
                obfuscationWorkarea = allworkarea[w];
                task.setArea(obfuscationWorkarea);
                return obfuscationWorkarea;
            }
        }
        throw new CSException(CSException.FAILED_TO_GET_FILES_ON_TASK_ERR,
                "No obufscation workarea found");
    }

    /*
     * (private) Utility Methods
     */
    /**
     * _b64Digest digest utility method.
     *
     * @param part
     * @return String
     * @throws UnsupportedEncodingException
     */
    private String _b64Digest(String part) throws UnsupportedEncodingException {

        byte[] digest = getMd().digest(part.getBytes());
        return new String(Base64.encode(digest)).replaceAll("\n", "").replaceAll("/", "").replaceAll("=", "").replaceAll("\\+", "");
    }

    /**
     * b64DigestEncoder digest utility method.
     *
     * @param message
     * @param type
     * @return String
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private String b64DigestEncoder(CSClient client, CSExternalTask task, String message,
            int type) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        String retVal = null;

        if (B64_SIMPLE_STRING == type) {
            retVal = _b64Digest(message);
        } else if (B64_NOT_PATH_AND_FILE_NAME == type) {
            int lastSlash = message.lastIndexOf('/') > 0 ? message.lastIndexOf('/') : 0;
            int lastDot = message.lastIndexOf('.');

            String path = (lastSlash > 0 ? message.substring(0, lastSlash + 1)
                    : "");
            String fileName = (lastDot > 0 ? message.substring(
                    lastSlash == 0 ? 0 : lastSlash + 1, lastDot) : message.substring(lastSlash == 0 ? 0 : lastSlash + 1));
            String extension = (lastDot > 0 ? message.substring(lastDot) : "");

            retVal = path + b64DigestEncoder(client, task, fileName) + extension;
        } else if (B64_FILE_NAME_ONLY == type) {
            int lastSlash = message.lastIndexOf('/') > 0 ? message.lastIndexOf('/') : 0;
            int lastDot = message.lastIndexOf('.');
            String fileName = (lastDot > 0 ? message.substring(
                    lastSlash == 0 ? 0 : lastSlash + 1, lastDot) : message.substring(lastSlash == 0 ? 0 : lastSlash + 1));
            String extension = (lastDot > 0 ? message.substring(lastDot) : "");
            retVal = b64DigestEncoder(client, task, fileName) + extension;
        } else if (B64_PATH_AND_FILE_NAME == type) {
            mLogger.createLogDebug("in type 2: " + message);

            String extension = null;
            String theRest = message;
            boolean startingSlash = message.charAt(0) == '/';
            int dotPos = message.lastIndexOf(".");

            if (dotPos > 0) {
                extension = message.substring(dotPos);
                theRest = message.substring(0, dotPos);

                StringBuffer x = new StringBuffer();
                String[] spl = theRest.split("/");

                for (String enc : theRest.split("/")) {
                    if (enc != null && !enc.equalsIgnoreCase("")) {
                        String encodedStr = _b64Digest(enc);
                        if (encodedStr.length() > 4) {

                            encodedStr = encodedStr.substring(0, 3);

                        }
                        x.append(encodedStr);
                        x.append("/");
                    }

                }

                x.deleteCharAt(x.length() - 1);

                retVal = getInitDir() + (startingSlash ? "/" : "")
                        + (x.toString()) + (extension != null ? extension : "");

            }
        }

        mLogger.createLogDebug("returned retval:  " + retVal);

        try {
            createObfuscatedFiles(client, task, file, retVal);
        } catch (IOException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSAuthorizationException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSRemoteException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSObjectNotFoundException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSExpiredSessionException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSException e) {
            // TODO Auto-generated catch block
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        }

        return retVal;
    }

    /**
     * b64DigestEncoder digest utility method.
     *
     * @param message
     * @return String
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private String b64DigestEncoder(CSClient client, CSExternalTask task, String message)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return b64DigestEncoder(client, task, message, B64_SIMPLE_STRING);
    }

    /**
     * Creates obfuscated files in temporary area.
     *
     * @param task
     * @param origFile
     * @param obfusFile
     * @throws IOException
     * @throws CSAuthorizationException
     * @throws CSRemoteException
     * @throws CSObjectNotFoundException
     * @throws CSExpiredSessionException
     * @throws CSException
     * @return CSAreaRelativePath[] required by CSTask[] attachFiles
     */
    private void createObfuscatedFiles(CSClient client, CSExternalTask task, String origFile,
            String obfusFile) throws IOException, CSAuthorizationException,
            CSRemoteException, CSObjectNotFoundException,
            CSExpiredSessionException, CSException {

        String relDir = obfusFile;
        int dirStart = relDir.lastIndexOf("/");
        relDir = relDir.substring(0, dirStart);

        // Get the obfuscation workarea vpath from task
        String waPath = getObfuscationWorkarea(task).getVPath().toString();

        // Create the directory if it does not exist
        String obfDir = waPath + "/" + relDir;
        mLogger.createLogDebug("createObfuscatedFiles making directory:" + obfDir);
        File obfDirFile = new File(obfDir);
        obfDirFile.mkdirs();

        CSAreaRelativePath origRelPath = new CSAreaRelativePath(origFile.substring(1));
        CSAreaRelativePath obfusRelPath = new CSAreaRelativePath(obfusFile);
        mLogger.createLogDebug("createObfuscatedFiles origRelPath: " + origRelPath);
        mLogger.createLogDebug("createObfuscatedFiles obfusRelPath: " + obfusRelPath);
        arrayList.add(obfusRelPath);
        for (Iterator iterator = arrayList.iterator(); iterator.hasNext();) {
            CSAreaRelativePath obf = (CSAreaRelativePath) iterator.next();
        }
        CSAreaRelativePath[] obfusRelPathArray = (CSAreaRelativePath[]) arrayList.toArray(new CSAreaRelativePath[0]);

        try {

            // update the origFile into the obfuscation workarea
            // do not update any files that are not modified
            // 1 = OVERWRITE_ALL
            mLogger.createLogDebug("createObfuscatedFiles Creating the following files");
            for (Object object : obfusRelPathArray) {
                CSAreaRelativePath fileToBeCreated = (CSAreaRelativePath) object;
                mLogger.createLogDebug("createObfuscatedFiles file created in obfuscation workarea:" + fileToBeCreated.getParentPath() + "/" + fileToBeCreated.getName());
            }
            CSAreaRelativePath[] currentFile = new CSAreaRelativePath[1];
            currentFile[0] = obfusRelPath;
            getObfuscationWorkarea(task).deleteFiles(currentFile);
            //getObfuscationWorkarea(task).update(obfusRelPathArray, task.getArea(), 1);
            copyImageWithMeta(client, task.getArea().getVPath() + "/" + origRelPath, waPath + "/" + obfusRelPath);
        } catch (CSAuthorizationException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSObjectNotFoundException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSExpiredSessionException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSRemoteException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (CSException e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } catch (Exception e) {
            mLogger.createLogDebug("Error executing obfuscation: " + e.getMessage(), e);
        } finally {
            System.gc();
        }
    }

    private void addMetaInformation(CSClient client, String sourceDCRFullPath, String targetDCRFullPath) throws Exception {

        mLogger.createLogDebug("Adding meta information");
        CSVPath pathToFile = new CSVPath(sourceDCRFullPath);
        CSSimpleFile sourceSimpFile = (CSSimpleFile) client.getFile(pathToFile);
        pathToFile = new CSVPath(targetDCRFullPath);
        CSSimpleFile targSimpFile = (CSSimpleFile) client.getFile(pathToFile);
        targSimpFile.setExtendedAttributes(sourceSimpFile.getExtendedAttributes(null));
    }

    /**
     * deleteObfuscationWorkareaFiles
     * @param task
     * @param obfusDCRRelPathArray
     * @param sitepubFilesArray
     * @throws CSReadOnlyFileSystemException
     * @throws CSRemoteException
     * @throws CSExpiredSessionException
     * @throws CSException
     */
    public void deleteObfuscationWorkareaFiles(CSExternalTask task, CSAreaRelativePath[] obfusDCRRelPathArray, CSAreaRelativePath[] sitepubFilesArray) throws CSReadOnlyFileSystemException, CSRemoteException, CSExpiredSessionException, CSException {

        if (obfusDCRRelPathArray != null) {
            //DCRs to attach
            getObfuscationWorkarea(task).deleteFiles(obfusDCRRelPathArray);
        }

        if (sitepubFilesArray != null) {
            //SitePublisher assets to attach
            getObfuscationWorkarea(task).deleteFiles(sitepubFilesArray);
        }

    }

    public void updateImagePaths(File inputXml) throws IOException {

        String inputDCR = Dom4jUtils.newDocument(inputXml).asXML();
        String outputDcr = null;

        mLogger.createLogDebug("inputDcr: " + inputDCR);

        // initialise FileOutputStream and Writer
        FileOutputStream fos = new FileOutputStream(inputXml);
        Writer out = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));

        // initialise keys from image Map
        Iterator keys = imageMap.keySet().iterator();

        while (keys.hasNext()) {

            String key = (String) keys.next();
            mLogger.createLogDebug("key: " + key);
            String obfusPath = (String) imageMap.get(key);
            mLogger.createLogDebug("value: " + obfusPath);

            inputDCR = StringUtils.replace(inputDCR, key, obfusPath);

        }

        out.write(inputDCR);
        mLogger.createLogDebug("inputDcr after obfuscation " + inputDCR);

        out.flush();
        out.close();
    }

    private void copyImageWithMeta(CSClient client, String srcImg, String tarImg) throws Exception {
        FileInputStream in = null;
        try {
            File inputFile = new File(srcImg);
            File outputFile = new File(tarImg);
            in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(outputFile);
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            mLogger.createLogDebug("Error in copyImage:", ex);
        } catch (IOException ex) {
            mLogger.createLogDebug("Error in copyImage:", ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                mLogger.createLogDebug("Error in copyImage:", ex);
            }
        }
        addMetaInformation(client, srcImg, tarImg);
    }

    private void updateHTMLWithImagePath(File srcFile) {
        try {
            mLogger.createLogDebug("The following HTML file will be updated for the obfuscated paths::" + srcFile.getName());
            FileInputStream fin = new FileInputStream(srcFile);
            String thisLine;
            BufferedReader input = new BufferedReader(new InputStreamReader(fin));
            StringBuffer inputString = new StringBuffer();
            while ((thisLine = input.readLine()) != null) {
                inputString.append(thisLine + "\n");
            }
            String finalStr = inputString.toString();
            mLogger.createLogDebug("HTML File before updation:::" + finalStr);
            Iterator keys = imageMap.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                mLogger.createLogDebug("key: " + key);
                String obfusPath = (String) imageMap.get(key);
                mLogger.createLogDebug("value: " + obfusPath);
                finalStr = StringUtils.replace(finalStr, key, obfusPath);
            }
            mLogger.createLogDebug("HTML File after updation:::" + finalStr);
            FileOutputStream fos = new FileOutputStream(srcFile);
            Writer out = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            out.write(finalStr);
            out.flush();
            out.close();
            fin.close();

        } catch (Exception e) {
            mLogger.createLogDebug("Error in updateHTMLWithImagePath", e);
        }
    }

    /*
     * Getters and Setters
     */
    /**
     * @param md
     *            the md to set
     */
    public void setMd(MessageDigest md) {
        this.md = md;
    }

    /**
     * @return the md
     */
    public MessageDigest getMd() {
        return md;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the initDir
     */
    public String getInitDir() {
        this.initDir = "tmp";
        return this.initDir;
    }

    /**
     * @param initDir
     *            the initDir to set
     */
    public void setInitDir(String initDir) {
        this.initDir = initDir;
    }

    /**
     * @param arrayList
     *            the arrayList to set
     */
    public void setArrayList(List arrayList) {
        this.arrayList = arrayList;
    }

    /**
     * @return the arrayList
     */
    public List getArrayList() {
        return arrayList;
    }

    /**
     * @return the obfusFilesa
     */
    public CSAreaRelativePath[] getObfusFiles() {
        return obfusFiles;
    }

    /**
     * @param obfusFiles
     *            the obfusFiles to set
     */
    public void setObfusFiles(CSAreaRelativePath[] obfusFiles) {
        this.obfusFiles = obfusFiles;
    }
}
