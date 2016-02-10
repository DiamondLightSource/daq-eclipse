package org.eclipse.scanning.api.malcolm.event;

import java.util.EventObject;

/**
 * Event object used notify of Malcolm events inside
 * same VM as IMalcolmService. A workaround for connecting to 
 * JMS and getting the topic if one does not want to deal with
 * JMS.
 * 
 * @author Matthew Gerring
 *
 */
public class MalcolmEvent<T> extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1945090670224642819L;
	private final String json;

	public MalcolmEvent(T bean) {
		this(null, bean);
	}
	public MalcolmEvent(String json, T bean) {
		super(bean);
		this.json = json;
	}
	
	/**
	 * Convenience method
	 * @return
	 */
	public T getBean() {
		return (T)getSource();
	}

	public String getJson() {
		return json;
	}

}
