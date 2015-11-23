package org.eclipse.scanning.api.event;

import java.util.UUID;


public class IdBean {
	
	private String    uniqueId;         // Unique id for each object.
	private boolean   explicitlySetId;
	
	public IdBean() {
		uniqueId = UUID.randomUUID().toString(); // Normally overridden
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId        = uniqueId;
		this.explicitlySetId = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((uniqueId == null) ? 0 : uniqueId.hashCode());
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
		IdBean other = (IdBean) obj;
		if (explicitlySetId) {
			if (uniqueId == null) {
				if (other.uniqueId != null)
					return false;
			} else if (!uniqueId.equals(other.uniqueId))
				return false;
		}
		return true;
	}

	/**
	 * Subclasses must override this method calling super.merge(...)
	 * 
	 * @param with
	 */
	public void merge(IdBean with) {
		this.uniqueId = with.uniqueId;
	}

}
