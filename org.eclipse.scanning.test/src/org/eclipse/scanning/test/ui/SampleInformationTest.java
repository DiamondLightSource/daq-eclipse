package org.eclipse.scanning.test.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.scanning.api.event.scan.SampleData;
import org.eclipse.scanning.api.ui.auto.IInterfaceService;
import org.eclipse.scanning.api.ui.auto.IModelViewer;
import org.eclipse.scanning.device.ui.model.InterfaceService;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SampleInformationTest extends ShellTest {

	private static IInterfaceService interfaceService; // We really get this from OSGi services!
	
	@BeforeClass
	public static void createServices() throws Exception {	
		interfaceService = new InterfaceService(); // Just for testing! This comes from OSGi really.
		UISuite.createTestServices(false);
	}
	
	@AfterClass
	public static void disposeServices() throws Exception {	
		interfaceService = null;
		UISuite.disposeTestServices();
	}

	private SampleData           config;
	private IModelViewer<Object> viewer;

	@Override
	protected Shell createShell(Display display) throws Exception {
		
		this.config = new SampleData();
		config.setName("Sample name");
		config.setDescription("Hello World");
		
		this.viewer = interfaceService.createModelViewer();

		Shell shell = new Shell(display);
		shell.setText("Sample");
		shell.setLayout(new GridLayout(1, false));
        viewer.createPartControl(shell);
		viewer.setModel(config);
		
		shell.pack();
		shell.setSize(500, 500);
		shell.open();

		return shell;
	}


	@Test
	public void checkShell() throws Exception {
		assertNotNull(bot.shell("Sample"));
	}
	
	@Test
	public void checkInitialValues() throws Exception {
		
		assertEquals(config.getName(),        bot.table(0).cell(0, 1));
		assertEquals(config.getDescription(), bot.table(0).cell(1, 1));

	}

	@Test
	public void checkApply() throws Exception {
		
		assertEquals(config.getDescription(),     bot.table(0).cell(1, 1));
		bot.table(0).click(1, 1); 
		
		SWTBotText text = bot.text(0);
		assertNotNull(text);
		assertEquals("Hello World", text.getText());
		
		assertEquals("Hello World", config.getDescription());
		text.setText("Something else");
		assertEquals("Hello World", config.getDescription());
		assertEquals("Something else", text.getText());
		
		text.display.syncExec(()->viewer.applyEditorValue());
		
		assertEquals("Something else", config.getDescription());
		assertEquals("Something else", bot.table(0).cell(1, 1));
		
	}
}
