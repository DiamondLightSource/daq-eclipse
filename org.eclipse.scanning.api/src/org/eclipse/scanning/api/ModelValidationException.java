package org.eclipse.scanning.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldValue;

public class ModelValidationException extends RuntimeException {
	// Use an unchecked exception because IPointGenerator.iterator() cannot
	// throw a checked exception. (TODO: Why can't we change signature of
	// IPointGenerator.iterator())?

	private static final long serialVersionUID = -2818058720202899355L;
	private Object model;
	private String[] fieldNames;

	public ModelValidationException(String message, Object model, String... fieldNames) {
		super(message);
		this.model      = model;
		this.fieldNames = fieldNames;
		
		boolean fieldFound = false;
		List<String> fields = Arrays.asList(fieldNames);
		if (fieldNames!=null && fieldNames.length>0) for (Field method : getFields(model)) {
			if (fields.contains(method.getName())) {
				fieldFound = true;
				break;
			}
		}
		if (!fieldFound) throw new RuntimeException("No field(s) '"+Arrays.toString(fieldNames)+"' has been found in Class "+model.getClass().getSimpleName());
	}
	
	public ModelValidationException(Exception e) {
		super(e);
	}


	private List<Field> getFields(Object model) {
		List<Field> fields = new ArrayList<>();
		Field[] fa = model.getClass().getDeclaredFields();
		if (fa!=null && fa.length>0) fields.addAll(Arrays.asList(fa));
		fa =model.getClass().getSuperclass().getDeclaredFields();
		fields.addAll(Arrays.asList(fa));
		return fields;
	}

	public ModelValidationException(RuntimeException e) {
		super(e);
	}

	public Object getModel() {
		return model;
	}

	public void setModel(Object model) {
		this.model = model;
	}

	public String[] getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
	}

	public boolean isField(FieldValue field) {
		if (fieldNames==null) return false;
		return Arrays.asList(fieldNames).contains(field.getName());
	}

}
