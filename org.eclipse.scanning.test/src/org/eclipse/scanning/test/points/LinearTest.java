package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class LinearTest {

	// TODO FIXME Test Linear Numerically
	
	private IGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}
	
	@Test
	public void testOneDEqualSpacing() throws GeneratorException {
		
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(10);
		
		// Get the point list
		IGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model, roi);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		assertEquals(pointList.size(), 10);
	}
	
	@Test(expected = GeneratorException.class)
	public void testOneDEqualSpacingNoROI() throws GeneratorException {
		
        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(10);
		
		// Get the point list
		IGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model, null);
		gen.createPoints();
	}

	@Test(expected = GeneratorException.class)
	public void testOneDEqualSpacingNoPoints() throws GeneratorException {
		
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(0);
		
		// Get the point list
		IGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model, roi);
		List<Point> pointList = gen.createPoints();
	}

}
