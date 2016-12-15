package org.eclipse.scanning.test.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.scanning.api.scan.AxisConfiguration;
import org.eclipse.scanning.api.ui.auto.IInterfaceService;
import org.eclipse.scanning.api.ui.auto.IModelViewer;
import org.eclipse.scanning.device.ui.model.InterfaceService;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ModelDialogTest extends ShellTest{
	
	private static IInterfaceService interfaceService; // We really get this from OSGi services!
	
	@BeforeClass
	public static void createServicesServiceHolder() throws Exception {
		interfaceService = new InterfaceService(); // Just for testing! This comes from OSGi really.
		org.eclipse.scanning.device.ui.ServiceHolder.setExpressionService(new ServerExpressionService());
	}

	private AxisConfiguration    config;
	private IModelViewer<Object> viewer;

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
		
		
		this.viewer = interfaceService.createModelViewer();

		Shell shell = new Shell(display);
		shell.setText("Scan Area");
		shell.setLayout(new GridLayout(1, false));
        viewer.createPartControl(shell);
		viewer.setModel(config);
		
		shell.pack();
		shell.setSize(500, 500);
		shell.open();

		return shell;
	}

	@Ignore("Cannot run this test in Travis - please fix! Idea: run separate to main tests")
	@Test
	public void checkShell() throws Exception {
		assertNotNull(bot.shell("Scan Area"));
	}
	
	@Ignore("Cannot run this test in Travis - please fix!")
	@Test
	public void checkInitialValues() throws Exception {
		
		assertEquals(config.getMicroscopeImage(),                 bot.table(0).cell(0, 1));
		
		assertEquals(config.getFastAxisName(),                    bot.table(0).cell(2, 1));
		assertEquals(String.valueOf(config.getFastAxisStart()),   bot.table(0).cell(3, 1));
		assertEquals(String.valueOf(config.getFastAxisEnd()),     bot.table(0).cell(4, 1));
		
		assertEquals(config.getSlowAxisName(),                    bot.table(0).cell(5, 1));
		assertEquals(String.valueOf(config.getSlowAxisStart()),   bot.table(0).cell(6, 1));
		assertEquals(String.valueOf(config.getSlowAxisEnd()),     bot.table(0).cell(7, 1));
		
	}

	
	@Ignore("Cannot run this test in Travis - please fix!")
	@Test
	public void checkFilePath() throws Exception {
		
		assertEquals(config.getMicroscopeImage(), bot.table(0).cell(0, 1));
		
		bot.table(0).click(0, 1); // Make the file editor
		
		SWTBotText text = bot.text(0);
		assertNotNull(text);
		assertEquals(config.getMicroscopeImage(), text.getText());
		
		text.setText("Invalid Path");
		
//		Color red = new Color(bot.getDisplay(), 255, 0, 0, 255);
//        assertEquals(red, text.foregroundColor());
	}

}
