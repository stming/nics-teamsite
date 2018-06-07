package com.interwoven.teamsite.nikon.springx.dao;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.interwoven.teamsite.nikon.dto.WWAControlGateDTO;

/**
 * ProductDTO DAO which makes use of Core Spring JDBC classes
 * @author nbamford
 *
 */
public class WWAControlGateDTOSpringDAO
{
	private Log log = LogFactory.getLog(WWAControlGateDTOSpringDAO.class);
	/**
	 * Default constructor 
	 */
	public WWAControlGateDTOSpringDAO(){};

	private NamedParameterJdbcTemplate simpleJdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	
	public void flagWWAControlGateRecordsSpent()
	{
		MapSqlParameterSource pMap = new MapSqlParameterSource();
		simpleJdbcTemplate.update("UPDATE WWA_DEPLOYMENT_GATE SET SPENT = 1 WHERE SPENT = 0", pMap);
	}
	
	public void insertWWAControlGateDTO(WWAControlGateDTO wwaControlGateDTO)
	{
/*
	    [DEPLOYMENT_DATE] [DATETIME],
	    [PROD_DEV_CODE] [NVARCHAR](2000) NULL,
	    [LOCAL_PRODUCT] [BIT],
	    [MIGRATED] [BIT],
	    [DEPLOY] [BIT]
	    [STATUS] [NVARCHAR] (2000) NULL 
*/
		MapSqlParameterSource msqps = new MapSqlParameterSource();
		msqps.addValue("deploymentDate", wwaControlGateDTO.getDeploymentDate(), Types.TIMESTAMP);
		msqps.addValue("prodDevCode", wwaControlGateDTO.getProdDevCode(), Types.VARCHAR);
		msqps.addValue("localProduct", new Boolean(wwaControlGateDTO.isLocalProduct()), Types.BIT);
		msqps.addValue("migrated", new Boolean(wwaControlGateDTO.isMigrated()), Types.BIT);
		msqps.addValue("deploy", new Boolean(wwaControlGateDTO.isDeploy()), Types.BIT);
		msqps.addValue("status", wwaControlGateDTO.getStatus(), Types.VARCHAR);

		//For newley inserted records we want to be able set spent to 0. Spen becomes 1 for old records
		//before a run of this process and filtered out in the the view V_WWA_DEPLOYMENT_GATE_LATEST so 
		//that we never use an old run's set of data
		simpleJdbcTemplate.update("INSERT INTO WWA_DEPLOYMENT_GATE (DEPLOYMENT_DATE, PROD_DEV_CODE, LOCAL_PRODUCT, MIGRATED, DEPLOY, STATUS, SPENT) VALUES (:deploymentDate, :prodDevCode, :localProduct, :migrated, :deploy, :status, 0)", msqps);
	}

	
}
