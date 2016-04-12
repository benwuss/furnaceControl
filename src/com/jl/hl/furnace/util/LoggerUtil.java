package com.jl.hl.furnace.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class LoggerUtil {

	static {
		try {
			String path = System.getProperty("user.dir")
					+ "\\resources\\log4j2.xml";
			InputStream in = new FileInputStream(new File(path));
			ConfigurationSource source = new ConfigurationSource(in);
			Configurator.initialize(null, source);
		} catch (SecurityException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static Logger getLogger(String name) {
		Logger logger = LogManager.getLogger(name);
		return logger;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = getLogger("Log4j");
		logger.info("I {} you {}", "love", "really?");
	}

}
