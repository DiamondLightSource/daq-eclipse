package org.eclipse.scanning.test.annot;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.sequencer.AnnotationManager;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * Test
 * 1. Basic counts
 * 2. Inheritance
 * 3. Large call size performance per call cycle.
 * 4. Injected arguments
 * 
 * @author Matthew Gerring
 *
 */
public class AnnotationManagerTest {
	
	private AnnotationManager      manager;
	private SimpleDevice           sdevice;
	private CountingDevice         cdevice;
	private ExtendedCountingDevice edevice;
	
	@Before
	public void before() {
		manager = new AnnotationManager();
		sdevice = new SimpleDevice();
		cdevice = new CountingDevice();
		edevice = new ExtendedCountingDevice();
		manager.addDevices(sdevice, cdevice, edevice);
	}

	@Test
	public void countSimple() throws Exception {
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		manager.invoke(ScanStart.class); 
		assertEquals(sdevice.getCount(), 5);
	}
	
	@Test
	public void countInherited() throws Exception {
		
		manager.invoke(ScanStart.class); 
		for (int i = 0; i < 5; i++) cycle();
		manager.invoke(ScanEnd.class); 
		
		assertEquals(cdevice.getCount("prepareVoltages"), 1);
		assertEquals(cdevice.getCount("dispose"), 1);
		
		assertEquals(edevice.getCount("prepareVoltages"), 1);
		assertEquals(edevice.getCount("moveToNonObstructingLocation"), 1);
		assertEquals(edevice.getCount("checkNextMoveLegal"), 5);  // Points done.
		assertEquals(edevice.getCount("notifyPosition"), 5); 
		assertEquals(edevice.getCount("dispose"), 1); // 
	}
	
	private void cycle() throws Exception {
		manager.invoke(LevelStart.class); 
		manager.invoke(PointStart.class); 
		manager.invoke(PointEnd.class); 
		manager.invoke(LevelEnd.class); 
		
	}

}
