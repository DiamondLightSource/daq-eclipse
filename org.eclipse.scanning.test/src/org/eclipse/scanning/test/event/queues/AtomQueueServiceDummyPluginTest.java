package org.eclipse.scanning.test.event.queues;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.event.queues.AtomQueueService;
import org.eclipse.scanning.test.event.queues.mocks.AllBeanQueueProcessCreator;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Test of the concrete implementation {@link AtomQueueService} of the {@link IQueueService}.
 * Tests located in {@link AbstractQueueServiceTest}.
 * 
 * This test uses OSGi.
 * 
 * @author Michael Wharmby
 *
 */
public class AtomQueueServiceDummyPluginTest extends AbstractQueueServiceTest {
			
	private static IQueueService queueService;

	public static IQueueService getQueueService() {
		return queueService;
	}

	public synchronized void setQueueService(IQueueService queueService) {
		AtomQueueServiceDummyPluginTest.queueService = queueService;
	}
	
	public synchronized void unsetQueueService() {
		queueService = null;
	}

	@BeforeClass
	public static void setupClass() throws URISyntaxException {
		qRoot = "uk.ac.diamond.i15-1";
		uri = new URI("vm://localhost?broker.persistent=false");
	}
	
	@Before
	public void createService() throws Exception {
		qServ = AtomQueueServiceDummyPluginTest.queueService;
		
		//Configure IQueueService as necessary using OSGi
		qServ.setQueueRoot(qRoot);
		qServ.setURI(uri);
//		qServ.setEventService(AtomQueueServiceDummyPluginTest.eventService);
		
		//Reset the IQueueService generic process creator
		qServ.setJobQueueProcessor(new AllBeanQueueProcessCreator<QueueBean>(true));
		qServ.setActiveQueueProcessor(new AllBeanQueueProcessCreator<QueueAtom>(true));
		
		//All set? Let's go!
		qServ.init();
	}

}
