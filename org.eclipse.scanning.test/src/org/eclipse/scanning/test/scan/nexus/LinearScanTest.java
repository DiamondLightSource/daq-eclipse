package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.remotedataset.test.mock.LoaderServiceMock;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class LinearScanTest extends BrokerTest{
	
	protected IRunnableDeviceService      dservice;
	protected IScannableDeviceService     connector;
	protected IPointGeneratorService      gservice;
	protected IEventService               eservice;
	protected ILoaderService              lservice;
	
	private IPublisher<ScanBean>          publisher;
	private ISubscriber<EventListener>    subscriber;
	private File tmp;
	
	@Before
	public void setup() throws Exception {
		
		this.lservice = new LoaderServiceMock();
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		connector = new MockScannableConnector();
		dservice  = new RunnableDeviceServiceImpl(connector);
		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)dservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
		impl._register(MandelbrotModel.class, MandelbrotDetector.class);

		gservice  = new PointGeneratorFactory();

		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice  = new EventServiceImpl(new ActivemqConnectorService());
		
		// TODO Perhaps put service setting in super class or utility
		Services.setEventService(eservice);
		Services.setRunnableDeviceService(dservice);
		Services.setGeneratorService(gservice);
		Services.setConnector(connector);
		org.eclipse.dawnsci.nexus.ServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		org.eclipse.scanning.sequencer.ServiceHolder.setTestServices(new LoaderServiceMock(), new DefaultNexusBuilderFactory(), null);
		
		this.publisher = eservice.createPublisher(uri, IEventService.STATUS_TOPIC);
		this.subscriber = eservice.createSubscriber(uri, IEventService.STATUS_TOPIC);

		tmp = File.createTempFile("testAScan_", ".nxs");
		tmp.deleteOnExit();

	}
	
	@After
	public void clean() throws Exception {
        this.publisher.disconnect();
        this.subscriber.disconnect();
        tmp.delete();
	}

	@Test
	public void testSimpleLineScan() throws Exception {
			
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});
		doScan(roi, 1, new int[]{10,64,64}, create1DModel(10));
		
	}
	
	@Ignore("This requires Point indexing to be refactored")
	@Test
	public void testWrappedLineScan() throws Exception {
			
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});
		doScan(roi, 2, new int[]{4,10,64,64}, new StepModel("T", 290, 300, 3), create1DModel(10));
		
	}

	@Ignore("This requires Point indexing to be refactored")
	@Test
	public void testBigWrappedLineScan() throws Exception {
			
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});
		doScan(roi, 5, new int[]{2,2,2,2,3,64,64}, new StepModel("T1", 290, 291, 1), 
				                                   new StepModel("T2", 290, 291, 1), 
                                                   new StepModel("T3", 290, 291, 1), 
                                                   new StepModel("T4", 290, 291, 1), 
                                                    create1DModel(3));
		
	}

	private IScanPathModel create1DModel(int size) {

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(size);
        model.setxName("xNex");
        model.setyName("yNex");
        return model;
	}

	@Test
	public void testSimpleGridScan() throws Exception {
			
		doScan(null, 2, new int[]{5,5,64,64}, createGridModel());
	}
	
	@Test
	public void testWrappedGridScan() throws Exception {
			
//		doScan(null, 3, new int[]{4,5,5,64,64}, new StepModel("T", 290, 300, 3), createGridModel());
		doScan(null, 3, new int[]{4,5,5,64,64}, new StepModel("T", 290, 300, 3),
												createGridLine("xNex"),
												createGridLine("yNex"));
	}
	
	@Test
	public void testBigWrappedGridScan() throws Exception {
			
//		doScan(null,6, new int[]{2,2,2,2,2,2,64,64}, new StepModel("T", 290, 291, 1), 
//										             new StepModel("T", 290, 291, 1), 
//										             new StepModel("T", 290, 291, 1), 
//										             new StepModel("T", 290, 291, 1), 
//										             createGridModel(2,2));
		doScan(null, 6, new int[]{2,2,2,2,2,2,64,64}, new StepModel("T1", 290, 291, 1), 
										             new StepModel("T2", 290, 291, 1), 
										             new StepModel("T3", 290, 291, 1), 
										             new StepModel("T4", 290, 291, 1),
													 createGridLine("xNex", 2),
													 createGridLine("yNex", 2));
	}
	
	
	private StepModel createGridLine(String name, int... size) {
		// TODO: Remove this and use createGrid once Compound refactored to extract and build generator
		
		if (size.length == 0) {
			size = new int[] {5};
		}
		if (size.length > 1) {
			throw new IllegalArgumentException("One or now values must be provided for size");
		}
		double stepSize = 3.0/size[0];
		
		return new StepModel(name, stepSize/2, 3.0 - stepSize/2, stepSize);
		
	}


	private GridModel createGridModel(int... size) {
		
		if (size==null)    size = new int[]{5,5};
		if (size.length<2) size = new int[]{5,5};
		if (size.length>2) throw new IllegalArgumentException("Two values or no values should be provided!");
		
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);
	
		GridModel model = new GridModel();
		model.setSlowAxisPoints(size[0]);
		model.setFastAxisPoints(size[1]);
		model.setBoundingBox(box);
		model.setFastAxisName("xNex");
		model.setSlowAxisName("yNex");
		return model;
	}
	
	private void doScan(LinearROI roi, int scanRank, int[]dshape, IScanPathModel... models) throws Exception {
		
		IRunnableDevice<ScanModel> scanner = createTestScanner(roi, models);
		
		final List<IPosition> positions = new ArrayList<>();
		subscriber.addListener(new IScanListener() {
			public void scanEventPerformed(ScanEvent evt) {
				final IPosition pos = evt.getBean().getPosition();
				positions.add(pos);
			}
		});

		scanner.run(null);

		Thread.sleep(100);
		int size = ((IPointGenerator)scanner.getModel().getPositionIterable()).size();
		assertEquals("The model size was "+size+" and the points found were "+positions.size(), size, positions.size());
		
		for (IPosition iPosition : positions) {
			assertEquals(scanRank, iPosition.getScanRank());
		}
		
		final IDataHolder holder = lservice.getData(scanner.getModel().getFilePath(), null);
		final ILazyDataset mdata = holder.getLazyDataset("/entry/instrument/detector/data");
		assertTrue(mdata!=null);
		assertArrayEquals(dshape, mdata.getShape());
	}

	private IRunnableDevice<ScanModel> createTestScanner(IROI roi,  IScanPathModel... models) throws Exception {
		
		// Configure a detector with a collection time.
		MandelbrotModel dmodel = new MandelbrotModel();
		dmodel.setExposureTime(0.01);
		dmodel.setName("detector");
		dmodel.setColumns(64);
		dmodel.setRows(64);
		dmodel.setRealAxisName("xNex");
		dmodel.setImaginaryAxisName("yNex");
		
		IRunnableDevice<MandelbrotModel>	detector = dservice.createRunnableDevice(dmodel);
		
		// Generate the last model using the roi then work back up creating compounds
		final IPointGenerator<?>[] gens = new IPointGenerator[models.length];
		for (int i = 0; i < models.length; i++)  {
			if (i==models.length-1) { // Last one uses roi
				gens[i] = roi!=null ? gservice.createGenerator(models[i], roi) : gservice.createGenerator(models[i]);
			} else {
				gens[i] = gservice.createGenerator(models[i]);
			}
		}

		IPointGenerator<?> gen = gservice.createCompoundGenerator(gens);		
		
		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		
		smodel.setFilePath(tmp.getAbsolutePath());
				
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, publisher);

		return scanner;
	}


}
