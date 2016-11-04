package org.eclipse.scanning.sequencer.nexus;

import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.ServiceHolder;

public class NexusScanFileManagerFactory {
	
	private static class DummyNexusScanFileManager implements INexusScanFileManager {

		@Override
		public void configure(ScanModel model) throws ScanningException {
			// do nothing
		}

		@Override
		public void createNexusFile(boolean async) throws ScanningException {
			// do nothing
		}

		@Override
		public void flushNexusFile() throws ScanningException {
			// do nothing
		}

		@Override
		public void scanFinished() throws ScanningException {
			// do nothing
		}

		@Override
		public boolean isNexusWritingEnabled() {
			return false;
		}

	}
	
	public static INexusScanFileManager createNexusScanFileManager(
			AbstractRunnableDevice<ScanModel> scanDevice) {
		if (scanDevice.getModel().getFilePath() == null || ServiceHolder.getFactory() == null) {
			return new DummyNexusScanFileManager();
		}
		
		return new NexusScanFileManager(scanDevice);
	}

}
