package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.EmptyPosition;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.EmptyModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

public class EmptyTest {
	
	private IPointGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new PointGeneratorFactory();
	}
	
	@Test
	public void testEmpty() throws Exception {
		EmptyModel model = new EmptyModel();
		IPointGenerator<EmptyModel> gen = service.createGenerator(model);
		assertEquals(1, gen.size());
		
		List<IPosition> positionList = gen.createPoints();
		assertEquals(1, positionList.size());
		IPosition position = positionList.get(0);
		assertEquals(0, position.size());
		assertEquals(new EmptyPosition(), position);
	}

}
