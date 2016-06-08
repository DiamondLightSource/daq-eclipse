package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.event.queues.QueueProcessCreator;
import org.eclipse.scanning.event.queues.QueueProcessorFactory;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtomProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.dummy.DummyBeanProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueueProcessor;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link QueueProcessCreator} class, which selects the process class to 
 * create based on atom/bean type.
 * This test also tests the {@link AllBeanQueueProcessCreator} class, which 
 * also has Dummy types.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueProcessCreatorTest {
	
	private IProcessCreator<Queueable> qpc;
	private IQueueProcess<Queueable> qProc;
	
	//Infrastructure
	private IPublisher<Queueable> statPub;
	
	/**
	 * Populate map similar to that in the process creator
	 */
	@Before
	public void setUp() throws Exception {
		
		qpc = new QueueProcessCreator<>(true);
		
		statPub = new MockPublisher<Queueable>(null, "test.topic");
		QueueProcessorFactory.initialize();
	}

	/**
	 * For a set of test atoms, check the correct processor is created.
	 * 
	 * Test of the Test support class {@link QueueProcessCreator}
	 * @throws EventException
	**/
	@Test
	public void testProcessTypeCreation() throws Exception {
		//Atoms to test
		List<Queueable> testAtoms = new ArrayList<>();
//		testAtoms.add(new MonitorAtom("Read bpm3", "bpm3", 10000)); FIXME
		testAtoms.add(new MoveAtom("Move robot arm", "robot_arm", "1250", 12000));
		testAtoms.add(makeScanAtom());
//		testAtoms.add(new ProcessAtom(...) TODO
//		testAtoms.add(TestAtomQueueBeanMaker.makeDummySubTaskBeanA()); FIXME
//		testAtoms.add(TestAtomQueueBeanMaker.makeDummyTaskBeanA()); FIXME
		
		
		IQueueProcessor<? extends Queueable> qProcr;
		for (Queueable atom : testAtoms) {
			qProc = (IQueueProcess<Queueable>) qpc.createProcess(atom, statPub);
			qProcr = qProc.getProcessor();
			
			assertEquals("Class of test atom & processor do not match", atom.getClass(), qProcr.getBeanClass());
			assertEquals("Process has different publisher associated", statPub, qProc.getPublisher());
		}
		
		QueueProcessorFactory.registerProcessors(DummyAtomProcessor.class, DummyBeanProcessor.class, DummyHasQueueProcessor.class);
		
		List<Queueable> dummyAtoms = new ArrayList<>();
		dummyAtoms.add(new DummyAtom("Marvin", 10));
		dummyAtoms.add(new DummyBean("Doris", 20));
		dummyAtoms.add(new DummyHasQueue("Delores", 30));
		
		for (Queueable atom : testAtoms) {
			qProc = (IQueueProcess<Queueable>) qpc.createProcess(atom, statPub);
			qProcr = qProc.getProcessor();
			
			assertEquals("Class of test atom & processor do not match", atom.getClass(), qProcr.getBeanClass());
			assertEquals("The atom to be processed differs from the input", atom, qProcr.getProcessBean());
			assertEquals("Queue broadcaster differs from the expected configuration", (IQueueBroadcaster<Queueable>)qProc, qProcr.getQueueBroadcaster());
			
			assertEquals("Publisher differs from the expect configuration", statPub, qProc.getPublisher());
		}
	}
	
	@Test
	public void testNoRegisteredProcessorForBean() throws Exception {
		DummyAtom dAt = new DummyAtom("Marvin", 10);
		
		try {
			qProc = (IQueueProcess<Queueable>) qpc.createProcess(dAt, statPub);
			fail("Should not be able to create QueueProcess for unregistered bean class");
		} catch (EventException evEx) {
			//Expected
		}
	}
	
	//TODO Check blocking persisted
	//TODO Setting blocking on a per-processor basis.
	
	private ScanAtom makeScanAtom() {
		//ScanAtomProcessor needs a ScanAtom with queue names etc.
		List<IScanPathModel> scanAxes = new ArrayList<>();
		scanAxes.add(new StepModel("ocs", 290, 80, 10));
		scanAxes.add(new StepModel("xMotor", 150, 100, 5));
		
		Map<String, Object> detectors = new HashMap<>();
		detectors.put("pe", new MockDetectorModel(30d));
		
		List<String> monitors = new ArrayList<>();
		monitors.add("bpm3");
		monitors.add("i0");
		
		ScanAtom scAt = new ScanAtom("VT scan across sample", scanAxes, detectors, monitors);

		scAt.setScanBrokerURI("tcp://localhost:8624");
		scAt.setScanSubmitQueueName(IEventService.SUBMISSION_QUEUE);
		scAt.setScanStatusTopicName(IEventService.STATUS_TOPIC);
		return scAt;
	}

}
