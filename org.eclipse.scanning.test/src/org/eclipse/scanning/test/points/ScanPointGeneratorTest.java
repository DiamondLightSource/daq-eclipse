package org.eclipse.scanning.test.points;

import org.eclipse.scanning.points.ScanPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ScanPointGeneratorTest {
	
	@Test
	public void test1DLineGenerator() {
	    ScanPointGenerator spg = new ScanPointGenerator();
        
	    List<IPosition> points = spg.createLinePoints("'x'", "'mm'", 1.0, 5.0, 5, false);
	    
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
        
	    List<IPosition> points = spg.create2DLinePoints(names, "'mm'", start, stop, 5, false);
        
        assertEquals(new Point(0, 1.0, 0, 2.0, false), points.get(0));
        assertEquals(new Point(1, 2.0, 1, 4.0, false), points.get(1));
        assertEquals(new Point(2, 3.0, 2, 6.0, false), points.get(2));
        assertEquals(new Point(3, 4.0, 3, 8.0, false), points.get(3));
        assertEquals(new Point(4, 5.0, 4, 10.0, false), points.get(4));
	}
	
}
