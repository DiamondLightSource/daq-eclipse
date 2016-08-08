package org.eclipse.scanning.api.event.scan;

/**
 * 
 * Information about a given device.
 * 
 * @author Matthew Gerring
 *
 */
public class DeviceInformation<T> {
	
	/**
	 * The device state, for instance, IDLE, READY, PAUSED, FAULT etc.
	 */
	private DeviceState state;

	/**
	 * Device name used in scan
	 */
	private String name;
	
	/**
	 * Level device will be run at
	 */
	private int level;
	
	/**
	 * Label visible in UI table
	 */
	private String label;
	
	/**
	 * Id used to identify the device
	 */
	private String id;
	
	/**
	 * Human readable note on what the device is. For instance 'Device which computes and mandelbrot set and images it.'
	 */
	private String description;
	
	/**
	 * The path to the icon, including bundle. The user interface will then attempt to 
	 * load this bundle (if it exists in the UI product) and the icon from it.
	 * <p>
	 * Form: bundle:subdir(s)/image name
	 * <p>
	 * Example: org.eclipse.scanning.example:icons/mandelbrot.png
	 * <p>
	 * If org.eclipse.scanning.example is not available on the client then a default icon or no icon will be
	 * used depending on the UI connecting.
	 */
	private String icon;
	
	/**
	 * The model which the detector is currently using. Or if 
	 * the detector does not have a model, null.
	 */
	private T model;
	
	/**
	 * The unit for the device, if any
	 */
	private String unit;
	
	/**
	 * The upper limit or null if none exists.
	 */
	private Object upper;
	
	/**
	 * The lower limit or null if none exists.
	 */
	private Object lower;
	

	public DeviceInformation() {

	}
	
	public DeviceInformation(String name) {
	    this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + level;
		result = prime * result + ((lower == null) ? 0 : lower.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((upper == null) ? 0 : upper.hashCode());
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
		DeviceInformation other = (DeviceInformation) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (icon == null) {
			if (other.icon != null)
				return false;
		} else if (!icon.equals(other.icon))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (level != other.level)
			return false;
		if (lower == null) {
			if (other.lower != null)
				return false;
		} else if (!lower.equals(other.lower))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (state != other.state)
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (upper == null) {
			if (other.upper != null)
				return false;
		} else if (!upper.equals(other.upper))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeviceInformation [name=" + name + ", label=" + label + "]";
	}

	public T getModel() {
		return model;
	}

	public void setModel(T model) {
		this.model = model;
	}

	public DeviceState getState() {
		return state;
	}

	public void setState(DeviceState state) {
		this.state = state;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Object getUpper() {
		return upper;
	}

	public void setUpper(Object upper) {
		this.upper = upper;
	}

	public Object getLower() {
		return lower;
	}

	public void setLower(Object lower) {
		this.lower = lower;
	}
}
