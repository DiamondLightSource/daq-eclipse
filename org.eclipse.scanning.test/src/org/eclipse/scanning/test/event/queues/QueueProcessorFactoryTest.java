package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.event.queues.QueueProcessorFactory;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.event.queues.processors.ScanAtomProcessor;
import org.eclipse.scanning.test.event.queues.mocks.DummyAtom;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.DummyProcessor;
import org.junit.Test;

public class QueueProcessorFactoryTest {
	
	@Test
	public void testRegisterProcessor() throws EventException {
		assertEquals("Processors already registered!", 0, QueueProcessorFactory.getProcessors().size());
		
		//Register a single processor
		QueueProcessorFactory.registerProcessor(DummyProcessor.class);
		assertEquals("Processors already registered!", 1, QueueProcessorFactory.getProcessors().size());
		
//		//Register multiple processors by varargs
//		qpf.registerProcessor(MoveAtomProcessor.class, ScanAtomProcessor.class);
//		assertEquals("Processors already registered!", 3, qpf.getProcessors().size());
	}
	
//	@Test
	public void testReturnProcessorForAtom() throws EventException {
		QueueProcessorFactory.registerProcessors(MoveAtomProcessor.class, ScanAtomProcessor.class, DummyProcessor.class);//TODO add AtomQueueProcessor
		
		DummyAtom dAt = new DummyAtom("Bill", 750);
		IQueueProcessor processOne = QueueProcessorFactory.getProcessor(dAt.getClass().getName());
		if(!(processOne instanceof DummyProcessor)) fail("Wrong IProcessor type returned for DummyAtom.");
		
		DummyBean dBe = new DummyBean("Ben", 750);
		IQueueProcessor processTwo = QueueProcessorFactory.getProcessor(dBe.getClass().getName());
		if(!(processTwo instanceof DummyProcessor)) fail("Wrong IProcessor type returned for DummyBean.");
		
		MoveAtom mvAt = new MoveAtom("Sam1", "Sam1", 3, 20);
		IQueueProcessor processThree = QueueProcessorFactory.getProcessor(mvAt.getClass().getName());
		if(!(processThree instanceof MoveAtomProcessor)) fail("Wrong IProcessor type returned for MoveAtom.");
	}

}
