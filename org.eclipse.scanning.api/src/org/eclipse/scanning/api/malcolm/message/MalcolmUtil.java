package org.eclipse.scanning.api.malcolm.message;

import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;

public class MalcolmUtil {

	/**
	 * Translate State out of strangely encoded map
	 * @param msg
	 * @return
	 * @throws Exception 
	 */
	public static DeviceState getState(JsonMessage msg) throws Exception {

		return getState(msg, true);
	}
	
	public static DeviceState getState(JsonMessage msg, boolean requireException) throws Exception {

		try {
			if (msg.getValue() instanceof String) {
				return DeviceState.valueOf(msg.getValue().toString().toUpperCase());
			} else {
				try {
				    return getState((Map)msg.getValue());
				} catch (Exception ne) {
					throw new Exception("Value is not a map containing state!", ne);
				}
			}
		} catch (Exception ne) {
			if (requireException) throw ne;
			return null;
		}
	}

	public static DeviceState getState(Map<String, ?> value) {
		
		if (value.containsKey("state")) {
			final String state = (String)value.get("state");
			return DeviceState.valueOf(state.toUpperCase());
		}

		if (value.containsKey("value") && !value.containsKey("choices")) value = (Map<String,Object>)value.get("value");
		List<String> choices = (List<String>)value.get("choices");
		int          index   = (int)value.get("index");
		final String state = choices.get(index);
		return DeviceState.valueOf(state.toUpperCase());

	}


    /**
     * Method to check if event is likely to be scanning, not the start or the end.
     * @param bean
     * @return
     */
	public static boolean isScanning(MalcolmEventBean bean) {
		return !bean.isScanEnd() && !bean.isScanStart() && bean.getState().isRunning();
	}

	public static boolean isStateChange(MalcolmEventBean bean) {
		return bean.getPreviousState()!=null && bean.getState()!=bean.getPreviousState();
	}

}
