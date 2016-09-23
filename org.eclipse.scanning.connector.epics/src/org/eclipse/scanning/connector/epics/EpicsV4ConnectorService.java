package org.eclipse.scanning.connector.epics;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientMonitorRequester;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvaClient.PvaClientRPC;
import org.epics.pvaClient.PvaClientUnlistenRequester;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpicsV4ConnectorService implements IMalcolmConnectorService<MalcolmMessage> {

	static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    static final double REQUEST_TIMEOUT = 3.0;
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsV4ConnectorService.class); // TODO use
	
	private EpicsV4MessageMapper mapper;
	
	PvaClient pvaClient;
    
    private Map<Long, Collection<EpicsV4MonitorListener>> listeners;
    
    public EpicsV4ConnectorService() {
		mapper = new EpicsV4MessageMapper();
		org.epics.pvaccess.ClientFactory.start();
		this.listeners = new Hashtable<Long, Collection<EpicsV4MonitorListener>>(7);
		pvaClient = PvaClient.get("pva");
	}
    
	@Override
	public void connect(URI malcolmUri) throws MalcolmDeviceException {
		// TODO don't need uri as no centralised connection is needed for Malcolm Devices
	}

	@Override
	public void disconnect() throws MalcolmDeviceException {
		// TODO do more on disconnect?
        org.epics.pvaccess.ClientFactory.stop();
        pvaClient.destroy();
	}
	
	@Override
	public String marshal(Object anyObject) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	public PVStructure pvMarshal(Object anyObject) throws Exception {
		return mapper.pvMarshal(anyObject);
	}

	@Override
	public <U> U unmarshal(String anyObject, Class<U> beanClass) throws Exception {
		throw new UnsupportedOperationException();
	}

	public <U> U pvUnmarshal(PVStructure anyObject, Class<U> beanClass) throws Exception {
		return mapper.pvUnmarshal(anyObject, beanClass);
	}

	@Override
	public MalcolmMessage send(IMalcolmDevice device, MalcolmMessage message) throws MalcolmDeviceException {
		
		System.out.println("EV4CS - send:");
		System.out.println(message.toString());
		
		MalcolmMessage result = new MalcolmMessage();
				
		try {

			switch (message.getType()) {
			case CALL:
				result = sendCallMessage(device, message);
				break;
			case GET:
				result = sendGetMessage(device, message);
				break;
			case PUT:
				result = sendPutMessage(device, message);
				break;
			default:
				throw new Exception("Unexpected MalcolmMessage type: " + message.getType());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			result.setEndpoint(message.getEndpoint());
			result.setId(message.getId());
			result.setMessage("Error sending message " + message.getEndpoint() + ": " + e.getMessage());
			result.setType(Type.ERROR);
		}
		return result;
	}

	@Override
	public void subscribe(IMalcolmDevice device, MalcolmMessage msg, IMalcolmListener<MalcolmMessage> listener)
			throws MalcolmDeviceException {

		System.out.println("EV4CS - subscribe:");
		System.out.println(msg.toString());
		try {
			EpicsV4ClientMonitorRequester monitorRequester = new EpicsV4ClientMonitorRequester(listener, msg);
			PvaClientChannel pvaChannel = pvaClient.createChannel(device.getName(),"pva");
	        pvaChannel.issueConnect();
	        Status status = pvaChannel.waitConnect(REQUEST_TIMEOUT);
	        if(!status.isOK()) {
	        	String errMEssage = "Connect failed for " + device.getName() + "(" + status.getType() + ": " + status.getMessage() + ")";
	        	System.err.println(errMEssage); // TODO remove or log
	        	throw new Exception(errMEssage);
	        }
			
	        PvaClientMonitor monitor = pvaChannel.monitor(msg.getEndpoint(),monitorRequester,monitorRequester);
	        
	        System.out.println("S U B SCRIBED");
			Collection<EpicsV4MonitorListener> ls = listeners.get(msg.getId());
			if (ls == null) {
				ls = new Vector<EpicsV4MonitorListener>(3);
				listeners.put(msg.getId(), ls);
			}
			
			EpicsV4MonitorListener monitorListener = new EpicsV4MonitorListener(listener, monitor);
			ls.add(monitorListener);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("TODO REINSTATE THIS WHEN MONITOR IS IMPLEMENTED"); // TODO and log
			//throw new MalcolmDeviceException(device, ex.getMessage());
		}
	}

	@Override
	public MalcolmMessage unsubscribe(IMalcolmDevice device, MalcolmMessage msg, IMalcolmListener<MalcolmMessage>... removeListeners)
			throws MalcolmDeviceException {
		
		System.out.println("EV4CS - unsubscribe:");
		System.out.println(msg.toString());
		
		MalcolmMessage result = new MalcolmMessage();
		result.setType(Type.RETURN);
		result.setId(msg.getId());
		
		try {
			if (removeListeners==null) { // Kill every subscriber
				
				for (EpicsV4MonitorListener monitorListener : listeners.get(msg.getId()))
				{
					monitorListener.getMonitor().stop();
				}
				listeners.remove(msg.getId());
			} else {
				Collection<EpicsV4MonitorListener> ls = listeners.get(msg.getId());
				if (ls!=null) {
					
					ArrayList<EpicsV4MonitorListener> toRemove = new ArrayList<EpicsV4MonitorListener>();
					
					for (EpicsV4MonitorListener monitorListener : ls)
					{
						if (Arrays.asList(removeListeners).contains(monitorListener.getMalcolmListener()))
						{
							toRemove.add(monitorListener);
							monitorListener.getMonitor().stop();
						}
					}
					
					ls.removeAll(toRemove);
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage("Error unsubscribing from message Id " + msg.getId() + ": " + e.getMessage());
			result.setType(Type.ERROR);
			throw new MalcolmDeviceException(device, result.getMessage());
		}
		return result;
	}
	
	private MalcolmMessage sendGetMessage(IMalcolmDevice device, MalcolmMessage message) {

		MalcolmMessage returnMessage = new MalcolmMessage();
		PvaClientChannel pvaChannel = null;
		try {
			PVStructure pvResult = null;
			pvaChannel = pvaClient.createChannel(device.getName(),"pva");
	        pvaChannel.issueConnect();
	        Status status = pvaChannel.waitConnect(REQUEST_TIMEOUT);
	        if(!status.isOK()) {
	        	String errMEssage = "Connect failed for " + device.getName() + "(" + status.getType() + ": " + status.getMessage() + ")";
	        	System.err.println(errMEssage); // TODO remove or log
	        	throw new Exception(errMEssage);
	        }
			String requestString = message.getEndpoint();
	        PvaClientGet pvaGet = pvaChannel.createGet(requestString);
	        pvaGet.issueConnect();
	        status = pvaGet.waitConnect();
	        if(!status.isOK()) {
	        	String errMEssage = "CreateGet failed for " + requestString + "(" + status.getType() + ": " + status.getMessage() + ")";
	        	System.err.println(errMEssage); // TODO remove or log
	        	throw new Exception(errMEssage);
	    	}
	        PvaClientGetData pvaData = pvaGet.getData();
			pvResult = pvaData.getPVStructure();
	        returnMessage = mapper.convertGetPVStructureToMalcolmMessage(pvResult, message);
		} catch (Exception ex) {
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage(ex.getMessage());
		}
		
		if (pvaChannel != null) {
			pvaChannel.destroy();
		}
		
        return returnMessage;
	}
	
	private MalcolmMessage sendPutMessage(IMalcolmDevice device, MalcolmMessage message) {
		
		MalcolmMessage returnMessage = new MalcolmMessage();
        returnMessage.setType(Type.RETURN);
        returnMessage.setId(message.getId());
        
        if (message.getValue() == null) {
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage("Unable to set field value to null: " + message.getEndpoint());
        }

		PvaClientChannel pvaChannel = null;
		
		try {
			String requestString = message.getEndpoint();
			
			pvaChannel = pvaClient.createChannel(device.getName(),"pva");
			pvaChannel.issueConnect();
	        Status status = pvaChannel.waitConnect(REQUEST_TIMEOUT);
	        if(!status.isOK()) {
	        	String errMEssage = "Connect failed for " + device.getName() + "(" + status.getType() + ": " + status.getMessage() + ")";
	        	System.err.println(errMEssage); // TODO remove or log
	        	throw new Exception(errMEssage);
	        }
	        PvaClientPut pvaPut = pvaChannel.createPut(requestString);
	        pvaPut.issueConnect();
	        status = pvaPut.waitConnect();
	        if(!status.isOK()) {
	        	String errMEssage = "CreatePut failed for " + requestString + "(" + status.getType() + ": " + status.getMessage() + ")";
	        	System.err.println(errMEssage); // TODO remove or log
	        	throw new Exception(errMEssage);
	    	}
	        PvaClientPutData putData = pvaPut.getData();
	        PVStructure pvStructure = putData.getPVStructure();
	        
	        mapper.populatePutPVStructure(pvStructure, message);
	        
	        pvaPut.put();
        
		} catch (Exception ex) {
			ex.printStackTrace();
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage("Error putting value into field " + message.getEndpoint() + ": " + ex.getMessage());
		}
		
		if (pvaChannel != null) {
			pvaChannel.destroy();
		}
        
        return returnMessage;
	}
	
	private MalcolmMessage sendCallMessage(IMalcolmDevice device, MalcolmMessage message) {
		
		MalcolmMessage returnMessage = new MalcolmMessage();
		PvaClientChannel pvaChannel = null;
		
		try {
			PVStructure pvResult = null;
			PVStructure pvRequest = mapper.convertMalcolmMessageToPVStructure(message);

			// Mapper outputs two nested structures, one for the method, one for the parameters 
			PVStructure methodStructure = pvRequest.getStructureField("method");
			PVStructure parametersStructure = pvRequest.getStructureField("parameters");
			
			pvaChannel = pvaClient.createChannel(device.getName(),"pva");
			pvaChannel.issueConnect();
	        Status status = pvaChannel.waitConnect(REQUEST_TIMEOUT);
	        if(!status.isOK()) {
	        	String errMEssage = "Connect failed for " + device.getName() + "(" + status.getType() + ": " + status.getMessage() + ")";
	        	System.err.println(errMEssage); // TODO remove or log
	        	throw new Exception(errMEssage);
	        }
			
	        PvaClientRPC rpc = pvaChannel.createRPC(methodStructure);
	        rpc.issueConnect();
	        status = rpc.waitConnect();
	        if(!status.isOK()) {
	        	String errMEssage = "CreateRPC failed for " + message.getMethod() + "(" + status.getType() + ": " + status.getMessage() + ")";
	        	System.err.println(errMEssage); // TODO remove or log
	        	throw new Exception(errMEssage);
	    	}
	        pvResult = rpc.request(parametersStructure);
			returnMessage = mapper.convertCallPVStructureToMalcolmMessage(pvResult, message);
			pvaChannel.destroy();
		} catch (Exception ex) {
			ex.printStackTrace();
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage(ex.getMessage());
		}
		
		if (pvaChannel != null) {
			pvaChannel.destroy();
		}
		
        return returnMessage;		
	}
	
	public MessageGenerator<MalcolmMessage> createDeviceConnection(IMalcolmDevice device) throws MalcolmDeviceException {
		return (MessageGenerator<MalcolmMessage>) new EpicsV4MalcolmMessageGenerator(device, this);
	}

	@Override
	public MessageGenerator<MalcolmMessage> createConnection() {
		return (MessageGenerator<MalcolmMessage>) new EpicsV4MalcolmMessageGenerator(this);
	}
	
	@Override
	public Object createConnectionFactory(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class EpicsV4ClientMonitorRequester implements PvaClientMonitorRequester, PvaClientUnlistenRequester {
		private IMalcolmListener<MalcolmMessage> listener;
		private MalcolmMessage subscribeMessage;
		
		public EpicsV4ClientMonitorRequester(IMalcolmListener<MalcolmMessage> listener, MalcolmMessage subscribeMessage) {
			this.listener = listener;
			this.subscribeMessage = subscribeMessage;
		}

		@Override
		public void event(PvaClientMonitor monitor) {
			while (monitor.poll()) {
				PvaClientMonitorData monitorData = monitor.getData();

				MalcolmMessage message = new MalcolmMessage();
				try {
					message = mapper.convertSubscribeUpdatePVStructureToMalcolmMessage(monitorData.getPVStructure(), subscribeMessage);
				} catch (Exception ex) {
					message.setType(Type.ERROR);
					message.setMessage("Error converting subscription update: " + ex.getMessage());
				}
				listener.eventPerformed(new MalcolmEvent<MalcolmMessage>(message));
				monitor.releaseEvent();
			}
		}

		@Override
		public void unlisten(PvaClientMonitor arg0) {
			// TODO What to do when unlisten is called?
		}
	}

}
