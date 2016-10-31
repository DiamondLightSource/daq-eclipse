package org.eclipse.scanning.api.malcolm.attributes;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scanning.api.malcolm.MalcolmTable;

/**
 * 
 * Encapsulates a table array attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class TableAttribute extends MalcolmAttribute {
	public static final String TABLE_ID = "malcolm:core/TableMeta:";
	
	MalcolmTable tableValue;
	String[] headings;
	List<MalcolmAttribute> elements = new LinkedList<MalcolmAttribute>();
	
	@Override
	public MalcolmTable getValue() {
		return tableValue;
	}
	public void setValue(MalcolmTable tableValue) {
		this.tableValue = tableValue;
	}
	public String[] getHeadings() {
		return headings;
	}
	public void setHeadings(String[] headings) {
		this.headings = headings;
	}
}
