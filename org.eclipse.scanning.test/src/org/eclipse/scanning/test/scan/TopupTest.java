package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.example.scannable.MockTopupScannable;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class TopupTest {

	protected IRunnableDeviceService                sservice;
	protected IScannableDeviceService       connector;
	protected IPointGeneratorService        gservice;
	protected IEventService                 eservice;
	private IWritableDetector<MockDetectorModel>       detector;
	
	private List<IPosition>                 positions;

	@Before
	public void setup() throws ScanningException {

		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new ScanningTestClassRegistry()),
				Arrays.asList(new PointsModelMarshaller())
				));
		eservice  = new EventServiceImpl(new ActivemqConnectorService());

		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		connector = new MockScannableConnector(null);
		sservice  = new RunnableDeviceServiceImpl(connector);
		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)sservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);

		gservice  = new PointGeneratorService();
		
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setExposureTime(0.001);
		dmodel.setName("detector");
		detector = (IWritableDetector<MockDetectorModel>) sservice.createRunnableDevice(dmodel);
		
		positions = new ArrayList<>(20);
		detector.addRunListener(new IRunListener() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                System.out.println("Ran mock detector @ "+evt.getPosition());
                positions.add(evt.getPosition());
			}
		});

	}

	@Test
	public void testPause() throws Exception {
		
		final List<String> moved   = new ArrayList<>();
		final IScannable<Number>   pauser   = connector.getScannable("pauser");
		if (pauser instanceof MockScannable) {
			((MockScannable)pauser).setRealisticMove(false);
		}
		((IPositionListenable)pauser).addPositionListener(new IPositionListener() {
			@Override
			public void positionPerformed(PositionEvent evt) {
				moved.add(pauser.getName());
			}
		});
		final IScannable<Number>   x       = connector.getScannable("x");
		if (x instanceof MockScannable) {
			((MockScannable)x).setRealisticMove(false);
		}
		((IPositionListenable)x).addPositionListener(new IPositionListener() {
			@Override
			public void positionPerformed(PositionEvent evt) {
				moved.add(x.getName());
			}
		});
		pauser.setLevel(1);
		
		// x and y are level 3
		IRunnableDevice<ScanModel> scanner = createTestScanner(pauser);
		scanner.run(null);
		
		assertEquals(25, positions.size());
		assertEquals(50, moved.size());
		assertTrue(moved.get(0).equals("pauser"));
		assertTrue(moved.get(1).equals("x"));
		
		moved.clear();
		positions.clear();
		pauser.setLevel(5); // Above x
		scanner.run(null);
		
		assertEquals(25, positions.size());
		assertEquals(50, moved.size());
		assertTrue(moved.get(0).equals("x"));
		assertTrue(moved.get(1).equals("pauser"));
	
		
	}
	
	@Test(expected=Exception.class)
	public void testBeamOn() throws Exception {
		
		final IScannable<Number>   beamon   = connector.getScannable("beamon");
		beamon.setLevel(1);
		
		// x and y are level 3
		IRunnableDevice<ScanModel> scanner = createTestScanner(beamon);
		scanner.run(null);
		
		assertEquals(10, positions.size());
	}

	
	private IRunnableDevice<ScanModel> createTestScanner(IScannable<?> monitor) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel("x", "y");
		gmodel.setSlowAxisPoints(5);
		gmodel.setFastAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));	
		IPointGenerator<?> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		if (monitor!=null) smodel.setMonitors(monitor);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = sservice.createRunnableDevice(smodel, null);
		return scanner;
	}

	@Test
	public void topupPeriod() throws Exception {
		
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		
		long fastPeriod = 500;
		long orig = topup.getPeriod();
		topup.setPeriod(fastPeriod);
		
		try {
			int max = Integer.MIN_VALUE;
			int min = Integer.MAX_VALUE;
			// We start a check the topup value for 15s
			long start = System.currentTimeMillis();
			while((System.currentTimeMillis()-start)<(fastPeriod*2.1)) {
				int pos = topup.getPosition().intValue();
				System.out.println("Topup in "+pos+"ms");
				max = Math.max(max, pos);
				min = Math.min(min, pos);
				Thread.sleep(fastPeriod/10);
			}
			assertTrue(max<600&&max>300);
			assertTrue(min<200&&min>-1);
			
		} finally {
			topup.setPeriod(orig);

		}
	}
}
