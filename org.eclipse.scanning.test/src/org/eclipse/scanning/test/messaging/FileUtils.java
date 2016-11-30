package org.eclipse.scanning.test.messaging;

import java.io.File;

public class FileUtils {

	static public final boolean recursiveDelete(File parent) {

		if (parent.exists()) {
			if (parent.isDirectory()) {

				File[] files = parent.listFiles();
				for (int ifile = 0; ifile < files.length; ++ifile) {
					if (files[ifile].isDirectory()) {
						recursiveDelete(files[ifile]);
					}
					if (files[ifile].exists()) {
						files[ifile].delete();
					}
				}
			}
			return parent.delete();
		}
		return false;
	}
	
}
