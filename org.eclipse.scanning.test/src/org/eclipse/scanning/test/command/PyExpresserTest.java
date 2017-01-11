package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.command.ParserServiceImpl;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;
import org.eclipse.scanning.command.factory.PyExpressionFactory;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;


public class PyExpresserTest {
	
	private PyExpressionFactory factory;

	@Before
	public void services() {
		ParserServiceImpl.setPointGeneratorService(new PointGeneratorService());
		this.factory = new PyExpressionFactory();
	}

	@Test
	public void testScanRequestWithMonitor()
			throws Exception {

		StepModel smodel = new StepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");


		Collection<String> monitors = new ArrayList<>();
		monitors.add("someMonitor");

		ScanRequest<IROI> request = new ScanRequest<>();
		request.setCompoundModel(new CompoundModel(smodel));
		request.setMonitorNames(monitors);

		assertEquals(  // Concise.
				"mscan(step('fred', 0.0, 10.0, 1.0), 'someMonitor')",
				factory.pyExpress(request, false));
		assertEquals(  // Verbose.
				"mscan(path=[step(axis='fred', start=0.0, stop=10.0, step=1.0)], mon=['someMonitor'])",
				factory.pyExpress(request, true));
	}

	@Test
	public void testScanRequestWithROI()
			throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setFastAxisStart(0);
		bbox.setSlowAxisStart(1);
		bbox.setFastAxisLength(10);
		bbox.setSlowAxisLength(11);

		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("myFast");
		gmodel.setSlowAxisName("mySlow");
		gmodel.setBoundingBox(bbox);
		gmodel.setFastAxisPoints(3);
		gmodel.setSlowAxisPoints(4);

		CircularROI croi = new CircularROI();
		ScanRequest<IROI> request = new ScanRequest<>();

		CompoundModel cmodel = new CompoundModel();
		cmodel.setData(gmodel, croi);
		request.setCompoundModel(cmodel);

		assertEquals(  // Concise.
				"mscan(grid(('myFast', 'mySlow'), (0.0, 1.0), (10.0, 12.0), count=(3, 4), snake=False, roi=circ((0.0, 0.0), 1.0)))",
				factory.pyExpress(request, false));
		assertEquals(  // Verbose.
				"mscan(path=[grid(axes=('myFast', 'mySlow'), start=(0.0, 1.0), stop=(10.0, 12.0), count=(3, 4), snake=False, roi=[circ(origin=(0.0, 0.0), radius=1.0)])])",
				factory.pyExpress(request, true));
	}

	@Test
	public void testCompoundScanRequest()
			throws Exception {

		StepModel smodel = new StepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");

		ArrayModel amodel = new ArrayModel();
		amodel.setName("fred");
		amodel.setPositions(0.1);

		ScanRequest<IROI> request = new ScanRequest<>();
		request.setCompoundModel(new CompoundModel(smodel, amodel));

		assertEquals(  // Concise.
				"mscan([step('fred', 0.0, 10.0, 1.0), val('fred', 0.1)])",
				factory.pyExpress(request, false));
		assertEquals(  // Verbose.
				"mscan(path=[step(axis='fred', start=0.0, stop=10.0, step=1.0), array(axis='fred', values=[0.1])])",
				factory.pyExpress(request, true));
	}

	@Test
	public void testStepModel() throws Exception{

		StepModel smodel = new StepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");

		assertEquals(  // Concise.
				"step('fred', 0.0, 10.0, 1.0)",
				factory.pyExpress(smodel, false));
		assertEquals(  // Verbose.
				"step(axis='fred', start=0.0, stop=10.0, step=1.0)",
				factory.pyExpress(smodel, true));
	}

	@Test
	public void testGridModel()
			throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setFastAxisStart(0);
		bbox.setSlowAxisStart(1);
		bbox.setFastAxisLength(10);
		bbox.setSlowAxisLength(11);

		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("myFast");
		gmodel.setSlowAxisName("mySlow");
		gmodel.setBoundingBox(bbox);
		gmodel.setFastAxisPoints(3);
		gmodel.setSlowAxisPoints(4);

		assertEquals(  // Concise.
				"grid(('myFast', 'mySlow'), (0.0, 1.0), (10.0, 12.0), count=(3, 4), snake=False)",
				factory.pyExpress(gmodel, new ArrayList<>(), false));
		assertEquals(  // Verbose.
				"grid(axes=('myFast', 'mySlow'), start=(0.0, 1.0), stop=(10.0, 12.0), count=(3, 4), snake=False)",
				factory.pyExpress(gmodel, new ArrayList<>(), true));
	}

	@Test
	public void testRasterModel()
			throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setFastAxisStart(0);
		bbox.setSlowAxisStart(1);
		bbox.setFastAxisLength(10);
		bbox.setSlowAxisLength(11);

		RasterModel rmodel = new RasterModel();
		rmodel.setFastAxisName("myFast");
		rmodel.setSlowAxisName("mySlow");
		rmodel.setBoundingBox(bbox);
		rmodel.setFastAxisStep(3);
		rmodel.setSlowAxisStep(4);
		rmodel.setSnake(true);

		assertEquals(  // Concise.
				"grid(('myFast', 'mySlow'), (0.0, 1.0), (10.0, 12.0), (3.0, 4.0))",
				factory.pyExpress(rmodel, null, false));
		assertEquals(  // Verbose.
				"grid(axes=('myFast', 'mySlow'), start=(0.0, 1.0), stop=(10.0, 12.0), step=(3.0, 4.0), snake=True)",
				factory.pyExpress(rmodel, null, true));
	}

	@Test
	public void testArrayModel() throws Exception {

		ArrayModel amodel = new ArrayModel();
		amodel.setName("fred");
		amodel.setPositions(0.1);

		assertEquals(  // Concise.
				"val('fred', 0.1)",
				factory.pyExpress(amodel, false));
		assertEquals(  // Verbose.
				"array(axis='fred', values=[0.1])",
				factory.pyExpress(amodel, true));

		amodel.setPositions(0.1, 0.2);
		assertEquals(  // Concise but with n>1 array values.
				"array('fred', [0.1, 0.2])",
				factory.pyExpress(amodel, false));
	}

}
