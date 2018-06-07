package com.interwoven.teamsite.nikon.data;

import java.util.ArrayList;
import java.util.List;

import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.hibernate.beans.Product;

public interface ProductManager {

	public List<String> retrieveAllAvailableLocale() throws DataAccessException;
	
	public List<Product> retrieveProductsByLocale(String locale) throws DataAccessException;
	
}
