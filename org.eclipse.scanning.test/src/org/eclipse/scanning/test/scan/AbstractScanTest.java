package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IScannableConnectorService;
import org.eclipse.scanning.api.scan.IScanner;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanModel;
import org.junit.Test;

public class AbstractScanTest {

	protected IScanningService              sservice;
	protected IScannableConnectorService    connector;
	protected IGeneratorService             gservice;

	@Test
	public void testSimpleScan() throws Exception {
				
		// Create a grid scan model
		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);
		model.setxStart(0);
		model.setyStart(0);
		model.setWidth(3);
		model.setHeight(3);
		
		Iterable<IPosition> gen = gservice.createGenerator(model);

		final ScanModel smodel = new ScanModel();
		smodel.setPositionIterator(gen);
		
		IScanner<ScanModel> scanner = sservice.createScanner(smodel, connector);
		
		scanner.run();
		
	}

}
