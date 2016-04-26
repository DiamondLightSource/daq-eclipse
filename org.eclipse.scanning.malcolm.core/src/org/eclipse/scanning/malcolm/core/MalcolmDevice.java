package org.eclipse.scanning.malcolm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.malcolm.message.MalcolmUtil;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.malcolm.models.MalcolmConnectionInfo;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
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
		
	private IMalcolmConnectorService<JsonMessage>   service;
	private boolean                          alive;
    private JsonMessage                      stateSubscriber;
    private JsonMessage                      scanSubscriber;

	private IPublisher<ScanBean>             publisher;


	public MalcolmDevice(String name, IMalcolmConnectorService<JsonMessage> service, IPublisher<ScanBean> publisher) throws MalcolmDeviceException {
		
		super(service);
    	setName(name);
       	this.service   = service;
       	this.publisher = publisher;
		
    	final DeviceState currentState = getDeviceState();
		logger.debug("Connecting '"+getName()+"'. Current state: "+currentState);
		alive = true;
		
		stateSubscriber = connectionDelegate.createSubscribeMessage("stateMachine.state");
		service.subscribe(this, stateSubscriber, new IMalcolmListener<JsonMessage>() {
			
			@Override
			public void eventPerformed(MalcolmEvent<JsonMessage> e) {				
				try {
					sendScanStateChange(e);										
				} catch (Exception ne) {
					logger.error("Problem dispatching message!", ne);
				}
			}
		});		
		
		scanSubscriber  = connectionDelegate.createSubscribeMessage("attributes.currentStep");
		service.subscribe(this, scanSubscriber, new IMalcolmListener<JsonMessage>() {
			
			@Override
			public void eventPerformed(MalcolmEvent<JsonMessage> e) {				
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
		DelegateNexusProvider prov = new DelegateNexusProvider<NXdetector>(getName(), NexusBaseClass.NX_DETECTOR, info, this);
		// TODO Find this out from the attributes of the device?
		prov.setExternalDatasetRank(NXdetector.NX_DATA, 4); // FIXME Malcolm1 can only to x and y scanning of a 2D detector.
		return prov;
	}

	@Override
	public NXdetector createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {
		
		// TODO Malcolm1 hard codes where the axes and detector write to. We do the same.
		final NXdetector detector = nodeFactory.createNXdetector();
		detector.addExternalLink(NXdetector.NX_DATA, getFileName(), "/entry/data/det1");
		
		for (String axis : info.getScannabkeNames()) {
			detector.addExternalLink(axis+"_demand", getFileName(), "/entry/data/"+axis+"_demand");
		}
		return detector;
	}


	protected void sendScanEvent(MalcolmEvent<JsonMessage> e) throws Exception {
		
		JsonMessage msg      = e.getBean();
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
	protected void sendScanStateChange(MalcolmEvent<JsonMessage> e) throws Exception {
		
		JsonMessage msg = e.getBean();
		
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
			final JsonMessage message = connectionDelegate.createGetMessage(getName()+".stateMachine.state");
			final JsonMessage reply   = service.send(this, message);
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
	public T validate(T params) throws MalcolmDeviceException {
		
		final JsonMessage msg   = connectionDelegate.createCallMessage("validate", params);
		final JsonMessage reply = service.send(this, msg);
        return (T)reply.getValue();
	}
	
	@Override
	public void configure(T model) throws MalcolmDeviceException {
		final JsonMessage msg   = connectionDelegate.createCallMessage("configure", model);
		service.send(this, msg);
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

	private final void unsubscribe(JsonMessage subscriber) throws MalcolmDeviceException {
		if (subscriber!=null) {
			final JsonMessage unsubscribeStatus = connectionDelegate.createUnsubscribeMessage();
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
			IMalcolmListener<JsonMessage> stateChanger = new IMalcolmListener<JsonMessage>() {
				@Override
				public void eventPerformed(MalcolmEvent<JsonMessage> e) {
					JsonMessage msg = e.getBean();
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
}
