package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.server.servlet.Services;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SeekTest extends AbstractAcquisitionTest {

	@Before
	public void setupServices() throws Exception {
		super.setupServices();
	}

	@Test
	public void seekFirst() throws Exception {
		
		IDeviceController controller = createTestScanner(null);
		AbstractRunnableDevice<ScanModel> scanner = (AbstractRunnableDevice<ScanModel>)controller.getDevice();
    
		try {
			scanner.start(null);		
			scanner.latch(100, TimeUnit.MILLISECONDS); // Latch onto the scan, breaking before it is finished.
			scanner.pause();
			
			IPosition first   = scanner.getModel().getPositionIterable().iterator().next();
			IPosition current = scanner.getPositioner().getPosition();
			assertNotNull(current);
			assertNotEquals(first, current);
			
			scanner.seek(0);
			current = scanner.getPositioner().getPosition();
			assertNotNull(current);
			assertEquals(first.getStepIndex(), current.getStepIndex());
			
		} finally {
			scanner.abort();
		}
	}
	
	@Ignore("This MUST pass but requires a larger refactor so seeing that the initial changes do with the tests first.")
	@Test
	public void seekFirstRestartsInCorrectLocation() throws Exception {
		
		IDeviceController controller = createTestScanner(null);
		AbstractRunnableDevice<ScanModel> scanner = (AbstractRunnableDevice<ScanModel>)controller.getDevice();
		
		final List<Integer> steps = new ArrayList<Integer>();
		scanner.addPositionListener(new IPositionListener() {
			public void positionPerformed(PositionEvent evt) throws ScanningException {
				steps.add(evt.getPosition().getStepIndex());
			}
		});
		try {
			scanner.start(null);		
			scanner.latch(100, TimeUnit.MILLISECONDS); // Latch onto the scan, breaking before it is finished.
			scanner.pause();
			
			IPosition first   = scanner.getModel().getPositionIterable().iterator().next();
			IPosition stopped = scanner.getPositioner().getPosition();
			assertNotNull(stopped);
			assertNotEquals(first, stopped);
			assertTrue(stopped.getStepIndex()>0);
			assertTrue(stopped.getStepIndex()<24);
			
			scanner.seek(0);
			IPosition current = scanner.getPositioner().getPosition();
			assertNotNull(current);
			assertEquals(first.getStepIndex(), current.getStepIndex());
			scanner.resume();
			scanner.latch(10, TimeUnit.SECONDS);
			
			// The scan should restart from where it is seeked to.
			// Therefore the steps should be size (25) + stopped.getStepIndex()
			assertEquals("The scan should restart from where it is seeked to.",
					     25+stopped.getStepIndex(), steps.size());
			
		} finally {
			scanner.abort();
		}
	}

	@Test
	public void staticScannerAvaileForJython() throws Exception {

		IDeviceController controller = createTestScanner(null);
		IRunnableDevice<ScanModel> scanner = (IRunnableDevice<ScanModel>)controller.getDevice();
    
		try {
			scanner.start(null);		
			scanner.latch(100, TimeUnit.MILLISECONDS); // Latch onto the scan, breaking before it is finished.

            assertTrue(Services.getRunnableDeviceService().getActiveScanner()!=null);
			scanner.latch(10, TimeUnit.SECONDS);
		
		} finally {
			scanner.abort();
		}
        assertTrue(Services.getRunnableDeviceService().getActiveScanner()==null);

	}

	@Test(expected=ScanningException.class)
	public void seekTooLarge() throws Exception {

		IDeviceController controller = createTestScanner(null);
		AbstractRunnableDevice<ScanModel> scanner = (AbstractRunnableDevice<ScanModel>)controller.getDevice();
    
		try {
			scanner.start(null);		
			scanner.latch(100, TimeUnit.MILLISECONDS); // Latch onto the scan, breaking before it is finished.
			scanner.pause();
			
			IPosition first   = scanner.getModel().getPositionIterable().iterator().next();
			IPosition current = scanner.getPositioner().getPosition();
			assertNotNull(current);
			assertNotEquals(first, current);
			
			scanner.seek(100); // Too large
			
		} finally {
			scanner.abort();
		}

	}
	
	@Test(expected=ScanningException.class)
	public void seekTooSmall() throws Exception {

		IDeviceController controller = createTestScanner(null);
		AbstractRunnableDevice<ScanModel> scanner = (AbstractRunnableDevice<ScanModel>)controller.getDevice();
    
		try {
			scanner.start(null);		
			scanner.latch(100, TimeUnit.MILLISECONDS); // Latch onto the scan, breaking before it is finished.
			scanner.pause();
			
			IPosition first   = scanner.getModel().getPositionIterable().iterator().next();
			IPosition current = scanner.getPositioner().getPosition();
			assertNotNull(current);
			assertNotEquals(first, current);
			
			scanner.seek(-1); // Too large
			
		} finally {
			scanner.abort();
		}

	}

}
