package org.eclipse.scanning.malcolm.core;

import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;

public enum MalcolmDatasetType {
	
	PRIMARY(ScanRole.DETECTOR, NexusBaseClass.NX_DETECTOR),
	SECONDARY(ScanRole.DETECTOR, NexusBaseClass.NX_DETECTOR),
	MONITOR(ScanRole.MONITOR, NexusBaseClass.NX_MONITOR),
	POSITION(ScanRole.SCANNABLE, NexusBaseClass.NX_POSITIONER);
	
	private final ScanRole scanRole;
	private final NexusBaseClass nexusBaseClass;
	
	private MalcolmDatasetType(ScanRole scanRole, NexusBaseClass nexusBaseClass) {
		this.scanRole = scanRole;
		this.nexusBaseClass = nexusBaseClass;
	}
	
	public ScanRole getScanRole() {
		return scanRole;
	}
	
	public NexusBaseClass getNexusBaseClass() {
		return nexusBaseClass;
	}

}
