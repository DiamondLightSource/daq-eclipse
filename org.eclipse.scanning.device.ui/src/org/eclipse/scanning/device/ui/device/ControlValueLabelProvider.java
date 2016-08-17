package org.eclipse.scanning.device.ui.device;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.scan.ui.Control;
import org.eclipse.scanning.api.scan.ui.ControlGroup;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ControlValueLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlValueLabelProvider.class);
	
	private IScannableDeviceService cservice;
	
	public ControlValueLabelProvider() {
		try {
			cservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IScannableDeviceService.class);
		} catch (EventException | URISyntaxException e) {
			logger.error("Cannot create a remote scannable device service!", e);
		}
	}

	@Override
	public StyledString getStyledText(Object element) {

		if(!(element instanceof INamedNode)) return new StyledString();

		final StyledString ret = new StyledString(getText(element));
		
		INamedNode node = (INamedNode)element;
		try {
			if (node instanceof Control) {
				final IScannable<?> scannable = cservice.getScannable(node.getName());
				ret.append("    ");
				ret.append(scannable.getUnit(), StyledString.DECORATIONS_STYLER);
			} else {
				// Intentionally do nothing!
			}
		} catch (Exception ne) {
			return ret.append(ne.getMessage(), StyledString.QUALIFIER_STYLER);
		}
		
		return ret;
	}

	@Override
	public String getText(Object element) {
		
		if(!(element instanceof INamedNode)) return super.getText(element);
		
		INamedNode node = (INamedNode)element;
		
		if (cservice==null) return "Server Error";
		try {
			if (node instanceof Control) {
				final IScannable<?> scannable = cservice.getScannable(node.getName());
				return String.valueOf(scannable.getPosition()); // TODO Formatting!
				
			} else {
				return ""; // Only controls have values...
			}
		} catch (Exception ne) {
			return ne.getMessage();
		}
		
	}

}
