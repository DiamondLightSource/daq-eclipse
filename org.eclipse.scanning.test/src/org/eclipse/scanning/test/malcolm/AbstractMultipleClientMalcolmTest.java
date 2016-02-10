package org.eclipse.scanning.test.malcolm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.junit.Test;

public abstract class AbstractMultipleClientMalcolmTest extends AbstractMalcolmTest {

	@Test
	public void twoDevicesOneGettingState() throws Throwable {
		
		final List<Throwable> exceptions = new ArrayList<>(1);
		
		// Start writing in thread.
		configure(device, 10);
		runDeviceInThread(device, exceptions); // Don't use device returned.
 
		// In this test thread, we simply keep asking for the state.
		// We get an instance to the device separately to test two 
		// device connections (although MockService will not do this)
		final Collection<DeviceState> states = new HashSet<DeviceState>();
		IMalcolmConnection    connection = service.createConnection(null); // TODO real URL
		try {
			IMalcolmDevice zebra = connection.getDevice("zebra");
			zebra.addMalcolmListener(new IMalcolmListener<MalcolmEventBean>() {				
				@Override
				public void eventPerformed(MalcolmEvent<MalcolmEventBean> e) {
					states.add(e.getBean().getDeviceState());
				}
			});
			
			for (int i = 0; i < 10; i++) {
				System.out.println("Device state is "+zebra.getDeviceState());
				if (zebra.getDeviceState() == DeviceState.IDLE) {
					throw new Exception("The device should not be IDLE!");
				}
				Thread.sleep(1000);
			}
		} finally {
			connection.dispose();
		}
		
		if (!states.containsAll(Arrays.asList(new DeviceState[]{DeviceState.READY, DeviceState.RUNNING}))){
			throw new Exception("Not all expected states encountered during run! States found were "+states);
		}
		
		if (exceptions.size()>0) throw exceptions.get(0);
	}
	
	
	@Test
	public void manyDevicesManyGettingState() throws Throwable {
		
		final List<Throwable> exceptions = new ArrayList<>(1);
		
		// Start writing in thread.
		configure(device, 10);
		runDeviceInThread(device, exceptions); // Don't use device returned.
 
		// In this test thread, we simply keep asking for the state.
		// We get an instance to the device separately to test two 
		// device connections (although MockService will not do this)
		final Collection<DeviceState> states = new HashSet<DeviceState>();
		final ExecutorService   exec   = Executors.newFixedThreadPool(10);

		for (int i = 0; i < 10; i++) { // Ten threads all getting state with separate devices.

			exec.execute(new Runnable() {
				public void run() {

					IMalcolmConnection connection = null;
					try {
						connection = service.createConnection(null); // TODO real URL
						IMalcolmDevice zebra = connection.getDevice("zebra");
						zebra.addMalcolmListener(new IMalcolmListener<MalcolmEventBean>() {				
							@Override
							public void eventPerformed(MalcolmEvent<MalcolmEventBean> e) {
								states.add(e.getBean().getDeviceState());
							}
						});

						for (int i = 0; i < 5; i++) {
							System.out.println("Device state is "+zebra.getDeviceState());
							if (zebra.getDeviceState() == DeviceState.IDLE) {
								exceptions.add(new Exception("The device should not be IDLE!"));
							}
							Thread.sleep(1000);
						}
					} catch (Exception ne) {
						exceptions.add(ne);
					} finally {
						if (connection!=null) {
							try {
								connection.dispose();
							} catch (MalcolmDeviceException e) {
								exceptions.add(e);
							}
						}
					}
				}
			});
		}

		exec.shutdown();
		exec.awaitTermination(20, TimeUnit.SECONDS);

		
		
		
		if (!states.containsAll(Arrays.asList(new DeviceState[]{DeviceState.READY, DeviceState.RUNNING}))){
			throw new Exception("Not all expected states encountered during run! States found were "+states);
		}
		
		if (exceptions.size()>0) throw exceptions.get(0);
	}

		
	@Test
	public void twoDevicesOneGettingStateWithPausing() throws Throwable {
		
		final List<Throwable> exceptions = new ArrayList<>(1);
		
		final Collection<DeviceState> states = new HashSet<DeviceState>();
		IMalcolmConnection    connection = service.createConnection(PAUSABLE); // TODO real URL
		try {
			// Add listener
			IMalcolmDevice zebra = connection.getDevice("zebra");
			zebra.addMalcolmListener(new IMalcolmListener<MalcolmEventBean>() {				
				@Override
				public void eventPerformed(MalcolmEvent<MalcolmEventBean> e) {
					states.add(e.getBean().getDeviceState());
				}
			});
		
			// Start writing in thread using different device reference.
			pause1000ResumeLoop(device, 5, 2, 2000, false, false, true); // Run scan in thread, run pause in thread, use separate device connections

			// In this test thread, we simply keep asking for the state.
			// We get an instance to the device separately to test two 
			// device connections (although MockService will not do this)
			for (int i = 0; i < 10; i++) {
				System.out.println("Device state is "+zebra.getDeviceState());
				if (zebra.getDeviceState() == DeviceState.IDLE) {
					throw new Exception("The device should not be IDLE!");
				}
				Thread.sleep(1000);
			}
		} finally {
			connection.dispose();
		}
		
		if (!states.containsAll(Arrays.asList(new DeviceState[]{DeviceState.READY, DeviceState.RUNNING, DeviceState.PAUSED, DeviceState.PAUSING}))){
			throw new Exception("Not all expected states encountered during run! States found were "+states);
		}
		
		if (exceptions.size()>0) throw exceptions.get(0);
	}

}
