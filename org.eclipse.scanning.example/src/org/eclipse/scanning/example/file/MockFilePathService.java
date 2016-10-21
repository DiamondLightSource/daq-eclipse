package org.eclipse.scanning.example.file;

import java.io.File;

import org.eclipse.scanning.api.scan.IFilePathService;

public class MockFilePathService implements IFilePathService {
	
	private final File dir;
	private String mostRecentPath;
	public MockFilePathService() {
		dir = new File(System.getProperty("java.io.tmpdir"));
	}
	
	@Override
	public String getNextPath() throws Exception {
		mostRecentPath = getUnique(dir, "Scan", "nxs").getAbsolutePath();
		return mostRecentPath;
	}
	
	/**
	 * Generates a unique file of the name template or template+an integer
	 * 
	 * @param dir
	 * @param template
	 * @param ext
	 *        if null will return a unique directory name
	 * @return a unique file.
	 */
	public static File getUnique(final File dir, final String template, final String ext) {
		String extension = ext != null ? (ext.startsWith(".")) ? ext : "." + ext : null;
		extension = extension != null ? extension : "";
		final File file = new File(dir, template + extension);
		if (!file.exists()) {
			return file;
		}

		return getUnique(dir, template, ext, 1);
	}

	/**
	 * @param dir
	 * @param template
	 * @param ext
	 * @param i
	 * @return file
	 */
	public static File getUnique(final File dir, final String template, final String ext, int i) {
		final String extension = ext != null ? (ext.startsWith(".")) ? ext : "." + ext : null;
		final File file = ext != null ? new File(dir, template + i + extension) : new File(dir, template + i);
		if (!file.exists()) {
			return file;
		}

		return getUnique(dir, template, ext, ++i);
	}

	@Override
	public String getMostRecentPath() throws IllegalStateException {
		if (mostRecentPath == null) throw new IllegalStateException("Must call getNextPath() first");
		return mostRecentPath;
	}

	@Override
	public String getTempDir() {
		return dir.toString();
	}

	@Override
	public String getProcessedFilesDir() {
		return new File(dir, "processed").toString();
	}

}
