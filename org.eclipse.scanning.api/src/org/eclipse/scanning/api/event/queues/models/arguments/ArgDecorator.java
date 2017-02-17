package org.eclipse.scanning.api.event.queues.models.arguments;

public abstract class ArgDecorator<P, V> implements IArg<V> {
	
	protected IArg<P> childArg;
	protected V value;
	
	protected ArgDecorator(IArg<P> childArg) {
		this.childArg = childArg;
	}
	
	@Override
	public V getValue() {
		return value;
	}
	
	@Override
	public void evaluate() {
		childArg.evaluate();
		processArg(childArg.getValue());
	}
	
	protected abstract void processArg(P parameter);
}
