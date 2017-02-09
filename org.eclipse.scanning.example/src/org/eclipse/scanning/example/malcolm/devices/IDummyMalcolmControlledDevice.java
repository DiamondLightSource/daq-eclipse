package org.eclipse.scanning.example.malcolm.devices;

import java.util.List;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;

public interface IDummyMalcolmControlledDevice {
	
	public void createNexusFile(String dirPath, int scanRank) throws NexusException;
	
	public void closeNexusFile() throws NexusException;

	public void writePosition(IPosition position) throws Exception;
	
	public String getName();
	
	public List<DummyMalcolmDatasetModel> getDatasetModels();
}