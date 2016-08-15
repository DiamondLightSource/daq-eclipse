package org.eclipse.scanning.points;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class PointsActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		ScanPointGeneratorFactory.init();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
