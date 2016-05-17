package org.eclipse.scanning.example.xcen.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class XcenPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		
		layout.setEditorAreaVisible(false);
		layout.addView("org.eclipse.scanning.example.xcen.ui.views.XcenDiagram", IPageLayout.LEFT, 0.60f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("org.eclipse.scanning.example.xcen.ui.views.XcenView", IPageLayout.RIGHT, 0.40f, IPageLayout.ID_EDITOR_AREA);
		
		/*
		    -submit dataacq.xcen.SUBMISSION_QUEUE 
		    -topic dataacq.xcen.STATUS_TOPIC 
		    -status dataacq.xcen.STATUS_QUEUE 
		    -bundle org.eclipse.scanning.example.xcen
		    -consumer org.eclipse.scanning.example.xcen.consumer.XcenConsumer
		 */
		layout.addView(XcenServices.getQueueViewSecondaryId(), IPageLayout.BOTTOM, 0.5f, "org.eclipse.scanning.example.xcen.ui.views.XcenView");
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout) {
	}

}
