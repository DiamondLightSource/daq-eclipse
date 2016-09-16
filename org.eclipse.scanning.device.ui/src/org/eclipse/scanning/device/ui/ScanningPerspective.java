package org.eclipse.scanning.device.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.device.ui.device.DetectorView;
import org.eclipse.ui.IFolderLayout;

public class ScanningPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.eclipse.scanning.device.ui.scanEditor");
			final String detectorId = DetectorView.createId(getUriString(), IEventService.DEVICE_REQUEST_TOPIC, IEventService.DEVICE_RESPONSE_TOPIC);
			folderLayout.addView(detectorId);
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_0", IPageLayout.TOP, 0.78f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.eclipse.scanning.device.ui.vis.visualiseView");
			folderLayout.addView("org.eclipse.scanning.device.ui.vis.StreamView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.3f, "org.eclipse.scanning.device.ui.scanEditor");
			folderLayout.addView("org.eclipse.scanning.device.ui.modelEditor");
			folderLayout.addView("org.eclipse.scanning.device.ui.device.ControlView");
			folderLayout.addView("org.eclipse.scanning.device.ui.points.scanRegionView");
		}
		layout.addView("org.eclipse.scanning.device.ui.scan.executeView", IPageLayout.BOTTOM, 0.69f, "org.eclipse.scanning.device.ui.modelEditor");
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
