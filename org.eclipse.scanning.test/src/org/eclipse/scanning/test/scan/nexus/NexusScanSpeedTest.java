package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * 
 * This class always runs the same nexus scan but puts in various parts of the 
 * scanning to see what effect they make.
 * 
 * @author Matthew Gerring
 *
 */
public class NexusScanSpeedTest extends NexusTest {

	private static EventServiceImpl eservice;
	private IPointGenerator<StepModel> gen;
	
	@BeforeClass
    public static void createEventService() throws Exception {
		eservice = new EventServiceImpl(new ActivemqConnectorService());
	}

	@Before
	public void before() throws GeneratorException, IOException {
		this.gen = gservice.createGenerator(new StepModel("xNex", 0, 1000, 1));
	}
	
	@Test
	public void testBareNexusStepScanSpeedNoNexus() throws Exception {
		
		// We create a step scan
		final IRunnableDevice<ScanModel> scan = dservice.createRunnableDevice(new ScanModel(gen));
		runAndCheck("No NeXus scan", scan, 5, 1);
	}
	
	
	@Test
	public void testBareNexusStepNoSetSlice() throws Exception {
		
		IScannable<?> scannable = connector.getScannable("xNex");
		MockNeXusScannable xNex = (MockNeXusScannable)scannable;
		try {
			xNex.setWritingOn(false);
			// We create a step scan
			final IRunnableDevice<ScanModel> scan = dservice.createRunnableDevice(new ScanModel(gen, output));
			runAndCheck("Scan no 'setSlice'", scan, 10, 2048);
		} finally {
			xNex.setWritingOn(true);
		}
	}

	@Test
	public void testBareNexusStepScanSpeed() throws Exception {
		
		// We create a step scan
		final IRunnableDevice<ScanModel> scan = dservice.createRunnableDevice(new ScanModel(gen, output));
		runAndCheck("Normal NeXus Scan", scan, 10, 2048);
	}
	
	@Test
	public void testPublishedNexusStepScanSpeed() throws Exception {
		
		// We create a step scan
		IPublisher<ScanBean> publisher = eservice.createPublisher(uri, EventConstants.SCAN_TOPIC);
		final IRunnableDevice<ScanModel> scan = dservice.createRunnableDevice(new ScanModel(gen, output), publisher);
		runAndCheck("NeXus with Publish", scan, 10, 2048);
	}

	
	private void runAndCheck(String name, final IRunnableDevice<ScanModel> scan, int pointTime, int fileSizeKB) throws Exception {
		
		long before = System.currentTimeMillis();
		scan.run(null);
		long after = System.currentTimeMillis();
	
		long time = (after-before);
		System.out.println("------------------------------");
		System.out.println("Ran "+name+" in "+time+"ms including tree write time");
		System.out.println(gen.size()+" points at "+(time/gen.size())+"ms/pnt");
		System.out.println("File size is "+output.length()/1024+"kB");
		System.out.println();
		
		assertTrue("The time must be less than "+pointTime+"ms", (time/gen.size())<pointTime);
		long sizeKB = (output.length()/1024);
		assertTrue("The size must be less than "+fileSizeKB+"kB. It is "+sizeKB+"kB", sizeKB<fileSizeKB);
	}
}
