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
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

public class RandomOffsetGridTest {

	private IPointGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new PointGeneratorFactory();
	}
	
	@Test
	public void testSimpleBox() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(-0.5);
		box.setSlowAxisStart(-0.5);
		box.setFastAxisLength(5);
		box.setSlowAxisLength(5);

		RandomOffsetGridModel model = new RandomOffsetGridModel();
		model.setSlowAxisPoints(5);
		model.setFastAxisPoints(5);
		model.setBoundingBox(box);
		model.setSeed(10);
		model.setOffset(25);

		IPointGenerator<RandomOffsetGridModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();
		
		assertEquals(new Point("x", 0, 0.09924303325000006,"y", 0, 0.012403455250000084), pointList.get(0));
		assertEquals(new Point("x", 1, 1.164356053,"y", 0, -0.16276468200000005), pointList.get(1));
		assertEquals(new Point("x", 2, 2.01859302275,"y", 0, 0.20470153074999997), pointList.get(2));
		assertEquals(new Point("x", 3, 2.97557593825,"y", 0, 0.05792535300000001), pointList.get(3));
		assertEquals(new Point("x", 4, 4.021858763,"y", 0, -0.21869839925), pointList.get(4));
		assertEquals(new Point("x", 0, -0.16136334424999998,"y", 1, 1.09698760275), pointList.get(5));
	}

}
