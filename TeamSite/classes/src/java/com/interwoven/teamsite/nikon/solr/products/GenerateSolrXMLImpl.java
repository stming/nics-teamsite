package com.interwoven.teamsite.nikon.solr.products;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.livesite.dom4j.Dom4jUtils;

public class GenerateSolrXMLImpl implements GenerateSolrXML {

	private static final Log logger = LogFactory.getLog(GenerateSolrXMLImpl.class);
	
	public String generate(CSClient client, String locale, String localeFileVPath) {
	
		logger.info("Generate SOLR XML");
		
		String localeInputXML = "";
		String masterInputXML = "";
		String outputXML = "";
		
		try 
		{
			// Locale DCR
			CSVPath csVpath = new CSVPath(localeFileVPath);
			CSSimpleFile localeFile = (CSSimpleFile) client.getFile(csVpath);
			
			logger.info("Locale DCR: " + localeFileVPath);
			
			// Master en_Asia DCR
			String masterFileVPath = localeFileVPath.replaceAll("(.*)/Nikon/(.*)/WORKAREA/(.*)", "$1/Nikon/Asia/WORKAREA/$3");
			       masterFileVPath = masterFileVPath.replaceAll("(.*)/templatedata/(.*)/(product_information_container|accessory_information_container)/(.*)",
			    		   										"$1/templatedata/en_Asia/$3/$4");
			       
			CSVPath csMasterVpath = new CSVPath(masterFileVPath);
			CSSimpleFile masterFile = (CSSimpleFile) client.getFile(csMasterVpath);
			
			logger.info("Master DCR: " + masterFileVPath);
			
			if ((localeFile != null) && 
				(localeFile.isValid()) && 
				(localeFile.getKind() == CSSimpleFile.KIND)) {
			
				BufferedInputStream in = null;
				Reader reader = null;
				BufferedReader bReader = null;
					
				// Locale DCR
				try {
						
					in = localeFile.getBufferedInputStream(false);
					reader = new InputStreamReader(in, "UTF-8");
					bReader = new BufferedReader(reader);
					
					StringBuffer inputString = new StringBuffer();

					String thisLine;
					
					while ((thisLine = bReader.readLine()) != null) {
					    inputString.append(thisLine + "\n");
					}
					
					localeInputXML = inputString.toString();
						
				} catch (IOException ex) {
					ex.printStackTrace();
			    } finally {
			            
			        	try {
			                if (in != null)
			                    in.close();
			            } catch (IOException ex) {
			                ex.printStackTrace();
			            }
			    }
			    
			    // Master DCR
			    if ((masterFile != null) && 
					(masterFile.isValid()) && 
					(masterFile.getKind() == CSSimpleFile.KIND)) {

				    try {
						
						in = masterFile.getBufferedInputStream(false);
						reader = new InputStreamReader(in, "UTF-8");
						bReader = new BufferedReader(reader);
							
						StringBuffer inputString = new StringBuffer();

						String thisLine;
						
						while ((thisLine = bReader.readLine()) != null) {
						    inputString.append(thisLine + "\n");
						}
						masterInputXML = inputString.toString();

							
					} catch (IOException ex) {
						ex.printStackTrace();
				    } finally {
				            
				        	try {
				                if (in != null)
				                    in.close();
				            } catch (IOException ex) {
				                ex.printStackTrace();
				            }
				    }
				}
					
			    Document solrXML = retrieveData(localeFile, locale, localeInputXML, masterInputXML);
			        	 solrXML.setXMLEncoding("UTF-8");
			        	 
			    outputXML = solrXML.getRootElement().selectSingleNode("add/doc").asXML();
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		return outputXML;
	}

	@SuppressWarnings("unchecked")
	private Document retrieveData(CSSimpleFile dcrFile, String locale, String localeXML, String masterXML) throws ParseException {
		
		// Create Output Document
		Document solrXML = Dom4jUtils.newDocument("<update/>");
	    		 solrXML.setXMLEncoding("UTF-8");
	    		 solrXML.getRootElement().addAttribute("type", "PRODUCT");
	    		 
	    // Parse Locale DCR Document
	    Document lProductXML = Dom4jUtils.newDocument(localeXML);
	    Element lRootElement = lProductXML.getRootElement();
	    
	    // Parse Master DCR Document
	    //Document mProductXML = Dom4jUtils.newDocument(masterXML);
	    //Element mRootElement = mProductXML.getRootElement();
   		
	    // DCR Variables
	    String productCategoryImage = null;
	    String productCategoryDescription = null;
	    String productOverviewDescription = null;
	    String productOverviewImage = null;
	    String productKeyFeatures = null;
	    String productBrochure = null;
	    
	    // Filter Variables
	    String productFilterType = null;
	    String productCompare = null;
	    
	    // Filter Variables - DSLR
	    String productFilterDType = null;
	    String productFilterDEffectivePixels = null;
	    String productFilterDImageSensorFormat = null;
	    String productFilterDMaximumISO = null;
	    String productFilterDFeatures = null;
	    String productFilterDPrice = null;
	    
	    // Filter Variables - Mirrorless
	    String productFilterMLType = null;
	    String productFilterMLEffectivePixels = null;
	    String productFilterMLImageSensorFormat = null;
	    String productFilterMLMaximumISO = null;
	    String productFilterMLFeatures = null;
	    String productFilterMLPrice = null;
	    
	    // Filter Variables - DL
	    String productFilterDlFeatures = null;
	    String productFilterDlMovie = null;
	    String productFilterDlMegapixels = null;
	    String productFilterDlZoom = null;
	    String productFilterDlMonitor = null;
	    String productFilterDlPrice = null;
	    
	    // Filter Variables - KeyMission
	    String productFilterKmAnimation = null;
	    String productFilterKmAngleOfView = null;
	    String productFilterKmConnectivity = null;
	    String productFilterKmWeight = null;
	    
	    // Filter Variables - COOLPIX
	    String productFilterCType = null;
	    String productFilterCFeatures = null;
	    String productFilterCMovie = null;
	    String productFilterCMegapixels = null;
	    String productFilterCZoom = null;
	    String productFilterCMonitor = null;
	    String productFilterCPrice = null;
	    
	    // Filter Variables - NIKON 1 ACIL
	    String productFilterN1Features = null;
	    String productFilterN1Megapixels = null;
	    String productFilterN1Fps = null;
	    String productFilterN1MaximumISO = null;
	    String productFilterN1Price = null;
	    
	    // Filter Variables - Lenses
	    String productFilterLType = null;
	    String productFilterLCategories = null;
	    String productFilterLFocalMin = null;
	    String productFilterLFocalMax = null;
	    String productFilterLMaximumAperture = null;
	    String productFilterLFormat = null;
	    String productFilterLTechnology = null;
	    String productFilterLPrice = null;
	    
	    // Filter Variables - Z-Mount Lenses
	    String productFilterZMSLine = null;
	    String productFilterZMType = null;
	    String productFilterZMCategories = null;
	    String productFilterZMFocalMin = null;
	    String productFilterZMFocalMax = null;
	    String productFilterZMMaximumAperture = null;
	    String productFilterZMFormat = null;
	    String productFilterZMTechnology = null;
	    String productFilterZMPrice = null;
	    
	    // Filter Variables - 1 Nikkor Lenses
	    String productFilterN1LType = null;
	    String productFilterN1LCategories = null;
	    String productFilterN1LFocalMin = null;
	    String productFilterN1LFocalMax = null;
	    String productFilterN1LMaximumAperture = null;
	    String productFilterN1LTechnology = null;
	    String productFilterN1LPrice = null;
		
	    //==================================================================
		// Process Metadata Fields
		//==================================================================
		String metaLocale = locale;
		String metaProductCategory = null;
		String metaNavCat1 = null;
		String metaNavCat2 = null;
		String metaNavCat3 = null;
		String metaWWADate = null;
		String metaProductId = null;
		String metaIsKit = null;
		String metaIsDiscontinued = null;
		String metaIsNew = null;
		String metaEndOfNew = null;
		String metaSortOrder = null;
		String metaShortName = null;
		String metaFormalName = null;
		String metaLocalProduct = null;
		String metaLocalShortName = null;
		String metaActive = null;
		String metaOptout = null;
		String metaAvailableDate = null;
		
		try {
			
			if ((dcrFile != null) && 
				(dcrFile.isValid()) && 
				(dcrFile.getKind() == CSSimpleFile.KIND)) {
			
				    CSExtendedAttribute[] extAttrs = dcrFile.getExtendedAttributes(null);
									
					for (int i = 0; i < extAttrs.length; i++) {
						
						String name = extAttrs[i].name;
						String value = extAttrs[i].value;
						
						logger.debug("Extended Attribute: " + name + " - " + value);
						
						if (name.equals("TeamSite/Metadata/ProductCategory")) {
							
							metaProductCategory = value;
						
						} else if (name.equals("TeamSite/Metadata/NavCat1")) {
						
							metaNavCat1 = value;
						
						} else if (name.equals("TeamSite/Metadata/NavCat2")) {
						
							metaNavCat2 = value;
						
						} else if (name.equals("TeamSite/Metadata/NavCat3")) {
						
							metaNavCat3 = value;
						
						} else if (name.equals("TeamSite/Metadata/prod_wwa_date")) {
						
							metaWWADate = value;
							
							TimeZone cet = TimeZone.getTimeZone("CET");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											 sdf.setTimeZone(cet);
							java.util.Date metaDate = sdf.parse(metaWWADate);
							
							TimeZone utc = TimeZone.getTimeZone("UTC");
							SimpleDateFormat sdfOut = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
											 sdfOut.setTimeZone(utc);
							
							String wwaDateXML = sdfOut.format(metaDate);
								
							metaWWADate = wwaDateXML.toString();
							
						} else if (name.equals("TeamSite/Metadata/prod_short_name")) {
						
							metaShortName = value;
							
						} else if (name.equals("TeamSite/Metadata/prod_formal_name")) {
						
							metaFormalName = value;
							
						} else if (name.equals("TeamSite/Metadata/isKit")) {
						
							metaIsKit = value;
						
						} else if (name.equals("TeamSite/Metadata/prod_id")) {
						
							metaProductId = value;
						
						} else if (name.equals("TeamSite/Metadata/sort_order")) {
						
							metaSortOrder = value;
						
						} else if (name.equals("TeamSite/Metadata/local_product")) {
						
							metaLocalProduct = value;
						
						} else if (name.equals("TeamSite/Metadata/local_short_name")) {
						
							metaLocalShortName = value;
						
						} else if (name.equals("TeamSite/Metadata/Discontinued")) {
						
							metaIsDiscontinued = value;
						
						} else if (name.equals("TeamSite/Metadata/new")) {
						
							metaIsNew = value;
						
						} else if (name.equals("TeamSite/Metadata/end_of_new")) {
						
							metaEndOfNew = value;
							
							TimeZone cet = TimeZone.getTimeZone("CET");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											 sdf.setTimeZone(cet);
							java.util.Date metaDate = sdf.parse(metaEndOfNew);
							
							TimeZone utc = TimeZone.getTimeZone("UTC");
							SimpleDateFormat sdfOut = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
											 sdfOut.setTimeZone(utc);
							
							String endOfNewDateXML = sdfOut.format(metaDate);
								
							metaEndOfNew = endOfNewDateXML.toString();			
						
						} else if (name.equals("TeamSite/Metadata/prod_active")) {
						
							metaActive = value;
						
						} else if (name.equals("TeamSite/Metadata/prod_local_opt_out")) {
							if ("0".equals(value)){
								metaOptout = "true";
							}else{
								metaOptout = "false";
							}
						} else if (name.equals("TeamSite/Metadata/available_date")) {
							
							metaAvailableDate = value;
															
						}
					}
			} 
			else {
				
				logger.info("ERROR Missing DCR: " + dcrFile.getVPath().toString());
			}
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (CSAuthorizationException e) {
			e.printStackTrace();
		} catch (CSExpiredSessionException e) {
			e.printStackTrace();
		} catch (CSRemoteException e) {
			e.printStackTrace();
		} catch (CSException e) {
			e.printStackTrace();;
		}
	  		
		//==================================================================
		// Process DCR Fields
		//==================================================================
	    
	    // Filter Tab
	    if ((lRootElement.selectSingleNode("filter_type") != null) && !(lRootElement.selectSingleNode("filter_type").getText().equals("")) && !("Discontinued".equalsIgnoreCase(metaProductCategory))){
	
			productFilterType = lRootElement.selectSingleNode("filter_type").getText();
			
			productCompare = lRootElement.selectSingleNode("filter_compare").getText();
			
			if (productFilterType.equalsIgnoreCase("slr")) {
				
				if (lRootElement.selectSingleNode("filter_dslr/type") != null) {

					productFilterDType = lRootElement.selectSingleNode("filter_dslr/type").getText();
					
				}
				
				if (lRootElement.selectSingleNode("filter_dslr/megapixels") != null) {

					productFilterDEffectivePixels = lRootElement.selectSingleNode("filter_dslr/megapixels").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dslr/image_sensor_format") != null) {

					productFilterDImageSensorFormat = lRootElement.selectSingleNode("filter_dslr/image_sensor_format").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dslr/maximum_iso") != null) {

					productFilterDMaximumISO = lRootElement.selectSingleNode("filter_dslr/maximum_iso").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dslr/features") != null) {

					productFilterDFeatures = lRootElement.selectSingleNode("filter_dslr/features").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dslr/price") != null) {

					productFilterDPrice = lRootElement.selectSingleNode("filter_dslr/price").getText();
				}
			
			} else if (productFilterType.equalsIgnoreCase("mirrorless")) {
				
				if (lRootElement.selectSingleNode("filter_mirrorless/megapixels") != null) {

					productFilterMLEffectivePixels = lRootElement.selectSingleNode("filter_mirrorless/megapixels").getText();
				}
				
				
				if (lRootElement.selectSingleNode("filter_mirrorless/maximum_iso") != null) {

					productFilterMLMaximumISO = lRootElement.selectSingleNode("filter_mirrorless/maximum_iso").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_mirrorless/features") != null) {

					productFilterMLFeatures = lRootElement.selectSingleNode("filter_mirrorless/features").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_mirrorless/price") != null) {

					productFilterMLPrice = lRootElement.selectSingleNode("filter_mirrorless/price").getText();
				}
				
			} else if (productFilterType.equalsIgnoreCase("dl")) {
				
				if (lRootElement.selectSingleNode("filter_dl/features") != null) {

					productFilterDlFeatures = lRootElement.selectSingleNode("filter_dl/features").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dl/hd_movie") != null) {

					productFilterDlMovie = lRootElement.selectSingleNode("filter_dl/hd_movie").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dl/megapixels") != null) {

					productFilterDlMegapixels = lRootElement.selectSingleNode("filter_dl/megapixels").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dl/zoom") != null) {

					productFilterDlZoom = lRootElement.selectSingleNode("filter_dl/zoom").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dl/monitor") != null) {

					productFilterDlMonitor = lRootElement.selectSingleNode("filter_dl/monitor").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_dl/price") != null) {

					productFilterDlPrice = lRootElement.selectSingleNode("filter_dl/price").getText();
				}
			
			} else if (productFilterType.equalsIgnoreCase("keymission")) {
				
				if (lRootElement.selectSingleNode("filter_keymission/animation") != null) {
					
					productFilterKmAnimation = lRootElement.selectSingleNode("filter_keymission/animation").getText();
					
				}
				
				if (lRootElement.selectSingleNode("filter_keymission/angle_of_view") != null) {
					
					productFilterKmAngleOfView = lRootElement.selectSingleNode("filter_keymission/angle_of_view").getText();
					
				}
				
				if (lRootElement.selectSingleNode("filter_keymission/connectivity") != null) {
					
					productFilterKmConnectivity = lRootElement.selectSingleNode("filter_keymission/connectivity").getText();
					
				}
				
				if (lRootElement.selectSingleNode("filter_keymission/weight") != null) {
					
					productFilterKmWeight = lRootElement.selectSingleNode("filter_keymission/weight").getText();
					
				}
				
			} else if (productFilterType.equalsIgnoreCase("coolpix")) {
				
				if (lRootElement.selectSingleNode("filter_coolpix/categories") != null) {

					productFilterCType = lRootElement.selectSingleNode("filter_coolpix/categories").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_coolpix/features") != null) {

					productFilterCFeatures = lRootElement.selectSingleNode("filter_coolpix/features").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_coolpix/hd_movie") != null) {

					productFilterCMovie = lRootElement.selectSingleNode("filter_coolpix/hd_movie").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_coolpix/megapixels") != null) {

					productFilterCMegapixels = lRootElement.selectSingleNode("filter_coolpix/megapixels").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_coolpix/zoom") != null) {

					productFilterCZoom = lRootElement.selectSingleNode("filter_coolpix/zoom").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_coolpix/monitor") != null) {

					productFilterCMonitor = lRootElement.selectSingleNode("filter_coolpix/monitor").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_coolpix/price") != null) {

					productFilterCPrice = lRootElement.selectSingleNode("filter_coolpix/price").getText();
				}
			
			} else if (productFilterType.equalsIgnoreCase("n1_acil")) {
				
				if (lRootElement.selectSingleNode("filter_n1acil/features") != null) {

					productFilterN1Features = lRootElement.selectSingleNode("filter_n1acil/features").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1acil/megapixels") != null) {

					productFilterN1Megapixels = lRootElement.selectSingleNode("filter_n1acil/megapixels").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1acil/fps") != null) {

					productFilterN1Fps = lRootElement.selectSingleNode("filter_n1acil/fps").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1acil/maximum_iso") != null) {

					productFilterN1MaximumISO = lRootElement.selectSingleNode("filter_n1acil/maximum_iso").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1acil/price") != null) {

					productFilterN1Price = lRootElement.selectSingleNode("filter_n1acil/price").getText();
				}
			
			} else if (productFilterType.equalsIgnoreCase("lens")) {
				
				if (lRootElement.selectSingleNode("filter_lens/type") != null) {

					productFilterLType = lRootElement.selectSingleNode("filter_lens/type").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_lens/categories") != null) {

					productFilterLCategories = lRootElement.selectSingleNode("filter_lens/categories").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_lens/minimum_focal_length") != null) {

					productFilterLFocalMin = lRootElement.selectSingleNode("filter_lens/minimum_focal_length").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_lens/maximum_focal_length") != null) {

					productFilterLFocalMax = lRootElement.selectSingleNode("filter_lens/maximum_focal_length").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_lens/maximum_aperture") != null) {

					productFilterLMaximumAperture = lRootElement.selectSingleNode("filter_lens/maximum_aperture").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_lens/lens_format") != null) {

					productFilterLFormat = lRootElement.selectSingleNode("filter_lens/lens_format").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_lens/technology") != null) {

					productFilterLTechnology = lRootElement.selectSingleNode("filter_lens/technology").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_lens/price") != null) {

					productFilterLPrice = lRootElement.selectSingleNode("filter_lens/price").getText();
				}
				
			} else if (productFilterType.equalsIgnoreCase("nz_lens")) {
				
				if (lRootElement.selectSingleNode("filter_nzlens/is_sline") != null) {

					productFilterZMSLine = lRootElement.selectSingleNode("filter_nzlens/is_sline").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/type") != null) {

					productFilterZMType = lRootElement.selectSingleNode("filter_nzlens/type").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/categories") != null) {

					productFilterZMCategories = lRootElement.selectSingleNode("filter_nzlens/categories").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/minimum_focal_length") != null) {

					productFilterZMFocalMin = lRootElement.selectSingleNode("filter_nzlens/minimum_focal_length").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/maximum_focal_length") != null) {

					productFilterZMFocalMax = lRootElement.selectSingleNode("filter_nzlens/maximum_focal_length").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/maximum_aperture") != null) {

					productFilterZMMaximumAperture = lRootElement.selectSingleNode("filter_nzlens/maximum_aperture").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/lens_format") != null) {

					productFilterZMFormat = lRootElement.selectSingleNode("filter_nzlens/lens_format").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/technology") != null) {

					productFilterZMTechnology = lRootElement.selectSingleNode("filter_nzlens/technology").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_nzlens/price") != null) {

					productFilterZMPrice = lRootElement.selectSingleNode("filter_nzlens/price").getText();
				}
				
			} else if (productFilterType.equalsIgnoreCase("n1_lens")) {
				
				if (lRootElement.selectSingleNode("filter_n1lens/type") != null) {

					productFilterN1LType = lRootElement.selectSingleNode("filter_n1lens/type").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1lens/categories") != null) {

					productFilterN1LCategories = lRootElement.selectSingleNode("filter_n1lens/categories").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1lens/minimum_focal_length") != null) {

					productFilterN1LFocalMin = lRootElement.selectSingleNode("filter_n1lens/minimum_focal_length").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1lens/maximum_focal_length") != null) {

					productFilterN1LFocalMax = lRootElement.selectSingleNode("filter_n1lens/maximum_focal_length").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1lens/maximum_aperture") != null) {

					productFilterN1LMaximumAperture = lRootElement.selectSingleNode("filter_n1lens/maximum_aperture").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1lens/technology") != null) {

					productFilterN1LTechnology = lRootElement.selectSingleNode("filter_n1lens/technology").getText();
				}
				
				if (lRootElement.selectSingleNode("filter_n1lens/price") != null) {

					productFilterN1LPrice = lRootElement.selectSingleNode("filter_n1lens/price").getText();
				}
			} 
		}
	    else {
			String prod_cat = metaProductCategory;
			String nav_cat1 = metaNavCat1;
			
			if (prod_cat != null){
				productFilterType = prod_cat.replaceAll("\\s","_");
				if (("Sport Optics".equalsIgnoreCase(prod_cat)||"Accessories".equalsIgnoreCase(prod_cat)) && nav_cat1 != null){
					nav_cat1 = nav_cat1.replaceAll("[\\s&]","_");
					nav_cat1 = nav_cat1.replaceAll("_+","_");
					productFilterType = productFilterType + "_" + nav_cat1;	
				}
				productFilterType = productFilterType.toUpperCase();
				if(lRootElement.selectSingleNode("filter_compare") != null) {productCompare = lRootElement.selectSingleNode("filter_compare").getText();} else { productCompare="false"; }
				logger.info("productFilterType: " + productFilterType);
				logger.info("productCompare: " + productCompare);
			}
		}
  
		// Category Image
		if (lRootElement.selectSingleNode("category_image") != null) {
	
			productCategoryImage = lRootElement.selectSingleNode("category_image").getText();
		}
		else if (lRootElement.selectSingleNode("summary_image") != null) {
	
			productCategoryImage = lRootElement.selectSingleNode("summary_image").getText();
		}
   
		// Category Description
		/*if (lRootElement.selectSingleNode("category_description") != null) {

			productCategoryDescription = lRootElement.selectSingleNode("category_description").getText();
		}
		else if (lRootElement.selectSingleNode("summary_description") != null) {
	
			productCategoryDescription = lRootElement.selectSingleNode("summary_description").getText();
		}*/
		   
		// Product Description
		if (lRootElement.selectSingleNode("overview_text") != null) {

			productOverviewDescription = lRootElement.selectSingleNode("overview_text").getText();
		}
		/*else if (lRootElement.selectSingleNode("description") != null) {
	
			productOverviewDescription = lRootElement.selectSingleNode("description").getText();
		}*/
		
		// Product Overview Image
		if (lRootElement.selectSingleNode("prod_overview_image") != null) {
				
			productOverviewImage = lRootElement.selectSingleNode("prod_overview_image").getText();
		}
		else if (lRootElement.selectSingleNode("overview_image") != null) {
	
			productOverviewImage = lRootElement.selectSingleNode("overview_image").getText();
		}
		
		// Product Key Features
		/*if (lRootElement.selectSingleNode("key_features") != null) {

			productKeyFeatures = lRootElement.selectSingleNode("key_features").getText();
		}*/
		
		// Product Brochure
		if (lRootElement.selectSingleNode("products_specs_link") != null) {
				
			productBrochure = lRootElement.selectSingleNode("products_specs_link").getText();
		}
		
		// Filter Image Source
		if ((lRootElement.selectSingleNode("filter_image_source") != null) && (lRootElement.selectSingleNode("filter_image_source").getText().equals("category_image"))){
			
			if(productCategoryImage != null){
				
				productOverviewImage = productCategoryImage;
			}
		}
		
		//==================================================================
		// Output SOLR XML Fields
		//==================================================================
		
		Element solrAddElement = solrXML.getRootElement().addElement("add");
		Element solrDocElement = solrAddElement.addElement("doc");
			    solrDocElement.addAttribute("boost", "1.0");
		
		//------------------------------------------------------------------
		// Product Id (If Does Not Exist... Do Not Output Anything
		//------------------------------------------------------------------
	    if (((metaProductId != null) && (!metaProductId.equals(""))) ||
	    		((metaLocale != null) && (!metaLocale.equals(""))))	
		{
			solrDocElement.addElement("field").addAttribute("name", "id").setText(metaProductId + "_" + metaLocale);
			solrDocElement.addElement("field").addAttribute("name", "pno_s").setText(metaProductId);
			solrDocElement.addElement("field").addAttribute("name", "pno_ss").setText(metaProductId);
			
			//----------------------------------------
	       	// Locale
	       	//----------------------------------------
			if ((metaLocale != null) && (!metaLocale.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "locale_s").setText(metaLocale);
				solrDocElement.addElement("field").addAttribute("name", "locale_ss").setText(metaLocale);	
			}
			
			//----------------------------------------
	       	// Product Category
	       	//----------------------------------------
			if ((metaProductCategory != null) && (!metaProductCategory.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "product_category_s").setText(metaProductCategory);
				solrDocElement.addElement("field").addAttribute("name", "product_category_ss").setText(metaProductCategory);	
			}
			
			//----------------------------------------
	       	// Product Category Image
	       	//----------------------------------------
			if ((productCategoryImage != null) && (!productCategoryImage.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "category_img_s").setText(productCategoryImage);
				solrDocElement.addElement("field").addAttribute("name", "category_img_ss").setText(productCategoryImage);	
			}
			
			//----------------------------------------
	       	// Product Category Description
	       	//----------------------------------------
			if ((productCategoryDescription != null) && (!productCategoryDescription.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "category_description_s").setText(productCategoryDescription);
				solrDocElement.addElement("field").addAttribute("name", "category_description_ss").setText(productCategoryDescription);	
			}
			
			//----------------------------------------
	       	// Product NavCat1
	       	//----------------------------------------
			if ((metaNavCat1 != null) && (!metaNavCat1.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "product_navcat1_s").setText(metaNavCat1);
				solrDocElement.addElement("field").addAttribute("name", "product_navcat1_ss").setText(metaNavCat1);	
			}
			
			//----------------------------------------
	       	// Product NavCat2
	       	//----------------------------------------
			if ((metaNavCat2 != null) && (!metaNavCat2.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "product_navcat2_s").setText(metaNavCat2);
				solrDocElement.addElement("field").addAttribute("name", "product_navcat2_ss").setText(metaNavCat2);	
			}
			
			//----------------------------------------
	       	// Product NavCat3
	       	//----------------------------------------
			if ((metaNavCat3 != null) && (!metaNavCat3.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "product_navcat3_s").setText(metaNavCat3);
				solrDocElement.addElement("field").addAttribute("name", "product_navcat3_ss").setText(metaNavCat3);	
			}
			
			//----------------------------------------
	       	// Product URL
	       	//----------------------------------------
			if ((metaProductId != null) && (!metaProductId.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "url_s").setText("/" + metaLocale + "/products/product_details.page?RunQuery=l3&ID=" + metaProductId);
				solrDocElement.addElement("field").addAttribute("name", "url_ss").setText("/" + metaLocale + "/products/product_details.page?RunQuery=l3&ID=" + metaProductId);
			}
			
			//----------------------------------------
	       	// WWA Date
	       	//----------------------------------------
			if ((metaWWADate != null) && (!metaWWADate.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "pub_d").setText(metaWWADate);	
			}
			
			//----------------------------------------
	       	// Formal Name
	       	//----------------------------------------
			if ((metaFormalName != null) && (!metaFormalName.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "formal_title_ut").setText(metaFormalName);
				solrDocElement.addElement("field").addAttribute("name", "formal_title_text_ss").setText(metaFormalName);
			}
			
			//----------------------------------------
	       	// Short Name
	       	//----------------------------------------
			if ((metaShortName != null) && (!metaShortName.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "title_ut").setText(metaShortName);
				solrDocElement.addElement("field").addAttribute("name", "title_text_ss").setText(metaShortName);	
			}
			
			//----------------------------------------
	       	// Product Description
	       	//----------------------------------------
			if ((productOverviewDescription != null) && (!productOverviewDescription.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "product_description_s").setText(productOverviewDescription);
				solrDocElement.addElement("field").addAttribute("name", "product_description_ss").setText(productOverviewDescription);
			}
			
			//----------------------------------------
	       	// Product Overview Image
	       	//----------------------------------------
			if ((productOverviewImage != null) && (!productOverviewImage.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "img_s").setText(productOverviewImage);
				solrDocElement.addElement("field").addAttribute("name", "img_ss").setText(productOverviewImage);
			}
			
			//----------------------------------------
	       	// Product Key Features
	       	//----------------------------------------
			if ((productKeyFeatures != null) && (!productKeyFeatures.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "product_key_features_s").setText(productKeyFeatures);
				solrDocElement.addElement("field").addAttribute("name", "product_key_features_ss").setText(productKeyFeatures);
			}
			
			//----------------------------------------
	       	// Product Brochure
	       	//----------------------------------------
			if ((productBrochure != null) && (!productBrochure.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "product_brochure_s").setText(productBrochure);
				solrDocElement.addElement("field").addAttribute("name", "product_brochure_ss").setText(productBrochure);
			}
			
			//----------------------------------------
	       	// Product Sort Order
	       	//----------------------------------------
			if ((metaSortOrder != null) && (!metaSortOrder.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "sort_order_i").setText(metaSortOrder);
				solrDocElement.addElement("field").addAttribute("name", "sort_order_si").setText(metaSortOrder);
			}
			
			//----------------------------------------
	       	// Product IsKit
	       	//----------------------------------------
			if ((metaIsKit != null) && (!metaIsKit.equals("")))
			{
				if (metaIsKit.equals("1")) {
					
					solrDocElement.addElement("field").addAttribute("name", "iskit_s").setText("true");
					solrDocElement.addElement("field").addAttribute("name", "iskit_ss").setText("true");
					
				} else {
					
					solrDocElement.addElement("field").addAttribute("name", "iskit_s").setText("false");
					solrDocElement.addElement("field").addAttribute("name", "iskit_ss").setText("false");
				}
			}
			
			//----------------------------------------
	       	// Product IsDiscontinued
	       	//----------------------------------------
			if ((metaIsDiscontinued != null) && (!metaIsDiscontinued.equals("")))
			{
				if (metaIsDiscontinued.equals("1")) {
					
					solrDocElement.addElement("field").addAttribute("name", "discontinued_s").setText("true");
					solrDocElement.addElement("field").addAttribute("name", "discontinued_ss").setText("true");
					
				} else {
					
					solrDocElement.addElement("field").addAttribute("name", "discontinued_s").setText("false");
					solrDocElement.addElement("field").addAttribute("name", "discontinued_ss").setText("false");
				}
			}
			
			//----------------------------------------
	       	// Product IsNew
	       	//----------------------------------------
			if ((metaIsNew != null) && (!metaIsNew.equals("")))
			{
				if (metaIsNew.equals("1")) {
					
					solrDocElement.addElement("field").addAttribute("name", "new_s").setText("true");
					solrDocElement.addElement("field").addAttribute("name", "new_ss").setText("true");
					
				} else {
					
					solrDocElement.addElement("field").addAttribute("name", "new_s").setText("false");
					solrDocElement.addElement("field").addAttribute("name", "new_ss").setText("false");
				}
			}
			
			//----------------------------------------
	       	// Product End of New
	       	//----------------------------------------
			if ((metaEndOfNew != null) && (!metaEndOfNew.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "new_until_d").setText(metaEndOfNew);	
			}
			
			//----------------------------------------
	       	// Product Active
	       	//----------------------------------------
			if ((metaActive != null) && (!metaActive.equals("")))
			{
				if (metaActive.equals("1")) {
					
					solrDocElement.addElement("field").addAttribute("name", "active_s").setText("true");
					solrDocElement.addElement("field").addAttribute("name", "active_ss").setText("true");
					
				} else {
					
					solrDocElement.addElement("field").addAttribute("name", "active_s").setText("false");
					solrDocElement.addElement("field").addAttribute("name", "active_ss").setText("false");
				}
			}
			
			//----------------------------------------
	       	// Product Optout
	       	//----------------------------------------
			if ((metaOptout != null) && (!metaOptout.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "locale_optout_s").setText(metaOptout);
				solrDocElement.addElement("field").addAttribute("name", "locale_optout_ss").setText(metaOptout);
			}
			
			//----------------------------------------
	       	// Product Local
	       	//----------------------------------------
			if ((metaLocalProduct != null) && (!metaLocalProduct.equals("")))
			{
				if (metaLocalProduct.equals("1")) {
					
					solrDocElement.addElement("field").addAttribute("name", "local_product_s").setText("true");
					solrDocElement.addElement("field").addAttribute("name", "local_product_ss").setText("true");
					
				} else {
					
					solrDocElement.addElement("field").addAttribute("name", "local_product_s").setText("false");
					solrDocElement.addElement("field").addAttribute("name", "local_product_ss").setText("false");
				}
			}
			
			//----------------------------------------
	       	// Product Local Name
	       	//----------------------------------------
			if ((metaLocalShortName != null) && (!metaLocalShortName.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "local_title_ut").setText(metaLocalShortName);
				solrDocElement.addElement("field").addAttribute("name", "local_title_text_ss").setText(metaLocalShortName);
			}
			
			//----------------------------------------
	       	// Product Available Date
	       	//----------------------------------------
			if ((metaAvailableDate != null) && (!metaAvailableDate.equals("")))
			{
				solrDocElement.addElement("field").addAttribute("name", "available_date_s").setText(metaAvailableDate);
				solrDocElement.addElement("field").addAttribute("name", "available_date_ss").setText(metaAvailableDate);
			}

			//----------------------------------------
	       	// Filter Type
	       	//----------------------------------------
	       	if ((productFilterType != null) && (!productFilterType.equals("")))
	       	{
	       		// Product Type
	       		solrDocElement.addElement("field").addAttribute("name", "ptc_s").setText(productFilterType.toUpperCase());
	       		solrDocElement.addElement("field").addAttribute("name", "ptc_ss").setText(productFilterType.toUpperCase());
	       		solrDocElement.addElement("field").addAttribute("name", "comp_b").setText(productCompare);
	       		
	       		// If DSLR Product...
	       		if (productFilterType.equalsIgnoreCase("dslr") || productFilterType.equalsIgnoreCase("slr")) {
	       			
	       			// Usage Type
	    		    if (productFilterDType != null && !productFilterDType.equals("")) {

	    		    	solrDocElement.addElement("field").addAttribute("name", "usage_s").setText(productFilterDType);
	    		    	solrDocElement.addElement("field").addAttribute("name", "usage_ss").setText(productFilterDType);
	    		    }
	       			
	       			// Effective Pixels
	       			if ((productFilterDEffectivePixels != null) && (!productFilterDEffectivePixels.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "effective_pixels_db").setText(productFilterDEffectivePixels);
	    	       		solrDocElement.addElement("field").addAttribute("name", "effective_pixels_sdb").setText(productFilterDEffectivePixels);
	       			}
	       			
	       			// Image Sensor
	       			if ((productFilterDImageSensorFormat != null) && (!productFilterDImageSensorFormat.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "image_sensor_s").setText(productFilterDImageSensorFormat);
	    	       		solrDocElement.addElement("field").addAttribute("name", "image_sensor_ss").setText(productFilterDImageSensorFormat);
	       			}
	       			
	       			// Maximum ISO
	       			if ((productFilterDMaximumISO != null) && (!productFilterDMaximumISO.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "iso_max_i").setText(productFilterDMaximumISO);
	    	       		solrDocElement.addElement("field").addAttribute("name", "iso_max_si").setText(productFilterDMaximumISO);
	       			}
	       			
	       			// Features
	       			if ((productFilterDFeatures != null) && (!productFilterDFeatures.equals(""))) 
	       			{
	       				
	       				String[] productFeatures = productFilterDFeatures.split(", ");
	       				
	       				for (int i = 0; i < productFeatures.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "features_m_s").setText(productFeatures[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "features_m_ss").setText(productFeatures[i]);
	       					
	       				}
	       			}
	       			
	       			// Price
	       			if ((productFilterDPrice != null) && (!productFilterDPrice.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterDPrice);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterDPrice);
	       			}
	       			
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());	       			
	       		}
	       		
	       		// If Mirrorless Product...
	       		else if (productFilterType.equalsIgnoreCase("mirrorless")) {
	       			
	       			// Effective Pixels
	       			if ((productFilterMLEffectivePixels != null) && (!productFilterMLEffectivePixels.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "effective_pixels_db").setText(productFilterMLEffectivePixels);
	    	       		solrDocElement.addElement("field").addAttribute("name", "effective_pixels_sdb").setText(productFilterMLEffectivePixels);
	       			}
	       			
	       			// Maximum ISO
	       			if ((productFilterMLMaximumISO != null) && (!productFilterMLMaximumISO.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "iso_max_i").setText(productFilterMLMaximumISO);
	    	       		solrDocElement.addElement("field").addAttribute("name", "iso_max_si").setText(productFilterMLMaximumISO);
	       			}
	       			
	       			// Features
	       			if ((productFilterMLFeatures != null) && (!productFilterMLFeatures.equals(""))) 
	       			{
	       				
	       				String[] productFeatures = productFilterMLFeatures.split(", ");
	       				
	       				for (int i = 0; i < productFeatures.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "features_m_s").setText(productFeatures[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "features_m_ss").setText(productFeatures[i]);
	       					
	       				}
	       			}
	       			
	       			// Price
	       			if ((productFilterMLPrice != null) && (!productFilterMLPrice.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterMLPrice);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterMLPrice);
	       			}
	       			
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());
	       			
	       		}
	       		
	       		// If DL Product...
	       		else if (productFilterType.equalsIgnoreCase("dl")) {
	       			
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());
	       			
	       			// Features
	       			if ((productFilterDlFeatures != null) && (!productFilterDlFeatures.equals(""))) 
	       			{
	       				
	       				String[] productFeatures = productFilterDlFeatures.split(", ");
	       				
	       				for (int i = 0; i < productFeatures.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "features_m_s").setText(productFeatures[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "features_m_ss").setText(productFeatures[i]);
	       					
	       				}
	       			}
	       			
	       			// Movie
	       			if ((productFilterDlMovie != null) && (!productFilterDlMovie.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "movie_mode_s").setText(productFilterDlMovie);
	    	       		solrDocElement.addElement("field").addAttribute("name", "movie_mode_ss").setText(productFilterDlMovie);
	       			}
	       			
	       			// Megapixels
	       			if ((productFilterDlMegapixels != null) && (!productFilterDlMegapixels.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "effective_pixels_db").setText(productFilterDlMegapixels);
	    	       		solrDocElement.addElement("field").addAttribute("name", "effective_pixels_sdb").setText(productFilterDlMegapixels);
	       			}
	       			
	       			// Zoom
	       			if ((productFilterDlZoom != null) && (!productFilterDlZoom.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "lens_zoom_db").setText(productFilterDlZoom);
	    	       		solrDocElement.addElement("field").addAttribute("name", "lens_zoom_sdb").setText(productFilterDlZoom);
	       			}
					
	       			// Monitor
	       			if ((productFilterDlMonitor != null) && (!productFilterDlMonitor.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "monitor_size_db").setText(productFilterDlMonitor);
	    	       		solrDocElement.addElement("field").addAttribute("name", "monitor_size_sdb").setText(productFilterDlMonitor);
	       			}
	       			
	       			// Price
	       			if ((productFilterDlPrice != null) && (!productFilterDlPrice.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterDlPrice);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterDlPrice);
	       			}
	       			
	       		}
	       		
	       		// If KeyMission Product...
	       		else if (productFilterType.equalsIgnoreCase("keymission")) {
	       			
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());
	       			
   				  	// Animation
	       			if ((productFilterKmAnimation != null) && (!productFilterKmAnimation.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "animation_s").setText(productFilterKmAnimation);
	    	       		solrDocElement.addElement("field").addAttribute("name", "animation_ss").setText(productFilterKmAnimation);
	       			}
	       			
	       			// Angle of view
	       			if ((productFilterKmAngleOfView != null) && (!productFilterKmAngleOfView.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "angle_of_view_s").setText(productFilterKmAngleOfView);
	    	       		solrDocElement.addElement("field").addAttribute("name", "angle_of_view_ss").setText(productFilterKmAngleOfView);
	       			}
   				  	
	       			// Connectivity
	       			if ((productFilterKmConnectivity != null) && (!productFilterKmConnectivity.equals(""))) 
	       			{
	       				
	       				String[] productFilterConnectivity = productFilterKmConnectivity.split(", ");
	       				
	       				for (int i = 0; i < productFilterConnectivity.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "connectivity_m_s").setText(productFilterConnectivity[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "connectivity_m_ss").setText(productFilterConnectivity[i]);
	       					
	       				}
	       			}
	       			
	       			// Weight
	       			if ((productFilterKmWeight != null) && (!productFilterKmWeight.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "weight_s").setText(productFilterKmWeight);
	    	       		solrDocElement.addElement("field").addAttribute("name", "weight_ss").setText(productFilterKmWeight);
	       			}
	       			
	       		}
	       		
	       		// If COOLPIX Product...
	       		else if (productFilterType.equalsIgnoreCase("coolpix")) {
	       			
	       			// Usage Type
	       			if ((productFilterCType != null) && (!productFilterCType .equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "usage_s").setText(productFilterCType);
	    	       		solrDocElement.addElement("field").addAttribute("name", "usage_ss").setText(productFilterCType);
	       			}
	       			
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			// Else Use en_Asia DCR...
	       			/*else {
	       			
	       				logger.info("Using en_Asia DCR Colours");
	       				viewsNodes = mRootElement.selectNodes("Colourways");
	       			}*/
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());
	       			
	       			// Features
	       			if ((productFilterCFeatures != null) && (!productFilterCFeatures.equals(""))) 
	       			{
	       				
	       				String[] productFeatures = productFilterCFeatures.split(", ");
	       				
	       				for (int i = 0; i < productFeatures.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "features_m_s").setText(productFeatures[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "features_m_ss").setText(productFeatures[i]);
	       					
	       				}
	       			}
	       			
	       			// Movie
	       			if ((productFilterCMovie != null) && (!productFilterCMovie.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "movie_mode_s").setText(productFilterCMovie);
	    	       		solrDocElement.addElement("field").addAttribute("name", "movie_mode_ss").setText(productFilterCMovie);
	       			}
	       			
	       			// Megapixels
	       			if ((productFilterCMegapixels != null) && (!productFilterCMegapixels.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "effective_pixels_db").setText(productFilterCMegapixels);
	    	       		solrDocElement.addElement("field").addAttribute("name", "effective_pixels_sdb").setText(productFilterCMegapixels);
	       			}
	       			
	       			// Zoom
	       			if ((productFilterCZoom != null) && (!productFilterCZoom.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "lens_zoom_db").setText(productFilterCZoom);
	    	       		solrDocElement.addElement("field").addAttribute("name", "lens_zoom_sdb").setText(productFilterCZoom);
	       			}
					
	       			// Monitor
	       			if ((productFilterCMonitor != null) && (!productFilterCMonitor.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "monitor_size_db").setText(productFilterCMonitor);
	    	       		solrDocElement.addElement("field").addAttribute("name", "monitor_size_sdb").setText(productFilterCMonitor);
	       			}
	       			
	       			// Price
	       			if ((productFilterCPrice != null) && (!productFilterCPrice.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterCPrice);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterCPrice);
	       			}
	       			
	       		}
	       		// If Nikon 1 ACIL Product...
	       		else if (productFilterType.equalsIgnoreCase("n1_acil")) {
	       			
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			// Else Use en_Asia DCR...
	       			/*else {
	       			
	       				logger.info("Using en_Asia DCR Colours");
	       				viewsNodes = mRootElement.selectNodes("Colourways");
	       			}*/
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());
	       			
	       			// Features
	       			if ((productFilterN1Features != null) && (!productFilterN1Features.equals(""))) 
	       			{
	       				
	       				String[] productFeatures = productFilterN1Features.split(", ");
	       				
	       				for (int i = 0; i < productFeatures.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "features_m_s").setText(productFeatures[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "features_m_ss").setText(productFeatures[i]);
	       					
	       				}
	       			}
	       			
	       			// Megapixels
	       			if ((productFilterN1Megapixels != null) && (!productFilterN1Megapixels.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "effective_pixels_db").setText(productFilterN1Megapixels);
	    	       		solrDocElement.addElement("field").addAttribute("name", "effective_pixels_sdb").setText(productFilterN1Megapixels);
	       			}
	       			
	       			// FPS
	       			if ((productFilterN1Fps != null) && (!productFilterN1Fps.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "fps_i").setText(productFilterN1Fps);
	    	       		solrDocElement.addElement("field").addAttribute("name", "fps_si").setText(productFilterN1Fps);
	       			}
	       			
	       			// Maximum ISO
	       			if ((productFilterN1MaximumISO != null) && (!productFilterN1MaximumISO.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "iso_max_i").setText(productFilterN1MaximumISO);
	    	       		solrDocElement.addElement("field").addAttribute("name", "iso_max_si").setText(productFilterN1MaximumISO);
	       			}
	       			
	       			// Price
	       			if ((productFilterN1Price != null) && (!productFilterN1Price.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterN1Price);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterN1Price);
	       			}
	       			
	       		}
	       		// If Lens Product...
	       		else if (productFilterType.equalsIgnoreCase("lens")) {
	       			
	       			// Usage Type
	       			if ((productFilterLType != null) && (!productFilterLType.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "usage_s").setText(productFilterLType);
	    	       		solrDocElement.addElement("field").addAttribute("name", "usage_ss").setText(productFilterLType);
	       			}
	       			
	       			// Categories
	       			if ((productFilterLCategories != null) && (!productFilterLCategories.equals(""))) 
	       			{
	       				
	       				String[] productCategories = productFilterLCategories.split(", ");
	       				
	       				for (int i = 0; i < productCategories.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "categories_m_s").setText(productCategories[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "categories_m_ss").setText(productCategories[i]);
	       					
	       				}
	       			}
	       			
	       			// Focal Minimum
	       			if ((productFilterLFocalMin != null) && (!productFilterLFocalMin.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_db").setText(productFilterLFocalMin);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_sdb").setText(productFilterLFocalMin);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_sort_sdb").setText(productFilterLFocalMin);
	       			}
	       			
	       			// Focal Maximum
	       			if ((productFilterLFocalMax != null) && (!productFilterLFocalMax.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_db").setText(productFilterLFocalMax);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_sdb").setText(productFilterLFocalMax);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_sort_sdb").setText(productFilterLFocalMax);
	       			}
	       			
	       			// Maximum Aperture
	       			if ((productFilterLMaximumAperture != null) && (!productFilterLMaximumAperture.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "aperture_max_db").setText(productFilterLMaximumAperture);
	    	       		solrDocElement.addElement("field").addAttribute("name", "aperture_max_sdb").setText(productFilterLMaximumAperture);
	       			}
					
	       			// Lens Format
	       			if ((productFilterLFormat != null) && (!productFilterLFormat.equals(""))) 
	       			{	
	       				solrDocElement.addElement("field").addAttribute("name", "lens_format_s").setText(productFilterLFormat);
	    	       		solrDocElement.addElement("field").addAttribute("name", "lens_format_ss").setText(productFilterLFormat);
	       			}
	       			
	       			// Technology
	       			if ((productFilterLTechnology != null) && (!productFilterLTechnology.equals("")))
	       			{
	       				
	       				String[] productTechnologies = productFilterLTechnology.split(", ");
	       				
	       				for (int i = 0; i < productTechnologies.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "lens_technology_m_s").setText(productTechnologies[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "lens_technology_m_ss").setText(productTechnologies[i]);
	       					
	       				}
	       			}
	       			
	       			// Price
	       			if ((productFilterLPrice != null) && (!productFilterLPrice.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterLPrice);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterLPrice);
	       			}
	       		
	       		} 
	       		// If Z-Mount Lens Product...
	       		else if (productFilterType.equalsIgnoreCase("nz_lens")) {
	       			
	       			// S-Line
	       			if ((productFilterZMSLine != null) && (!productFilterZMSLine.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "isSLine_s").setText(productFilterZMSLine);
	    	       		solrDocElement.addElement("field").addAttribute("name", "isSLine_ss").setText(productFilterZMSLine);
	       			}
	       			
	       			// Usage Type
	       			if ((productFilterZMType != null) && (!productFilterZMType.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "usage_s").setText(productFilterZMType);
	    	       		solrDocElement.addElement("field").addAttribute("name", "usage_ss").setText(productFilterZMType);
	       			}
	       			
	       			// Categories
	       			if ((productFilterZMCategories != null) && (!productFilterZMCategories.equals(""))) 
	       			{
	       				
	       				String[] productCategories = productFilterZMCategories.split(", ");
	       				
	       				for (int i = 0; i < productCategories.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "categories_m_s").setText(productCategories[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "categories_m_ss").setText(productCategories[i]);
	       					
	       				}
	       			}
	       			
	       			// Focal Minimum
	       			if ((productFilterZMFocalMin != null) && (!productFilterZMFocalMin.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_db").setText(productFilterZMFocalMin);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_sdb").setText(productFilterZMFocalMin);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_sort_sdb").setText(productFilterZMFocalMin);
	       			}
	       			
	       			// Focal Maximum
	       			if ((productFilterZMFocalMax != null) && (!productFilterZMFocalMax.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_db").setText(productFilterZMFocalMax);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_sdb").setText(productFilterZMFocalMax);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_sort_sdb").setText(productFilterZMFocalMax);
	       			}
	       			
	       			// Maximum Aperture
	       			if ((productFilterZMMaximumAperture != null) && (!productFilterZMMaximumAperture.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "aperture_max_db").setText(productFilterZMMaximumAperture);
	    	       		solrDocElement.addElement("field").addAttribute("name", "aperture_max_sdb").setText(productFilterZMMaximumAperture);
	       			}
					
	       			// Lens Format
	       			if ((productFilterZMFormat != null) && (!productFilterZMFormat.equals(""))) 
	       			{	
	       				solrDocElement.addElement("field").addAttribute("name", "lens_format_s").setText(productFilterZMFormat);
	    	       		solrDocElement.addElement("field").addAttribute("name", "lens_format_ss").setText(productFilterZMFormat);
	       			}
	       			
	       			// Technology
	       			if ((productFilterZMTechnology != null) && (!productFilterZMTechnology.equals("")))
	       			{
	       				
	       				String[] productTechnologies = productFilterZMTechnology.split(", ");
	       				
	       				for (int i = 0; i < productTechnologies.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "lens_technology_m_s").setText(productTechnologies[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "lens_technology_m_ss").setText(productTechnologies[i]);
	       					
	       				}
	       			}
	       			
	       			// Price
	       			if ((productFilterZMPrice != null) && (!productFilterZMPrice.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterZMPrice);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterZMPrice);
	       			}
	       			
	       		}
	       		
	       		// If 1 Nikkor Lens Product...
	       		else if (productFilterType.equalsIgnoreCase("n1_lens")) {
	       			
	       			// Lens Type
	       			if ((productFilterN1LType != null) && (!productFilterN1LType.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "type_s").setText(productFilterN1LType);
	    	       		solrDocElement.addElement("field").addAttribute("name", "type_ss").setText(productFilterN1LType);
	       			}
	       			
	       			// Lens Categories
	       			if ((productFilterN1LCategories != null) && (!productFilterN1LCategories.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "category_s").setText(productFilterN1LCategories);
	    	       		solrDocElement.addElement("field").addAttribute("name", "category_ss").setText(productFilterN1LCategories);
	       			}
	       			
	       			// Focal Minimum
	       			if ((productFilterN1LFocalMin != null) && (!productFilterN1LFocalMin.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_db").setText(productFilterN1LFocalMin);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_sdb").setText(productFilterN1LFocalMin);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_minimum_sort_sdb").setText(productFilterN1LFocalMin);
	       			}
	       			
	       			// Focal Maximum
	       			if ((productFilterN1LFocalMax != null) && (!productFilterN1LFocalMax.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_db").setText(productFilterN1LFocalMax);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_sdb").setText(productFilterN1LFocalMax);
	    	       		solrDocElement.addElement("field").addAttribute("name", "focal_length_maximum_sort_sdb").setText(productFilterN1LFocalMax);
	       			}
	       			
	       			// Maximum Aperture
	       			if ((productFilterN1LMaximumAperture != null) && (!productFilterN1LMaximumAperture.equals("")))
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "aperture_max_db").setText(productFilterN1LMaximumAperture);
	    	       		solrDocElement.addElement("field").addAttribute("name", "aperture_max_sdb").setText(productFilterN1LMaximumAperture);
	       			}
					
	       			// Technology
	       			if ((productFilterN1LTechnology != null) && (!productFilterN1LTechnology.equals("")))
	       			{
	       				
	       				String[] productTechnologies = productFilterN1LTechnology.split(", ");
	       				
	       				for (int i = 0; i < productTechnologies.length; i++) {
	       					
	       					solrDocElement.addElement("field").addAttribute("name", "lens_technology_m_s").setText(productTechnologies[i]);
		    	       		solrDocElement.addElement("field").addAttribute("name", "lens_technology_m_ss").setText(productTechnologies[i]);
	       					
	       				}
	       			}
	       			
	       			// Price
	       			if ((productFilterN1LPrice != null) && (!productFilterN1LPrice.equals(""))) 
	       			{
	       				
	       				solrDocElement.addElement("field").addAttribute("name", "price_db").setText(productFilterN1LPrice);
	    	       		solrDocElement.addElement("field").addAttribute("name", "price_sdb").setText(productFilterN1LPrice);
	       			}
	       			
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			// Else Use en_Asia DCR...
	       			/*else {
	       			
	       				logger.info("Using en_Asia DCR Colours");
	       				viewsNodes = mRootElement.selectNodes("Colourways");
	       			}*/
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());
	       			
	       		}
	       		
	       		else {
	       			// Variants
	       			List <Node> viewsNodes = null;
	       			
	       			ObjectMapper mapper = new ObjectMapper();
   				  	ObjectNode rootObj = mapper.createObjectNode();
   				  	
   				  	// If Locale DCR Has Colors...Use It...
	       			if ((lRootElement.selectSingleNode("Colourways") != null) && (!lRootElement.selectSingleNode("Colourways/title").getText().equals(""))) {
	  				   
	       				logger.info("Using Locale DCR Colours");
	       				viewsNodes = lRootElement.selectNodes("Colourways");
	       			
	       			}
	       			
	       			if (viewsNodes != null) {
	       				
	       				int count = 0;
	   				  	
	   				  	for ( Node n : viewsNodes )
	   				  	{
	   				  		if (n.selectSingleNode("title") != null && !n.selectSingleNode("title").getText().equals("")
	   						    && n.selectSingleNode("swatch_image") != null && !n.selectSingleNode("swatch_image").getText().equals("")
	   						    && n.selectSingleNode("image") != null && !n.selectSingleNode("image").getText().equals("")) {
	   				  			
	   				  			ObjectNode idObject = rootObj.putObject(metaProductId + "_" + count);
	   				  		
	   				  			solrDocElement.addElement("field").addAttribute("name", "pnos_m_et").setText(metaProductId + "_" + count);
	   				  		
	   				  			ObjectNode colorObject = idObject.putObject("color");
	   				  			
	   				  			colorObject.put("parent_id", metaProductId + "_" + metaLocale);
	   				  			colorObject.put("name", n.selectSingleNode("title").getText());
	   				  			
	   				  			logger.info("Adding Colour: " + n.selectSingleNode("title").getText());
	   				  			
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_s").setText(n.selectSingleNode("title").getText());
	   				  			solrDocElement.addElement("field").addAttribute("name", "color_m_ss").setText(n.selectSingleNode("title").getText());
		    	       		
	   				  			colorObject.put("swatch", n.selectSingleNode("swatch_image").getText());
	   				  			colorObject.put("image", n.selectSingleNode("image").getText());
	   				  			
	   				  			count++;
	   				  		}
	   				  	}
	       			}
	       		
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_s").setText(rootObj.toString());
   				  	solrDocElement.addElement("field").addAttribute("name", "variants_ss").setText(rootObj.toString());
	       			
	       		}
	       	}
			
		}
		
		return solrXML;
	}
}
