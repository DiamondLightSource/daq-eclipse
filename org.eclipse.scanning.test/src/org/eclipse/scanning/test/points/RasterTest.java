package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

public class RasterTest {

	
	private IPointGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new PointGeneratorFactory();
	}

	@Test
	public void testFillingBoundingRectangle() throws Exception {
		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		RasterModel model = new RasterModel();
		model.setxStep(1);
		model.setyStep(1);

		// Get the point list
		IPointGenerator<RasterModel,Point> gen = service.createGenerator(model, boundingRectangle);
		List<Point> pointList = gen.createPoints();

		// Check correct number of points
		assertEquals(16, pointList.size());

		// Check some random points are correct
		assertEquals(0.0, pointList.get(0).getX(), 1e-8);
		assertEquals(0.0, pointList.get(0).getY(), 1e-8);

		assertEquals(3.0, pointList.get(3).getX(), 1e-8);
		assertEquals(0.0, pointList.get(3).getY(), 1e-8);

		assertEquals(3.0, pointList.get(7).getX(), 1e-8);
		assertEquals(1.0, pointList.get(7).getY(), 1e-8);

		assertEquals(3.0, pointList.get(11).getX(), 1e-8);
		assertEquals(2.0, pointList.get(11).getY(), 1e-8);
		
        GeneratorUtil.testGeneratorPoints(gen);
	}

	// Note this is a bit of a integration test not a strict unit test
	@Test
	public void testFillingAMoreComplicatedBoundingRectangle() throws Exception {
		double xStart = 0.0;
		double xStop = 25.5;
		double yStart = 0.0;
		double yStop = 33.33;

		double xStep = 0.4;
		double yStep = 0.6;

		RectangularROI roi = new RectangularROI();
		roi.setPoint(Math.min(xStart, xStop), Math.min(yStart, yStop));
		roi.setLengths(Math.abs(xStop - xStart), Math.abs(yStop - yStart));

	
		RasterModel model = new RasterModel();
		model.setxStep(xStep);
		model.setyStep(yStep);

		// Get the point list
		IPointGenerator<RasterModel,Point> gen = service.createGenerator(model, roi);
		List<Point> pointList = gen.createPoints();

		int rows = (int) (Math.floor((xStop - xStart) / xStep) + 1);
		int cols = (int) (Math.floor((yStop - yStart) / yStep) + 1);
		// Check the list size
		assertEquals("Point list size should be correct", rows * cols, pointList.size());

		// Check some points
		assertEquals(new Point(0, xStart, 0, yStart), pointList.get(0));
		assertEquals(xStart + 3 * xStep, pointList.get(3).getX(), 1e-8);
		// TODO more
		
        GeneratorUtil.testGeneratorPoints(gen);
	}

	// Note this is a bit of a integration test not a strict unit test
	@Test
	public void testFillingACircle() throws Exception {
		double xCentre = 0;
		double yCentre = 0;
		double radius = 1;

		CircularROI roi = new CircularROI();
		roi.setPoint(xCentre, yCentre);
		roi.setRadius(radius);

		RasterModel model = new RasterModel();
		model.setxStep(1);
		model.setyStep(1);

		// Get the point list
		IPointGenerator<RasterModel,Point> gen = service.createGenerator(model, roi);
		List<Point> pointList = gen.createPoints();

		// Check the length of the lists are equal
		assertEquals(5, pointList.size());

		// Check the points are correct and the order is maintained
		// 0
		assertEquals(0, pointList.get(0).getX(), 1e-8);
		assertEquals(-1, pointList.get(0).getY(), 1e-8);
		// 1
		assertEquals(-1, pointList.get(1).getX(), 1e-8);
		assertEquals(0, pointList.get(1).getY(), 1e-8);
		// 2
		assertEquals(0, pointList.get(2).getX(), 1e-8);
		assertEquals(0, pointList.get(2).getY(), 1e-8);
		// 3
		assertEquals(1, pointList.get(3).getX(), 1e-8);
		assertEquals(0, pointList.get(3).getY(), 1e-8);
		// 4
		assertEquals(0, pointList.get(4).getX(), 1e-8);
		assertEquals(1, pointList.get(4).getY(), 1e-8);
		
        GeneratorUtil.testGeneratorPoints(gen);
	}

	
	@Test
	public void testNestedNeXus() throws Exception {
		
		int[] size = {8,5};
		
		// Create scan points for a grid and make a generator
		RasterModel rmodel = new RasterModel();
		rmodel.setxName("xNex");
		rmodel.setxStep(3d/size[1]);
		rmodel.setyName("yNex");
		rmodel.setyStep(3d/size[0]);
		rmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<?,IPosition> gen = service.createGenerator(rmodel);
    
		IPosition first = gen.iterator().next();
		assertEquals(0d, first.get("xNex"));
		assertEquals(0d, first.get("yNex"));
		
		IPosition last = null;
		Iterator<IPosition> it = gen.iterator();
		while(it.hasNext()) last = it.next();
		
		assertEquals(3d, last.get("xNex"));
		assertEquals(3d, last.get("yNex"));
		
	}

}
