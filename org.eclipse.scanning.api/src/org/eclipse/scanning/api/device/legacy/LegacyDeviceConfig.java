package org.eclipse.scanning.api.device.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Defines a legacy device.
 * 
 * @author Matthew Dickie
 */
public class LegacyDeviceConfig {
	
	private final List<String> paths;
	
	private final List<String> units;
	
	private Set<String> prerequisiteScannableNames = null;
	
	public LegacyDeviceConfig() {
		paths = new ArrayList<>();
		units = new ArrayList<>();
	}
	
	public void addField(String path) {
		addField(path, null);
	}
	
	public void addField(String path, String unit) {
		this.paths.add(path);
		this.units.add(unit);
	}
	
	public int getNumFields() {
		return paths.size();
	}

	private void checkFieldIndex(int fieldIndex) {
		if (fieldIndex > getNumFields()) {
			throw new IllegalArgumentException("No such field with index " + fieldIndex);
		}
	}
	
	public String getPath(int fieldIndex) {
		checkFieldIndex(fieldIndex);
		return paths.get(fieldIndex); 
	}
	
	public String getUnit(int fieldIndex) {
		checkFieldIndex(fieldIndex);
		return units.get(fieldIndex);
	}
	
	public Set<String> getRequiredScannableNames() {
		if (prerequisiteScannableNames == null) {
			return Collections.emptySet();
		}
		return prerequisiteScannableNames;
	}
	
	public void setRequiredScannableNames(Set<String> requiredScannableNames) {
		this.prerequisiteScannableNames = requiredScannableNames;
	}
	
}
