package org.eclipse.scanning.api.device.models;

/**
 * 
 * Model for confuring a watchdog.
 * 
<pre>
Topup watchdogs should watch the following PV:

SR-CS-FILL-01:COUNTDOWN: this is a float-valued counter that runs to zero
at the start of TopUp and remains there until the fill is complete when 
it resets to time before next TopUp fill,
 Example XML configuration
    <pre>
    {@literal <!--  Watchdog Example -->}
	{@literal <bean id="topupModel" class="org.eclipse.scanning.api.device.models.DeviceWatchdogModel">}
	{@literal 	<property name="countdownName"          value="topup"/>}
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
	private String message;
	
	private String countdownName; // e.g. "topup", "countdown" PV likely to be SR-CS-FILL-01:COUNTDOWN
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cooloff ^ (cooloff >>> 32));
		result = prime * result + ((countdownName == null) ? 0 : countdownName.hashCode());
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (warmup != other.warmup)
			return false;
		return true;
	}
	
}
