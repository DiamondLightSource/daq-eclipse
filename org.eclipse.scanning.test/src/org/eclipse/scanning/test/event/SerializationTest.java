package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;
import uk.ac.diamond.json.JsonMarshaller;

public class SerializationTest {

	private ActivemqConnectorService connectorService;

	@Before
	public void create() throws Exception {
		// Non-OSGi for test - do not copy!
		ActivemqConnectorService.setJsonMarshaller(new JsonMarshaller());
		connectorService = new ActivemqConnectorService();
	}
	
	@Test
	public void testSerializeScanBean() throws Exception {
		
		final ScanBean sent = new ScanBean();
		sent.setDeviceName("fred");
		sent.setDeviceState(DeviceState.RUNNING);
		sent.setPreviousDeviceState(DeviceState.READY);
		sent.setPosition(new MapPosition("x", 0, 1.0));
		sent.setUniqueId(UUID.randomUUID().toString());
		sent.setHostName(InetAddress.getLocalHost().getHostName());
		
        String json = connectorService.marshal(sent);
        
        ScanBean ret = connectorService.unmarshal(json, ScanBean.class);
        
        if (!ret.equals(sent)) throw new Exception("Cannot deserialize "+ScanBean.class.getName());
        if (!ret.getPosition().equals(sent.getPosition())) throw new Exception("Cannot deserialize "+ScanBean.class.getName());
	}
	
	
	@Test
	public void testStepSerialize() throws Exception {
		ScanBean bean = createStepScan();
        String   json = connectorService.marshal(bean);
        ScanBean naeb = connectorService.unmarshal(json, null);
        assertEquals(bean, naeb);
	}
	
	@Test
	public void testGridSerialize() throws Exception {
		ScanBean bean = createGridScanWithRegion();
        String   json = connectorService.marshal(bean);
        ScanBean naeb = connectorService.unmarshal(json, null);
        assertEquals(bean, naeb);
	}
	
	@Test
	public void testSerializePosition() throws Exception {
		// Create a simple bounding rectangle
		IPosition roi = new MapPosition("Fred:1:0");
		String   json = connectorService.marshal(roi);
		IPosition ior = connectorService.unmarshal(json, IPosition.class);
		assertEquals(roi, ior);
	}

	@Test
	public void testSerializeRegion() throws Exception {
		// Create a simple bounding rectangle
		IROI roi = new RectangularROI(0, 0, 3, 3, 0);
		String   json = connectorService.marshal(roi);
		IROI ior  = connectorService.unmarshal(json, IROI.class);
		assertEquals(roi, ior);
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
		dmodel.setExposureTime(0.1);
		req.putDetector("detector", dmodel);
		
		bean.setScanRequest(req);
		return bean;
	}
	
	private ScanBean createGridScanWithRegion() throws IOException {
			
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
		final ScanRequest<IROI> req = new ScanRequest<IROI>();
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);

		GridModel gmodel = new GridModel();
		gmodel.setSlowAxisPoints(5);
		gmodel.setFastAxisPoints(5);
		gmodel.setBoundingBox(box);
		gmodel.setFastAxisName("xNex");
		gmodel.setSlowAxisName("yNex");

		req.setModels(gmodel);
		req.setMonitorNames("monitor");
		IROI roi = new RectangularROI(0, 0, 3, 3, 0);
		req.putRegion(gmodel.getUniqueKey(), roi);
		
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

}
