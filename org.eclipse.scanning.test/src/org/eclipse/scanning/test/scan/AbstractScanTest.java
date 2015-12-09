package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IPositionListener;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.junit.Test;

public class AbstractScanTest {

	protected IScanningService              sservice;
	protected IDeviceConnectorService     connector;
	protected IGeneratorService             gservice;

	@Test
	public void testSetSimplePosition() throws Exception {

		IPositioner     pos    = sservice.createPositioner(connector);
		pos.setPosition(new MapPosition("x:1, y:2"));
		
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
		
		pos.setPosition(new MapPosition("a:10, b:10, p:10, q:10, x:10, y:10"));
		
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
				
		// Configure a detector
		IRunnableDevice<MockDetectorModel> detector = connector.getDetector("detector");
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCollectionTime(0.1);
		detector.configure(dmodel);
		
		// Create scan points for a grid
		BoundingBox box = new BoundingBox();
		box.setxStart(0);
		box.setyStart(0);
		box.setWidth(3);
		box.setHeight(3);

		GridModel gmodel = new GridModel();
		gmodel.setRows(20);
		gmodel.setColumns(20);
		gmodel.setBoundingBox(box);
		
		IGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan and scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterator(gen);
		smodel.setDetectors(Arrays.asList(new IRunnableDevice<?>[]{detector}));
		
		IRunnableDevice<ScanModel> scanner = sservice.createScanner(smodel, connector);
		scanner.run();
		
		assertEquals(gen.size(), dmodel.getRan());
		assertEquals(gen.size(), dmodel.getRead());
	}

}
