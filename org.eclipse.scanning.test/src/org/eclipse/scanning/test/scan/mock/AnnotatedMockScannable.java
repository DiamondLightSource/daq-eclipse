package org.eclipse.scanning.test.scan.mock;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Should probably use Mockito
 * @author fcp94556
 *
 */
public class AnnotatedMockScannable extends MockScannable implements AnnotationRecorder {
	

	public AnnotatedMockScannable(String name, double value) {
		super(name, value);
	}
	
	private Map<Class<? extends Annotation>, Integer> calls = new HashMap<>();
	public void record(Class<? extends Annotation> method) {
		if (!calls.containsKey(method)) calls.put(method, 0);
		calls.put(method, calls.get(method)+1);
	}
	
	public int getCount(Class<? extends Annotation> methodName) {
		Integer val = calls.get(methodName);
		if (val == null) return 0;
		return val;
	}

}
