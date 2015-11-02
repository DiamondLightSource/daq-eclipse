package org.eclipse.scanning.event;

public class Constants {

	/**
	 * Frequency in ms, default 2000. Set org.eclipse.scanning.event.heartbeat.freq system property to change this time.
	 */
	private static final long NOTIFICATION_FREQUENCY = 2000;
	
	public static long getNotificationFrequency() {
		return getLong("org.eclipse.scanning.event.heartbeat.freq", NOTIFICATION_FREQUENCY);
	}
			                                            

    /**
      * The timeout in ms, default 1 day. Set org.eclipse.scanning.event.heartbeat.timeout to change (in ms)
	 */
	private static final long TIMEOUT = 24*60*60*1000; //  a day!

    public static long getTimeout() {
		return getLong("org.eclipse.scanning.event.heartbeat.timeout", TIMEOUT);
    }
    
    private static long getLong(String propName, long defaultVal) {
    	String value = System.getProperty(propName);
	    return value!=null && !"".equals(value) ? Long.parseLong(value) : NOTIFICATION_FREQUENCY;
    }
}
