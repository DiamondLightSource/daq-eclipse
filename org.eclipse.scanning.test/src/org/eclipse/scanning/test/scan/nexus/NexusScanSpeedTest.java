package org.eclipse.scanning.test.scan.nexus;

import java.io.File;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NexusScanSpeedTest extends NexusTest {

	@Before
	public void before() {
		System.setProperty("org.eclipse.scanning.sequencer.AcquisitionDevice.Metrics", "true");
	}
	@After
	public void after() {
		System.setProperty("org.eclipse.scanning.sequencer.AcquisitionDevice.Metrics", "false");
	}
	
	@Test
	public void testStepScanSpeed() throws Exception {
		
		// We create a step scan
		final ScanModel model = new ScanModel();
		IPointGenerator<StepModel> gen = gservice.createGenerator(new StepModel("xNex", 0, 100, 1));
		model.setPositionIterable(gen);
		final File file = File.createTempFile("test_nexus_scan", ".nxs");
		file.deleteOnExit();
		model.setFilePath(file.getAbsolutePath());
		
		final IRunnableDevice<ScanModel> scan = dservice.createRunnableDevice(model);
		
		long before = System.currentTimeMillis();
		scan.run(null);
		long after = System.currentTimeMillis();
	
		long time = (after-before);
		System.out.println("Ran "+gen.getLabel()+" in "+time+"ms");
		System.out.println(gen.size()+" points at "+(time/gen.size())+"ms/pnt");
		System.out.println("File size is "+getFileSize(file));
	}
	
	private String getFileSize(File file) {
		long bytes = file.length();
	    int u = 0;
	    for (;bytes > 1024*1024; bytes >>= 10)  u++;
	    if (bytes > 1024) u++;
	    return String.format("%.1f %cB", bytes/1024f, " kMGTPE".charAt(u));
	}
}
