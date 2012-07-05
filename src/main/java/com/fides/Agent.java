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
import net.grinder.engine.agent.AgentDaemon;
import net.grinder.engine.agent.AgentImplementation;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run agent process.
 *
 * @goal agent
 *
 * @author Giuseppe Iacono
 */
public class Agent extends GrinderPropertiesConfigure {

	// Agent logger
	private final Logger logger = LoggerFactory.getLogger("agent");

	/**
	 * Constructor
	 */
	public Agent() {
		super();
	}

	@Override
	public void execute()
	{
		try {
			super.execute();
		} catch (final MojoExecutionException e1) {
			logger.error("Failed to execute Grinder Maven goal.", e1);
		} catch (final MojoFailureException e1) {
			logger.error("Failed to execute Grinder Maven goal.", e1);
		}

		AgentDaemon daemon_agent;
		AgentImplementation default_agent;
		try {
			if (isDaemonOption()) {
				if(logger.isDebugEnabled()){
					logger.debug("");
					logger.debug(" ---------------------------");
					logger.debug("|   Create an AgentDaemon   |");
					logger.debug(" ---------------------------");
				}

				daemon_agent =
						new AgentDaemon(
								logger,
								getDaemonPeriod(),
								new AgentImplementation(logger, getFileProperties(), false));
				daemon_agent.run();
				daemon_agent.shutdown();
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("");
					logger.debug(" -----------------------------------");
					logger.debug("|   Create an AgentImplementation   |");
					logger.debug(" -----------------------------------");
				}

				default_agent = new AgentImplementation(logger, getFileProperties(), true);
				default_agent.run();
				default_agent.shutdown();
			}
		} catch (final GrinderException e) {
			logger.error("Failed to execute Grinder Maven goal.", e);
		}
	}
}
