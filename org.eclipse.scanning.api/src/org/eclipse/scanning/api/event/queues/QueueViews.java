package org.eclipse.scanning.api.event.queues;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class QueueViews {

	public static final String getQueueViewID() {
		return "org.eclipse.scanning.event.ui.queueView"; // Might need to move this to a preference or other property.
	}

	public static String createSecondaryId(final String beanBundleName, final String beanClassName, final String queueName, final String topicName, final String submissionQueueName) {
        return createSecondaryId(null, beanBundleName, beanClassName, queueName, topicName, submissionQueueName);
	}
	
	public static String createSecondaryId(String uri, final String beanBundleName, final String beanClassName, final String queueName, final String topicName, final String submissionQueueName) {
		
		final StringBuilder buf = new StringBuilder();
		if (uri!=null) {
			try {
				uri = URLEncoder.encode(uri, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); // Not fatal
			} 
			append(buf, "uri",      uri);
		}
		append(buf, "beanBundleName",      beanBundleName);
		append(buf, "beanClassName",       beanClassName);
		append(buf, "queueName",           queueName);
		append(buf, "topicName",           topicName);
		append(buf, "submissionQueueName", submissionQueueName);
		return buf.toString();
	}
	

	protected static String createSecondaryId(String uri, String requestName, String responseName) {
		final StringBuilder buf = new StringBuilder();
		if (uri!=null) {
			try {
				uri = URLEncoder.encode(uri, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); // Not fatal
			} 
			append(buf, "uri",      uri);
		}
		append(buf, "requestName",  requestName);
		append(buf, "responseName", responseName);
		return buf.toString();
	}


	protected static void append(StringBuilder buf, String name, String value) {
		buf.append(name);
		buf.append("=");
		buf.append(value);
		buf.append(";");
	}

}
