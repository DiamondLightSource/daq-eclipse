package org.eclipse.scanning.test.scan;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceOperationCancelledException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IPauseableDevice;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ThreadScanTest {
	
	private IScanningService           sservice;
	private IDeviceConnectorService    connector;
	private IGeneratorService          gservice;
	private IEventService              eservice;
	private ISubscriber<IScanListener> subscriber;
	private IPublisher<ScanBean>       publisher;

	protected final static int IMAGE_COUNT = 5;

	@Before
	public void setup() throws Exception {
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		sservice  = new ScanningServiceImpl();
		connector = new MockScannableConnector();
		gservice  = new GeneratorServiceImpl();
		
		
		eservice   = new EventServiceImpl();
		subscriber = eservice.createSubscriber(new URI("tcp://sci-serv5.diamond.ac.uk:61616"), IEventService.SCAN_TOPIC, new ActivemqConnectorService()); // Create an in memory consumer of messages.
		publisher  = eservice.createPublisher(new URI("tcp://sci-serv5.diamond.ac.uk:61616"), IEventService.SCAN_TOPIC, new ActivemqConnectorService());
	}
	
	@After
	public void shutdown() throws Exception {
		subscriber.disconnect();
		publisher.disconnect();
	}
	
	@Test
	public void testPauseAndResume2Threads() throws Throwable {
		
		IPauseableDevice<?> device = createConfiguredDevice(5, 5);
		pause1000ResumeLoop(device, 2, 2000, false);
	}
	
	@Test
	public void testPauseAndResume10Threads() throws Throwable {
		
		IPauseableDevice<?> device = createConfiguredDevice(10, 10);
		pause1000ResumeLoop(device, 10, 2000, false);
	}


	protected IRunnableDevice<?> pause1000ResumeLoop(final IPauseableDevice<?> device, 
													 int threadcount, 
													 long sleepTime, 
													 boolean expectExceptions) throws Throwable {

		final List<Throwable> exceptions = new ArrayList<>(1);
		runDeviceInThread(device, exceptions);
		
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanEventPerformed(ScanEvent e) {
			    if (e.getBean().getMessage()!=null) System.out.println(e.getBean().getMessage());
			}
		});

		final List<ScanBean> beans = new ArrayList<ScanBean>(IMAGE_COUNT);
		createPauseEventListener(device, beans);	

		final List<Integer> usedThreads = new ArrayList<>();
		for (int i = 0; i < threadcount; i++) {
			final Integer current = i;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						System.out.println("Running thread Thread"+current+". Device = "+device.getName());
						checkPauseResume(device, 1000, true);

					} catch(MalcolmDeviceOperationCancelledException mdoce) {
						mdoce.printStackTrace();
						usedThreads.add(current);
						exceptions.add(mdoce);

					} catch (Exception e) {
						e.printStackTrace();
						exceptions.add(e);
					}
				}
			}, "Thread"+i);

			thread.setPriority(9);
			if (sleepTime>0) {
				thread.setDaemon(true); // Otherwise we are running them in order anyway
			}
			thread.start();
			System.out.println("Started thread Thread"+i);

			if (sleepTime>0) {
				Thread.sleep(sleepTime);
			} else{
				Thread.sleep(100);
				thread.join();
			}
		}

		if (expectExceptions && exceptions.size()>0) {
			for (Throwable ne : exceptions) {
				ne.printStackTrace();
			}
			return device; // Pausing failed as expected
		}

		// Wait for end of run for 30 seconds, otherwise we carry on (test will then likely fail)
		if (device.getState()!=DeviceState.READY) {
			latch(30, TimeUnit.SECONDS, DeviceState.RUNNING, DeviceState.PAUSED, DeviceState.PAUSING); // Wait until not running.
		}

		if (exceptions.size()>0) throw exceptions.get(0);
		
		if (device.getState()!=DeviceState.READY) throw new Exception("The state at the end of the pause/resume cycle(s) must be "+DeviceState.READY+" not "+device.getState());
		int expectedThreads = usedThreads.size() > 0 ? usedThreads.get(0) : threadcount;
		// TODO Sometimes too many pause events come from the real malcolm connection.
		if (beans.size()<expectedThreads) throw new Exception("The pause event was not encountered the correct number of times! Found "+beans.size()+" required "+expectedThreads);

		return device;
	}
	
	
	private void latch(long time, TimeUnit unit, final DeviceState... invalids) throws Exception {
		
		final CountDownLatch latch = new CountDownLatch(1);
		ISubscriber<IScanListener> subscriber = eservice.createSubscriber(new URI("tcp://sci-serv5.diamond.ac.uk:61616"), IEventService.SCAN_TOPIC, new ActivemqConnectorService()); // Create an in memory consumer of messages.
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				ScanBean bean = evt.getBean();
	   			if (!Arrays.asList(invalids).contains(bean.getDeviceState())) {
	   				latch.countDown();
	   			}
			}
		});
		latch.await(time, unit);
	}


	protected void createPauseEventListener(IRunnableDevice<?> device, final List<ScanBean> beans) throws EventException, URISyntaxException {
		
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				ScanBean bean = evt.getBean();
	   			if (bean.getDeviceState()==DeviceState.PAUSED) {
				    beans.add(bean);
				}
			}
		});	
	}

	private IPauseableDevice<?> createConfiguredDevice(int rows, int columns) throws ScanningException, GeneratorException, URISyntaxException {
		
		// Configure a detector with a collection time.
		IRunnableDevice<MockDetectorModel> detector = connector.getDetector("detector");
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCollectionTime(0.1);
		detector.configure(dmodel);
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setRows(rows);
		gmodel.setColumns(columns);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));	
		IGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterator(gen);
		smodel.setDetectors(detector);
		
		// Create a scan and run it
		IPauseableDevice<ScanModel> scanner = (IPauseableDevice<ScanModel>) sservice.createRunnableDevice(smodel, publisher, connector);
		return scanner;
	}


	protected IRunnableDevice<?> runDeviceInThread(final IRunnableDevice<?> device, final List<Throwable> exceptions) throws Exception {
		
		final Thread runner = new Thread(new Runnable() {
			public void run() {
				try {
					device.run();
				} catch (Exception e) {
					exceptions.add(e);
				} // blocks until finished
			}
		}, "Malcolm test execution thread");
		runner.start();
		
		// We sleep because this is a test
		// which starts a thread running from the same location.
		Thread.sleep(1000); // Let it get going.
		// The idea is that using Malcolm will NOT require sleeps like we used to have.
				
		return device;
	}
	
	protected synchronized void checkPauseResume(IPauseableDevice<?> device, long pauseTime, boolean ignoreReady) throws Exception {
		
		
		// No fudgy sleeps allowed in test must be as dataacq would use.
		if (ignoreReady && device.getState()==DeviceState.READY) return;
		System.out.println("Pausing device in state: "+device.getState());
		
		device.pause();
		System.out.println("Device is "+device.getState());
		
		if (pauseTime>0) {
			Thread.sleep(pauseTime);
			System.out.println("We waited with for "+pauseTime+" device is now in state "+device.getState());
		}
		
		DeviceState state = device.getState();
		if (state!=DeviceState.PAUSED) throw new Exception("The state is not paused!");

		device.resume();  // start it going again, non-blocking

		Thread.sleep(100);
		System.out.println("Device is resumed state is "+device.getState());
	}

}
