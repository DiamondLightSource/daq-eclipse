package org.eclipse.scanning.api.annotation.scan;

import java.io.PrintStream;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelInformation;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * 
 * This class is an interface which defaults all the annotated methods
 * which a device may use when 
 * 
 * @author Matthew Gerring
 *
 */
public interface AnnotatedDevice {
	
	default boolean isDebug() {
		return false;
	}
	
	default PrintStream getStream() {
		return System.out;
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@PreConfigure
	default void preConfigure(Object model) throws ScanningException {
		if (isDebug()) getStream().println("Pre-configure of "+model);
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@PostConfigure
	default void postConfogure(Object model) throws ScanningException {
		if (isDebug()) getStream().println("Post-configure of "+model);
	}


	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@LevelStart
    default void levelStart(LevelInformation info)  throws ScanningException { // Other arguments are allowed
		if (isDebug()) getStream().println("Level start and level is "+info.getLevel());
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@LevelEnd
    default void levelEnd(LevelInformation info) throws ScanningException { // Other arguments are allowed
		if (isDebug()) getStream().println("Level end and level is "+info.getLevel());
	}

	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@PointStart
	default void pointStart(IPosition point) throws ScanningException {
		if (isDebug()) getStream().println("Point start "+point.getStepIndex());
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@PointEnd
	default void pointEnd(IPosition point) throws ScanningException {
		if (isDebug()) getStream().println("Point end "+point.getStepIndex());
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@ScanStart
	default void scanStart(ScanInformation info) throws ScanningException {
		if (isDebug()) getStream().println("Scan start "+info.getFilePath());
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@ScanEnd
	default void scanEnd(ScanInformation info) throws ScanningException {
		if (isDebug()) getStream().println("Scan end "+info.getFilePath());
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@ScanAbort
	default void scanAbort(ScanInformation info) throws ScanningException {
		if (isDebug()) getStream().println("Scan aborted");
	}

	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@ScanFault
	default void scanFault(ScanInformation info) throws ScanningException {
		if (isDebug()) getStream().println("Scan fault");
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@ScanFinally
	default void scanFinally(ScanInformation info) throws ScanningException {
		if (isDebug()) getStream().println("Scan finally");
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@ScanPause
	default void scanPaused() throws ScanningException {
		if (isDebug()) getStream().println("Scan paused");
	}
	
	/**
	 * Implement default method for use with Jython.
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@ScanResume
	default void scanResumed() throws ScanningException {
		if (isDebug()) getStream().println("Scan resumed");
	}

}
