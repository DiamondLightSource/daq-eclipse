package org.eclipse.scanning.device.ui;

public class DevicePreferenceConstants {

	/**
	 * Preference for showing tooltips in control table.
	 */
	public final static String SHOW_CONTROL_TOOLTIPS = "org.eclipse.scanning.device.ui.control.showTips";
	
	/**
	 * Preference for number format.
	 */
	public static final String NUMBER_FORMAT = "org.eclipse.scanning.device.ui.device.numberFormat";
	
	/**
	 * Topic for publishing the result of an event calculation.
	 */
	public static final String PATH_CALCULATION_TOPIC = "uk/ac/diamond/daq/mapping/client/events/PathCalculationEvent";

	/**
	 * Normally scan regions are saved and remembered when the user restarts.
	 */
	public static final String AUTO_SAVE_REGIONS = "org.eclipse.scanning.device.ui.device.autoSaveRegions";

	/**
	 * Stores if the user should not be able to edit the scan sequence. They can still use the UI to switch this back on.
	 */
	public static final String LOCK_SCAN_SEQUENCE = "org.eclipse.scanning.device.ui.control.lockScanPipeline";
}
