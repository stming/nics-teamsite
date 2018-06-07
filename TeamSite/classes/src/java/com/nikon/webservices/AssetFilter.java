package com.nikon.webservices;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.*;
import com.interwoven.livesite.model.EndUserSite;
import com.interwoven.livesite.runtime.RequestContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AssetFilter extends com.nikon.utils.SQLCSNikon{

	private static Log LOG = LogFactory.getLog(AssetFilter.class);


	public Document getAssetsByCategory(RequestContext context)  {

		Document retdoc;

        String source = context.getParameterString("source");
        if(null == source){
                return ExternalUtils.newErrorDocument("executeSqlQuery: Parameter 'source' not found.");
        }

        String cat = context.getParameterString("category");
        EndUserSite site = context.getSite();
        String locale = site.getName().replace("_IAM", "");

        Integer set = Integer.parseInt(context.getParameterString("set"));
        if(null == set){
            return ExternalUtils.newErrorDocument("executeSqlQuery: Parameter 'set' not found.");
        }
        Integer images_per_page = Integer.parseInt(context.getParameterString("images_per_page"));
        if(null == images_per_page){
            return ExternalUtils.newErrorDocument("executeSqlQuery: Parameter 'images_per_page' not found.");
        }

        Integer startrecord = (images_per_page*set)-images_per_page+1;
        Integer endrecord = (images_per_page*set);

		String BASESQL = new String();

		if (source.equalsIgnoreCase("flickr")) {

			BASESQL += " WITH OrderedResults AS ( ";
			BASESQL += " select RawData.*, ROW_NUMBER() OVER (order by MediaOrder desc) as rowNumber from ";
			BASESQL += " (select communityAssetOverride.*, ";
			BASESQL += " OverrideCameraData.supplementalValue as cameraModel, ";
			BASESQL += " ISNULL(overrideCountryData.supplementalValue, 'unknown') as country, ";
			BASESQL += " OverrideUserData.supplementalValue as username, ";
			BASESQL += " OverrideAuthorData.supplementalValue as userid, ";
			BASESQL += " CONVERT(int, '2147483647') as MediaOrder ";

			BASESQL += " from communityAssetOverride ";

			BASESQL += " left join communityAssetOverrideSupp as OverrideCameraData on (communityAssetOverride.vpath = OverrideCameraData.vpath and OverrideCameraData.supplementalKey = 'exif/Model') ";
			BASESQL += " left join communityAssetOverrideSupp as OverrideCountryData on (communityAssetOverride.vpath = OverrideCountryData.vpath and OverrideCountryData.supplementalKey = 'location/country') ";
			BASESQL += " left join communityAssetOverrideSupp as OverrideUserData on (communityAssetOverride.vpath = OverrideUserData.vpath and OverrideUserData.supplementalKey = 'photo/owner/ATTR_username') ";
			BASESQL += " left join communityAssetOverrideSupp as OverrideAuthorData on (communityAssetOverride.vpath = OverrideAuthorData.vpath and OverrideAuthorData.supplementalKey = 'photo/owner/ATTR_nsid') ";
			BASESQL += " left join dbo.GetLocalisedCommunityAssetOrder('"+locale+"') as fCommunityAssetOrder on communityAssetOverride.id = fCommunityAssetOrder.id ";

			BASESQL += " where communityAssetOverride.vpath like '%"+source+"%' and communityAssetOverride.vpath like '%"+locale+"%' ";
			BASESQL += " and (not(communityAssetOverride.hidden = 'true') or communityAssetOverride.hidden is null)";


	        if(!(null == cat)&&(cat.length()>0)){

	        	BASESQL += "and communityAssetOverride.vpath in ( select communityAssetOverrideCategories.vpath from communityAssetOverrideCategories where ";
	        	BASESQL += " "+getCategoryRequest(context, "category");
	        }


			BASESQL += " union ";
			BASESQL += " select communityAsset.*, ";
			BASESQL += " CameraData.supplementalValue as cameraModel,  ";
			BASESQL += " ISNULL(countryData.supplementalValue, 'unknown') as country,  ";
			BASESQL += " UserData.supplementalValue as username,  ";
			BASESQL += " AuthorData.supplementalValue as userid, ";
			BASESQL += " CONVERT(int, communityAsset.modifyDate) as MediaOrder ";

			BASESQL += " from communityAsset ";

			BASESQL += " left join communityAssetSupp as CameraData on (communityAsset.vpath = CameraData.vpath and CameraData.supplementalKey = 'exif/Model') ";
			BASESQL += " left join communityAssetSupp as CountryData on (communityAsset.vpath = CountryData.vpath and CountryData.supplementalKey = 'location/country') ";
			BASESQL += " left join communityAssetSupp as UserData on (communityAsset.vpath = UserData.vpath and UserData.supplementalKey = 'photo/owner/ATTR_username') ";
			BASESQL += " left join communityAssetSupp as AuthorData on (communityAsset.vpath = AuthorData.vpath and AuthorData.supplementalKey = 'photo/owner/ATTR_nsid') ";
			BASESQL += " left join dbo.GetLocalisedCommunityAssetOrder('"+locale+"') as fCommunityAssetOrder on communityAsset.id = fCommunityAssetOrder.id ";

			BASESQL += " where communityAsset.vpath like '%"+source+"%' ";
			BASESQL += " and communityAsset.id not in ( select communityAssetOverride.id from communityAssetOverride where communityAssetOverride.vpath like '%"+locale+"%') ";

			if(!(null == cat)&&(cat.length()>0)){

	        	BASESQL += "and communityAsset.vpath in ( select communityAssetCategories.vpath from communityAssetCategories where ";
	        	BASESQL += " "+getCategoryRequest(context, "category");
	        }

			BASESQL += " ) as RawData ";
			BASESQL += " ) ";
			BASESQL += "SELECT *, (select count(*) from OrderedResults) as totalrows ";
			BASESQL += "FROM OrderedResults ";
			BASESQL +=" WHERE rowNumber between "+startrecord.toString()+" and "+endrecord.toString()+" ";
		}

		if (source.equalsIgnoreCase("youtube")){
			BASESQL += " WITH OrderedResults AS ( ";

			BASESQL += " select RawData.*, ROW_NUMBER() OVER (order by MediaOrder desc) as rowNumber from ";
			BASESQL += " (select 	communityAssetOverride.*,  ";
			BASESQL += " 	CONVERT(int, '2147483647') as MediaOrder ";
			
			BASESQL += " from communityAssetOverride ";
			BASESQL += " left join dbo.GetLocalisedCommunityAssetOrder('"+locale+"') as fCommunityAssetOrder on communityAssetOverride.id = fCommunityAssetOrder.id ";
			
			BASESQL += " where communityAssetOverride.vpath like '%"+source+"%' and communityAssetOverride.vpath like '%"+locale+"%' ";
			BASESQL += " and (not(communityAssetOverride.hidden = 'false') or communityAssetOverride.hidden is null)";

	        if(!(null == cat)&&(cat.length()>0)){

	        	BASESQL += "and communityAssetOverride.vpath in ( select communityAssetOverrideCategories.vpath from communityAssetOverrideCategories where ";
	        	BASESQL += " "+getCategoryRequest(context, "category");
	        }

			BASESQL += " union ";
			
			BASESQL += " select 	communityAsset.*,  ";
			BASESQL += " 	CONVERT(int, communityAsset.modifyDate) as MediaOrder ";
			
			BASESQL += " from communityAsset ";
			BASESQL += " left join dbo.GetLocalisedCommunityAssetOrder('"+locale+"') as fCommunityAssetOrder on communityAsset.id = fCommunityAssetOrder.id ";
			
			BASESQL += " where communityAsset.vpath like '%"+source+"%' ";
			BASESQL += " and communityAsset.id not in ( select communityAssetOverride.id from communityAssetOverride where communityAssetOverride.vpath like '%"+locale+"%') ";

	        if(!(null == cat)&&(cat.length()>0)){

	        	BASESQL += "and communityAsset.vpath in ( select communityAssetCategories.vpath from communityAssetCategories where ";
	        	BASESQL += " "+getCategoryRequest(context, "category");
	        }

			BASESQL += " ) as RawData ";
			
			BASESQL += " ) ";
			BASESQL += "SELECT *, (select count(*) from OrderedResults) as totalrows ";
			BASESQL += "FROM OrderedResults ";
			BASESQL +=" WHERE rowNumber between "+startrecord+" and "+endrecord+" ";


		}

        String pool = context.getParameterString("Pool");
        if(null == pool){
                return ExternalUtils.newErrorDocument("executeSqlQuery: Parameter 'pool' not found.");
        }


        String sql = BASESQL;
        // add the category sql to the final query if there are any



        // add the camera sql to the final query if there are any
        String cam = context.getParameterString("camera");
        if(!(null == cam)&&(cam.length()>0)){
        	if(!(null == cat)){
        		sql += " and ";
        	}
        	sql += " communityAsset.vpath in (select communityAssetSupp.vpath from communityAssetSupp where ";
        	sql += " "+getCameraRequest(context, "camera");
        }



        LOG.debug("IAM SQL query = "+sql);
        //make the query
		Document sqldoc = runCS(pool,sql);
		//get the rules
		Document dcrdoc = getRule(context);

		//now search the xml doc for all the camera model on each pic
		List assets = sqldoc.selectNodes("//Row");
		ListIterator itassets = assets.listIterator();

		//for each camera found look up the rule in the dcrdoc

		String promotion = "";

        while (itassets.hasNext()) {

			Node tempnode = (Node) itassets.next();

			// JW
			//LOG.debug("Iteration Node: " + tempnode.toString());

            promotion = ruleLookup(context,dcrdoc,tempnode.valueOf("./cameraModel/."));
            Element e = (Element)tempnode;
            Document promodoc = getPromo(context, promotion);

            Node promodocroot = promodoc.selectSingleNode("//promotion_content").detach();

            e.addElement("promotion").add(promodocroot);

        }

		return sqldoc;

	}


	public String ruleLookup(RequestContext context, Document dcrdoc, String camera){
		
		LOG.debug("loading rules");
		String promotion = "";
		//get the rules


		//if a rule match is found add the relevant promotion
		String rulematch = "n";
		List rules = dcrdoc.selectNodes("//promotion_rules/rules");
		//loop through all the rules
		LOG.debug("there are " +rules.size()+" rules");
		for (int i = 0; i < rules.size(); i++) {
			//grab the rule target and contains values
			Node nrule = (Node)rules.get(i);
			Node ntarget = nrule.selectSingleNode("./target");
			Node ncontains = nrule.selectSingleNode("./contains");
			//check the target = model and the camera is like the contains
			//if we do then grab the promo dcr
			LOG.debug("camera = "+ camera);
			LOG.debug("rule text = "+ncontains.getText());
			if((ntarget.getText().equalsIgnoreCase("model"))&&(camera.matches("(?i).*"+ncontains.getText()+".*"))){
				Node npromodcr = nrule.selectSingleNode("./promotion");
				promotion = npromodcr.getText();
				LOG.debug("IAM promotion = "+promotion);
				rulematch = "y";
				break;
			}
		}
		//else use the fallback rule
		if(rulematch.equalsIgnoreCase("n")){
			LOG.debug("IAM fallback promotion being used = "+promotion);
			Node fallbacknode = dcrdoc.selectSingleNode("//fallback");
			promotion = fallbacknode.getText();
		}

		return promotion;


	}

	public Document openDCR(RequestContext context, String dcr){

		Document doc = Dom4jUtils.newDocument("<Rule/>");
		Document dcrdoc = null;
		String rootpath = context.getFileDal().getRoot();

		//get the path to the dcr
		String dcrpath = new String();
		dcrpath = rootpath+"/"+dcr;
		LOG.debug("IAM dcr path = "+dcrpath);
		File dcrfile = null;
        try {
            dcrfile = new File(dcrpath);
        } catch (Exception e) {
        	LOG.error("Error getting the dcr "+ dcrfile.toString());
        }
        LOG.debug("found the dcr "+ dcrfile.toString());

		//parse as XML
        SAXReader reader = new SAXReader();

        //disable dtd parsing...
        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                if (systemId.contains(".dtd")) {
                    return new InputSource(new StringReader(""));
                } else {
                    return null;
                }
            }
        });


        try{
        	dcrdoc = reader.read(dcrfile);
        }catch (DocumentException de){
        	LOG.error("Error reading the dcr in as XML "+ de.toString());
        }
        Element eDCR = doc.getRootElement().addElement("dcr");
        //add each doc to the final return doc.
        org.dom4j.Node node = null;
        try{
        	node = dcrdoc.getRootElement();
        }catch (Exception e){
        	LOG.error("Error selecting the node "+ e.toString());
        }

        eDCR.add(node);

		return doc;

	}


    public String getCategoryRequest(RequestContext context, String datum) {
        String result = "";
        Object o = context.getParameters().get(datum);
        // if the datum gives an array

        if (o instanceof String) {
            //it's a string
            result = " category='"+(String) o+"')";
        } else {
        	List vals = null;
            try{
            	vals = (List) o;
        	}catch(Exception e){
        	}
            String newString = "";
            for (int i = 0; i < vals.size(); i++) {
            	if (i==0){
            		newString += " category='"+(String) vals.get(i)+"'";
            	}else{
            		newString += " OR category='"+(String) vals.get(i)+"'";
            	}
            }
            result = newString+")";

        }

        return result;

    }

    public String getCameraRequest(RequestContext context, String datum) {
        String result = "";
        Object o = context.getParameters().get(datum);
        // if the datum gives an array

        if (o instanceof String) {
            //it's a string
            result = " communityAssetSupp.supplementalValue='"+(String) o+"')";

        } else {

        	List vals = null;
            try{
            	vals = (List) o;
        	}catch(Exception e){
        		LOG.error("IAN ERROR getting images by cameras "+e.toString());
        	}
            String newString = "";

            for (int i = 0; i < vals.size(); i++) {
            	if (i==0){
            		newString += " communityAssetSupp.supplementalValue='"+(String) vals.get(i)+"'";
            	}else{
            		newString += " OR communityAssetSupp.supplementalValue='"+(String) vals.get(i)+"'";
            	}
            	LOG.debug("IAN camera = "+(String) vals.get(i));
            }
            result = newString+")";

        }

        return result;

    }

	public Document getRule(RequestContext context) {

		Document doc;
		String rulepath = context.getParameterString("Rule");
		doc = openDCR(context,rulepath);
		return doc;

	}

	public Document getPromo(RequestContext context, String dcr) {

		Document doc;
		doc = openDCR(context,dcr);
		return doc;

	}

}
