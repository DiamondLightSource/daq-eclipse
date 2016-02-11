package org.eclipse.scanning.test.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanServletPluginTest {

	private static IEventConnectorService marshaller;
	private static ScanServlet servlet; 
	
	@BeforeClass
	public static void connect()  throws Exception {
		
		marshaller = new ActivemqConnectorService();
		
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
		servlet.setBroker("tcp://sci-serv5.diamond.ac.uk:61616");
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

	@Test
	public void testGridScan() throws Exception {
		
		ScanBean bean = createGridScan();
		runAndCheck(bean, 20);

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

	private void runAndCheck(ScanBean bean, long maxScanTimeS) throws Exception {
		
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
