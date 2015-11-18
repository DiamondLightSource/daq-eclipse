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

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
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
	public void testFillingRectangleNoROI() throws Exception {
		
		// Create a raster scan path
		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);
		model.setX(0);
		model.setY(0);
		model.setxLength(3);
		model.setyLength(3);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(model, null);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		
        checkPoints(pointList);
	}

	@Test
	public void testFillingRectangle() throws Exception {
		
		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(model, roi);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		
        checkPoints(pointList);
	}

	@Test
	public void testFillingRectangleIterator() throws Exception {
		
		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(model, roi);
        Iterator<Point> it = gen.iterator();
        List<Point> pointList = new ArrayList<Point>(7);
        while (it.hasNext()) pointList.add(it.next());
        
        assertArrayEquals(pointList.toArray(), gen.createPoints().toArray());
        
        checkPoints(pointList);
       
	}

	@Test
	public void testFillingCircle() throws Exception {
		
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
		
		assertEquals(pointList.size(), 316); 
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
		
		assertEquals(pointList.size(), 3156); 
 	}
	
	
	@Test
	public void testFillingPolygon() throws Exception {

        PolygonalROI diamond = new PolygonalROI(new double[]{1.5, 0});
        diamond.addPoint(new double[]{3,1.5});
        diamond.addPoint(new double[]{1.5,3});
        diamond.addPoint(new double[]{0,1.5});
        diamond.addPoint(new double[]{1.5, 0});
               
		GridModel model = new GridModel();
		model.setRows(20);
		model.setColumns(20);
		model.setX(0);
		model.setY(0);
		model.setxLength(3);
		model.setyLength(3);
		model.setLock(true); // Isolate the bounding box which can come back from the ROI wrong

		// Get the point list
		IGenerator<GridModel> gen = service.createGenerator(model, diamond);
		List<Point> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		
		assertTrue(pointList.size()<400); 
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

}
