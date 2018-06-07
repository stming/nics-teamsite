package com.interwoven.teamsite.nikon.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;

public class RewriteFilter implements Filter {

	private String SOLR_SERVER = "localhost:8983";
	private String MASTER_LOCALE = "en_Asia";
	
	private String TAG_EXT = ".tag";
	private String DCR_EXT = ".dcr";
	
	private String PRODUCT_PREFIX = "product";
	private String PRODUCT_DETAIL_PAGE = "products/product_details.page";
	private ServletContext context;
	private Pattern localePattern = Pattern.compile("^([a-zA-Z]{2}_[a-zA-Z]{2,4})/(.*)");
	
	private static Log log = LogFactory.getLog(RewriteFilter.class);
	
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI();
		String[] paths = requestURI.split("/");
		log.debug("Request URI: " + httpRequest.getRequestURI());
		log.debug("Request URL: " + httpRequest.getRequestURL());
		Enumeration names = httpRequest.getAttributeNames();
		while (names.hasMoreElements()){
			String name = (String) names.nextElement();
			log.debug("Attr Name: " + name + ":" + httpRequest.getAttribute(name));
		}
		
		Enumeration paraNames = httpRequest.getParameterNames();
		while (paraNames.hasMoreElements()){
			String name = (String) paraNames.nextElement();
			log.debug("Para Name: " + name + ":" + httpRequest.getParameter(name));
		}
		try {
			if (requestURI.endsWith(TAG_EXT) || requestURI.endsWith(DCR_EXT) || requestURI.endsWith(PRODUCT_DETAIL_PAGE) || (paths.length > 2 && paths[2].equals(PRODUCT_PREFIX))){
				log.debug("Within the request, will try to wrap it for rewrite");
				if (requestURI.endsWith(TAG_EXT) || requestURI.endsWith(DCR_EXT)){
					if (paths.length > 2){
						HashMap<String, Document> localeExplorerBlockMap = null;
						String locale = paths[1];
						if (!"iw-preview".equals(locale)){
							JCS explorerBlockCache = JCS.getInstance("taggbleContentExplorerBlock");
							localeExplorerBlockMap = (HashMap<String, Document>) explorerBlockCache.get(locale);						
						}
						if (localeExplorerBlockMap == null){
							localeExplorerBlockMap = buildExplorerBlockMap(httpRequest, locale);
						}
						String category = paths[2];
						log.debug("Category: " + category);
						
						Document explorerDoc = localeExplorerBlockMap.get(category);
						if (explorerDoc != null){
							String targetPage = "";
							String section = "";
							String pathId = "";
							boolean found = false;
							for (int i=3;i<paths.length;i++){
								String path = paths[i];
								if (paths[i].endsWith(TAG_EXT)){
									path = path.replace(TAG_EXT, "");
									found = true;
								}
								if (!found || paths[i].endsWith(TAG_EXT)){
									section = ("".equals(section)) ? path : section + "/" + path;
								}else{
									if (paths[i].endsWith(DCR_EXT)){
										path = path.replace(DCR_EXT, "");
									}
									pathId = ("".equals(pathId)) ? path : pathId + "/" + path;
								}
							}
							String showQuarter = (explorerDoc.getRootElement().selectSingleNode("show_by_quarter") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("show_by_quarter").getText();
						    String defaultListingPage = (explorerDoc.getRootElement().selectSingleNode("listing_page") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("listing_page").getText();     	
						    String defaultDetailPage = (explorerDoc.getRootElement().selectSingleNode("detail_page") == null ) ? "" : explorerDoc.getRootElement().selectSingleNode("detail_page").getText();
					        log.debug("Section ["+section+"], showQuarter ["+showQuarter+"], defaultListingPage ["+defaultListingPage+"], defaultDetailPage ["+defaultDetailPage+"]");
					        String listingPage = "";
					        String detailPage = "";
					        String xpath = "";
					        String[] sections = section.split("/");
					    	for (int i=0;i<sections.length;i++){
					    		String identifier = sections[i];
					    		String sectionName = "section_three";
					    		if (i==0){
					    			sectionName = "section_one";
					    		}else if (i==1){
					    			sectionName = "section_two";
					    		}

					    		xpath = ("".equals(xpath)) ? sectionName + "/identifier[text()='" +identifier+"']" : xpath + "/../" + sectionName + "/identifier[text()='" +identifier+"']";
					    	}
					    	if (!"".equals(xpath)){
					    		log.debug("Attempt to get document for xpath: ["+xpath+"]");
					    		Element identifierNode = (Element) explorerDoc.getRootElement().selectSingleNode(xpath);
					    		if (identifierNode != null){
					    			Element current = identifierNode.getParent();
					    			listingPage = current.selectSingleNode("listing_page") == null ? "" : current.selectSingleNode("listing_page").getText();
					    			detailPage = current.selectSingleNode("detail_page") == null ? "" : current.selectSingleNode("detail_page").getText();

					    		}
					    	}
					        log.debug("Section listing page: ["+listingPage+"], detail page: ["+detailPage+"] " );
					        if (requestURI.endsWith(TAG_EXT)){
						        if (!"".equals(listingPage)){
						        	targetPage = listingPage;
						        }else if (!"".equals(defaultListingPage)){
						        	targetPage = defaultListingPage;
						        }else if (!"".equals(detailPage)){
						        	targetPage = detailPage;
						        }else if (!"".equals(defaultDetailPage)){
						        	targetPage = defaultDetailPage;
						        }
					        }
					        if (requestURI.endsWith(DCR_EXT)){
					        	if (!"".equals(detailPage)){
						        	targetPage = detailPage;
						        }else if (!"".equals(defaultDetailPage)){
						        	targetPage = defaultDetailPage;
						        }
					        	if (!"".equals(pathId)){
					        		if (!localePattern.matcher(pathId).matches()){
					        			pathId = "templatedata/" + locale + "/taggable_content/data/" + pathId;
					        		}else{
					        			String[] pathIds = pathId.split("/");
					        			pathId = "templatedata/" + pathIds[0] + "/taggable_content/data/";
					        			for (int i=1;i<pathIds.length;i++){
					        				pathId = pathId + "/" + pathIds[i];
					        			}
					        		}
					        		log.info("pathId:" + pathId);
					        	}
					        	pathId = URLDecoder.decode(pathId,"UTF-8");
					        }else{
					        	pathId = "";
					        }
					        if (!"".equals(targetPage)){
					        	targetPage = targetPage.replace("/sites/", "/");
					        	log.debug("Resolved rewrite page: " + targetPage + ",section:" + section + ",category:" + category);
								Hashtable rewriteRequestMaps = new Hashtable(request.getParameterMap());
								rewriteRequestMaps.put("Section", new String[] {section});
								rewriteRequestMaps.put("Category", new String[] {category});
								if (!"".equals(pathId)){
									rewriteRequestMaps.put("ID", new String[] {pathId});
								}
								RewriteRequestWrapper wrapper = new RewriteRequestWrapper(httpRequest, targetPage, rewriteRequestMaps);
								chain.doFilter(wrapper, response);
								return;
					        }
						}else{
							log.error("Cannot found category: ["+category+"], will ignore");
						}
					}
				}else if (requestURI.endsWith(PRODUCT_DETAIL_PAGE)){
					String productId = request.getParameter("ID");
					String locale = paths[1];
					if (!"".equals(productId) && productId != null){
						JCS idToFriendlyMapCache = JCS.getInstance("idToFriendlyMap");
						String returnDoc = (String) idToFriendlyMapCache.get(locale + "/" + productId);
						Document doc = null;
						if (returnDoc != null){
							log.debug("got the idToFriendly from cache");
							doc = Dom4jUtils.newDocument(returnDoc);
						}else{
							doc = querySolr("?q="+URLEncoder.encode("+pno_s:" + productId + " +locale_s:" + locale, "UTF-8") + "&fl=product_navcat1_s,product_navcat2_s,product_navcat3_s,product_category_s,title_ut,locale_s,pno_s", "products", this.SOLR_SERVER);
							idToFriendlyMapCache.put(locale + "/" + productId, doc.asXML());
						}
						log.debug("returned doc: " + doc.asXML());
						Node resultElement = doc.getRootElement().selectSingleNode("result");
						if (resultElement.selectSingleNode("doc") != null) {
							Node docNode = resultElement.selectSingleNode("doc");
							String productName = docNode.selectSingleNode("str[@name='title_ut']") == null ? "" : docNode.selectSingleNode("str[@name='title_ut']").getText();
							String productCategory = docNode.selectSingleNode("str[@name='product_category_s']") == null ? "" : docNode.selectSingleNode("str[@name='product_category_s']").getText();
							String productNavCat1 = docNode.selectSingleNode("str[@name='product_navcat1_s']") == null ? "" : docNode.selectSingleNode("str[@name='product_navcat1_s']").getText();
							String productNavCat2 = docNode.selectSingleNode("str[@name='product_navcat2_s']") == null ? "" : docNode.selectSingleNode("str[@name='product_navcat2_s']").getText();
							String productNavCat3 = docNode.selectSingleNode("str[@name='product_navcat3_s']") == null ? "" : docNode.selectSingleNode("str[@name='product_navcat3_s']").getText();
							String url = "";
							url = "/" + locale + "/product/" + 
							normalizePath(replaceTemporaryFix(productCategory)) + "/" + 
							normalizePath(replaceTemporaryFix(productNavCat1)) + "/" + 
							normalizePath(replaceTemporaryFix(productNavCat2)) + "/" + 
							normalizePath(replaceTemporaryFix(productNavCat3)) + "/" + 
							URLEncoder.encode(normalizePath(replaceTemporaryFix(productName)),"UTF-8");
							url = url.replaceAll("[\\/]{2,}", "/"); // Remove empty path name
							url = url.replaceAll("[\\-]{2,}", "-"); // Remove empty path name
							log.info("URL rewrite to: " + url);
							
							HttpServletResponse httpResponse = (HttpServletResponse) response;
							httpResponse.sendRedirect(url);
							return;
						}

					}
				}else{
					
					String productName = "";
					String productCategory = "";
					String productNavCat1 = "";
					String productNavCat2 = "";
					String productNavCat3 = "";
					String locale = paths[1];
					for (int i=3;i<paths.length;i++){
						if (i == paths.length-1){
							productName = URLDecoder.decode(paths[i],"UTF-8");
						}else if (i == 3){
							productCategory = paths[i];
						}else if (i == 4){
							productNavCat1 = paths[i];
						}else if (i == 5){
							productNavCat2 = paths[i];
						}else if (i == 6){
							productNavCat3 = paths[i];
						}
					}
					String query = "+locale_s: " + locale;
					if (!"".equals(productName)){
						query = query + " +title_text_ss:" + productName;
					}
					if (!"".equals(productCategory)){
						query = query + " +product_category_ss:" + productCategory;
					}
					if (!"".equals(productNavCat1)){
						query = query + " +product_navcat1_ss:" + productNavCat1;
					}
					if (!"".equals(productNavCat2)){
						query = query + " +product_navcat2_ss:" + productNavCat2;
					}
					if (!"".equals(productNavCat3)){
						query = query + " +product_navcat3_ss:" + productNavCat3;
					}
					JCS friendlyToIdMapCache = JCS.getInstance("friendlyToIdMap");
					String returnDoc = (String) friendlyToIdMapCache.get(query);
					Document doc = null;
					if (returnDoc != null){
						log.debug("got the friendlyToIdMap from cache");
						doc = Dom4jUtils.newDocument(returnDoc);
					}else{
						doc = querySolr("?q="+URLEncoder.encode(query, "UTF-8") + "&fl=product_navcat1_s,product_navcat2_s,product_navcat3_s,product_category_s,title_ut,locale_s,pno_s", "products", this.SOLR_SERVER);
						friendlyToIdMapCache.put(query, doc.asXML());
					}
					log.debug("returned doc: " + doc.asXML());
					Node resultElement = doc.getRootElement().selectSingleNode("result");
					if (resultElement.selectSingleNode("doc") != null) {
						Node docNode = resultElement.selectSingleNode("doc");
						String productNo = docNode.selectSingleNode("str[@name='pno_s']") == null ? "" : docNode.selectSingleNode("str[@name='pno_s']").getText();
						Hashtable rewriteRequestMaps = new Hashtable(request.getParameterMap());
						rewriteRequestMaps.put("ID", new String[] {productNo});
						log.info("rewrite to product id: " + productNo);
						RewriteRequestWrapper wrapper = new RewriteRequestWrapper(httpRequest, "/" + locale + "/" + PRODUCT_DETAIL_PAGE, rewriteRequestMaps);
						chain.doFilter(wrapper, response);
						return;
					}else{
						log.error("Product not found, will redirect back to homepage");
						HttpServletResponse httpResponse = (HttpServletResponse) response;
						httpResponse.sendRedirect("/" + locale + "/");
						return;
					}
				}
			}
		}catch (Exception e){
			log.error("Error while trying to rewrite URL", e);
		}
		chain.doFilter(httpRequest, response);
		
		
	}

	public static String normalizePath(String path) {
		if ("".equals(path) || path == null || "null".equals(path)){
			return "";
		}else{
			return replaceAccentLetters(path.toLowerCase().replaceAll("[ \\+\\#\\/\\(\\)\\,\\&\\:\\.]", "-").replaceAll("\\-$", ""));
		}
	}
	
	public static String replaceAccentLetters (String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
	
    public static String replaceTemporaryFix(String cat)
    {
        if("Fixed Focus".equals(cat))
            return "Single Focal Length";
        else
            return cat;
    }
    
	protected synchronized HashMap<String, Document> buildExplorerBlockMap(HttpServletRequest request, String locale) throws CacheException {
		JCS explorerBlockCache = null;
		HashMap<String, Document> localeExplorerBlockMap = null;
		if (!"iw-preview".equals(locale)){
			explorerBlockCache = JCS.getInstance("taggbleContentExplorerBlock");
			localeExplorerBlockMap = (HashMap<String, Document>) explorerBlockCache.get(locale);
		}
		if (localeExplorerBlockMap == null){
			log.debug("Building the locale/taggable content explorer block map for locale ["+locale+"]");
			String path = "/templatedata/" + locale + "/taggable_content_explorer_block/data";
			log.debug("Look up files within the following path: " + path);
			ArrayList<String> files = retrieveAllFilePaths(request, path);
			log.debug("Files found: " + files);
			localeExplorerBlockMap = new HashMap<String, Document>();
			for (Iterator<String> it = files.iterator();it.hasNext();){
				String filePath = it.next();
				log.debug("Attempt to parse doc ["+filePath+"]");
				InputStream fileStream = null;
				
				try {
					fileStream = context.getResourceAsStream(filePath);
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
			if (!"iw-preview".equals(locale)){
				explorerBlockCache.put(locale, localeExplorerBlockMap);
			}
		}
		return localeExplorerBlockMap;
	}
	
	protected ArrayList<String> retrieveAllFilePaths(HttpServletRequest request, String path){
		ArrayList<String> rtn = new ArrayList<String>();
		String realPath = context.getRealPath(path);
		log.debug("Looking into child directories/files of path ["+realPath+"]");
		File dir = new File(realPath);
		if (dir.isDirectory() && dir.exists()){
			File[] files = dir.listFiles();
			for (int i=0;i<files.length;i++){
				if (!".".equals(files[i].getName()) && !"..".equals(files[i].getName())){
					if (files[i].isDirectory() && files[i].exists()){
						ArrayList<String> rtnFiles = retrieveAllFilePaths(request, path + "/" + files[i].getName());
						rtn.addAll(rtnFiles);
					}else if (files[i].isFile() && files[i].exists()){
						rtn.add(path + "/" + files[i].getName());
					}
				}
			}			
		}
		return rtn;
	}	
	public void init(FilterConfig config) throws ServletException {
		String solrServer = config.getInitParameter("SOLR_SERVER");
		context = config.getServletContext();
		if (!"".equals(solrServer) && solrServer != null){
			this.SOLR_SERVER = solrServer;
		}
		
		String masterLocale = config.getInitParameter("MASTER_LOCALE");
		if (!"".equals(masterLocale) && masterLocale != null){
			this.MASTER_LOCALE = masterLocale;
		}
		
		String tagExt = config.getInitParameter("TAG_EXT");
		if (!"".equals(tagExt) && tagExt != null){
			this.TAG_EXT = tagExt;
		}
		
		String dcrExt = config.getInitParameter("DCR_EXT");
		if (!"".equals(dcrExt) && dcrExt != null){
			this.DCR_EXT = dcrExt;
		}
		
		String productPrefix = config.getInitParameter("PRODUCT_PREFIX");
		if (!"".equals(productPrefix) && productPrefix != null){
			this.PRODUCT_PREFIX = productPrefix;
		}
		
	}

	private Document querySolr(String queryXML, String core, String serverUrl) {
		
		Document results = null;
		
		String solrServerUrl = "http://" + serverUrl + "/solr/"+core+"/select/";
		
		log.debug("Solr URL: " + solrServerUrl + ", Query: " + queryXML);
		
		try {
			
			URL urlForInfWebSvc = new URL(solrServerUrl + queryXML);

            URLConnection UrlConnInfWebSvc = urlForInfWebSvc.openConnection();
            
            HttpURLConnection httpUrlConnInfWebSvc = (HttpURLConnection) UrlConnInfWebSvc;
			            httpUrlConnInfWebSvc.setDoOutput( true );
			            httpUrlConnInfWebSvc.setRequestProperty( "Content-Type", "application/xml; charset=utf-8" );
			            httpUrlConnInfWebSvc.setRequestProperty( "Accept", "application/xml; charset=utf-8" );
			
		    BufferedReader infWebSvcReplyReader = new BufferedReader( new InputStreamReader( httpUrlConnInfWebSvc.getInputStream(), "UTF8" ) );
            
            String line;
            String infWebSvcReplyString = "";
            
            while (( line = infWebSvcReplyReader.readLine() ) != null )
            {
                infWebSvcReplyString = infWebSvcReplyString.concat( line );
            }

            infWebSvcReplyReader.close();
            httpUrlConnInfWebSvc.disconnect();
		
		
			results = Dom4jUtils.newDocument( infWebSvcReplyString );
			results.setXMLEncoding( "UTF-8" );
		
		}
		catch (MalformedURLException e) {
			
			System.out.println( "Error has occurred: " + e.toString() );
		
		} catch (IOException e) {

			System.out.println( "Error has occurred: " + e.toString() );
		}
		
		return results;
	}
}
