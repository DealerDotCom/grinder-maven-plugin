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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Provide Maven repository utilities
 *
 * @author Giuseppe Iacono
 */
public class MavenUtilities {

	/**
	 * @return the path of Maven local repository
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String getMavenLocalRepository()
			throws FileNotFoundException, IOException, XmlPullParserException {
		final File m2Dir = new File(System.getProperty("user.home"), ".m2");
		final File settingsFile = new File(m2Dir, "settings.xml");
		String localRepo = null;
		if (settingsFile.exists()) {
			final Settings settings = new SettingsXpp3Reader()
			.read(new FileReader(settingsFile));
			localRepo = settings.getLocalRepository();
		}
		if (localRepo == null) {
			localRepo = System.getProperty("user.home") + File.separator
					+ ".m2" + File.separator + "repository";
		}

		return localRepo;
	}

	/**
	 * @param groupId
	 *            the groupId of the plugin
	 * @param artifactId
	 *            the artifactId of the plugin
	 * @param version
	 *            the version of the plugin
	 *
	 * @return the relative path of plugin jar
	 */
	public static String getPluginRelativePath(final String groupId,
			final String artifactId, final String version,
			final String classifier) {
		final StringBuilder relativePath = new StringBuilder();
		final String[] words = groupId.split("\\.");

		for (final String word : words) {
			relativePath.append(word).append(File.separator);
		}

		relativePath.append(artifactId);
		relativePath.append(File.separator);
		relativePath.append(version);
		relativePath.append(File.separator);

		relativePath.append(artifactId);
		relativePath.append("-");
		relativePath.append(version);
		if (classifier != null && classifier.length() > 0) {
			relativePath.append("-");
			relativePath.append(classifier);
		}
		relativePath.append(".jar");

		return relativePath.toString();
	}

	/**
	 * @param groupId
	 *            the groupId of the plugin
	 * @param artifactId
	 *            the artifactId of the plugin
	 * @param version
	 *            the version of the plugin
	 * @param classifier
	 *            The classifier of the plugin.
	 *
	 * @return the absolute path of plugin jar
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String getPluginAbsolutePath(final String groupId,
			final String artifactId, final String version,
			final String classifier) throws FileNotFoundException, IOException,
			XmlPullParserException {
		final StringBuilder absolutePath = new StringBuilder();
		absolutePath.append(getMavenLocalRepository());
		absolutePath.append(File.separator);
		absolutePath.append(getPluginRelativePath(groupId, artifactId, version,classifier));
		return absolutePath.toString();
	}

	/**
	 * @return the absolute path of Maven local repository
	 */
	public static String getCurrentDir() {
		final StringBuilder directory = new StringBuilder();
		directory.append(System.getProperty("java.io.tmpdir")).append(File.separator).append("jar_grinderplugin");
		return directory.toString();
	}

	/**
	 * Replace special character '\' with the special character '/'.
	 *
	 * @param path
	 *            the path to normalize
	 *
	 * @return the normalized representation of path
	 */
	public static String normalizePath(final String path) {
		return path.replaceAll(Matcher.quoteReplacement("\\"), "/");
	}
}
