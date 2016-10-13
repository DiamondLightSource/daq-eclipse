package org.eclipse.scanning.test.event.queues.beans;

import org.eclipse.scanning.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.eclipse.scanning.test.event.queues.util.TestAtomQueueBeanMaker;
import org.junit.Before;

/**
 * Test the {@link TaskBean} class, which contains a queue of QueueAtoms, which
 * will form an active-queue when processed. This class only create the POJO.
 * Tests themselves in {@link AbstractAtomQueueTest}.
 * 
 * @author Michael Wharmby
 *
 */
public class TaskBeanTest extends AbstractAtomQueueTest<TaskBean, SubTaskAtom> {
	
	@Before
	public void buildBeans() throws Exception {
		beanA = new TaskBean(nameA);
		beanB = new TaskBean(nameA);
		
		//Create atoms to be queued
		atomA = TestAtomQueueBeanMaker.makeDummySubTaskBeanA();
		atomB = TestAtomQueueBeanMaker.makeDummySubTaskBeanB();
		atomC = TestAtomQueueBeanMaker.makeDummySubTaskBeanC();
		atomD = TestAtomQueueBeanMaker.makeDummySubTaskBeanD();
		atomE = TestAtomQueueBeanMaker.makeDummySubTaskBeanE();
		
		setupQueues();	
	}
	
}
