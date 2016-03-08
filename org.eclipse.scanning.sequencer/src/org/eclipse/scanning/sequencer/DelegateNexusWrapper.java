package org.eclipse.scanning.sequencer;

import org.eclipse.dawnsci.analysis.api.dataset.Dtype;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;

/**
 * Class provides aq default implementation which will write any
 * scannable to NeXus
 * 
 * @author Matthew Gerring
 *
 */
class DelegateNexusWrapper extends AbstractScannable<Object> implements INexusDevice<NXpositioner> {
	
	public static final String FIELD_NAME_DEMAND_VALUE = NXpositioner.NX_VALUE + "_demand";

	private IScannable<Object>    scannable;
	private ILazyWriteableDataset lzDemand;
	private ILazyWriteableDataset lzValue;

	DelegateNexusWrapper(IScannable<Object> scannable) {
		this.scannable = scannable;
		
	}

	@SuppressWarnings("unchecked")
	public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) {
		return new DelegateNexusProvider<NXpositioner>(scannable.getName(), NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE, info, this);
	}

	@Override
	public NXpositioner createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {
		
		// FIXME the AxisModel should be used here to work out axes if it is non-null
		
		final NXpositioner positioner = nodeFactory.createNXpositioner();
		positioner.setNameScalar(scannable.getName());

		this.lzDemand = positioner.initializeLazyDataset(FIELD_NAME_DEMAND_VALUE, 1, Dtype.FLOAT64);
		lzDemand.setChunking(new int[]{1});
		
		this.lzValue  = positioner.initializeLazyDataset(NXpositioner.NX_VALUE, info.getRank(), Dtype.FLOAT64);
		lzValue.setChunking(info.createChunk(1)); // TODO Might be slow, need to check this

		return positioner;
	}

	@Override
	public void setLevel(int level) {
		scannable.setLevel(level);
	}

	@Override
	public int getLevel() {
		return scannable.getLevel();
	}

	@Override
	public String getName() {
		return scannable.getName();
	}

	@Override
	public void setName(String name) {
		scannable.setName(name);
	}

	@Override
	public Object getPosition() throws Exception {
		return scannable.getPosition();
	}

	@Override
	public void setPosition(Object value) throws Exception {
		scannable.setPosition(value);
	}

	@Override
	public void setPosition(Object value, IPosition position) throws Exception {
		scannable.setPosition(value, position);
		if (position!=null) write(value, getPosition(), position);
	}
	
	private void write(Object demand, Object actual, IPosition loc) throws Exception {
		

		if (actual!=null) {
			// write actual position
			final IDataset newActualPositionData = DatasetFactory.createFromObject(actual);
			SliceND sliceND = NexusScanInfo.createLocation(lzValue, loc.getNames(), loc.getIndices());
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
			final IDataset newDemandPositionData = DatasetFactory.createFromObject(demand);
			lzDemand.setSlice(null, newDemandPositionData, startPos, stopPos, null);
		}
	}

	@Override
	public String getUnit() {
		return scannable.getUnit();
	}

}
