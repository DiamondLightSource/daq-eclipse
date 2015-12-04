package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class LissajousTest {

	// TODO FIXME Test Lissajous Numerically

	private IGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}

	@Test
	public void testLissajousNoROI() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxStart(0);
		box.setyStart(0);
		box.setWidth(3);
		box.setHeight(3);

		LissajousModel model = new LissajousModel();
		model.setBoundingBox(box);

		// Get the point list
		IGenerator<LissajousModel, Point> gen = service.createGenerator(model);
		List<Point> pointList = gen.createPoints();

		assertEquals(pointList.size(), gen.size());
		assertEquals(1257, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	public void testLissajous() throws Exception {

		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Get the point list
		IGenerator<LissajousModel, Point> gen = service.createGenerator(new LissajousModel(), roi);
		List<Point> pointList = gen.createPoints();

		assertEquals(pointList.size(), gen.size());
		assertEquals(1257, gen.size());
		GeneratorUtil.testGeneratorPoints(gen);
	}

}
