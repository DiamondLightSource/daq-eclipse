package org.eclipse.scanning.test.scan.preprocess;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.malcolm.models.MalcolmConnectionInfo;
import org.eclipse.scanning.api.malcolm.models.MalcolmDetectorModelWithMap;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.preprocess.ExamplePreprocessor;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Test;

public class PreprocessTest {

	protected IPreprocessor preprocessor;

	@Before
	public void before() {
		preprocessor = new ExamplePreprocessor();
	}

	@Test
	public void testSimplePreprocess() throws Exception {
		
		ScanRequest<?> req = createStepRequest();
		req = preprocessor.preprocess(req);
		assertNotNull(req);

		StepModel step = (StepModel)req.getModels().toArray()[0];
		assertTrue(step.getName().equals("xfred"));
	}
	
	@Test
	public void testGridPreprocess() throws Exception {
		
		ScanRequest<?> req = createGridRequest();
		req = preprocessor.preprocess(req);
		assertNotNull(req);

		GridModel grid = (GridModel)req.getModels().toArray()[0];
		assertTrue(grid.getxName().equals("xfred"));
		assertTrue(grid.getyName().equals("yfred"));
	}
	
	@Test
	public void testGridStepPreprocess() throws Exception {
		
		ScanRequest<?> req = createStepGridRequest(5);
		req = preprocessor.preprocess(req);
		assertNotNull(req);

		// TODO 
	}

	@Test
	public void testMalcolmPreprocess() throws Exception {
		
		ScanRequest<?> req = createMalcolmRequest();
		req = preprocessor.preprocess(req);
		assertNotNull(req);

		// TODO 
	}
	
	private ScanRequest<?> createStepRequest() throws IOException {
		
		final ScanRequest<?> req = new ScanRequest<IROI>();
		req.setModels(new StepModel("fred", 0, 9, 1));
		req.setMonitorNames("monitor");

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.1);
		req.putDetector("detector", dmodel);

		return req;
	}

	private ScanRequest<?> createStepGridRequest(int outerScanNum) throws IOException {
		
		
		final ScanRequest<?> req = new ScanRequest<IROI>();
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setxStart(0);
		box.setyStart(0);
		box.setWidth(3);
		box.setHeight(3);

		GridModel gmodel = new GridModel();
		gmodel.setRows(5);
		gmodel.setColumns(5);
		gmodel.setBoundingBox(box);
		gmodel.setxName("xNex");
		gmodel.setyName("yNex");

		// 2 models
		List<IScanPathModel> models = new ArrayList<>(outerScanNum+1);
		for (int i = 0; i < outerScanNum; i++) {
			models.add(new StepModel("neXusScannable"+i, 1, 2, 1));
		}
		models.add(gmodel);
		req.setModels(models.toArray(new IScanPathModel[models.size()]));
		req.setMonitorNames("monitor");
		
		final File tmp = File.createTempFile("scan_servlet_test", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.
		
		// 2 detectors
		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setxName("xNex");
		mandyModel.setyName("yNex");
		mandyModel.setExposureTime(0.01);
		req.putDetector("mandelbrot", mandyModel);
		
		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.01);
		req.putDetector("detector", dmodel);

		return req;
	}

	private ScanRequest<?> createGridRequest() throws IOException {
		
		final ScanRequest<?> req = new ScanRequest<IROI>();
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setxStart(0);
		box.setyStart(0);
		box.setWidth(3);
		box.setHeight(3);

		GridModel gmodel = new GridModel();
		gmodel.setRows(5);
		gmodel.setColumns(5);
		gmodel.setBoundingBox(box);
		gmodel.setxName("xNex");
		gmodel.setyName("yNex");

		req.setModels(gmodel);
		req.setMonitorNames("monitor");
		
		final File tmp = File.createTempFile("scan_servlet_test", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.
		
		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setxName("xNex");
		mandyModel.setyName("yNex");
		req.putDetector("mandelbrot", mandyModel);
		
		return req;
	}

	private ScanRequest<?> createMalcolmRequest() throws Exception {
		
		final ScanRequest<?> req = new ScanRequest<IROI>();
		req.setModels(new StepModel("temperature", 0, 9, 1));
		req.setMonitorNames("monitor");
		
		final File tmp = File.createTempFile("scan_servlet_test_malc", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.

		final MalcolmDetectorModelWithMap malcModel = new MalcolmDetectorModelWithMap();
		// Test params for starting the device
		fillParameters(malcModel.getParameterMap(), -1, 10);

		final MalcolmConnectionInfo connectionInfo = new MalcolmConnectionInfo();
		connectionInfo.setDeviceName("zebra");
		connectionInfo.setHostName("pausable");
		connectionInfo.setPort(-1);

		malcModel.setConnectionInfo(connectionInfo);

		req.putDetector("zebra", malcModel);

		return req;
	}
	
	private void fillParameters(Map<String, Object> config, long configureSleep, int imageCount) throws Exception {
		
		// Params for driving mock mode
		config.put("nframes", imageCount); // IMAGE_COUNT images to write
		config.put("shape", new int[]{1024,1024});
		
		final File temp = File.createTempFile("testingFile", ".hdf5");
		temp.deleteOnExit();
		config.put("file", temp.getAbsolutePath());
		
		// The exposure is in seconds
		config.put("exposure", 0.5);
		
		double csleep = configureSleep/1000d;
		if (configureSleep>0) config.put("configureSleep", csleep); // Sleeps during configure

	}
}
