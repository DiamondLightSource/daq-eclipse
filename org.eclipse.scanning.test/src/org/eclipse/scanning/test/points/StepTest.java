package org.eclipse.scanning.test.points;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.StepGenerator;
import org.junit.Before;
import org.junit.Test;

/**
 * Actually tests any scalar scan
 * 
 * @author Matthew Gerring
 *
 */
public class StepTest {
	
	private IPointGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new PointGeneratorFactory();
	}
	
	@Test
	public void testSizes() throws Exception {
		
		IPointGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel());

		StepModel model = new StepModel("Temperature", 290,300,1);	
		gen.setModel(model);
		assertEquals(11, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);
		
		model = new StepModel("Temperature", 0,10,1);	
		gen.setModel(model);
		assertEquals(11, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 1,11,1);	
		gen.setModel(model);
		assertEquals(11, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);
	
		model = new StepModel("Temperature", 0,3, 0.9);	
		gen.setModel(model);
		assertEquals(4, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 1,4, 0.9);	
		gen.setModel(model);
		assertEquals(4, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 0, 3, 0.8);	
		gen.setModel(model);
		assertEquals(4, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 1,4, 0.8);	
		gen.setModel(model);
		assertEquals(4, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);
		
		model = new StepModel("Temperature", 0,3, 0.6);	
		gen.setModel(model);
		assertEquals(6, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 1,4, 0.6);	
		gen.setModel(model);
		assertEquals(6, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 1,4, 0.5);	
		gen.setModel(model);
		assertEquals(7, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);

	}
	
	@Test
	public void testTooLargeStep() throws Exception {

		IPointGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel("fred", 0, 3, 5));
		assertEquals(1, gen.size());
		assertEquals(0d, gen.iterator().next().get("fred"));
		// TODO Should this throw an exception or do this? Possible to do a size 1 step makes some tests easier to write.
	}
	
	@Test(expected = GeneratorException.class)
	public void testMisdirectedStepGenSize() throws Exception {

		IPointGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel("Temperature", 290, 300, -1));
		gen.size();
	}

	@Test(expected = GeneratorException.class)
	public void testMisdirectedStepGenPoints() throws Exception {

		IPointGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel("Temperature", 290, 300, -1));
		gen.createPoints();
	}

	@Test
	public void testTolerance() throws Exception {
		IPointGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel());

		// within the 1% of step size tolerance
		StepModel model = new StepModel("Temperature", 0.0, 2.0, 0.667);	
		gen.setModel(model);
		assertEquals(4, gen.size());
		
		// outside the 1% of step size tolerance
		model = new StepModel("Temperature", 0.0, 2.0, 0.67);
		gen.setModel(model);
		assertEquals(3, gen.size());
	}
	
	@Test
	public void testSequence() throws Exception {
		

		IPointGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel());

		StepModel model = new StepModel("Temperature", 290,300,1);	
		gen.setModel(model);
		checkSequence(gen, 290.0, 291.0, 292.0, 293.0, 294.0, 295.0, 296.0, 297.0, 298.0, 299.0, 300.0);
		GeneratorUtil.testGeneratorPoints(gen);
		
		model = new StepModel("Temperature", 0,3, 0.6);	
		gen.setModel(model);
		checkSequence(gen, 0d, 0.6, 1.2, 1.8, 2.4, 3.0);
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 1, 4, 0.6);	
		gen.setModel(model);
		checkSequence(gen, 1.0, 1.6, 2.2, 2.8, 3.4, 4.0);
		GeneratorUtil.testGeneratorPoints(gen);
		
		model = new StepModel("Temperature", 11, 14, 0.6);	
		gen.setModel(model);
		checkSequence(gen, 11.0, 11.6, 12.2, 12.8, 13.4, 14.0);
		GeneratorUtil.testGeneratorPoints(gen);

		model = new StepModel("Temperature", 1,4, 0.5);	
		gen.setModel(model);
		checkSequence(gen, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0);
		GeneratorUtil.testGeneratorPoints(gen);
	}

	private void checkSequence(IPointGenerator<StepModel, IPosition> gen, double... positions) throws Exception {
		
		Iterator<IPosition> it = gen.iterator();
        for (int i = 0; i < positions.length; i++) {
			double position = positions[i];
			IPosition pos = it.next();
			if (!equalsWithinTolerance(new Double(position), (Number)pos.get("Temperature"), 0.00001)) {
				throw new Exception("Position not equal! "+(Number)pos.get("Temperature"));
			}
		} 
	}
	
	private static boolean equalsWithinTolerance(Number foo, Number bar, Number tolerance) {
		final double a = foo.doubleValue();
		final double b = bar.doubleValue();
		final double t = tolerance.doubleValue();	
		return t>=Math.abs(a-b);
	}

}
