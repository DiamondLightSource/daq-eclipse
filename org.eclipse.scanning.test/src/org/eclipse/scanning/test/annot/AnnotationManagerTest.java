package org.eclipse.scanning.test.annot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.AnnotationManager;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * Test
 * 1. Basic counts
 * 2. Inheritance
 * 3. Injected arguments
 * 4. Large call size performance per call cycle.
 * 
 * @author Matthew Gerring
 *
 */
public class AnnotationManagerTest {
	
	private AnnotationManager      manager;
	private SimpleDevice           sdevice;
	private CountingDevice         cdevice;
	private ExtendedCountingDevice edevice;
	private InjectionDevice        idevice;
	
	@Before
	public void before() {
		
		final Map<Class<?>, Object> testServices = new HashMap<>();
		testServices.put(IPointGeneratorService.class,  new PointGeneratorFactory());
		testServices.put(IDeviceConnectorService.class, new MockScannableConnector());
		testServices.put(IRunnableDeviceService.class,  new RunnableDeviceServiceImpl((IDeviceConnectorService)testServices.get(IDeviceConnectorService.class)));
		manager = new AnnotationManager(testServices);

		sdevice = new SimpleDevice();
		cdevice = new CountingDevice();
		edevice = new ExtendedCountingDevice();
		idevice = new InjectionDevice();
		manager.addDevices(sdevice, cdevice, edevice, idevice);
	}
	
	@After
	public void after() {
		manager.dispose();
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
		for (int i = 0; i < 5; i++) cycle(i);
		manager.invoke(ScanEnd.class); 
		
		assertEquals(1, cdevice.getCount("prepareVoltages"));
		assertEquals(1, cdevice.getCount("dispose"));
		assertEquals(5, edevice.getCount("prepare"));  // Points done.
		
		assertEquals(1, edevice.getCount("prepareVoltages"));
		assertEquals(1, edevice.getCount("moveToNonObstructingLocation"));
		assertEquals(5, edevice.getCount("prepare"));  // Points done.
		assertEquals(5, edevice.getCount("checkNextMoveLegal"));  // Points done.
		assertEquals(5, edevice.getCount("notifyPosition")); 
		assertEquals(1, edevice.getCount("dispose")); // 
	}
	
	private void cycle(int i) throws Exception {
		manager.invoke(LevelStart.class); 
		manager.invoke(PointStart.class, new Point(i, i*10, i, i*20)); 
		manager.invoke(PointEnd.class, new Point(i, i*10, i, i*20)); 
		manager.invoke(LevelEnd.class); 
	}
	
	@Test
	public void simpleInject() throws Exception {
		
		manager.invoke(PointStart.class, new Point(0, 10, 0, 20)); 
		assertEquals(1, edevice.getPositions().size());
		
		IPosition firstPoint = edevice.getPositions().get(0);
		if (firstPoint==null) throw new Exception("The manager failed to inject a position!");
		assertTrue(firstPoint.equals(new Point(0, 10, 0, 20)));
	}
	
	@Test
	public void somePointsInject() throws Exception {
		
		manager.invoke(ScanStart.class); 
		for (int i = 0; i < 5; i++) cycle(i);

		assertEquals(5, edevice.getPositions().size());
		for (IPosition p : edevice.getPositions()) {
			if (p==null) throw new Exception("The manager failed to inject a position!");
		}
		
		assertEquals(1, edevice.getServices().size());
		for (IRunnableDeviceService p : edevice.getServices()) {
			if (p==null) throw new Exception("The manager failed to inject a IRunnableDeviceService!");
		}
		
		manager.invoke(ScanEnd.class); 
		assertEquals(0, edevice.getPositions().size());
	}

	@Test
	public void complexInject() throws Exception {
		
		manager.invoke(PointStart.class, new Point(0, 10, 0, 20));
		checkCalls(1, idevice, "method1");
		checkCalls(1, idevice, "method2");
		checkCalls(1, idevice, "method3");
		checkCalls(1, idevice, "method4");
		checkCalls(1, idevice, "method5");
		checkCalls(1, idevice, "method6");
		
		manager.invoke(ScanEnd.class); 
		checkCalls(0, idevice, "method1");

	}
	
	@Test
	public void complexMultipleInjects() throws Exception {
		
		manager.invoke(ScanStart.class); 
		for (int i = 0; i < 5; i++) cycle(i);

		checkCalls(5, idevice, "method1");
		checkCalls(5, idevice, "method2");
		checkCalls(5, idevice, "method3");
		checkCalls(5, idevice, "method4");
		checkCalls(5, idevice, "method5");
		checkCalls(5, idevice, "method6");
		
		manager.invoke(ScanEnd.class); 
		checkCalls(0, idevice, "method1");

	}


	private void checkCalls(int size, InjectionDevice device, String methodName) {
		
		if (size<1) {
			assertTrue(device.getArguments(methodName)==null);
			return;
		}
		assertEquals(size, device.getArguments(methodName).size());
		
		Class<?>[] methodClasses = getFirstMethodArgs(device, methodName);
		for (Object[] oa : device.getArguments(methodName)) {
			assertTrue(oa.length>0);
			for (int i = 0; i < oa.length; i++) {
				assertTrue(oa[i]!=null);
				assertTrue(getClasses(oa[i]).contains(methodClasses[i]));
			}
		}
		
	}

	private Class<?>[] getFirstMethodArgs(InjectionDevice device, String methodName) {
		for (Method method : device.getClass().getMethods()) {
			if (method.getName().equals(methodName)) return method.getParameterTypes();
		}
		return null;
	}

	private Collection<Class<?>> getClasses(Object object) {
		
		final Class<?> clazz = object.getClass();
		
		final Collection<Class<?>> classes = new HashSet<>();
		classes.add(clazz);
		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> class1 : interfaces)  classes.add(class1);
		
		// TODO Currently only support one level deep
		classes.add(clazz.getSuperclass());
		interfaces = clazz.getSuperclass().getInterfaces();
		for (Class<?> class1 : interfaces)  classes.add(class1);
		
		return classes;
	}

}
