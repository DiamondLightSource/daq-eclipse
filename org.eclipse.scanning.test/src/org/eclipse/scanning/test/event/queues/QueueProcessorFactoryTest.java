package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.event.queues.QueueProcessorFactory;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtomProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.dummy.DummyBeanProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueueProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyProcessor;
import org.junit.Before;
import org.junit.Test;

public class QueueProcessorFactoryTest {
	
	@Before
	public void setUp() {
		QueueProcessorFactory.initialize();
	}
	
	@Test
	public void testRegisterProcessor() throws EventException {
		assertEquals("Processors already registered!", 2, QueueProcessorFactory.getProcessors().size());
		
		//Register a single processor
		QueueProcessorFactory.registerProcessor(DummyAtomProcessor.class);
		assertEquals("Processors already registered!", 3, QueueProcessorFactory.getProcessors().size());
		
		//Register multiple processors by varargs
		QueueProcessorFactory.registerProcessors(DummyBeanProcessor.class, DummyHasQueueProcessor.class);
		assertEquals("Processors already registered!", 5, QueueProcessorFactory.getProcessors().size());
	}
	
	@Test
	public void testReturnProcessorForAtom() throws EventException {
		QueueProcessorFactory.registerProcessors(DummyAtomProcessor.class, DummyBeanProcessor.class);
		
		DummyAtom dAt = new DummyAtom("Bill", 750);
		IQueueProcessor<?> processOne = QueueProcessorFactory.getProcessor(dAt.getClass().getName());
		if(!(processOne instanceof DummyProcessor)) fail("Wrong IProcessor type returned for DummyAtom.");
		
		DummyBean dBe = new DummyBean("Ben", 750);
		IQueueProcessor<?> processTwo = QueueProcessorFactory.getProcessor(dBe.getClass().getName());
		if(!(processTwo instanceof DummyProcessor)) fail("Wrong IProcessor type returned for DummyBean.");
		
		MoveAtom mvAt = new MoveAtom("Sam1", "Sam1", 3, 20);
		IQueueProcessor<?> processThree = QueueProcessorFactory.getProcessor(mvAt.getClass().getName());
		if(!(processThree instanceof MoveAtomProcessor)) fail("Wrong IProcessor type returned for MoveAtom.");
		
		try {
			DummyHasQueue dHQ = new DummyHasQueue("Dilbert", 25);
			@SuppressWarnings("unused")
			IQueueProcessor<?> processFour = QueueProcessorFactory.getProcessor(dHQ.getClass().getName());
			fail("DummyHasQueue not registered, should not be able to get processor!");
		} catch (EventException evEx) {
			//Expected
		}
	}

}
