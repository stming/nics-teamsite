package com.interwoven.teamsite.nikon.externals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.impl.BaseRequestContext;
import com.interwoven.livesite.runtime.model.SiteMap;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.util.NikonUtils;


public class NikonLiveSiteBreadcrumbExternalDelegate extends
		NikonLiveSiteBaseDelegate {

	
	private ExternalSiteMapXmlAdapter mXmlAdapter;

    public NikonLiveSiteBreadcrumbExternalDelegate() {
      this.mXmlAdapter = null;
      this.mXmlAdapter = new ExternalSiteMapXmlAdapter();
  }
	public Document displayBreadcrumb(RequestContext context) {
		Document doc = null;
		try {
			String pageName = context.getPageName();
			String requestedURL = context.getRequest().getRequestURL().toString();
			String category = context.getParameterString("Category");
			log.debug("Page Name [" + pageName + "], Request URL ["
					+ requestedURL + "], Category = ["+category+"]");
			SiteMap siteMap = context.getLiveSiteDal().getSiteMap(
					context.getSite().getName());
			String nodeId = getSelectedNodeId(siteMap, (BaseRequestContext) context);
			String locale = LocaleResolver.getRequestedLanguageCountryCode(context);
			log.debug("Current Node ID [" + nodeId + "]");
			if (pageName.startsWith("products") && !"products/catalogue".equals(pageName)){
				log.debug("Page within products directory and it is not catalogue page, will try to resolve with the product_explorer_block");
				Document productExplorerBlock = retrieveProductExplorerBlock(context, locale);
				if (productExplorerBlock == null){
					log.warn("No product explorer block, will fallback to sitemap");
					doc = buildBreadcrumbDocument(context, siteMap, nodeId);
				}else {
					String categoryPage = "products/catalogue";
					List categoryNodeIds = siteMap.getNodeIds("0", categoryPage);
					if (categoryNodeIds == null && !categoryNodeIds.isEmpty()){
						log.debug("Cannot found categoryPage node Id: ["+categoryPage+"], will fallback to sitemap");
						doc = buildBreadcrumbDocument(context, siteMap, nodeId);
					}else{
						String parentNodeId = (String) categoryNodeIds.get(0);
			        	log.debug("Category Node ID " + parentNodeId + " will try to use the sitemap to build the doc");
			        	doc = buildBreadcrumbDocument(context, siteMap, parentNodeId);
			        	log.debug("Partial return based on the Category Node ID: " + doc.asXML());
			        	Element nodes = (Element) doc.getRootElement().selectSingleNode("Nodes");
						if ("products/product_details".equals(pageName)){
							log.debug("Product details page, will attempt to grab product document");
							NikonLiveSiteHBN8ExternalDelegate productDelegate = new NikonLiveSiteHBN8ExternalDelegate();
							Document productDoc = productDelegate.listProductDetails(context);
							Element root = productDoc.getRootElement();
							Element productNode = (Element) root.selectSingleNode("Product");
							String productCategory = productNode.attributeValue("productCategory");
							String localShortName = productNode.attributeValue("localShortName");
							String navCat1 = productNode.attributeValue("navCat1");
							String navCat2 = productNode.attributeValue("navCat2");
							String navCat3 = productNode.attributeValue("navCat3");
							breadBreadcrumbByProduct(nodes, productExplorerBlock, productCategory, navCat1, navCat2, navCat3);
							log.debug("Adding product title from product document ["+localShortName+"]");
							Element node = nodes.addElement("Node");
							node.addAttribute("Generated", "true");
							node.addAttribute("Product", "true");
							node.addElement("Label").addText(localShortName);
							
						}else{
							String requestedPage = "/sites/" + context.getSite().getDirectory() + "/" + pageName + ".page";
							if (context.getRequest().getQueryString() != null && !"".equals(context.getRequest().getQueryString())){
								requestedPage = requestedPage + "?" + context.getRequest().getQueryString();
							}
							log.debug("Not product detail page, will assume it is category page, request page: ["+requestedPage+"]");
							
							
							buildBreadcrumbByCategoryPage(nodes, productExplorerBlock, requestedPage);
						}
					}

				}
				
			}else if (category != null && !"".equals(category)){
				log.debug("Category is not empty, will try to build breadcrumb based on category");
				doc = buildBreadcrumbDocument(context, siteMap, nodeId, category, locale);
			}else{
				log.debug("Category is empty, will try to build breadcrumb based on sitemap");
				doc = buildBreadcrumbDocument(context, siteMap, nodeId);
			}
			Element nodes = (Element) doc.getRootElement().selectSingleNode("Nodes");
			if ((context.getParameterString("ID") != null) && (!context.getParameterString("ID").equals(""))){
	        	String dcrFile = context.getParameterString("ID").toString();
				
				Document dcrDoc = null;
				log.debug("ID paramater is not null, will determine whether it require to retrieve document");
				if (dcrFile.startsWith("templatedata") && dcrFile.contains("taggable_content") && !dcrFile.contains("..")){
					dcrDoc = context.getLiveSiteDal().readXmlFile(dcrFile);
					String title = dcrDoc.getRootElement().selectSingleNode("title") == null ? "" : dcrDoc.getRootElement().selectSingleNode("title").getText();
					if (!"".equals(title)){
						log.debug("Adding title from the document ["+title+"]");
						Element node = nodes.addElement("Node");
						node.addAttribute("Generated", "true");
						node.addAttribute("Article", "true");
						node.addElement("Label").addText(title);
					}
				}else{
					log.warn("Ignore because it does not contains the taggable_content path:" + dcrFile);
				}
	        }
			List breadCrumbLabelNodes = doc.getRootElement().selectNodes("//Nodes/Node/Label");
			
			log.debug("Nodes is null [ "+breadCrumbLabelNodes != null+"] and size: ["+breadCrumbLabelNodes.size()+"]");
			if (breadCrumbLabelNodes != null && breadCrumbLabelNodes.size() > 0){
				String breadCrumbHeadMeta = "";
				String categoriesHeadMeta = "";
				for (int i=0;i<breadCrumbLabelNodes.size();i++){
					String label = ((Element) breadCrumbLabelNodes.get(i)).getText();
					if (i==0){
						categoriesHeadMeta = "<meta name=\"categories\" content=\""+StringEscapeUtils.escapeHtml(label)+"\"/>";
					}
					String article = ((Element) breadCrumbLabelNodes.get(i)).getParent().attributeValue("Article");
					String product = ((Element) breadCrumbLabelNodes.get(i)).getParent().attributeValue("Product");
					if (!"true".equals(article) || !"true".equals(product)){
						breadCrumbHeadMeta = ("".equals(breadCrumbHeadMeta)) ? label : breadCrumbHeadMeta + " > " + label;
					}
				}
				log.debug("HEAD INJECTION: <meta name=\"breadcrumbs\" content=\""+StringEscapeUtils.escapeHtml(breadCrumbHeadMeta)+"\"/>" + categoriesHeadMeta);
				context.getPageScopeData().put(RuntimePage.PAGESCOPE_HEAD_INJECTION, "<meta name=\"breadcrumbs\" content=\""+StringEscapeUtils.escapeHtml(breadCrumbHeadMeta)+"\"/>" + categoriesHeadMeta);
			
			}else{
				log.debug("No Nodes found, will not perform HEAD INJECTION");
			}
		
		}catch (Exception e){
			log.error("Error while trying to build the breadcrumb", e);
		}

		log.debug("Breadcrumb doc: " + doc.asXML());
		return doc;
	}
	
	public void breadBreadcrumbByProduct(Element nodes,
			Document productExplorerBlock, String productCategory,
			String navCat1, String navCat2, String navCat3) {
		String xpath = "";
		if (productCategory != null && !"".equals(productCategory)){
			xpath = "//catalogue/ParamValue[text() = \""+productCategory+"\"]";
			if (navCat1 != null && !"".equals(navCat1)){
				xpath = xpath + "/../Sub1/Subnav1Param[text() = \""+navCat1+"\"]";
				if (navCat2 != null && !"".equals(navCat2)){
					xpath = xpath + "/../Sub2/Subnav2Param[text() = \""+navCat2+"\"]";
					if (navCat3 != null && !"".equals(navCat3)){
						xpath = xpath + "/../Sub3/Subnav3Param[text() = \""+navCat3+"\"]";
					}
				}
			}
		}
		if (!"".equals(xpath)){
			System.out.println(xpath);
			List categoryPages = productExplorerBlock.getRootElement().selectNodes(xpath);
			if (categoryPages != null && !categoryPages.isEmpty()){
				ArrayList<String> breadCrumbList = new ArrayList<String>();
				for (Element selectedNode = (Element) categoryPages.get(0);selectedNode != null;selectedNode = selectedNode.getParent()){
					injectNode(breadCrumbList, selectedNode);
				}
				for (int i=0;i<breadCrumbList.size();i++){
					
					String labelLinkPair = breadCrumbList.get(breadCrumbList.size() - i - 1);
					String[] labelLinkArr = labelLinkPair.split("#####");
					Element node = nodes.addElement("Node");
		        	//node.addAttribute("Generated", "true");
		        	node.addElement("Label").addText(labelLinkArr[0]);
		        	node.addElement("Link").addElement("Url").addText((labelLinkArr.length == 1 ) ? "" : labelLinkArr[1]);
				}
			}			
		}
		
	}
	public void buildBreadcrumbByCategoryPage(Element nodes, Document productExplorerBlock, String requestedPage){
		List categoryPages = productExplorerBlock.getRootElement().selectNodes("//cataloguepage[text() = \""+requestedPage+"\"] | //Sub1page[text() = \""+requestedPage+"\"] | //Sub2page[text() = \""+requestedPage+"\"] | //Sub3page[text() = \""+requestedPage+"\"]");
		if (categoryPages != null && !categoryPages.isEmpty()){
			ArrayList<String> breadCrumbList = new ArrayList<String>();
			for (Element selectedNode = (Element) categoryPages.get(0);selectedNode != null;selectedNode = selectedNode.getParent()){
				injectNode(breadCrumbList, selectedNode);
			}
			for (int i=0;i<breadCrumbList.size();i++){
				
				String labelLinkPair = breadCrumbList.get(breadCrumbList.size() - i - 1);
				String[] labelLinkArr = labelLinkPair.split("#####");
				Element node = nodes.addElement("Node");
	        	//node.addAttribute("Generated", "true");
	        	node.addElement("Label").addText(labelLinkArr[0]);
	        	node.addElement("Link").addElement("Url").addText((labelLinkArr.length == 1 ) ? "" : labelLinkArr[1]);
			}
		}
	}
	
	protected void injectNode(ArrayList<String> breadCrumbList, Element selectedNode){
		String[] pageField = {"cataloguepage","Sub1page", "Sub2page", "Sub3page"};
		String[] labelField = {"CatalogueSection","Subnav1", "Subnav2", "Subnav3"};
		for (int i=0;i<pageField.length;i++){
			if (selectedNode.selectSingleNode(labelField[i]) != null){
				String url = (selectedNode.selectSingleNode(pageField[i]) == null) ? "" : selectedNode.selectSingleNode(pageField[i]).getText();
				String label = selectedNode.selectSingleNode(labelField[i]).getText();
				breadCrumbList.add(label + "#####" + url);
	        	break;
			}
		}
	}
	
	protected Document retrieveProductExplorerBlock(RequestContext context, String locale){
		Document rtnDoc = null;
		InputStream stream = null;
		try {
			FileDal fileDal = context.getFileDal();
			String path = fileDal.getRoot() + "/templatedata/" + locale + "/product_explorer_block/data/products_hierarchy";
			log.debug("Retrieve product hierarchu: " + path);
			stream = fileDal.getStream(path);
			rtnDoc = Dom4jUtils.newDocument(stream);
		}catch(Exception e){
			log.error("Error while try to retrieveProductExplorerBlock", e);
		}finally{
			if (stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					//Ignore
				}
			}
		}
		
		return rtnDoc;
	}

	protected String getSelectedNodeId(SiteMap siteMap,
			BaseRequestContext context) {
		String nodeId = siteMap.findNodeId(context);
		log.debug("NodeID from the sitemap findNodeId: " + nodeId);
		if (StringUtils.isEmpty(nodeId)) {
			nodeId = siteMap.getSelectedNodeId();
			log.debug("NodeID from the sitemap get selected: " + nodeId);
		}
		
		return nodeId;
	}
	
	protected Document buildBreadcrumbDocument(RequestContext context, SiteMap map, String nodeId, String category, String locale) throws CacheException {
		
        Document doc = null;
        
        Document taggableContentExploreBlock = retrieveTaggableContentExplorerBlockDoc(context, locale, category);
        
        if (taggableContentExploreBlock != null){
			log.debug("taggableContentExploreBlock found, attempt to build the breadcrumb");
			doc = buildBreadcrumbDocumentByExplorerDoc(context, map, nodeId, locale, taggableContentExploreBlock);
		}else{
			log.debug("taggableContentExplorerBlock is null, will fall back to sitemap method");
			doc = buildBreadcrumbDocument(context, map, nodeId);
		}
        
       
        
        return doc;
	}
	
	protected Document buildBreadcrumbDocumentByExplorerDoc(RequestContext context, SiteMap map, String nodeId, String locale, Document explorerDoc) {
		Document doc = null;
        String category = context.getParameterString("Category");
        String section = context.getParameterString("Section");
        String pageName = context.getPageName();
        String parentPage = (explorerDoc.getRootElement().selectSingleNode("parent_page") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("parent_page").getText();
        
        String showQuarter = (explorerDoc.getRootElement().selectSingleNode("show_by_quarter") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("show_by_quarter").getText();
        
        String defaultListingPage = (explorerDoc.getRootElement().selectSingleNode("listing_page") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("listing_page").getText(); 
        String defaultDetailPage = (explorerDoc.getRootElement().selectSingleNode("detail_page") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("detail_page").getText();
        
        
        log.debug("Locale ["+locale+"], Category ["+category+"], section ["+section+"], parentPage ["+parentPage+"], showQuarter ["+showQuarter+"], defaultListingPage["+defaultListingPage+"], defaultDetailPage ["+defaultDetailPage+"]");
        
        List parentNodeIds = null;
        if ("".equals(parentPage) && (!"".equals(defaultListingPage) || !"".equals(defaultDetailPage))){
        	parentPage = ("".equals(defaultListingPage) ? defaultDetailPage : defaultListingPage);
        	log.debug("Parent page is now ["+parentPage+"]");
        }
        if (parentPage != null && !"".equals(parentPage)){
        	parentNodeIds = map.getNodeIds("0", parentPage.replace("/sites/" + context.getSite().getDirectory() + "/", ""));
        }
        
        log.debug("Node IDs return: " + parentNodeIds);
        
        if (parentNodeIds != null && !parentNodeIds.isEmpty()){
        	String parentNodeId = (String) parentNodeIds.get(0);
        	log.debug("Parent Node ID " + parentNodeId + " will try to use the sitemap to build the doc");
        	doc = buildBreadcrumbDocument(context, map, parentNodeId);
        	log.debug("Partial return based on the parent Node ID: " + doc.asXML());
        }else{
        	log.debug("No parent ID found, will create empty doc");
        	doc = Dom4jUtils.newDocument();
            Element root = doc.addElement("Breadcrumb");
            String startPage = context.getSite().getStartPage();
            if (StringUtils.isNotEmpty(startPage)) {
                emitPage(root, startPage).setName("StartPage");
            } else {
                root.addElement("StartPage");
            }
            
        }
        Element nodes = (Element) doc.getRootElement().selectSingleNode("Nodes");
        if (nodes == null){
        	nodes = doc.getRootElement().addElement("Nodes");
        }
        if ("true".equals(showQuarter)){
        	log.debug("Current explorer block is show quarterly, will ignore section parameter");
        	String label = (explorerDoc.getRootElement().selectSingleNode("name") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("name").getText();
        	String targetPageName = (!"".equals(defaultListingPage)) ? defaultListingPage : defaultDetailPage;
        	if (parentPage != null && parentPage.equals(targetPageName) && parentNodeIds != null && !parentNodeIds.isEmpty()){
        		log.debug("Parent Page is same as Target page and can be found in Sitemap, so we will skip because of duplication");
        	}else{
	        	if ("".equals(targetPageName)){
	        		targetPageName = pageName;
	        	}else{
	        		targetPageName = targetPageName.replace("/sites/" + context.getSite().getDirectory() + "/", "");
	        	}
	        	targetPageName.replaceAll("\\.page$", "");
	        	log.debug("Target Page Name: ["+targetPageName+"]");
	        	String url = context.getPageLink(targetPageName);
	        	log.debug("Target URL: ["+url+"]");
	        	Element node = nodes.addElement("Node");
	        	//node.addAttribute("Generated", "true");
	        	node.addElement("Label").addText(label);
	        	node.addElement("Link").addElement("Url").addText(url);
        	}
        }else if (section != null && !"".equals(section)){
        	log.debug("Section is not empty ["+section+"], will build based on section");
        	buildBreadcrumbBySection(context, locale, nodes, explorerDoc, section);
        
        }
        
        
        
        return doc;
	}
	
	public void buildBreadcrumbBySection(RequestContext context, String locale, Element nodes, Document explorerDoc, String section){
    	String[] sections = section.split("/");
    	Element current = explorerDoc.getRootElement();
        String defaultListingPage = (explorerDoc.getRootElement().selectSingleNode("listing_page") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("listing_page").getText();     	
    	String categoryIdentifier = (explorerDoc.getRootElement().selectSingleNode("identifier") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("identifier").getText();
        String currentIdentifier = "";
    	for (int i=0;i<sections.length;i++){
    		String identifier = sections[i];
    		String sectionName = "section_three";
    		if (i==0){
    			sectionName = "section_one";
    		}else if (i==1){
    			sectionName = "section_two";
    		}
    		
    		currentIdentifier = ("".equals(currentIdentifier)) ? identifier : currentIdentifier + "/" + identifier;
    		
    		Element identifierNode = (Element) current.selectSingleNode(sectionName + "/identifier[text()='" +identifier+"']" );
    		if (identifierNode == null){
    			break;
    		}
    		current = identifierNode.getParent();
    		Element node = nodes.addElement("Node");
    		node.addAttribute("Generated", "true");
    		node.addElement("Label").addText(current.selectSingleNode("title").getText());
    		String url = current.selectSingleNode("external_link") == null ? "" : current.selectSingleNode("external_link").getText();
    		if ("".equals(url) ){
    			url = current.selectSingleNode("listing_page") == null ? "" : current.selectSingleNode("listing_page").getText();
    			if ("".equals(url)){
    				url = defaultListingPage;
    			}
    			if (context.isPreview()){
    				url = url + "?Category=" + categoryIdentifier + "&section=" + currentIdentifier;
    			}else{
    				url = "/"+locale+"/" + categoryIdentifier + "/" + currentIdentifier + ".tag";
    			}
    		}
    		node.addElement("Link").addElement("Url").addText(url);
    		log.debug( identifier + ":" + current.selectSingleNode("title").getText() + ",url:" + url);
    	}
	}
	
	protected Document retrieveTaggableContentExplorerBlockDoc(RequestContext context, String locale, String category) throws CacheException{
		log.debug("Attempt to load localeExplorerBlockMap block for locale ["+locale+"] and category ["+category+"], preview ["+context.isPreview()+"]");
		HashMap<String, Document> localeExplorerBlockMap = null;

		if (!context.isPreview()){
			JCS explorerBlockCache = JCS.getInstance("taggbleContentExplorerBlock");
			localeExplorerBlockMap = (HashMap<String, Document>) explorerBlockCache.get(locale);
		}
		
		if (localeExplorerBlockMap == null){
			localeExplorerBlockMap = buildExplorerBlockMap(context, locale);
		}		

		Document explorerDoc = localeExplorerBlockMap.get(category);

		
		return explorerDoc;
	}
	
	protected synchronized HashMap<String, Document> buildExplorerBlockMap(RequestContext context, String locale) throws CacheException {
		JCS explorerBlockCache = null;
		HashMap<String, Document> localeExplorerBlockMap = null;
		if (!context.isPreview()){
			explorerBlockCache = JCS.getInstance("taggbleContentExplorerBlock");
			localeExplorerBlockMap = (HashMap<String, Document>) explorerBlockCache.get(locale);
		}
		if (localeExplorerBlockMap == null){
			log.debug("Building the locale/taggable content explorer block map for locale ["+locale+"]");
			FileDal fileDal = context.getFileDal();
			String path = fileDal.getRoot() + "/templatedata/" + locale + "/taggable_content_explorer_block/data";
			log.debug("Look up files within the following path: " + path);
			ArrayList<String> files = retrieveAllFilePaths(fileDal, path);
			log.debug("Files found: " + files);
			localeExplorerBlockMap = new HashMap<String, Document>();
			for (Iterator<String> it = files.iterator();it.hasNext();){
				String filePath = it.next();
				log.debug("Attempt to parse doc ["+filePath+"]");
				InputStream fileStream = null;
				
				try {
					fileStream = fileDal.getStream(filePath);
					Document doc = Dom4jUtils.newDocument(fileStream);
					String identifier = doc.getRootElement().selectSingleNode("identifier").getText();
					log.debug("Identifier found ["+identifier+"] for path ["+filePath+"]");
					if (!"".equals(identifier)){
						localeExplorerBlockMap.put(identifier, doc);
					}
				}catch(Exception e){
					log.warn("Fail to parse the doc ["+filePath+"], will ignore");
				}finally{
					if (fileStream != null){
						try {
							fileStream.close();
						} catch (IOException e) {
							//Ignore
						}
					}
				}
			}
			if (!context.isPreview()){
				explorerBlockCache.put(locale, localeExplorerBlockMap);
			}
		}
		return localeExplorerBlockMap;
	}
	
	protected ArrayList<String> retrieveAllFilePaths(FileDal fileDal, String path){
		ArrayList<String> rtn = new ArrayList<String>();
		log.debug("Looking into child directory of path ["+path+"]");
		String[] dirs = fileDal.getChildDirectories(path);
		for (int i=0;i<dirs.length;i++){
			if (!".".equals(dirs[i]) && !"..".equals(dirs[i])){
				ArrayList<String> files = retrieveAllFilePaths(fileDal, path + "/" + dirs[i]);
				rtn.addAll(files);
			}
		}
		log.debug("Looking into child files of path ["+path+"]");
		String[] files = fileDal.getChildFiles(path);
		for (int i=0;i<files.length;i++){
			if (!".".equals(files[i]) && !"..".equals(files[i])){
				rtn.add(path + "/" + files[i]);
			}
		}
		return rtn;
	}
	
	protected Document buildBreadcrumbDocument(RequestContext context, SiteMap map, String nodeId) {
		Document doc = Dom4jUtils.newDocument();
        Element root = doc.addElement("Breadcrumb");
        String refAttrId = null;
        Attribute visibleBreadCrumbAttr = null;
        String visibleBreadCrumbAttrValue = null;

        String startPage = context.getSite().getStartPage();
        if (StringUtils.isNotEmpty(startPage)) {
            emitPage(root, startPage).setName("StartPage");
        } else {
            root.addElement("StartPage");
        }
        
        if (StringUtils.isNotEmpty(nodeId)) {
            List nodes = new ArrayList();
            Element current = (Element) map.getDocument().getRootElement().selectSingleNode("segment" + "//" + "node" + "[@" + "id" + "='" + nodeId + "']");

            if (current != null) {
                refAttrId = current.attributeValue("refid");

                if (null != refAttrId) {
                    current = (Element) map.getDocument().getRootElement().selectSingleNode("segment" + "//" + "node" + "[@" + "id" + "='" + refAttrId + "']");
                }

                visibleBreadCrumbAttr = current.attribute("visible-in-breadcrumbs");
                if (null != visibleBreadCrumbAttr) {
                    visibleBreadCrumbAttrValue = visibleBreadCrumbAttr.getValue();
                }

                if ("false".equals(visibleBreadCrumbAttrValue)) {
                    return null;
                }
            }

            while ((current != null) && (!(current.getName().equals("segment")))) {
                Element node = current.createCopy();
                node.content().removeAll(node.selectNodes("node"));
                nodes.add(0, node);
                current = current.getParent();
            }

            Element last = root.addElement("Nodes");
            int nodeCount = nodes.size();

            for (int i = 0; i < nodeCount; ++i) {
                Element newNode = (Element) nodes.get(i);
                String nId = newNode.attributeValue("id");

                log.debug("calling getNodeLinkUrl with:" + nId);
                String url = map.getNodeLinkUrl(nId, context);
                log.debug("url:" + url);

                this.mXmlAdapter.toExternalFormat(newNode);
                
                ((Element) newNode.elements("Link").get(0)).addElement("Url").setText((StringUtils.isNotEmpty(url)) ? url : "");
                
                log.debug("newNode=" + newNode.asXML());

                last.add(newNode);
//                last = newNode;
            }
        }
        
		return doc;
	}
	
	protected Element emitPage(Element parent, String pageName) {
        Element root = (parent == null) ? DocumentHelper.createElement("Page") : parent.addElement("Page");

        root.setText(pageName);
        return root;
    }
	
    static class ExternalSiteMapXmlAdapter {

        public void toExternalFormat(Element node) {
            formatNames(node);
        }

        protected void formatNames(Element e) {
            e.setName(formatXmlName(e.getName()));
            List attributes = new LinkedList(e.attributes());
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                Attribute a = (Attribute) i.next();
                a.detach();
                e.addAttribute(formatXmlName(a.getName()), a.getValue());
            }
            for (Iterator i = e.elements().iterator(); i.hasNext();) {
                Element el = (Element) i.next();
                formatNames(el);
            }
        }

        protected String formatXmlName(String name) {
            StringBuffer newName = new StringBuffer();
            int i = name.indexOf("-");
            while (i > -1) {
                newName.append(name.substring(0, 1).toUpperCase()).append((i == 1) ? "" : name.substring(1, i).toLowerCase());

                if (i >= name.length()) {
                    break;
                }
                name = name.substring(i + 1);
                i = name.indexOf("-");
            }

            if (name.length() > 0) {
                name = name.toLowerCase();
                newName.append(name.substring(0, 1).toUpperCase());
                if (name.length() > 1) {
                    newName.append(name.substring(1));
                }
            }
            return newName.toString();
        }
    }	
}
