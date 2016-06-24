package org.eclipse.scanning.test.annot;

import static org.junit.Assert.assertEquals;

import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.sequencer.AnnotationManager;
import org.junit.Before;
import org.junit.Test;

public class AnnotationManagerTest {
	
	private AnnotationManager manager;
	private SimpleDevice      sdevice;
	
	@Before
	public void before() {
		manager = new AnnotationManager();
		sdevice = new SimpleDevice();
		manager.addDevices(sdevice);
	}

	@Test
	public void countTest() throws Exception {
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		assertEquals(sdevice.getCount(), 5);
	}
}
