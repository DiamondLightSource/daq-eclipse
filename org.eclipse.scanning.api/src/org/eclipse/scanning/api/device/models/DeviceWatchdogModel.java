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

 Example XML configuration
    <pre>
    {@literal <!--  Watchdog Example -->}
	{@literal <bean id="topupModel" class="org.eclipse.scanning.api.device.models.DeviceWatchdogModel">}
	{@literal 	<property name="countdownName"          value="topup"/>}
	{@literal 	<property name="periodName"             value="period"/>}
	{@literal 	<property name="cooloff"                value="4000"/>}
	{@literal 	<property name="warmup"                 value="5000"/>}
    {@literal   <property name="bundle"               value="org.eclipse.scanning.api" /> <!-- Delete for real spring? -->}
	{@literal </bean>}
	{@literal <bean id="topupWatchdog"    class="org.eclipse.scanning.sequencer.watchdog.TopupWatchdog" init-method="activate">}
	{@literal 	<property name="model"    ref="topupModel"/>}
    {@literal   <property name="bundle"   value="org.eclipse.scanning.sequencer" /> <!-- Delete for real spring? -->}
	{@literal </bean>}
    </pre>

 * @author Matthew Gerring
 *
 */
public class DeviceWatchdogModel {

	/**
	 * This is an expression of scannables which are monitored and the expression
	 * reevaluated if they change during a scan. Each variable in the scan *must*
	 * be a scannable name. The expression will be evaluated using JEXL which is
	 * available through the IExpressionService. Any expression
	 */
	private String expression; // e.g. 'beamcurrent >= 1.0 && !portshutter.equalsIgnoreCase("Closed")'
	
	private String countdownName; // e.g. "topup", "countdown" PV likely to be SR-CS-FILL-01:COUNTDOWN
	private String periodName;    // e.g. "period", PV likely to be SR-CS-FILL-01:STACOUNTDN
	private long   cooloff;       // time in ms before topup for which the scan should be paused.
	private long   warmup;        // time in ms after topup the scan should wait before starting.
	
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
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
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
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
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
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
}
