package org.eclipse.scanning.malcolm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.MalcolmUtil;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.points.mutators.FixedDurationMutator;
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
public class MalcolmDevice<M extends MalcolmModel> extends AbstractMalcolmDevice<M> {
	
	public static final class EpicsMalcolmModel {
		private final IPointGenerator<?> generator;
		private final List<String> axesToMove;
		private final String fileDir;

		public EpicsMalcolmModel(String fileDir, List<String> axesToMove,
				IPointGenerator<?> generator) {
			this.fileDir = fileDir;
			this.axesToMove = axesToMove;
			this.generator = generator;
		}

		public String getFileDir() {
			return fileDir;
		}

		public List<String> getAxesToMove() {
			return axesToMove;
		}

		public IPointGenerator<?> getGenerator() {
			return generator;
		}
		
	}
	


	private static Logger logger = LoggerFactory.getLogger(MalcolmDevice.class);
		
	private boolean                          alive;
    private MalcolmMessage                      stateSubscriber;
    private MalcolmMessage                      scanSubscriber;

	private IPublisher<ScanBean>             publisher;
	
	private MalcolmEventBean meb;

	private static String STATE_ENDPOINT = "state";
	
	private static String STATUS_ENDPOINT = "status";
	
	private static String BUSY_ENDPOINT = "busy";
	
	private static String CURRENT_STEP_ENDPOINT = "completedSteps";

	public MalcolmDevice() throws MalcolmDeviceException {
		super(Services.getConnectorService(), Services.getRunnableDeviceService());
	}

	public MalcolmDevice(String name,
			IMalcolmConnectorService<MalcolmMessage> service,
			IRunnableDeviceService runnableDeviceService,
			IPublisher<ScanBean> publisher) throws MalcolmDeviceException {
		super(service, runnableDeviceService);
    	setName(name);
       	this.publisher = publisher;
	}
	
	public void register() {
		super.register();
		
		try {
			initialize();
		} catch (MalcolmDeviceException e) {
			logger.error("Could not initialize malcolm device " + getName(), e);
		}
	}
	
	public void initialize() throws MalcolmDeviceException {
    	final DeviceState currentState = getDeviceState();
		logger.debug("Connecting '"+getName()+"'. Current state: "+currentState);
		alive = true;
		
		stateSubscriber = connectionDelegate.createSubscribeMessage(STATE_ENDPOINT);
		connector.subscribe(this, stateSubscriber, new IMalcolmListener<MalcolmMessage>() {
			
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
		connector.subscribe(this, scanSubscriber, new IMalcolmListener<MalcolmMessage>() {
			
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


	protected void sendScanEvent(MalcolmEvent<MalcolmMessage> e) throws Exception {
		
		MalcolmMessage msg      = e.getBean();
		DeviceState newState = MalcolmUtil.getState(msg, false);

		ScanBean bean = getBean();
		bean.setDeviceName(getName());
		bean.setPreviousDeviceState(bean.getDeviceState());
		if (newState!=null) {
			bean.setDeviceState(newState);
		}
		
		Object value = msg.getValue();
		if (value instanceof Map) {
			final Integer point = (Integer)((Map)value).get("value");
			bean.setPoint(point);
		} else if (value instanceof NumberAttribute) {
			final Integer point = (Integer)((NumberAttribute)value).getValue();
			bean.setPoint(point);
		}
		if (publisher!=null) publisher.broadcast(bean);
	}

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
			final MalcolmMessage reply   = connector.send(this, message);
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(reply.getMessage());
			}

			return MalcolmUtil.getState(reply);
			
		} catch (MalcolmDeviceException mne) {
			throw mne;
			
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device '" + getName() + "'", ne);
		}
	}

	@Override
	public String getDeviceStatus() throws MalcolmDeviceException {
		try {
			final MalcolmMessage message = connectionDelegate.createGetMessage(STATUS_ENDPOINT);
			final MalcolmMessage reply   = connector.send(this, message);
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(reply.getMessage());
			}

			return MalcolmUtil.getStatus(reply);
			
		} catch (MalcolmDeviceException mne) {
			throw mne;
			
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device '" + getName() + "'", ne);
		}
	}

	@Override
	public boolean isDeviceBusy() throws MalcolmDeviceException {
		try {
			final MalcolmMessage message = connectionDelegate.createGetMessage(BUSY_ENDPOINT);
			final MalcolmMessage reply   = connector.send(this, message);
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(reply.getMessage());
			}

			return MalcolmUtil.getBusy(reply);
			
		} catch (MalcolmDeviceException mne) {
			throw mne;
			
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device '" + getName() + "'", ne);
		}
	}


	@Override
	public void validate(M params) throws MalcolmDeviceException {
		logger.info("validate params = " + params);
		final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(params);
		final MalcolmMessage msg   = connectionDelegate.createCallMessage("validate", epicsModel);
		final MalcolmMessage reply = connector.send(this, msg);
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(reply.getMessage());
		}
	}
	
	@Override
	public void configure(M model) throws MalcolmDeviceException {
		
		// Reset the device before configure in case it's in a fault state
		try {
			reset();
		} catch (Exception ex) {
			// Swallow the error as it might throw one if in a non-resetable state
		}
		
		logger.info("configure model = " + model);
		final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(model);
		final MalcolmMessage msg   = connectionDelegate.createCallMessage("configure", epicsModel);
		MalcolmMessage reply = connector.send(this, msg);
		if (reply.getType() == Type.ERROR) {
			throw new MalcolmDeviceException(reply.getMessage());
		}
		setModel(model);
	}

	private EpicsMalcolmModel createEpicsMalcolmModel(M model) {
		logger.info("createEpicsMalcolmModel model = " + model);
		double exposureTime = model.getExposureTime();
		IPointGenerator<?> pointGenerator = getPointGenerator();
		if (pointGenerator != null) { // TODO could the point generator be null here?
			List<IMutator> mutators = Arrays.asList(new FixedDurationMutator(exposureTime));
			((CompoundModel<?>) pointGenerator.getModel()).setMutators(mutators);
		}
		
		final EpicsMalcolmModel epicsModel = new EpicsMalcolmModel(model.getFileDir(),
				model.getAxesToMove(), pointGenerator);
		return epicsModel;
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
			connector.unsubscribe(this, subscriber);
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
				
			connector.subscribe(this, stateSubscriber, stateChanger);
			
			boolean countedDown = false;
			if (time>0) {
				countedDown = latch.await(time, unit);
			} else {
				latch.await();
			}
	
			connector.unsubscribe(this, stateSubscriber, stateChanger);
			
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
	
	public Object getAttribute(String attribute) throws MalcolmDeviceException {
		final MalcolmMessage message = connectionDelegate.createGetMessage(attribute);
		final MalcolmMessage reply   = connector.send(this, message);
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(reply.getMessage());
		}
		return reply.getValue();
	}
	
	public List<MalcolmAttribute> getAllAttributes() throws MalcolmDeviceException {
		List<MalcolmAttribute> attributeList = new LinkedList<MalcolmAttribute>();
		String endpoint = "";
		final MalcolmMessage message = connectionDelegate.createGetMessage(endpoint);
		final MalcolmMessage reply   = connector.send(this, message);
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
	
	/**
	 * Gets the value of an attribute on the device
	 */
	public Object getAttributeValue(String attributeName) throws MalcolmDeviceException {
		Object attribute = getAttribute(attributeName);
		if (attribute instanceof MalcolmAttribute) {
			MalcolmAttribute malcolmAttribute = (MalcolmAttribute)attribute;
			return malcolmAttribute.getValue();
		}
		return attribute;
	}
}
