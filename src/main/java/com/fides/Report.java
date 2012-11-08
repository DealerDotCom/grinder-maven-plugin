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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jtmb.grinderAnalyzer.MDC;
import org.python.core.PyString;
import org.python.core.PySystemState;
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

	// The Maven artifact ID of the Grinder Analyzer project
	private static final String ANALYZER_ARTIFACT_ID = "grinder-analyzer";

	// The name of the analyzer directory found in classpathtool.py in the Grinder Analyzer project.
	private static final String ANALYZER_DIR = "GrinderAnalyzer";

	// Grinder Analyzer main Python script file name.
	private static final String ANALYZER_FILE_NAME = "analyzer.py";

	// Regular expression used to find the Gridner data log file.
	private static final Pattern DATA_LOG_FILE_REGEX = Pattern.compile(".*-\\d+-data\\.log");

	// Regular expression used to find the Grinder mapping log file.
	private static final Pattern MAPPING_LOG_FILE_REGEX = Pattern.compile(".*-\\d+\\.log");

	// Report logger
	private final Logger logger = LoggerFactory.getLogger(Report.class);

	/**
	 * Constructor
	 */
	public Report() {
		super();
	}

	public static String getAnalyzerFileName() {
		return ANALYZER_FILE_NAME;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Create HTML reports of LOG_DIRECTORY's file
	 */
	private void jythonInterpreter() throws MojoExecutionException {
		try {
			final String currentDir = getCurrentDir();
			final String analyzerPyFile = currentDir + File.separator + ANALYZER_FILE_NAME;

			// create cache directory
			final Properties propertiesJython = new Properties();
			propertiesJython.put("python.cachedir",System.getProperty("java.io.tmpdir"));
			PythonInterpreter.initialize(System.getProperties(),propertiesJython, new String[0] );

			// Add some additional variables to the MDC for use in the Python scripts.
			MDC.put("current.dir", new PyString(currentDir));
			if(getAnalyzerProperties() != null) {
				MDC.put("user.analyzer.config", new PyString(getAnalyzerProperties()));
			}

			// Add the argv values needed to run the analyzer script.
			final PySystemState sys = new PySystemState();
			sys.argv.append(new PyString(analyzerPyFile));
			sys.argv.append(new PyString(getLogFile(DATA_LOG_FILE_REGEX)));
			sys.argv.append(new PyString(getLogFile(MAPPING_LOG_FILE_REGEX)));
			sys.argv.append(new PyString("1"));	// 1 agent

			// Add the Maven dependencies to the Python path so that the modules can be used by the scripts.
			for(final String dependency : getPropertiesPlugin().getProperty(GRINDER_JVM_CLASSPATH).split(File.pathSeparator)) {
				sys.path.append(new PyString(dependency));
			}

			// Create the Python interpreter
			final PythonInterpreter interp = new PythonInterpreter(null, sys);

			// Execute the Grinder Analyzer script
			interp.execfile(analyzerPyFile);
		} catch (final FileNotFoundException e) {
			throw new MojoExecutionException("Unable to set up Jython interpreter.", e);
		}
	}

	/**
	 * Finds the log file from the log directory using the provided regular expression.
	 * @param pattern A regular expression used to find a log file.
	 * @return The matching log file path or an empty string if no match could be found.
	 */
	private String getLogFile(final Pattern pattern) {
		final File logDirectory = new File(getLOG_DIRECTORY());
		final File[] logFiles = logDirectory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {
				return pattern.matcher(name).matches();
			}
		});
		return logFiles.length > 0 ? logFiles[0].getPath() : "";
	}

	/**
	 * Returns the absolute path to the Grinder Analyzer dependency resolved by Maven.
	 * @return The absolute path to the Grinder Analyzer dependency or {@code null}
	 * 	if the dependency cannot be found.
	 */
	private String getAnalyzerPath() {
		final List<Artifact> artifacts = getPluginArtifacts();

		for(final Artifact artifact : artifacts) {
			if(ANALYZER_ARTIFACT_ID.equals(artifact.getArtifactId())) {
				return MavenUtilities.normalizePath(artifact.getFile().getPath());
			}
		}

		return null;
	}

	/**
	 * @return the absolute path of unjar directory
	 */
	private String getCurrentDir() throws FileNotFoundException {
		final String pathToAnalyzer = getAnalyzerPath();

		if(logger.isDebugEnabled()) {
			logger.debug("Jar Absolute Path: {}", pathToAnalyzer);
		}

		if(pathToAnalyzer != null) {
			final String directory = MavenUtilities.getCurrentDir() + File.separator + ANALYZER_DIR;

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
				FileUtil.unJarDirectory(pathToAnalyzer, jar_directory);
			} catch (final IOException e) {
				logger.error("Unable to delete jython JAR file.", e);
			}

			if(logger.isDebugEnabled()) {
				logger.debug("unjar: {}", jar_directory);
			}

			return jar_directory.getPath();
		} else {
			throw new FileNotFoundException("Could not extract Grinder analyzer:  dependency not found!");
		}
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
			jythonInterpreter();
		} catch (final MojoExecutionException e) {
			logger.error("Failed to execute Grinder Maven goal.", e);
		} catch (final MojoFailureException e) {
			logger.error("Failed to execute Grinder Maven goal.", e);
		}
	}
}

