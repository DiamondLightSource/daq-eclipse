package org.eclipse.scanning.api.scan;

/**
 * 
 * Any class wishing to have annotated methods called during
 * a scan may implement this interface and then export the
 * class as an OSGi service.
 * 
 * The Scanning System will get all objects declared as implementing
 * this interface from OSGi and include the devices in the scan.
 * Any methods then implemented in the device will be called where they
 * are annotated as the scan expects.
 * 
 * One example of this is archiving. The archiver implements IScanParticipant
 * and is then included in the scan. As the files are declared as being part
 * of the scan, the @FileDeclared annotation is called, at the end of the
 * scan @ScanEnd is called, finally @ScanFinally is called to close any resources
 * or reset the file list because there is no more a scan!
 * 
 * @author Matthew Gerring
 *
 */
public interface IScanParticipant {

}
