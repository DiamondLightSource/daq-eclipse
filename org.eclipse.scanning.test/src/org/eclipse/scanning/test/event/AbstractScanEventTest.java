package org.eclipse.scanning.test.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.State;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class AbstractScanEventTest {

	protected IEventService              eservice;
	protected IPublisher<ScanBean>       publisher;
	protected ISubscriber<IScanListener> subscriber;

	@Test
	public void badURITest() throws Exception {
		try {
			final URI uri = new URI("tcp://rubbish:5600");	
			publisher = eservice.createPublisher(uri, IEventService.SCAN_TOPIC, new ActivemqConnectorService());
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
		bean.setScanName("fred");
		bean.setStart(0);
		bean.setStop(10);
		bean.setStep(2);
		publisher.broadcast(bean);
	}

	@Test
	public void checkedBroadcastTest() throws Exception {

		final ScanBean bean = new ScanBean();
		bean.setScanName("fred");
		bean.setStart(0);
		bean.setStop(10);
		bean.setStep(2);
		
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanEventPerformed(ScanEvent evt) {
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
		bean.setScanName("fred");
		bean.setState(State.IDLE);
		
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
		});
		
		// Mimic scan
		bean.setState(State.CONFIGURING);
		publisher.broadcast(bean);

		bean.setState(State.READY);
		publisher.broadcast(bean);

		for (int i = 0; i < 10; i++) {
			bean.setState(State.RUNNING);
			bean.setPercentComplete(i*10);
			publisher.broadcast(bean);
		}
		
		bean.setState(State.IDLE);
		publisher.broadcast(bean);
		
		Thread.sleep(500); // The bean should go back and forth in ms anyway

		if (gotBack.size()!=4) throw new Exception("The wrong number of state changes happened during the fake scan! Number found "+gotBack.size());
 	
		checkState(0, State.CONFIGURING, gotBack);
		checkState(1, State.READY,       gotBack);
		checkState(2, State.RUNNING,     gotBack);
		checkState(3, State.IDLE,        gotBack);
	}
	
	@Test
	public void checkedStateTestScanSpecific() throws Exception {

		final ScanBean bean = new ScanBean();
		bean.setScanName("fred");
		bean.setState(State.IDLE);
		
		final ScanBean bean2 = new ScanBean();
		bean2.setScanName("fred2");
		bean2.setState(State.IDLE);
		
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(bean.getId(), new IScanListener.Stub() {
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
		bean.setState(State.CONFIGURING);
		publisher.broadcast(bean);
		bean2.setState(State.CONFIGURING);
		publisher.broadcast(bean2);

		bean.setState(State.READY);
		publisher.broadcast(bean);
		bean2.setState(State.READY);
		publisher.broadcast(bean2);

		for (int i = 0; i < 10; i++) {
			bean.setState(State.RUNNING);
			bean.setPercentComplete(i*10);
			publisher.broadcast(bean);
			bean2.setState(State.RUNNING);
			bean2.setPercentComplete(i*10);
			publisher.broadcast(bean2);
		}
		
		bean.setState(State.IDLE);
		publisher.broadcast(bean);
		bean2.setState(State.IDLE);
		publisher.broadcast(bean2);
		
		Thread.sleep(500); // The bean should go back and forth in ms anyway

		if (gotBack.size()!=4) throw new Exception("The wrong number of state changes happened during the fake scan! Number found "+gotBack.size());
 	
		checkState(0, State.CONFIGURING, gotBack);
		checkState(1, State.READY,       gotBack);
		checkState(2, State.RUNNING,     gotBack);
		checkState(3, State.IDLE,        gotBack);
		
		if (all.size()!=(2*gotBack.size())) {
			throw new Exception("The size of all events was not twice as big as those for one specific scan yet we only had two scans publishing!");
		}
	}
	
	private void checkState(int i, State state, List<ScanBean> gotBack) throws Exception {
	    if (gotBack.get(i).getState()!=state) throw new Exception("The "+i+" change was not "+state);
	}

}
