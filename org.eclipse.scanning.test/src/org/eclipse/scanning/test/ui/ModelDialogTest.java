package org.eclipse.scanning.test.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.scanning.api.scan.AxisConfiguration;
import org.eclipse.scanning.api.ui.auto.IInterfaceService;
import org.eclipse.scanning.api.ui.auto.IModelDialog;
import org.eclipse.scanning.device.ui.model.InterfaceService;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelDialogTest extends ShellTest{
	
	private static IInterfaceService interfaceService; // We really get this from OSGi services!
	
	@BeforeClass
	public static void createServicesServiceHolder() throws Exception {
		interfaceService = new InterfaceService(); // Just for testing! This comes from OSGi really.
		org.eclipse.scanning.device.ui.ServiceHolder.setExpressionService(new ServerExpressionService());
	}

	private AxisConfiguration    config;
	private IModelDialog<Object> dialog;

	@Override
	protected Shell createShell(Display display) throws Exception {
		
		this.config = new AxisConfiguration();
		config.setApplyModels(true);
		config.setApplyRegions(true);
		config.setFastAxisName("xxx");
		config.setFastAxisStart(0);
		config.setFastAxisEnd(100);
		config.setSlowAxisName("yyy");
		config.setSlowAxisStart(-100);
		config.setSlowAxisEnd(-200);
		config.setMicroscopeImage("C:/tmp/fred.png");
		
		this.dialog = interfaceService.createModelDialog(new Shell(display));
		dialog.setPreamble("Please define the axes and their ranges we will map within.");
		dialog.create();
		dialog.setSize(550,450); // As needed
		dialog.setText("Scan Area");
		dialog.setModel(config);
		
		Shell ret = (Shell)dialog.getControl();
		ret.pack();
		ret.open();

		return ret;
	}

	
	@Test
	public void checkShell() throws Exception {
		assertNotNull(bot.shell("Scan Area"));
	}
	
	@Test
	public void checkInitialValues() throws Exception {
		
		assertEquals(config.getMicroscopeImage(),   bot.table(0).cell(0, 1));
		
		assertEquals(config.getFastAxisName(),      bot.table(0).cell(2, 1));
		assertEquals(String.valueOf(config.getFastAxisStart()),   bot.table(0).cell(3, 1));
		assertEquals(String.valueOf(config.getFastAxisEnd()),     bot.table(0).cell(4, 1));
		
		assertEquals(config.getSlowAxisName(),      bot.table(0).cell(5, 1));
		assertEquals(String.valueOf(config.getSlowAxisStart()),   bot.table(0).cell(6, 1));
		assertEquals(String.valueOf(config.getSlowAxisEnd()),     bot.table(0).cell(7, 1));
		
	}

}
