package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.points.ArrayGenerator;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

public class ArrayTest {

	private ArrayGenerator generator;

	@Before
	public void before() throws Exception {

		ScanPointGeneratorFactory.setBundlePath("../org.eclipse.scanning.points");
		
		double[] positions = new double[] {1.0, 2.0 ,3.0, 4.0, 5.0};

		ArrayModel model = new ArrayModel();
		model.setPositions(positions);
		model.setName("x");

		generator = new ArrayGenerator();
		generator.setModel(model);
	}

	@Test
	public void testArray() throws Exception {

		// Get the point list
		List<IPosition> pointList = generator.createPoints();

		assertEquals(5, pointList.size());

		// Test a few points
		assertEquals(new Scalar("x", 0, 1.0), pointList.get(0));
		assertEquals(new Scalar("x", 1, 2.0), pointList.get(1));
		assertEquals(new Scalar("x", 2, 3.0), pointList.get(2));
		assertEquals(new Scalar("x", 3, 4.0), pointList.get(3));
		assertEquals(new Scalar("x", 4, 5.0), pointList.get(4));
	}

}
