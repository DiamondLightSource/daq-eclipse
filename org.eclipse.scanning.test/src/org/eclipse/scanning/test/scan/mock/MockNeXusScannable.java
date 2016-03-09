package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.sequencer.nexus.AttributeManager;

/**
 * 
 * A class to wrap any IScannable as a positioner and then write to a nexus file
 * as the positions are set during the scan.
 * 
 * @author Matthew Gerring
 *
 */
public class MockNeXusScannable extends MockScannable implements INexusDevice<NXpositioner> {
	
	public static final String FIELD_NAME_DEMAND_VALUE = NXpositioner.NX_VALUE + "_demand";
	
	private ILazyWriteableDataset lzDemand;
	private ILazyWriteableDataset lzValue;

	public MockNeXusScannable() {
		super();
	}
	
	public MockNeXusScannable(String name, double d, int i) {
		super(name, d, i);
	}

	public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) {
		DelegateNexusProvider<NXpositioner> nexusDelegate = new DelegateNexusProvider<>(
				getName(), NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE, info, this);
		nexusDelegate.setDemandDataField(FIELD_NAME_DEMAND_VALUE);
		return nexusDelegate;
	}

	@Override
	public NXpositioner createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {
		
		final NXpositioner positioner = nodeFactory.createNXpositioner();
		positioner.setNameScalar(getName());

		this.lzDemand = positioner.initializeLazyDataset(FIELD_NAME_DEMAND_VALUE, 1, Dataset.FLOAT64);
		lzDemand.setChunking(new int[]{1});
		
		this.lzValue  = positioner.initializeLazyDataset(NXpositioner.NX_VALUE, info.getRank(), Dataset.FLOAT64);
		lzValue.setChunking(info.createChunk(1)); // TODO Might be slow, need to check this

		try {
			AttributeManager.registerAttributes(positioner, this);
		} catch (Exception e) {
			e.printStackTrace(); // This is a mock, it should not do this
			throw new RuntimeException(e);
		}
		
		return positioner;
	}	

	public void setPosition(Number value, IPosition position) throws Exception {
		if (value!=null) super.setPosition(value, position);	
		if (position!=null) write(value, getPosition(), position);
	}

	private void write(Number demand, Number actual, IPosition loc) throws Exception {
		

		if (actual!=null) {
			// write actual position
			final Dataset newActualPositionData = DatasetFactory.createFromObject(actual);
			SliceND sliceND = NexusScanInfo.createLocation(lzValue, loc.getNames(), loc.getIndices()); // no varargs for scalar value
			lzValue.setSlice(null, newActualPositionData, sliceND);
		}

		if (demand!=null) {
			int index = loc.getIndex(getName());
			if (index<0) {
				throw new Exception("Incorrect data index for scan for value of '"+getName()+"'. The index is "+index);
			}
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };

			// write demand position
			final Dataset newDemandPositionData = DatasetFactory.createFromObject(demand);
			lzDemand.setSlice(null, newDemandPositionData, startPos, stopPos, null);
		}
	}

}
