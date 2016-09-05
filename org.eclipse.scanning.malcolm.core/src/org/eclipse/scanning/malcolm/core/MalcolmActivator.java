package org.eclipse.scanning.malcolm.core;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class MalcolmActivator implements BundleActivator {

	private static MalcolmActivator currentInstance;
	private BundleContext           context;
	
	@Override
	public void start(BundleContext context) throws Exception {
		currentInstance = this;
		this.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		currentInstance = null;
		this.context = null;
	}

	public static Collection<IMalcolmConnectorService<MalcolmMessage>> getConnectionServices() throws Exception {
		
		final Collection<ServiceReference<IMalcolmConnectorService>> refs = currentInstance.context.getServiceReferences(IMalcolmConnectorService.class, null);
		final Collection<IMalcolmConnectorService<MalcolmMessage>> ret = new HashSet<IMalcolmConnectorService<MalcolmMessage>>();
		for (ServiceReference<IMalcolmConnectorService> ref : refs) ret.add(currentInstance.context.getService(ref));
		return ret;
	}

}
