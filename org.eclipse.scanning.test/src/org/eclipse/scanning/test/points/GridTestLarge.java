package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

public class GridTestLarge {

	private IGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}
	
	@Test
	public void testApprox10millIteratorTimeCircle() throws Exception {
		
		// Create a simple bounding rectangle
		CircularROI roi = new CircularROI(500, 500, 500);

		// Create a raster scan path
		GridModel model = new GridModel();
		model.setRows(3162);
		model.setColumns(3162);

		testIteratorTime(model, roi, 7852632, 20000);
	}
	
	@Test
	public void test10millIteratorTimeRectangle() throws Exception {
		
		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 1000, 10000, 0);

		// Create a raster scan path
		GridModel model = new GridModel();
		model.setRows(1000);
		model.setColumns(10000);

		testIteratorTime(model, roi, 10000000, 20000);
	}

	
	private void testIteratorTime(GridModel model, IROI roi, int size, long tenMilTime) throws Exception {
		
		long start = System.currentTimeMillis();
		
		// Get the point list
		IGenerator<GridModel,Point> gen = service.createGenerator(model, roi);
        Iterator<Point>       it  = gen.iterator();

		long after1 = System.currentTimeMillis();

		assertTrue(start>(after1-50)); // Shouldn't take that long to make it!
		
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
		if ((after3-after2)>tenMilTime) throw new Exception(" The time was longer than "+tenMilTime+" to iterate "+size+". It took "+(after3-after2));
		
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
		IGenerator<GridModel,Point> gen = service.createGenerator(gridScanPath, boundingRectangle);
		List<Point> points = gen.createPoints();
		
		assertEquals(10000000, points.size());

		long after = System.currentTimeMillis();
		
		System.out.println("It took "+(after-start)+"ms to make 10million Point and keep in memory");

	}

}
