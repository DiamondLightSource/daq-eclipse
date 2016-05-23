package org.eclipse.scanning.test.event.queues;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.queues.AtomQueueService;
import org.eclipse.scanning.test.event.queues.mocks.AllBeanQueueProcessCreator;
import org.junit.Before;
import org.junit.BeforeClass;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * Test of the concrete implementation {@link AtomQueueService} of the {@link IQueueService}.
 * Tests located in {@link AbstractQueueServiceTest}.
 * @author wnm24546
 *
 */
public class AtomQueueServiceDummyTest extends AbstractQueueServiceTest {
	
	@Before
	public void createService() throws Exception {
		//Configure AtomQueueService as necessary before setting the test field
		AtomQueueService localQServ = new AtomQueueService();
		localQServ.setQueueRoot(qRoot);
		localQServ.setURI(uri);
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService());
		localQServ.setEventService(new EventServiceImpl(new ActivemqConnectorService()));
		
		qServ = localQServ;
		
		//Reset the IQueueService generic process creator
		qServ.setJobQueueProcessor(new AllBeanQueueProcessCreator<QueueBean>(true));
		qServ.setActiveQueueProcessor(new AllBeanQueueProcessCreator<QueueAtom>(true));
		
		//All set? Let's go!
		qServ.init();
	}

}
