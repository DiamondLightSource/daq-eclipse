package org.eclipse.scanning.example.detector;

public class DarkImageModel {

	private String name;
	private int    columns;
	private int    rows;
	private int    frequency;
	
	public DarkImageModel() {
		name          = "dkExmpl";
		columns       = 64;
		rows          = 60;
		frequency     = 10;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columns;
		result = prime * result + frequency;
		result = prime * result + rows;
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
		DarkImageModel other = (DarkImageModel) obj;
		if (columns != other.columns)
			return false;
		if (frequency != other.frequency)
			return false;
		if (rows != other.rows)
			return false;
		return true;
	}
	public int getColumns() {
		return columns;
	}
	public void setColumns(int columns) {
		this.columns = columns;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int points) {
		this.rows = points;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
}
