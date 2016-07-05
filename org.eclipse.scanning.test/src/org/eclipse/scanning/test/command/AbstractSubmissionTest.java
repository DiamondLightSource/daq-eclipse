package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.AbstractPausableProcess;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractSubmissionTest extends AbstractJythonTest {

	protected static IEventService eservice;
	protected IConsumer<ScanBean> consumer;

	private static BlockingQueue<String> testLog;
	// We'll use this to check that things happen in the right order.
	static {
		testLog = new ArrayBlockingQueue<>(2);
	}

	// Subclasses must tell us how to set up the IEventService.
	protected abstract void setUpEventService();

	@Before
	public void start() throws Exception {

		setUpEventService();

		consumer = eservice.createConsumer(uri,
				IEventService.SUBMISSION_QUEUE,
				IEventService.STATUS_SET,
				IEventService.STATUS_TOPIC,
				IEventService.HEARTBEAT_TOPIC,
				IEventService.CMD_TOPIC);

		consumer.setRunner(new IProcessCreator<ScanBean>() {
			@Override
			public IConsumerProcess<ScanBean> createProcess(
					ScanBean bean, IPublisher<ScanBean> statusNotifier)
							throws EventException {

				return new AbstractPausableProcess<ScanBean>(bean, statusNotifier) {

					@Override
					public void execute() throws EventException {
						try {
							// Pretend the scan is happening now...
							Thread.sleep(1000);
							testLog.put("Scan complete.");
							bean.setStatus(Status.COMPLETE);
							broadcast(bean);
						} catch (InterruptedException e) {}
					}

					@Override public void terminate() throws EventException {}
				};
			}
		});
		consumer.start();

		// Put any old ScanRequest in the Python namespace.
		pi.exec("sr = scan_request(step(my_scannable, 0, 10, 1), det=mandelbrot(0.1))");
	}

	@Test
	public void testSubmission() throws InterruptedException {
		pi.exec("submit(sr, broker_uri='"+uri+"')");
		testLog.put("Jython command returned.");

		// Jython returns *after* scan is complete.
		assertEquals("Scan complete.", testLog.take());
		assertEquals("Jython command returned.", testLog.take());
	}

	@Test
	public void testNonBlockingSubmission() throws InterruptedException {
		pi.exec("submit(sr, block=False, broker_uri='"+uri+"')");
		testLog.put("Jython command returned.");

		// Jython returns *before* scan is complete.
		assertEquals("Jython command returned.", testLog.take());
		assertEquals("Scan complete.", testLog.take());
	}

}
