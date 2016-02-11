package org.eclipse.scanning.api.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 * @param <P>
 */
public abstract class AbstractGenerator<T,P> implements IPointGenerator<T,P>, Iterable<P> {

	protected T model;
	protected List<IPointContainer<?>> containers;
	private String id;
	private String label;
	private String description;
	private String iconPath;
	private boolean visible=true;
	private boolean enabled=true;
	
	protected AbstractGenerator() {
		super();
		this.id = getClass().getName();
	}

	protected AbstractGenerator(String id) {
		this.id = id;
	}

	@Override
	public T getModel() {
		return model;
	}

	@Override
	public void setModel(T model) {
		this.model = model;
	}
	
	/**
	 * Please override this method, the default creates all points and 
	 * returns their size
	 */
	@Override
	public int size() throws GeneratorException {
		// For those generators which implement an iterator,
		// doing this loop is *much* faster for large arrays
		// because memory does not have to be allocated.
		Iterator<P> it = iterator();
		int index = -1;
		while(it.hasNext()) {
			it.next();
			index++;
		}
		return index+1;
	}
	
	@Override
	public List<P> createPoints() throws GeneratorException {
		final List<P> points = new ArrayList<P>(89);
		Iterator<P> it = iterator();
		while(it.hasNext()) points.add(it.next());
		return points;
	}

	@Override
	public List<IPointContainer<?>> getContainers() {
		return containers;
	}

	@Override
	public void setContainers(List<IPointContainer<?>> containers) throws GeneratorException {
		this.containers = containers;
	}
	
	/**
	 * If there are no containers, the point is considered contained.
	 * 
	 * @param x
	 * @param y
	 */
	public boolean containsPoint(double x, double y) {
		if (containers==null)    return true;
		if (containers.size()<1) return true;
		for (IPointContainer<?> container : containers) {
			if (container.containsPoint(x, y)) return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containers == null) ? 0 : containers.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((iconPath == null) ? 0 : iconPath.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + (visible ? 1231 : 1237);
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
		AbstractGenerator other = (AbstractGenerator) obj;
		if (containers == null) {
			if (other.containers != null)
				return false;
		} else if (!containers.equals(other.containers))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (enabled != other.enabled)
			return false;
		if (iconPath == null) {
			if (other.iconPath != null)
				return false;
		} else if (!iconPath.equals(other.iconPath))
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
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (visible != other.visible)
			return false;
		return true;
	}

}
