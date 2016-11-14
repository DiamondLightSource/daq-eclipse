package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;

public abstract class GeneratorTest {

	protected IPointGeneratorService service;

	@Before
	public void before() throws Exception {
	
		service = new PointGeneratorService();
	}

	
	public void checkWrtCompound(Object model, IROI roi, int size) throws Exception {
		
		// Get the point list
		IPointGenerator<?> generator = service.createGenerator(model, roi);
	    List<IPosition> pointList = generator.createPoints();

		assertEquals(size, pointList.size());

		CompoundModel<IROI> cmodel = new CompoundModel<>(model);
		cmodel.setRegions(Arrays.asList(new ScanRegion(roi, Arrays.asList("x", "y"))));
		
		IPointGenerator<?> cgenerator = service.createCompoundGenerator(cmodel); 
	    List<IPosition> cpointList = cgenerator.createPoints();
		assertEquals(size, cpointList.size());

	}

}
