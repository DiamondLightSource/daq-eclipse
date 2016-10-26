package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

public class StaticTest {
	
	private IPointGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new PointGeneratorFactory();
	}
	
	@Test
	public void testSingleStatic() throws Exception {
		StaticModel model = new StaticModel();
		IPointGenerator<StaticModel> gen = service.createGenerator(model);
		assertEquals(1, gen.size());
		
		List<IPosition> positionList = gen.createPoints();
		assertEquals(1, positionList.size());
		IPosition position = positionList.get(0);
		assertEquals(0, position.size());
		assertEquals(new StaticPosition(), position);
	}
	
	@Test
	public void testMultipleStatic() throws Exception {
		final int size = 8;
		StaticModel model = new StaticModel();
		model.setSize(size);
		IPointGenerator<StaticModel> gen = service.createGenerator(model);
		assertEquals(size, gen.size());
		
		final StaticPosition expected = new StaticPosition();
		List<IPosition> positionList = gen.createPoints();
		assertEquals(size, positionList.size());
		for (IPosition position : positionList) {
			assertEquals(0, position.size());
			assertEquals(expected, position);
		}
	}
	
	@Test(expected = ModelValidationException.class)
	public void testInvalidZeroSize() throws Exception {
		StaticModel model = new StaticModel();
		model.setSize(0);
		IPointGenerator<StaticModel> gen = service.createGenerator(model);
		gen.iterator();
	}

	@Test(expected = ModelValidationException.class)
	public void testInvalidNegativeSize() throws Exception {
		StaticModel model = new StaticModel();
		model.setSize(-3);
		IPointGenerator<StaticModel> gen = service.createGenerator(model);
		gen.iterator();
	}

}
