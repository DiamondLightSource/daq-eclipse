package org.eclipse.scanning.test.scan.mock;

import java.lang.annotation.Annotation;

import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFault;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanPause;
import org.eclipse.scanning.api.annotation.scan.ScanResume;
import org.eclipse.scanning.api.annotation.scan.ScanStart;

/**
 *
 * Yes should use Mockito but that has the issues with
 * hamcrest which are hard to resolve in the build.
 * 
 * This class is a handy way to do multiple inheritance in Java by defaulting
 * methods, filthy I know.
 * 
 * @author Matthew Gerring
 *
 */
public interface AnnotationRecorder {

 
	@ScanStart
	default void scanStart() {
		record(ScanStart.class);
	}
	
	@ScanEnd
	default void scanEnd() {
		record(ScanEnd.class);
	}

	@PointStart
	default void pointStart() {
		record(PointStart.class);
	}
	
	@PointEnd
	default void pointEnd() {
		record(PointEnd.class);
	}

	@LevelStart
	default void levelStart() {
		record(LevelStart.class);
	}
	
	@LevelEnd
	default void levelEnd() {
		record(LevelEnd.class);
	}

	@ScanPause
	default void scanPause() {
		record(ScanPause.class);
	}
	
	@ScanResume
	default void scanResume() {
		record(ScanResume.class);
	}

	@ScanAbort
	default void scanAbort() {
		record(ScanAbort.class);
	}
	
	@ScanFault
	default void scanFault() {
		record(ScanFault.class);
	}
	
	@ScanFinally
	default void scanFinally() {
		record(ScanFinally.class);
	}

	void record(Class<? extends Annotation> method);
	
	int getCount(Class<? extends Annotation> methodName);
}
