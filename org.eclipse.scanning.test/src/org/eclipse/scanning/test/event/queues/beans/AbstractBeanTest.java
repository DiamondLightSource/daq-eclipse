package org.eclipse.scanning.test.event.queues.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;
import org.junit.Test;

/**
 * Test the that given POJO is correctly serialised by the JSON 
 * {@link IMarshaller}.
 * 
 * @author Michael Wharmby
 *
 * @param <S> POJO type to be serialised.
 */
public abstract class AbstractBeanTest<S extends IQueueable> {
	
	protected S beanA, beanB;
	
	@Test
	public void testSerialization() throws Exception {
		IMarshallerService jsonMarshaller = new MarshallerService();
		
		String jsonA = null, jsonB = null;
		try {
			jsonA = jsonMarshaller.marshal(beanA);
		} catch(Exception e) {
			fail("Bad conversion to JSON (first bean)");
		}
		S deSerBean = jsonMarshaller.unmarshal(jsonA, null);
		assertTrue("De-serialized bean differs from serialized", deSerBean.equals(beanA));
		
		try {
			jsonB = jsonMarshaller.marshal(beanB);
		} catch(Exception e) {
			fail("Bad conversion to JSON (second bean)");
		}
		assertFalse("JSON of beanA & beanB should differ", jsonA.equals(jsonB));
	}

}
