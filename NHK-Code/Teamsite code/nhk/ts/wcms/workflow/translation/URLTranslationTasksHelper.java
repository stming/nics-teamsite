package nhk.ts.wcms.workflow.translation;

import nhk.ts.wcms.common.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.LogFactory;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.livesite.util.NodeHelper;
import java.io.File;

public class URLTranslationTasksHelper {

    private static Logger log = new Logger(LogFactory.getLog("nhk.ts.wcms.common.URLTranslationTasksHelper"));

    /**
     * Add method for attaching the matched files to the workflow job
     *
     * @param files
     *            of the currently attached files.
     * @param client
     *            to access the attached files
     * @param workarea
     *            Full workarea path
     * @return The file to be attached
     * @throws CSException
     *
     */
    public static CSAreaRelativePath[] addDependentFiles(CSSimpleFile file, CSExternalTask currentTask, CSClient client) throws CSException {
        log.createLogDebug("addDependentFiles task invoked.");
        if (currentTask == null) {
            return null;
        }
        Vector<CSAreaRelativePath> dependentFiles = new Vector<CSAreaRelativePath>();
        String workarea = currentTask.getArea().getName();
        try {
            if (FileTypeChecker.isPageFile(file, client) || FileTypeChecker.isDcr(file)) {
                log.createLogDebug("file " + file.getName() + " is a valid page or dcr !!");
                getDependentFiles(dependentFiles, file, workarea, currentTask);
            } else {
                log.createLogDebug("file " + file.getName() + " is not a valid page or dcr. Not attaching dependent files");
            }
        } catch (Exception e) {
            log.createLogDebug("Exception in addDependentFiles::", e);
        }
        return dependentFiles.toArray(new CSAreaRelativePath[dependentFiles.size()]);
    }

    /**
     * A get method that extracts files that match a particular extension for attachment to the
     * workflow job.
     *
     * @param file
     *            that are attached
     * @param workarea
     *            full path to the workarea.
     * @return Vector of the new files to be attached.
     */
    @SuppressWarnings("unchecked")
    private static void getDependentFiles(Vector<CSAreaRelativePath> dependentFiles, CSSimpleFile file, String workarea, CSExternalTask currentTask) {
        log.createLogDebug("getDependentFiles files invoked.");
        Pattern pattern = null;
        if (file.isValid()) {
            try {
                log.createLogDebug("Getting dependent files of " + file.getName());
                InputStream inStream = file.getInputStream(true);
                InputStreamReader inputStreamReader = new InputStreamReader(inStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                File temp = File.createTempFile("dependencies-", "-temp");
                BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
                String readCount = "";

                while ((readCount = bufferedReader.readLine()) != null) {
                    writer.write(readCount);
                }
                writer.close();
                inStream.close();
                temp.deleteOnExit();

                FileInputStream inputStream = new FileInputStream(temp);
                FileChannel fileChannel = inputStream.getChannel();
                ByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) fileChannel.size());
                Charset charset = Charset.forName(IOHelper.getCommonPropertyValue("AttachDependencyTask.charSet"));
                CharsetDecoder charsetDecoder = charset.newDecoder();
                CharBuffer cb = charsetDecoder.decode(byteBuffer);
                String[] extns = IOHelper.getCommonPropertyValue("AttachDependencyTask.TranslationfileExtensions").split(Constants.delimiter);
                String filePath = "";
                for (String extMatch : extns) {
                    pattern = Pattern.compile(IOHelper.getString("AttachDependencyTask.fileExtRegExp") + extMatch.trim() + "", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(cb);
                    while (matcher.find()) {
                        try {
                            filePath = matcher.group();
                            if (filePath.startsWith(Constants.urlPathSep)) {
                                filePath = filePath.substring(11, filePath.length());
                            }
                            if (filePath.startsWith(Constants.filePathSep)) {
                                filePath = filePath.substring(1, filePath.length());
                            }
                            log.createLogDebug("FilePath: " + filePath);
                            if (filePath.startsWith("/")) {
                                log.createLogDebug("Area name: " + currentTask.getArea().getName());
                                filePath = filePath.substring(filePath.indexOf(currentTask.getArea().getName()) + currentTask.getArea().getName().length() + 1);
                                log.createLogDebug("After modification FilePath: " + filePath);
                            }
                            CSAreaRelativePath vpath = new CSAreaRelativePath(filePath);

                            // checking if file is a valid file or a CSHole
                            // object
                            Object depfile = currentTask.getArea().getFile(vpath);
                            //  log.createLogDebug("Dep File: " + depfile.toString());
                            if (depfile instanceof CSSimpleFile) {
                                CSSimpleFile depFile = (CSSimpleFile) currentTask.getArea().getFile(vpath);

                                if (depFile.isValid() && !vpath.toString().contains("resources/common")) {
                                    // Attach all files except those under the resources/common path
                                    log.createLogDebug("Valid: " + vpath.toString());
                                    dependentFiles.add(vpath);
                                }

                                String origFilePath = file.getVPath().toString();
                                String depFilePath = depFile.getVPath().toString();

                                log.createLogDebug("origFilePath::" + origFilePath);
                                log.createLogDebug("deptFilePath::" + depFilePath);
                                //attaching dependent files of the DCR
                                if (!(origFilePath.equalsIgnoreCase(depFilePath)) && FileTypeChecker.isDcr(depFile)) {
                                    //to avoid recursive call. Scanning only one level
                                    log.createLogDebug("depFile called by name::" + depFile.getName());
                                    getDependentFiles(dependentFiles, depFile, depFile.getArea().getName(), currentTask);
                                }

                                // Do not need to attach html files here. For translation, only the referenced DCR files are enough.

                            } else if (depfile instanceof CSHole) {
                                CSHole invalidFile = (CSHole) currentTask.getArea().getFile(vpath);
                                currentTask.addComment(Constants.inValidFileComment(invalidFile));
                            }
                            // this is to create dummy resource file for obfuscation
                   /*         else if (depfile==null){
                            String filepath= file.getVPath().toString();
                            log.createLogDebug("FilePath when not a valid file: " + filepath);
                            log.createLogDebug("Locale: " + IOHelper.getPropertyValue("ConvertProductWF.TargetLocale") );
                            if((!(filepath.contains(IOHelper.getPropertyValue("ConvertProductWF.TargetLocale")))) && vpath.toString().contains("resources/common"))
                            {
                            String resourcefilePath = currentTask.getArea().getVPath().toString() + "/" + vpath.toString();
                            log.createLogDebug("Not a valid file and the DCR dont belong to en_Asia" + resourcefilePath);
                            File catFile = new File(resourcefilePath);
                            File dirFile =new File(resourcefilePath.substring(0,resourcefilePath.lastIndexOf("/")));
                            log.createLogDebug("File name: "+catFile.getName());
                            log.createLogDebug("Dir Path: " + dirFile.getPath());
                            dirFile.mkdirs();
                            catFile.createNewFile();
                            CSSimpleFile tempFile = (CSSimpleFile) currentTask.getArea().getFile(vpath);
                            NodeHelper.copyEAs(file,tempFile);
                            String [] tempEA = {"TeamSite/Templating/DCR/Type"};
                            tempFile.deleteExtendedAttributes(tempEA);
                            NodeHelper.setStringEA(tempFile, "TeamSite/Resource/Common/Temp", "Yes");
                            //  NodeHelper.setStringEA(tempFile, "TeamSite/Templating/DCR/Type","");
                            dependentFiles.add(vpath);
                            }
                            } */
                        } catch (Exception e) {
                            log.createLogError("Exception in getDependentFiles::", e);
                        }
                    }
                }
                inputStream.close();
            } catch (ClassCastException cce) {
                log.createLogError("Exception in URLTaskHelper", cce);
            } catch (CSException cse) {
                log.createLogError("Exception in URLTaskHelper", cse);
            } catch (Exception e) {
                log.createLogError("Exception in URLTaskHelper", e);
            }
        }
        log.createLogDebug("Exit method getDependentFiles");
    }

    /**
     * Convenient method to get a task by name from a workflow. (linear search) Return null if task
     * by name is not found.
     */
    public static CSTask getTaskByName(CSWorkflow job, String name) throws CSException {
        if (name == null) {
            log.createLogDebug("The task being looked up in the Work Flow is Null");
            return null;
        }
        CSTask[] tasks = job.getTasks();
        for (int i = 0; i < tasks.length; i++) {
            if (name.equals(tasks[i].getName())) {
                return tasks[i];
            }
        }
        log.createLogDebug("The task [" + name + "] being looked up in the Work Flow does not exist!");
        return null;
    }
}
