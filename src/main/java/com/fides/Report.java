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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate grinder build reports.
 *
 * @goal report
 *
 * @author Giuseppe Iacono
 */
public class Report extends GrinderPropertiesConfigure {
	// unjar directory
	private static final String JYTHON_DIR = "jython";

	// Grinder Analyzer
	private static final String JYTHON_FILE_NAME = "analyzer.py";

	// Report logger
	private final Logger logger = LoggerFactory.getLogger(Report.class);

	/**
	 * Constructor
	 */
	public Report() {
		super();
	}

	public static String getJythonDir() {
		return JYTHON_DIR;
	}

	public static String getJythonFileName() {
		return JYTHON_FILE_NAME;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Create HTML reports of LOG_DIRECTORY's file
	 */
	private void jythonInterpreter() {
		// Maven repository
		final String currentDir = getCurrentDir();

		final String analyzerPyFile = currentDir + File.separator + JYTHON_FILE_NAME;

		InputStream stream1 = null;

		try {
			stream1 = new FileInputStream(analyzerPyFile);
		} catch (final FileNotFoundException e) {
			logger.error("Analyzer Python file not found.", e);
		}

		if(logger.isDebugEnabled()) {
			logger.debug("Try to find the file " + analyzerPyFile);
		}

		// create cache directory
		final Properties propertiesJython = new Properties();
		propertiesJython.put("python.cachedir",System.getProperty("java.io.tmpdir"));
		propertiesJython.put("os.path.curdir" , "'" + currentDir + "'");

		PythonInterpreter.initialize(System.getProperties(),propertiesJython, null );

		// create python interpreter
		final PythonInterpreter interp = new PythonInterpreter();

		if(logger.isDebugEnabled()) {
			logger.debug("fullCurrentDir = {}",  currentDir);
		}

		interp.exec("import sys");
		interp.exec("sys.path.append(\"" + currentDir + "\")");
		interp.exec("sys.path.append(\"" + currentDir + File.separator + "lib\")");

		interp.execfile(stream1, JYTHON_FILE_NAME);
	}

	/**
	 * @return the absolute path of unjar directory
	 */
	private String getCurrentDir() {
		final InputStream mavenProperties = this.getClass().getClassLoader().getResourceAsStream("grinderplugin.properties");

		final Properties propertiesMaven = new Properties();

		try {
			propertiesMaven.load(mavenProperties);
			mavenProperties.close();
		} catch (final IOException e) {
			logger.error("Unable to load Maven properties.", e);
		}

		final String groupId = propertiesMaven.getProperty("grinderplugin.project.groupId");
		final String artifactId = propertiesMaven.getProperty("grinderplugin.project.artifactId");
		final String version = propertiesMaven.getProperty("grinderplugin.project.version");

		String jarPath = null;

		try {
			jarPath = MavenUtilities.getPluginAbsolutePath(groupId, artifactId, version, null);
		} catch (final FileNotFoundException e) {
			logger.error("Unable to get current directory.", e);
		} catch (final IOException e) {
			logger.error("Unable to get current directory.", e);
		} catch (final XmlPullParserException e) {
			logger.error("Unable to get current directory.", e);
		}

		logger.debug("Jar Absolute Path: {}", jarPath);

		final String directory = MavenUtilities.getCurrentDir();

		final File jar_directory = new File(directory);

		// Delete the last unjar directory
		if (jar_directory.exists()){
			jar_directory.delete();
		}

		// make sure the jar_directory exists
		if (jar_directory != null) {
			jar_directory.mkdirs();
		}

		jar_directory.setExecutable(true);
		jar_directory.setWritable(true);
		jar_directory.setReadable(true);

		// extract jarpath file to jar_directory/JYTHON_DIR
		try {
			FileUtil.unJarDirectory(jarPath, jar_directory, JYTHON_DIR);
		} catch (final IOException e) {
			logger.error("Unable to delete jython JAR file.", e);
		}

		if(logger.isDebugEnabled()) {
			logger.debug("unjar: {}", jar_directory);
		}

		return jar_directory + File.separator + JYTHON_DIR;
	}
	@Override
	public void execute() {
		final File logDir = new File(getLOG_DIRECTORY());

		if (!logDir.exists()) {
			if(logger.isDebugEnabled()) {
				logger.error("");
				logger.error(" ----------------------------");
				logger.error("|   Configuration ERROR!!!   |");
				logger.error(" ----------------------------");
				logger.error("");
				logger.error(" " + getLOG_DIRECTORY() + " do not exists! ");
				logger.error("");
				logger.error(" Create this directory and copy log files ");
				logger.error(" or run agent goal before report goal ");
				System.exit(0);
			}
		}

		final int logFiles = logDir.listFiles().length;

		if (logFiles == 0) {
			if(logger.isDebugEnabled()){
				logger.error("");
				logger.error(" ----------------------------");
				logger.error("|   Configuration ERROR!!!   |");
				logger.error(" ----------------------------");
				logger.error("");
				logger.error(" Log directory is empty! ");
				logger.error("");
				logger.error(" Copy log files to " + getLOG_DIRECTORY());
				logger.error(" or run agent goal before report goal ");
				System.exit(0);
			}
		}

		try {
			super.execute();
		} catch (final MojoExecutionException e) {
			logger.error("Failed to execute Grinder Maven goal.", e);
		} catch (final MojoFailureException e) {
			logger.error("Failed to execute Grinder Maven goal.", e);
		}

		jythonInterpreter();
	}
}

