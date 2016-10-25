package org.eclipse.scanning.api.malcolm;

import java.util.LinkedList;
import java.util.Map;

/**
 * Class representing a table in Malcolm Format (List of columns with data)
 */
public class MalcolmTable {
	private Map<String, LinkedList<Object>> tableData = null;
	private Map<String, Class<?>> tableDataTypes = null;
	private LinkedList<String> headings;
	
	public MalcolmTable(Map<String, LinkedList<Object>> tableAsMap, Map<String, Class<?>> dataTypes) {
		tableData = tableAsMap;
		tableDataTypes = dataTypes;
		headings = new LinkedList<String>(tableAsMap.keySet());
	}
	
	public LinkedList<Object> getColumn(String columnName) throws Exception {
		LinkedList<Object> column = tableData.get(columnName);
		if (column != null) {
			return tableData.get(columnName);
		}
		throw new Exception("Unknown column: " + columnName);
	}
	
	public Class<?> getColumnClass(String columnName) throws Exception {
		LinkedList<Object> column = tableData.get(columnName);
		if (column != null) {
			return tableDataTypes.get(columnName);
		}
		throw new Exception("Unknown column: " + columnName);
	}
	
	public LinkedList<String> getHeadings() {
		return headings;
	}
}
