package com.nikon.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.Cookie;

import com.interwoven.livesite.runtime.RequestContext;

/**
 * Class for fetching the Store Id to be used by the ecommerce cart.
 * 
 * @author Niklas Persson (npersson@interwoven.com)
 */
public class CartStoreId {

	public static final String defaultCartStoreId = "_nikonukukgbp";
	public static final String langCookieName = "langCookie";
	public static final String langPropsFileName = "cart_store_id_mappings.properties";

	public static String getCartStoreId(String langParam) {
		String storeId = null;

		InputStream is = CartStoreId.class.getClassLoader().getResourceAsStream(langPropsFileName);
		if (is == null) {
			System.err.println("Could not find property file " + langPropsFileName);
		} else {
			Properties langProps = new Properties();
			try {
				langProps.load(is);
				storeId = langProps.getProperty(langParam);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return storeId;
	}

	public static String getCartStoreId(RequestContext context) {
		String cartStoreId = defaultCartStoreId;
		Cookie cookie = context.getCookies().getCookie(langCookieName);
		if (cookie != null && cookie.getValue() != null) {
			String mappedCartStoreId = getCartStoreId(cookie.getValue());
			if (mappedCartStoreId != null) {
				cartStoreId = mappedCartStoreId;
			}
		}
		return cartStoreId;
	}

}
