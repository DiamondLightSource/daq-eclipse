package org.eclipse.scanning.test.scan.nexus;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.scan.IScanParticipant;
import org.eclipse.scanning.test.scan.mock.AnnotationRecorder;

/**
 * 
 * Class which counts annotations called.
 * 
 * @author Matthew Gerring
 *
 */
public class MockScanParticpiant implements AnnotationRecorder, IScanParticipant{

	
	private Map<Class<? extends Annotation>, Integer> calls = new HashMap<>();
	private List<String> paths = new ArrayList<>();
	
	public void record(Class<? extends Annotation> method) {
		if (!calls.containsKey(method)) calls.put(method, 0);
		calls.put(method, calls.get(method)+1);
	}
	
	public int getCount(Class<? extends Annotation> methodName) {
		Integer val = calls.get(methodName);
		if (val == null) return 0;
		return val;
	}

	public void clear() {
		calls.clear();
		paths.clear();
	}

	@FileDeclared
	public void addFile(String path) {
		paths.add(path);
	}

	public List<String> getPaths() {
		return paths;
	}
}
