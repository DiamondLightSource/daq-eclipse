package org.eclipse.scanning.api.scan.models;

/**
 *
 * This class contains the data required by a scannable when it
 * is configured during a scan. For instance, information for writing a NeXus file.
 *
 * @author Matthew Gerring
 * @see AxisModel
 *
 */
public class ScannableModel {

	private int rank;
	
	public ScannableModel() {
		this(-1);
	}

	public ScannableModel(int rank) {
		super();
		this.rank = rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

}
