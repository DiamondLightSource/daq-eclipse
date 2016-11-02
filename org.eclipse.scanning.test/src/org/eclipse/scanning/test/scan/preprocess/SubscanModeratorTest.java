package org.eclipse.scanning.test.scan.preprocess;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.junit.BeforeClass;
import org.junit.Test;

public class SubscanModeratorTest {
	
	protected static IPointGeneratorService  gservice;

	@BeforeClass
	public static void setServices() throws Exception {
		gservice    = new PointGeneratorFactory();
	}
	
	@Test
	public void testSimpleWrappedScan() throws Exception {
		
		CompoundModel cmodel = new CompoundModel<>();
		
		GridModel gmodel = new GridModel("x", "y");
		gmodel.setSlowAxisPoints(5);
		gmodel.setFastAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));	
		
		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), gmodel));
		
		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);
		
		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue("axesToMove", new String[]{"x", "y"});
		
		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		IPointGenerator<?> moderated = (IPointGenerator<?>)moderator.getPositionIterable();
		
		assertTrue("The moderated size should be 6 not "+moderated.size(), moderated.size()==6);
	}
}
