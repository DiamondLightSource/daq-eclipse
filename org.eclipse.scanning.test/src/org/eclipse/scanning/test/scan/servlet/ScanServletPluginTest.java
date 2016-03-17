package org.eclipse.scanning.test.scan.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.models.MalcolmRequest;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.malcolm.device.MockedMalcolmService;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScanServletPluginTest {

	private static ScanServlet servlet; 
	
	@BeforeClass
	public static void connect()  throws Exception {
		
		
		// Set up stuff because we are not the server
		doHardCodedTestThings();

		
		// Create an object for the servlet
		/**
		 *  This would be done by spring on the GDA Server
		 *  @see org.eclipse.scanning.server.servlet.AbstractConsumerServlet
		 *  In spring we have something like:

		    {@literal <bean id="scanner" class="org.eclipse.scanning.server.servlet.ScanServlet">}
		    {@literal    <property name="broker"      value="tcp://p45-control:61616" />}
		    {@literal    <property name="submitQueue" value="uk.ac.diamond.p45.submitQueue" />}
		    {@literal    <property name="statusSet"   value="uk.ac.diamond.p45.statusSet"   />}
		    {@literal    <property name="statusTopic" value="uk.ac.diamond.p45.statusTopic" />}
		    {@literal </bean>}

		 */
		servlet = new ScanServlet();
		servlet.setBroker("vm://localhost?broker.persistent=false");
		servlet.setSubmitQueue("org.eclipse.scanning.test.servlet.submitQueue");
		servlet.setStatusSet("org.eclipse.scanning.test.servlet.statusSet");
		servlet.setStatusTopic("org.eclipse.scanning.test.servlet.statusTopic");
		servlet.connect(); // Gets called by Spring automatically
		
	}

	@SuppressWarnings("rawtypes")
	@AfterClass
	public static void disconnect() throws EventException, InterruptedException {
		servlet.disconnect();
	}
	
	/**
	 * This test mimiks a client submitting a scan. The client may submit any status bean
	 * to the consumer of course and then  
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStepScan() throws Exception {
		
		ScanBean bean = createStepScan();
		runAndCheck(bean, 20);
	}
	
	/**
	 * This test mimiks a client submitting a scan. The client may submit any status bean
	 * to the consumer of course and then  
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStepScanProcessing() throws Exception {
		
		System.setProperty("org.eclipse.scanning.api.preprocessor.name", "example");
		try {
			ScanBean bean = createStepScan();
			List<ScanBean> beans = runAndCheck(bean, 20);
			// We now check that they all had xfred set.
			for (ScanBean scanBean : beans) {
				ScanRequest<?> req = scanBean.getScanRequest();
				
				StepModel step = (StepModel)req.getModels()[0];
				assertTrue(step.getName().equals("xfred"));
			}
		} finally {
		    System.setProperty("org.eclipse.scanning.api.preprocessor.name", "");
		}
		
	}


	@Test
	public void testGridScan() throws Exception {
		
		ScanBean bean = createGridScan();
		runAndCheck(bean, 20);

	}
	
	@Test
	public void testMalcScan() throws Exception {
		
		ScanBean bean = createMalcolmScan();
		runAndCheck(bean, 20);
		// TODO check nexus file written correctly, including unique keys
	}


	@Test
	public void testStepGridScanNested1() throws Exception {
		
		ScanBean bean = createStepGridScan(1);
		runAndCheck(bean, 100);
	}
	
	@Test
	public void testStepGridScanNested5() throws Exception {
		
		ScanBean bean = createStepGridScan(5);
		runAndCheck(bean, 500);
	}

	
	private ScanBean createStepScan() throws IOException {
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
		final ScanRequest<?> req = new ScanRequest<IROI>();
		req.setModels(new StepModel("fred", 0, 9, 1));
		req.setMonitorNames("monitor");

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setCollectionTime(0.1);
		req.putDetector("detector", dmodel);
		
		bean.setScanRequest(req);
		return bean;
	}

	private ScanBean createStepGridScan(int outerScanNum) throws IOException {
		
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
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
		mandyModel.setExposure(0.01);
		req.putDetector("mandelbrot", mandyModel);
		
		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setCollectionTime(0.01);
		req.putDetector("detector", dmodel);

		bean.setScanRequest(req);
		return bean;
	}

	private ScanBean createGridScan() throws IOException {
		
		
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
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
		
		bean.setScanRequest(req);
		return bean;
	}

	private ScanBean createMalcolmScan() throws Exception {
		
		
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
		final ScanRequest<?> req = new ScanRequest<IROI>();
		req.setModels(new StepModel("temperature", 0, 9, 1));
		req.setMonitorNames("monitor");
		
		final File tmp = File.createTempFile("scan_servlet_test_malc", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.
		
		final MalcolmRequest<Map<String, Object>> malcModel = new MalcolmRequest<Map<String, Object>>();
	    Map<String, Object> config = new HashMap<String,Object>(2);    
		// Test params for starting the device 		
	    fillParameters(config, -1, 10);
	    malcModel.setDeviceModel(config);
		malcModel.setDeviceName("zebra");
		malcModel.setHostName("standard");
		malcModel.setPort(-1);
		req.putDetector("zebra", malcModel);
		
		bean.setScanRequest(req);
		return bean;
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

	private List<ScanBean> runAndCheck(ScanBean bean, long maxScanTimeS) throws Exception {
		
		final IEventService eservice = Services.getEventService();

		// Let's listen to the scan and see if things happen when we run it
		final ISubscriber<IScanListener> subscriber = eservice.createSubscriber(new URI(servlet.getBroker()), servlet.getStatusTopic());
		final ISubmitter<ScanBean>       submitter  = eservice.createSubmitter(new URI(servlet.getBroker()),  servlet.getSubmitQueue());
		
		try {
			final List<ScanBean> beans = new ArrayList<>(13);
			final List<ScanBean> startEvents = new ArrayList<>(13);
			final List<ScanBean> endEvents   = new ArrayList<>(13);
			
			final CountDownLatch latch = new CountDownLatch(1);
			subscriber.addListener(new IScanListener() {
				@Override
				public void scanEventPerformed(ScanEvent evt) {
					System.out.println(evt.getBean());
					if (evt.getBean().getPosition()!=null) {
						beans.add(evt.getBean());
					}
				}
	
				@Override
				public void scanStateChanged(ScanEvent evt) {
					System.out.println("Device:"+evt.getBean().getPreviousDeviceState()+"->"+evt.getBean().getDeviceState());
					System.out.println("Status:"+evt.getBean().getPreviousStatus()+"->"+evt.getBean().getStatus());
					if (evt.getBean().scanStart()) {
						System.out.println("Scan started. Size is "+evt.getBean().getSize());
						startEvents.add(evt.getBean()); // Should be just one
					}
	                if (evt.getBean().scanEnd()) {
	                	endEvents.add(evt.getBean());
	                	try {
							Thread.sleep(100); // TODO Why does scanEnd() sometimes come over slightly before the device is done?
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                	latch.countDown();
	                }
				}
			});
	
			
			// Ok done that, now we sent it off...
			submitter.submit(bean);
			
			boolean ok = latch.await(maxScanTimeS, TimeUnit.SECONDS);
			if (!ok) throw new Exception("The latch broke before the scan finished!");
			
			assertEquals(startEvents.get(0).getSize(), beans.size());
			assertEquals(1, startEvents.size());
			assertEquals(1, endEvents.size());
			
			return beans;
			
		} finally {
			subscriber.disconnect();
			submitter.disconnect();
		}
	}
	
	private static void doHardCodedTestThings() throws Exception {
		// We will run this test without real GDA devices. Therefore we
		// override the connector
		// DO NOT COPY TESTING ONLY
		((DeviceServiceImpl)Services.getScanService()).setDeviceService(new MockScannableConnector()); 
		
		
		// Put a connection in the DeviceServiceImpl which is used for the test
		IMalcolmService malcolmService = new MockedMalcolmService();
		
		// Should create a standard MockedMalcolmDevice and not one of the more complex types.
		IMalcolmConnection connection   = malcolmService.createConnection(URI.create("tcp://standard"));
		((DeviceServiceImpl)Services.getScanService())._registerConnection(URI.create("tcp://standard"), connection);
		
		// DO NOT COPY TESTING ONLY
		Services.setConnector(new MockScannableConnector());
		
		// We double check that the services injected into the servlet bundle are there.
		assertNotNull(Services.getConnector());
		assertNotNull(Services.getEventService());
		assertNotNull(Services.getGeneratorService());
		assertNotNull(Services.getMalcService());
		assertNotNull(Services.getScanService());
	}

}
