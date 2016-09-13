package org.eclipse.scanning.points;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class PointsActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		if (Boolean.getBoolean("org.eclipse.scanning.points.initJython")) {
			ScanPointGeneratorFactory.init();  // This is only needed for speed and might break GDA.
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
