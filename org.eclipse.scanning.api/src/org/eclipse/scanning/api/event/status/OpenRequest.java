package org.eclipse.scanning.api.event.status;

/**
 * Class used to broadcast fact that user wanted to open a given item.
 * 
 * @author Matthew Gerring
 *
 */
public class OpenRequest {

	private StatusBean statusBean;
	
	public OpenRequest() {
		
	}

	public OpenRequest(StatusBean statusBean) {
		this.statusBean = statusBean;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((statusBean == null) ? 0 : statusBean.hashCode());
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
		OpenRequest other = (OpenRequest) obj;
		if (statusBean == null) {
			if (other.statusBean != null)
				return false;
		} else if (!statusBean.equals(other.statusBean))
			return false;
		return true;
	}

	public StatusBean getStatusBean() {
		return statusBean;
	}

	public void setStatusBean(StatusBean statusBean) {
		this.statusBean = statusBean;
	}
}
