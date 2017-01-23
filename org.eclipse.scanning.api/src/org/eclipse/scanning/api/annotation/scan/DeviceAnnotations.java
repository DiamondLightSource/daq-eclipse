package org.eclipse.scanning.api.annotation.scan;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DeviceAnnotations {
	
	private static final Set<Class<? extends Annotation>> annotations;
	static {
		Set<Class<? extends Annotation>> tmp = new HashSet<>();
		
		// Alphabetic order
		tmp.add(LevelEnd.class);
		tmp.add(LevelStart.class);
		tmp.add(WriteComplete.class);
		tmp.add(PointEnd.class);
		tmp.add(PointStart.class);
		tmp.add(ScanAbort.class);
		tmp.add(ScanEnd.class);
		tmp.add(ScanFault.class);
		tmp.add(ScanFinally.class);
		tmp.add(ScanPause.class);
		tmp.add(ScanResume.class);
		tmp.add(ScanStart.class);
		tmp.add(PreConfigure.class);
		tmp.add(PostConfigure.class);
		
		annotations = Collections.unmodifiableSet(tmp);
		
		// NOTE There is no line start/end because the 9 scanning
		// does not really have a concept of a line.
	}

	public static Collection<Class<? extends Annotation>> getAllAnnotations() {
		return annotations;
	}
}
