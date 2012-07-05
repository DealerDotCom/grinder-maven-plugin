//   Copyright 2012 Giuseppe Iacono, Felipe Munoz Castillo
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.fides;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Provide method to decompress jar files
 *
 * @author Giuseppe Iacono
 */
public final class FileUtil {

	private static final int BUFFER = 1024;

	public static void unJarDirectory(final String in, final File fOut, final String directoryName)
			throws IOException {
		if (!fOut.exists()) {
			fOut.mkdirs();
		}
		if (!fOut.isDirectory()) {
			throw new IOException("Destination must be a directory.");
		}
		final JarInputStream jin = new JarInputStream(new FileInputStream(in));
		final byte[] buffer = new byte[BUFFER];

		ZipEntry entry = jin.getNextEntry();
		while (entry != null) {
			String fileName = entry.getName();
			if (fileName.charAt(fileName.length() - 1) == '/') {
				fileName = fileName.substring(0, fileName.length() - 1);
			}
			if (fileName.charAt(0) == '/') {
				fileName = fileName.substring(1);
			}
			if (File.separatorChar != '/') {
				fileName = fileName.replace('/', File.separatorChar);
			}
			final File file = new File(fOut, fileName);

			if (entry.getName().contains(directoryName)) {
				if (entry.isDirectory()) {
					// make sure the directory exists
					file.mkdirs();
					jin.closeEntry();
				} else {
					// make sure the directory exists
					final File parent = file.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}

					// dump the file
					final OutputStream out = new FileOutputStream(file);
					int len = 0;
					while ((len = jin.read(buffer, 0, buffer.length)) != -1) {
						out.write(buffer, 0, len);
					}
					out.flush();
					out.close();
					jin.closeEntry();
					file.setLastModified(entry.getTime());
				}
			}
			entry = jin.getNextEntry();
		}
		jin.close();
	}
}
