<%--
    /**
     *
     * Realise - @Mike Stewart
     */
--%>

<%@ page import="com.interwoven.teamsite.nikon.dealerfinder.HibernateUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<%@ page import="com.interwoven.cssdk.common.*" %> 
<%@ page import="com.interwoven.cssdk.access.*" %> 
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.interwoven.livesite.spring.ApplicationContextUtils" %>
<%@ page import="com.interwoven.teamsite.nikon.dealerfinder.AccessUtil" %>

<%
HibernateUtil.init(ApplicationContextUtils.getApplicationContext());
CSClient client = (CSClient) request.getAttribute("iw.csclient");
List<String> userGroups = AccessUtil.getCurrentUserGroups(client);
String s_UserGroups = "";

int count = 1;

for ( String s : userGroups)
{
	s_UserGroups += s;
	
	if (count < userGroups.size()) {
	
		s_UserGroups += ",";
	}
	
	count++;
}

%>
<!DOCTYPE HTML>

<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 

<script>

// PASSED FROM TEAMSITE
var securityGroups = '<%= s_UserGroups %>';
var user = '<%= client.getCurrentUser().getNormalizedName() %>';
var securityGroupsArr = securityGroups.split(/,/);
var isMaster = <%= client.getCurrentUser().isMaster() %>;

</script>

<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.js"></script>
<script type="text/javascript" src="//maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript" src="js/gmap3.min.js"></script>
<script type="text/javascript" src="js/jquery.xml2json.js"></script>
<script type="text/javascript" src="js/tiny_mce/jquery.tinymce.js"></script>
<script type="text/javascript" src="js/$console_wrapper.js"></script>
<script type="text/javascript" src="js/dealerfinder.js"></script>



<link rel="stylesheet" type='text/css' href="css/jquery-ui.css" />
<link rel="stylesheet" type='text/css' href="css/jquery.dataTables.css" />
<link rel='stylesheet' type='text/css' href='css/dealerfinder.css'>
<link href="/iw-cc/base/styles/iw.css" type="text/css" rel="stylesheet">
<link href="/iw-cc/base/styles/custom.css" type="text/css" rel="stylesheet">

<style>

			.label_val { float: left; width: 120px; font-weight: bold; color: #000000; font-family: Verdana,sans-serif; font-size: 11px; }
			.dataTables_info { float: left; font-weight: bold; color: #000000; font-family: Verdana,sans-serif; font-size: 11px; }
			table.dataTable thead th { font-weight: bold; color: #000000; font-family: Verdana,sans-serif; font-size: 11px; }

			</style>
		
</head>

<body>

<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
	<tr>
		<td>
			<table width="100%" height="25" border="0" class="iw-base-heading">
			<tbody><tr>
			  <td align="left" style="" class="iw-base-heading-title">Dealer finder management console</td>
			  <td align="right" style="white-space: nowrap;"></td>
			</tr></tbody>
			</table>
		</td>
	</tr>
</table>

<%

	if (userGroups.size() > 0) {

%>

<div id="df_intro">
	<p><div class="iw-base-notetext">Please search and select your dealer records below before editing and saving or select "new".</div></p>
	<input id="df_new" name="new" type="button" value="New dealer"/>
</div>

<div id="df_search" class="df_search">

	<label for="q" class="label_val">Search text:</label>
	<input name="q" type="text" class="iw-base-textbox" value="enter text to search"/>
	<select name="field">
		<!-- make this list manageable from XML file -->
		<option value="name">Name</option>
		<option value="description">Description</option>
		<option value="street">Street</option>
		<option value="town">Town / City</option>
		<option value="country">Country</option>
		<option value="countryCode">Country code</option>
		<option value="postCode">Post code</option>
		<option value="author">Author</option>
	</select>
	
	<input name="submit" type="submit" value="Search" id="df_basicSearchButton"/>
	<input id="df_advsearch_link" name="new" type="button" value="Advanced search"/>
</div>

<div id="df_advsearch" class="df_search">
	<div class="iw-base-notetext">Please enter search critieria to be matched against (and query)</div><p/>

	<span><label for="name" class="label_val">Name</label> <input name="name" type="text" value="" class="iw-base-textbox"/></span>
	<span><label for="description" class="label_val">Description</label> <input name="description" type="text" value="" class="iw-base-textbox"/></span>
	<span><label for="street" class="label_val">Street</label> <input name="street" type="text" value="" class="iw-base-textbox"/></span>
	<span><label for="town" class="label_val">Town</label> <input name="town" type="text" value="" class="iw-base-textbox"/></span>
	<span><label for="state" class="label_val">State</label> <input name="state" type="text" value="" class="iw-base-textbox"/></span>
	<span>
		<label for="country" class="label_val">Country</label> 
		<select id="df_advSearchCountry" name="country">
			<option/>
		</select>
	</span>
	<span><label for="postCode" class="label_val">Post code</label> <input name="postCode" type="text" value="" class="iw-base-textbox"/></span>
	<span><label for="author" class="label_val">Author</label> <input name="author" type="text" value="" class="iw-base-textbox"/></span>
	<span><label for="from" class="label_val">Last modified</label> From: <input name="from" type="text" value="" class="iw-base-textbox"/> To: <input name="to" type="text" value="" class="iw-base-textbox"/></span>
	<span>
		<label for="status" class="label_val">Status</label> 
		<select name="status">
			<option value="all">All</option>
			<option value="draft">Draft</option>
			<option value="deleted">Deleted</option>
			<option value="published">Published</option>
		</select>
	</span>		
	
	<div id="df_advSearchOtherFields" class="iw-base-notetext">
		To search other fields please first select the country:
	</div><p/>
	
	<input name="submit" type="submit" value="Search" id="df_advSearchButton"/>
	
	<input id="df_basicsearch_link" name="new" type="button" value="Basic search"/>
</div>



<div id="df_results">
	<table cellpadding="0" cellspacing="0" id="df_dealerstable" class="display dataTable" width="100%">
		<thead>
			<tr>
				<th width="0%">ID</th>
				<th width="26%"><input id="checkAll" title="Check all" name="Check all" type="checkbox" />Name</th>
				<th width="23%">Description</th>
				<th width="29%">Address</th>
				<th width="9%">Modified</th>
				<th width="5%">Status</th>
				<th width="4%">ABF</th>
				<th width="4%">Live</th>
			</tr>
		</thead>
		<tbody>
			
		</tbody>
	</table>
	
	<div id="df_tablecontrols">
		<input id="df_publish" name="publish" type="button" value="Publish"/> <input id="df_delete" name="delete" type="button" value="Delete"/>
	</div>
</div>

<div id="df_record">
	<div id="df_record_map">Loading...</div>
	<div id="df_record_details">
		<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
		<tr>
			<td>
				<table width="100%" height="25" border="0" class="iw-base-heading">
				<tbody><tr>
				  <td align="left" style="" class="iw-base-heading-title">Key details</td>
				  <td align="right" style="white-space: nowrap;"><div id="df_status"></div></td>
				</tr></tbody>
				</table>
			</td>
		</tr>
		</table><p/>
		<div class="left">
			<label for="df_record_id" class="label_val">ID: </label><span id="df_record_id" class="readonly">12345</span><br/>
			<label for="df_record_name" class="label_val">Name: </label><input id="df_record_name" type="text"/><br/>
			<label for="df_record_description" class="label_val">Description: </label><textarea id="df_record_description"></textarea><br/>
			<label for="df_record_street" class="label_val">Street: </label><input id="df_record_street" type="text" /><br/>
			<label for="df_record_town" class="label_val">Town: </label><input id="df_record_town" type="text"/><br/>
			<label for="df_record_state" class="label_val">State: </label><input id="df_record_state" type="text"/><br/>
			<label for="df_record_pc" class="label_val">Postal code: </label><input id="df_record_pc" type="text"/><br/>
			<label for="df_record_country" class="label_val">Country: </label><select id="df_record_country"><option>Select the country</option></select><br/>
		</div>
		
		<div class="df_record_coordinates">
			<label for="df_record_long" class="label_val">Longitude: </label><span id="df_record_long" class="readonly">11111</span><br/>
			<label for="df_record_lat" class="label_val">Latitude: </label><span id="df_record_lat" class="readonly">22222</span><br/>
		</div>
		<div>
			<input id="df_record_geocode" name="geocode" type="button" value="Geo-code"/>
		</div>
		<div class="clear">
			<label for="df_record_telephone" class="label_val">Telephone: </label><input id="df_record_telephone" type="text"/><br/>
			<label for="df_record_fax" class="label_val">Fax: </label><input id="df_record_fax" type="text"/><br/>
			<label for="df_record_email" class="label_val">Email: </label><input id="df_record_email" type="text"/><br/>
			<label for="df_record_url" class="label_val">Website: </label><input id="df_record_url" type="text"/><input id="df_record_url_browse" name="url_browse" type="button" value="Test..."/><br class="clear"/>
			<label for="df_record_opening" class="label_val">Opening hours: </label><input id="df_record_opening" type="text"/><br/>	
		</div>
		<div class="left" id="df_record_additional"></div>
		<div id="df_record_buttons" class="left">
			<input id="df_record_draft" name="publish" type="button" value="Save as Draft"/> <input id="df_record_publish" name="delete" type="button" value="Save and Publish"/>
			<input id="df_record_delete" name="delete" type="button" value="Delete"/> <input id="df_record_cancel" name="cancel" type="button" value="Cancel"/>
		</div>
	</div>
</div>

<!-- dialogs below -->
<div id="df_record_geocode_confirm" class="df_dialog" title="Please confirm the correct address">    
	<div></div>
</div>

<div id="df_record_country_select" class="df_dialog" title="Please confirm the correct country">    
	<h3>Countries:</h3>
</div>

<div id="loadingScreen"></div>

<%

	} else {

%>

<div id="df_intro">
	<p><div class="iw-base-notetext">You do not have access to use this tool. Please contact your TeamSite administrator.</div></p>
</div>

<%
	}
%>

</body>

</html>
