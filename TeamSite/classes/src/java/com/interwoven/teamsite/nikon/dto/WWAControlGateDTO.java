package com.interwoven.teamsite.nikon.dto;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.interwoven.teamsite.nikon.hibernate.beans.Product;

/**
 * @author nbamford
 * DTO version of the {@link Product} bean
 */
public class WWAControlGateDTO 
extends ProductDTO
{
	
	public WWAControlGateDTO()
	{
		
	}
	public WWAControlGateDTO(ProductDTO productDto)
	{
		BeanUtils.copyProperties(productDto, this);
	}
	
	private Date deploymentDate;
	private boolean deploy;
	private String status = "OK";
	
	public Date getDeploymentDate() {
		return deploymentDate;
	}
	public void setDeploymentDate(Date deploymentDate) {
		this.deploymentDate = deploymentDate;
	}
	public boolean isDeploy() {
		return deploy;
	}
	public void setDeploy(boolean deploy) {
		this.deploy = deploy;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}

}
