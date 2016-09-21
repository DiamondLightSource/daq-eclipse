package org.eclipse.scanning.device.ui.util;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.queues.QueueViews;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.FrameworkUtil;

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
			if (action==null) continue;
			manager.add(action);
		}
	}

	
	public static void openQueueMonitor(Class<? extends StatusBean> beanClass, String partName) throws PartInitException, UnsupportedEncodingException {
		openQueueMonitor(beanClass, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC, EventConstants.SUBMISSION_QUEUE, partName);
	}
	
	public static void openQueueMonitor(Class<? extends StatusBean> beanClass, 
			                           final String queueName, 
			                           final String topicName, 
			                           final String submissionQueueName, 
			                           String partName) throws PartInitException, UnsupportedEncodingException {
		
		String bundle = FrameworkUtil.getBundle(beanClass).getSymbolicName();
		String bean   = beanClass.getName();
		String sqn    = queueName;
		String stn    = topicName;
		String submit = submissionQueueName;

		String queueViewId = QueueViews.createSecondaryId(Activator.getJmsUri(), bundle,bean, sqn, stn, submit);
		if (partName!=null) queueViewId = queueViewId+"partName="+partName;
		try {
			PageUtil.getPage().showView(QueueViews.getQueueViewID(), queueViewId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot open view", "Cannot open view "+queueViewId, 
					new Status(Status.ERROR, "org.eclipse.scanning.event.ui", e.getMessage()));
			throw e;
		}
	}

}
