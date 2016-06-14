package org.eclipse.scanning.sequencer.nexus;

import java.util.Map;

import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.data.AxisDataDevice;

public class NexusScanFileDataBuilder {
	
	public Map<NexusObjectProvider<?>, AxisDataDevice<?>> dataDevices;

	public NexusScanFileDataBuilder(Map<NexusObjectProvider<?>, AxisDataDevice<?>> dataDevices) {
		this.dataDevices = dataDevices;
	}
}