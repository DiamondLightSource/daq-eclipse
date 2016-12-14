/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.ui;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;

/**
 * 
 * Class used to start and stop an SWT Display for use with the test.
 * 
 * @author Matthew Gerring
 *
 */
class TestUI {

	private ReentrantLock     currentTestLock;
	private final CyclicBarrier swtBarrier = new CyclicBarrier(2);
	private Thread uiThread;
	
	private ShellTest currentTest;
	private volatile boolean disposed = false;
	private Shell appShell;
	
	public void start()  {
		
		currentTestLock = new ReentrantLock();
		currentTestLock.lock();
		uiThread = new Thread(new Runnable() {

			public void run() {
				try {
					System.out.println("Starting "+Thread.currentThread().getName());
					while (!disposed && !uiThread.isInterrupted()) {
						
						currentTestLock.lockInterruptibly();
						currentTestLock.unlock();
						
						final Display display = new Display();
						appShell = currentTest.createShell(display);
						currentTest.setBot(new SWTBot(appShell));
						swtBarrier.await();
						System.out.println(Thread.currentThread().getName()+" entering readAndDespatch for test");
						while (!appShell.isDisposed()) {
							if (!display.readAndDispatch()) {
								display.sleep();
							}
						}
						display.dispose();
						System.out.println(Thread.currentThread().getName()+" test finished");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "SWTBot UI Thread");
		uiThread.setDaemon(true);
		uiThread.start();
	}

	public void createBot(ShellTest shellTest) throws InterruptedException, BrokenBarrierException {
		this.currentTest = shellTest;
		currentTestLock.unlock();
		swtBarrier.await();
	}

	public void stop() {
		uiThread.interrupt();
		disposed = true;
		currentTestLock.unlock();
	}

	public void disposeBot(ShellTest shellTest) throws InterruptedException {
		currentTestLock.lockInterruptibly();
		currentTest = null;
		Display.getDefault().syncExec(() -> {appShell.close();});
	}

}
