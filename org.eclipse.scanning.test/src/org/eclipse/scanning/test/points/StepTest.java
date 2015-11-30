package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Actually tests any scalar scan
 * 
 * @author fcp94556
 *
 */
public class StepTest {
	
	private IGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}
	
	@Test
	public void testSizes() throws Exception {
		
		IGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel());

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
	public void testSequence() throws Exception {
		

		IGenerator<StepModel, IPosition> gen = service.createGenerator(new StepModel());

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

	private void checkSequence(IGenerator<StepModel, IPosition> gen, double... positions) throws Exception {
		
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
