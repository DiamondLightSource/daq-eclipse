package org.eclipse.scanning.api.event;

import java.net.URI;

/**
 * A service which provides the marshalling (the implementation uses Jackson) and
 * the JMS service provider (the implementation uses ActiveMQ)
 * 
 * @author fcp94556
 *
 */
public interface IEventConnectorService {

	/**
	 * May be used to use the serializer to marshal any object using its serilization routine.
	 * This can be used for objects other than T
	 * @param event
	 * @return
	 */
	String marshal(Object anyObject) throws Exception;

	/**
	 * May be used to get the serializer to unmarshal any object using its serialization routine.
	 * This can be used for objects other than T
	 * @param event
	 * @return
	 */
	<U> U unmarshal(String anyObject, Class<U> beanClass) throws Exception;

	/**
	 * Create a connection factory for sending events. This method 
	 * may return null or a class implementing javax.jms.ConnectionFactory
	 * or javax.jms.QueueConnectionFactory
	 * 
	 * @param uri
	 * @return
	 */
	Object createConnectionFactory(URI uri);

}
