package org.eclipse.scanning.device.ui;

import java.net.URI;
import java.util.Properties;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.ui.part.ViewPart;

public abstract class EventConnectionView extends ViewPart {

	protected Properties                        idProperties;
	
	protected String getRequestName() {
		final String rName = getSecondaryIdAttribute("requestName");
		if (rName != null) return rName;
		return IEventService.DEVICE_REQUEST_TOPIC;
	}

	protected String getResponseName() {
		final String rName = getSecondaryIdAttribute("responseName");
		if (rName != null) return rName;
		return IEventService.DEVICE_RESPONSE_TOPIC;
	}

	protected String getTopicName() {
		final String topicName = getSecondaryIdAttribute("topicName");
		if (topicName != null) return topicName;
		return "scisoft.default.STATUS_TOPIC";
	}

    protected URI getUri() throws Exception {
		final String uri = getSecondaryIdAttribute("uri");
		if (uri != null) return new URI(uri.replace("%3A", ":"));
		return new URI(getCommandPreference(CommandConstants.JMS_URI));
	}
    
    protected String getUserName() {
		final String name = getSecondaryIdAttribute("userName");
		if (name != null) return name;
		return System.getProperty("user.name");
	}
   
    protected String getCommandPreference(String key) {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    	return store.getString(key);
    }

	protected String getQueueName() {
		final String qName =  getSecondaryIdAttribute("queueName");
		if (qName != null) return qName;
		return "scisoft.default.STATUS_QUEUE";
	}
	
	protected String getSubmissionQueueName() {
		final String qName =  getSecondaryIdAttribute("submissionQueueName");
		if (qName != null) return qName;
		return "scisoft.default.SUBMISSION_QUEUE";
	}

	protected String getSubmitOverrideSetName() {
		return getSubmissionQueueName()+".overrideSet";
	}
	
	protected String getSecondaryIdAttribute(String key) {
		if (idProperties!=null) return idProperties.getProperty(key);
		if (getViewSite()==null) return null;
		final String secondId = getViewSite().getSecondaryId();
		if (secondId == null) return null;
		idProperties = parseString(secondId);
		return idProperties.getProperty(key);
	}

	public static String createSecondaryId(final String beanBundleName, final String beanClassName, final String queueName, final String topicName, final String submissionQueueName) {
        return createSecondaryId(null, beanBundleName, beanClassName, queueName, topicName, submissionQueueName);
	}
	
	public static String createSecondaryId(final String uri, final String beanBundleName, final String beanClassName, final String queueName, final String topicName, final String submissionQueueName) {
		
		final StringBuilder buf = new StringBuilder();
		if (uri!=null) append(buf, "uri",      uri);
		append(buf, "beanBundleName",      beanBundleName);
		append(buf, "beanClassName",       beanClassName);
		append(buf, "queueName",           queueName);
		append(buf, "topicName",           topicName);
		append(buf, "submissionQueueName", submissionQueueName);
		return buf.toString();
	}
	

	protected static String createSecondaryId(String uri, String requestName, String responseName) {
		final StringBuilder buf = new StringBuilder();
		if (uri!=null) append(buf, "uri",  uri);
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
	
	
	/**
	 * String to be parsed to properties. In the form of key=value pairs
	 * separated by semi colons. You may not use semi-colons in the 
	 * keys or values. Keys and values are trimmed so extra spaces will be
	 * ignored.
	 * 
	 * @param secondId
	 * @return map of values extracted from the 
	 */
	protected static Properties parseString(String properties) {
		
		if (properties==null) return new Properties();
		Properties props = new Properties();
		final String[] split = properties.split(";");
		for (String line : split) {
			final String[] kv = line.split("=", 2);
			props.setProperty(kv[0].trim(), kv[1].trim());
		}
		return props;
	}

}
