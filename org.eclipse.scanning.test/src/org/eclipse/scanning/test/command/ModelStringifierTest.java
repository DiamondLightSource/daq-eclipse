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
		bbox.setxStart(0);
		bbox.setyStart(1);
		bbox.setWidth(10);
		bbox.setHeight(11);

		GridModel gmodel = new GridModel();
		gmodel.setyName("alice");
		gmodel.setxName("bob");
		gmodel.setBoundingBox(bbox);
		gmodel.setRows(3);
		gmodel.setColumns(4);

		assertEquals(
				"grid(('bob', 'alice'), (0.0, 1.0), (10.0, 11.0), count=(3, 4), snake=False)",
				stringify(gmodel, false));
	}

	@Test
	public void testGridModelVerbose() {
		BoundingBox bbox = new BoundingBox();
		bbox.setxStart(0);
		bbox.setyStart(1);
		bbox.setWidth(10);
		bbox.setHeight(11);

		GridModel gmodel = new GridModel();
		gmodel.setyName("alice");
		gmodel.setxName("bob");
		gmodel.setBoundingBox(bbox);
		gmodel.setRows(3);
		gmodel.setColumns(4);

		assertEquals(
				"grid(axes=('bob', 'alice'), origin=(0.0, 1.0), size=(10.0, 11.0), count=(3, 4), snake=False)",
				stringify(gmodel, true));
	}

}
