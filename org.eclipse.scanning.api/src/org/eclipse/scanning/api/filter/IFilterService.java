package org.eclipse.scanning.api.filter;

import java.util.Collection;
import java.util.List;

public interface IFilterService {

	/**
	 * The default filter service. OSGi is not used for this because
	 * it is a simple implementation using JDK classes.
	 * 
	 * When a {@link Filter} is made it registers itself to IFilterService.DEFAULT
	 */
	public final static IFilterService DEFAULT = new FilterService();

	
	/**
	 * Call to register a filter with the service.
	 * 
	 * @param filter
	 */
	<T> void register(IFilter<T> filter);
	
	/**
	 * Call to register a filter with the service.
	 * 
	 * @param filter
	 */
	<T> void unregister(IFilter<T> filter);

	/**
	 * Filter the given list of items with a named filter.
	 * 
	 * If there is no filter with this name, the original list is returned.
	 * Use getFilter(...) to check if a filter is there.
     *
	 * @param filterName
	 * @param items
	 * @return
	 */
	<T> List<T> filter(String filterName, Collection<T> items);
	
	/**
	 * Filter the given list of items with a named filter.
	 * 
	 * @param filterName
	 * @param items
	 * @return
	 */
	<T> IFilter<T> getFilter(String filterName);

	/**
	 * Clears all the added filters.
	 * WARNING USe with caution, this will blat any spring created filters.
	 */
	void clear();
}
