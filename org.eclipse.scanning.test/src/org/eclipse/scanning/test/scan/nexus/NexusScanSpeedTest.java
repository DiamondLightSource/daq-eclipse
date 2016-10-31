package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.After;
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
		System.setProperty("org.eclipse.scanning.sequencer.AcquisitionDevice.Metrics", "true");
		this.gen = gservice.createGenerator(new StepModel("xNex", 0, 1000, 1));
	}
	@After
	public void after() {
		System.setProperty("org.eclipse.scanning.sequencer.AcquisitionDevice.Metrics", "false");
	}
	
	@Test
	public void testBareNexusStepScanSpeed() throws Exception {
		
		// We create a step scan
		final IRunnableDevice<ScanModel> scan = dservice.createRunnableDevice(new ScanModel(gen, output));
		runAndCheck(scan, 10, 256);
	}
	
	@Test
	public void testPublishedNexusStepScanSpeed() throws Exception {
		
		// We create a step scan
		IPublisher<ScanBean> publisher = eservice.createPublisher(uri, EventConstants.SCAN_TOPIC);
		final IRunnableDevice<ScanModel> scan = dservice.createRunnableDevice(new ScanModel(gen, output), publisher);
		runAndCheck(scan, 10, 256);
	}

	
	private void runAndCheck(final IRunnableDevice<ScanModel> scan, int pointTime, int fileSizeKB) throws Exception {
		
		long before = System.currentTimeMillis();
		scan.run(null);
		long after = System.currentTimeMillis();
	
		long time = (after-before);
		System.out.println("Ran "+gen.getLabel()+" in "+time+"ms");
		System.out.println(gen.size()+" points at "+(time/gen.size())+"ms/pnt");
		System.out.println("File size is "+getFileSize(output));
		
		assertTrue((time/gen.size())<pointTime);
		assertTrue((output.length()/1024)<fileSizeKB);
	}
	
	private String getFileSize(File file) {
		long bytes = file.length();
	    int u = 0;
	    for (;bytes > 1024*1024; bytes >>= 10)  u++;
	    if (bytes > 1024) u++;
	    return String.format("%.1f %cB", bytes/1024f, " kMGTPE".charAt(u));
	}
}
