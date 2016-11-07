package org.eclipse.scanning.api.malcolm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class representing a table in Malcolm Format (List of columns with data).
 * Instances of this class are NOT thread safe.
 */
public class MalcolmTable implements Iterable<Map<String, Object>> {
	
	private final Map<String, ArrayList<Object>> tableData;
	private final Map<String, Class<?>> tableDataTypes;
	private final List<String> headings;
	private int numRows;
	
	/**
	 * Create a new Malcolm table with the given columns data and data types. The
	 * size of each column must be equal, and the keys of both maps must be the same, these
	 * are the column headings.  
	 * @param tableAsMap a list of values for each column, of equal lengths
	 * @param dataTypes a map of the data types for each column, this map must have the
	 *    same keys as the tableAsMap
	 */
	public MalcolmTable(Map<String, ArrayList<Object>> tableAsMap, Map<String, Class<?>> dataTypes) {
		if (tableAsMap == null || dataTypes == null) throw new NullPointerException();
		if (tableAsMap.size() != dataTypes.size()) {
			throw new IllegalArgumentException("The given arguments are not of the same size");
		}
		
		tableData = tableAsMap;
		
		numRows = tableData.isEmpty() ? 0 : tableData.values().iterator().next().size();
		tableDataTypes = dataTypes;
		headings = new LinkedList<String>(tableAsMap.keySet());
		
		for (String heading : headings) {
			if (!dataTypes.containsKey(heading)) {
				throw new IllegalArgumentException("The types map has no entry for column " + heading);
			}
			
			if (tableAsMap.get(heading).size() != numRows) {
				throw new IllegalArgumentException(String.format("The column %s has size %d, should be %d",
						heading, tableAsMap.get(heading).size(), numRows));
			}
		}
	}
	
	/**
	 * Creates a new empty table with columns of the given types.
	 * @param dataTypes map from column name to data type for that column
	 */
	public MalcolmTable(Map<String, Class<?>> dataTypes) {
		tableDataTypes = dataTypes;
		headings = new LinkedList<String>(tableDataTypes.keySet());
		
		tableData = new LinkedHashMap<>(headings.size());
		for (String heading : headings) {
			tableData.put(heading, new ArrayList<>());
		}
		numRows = 0;
	}
	
	public List<Object> getColumn(String columnName) {
		ArrayList<Object> column = tableData.get(columnName);
		if (column != null) {
			return tableData.get(columnName);
		}
		throw new RuntimeException("Unknown column: " + columnName);
	}
	
	public Class<?> getColumnClass(String columnName) {
		ArrayList<Object> column = tableData.get(columnName);
		if (column != null) {
			return tableDataTypes.get(columnName);
		}
		throw new RuntimeException("Unknown column: " + columnName);
	}
	
	public Map<String, Object> getRow(int rowIndex) {
		if (rowIndex >= numRows) {
			throw new IndexOutOfBoundsException("No such row " + rowIndex + ", number of rows = " + numRows);
		}
		
		Map<String, Object> row = new LinkedHashMap<>(headings.size());
		for (String heading : headings) {
			row.put(heading, getCellValue(heading, rowIndex));
		}
		
		return row;
	}
	
	public Object getCellValue(String columnName, int rowIndex) {
		return getColumn(columnName).get(rowIndex);
	}
	
	public List<String> getHeadings() {
		return headings;
	}
	
	public void addRow(Map<String, Object> newRow) {
		if (newRow.size() != headings.size()) {
			throw new IllegalArgumentException("The size of the map for the new row must match the number of columns in the table.");
		}
		for (String heading : headings) {
			if (!newRow.containsKey(heading)) {
				throw new IllegalArgumentException("This row map does not have an entry for the column with the heading " + heading);
			}
			List<Object> columnValues = tableData.get(heading);
			columnValues.add(newRow.get(heading));
		}
		numRows++;
	}

	@Override
	public Iterator<Map<String, Object>> iterator() {
		return new MalcolmTableRowIterator();
	}
	
	public class MalcolmTableRowIterator implements Iterator<Map<String, Object>> {
		
		private int rowNum = 0;
		
		@Override
		public boolean hasNext() {
			return rowNum < numRows;
		}

		@Override
		public Map<String, Object> next() {
			return getRow(rowNum++);
		}
		
	}
}
