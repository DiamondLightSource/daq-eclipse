package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class AbstractScanTest {

	protected IScanningService              sservice;
	protected IDeviceConnectorService       connector;
	protected IGeneratorService             gservice;
	protected IEventService                 eservice;

	@Test
	public void testSetSimplePosition() throws Exception {

		IPositioner     pos    = sservice.createPositioner(connector);
		pos.setPosition(new MapPosition("x:0:1, y:0:2"));
		
		assertTrue(connector.getScannable("x").getPosition().equals(1d));
		assertTrue(connector.getScannable("y").getPosition().equals(2d));
	}
	
	@Test
	public void testLevels() throws Exception {

		IPositioner     pos    = sservice.createPositioner(connector);
		
		final List<String> scannablesMoved = new ArrayList<>(6);
		pos.addPositionListener(new IPositionListener.Stub() {
			@Override
			public void levelPerformed(PositionEvent evt) {
				System.out.println("Level complete "+evt.getLevel());
				for (INameable s : evt.getLevelObjects()) scannablesMoved.add(s.getName());
			}
		});
		
		pos.setPosition(new MapPosition("a:0:10, b:0:10, p:0:10, q:0:10, x:0:10, y:0:10"));
		
		assertTrue(scannablesMoved.get(0).equals("a") || scannablesMoved.get(0).equals("b"));
		assertTrue(scannablesMoved.get(1).equals("a") || scannablesMoved.get(1).equals("b"));
		assertTrue(scannablesMoved.get(2).equals("p") || scannablesMoved.get(2).equals("q"));
		assertTrue(scannablesMoved.get(3).equals("p") || scannablesMoved.get(3).equals("q"));
		assertTrue(scannablesMoved.get(4).equals("x") || scannablesMoved.get(4).equals("y"));
		assertTrue(scannablesMoved.get(5).equals("x") || scannablesMoved.get(5).equals("y"));
		
		for (String name : pos.getPosition().getNames()) {
			assertTrue(connector.getScannable(name).getPosition().equals(10d));
		}
	}
	
	@Test
	public void testMassiveMove() throws Exception {

		MapPosition pos = new MapPosition();
		for (int ilevel = 0; ilevel < 100; ilevel++) {
			for (int iscannable = 0; iscannable < 1000; iscannable++) {
				String name = "pos"+ilevel+"_"+iscannable;
				
				// We set the level in this loop, normally this comes
				// in via spring.
				IScannable<?> motor = connector.getScannable(name);
				motor.setLevel(ilevel);
				if (motor instanceof MockScannable) ((MockScannable)motor).setRequireSleep(false);
				
				// We set the position required
				pos.put(name, ilevel+iscannable);
 			}
		} 
		
		IPositioner positioner   = sservice.createPositioner(connector);

		final List<String> levelsMoved = new ArrayList<>(6);
		positioner.addPositionListener(new IPositionListener.Stub() {
			@Override
			public void levelPerformed(PositionEvent evt) {
				System.out.println("Level complete "+evt.getLevel());
				for (ILevel s : evt.getLevelObjects()) {
					levelsMoved.add(String.valueOf(s.getLevel()));
				}
			}
		});

		long start = System.currentTimeMillis();
		positioner.setPosition(pos);
		long end   = System.currentTimeMillis();
		
		// Check the size
		assertTrue(levelsMoved.size()==100000);
		
		// Check that the level order was right
		final List<String> sorted = new ArrayList<String>(levelsMoved.size());
	    sorted.addAll(levelsMoved);
	    Collections.sort(sorted, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.parseInt(o1)-Integer.parseInt(o2);
			}
		});
		
	    for (int i = 0; i < levelsMoved.size(); i++) {
		    assertEquals("The wrong level was encountered sorted='"+sorted.get(i)+"' moved='"+levelsMoved.get(i)+"'", levelsMoved.get(i), sorted.get(i));
		}
	    
		System.out.println("Positioning 100,000 test motor with 100 levels took "+(end-start)+" ms");
	}
	
	@Test
	public void testSimpleScan() throws Exception {
				
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null);
		scanner.run();
		checkRun(scanner);
	}

	@Test
	public void testSimpleScanWithStatus() throws Exception {
			
		
		final ScanBean bean = new ScanBean();
		bean.setName("Fred");
		bean.setUniqueId("fred");
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		final IPublisher<ScanBean> publisher = eservice.createPublisher(uri, IEventService.STATUS_TOPIC, new ActivemqConnectorService());
		
		final ISubscriber<IScanListener> subscriber = eservice.createSubscriber(uri, IEventService.STATUS_TOPIC, new ActivemqConnectorService());
		final List<ScanBean>    events = new ArrayList<ScanBean>(11);
		final List<DeviceState> states = new ArrayList<DeviceState>(11);
		subscriber.addListener(new IScanListener.Stub() {		
			@Override
			public void scanStateChanged(ScanEvent evt) {
				states.add(evt.getBean().getDeviceState());
			}
			@Override
			public void scanEventPerformed(ScanEvent evt) {
				events.add(evt.getBean());
				System.out.println("State : "+evt.getBean().getDeviceState());
				System.out.println("Percent complete : "+evt.getBean().getPercentComplete());
				System.out.println(evt.getBean().getPosition());
			}
		});
		
		try {
			
			// Create a scan and run it without publishing events
			IRunnableDevice<ScanModel> scanner = createTestScanner(bean, publisher, null);
			scanner.run();
			
			Thread.sleep(1000); // Wait for all events to make it over from ActiveMQ
			
			checkRun(scanner);
			
			// Bit of a hack to get the generator from the model - should this be easier?
			IGenerator<?,IPosition> gen = (IGenerator<?,IPosition>)((ScanModel)((AbstractRunnableDevice)scanner).getModel()).getPositionIterator();
			assertEquals(gen.size()+states.size(), events.size());
			assertEquals(Arrays.asList(DeviceState.READY, DeviceState.RUNNING, DeviceState.READY), states);
			
			for (ScanBean b : events) assertEquals("fred", b.getUniqueId());
		
		} finally {
			publisher.disconnect();
		}
	}

	@Test
	public void testSimpleScanSetPositionCalls() throws Exception {
			
		IScannable<Number> x = connector.getScannable("x");
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null);
		
		scanner.run();
		
		checkRun(scanner);
		
		// NOTE Did with Mockito but caused dependency issues.
		MockScannable ms = (MockScannable)x;
		ms.verify(0.3, new Point(0,0.3,0,0.3));
		ms.verify(1.5, new Point(0,1.5,2,0.3));
		ms.verify(0.3, new Point(2,0.3,0,1.5));
		ms.verify(1.5, new Point(2,1.5,2,1.5));
	}
	
	@Test
	public void testSimpleScanWithMonitor() throws Exception {
			
		IScannable<Number> monitor = connector.getScannable("monitor");
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, monitor);
		
		scanner.run();
		
		checkRun(scanner);
		
		// NOTE Did with Mockito but caused dependency issues.
		MockScannable ms = (MockScannable)monitor;
		ms.verify(null, new Point(0,0.3,0,0.3));
		ms.verify(null, new Point(0,1.5,2,0.3));
		ms.verify(null, new Point(2,0.3,0,1.5));
		ms.verify(null, new Point(2,1.5,2,1.5));
	}

	private void checkRun(IRunnableDevice<ScanModel> scanner) throws Exception {
		// Bit of a hack to get the generator from the model - should this be easier?
		// Do not copy this code
		ScanModel smodel = (ScanModel)((AbstractRunnableDevice)scanner).getModel();
		IGenerator<?,IPosition> gen = (IGenerator<?,IPosition>)smodel.getPositionIterator();
		MockDetectorModel dmodel = (MockDetectorModel)((AbstractRunnableDevice)smodel.getDetectors().get(0)).getModel();
		assertEquals(gen.size(), dmodel.getRan());
		assertEquals(gen.size(), dmodel.getRead());
	}

	private IRunnableDevice<ScanModel> createTestScanner(final ScanBean bean, final IPublisher<ScanBean> publisher, IScannable<?> monitor) throws Exception {
		
		// Configure a detector with a collection time.
		IRunnableDevice<MockDetectorModel> detector = connector.getDetector("detector");
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCollectionTime(0.1);
		detector.configure(dmodel);
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setRows(5);
		gmodel.setColumns(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));	
		IGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterator(gen);
		smodel.setDetectors(detector);
		smodel.setBean(bean);
		if (monitor!=null) smodel.setMonitors(monitor);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = sservice.createRunnableDevice(smodel, publisher, connector);
		return scanner;
	}

}
