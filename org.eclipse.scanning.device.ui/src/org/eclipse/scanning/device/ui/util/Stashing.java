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

import org.eclipse.scanning.api.event.IEventConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stashing {

	private static final Logger logger = LoggerFactory.getLogger(Stashing.class);
	
	private final String fileName;

	private IEventConnectorService marshallerService;
	
	/**
	 * 
	 * @param fileName, e.g. org.eclipse.scanning.device.ui.device.controls.json
	 */
	public Stashing(String fileName, IEventConnectorService marshallerService) {
		this.fileName = fileName;
		this.marshallerService = marshallerService;
	}
	
	private File getStashFile() {
		final File stash = new File(System.getProperty("user.name")+"/.solstice/"+fileName);
        return stash;
	}
	
	public boolean isStashed() {
		return getStashFile().exists();
	}
	
	public void stash(Object object) throws Exception {
		final String json = marshallerService.marshal(object);
		write(getStashFile(), json);
	}
	
	public <T> T unstash(Class<T> clazz) {
		
		if (!isStashed()) return null;
		try {
			final String json = readFile(getStashFile()).toString();
			final T ret = marshallerService.unmarshal(json, clazz);
			return ret;
		} catch (Exception ne) {
			logger.error("Cannot read file "+getStashFile(), ne);
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

}
