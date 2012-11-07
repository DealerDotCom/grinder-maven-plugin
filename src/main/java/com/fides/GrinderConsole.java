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

import net.grinder.common.GrinderException;
import net.grinder.console.ConsoleFoundation;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.util.AbstractMainClass;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run console process.
 *
 * @goal console
 *
 * @author Giuseppe Iacono
 */
public class GrinderConsole extends GrinderPropertiesConfigure {

	private final Resources resources = new ResourcesImplementation(
			"net.grinder.console.common.resources.Console");

	private final Logger logger =
			LoggerFactory.getLogger(resources.getString("shortTitle"));

	public Resources getResources() {
		return resources;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	public final static class Console extends AbstractMainClass {
		private static final String USAGE = "  java "
				+ Console.class.getName()
				+ " [-headless]"
				+ "\n"
				+ "\n  -headless                    Don't use a graphical user interface.";

		private final ConsoleFoundation m_consoleFoundation;

		private Console(final Resources resources, final Logger logger) throws GrinderException {
			super(logger, USAGE);
			m_consoleFoundation = new ConsoleFoundation(resources, logger, false);
		}

		private void run() {
			m_consoleFoundation.run();
		}
	}

	@Override
	public void execute() {
		final Console console;
		try {
			super.execute();
			console = new Console(resources, logger);
			console.run();
		} catch (final GrinderException e) {
			logger.error("Could not initialise", e);
			System.exit(2);
		} catch (final MojoExecutionException e) {
			logger.error("Could not initialise", e);
		} catch (final MojoFailureException e) {
			logger.error("Could not initialise", e);
		}
	}
}
