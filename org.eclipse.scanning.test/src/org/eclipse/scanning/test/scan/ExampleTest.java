package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertTrue;

import java.awt.List;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.eclipse.scanning.event.remote.RemoteServiceFactory;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.eclipse.dawnsci.analysis.dataset.roi.json.CircularFitROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBean;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.event.Constants;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.BrokerTest;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

//public class ExampleTest extends AbstractScanTest {
//	
//	
//	@Before
//	public void setup() throws ScanningException {
//
//		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
//		eservice  = new EventServiceImpl(new ActivemqConnectorService());
//
//		// We wire things together without OSGi here 
//		// DO NOT COPY THIS IN NON-TEST CODE!
//		connector = new MockScannableConnector(eservice.createPublisher(uri, EventConstants.POSITION_TOPIC));
//		dservice  = new RunnableDeviceServiceImpl(connector);
//		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)dservice;
//		impl._register(MockDetectorModel.class, MockWritableDetector.class);
//		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
//
//		gservice  = new PointGeneratorFactory();
//	}
//}

public class ExampleTest extends BrokerTest {
	
	protected static URI uri;     
	
	protected IRunnableDeviceService    dservice;
	protected IEventService             eservice;
	protected IRequester<DeviceRequest> requester;
	protected IResponder<DeviceRequest> responder;
	
//	@Before
//	public void setup() throws Exception {
//
//		uri = new URI("tcp://localhost:61616");
//		
//		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
//		eservice  = new EventServiceImpl(new ActivemqConnectorService());
//
//		dservice = eservice.createRemoteService(uri, IRunnableDeviceService.class); // Can make a RunnableDeviceService to obtain runnable devices.
//	}
//	
//	@Test
//	public void testGetDeviceNames() throws ScanningException {
//		
//		Collection<String> deviceNames;
//
//		deviceNames = dservice.getRunnableDeviceNames();
//		
//		System.out.println(deviceNames.toString());
//
//	}

//	@Test
//	public void testAvailableQueuesAndTopics() throws ScanningException, JMSException {
//
//		// Create a ConnectionFactory
//		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
//
//		// Create a Connection
//		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
//
//		// Important point that was missed in the above answer
//		connection.start();
//
//		DestinationSource ds = connection.getDestinationSource();
//		Set<ActiveMQQueue> queues = ds.getQueues();
//
//	System.out.println("List of queues:");
//
//		for (ActiveMQQueue queue : queues) {
//			try {
//				System.out.println(queue.getQueueName());
//			} catch (JMSException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		System.out.println("\nNow for topics:");
//		
//		Set<ActiveMQTopic> topics = ds.getTopics();
//
//		for (ActiveMQTopic topic : topics) {
//			try {
//				System.out.println(topic.getTopicName());
//			} catch (JMSException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	
//	@Test
//	public void testProducedMessage() throws Exception {
//		
//		MarshallerService marshaller = new MarshallerService(new PointsModelMarshaller());
//		DeviceRequest req = new DeviceRequest();
//		Collection<DeviceInformation<?>> devices = new ArrayList<DeviceInformation<?>>();
//		
//		System.out.println("First, no collection:");
//		System.out.println(marshaller.marshal(req));
//		
//		System.out.println("\nZero items in collection");
//		req.setDevices(devices);
//		System.out.println(marshaller.marshal(req));
//		
//		System.out.println("\nNow, with one DeviceInformation");
//		devices.add(new DeviceInformation<Object>("This is the first device name."));
//		System.out.println(marshaller.marshal(req));
//		
//		System.out.println("\nNow, with two DeviceInformation's");
//		devices.add(new DeviceInformation<Object>("This is the second device's name."));
//		System.out.println(marshaller.marshal(req));
//	}
	
	@Before
	public void createServices() throws Exception {
		
		uri = new URI("tcp://localhost:61616");
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!
		
		// Set up stuff because we are not in OSGi with a test
		// DO NOT COPY TESTING ONLY
		dservice = new RunnableDeviceServiceImpl(new MockScannableConnector(eservice.createPublisher(uri, EventConstants.POSITION_TOPIC)));
		MandelbrotDetector mandy = new MandelbrotDetector();
		final DeviceInformation<MandelbrotModel> info = new DeviceInformation<MandelbrotModel>(); // This comes from extension point or spring in the real world.
		info.setName("mandelbrot");
		info.setLabel("Example Mandelbrot");
		info.setDescription("Example mandelbrot device");
		info.setId("org.eclipse.scanning.example.detector.mandelbrotDetector");
		info.setIcon("org.eclipse.scanning.example/icon/mandelbrot.png");
		mandy.setDeviceInformation(info);
		((RunnableDeviceServiceImpl)dservice)._register("mandelbrot", mandy);
		

		Services.setRunnableDeviceService(dservice);
		Services.setEventService(eservice);
	
		connect(eservice, dservice);
	}
	
	@Before
	public void start() throws Exception {
		
	   	Constants.setNotificationFrequency(200); // Normally 2000
	   	Constants.setReceiveFrequency(100);
	}
	
	@After
	public void stop() throws Exception {
		
    	Constants.setNotificationFrequency(2000); // Normally 2000
    	if (requester!=null) requester.disconnect();
    	if (responder!=null) responder.disconnect();
	}

	protected void connect(IEventService eservice, IRunnableDeviceService dservice) throws Exception {
		
		this.eservice = eservice;
		this.dservice = dservice;
		
		DeviceServlet dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.setRequestTopic(IEventService.DEVICE_REQUEST_TOPIC);
		dservlet.setResponseTopic(IEventService.DEVICE_RESPONSE_TOPIC);
		dservlet.connect();
				
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		requester  = eservice.createRequestor(uri, IEventService.DEVICE_REQUEST_TOPIC, IEventService.DEVICE_RESPONSE_TOPIC);
		requester.setTimeout(10, TimeUnit.SECONDS); // It's a test, give it a little longer.

	}
	
	@Test
	public void simpleSerialize() throws Exception {
		Collection<DeviceInformation<?>> devices = new HashSet<DeviceInformation<?>>();
		devices.add(new DeviceInformation<Object>("This is the first device name."));
		devices.add(new DeviceInformation<Object>("This is the second device's name."));
		DeviceRequest in = new DeviceRequest();
		in.setDevices(devices);
        String json = eservice.getEventConnectorService().marshal(in);
        DeviceRequest back = eservice.getEventConnectorService().unmarshal(json, DeviceRequest.class);
        assertTrue(in.equals(back));
	}

}