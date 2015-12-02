package org.eclipse.scanning.api.malcolm.connector;

import java.util.Map;

import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.State;

/**
 * Deals with details of:
 * 1. Serializing JSON   (for instance Jackson)
 * 2. Sending string (for instance over zeromq)
 * 
 * 
 * @author fcp94556
 *
 */
public interface MessageGenerator<T> {

	/**
	 * Automatically generates a call for a given method by using the method name from the stack.
	 * 
	 * @param stackTrace
	 * @param running
	 * @throws MalcolmDeviceException
	 */
	T call(StackTraceElement[] stackTrace, State... states) throws MalcolmDeviceException;
	
	/**
	 * Create a get message
	 * @param endpointString
	 * @return
	 */
	T createGetMessage(String endpointString)  throws MalcolmDeviceException;

	/**
	 * Create a call message
	 * @param methodName
	 * @param params
	 * @return
	 * @throws MalcolmDeviceException
	 */
	T createCallMessage(String methodName, Object params) throws MalcolmDeviceException;

	
    /**
     * 
     * @param subscription - for example "stateMachine"
     * @return
     */
	T createSubscribeMessage(String subscription);


	/**
	 * 
	 * @return
	 */
	T createUnsubscribeMessage();

}
