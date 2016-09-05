package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.ISpringParser;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.server.application.PseudoSpringParser;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Test;

public class SerializationTest {

	private IMarshallerService service;

	@Before
	public void create() throws Exception {
		// Non-OSGi for test - do not copy!
		service = new MarshallerService(new PointsModelMarshaller());
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
		
        String json = service.marshal(sent);
        
        ScanBean ret = service.unmarshal(json, ScanBean.class);
        
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

        String json = service.marshal(sent);
      
        ScanBean ret = (ScanBean)service.unmarshal(json, Object.class);
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
        String json = service.marshal(sent);
      
        ScanBean ret = (ScanBean)service.unmarshal(json, Object.class);
        if (!ret.equals(sent)) throw new Exception("Cannot deserialize "+ScanBean.class.getName());

	}

	@Test
	public void testStepSerialize() throws Exception {
		ScanBean bean = createStepScan();
        String   json = service.marshal(bean);
        ScanBean naeb = service.unmarshal(json, null);
        assertEquals(bean, naeb);
	}
	
	@Test
	public void testGridSerialize() throws Exception {
		ScanBean bean = createGridScanWithRegion();
        String   json = service.marshal(bean);
        ScanBean naeb = service.unmarshal(json, null);
        assertEquals(bean, naeb);
	}
	
	@Test
	public void testSerializeBasicPosition1() throws Exception {
		// Create a simple bounding rectangle
		IPosition pos = new MapPosition("x", 0, 1.0);
		String   json = service.marshal(pos);
		IPosition sop = service.unmarshal(json, IPosition.class);
		assertEquals(pos, sop);
	}
	
	@Test
	public void testSerializeBasicPosition2() throws Exception {
		// Create a simple bounding rectangle
		IPosition pos = new MapPosition("Fred:1:0");
		pos.setStepIndex(100);
		String   json = service.marshal(pos);
		IPosition sop = service.unmarshal(json, IPosition.class);
		assertEquals(pos, sop);
	}
	
	@Test
	public void testSerializePositionWithIndices() throws Exception {
		// Create a simple bounding rectangle
		Point point = new Point("x", 100, 0.02, "y", 150, 0.03); 
		point.setStepIndex(100);
		String   json = service.marshal(point);
		IPosition tniop = service.unmarshal(json, IPosition.class);
		assertEquals(point, tniop);
	}


	@Test
	public void testSerializeRegion() throws Exception {
		// Create a simple bounding rectangle
		IROI roi = new RectangularROI(0, 0, 3, 3, 0);
		String   json = service.marshal(roi);
		IROI ior  = service.unmarshal(json, IROI.class);
		assertEquals(roi, ior);
	}

	
	
	private ScanBean createStepScan() throws IOException {
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
		final ScanRequest<?> req = new ScanRequest<IROI>();
		req.setCompoundModel(new CompoundModel(new StepModel("fred", 0, 9, 1)));
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

		IROI roi = new RectangularROI(0, 0, 3, 3, 0);
		req.setCompoundModel(new CompoundModel(gmodel, roi));
		req.setMonitorNames("monitor");
		
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

	@Test
	public void testCompoundModel1() throws Exception {

		double[] spt = {1, 2};
		double[] ept = {3, 4};
		
		CompoundModel model = new CompoundModel();
		model.setData(new SpiralModel("x", "y", 1, new BoundingBox(0, -5, 10, 5)), new LinearROI(spt, ept));
		
		String   json = service.marshal(model, true); // TODO Should work with false here but does not, see below.

		System.out.println(json);
		// TODO This json uses the @bundle_and_class
		// It is required to have a type: field in the model refering to the simple name of the class
		// and replacing @bundle_and_class which is a java specific thing.
		CompoundModel ledom = service.unmarshal(json, CompoundModel.class);
		
		assertEquals(model, ledom);
	}
	

	@Test
	public void testCompoundModel2() throws Exception {

		CompoundModel model = new CompoundModel();
		
		model.setModelsVarArgs(new StepModel("T", 290, 300, 1), new SpiralModel("x", "y", 1, new BoundingBox(0, -5, 10, 5)), new GridModel("fast", "slow"));
		model.setRegionsVarArgs(new ScanRegion(new CircularROI(2, 0, 0), "x", "y"), new ScanRegion(new RectangularROI(1,2,0), "fast", "slow"));
		
		String   json = service.marshal(model, true); // TODO Should work with false here but does not, see below.

		// TODO This json uses the @bundle_and_class
		// It is required to have a type: field in the model refering to the simple name of the class
		// and replacing @bundle_and_class which is a java specific thing.
		CompoundModel ledom = service.unmarshal(json, CompoundModel.class);
		
		assertEquals(model, ledom);
	}

	@Test
	public void createScanCompoundModel() throws Exception {
		
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
		final ScanRequest<IROI> req = new ScanRequest<IROI>();
		// Create a grid scan model
		CompoundModel model = new CompoundModel();
		model.setModelsVarArgs(new StepModel("T", 290, 300, 1), new SpiralModel("x", "y", 1, new BoundingBox(0, -5, 10, 5)), new GridModel("fast", "slow"));
		model.setRegionsVarArgs(new ScanRegion(new CircularROI(2, 0, 0), "x", "y"), new ScanRegion(new RectangularROI(1,2,0), "fast", "slow"));
		req.setCompoundModel(model);
		
		final File tmp = File.createTempFile("scan_servlet_test", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.
		
		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setRealAxisName("xNex");
		mandyModel.setImaginaryAxisName("yNex");
		req.putDetector("mandelbrot", mandyModel);
		
		bean.setScanRequest(req);
		
        String   json = service.marshal(bean, true);
        ScanBean naeb = service.unmarshal(json, null);
        assertEquals(bean, naeb);
	}

	
	@Test
	public void testControlFactorySerialize() throws Exception {
		
		ISpringParser parser = new PseudoSpringParser();
		InputStream in = getClass().getResourceAsStream("client-test.xml");
		parser.parse(in);
		
		assertTrue(!ControlTree.getInstance().isEmpty());
		
		ControlTree.getInstance().build();
		
		String json = service.marshal(ControlTree.getInstance());
		
		assertTrue(json!=null);
		
		ControlTree factory = service.unmarshal(json, ControlTree.class);
		
		assertEquals(factory, ControlTree.getInstance());
	}

	@Test
	public void testControlFactoryToPosition() throws Exception {
		
		ISpringParser parser = new PseudoSpringParser();
		InputStream in = getClass().getResourceAsStream("client-test.xml");
		parser.parse(in);
		
		ControlTree tree = ControlTree.getInstance();
		assertTrue(!tree.isEmpty());
		
        Iterator<INamedNode> it = tree.iterator();
        while (it.hasNext()) {
			INamedNode iNamedNode = (INamedNode) it.next();
			if (iNamedNode instanceof ControlNode) {
				((ControlNode)iNamedNode).setValue(Math.random());
			}
		}

        IPosition pos = tree.toPosition();
        assertTrue(pos.size()>0);
        it = tree.iterator();
        while (it.hasNext()) {
			INamedNode iNamedNode = (INamedNode) it.next();
			if (iNamedNode instanceof ControlNode) {
				Object value = ((ControlNode)iNamedNode).getValue();
				assertEquals(value, pos.get(iNamedNode.getName()));
			}
        }
	}

}
