package com.interwoven.teamsite.nikon.springx.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.interwoven.teamsite.nikon.dto.ProductDTO;

/**
 * Maps the V_METADATA_BEFORE_WWA view to the ProductDTO
 * @author nbamford
 *
 */
public class ProductDTOWWABeforeRowMapper
implements RowMapper
{
	/**
	 * Default constructor
	 */
	public ProductDTOWWABeforeRowMapper(){} 
	
	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public ProductDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		ProductDTO dto = new ProductDTO();

		dto.setProdDevCode(rs.getString("PROD_DEV_CODE"));
		dto.setLocalProduct(rs.getBoolean("LOCAL_PROD"));
		dto.setMigrated(rs.getBoolean("MIGRATED"));

		return dto;
	}
}
