package org.eclipse.scanning.sequencer.watchdog;

import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This watchdog may be started to run with a scan.
 * 
 * It will attempt to pause a scan when topup is about 
 * to happen and restart it after topup has finished.
 * 
 * Once made a watch dog is active if the activate method
 * is called. The deactivate method may be called to stop
 * a given watchdog watching scans.
 * 
 * https://en.wikipedia.org/wiki/Watchdog_timer
 * 
 * NOTE This class will usually be created in spring
 * 
 * Note there are 3 PVs to describe the state. 
 * A typical top-up event takes around 20s and in low-alpha mode this can be longer, maybe around a minute.
 * Will Rogers and Nick Battam are the ones who know and maintain this page.
 * If you are missing information on this page, please ask them to explain and possibly add information to the page.
 * The gruesome details of the top-up application are here: http://confluence.diamond.ac.uk/x/QpRTAQ 
 * 
 *
 <pre>
SR-CS-FILL-01:STACOUNTDN: 
this is an integer counter that runs to zero at the start of TopUp fill 
and is reset immediately to the time to next TopUp fill, fillPeriod,

SR-CS-FILL-01:COUNTDOWN: this is a float-valued counter that runs to zero
at the start of TopUp and remains there until the fill is complete when 
it resets to time before next TopUp fill,

SR-CS-FILL-01:ENDCOUNTDN: this is an integer counter that runs to zero 
at the end of TopUp fill and resets immediately to an estimate of the 
time before the end of the next TopUp fill.
</pre>
 * 
 * @author Matthew Gerring
 *
 */
public class TopupWatchdog implements IDeviceWatchdog, IPositionListener {
	
	private static Logger logger = LoggerFactory.getLogger(TopupWatchdog.class);

	private DeviceWatchdogModel model;
	private IPausableDevice<?>  device;
	private String              countdownUnit;
	private String              periodUnit;
	private volatile IPosition lastCompletedPoint;
	
	public TopupWatchdog() {
		
	}
	public TopupWatchdog(DeviceWatchdogModel model) {
		this.model = model;
	}
	public DeviceWatchdogModel getModel() {
		return model;
	}
	public void setModel(DeviceWatchdogModel model) {
		this.model = model;
	}
	
	/**
	 * Called on a thread when the position changes.
	 */
	public void positionChanged(PositionEvent evt) {
		try {
			// Topup is currently 10Hz which is the rate that the scannable should call positionChanged(...) at.
			long time = getValue(evt, model.getCountdownName(), countdownUnit);
			//logger.info("Topup time is "+time+" ms");
			processPosition(time);
		} catch (Exception ne) {
			logger.error("Cannot process position "+evt, ne);
		}
	}

	private volatile boolean paused = false;
	private volatile boolean busy   = false;
	private volatile boolean rewind = false;	
	/**
	 * This method may be called at around 10Hz. In order to reduce
	 * CPU, we could disable events at the start of topup but this may not detect
	 * beam dump so might not be desirable. If the beam is dumped, pos also
	 * goes to 0 so the devices will be paused. In this case rewind must be called
	 * because 
	 * @param pos in ms or 0 if topup is happening, or -1 is no beam.
	 * @throws ScanningException 
	 */
	private void processPosition(long pos) throws Exception {
		
		// It's 10Hz, we can ignore events if we are doing something.
		// We ignore events while processing an event. 
		// Events are frequent and blocking is bad.
		if (busy) { 
			logger.trace("Event '"+model.getCountdownName()+"'@"+pos+" has been ignored.");
			return;
		}
		try {
			busy = true;
			if (pos<=model.getCooloff()) {
			    if (!paused) {
			    	rewind = pos<0; // We did not detect it before loosing beam
			    	device.pause();
			    	paused = true;
			    }
			} else if (paused) { // See if we can resume
				
				long period = getValue(model.getPeriodName(), periodUnit);
				if (pos>(period-model.getWarmup())) { // Still waiting to start it up
					return; // Keep waiting
				} else {
					if (rewind && lastCompletedPoint!=null) {
						device.seek(lastCompletedPoint.getStepIndex()); // Probably only does something useful for malcolm
						rewind = false;
					}
					device.resume();
					paused = false;
				}
			}
			
		} finally {
			busy = false;
		}
	}
	
	private static TimeUnit getTimeUnit(String unit) {
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
	
	@ScanStart
	public void start() {
		logger.debug("Watchdog starting on "+device.getName());
		try {
			// Get the topup, the unit and add a listener
		    IScannable<?> topup = getScannable(model.getCountdownName());
		    if (countdownUnit==null) this.countdownUnit = topup.getUnit();
		    if (!(topup instanceof IPositionListenable)) throw new ScanningException(model.getCountdownName()+" is not a position listenable!");
		    ((IPositionListenable)topup).addPositionListener(this);
		   
		    // Get the period unit (for speed and override reasons)
		    if (model.getPeriodName()!=null && periodUnit==null) {
		    	IScannable<?> period = getScannable(model.getPeriodName());
		    	this.periodUnit = period.getUnit();
		    }
		    
		} catch (ScanningException ne) {
			logger.error("Cannot start watchdog!");
		}
		logger.debug("Watchdog started on "+device.getName());
	} 
	
	@PointEnd
	public void pointEnd(IPosition done) {
		this.lastCompletedPoint = done;
	}
	
	@ScanFinally
	public void stop() {
		logger.debug("Watchdog stopping on "+device.getName());
		try {
		    IScannable<?> topup = getScannable(model.getCountdownName());
		    ((IPositionListenable)topup).removePositionListener(this);
		    
		} catch (ScanningException ne) {
			logger.error("Cannot stop watchdog!");
		}
		logger.debug("Watchdog stopped on "+device.getName());
	}
	
	private long getValue(PositionEvent evt, String name, String unit) {
		double pos = evt.getPosition().getValue(name);
		return getValue(pos, unit);
	}

	private long getValue(String name, String unit) throws Exception {
	    IScannable<Number> scannable = getScannable(name);
		return getValue(scannable.getPosition().doubleValue(), unit);
	}

	private long getValue(double pos, String unit) {
		TimeUnit tu = getTimeUnit(unit);
		return tu.toMillis(Math.round(pos)); // Assuming that they do not use double and seconds and assume fraction is maintained.
	}

	private <T> IScannable<T> getScannable(String name) throws ScanningException {
		IScannableDeviceService cservice = ServiceHolder.getRunnableDeviceService().getDeviceConnectorService();
		return cservice.getScannable(name);
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
	public String getCountdownUnit() {
		return countdownUnit;
	}
	public void setCountdownUnit(String countdownUnit) {
		this.countdownUnit = countdownUnit;
	}
	public String getPeriodUnit() {
		return periodUnit;
	}
	public void setPeriodUnit(String periodUnit) {
		this.periodUnit = periodUnit;
	}
}
