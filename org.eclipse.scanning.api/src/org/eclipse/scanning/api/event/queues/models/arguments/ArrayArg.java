package org.eclipse.scanning.api.event.queues.models.arguments;

public class ArrayArg<P, V> extends ArgDecorator<P, V> {
	
	private V[] valuesArray;
	private int index;
	
	public ArrayArg(IArg<P> childArg) {
		super(childArg);
	}
	
	public ArrayArg(IArg<P> childArg, int index) {
		super(childArg);
		this.index = index;
	}
	
	public ArrayArg(IArg<P> childArg, V[] valuesArray) {
		super(childArg);
		this.valuesArray = valuesArray;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void processArg(P parameter) {
		if (valuesArray == null) valuesArray = (V[])parameter;
		value = valuesArray[index];
	}
	
	public V index(int index) {
		if (valuesArray == null) { 
			evaluate();
		}
		return valuesArray[index];
	}

}
