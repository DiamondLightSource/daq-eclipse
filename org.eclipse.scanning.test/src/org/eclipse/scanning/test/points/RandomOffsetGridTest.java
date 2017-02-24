/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
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

		RandomOffsetGridModel rm = new RandomOffsetGridModel("x", "y");
		rm.setSlowAxisPoints(5);
		rm.setFastAxisPoints(5);
		rm.setBoundingBox(box);
		rm.setSeed(10);
		rm.setOffset(25);
		IPointGenerator<RandomOffsetGridModel> rg = service.createGenerator(rm);
		GeneratorUtil.testGeneratorPoints(rg, 5, 5);

		GridModel m = new GridModel("x", "y");
		m.setSlowAxisPoints(5);
		m.setFastAxisPoints(5);
		m.setBoundingBox(box);
		IPointGenerator<GridModel> g = service.createGenerator(m);

		for (Iterator<IPosition> it1 = rg.iterator(), it2 = g.iterator(); it1.hasNext() && it2.hasNext();) {
			IPosition t1 = it1.next();
			IPosition t2 = it2.next();
			assertTrue(Math.abs(t1.getValue("x") - t2.getValue("x")) <= 0.25 * 1.25);
			assertTrue(Math.abs(t1.getValue("y") - t2.getValue("y")) <= 0.25 * 2.5);
		}
	}

}
