package org.eclipse.scanning.test.malcolm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class AbstractAbortingMalcolmTest extends AbstractMalcolmTest {
	
	private static final int REPEAT_COUNT = 1;
	
	@Parameterized.Parameters
	public static List<Object[]> data() {
	    return Arrays.asList(new Object[REPEAT_COUNT][0]);
	}

	@Test
	public void testAbortConfiguring() throws Throwable {

		final List<Throwable> exceptions = new ArrayList<>(1);
		configureInThread(device, 5000, 10, exceptions);
		checkAbort(device, 1000);
		
		if (exceptions.size()>0) throw exceptions.get(0);
	}

	@Test
	public void testAbortRunning() throws Throwable {

		final List<Throwable> exceptions = new ArrayList<>(1);
		configure(device, 10);
		runDeviceInThread(device, exceptions);		
		checkAbort(device, 1000);
		
		if (exceptions.size()>0) throw exceptions.get(0);
	}
	
	@Test
	public void testAbortPaused() throws Throwable {

		final List<Throwable> exceptions = new ArrayList<>(1);
		configure(device, 10);
		runDeviceInThread(device, exceptions);		
		Thread.sleep(1000);
		device.pause();
		checkAbort(device, 1000);
		
		if (exceptions.size()>0) throw exceptions.get(0);
	}
	
	@Test
	public void testAbortPausedByAnotherThread() throws Throwable {

		final List<Throwable> exceptions = new ArrayList<>(1);
		configure(device, 10);
		runDeviceInThread(device, exceptions);		
		Thread.sleep(1000);
		
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println("Requesting pause, device is "+device.getState());
					device.pause();
					System.out.println("Device is "+device.getState());
				} catch (Exception e) {
					exceptions.add(e);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
		
		// Wait for pause
		if (device.getState()!=DeviceState.PAUSED) {
		    device.latch(10, TimeUnit.SECONDS, DeviceState.PAUSING, DeviceState.RUNNING);
		}
		
		System.out.println("Aborting paused run, current state is "+device.getState());
		device.abort();
        
        if (device.getState()!=DeviceState.ABORTED) {
        	throw new Exception("State was not aborted after abort!");
        }
		
		if (exceptions.size()>0) throw exceptions.get(0);
	}

	@Test
	public void testAbortAfterSeveralPauseResume() throws Throwable {
        
		pause1000ResumeLoop(device, 10, 3, 2000, false, false, false);
		
		System.out.println("Aborting paused run, current state is "+device.getState());
		device.abort();
        
        if (device.getState()!=DeviceState.ABORTED) {
        	throw new Exception("State was not aborted after abort!");
        }
		
	}

	private void checkAbort(IMalcolmDevice zebra, long sleepTime) throws Exception {
		
        Thread.sleep(sleepTime);
        
        zebra.abort();
        
        if (zebra.getState()!=DeviceState.ABORTED) {
        	throw new Exception("State was not aborted after abort!");
        }
	}

}
