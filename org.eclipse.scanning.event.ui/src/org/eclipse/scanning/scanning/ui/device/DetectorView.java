package org.eclipse.scanning.scanning.ui.device;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.scanning.event.ui.view.EventConnectionView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a list of available detectors to the user. 
 * They may click on one and configure it.
 * 
 * @author Matthew Gerring
 *
 */
public class DetectorView extends EventConnectionView {

	private static final Logger logger = LoggerFactory.getLogger(DetectorView.class);
	public  static final String ID     = "org.eclipse.scanning.event.ui.detectorView";
	
	private TableViewer list;
	
	@Override
	public void createPartControl(Composite parent) {
		
		list = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		
		list.getTable().setLinesVisible(true);
		list.getTable().setHeaderVisible(false);
		list.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumns(list, "", "Name");
		
		try {
			list.setContentProvider(new DetectorContentProvider(getUri(), getRequestName(), getResponseName()));
		} catch (Exception e) {
			logger.error("Cannot create content provider", e);
		}
	}

	private void createColumns(TableViewer tableViewer, String icon, String name) {
		
		TableViewerColumn iconColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		iconColumn.getColumn().setWidth(20);
		iconColumn.getColumn().setMoveable(false);
		iconColumn.getColumn().setText(icon);

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setWidth(300);
		nameColumn.getColumn().setMoveable(false);
		nameColumn.getColumn().setText(name);
				
	}

	@Override
	public void setFocus() {
		list.getTable().setFocus();
	}

	public static String createId(final String uri, final String requestName, final String responseName) {
		
		final StringBuilder buf = new StringBuilder();
		buf.append(ID);
		buf.append(":");
		buf.append(createSecondaryId(uri, requestName, responseName));
		return buf.toString();
	}

}
