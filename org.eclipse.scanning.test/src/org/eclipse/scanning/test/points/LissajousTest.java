package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.points.LissajousGenerator;
import org.junit.Before;
import org.junit.Test;

public class LissajousTest {

	private LissajousGenerator generator;

	@Before
	public void before() throws Exception {
		
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(-10);
		box.setSlowAxisStart(5);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(4);

		LissajousModel model = new LissajousModel();
		model.setBoundingBox(box);
		// use default parameters

		generator = new LissajousGenerator();
		generator.setModel(model);
	}

	@Test
	public void testLissajousNoROI() throws Exception {

		// Get the point list
		List<IPosition> pointList = generator.createPoints();

		assertEquals(503, pointList.size());

		// Test a few points
		// TODO check x and y index values - currently these are not tested by AbstractPosition.equals()
		assertEquals(new Point(0, -8.5, 0, 7.0, false), pointList.get(0));
		assertEquals(new Point(100, -9.939837880744866, 100, 6.925069008238842, false), pointList.get(100));
		assertEquals(new Point(300, -7.5128903496577735, 300, 6.775627736273646, false), pointList.get(300));
	}
}
