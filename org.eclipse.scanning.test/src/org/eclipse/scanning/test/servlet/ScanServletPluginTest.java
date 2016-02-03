package org.eclipse.scanning.test.servlet;

import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScanServletPluginTest {

	
	private ScanServlet servlet; 
	
	@BeforeClass
	public void createServlet()  throws Exception {
		
		/**
		 *  This would be done by spring on the GDA Server
		 *  @see org.eclipse.scanning.server.servlet.AbstractConsumerServlet
		 */
		servlet = new ScanServlet();
		servlet.setBroker("vm://localhost?broker.persistent=false");
		servlet.setSubmitQueue("org.eclipse.scanning.test.servlet.submitQueue");
		servlet.setStatusSet("org.eclipse.scanning.test.servlet.statusSet");
		servlet.setStatusTopic("org.eclipse.scanning.test.servlet.statusTopic");
		servlet.connect(); // Gets called by Spring @PostConstruct
	}
	
	@Test
	public void testAScan() {
		
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");
		
	}
}
