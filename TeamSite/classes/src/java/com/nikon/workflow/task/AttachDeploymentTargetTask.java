package com.nikon.workflow.task;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.common.query.*;
import com.interwoven.cssdk.filesys.*;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.json.JsonUtils;
import com.interwoven.livesite.model.Site;
import com.interwoven.livesite.util.NodeHelper;
import com.interwoven.livesite.util.VPathHelper;
import com.interwoven.livesite.workflow.WorkflowUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

import java.util.*;

/**
 * Created by Paul W Russell on 10/07/2014.
 */
public class AttachDeploymentTargetTask implements CSURLExternalTask {

    private static final Log logger = LogFactory.getLog(AttachDeploymentTargetTask.class);

    public static final String DEFAULT_TARGET_NODE = "LiveSiteRuntime";
    public static final String DEPLOYMENT_TARGET_UNKNOWN = "UnknownTarget";
    private static final String JSON_UI = "json";
    private Set<String> mUnsupportedTargetNodes;
    private String mTargetNodesVariableName;
    private String mTargetNodesDelimiterPattern;

    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {

        try {
            Map originalTaskFilesMap = WorkflowUtils.getTaskFileMap(task, client);
            List originalTaskFiles = new ArrayList();
            originalTaskFiles.addAll(originalTaskFilesMap.values());

            Map<String, List<CSFile>> deploymentFileListMap = null;

            Map siteDeploymentTargetMap = getSitesTargetMap(client, task);
            Set<String> deploymentTargetSet = new HashSet();

            deploymentTargetSet.addAll(siteDeploymentTargetMap.values());
            if (logger.isDebugEnabled()) {
                logger.debug("Deployment Targets present in the WORKAREA  : " + deploymentTargetSet);
            }

            if (deploymentTargetSet.size() == 2) {
                deploymentFileListMap = new HashMap();
                deploymentTargetSet.remove("UnknownTarget");
                deploymentFileListMap.put(deploymentTargetSet.iterator().next(), originalTaskFiles);
            } else {
                deploymentFileListMap = getDeploymentFileListMap(originalTaskFiles, siteDeploymentTargetMap);
            }

            Map wfModelMap = new HashMap();

            Map uiModelMap = new HashMap();

            if (logger.isDebugEnabled()) {
                logger.debug("Deployable Files obtained from the method  : " + deploymentFileListMap);
            }

            if (deploymentFileListMap.containsKey("UnknownTarget")) {
                for (String deploymentTarget : deploymentTargetSet) {
                    if ("UnknownTarget".equals(deploymentTarget)) {
                        List fileList = (List) deploymentFileListMap.get("UnknownTarget");

                        putIntoUIModelMap(uiModelMap, fileList, "UnknownTarget");
                    } else {
                        putIntoUIModelMap(uiModelMap, Collections.<CSFile>emptyList(), deploymentTarget);
                    }
                }
                deploymentFileListMap.remove("UnknownTarget");
            }

            for (String deploymentTarget : deploymentFileListMap.keySet()) {
                convertToRelativePath(deploymentFileListMap, deploymentTarget, wfModelMap);
            }

            String jsonUIModel = JsonUtils.toJson(new TreeMap(uiModelMap));
            String jsonWFModel = JsonUtils.toJson(wfModelMap);

            if (logger.isDebugEnabled()) {
                logger.debug("Shared JSON in the Workflow  : " + jsonWFModel);
            }

            WorkflowUtils.setLargeVariable("deployable.files", jsonWFModel, task.getWorkflow());
        } catch (CSException cse) {
            task.chooseTransition(task.getName() + " Failure", "Target nodes failed to be attached.");
            logger.error("Error while Getting Deployment Target for the files : " + cse.getMessage(), cse);
        }

        //------------------------------------------------------------------
        // Transition Task (Success)
        //------------------------------------------------------------------
        task.chooseTransition(task.getName() + " Success", "Target nodes successfully attached.");
    }


    private void convertToRelativePath(Map<String, List<CSFile>> srcMap, String key, Map<String, List<String>> destMap) {
        List fileList = (List) srcMap.get(key);
        List fileNameStrArray = getRelativePathList(fileList);
        destMap.put(key, fileNameStrArray);
    }

    private List<String> getRelativePathList(List<CSFile> fileList) {
        List relativeNameList = new ArrayList(fileList.size());

        for (CSFile file : fileList) {
            relativeNameList.add(file.getVPath().getAreaRelativePath().toString());
        }
        return relativeNameList;
    }

    private void putIntoUIModelMap(Map<String, List<List>> uiModelMap, List<CSFile> fileList, String key)
            throws CSException {
        List fileListMap = new ArrayList();
        for (CSFile file : fileList) {
            List fileInfoList = new ArrayList();
            fileInfoList.add(file.getName());
            fileInfoList.add(file.getVPath().getAreaRelativePath().getParentPath().toString());
            fileListMap.add(fileInfoList);
        }
        uiModelMap.put(key, fileListMap);
    }

    private Map<String, List<CSFile>> getDeploymentFileListMap(List<CSFile> areaFiles, Map<String, String> sitesTargetMap)
            throws CSException {

        Map deploymentFileListMap = new HashMap();
        Set<String> siteNameSet = new HashSet();
        List deploymentFileList = null;
        CSFile deployableFile;

        for (Iterator i$ = areaFiles.iterator(); i$.hasNext(); ) {
            deployableFile = (CSFile) i$.next();

            siteNameSet = getSitesReferred(deployableFile);
            for (String siteName : siteNameSet) {
                String deploymentTargetName = (String) sitesTargetMap.get(siteName);

                if (deploymentFileListMap.containsKey(deploymentTargetName)) {
                    deploymentFileList = (List) deploymentFileListMap.get(deploymentTargetName);
                } else {
                    deploymentFileList = new ArrayList();
                }
                deploymentFileList.add(deployableFile);
                deploymentFileListMap.put(deploymentTargetName, deploymentFileList);
            }
        }

        return deploymentFileListMap;
    }

    private Map<String, String> getSitesTargetMap(CSClient client, CSExternalTask task)
            throws CSException {
        Map siteDeploymentTargetMap = new HashMap();

        CSVPath sitesDirVpath = task.getArea().getVPath().concat("sites");
        CSDir sitesDirectory = (CSDir) client.getFile(sitesDirVpath);

        if (null != sitesDirectory) {
            CSNode[] directoryNodes = sitesDirectory.getChildren();
            for (CSNode fileNode : directoryNodes) {
                if (fileNode.getKind() != 2)
                    continue;
                CSSimpleFile siteFile = (CSSimpleFile) client.getFile(fileNode.getVPath().concat("default.site"));
                String siteName = VPathHelper.getInstance().getSiteName(siteFile.getVPath().toString());
                Site site = new Site(siteName, NodeHelper.readFileData(siteFile, -1L));
                siteDeploymentTargetMap.put(siteName, site.getDeploymentTargetName());
            }

        } else {
            siteDeploymentTargetMap.put("LiveSiteRuntime", "LiveSiteRuntime");
        }

        siteDeploymentTargetMap.put("UnknownTarget", "UnknownTarget");
        return siteDeploymentTargetMap;
    }

    private Set<String> getSitesReferred(CSFile deployableFile)
            throws CSException {
        VPathHelper vpathHelper = VPathHelper.getInstance();
        String deployableFileVpath = deployableFile.getVPath().toString();
        Set siteNameSet = new HashSet();

        if (vpathHelper.isInSite(deployableFileVpath)) {
            siteNameSet.add(vpathHelper.getSiteName(deployableFileVpath));
        } else {
            CSSelectorAttribute attr1 = new CSSelectorAttribute("vpath", null, null, -1);
            CSSelectorAttribute attr2 = new CSSelectorAttribute("name", null, null, -1);
            CSSelectorAttribute[] attrs = {attr1, attr2};

            CSSelector selector = new CSSelector(attrs);
            CSMatchesConstraint fileNameContraint = new CSMatchesConstraint("vpath", "sites/([^/]*)(\\/.*)*", CSAttributeCase.DEFAULT);
            CSPredicate predicate = new CSPredicate(fileNameContraint);

            CSComparisonConstraint childAssociationsConstraint = new CSComparisonConstraint("associationDirection", null, null, null, null, CSOperator.EQUALS, "2");
            CSAndConstraint tconstraint = new CSAndConstraint(new CSConstraint[]{childAssociationsConstraint});

            CSTraverse traversal = new CSTraverse(tconstraint);
            CSQuery query = new CSQuery(selector, predicate, traversal, null, null);

            CSQueryResult res = deployableFile.getRelatedAssets(query.toString(), 0, -1, true, true);
            CSIterator resultIterator = res.getResultsIterator();

            while (resultIterator.hasNext()) {
                CSFile associatedFile = (CSFile) resultIterator.next();
                String associatedFileVPath = associatedFile.getVPath().toString();
                if (vpathHelper.isInSite(associatedFileVPath)) {
                    siteNameSet.add(vpathHelper.getSiteName(associatedFileVPath));
                }
            }
        }
        if (siteNameSet.isEmpty()) {
            siteNameSet.add("UnknownTarget");
        }
        return siteNameSet;
    }



    protected List<String> getTargetNodes(CSExternalTask task) {
        List nodes = new ArrayList();

        String targetNodeStr = WorkflowUtils.findVariable(task, getTargetNodesVariableName());
        if ((targetNodeStr != null) && (!"".equals(targetNodeStr.trim()))) {
            String delimPattern = getTargetNodesDelimiterPattern();
            nodes = Arrays.asList(targetNodeStr.split(delimPattern));

            if (logger.isDebugEnabled()) {
                logger.debug("getTargetNodes: added target nodes: " + nodes.toString());
            }

        } else if (logger.isWarnEnabled()) {
            logger.warn("getTargetNodes: no target nodes found in task variable: " + this.mTargetNodesVariableName);
        }

        return nodes;
    }

    public String getTargetNodesVariableName() {
        return this.mTargetNodesVariableName;
    }

    public void setTargetNodesVariableName(String variableName) {
        this.mTargetNodesVariableName = variableName;
    }

    public String getTargetNodesDelimiterPattern() {
        return this.mTargetNodesDelimiterPattern;
    }

    public void setTargetNodesDelimiterPattern(String pattern) {
        this.mTargetNodesDelimiterPattern = pattern;
    }

}
