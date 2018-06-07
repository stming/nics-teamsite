package com.interwoven.teamsite.javax.util;

/**
 * Simple Filter interface
 * @author nbamford
 *
 * @param <T>
 */
public interface Filter<T> 
{
	public boolean test(T testObject);
}
