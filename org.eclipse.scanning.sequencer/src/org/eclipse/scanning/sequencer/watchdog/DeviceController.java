package org.eclipse.scanning.sequencer.watchdog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <pre>
 	  Rules for controller:
	  o Pause proceeds and sets preference to pause.
	  o Resume is allowed if none of the states are paused.
	  o Seek is allowed if all names bar the current device are not paused.
	  o Abort is allowed regardless.
   </pre>
   
 * @author Matthew Gerring
 *
 * @param <T>
 */
class DeviceController implements IDeviceController {

	private static Logger logger = LoggerFactory.getLogger(DeviceController.class);
	
	private IPausableDevice<?> device;
	private List<?>            objects;
	private ScanBean           bean;
	
	/**
	 * Rules for states:
	 * Pause proceeds and sets preference to pause.
	 * Resume is allowed if none of the states are paused.
	 * Seek is allowed if all names bar the current device are not paused.
	 * Abort is allowed regardless.
	 */
	private Map<String, DeviceState> states;

	public DeviceController(IPausableDevice<?> device) {
		this.device = device;
		this.states = new HashMap<>(3);
	}

	/**
	 * Only pauses the delegate if is running, otherwise returns
	 * silently.
	 */
	public void pause(String id, DeviceWatchdogModel model) throws ScanningException {
		
		states.put(id, DeviceState.PAUSED);
		if (device.getDeviceState()!=DeviceState.RUNNING) return; // Cannot pause it.
		if (bean!=null&&model!=null) bean.setMessage(model.getMessage());
		logger.debug("Controller pausing on "+getName()+" because of id "+id);
		device.pause();
	}
	public void seek(String id, int stepNumber) throws ScanningException {
		
		// If any of the others think it should be paused, we do not resume
		Map<String, DeviceState> copy = new HashMap<>(states);
		copy.put(id, DeviceState.RUNNING);
		if (!canResume(copy)) return;

		device.seek(stepNumber);
	}

	public void resume(String id) throws ScanningException {
		
		states.put(id, DeviceState.RUNNING);
		if (device.getDeviceState()!=DeviceState.PAUSED) return; // Cannot resume it.
		
		// If any of the others think it should be paused, we do not resume
		if (!canResume(states)) return;
		
		logger.debug("Controller resuming on "+getName()+" because of id "+id);
		device.resume();
	}
	
	private static final boolean canResume(Map<String, DeviceState> states) {
		List<DeviceState> paused = states.values().stream().filter(state -> state==DeviceState.PAUSED).collect(Collectors.toList());
		if (paused!=null && !paused.isEmpty()) return false;
		return true;
	}

	public void abort(String id) throws ScanningException {
		logger.debug("Controller aborting on "+getName()+" because of id "+id);
		device.abort();
	}

	public IPausableDevice<?> getDevice() {
		return device;
	}

	public void setDevice(IPausableDevice<?> device) {
		this.device = device;
	}

	public List<?> getObjects() {
		return objects;
	}

	public void setObjects(List<?> objects) {
		this.objects = objects;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	public ScanBean getBean() {
		return bean;
	}

	public void setBean(ScanBean bean) {
		this.bean = bean;
	}
}
