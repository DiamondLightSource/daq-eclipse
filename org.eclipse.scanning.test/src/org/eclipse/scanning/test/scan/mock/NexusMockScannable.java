package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.impl.NXpositionerImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * A class to wrap any IScannable as a positioner and then write to a nexus file
 * as the positions are set during the scan.
 * 
 * @author Matthew Gerring
 *
 */
public class NexusMockScannable extends MockScannable implements INexusDevice {
	
	public static final String FIELD_NAME_DEMAND_VALUE = NXpositionerImpl.NX_VALUE + "_demand";

	private DelegateNexusProvider<NXpositioner> prov;

	public NexusMockScannable() {
		super();
		prov = new DelegateNexusProvider<NXpositioner>("positioner", NexusBaseClass.NX_POSITIONER, NXpositionerImpl.NX_VALUE, this);
	}
	
	public NexusMockScannable(String name, double d, int i) {
		super(name, d, i);
	}

	@SuppressWarnings("unchecked")
	public NexusObjectProvider<NXpositioner> getNexusProvider() throws Exception {
		return prov;
	}

	@Override
	public NXpositioner createNexusObject(NexusNodeFactory nodeFactory) {
		
		final NXpositionerImpl positioner = nodeFactory.createNXpositioner();
		positioner.setNameScalar(getName());

		final int scanRank = model.getRank();
		positioner.initializeLazyDataset(FIELD_NAME_DEMAND_VALUE,   scanRank, Dataset.FLOAT64);
		positioner.initializeLazyDataset(NXpositionerImpl.NX_VALUE, scanRank, Dataset.FLOAT64);

		return positioner;
	}	

	
	public void setPosition(Number value, IPosition position) throws Exception {
		super.setPosition(value, position);	
		if (position!=null) write(value, getPosition(), position);
	}
	
	private void write(Number demand, Number actual, IPosition location) throws Exception {
		
		int index = location.getIndex(getName());
		final int[] startPos = new int[] { index };
		final int[] stopPos = new int[] { index + 1 };

		// write actual position
		final Dataset newActualPositionData = DatasetFactory.createFromObject(actual);
		prov.getDefaultDataset().setSlice(null, newActualPositionData, startPos, stopPos, null);

		// write demand position
		final Dataset newDemandPositionData = DatasetFactory.createFromObject(demand);
		prov.getWriteableDataset(FIELD_NAME_DEMAND_VALUE).setSlice(null, newDemandPositionData, startPos, stopPos, null);
	}
}
