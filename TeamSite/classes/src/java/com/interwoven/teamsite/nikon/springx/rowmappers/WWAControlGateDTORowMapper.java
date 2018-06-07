package com.interwoven.teamsite.nikon.springx.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.dto.WWAControlGateDTO;

/**
 * Concrete implementation of org.springframework.jdbc.core.RowMapper
 * For mapping V_PORODUCT to com.interwoven.teamsite.nikon.dto.ProductDTO
 * @author nbamford
 *
 */
public class WWAControlGateDTORowMapper
implements RowMapper
{
	/**
	 * Default constructor
	 */
	public WWAControlGateDTORowMapper(){} 
	
	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public WWAControlGateDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		WWAControlGateDTO dto = new WWAControlGateDTO();

		dto.setProdDevCode(rs.getString("PROD_DEV_CODE"));
		dto.setLocalProduct(rs.getBoolean("LOCAL_PROD"));
		dto.setMigrated(rs.getBoolean("MIGRATED"));
		dto.setDeploy(rs.getBoolean("DEPLOY"));
		dto.setStatus(rs.getString("STATUS"));

		return dto;
	}
}
