package org.eclipse.scanning.api.event.queues.models.arguments;

import java.util.Map;

public class LookupArg<P, V> extends ArgDecorator<P, V> {
	
	private Map<P, V> lookupTable;

	public LookupArg(IArg<P> childArg, Map<P, V> lookupTable) {
		super(childArg);
		this.lookupTable = lookupTable;
	}

	@Override
	protected void processArg(P parameter) {
		value = lookupTable.get(parameter);
	}

}
