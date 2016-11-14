package org.eclipse.scanning.sequencer.nexus;

import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.scanning.api.IConfigurable;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * An interface defining a nexus scan file to managed.
 */
public interface INexusScanFileManager extends IConfigurable<ScanModel> {

	/**
	 * Creates the nexus file.
	 * @throws ScanningException
	 */
	public void createNexusFile(boolean async) throws ScanningException;

	/**
	 * Flushes any pending data into the nexus file.
	 * @throws ScanningException
	 */
	public void flushNexusFile() throws ScanningException;
	
	/**
	 * Informs the manager that the scan has finished. This will
	 * cause the scanFinished dataset to be updated and the nexus file to be closed.
	 * @throws ScanningException
	 */
	public void scanFinished() throws ScanningException;
	
	/**
	 * Get the nexus scan info for the scan.
	 * @return
	 */
	public NexusScanInfo getNexusScanInfo();
	
	/**
	 * Returns whether this nexus file manager actually writes nexus, i.e.
	 * returns <code>false</code> if this is a dummy.
	 * @return
	 */
	public boolean isNexusWritingEnabled();
	
}
