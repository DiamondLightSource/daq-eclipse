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

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
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

		RandomOffsetGridModel rmodel = new RandomOffsetGridModel("x", "y");
		rmodel.setSlowAxisPoints(5);
		rmodel.setFastAxisPoints(5);
		rmodel.setBoundingBox(box);
		rmodel.setSeed(10);
		rmodel.setOffset(25);

		IPointGenerator<RandomOffsetGridModel> genWithRandom = service.createGenerator(rmodel);
		GeneratorUtil.testGeneratorPoints(genWithRandom, 5, 5);

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setSlowAxisPoints(5);
		gmodel.setFastAxisPoints(5);
		gmodel.setBoundingBox(box);
		IPointGenerator<GridModel> gen = service.createGenerator(gmodel);
		
		for (Iterator it1 = genWithRandom.iterator(), it2 = gen.iterator(); it1.hasNext() && it2.hasNext();) {
			assertNotEquals(it1.next(), it2.next());
		}
	}

}
