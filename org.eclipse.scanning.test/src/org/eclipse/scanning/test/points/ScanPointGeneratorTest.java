package org.eclipse.scanning.test.points;

import org.eclipse.scanning.points.ScanPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.Scalar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ScanPointGeneratorTest {
	
	@Test
	public void test1DLineGenerator() {
	    ScanPointGenerator spg = new ScanPointGenerator();
        
	    List<IPosition> points = spg.createLinePoints("x", "mm", 1.0, 5.0, 5);
	    
	    assertEquals(new Point(0, 1.0, 0, 1.0, false), points.get(0));
	    assertEquals(new Point(1, 2.0, 1, 2.0, false), points.get(1));
	    assertEquals(new Point(2, 3.0, 2, 3.0, false), points.get(2));
	    assertEquals(new Point(3, 4.0, 3, 4.0, false), points.get(3));
	    assertEquals(new Point(4, 5.0, 4, 5.0, false), points.get(4));
	}
	
	@Test
	public void test2DLineGenerator() {
	    ScanPointGenerator spg = new ScanPointGenerator();

        String[] names = {"'x'", "'y'"};
        double[] start = {1.0, 2.0};
        double[] stop = {5.0, 10.0};
        
	    List<IPosition> points = spg.create2DLinePoints(names, "mm", start, stop, 5);
        
        assertEquals(new Point(0, 1.0, 0, 2.0, false), points.get(0));
        assertEquals(new Point(1, 2.0, 1, 4.0, false), points.get(1));
        assertEquals(new Point(2, 3.0, 2, 6.0, false), points.get(2));
        assertEquals(new Point(3, 4.0, 3, 8.0, false), points.get(3));
        assertEquals(new Point(4, 5.0, 4, 10.0, false), points.get(4));
	}
    
    @Test
    public void testArrayGenerator() {
        ScanPointGenerator spg = new ScanPointGenerator();
        
        double[] p1 = {1.0};
        double[] p2 = {2.0};
        double[] p3 = {3.0};
        double[] p4 = {4.0};
        double[] p5 = {5.0};
        double[][] arrayPoints = {p1, p2, p3, p4, p5};
        
        List<IPosition> points = spg.createArrayPoints("x", "mm", arrayPoints);
        
        assertEquals(new Scalar("x", 0, 1.0), points.get(0));
        assertEquals(new Scalar("x", 1, 2.0), points.get(1));
        assertEquals(new Scalar("x", 2, 3.0), points.get(2));
        assertEquals(new Scalar("x", 3, 4.0), points.get(3));
        assertEquals(new Scalar("x", 4, 5.0), points.get(4));
    }
	
	@Test
	public void testRasterGenerator() {
	    ScanPointGenerator spg = new ScanPointGenerator();

        HashMap<String, Object> inner = new HashMap<String, Object>();
        inner.put("name", "x");
        inner.put("units", "mm");
        inner.put("start", 1.0);
        inner.put("stop", 3.0);
        inner.put("num_points", 3);

        HashMap<String, Object> outer = new HashMap<String, Object>();
        outer.put("name", "y");
        outer.put("units", "mm");
        outer.put("start", 0.0);
        outer.put("stop", 5.0);
        outer.put("num_points", 2);
        
	    List<IPosition> points = spg.createRasterPoints(inner, outer, true);
        
        assertEquals(new Point(0, 1.0, 0, 0.0), points.get(0));
        assertEquals(new Point(1, 2.0, 0, 0.0), points.get(1));
        assertEquals(new Point(2, 3.0, 0, 0.0), points.get(2));
        assertEquals(new Point(2, 3.0, 1, 5.0), points.get(3));
        assertEquals(new Point(1, 2.0, 1, 5.0), points.get(4));
        assertEquals(new Point(0, 1.0, 1, 5.0), points.get(5));
	}
    
    @Test
    public void testSpiralGenerator() {
        ScanPointGenerator spg = new ScanPointGenerator();

        String[] names = {"'x'", "'y'"};
        double[] centre = {0.0, 0.0};
        double radius = 1.5;
        double scale = 1.0;
        boolean alternateDirection = false;
        
        List<IPosition> points = spg.createSpiralPoints(names, "mm", centre, radius, scale, alternateDirection);
        
        assertEquals(new Point(0, 0.23663214944574582, 0, -0.3211855677650875, false), points.get(0));
        assertEquals(new Point(1, -0.6440318266552169, 1, -0.25037538922751695, false), points.get(1));
        assertEquals(new Point(2, -0.5596688286164636, 2, 0.6946549630820702, false), points.get(2));
        assertEquals(new Point(3, 0.36066957248394327, 3, 0.9919687803189761, false), points.get(3));
        assertEquals(new Point(4, 1.130650533568409, 4, 0.3924587351155914, false), points.get(4));
        assertEquals(new Point(5, 1.18586065489788, 5, -0.5868891557832875, false), points.get(5));
        assertEquals(new Point(6, 0.5428735608675326, 6, -1.332029488076613, false), points.get(6));
    }
    
    @Test
    public void testLissajousGenerator() {
        ScanPointGenerator spg = new ScanPointGenerator();

        double width = 1.5;
        double height = 1.5;
        double[] centre = {0.0, 0.0};
        
        HashMap<String, Object> box = new HashMap<String, Object>();
        box.put("width", String.valueOf(width));
        box.put("height", String.valueOf(height));
        box.put("centre", Arrays.toString(centre));
        
        String[] names = {"'x'", "'y'"};
        int numLobes = 2;
        int numPoints = 500;
        
        List<IPosition> points = spg.createLissajousPoints(names, "mm", box, numLobes, numPoints, false);
        
        assertEquals(new Point(0, 0.0, 0, 0.0, false), points.get(0));
        assertEquals(new Point(1, 0.01884757158250311, 1, 0.028267637002450906, false), points.get(1));
        assertEquals(new Point(2, 0.03768323863482717, 2, 0.05649510414594954, false), points.get(2));
        assertEquals(new Point(3, 0.05649510414594954, 3, 0.08464228865511125, false), points.get(3));
        assertEquals(new Point(4, 0.07527128613841116, 4, 0.1126691918405678, false), points.get(4));
        assertEquals(new Point(5, 0.0939999251732282, 5, 0.14053598593929348, false), points.get(5));
    }
}
