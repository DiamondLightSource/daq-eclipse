package org.eclipse.scanning.api.event;


/**
 * This class represents 
 * 
 * @author Matthew Gerring
 */
public interface EventConstants {

    /**
     * A topic which may be used for scan events.
     * It is usually better to use your own topic rather than the default.
     */
	public static final String SCAN_TOPIC      = "org.eclipse.scanning.scan.topic";
    /**
     * The default topic used for status update events
     * It is usually better to use your own topic rather than the default.
     */
	public static final String STATUS_TOPIC      = "org.eclipse.scanning.status.topic";
	
	/**
	 * The default topic used for heartbeat events.
     * It is usually better to use your own topic rather than the default.
	 */
	public static final String HEARTBEAT_TOPIC = "org.eclipse.scanning.alive.topic";
	
	/**
	 * The default topic used for terminate events. This topic is generally used internally.
     * It is usually better to use your own topic rather than the default.
	 */
	public static final String CMD_TOPIC = "org.eclipse.scanning.command.topic";

	/**
	 * The default queue used for submitting things (like ScanRequests) to a queue.
	 * Ordered by submission.
	 */
	public static final String SUBMISSION_QUEUE = "org.eclipse.scanning.submission.queue";

	/**
	 * The default queue used for holding status events. This queue does not hold order
	 * reliably and behaves like a set. The user interface sorts the status objects by
	 * date submitted (usually) when the user looks at the queue.
	 */
	public static final String STATUS_SET = "org.eclipse.scanning.status.set";
	
	/**
	 * Topic used to tell UI users that a give consumer will be going down.
     * It is usually better to use your own topic rather than the default.
	 */
	public static final String ADMIN_MESSAGE_TOPIC = "org.eclipse.scanning.consumer.administratorMessage";

    /**
     * The default topic used for requests. It is usually better to use your own topic rather than the default.
     * It is usually better to use your own topic rather than the default.
     */
	public static final String REQUEST_TOPIC      = "org.eclipse.scanning.request.topic";

    /**
     * The default topic used for responses.  It is usually better to use your own topic rather than the default.
     * It is usually better to use your own topic rather than the default.
     */
	public static final String RESPONSE_TOPIC      = "org.eclipse.scanning.response.topic";


}
