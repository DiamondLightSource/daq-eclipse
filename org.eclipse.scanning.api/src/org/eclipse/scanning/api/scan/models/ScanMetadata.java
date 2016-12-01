package org.eclipse.scanning.api.scan.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates metadata about the scan of a particular type, e.g. metadata about the sample.
 */
public class ScanMetadata {
	
	/**
	 * The type of metadata. This determines where location of the metadata in
	 * the scan output (e.g. NeXus file)
	 */
	public enum MetadataType {
		
		ENTRY,
		SAMPLE,
		INSTRUMENT,
		USER
	}
	
	/**
	 * The type of the metadata contained in this object.
	 */
	private MetadataType type; 
	
	/**
	 * A map from field name to value for the metadata contained within this object. 
	 */
	private Map<String, Object> fields = new HashMap<>();
	
	public ScanMetadata() {
		// no-args constructor for json marshalling
	}
	
	public MetadataType getType() {
		return type;
	}

	public void setType(MetadataType type) {
		this.type = type;
	}
	
	public ScanMetadata(MetadataType type) {
		this.type = type;
	}
	
	public void addField(String fieldName, Object value) {
		fields.put(fieldName, value);
	}
	
	public Object getFieldValue(String fieldName) {
		return fields.get(fieldName);
	}
	
	public Map<String, Object> getFields() {
		return fields;
	}
	
	@Deprecated
	public void setFields(Map<String, Object> fields) {
		// for use when marshalling, addField(String, Object) should be used in code
		this.fields = fields;
	}

}
