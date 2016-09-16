package org.eclipse.scanning.device.ui.util;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;

public class ViewUtil {

	/**
	 * 
	 * @param id
	 * @param managers
	 * @param actions
	 */
	public static void addGroups(String id, List<IContributionManager> managers, IAction... actions) {
		for (IContributionManager man : managers) addGroup(id, man, actions);
	}
	
	/**
	 * 
	 * @param id
	 * @param manager
	 * @param actions
	 */
	public static  void addGroup(String id, IContributionManager manager, IAction... actions) {
		manager.add(new Separator(id));
		for (IAction action : actions) {
			manager.add(action);
		}
	}

}
