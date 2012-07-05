package org.jtmb.velocityMerger;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 *
 * @author Travis Bear
 *
 *         Simple tool with 2 purposes:
 *
 *         1. Provide a simple API to merge velocity templates that are stored as Strings or Files.
 *
 *         2. Provide a simple mechanism where plugins (Velocity contexts) can be added simply by referring to them in a
 *         config file and dropping the plugin jar file into the lib directory.
 */
public class VelocityMerger {

	private static final String PLUGIN_PREFIX = "vPlugin.";
	private static final String LOGTAG = "velocityMerger String";
	private VelocityEngine velocityEngine;
	private VelocityContext velocityContext;
	private static final Logger logger = Logger.getLogger(VelocityMerger.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public VelocityMerger(final Properties props) {
		try {
			velocityEngine = new VelocityEngine();
			velocityContext = new VelocityContext();
			velocityEngine.init();
			// all plugins must have a single contructor that takes a
			// single java.util.Properties parameter
			final Class[] classParmeters = { Properties.class };
			final Object[] objectParmeters = { props };
			// discover and load plugins configured
			for (final Enumeration e = props.keys(); e.hasMoreElements();) {
				final String key = (String) e.nextElement();
				if (!key.startsWith(PLUGIN_PREFIX)) {
					logger.debug("key " + key + "  -- ignored.");
					continue;
				}
				logger.debug("key " + key + "  -- USED.");
				final String contextKey = key.substring(PLUGIN_PREFIX.length());
				logger.info(key + " '" + contextKey + "'  -- loading.");
				final Class pluginClass = Class.forName(props.getProperty(key, "not found"));
				final java.lang.reflect.Constructor constructor = pluginClass.getConstructor(classParmeters);
				velocityContext.put(contextKey, constructor.newInstance(objectParmeters));
			}
		} catch (final Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * Processes a VTL template on the filesystem
	 *
	 * @param fileName
	 *            -- location of the template file
	 * @return -- String containing the value of the merged template
	 */
	public String mergeTemplateFile(final String fileName) {
		try {
			final Template t = velocityEngine.getTemplate(fileName);
			final StringWriter stringWriter = new StringWriter();
			t.merge(velocityContext, stringWriter);
			return stringWriter.toString();

		} catch (final Exception e) {
			logger.warn(e.getMessage(), e);
			return e.getMessage();
		}
	}

	/**
	 * Processes a VTL template on the filesystem
	 *
	 * @param templateFile
	 *            -- the template file to be merged
	 */
	public void mergeTemplateFile(final File templateFile) {
		mergeTemplateFile(templateFile.getAbsolutePath());
	}

	/**
	 * This method processes a velocity template that is contained in a string.
	 *
	 * @param templateText
	 *            -- string containing the VTL to be processed
	 * @return -- string containing the value of the processed template
	 */
	public String mergeTemplateString(final String templateText) {
		logger.debug("Merging template string:\n" + templateText);
		try {
			final StringWriter stringWriter = new StringWriter();
			velocityEngine.evaluate(velocityContext, stringWriter, LOGTAG, new StringReader(templateText));
			return stringWriter.toString();
		} catch (final Exception e) {
			logger.warn(e.getMessage(), e);
			return e.getMessage();
		}
	}

	public Object getPlugin(final String key) {
		if (velocityContext.internalContainsKey(key)) {
			logger.debug("Found Velocity plugin '" + key + "'");
		}
		return velocityContext.get(key);
	}
}
