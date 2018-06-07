package com.interwoven.teamsite.nikon.dealerfinder.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.interwoven.teamsite.nikon.dealerfinder.AdditionalData;
import com.interwoven.teamsite.nikon.dealerfinder.Dealer;
import com.interwoven.teamsite.nikon.dealerfinder.HibernateUtil;

public class Exporter {

	Logger oLogger = LoggerFactory.getLogger(this.getClass());
	
	public void execute(String country) throws Exception{
		
		SessionFactory factory = HibernateUtil.getSessionFactory();
		Session session = factory.openSession();
		List countryDealer = session.createCriteria(Dealer.class).add(Restrictions.eq("countryCode", country)).list();
		final String[] header = new String[] { "source", "client_id", "innercode", "Name", "Description",
                "street", "pc", "town", "state", "country", "countrycode","cmpadr","cmpadr2","cmpadr3","cmpadr4","cmpadr5",
                "tel","fax","email","url","c00","c01","c02","c03","c04","c05","c06","c07","c08","c09","c10","c11",
                "c12","c14","c15","c16","c17","c18","c19","c20","c21","c22","c23","c24","c25",
                "c26","c27","c28","c29","c30","c31","c32","c33","c34","c35","c36","c37","c38",
                "c39","c40","c41","c42","c43","c44","c45","c46","c47","c48","c49","opening_hours",
                "lon","lat","storelocation","src_email","icon","logo","id","GeoQuality","GeocodeSource","GeocodeDate"};


		
		ICsvMapWriter mapWriter = new CsvMapWriter(
				new OutputStreamWriter(new FileOutputStream(new File("export_"+country+".csv")), "UTF-8"),
                CsvPreference.STANDARD_PREFERENCE);		
		mapWriter.writeHeader(header);
		for (Iterator it = countryDealer.iterator();it.hasNext();){
			//source,client_id,innercode,Name,Description,street,pc,town,state,country,countrycode,
			//cmpadr,cmpadr2,cmpadr3,cmpadr4,cmpadr5,
			//tel,fax,email,url,c00,c01,c02,c03,c04,c05,c06,c07,c08,c09,c10,c11,c12,c14,c15,c16,c17,c18,c19,c20,c21,c22,c23,c24,c25
			//c26,c27,c28,c29,c30,c31,c32,c33,c34,c35,c36,c37,c38,c39,c40,c41,c42,c43,c44,c45,c46,c47,c48,c49,opening_hours,
			//lon,lat,storelocation,src_email,icon,logo,id,GeoQuality,GeocodeSource,GeocodeDate
			Dealer dealer = (Dealer) it.next();
			final Map<String, String> dealerMap = new HashMap<String, String>();
			dealerMap.put(header[0], "dealerdb");
			dealerMap.put(header[1], "");
			dealerMap.put(header[2], "");
			dealerMap.put(header[3], dealer.getName());
			dealerMap.put(header[4], dealer.getDescription());
			dealerMap.put(header[5], dealer.getStreet());
			dealerMap.put(header[6], dealer.getPostCode());
			dealerMap.put(header[7], dealer.getTown());
			dealerMap.put(header[8], dealer.getState());
			dealerMap.put(header[9], dealer.getCountry());
			dealerMap.put(header[10], dealer.getCountryCode());
			dealerMap.put(header[11], "");
			dealerMap.put(header[12], "");
			dealerMap.put(header[13], "");
			dealerMap.put(header[14], "");
			dealerMap.put(header[15], "");
			dealerMap.put(header[16], " " + dealer.getTel());
			dealerMap.put(header[17], " " + dealer.getFax());
			dealerMap.put(header[18], dealer.getEmail());
			dealerMap.put(header[19], dealer.getUrl());
			
			Set<AdditionalData> additionalDatas = dealer.getAdditionalData();
			Map<Integer, String> additionalData = new HashMap<Integer, String>(); 
			if (additionalDatas !=null)
            {
                for (AdditionalData aData : additionalDatas)
                {
                	int fieldId = aData.getFieldId().intValue();
                	String fieldValue = aData.getFieldValue();
                	fieldId = fieldId - 1;
                	additionalData.put(fieldId, fieldValue);
                }
            }
			for (int i=0;i<50;i++){
				String value = additionalData.get(i);
				String field = ""+i;
				if (i<10){
					field = "0" + field;
				}
				if ("true".equals(value)){
					dealerMap.put("c" + field, "1");
				}else{
					dealerMap.put("c" + field, "0");
				}
			}
			dealerMap.put(header[69], dealer.getOpeningHours());
			
			System.out.println(dealer.getLatitude());
			dealerMap.put(header[70], ""+dealer.getLongitude());
			dealerMap.put(header[71], ""+dealer.getLatitude());
			dealerMap.put(header[76], ""+ dealer.getId());
			
			mapWriter.write(dealerMap, header);
			

			
		}
		mapWriter.close();
	}

}
