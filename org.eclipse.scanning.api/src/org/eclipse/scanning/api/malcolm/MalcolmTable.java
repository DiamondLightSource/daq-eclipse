package org.eclipse.scanning.api.malcolm;

import java.util.ArrayList;
import java.util.Map;

/**
 * Class representing a table in Malcolm Format (List of columns with data)
 */
public class MalcolmTable {
	
	
	private Map<String, ArrayList<Object>> tableData = null;
	private Map<String, Class<?>> tableDataTypes = null;
	private ArrayList<String> headings;
	
	/**
	 * Must have 
	 */
	public MalcolmTable() {
		
	}
	
	public MalcolmTable(Map<String, ArrayList<Object>> tableAsMap, Map<String, Class<?>> dataTypes) {
		tableData = tableAsMap;
		tableDataTypes = dataTypes;
		headings = new ArrayList<String>(tableAsMap.keySet());
	}
	
	public ArrayList<Object> getColumn(String columnName) throws Exception {
		ArrayList<Object> column = tableData.get(columnName);
		if (column != null) {
			return tableData.get(columnName);
		}
		throw new Exception("Unknown column: " + columnName);
	}
	
	public Class<?> getColumnClass(String columnName) throws Exception {
		ArrayList<Object> column = tableData.get(columnName);
		if (column != null) {
			return tableDataTypes.get(columnName);
		}
		throw new Exception("Unknown column: " + columnName);
	}
	
	public ArrayList<String> getHeadings() {
		return headings;
	}
}
