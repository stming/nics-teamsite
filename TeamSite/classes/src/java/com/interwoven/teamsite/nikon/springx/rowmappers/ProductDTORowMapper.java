package com.interwoven.teamsite.nikon.springx.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.interwoven.teamsite.nikon.dto.ProductDTO;

/**
 * Concrete implementation of org.springframework.jdbc.core.RowMapper
 * For mapping V_PORODUCT to com.interwoven.teamsite.nikon.dto.ProductDTO
 * @author nbamford
 *
 */
public class ProductDTORowMapper
implements RowMapper
{
	/**
	 * Default constructor
	 */
	public ProductDTORowMapper(){} 
	
	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public ProductDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		ProductDTO dto = new ProductDTO();

		dto.setId(rs.getString("ID"));
		dto.setProdId(rs.getString("PROD_ID"));
		dto.setProdDevCode(rs.getString("PROD_DEV_CODE"));
		dto.setLocalProduct(rs.getBoolean("LOCAL_PROD"));
		dto.setMigrated(rs.getBoolean("MIGRATED"));
		dto.setPath(rs.getString("PATH"));

		return dto;
	}
}
