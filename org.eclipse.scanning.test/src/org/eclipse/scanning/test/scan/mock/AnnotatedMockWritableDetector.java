package org.eclipse.scanning.test.scan.mock;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedMockWritableDetector extends MockWritableDetector implements AnnotationRecorder {
	

	public AnnotatedMockWritableDetector() {
		super();
	}
	
	public AnnotatedMockWritableDetector(String name) {
		super(name);
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
