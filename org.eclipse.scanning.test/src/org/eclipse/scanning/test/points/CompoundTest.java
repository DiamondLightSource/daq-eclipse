package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class CompoundTest {
	
	private IGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}
	
	@Test
	public void testSimpleCompound() throws Exception {
		
		IGenerator<StepModel, IPosition> temp = service.createGenerator(new StepModel("Temperature", 290,300,1));
		assertEquals(11, temp.size());
		
		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);
		model.setX(0);
		model.setY(0);
		model.setxLength(3);
		model.setyLength(3);

		// Get the point list
		IGenerator<GridModel,Point> grid = service.createGenerator(model, null);
		assertEquals(400, grid.size());

		IGenerator<?,IPosition> scan = service.createCompoundGenerator(temp, grid);
		assertEquals(4400, scan.size());
		
		List<IPosition> points = scan.createPoints();

		List<IPosition> first400 = new ArrayList<>(400);

		// The fist 400 should be T=290
		for (int i = 0; i < 400; i++) {
			assertEquals(new Double(290.0), points.get(i).get("Temperature"));
			first400.add(points.get(i));
		}
		checkPoints(first400);
		
		for (int i = 400; i < 800; i++) {
			assertEquals(new Double(291.0), points.get(i).get("Temperature"));
		}
		for (int i = 4399; i >= 4000; i--) {
			assertEquals(new Double(300.0), points.get(i).get("Temperature"));
		}

	}
	
	private void checkPoints(List<IPosition> pointList) {
		// Check correct number of points
		assertEquals(20 * 20, pointList.size());

		// Check some random points are correct
		assertEquals(0.075, (Double)pointList.get(0).get("X"), 1e-8);
		assertEquals(0.075, (Double)pointList.get(0).get("Y"), 1e-8);

		assertEquals(0.075 + 3 * (3.0 / 20.0), (Double)pointList.get(3).get("X"), 1e-8);
		assertEquals(0.075 + 0.0, (Double)pointList.get(3).get("Y"), 1e-8);

		assertEquals(0.075 + 2 * (3.0 / 20.0), (Double)pointList.get(22).get("X"), 1e-8);
		assertEquals(0.075 + 1 * (3.0 / 20.0), (Double)pointList.get(22).get("Y"), 1e-8);

		assertEquals(0.075 + 10 * (3.0 / 20.0), (Double)pointList.get(350).get("X"), 1e-8);
		assertEquals(0.075 + 17 * (3.0 / 20.0), (Double)pointList.get(350).get("Y"), 1e-8);

	}
}
