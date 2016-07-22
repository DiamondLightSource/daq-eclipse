package org.eclipse.scanning.device.ui.util;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MarginUtils {
	/**
	 * 
	 * @param area
	 */
	public static void removeMargins(Composite area) {
		final GridLayout layout = (GridLayout)area.getLayout();
		if (layout==null) return;
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =0;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;

	}
}
