<%-- 
    Document   : NikonDealerWebApp main index
    Created on : 20-Nov-2012, 15:48:49
    Author     : Mike
--%>

<%@page import="com.interwoven.livesite.spring.web.WebApplicationContextUtils"%>
<%@page import="com.interwoven.teamsite.nikon.dealerfinder.HibernateUtil"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.io.*"%>

<%@page import="org.json.*"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>

<%@page import="com.interwoven.teamsite.nikon.dealerfinder.AdditionalData"%>
<%@page import="com.interwoven.teamsite.nikon.dealerfinder.search.AdvancedSearch.DealerCol"%>
<%@page import="com.interwoven.teamsite.nikon.dealerfinder.search.AdvancedSearch"%>
<%@page import="com.interwoven.teamsite.nikon.dealerfinder.search.BasicSearch"%>

<%@page import="com.interwoven.teamsite.nikon.dealerfinder.Dealer"%>
<%@page import="com.interwoven.teamsite.nikon.dealerfinder.DealerDAO"%>

<%@ page import="com.interwoven.cssdk.common.*" %> 
<%@ page import="com.interwoven.cssdk.access.*" %> 
<%@ page import="com.interwoven.cssdk.filesys.*" %> 

<%@page contentType="text/html; charset=UTF-8"%>

<%!    Logger oLogger = LoggerFactory.getLogger("Controller");
%>

<%!    public class ErrorResponse extends JSONObject
    {

        public String sMessage = "";
        public boolean bError = false;

		public String toString()
        {
            try
            {
                JSONObject oError = new JSONObject();
                oError.put("message", sMessage);
                oError.put("error", new Boolean(bError));
                return oError.toString();
            }
            catch (Exception e)
            {
            }
            return null;
        }
    }
%>

<%! public String getPostBody(InputStream oInputStream, Enumeration<String> oParameters) throws Exception
    {
        String sParameters = null;
        if (oInputStream != null)
        {
            BufferedReader oStream = new BufferedReader(new InputStreamReader(oInputStream));
            if (oStream != null)
            {
                StringBuffer oBuffer = new StringBuffer();
                String sLine = null;
                while ((sLine = oStream.readLine()) != null)
                {
                    oBuffer.append(sLine);
                }
                sParameters = oBuffer.toString();
            }
            
            if (sParameters == null || sParameters.equals("") &&  oParameters !=null)
            {
                //the calling API may have sent it as a malformed parameter
                while (oParameters.hasMoreElements())
                {
                    String sParameter = (String) oParameters.nextElement();
                    if (sParameter.startsWith("{"))
                    {
                        sParameters = sParameter;
                        break;
                    }
                }
            }
        }
        return sParameters;
    }

    public void addErrorResponse(JSONObject obj, boolean bError, String sMessage) throws Exception
    {
        ErrorResponse oErrorResponse = new ErrorResponse();
        oErrorResponse.bError = bError;
        oErrorResponse.sMessage = sMessage == null ? "SUCCESS" : sMessage;
        obj.put("Response", oErrorResponse);
    }

    public void doUpdate(HttpServletRequest request, JspWriter out) throws Exception
    {
        oLogger.info("Saving dealer");

        boolean bError = true;
        String sErrorMessage = null;

        //retrieve the values from the query
        String sJSON = getPostBody(request.getInputStream(), request.getParameterNames());
		oLogger.info("JSON object fetched");                
                
        try
        {
            JSONObject oJSON = new JSONObject(sJSON);

            if (oJSON != null)
            {
                //pull back the fields from the JSON we've been sent
                Dealer oDealer = new Dealer();

				oDealer.setStatus((String) oJSON.get("status"));
                oDealer.setId(new Long((String) oJSON.get("id")).longValue());
                oDealer.setName((String) oJSON.get("name"));
                oDealer.setDescription((String) oJSON.get("description"));
                oDealer.setStreet((String) oJSON.get("street"));
                oDealer.setTown((String) oJSON.get("town"));
                oDealer.setState((String) oJSON.get("state"));
                oDealer.setPostCode((String) oJSON.get("post_code"));
                oDealer.setCountry((String) oJSON.get("country"));
                oDealer.setCountryCode((String) oJSON.get("country_code"));
                oDealer.setLongitude(new Float((String) oJSON.get("longitude")).floatValue());
                oDealer.setLatitude(new Float((String) oJSON.get("latitude")).floatValue());
                oDealer.setTel((String) oJSON.get("tel"));
                oDealer.setFax((String) oJSON.get("fax"));
                oDealer.setEmail((String) oJSON.get("email"));
                oDealer.setUrl((String) oJSON.get("url"));
                oDealer.setAuthor((String) oJSON.get("user"));
                oDealer.setGroup((String) oJSON.get("groups"));
                oDealer.setOpeningHours((String) oJSON.get("opening_hours"));
                
                Set<AdditionalData> additionalData = new HashSet<AdditionalData>();
                
                JSONArray additionalDataArray = oJSON.getJSONArray( "additional" );
                for ( int i = 0; i < additionalDataArray.length(); i++ )
                {
                	AdditionalData data = new AdditionalData();
                	data.setDealer(oDealer);
                	data.setId( oDealer.getId() );
                	data.setPk( oDealer.getId() + "__" + additionalDataArray.getJSONObject( i ).getString("fieldID") );
                	data.setFieldId( new Long( additionalDataArray.getJSONObject( i ).getString("fieldID") ) );
                	data.setFieldValue( additionalDataArray.getJSONObject( i ).getString("fieldValue") );
                	
                	additionalData.add( data );
                }  
                
                oDealer.setAdditionalData( additionalData );
                
                DealerDAO oDealerDAO = new DealerDAO();
                oDealerDAO.updateDealer(oDealer);

				oLogger.info("Dealer updated");
                            
                bError = false;
                sErrorMessage = "SUCCESS";
            }
			else
			{
				sErrorMessage = "JSON object null";
			}
        }
        catch (Exception e)
        {
            sErrorMessage = e.getMessage();
			oLogger.error( e.toString() );
        }

        JSONObject obj = new JSONObject();
        addErrorResponse(obj, bError, sErrorMessage);
        out.print(obj);
        out.flush();
    }

    public void doCreate(HttpServletRequest request, JspWriter out) throws Exception
    {
        oLogger.info("create dealer");
        boolean bError = true;
        String sErrorMessage = null;
        long lDealerId = -1;

        //retrieve the values from the query
        String sJSON = getPostBody(request.getInputStream(), request.getParameterNames());
        try
        {
            JSONObject oJSON = new JSONObject(sJSON);

            if (oJSON != null)
            {
                Dealer oDealer = new Dealer();
                oDealer.setStatus((String) oJSON.get("status"));
                oDealer.setName((String) oJSON.get("name"));
                oDealer.setDescription((String) oJSON.get("description"));
                oDealer.setStreet((String) oJSON.get("street"));
                oDealer.setTown((String) oJSON.get("town"));
                oDealer.setState((String) oJSON.get("state"));
                oDealer.setPostCode((String) oJSON.get("post_code"));
                oDealer.setCountry((String) oJSON.get("country"));	  
                oDealer.setCountryCode((String) oJSON.get("country_code"));
                oDealer.setLongitude(new Float((String) oJSON.get("longitude")).floatValue());
                oDealer.setLatitude(new Float((String) oJSON.get("latitude")).floatValue());
                oDealer.setTel((String) oJSON.get("tel"));
                oDealer.setFax((String) oJSON.get("fax"));
                oDealer.setEmail((String) oJSON.get("email"));
                oDealer.setUrl((String) oJSON.get("url"));
                oDealer.setOpeningHours((String) oJSON.get("opening_hours"));
				oDealer.setAuthor((String) oJSON.get("user"));
                oDealer.setGroup((String) oJSON.get("groups"));
                
                DealerDAO oDealerDAO = new DealerDAO();
                lDealerId = oDealerDAO.createDealer(oDealer);

                if (lDealerId > 0l)
                {
                    bError = false;
                    sErrorMessage = "SUCCESS";
                }
            }
        }
        catch (Exception e)
        {
            sErrorMessage = e.getMessage();
        }

        JSONObject obj = new JSONObject();
        obj.put("ID", lDealerId);
        addErrorResponse(obj, bError, sErrorMessage);
        out.print(obj);
        out.flush();
    }

    public void doRemove(HttpServletRequest request, JspWriter out) throws Exception
    {
        boolean bError = true;
        String sErrorMessage = null;

        try
        {
            //retrieve the id
            String sJSON = getPostBody(request.getInputStream(), request.getParameterNames());
            JSONArray oJSONArray = new JSONArray(sJSON);
            List<Long> olIDs = new ArrayList<Long>();
            if (oJSONArray != null)
            {
                for (int i = 0; i < oJSONArray.length(); i++)
                {
                    olIDs.add(Long.parseLong(oJSONArray.getString(i)));
                }
            }

            if (olIDs.size() > 0)
            {
                DealerDAO oDealerDAO = new DealerDAO();
                oDealerDAO.removeDealer(olIDs);
                sErrorMessage = "SUCCESS";
                bError = false;
            }
        }
        catch (Exception e)
        {
            sErrorMessage = e.getMessage();
        }

        JSONObject obj = new JSONObject();
        addErrorResponse(obj, bError, sErrorMessage);
        out.print(obj);
        out.flush();
    }

    public void doPublish(HttpServletRequest request, JspWriter out) throws Exception
    {
        boolean bError = true;
        String sErrorMessage = null;

        try
        {
            String sJSON = getPostBody(request.getInputStream(), request.getParameterNames());
            JSONObject oJSONObject= new JSONObject(sJSON);
            List<Long> olIDs = new ArrayList<Long>();
            if (oJSONObject != null)
            {
                JSONArray oJSONIDs = oJSONObject.getJSONArray("ids");
                if (oJSONIDs !=null)
                {
                    for (int i = 0; i < oJSONIDs.length(); i++)
                    {
                        JSONObject oID = (JSONObject) oJSONIDs.get(i);
                        if (oID !=null)
                        {
                            olIDs.add(((long)((Integer)oID.get("id")).intValue()));
                        }
                    }
                }
            }

            if (olIDs.size() > 0)
            {
                DealerDAO oDealerDAO = new DealerDAO();
                oDealerDAO.publishDealer(olIDs);
                sErrorMessage = "SUCCESS";
                bError = false;
            }
        }
        catch (Exception e)
        {
            sErrorMessage = e.getMessage();
        }

        JSONObject obj = new JSONObject();
        addErrorResponse(obj, bError, sErrorMessage);
        out.print(obj);
        out.flush();
    }

    public void doDelete(HttpServletRequest request, JspWriter out) throws Exception
    {
        boolean bError = true;
        String sErrorMessage = null;

        try
        {
            // Retrieve the id
            String sJSON = getPostBody(request.getInputStream(), request.getParameterNames());
            JSONObject oJSONObject= new JSONObject(sJSON);
            List<Long> olIDs = new ArrayList<Long>();
            if (oJSONObject != null)
            {
                JSONArray oJSONIDs = oJSONObject.getJSONArray("ids");
                if (oJSONIDs !=null)
                {
                    for (int i = 0; i < oJSONIDs.length(); i++)
                    {
                        JSONObject oID = (JSONObject) oJSONIDs.get(i);
                        if (oID !=null)
                        {
                            olIDs.add(((long)((Integer)oID.get("id")).intValue()));
                        }
                    }
                }
            }

            if (olIDs.size() > 0)
            {
                DealerDAO oDealerDAO = new DealerDAO();
                oDealerDAO.deleteDealer(olIDs);
                sErrorMessage = "SUCCESS";
                bError = false;
            }
        }
        catch (Exception e)
        {
            sErrorMessage = e.getMessage();
        }

        JSONObject obj = new JSONObject();
        addErrorResponse(obj, bError, sErrorMessage);
        out.print(obj);
        out.flush();
    }

    public void doSearch(HttpServletRequest request, JspWriter out) throws Exception
    {
        boolean bError = true;
        String sErrorMessage = null;
        List<Dealer> lDealers = null;

		oLogger.info( "Searching" );

        //retrieve the values from the query
        String sJSON = getPostBody(request.getInputStream(), request.getParameterNames());
        
        DealerDAO oDealerDAO = new DealerDAO();

        try
        {
            //pull out the values from the JSON
            String sMethod = "";
            String sSearchText = "";
            String sField = "";
            String sGroup = "";
            String sName = "";
            String sCountry = "";
            String sTown = "";
			String sState = "";
            String sCountryCode = "";
            String sStreet = "";
			String sDescription = "";
			String sPostCode = "";
			String sStatus = "";
			String sFrom = "";
			String sTo = "";
			String sAuthor = "";
            
            JSONObject oJSON = new JSONObject(sJSON);
            if (oJSON !=null)
            {
                JSONArray oData = (JSONArray) oJSON.get("data");
                if (oData !=null)
                {
                    for (int i=0; i<oData.length(); i++)
                    {
                        JSONObject oElement = (JSONObject)oData.get(i);
                        if (oElement !=null)
                        {
                            String sFieldName = (String) oElement.get("name");
                            String sFieldValue = (String) oElement.get("value");
                            if (sFieldName.equalsIgnoreCase("method"))
                            {
                                sMethod = sFieldValue;
                            }
                            else if (sFieldName.equalsIgnoreCase("q"))
                            {
                                sSearchText = sFieldValue;
                            }
                            else if (sFieldName.equalsIgnoreCase("field"))
                            {
                                sField = sFieldValue;
                            }
                            else if (sFieldName.equalsIgnoreCase("group"))
                            {
                                sGroup = sFieldValue;
                            }
                            else if (sFieldName.equalsIgnoreCase("name"))
                            {
                                sName = sFieldValue;
                            }
                            else if (sFieldName.equalsIgnoreCase("country"))
                            {
                                sCountry = sFieldValue;
                            }
                            else if (sFieldName.equalsIgnoreCase("countryCode"))
                            {
                                sCountryCode = sFieldValue;
                            }
                            else if (sFieldName.equalsIgnoreCase("street"))
                            {
                                sStreet = sFieldValue;
                            }
							else if (sFieldName.equalsIgnoreCase("town"))
                            {
                                sTown = sFieldValue;
                            }
							else if (sFieldName.equalsIgnoreCase("description"))
                            {
                                sDescription = sFieldValue;
                            }
							else if (sFieldName.equalsIgnoreCase("state"))
                            {
                                sState = sFieldValue;
                            }
							else if (sFieldName.equalsIgnoreCase("postCode"))
                            {
                                sPostCode = sFieldValue;
                            }
							else if (sFieldName.equalsIgnoreCase("status"))
                            {
								if (!sFieldValue.equalsIgnoreCase("all")) {
									sStatus = sFieldValue;
								}
                            }
							else if (sFieldName.equalsIgnoreCase("from"))
                            {
								sFrom = sFieldValue;
							}
							else if (sFieldName.equalsIgnoreCase("to"))
                            {
								sTo = sFieldValue;
							}
							else if (sFieldName.equalsIgnoreCase("author"))
                            {
								sAuthor = sFieldValue;
							}
                        }
                    }
                }
            }

            
            if (sMethod.equalsIgnoreCase("basicSearch"))
            {
                BasicSearch oBasicSearch = new BasicSearch();
                oBasicSearch.setSearchText(sSearchText);
                oBasicSearch.setFieldName(sField);
				oBasicSearch.setGroups(Arrays.asList(sGroup.split(",")));

                lDealers = oDealerDAO.doBasicSearch(oBasicSearch);
            }
            else if (sMethod.equalsIgnoreCase("advSearch"))
            {
                AdvancedSearch oAdvSearch = new AdvancedSearch();

                if (sName != null && sName.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.name, sName);
                }
                if (sCountry != null && sCountry.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.country, sCountry);
                }
                if (sTown != null && sTown.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.town, sTown);
                }
                if (sStreet != null && sStreet.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.street, sStreet);
                }
				if (sDescription != null && sDescription.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.description, sDescription);
                }
				if (sState != null && sState.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.state, sState);
                }
				if (sPostCode != null && sPostCode.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.postCode, sPostCode);
                }
				if (sStatus != null && sStatus.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.status, sStatus);
                }
				if (sFrom != null && sFrom.length() > 0)
                {
					Date fromDate = new Date(sFrom);
					
                    oAdvSearch.addConstraint(DealerCol.from, fromDate.getTime() + "");
                }
				if (sTo != null && sTo.length() > 0)
                {
					Date toDate = new Date(sTo);
					
                    oAdvSearch.addConstraint(DealerCol.to, toDate.getTime() + "");
                }
				if (sAuthor != null && sAuthor.length() > 0)
                {
                    oAdvSearch.addConstraint(DealerCol.author, sAuthor);
                }
				
                lDealers = oDealerDAO.doAdvancedSearch(oAdvSearch);
            }
 
            bError = false;
            sErrorMessage = "SUCCESS";
        }
        catch (JSONException e)
        {
            //TODO nothing sent, just return everything for now
            oLogger.error("JSONException "+e.getMessage());
            oLogger.error("defaulting to listing all dealers");
            lDealers = oDealerDAO.listAllDealers();
            bError = false;
            sErrorMessage = "SUCCESS";
        }
        catch (Exception e)
        {
            sErrorMessage = e.getMessage();
        }

        //return the data
        JSONObject obj = new JSONObject();
        addErrorResponse(obj, bError, sErrorMessage);

        //need to convert the Dealer object to the JSON representation
        //we can't rely on the toString function in Dealer as that's used by
        //hiberate so we need to manually serialise each object
        JSONArray olDealers = new JSONArray();
        for (Dealer oDealer : lDealers)
        {
            JSONArray oJSONDealer = new JSONArray();
            oJSONDealer.put(oDealer.getId());
            oJSONDealer.put(oDealer.getName());
            oJSONDealer.put(oDealer.getDescription());
            oJSONDealer.put(oDealer.getStreet() +"," +oDealer.getTown() +"," +oDealer.getCountry());
			
			Date date = new Date(oDealer.getModifiedDate());
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy"); 
			
            oJSONDealer.put(sdf.format(date));
            
			oJSONDealer.put(oDealer.getStatus());
            oJSONDealer.put(oDealer.getABFCode());
            oJSONDealer.put(oDealer.getLiveCode());
            olDealers.put(oJSONDealer);
        }

        obj.put("aaData", olDealers);
        out.print(obj);
        out.flush();
    }
    
    public void doGetDetail(HttpServletRequest request, JspWriter out) throws Exception
    {
        boolean bError = true;
        String sErrorMessage = null;
        Dealer oDealer = null;

        try
        {
            long lnID = -1;
            String sJSON = getPostBody(request.getInputStream(), request.getParameterNames());
            JSONObject oJSONObject= new JSONObject(sJSON);
            if (oJSONObject != null)
            {
                lnID = (long) (((Integer)oJSONObject.get("id")).intValue());
            }

            DealerDAO oDealerDAO = new DealerDAO();
            oDealer = oDealerDAO.getDealerDetail(lnID);
            if (oDealer !=null)
            {
                sErrorMessage = "SUCCESS";
                bError = false;
            }
        }
        catch (Exception e)
        {
            sErrorMessage = e.getMessage();
        }

        JSONObject obj = new JSONObject();
        addErrorResponse(obj, bError, sErrorMessage);
        
        if (oDealer !=null)
        {
            obj.put("id", oDealer.getId());
            obj.put("name", oDealer.getName());
            obj.put("street", oDealer.getStreet());
            obj.put("town", oDealer.getTown());
            obj.put("state", oDealer.getState());
            obj.put("post_code", oDealer.getPostCode());
            obj.put("country", oDealer.getCountry());
            obj.put("longitude", oDealer.getLongitude());
            obj.put("latitude", oDealer.getLatitude());
            obj.put("tel", oDealer.getTel());
            obj.put("fax", oDealer.getFax());
            obj.put("email", oDealer.getEmail());
			obj.put("url", oDealer.getUrl());
			obj.put("description", oDealer.getDescription());
            obj.put("opening_hours", oDealer.getOpeningHours());
            obj.put("author", oDealer.getAuthor());
            obj.put("group", oDealer.getGroup());
            obj.put("modified_date", oDealer.getModifiedDate());
            obj.put("status", oDealer.getStatus());
            obj.put("abf", oDealer.getABFCode());
            obj.put("prod", oDealer.getLiveCode());
            JSONArray lAdditionalJSON = new JSONArray();
            Set<AdditionalData> lAdditional = oDealer.getAdditionalData();
            if (lAdditional !=null)
            {
                for (AdditionalData oData : lAdditional)
                {
                    JSONObject oField = new JSONObject();
                    oField.put("fieldID", oData.getFieldId());
                    oField.put("fieldValue", oData.getFieldValue());
                    lAdditionalJSON.put(oField);
                }
                obj.put("additional", lAdditionalJSON);
            }
        }
        
        out.print(obj);
        out.flush();
    }

    public void doError(HttpServletRequest request, JspWriter out, String sMessage) throws Exception
    {
        System.out.println("Error " + sMessage);
        JSONObject obj = new JSONObject();
        addErrorResponse(obj, true, sMessage);
        out.print(obj);
        out.flush();
    }
	
	public void doCountries(HttpServletRequest request, JspWriter out) throws Exception
    {
		CSClient client = (CSClient) request.getAttribute("iw.csclient");
	
		String configFile = request.getParameter("config_file");
		
		CSVPath csVpath = new CSVPath(configFile);			
		CSSimpleFile file = (CSSimpleFile) client.getFile(csVpath);
					
		String xml = retrieveXML(file);
		
		out.clear();
		out.print(xml);
        out.flush();
    }
	
	//====================================================================
	// retrieveXML()
	//====================================================================
	public String retrieveXML(CSSimpleFile file) {
		
		String xml = "";
		
		BufferedInputStream in = null;
		Reader reader = null;
		
		try {
			
			in = file.getBufferedInputStream(false);
			reader = new InputStreamReader(in, "UTF-8");
			
			int bytesRead;
			
			while ((bytesRead = reader.read()) != -1) {
                
				xml += (char) bytesRead;
            }
			
		} catch (IOException ex) {
            ex.printStackTrace();
        } catch (CSAuthorizationException e) {
			e.printStackTrace();
		} catch (CSObjectNotFoundException e) {
			e.printStackTrace();
		} catch (CSExpiredSessionException e) {
			e.printStackTrace();
		} catch (CSRemoteException e) {
			e.printStackTrace();
		} catch (CSException e) {
			e.printStackTrace();
		} finally {
            
        	try {
                if (in != null)
                    in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return xml;
	}
%>


<%
    response.setHeader("Cache-Control", "no-cache");

    String sRequestType = request.getParameter("type");

    if (sRequestType != null)
    {
        if (sRequestType.equalsIgnoreCase("create"))
        {
            doCreate(request, out);
        }
        else if (sRequestType.equalsIgnoreCase("update"))
        {
            doUpdate(request, out);
        }
        else if (sRequestType.equalsIgnoreCase("remove"))
        {
            doRemove(request, out);
        }
        else if (sRequestType.equalsIgnoreCase("search"))
        {
            doSearch(request, out);
        }
        else if (sRequestType.equalsIgnoreCase("publish"))
        {
            doPublish(request, out);
        }
        else if (sRequestType.equalsIgnoreCase("detail"))
        {
            doGetDetail(request, out);
        }        
        else if (sRequestType.equalsIgnoreCase("delete"))
        {
            doDelete(request, out);
        }
		else if (sRequestType.equalsIgnoreCase("countries"))
        {
            doCountries(request, out);
        }
        else
        {
            doError(request, out, "Unknown Type");
        }
    }
    else
    {
        doError(request, out, "Missing Type");
    }
%>