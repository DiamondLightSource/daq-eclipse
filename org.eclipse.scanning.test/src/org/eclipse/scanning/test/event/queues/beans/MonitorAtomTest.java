package org.eclipse.scanning.test.event.queues.beans;

import org.eclipse.scanning.event.queues.beans.MonitorAtom;
import org.junit.Before;

/**
 * Test for the {@link MonitorAtom} class. This class only create the POJO.
 * Actual tests in {@link AbstractBeanTest}.
 * 
 * @author Michael Wharmby
 *
 */
public class MonitorAtomTest extends AbstractBeanTest<MonitorAtom> {
	
	private String nameA = "testMonitorA", nameB = "testMonitorB";
	private String deviceA = "testMonDeviceA", deviceB = "testMonDeviceB";
	private long timeA = 26430, timeB = 4329;
	
	@Before
	public void buildBeans() throws Exception {
		beanA = new MonitorAtom(nameA, deviceA, timeA);
		beanB = new MonitorAtom(nameB, deviceB, timeB);
	}
}
