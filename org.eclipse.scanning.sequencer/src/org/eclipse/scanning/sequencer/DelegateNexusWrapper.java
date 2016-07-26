package org.eclipse.scanning.sequencer;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;

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
	
	@ScanFinally
	public void clean() {
		lzDemand = null;
		lzValue  = null;
	}

	public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) {
		// FIXME the AxisModel should be used here to work out axes if it is non-null
		
		final NXpositioner positioner = NexusNodeFactory.createNXpositioner();
		positioner.setNameScalar(scannable.getName());

		this.lzDemand = positioner.initializeLazyDataset(FIELD_NAME_DEMAND_VALUE, 1, Double.class);
		lzDemand.setChunking(new int[]{1});
		
		this.lzValue  = positioner.initializeLazyDataset(NXpositioner.NX_VALUE, info.getRank(), Double.class);
		lzValue.setChunking(info.createChunk(1)); // TODO Might be slow, need to check this

		return new NexusObjectWrapper<NXpositioner>(scannable.getName(), positioner, NXpositioner.NX_VALUE);
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
			IScanSlice rslice = IScanRankService.getScanRankService().createScanSlice(loc);
			SliceND sliceND = new SliceND(lzValue.getShape(), lzValue.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
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
