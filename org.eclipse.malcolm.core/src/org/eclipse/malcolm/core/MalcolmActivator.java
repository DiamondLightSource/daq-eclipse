package org.eclipse.malcolm.core;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.malcolm.api.connector.IMalcolmConnectorService;
import org.eclipse.malcolm.api.message.JsonMessage;
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

	public static Collection<IMalcolmConnectorService<JsonMessage>> getConnectionServices() throws Exception {
		
		final Collection<ServiceReference<IMalcolmConnectorService>> refs = currentInstance.context.getServiceReferences(IMalcolmConnectorService.class, null);
		final Collection<IMalcolmConnectorService<JsonMessage>> ret = new HashSet<IMalcolmConnectorService<JsonMessage>>();
		for (ServiceReference<IMalcolmConnectorService> ref : refs) ret.add(currentInstance.context.getService(ref));
		return ret;
	}

}
