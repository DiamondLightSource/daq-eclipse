package org.eclipse.scanning.api.device;

/**
 * 
 * This service holds available watchdogs and if they are
 * active will start them for a given IRunnableDevice.
 * 
 * Multiple watchdogs may be created and activated in spring.
 * These will be started if any scan is run and their annotations
 * called at different points of the scan so that they can monitor
 * the scan.
 * 
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

 * 
 * @author Matthew Gerring
 *
 */
public interface IDeviceWatchdogService {

	/**
	 * Call to add a watchdog to a scan
	 * @param dog
	 */
	void register(IDeviceWatchdog dog);
	
	/**
	 * Call to remove a watchdog from a scan
	 * @param dog
	 */
	void unregister(IDeviceWatchdog dog);
	
	/**
	 * Initiate a list of dogs to run with 
	 * the device. These then implement @ScanStart, @PointStart, @ScanFinally
	 * as required to participate themselves in the running of the device
	 * and watch the process.
	 * 
	 * This process actually makes new dogs from the active list which means that
	 * the IRunnableDevice<?> passed in is unique to that list.
	 * 
	 * @param device
	 * @return list of objects which may be added to the scan and will be processed
	 * by their annotations.
	 */
	IDeviceController create(IPausableDevice<?> device);
	

}
