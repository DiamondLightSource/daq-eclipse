package org.eclipse.scanning.test.ui;

import static org.junit.Assert.assertNotNull;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.scanning.device.ui.device.ScannableViewer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ScannableViewerTest extends ShellTest {
	
	@BeforeClass
	public static void createServices() throws Exception {	
		UISuite.createTestServices(true);
	}
	
	@AfterClass
	public static void disposeServices() throws Exception {	
		UISuite.disposeTestServices();
	}
	
	private ScannableViewer viewer;

	@Override
	protected Shell createShell(Display display) throws Exception {
		
		this.viewer = new ScannableViewer();
	
		Shell shell = new Shell(display);
		shell.setText("Monitors");
		shell.setLayout(new GridLayout(1, false));
        viewer.createPartControl(shell);
		
		shell.pack();
		shell.setSize(500, 500);
		shell.open();

		return shell;
	}

	@Test
	public void checkShell() throws Exception {
		assertNotNull(bot.shell("Monitors"));
	}

}
