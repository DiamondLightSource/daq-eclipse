package org.eclipse.scanning.api.event.queues.models.arguments;

public class Arg<P, V> implements IArg<V> {
	
	private V value;
	private P parameter;
	
	public Arg(P parameter) {
		this.parameter = parameter;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void evaluate() {
		value = (V)parameter;
	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	public P getParameter() {
//		return parameter;
//	}
	
	@Override
	public V getValue() {
		return value;
	}

}
