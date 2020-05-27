package com.interwoven.teamsite.nikon.externals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.jcs.JCS;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.springframework.context.ApplicationContext;

import com.interwoven.livesite.common.codec.URLUTF8Codec;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.ExternalUtils;
import com.interwoven.livesite.external.ParameterHash;
import com.interwoven.livesite.file.FileDALIfc;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.javax.VisitorAdapter;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.components.ComponentHelper;
import com.interwoven.teamsite.nikon.dto.CommonDTOFields;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.hibernate.manager.NikonHBN8DAOManager;
import com.interwoven.teamsite.nikon.repository.NikonRepository;
import com.interwoven.teamsite.nikon.springx.NikonBusinessManager;
import com.interwoven.teamsite.nikon.util.NikonUtils;

/**
 * This class has the responsibility to couple the Livesite component to the
 * Hibernate DAO manager. The reason for this class is to deal with conversion
 * of parameters to the manager class from the external call and the conversion
 * to XML back to the component for consumption.
 * 
 * The idea is for each <bold>USED</bold> service method on the manager we
 * should have a corresponding method here
 * 
 * i.e.
 * 
 * If the manager has the method Collection<ProductDTO>
 * listProductsByCategory(HBN8QueryParamDTO params)
 * 
 * Then this class should wrap the call to do the necessary conversions in
 * Document listProductsInCategoryByLanguageCountry(RequestContext
 * requestContext)
 * 
 * @author nbamford
 * 
 */
public class NikonLiveSiteHBN8ExternalDelegate extends NikonLiveSiteBaseDelegate {
	/**
	 * This visitor class is used to added URL Encoded attribute values to the
	 * Product Explorer DCR
	 * 
	 * @author nbamford
	 * 
	 */
	private class EncodedAttributeVisitor extends VisitorAdapter {

		@Override
		public void visit(final Element arg0) {

			// Matches on Elements with the following Regex Pattern
			final String patternStr = "Subnav[0-9]*|CatalogueSection";
			final Pattern pattern = Pattern.compile(patternStr);
			final Matcher matcher = pattern.matcher(arg0.getName());

			if ("ParamValue".equals(arg0.getName())) {
				arg0.setText(Utils.URLEncode(arg0.getText()));
			} else if (arg0.getName().endsWith("Param")) {
				arg0.setText(Utils.URLEncode(arg0.getText()));
			} else if (matcher.matches()) {
				matcher.reset();
				arg0.addAttribute("Enc", Utils.URLEncode(arg0.getText()));
			}
		}
	}

	protected NikonRepository repository;

	public NikonLiveSiteHBN8ExternalDelegate() {
	}

	public NikonLiveSiteHBN8ExternalDelegate(final NikonBusinessManager dm, final TeamsiteEnvironment env,
			final NikonRepository repository) {
		super();
		this.dm = dm;
		environment = env;
		this.repository = repository;
	}

	public NikonLiveSiteHBN8ExternalDelegate(final NikonHBN8DAOManager dm, final TeamsiteEnvironment env) {
		super();
		this.dm = dm;
		environment = env;
	}

	// Method to create the org.dom4j.Document from a product. Not that if we
	// pass a null RequestContext then
	// we don't attempt to append the content
	private Document _listProductDetailsDoc(final RequestContext requestContext, final ProductDTO prod,
			final HBN8QueryParamDTO param) {
		log.debug("Entering private Document _listProductDetailsDoc(RequestContext requestContext, ProductDTO prod)");
		final Document doc = Dom4jUtils.newDocument();
		doc.addElement("staticcontent");

		// Main Product DCR
		if (prod != null && !"".equals(prod.getPath())) {
			final String nameId = FormatUtils.mFormat("prod{0}", 1);
			this.createProductXML(doc.getRootElement(), "Product", nameId, nameId, prod, true);
			if (param != null) {
				doc.getRootElement().appendContent(
						Utils.fileToXML(new File(param.getSourcePath() + "/" + prod.getPath())));
			} else if (requestContext != null) {
				doc.getRootElement().appendContent(
						this.loadDCRForDTOInLocale(requestContext, prod.getPath(), prod.getNikonLocale()));
			}
			doc.getRootElement().addElement("productLocalName")
					.setText(prod.getLocalShortName() == null ? "" : prod.getLocalShortName());

		}

		// Related Products
		if (prod != null) {
			int idCnt = 0;
			for (final ProductDTO relProd : prod.getRelatedProducts()) {
				final String nameId = FormatUtils.mFormat("relProd{0}", ++idCnt);
				final Element e = this.createProductXML(doc.getRootElement(), "RelatedProduct", nameId, nameId,
						relProd, true);
				if (param != null) {
					e.appendContent(Utils.fileToXML(new File(param.getSourcePath() + "/" + relProd.getPath())));
				} else if (requestContext != null) {
					e.appendContent(this.loadDCRForDTOInLocale(requestContext, relProd.getPath(),
							relProd.getNikonLocale()));
				}
			}
			// BOM Products
			idCnt = 0;
			try {
				for (final ProductDTO bomProd : prod.getBillOfMaterials()) {
					final String nameId = FormatUtils.mFormat("bomProd{0}", ++idCnt);
					final Element e = this.createProductXML(doc.getRootElement(), "BomProduct", nameId, nameId,
							bomProd, true);
					if (param != null) {
						e.appendContent(Utils.fileToXML(new File(param.getSourcePath() + "/" + bomProd.getPath())));
					} else if (requestContext != null) {
						e.appendContent(this.loadDCRForDTOInLocale(requestContext, bomProd.getPath(),
								bomProd.getNikonLocale()));
					}
				}
			} catch (final Throwable t) {
				log.error("Throwable", t);
			}

			// Accessories
			idCnt = 0;
			for (final ProductDTO accProd : prod.getAccessories()) {
				final String nameId = FormatUtils.mFormat("accProd{0}", ++idCnt);
				final Element e = this.createProductXML(doc.getRootElement(), "AccessoryProduct", nameId, nameId,
						accProd, true);
				if (param != null) {
					e.appendContent(Utils.fileToXML(new File(param.getSourcePath() + "/" + accProd.getPath())));
				} else if (requestContext != null) {
					e.appendContent(this.loadDCRForDTOInLocale(requestContext, accProd.getPath(),
							accProd.getNikonLocale()));
				}
			}

			// Product Marketing Related
			idCnt = 0;
			for (final ProductDTO productMarketingRelated : prod.getProductMarketingRelated()) {
				final String nameId = FormatUtils.mFormat("productMarketingRelated{0}", ++idCnt);
				final Element e = this.createProductXML(doc.getRootElement(), "ProductMarketingRelated", nameId,
						nameId, productMarketingRelated, true);
				if (param != null) {
					e.appendContent(Utils.fileToXML(new File(param.getSourcePath() + "/"
							+ productMarketingRelated.getPath())));
				} else if (requestContext != null) {
					e.appendContent(this.loadDCRForDTOInLocale(requestContext, productMarketingRelated.getPath(),
							productMarketingRelated.getNikonLocale()));
				}
			}

			// If the product is an accessory then we need to get the
			// products it is related to
			// TODO Put in the DTO and create the neccessary
			// 2020-05-27: Not limited to Accessory type only. For Product type, it's used for Related Products relationship.
//			if (prod.isAccessory()) {
				idCnt = 0;
				for (final ProductDTO accOfProd : prod.getAccessoryOf()) {
					final String nameId = FormatUtils.mFormat("accOfProd{0}", ++idCnt);
					final Element e = this.createProductXML(doc.getRootElement(), "AccessoryOfProduct", nameId, nameId,
							accOfProd, true);
					if (param != null) {
						e.appendContent(Utils.fileToXML(new File(param.getSourcePath() + "/" + accOfProd.getPath())));

					} else if (requestContext != null) {
						e.appendContent(this.loadDCRForDTOInLocale(requestContext, accOfProd.getPath(),
								accOfProd.getNikonLocale()));
					}
				}
//			}
		}
		log.debug("Exiting private Document _listProductDetailsDoc(RequestContext requestContext, ProductDTO prod)");
		return doc;
	}

	private Element createProductXML(final Element parentElement, final String rootElementName, final String name,
			final String id, final ProductDTO prod) {
		return this.createProductXML(parentElement, rootElementName, name, id, prod, false);
	}

	private Element createProductXML(final Element parentElement, final String rootElementName, final String name,
			final String id, final ProductDTO prod, final boolean addPrice) {
		final Element e = parentElement.addElement(rootElementName);
		e.addAttribute(CommonDTOFields.name, name);
		e.addAttribute(CommonDTOFields.id, id);
		e.addAttribute(ProductDTO.index, prod.getProdId());
		e.addAttribute(CommonDTOFields.nikonLocale, prod.getNikonLocale());
		e.addAttribute(ProductDTO.index, prod.getProdId());
		e.addAttribute(CommonDTOFields.path, prod.getPath());
		e.addAttribute(CommonDTOFields.environment, prod.getEnvironment());
		e.addAttribute(CommonDTOFields.isProduction, FormatUtils.boolean2String(prod.isProduction()));
		e.addAttribute(ProductDTO.localShortName, FormatUtils.nvl(prod.getLocalShortName(), ""));
		e.addAttribute(ProductDTO.prodShortName, FormatUtils.nvl(prod.getProdShortName(), ""));
		e.addAttribute(ProductDTO.isNew, FormatUtils.boolean2String(prod.isStillNew()));
		e.addAttribute(ProductDTO.isKit, FormatUtils.boolean2String(prod.isKit()));
		e.addAttribute(ProductDTO.type, prod.getType());
		e.addAttribute(ProductDTO.productCategory, FormatUtils.nvl(prod.getProductCategory(), ""));
		e.addAttribute(ProductDTO.navCat1, FormatUtils.nvl(prod.getNavCat1(), ""));
		e.addAttribute(ProductDTO.navCat2, FormatUtils.nvl(prod.getNavCat2(), ""));
		e.addAttribute(ProductDTO.navCat3, FormatUtils.nvl(prod.getNavCat3(), ""));
		e.addAttribute(CommonDTOFields.wwaDate, FormatUtils.formatDateTime(prod.getWwaDate()));

		return e;
	}

	/**
	 * Method to inject default_content DCR to product_detail according to the
	 * product category and locale
	 * 
	 * @param requestContext
	 *            , doc
	 * @return XMLDocument
	 */
	public Document defaultContentInjection(final RequestContext requestContext, final Document doc) {
		log.info("run default_content_injection...");
		final String strLocale = LocaleResolver.getRequestedLanguageCountryCode(context);
		log.info("locale of current context is " + strLocale);
		final Element root = doc.getRootElement();
		final Element prod_node = (Element) root.selectSingleNode("//staticcontent/Product");
		if (prod_node != null) {
			// log.info(doc.asXML());
			try {
				// check category and navigation category lv1 of this product,
				// and get default content DCR path
				String injectDCR = null;
				if (prod_node.attribute("productCategory") != null
						&& !"".equals(prod_node.attribute("productCategory").getValue())) {
					log.info("productCategory = " + prod_node.attribute("productCategory").getValue());
					if ("Digital Cameras".equals(prod_node.attribute("productCategory").getValue())) {
						if (prod_node.attribute("navCat1") != null) {
							log.info("navCat1 = " + prod_node.attribute("navCat1").getValue());
							injectDCR = "templatedata/" + strLocale + "/default_content/data/category_"
									+ prod_node.attribute("navCat1").getValue().toLowerCase().replaceAll("\\s", "");
						}
					} else {
						injectDCR = "templatedata/" + strLocale + "/default_content/data/category_"
								+ prod_node.attribute("productCategory").getValue().toLowerCase().replaceAll("\\s", "");
					}
				}
				if (injectDCR != null) {
					log.info("injectDCR = " + injectDCR);
					// add an additional parameter (injectDCR) to requestContext
					// in order to reuse getLocalisedDCR
					final ParameterHash param = requestContext.getParameters();
					param.put("injectDCR", injectDCR);
					requestContext.setParameters(param);
					// log.info(context.getParameterString("injectDCR"));
					final ComponentHelper ch = new ComponentHelper();
					final Document default_content_doc = ch.getLocalisedDCR(requestContext, "injectDCR",
							"default_content", null);

					// log.info(default_content_doc.asXML());
					root.add(default_content_doc.getRootElement());
					// log.debug(doc.asXML());
				}
			} catch (final Exception e) {
				log.error("Default content injection error!!");
			}
		}
		return doc;
	}

	private FastHashMap getECommerceMap(final NikonRepository repo, final String locale) throws Exception {
		JCS commerceJCS = null;
		if (repo.isCommerceCacheEnable()) {
			commerceJCS = JCS.getInstance(NikonDomainConstants.JCS_REGION_COMMERCE_MAP);
		}
		FastHashMap commerceMap = null;
		if (commerceJCS != null) {
			commerceMap = (FastHashMap) commerceJCS.get(locale);
		}
		if (commerceMap == null) {
			log.debug("Attempt to load the commerceMap...");
			final InputStream isEComm = repo.retrieveContent("product", "ecommercelink.properties", locale);
			if (isEComm != null) {
				commerceMap = new FastHashMap();
				try {
					final List<String> lines = IOUtils.readLines(isEComm, "UTF-8");
					for (final String line : lines) {
						if (!line.startsWith("#")) {
							final String[] map = line.split("=");
							if (map.length >= 2) {
								String url = "";
								final String id = map[0];
								for (int i = 1; i < map.length; i++) {
									if ("".equals(url)) {
										url = map[i];
									} else {
										url = url + "=" + map[i];
									}
								}
								commerceMap.put(id, url);
							}
						}
					}
				} finally {
					isEComm.close();
				}
				// commerceMap.load(isEComm);
				if (repo.isCommerceCacheEnable()) {
					commerceJCS.put(locale, commerceMap);
				}
			} else {
				log.debug("No ECommercelink file available for locale [" + locale + "], will ignore the ECommerce link");
			}
		}
		return commerceMap;
	}

	NikonRepository getNikonRepository() {
		if (repository == null) {

			final ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
			final Object o = appCtx.getBean("nikon.Repository");

			repository = (NikonRepository) o;
		}

		return repository;
	}

	private void insertChilds(final Element rootElement, final String prodIds, final String elementName,
			final String idName, final NikonRepository repo, final Collection<String> locales,
			final HashMap eCommerceMap) throws IOException {
		if (prodIds != null && !"".equals(prodIds)) {
			int i = 1;
			for (final String id : prodIds.split(",")) {
				if (id != null && !"".equals(id)) {
					int count = 0;
					log.debug("Looping available locale to try to find the fragment for [" + id + "]");
					for (final String locale : locales) {
						log.debug("Looking into locale [" + locale + "] for fragment [" + id + "]");
						InputStream is = null;
						try {
							is = repo.retrieveContent("product", id + "_fragment.xml", locale);
							if (is != null) {
								final Document document = Dom4jUtils.newDocument(is);
								if (document.selectSingleNode("//localeOptout") == null) {
									document.getRootElement().setName(elementName);
									document.getRootElement().attribute("id").setValue(idName + i);
									document.getRootElement().attribute("name").setValue(idName + i);
									i++;
									log.debug("Found fragment [" + id + "] in locale [" + locale
											+ "], will try to look for eCommerceMap");
									if (eCommerceMap != null) {
										final String idLink = (String) eCommerceMap.get(id);
										if (idLink != null && !"".equals(idLink)) {
											log.debug("Found eCommerce record for [" + id + "] in locale [" + locale
													+ "], adding:" + idLink);
											document.getRootElement().addAttribute("eCommerceURL", idLink);
											document.getRootElement().addAttribute("PriceNikonLocale",
													locales.iterator().next());
											document.getRootElement().addAttribute("PriceCurrCode", "");
											document.getRootElement().addAttribute("PriceIncVat", " ");
											document.getRootElement().addAttribute(ProductDTO.upc, "");

										}
									}
									log.debug("debug:" + document.asXML());
									rootElement.appendContent(document);
									log.debug("Found fragment [" + id + "] in locale [" + locale
											+ "], breaking and go to next product if available");
									break;
								} else {
									if (count == 0) {
										log.warn("locale opt out in current locale, will ignore product: [" + id
												+ "] in locale [" + locale
												+ "], breaking and go to next product if available");
										break;
									} else {
										log.warn("opt out happen in fallback locale, will go further up in the locales to find product: ["
												+ id + "]");
									}
								}
							}
						} finally {
							try {
								if (is != null) {
									is.close();
								}
							} catch (final Exception e) {
								// ignore
							}
						}
						count++;
					}
				}
			}

		}

	}

	public Document listCatalogueProducts(final RequestContext requestContext) {
		context = requestContext;
		FormatUtils.pFormat("ENVIRONMENT:{0}", this.getTeamsiteEnvironment().getEnvironment());
		log.debug("Entering Document listCatalogueProducts(RequestContext requestContext)");

		log.debug("Creating HBN8QueryParamDTO");
		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());
		log.debug("-- HBN8QueryParamDTO param Start --");
		log.debug(param.toString());
		log.debug("-- HBN8QueryParamDTO param End --");

		// FIXME: AH: This block of code looks like its not used as the 'Product
		// Headings' parameter is never passed in from a URL????
		String navProductsHeadings = requestContext.getParameterString("Product Headings");

		String navProducts = null;
		if (navProductsHeadings == null) {
			navProductsHeadings = requestContext.getParameterString("testProductHeadings");
			navProducts = navProductsHeadings;
			log.debug(FormatUtils.mFormat("Using testProductHeadings:{0}", navProducts));
		} else {
			navProducts = "/templatedata/" + param.getLanguageCountryCode() + navProductsHeadings.substring(18);
			log.debug(FormatUtils.mFormat("Using productHeadings:{0}", navProducts));
		}
		// END AH

		final List<ProductDTO> prodList = this.getNikonHBN8DAOManager().listCatalogueProducts(param);

		// create main doc
		log.debug("Creating XML Doc");
		final Document doc = Dom4jUtils.newDocument("<staticcontent/>");
		final Element root = doc.getRootElement();

		// debug data
		doc.getRootElement().addElement("query").addText(param.getRunQuery());
		// add the locale
		doc.getRootElement().addElement("nklocale").addText(param.getNikonLocale());

		// get data
		for (final ProductDTO prod : prodList) {
			final Document dcrDocument = ExternalUtils.readXmlFile(requestContext, prod.getPath());
			if (dcrDocument != null) {

				final Element dcrRoot = dcrDocument.getRootElement();
				/*****
				 * Create the root node from the DCR document to your document
				 * that will be returned to the component XSL
				 */
				final Element dcr = root.addElement(dcrRoot.getName());
				dcr.addAttribute("Status", "Success");
				dcr.addAttribute("DCRPath", prod.getPath());

				final Iterator i = dcrRoot.attributeIterator();
				for (; i.hasNext();) {
					final Attribute attribute = (Attribute) i.next();
					dcr.addAttribute(attribute.getName(), attribute.getValue());
				}

				// Add the Product short name to the doc and the isnew property
				dcr.addElement(CommonDTOFields.path).addText(prod.getPath());
				dcr.addElement(CommonDTOFields.environment).addText(prod.getEnvironment());
				dcr.addElement(CommonDTOFields.isProduction).addText(FormatUtils.boolean2String(prod.isProduction()));
				dcr.addElement(ProductDTO.productTitle).addText(FormatUtils.nvl(prod.getProdShortName(), ""));
				dcr.addElement(ProductDTO.isNew).addText(FormatUtils.boolean2String(prod.isStillNew()));
				dcr.addElement(ProductDTO.upc).addText(FormatUtils.nvl(prod.getUpc(), "0"));
				dcr.addElement(ProductDTO.index).addText(prod.getProdId());
				dcr.addElement(ProductDTO.localShortName).addText(
						prod.getLocalShortName() == null ? "" : prod.getLocalShortName());

				dcr.addElement(ProductDTO.productCategory).addText(FormatUtils.nvl(prod.getProductCategory(), "0"))
						.addAttribute("Enc", Utils.URLEncode(FormatUtils.nvl(prod.getProductCategory(), "0")));
				dcr.addElement(ProductDTO.navCat1).addText(FormatUtils.nvl(prod.getNavCat1(), "0"))
						.addAttribute("Enc", Utils.URLEncode(FormatUtils.nvl(prod.getNavCat1(), "0")));
				dcr.addElement(ProductDTO.navCat2).addText(FormatUtils.nvl(prod.getNavCat2(), "0"))
						.addAttribute("Enc", Utils.URLEncode(FormatUtils.nvl(prod.getNavCat2(), "0")));
				dcr.addElement(ProductDTO.navCat3).addText(FormatUtils.nvl(prod.getNavCat3(), "0"))
						.addAttribute("Enc", Utils.URLEncode(FormatUtils.nvl(prod.getNavCat3(), "0")));

				/*****
				 * Copy rest of the nodes from DCR document to your document
				 * that will be returned to the component XSL
				 */
				for (final Iterator j = dcrRoot.elementIterator(); j.hasNext();) {
					final Element element = (Element) j.next();
					log.debug("attribute:" + element);
					dcr.add(element.createCopy());
				}
			}
		}

		// return selected parameters
		doc.getRootElement().addElement("sParamValue").addText(FormatUtils.nvl(param.getCategory(), ""));
		doc.getRootElement().addElement("sSubnav1Param").addText(FormatUtils.nvl(param.getNavCat1(), ""));
		doc.getRootElement().addElement("sSubnav2Param").addText(FormatUtils.nvl(param.getNavCat2(), ""));
		doc.getRootElement().addElement("sSubnav3Param").addText(FormatUtils.nvl(param.getNavCat3(), ""));
		doc.getRootElement().addElement("sSubnav4Param").addText(FormatUtils.nvl(param.getNavCat4(), ""));
		doc.getRootElement().addElement("sSubnav5Param").addText(FormatUtils.nvl(param.getNavCat5(), ""));

		doc.getRootElement().addElement("sSubnav1ParamLbl").addText(FormatUtils.nvl(param.getNavCat1Lbl(), ""));
		doc.getRootElement().addElement("sSubnav2ParamLbl").addText(FormatUtils.nvl(param.getNavCat2Lbl(), ""));
		doc.getRootElement().addElement("sSubnav3ParamLbl").addText(FormatUtils.nvl(param.getNavCat3Lbl(), ""));
		doc.getRootElement().addElement("sSubnav4ParamLbl").addText(FormatUtils.nvl(param.getNavCat4Lbl(), ""));
		doc.getRootElement().addElement("sSubnav5ParamLbl").addText(FormatUtils.nvl(param.getNavCat5Lbl(), ""));

		doc.getRootElement().addElement("debugInfo").addText(doc.asXML());

		// return the XML to the component
		return doc;
	}

	public Document listCompareProductDetailsFromIdCode(final String productId, final String siteLanguageCountryCode,
			final RequestContext requestContext) {
		// Create the parameter class
		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());
		param.setProductId(productId);
		param.setNikonLocale(siteLanguageCountryCode);
		param.setSiteCountryCode(FormatUtils.countryCode(param.getNikonLocale()));
		param.setTeamsiteEnvironment(this.getTeamsiteEnvironment());

		final ProductDTO prod = this.getNikonHBN8DAOManager().listProductDetails(param);

		final Document doc = Dom4jUtils.newDocument();
		doc.addElement("staticcontent");

		// Main Product DCR
		if (prod != null && !"".equals(prod.getPath())) {
			final String nameId = FormatUtils.mFormat("prod{0}", 1);
			this.createProductXML(doc.getRootElement(), "Product", nameId, nameId, prod, true);
			if (requestContext != null) {
				doc.getRootElement().appendContent(
						this.loadDCRForDTOInLocale(requestContext, prod.getPath(), prod.getNikonLocale()));
			}
			doc.getRootElement().addElement("productLocalName")
					.setText(prod.getLocalShortName() == null ? "" : prod.getLocalShortName());

		}

		return doc;
	}

	/**
	 * Method to return the Navigation Data for Products based on country and
	 * UPC from a request parameter
	 * 
	 * @param requestContext
	 * @return XMLDocument
	 */
	public Document listNavProducts(final RequestContext requestContext) {
		context = requestContext;
		FormatUtils.pFormat("ENVIRONMENT:{0}", this.getTeamsiteEnvironment().getEnvironment());
		log.debug("Entering Document listNavProducts(RequestContext requestContext)");

		log.debug("Creating HBN8QueryParamDTO");
		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());

		String navProductsHeadings = requestContext.getParameterString("Product Headings");

		String navProducts = null;
		Document localisedNav = null;
		if (navProductsHeadings == null) {
			navProductsHeadings = requestContext.getParameterString("testProductHeadings");
			navProducts = navProductsHeadings;
			log.debug(FormatUtils.mFormat("Using testProductHeadings:{0}", navProducts));
		} else {
			// XXX
			final ComponentHelper ch = new ComponentHelper();
			// ComponentHelper
			localisedNav = ch.getLocalisedDCR(context, "Product Headings");
			// navProducts = "/templatedata/" + param.getLanguageCountryCode() +
			// navProductsHeadings.substring(18);
			// log.debug(FormatUtils.mFormat("Using productHeadings:{0}",
			// navProducts));
		}
		// get the TeamSite workarea vpath from the context

		// gcreate main doc
		log.debug("Creating XML Doc");
		Document doc;
		doc = localisedNav.getDocument();

		// Add the Encoded Attribute values
		doc.accept(new EncodedAttributeVisitor());

		// debug data
		doc.getRootElement().addElement("query")
				.addText(NikonDomainConstants.CTXT_RN_QRY + " - " + param.getRunQuery());

		log.debug("param.getRunQuery()                  :" + param.getRunQuery());
		log.debug("NikonDomainConstants.RN_QRY_VAL_LVL_0:" + NikonDomainConstants.RN_QRY_VAL_LVL_0);

		String productShortName = "";

		log.debug("param.getRunQuery: " + param.getRunQuery() + " and NikonDomainConstants.RN_QRY_VAL_LVL_0: "
				+ NikonDomainConstants.RN_QRY_VAL_LVL_0);

		if (!param.getRunQuery().equals(NikonDomainConstants.RN_QRY_VAL_LVL_0)) {
			log.debug("Calling service listNavCatProductsI2(param)");
			doc.getRootElement().addElement("debug1").addText("Running query");
			List<ProductDTO> prodList = this.getNikonHBN8DAOManager().listNavCatProducts(param);

			log.debug("Loop through ProductDTO");
			for (final ProductDTO prod : prodList) {
				log.debug("Adding product ID " + prod.getProdId() + " (" + prod.getLocalShortName() + ")");
				doc.getRootElement().addElement(CommonDTOFields.path).addText(prod.getPath());
				doc.getRootElement().addElement(CommonDTOFields.environment).addText(prod.getEnvironment());
				doc.getRootElement().addElement(CommonDTOFields.isProduction)
						.addText(FormatUtils.boolean2String(prod.isProduction()));
				doc.getRootElement().addElement("UPC").addText(prod.getProdId() == null ? "" : prod.getProdId());
				doc.getRootElement().addElement("ProductRow")
						.addText(prod.getProdShortCode() == null ? "" : prod.getProdShortCode());
				doc.getRootElement().addElement("LocalShortName")
						.addText(prod.getLocalShortName() == null ? "" : prod.getLocalShortName());
			}

			log.debug("param.getProductId: " + param.getProductId());
			if (!param.getProductId().equals("0")) {
				prodList = this.getNikonHBN8DAOManager().listCatalogueProducts(param);

				for (final ProductDTO prod : prodList) {
					if (prod.getProdId().equals(param.getProductId())) {
						if (prod.getLocalShortName() != null && !prod.getLocalShortName().equals("")) {
							productShortName = prod.getLocalShortName();
						} else {
							productShortName = prod.getProdShortName();
						}
					}
				}
			}
		}

		// return selected parameters
		doc.getRootElement().addElement("sParamValue")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getCategory(), "")));
		doc.getRootElement().addElement("sSubnav1Param")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat1(), "")));
		doc.getRootElement().addElement("sSubnav2Param")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat2(), "")));
		doc.getRootElement().addElement("sSubnav3Param")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat3(), "")));
		doc.getRootElement().addElement("sSubnav4Param")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat4(), "")));
		doc.getRootElement().addElement("sSubnav5Param")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat5(), "")));

		doc.getRootElement().addElement("sParamValueLbl")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getCategoryLbl(), "")));
		doc.getRootElement().addElement("sSubnav1ParamLbl")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat1Lbl(), "")));
		doc.getRootElement().addElement("sSubnav2ParamLbl")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat2Lbl(), "")));
		doc.getRootElement().addElement("sSubnav3ParamLbl")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat3Lbl(), "")));
		doc.getRootElement().addElement("sSubnav4ParamLbl")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat4Lbl(), "")));
		doc.getRootElement().addElement("sSubnav5ParamLbl")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat5Lbl(), "")));
		doc.getRootElement().addElement("sID").addText(param.getProductId());

		// XXX NB 20091027 We don't really need these as we encode in
		// attributes. Can seek to take out in next release
		doc.getRootElement().addElement("sParamValueEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getCategory(), "")));
		doc.getRootElement().addElement("sSubnav1ParamEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat1(), "")));
		doc.getRootElement().addElement("sSubnav2ParamEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat2(), "")));
		doc.getRootElement().addElement("sSubnav3ParamEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat3(), "")));
		doc.getRootElement().addElement("sSubnav4ParamEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat4(), "")));
		doc.getRootElement().addElement("sSubnav5ParamEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat5(), "")));
		doc.getRootElement().addElement("sSubnav1ParamLblEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat1Lbl(), "")));
		doc.getRootElement().addElement("sSubnav2ParamLblEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat2Lbl(), "")));
		doc.getRootElement().addElement("sSubnav3ParamLblEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat3Lbl(), "")));
		doc.getRootElement().addElement("sSubnav4ParamLblEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat4Lbl(), "")));
		doc.getRootElement().addElement("sSubnav5ParamLblEnc")
				.addText(Utils.URLEncode(FormatUtils.nvl(param.getNavCat5Lbl(), "")));

		// Initialize The Breadcrumb Array
		final ArrayList<String> breadCrumb = new ArrayList<String>();

		log.debug("Country Code: " + param.getLanguageCountryCode().toString());

		final FileDALIfc fileDal = context.getFileDAL();

		boolean titleFallback = false;

		final String dcrPath = fileDal.getRoot() + "/templatedata/" + param.getLanguageCountryCode()
				+ "/page_titles/data/page_titles";
		String fallbackDCRPath = fileDal.getRoot() + "/templatedata/en_Asia/page_titles/data/page_titles";

		if (context.isPreview()) {

			final String countryCode = param.getSiteCountryCode();

			fallbackDCRPath = fallbackDCRPath.replaceAll("^(.*)/" + countryCode + "/(.*)$", "$1/Asia/$2");

			log.debug("Fallback DCR: " + fallbackDCRPath);
		}

		java.io.InputStream is = null;
		Document localisedTitles = null;

		try {

			if (fileDal.exists(dcrPath)) {

				// Get Localised Page Title DCR
				is = fileDal.getStream(dcrPath);

				localisedTitles = Dom4jUtils.newDocument(is);

			} else if (fileDal.exists(fallbackDCRPath)) {

				is = fileDal.getStream(fallbackDCRPath);
				titleFallback = true;

				localisedTitles = Dom4jUtils.newDocument(is);
			}

		} catch (final Exception e) {
			log.debug("Error: " + e.toString());

		}

		// Get NSO Site Title
		if (localisedTitles != null) {

			final String nsoSiteTitle = localisedTitles.selectSingleNode("/page_titles/nso_title").getText();

			log.debug("NSO Site Title: " + nsoSiteTitle);

			breadCrumb.add(nsoSiteTitle);

			// Get All Titles
			final List<Node> pageTitleList = localisedTitles.selectNodes("/page_titles/titles");

			// Current Page URL
			String currentPageURL = context.getRequest().getRequestURI();

			if (context.isPreview()) {

				currentPageURL = currentPageURL.replaceAll("^/iw-preview(.*)$", "$1");
			}

			log.debug("Current Page URL: " + currentPageURL);

			// Loop Through Titles
			for (final Node pageNode : pageTitleList) {
				final Node pageURLNode = pageNode.selectSingleNode("page");
				String pageURL = pageURLNode.getText();

				if (titleFallback) {

					pageURL = pageURL.replaceAll("Asia", param.getLanguageCountryCode());
				}

				log.debug("Loop Page URL: " + pageURL);

				// If We Match On Current URL
				if (pageURL.equals(currentPageURL)) {

					final Node pageTitleNode = pageNode.selectSingleNode("title");
					final String localisedPageTitle = pageTitleNode.getText();

					log.debug("Match Page Title: " + localisedPageTitle);

					// If A Product Details Page...
					if (pageURL.equals("/" + param.getLanguageCountryCode() + "/products/product_details.page")) {

						if (param.getCategory() != null && !param.getCategoryLbl().equals("0")) {
							breadCrumb.add(param.getCategoryLbl());

							if (param.getNavCat1Lbl() != null && !param.getNavCat1Lbl().equals("0")) {
								// sub nav 1 exists so add to bread crumb
								breadCrumb.add(param.getNavCat1Lbl());
								if (param.getNavCat2Lbl() != null && !param.getNavCat2Lbl().equals("0")) {
									// sub nav 2 exists so add to bread crumb
									breadCrumb.add(param.getNavCat2Lbl());
									if (param.getNavCat3Lbl() != null && !param.getNavCat3Lbl().equals("0")) {
										// sub nav 3 exists so add to bread
										// crumb
										breadCrumb.add(param.getNavCat3Lbl());
										if (param.getNavCat4Lbl() != null && !param.getNavCat4Lbl().equals("0")) {
											// sub nav 4 exists so add to bread
											// crumb
											breadCrumb.add(param.getNavCat4Lbl());
											if (param.getNavCat5Lbl() != null && !param.getNavCat5Lbl().equals("0")) {
												// sub nav 5 exists so add to
												// bread crumb
												breadCrumb.add(param.getNavCat5Lbl());
											}
										}
									}
								}
							}

							// Add Product Short Name...
							if (!productShortName.equals("")) {
								if (!productShortName.equals(breadCrumb.get(breadCrumb.size() - 1))) {
									breadCrumb.add(productShortName);

								}
							}
						}

						// Add DCR Title to End
						breadCrumb.add(localisedPageTitle);

					} else {

						// Add DCR Title
						breadCrumb.add(localisedPageTitle);
					}
				}
			}
		}

		String pageTitle = "";

		// Output Title Breadcrumb
		for (final String bcPart : breadCrumb) {
			if (!pageTitle.equals("")) {
				pageTitle += " - ";
			}

			pageTitle += bcPart;
		}

		// Output For Debug
		doc.getRootElement().addElement("pageTitle").addText(pageTitle);

		// Set The Title In The Page
		context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, pageTitle);

		log.debug("Exiting Document listNavProducts(RequestContext requestContext)");
		doc.getRootElement().addElement("debugInfo").addText(doc.asXML());

		// return the XML to the component
		return doc;
	}

	/**
	 * Change to public so we can use it for generating the XML response
	 * 
	 * @param param
	 * @param requestContext
	 * @return
	 */
	public Document listProductDetails(final HBN8QueryParamDTO param, final RequestContext requestContext) {
		ProductDTO prod = null;

		final String productId = param.getProductId();
		final String productLocale = param.getNikonLocale();
		if (requestContext != null
				&& requestContext.getRequest().getAttribute("prodDTO_" + productLocale + "_" + productId) != null) {
			// Try page wide product DTO
			prod = (ProductDTO) requestContext.getRequest().getAttribute("prodDTO_" + productLocale + "_" + productId);
		}
		log.debug("_listProductDetails() - Product: " + productId + " - " + productLocale + ", Reading Mode: "
				+ param.getMode());
		Document doc = null;
		boolean fallback = false;
		if (requestContext != null) {
			log.debug("Page name: " + requestContext.getPageName());
		}
		if (NikonDomainConstants.NT_APPLCBL.equals(productId) || !NikonUtils.isNumeric(productId)) {
			log.debug("NA is the product ID, or it is not Integer, (" + productId
					+ ") will not look up and return a blank document");
			doc = Dom4jUtils.newDocument();
			doc.addElement("staticcontent");
			return doc;
		}
		if (NikonHBN8ParamConstants.MODE_READ_STATIC.equals(param.getMode())) {
			InputStream isXML = null;
			InputStream isProp = null;
			final InputStream isEComm = null;
			try {
				final long startTime = System.currentTimeMillis();
				final NikonRepository repo = param.getRepo();
				FastHashMap commerceMap = null;
				JCS prodResponseJCS = null;
				String asXML = "";
				try {
					if (repo.isCacheEnable()) {
						prodResponseJCS = JCS.getInstance(NikonDomainConstants.JCS_REGION_PROD_RESPONSE);
					}
					commerceMap = this.getECommerceMap(repo, productLocale);
				} catch (final Exception e) {
					log.error("Error while trying to get JCS cache");
				}
				if (prodResponseJCS != null) {
					log.info("Attempt to loading XML from the cache...");
					asXML = (String) prodResponseJCS.get(productLocale + "_" + productId);
				}
				if (asXML != null && !"".equals(asXML)) {
					doc = Dom4jUtils.newDocument(asXML);
				} else {
					final Collection<String> locales = LocaleResolver.resolvePossibleLocales(param);
					log.info("Loading [" + productId + "] XML from the repo in locales: " + locales.toString());
					isXML = repo.retreieveContentWithFallbackSupport("product", productId + ".xml", locales);

					if (isXML == null) {
						log.error("Cannot find the XML file for product id [" + productId
								+ "] even using fallback locale, will going to use DB instead");
						fallback = true;
					} else {
						doc = Dom4jUtils.newDocument(isXML);
						if (doc.selectSingleNode("//localeOptout") == null) {

							// Feature explained
							Node elProdDetails = doc.getRootElement().selectSingleNode("product_details");
							List<Node> featuresNodes = null;
							if (elProdDetails.selectSingleNode("features_explained") != null) {
								featuresNodes = elProdDetails.selectNodes("features_explained");
								if (featuresNodes != null) {
									Element elFeatures = doc.getRootElement().addElement("features_explained");
									for (Node fen : featuresNodes) {
										if ((fen.selectSingleNode("reference") != null)
												&& (!fen.selectSingleNode("reference").getText().equals(""))) {

											if (requestContext != null) {
												elFeatures.addElement("features")
														.appendContent(
																this.loadDCRForDTOInLocale(requestContext, fen
																		.selectSingleNode("reference").getText(),
																		productLocale));
											}
										}
									}
								}
							}

							final Element node = (Element) doc.selectSingleNode("//Product");
							String idLink = null;
							if (node != null) {
								final String locale = node.attributeValue("nikonLocale");
								final String productType = node.attributeValue("type");
								Element buyNowNode = (Element) doc
										.selectSingleNode("/staticcontent/product_details/buy_now");
								if (commerceMap != null) {
									idLink = (String) commerceMap.get(productId);
								}
								if (buyNowNode == null) {
									buyNowNode = new DefaultElement("buy_now");
									final Element pd = (Element) doc.selectSingleNode("/staticcontent/product_details");
									Element currLocaleLink = null;
									if (commerceMap != null && idLink != null) {
										currLocaleLink = buyNowNode.addElement("buy_now_link");
										currLocaleLink.addAttribute("href", idLink);
										currLocaleLink.addAttribute("target", "t" + productId);
									}
									if (productLocale.equals("en_GB")) {
										final FastHashMap ecIE = this.getECommerceMap(repo, "en_IE");
										if (ecIE != null) {
											final String idLinkIe = (String) ecIE.get(productId);
											if (idLinkIe != null) {
												if (currLocaleLink != null) {
													currLocaleLink.addText("Great Britain");
												}
												final DefaultElement ireland = (DefaultElement) buyNowNode
														.addElement("buy_now_link");
												ireland.addText("Ireland");
												ireland.addAttribute("target", "ie" + productId);
												ireland.addAttribute("href", idLinkIe);
											}
										}
									}
									pd.add(buyNowNode);
								}
								if (commerceMap != null) {
									log.debug("Lookup eCommerce link in the existing document...");
									final String commerceImageLink = (String) commerceMap.get("image");
									if (commerceImageLink != null && !"".equals(commerceImageLink) && idLink != null
											&& !"".equals(idLink)) {
										if (!"Accessory".equals(productType)) {
											final Element promotionNode = (Element) doc
													.selectSingleNode("/staticcontent/product_details/promotions[image='"
															+ commerceImageLink + "']");
											commerceImageLink.replaceFirst("_[A-Z][A-Z]$", "");
											commerceImageLink.replaceFirst("_Asia$", "");
											if (promotionNode == null) {
												log.debug("Promotion node does not have the image link, will attempt to add eComm link");
												final DefaultElement promotionElem = new DefaultElement("promotions");
												promotionElem.addElement("task2").addText("(Get Original)");
												promotionElem.addElement("image").addText(commerceImageLink);
												promotionElem.addElement("image_obf");
												promotionElem.addElement("image_link").addText(idLink);
												final List contents = ((Element) doc
														.selectSingleNode("/staticcontent/product_details")).content();
												contents.add(0, promotionElem);
											} else {
												log.debug("Promotion node already have that image, will not add the EComm link");
											}

										} else {
											final Element promotionNode = (Element) doc
													.selectSingleNode("/staticcontent/product_details/banners[banner='"
															+ commerceImageLink + "']");
											if (promotionNode == null) {
												log.debug("Promotion node does not have the image link, will attempt to add eComm link");
												final DefaultElement promotionElem = new DefaultElement("banners");
												promotionElem.addElement("task1").addText("(Get Original)");
												promotionElem.addElement("banner").addText(commerceImageLink);
												promotionElem.addElement("banner_image_link").addText(idLink);
												final List contents = ((Element) doc
														.selectSingleNode("/staticcontent/product_details")).content();
												contents.add(0, promotionElem);
											} else {
												log.debug("Promotion node already have that image, will not add the EComm link");
											}
										}

										node.addAttribute("eCommerceURL", idLink);
										node.addAttribute("PriceNikonLocale", locale);
										node.addAttribute("PriceCurrCode", "");
										node.addAttribute("PriceIncVat", " ");
										node.addAttribute(ProductDTO.upc, "");

									}
								}
								log.debug("XML is coming from locale [" + locale + "], will lookup the properites file");
								isProp = repo
										.retrieveContent("product", productId + "_relationship.properties", locale);
								if (isProp != null) {
									final Properties prop = new Properties();
									prop.load(isProp);
									log.debug("Properties file found, will start working on the relationship...");
									this.insertChilds(doc.getRootElement(), prop.getProperty("RelatedProduct"),
											"RelatedProduct", "relProd", repo, locales, commerceMap);
									this.insertChilds(doc.getRootElement(), prop.getProperty("BomProduct"),
											"BomProduct", "bomProd", repo, locales, commerceMap);
									this.insertChilds(doc.getRootElement(), prop.getProperty("AccessoryProduct"),
											"AccessoryProduct", "accProd", repo, locales, commerceMap);
									this.insertChilds(doc.getRootElement(), prop.getProperty("MarketingRelated"),
											"ProductMarketingRelated", "productMarketingRelated", repo, locales,
											commerceMap);
									// 2020-05-27: Not limited to Accessory type only. For Product type, it's used for Related Products relationship.
//									if ("Accessory".equals(productType)) {
										this.insertChilds(doc.getRootElement(), prop.getProperty("AccessoryOf"),
												"AccessoryOfProduct", "accOfProd", repo, locales, commerceMap);
//									}
									log.info("Finish loading [" + productId + "] in locale [" + productLocale
											+ "], Loading time: " + (System.currentTimeMillis() - startTime) + "ms");
									if (prodResponseJCS != null) {
										prodResponseJCS.put(productLocale + "_" + productId, doc.asXML());
									}
								} else {
									log.error("Cannot find the Properties file for product id [" + productId
											+ "] even using fallback locale, will going to use DB instead");
									fallback = true;
								}
							} else {
								log.error("No Nikon locale found within the files, will fallback to DB");
								fallback = true;
							}
						} else {
							log.warn("Product id [" + productId + "] is opt out in locale [" + productLocale
									+ "] this will be empty page");
						}
					}

				}

			} catch (final Exception e) {
				log.error(
						"Error while trying to generate reponse from static file, will try to fallback to reading db",
						e);
				fallback = true;
			} finally {
				try {
					if (isXML != null) {
						isXML.close();
					}
					if (isProp != null) {
						isProp.close();
					}
					if (isEComm != null) {
						isEComm.close();
					}
				} catch (final Exception e) {
					// Ignore
				}
			}

		}
		if (NikonHBN8ParamConstants.MODE_READ_DB.equals(param.getMode()) || fallback == true) {
			if (fallback == true) {
				log.warn("Something wrong while trying to read the static file, will fallback to reading DB: id ["
						+ productId
						+ "], locale ["
						+ productLocale
						+ "]. Please note that Commerce link will be missing because we assume repository is not available at this stage");
			}
			if (requestContext.getRequest().getAttribute("prodDTO_" + productLocale + "_" + productId) != null) {

				log.debug("_listProductDetails() - Use Page Scope Product DTO");
				prod = (ProductDTO) requestContext.getRequest().getAttribute(
						"prodDTO_" + productLocale + "_" + productId);

			} else {

				log.debug("_listProductDetails() - Build Product DTO");
				prod = this.getNikonHBN8DAOManager().listProductDetails(param);
			}
			// Related Products
			doc = this._listProductDetailsDoc(requestContext, prod, null);
		} else if (NikonHBN8ParamConstants.MODE_GENERATE.equals(param.getMode())) {
			log.debug("Pre Generating XML...");
			prod = this.getNikonHBN8DAOManager().listProductDetails(param);
			doc = this._listProductDetailsDoc(null, prod, param);
			log.debug("Param Source Path:" + param.getSourcePath());
			log.debug("Product Details: " + doc.asXML());
			if (prod != null) {
				try {
					OutputStream os = param.getRepo().writeContent("product", prod.getProdId() + "_org.xml",
							productLocale);
					XMLWriter writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
					writer.write(doc);
					writer.flush();
					writer.close();
					final Element rootElement = doc.getRootElement();

					final Document prodDoc = Dom4jUtils.newDocument();
					final Element prodDocRootElement = prodDoc.addElement("staticcontent");
					if (rootElement.selectSingleNode("/staticcontent/Product") != null) {
						prodDocRootElement
								.add((Element) rootElement.selectSingleNode("/staticcontent/Product").clone());
					}
					if (rootElement.selectSingleNode("/staticcontent/product_details") != null) {
						prodDocRootElement.add((Element) rootElement.selectSingleNode("/staticcontent/product_details")
								.clone());
					}
					if (rootElement.selectSingleNode("/staticcontent/productLocalName") != null) {
						prodDocRootElement.add((Element) rootElement
								.selectSingleNode("/staticcontent/productLocalName").clone());
					}

					os = param.getRepo().writeContent("product", prod.getProdId() + ".xml", productLocale);
					writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
					writer.write(prodDoc);
					writer.flush();
					writer.close();
					final Document fragmentDoc = Dom4jUtils.newDocument();
					if (rootElement.selectSingleNode("/staticcontent/Product") != null) {
						final Element mainNode = (Element) rootElement.selectSingleNode("/staticcontent/Product")
								.clone();
						if (rootElement.selectSingleNode("/staticcontent/product_details") != null) {
							mainNode.add((Element) rootElement.selectSingleNode("/staticcontent/product_details")
									.clone());
						}
						fragmentDoc.add(mainNode);
					}
					os = param.getRepo().writeContent("product", prod.getProdId() + "_fragment.xml", productLocale);
					writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
					writer.write(fragmentDoc);
					writer.flush();
					writer.close();

					// Update the product relationship
					final Properties prop = new Properties();
					final InputStream is = param.getRepo().retrieveContent("product",
							prod.getProdId() + "_relationship.properties", productLocale);
					if (is != null) {
						prop.load(is);
						is.close();
					}

					String related = "";
					for (final ProductDTO dto : prod.getRelatedProducts()) {
						if ("".equals(related)) {
							related = dto.getProdId();
						} else {
							related = related + "," + dto.getProdId();
						}
					}
					prop.setProperty("RelatedProduct", related);

					String bom = "";
					for (final ProductDTO dto : prod.getBillOfMaterials()) {
						if ("".equals(bom)) {
							bom = dto.getProdId();
						} else {
							bom = bom + "," + dto.getProdId();
						}
					}
					prop.setProperty("BomProduct", bom);

					String accessory = "";
					for (final ProductDTO dto : prod.getAccessories()) {
						if ("".equals(accessory)) {
							accessory = dto.getProdId();
						} else {
							accessory = accessory + "," + dto.getProdId();
						}
					}
					prop.setProperty("AccessoryProduct", accessory);

					String marketingRelated = "";
					for (final ProductDTO dto : prod.getProductMarketingRelated()) {
						if ("".equals(marketingRelated)) {
							marketingRelated = dto.getProdId();
						} else {
							marketingRelated = marketingRelated + "," + dto.getProdId();
						}
					}
					prop.setProperty("MarketingRelated", marketingRelated);

					String accessoryOf = prop.getProperty("AccessoryOf");
					if (accessoryOf == null) {
						accessoryOf = "";
					}
					// Store the existing list, which will then get remove from
					// the list later
					final ArrayList<String> accessoryOfToBeRemove = new ArrayList<String>();
					final String[] accessoryOfArr = accessoryOf.split(",");
					for (final String s : accessoryOfArr) {
						accessoryOfToBeRemove.add(s);
					}
					// Reset the Accessory
					accessoryOf = "";
					for (final ProductDTO dto : prod.getAccessoryOf()) {
						if (accessoryOfToBeRemove.contains(dto.getProdId())) {
							accessoryOfToBeRemove.remove(dto.getProdId());
						}
						if ("".equals(accessoryOf)) {
							accessoryOf = dto.getProdId();
						} else {
							accessoryOf = accessoryOf + "," + dto.getProdId();
						}
					}
					//
					prop.setProperty("AccessoryOf", accessoryOf);

					String marketingRelatedReverse = prop.getProperty("MarketingRelatedReverse");
					if (marketingRelatedReverse == null) {
						marketingRelatedReverse = "";
					}
					// Store the existing list, which will then get remove from
					// the list later

					final ArrayList<String> marketingRelatedReverseRemove = new ArrayList<String>();
					final String[] marketingRelatedReverseArr = marketingRelatedReverse.split(",");
					for (final String s : marketingRelatedReverseArr) {
						marketingRelatedReverseRemove.add(s);
					}
					// Reset the Accessory
					marketingRelatedReverse = "";
					for (final ProductDTO dto : prod.getProductMarketingRelatedReverse()) {
						if (marketingRelatedReverseRemove.contains(dto.getProdId())) {
							marketingRelatedReverseRemove.remove(dto.getProdId());
						}
						if ("".equals(marketingRelatedReverse)) {
							marketingRelatedReverse = dto.getProdId();
						} else {
							marketingRelatedReverse = marketingRelatedReverse + "," + dto.getProdId();
						}
					}
					//
					prop.setProperty("MarketingRelatedReverse", marketingRelatedReverse);

					os = param.getRepo().writeContent("product", prod.getProdId() + "_relationship.properties",
							productLocale);
					prop.store(os, "Relationship");
					os.flush();
					os.close();
					final ArrayList<String> updatedRelationshipFile = new ArrayList<String>();
					log.info("List of product/accessory will remove id[" + prod.getProdId()
							+ "] from this accessoryProductRelationship" + accessoryOfToBeRemove);
					updatedRelationshipFile.addAll(this.updateOtherProduct(prod, prod.getAccessoryOf(),
							accessoryOfToBeRemove, "AccessoryProduct", param));
					log.info("List of product/accessory will remove id[" + prod.getProdId()
							+ "] from this marketingRelatedRelationship" + marketingRelatedReverseRemove);
					updatedRelationshipFile.addAll(this.updateOtherProduct(prod,
							prod.getProductMarketingRelatedReverse(), marketingRelatedReverseRemove,
							"MarketingRelated", param));
					String updatedFiles = "";
					for (final String s : updatedRelationshipFile) {
						if ("".equals(updatedFiles)) {
							updatedFiles = s;
						} else {
							updatedFiles = updatedFiles + "," + s;
						}
					}
					log.info("List of relationship file updated: " + updatedFiles);
					rootElement.addElement("updatedRelationshipFile").setText(updatedFiles);
					rootElement.addElement("productDCRPath").setText(prod.getPath());
				} catch (final IOException e) {
					log.error("Error while trying to write content to the repository", e);
				}
			} else {
				// prod not found, as they only way you can here when generated
				// through the workflow (A DCR has been submitted), we will
				// assume this is opt out.
				try {
					final String dcrPath = this.getNikonHBN8DAOManager().getProductDCRByIdAndLocale(param);
					if (!"".equals(dcrPath)) {
						final Element rootElement = doc.getRootElement();
						rootElement.addElement("localeOptout").setText("true");
						OutputStream os = param.getRepo().writeContent("product", productId + ".xml", productLocale);
						XMLWriter writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
						writer.write(doc);
						writer.flush();
						writer.close();
						os = param.getRepo().writeContent("product", productId + "_fragment.xml", productLocale);
						writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
						writer.write(doc);
						writer.flush();
						writer.close();
						rootElement.addElement("productDCRPath").setText(dcrPath);
					} else {
						log.error("cannot found DCR path for [" + productId + "] and locale [" + productLocale + "]");
					}
				} catch (final Exception e) {
					log.error("Error while trying to write content to the repository", e);
				}

			}
		}

		final Element rootElement = doc.getRootElement();

		// ---------------------------------------------------------------------------
		// Set The META Description Metatag In The Page
		// ---------------------------------------------------------------------------

		// Products
		if (rootElement.selectSingleNode("product_details/category_description") != null) {

			String productCategoryDescription = rootElement.selectSingleNode("product_details/category_description")
					.getText();
			productCategoryDescription = productCategoryDescription.replaceAll("\\<.*?>", "");

			if (productCategoryDescription != null && context != null) {

				context.getPageScopeData().put(RuntimePage.PAGESCOPE_DESCRIPTION, productCategoryDescription);

			}
		}
		// Accessories
		else if (rootElement.selectSingleNode("product_details/summary_description") != null) {

			String productCategoryDescription = rootElement.selectSingleNode("product_details/summary_description")
					.getText();
			productCategoryDescription = productCategoryDescription.replaceAll("\\<.*?>", "");

			if (productCategoryDescription != null && context != null) {

				context.getPageScopeData().put(RuntimePage.PAGESCOPE_DESCRIPTION, productCategoryDescription);

			}
		}

		// ---------------------------------------------------------------------------
		// Set The Search Image Metatag In The Page
		// ---------------------------------------------------------------------------

		// Products
		if (rootElement.selectSingleNode("product_details/category_image") != null) {

			String pageProductImageMeta = null;

			final String productCategoryImage = rootElement.selectSingleNode("product_details/category_image")
					.getText();

			if (productCategoryImage != null && context != null) {

				if (productCategoryImage.matches("^.*\\.jpg$") || productCategoryImage.matches("^.*\\.gif$")
						|| productCategoryImage.matches("^.*\\.png$")) {

					pageProductImageMeta = "<meta name=\"product-image\" content=\"/" + productCategoryImage + "\" />";

					context.getPageScopeData().put(RuntimePage.PAGESCOPE_HEAD_INJECTION, pageProductImageMeta);

				}
			}

		}
		// Accessories
		else if (rootElement.selectSingleNode("product_details/summary_image") != null) {

			String pageProductImageMeta = null;

			final String productCategoryImage = rootElement.selectSingleNode("product_details/summary_image").getText();

			if (productCategoryImage != null && context != null) {

				if (productCategoryImage.matches("^.*\\.jpg$") || productCategoryImage.matches("^.*\\.gif$")
						|| productCategoryImage.matches("^.*\\.png$")) {

					pageProductImageMeta = "<meta name=\"product-image\" content=\"/" + productCategoryImage + "\" />";

					context.getPageScopeData().put(RuntimePage.PAGESCOPE_HEAD_INJECTION, pageProductImageMeta);

				}
			}
		}
		return doc;
	}

	public Document listProductDetails(final RequestContext requestContext) {
		Document doc = null;
		if (this.getNikonRepository().isEnable()) {
			doc = this.listProductDetails(requestContext, NikonHBN8ParamConstants.MODE_READ_STATIC);
		} else {
			doc = this.listProductDetails(requestContext, NikonHBN8ParamConstants.MODE_READ_DB);
		}
		doc = this.defaultContentInjection(requestContext, doc);
		this.setPageTitleScope(requestContext, doc);
		this.setPageKeywordDescScope(requestContext, doc);
		return doc;
	}

	public void setPageTitleScope(RequestContext requestContext, Document doc) {

		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());

		// Initialize The Breadcrumb Array
		final ArrayList<String> breadCrumb = new ArrayList<String>();

		log.debug("Country Code: " + param.getLanguageCountryCode().toString());

		final FileDALIfc fileDal = context.getFileDAL();

		boolean titleFallback = false;

		final String dcrPath = fileDal.getRoot() + "/templatedata/" + param.getLanguageCountryCode()
				+ "/page_titles/data/page_titles";
		String fallbackDCRPath = fileDal.getRoot() + "/templatedata/en_Asia/page_titles/data/page_titles";

		if (context.isPreview()) {

			final String countryCode = param.getSiteCountryCode();

			fallbackDCRPath = fallbackDCRPath.replaceAll("^(.*)/" + countryCode + "/(.*)$", "$1/en_Asia/$2");

			log.debug("Fallback DCR: " + fallbackDCRPath);
		}

		java.io.InputStream is = null;
		Document localisedTitles = null;

		try {

			if (fileDal.exists(dcrPath)) {

				// Get Localised Page Title DCR
				is = fileDal.getStream(dcrPath);

				localisedTitles = Dom4jUtils.newDocument(is);

			} else if (fileDal.exists(fallbackDCRPath)) {

				is = fileDal.getStream(fallbackDCRPath);
				titleFallback = true;

				localisedTitles = Dom4jUtils.newDocument(is);
			}

		} catch (final Exception e) {
			log.debug("Error: " + e.toString());

		}

		// Get NSO Site Title
		if (localisedTitles != null) {

			final String nsoSiteTitle = localisedTitles.selectSingleNode("/page_titles/nso_title").getText();

			log.debug("NSO Site Title: " + nsoSiteTitle);

			breadCrumb.add(nsoSiteTitle);

			// Get All Titles
			final List<Node> pageTitleList = localisedTitles.selectNodes("/page_titles/titles");

			// Current Page URL
			String currentPageURL = context.getRequest().getRequestURI();

			if (context.isPreview()) {

				currentPageURL = currentPageURL.replaceAll("^/iw-preview(.*)$", "$1");
			}

			log.debug("Current Page URL: " + currentPageURL);

			// Loop Through Titles
			for (final Node pageNode : pageTitleList) {
				final Node pageURLNode = pageNode.selectSingleNode("page");
				String pageURL = pageURLNode.getText();

				if (titleFallback) {

					pageURL = pageURL.replaceAll("Asia", param.getLanguageCountryCode());
				}

				log.debug("Loop Page URL: " + pageURL);

				// If We Match On Current URL
				if (pageURL.equals(currentPageURL)) {

					final Node pageTitleNode = pageNode.selectSingleNode("title");
					final String localisedPageTitle = pageTitleNode.getText();

					log.debug("Match Page Title: " + localisedPageTitle);

					// If A Product Details Page...
					if (pageURL.equals("/" + param.getLanguageCountryCode() + "/products/product_details.page")) {

						if (param.getCategory() != null && !param.getCategoryLbl().equals("0")) {
							breadCrumb.add(param.getCategoryLbl());

							if (param.getNavCat1Lbl() != null && !param.getNavCat1Lbl().equals("0")) {
								// sub nav 1 exists so add to bread crumb
								breadCrumb.add(param.getNavCat1Lbl());
								if (param.getNavCat2Lbl() != null && !param.getNavCat2Lbl().equals("0")) {
									// sub nav 2 exists so add to bread crumb
									breadCrumb.add(param.getNavCat2Lbl());
									if (param.getNavCat3Lbl() != null && !param.getNavCat3Lbl().equals("0")) {
										// sub nav 3 exists so add to bread
										// crumb
										breadCrumb.add(param.getNavCat3Lbl());
										if (param.getNavCat4Lbl() != null && !param.getNavCat4Lbl().equals("0")) {
											// sub nav 4 exists so add to bread
											// crumb
											breadCrumb.add(param.getNavCat4Lbl());
											if (param.getNavCat5Lbl() != null && !param.getNavCat5Lbl().equals("0")) {
												// sub nav 5 exists so add to
												// bread crumb
												breadCrumb.add(param.getNavCat5Lbl());
											}
										}
									}
								}
							}

						}
						// Add Product Short Name...
						final Element prod_node = (Element) doc.getRootElement().selectSingleNode(
								"//staticcontent/Product");
						if (prod_node != null) {
							if (prod_node.attribute("localShortName") != null
									&& !"".equals(prod_node.attribute("localShortName").getValue())) {
								String productShortName = prod_node.attribute("localShortName").getValue();
								if (!productShortName.equals("")) {
									if (!productShortName.equals(breadCrumb.get(breadCrumb.size() - 1))) {
										breadCrumb.add(productShortName);
									}
								}
							}
						} else {
							log.warn("No prod detail node found when try to injest page title");
						}

						// Add DCR Title to End
						breadCrumb.add(localisedPageTitle);

					} else {

						// Add DCR Title
						breadCrumb.add(localisedPageTitle);
					}
				}
			}
		}

		String pageTitle = "";

		// Output Title Breadcrumb
		for (final String bcPart : breadCrumb) {
			if (!pageTitle.equals("")) {
				pageTitle += " - ";
			}

			pageTitle += bcPart;
		}

		// Output For Debug
		doc.getRootElement().addElement("pageTitle").addText(pageTitle);

		// Set The Title In The Page
		context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, pageTitle);
	}

	public void setPageKeywordDescScope(RequestContext requestContext, Document doc) {

		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());
		log.debug("Country Code: " + param.getLanguageCountryCode().toString());
		final FileDALIfc fileDal = context.getFileDAL();
		boolean keywordDescFallback = false;

		// 1. read product_html_meta DCR
		final String dcrPath = fileDal.getRoot() + "/templatedata/" + param.getLanguageCountryCode()
				+ "/product_html_meta/data/product_html_meta";
		String fallbackDCRPath = fileDal.getRoot() + "/templatedata/en_Asia/product_html_meta/data/product_html_meta";
		log.debug("dcrPath: " + dcrPath);
		log.debug("fallbackDCRPath: " + fallbackDCRPath);
		if (context.isPreview()) {
			final String countryCode = param.getSiteCountryCode();
			fallbackDCRPath = fallbackDCRPath.replaceAll("^(.*)/" + countryCode + "/(.*)$", "$1/en_Asia/$2");
			log.debug("Fallback DCR: " + fallbackDCRPath);
		}
		java.io.InputStream is = null;
		Document localisedKeywordDesc = null;
		try {
			if (fileDal.exists(dcrPath)) {
				// Get Localised Page Title DCR
				is = fileDal.getStream(dcrPath);
				localisedKeywordDesc = Dom4jUtils.newDocument(is);
			} else if (fileDal.exists(fallbackDCRPath)) {
				is = fileDal.getStream(fallbackDCRPath);
				keywordDescFallback = true;
				localisedKeywordDesc = Dom4jUtils.newDocument(is);
			}
		} catch (final Exception e) {
			log.error("Error: " + e.toString());
		}

		// 2. get name of category, navcat123 and short name of current product
		String productShortName = "";
		String productCategory = "";
		String productNavCat1 = "";
		String productNavCat2 = "";
		String productNavCat3 = "";

		final Element prod_node = (Element) doc.getRootElement().selectSingleNode("//staticcontent/Product");
		if (prod_node != null) {
			if (prod_node.attribute("localShortName") != null
					&& !"".equals(prod_node.attribute("localShortName").getValue())) {
				productShortName = prod_node.attribute("localShortName").getValue();
			}
			if (prod_node.attribute("productCategory") != null
					&& !"".equals(prod_node.attribute("productCategory").getValue())) {
				productCategory = prod_node.attribute("productCategory").getValue();
			}
			if (prod_node.attribute("navCat1") != null && !"".equals(prod_node.attribute("navCat1").getValue())) {
				productNavCat1 = prod_node.attribute("navCat1").getValue();
			}
			if (prod_node.attribute("navCat2") != null && !"".equals(prod_node.attribute("navCat2").getValue())) {
				productNavCat2 = prod_node.attribute("navCat2").getValue();
			}
			if (prod_node.attribute("navCat3") != null && !"".equals(prod_node.attribute("navCat3").getValue())) {
				productNavCat3 = prod_node.attribute("navCat3").getValue();
			}

		} else {
			log.warn("No prod detail node found when try to injest page title");
		}

		// 3. Get Keyword and description from product_html_meta DCR
		String pageKeyword = "";
		String pageDescription = "";
		if (localisedKeywordDesc != null) {
			final List<Node> categoryNodeList = localisedKeywordDesc.selectNodes("/product_html_meta/prod_category");

			// Loop through categories in product_html_meta DCR
			for (Node categoryNode : categoryNodeList) {
				Node categoryNameNode = categoryNode.selectSingleNode("category_name");
				Node keywordNode = categoryNode.selectSingleNode("keyword");
				Node descriptionNode = categoryNode.selectSingleNode("description");

				String categoryName = "";
				if (categoryNameNode != null) {
					categoryName = categoryNameNode.getText();
				}
				if (categoryName.equals(productCategory)) {
					// category keyword
					if (keywordNode != null && !"".equals(keywordNode.getText())) {
						pageKeyword = keywordNode.getText();
					}
					// category description
					if (descriptionNode != null && !"".equals(descriptionNode.getText())) {
						pageDescription = descriptionNode.getText();
					}
					// loop cat1
					List<Node> navcat1NodeList = categoryNode.selectNodes("navcat1");
					for (Node navcat1Node : navcat1NodeList) {
						Node cat1NameNode = navcat1Node.selectSingleNode("navcat1_name");
						Node cat1KeywordNode = navcat1Node.selectSingleNode("keyword");
						Node cat1DescriptionNode = navcat1Node.selectSingleNode("description");
						String cat1Name = "";
						if (cat1NameNode != null) {
							cat1Name = cat1NameNode.getText();
						}
						if (cat1Name.equals(productNavCat1)) {
							// NavCat1 keyword
							if (cat1KeywordNode != null && !"".equals(cat1KeywordNode.getText())) {
								if (!"".equals(pageKeyword))
									pageKeyword += "," + cat1KeywordNode.getText();
								else
									pageKeyword += cat1KeywordNode.getText();
							}
							// NavCat1 description
							if (cat1DescriptionNode != null && !"".equals(cat1DescriptionNode.getText())) {
								if (!"".equals(pageDescription))
									pageDescription += "," + cat1DescriptionNode.getText();
								else
									pageDescription += cat1DescriptionNode.getText();
							}
							// loop cat2
							List<Node> navcat2NodeList = navcat1Node.selectNodes("navcat2");
							for (Node navcat2Node : navcat2NodeList) {
								Node cat2NameNode = navcat2Node.selectSingleNode("navcat2_name");
								Node cat2KeywordNode = navcat2Node.selectSingleNode("keyword");
								Node cat2DescriptionNode = navcat2Node.selectSingleNode("description");
								String cat2Name = "";
								if (cat2NameNode != null) {
									cat2Name = cat2NameNode.getText();
								}
								if (cat2Name.equals(productNavCat2)) {
									// NavCat2 keyword
									if (cat2KeywordNode != null && !"".equals(cat2KeywordNode.getText())) {
										if (!"".equals(pageKeyword))
											pageKeyword += "," + cat2KeywordNode.getText();
										else
											pageKeyword += cat2KeywordNode.getText();
									}
									// NavCat2 description
									if (cat2DescriptionNode != null && !"".equals(cat2DescriptionNode.getText())) {
										if (!"".equals(pageDescription))
											pageDescription += "," + cat2DescriptionNode.getText();
										else
											pageDescription += cat2DescriptionNode.getText();
									}
								}
							}
						}
					}
				}

			}
		}
		// replace {local_short_name} with actual product name
		log.debug("productShortName = " + productShortName);
		log.debug("pageKeyword (before replace) = " + pageKeyword);
		pageKeyword = pageKeyword.replaceAll("\\{local_short_name\\}", productShortName);
		log.debug("pageKeyword (after replace)  = " + pageKeyword);
		log.debug("pageDescription = " + pageDescription);
		log.debug("Component Name =" + requestContext.getThisComponent().getName());

		// replace {focal_length} and {aperture} with actual focal length and
		// aperture
		String focal_length = "";
		String aperture = "";
		Pattern p = Pattern.compile("(\\d+\\-?\\d*)mm");
		Matcher m = p.matcher(productShortName);
		while (m.find()) {
			focal_length = m.group(1);
		}
		pageKeyword = pageKeyword.replaceAll("\\{focal_length\\}", focal_length);
		p = Pattern.compile("f\\/(\\d\\.?\\d?\\-?\\d?\\.?\\d?)");
		m = p.matcher(productShortName);
		while (m.find()) {
			aperture = m.group(1);
		}
		pageKeyword = pageKeyword.replaceAll("\\{aperture\\}", aperture);

		// Set The keyword/description In The Page
		if ("product_presentation".equals(requestContext.getThisComponent().getName())) {
			context.getPageScopeData().put(RuntimePage.PAGESCOPE_KEYWORDS, pageKeyword);
			context.getPageScopeData().put(RuntimePage.PAGESCOPE_DESCRIPTION, pageDescription);
		}
	}

	/**
	 * Method to return data on a product given a language code via cookie in
	 * request and UPC from a request parameter
	 * 
	 * @param requestContext
	 * @return XMLDocument
	 */
	public Document listProductDetails(final RequestContext requestContext, final String mode) {
		context = requestContext;
		FormatUtils.pFormat("ENVIRONMENT:{0}", this.getTeamsiteEnvironment().getEnvironment());
		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());
		param.setMode(mode);
		param.setRepo(this.getNikonRepository());
		return this.listProductDetails(param, requestContext);
	}

	/**
	 * Method to take a product id and language_country code combination create
	 * the relevant parameter class and call the underlying service method to
	 * return the dom4j.Dcoument. Note this needs adding to the caching strategy
	 * 
	 * @param productId
	 * @param siteLanguageCountryCode
	 * @return
	 */
	public Document listProductDetailsFromIdCCode(final String productId, final String siteLanguageCountryCode,
			final RequestContext requestContext) {
		// Create the parameter class
		final HBN8QueryParamDTO param = new HBN8QueryParamDTO();
		param.setProductId(productId);
		param.setNikonLocale(siteLanguageCountryCode);
		param.setSiteCountryCode(FormatUtils.countryCode(param.getNikonLocale()));
		param.setTeamsiteEnvironment(this.getTeamsiteEnvironment());

		final ProductDTO prod = this.getNikonHBN8DAOManager().listProductDetails(param);
		return this._listProductDetailsDoc(requestContext, prod, null);
	}

	public Document listProductMetaDataFromDCRPath(final RequestContext requestContext) {

		context = requestContext;
		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());
		final ProductDTO prod = this.getNikonHBN8DAOManager().listProductMetaDataFromDCRPath(param);
		final Document doc = Dom4jUtils.newDocument();
		final Element root = doc.addElement("metadata");
		this.createProductXML(root, "Product", null, null, prod);

		return doc;
	}

	public ProductDTO retrieveProductDTO(final String productId, final RequestContext requestContext) {

		final HBN8QueryParamDTO param = new HBN8QueryParamDTO(requestContext, this.getTeamsiteEnvironment());
		param.setProductId(productId);
		param.setTeamsiteEnvironment(this.getTeamsiteEnvironment());

		log.debug("retrieveProductDTO: Get Product DTO");

		final ProductDTO prod = this.getNikonHBN8DAOManager().listProductDetails(param);

		return prod;
	}

	/**
	 * Helper method to return the contents of a DCR as {@link Document}
	 * 
	 * @param requestContext
	 * @param path
	 * @return
	 */
	// private Document dcrToXML(RequestContext requestContext, String path)
	// {
	// Document retDoc = null;
	// try {
	// FileDALIfc fileDal = requestContext.getFileDAL();
	// log.debug(FormatUtils.mFormat("Looking for DCR {0}", (fileDal.getRoot() +
	// path)));
	// java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + path);
	//
	// //declare doc for return of headings
	//
	// //add is stream to xml
	// retDoc = Dom4jUtils.newDocument(is);
	// } catch (Exception e) {
	// retDoc = Dom4jUtils.newDocument("error");
	// retDoc.getRootElement().setText(e.getMessage());
	// }
	// finally
	// {
	// return retDoc;
	// }
	// }

	private List<String> updateOtherProduct(final ProductDTO prod, final List<ProductDTO> list,
			final List<String> removeList, final String type, final HBN8QueryParamDTO param) throws IOException {
		final ArrayList<String> updatedFiles = new ArrayList<String>();
		for (final ProductDTO dto : list) {
			final Properties prop = new Properties();
			final InputStream is = param.getRepo().retrieveContent("product",
					dto.getProdId() + "_relationship.properties", param.getNikonLocale());
			if (is != null) {
				prop.load(is);
				is.close();
			}

			String listOfProd = prop.getProperty(type);
			if (listOfProd == null) {
				listOfProd = "";
			}
			final String[] prods = listOfProd.split(",");
			boolean found = false;
			for (final String a : prods) {
				if (a.equals(prod.getProdId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				if ("".equals(listOfProd)) {
					listOfProd = prod.getProdId();
				} else {
					listOfProd = listOfProd + "," + prod.getProdId();
				}
				prop.setProperty(type, listOfProd);
				final OutputStream os = param.getRepo().writeContent("product",
						dto.getProdId() + "_relationship.properties", param.getNikonLocale());
				prop.store(os, "Relationship");
				os.flush();
				os.close();
				updatedFiles.add(dto.getProdId() + "_relationship.properties|" + dto.getPath());
			}
		}

		for (final String removeId : removeList) {
			final Properties prop = new Properties();
			final InputStream is = param.getRepo().retrieveContent("product", removeId + "_relationship.properties",
					param.getNikonLocale());
			if (is != null) {
				prop.load(is);
				is.close();
			}
			String listOfProd = prop.getProperty(type);
			if (listOfProd == null) {
				listOfProd = "";
			}
			final String[] prods = listOfProd.split(",");
			listOfProd = "";
			boolean changed = false;
			for (final String a : prods) {
				if (a.equals(prod.getProdId())) {
					changed = true;
				} else {
					if ("".equals(listOfProd)) {
						listOfProd = a;
					} else {
						listOfProd = listOfProd + "," + a;
					}
				}
			}
			if (changed) {
				prop.setProperty(type, listOfProd);
				final OutputStream os = param.getRepo().writeContent("product", removeId + "_relationship.properties",
						param.getNikonLocale());
				prop.store(os, "Relationship");
				os.flush();
				os.close();
				final String oldId = param.getProductId();
				try {
					param.setProductId(removeId);
					final String dcrPath = this.getNikonHBN8DAOManager().getProductDCRByIdAndLocale(param);
					if (!"".equals(dcrPath)) {
						updatedFiles.add(removeId + "_relationship.properties|" + dcrPath);
					} else {
						log.error("cannot find dcrPath for removeId [" + removeId + "]");
					}
				} catch (final Exception e) {
					log.error("Exception while try to determine the dcrPath for removeId [" + removeId + "]");
				} finally {
					param.setProductId(oldId);
				}

			}
		}

		return updatedFiles;

	}
}
