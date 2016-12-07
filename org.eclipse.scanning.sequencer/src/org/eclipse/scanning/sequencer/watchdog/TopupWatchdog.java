package org.eclipse.scanning.sequencer.watchdog;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
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
 * Note there are 3 PVs to describe the topup state. This implementation assumes
 * that the scannable referred to by the countdown property of the model
 * wraps the PV SR-CS-FILL-01:COUNTDOWN.
 * 
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
 
 Example XML configuration
    <pre>
    {@literal <!--  Watchdog Example -->}
	{@literal <bean id="topupModel" class="org.eclipse.scanning.api.device.models.DeviceWatchdogModel">}
	{@literal 	<property name="countdownName"     value="topup"/>}
	{@literal 	<property name="periodName"        value="period"/>}
	{@literal 	<property name="cooloff"           value="4000"/>}
	{@literal 	<property name="message"           value="Paused during topup"/>}
	{@literal 	<property name="warmup"            value="5000"/>}
    {@literal   <property name="bundle"            value="org.eclipse.scanning.api" /> <!-- Delete for real spring? -->}
	{@literal </bean>}
	{@literal <bean id="topupWatchdog" class="org.eclipse.scanning.sequencer.watchdog.TopupWatchdog" init-method="activate">}
	{@literal 	<property name="model"             ref="topupModel"/>}
    {@literal   <property name="bundle"            value="org.eclipse.scanning.sequencer" /> <!-- Delete for real spring? -->}
	{@literal </bean>}
    </pre>
 
 * @author Matthew Gerring
 *
 */
public class TopupWatchdog extends AbstractWatchdog implements IPositionListener {
	
	private static Logger logger = LoggerFactory.getLogger(TopupWatchdog.class);

	private String              countdownUnit;
	private volatile IPosition lastCompletedPoint;
	
	private volatile boolean paused = false;

	private volatile boolean busy   = false;

	private volatile boolean rewind = false;
	
	private volatile long warmupEndPos = 0;

	public TopupWatchdog() {
		super();
	}
	public TopupWatchdog(DeviceWatchdogModel model) {
		super(model);
	}
	
	/**
	 * Called on a thread when the position changes.
	 * The coutndown is likely to report at 10Hz. TODO Check if this is ok during a scan and does not
	 * use too much CPU.
	 */
	public void positionChanged(PositionEvent evt) {
		try {
			// Topup is currently 10Hz which is the rate that the scannable should call positionChanged(...) at.
			long time = getValueMs(evt, model.getCountdownName(), countdownUnit);
			//logger.info("Topup time is "+time+" ms");
			processPosition(time);
		} catch (Exception ne) {
			logger.error("Cannot process position "+evt, ne);
		}
	}

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
		
		// It's 10Hz don't write much in here other than
		// simple tests or FPE's
		try {
			busy = true;
			if (pos <= model.getCooloff()) {
				if (!paused) {
					rewind = pos<0; // We did not detect it before loosing beam
					bean.setMessage(model.getMessage());
					device.pause();
					warmupEndPos = 0; // set to 0, so we know be recalculate it when topup ends
					paused = true;
				}
			} else if (paused) { // See if we can resume
				if (pos == 0) {
					return;
				}
				if (warmupEndPos == 0) {
					// topup has finished and pos is now counting down to the next topup
					// calculate the position at which warmup should end
					warmupEndPos = pos - (model.getWarmup());
				}
				
				if (pos <= warmupEndPos) {
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
	
	@ScanStart
	public void start(ScanBean bean) {
		setBean(bean);
		logger.debug("Watchdog starting on "+device.getName());
		try {
			// Get the topup, the unit and add a listener
			IScannable<?> topup = getScannable(model.getCountdownName());
			if (countdownUnit==null) this.countdownUnit = topup.getUnit();
			if (!(topup instanceof IPositionListenable)) {
				throw new ScanningException(model.getCountdownName()+" is not a position listenable!");
			}
			((IPositionListenable)topup).addPositionListener(this);
		} catch (ScanningException ne) {
			logger.error("Cannot start watchdog!", ne);
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
			logger.error("Cannot stop watchdog!", ne);
		}
		logger.debug("Watchdog stopped on "+device.getName());
	}
	
	public String getCountdownUnit() {
		return countdownUnit;
	}
	
	public void setCountdownUnit(String countdownUnit) {
		this.countdownUnit = countdownUnit;
	}
	
}
