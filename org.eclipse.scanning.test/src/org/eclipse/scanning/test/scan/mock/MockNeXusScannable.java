package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
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
	
	public MockNeXusScannable(String name, double d, int level) {
		super(name, d, level);
	}

	public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXpositioner positioner = NexusNodeFactory.createNXpositioner();
		positioner.setNameScalar(getName());

		if (info.isMetadataScannable(getName())) {
			positioner.setField(FIELD_NAME_DEMAND_VALUE, getPosition().doubleValue());
			positioner.setValueScalar(getPosition().doubleValue());
		} else {
			this.lzDemand = positioner.initializeLazyDataset(FIELD_NAME_DEMAND_VALUE, 1, Dataset.FLOAT64);
			lzDemand.setChunking(new int[]{1});
			
			this.lzValue  = positioner.initializeLazyDataset(NXpositioner.NX_VALUE, info.getRank(), Dataset.FLOAT64);
			lzValue.setChunking(info.createChunk(1)); // TODO Might be slow, need to check this
		}

		AttributeManager.registerAttributes(positioner, this);
		
		NexusObjectWrapper<NXpositioner> nexusDelegate = new NexusObjectWrapper<>(
				getName(), positioner, NXpositioner.NX_VALUE);
		nexusDelegate.setDefaultAxisDataFieldName(FIELD_NAME_DEMAND_VALUE);
		return nexusDelegate;
	}	

	public void setPosition(Number value, IPosition position) throws Exception {
        //if (value!=null) super.setPosition(value, position);	
		if (position!=null) write(value, getPosition(), position);
	}

	private void write(Number demand, Number actual, IPosition loc) throws Exception {
		
		if (lzValue==null) return;
		if (actual!=null) {
			// write actual position
			final Dataset newActualPositionData = DatasetFactory.createFromObject(actual);
			IScanSlice rslice = IScanRankService.getScanRankService().createScanSlice(loc);
			SliceND sliceND = new SliceND(lzValue.getShape(), lzValue.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
			lzValue.setSlice(null, newActualPositionData, sliceND);
		}

		if (lzDemand==null) return;
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
