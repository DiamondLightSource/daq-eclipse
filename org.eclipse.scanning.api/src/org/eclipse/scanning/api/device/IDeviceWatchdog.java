package org.eclipse.scanning.api.device;

import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;

/**
 * 
 * A watchdog may be started to run with a scan.
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
 * NOTE: IDeviceWatchdog concrete class MUST have a no-argument constructor.
 * 
<pre>
PV's often used by watchdogs to monitor the scan.
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
	{@literal 	<property name="countdownName"          value="topup"/>}
	{@literal 	<property name="periodName"             value="period"/>}
	{@literal 	<property name="cooloff"                value="4000"/>}
	{@literal 	<property name="warmup"                 value="5000"/>}
    {@literal     <property name="bundle"                 value="org.eclipse.scanning.api" /> <!-- Delete for real spring? -->}
	{@literal </bean>}
	{@literal <bean id="topupWatchdog" class="org.eclipse.scanning.sequencer.watchdog.TopupWatchdog" init-method="activate">}
	{@literal 	<property name="model"             ref="topupModel"/>}
    {@literal     <property name="bundle"            value="org.eclipse.scanning.sequencer" /> <!-- Delete for real spring? -->}
	{@literal </bean>}
    </pre>

 * @author Matthew Gerring
 *
 */
public interface IDeviceWatchdog extends IModelProvider<DeviceWatchdogModel> {
	
    /**
	 * Make this device active, it will then be used in any scans run
	 * IMPORTANT: Call this method when the object is created in spring to register with the service.
	 */
	void activate();
	
	/**
	 * Called by the framework when a device is created to run with a specific scan.
	 * @param device
	 */
	void setDevice(IPausableDevice<?> device);

	/**
	 * 
	 */
	DeviceWatchdogModel getModel();

	/**
	 * 
	 * @param model
	 */
	void setModel(DeviceWatchdogModel model);
}
