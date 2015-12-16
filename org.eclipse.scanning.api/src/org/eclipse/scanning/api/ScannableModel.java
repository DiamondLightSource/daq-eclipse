package org.eclipse.scanning.api;

/**
 *
 * This class contains the data required by a scannable when it
 * is configured during a scan. For instance, information for writing a NeXus file.
 *
 * @author Matthew Gerring
 *
 */
public class ScannableModel {

	private int rank;

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

}
