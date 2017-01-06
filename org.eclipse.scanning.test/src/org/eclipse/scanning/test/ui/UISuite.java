package org.eclipse.scanning.test.ui;

import java.net.URISyntaxException;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.eclipse.scanning.server.application.PseudoSpringParser;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({

	SampleInformationTest.class,
	AxisConfigurationTest.class,
	KnownModelsTest.class,
	ControlTreeViewerTest.class

})
public class UISuite {
	
	private static DeviceServlet dservlet;

	public static void createTestServices() throws EventException, URISyntaxException {

		// DO NOT COPY this outside of tests, these services and servlets are created in OSGi and Spring. 
		
		System.setProperty("org.eclipse.scanning.broker.uri", "vm://localhost?broker.persistent=false");
		
		IMarshallerService marshaller = new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(), new ScanningExampleClassRegistry(), new ScanningTestClassRegistry()),
				Arrays.asList(new PointsModelMarshaller())
		);
		ActivemqConnectorService.setJsonMarshaller(marshaller);

		org.eclipse.scanning.device.ui.ServiceHolder.setExpressionService(new ServerExpressionService());
		org.eclipse.scanning.device.ui.ServiceHolder.setSpringParser(new PseudoSpringParser());
		
		IEventService eservice = new EventServiceImpl(new ActivemqConnectorService());
		Services.setConnector(new MockScannableConnector(null));
		Services.setEventService(eservice);
		org.eclipse.scanning.device.ui.ServiceHolder.setEventService(eservice);
		
		// Servlet to provide access to the remote scannables.
		dservlet = new DeviceServlet();
		dservlet.setBroker("vm://localhost?broker.persistent=false");
		dservlet.connect(); // Gets called by Spring automatically

	}

	public static void disposeTestServices() throws EventException {
		dservlet.disconnect();
	}
	

}
