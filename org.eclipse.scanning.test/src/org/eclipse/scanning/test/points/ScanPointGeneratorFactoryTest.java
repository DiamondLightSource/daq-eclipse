package org.eclipse.scanning.test.points;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;

public class ScanPointGeneratorFactoryTest {
	
    @Test
    public void testJLineGeneratorFactory1D() {
        JythonObjectFactory lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>)  lineGeneratorFactory.createObject(
				"x", "mm", 1.0, 5.0, 5);
        
        List<Object> expected_points = new ArrayList<Object>();
	    expected_points.add(new Scalar("x", 0, 1.0));
	    expected_points.add(new Scalar("x", 1, 2.0));
	    expected_points.add(new Scalar("x", 2, 3.0));
	    expected_points.add(new Scalar("x", 3, 4.0));
	    expected_points.add(new Scalar("x", 4, 5.0));
	    
	    int index = 0;
        while (iterator.hasNext()){

            Object point = iterator.next();
            assertEquals(expected_points.get(index), point);

        	index++;
        }
        assertEquals(5, index);
    }
    
    @Test
    public void testJLineGeneratorFactory2D() {
        JythonObjectFactory lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator2DFactory();
        
        String name = "XYLine";
        double[] start = {1.0, 2.0};
        double[] stop = {5.0, 10.0};
        
        @SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>) lineGeneratorFactory.createObject(
				name, "mm", start, stop, 5);
        
        List<Object> expected_points = new ArrayList<Object>();
	    expected_points.add(new Point("XYLine_X", 0, 1.0, "XYLine_Y", 0, 2.0, false));
	    expected_points.add(new Point("XYLine_X", 1, 2.0, "XYLine_Y", 1, 4.0, false));
	    expected_points.add(new Point("XYLine_X", 2, 3.0, "XYLine_Y", 2, 6.0, false));
	    expected_points.add(new Point("XYLine_X", 3, 4.0, "XYLine_Y", 3, 8.0, false));
	    expected_points.add(new Point("XYLine_X", 4, 5.0, "XYLine_Y", 4, 10.0, false));
	    
	    int index = 0;
        while (iterator.hasNext()){  // Just get first few points

            Object point = iterator.next();
            assertEquals(expected_points.get(index), point);

        	index++;
        }
        assertEquals(5, index);
    }
    
    @Test
    public void testJArrayGeneratorFactory() {
        JythonObjectFactory arrayGeneratorFactory = ScanPointGeneratorFactory.JArrayGeneratorFactory();
        
        double[] array_points = new double[] {1.0, 2.0, 3.0, 4.0, 5.0};

        @SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>) arrayGeneratorFactory.createObject(
				"x", "mm", array_points);
        
        List<Object> expected_points = new ArrayList<Object>();
	    expected_points.add(new Scalar("x", 0, 1.0));
	    expected_points.add(new Scalar("x", 1, 2.0));
	    expected_points.add(new Scalar("x", 2, 3.0));
	    expected_points.add(new Scalar("x", 3, 4.0));
	    expected_points.add(new Scalar("x", 4, 5.0));
	    
	    int index = 0;
        while (iterator.hasNext()){

            Object point = iterator.next();
            assertEquals(expected_points.get(index), point);

        	index++;
        }
        assertEquals(5, index);
    }
    
    @Test
    public void testJSpiralGeneratorFactory() {
        JythonObjectFactory spiralGeneratorFactory = ScanPointGeneratorFactory.JSpiralGeneratorFactory();
        
        String name = "XYSpiral";
        PyList centre = new PyList(Arrays.asList(new Double[] {0.0, 0.0}));
        double radius = 1.5;
        double scale = 1.0;
        boolean alternate = false;
        
        @SuppressWarnings("unchecked")
        Iterator<IPosition> iterator = (Iterator<IPosition>)  spiralGeneratorFactory.createObject(
				name, "mm", centre, radius, scale, alternate);
        
        List<Object> expected_points = new ArrayList<Object>();
	    expected_points.add(new Point("XYSpiral_X", 0, 0.23663214944574582, "XYSpiral_Y", 0, -0.3211855677650875, false));
	    expected_points.add(new Point("XYSpiral_X", 1, -0.6440318266552169, "XYSpiral_Y", 1, -0.25037538922751695, false));
	    expected_points.add(new Point("XYSpiral_X", 2, -0.5596688286164636, "XYSpiral_Y", 2, 0.6946549630820702, false));
	    expected_points.add(new Point("XYSpiral_X", 3, 0.36066957248394327, "XYSpiral_Y", 3, 0.9919687803189761, false));
	    expected_points.add(new Point("XYSpiral_X", 4, 1.130650533568409, "XYSpiral_Y", 4, 0.3924587351155914, false));
	    expected_points.add(new Point("XYSpiral_X", 5, 1.18586065489788, "XYSpiral_Y", 5, -0.5868891557832875, false));
	    
	    int index = 0;
	    while (iterator.hasNext() && index < 6){  // Just get first few points

            Object point = iterator.next();
            assertEquals(expected_points.get(index), point);

        	index++;
        }
        assertEquals(6, index);
    }
    
    @Test
    public void testJLissajousGeneratorFactory() {
        JythonObjectFactory lissajousGeneratorFactory = ScanPointGeneratorFactory.JLissajousGeneratorFactory();

        PyDictionary box = new PyDictionary();
        box.put("width", 1.5);
        box.put("height", 1.5);
        box.put("centre", new double[] {0.0, 0.0});

        String name = "XYLissajous";
        int numLobes = 2;
        int numPoints = 500;
        
        @SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>) lissajousGeneratorFactory.createObject(
				name, "mm", box, numLobes, numPoints);
        
        List<Object> expected_points = new ArrayList<Object>();
	    expected_points.add(new Point("XYLissajous_X", 0, 0.0, "XYLissajous_Y", 0, 0.0, false));
	    expected_points.add(new Point("XYLissajous_X", 1, 0.01884757158250311, "XYLissajous_Y", 1, 0.028267637002450906, false));
	    expected_points.add(new Point("XYLissajous_X", 2, 0.03768323863482717, "XYLissajous_Y", 2, 0.05649510414594954, false));
	    expected_points.add(new Point("XYLissajous_X", 3, 0.05649510414594954, "XYLissajous_Y", 3, 0.08464228865511125, false));
	    expected_points.add(new Point("XYLissajous_X", 4, 0.07527128613841116, "XYLissajous_Y", 4, 0.1126691918405678, false));
	    expected_points.add(new Point("XYLissajous_X", 5, 0.0939999251732282, "XYLissajous_Y", 5, 0.14053598593929348, false));
	    
	    int index = 0;
        while (iterator.hasNext() && index < 6){  // Just get first few points

            Object point = iterator.next();
            assertEquals(expected_points.get(index), point);

        	index++;
        }
        assertEquals(6, index);
    }
    
    @Test
    public void testJCompoundGeneratorFactoryWithRaster() {
		
		JythonObjectFactory lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> outerLine = (Iterator<IPosition>)  lineGeneratorFactory.createObject(
				"y", "mm", 0.0, 5.0, 2);
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> innerLine = (Iterator<IPosition>)  lineGeneratorFactory.createObject(
				"x", "mm", 1.0, 3.0, 3, true);
		
		JythonObjectFactory compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();
        
        Object[] generators = {outerLine, innerLine};
        Object[] excluders = {};
        Object[] mutators = {};
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>)  compoundGeneratorFactory.createObject(
				generators, excluders, mutators);
        
        List<Object> expected_points = new ArrayList<Object>();
	    expected_points.add(new Point(0, 1.0, 0, 0.0));
	    expected_points.add(new Point(1, 2.0, 0, 0.0));
	    expected_points.add(new Point(2, 3.0, 0, 0.0));
	    expected_points.add(new Point(2, 3.0, 1, 5.0));
	    expected_points.add(new Point(1, 2.0, 1, 5.0));
	    expected_points.add(new Point(0, 1.0, 1, 5.0));
	    
	    int index = 0;
        while (iterator.hasNext()){

            Object point = iterator.next();
            assertEquals(expected_points.get(index), point);

        	index++;
        }
        assertEquals(6, index);
    }
    
    @Test
    public void testJCompoundGeneratorFactoryWithMutatedRaster() {
        JythonObjectFactory lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> line1 = (Iterator<IPosition>)  lineGeneratorFactory.createObject(
				"y", "mm", 2.0, 10.0, 5);
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> line2 = (Iterator<IPosition>)  lineGeneratorFactory.createObject(
				"x", "mm", 1.0, 5.0, 5);
		
        JythonObjectFactory randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();
		
        int seed = 10;

        PyDictionary maxOffset = new PyDictionary();
        maxOffset.put("x", 0.5);
        maxOffset.put("y", 0.5);
        
		PyObject randomOffset = (PyObject) randomOffsetMutatorFactory.createObject(seed, maxOffset);
        
        JythonObjectFactory compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();
        
        Object[] generators = {line1, line2};
        Object[] excluders = {};
        Object[] mutators = {randomOffset};
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>)  compoundGeneratorFactory.createObject(
				generators, excluders, mutators);
        
        List<Object> expected_points = new ArrayList<Object>();
//	    expected_points.add(new MapPosition("x:0:1.1984860665000001, y:0:2.0248069105"));
//	    expected_points.add(new MapPosition("x:1:2.328712106, y:0:1.674470636"));
//	    expected_points.add(new MapPosition("x:2:3.0371860455, y:0:2.4094030615"));
//	    expected_points.add(new MapPosition("x:3:3.9511518765, y:0:2.115850706"));
//	    expected_points.add(new MapPosition("x:4:5.043717526, y:0:1.5626032015"));
//	    expected_points.add(new MapPosition("x:0:0.6772733115, y:1:4.1939752055"));
//	    expected_points.add(new MapPosition("x:1:1.5828061555000001, y:1:3.9489767459999996"));
//	    expected_points.add(new MapPosition("x:2:3.3888981960000004, y:1:3.661987452"));
//	    expected_points.add(new MapPosition("x:3:3.9093635265, y:1:4.2730717205"));
//	    expected_points.add(new MapPosition("x:4:4.554744956, y:1:3.8436031415"));
	    expected_points.add(new Point("x", 0, 1.0248069105000002, "y", 0, 2.1984860665));
	    expected_points.add(new Point("x", 1, 1.674470636, "y", 0, 2.328712106));
	    expected_points.add(new Point("x", 2, 3.4094030615, "y", 0, 2.0371860455));
	    expected_points.add(new Point("x", 3, 4.115850706, "y", 0, 1.9511518765));
	    expected_points.add(new Point("x", 4, 4.5626032015, "y", 0, 2.043717526));
	    expected_points.add(new Point("x", 0, 1.1939752055, "y", 1, 3.6772733115));
	    expected_points.add(new Point("x", 1, 1.9489767459999996, "y", 1, 3.5828061555000001));
	    expected_points.add(new Point("x", 2, 2.661987452, "y", 1, 4.3888981960000004));
	    expected_points.add(new Point("x", 3, 4.2730717205, "y", 1, 3.9093635265));
	    expected_points.add(new Point("x", 4, 4.8436031415, "y", 1, 3.554744956));
	    
	    int index = 0;
        while (iterator.hasNext() && index < 10){  // Just get first few points

            Object point = iterator.next();
            assertEquals(expected_points.get(index), point);
            expected_points.add(point);

        	index++;
        }
        assertEquals(10, index);
    }
}
