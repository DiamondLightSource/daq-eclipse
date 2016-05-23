package org.eclipse.scanning.scanning.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.scanning.ui.device.DetectorView;
import org.eclipse.ui.IFolderLayout;

public class ScanningPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.eclipse.scanning.scanning.ui.generatorEditor");
			final String detectorId = DetectorView.createId(getUriString(), IEventService.REQUEST_TOPIC, IEventService.RESPONSE_TOPIC);
			folderLayout.addView(detectorId);
		}
		layout.addView("org.eclipse.scanning.scanning.ui.modelEditor", IPageLayout.BOTTOM, 0.5f, "org.eclipse.scanning.scanning.ui.generatorEditor");
	}

	private String getUriString() {
		String broker = System.getProperty("org.eclipse.scanning.broker.uri");
		if (broker==null) broker = System.getProperty("gda.activemq.broker.uri");
		if (broker==null) broker = "tcp://localhost:61616";
		return broker;
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
