package org.eclipse.scanning.test.points;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class LissajousTest {

	// TODO FIXME Test Lissajous Numerically
	
	private IGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}
	

	@Test
	public void testLissajousNoROI() throws GeneratorException {
		
		LissajousModel model = new LissajousModel();
		model.setX(0);
		model.setY(0);
		model.setxLength(3);
		model.setyLength(3);

		// Get the point list
		IGenerator<LissajousModel> gen = service.createGenerator(model, null);
		List<Point> pointList = gen.createPoints();
		
		assertTrue(gen.size()==1257);
		assertEquals(pointList.size(), gen.size());
		
	}
	
	@Test
	public void testLissajous() throws GeneratorException {
		
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Get the point list
		IGenerator<LissajousModel> gen = service.createGenerator(new LissajousModel(), roi);
		List<Point> pointList = gen.createPoints();
		
		assertTrue(gen.size()==1257);
		assertEquals(pointList.size(), gen.size());
		
	}

}
