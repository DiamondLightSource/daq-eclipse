package org.eclipse.scanning.example.scannable;

import org.eclipse.dawnsci.nexus.NexusFile;

public class MockScannableModel  {

	private NexusFile file;
	private int size;

	public NexusFile getFile() {
		return file;
	}

	public void setFile(NexusFile file) {
		this.file = file;
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
		result = prime * result + ((file == null) ? 0 : file.hashCode());
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
		MockScannableModel other = (MockScannableModel) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (size != other.size)
			return false;
		return true;
	}
}
