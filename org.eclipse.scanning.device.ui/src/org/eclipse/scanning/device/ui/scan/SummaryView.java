package org.eclipse.scanning.device.ui.scan;

import java.util.Arrays;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.points.GeneratorDescriptor;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A view which attempts to pick up selection events
 * from building a scan and then display the scan information.
 * 
 * @author Matthew Gerring
 *
 */
public class SummaryView extends ViewPart implements ISelectionListener {

	public static final String ID = "org.eclipse.scanning.device.ui.scan.SummaryView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(SummaryView.class);
	
	private Text text;

	private IPointGeneratorService pservice; // Used to create a compound generator
	private IValidatorService      vservice; // Used to validate a selection

	public SummaryView() {
		this.pservice = ServiceHolder.getGeneratorService();
		this.vservice = ServiceHolder.getValidatorService();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		
		this.text = new Text(container, SWT.MULTI|SWT.READ_ONLY);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		createActions();
		initializeToolBar();
		initializeMenu();
		PageUtil.getPage(getSite()).addSelectionListener(this);
	}
	
	@Override
	public void dispose() {
		if (PageUtil.getPage()!=null) PageUtil.getPage().removeSelectionListener(this);
		super.dispose();
	}
	
	private IAdaptable modelAdaptable;
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object ob = ((IStructuredSelection)selection).getFirstElement();
			if (ob instanceof IAdaptable) {
                CompoundModel cm = ((IAdaptable)ob).getAdapter(CompoundModel.class);
                if (cm !=null) modelAdaptable = (IAdaptable)ob;
			}
			if (isUpdatableSelection(ob)) update();
		}
	}

	private boolean isUpdatableSelection(Object ob) {
		if (ob instanceof GeneratorDescriptor) return true; // Gen changed.
		if (ob instanceof FieldValue)          return true; // Model changed.
		return false;
	}

	private void update() {
		if (modelAdaptable==null) return;
		try {
	        CompoundModel cm = modelAdaptable.getAdapter(CompoundModel.class);
	        if (cm != null) {
	        	// Validate
	        	vservice.validate(cm);
	        	
	        	// Create generator for points
	        	final IPointGenerator<?> gen = pservice.createCompoundGenerator(cm);
	        	text.setText(gen.getDescription());
	        }
		} catch (PointsValidationException ne) {
			 text.setText(ne.getMessage());
			 
		} catch (Exception ne) {
			logger.error("Cannot create summary of scan!", ne);
			if (ne.getMessage()!=null) {
			    text.setText(ne.getMessage());
			} else {
				text.setText(ne.toString());
			}
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		if (text!=null && !text.isDisposed()) text.setFocus();
	}

}
