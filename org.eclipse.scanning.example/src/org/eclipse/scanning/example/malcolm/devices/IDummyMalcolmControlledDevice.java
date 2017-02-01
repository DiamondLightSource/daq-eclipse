package org.eclipse.scanning.example.malcolm.devices;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.points.IPosition;

public interface IDummyMalcolmControlledDevice {
	
	public void createNexusFile(String dirPath, int scanRank) throws NexusException;
	
	public void closeNexusFile() throws NexusException;

	public void writePosition(IPosition position) throws Exception;
	
	public String getName();
	
}