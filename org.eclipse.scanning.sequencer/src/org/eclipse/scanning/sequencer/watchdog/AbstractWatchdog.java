package org.eclipse.scanning.sequencer.watchdog;

import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.sequencer.ServiceHolder;

public abstract class AbstractWatchdog implements IDeviceWatchdog {
	
	protected DeviceWatchdogModel model;
	protected IPausableDevice<?>  device;
	protected ScanBean bean;

	public AbstractWatchdog() {
		this(null);
	}
	public AbstractWatchdog(DeviceWatchdogModel model2) {
		this.model = model2;
	}
	
	public DeviceWatchdogModel getModel() {
		return model;
	}
	public void setModel(DeviceWatchdogModel model) {
		this.model = model;
	}

	
	protected long getValue(PositionEvent evt, String name, String unit) {
		double pos = evt.getPosition().getValue(name);
		return getValue(pos, unit);
	}

	protected long getValue(String name, String unit) throws Exception {
	    IScannable<Number> scannable = getScannable(name);
		return getValue(scannable.getPosition().doubleValue(), unit);
	}

	protected long getValue(double pos, String unit) {
		TimeUnit tu = getTimeUnit(unit);
		return tu.toMillis(Math.round(pos)); // Assuming that they do not use double and seconds and assume fraction is maintained.
	}

	protected <T> IScannable<T> getScannable(String name) throws ScanningException {
		IScannableDeviceService cservice = ServiceHolder.getRunnableDeviceService().getDeviceConnectorService();
		return cservice.getScannable(name);
	}
	
	private static final TimeUnit getTimeUnit(String unit) {
		TimeUnit tu = TimeUnit.SECONDS;
		if (unit!=null) {
			if ("s".equalsIgnoreCase(unit))  tu = TimeUnit.SECONDS;
			if ("seconds".equalsIgnoreCase(unit))  tu = TimeUnit.SECONDS;
			if ("ms".equalsIgnoreCase(unit)) tu = TimeUnit.MILLISECONDS;
			if ("milliseconds".equalsIgnoreCase(unit)) tu = TimeUnit.MILLISECONDS;
			if ("m".equalsIgnoreCase(unit)) tu = TimeUnit.MINUTES;
			if ("min".equalsIgnoreCase(unit)) tu = TimeUnit.MINUTES;
		}
		return tu;
	}

	/**
	 * Used by spring
	 */
	@Override
	public void activate() {
		ServiceHolder.getWatchdogService().register(this);
	}
	public void deactivate() {
		ServiceHolder.getWatchdogService().unregister(this);
	}
	public IPausableDevice<?> getDevice() {
		return device;
	}
	@Override
	public void setDevice(IPausableDevice<?> device) {
		this.device = device;
	}
	public ScanBean getBean() {
		return bean;
	}
	public void setBean(ScanBean bean) {
		this.bean = bean;
	}

}
