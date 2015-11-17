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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class GridTest {
	
	private IGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}
	
	@Test
	public void testFillingBoundingRectangleNoROI() throws Exception {
		
		// Create a raster scan path
		GridModel gridScanPath = new GridModel();
		gridScanPath.setRows(20);
		gridScanPath.setColumns(20);
		gridScanPath.setMinX(0);
		gridScanPath.setMinY(0);
		gridScanPath.setxLength(3);
		gridScanPath.setyLength(3);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(gridScanPath, null);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		
        checkPoints(pointList);
	}


	@Test
	public void testFillingBoundingRectangle() throws Exception {
		
		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		GridModel gridScanPath = new GridModel();
		gridScanPath.setRows(20);
		gridScanPath.setColumns(20);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(gridScanPath, boundingRectangle);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		
        checkPoints(pointList);
	}
	
	@Test
	public void testFillingBoundingCircle() throws Exception {
		
		// Create a simple bounding rectangle
		CircularROI circle = new CircularROI(1.5, 1.5, 1.5);

		// Create a raster scan path
		GridModel gridScanPath = new GridModel();
		gridScanPath.setRows(20);
		gridScanPath.setColumns(20);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(gridScanPath, circle);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		
		// TODO Check circle performed!
 	}
	
	@Test
	public void testFillingBoundingCircleSkewed() throws Exception {
		
		// Create a simple bounding rectangle
		CircularROI circle = new CircularROI(1.5, 1.5, 15);

		// Create a raster scan path
		GridModel gridScanPath = new GridModel();
		gridScanPath.setRows(20);
		gridScanPath.setColumns(200);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(gridScanPath, circle);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		
		// TODO Check circle performed!
 	}


	private void checkPoints(List<Point> pointList) {
		// Check correct number of points
		assertEquals(20 * 20, pointList.size());

		// Check some random points are correct
		assertEquals(0.075, pointList.get(0).getX(), 1e-8);
		assertEquals(0.075, pointList.get(0).getY(), 1e-8);

		assertEquals(0.075 + 3 * (3.0 / 20.0), pointList.get(3).getX(), 1e-8);
		assertEquals(0.075 + 0.0, pointList.get(3).getY(), 1e-8);

		assertEquals(0.075 + 2 * (3.0 / 20.0), pointList.get(22).getX(), 1e-8);
		assertEquals(0.075 + 1 * (3.0 / 20.0), pointList.get(22).getY(), 1e-8);

		assertEquals(0.075 + 10 * (3.0 / 20.0), pointList.get(350).getX(), 1e-8);
		assertEquals(0.075 + 17 * (3.0 / 20.0), pointList.get(350).getY(), 1e-8);

	}

	@Test
	public void testFillingBoundingRectangleIterator() throws Exception {
		
		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		GridModel gridScanPath = new GridModel();
		gridScanPath.setRows(20);
		gridScanPath.setColumns(20);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(gridScanPath, boundingRectangle);
        Iterator<Point> it = gen.iterator();
        List<Point> pointList = new ArrayList<Point>(7);
        while (it.hasNext()) pointList.add(it.next());
        
        assertArrayEquals(pointList.toArray(), gen.createPoints().toArray());
        
        checkPoints(pointList);
       
	}
	
	@Test
	public void testApprox10millIteratorTimeCircle() throws Exception {
		
		// Create a simple bounding rectangle
		CircularROI roi = new CircularROI(500, 500, 500);

		// Create a raster scan path
		GridModel model = new GridModel();
		model.setRows(3162);
		model.setColumns(3162);

		testIteratorTime(model, roi, 7852633, 10000);
	}
	
	@Test
	public void test10millIteratorTimeRectangle() throws Exception {
		
		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 1000, 10000, 0);

		// Create a raster scan path
		GridModel model = new GridModel();
		model.setRows(1000);
		model.setColumns(10000);

		testIteratorTime(model, roi, 10000000, 500);
	}

	
	private void testIteratorTime(GridModel model, IROI roi, int size, long tenMilTime) throws Exception {
		
		long start = System.currentTimeMillis();
		
		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(model, roi);
        Iterator<Point>       it  = gen.iterator();

		long after1 = System.currentTimeMillis();

		assertTrue(start>(after1-200)); // Shouldn't take that long to make it!
		
		// Now iterate a few, shouldn't take that long
		int count = 0;
		while (it.hasNext()) {
			Point point = (Point) it.next();
			count++;
			if (count>10000) break;
		}
		
		long after2 = System.currentTimeMillis();
		assertTrue(after1>(after2-200)); // Shouldn't take that long to make it!

		while (it.hasNext()) { // 10mill!
			Point point = (Point) it.next();
			count++;
		}
		
		long after3 = System.currentTimeMillis();
		System.out.println("It took "+(after3-after2)+"ms to iterate "+size+" with "+roi.getClass().getSimpleName());
		assertTrue(after2>(after3-tenMilTime)); // Shouldn't take that long to make it!
		
		assertEquals(size, count);
		
	}

	@Test
	public void test10millTimeInMemory() throws Exception {

		long start = System.currentTimeMillis();
		
		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 1000, 10000, 0);

		// Create a raster scan path
		GridModel gridScanPath = new GridModel();
		gridScanPath.setRows(1000);
		gridScanPath.setColumns(10000);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(gridScanPath, boundingRectangle);
		List<Point> points = gen.createPoints();
		
		assertEquals(10000000, points.size());

		long after = System.currentTimeMillis();
		
		System.out.println("It took "+(after-start)+"ms to make 10million Point and keep in memory");

	}

}
