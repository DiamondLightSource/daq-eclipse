package uk.ac.diamond.malcom.jacksonzeromq.connector;

import java.util.Map;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.State;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.malcolm.message.Type;

/**
 * Class to encapsulate the details of sending stuff.
 * 
 * @author fcp94556
 *
 */
class JsonMessageGenerator implements MessageGenerator<JsonMessage> {

	private IMalcolmDevice                 device;
	private IMalcolmConnectorService<JsonMessage> service;

	JsonMessageGenerator(IMalcolmConnectorService<JsonMessage> service) {
		this(null, service);
	}

	JsonMessageGenerator(IMalcolmDevice device, IMalcolmConnectorService<JsonMessage> service) {
		this.device  = device;
		this.service = service;
	}

	private static volatile long callCount = 0;
	
	private JsonMessage createMalcolmMessage() {
		JsonMessage ret = new JsonMessage();
		ret.setId(callCount++);
		return ret;
	}

	@Override
	public JsonMessage createSubscribeMessage(String subscription) {
		final JsonMessage msg = createMalcolmMessage();
		msg.setType(Type.SUBSCRIBE);
		msg.setEndpoint(device.getName()+"."+subscription);
		return msg;
	}

	@Override
	public JsonMessage createUnsubscribeMessage() {
		final JsonMessage msg = createMalcolmMessage();
		msg.setType(Type.UNSUBSCRIBE);
		return msg;
	}



	@Override
	public JsonMessage createGetMessage(String cmd) throws MalcolmDeviceException {
		final JsonMessage msg = createMalcolmMessage();
		msg.setType(Type.GET);
		msg.setEndpoint(cmd);
		return msg;
	}
	
	private JsonMessage createCallMessage(final String methodName) throws MalcolmDeviceException {
		final JsonMessage msg = createMalcolmMessage();
		msg.setType(Type.CALL);
		msg.setEndpoint(device.getName());
		msg.setMethod(methodName); 
		return msg;
	}
	@Override
	public JsonMessage createCallMessage(final String methodName, Object arg) throws MalcolmDeviceException {
		final JsonMessage msg = createCallMessage(methodName);
		msg.setArguments(arg);
		return msg;
	}
	
	@Override
	public JsonMessage call(StackTraceElement[] stackTrace, State... latches) throws MalcolmDeviceException {
		final JsonMessage msg   = createCallMessage(getMethodName(stackTrace));
		final JsonMessage reply = service.send(device, msg);
		// TODO What about state changes? Should we block?
		//if (latches!=null) latch(latches);
		return reply;
	}
	
	private static final String getMethodName ( StackTraceElement ste[] ) {  
		   
	    String methodName = "";  
	    boolean flag = false;  
	   
	    for ( StackTraceElement s : ste ) {  
	   
	        if ( flag ) {  
	   
	            methodName = s.getMethodName();  
	            break;  
	        }  
	        flag = s.getMethodName().equals( "getStackTrace" );  
	    }  
	    return methodName;  
	}
	

}
