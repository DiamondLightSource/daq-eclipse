package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.UUID;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class SerializationTest {

	private ActivemqConnectorService connectorService;

	@Before
	public void create() throws Exception {
		// Non-OSGi for test - do not copy!
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
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
	public void testScanBeanPosition12() throws Exception {
		// ScanBean [deviceName=detector, beamline=null, point=12, size=25, position=[Point: stepIndex=12, yNex(2)=1.5, xNex(2)=1.5], deviceState=RUNNING, previousDeviceState=RUNNING, filePath=null, scanNumber=0, datasetPath=null StatusBean [previousStatus=RUNNING, status=RUNNING, name=null, message=null, percentComplete=52.0, userName=null, hostName=DIAMRL5294, runDirectory=null, submissionTime=0, properties=null, id=1763047e-2f22-4ca1-a5a9-d15b4041578f]]
		// ScanBean [deviceName=detector, beamline=null, point=12, size=25, position=[Point: stepIndex=12, yNex(2)=1.5, xNex(2)=1.5], deviceState=RUNNING, previousDeviceState=RUNNING, filePath=null, scanNumber=0, datasetPath=null StatusBean [previousStatus=RUNNING, status=RUNNING, name=null, message=null, percentComplete=52.0, userName=null, hostName=DIAMRL5294, runDirectory=null, submissionTime=0, properties=null, id=73a05eeb-f8cc-4a78-8819-2a33a0ae1bd7]]
		final ScanBean sent = new ScanBean();
		sent.setDeviceName("detector");
		sent.setPoint(12);
		sent.setSize(25);
		Point pnt = new Point("xNex", 2, 1.5, "yNex", 2, 1.5);
		pnt.setStepIndex(12);
		sent.setPosition(pnt);
		sent.setDeviceState(DeviceState.RUNNING);
		sent.setPreviousDeviceState(DeviceState.RUNNING);
		sent.setUniqueId(UUID.randomUUID().toString());
		sent.setPreviousStatus(Status.RUNNING);
		sent.setStatus(Status.RUNNING);
		sent.setPercentComplete(52);
		sent.setHostName(InetAddress.getLocalHost().getHostName());

        String json = connectorService.marshal(sent);
      
        ScanBean ret = (ScanBean)connectorService.unmarshal(json, Object.class);
        if (!ret.equals(sent)) throw new Exception("Cannot deserialize "+ScanBean.class.getName());
        if (!ret.getPosition().equals(sent.getPosition())) throw new Exception("Cannot deserialize "+ScanBean.class.getName());

	}
	
	@Test
	public void testScanBeanSerializationWithJava() throws Exception {

		ScanBean sent = new ScanBean();
  	    
		// We read a scan bean from serialization file then parse to json
		try(InputStream file = getClass().getResourceAsStream("scanbean.ser");
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer); ){
			
			//deserialize the bean
			sent = (ScanBean)input.readObject();
		} 

        // Check that this bean goes to and from json
        String json = connectorService.marshal(sent);
      
        ScanBean ret = (ScanBean)connectorService.unmarshal(json, Object.class);
        if (!ret.equals(sent)) throw new Exception("Cannot deserialize "+ScanBean.class.getName());

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
	public void testSerializeBasicPosition1() throws Exception {
		// Create a simple bounding rectangle
		IPosition pos = new MapPosition("x", 0, 1.0);
		String   json = connectorService.marshal(pos);
		IPosition sop = connectorService.unmarshal(json, IPosition.class);
		assertEquals(pos, sop);
	}
	
	@Test
	public void testSerializeBasicPosition2() throws Exception {
		// Create a simple bounding rectangle
		IPosition pos = new MapPosition("Fred:1:0");
		pos.setStepIndex(100);
		String   json = connectorService.marshal(pos);
		IPosition sop = connectorService.unmarshal(json, IPosition.class);
		assertEquals(pos, sop);
	}
	
	@Test
	public void testSerializePositionWithIndices() throws Exception {
		// Create a simple bounding rectangle
		Point point = new Point("x", 100, 0.02, "y", 150, 0.03); 
		point.setStepIndex(100);
		String   json = connectorService.marshal(point);
		IPosition tniop = connectorService.unmarshal(json, IPosition.class);
		assertEquals(point, tniop);
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
		req.setMetadataScannableNames("metadata");

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
		mandyModel.setRealAxisName("xNex");
		mandyModel.setImaginaryAxisName("yNex");
		req.putDetector("mandelbrot", mandyModel);
		
		bean.setScanRequest(req);
		return bean;
	}

}
