package org.eclipse.scanning.api.annotation.scan;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class DeviceAnnotations {
	
	private static Set<Class<? extends Annotation>> annotations;
	static {
		annotations = new HashSet<>();
		annotations.add(ScanStart.class);
	}

	public static boolean isDeviceAnnotation(Annotation annotation) {
		return annotations.contains(annotation.annotationType());
	}
}
