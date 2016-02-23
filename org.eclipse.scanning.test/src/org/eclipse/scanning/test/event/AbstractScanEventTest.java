package org.eclipse.scanning.test.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.junit.After;
import org.junit.Test;

public class AbstractScanEventTest {

	protected IEventService              eservice;
	protected IPublisher<ScanBean>       publisher;
	protected ISubscriber<IScanListener> subscriber;

	@After
	public void dispose() throws EventException {
		publisher.disconnect();
		subscriber.disconnect();
	}

	@Test
	public void badURITest() throws Exception {
		try {
			final URI uri = new URI("tcp://rubbish:5600");	
			publisher = eservice.createPublisher(uri, IEventService.SCAN_TOPIC);
			final ScanBean bean = new ScanBean();
			publisher.broadcast(bean);

		} catch (EventException required) {
			return;
		}
		throw new Exception("Able to connect to tcp://rubbish:5600 without an error!");
	}

	@Test
	public void blindBroadcastTest() throws Exception {

		final ScanBean bean = new ScanBean();
		bean.setName("fred");
		publisher.broadcast(bean);
	}

	@Test
	public void checkedBroadcastTest() throws Exception {

		final ScanBean bean = new ScanBean();
		bean.setName("fred");
		
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanEventPerformed(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
		});
		
		publisher.broadcast(bean);
		
		Thread.sleep(500); // The bean should go back and forth in ms anyway
		
		if (!bean.equals(gotBack.get(0))) throw new Exception("Bean did not come back!");
	}
	
	@Test
	public void checkedStateTest() throws Exception {

		final ScanBean bean = new ScanBean();
		bean.setName("fred");
		bean.setDeviceState(DeviceState.IDLE);
		
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
		});
		
		// Mimic a scan
		bean.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean);

		bean.setDeviceState(DeviceState.READY);
		publisher.broadcast(bean);

		for (int i = 0; i < 10; i++) {
			bean.setDeviceState(DeviceState.RUNNING);
			bean.setPercentComplete(i*10);
			publisher.broadcast(bean);
		}
		
		bean.setDeviceState(DeviceState.IDLE);
		publisher.broadcast(bean);
		
		Thread.sleep(500); // The bean should go back and forth in ms anyway

		if (gotBack.size()!=4) throw new Exception("The wrong number of state changes happened during the fake scan! Number found "+gotBack.size());
 	
		checkState(0, DeviceState.CONFIGURING, gotBack);
		checkState(1, DeviceState.READY,       gotBack);
		checkState(2, DeviceState.RUNNING,     gotBack);
		checkState(3, DeviceState.IDLE,        gotBack);
	}
	
	@Test
	public void checkedStateTestScanSpecific() throws Exception {

		final ScanBean bean = new ScanBean();
		bean.setName("fred");
		bean.setDeviceState(DeviceState.IDLE);
		
		final ScanBean bean2 = new ScanBean();
		bean2.setName("fred2");
		bean2.setDeviceState(DeviceState.IDLE);
		
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(bean.getUniqueId(), new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
		});
		
		final List<ScanBean> all = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				all.add(evt.getBean());
			}
		});

		
		// Mimic scan
		bean.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean);
		bean2.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean2);

		bean.setDeviceState(DeviceState.READY);
		publisher.broadcast(bean);
		bean2.setDeviceState(DeviceState.READY);
		publisher.broadcast(bean2);

		for (int i = 0; i < 10; i++) {
			bean.setDeviceState(DeviceState.RUNNING);
			bean.setPercentComplete(i*10);
			publisher.broadcast(bean);
			bean2.setDeviceState(DeviceState.RUNNING);
			bean2.setPercentComplete(i*10);
			publisher.broadcast(bean2);
		}
		
		bean.setDeviceState(DeviceState.IDLE);
		publisher.broadcast(bean);
		bean2.setDeviceState(DeviceState.IDLE);
		publisher.broadcast(bean2);
		
		Thread.sleep(500); // The bean should go back and forth in ms anyway

		if (gotBack.size()!=4) throw new Exception("The wrong number of state changes happened during the fake scan! Number found "+gotBack.size());
 	
		checkState(0, DeviceState.CONFIGURING, gotBack);
		checkState(1, DeviceState.READY,       gotBack);
		checkState(2, DeviceState.RUNNING,     gotBack);
		checkState(3, DeviceState.IDLE,        gotBack);
		
		if (all.size()!=(2*gotBack.size())) {
			throw new Exception("The size of all events was not twice as big as those for one specific scan yet we only had two scans publishing!");
		}
	}
	
	@Test
	public void missedScanEventsTest() throws Exception {

		final ScanBean bean = new ScanBean();
		bean.setName("fred");
		bean.setDeviceState(DeviceState.IDLE);
		
		final ScanBean bean2 = new ScanBean();
		bean2.setName("fred2");
		bean2.setDeviceState(DeviceState.IDLE);
		
		final List<ScanBean> gotBack = new ArrayList<ScanBean>();
		subscriber.addListener(bean.getUniqueId(), new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
				try {
					// Should go here 4 times, taking ~2 secs
					// Make this handler slow so events are missed
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Do nothing in a test
				}
			}
		});
		
		final List<ScanBean> all = new ArrayList<ScanBean>();
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				all.add(evt.getBean());
			}
		});

		// Mimic scan
		bean.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean);
		bean2.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean2);

		bean.setDeviceState(DeviceState.READY);
		publisher.broadcast(bean);
		bean2.setDeviceState(DeviceState.READY);
		publisher.broadcast(bean2);

		for (int i = 0; i < 10; i++) {
			bean.setDeviceState(DeviceState.RUNNING);
			bean.setPercentComplete(i*10);
			publisher.broadcast(bean);
			bean2.setDeviceState(DeviceState.RUNNING);
			bean2.setPercentComplete(i*10);
			publisher.broadcast(bean2);
		}
		
		bean.setDeviceState(DeviceState.IDLE);
		publisher.broadcast(bean);
		bean2.setDeviceState(DeviceState.IDLE);
		publisher.broadcast(bean2);
		
		// Wait for 10 secs >> 2 secs
		Thread.sleep(10000); // The bean should go back and forth in ms anyway

		if (gotBack.size()!=4) throw new Exception("The wrong number of state changes happened during the fake scan! Number found "+gotBack.size());
 	
		checkState(0, DeviceState.CONFIGURING, gotBack);
		checkState(1, DeviceState.READY,       gotBack);
		checkState(2, DeviceState.RUNNING,     gotBack);
		checkState(3, DeviceState.IDLE,        gotBack);
		
		if (all.size()!=(2*gotBack.size())) {
			throw new Exception("The size of all events was not twice as big as those for one specific scan yet we only had two scans publishing!");
		}
	}
	
	private void checkState(int i, DeviceState state, List<ScanBean> gotBack) throws Exception {
	    if (gotBack.get(i).getDeviceState()!=state) throw new Exception("The "+i+" change was not "+state);
	}

}
