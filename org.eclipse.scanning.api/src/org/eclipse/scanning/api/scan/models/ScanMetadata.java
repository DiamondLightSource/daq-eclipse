package org.eclipse.scanning.api.scan.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates metadata about the scan associated with  
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
	 * The type of the metadata.
	 */
	private final MetadataType type; 
	
	/**
	 * A map from metadata name to value for the metadata contained within this object. 
	 */
	private final Map<String, Object> metadataFields = new HashMap<>();
	
	public ScanMetadata(MetadataType type) {
		this.type = type;
	}
	
	public void addMetadataField(String fieldName, Object value) {
		metadataFields.put(fieldName, value);
	}
	
	public Object getMetadataFieldValue(String fieldName) {
		return metadataFields.get(fieldName);
	}
	
	public Set<String> getMetadataFieldNames() {
		return metadataFields.keySet();
	}
	
	public MetadataType getType() {
		return type;
	}

}
