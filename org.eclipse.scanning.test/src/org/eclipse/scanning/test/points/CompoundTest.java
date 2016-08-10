package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

public class CompoundTest {
	
	private IPointGeneratorService service;
	
	@Before
	public void before() throws Exception {
		ScanPointGeneratorFactory.setBundlePath("../org.eclipse.scanning.points");
		service = new PointGeneratorFactory();
	}

	@Test(expected=org.python.core.PyException.class)
	public void testCompoundCompoundException() throws Exception {

		IPointGenerator<StepModel> pos = service.createGenerator(new StepModel("Position", 1, 4, 0.6));
		
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);

		GridModel model = new GridModel();
		model.setSlowAxisPoints(20);
		model.setFastAxisPoints(20);
		model.setBoundingBox(box);

		IPointGenerator<GridModel> gen = service.createGenerator(model);
		IPointGenerator<?> scan = service.createCompoundGenerator(pos, gen);
		scan.iterator();
	}
	@Test(expected=org.python.core.PyException.class)
	public void testDuplicateAxisNameException() throws Exception {

		IPointGenerator<StepModel> pos1 = service.createGenerator(new StepModel("Position", 1, 4, 0.6));
		IPointGenerator<StepModel> pos2 = service.createGenerator(new StepModel("Position", 1, 4, 0.6));
		IPointGenerator<?> scan = service.createCompoundGenerator(pos1, pos2);
		scan.iterator().next();
	}
	@Test
	public void testIteratedSize() throws Exception {

		IPointGenerator<StepModel> temp = service.createGenerator(new StepModel("Temperature", 290,295,1));
		assertEquals(6, temp.size());

		IPointGenerator<StepModel> pos = service.createGenerator(new StepModel("Position", 1,4, 0.6));
		assertEquals(6, pos.size());

		IPointGenerator<?> scan = service.createCompoundGenerator(temp, pos);
		assertTrue(scan.iterator().next()!=null);
		assertEquals(36, scan.size());
		
		Iterator<IPosition> it = scan.iterator();
		int size = scan.size();
		int sz=0;
		while(it.hasNext()) {
			it.next();
			sz++;
			if (sz>size) throw new Exception("Iterator grew too large!");
		}
	}
	
	@Test
	public void testSimpleCompoundStep2Step() throws Exception {
		
		IPointGenerator<StepModel> temp = service.createGenerator(new StepModel("Temperature", 290,295,1));
		assertEquals(6, temp.size());

		IPointGenerator<StepModel> pos = service.createGenerator(new StepModel("Position", 1,4, 0.6));
		assertEquals(6, pos.size());

		IPointGenerator<?> scan = service.createCompoundGenerator(temp, pos);
		assertEquals(36, scan.size());

		final List<IPosition> points = scan.createPoints();
		
		// 290K
		assertEquals(new Double(290), (Double)points.get(0).get("Temperature"));
		assertEquals(new Double(1),   (Double)points.get(0).get("Position"));
		assertEquals(new Double(290), (Double)points.get(1).get("Temperature"));
		assertEquals(new Double(1.6), (Double)points.get(1).get("Position"));
		assertEquals(new Double(290), (Double)points.get(2).get("Temperature"));
		assertEquals(new Double(2.2), (Double)points.get(2).get("Position"));
		
		// 291K
		assertEquals(new Double(291), (Double)points.get(6).get("Temperature"));
		assertEquals(new Double(1),   (Double)points.get(6).get("Position"));
		assertEquals(new Double(291), (Double)points.get(7).get("Temperature"));
		assertEquals(new Double(1.6), (Double)points.get(7).get("Position"));
		assertEquals(new Double(291), (Double)points.get(8).get("Temperature"));
		assertEquals(new Double(2.2), (Double)points.get(8).get("Position"));
		
		// 295K
		assertEquals(new Double(295), (Double)points.get(30).get("Temperature"));
		assertEquals(new Double(1),   (Double)points.get(30).get("Position"));
		assertEquals(new Double(295), (Double)points.get(31).get("Temperature"));
		assertEquals(new Double(1.6), (Double)points.get(31).get("Position"));
		assertEquals(new Double(295), (Double)points.get(32).get("Temperature"));
		assertEquals(new Double(2.2), (Double)points.get(32).get("Position"));

        GeneratorUtil.testGeneratorPoints(scan);
	}
	
	@Test
	public void testSimpleCompoundStep3Step() throws Exception {
		
		IPointGenerator<StepModel> temp = service.createGenerator(new StepModel("Temperature", 290,295,1));
		assertEquals(6, temp.size());

		IPointGenerator<StepModel> y = service.createGenerator(new StepModel("Y", 11, 14, 0.6));
		assertEquals(6, y.size());

		IPointGenerator<StepModel> x = service.createGenerator(new StepModel("X", 1, 4, 0.6));
		assertEquals(6, x.size());
	

		IPointGenerator<?> scan = service.createCompoundGenerator(temp, y, x);
		assertEquals(216, scan.size());

		final List<IPosition> points = scan.createPoints();
		
		// 290K
		assertEquals(new Double(290), (Double)points.get(0).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(0).get("Y"));
		assertEquals(new Double(1),   (Double)points.get(0).get("X"));
		assertEquals(new Double(290), (Double)points.get(1).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(1).get("Y"));
		assertEquals(new Double(1.6), (Double)points.get(1).get("X"));
		assertEquals(new Double(290), (Double)points.get(2).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(2).get("Y"));
		assertEquals(new Double(2.2), (Double)points.get(2).get("X"));
		
		// 291K
		assertEquals(new Double(291), (Double)points.get(36).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(36).get("Y"));
		assertEquals(new Double(1),   (Double)points.get(36).get("X"));
		assertEquals(new Double(291), (Double)points.get(37).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(37).get("Y"));
		assertEquals(new Double(1.6), (Double)points.get(37).get("X"));
		assertEquals(new Double(291), (Double)points.get(38).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(38).get("Y"));
		assertEquals(new Double(2.2), (Double)points.get(38).get("X"));

		// 295K
		assertEquals(new Double(295), (Double)points.get(180).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(180).get("Y"));
		assertEquals(new Double(1),   (Double)points.get(180).get("X"));
		assertEquals(new Double(295), (Double)points.get(181).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(181).get("Y"));
		assertEquals(new Double(1.6), (Double)points.get(181).get("X"));
		assertEquals(new Double(295), (Double)points.get(182).get("Temperature"));
		assertEquals(new Double(11),  (Double)points.get(182).get("Y"));
		assertEquals(new Double(2.2), (Double)points.get(182).get("X"));

        GeneratorUtil.testGeneratorPoints(scan);
	}

	@Test
	public void testSimpleCompoundGrid() throws Exception {
		
		IPointGenerator<StepModel> temp = service.createGenerator(new StepModel("Temperature", 290,300,1));
		assertEquals(11, temp.size());

//		BoundingBox box = new BoundingBox();
//		box.setFastAxisStart(0);
//		box.setSlowAxisStart(0);
//		box.setFastAxisLength(3);
//		box.setSlowAxisLength(3);
//
//		GridModel model = new GridModel();
//		model.setSlowAxisPoints(20);
//		model.setFastAxisPoints(20);
//		model.setBoundingBox(box);
//		
//		IPointGenerator<GridModel> grid = service.createGenerator(model);

		IPointGenerator<StepModel> line1 = service.createGenerator(new StepModel("x", 0.075, 2.925, 3.0/20.0));
		assertEquals(20, line1.size());
		IPointGenerator<StepModel> line2 = service.createGenerator(new StepModel("y", 0.075, 2.925, 3.0/20.0));
		assertEquals(20, line2.size());
		
//		BoundingLine bLine1 = new BoundingLine();
//		bLine1.setxStart(0.0);
//		bLine1.setyStart(0.0);
//		bLine1.setLength(3.0);
//		OneDEqualSpacingModel model1 = new OneDEqualSpacingModel();
//		model1.setBoundingLine(bLine1);
//		model1.setName("x");
//		model1.setPoints(20);
//		IPointGenerator<OneDEqualSpacingModel> line1 = service.createGenerator(model1);
//		assertEquals(20, line1.size());
//		
//		BoundingLine bLine2 = new BoundingLine();
//		bLine2.setxStart(0.0);
//		bLine2.setyStart(0.0);
//		bLine2.setLength(3.0);
//		OneDEqualSpacingModel model2 = new OneDEqualSpacingModel();
//		model2.setBoundingLine(bLine2);
//		model2.setName("y");
//		model2.setPoints(20);
//		IPointGenerator<OneDEqualSpacingModel> line2 = service.createGenerator(model2);
//		assertEquals(20, line2.size());
		
		IPointGenerator<?> scan = service.createCompoundGenerator(temp, line2, line1);
		assertEquals(4400, scan.size());

		List<IPosition> points = scan.createPoints();

		List<IPosition> first400 = new ArrayList<>(400);

		// The fist 400 should be T=290
		for (int i = 0; i < 400; i++) {
			assertEquals(new Double(290.0), points.get(i).get("Temperature"));
			first400.add(points.get(i));
		}
		checkPoints(first400);
		
		for (int i = 400; i < 800; i++) {
			assertEquals(new Double(291.0), points.get(i).get("Temperature"));
		}
		for (int i = 4399; i >= 4000; i--) {
			assertEquals(new Double(300.0), points.get(i).get("Temperature"));
		}
        GeneratorUtil.testGeneratorPoints(scan);

	}
	
	private void checkPoints(List<IPosition> pointList) {
		// Check correct number of points
		assertEquals(20 * 20, pointList.size());

		// Check some random points are correct
		assertEquals(0.075, (Double)pointList.get(0).get("x"), 1e-8);
		assertEquals(0.075, (Double)pointList.get(0).get("y"), 1e-8);

		assertEquals(0.075 + 3 * (3.0 / 20.0), (Double)pointList.get(3).get("x"), 1e-8);
		assertEquals(0.075 + 0.0, (Double)pointList.get(3).get("y"), 1e-8);

		assertEquals(0.075 + 2 * (3.0 / 20.0), (Double)pointList.get(22).get("x"), 1e-8);
		assertEquals(0.075 + 1 * (3.0 / 20.0), (Double)pointList.get(22).get("y"), 1e-8);

		assertEquals(0.075 + 10 * (3.0 / 20.0), (Double)pointList.get(350).get("x"), 1e-8);
		assertEquals(0.075 + 17 * (3.0 / 20.0), (Double)pointList.get(350).get("y"), 1e-8);

	}
	
	@Test
	public void testNestedNeXus() throws Exception {
		
		int[] size = {10,8,5};
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("xNex");
		gmodel.setFastAxisPoints(size[size.length-2]);
		gmodel.setSlowAxisName("yNex");
		gmodel.setSlowAxisPoints(size[size.length-1]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<StepModel> line1 = service.createGenerator(new StepModel("xNex", 0.075, 2.925, size[size.length-2]));
		IPointGenerator<StepModel> line2 = service.createGenerator(new StepModel("yNex", 0.075, 2.925, size[size.length-1]));
		
		IPointGenerator<?> gen = service.createGenerator(gmodel);
		
		// We add the outer scans, if any
		if (size.length > 2) {
			IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length];
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model = new StepModel("neXusScannable"+dim, 10,20,11/size[dim]);
				gens[dim] = service.createGenerator(model);
			}
		gens[size.length-1] = line1;
		gens[size.length-2] = line2;
		gen = service.createCompoundGenerator(gens);
		}
		
		final IPosition pos = gen.iterator().next();
		assertEquals(size.length, pos.size());
		
	}
}
