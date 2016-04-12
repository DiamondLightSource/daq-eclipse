package org.eclipse.scanning.example.xcen.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class XcenActivator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = "org.eclipse.scanning.example.xcen.ui";

	public XcenActivator() {
		// TODO Auto-generated constructor stub
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return getImageDescriptor(PLUGIN_ID, path);
	}

	private static ImageDescriptor getImageDescriptor(String plugin, String path) {
		return imageDescriptorFromPlugin(plugin, path);
	}
}
