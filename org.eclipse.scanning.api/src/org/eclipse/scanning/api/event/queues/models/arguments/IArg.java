package org.eclipse.scanning.api.event.queues.models.arguments;

public interface IArg<V> {
	
	public void evaluate();
	
//	public <P> P getParameter();
	
	public V getValue();

}
