package org.eclipse.scanning.device.ui.device.scannable;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlEnumNode;
import org.eclipse.scanning.api.scan.ui.ControlFileNode;
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
		Activator.getStore().setDefault(DevicePreferenceConstants.NUMBER_FORMAT, "##########0.0###");
		this.cservice = cservice;
		this.mode     = mode;
	}

	@Override
	public StyledString getStyledText(Object element) {

		if(!(element instanceof INamedNode)) return new StyledString();

		final String       text       = getText(element);
		final StyledString styledText = new StyledString(text!=null?text:"");
		
		INamedNode node = (INamedNode)element;
		try {
			if (node instanceof ControlNode) {
				final String scannableName = node.getName();
				if (scannableName != null && !scannableName.equals("")) {
					final IScannable<?> scannable = cservice.getScannable(node.getName());
						if (scannable.getUnit() != null) {
						styledText.append("    ");
						styledText.append(scannable.getUnit(), StyledString.DECORATIONS_STYLER);
					}
				}				
			} else {
		
				// Intentionally do nothing!
			}
		} catch (Exception ne) {
			String message = ne.getMessage() == null ? "" : ne.getMessage();
			return styledText.append(message, StyledString.QUALIFIER_STYLER);
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
				final ControlNode cnode = (ControlNode)node;
				final String scannableName = cnode.getName();
				if (scannableName == null || scannableName.equals("")) {
					return "";
				}

				Object value = cnode.getValue(mode.isDirectlyConnected(), cservice);
				
				if (value == null) return "!VALUE";
				if (value instanceof Number) {
					try {
						final DecimalFormat format = new DecimalFormat(Activator.getDefault().getPreferenceStore().getString(DevicePreferenceConstants.NUMBER_FORMAT));
						return format.format(value); 
					} catch (Exception ne) {
						logger.warn("Could not format value as a decimal: ", value);
						return value.toString();
					}
				}
				if (value instanceof Object[]) {
					return Arrays.toString((Object[]) value);
				} else if (value instanceof double[]) {
					return Arrays.toString((double[]) value);
				}
				
				return value.toString();
				
			} else if (node instanceof ControlFileNode) {
				final ControlFileNode fnode = (ControlFileNode)node;
				return fnode.getFile();
			} else if (node instanceof ControlEnumNode) {
				final ControlEnumNode fnode = (ControlEnumNode)node;
				return fnode.getValue().toString();
				
			} else {
				return ""; // Only controls have values...
			}
		} catch (Exception ne) {
			logger.error("Error with value for "+node, ne);
		    return ne.toString();
		}
		
	}

}
