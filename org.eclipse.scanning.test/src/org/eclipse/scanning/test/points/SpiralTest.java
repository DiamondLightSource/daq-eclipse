package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.points.SpiralGenerator;
import org.junit.Before;
import org.junit.Test;

public class SpiralTest {

	private SpiralGenerator generator;

	@Before
	public void before() throws Exception {
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(-10);
		box.setSlowAxisStart(5);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(4);

		SpiralModel model = new SpiralModel();
		model.setBoundingBox(box);
		// use default parameters

		generator = new SpiralGenerator();
		generator.setModel(model);
	}

	@Test
	public void testSpiralNoROI() throws Exception {

		// Get the point list
		List<IPosition> pointList = generator.createPoints();

		assertEquals(20, pointList.size());

		// Test a few points
		// TODO check x and y index values - currently these are not tested by AbstractPosition.equals()
		assertEquals(new Point("x", 0, -8.263367850554253, "y", 0, 6.678814432234913, false), pointList.get(0));
		assertEquals(new Point("x", 3, -8.139330427516057, "y", 3, 7.991968780318976, false), pointList.get(3));
		assertEquals(new Point("x", 15, -6.315009394139057, "y", 15, 7.399523826759042, false), pointList.get(15));
	}
}
