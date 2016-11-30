package org.eclipse.scanning.api.device.models;

/**
 * 
 * Model for confuring a watchdog.
 * 
<pre>
Topup watchdogs have the following PV's available.

SR-CS-FILL-01:STACOUNTDN: 
this is an integer counter that runs to zero at the start of TopUp fill 
and is reset immediately to the time to next TopUp fill, fillPeriod,

SR-CS-FILL-01:COUNTDOWN: this is a float-valued counter that runs to zero
at the start of TopUp and remains there until the fill is complete when 
it resets to time before next TopUp fill,

SR-CS-FILL-01:ENDCOUNTDN: this is an integer counter that runs to zero 
at the end of TopUp fill and resets immediately to an estimate of the 
time before the end of the next TopUp fill.
</pre>

 * @author Matthew Gerring
 *
 */
public class DeviceWatchdogModel {

	private String countdownName; // e.g. "topup", "countdown" PV likely to be SR-CS-FILL-01:COUNTDOWN
	private String periodName; // e.g. "topup", "countdown" PV likely to be SR-CS-FILL-01:STACOUNTDN
	private long   cooloff; // time in ms before topup for which the scan should be paused.
	private long   warmup;  // time in ms after topup the scan should wait before starting.
	
	public String getCountdownName() {
		return countdownName;
	}
	public void setCountdownName(String monitorName) {
		this.countdownName = monitorName;
	}
	public long getCooloff() {
		return cooloff;
	}
	public void setCooloff(long cooloff) {
		this.cooloff = cooloff;
	}
	public long getWarmup() {
		return warmup;
	}
	public void setWarmup(long warmup) {
		this.warmup = warmup;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cooloff ^ (cooloff >>> 32));
		result = prime * result + ((countdownName == null) ? 0 : countdownName.hashCode());
		result = prime * result + ((periodName == null) ? 0 : periodName.hashCode());
		result = prime * result + (int) (warmup ^ (warmup >>> 32));
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
		DeviceWatchdogModel other = (DeviceWatchdogModel) obj;
		if (cooloff != other.cooloff)
			return false;
		if (countdownName == null) {
			if (other.countdownName != null)
				return false;
		} else if (!countdownName.equals(other.countdownName))
			return false;
		if (periodName == null) {
			if (other.periodName != null)
				return false;
		} else if (!periodName.equals(other.periodName))
			return false;
		if (warmup != other.warmup)
			return false;
		return true;
	}
	public String getPeriodName() {
		return periodName;
	}
	public void setPeriodName(String periodName) {
		this.periodName = periodName;
	}
	
}
