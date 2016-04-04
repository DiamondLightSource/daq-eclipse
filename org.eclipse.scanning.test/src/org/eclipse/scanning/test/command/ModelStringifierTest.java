package org.eclipse.scanning.test.command;

import static org.junit.Assert.*;
import static org.eclipse.scanning.command.ModelStringifier.stringify;

import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.junit.Test;


public class ModelStringifierTest {
	// TODO: Test round-trips, i.e. interpret(stringify(model)) == model.

	@Test
	public void testStepModelConcise() {
		StepModel smodel = new StepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");

		assertEquals("step('fred', 0.0, 10.0, 1.0)", stringify(smodel, false));
	}

	@Test
	public void testStepModelVerbose() {
		StepModel smodel = new StepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");

		assertEquals("step(axis='fred', start=0.0, stop=10.0, step=1.0)", stringify(smodel, true));
	}

	@Test
	public void testGridModelConcise() {
		BoundingBox bbox = new BoundingBox();
		bbox.setFastAxisStart(0);
		bbox.setSlowAxisStart(1);
		bbox.setFastAxisLength(10);
		bbox.setSlowAxisLength(11);

		GridModel gmodel = new GridModel();
		gmodel.setSlowAxisName("alice");
		gmodel.setFastAxisName("bob");
		gmodel.setBoundingBox(bbox);
		gmodel.setSlowAxisPoints(3);
		gmodel.setFastAxisPoints(4);

		assertEquals(
				"grid(('bob', 'alice'), (0.0, 1.0), (10.0, 11.0), count=(3, 4), snake=False)",
				stringify(gmodel, false));
	}

	@Test
	public void testGridModelVerbose() {
		BoundingBox bbox = new BoundingBox();
		bbox.setFastAxisStart(0);
		bbox.setSlowAxisStart(1);
		bbox.setFastAxisLength(10);
		bbox.setSlowAxisLength(11);

		GridModel gmodel = new GridModel();
		gmodel.setSlowAxisName("alice");
		gmodel.setFastAxisName("bob");
		gmodel.setBoundingBox(bbox);
		gmodel.setSlowAxisPoints(3);
		gmodel.setFastAxisPoints(4);

		assertEquals(
				"grid(axes=('bob', 'alice'), origin=(0.0, 1.0), size=(10.0, 11.0), count=(3, 4), snake=False)",
				stringify(gmodel, true));
	}

}
