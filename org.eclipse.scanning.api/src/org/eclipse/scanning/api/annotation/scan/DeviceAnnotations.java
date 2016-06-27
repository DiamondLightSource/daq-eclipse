package org.eclipse.scanning.api.annotation.scan;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class DeviceAnnotations {
	
	private static Set<Class<? extends Annotation>> annotations;
	static {
		annotations = new HashSet<>();
		annotations.add(ScanStart.class);
		annotations.add(ScanEnd.class);
		annotations.add(LevelStart.class);
		annotations.add(LevelEnd.class);
		annotations.add(PointStart.class);
		annotations.add(PointEnd.class);
		// NOTE There is no line start/end because the 9 scanning
		// does not really have a concept of a line.
	}

	public static boolean isDeviceAnnotation(Annotation annotation) {
		return annotations.contains(annotation.annotationType());
	}
}
