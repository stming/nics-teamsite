package com.interwoven.teamsite.javax.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * Extension of LinkedList to allow the filtering of items.
 * LinkedList is filtered to ensure correct order.
 * @author nbamford
 *
 * @param <E>
 */
public class FilteredList<E> extends LinkedList<E>
{
	public FilteredList()
	{
		super();
	}
	
	public FilteredList(Collection<E> collection)
	{
		super(collection);
	}
	
	/**
	 * Method to reurn a list based on the passed filter
	 * @param filter
	 * @return
	 */
	public List<E> filterList(Filter<E> filter)
	{
	    FilteredList<E> filteredList = new FilteredList<E>();
	    for (E e: this)
	    {
	      if (filter.test(e)) filteredList.add(e);
	    }
		return filteredList;
	}
}
