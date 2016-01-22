package org.eclipse.scanning.test.scan;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TopupTest {

	protected IScanningService              sservice;
	protected IDeviceConnectorService       connector;
	protected IGeneratorService             gservice;
	protected IEventService                 eservice;
	private IWritableDetector<Object>       detector;
	
	private List<IPosition>                 positions;

	@Before
	public void setup() throws ScanningException {
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		sservice  = new ScanningServiceImpl();
		connector = new MockScannableConnector();
		gservice  = new GeneratorServiceImpl();
		eservice  = new EventServiceImpl();
		
		detector = connector.getDetector("detector");
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCollectionTime(0.1);
		detector.configure(dmodel);
		
		positions = new ArrayList<>(20);
		detector.addRunListener(new IRunListener.Stub() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                System.out.println("Ran mock detector @ "+evt.getPosition());
                positions.add(evt.getPosition());
			}
		});

	}

	@Test
	public void testTopup() throws Exception {
		
		final List<String> moved   = new ArrayList<>();
		final IScannable<Number>   topup   = connector.getScannable("topup");
		((IPositionListenable)topup).addPositionListener(new IPositionListener.Stub() {
			@Override
			public void positionPerformed(PositionEvent evt) {
				moved.add(topup.getName());
			}
		});
		final IScannable<Number>   x       = connector.getScannable("x");
		((IPositionListenable)x).addPositionListener(new IPositionListener.Stub() {
			@Override
			public void positionPerformed(PositionEvent evt) {
				moved.add(x.getName());
			}
		});
		topup.setLevel(1);
		
		// x and y are level 3
		IRunnableDevice<ScanModel> scanner = createTestScanner(topup);
		scanner.run(null);
		
		assertEquals(25, positions.size());
		assertEquals(50, moved.size());
		assertTrue(moved.get(0).equals("topup"));
		assertTrue(moved.get(1).equals("x"));
		
		moved.clear();
		positions.clear();
		topup.setLevel(5); // Above x
		scanner.run(null);
		
		assertEquals(25, positions.size());
		assertEquals(50, moved.size());
		assertTrue(moved.get(0).equals("x"));
		assertTrue(moved.get(1).equals("topup"));
	
		
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
		GridModel gmodel = new GridModel();
		gmodel.setRows(5);
		gmodel.setColumns(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));	
		IGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		if (monitor!=null) smodel.setMonitors(monitor);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = sservice.createRunnableDevice(smodel, null, connector);
		return scanner;
	}

}
