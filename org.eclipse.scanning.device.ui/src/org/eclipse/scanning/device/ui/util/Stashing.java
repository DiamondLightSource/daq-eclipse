package org.eclipse.scanning.device.ui.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stashing {

	private static final Logger logger = LoggerFactory.getLogger(Stashing.class);
	
	private final File file;

	private IEventConnectorService marshallerService;
	
	/**
	 * 
	 * @param fileName, e.g. org.eclipse.scanning.device.ui.device.controls.json
	 */
	public Stashing(String fileName, IEventConnectorService marshallerService) {
		this(new File(System.getProperty("user.home")+"/.solstice/"+fileName),marshallerService);
	}
	
	public Stashing(File file, IEventConnectorService marshallerService) {
		this.file = file;
		this.marshallerService = marshallerService;
	}

	public boolean isStashed() {
		return file.exists();
	}
	
	public void stash(Object object) throws Exception {
		final String json = marshallerService.marshal(object);
		write(file, json);
	}
	
	public <T> T unstash(Class<T> clazz) {
		
		if (!isStashed()) return null;
		try {
			final String json = readFile(file).toString();
			final T ret = marshallerService.unmarshal(json, clazz);
			return ret;
		} catch (Exception ne) {
			logger.error("Cannot read file "+file, ne);
			return null;
		}
	}
	
	private static void write(final File file, final String text) throws Exception {
		
		file.getParentFile().mkdirs();
		BufferedWriter b = null;
		try {
			final OutputStream out = new FileOutputStream(file);
			final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
			b = new BufferedWriter(writer);
			b.write(text.toCharArray());
		} finally {
			if (b != null) {
				b.close();
			}
		}
	}

	private static final StringBuffer readFile(final File file) throws Exception {

		final String charsetName = "UTF-8";
		final InputStream in = new FileInputStream(file);
		BufferedReader ir = null;
		try {
			ir = new BufferedReader(new InputStreamReader(in, charsetName));

			// deliberately do not remove BOM here
			int c;
			StringBuffer currentStrBuffer = new StringBuffer();
			final char[] buf = new char[4096];
			while ((c = ir.read(buf, 0, 4096)) > 0) {
				currentStrBuffer.append(buf, 0, c);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}
	
	/**
	 * Stash using appropriate messages to the user.
	 * @param models
	 * @param shell
	 */
	public void save(Object models, Shell shell) {
		try {
			
			if (file.exists()) {
				boolean ok = MessageDialog.openConfirm(shell.getShell(), "Confirm Overwrite", "Are you sure that you would like to overwrite '"+file.getName()+"'?");
				if (ok) return;
			}
			
			stash(models);
			
		} catch (Exception e) {
			ErrorDialog.openError(shell.getShell(), "Error Saving Information", "An exception occurred while writing to file.",
					              new Status(IStatus.ERROR, "org.eclipse.scanning.device.ui", e.getMessage()));
		    logger.error("Error Saving Information", e);
		}
	}
	
	public <T> T load(Class<T> clazz, Shell shell) {
		try {
            return unstash(clazz);	
		} catch (Exception e) {
			MessageDialog.openInformation(shell.getShell(), "Exception while reading scans from file", "An exception occurred while reading scans from a file.\n" + e.getMessage());
		    return null;
		}
     
	}


}
