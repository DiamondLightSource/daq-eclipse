package org.eclipse.scanning.example.xcen.ui;

import org.eclipse.scanning.event.ui.view.StatusQueueView;
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
		String uri = System.getProperty("org.eclipse.scanning.broker");
		if (uri==null || "".equals(uri)) uri = "tcp://sci-serv5.diamond.ac.uk:61616";
		String queueViewId = StatusQueueView.createId(uri, "org.eclipse.scanning.example.xcen", "org.eclipse.scanning.example.xcen.beans.XcenBean", "dataacq.xcen.STATUS_QUEUE", "dataacq.xcen.STATUS_TOPIC", "dataacq.xcen.SUBMISSION_QUEUE");
		queueViewId = queueViewId+"partName=Queue";
		layout.addView(queueViewId, IPageLayout.BOTTOM, 0.5f, "org.eclipse.scanning.example.xcen.ui.views.XcenView");
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
