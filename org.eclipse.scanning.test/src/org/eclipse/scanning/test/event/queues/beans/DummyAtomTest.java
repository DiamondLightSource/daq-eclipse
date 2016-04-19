package org.eclipse.scanning.test.event.queues.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.test.event.queues.mocks.DummyAtom;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.DummyQueueable;
import org.junit.Before;
import org.junit.Test;

/**
 * Test to ensure the serializability of the Dummy classes.
 * 
 * @author Michael Wharmby
 *
 */
public class DummyAtomTest {
	
	private DummyAtom beanA, beanB;
	private DummyBean beanC, beanD;
	private DummyQueueable beanE, beanF;
	
	private long timeA = 26430, timeB = 4329;
	
	private IMarshallerService jsonMarshaller;
	
	@Before
	public void buildBeans() throws Exception {
		beanA = new DummyAtom("Henry", timeA);
		beanB = new DummyAtom("Jane", timeB);
		
		beanC = new DummyBean("Henry", timeA);
		beanD = new DummyBean("Jane", timeB);
		
		beanE = new DummyQueueable("Henry", timeA);
		beanF = new DummyQueueable("Jane", timeB);
		
		jsonMarshaller = new MarshallerService();
	}
		
	@Test
	public void testAtomSerialization() throws Exception {
		String jsonA = null, jsonB = null;
		try {
			jsonA = jsonMarshaller.marshal(beanA);
		} catch(Exception e) {
			fail("Bad conversion to JSON (first bean)");
		}
		DummyAtom deSerBean = jsonMarshaller.unmarshal(jsonA, null);
		assertTrue("De-serialized bean differs from serialized", deSerBean.equals(beanA));
		
		try {
			jsonB = jsonMarshaller.marshal(beanB);
		} catch(Exception e) {
			fail("Bad conversion to JSON (second bean)");
		}
		assertFalse("JSON of beanA & beanB should differ", jsonA.equals(jsonB));
	}
	
	@Test
	public void testBeanSerialization() throws Exception {
		String jsonA = null, jsonB = null;
		try {
			jsonA = jsonMarshaller.marshal(beanC);
		} catch(Exception e) {
			fail("Bad conversion to JSON (first bean)");
		}
		DummyBean deSerBean = jsonMarshaller.unmarshal(jsonA, null);
		assertTrue("De-serialized bean differs from serialized", deSerBean.equals(beanC));
		
		try {
			jsonB = jsonMarshaller.marshal(beanD);
		} catch(Exception e) {
			fail("Bad conversion to JSON (second bean)");
		}
		assertFalse("JSON of beanA & beanB should differ", jsonA.equals(jsonB));
	}
	
	@Test
	public void testQueueableSerialization() throws Exception {
		String jsonA = null, jsonB = null;
		try {
			jsonA = jsonMarshaller.marshal(beanE);
		} catch(Exception e) {
			fail("Bad conversion to JSON (first bean)");
		}
		DummyQueueable deSerBean = jsonMarshaller.unmarshal(jsonA, null);
		assertTrue("De-serialized bean differs from serialized", deSerBean.equals(beanE));
		
		try {
			jsonB = jsonMarshaller.marshal(beanF);
		} catch(Exception e) {
			fail("Bad conversion to JSON (second bean)");
		}
		assertFalse("JSON of beanA & beanB should differ", jsonA.equals(jsonB));
	}
}
