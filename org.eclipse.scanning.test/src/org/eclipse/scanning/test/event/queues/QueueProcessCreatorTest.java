package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.event.queues.QueueProcessCreator;
import org.eclipse.scanning.event.queues.beans.MonitorAtom;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.eclipse.scanning.event.queues.beans.SubTaskBean;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.eclipse.scanning.event.queues.processors.AtomQueueProcessor;
import org.eclipse.scanning.event.queues.processors.MonitorAtomProcessor;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.event.queues.processors.ScanAtomProcessor;
import org.eclipse.scanning.test.event.queues.mocks.AllBeanQueueProcessCreator;
import org.eclipse.scanning.test.event.queues.mocks.DummyAtom;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.DummyProcessor;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.util.TestAtomMaker;
import org.eclipse.scanning.test.event.queues.util.TestAtomQueueBeanMaker;
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
	
	private IProcessCreator<QueueAtom> qpcA;
	private IProcessCreator<QueueBean> qpcB;
	public Map<String, String> classMap;
	
	/**
	 * Populate map similar to that in the process creator
	 */
	@Before
	public void createClassMapping() throws Exception {
		classMap = new HashMap<>();
		classMap.put(DummyAtom.class.getSimpleName(), new DummyProcessor().makeProcess(null, null, false).getClass().getSimpleName());
		classMap.put(DummyBean.class.getSimpleName(), new DummyProcessor().makeProcess(null, null, false).getClass().getSimpleName());
		classMap.put(MonitorAtom.class.getSimpleName(), new MonitorAtomProcessor().makeProcess(null, null, false).getClass().getSimpleName());
		classMap.put(MoveAtom.class.getSimpleName(), new MoveAtomProcessor().makeProcess(null, null, false).getClass().getSimpleName());
		classMap.put(ScanAtom.class.getSimpleName(), new ScanAtomProcessor().makeProcess(makeScanAtom(), null, false).getClass().getSimpleName());
		classMap.put(SubTaskBean.class.getSimpleName(), new AtomQueueProcessor().makeProcess(null, null, false).getClass().getSimpleName());
		classMap.put(TaskBean.class.getSimpleName(), new AtomQueueProcessor().makeProcess(null, null, false).getClass().getSimpleName());
	}
	
	/**
	 * For a set of test atoms, check the correct processor is created.
	 * 
	 * Test of the Test support class {@link QueueProcessCreator}
	 * @throws EventException
	 */
	@Test
	public void testProcessTypeCreation() throws EventException {
		//Shared test variables
		boolean blocking = false;
		IPublisher<QueueAtom> pubA = new MockPublisher<QueueAtom>(null, null);
		IPublisher<QueueBean> pubB = new MockPublisher<QueueBean>(null, null);
		
		//Atoms to test
		IConsumerProcess<QueueAtom> atomProc;
		List<QueueAtom> testAtoms = new ArrayList<QueueAtom>();
		testAtoms.add(new MonitorAtom("Read bpm3", "bpm3", 10000));
		testAtoms.add(new MoveAtom("Move robot arm", "robot_arm", "1250", 12000));
		testAtoms.add(makeScanAtom());
		testAtoms.add(TestAtomQueueBeanMaker.makeDummySubTaskBeanA());
		
		qpcA = new QueueProcessCreator<QueueAtom>(blocking);
		
		//Let's try each atom in turn and see that we get the right sort of processor back.
		for (QueueAtom at : testAtoms) {
			atomProc = qpcA.createProcess(at, pubA);
			assertEquals("", classMap.get(at.getClass().getSimpleName()), atomProc.getClass().getSimpleName());
			assertEquals("Process has no publisher", pubA, atomProc.getPublisher());
		}
		
		//Atoms to test
		IConsumerProcess<QueueBean> beanProc;
		List<QueueBean> testBeans = new ArrayList<QueueBean>();
		testBeans.add(TestAtomQueueBeanMaker.makeDummyTaskBeanA());
		
		qpcB = new QueueProcessCreator<QueueBean>(blocking);
		
		//And now let's try each bean in turn and see that we get the right sort of processor back.
		for (QueueBean at : testBeans) {
			beanProc = qpcB.createProcess(at, pubB);
			assertEquals("", classMap.get(at.getClass().getSimpleName()), beanProc.getClass().getSimpleName());
			assertEquals("Process has no publisher", pubB, beanProc.getPublisher());
		}

	}
	
	/**
	 * For a set of test atoms, check the correct processor is created.
	 * 
	 * Test of the Test support class {@link AllBeanQueueProcessCreator}
	 * @throws EventException
	 */
	@Test
	public void testAllBeanProcessTypeCreation() throws EventException {
		//Shared test variables
		boolean blocking = false;
		IPublisher<QueueAtom> pubA = new MockPublisher<QueueAtom>(null, null);
		IPublisher<QueueBean> pubB = new MockPublisher<QueueBean>(null, null);
		
		//Atoms to test
		IConsumerProcess<QueueAtom> atomProc;
		List<QueueAtom> testAtoms = new ArrayList<QueueAtom>();
		testAtoms.add(new DummyAtom("Charles", 1500));
		testAtoms.add(new MoveAtom("Move robot arm", "robot_arm", "1250", 12000));
		testAtoms.add(new MonitorAtom("Read bpm3", "bpm3", 10000));
		testAtoms.add(makeScanAtom());
		testAtoms.add(TestAtomQueueBeanMaker.makeDummySubTaskBeanA());
		
		qpcA = new AllBeanQueueProcessCreator<QueueAtom>(blocking);
		
		//Let's try each atom in turn and see that we get the right sort of processor back.
		for (QueueAtom at : testAtoms) {
			atomProc = qpcA.createProcess(at, pubA);
			assertEquals("", classMap.get(at.getClass().getSimpleName()), atomProc.getClass().getSimpleName());
			assertEquals("Process has no publisher", pubA, atomProc.getPublisher());
		}
		
		//Atoms to test
		IConsumerProcess<QueueBean> beanProc;
		List<QueueBean> testBeans = new ArrayList<QueueBean>();
		testBeans.add(new DummyBean("Andrew", 3000));
		testBeans.add(TestAtomQueueBeanMaker.makeDummyTaskBeanA());
		
		qpcB = new AllBeanQueueProcessCreator<QueueBean>(blocking);
		
		//And now let's try each bean in turn and see that we get the right sort of processor back.
		for (QueueBean at : testBeans) {
			beanProc = qpcB.createProcess(at, pubB);
			assertEquals("", classMap.get(at.getClass().getSimpleName()), beanProc.getClass().getSimpleName());
			assertEquals("Process has no publisher", pubB, beanProc.getPublisher());
		}

	}
	
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
		
		ScanAtom scAt = new ScanAtom("VT scan across sample", scanAxes, detectors);

		
		scAt.setScanConsumerURI("tcp://localhost:8624");
		scAt.setScanSubmitQueueName(IEventService.SUBMISSION_QUEUE);
		scAt.setScanStatusQueueName(IEventService.STATUS_SET);
		scAt.setScanStatusTopicName(IEventService.STATUS_TOPIC);
		return scAt;
	}

}
