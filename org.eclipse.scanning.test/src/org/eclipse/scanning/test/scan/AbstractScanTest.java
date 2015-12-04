package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IHardwareConnectorService;
import org.eclipse.scanning.api.scan.IPositionListener;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.IScanner;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanModel;
import org.junit.Test;

public class AbstractScanTest {

	protected IScanningService              sservice;
	protected IHardwareConnectorService     connector;
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
				for (IScannable<?> s : evt.getScannables()) scannablesMoved.add(s.getName());
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

		
	}


	
	@Test
	public void testSimpleScan() throws Exception {
				
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setxStart(0);
		box.setyStart(0);
		box.setWidth(3);
		box.setHeight(3);

		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);
		model.setBoundingBox(box);
		
		Iterable<IPosition> gen = gservice.createGenerator(model);
		final ScanModel  smodel = new ScanModel(gen, "det", 0.1);
		final IPosition  start  = new MapPosition("x:0, y:0");
		smodel.setStart(start);
		
		IScanner<ScanModel> scanner = sservice.createScanner(smodel, connector);
		
		scanner.run();
		
	}

}
