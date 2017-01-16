package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockTopupScannable;
import org.eclipse.scanning.sequencer.watchdog.TopupWatchdog;
import org.junit.After;
import org.junit.Test;

public class WatchdogTopupTest extends AbstractWatchdogTest {

	
	
	private IDeviceWatchdog dog;
		
	void createWatchdogs() throws Exception {

		// We create a device watchdog (done in spring for real server)
		DeviceWatchdogModel model = new DeviceWatchdogModel();
		model.setCountdownName("topup");
		model.setCooloff(500); // Pause 500ms before
		model.setWarmup(200);  // Unpause 200ms after
		model.setTopupTime(150);
		model.setPeriod(5000);
		
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.setPosition(1000);

		this.dog = new TopupWatchdog(model);
		dog.activate();
	}
	
	@After
	public void disconnect() throws Exception {
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.disconnect();
		topup.setPosition(1000);
	}
	
	@Test(expected=Exception.class)
	public void testBeamOn() throws Exception {
		
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		System.out.println(topup.getPosition());

		final IScannable<Number>   beamon   = connector.getScannable("beamon");
		beamon.setLevel(1);
		
		// x and y are level 3
		IDeviceController controller = createTestScanner(beamon);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		scanner.run(null);
		
		assertEquals(10, positions.size());
	}


	@Test
	public void topupPeriod() throws Exception {
		
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
	
		long fastPeriod = 500;
		long orig = topup.getPeriod();
		topup.setPeriod(fastPeriod);
		topup.start();
		Thread.sleep(100);
		
		try {
			int max = Integer.MIN_VALUE;
			int min = Integer.MAX_VALUE;
			// We start a check the topup value for 15s
			long start = System.currentTimeMillis();
			while((System.currentTimeMillis()-start)<(fastPeriod*2.1)) {
				int pos = topup.getPosition().intValue();
				max = Math.max(max, pos);
				min = Math.min(min, pos);
				Thread.sleep(fastPeriod/10);
			}
			assertTrue("The max is "+max, max<600&&max>300);
			assertTrue("The min is "+min, min<200&&min>-1);
			
		} finally {
			topup.setPeriod(orig);

		}
	}
	
	@Test
	public void topupInScan() throws Exception {

		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
        topup.start();
		
		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		
		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.run(null);
		
		assertTrue(states.contains(DeviceState.PAUSED));
		assertTrue(states.contains(DeviceState.RUNNING));
		assertTrue(states.contains(DeviceState.SEEKING));
	}
	
	@Test
	public void scanDuringTopup() throws Exception {

		// Stop topup, we want to controll it programmatically.
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertTrue(topup.isDisconnected());
		topup.setPosition(10);
		
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		
		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.start(null);
		Thread.sleep(50);  // Do a bit
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
		
		topup.setPosition(0);    // Should do nothing, device is already paused
		topup.setPosition(5000); // Gets it ready to think it has to resume
		topup.setPosition(4000); // Will resume it because warmup passed
		
		Thread.sleep(100);       // Ensure watchdog event has fired and it did something.		
		assertEquals(DeviceState.RUNNING, scanner.getDeviceState()); // Should still be paused
		
		scanner.latch();
		
		assertEquals(DeviceState.READY, scanner.getDeviceState()); // Should still be paused
	}

	
	@Test
	public void topupWithExternalPause() throws Exception {

		// Stop topup, we want to controll it programmatically.
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertTrue(topup.isDisconnected());
		topup.setPosition(5000);

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		
		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.start(null);
		Thread.sleep(25);  // Do a bit
		controller.pause("test", null);   // Pausing externally should override any watchdog resume.
		
		topup.setPosition(0);    // Should do nothing, device is already paused
		topup.setPosition(5000); // Gets it ready to think it has to resume
		topup.setPosition(4000); // Will resume it because warmup passed
		
		Thread.sleep(100);       // Ensure watchdog event has fired and it did something.		
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState()); // Should still be paused
		
		controller.resume("test");
		
		Thread.sleep(100);       	
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());

		topup.setPosition(0);

		Thread.sleep(100);       
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
		
		controller.resume("test"); // It shouldn't because now topup has set to pause.

		Thread.sleep(25);       // Ensure watchdog event has fired and it did something.		
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.abort("test");
	}

	
	@Test
	public void topupOutScan() throws Exception {

		try {
			dog.deactivate(); // Are a testing a pausing monitor here

			// x and y are level 3
			IDeviceController controller = createTestScanner(null);
			IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
			
			List<DeviceState> states = new ArrayList<>();
			// This run should get paused for beam and restarted.
			scanner.addRunListener(new IRunListener() {
				public void stateChanged(RunEvent evt) throws ScanningException {
					states.add(evt.getDeviceState());
				}
			});
			
			scanner.run(null);
			
			assertFalse(states.contains(DeviceState.PAUSED));
			assertTrue(states.contains(DeviceState.RUNNING));
			assertFalse(states.contains(DeviceState.SEEKING));
			
		} finally {
			dog.activate();
		}
	}
	
	@Test
	public void testPause() throws Exception {
		
		try {
			dog.deactivate(); // Are a testing a pausing monitor here
			detector.getModel().setExposureTime(0.0001); // Save some scan time.

			final List<String> moved   = new ArrayList<>();
			final IScannable<Number>   pauser   = connector.getScannable("pauser");
			if (pauser instanceof MockScannable) {
				((MockScannable)pauser).setRealisticMove(false);
			}
			((IPositionListenable)pauser).addPositionListener(new IPositionListener() {
				@Override
				public void positionPerformed(PositionEvent evt) {
					moved.add(pauser.getName());
				}
			});
			final IScannable<Number>   x       = connector.getScannable("x");
			if (x instanceof MockScannable) {
				((MockScannable)x).setRealisticMove(false);
			}
			((IPositionListenable)x).addPositionListener(new IPositionListener() {
				@Override
				public void positionPerformed(PositionEvent evt) {
					moved.add(x.getName());
				}
			});
			pauser.setLevel(1);
			
			// x and y are level 3
			IDeviceController controller = createTestScanner(pauser);
			IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
			scanner.run(null);
			
			assertEquals(25, positions.size());
			assertEquals(50, moved.size());
			assertTrue(moved.get(0).equals("pauser"));
			assertTrue(moved.get(1).equals("x"));
			
			moved.clear();
			positions.clear();
			pauser.setLevel(5); // Above x
			scanner.run(null);
			
			assertEquals(25, positions.size());
			assertEquals(50, moved.size());
			assertTrue(moved.get(0).equals("x"));
			assertTrue(moved.get(1).equals("pauser"));
		
		} finally {
			dog.activate();
			detector.getModel().setExposureTime(0.25);
		}
	}

}
