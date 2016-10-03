package org.eclipse.scanning.malcolm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.MalcolmUtil;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that make the connection to the device and monitors its status.
 * 
 * Important things to do:
 * 1. The locking in AbstractMalcolmDevice should in theory not be needed, we will push this all into the connection.
 * 2. The Serializer to JSON, 'ObjectMapper mapper' must be abstacted out because real connection can be JSON or EPICSV4
 * 3. The Socket to ZeroMQ must be abstracted out because the real socket can be ZeroMQ or EPICSV4
 * 
 * @author Matthew Gerring
 *
 */
class MalcolmDevice<T> extends AbstractMalcolmDevice<T> {

	private static Logger logger = LoggerFactory.getLogger(MalcolmDevice.class);
		
	private IMalcolmConnectorService<MalcolmMessage>   service;
	private boolean                          alive;
    private MalcolmMessage                      stateSubscriber;
    private MalcolmMessage                      scanSubscriber;

	private IPublisher<ScanBean>             publisher;
	
	private static String STATE_ENDPOINT = "state";
	
	private static String STATUS_ENDPOINT = "status";
	
	private static String BUSY_ENDPOINT = "busy";
	
	private static String CURRENT_STEP_ENDPOINT = "currentStep";


	public MalcolmDevice(String name, IMalcolmConnectorService<MalcolmMessage> service, IPublisher<ScanBean> publisher) throws MalcolmDeviceException {
		
		super(service);
    	setName(name);
       	this.service   = service;
       	this.publisher = publisher;
		
    	final DeviceState currentState = getDeviceState();
		logger.debug("Connecting '"+getName()+"'. Current state: "+currentState);
		alive = true;
		
		stateSubscriber = connectionDelegate.createSubscribeMessage(STATE_ENDPOINT);
		service.subscribe(this, stateSubscriber, new IMalcolmListener<MalcolmMessage>() {
			
			@Override
			public void eventPerformed(MalcolmEvent<MalcolmMessage> e) {				
				try {
					sendScanStateChange(e);										
				} catch (Exception ne) {
					logger.error("Problem dispatching message!", ne);
				}
			}
		});		
		
		scanSubscriber  = connectionDelegate.createSubscribeMessage(CURRENT_STEP_ENDPOINT);
		service.subscribe(this, scanSubscriber, new IMalcolmListener<MalcolmMessage>() {
			
			@Override
			public void eventPerformed(MalcolmEvent<MalcolmMessage> e) {				
				try {
					sendScanEvent(e);										
				} catch (Exception ne) {
					logger.error("Problem dispatching message!", ne);
				}
			}
		});		
		
	}


	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) {
		NXdetector detector = createNexusObject(info);
		NexusObjectWrapper<NXdetector> prov = new NexusObjectWrapper<NXdetector>(getName(), detector);
		// TODO Find this out from the attributes of the device?
		prov.setExternalDatasetRank(NXdetector.NX_DATA, 4); // FIXME Malcolm1 can only to x and y scanning of a 2D detector.
		return prov;
	}

	public NXdetector createNexusObject(NexusScanInfo info) {
		
		// TODO Malcolm1 hard codes where the axes and detector write to. We do the same.
		final NXdetector detector = NexusNodeFactory.createNXdetector();
		detector.addExternalLink(NXdetector.NX_DATA, getFileName(), "/entry/data/det1");
		
		for (String axis : info.getScannableNames()) {
			detector.addExternalLink(axis+"_demand", getFileName(), "/entry/data/"+axis+"_demand");
		}
		return detector;
	}


	protected void sendScanEvent(MalcolmEvent<MalcolmMessage> e) throws Exception {
		
		MalcolmMessage msg      = e.getBean();
		DeviceState newState = MalcolmUtil.getState(msg, false);

		ScanBean bean = getBean();
		bean.setDeviceName(getName());
		bean.setPreviousDeviceState(bean.getDeviceState());
		if (newState!=null) {
			bean.setDeviceState(newState);
		}
		
		// FIXME need to send proper position.
		Object value = msg.getValue();
		if (value instanceof Map) {
			final Integer point = (Integer)((Map)value).get("value");
			bean.setPoint(point);
		}
		if (publisher!=null) publisher.broadcast(bean);
	}

	private MalcolmEventBean meb;
	protected void sendScanStateChange(MalcolmEvent<MalcolmMessage> e) throws Exception {
		
		MalcolmMessage msg = e.getBean();
		
		DeviceState newState = MalcolmUtil.getState(msg, false);
		
		// Send scan state changed
		ScanBean bean = getBean();
		bean.setDeviceName(getName());
		bean.setPreviousDeviceState(bean.getDeviceState());
		bean.setDeviceState(newState);
		if (publisher!=null) publisher.broadcast(bean);
		
		// We also send a malcolm event
		if (meb==null) meb = new MalcolmEventBean();
		meb.setDeviceName(getName());
		meb.setMessage(msg.getMessage());
		
		meb.setPreviousState(meb.getDeviceState());
		meb.setDeviceState(newState);
		
		if (msg.getType().isError()) { // Currently used for debugging the device.
			logger.error("Error message encountered: "+msg);
			Thread.dumpStack();
		}

		eventDelegate.sendEvent(meb);
	}

	@Override
	public DeviceState getDeviceState() throws MalcolmDeviceException {
		try {
			final MalcolmMessage message = connectionDelegate.createGetMessage(STATE_ENDPOINT);
			final MalcolmMessage reply   = service.send(this, message);
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(reply.getMessage());
			}

			return MalcolmUtil.getState(reply);
			
		} catch (MalcolmDeviceException mne) {
			throw mne;
			
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device "+getName(), ne);
		}
	}

	@Override
	public String getDeviceStatus() throws MalcolmDeviceException {
		try {
			final MalcolmMessage message = connectionDelegate.createGetMessage(STATUS_ENDPOINT);
			final MalcolmMessage reply   = service.send(this, message);
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(reply.getMessage());
			}

			return MalcolmUtil.getStatus(reply);
			
		} catch (MalcolmDeviceException mne) {
			throw mne;
			
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device "+getName(), ne);
		}
	}

	@Override
	public boolean isDeviceBusy() throws MalcolmDeviceException {
		try {
			final MalcolmMessage message = connectionDelegate.createGetMessage(BUSY_ENDPOINT);
			final MalcolmMessage reply   = service.send(this, message);
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(reply.getMessage());
			}

			return MalcolmUtil.getBusy(reply);
			
		} catch (MalcolmDeviceException mne) {
			throw mne;
			
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device "+getName(), ne);
		}
	}


	@Override
	public void validate(T params) throws MalcolmDeviceException {
		
		final MalcolmMessage msg   = connectionDelegate.createCallMessage("validate", params);
		final MalcolmMessage reply = service.send(this, msg);
        if (reply.getType()==Type.ERROR) {
        	throw new MalcolmDeviceException(reply.getMessage());
        }
	}
	
	@Override
	public void configure(T model) throws MalcolmDeviceException {
		final MalcolmMessage msg   = connectionDelegate.createCallMessage("configure", model);
		MalcolmMessage reply = service.send(this, msg);
        if (reply.getType() == Type.ERROR) {
        	throw new MalcolmDeviceException(reply.getMessage());
        }
		setModel(model);
	}

	@Override
	public void run(IPosition pos) throws MalcolmDeviceException {
		connectionDelegate.call(Thread.currentThread().getStackTrace(), DeviceState.RUNNING);
	}

	@Override
	public void abort() throws MalcolmDeviceException {
		connectionDelegate.call(Thread.currentThread().getStackTrace());
	}

	@Override
	public void disable() throws MalcolmDeviceException {
		connectionDelegate.call(Thread.currentThread().getStackTrace());
	}

	@Override
	public void reset() throws MalcolmDeviceException {
		connectionDelegate.call(Thread.currentThread().getStackTrace());
	}

	@Override
	public void pause() throws MalcolmDeviceException {
		connectionDelegate.call(Thread.currentThread().getStackTrace());
	}
	
	@Override
	public void resume() throws MalcolmDeviceException {
		connectionDelegate.call(Thread.currentThread().getStackTrace());
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		unsubscribe(stateSubscriber);
		unsubscribe(scanSubscriber);

		setAlive(false);
	}

	private final void unsubscribe(MalcolmMessage subscriber) throws MalcolmDeviceException {
		if (subscriber!=null) {
			final MalcolmMessage unsubscribeStatus = connectionDelegate.createUnsubscribeMessage();
			unsubscribeStatus.setId(subscriber.getId());
			service.unsubscribe(this, subscriber);
			logger.debug("Unsubscription "+getName()+" made "+unsubscribeStatus);
		}
	}

	@Override
	public boolean isLocked() throws MalcolmDeviceException {
		final DeviceState state = getDeviceState();
		return state.isTransient(); // Device is not locked but it is doing something.
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	@Override
	public DeviceState latch(long time, TimeUnit unit, final DeviceState... ignoredStates) throws MalcolmDeviceException {
		
		try {
			
			final CountDownLatch latch = new CountDownLatch(1);
			final List<DeviceState>     stateContainer     = new ArrayList<>(1);
			final List<Exception> exceptionContainer = new ArrayList<>(1);
			
			// Make a listener to check for state and then add it and latch
			IMalcolmListener<MalcolmMessage> stateChanger = new IMalcolmListener<MalcolmMessage>() {
				@Override
				public void eventPerformed(MalcolmEvent<MalcolmMessage> e) {
					MalcolmMessage msg = e.getBean();
					try {
						DeviceState state = MalcolmUtil.getState(msg);
						if (state != null) {
							if (ignoredStates!=null && Arrays.asList(ignoredStates).contains(state)) {
								return; // Found state that we don't want!
							}
						}
						stateContainer.add(state);
						latch.countDown();
						
					} catch (Exception ne) {
						exceptionContainer.add(ne);
						latch.countDown();
					}
				}
			};
				
			service.subscribe(this, stateSubscriber, stateChanger);
			
			boolean countedDown = false;
			if (time>0) {
				countedDown = latch.await(time, unit);
			} else {
				latch.await();
			}
	
			service.unsubscribe(this, stateSubscriber, stateChanger);
			
			if (exceptionContainer.size()>0) throw exceptionContainer.get(0);
			
			if (stateContainer.size() > 0) return stateContainer.get(0);
			
			if (countedDown) {
			    throw new Exception("The countdown of "+time+" "+unit+" timed out waiting for state change for device "+getName());
			} else {
				throw new Exception("A problem occured trying to latch state change for device "+getName());
			}
			
		} catch (MalcolmDeviceException ne) {
			throw ne;
			
		} catch (Exception neOther) {
			throw new MalcolmDeviceException(this, neOther);
		}

	}
	
	public Object getAttributeValue(String attribute) throws MalcolmDeviceException {
		String endpoint = attribute + ".value";
		final MalcolmMessage message = connectionDelegate.createGetMessage(endpoint);
		final MalcolmMessage reply   = service.send(this, message);
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(reply.getMessage());
		}
		return reply.getValue();
	}
	
	public List<MalcolmAttribute> getAllAttributes() throws MalcolmDeviceException {
		List<MalcolmAttribute> attributeList = new LinkedList<MalcolmAttribute>();
		String endpoint = "";
		final MalcolmMessage message = connectionDelegate.createGetMessage(endpoint);
		final MalcolmMessage reply   = service.send(this, message);
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(reply.getMessage());
		}
		
		Object wholeBlock = reply.getValue();
		
		if (wholeBlock instanceof Map) {
			Map wholeBlockMap = (Map)wholeBlock;
			
			for (Object entry : wholeBlockMap.values()) {
				if (entry instanceof MalcolmAttribute) {
					attributeList.add((MalcolmAttribute)entry);
				}
			}
		}
		
		return attributeList;
	}
}
