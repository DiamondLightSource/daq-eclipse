package org.eclipse.scanning.api.scan;

import java.util.Collection;

import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * 
 * Holds state of whole scan. May be used in annotated methods like &#64;ScanStart
 * to provide information about whole scan. Should not be used to hold transient
 * state during the scan. One should be created per run.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanInformation implements IModelProvider<ScanModel>{

	private ScanModel          model;
	private int                size;
	private int                rank;
	private Collection<String> scannableNames;

	public ScanModel getModel() {
		return model;
	}
	public void setModel(ScanModel model) {
		this.model = model;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + rank;
		result = prime * result + ((scannableNames == null) ? 0 : scannableNames.hashCode());
		result = prime * result + size;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanInformation other = (ScanInformation) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (rank != other.rank)
			return false;
		if (scannableNames == null) {
			if (other.scannableNames != null)
				return false;
		} else if (!scannableNames.equals(other.scannableNames))
			return false;
		if (size != other.size)
			return false;
		return true;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public Collection<String> getScannableNames() {
		return scannableNames;
	}
	public void setScannableNames(Collection<String> scannableNames) {
		this.scannableNames = scannableNames;
	}
}
