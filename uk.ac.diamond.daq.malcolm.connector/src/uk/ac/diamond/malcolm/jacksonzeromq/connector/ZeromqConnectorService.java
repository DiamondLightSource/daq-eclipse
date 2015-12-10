package uk.ac.diamond.malcolm.jacksonzeromq.connector;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ZeromqConnectorService implements IMalcolmConnectorService<JsonMessage> {
	
	static {
		System.out.println("Started "+IMalcolmConnectorService.class.getSimpleName());
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ZeromqConnectorService.class);

	private Context                                   context;

	private URI                                       uri;
	private Socket                                    socket;
	
	private static int socketCount = 0;
	private String inprocUri;
	private Socket                                    sender;
	
	// Malc Id -> Zeromq Id
	private Map<Long, byte[]>                         idMap; // TODO Subscribes memory leak into this map at the moment.
	
	private Map<Long, Collection<IMalcolmListener<JsonMessage>>>  listeners;
	private boolean                                   alive;
	private ObjectMapper                              mapper;
	
	private static final String FINISH = "finish";

	
	private Thread brokerThread;

	public void connect(URI uri) throws MalcolmDeviceException {
	    
		if (socket!=null) throw new MalcolmDeviceException("Connector is already busy! Currently each connector may only have one connection! Call disconnect and try again.");
		
	    this.context = ZMQ.context(1);
	    
		this.socket = context.socket(ZMQ.DEALER);			
		this.uri    = uri;
		socket.connect(uri.toString());

		this.sender = context.socket(ZMQ.ROUTER);
		this.sender.setRouterMandatory(true);
		this.inprocUri   = "inproc://malcolm"+(socketCount++);
		sender.bind(inprocUri);

		this.mapper = createJacksonMapper();
		
		// Latches to deal with threading, not ideal, overly complex?
		this.idMap        = new Hashtable<Long, byte[]>(7);
		this.listeners    = new Hashtable<Long, Collection<IMalcolmListener<JsonMessage>>>(7);
		
		setAlive(true);
		
		brokerThread = createMonitorThread();
		brokerThread.setName("0MQ Message Broker "+socket.toString());
		brokerThread.setDaemon(true);		
		brokerThread.start();
	}

	private Thread createMonitorThread() {
		return new Thread(new Runnable() {
			public void run() {
				
				if (mapper==null) mapper = createJacksonMapper();
				
				ZMQ.Poller items = new ZMQ.Poller(2);
		        items.register(socket, ZMQ.Poller.POLLIN); // Read
		        items.register(sender, ZMQ.Poller.POLLIN); // Write
		        
				while(isAlive()) {
					
					items.poll();
					if (items.pollin(0)) { // Read
						String received = socket.recvStr();
					    messageRead(mapper, received);
					}
					
					if (items.pollin(1)) {
						byte[] zmqid    = sender.recv(0);
						String malcid   = sender.recvStr();
						String received = sender.recvStr();
						
						if (FINISH.equals(received)) {
							logger.debug("Exiting "+Thread.currentThread().getName());
							break;
						}
						idMap.put(Long.parseLong(malcid), zmqid);

		                socket.send(received);
					}
				}

			}
		});
	}
	
	private void messageRead(ObjectMapper mapper, String received) {
        try {
        	JsonMessage msg = (JsonMessage)mapper.readValue(received, JsonMessage.class);
        	
        	if (listeners.containsKey(msg.getId())) {
        		
        		// TODO Do not use the 0MQ socket thread to despatch events?
        		Collection<IMalcolmListener<JsonMessage>> ls = listeners.get(msg.getId());
        		IMalcolmListener<JsonMessage>[] snapshot = ls.toArray(new IMalcolmListener[ls.size()]);
        		for (IMalcolmListener<JsonMessage> l : snapshot) l.eventPerformed(new MalcolmEvent<JsonMessage>(msg));
        		
        	} else if (idMap.containsKey(msg.getId())) {
        		
            	byte[] zmqId = idMap.remove(msg.getId());
        		sender.sendMore(zmqId);
        		sender.send(received);
        		
        	} else {
        		throw new RuntimeException("ZMQ Id not in idMap!");
        	}
        							
		} catch (Exception e) {
			logger.error("Something went wrong talking to ZeroMQ Malcolm!", e);
			e.printStackTrace();
		}
	}

	private final ObjectMapper createJacksonMapper() {
		
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(IPosition.class,   new PositionSerializer());
		module.addDeserializer(IPosition.class, new PositionDeserializer());
		module.addSerializer(DeviceState.class,   new StateSerializer());
		module.addDeserializer(DeviceState.class, new StateDeserializer());
		module.addSerializer(Type.class,    new TypeSerializer());
		module.addDeserializer(Type.class,  new TypeDeserializer());
		mapper.registerModule(module);	
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper;
	}


	@Override
	public void disconnect() throws MalcolmDeviceException {
		
		setAlive(false);
		
		ZMQ.Socket connection = context.socket(ZMQ.DEALER);
		connection.connect(inprocUri);       
		connection.sendMore(String.valueOf(-1)); // A thread will send it for us.		
		connection.send(FINISH); // A thread will send it for us.		
		connection.close();
		
		socket.disconnect(uri.toString());
		socket = null;
		sender.disconnect(inprocUri);
		sender = null;
		
		idMap.clear();
		
		try {
			Thread.sleep(100);
			brokerThread.interrupt(); // Just in case.
		} catch (InterruptedException e) {
			logger.error("Cannot suspend calling thread then interrupt helper threads.", e);
		}
	}

	@Override
	public MessageGenerator<JsonMessage> createDeviceConnection(IMalcolmDevice device) throws MalcolmDeviceException {

		return (MessageGenerator<JsonMessage>) new JsonMessageGenerator(device, this);
	}

	@Override
	public MessageGenerator<JsonMessage> createConnection() {
		return (MessageGenerator<JsonMessage>) new JsonMessageGenerator(this);
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	

	@Override
	public String marshal(Object anyObject) throws Exception {
		if (mapper==null) mapper = createJacksonMapper();
		return mapper.writeValueAsString(anyObject);
	}


	@Override
	public <U> U unmarshal(String string, Class<U> beanClass) throws Exception {
		if (mapper==null) mapper = createJacksonMapper();
		return (U)mapper.readValue(string, beanClass);
	}

	@Override
	public JsonMessage send(IMalcolmDevice device, JsonMessage msg) throws MalcolmDeviceException {
        JsonMessage reply = this.communicate(device, msg, true);
		if (reply!=null && reply.getType().isError()) throw new MalcolmDeviceException(device, reply.getMessage());
        return reply;
	}
	
	@Override
	public void subscribe(IMalcolmDevice device, JsonMessage msg, IMalcolmListener<JsonMessage> listener) throws MalcolmDeviceException {
		this.communicate(device, msg, false);
		
		Collection<IMalcolmListener<JsonMessage>> ls = listeners.get(msg.getId());
		if (ls == null) {
			ls = new Vector<IMalcolmListener<JsonMessage>>(3);
			listeners.put(msg.getId(), ls);
		}
		ls.add(listener);
	}

	@Override
	public JsonMessage unsubscribe(IMalcolmDevice device, JsonMessage msg, IMalcolmListener<JsonMessage>... removeListeners) throws MalcolmDeviceException {
		
		if (removeListeners==null) { // Kill ever subscribe
			JsonMessage reply = this.communicate(device, msg, false);
			listeners.remove(msg.getId());
			if (reply!=null && reply.getType().isError()) throw new MalcolmDeviceException(device, reply.getMessage());
			return reply;
			
		} else {
			Collection<IMalcolmListener<JsonMessage>> ls = listeners.get(msg.getId());
			if (ls!=null) {
				ls.removeAll(Arrays.asList(removeListeners));
				
				if (ls.isEmpty()) {
					JsonMessage reply = this.communicate(device, msg, false);
					listeners.remove(msg.getId());
					if (reply!=null && reply.getType().isError()) throw new MalcolmDeviceException(device, reply.getMessage());
					return reply;
				}
			}
			return null;
		}
 	}
	
	protected JsonMessage communicate(IMalcolmDevice device, JsonMessage msg, boolean block) throws MalcolmDeviceException {
		
		if (!isAlive())	throw new MalcolmDeviceException(device, "Cannot send message to "+device.getName()+" we are not connected!");
		

		final String message = toString(device, msg);

		ZMQ.Socket connection = context.socket(ZMQ.DEALER);
		try {
			connection.connect(inprocUri);  
			
			connection.sendMore(String.valueOf(msg.getId())); // A thread will send it for us.		
			connection.send(message); // A thread will send it for us.		
			
			if (block) {
			    try {
					if (!isAlive())	throw new MalcolmDeviceException(device, "Cannot send message to "+device.getName()+" we are not connected!");
					String received = connection.recvStr(); // Blocks until message is returned.
					return (JsonMessage)mapper.readValue(received, JsonMessage.class);
	
			    } catch (MalcolmDeviceException mde) {
					throw mde;
				} catch (Exception e) {
					throw new MalcolmDeviceException(device, "Cannot latch waiting for message of id "+msg.getId());
				}
			} else {
				return null;
			}
			
		} finally {
			connection.close();
		}
		
	}

	private String toString(IMalcolmDevice device, JsonMessage msg) throws MalcolmDeviceException {
		try {
			if (mapper==null) mapper = createJacksonMapper();
		    return mapper.writeValueAsString(msg);
		} catch (Exception ne) {
			throw new MalcolmDeviceException(device, "Cannot serialize object to JSON "+msg, ne);
		}
	}

	@Override
	public Object createConnectionFactory(URI uri) {
		return new ActiveMQConnectionFactory(uri);		
	}

}
