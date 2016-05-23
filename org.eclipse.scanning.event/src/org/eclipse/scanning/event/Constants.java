package org.eclipse.scanning.event;

public class Constants {

	/**
	 * Frequency in ms, default 2000. Set org.eclipse.scanning.event.heartbeat.freq system property to change this time.
	 */
	private static long NOTIFICATION_FREQUENCY = 2000;
	
	public static long getNotificationFrequency() {
		return Long.getLong("org.eclipse.scanning.event.heartbeat.freq", NOTIFICATION_FREQUENCY);
	}
	public static void setNotificationFrequency(long freq) {
		NOTIFICATION_FREQUENCY = freq;
		System.setProperty("org.eclipse.scanning.event.heartbeat.freq", String.valueOf(freq));
	}
		
	/**
	 * Receive from consumer.receive(...). A higher rate might be better for some applications.
	 * @return
	 */
	public final static int getReceiveFrequency() {
		return Integer.getInteger("org.eclipse.scanning.receive.freq", 500);
	}
    public static void setReceiveFrequency(int freq) {
    	System.setProperty("org.eclipse.scanning.event.heartbeat.freq", String.valueOf(freq));
    }
    /**
      * The timeout in ms, default 1 day. Set org.eclipse.scanning.event.heartbeat.timeout to change (in ms)
	 */
	private static final long TIMEOUT = 24*60*60*1000; //  a day!

    public static long getTimeout() {
    	return Long.getLong("org.eclipse.scanning.event.heartbeat.timeout", TIMEOUT);
    }
}
