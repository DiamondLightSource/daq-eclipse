package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class MappingScanTest {

	protected IEventService eservice;
	protected IPublisher<ScanBean> publisher;
	protected ISubscriber<IScanListener> subscriber;
	protected IGeneratorService gservice;

	@Before
	public void createServices() throws Exception {

		// Do not copy this get the service from OSGi!
		eservice = new EventServiceImpl();
		gservice = new GeneratorServiceImpl();

		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		publisher = eservice.createPublisher(uri, IEventService.SCAN_TOPIC, new ActivemqConnectorService()); // Do not copy this leave as null!
		subscriber = eservice.createSubscriber(uri, IEventService.SCAN_TOPIC, new ActivemqConnectorService()); // Do not copy this leave as null!
	}

	/**
	 * This test mimics a scan being run
	 * 
	 * Eventually we will need a test running the sequencing system.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimpleMappingScan() throws Exception {

		// Listen to events sent
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
		});

		// Simulate queueing it
		final ScanBean bean = new ScanBean();
		bean.setName("Test Mapping Scan");
		bean.setBeamline("I05-1");
		bean.setUserName("Joe Bloggs");
		bean.setDeviceState(DeviceState.IDLE);
		bean.setPreviousStatus(Status.SUBMITTED);
		bean.setStatus(Status.QUEUED);
		bean.setFilePath("/dls/tmp/fred.h5");
		bean.setDatasetPath("/entry/data");
		publisher.broadcast(bean);

		// Tell them we started it.
		bean.setPreviousStatus(Status.QUEUED);
		bean.setStatus(Status.RUNNING);
		publisher.broadcast(bean);

		bean.setSize(10);
		int ipoint = 0;

		BoundingBox box = new BoundingBox();
		box.setxStart(10);
		box.setyStart(10);
		box.setWidth(5);
		box.setHeight(2);

		final GridModel model = new GridModel();
		model.setRows(2);
		model.setColumns(5);
		model.setBoundingBox(box);

		IGenerator<GridModel, Point> gen = gservice.createGenerator(model, null);

		// Outer loop temperature, will be scan command driven when sequencer exists.
		for (double temp = 273; temp < 283; temp++) {
			bean.setPoint(ipoint);
			bean.putPosition("temperature", temp);
			testDeviceScan(bean, gen);
			Thread.sleep(1000); // Moving to the new temp takes non-zero time so I've heard.
			++ipoint;
		}

		bean.setPreviousStatus(Status.RUNNING);
		bean.setStatus(Status.COMPLETE);
		publisher.broadcast(bean);

		Thread.sleep(1000); // Just to make sure all the message events come in

		assertTrue(gotBack.size() > 10);
		assertTrue(gotBack.get(1).scanStart());
		assertTrue(gotBack.get(gotBack.size() - 1).scanEnd());
	}

	private void testDeviceScan(ScanBean bean, IGenerator<GridModel, Point> gen)
			throws Exception {

		// Mimic a scan
		bean.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean);

		bean.setDeviceState(DeviceState.READY);
		publisher.broadcast(bean);

		bean.setDeviceState(DeviceState.RUNNING);
		int size = 0;
		for (IPosition pnt : gen) {
			bean.putPosition("zebra_x", pnt.get("X"));
			bean.putPosition("zebra_y", pnt.get("Y"));
			publisher.broadcast(bean);
			++size;
		}
		System.out.println("Did hardware scan of size " + size);
		assertTrue(size == gen.size());

		bean.setDeviceState(DeviceState.IDLE);
		publisher.broadcast(bean);

	}

}
