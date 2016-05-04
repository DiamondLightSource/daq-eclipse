package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LinearTest {

	// TODO FIXME Test Linear Numerically
	
	private IPointGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new PointGeneratorFactory();
	}
	
	@Test
	public void testOneDEqualSpacing() throws Exception {
		
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(10);
		
		// Get the point list
		IPointGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model, roi);
		List<IPosition> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		assertEquals(pointList.size(), 10);
        GeneratorUtil.testGeneratorPoints(gen);
	}
	
	@Ignore("2016-02-29, waiting for better OneDEqualSpacingGenerator implementation")
	@Test
	public void testOneDEqualSpacingNoROI() throws GeneratorException {
		
		OneDEqualSpacingModel model = new OneDEqualSpacingModel();
		model.setPoints(10);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model);
		gen.createPoints();
	}

	@Test(expected = PointsValidationException.class)
	public void testOneDEqualSpacingNoPoints() throws Exception {
		
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(0);
		
		// Get the point list
		IPointGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model, roi);
		List<IPosition> pointList = gen.createPoints();
        GeneratorUtil.testGeneratorPoints(gen);
	}

	
	@Test
	public void testOneDStep() throws Exception {
		
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDStepModel model = new OneDStepModel();
        model.setStep(0.3);
		
		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model, roi);
		List<IPosition> pointList = gen.createPoints();
		
		assertEquals(pointList.size(), gen.size());
		assertEquals(15, pointList.size());
        GeneratorUtil.testGeneratorPoints(gen);
	}

	@Ignore("2016-02-29, waiting for better OneDStepGenerator implementation")
	@Test
	public void testOneDStepNoROI() throws GeneratorException {

		OneDStepModel model = new OneDStepModel();
		model.setStep(1);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model);
		gen.createPoints();
	}

	@Test(expected = PointsValidationException.class)
	public void testOneDStepNoStep() throws Exception {
		
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDStepModel model = new OneDStepModel();
        model.setStep(0);
		
		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model, roi);
		List<IPosition> pointList = gen.createPoints();
        GeneratorUtil.testGeneratorPoints(gen);
		
	}
	
	@Test(expected = PointsValidationException.class)
	public void testOneDStepNegativeStep() throws Exception {

		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

		OneDStepModel model = new OneDStepModel();
		model.setStep(-0.3);

		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model, roi);
		List<IPosition> pointList = gen.createPoints();
		GeneratorUtil.testGeneratorPoints(gen);

	}

	@Test(expected = GeneratorException.class)
	public void testOneDStepWrongROI() throws Exception {
		
		RectangularROI roi = new RectangularROI(new double[]{0,0}, new double[]{3,3});

        OneDStepModel model = new OneDStepModel();
        model.setStep(0);
		
		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model, roi);
		List<IPosition> pointList = gen.createPoints();
        GeneratorUtil.testGeneratorPoints(gen);
		
	}


}
