package org.eclipse.scanning.device.ui.device.scannable;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ControlValueLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlValueLabelProvider.class);
	
	private IScannableDeviceService cservice;
	private ControlViewerMode       mode;
	
	public ControlValueLabelProvider(IScannableDeviceService cservice, ControlViewerMode mode) {
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.NUMBER_FORMAT, "##########0.0###");
		this.cservice = cservice;
		this.mode     = mode;
	}

	@Override
	public StyledString getStyledText(Object element) {

		if(!(element instanceof INamedNode)) return new StyledString();

		final StyledString styledText = new StyledString(getText(element));
		
		INamedNode node = (INamedNode)element;
		try {
			if (node instanceof ControlNode) {
				final IScannable<?> scannable = cservice.getScannable(node.getName());
					if (scannable.getUnit() != null) {
					styledText.append("    ");
					styledText.append(scannable.getUnit(), StyledString.DECORATIONS_STYLER);
				}
			} else {
				// Intentionally do nothing!
			}
		} catch (Exception ne) {
			return styledText.append(ne.getMessage(), StyledString.QUALIFIER_STYLER);
		}
		
		return styledText;
	}

	@Override
	public String getText(Object element) {
		
		if(!(element instanceof INamedNode)) return super.getText(element);
		
		INamedNode node = (INamedNode)element;
		
		if (cservice==null) return "Server Error";
		try {
			if (node instanceof ControlNode) {
				ControlNode cnode = (ControlNode)node;
				
				Object value = null;
				if (!mode.isDirectlyConnected() && cnode.getValue()!=null) {
					value = cnode.getValue();
				} else {
					final IScannable<Number> scannable = cservice.getScannable(cnode.getName());
					value = scannable.getPosition();
				}
				
				if (value == null) return "!VALUE";
				final DecimalFormat format = new DecimalFormat(Activator.getDefault().getPreferenceStore().getString(DevicePreferenceConstants.NUMBER_FORMAT));
				try {
					return format.format(value); 
				} catch (Exception ne) {
					return value.toString();
				}
				
			} else {
				return ""; // Only controls have values...
			}
		} catch (Exception ne) {
			logger.error("Error with value for "+node, ne);
		    return ne.toString();
		}
		
	}

}
