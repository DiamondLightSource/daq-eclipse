package org.eclipse.malcolm.test.device;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.malcolm.api.IMalcolmDevice;
import org.eclipse.malcolm.api.MalcolmDeviceException;
import org.eclipse.malcolm.api.State;

public class LatchDelegate {

	
	protected Map<Long, CountDownLatch> testLatchMap;
	
	public LatchDelegate() {
		this.testLatchMap = new ConcurrentHashMap<>(3);
	}

	protected State latch(IMalcolmDevice device, State... ignored) throws Exception {
        return latch(device, -1, TimeUnit.SECONDS, ignored);
	}
	
	protected State latch(IMalcolmDevice device, long time, TimeUnit unit, State... ignored) throws Exception {
		Long id = Thread.currentThread().getId();
		if (testLatchMap.containsKey(id)) {
			throw new MalcolmDeviceException(device, "The thread '"+Thread.currentThread().getName()+"'is already waiting for state change! Something went wrong with the latch locking!");
		}

		CountDownLatch latch = new CountDownLatch(1);
		testLatchMap.put(id, latch);

		await(device, latch, time, unit);  // We cannot latch forever or risk deadlock
		if (testLatchMap.containsKey(id)) {
			throw new MalcolmDeviceException(device, "Unexpected latch key thread '"+Thread.currentThread().getName()+"' not cleared!");
		}

		if (ignored!=null) {
			List<State> states = Arrays.asList(ignored);
			while(states.contains(device.getState())) {
				latch = new CountDownLatch(1);
				testLatchMap.put(id, latch);
				await(device, latch, time, unit); // We cannot latch forever or risk deadlock
				if (testLatchMap.containsKey(id)) {
					throw new MalcolmDeviceException(device, "Unexpected latch key thread '"+Thread.currentThread().getName()+"' not cleared!");
				}
			}
		} 
		
		return device.getState();
	}
	
	private final void await(IMalcolmDevice device, CountDownLatch latch, long timeout, TimeUnit unit) throws Exception {
		try {
			if (timeout < 1) {
				latch.await();
			} else {
				boolean ok = latch.await(timeout, unit);
				if (!ok) {
					throw new MalcolmDeviceException(device, "Latch took longer than "+timeout+" "+unit+". Device state is " + device.getState());
				}
			}
		} catch (InterruptedException e) {
			throw new Exception("Cannot latch on to state change!", e);
		}
	}

	public void setState(State state) {
		
		if (testLatchMap != null) {
			// Important, copy, clear, notify or will not work
			CountDownLatch[] latches = testLatchMap.values().toArray(new CountDownLatch[testLatchMap.size()]);
			testLatchMap.clear();
			for (CountDownLatch latch : latches)  {
				latch.countDown();
			}
		}
	}


}
