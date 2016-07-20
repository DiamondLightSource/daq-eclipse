package org.eclipse.scanning.test.event.queues.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.event.queues.beans.SubTaskBean;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link SubTaskBean} class, which contains a queue of QueueAtoms, 
 * which will form an active-queue when processed. This class creates the POJO. 
 * Tests themselves in {@link AbstractAtomQueueTest}. Additional test of 
 * nesting.
 * 
 * @author Michael Wharmby
 *
 */
public class SubTaskBeanTest extends AbstractAtomQueueTest<SubTaskBean, QueueAtom> {
	
	@Before
	public void buildBeans() throws Exception {
		beanA = new SubTaskBean(nameA);
		beanB = new SubTaskBean(nameB);
		
		//Create the atoms to be queued
		atomA = new DummyAtom("Hildebrand", timeA);
		atomB = new DummyAtom("Yuri", timeB);
		atomC = new DummyAtom("Ingrid", timeC);
		atomD = new DummyAtom("Arnold", timeD);
		atomE = new DummyAtom("Filipe", timeE);
		
		setupQueues();
	}
	
	/**
	 * To allow nested hierarchies, it should be possible to put a SubTaskBean 
	 * within the queue of another SubTaskBean.
	 */
	@Test
	public void testAddingSubTaskBean() throws Exception {
		SubTaskBean bean = new SubTaskBean();
		bean.queue().add(atomC);
		bean.queue().add(atomD);
		
		assertTrue(beanA.queue().add(bean));
		assertEquals(bean, beanA.queue().viewLast());
		
		//Check this bean is still serializable
		IMarshallerService jsonMarshaller = new MarshallerService();
		
		String jsonA = null;
		try {
			jsonA = jsonMarshaller.marshal(beanA);
		} catch(Exception e) {
			fail("Bad conversion to JSON (first bean)");
		}
		
		SubTaskBean deSerBean = jsonMarshaller.unmarshal(jsonA, null);
		assertTrue("De-serialized bean differs from serialized", deSerBean.equals(beanA));
	}

}
