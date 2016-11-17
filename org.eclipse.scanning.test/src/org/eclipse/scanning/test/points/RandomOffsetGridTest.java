/*-
 * Copyright © 2015 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

public class RandomOffsetGridTest {

	private IPointGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new PointGeneratorService();
	}
	
	@Test
	public void testSimpleBox() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(-0.5);
		box.setSlowAxisStart(-1.0);
		box.setFastAxisLength(5);
		box.setSlowAxisLength(10);

		RandomOffsetGridModel model = new RandomOffsetGridModel("x", "y");
		model.setSlowAxisPoints(5);
		model.setFastAxisPoints(5);
		model.setBoundingBox(box);
		model.setSeed(10);
		model.setOffset(25);

		IPointGenerator<RandomOffsetGridModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();
		
		assertEquals(new Point("x", 0, 0.012403455250000084,"y", 0, 0.09924303325000006), pointList.get(0));
		assertEquals(new Point("x", 1, 0.837235318,"y", 0, 0.1643560529999999), pointList.get(1));
		assertEquals(new Point("x", 2, 2.20470153075,"y", 0, 0.018593022749999966), pointList.get(2));
		assertEquals(new Point("x", 3, 3.057925353,"y", 0, -0.024424061750000003), pointList.get(3));
		assertEquals(new Point("x", 4, 3.78130160075,"y", 0, 0.021858763000000003), pointList.get(4));
		assertEquals(new Point("x", 0, 0.09698760274999996,"y", 1, 1.83863665575), pointList.get(5));
	}

}
