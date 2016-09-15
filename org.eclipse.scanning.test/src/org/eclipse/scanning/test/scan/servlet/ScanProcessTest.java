package org.eclipse.scanning.test.scan.servlet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.remotedataset.test.mock.LoaderServiceMock;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.ScriptResponse;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.server.servlet.ScanProcess;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanProcessTest {
	
	private static final class MockScriptService implements IScriptService {

		private List<ScriptRequest> scriptRequests = new ArrayList<>();
		
		@Override
		public ScriptLanguage[] supported() {
			return ScriptLanguage.values();
		}

		@Override
		public ScriptResponse<?> execute(ScriptRequest req)
				throws UnsupportedLanguageException, ScriptExecutionException {
			scriptRequests.add(req);
			return new ScriptResponse<>();
		}
		
		public List<ScriptRequest> getScriptRequests() {
			return scriptRequests;
		}

	}
	
	protected IRunnableDeviceService      dservice;
	protected IScannableDeviceService     connector;
	protected IPointGeneratorService      gservice;
	protected IEventService               eservice;
	protected ILoaderService              lservice;
	protected MockScriptService           sservice;

	@Before
	public void setUp() {
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice  = new EventServiceImpl(new ActivemqConnectorService());
		
		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE
		connector = new MockScannableConnector(null);
		dservice  = new RunnableDeviceServiceImpl(connector);
		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)dservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
		impl._register(MandelbrotModel.class, MandelbrotDetector.class);

		gservice  = new PointGeneratorFactory();
		
		lservice = new LoaderServiceMock();
		sservice = new MockScriptService();
		
		// Provide lots of services that OSGi would normally.
		Services.setEventService(eservice);
		Services.setRunnableDeviceService(dservice);
		Services.setGeneratorService(gservice);
		Services.setConnector(connector);
		ServiceHolder.setTestServices(lservice, new DefaultNexusBuilderFactory(), null);
		org.eclipse.dawnsci.nexus.ServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		Services.setScriptService(sservice);
	}
	
	@Test
	public void testScriptFilesRun() throws Exception {
		// Arrange
		ScanBean scanBean = new ScanBean();
		ScanRequest<?> scanRequest = new ScanRequest<>();
		scanRequest.setCompoundModel(new CompoundModel<>(new StepModel("fred", 0, 9, 1)));
		
		ScriptRequest before = new ScriptRequest();
		before.setFile("/path/to/before.py");
		before.setLanguage(ScriptLanguage.PYTHON);
		scanRequest.setBefore(before);
		
		ScriptRequest after = new ScriptRequest();
		after.setFile("/path/to/after.py");
		after.setLanguage(ScriptLanguage.PYTHON);
		scanRequest.setAfter(after);
		
		scanBean.setScanRequest(scanRequest);
		ScanProcess process = new ScanProcess(scanBean, null, true);
		
		// Act
		process.execute();

		// Assert
		List<ScriptRequest> scriptRequests = ((MockScriptService) sservice).getScriptRequests();
		assertThat(scriptRequests.size(), is(2));
		assertThat(scriptRequests, hasItems(before, after));
	}
	
	@Test
	public void testStartAndEndPos() throws Exception {
		// Arrange
		ScanBean scanBean = new ScanBean();
		ScanRequest<?> scanRequest = new ScanRequest<>();
		scanRequest.setCompoundModel(new CompoundModel<>(new StepModel("fred", 0, 9, 1)));
		
		final MapPosition start = new MapPosition();
		start.put("p", 1.0);
		start.put("q", 2.0);
		start.put("r", 3.0);
		scanRequest.setStart(start);
		
		final MapPosition end = new MapPosition();
		end.put("p", 6.0);
		end.put("q", 7.0);
		end.put("r", 8.0);
		scanRequest.setEnd(end);
		
		scanBean.setScanRequest(scanRequest);
		ScanProcess process = new ScanProcess(scanBean, null, true);
		
		// Act
		process.execute();
		
		// Assert
		for (String scannableName : start.getNames()) {
			final Number startPos = start.getValue(scannableName);
			final Number endPos = end.getValue(scannableName);
			
			IScannable<Number> scannable = connector.getScannable(scannableName);
			MockScannable mockScannable = (MockScannable) scannable;
			
			mockScannable.verify(start.getValue(scannableName), start);
			mockScannable.verify(end.getValue(scannableName), end);
			
			final List<Number> values = mockScannable.getValues();
			assertThat(values.get(0), is(equalTo(startPos)));
			assertThat(values.get(values.size() - 1), is(equalTo(endPos)));
		}
	}

}
