package org.eclipse.scanning.test.scan.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ScannableModel;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.PositionDelegate;

public class MockScannable implements IScannable<Number>, IPositionListenable {

	private int     level;
	private String  name;
	private Number  position = 0d;
	private boolean requireSleep=true;

	private List<Number>            values;
	private List<AbstractPosition>  positions;
	
	protected ScannableModel     model;
	private LazyWriteableDataset writer;
	private PositionDelegate     delegate;
	
    public MockScannable() {
       	values    = new ArrayList<>();
       	positions = new ArrayList<>();
       	delegate  = new PositionDelegate();
    }
    public MockScannable(double position) {
    	this();
    	this.position = position;
    }
	public MockScannable(String name, double position) {
    	this();
		this.name = name;
		this.position = position;
	}
	
	public MockScannable(String name, Double position, int level) {
    	this();
		this.level = level;
		this.name = name;
		this.position = position;
	}
	public MockScannable(String name, Double position, int level, String unit) {
    	this();
		this.level = level;
		this.name = name;
		this.position = position;
		this.unit = unit;
	}
	
	@Override
	public void configure(ScannableModel model) throws ScanningException {
		this.model = model;
		
		if (model instanceof MockScannableModel) {
			MockScannableModel mod = (MockScannableModel)model;
			
			// We make a lazy writeable dataset to write out the mandels.
			final int[] shape = new int[]{mod.getSize()};
			
			try {
				/**
				 * @see org.eclipse.dawnsci.nexus.NexusFileTest.testLazyWriteStringArray()
				 
				  TODO FIXME Hack warning! This is not the way to write to NeXus.
				  We are just doing this for the test!
				  
				  DO NOT COPY!
				*/
				NexusFile file = mod.getFile();			
				GroupNode par = file.getGroup("/entry1/instrument/axes", true); // DO NOT COPY!
				writer = new LazyWriteableDataset(getName(), Dataset.FLOAT, shape, shape, shape, null); // DO NOT COPY!
			
				file.createData(par, writer); // DO NOT COPY!

			} catch (NexusException ne) {
				throw new ScanningException("Cannot open file for writing!", ne);
			}
	 		
		} else {
			writer = null;
		}

	}

	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Number getPosition() {
		return position;
	}
	public void setPosition(Number position) throws Exception {
		setPosition(position, null);
	}
	
	public void setPosition(Number position, IPosition loc) throws Exception {
		
		double value = position!=null ? position.doubleValue() : Double.NaN;
		boolean ok = delegate.firePositionWillPerform(new Scalar(getName(), loc.getIndex(getName()), value));
		if (!ok) return;
		
		if (requireSleep && position!=null) {
			long time = Math.abs(Math.round((position.doubleValue()-this.position.doubleValue())/1)*100);
			time = Math.max(time, 1);
			Thread.sleep(time);
		}
		this.position = position;
		
		values.add(position);
		positions.add((AbstractPosition)loc);
		
		
		if (writer!=null && loc!=null) {
			
			// Write a single value
			IDataset toWrite = DatasetFactory.createFromObject(position);
			
			final int[] start = new int[]{loc.getIndex(getName())};    // DO NOT COPY!
			final int[] stop  = new int[] {loc.getIndex(getName())+1}; // DO NOT COPY!
			
			SliceND slice = SliceND.createSlice(writer, start, stop); // DO NOT COPY!
			try {
				writer.setSlice(new IMonitor.Stub(), toWrite, slice); // DO NOT COPY!
			} catch (Exception e) {
				throw new ScanningException("Slice unable to write!", e); // DO NOT COPY!
			}
	 
		}
		
		delegate.firePositionPerformed(-1, new Scalar(getName(), loc.getIndex(getName()), value));

	}
	
	@Override
	public String toString() {
		return "MockScannable [level=" + level + ", name=" + name
				+ ", position=" + position + "]";
	}
	public boolean isRequireSleep() {
		return requireSleep;
	}
	public void setRequireSleep(boolean requireSleep) {
		this.requireSleep = requireSleep;
	}
	
	public void verify(Number value, IPosition point) throws Exception {
		
		for (int i = 0; i < positions.size(); i++) {
			
			if (positions.get(i).equals(point, false)  && ( 
				values.get(i) == value || values.get(i).equals(value))) {
				return;
			}
		}
		
		throw new Exception("Not call to setPosition had value="+value+" and position="+point);
	}
	
	@Override
	public void addPositionListener(IPositionListener listener) {
		delegate.addPositionListener(listener);
	}
	@Override
	public void removePositionListener(IPositionListener listener) {
		delegate.removePositionListener(listener);
	}
	
	private String unit = "mm";
	@Override
	public String getUnit() {
		return unit;
	}

    public void setUnit(String unit) {
    	this.unit = unit;
    }
}
