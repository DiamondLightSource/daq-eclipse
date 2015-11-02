package org.eclipse.scanning.api.event.alive;

import java.util.UUID;

import org.eclipse.scanning.api.event.IdBean;

public class HeartbeatBean extends IdBean {

	/**
	 * Beamline that the acquisition server is controlling
	 */
	private String  beamline;
	
	/**
	 * Time that the beat happened on the server
	 */
	private long    publishTime;
	
	/**
	 * Time that the heartbeater started or -1 is the checker does not provide this information
	 */
	private long    conceptionTime;

	/**
	 * Tells you the unique id of the thing that is alive.
	 */
	private UUID consumerId;


	public long getConceptionTime() {
		return conceptionTime;
	}

	public void setConceptionTime(long conceptionTime) {
		this.conceptionTime = conceptionTime;
	}

	public long getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(long publishTime) {
		this.publishTime = publishTime;
	}

	public String getBeamline() {
		return beamline;
	}

	public void setBeamline(String beamline) {
		this.beamline = beamline;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((beamline == null) ? 0 : beamline.hashCode());
		result = prime * result
				+ (int) (conceptionTime ^ (conceptionTime >>> 32));
		result = prime * result
				+ ((consumerId == null) ? 0 : consumerId.hashCode());
		result = prime * result + (int) (publishTime ^ (publishTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeartbeatBean other = (HeartbeatBean) obj;
		if (beamline == null) {
			if (other.beamline != null)
				return false;
		} else if (!beamline.equals(other.beamline))
			return false;
		if (conceptionTime != other.conceptionTime)
			return false;
		if (consumerId == null) {
			if (other.consumerId != null)
				return false;
		} else if (!consumerId.equals(other.consumerId))
			return false;
		if (publishTime != other.publishTime)
			return false;
		return true;
	}

	public UUID getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(UUID consumerId) {
		this.consumerId = consumerId;
	}

	@Override
	public String toString() {
		return "HeartbeatBean [beamline=" + beamline + ", publishTime="
				+ publishTime + ", conceptionTime=" + conceptionTime
				+ ", consumerId=" + consumerId + "]";
	}

}
