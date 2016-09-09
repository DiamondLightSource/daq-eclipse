package org.eclipse.scanning.device.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.scanning.device.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    public static final String getJmsUri() {
    	String uri = null;
		if (uri == null) uri = getNovelCommandPreference(CommandConstants.JMS_URI);
	    if (uri == null) uri = System.getProperty("org.eclipse.scanning.broker.uri");
	    if (uri == null) uri = System.getProperty("gda.activemq.broker.uri"); // GDA specific but not a compilation dependency.
		if (uri == null) {
			throw new RuntimeException("URI not set for JMS. Please set system property org.eclipse.scanning.broker.uri or gda.activemq.broker.uri");
		}
		
		return uri;
	}


    /**
     * Get the command value if it has been changed by the user.
     * @param key
     * @return
     */
    private static final String getNovelCommandPreference(String key) {
		final IPreferenceStore store = getDefault().getPreferenceStore();
    	String val = store.getString(key);
    	String def = store.getDefaultString(key);
    	if (!val.equals(def)) return val;
    	return null;
     }


}
