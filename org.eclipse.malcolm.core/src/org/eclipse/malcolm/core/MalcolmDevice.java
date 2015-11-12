package org.eclipse.malcolm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.malcolm.api.MalcolmDeviceException;
import org.eclipse.malcolm.api.State;
import org.eclipse.malcolm.api.connector.IMalcolmConnectorService;
import org.eclipse.malcolm.api.event.IMalcolmListener;
import org.eclipse.malcolm.api.event.MalcolmEvent;
import org.eclipse.malcolm.api.event.MalcolmEventBean;
import org.eclipse.malcolm.api.message.JsonMessage;
import org.eclipse.malcolm.api.message.MalcolmUtil;
import org.eclipse.malcolm.api.message.Type;
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
public class MalcolmDevice extends AbstractMalcolmDevice {

	private static Logger logger = LoggerFactory.getLogger(MalcolmDevice.class);
		
	private IMalcolmConnectorService<JsonMessage>   service;
	private boolean                          alive;
    private JsonMessage                      subscribeStateMachine;


	public MalcolmDevice(String name, IMalcolmConnectorService<JsonMessage> service) throws MalcolmDeviceException {
		
		super(service);
    	this.name    = name;
    	this.service = service;
		
    	final State currentState = getState();
		logger.debug("Connecting '"+getName()+"'. Current state: "+currentState);
		alive = true;
		
		subscribeStateMachine = connectionDelegate.createSubscribeMessage("stateMachine");
		
		service.subscribe(this, subscribeStateMachine, new IMalcolmListener<JsonMessage>() {
			
			private State previousState = currentState;
			@Override
			public void eventPerformed(MalcolmEvent<JsonMessage> e) {
				
				State newState=null;
				try {
					JsonMessage msg = e.getBean();
					MalcolmEventBean meb = new MalcolmEventBean();
					meb.setDeviceName(getName());
					meb.setMessage(msg.getMessage());
					
					newState = MalcolmUtil.getState(msg, false);
					meb.setState(newState);
					
                    meb.setScanStart(newState!=previousState && newState==State.RUNNING);
					meb.setScanEnd(previousState==State.RUNNING && newState.isPostRun());
					
					if (msg.getType().isError()) {
						logger.error("Error message encountered: "+msg);
						Thread.dumpStack();
					}
	
					// TODO No enough information is being broadcast about scan..
					eventDelegate.sendEvent(meb);
					
				} catch (Exception ne) {
					logger.error("Problem dispatching message!", ne);
				} finally {
					if (newState!=null) previousState = newState;
				}
			}
		});			
	}

	@Override
	public State getState() throws MalcolmDeviceException {
		try {
			final JsonMessage message = connectionDelegate.createGetMessage(name+".stateMachine.state");
			final JsonMessage reply   = service.send(this, message);
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(reply.getMessage());
			}

			return MalcolmUtil.getState(reply);
			
		} catch (MalcolmDeviceException mne) {
			throw mne;
			
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device "+name, ne);
		}
	}


	@Override
	public Map<String, Object> validate(Map<String, Object> params) throws MalcolmDeviceException {
		
		final JsonMessage msg   = connectionDelegate.createCallMessage("validate", params);
		final JsonMessage reply = service.send(this, msg);
        return (Map<String, Object>)reply.getValue();
	}
	
	@Override
	public void configure(Map<String, Object> params) throws MalcolmDeviceException {
		final JsonMessage msg   = connectionDelegate.createCallMessage("configure", params);
		service.send(this, msg);
	}

	@Override
	public void run() throws MalcolmDeviceException {
		// Run will block.
		connectionDelegate.call(Thread.currentThread().getStackTrace(), State.RUNNING);
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
		if (subscribeStateMachine!=null) {
			final JsonMessage unsubscribeStatus = connectionDelegate.createUnsubscribeMessage();
			unsubscribeStatus.setId(subscribeStateMachine.getId());
			service.unsubscribe(this, unsubscribeStatus);
			logger.debug("Unsubscription "+getName()+" made "+unsubscribeStatus);
		}
		setAlive(false);
	}

	@Override
	public boolean isLocked() throws MalcolmDeviceException {
		final State state = getState();
		return state.isTransient(); // Device is not locked but it is doing something.
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	@Override
	public State latch(long time, TimeUnit unit, State... ignoredStates) throws MalcolmDeviceException {
		
		try {
			
			final CountDownLatch latch = new CountDownLatch(1);
			final List<State>     stateContainer     = new ArrayList<>(1);
			final List<Exception> exceptionContainer = new ArrayList<>(1);
			
			// Make a listener to check for state and then add it and latch
			IMalcolmListener<JsonMessage> stateChanger = new IMalcolmListener<JsonMessage>() {
				@Override
				public void eventPerformed(MalcolmEvent<JsonMessage> e) {
					JsonMessage msg = e.getBean();
					try {
						State state = MalcolmUtil.getState(msg);
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
				
			service.subscribe(this, subscribeStateMachine, stateChanger);
			
			boolean countedDown = false;
			if (time>0) {
				countedDown = latch.await(time, unit);
			} else {
				latch.await();
			}
	
			service.unsubscribe(this, subscribeStateMachine, stateChanger);
			
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
