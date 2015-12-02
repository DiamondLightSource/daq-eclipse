package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.IScanner;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScannerServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class ScanTest {
	
	private IScanningService    sservice;
	private IGeneratorService   gservice;
	
	@Before
	public void setup() throws ScanningException {
		sservice = new ScannerServiceImpl();
		gservice = new GeneratorServiceImpl();
	}
	
	@Test
	public void testSimpleScan() throws Exception {
				
		// Create a grid scan model
		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);
		model.setX(0);
		model.setY(0);
		model.setxLength(3);
		model.setyLength(3);
		
		Iterable<IPosition> gen = gservice.createGenerator(model);

		final ScanModel smodel = new ScanModel();
		smodel.setPositionIterator(gen);
		
		IScanner<ScanModel> scanner = sservice.createScanner(smodel);

		
	}
}
