// CONSTANTS
var CONST = {};
CONST.method = "POST";
CONST.basicSearchURL = '/iw-cc/nikon/dealer/controller.jsp?type=search';
CONST.advSearchURL = '/iw-cc/nikon/dealer/controller.jsp?type=search';
CONST.getDetail = '/iw-cc/nikon/dealer/controller.jsp?type=detail';
CONST.bulkPublish = '/iw-cc/nikon/dealer/controller.jsp?type=publish';
CONST.bulkDelete = '/iw-cc/nikon/dealer/controller.jsp?type=delete';
CONST.update = '/iw-cc/nikon/dealer/controller.jsp?type=update';
CONST.save = '/iw-cc/nikon/dealer/controller.jsp?type=create';
CONST.remove = '/iw-cc/nikon/dealer/controller.jsp?type=remove';
CONST.getCountryOptions = '/iw-cc/nikon/dealer/controller.jsp?type=countries&config_file=/default/main/Nikon/WORKAREA/main_wa/templatedata/Admin/dealer_finder_configuration/data/dealer_finder';

// status
var statusMessage = "";

//serialize object - not sure if we need this or is it core jQuery these days!
$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

// status used for loading message
var status = "";
// geoSearch results
var geoResults;
// var country specific options
var countryOptions;

// new or update
var newFlag = false;



$(document).ready(function() {		
   // load stuff here
   $.console.setLevel('log');
   
   /************************ LOADING SCREEN ***********************************/
   $.ajax({
		url: CONST.getCountryOptions,
		success: function(data) {

			countryOptions = $.xml2json(data);

			var newCountryOption = "<option/>";
			var newText = "";

			for (  var i = 0; i < countryOptions.country.length; i++ ) 
			{
				for (var s = 0; s < securityGroupsArr.length; s++)
				{
					if (securityGroupsArr[s].substr(8) == (countryOptions.country[i].code ))
					{
						newCountryOption += "<option value='" + countryOptions.country[i].name + "'>" + countryOptions.country[i].name + "</option>" 
						newText += "<p>" + countryOptions.country[i].name + "</p>";					
					}
				}
			}

			$("#df_advSearchCountry").html(newCountryOption);
			$("#df_record_country").html(newCountryOption);
		}
   });     
   /***************************************************************************/
   
   /************************ LOADING SCREEN ***********************************/
   $("#loadingScreen").dialog({ 
		autoOpen: false,    // set this to false so we can manually open it 
		dialogClass: "loadingScreenWindow", 
		closeOnEscape: false, 
		draggable: false, 
		dialogClass: "noclose",
		width: 460, 
		minHeight: 100, 
		modal: true, 
		buttons: {}, 
		resizable: false, 
		open: function() { 
			$('body').css('overflow','hidden'); 
		}, 
		close: function() { 
			$('body').css('overflow','auto'); 
		} 
   }); 
   
   $("body").on({
		ajaxStart: function() { 
			$("#loadingScreen").dialog('option', 'title', 'Please wait...'); 
			$("#loadingScreen").dialog('open'); 
			$("#loadingScreen").html(status); 
		},
		ajaxStop: function() { 
			$("#loadingScreen").dialog('close');
		}    
   });
   /******************** END LOADING SCREEN ***********************************/
   
   /****************************** SEARCH *************************************/
   $("#df_advsearch_link").click( function() {  
		$("#df_advsearch").show();
		$("#df_search").hide();
   });
   $("#df_basicsearch_link").click( function() {  
		$("#df_advsearch").hide();
		$("#df_search").show();
   });
   
   $( "#df_search input[name='q']" ).focus(function() { $(this).val(""); });
   $( "#df_search input[name='q']" ).keyup(function(event) { if ( event.keyCode == 13 ){ $( "#df_basicSearchButton" ).click(); } });
   
   $("#df_basicSearchButton").click( function() {
		var data = $("#df_search input, #df_search select").serializeArray();
		data.push( { "name" : "method", "value" : "basicSearch" } );
		data.push( { "name" : "group", "value" : securityGroups } );

		displayResults( CONST.basicSearchURL, data );		   
   });   
   
   $("#df_advSearchButton").click( function() {   		   
		var data = $("#df_advsearch input, #df_advsearch select").serializeArray();
		data.push( { "name" : "method", "value" : "advSearch" } );
		data.push( { "name" : "group", "value" : securityGroups } );  	

		displayResults( CONST.advSearchURL, data );		   
   });
   
   $( "#df_advsearch input[name='from']" ).datepicker();
   $( "#df_advsearch input[name='to']" ).datepicker();
   
   $( "#df_advsearch select[name='country']" ).change( function() {
		var value = $(this).val();
		if ( value == "" )
		{
			$("#df_advSearchOtherFields").html("To search other fields please first select the country");
		}
		else
		{
			for (  var i = 0; i < countryOptions.country.length; i++ )
			{
				if ( countryOptions.country[i].name == value )
				{
					var selectedCountry = countryOptions.country[i];
					if ( selectedCountry.option == null )
					{
						$("#df_advSearchOtherFields").html("The selected country has no other fields");
					}
					else
					{
						$("#df_advSearchOtherFields").html("");
						
						if (selectedCountry.option.length != undefined) 
						{
							for ( var j = 0; j < selectedCountry.option.length; j++ )
							{
								// country match found
								$("#df_advSearchOtherFields").append( "<span><input name='" + selectedCountry.option[j].option_id + "' value='true' type='checkbox'/>" + selectedCountry.option[j].name + "</span>");
							}
						}
						else
						{
							$("#df_advSearchOtherFields").append( "<span><input name='" + selectedCountry.option.option_id + "' value='true' type='checkbox'/>" + selectedCountry.option.name + "</span>");
						}
					}
					
					i = countryOptions.country.length + 1;
				}
			}
		}
   });
   /****************************** END SEARCH *********************************/
   
   /********************************** NEW ************************************/
   $("#df_new").click( function() {  
		var data = {};
		data.name = "";
		data.description = "";
		data.longitude = 0;
		data.latitude = 0;
		data.zoom = 1;

		$('#df_record').show();
		newFlag = true;
		loadData( data );
   });
   /********************************END NEW ************************************/
   
   /******************************** DATA TABLE ********************************/
   $("#checkAll").click( function(ev) {
   
		var data = $('#df_dealerstable').dataTable().fnGetData();
		
		for ( var i = 0; i < data.length; i++ )
		{
			if ( $("#checkAll").is(':checked') ) 
			{
				data[i][8] = "checked";
			}
			else
			{
				data[i][8] = "";
			}
		}
		
		showBulkControls();
		
		ev.stopPropagation();
   });   
   
   $("#df_publish").click( function() {
		var idObj = {};
		idObj.user = user;
		idObj.groups = securityGroups;
		idObj.ids = new Array();

		var data = $('#df_dealerstable').dataTable().fnGetData();
		
		for ( var i = 0; i < data.length; i++ )
		{
			var row = $('#df_dealerstable').dataTable().fnGetNodes( i );
			if ( data[i][8] == "checked" )
			{
				idObj.ids.push( {id : data[i][0]} );
			}
		}
		
		status = '<p>Publishing records...</p>';

		$.ajax({
			url: CONST.bulkPublish,
			dataType: "json",
			data: JSON.stringify(idObj),
					type: CONST.method,
			success: function(data) {
				// trigger search event
				if ( $("#df_search").is(":visible") )
				{
					$("#df_basicSearchButton").trigger('click');	
				}
				else
				{
					$("#df_advSearchButton").trigger('click');
				}
			}
		});
   });
   
   $("#df_delete").click( function() {
		var idObj = {};
		idObj.user = user;
		idObj.groups = securityGroups;
		idObj.ids = new Array();

		var data = $('#df_dealerstable').dataTable().fnGetData();
		
		for ( var i = 0; i < data.length; i++ )
		{
			var row = $('#df_dealerstable').dataTable().fnGetNodes( i );
			if ( data[i][8] == "checked" )
			{
				idObj.ids.push( {id : data[i][0]} );
			}
		}
		
		status = '<p>Deleting records...</p>';
		$.ajax({
			url: CONST.bulkDelete,
			dataType: "json",
			data: JSON.stringify(idObj),
					type: CONST.method,
			success: function(data) {
				// trigger search event
				if ( $("#df_search").is(":visible") )
				{
					$("#df_basicSearchButton").trigger('click');	
				}
				else
				{
					$("#df_advSearchButton").trigger('click');
				}
			}
		});	   
	});
	/******************************** DATA TABLE ********************************/
	
    /********************************** DETAIL **********************************/
    /*$("#df_record_country").click( function() {
		selectCountry( this );
    });*/
	   
	$("#df_record_details textarea#df_record_description").tinymce({
	   script_url: 'js/tiny_mce/tiny_mce.js',
	   theme: "simple",
	   oninit : hideTinyMCE,
	   forced_root_block : '',
	   setup : function(ed) {
			ed.onInit.add(function(ed) {
			var dom = ed.dom,
				doc = ed.getDoc(),
				el = doc.content_editable ? ed.getBody() : (tinymce.isGecko ? doc : ed.getWin());
			tinymce.dom.Event.add(el, 'blur', function(e) {
				ed.hide();
			});
			});
	   }
    });
    
	$("#df_record_details textarea#df_record_description").click( function() {
		$(this ).tinymce().show();
		$(this ).tinymce().focus();
	});    
    
    $("#df_record_geocode").click( function() {
    	$('#df_record_map').gmap3('clear', 'markers');
    	$("#df_record_map").gmap3({  
    		getlatlng:{    
    			address:  $("#df_record_street").val() + "," + $("#df_record_town").val()+ "," + $("#df_record_country").val(),    
    			callback: function(results){      
    				if ( !results ) return;
    				if ( results.length > 1 )
    				{
						geoResults = results;
							$("#df_record_geocode_confirm").html("");
							for ( var i = 0; i < results.length; i++ )
						{
							$("#df_record_geocode_confirm").append('<p class="df_addressSelector" index="' + i + '">' + results[i].formatted_address + '</p>');
						}
						$(".df_addressSelector").click( function() {
							$("#df_record_map").gmap3({        
								marker:{          
									latLng:geoResults[$(this).attr( "index" )].geometry.location,
									options:{
										draggable: true
									},
									events:{
										dragend: function(marker, event, context) {
											$("#df_record_long").html( marker.getPosition().lng() );
											$("#df_record_lat").html( marker.getPosition().lat());
										}
									}
								}
							});
							$("#df_record_map").gmap3('get').setZoom( 16 );
							$("#df_record_map").gmap3('get').panTo( geoResults[$(this).attr( "index" )].geometry.location );
							$("#df_record_long").html( geoResults[$(this).attr( "index" )].geometry.location.lng() );
							$("#df_record_lat").html( geoResults[$(this).attr( "index" )].geometry.location.lat() );
							$("#df_record_geocode_confirm" ).dialog( "close" );
						});

						$("#df_record_geocode_confirm").dialog({            
							height: 320,   
							width: 500,
							position: { my: "center", at: "center-55 center-125", of: window },
							modal: true						
						});
					}
					else
					{
						$(this).gmap3({        
							marker:{          
								latLng:results[0].geometry.location,
								options:{
									draggable: true
								},
								events:{
									dragend: function(marker, event, context) {
										$("#df_record_long").html( marker.getPosition().lng() );
										$("#df_record_lat").html( marker.getPosition().lat());
									}
								}
							}      
						});   
						$("#df_record_long").html( results[0].geometry.location.lng() );
						$("#df_record_lat").html( results[0].geometry.location.lat() );
						$("#df_record_map").gmap3('get').setZoom( 16 );
						$("#df_record_map").gmap3('get').panTo( results[0].geometry.location );
					}
    			}  
    		}
    	});
    });
	
	$("#df_record_url_browse").click( function() {
		var url = $("#df_record_url").val();
		if ( !url.match( /^http.*/ ) )
		{
			url = "http:\/\/" + url;
		}
		window.open( url );		
	});
	
	/*******************************END DETAIL **********************************/
});

function hideTinyMCE()
{
	$('#df_record_details textarea#df_record_description').tinymce().hide();
}

function updateRowSelected( checkbox )
{
	var row = $(checkbox).parent().parent();
	var data = $('#df_dealerstable').dataTable().fnGetData( row[0] );
	
	if ( $(checkbox).is( ":checked" ) )
	{
		data[8] = "checked";					
	}
	else
	{
		data[8] = "";
	}
	
	showBulkControls();
}

function showBulkControls()
{
	var data = $('#df_dealerstable').dataTable().fnGetData();
		
	var selected = 0;	
		
	for ( var i = 0; i < data.length; i++ )
	{
		var row = $('#df_dealerstable').dataTable().fnGetNodes( i );
		if ( data[i][8] == "checked" )
		{
			selected++;
			if ( row != null )
			{
				$(row).addClass('row_selected');
				$(row).children('td').children('input').attr( "checked", "checked" );
			}
		}
		else
		{
			if ( row != null )
			{
				$(row).removeClass('row_selected');
				$(row).children('td').children('input').removeAttr( "checked" );
			}
		}
	}
	
	$.console.log( selected + " rows selected" );
	
	if ( selected > 0 )
	{
		$("#df_publish").attr( "value", "Publish " + selected + " record" + ((selected>1)?"s":"") );
		$("#df_delete").attr( "value", "Delete " + selected + " record" + ((selected>1)?"s":"") );
		$("#df_tablecontrols").show();	
	}
	else
	{
		$("#df_tablecontrols").hide();	
	}
	
}

function displayResults( url, data )
{
	$("#df_dealerstable td input").live("click", function(ev){ updateRowSelected( this ); ev.stopPropagation(); }); 
	$("#df_dealerstable tr").live("click", function(){ loadRecord( this ); }); 
	
	if ( $('#df_dealerstable').dataTable() )
	{
		$('#df_dealerstable').dataTable().fnDestroy();
	}
	
	status = '<p>Searching...</p>';
	$("#df_results").show();
	$('#df_dealerstable').dataTable({
		"sScrollY": "200px",        
		"sPaginationType": "full_numbers",
		"oLanguage": 
            {
                "oPaginate": 
                {
                    "sNext": '>',
                    "sLast": '>>',
                    "sFirst": '<<',
                    "sPrevious": '<'
                }
            },
		"bScrollCollapse": true,        
		"bLengthChange": true,        
		"bFilter": false,        
		"bSort": true,        
		"bInfo": true,        
		"bAutoWidth": false,
		"bProcessing": false,
		"sServerMethod": CONST.method,
		"sAjaxSource": url,
		"fnServerParams": function ( aoData ) {
			aoData = jQuery.merge( aoData, data );			
		},
		"fnServerData": function( sUrl, aoData, fnCallback, oSettings ) {            
			oSettings.jqXHR = $.ajax( {                
				"url": sUrl,                
				"data": "{\"data\": " + JSON.stringify(aoData) + "}",                
				"success": fnCallback,                
				"dataType": "json",
				"type" : CONST.method
			} );        
		},
		"bDeferRender": true,
		"aoColumnDefs": [   
			{ "bSearchable": false, "bVisible": false, "aTargets": [ 0 ] },
			{ "aTargets": [ 1 ], "mRender": function ( data, type, full ) {
				   return '<input type="checkbox" class="rowSelect"/>' + data;
				}
			},
			{ "sClass": "abf", "aTargets": [ 6 ] },
			{ "sClass": "live", "aTargets": [ 7 ] }
		],
		"fnDrawCallback": function () { 
			//fnInitComplete
			this.$('td.abf:contains(0), td.live:contains(0)').addClass("red").html( "" );
			this.$('td.abf:contains(1), td.live:contains(1)').addClass("amber").html( "" );
			this.$('td.abf:contains(2), td.live:contains(2)').addClass("green").html( "" );
			showBulkControls();
		}
	});	
}
 
function loadRecord( row )
{
	$('#df_dealerstable tr').removeClass( 'record_visible' );
	$(row).addClass('record_visible');

	$('#df_record').show();

	var id = $('#df_dealerstable').dataTable().fnGetData( row, 0 );
	 
	$.console.log( "Loading record" );
	
	status = '<p>Loading record...</p>';
	$.ajax({
		url: CONST.getDetail,
		dataType: "json",
		data: JSON.stringify({id : id}),
		type: CONST.method,
		success: function(data) {
			newFlag = false;
			loadData( data );
		}
	});
}

function loadData( data )
{
	$.console.log( "Got record - " + notNull( data.id ) );
	$("#df_record_id").html( notNull( data.id ) );
	$("#df_record_name").val( notNull( data.name ) );
	$.console.log( "Description - " + notNull( data.description ) );
	$("#df_record_details textarea#df_record_description").tinymce().show();
	$("#df_record_details textarea#df_record_description").tinymce().setContent( notNull( data.description ) );
	$("#df_record_details textarea#df_record_description").tinymce().hide();
	
	$("#df_record_street").val( notNull( data.street ) );
	$("#df_record_town").val( notNull( data.town ) );
	$("#df_record_state").val( notNull( data.state ) );
	$("#df_record_pc").val( notNull( data.post_code ) );
	
	if ( notNull( data.country ) == "" )
	{
		$("#df_record_country").val( "" );
	}
	else
	{
		$("#df_record_country").val( notNull( data.country ) );
	}
				
	$("#df_record_email").val( notNull( data.email ) );
	$("#df_record_url").val( notNull( data.url ) );
	$("#df_record_telephone").val( notNull( data.tel ) );
	$("#df_record_fax").val( notNull( data.fax ) );
	$("#df_record_opening").val( notNull( data.opening_hours ) );
			
	$.console.log( "Geo-data " + notNull( data.longitude ) + "," + notNull( data.latitude ) );
	
	if (!( notNull( data.longitude ) + "" != "" && notNull( data.latitude ) + "" != "" )){
		data.longitude = 4.85551847608258;
		data.latitude = 52.33993637168863;
		data.zoom = 5;
	}
	$("#df_record_long").html( data.longitude );
	$("#df_record_lat").html( data.latitude );
			
	$.console.log( "Load map" );
	$.console.log( data.latitude + ", " + data.longitude );
	// map the position
	$('#df_record_map').gmap3('clear', 'markers');
	$("#df_record_map").gmap3({ 
		options:{     
			center:[22.49156846196823, 89.75802349999992],     
			zoom:2,     
			mapTypeId: google.maps.MapTypeId.MAP,     
			mapTypeControl: true,     
			mapTypeControlOptions: {       
				style: google.maps.MapTypeControlStyle.DROPDOWN_MENU     
			},     
			navigationControl: true,     
			scrollwheel: true,     
			streetViewControl: true    
		},       
		marker:{          
			latLng:[data.latitude, data.longitude],
			options:{
				draggable: true
			},
			events:{
				dragend: function(marker, event, context) {
					$("#df_record_long").html( marker.getPosition().lng() );
					$("#df_record_lat").html( marker.getPosition().lat());
				}
			}
		}
	});
			
	$.console.log( "Zoom and position on map" );
	$("#df_record_map").gmap3('get').setZoom( notNull( data.zoom ) + "" != "" ? data.zoom : 16 );
	$("#df_record_map").gmap3('get').panTo( new google.maps.LatLng(data.latitude, data.longitude) );

			
	$.console.log( "Status" );
	// reset status
	$("#df_status").html("");
	if ( notNull( data.status ) == "" || data.status == "draft" )
	{
		$("#df_status").append( '<img src="img/amber.png" title="Draft"/>' );
	}
	else if ( (data.status).match(/publish/i) )
	{
		$("#df_status").append( '<img src="img/green.png" title="' + data.status + '"/>' );
	}
	else
	{
		$("#df_status").append( '<img src="img/red.png" title="' + data.status + '"/>' );
	}
			
	if ( notNull( data.abf ) == "" || data.abf == "0" )
	{
		$("#df_status").append( '<img src="img/red.png" title="Not on ABF"/>' );
	}
	else if ( data.abf == "1" )
	{
		$("#df_status").append( '<img src="img/amber.png" title="Older on ABF"/>' );
	}
	else
	{
		$("#df_status").append( '<img src="img/green.png" title="Same as ABF"/>' );
	}
			
	if ( notNull( data.prod ) == "" || data.prod == "0" )
	{
		$("#df_status").append( '<img src="img/red.png" title="Not on live"/>' );
	}
	else if ( data.prod == "1" )
	{
		$("#df_status").append( '<img src="img/amber.png" title="Older on live"/>' );
	}
	else
	{
		$("#df_status").append( '<img src="img/green.png" title="Same as live"/>' );
	}
			
	$.console.log( "Additional fields" );
	$("#df_record_additional").html("<span class='iw-base-textbox'>Other fields</span><br/>");
	loadAdditionalFields( $("#df_record_additional"), data.country, data );
	$("#df_record_country").data("data", data);
 	$("#df_record_country").change(function() {
//		alert("on change called" + $(this).find(":selected").text());
		loadAdditionalFields( $("#df_record_additional"), $(this).find(":selected").text(), $(this).data("data") );
	});	
	$("#df_record_publish").off( "click" );
	$("#df_record_publish").click( function() {
		saveRecord( "publish" );			
	});
				
	$("#df_record_draft").off( "click" );
	$("#df_record_draft").click( function() {
		saveRecord( "draft" );	
	});
	
	if ( ! newFlag )
	{
		$("#df_record_delete").off( "click" );
		$("#df_record_delete").click( function() {
			saveRecord( "delete" );		
		});
	}
	
	$("#df_record_cancel").off( "click" );
	$("#df_record_cancel").click( function() {
			$('#df_record').hide();
	});
	
}

function reloadData()
{
	if ( $("#df_basicSearchButton").is( ":visible" ) )
	{
		$("#df_basicSearchButton").trigger('click');
		$('#df_record').hide();
	}
	else
	{
		$("#df_advSearchButton").trigger('click');
		$('#df_record').hide();
		
	}
}

function loadAdditionalFields( target, selectedCountryName, data )
{
	// iterate the country options
	for ( var i = 0; i < countryOptions.country.length; i++ )
	{
		if ( countryOptions.country[i].name == selectedCountryName )
		{
			var selectedCountry = countryOptions.country[i];
			
			$.console.log( "Looking at additional fields for " + selectedCountry.name );
					
			if ( selectedCountry.option != null )
			{
				var htmlFields = "";
				
				if (selectedCountry.option.length != undefined) 
				{
					for ( var j = 0; j < selectedCountry.option.length; j++ )
					{
						// country match found
						var selOptionID = countryOptions.country[i].option[j].option_id;
						var selected = "";
						
						if ( data != null && data.additional != null)
						{
							for ( var k = 0; k < data.additional.length; k++ )
							{
								if ( data.additional[k].fieldID == selOptionID && data.additional[k].fieldValue == "true" )
								{
									selected = "checked='checked'";
									k = data.additional.length + 1;
								}
							}
						}
								
						htmlFields += "<span class='iw-base-textbox'><input name='" + selOptionID + "' value='true' type='checkbox'" + selected +"/>" + countryOptions.country[i].option[j].name + "</span>";
					}
				}
				else
				{
					var selOptionID = selectedCountry.option.option_id;
					var selected = "";
					
					if ( data != null )
					{
						for ( var k = 0; k < data.additional.length; k++ )
						{
							if ( data.additional[k].fieldID == selOptionID && data.additional[k].fieldValue == "true" )
							{
								selected = "checked='checked'";
								k = data.additional.length + 1;
							}
						}
					}
					
					htmlFields += "<span class='iw-base-textbox'><input name='" + selOptionID + "' value='true' type='checkbox'" + selected +"/>" + selectedCountry.option.name + "</span>";
				}
				
				target.html(htmlFields);
			}
			// break the loop
			i = countryOptions.country.length + 1;
		}
	}
}

function saveRecord( status )
{
	statusMessage = status;
	
	var record = {};
	
	record.status = status;
	record.user = user;
   	record.id = $("#df_record_id").html();
	record.name = $("#df_record_name").val();
	record.description = $("#df_record_description").val();
	record.street = $("#df_record_street").val();
	record.town = $("#df_record_town").val();
	record.state = $("#df_record_state").val();
	record.post_code = $("#df_record_pc").val();
	record.country = $("#df_record_country").val();
	record.longitude = $("#df_record_long").html();
	record.latitude = $("#df_record_lat").html();
	record.tel = $("#df_record_telephone").val();
	record.fax = $("#df_record_fax").val();
	record.email = $("#df_record_email").val();
	record.url = $("#df_record_url").val();
	record.opening_hours = $("#df_record_opening").val();
	
	for ( var i = 0; i < countryOptions.country.length; i++ )
	{
		if ( countryOptions.country[i].name == record.country )
		{
			record.country_code = countryOptions.country[i].code;
			
			// break the loop
			i = countryOptions.country.length + 1;
		}
	}
	
	record.groups = "dealers_" + record.country_code;
	
	// process additional fields
	var additionalFields = $("#df_record_additional input");
	
	if ( additionalFields != null )
	{
		record.additional = new Array();
		for ( var i = 0; i < additionalFields.length; i++ )
		{
			record.additional.push( { fieldID : $(additionalFields[i]).attr( "name" ), fieldValue : $(additionalFields[i]).is(':checked') ? $(additionalFields[i]).attr( "value" ) : "false" } );
		}
	}

	var saveUrl = CONST.update
	if ( newFlag )
	{
		saveUrl = CONST.save;
	}
	
	status = '<p>Updating record...</p>';
	$.ajax({
		url: saveUrl,
		dataType: "json",
		data: JSON.stringify(record),
                type: CONST.method,
		success: function(data) {
			// alert saved
			if ( statusMessage == "delete" )
			{
				alert( "Record marked as deleted" );
			}
			else
			{
				alert( "Record saved" );
			}
			reloadData();
		}
	});	   
}

function selectCountry( obj )
{
	var id = $(obj).attr( "id");
	
	var targetDialog = $("#" + id + "_select");
	
	targetDialog.dialog({            
		height: 320,    
		width: 200,
		position: { my: "center", at: "center-55 center-125", of: window },
		modal: true        
	});

	targetDialog.children( "p" ).click( function() {
		var target = $(this).parent().attr( "id");
		target="#" + target.replace( /(.*)_select/, "$1" );
		target= target + " option";
		$(target).html( $(this).text() );
		
		// now update additional options based on change
		loadAdditionalFields( $("#df_record_additional"), $(this).text(), null );

		$(this).parent().dialog( "close" );		
	});	
}
	
function notNull( data )
{
	if ( data == null || typeof(data) == 'undefined' || data == " ")
	{
		return "";
	}
	else
	{
		return data;
	}
}