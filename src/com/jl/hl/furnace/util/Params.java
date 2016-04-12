package com.jl.hl.furnace.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.io.FileInputStream;
import java.io.File;

import org.apache.logging.log4j.Logger;

public class Params {

	private final static Logger logger = LoggerUtil.getLogger(Params.class
			.getSimpleName());

	public static String LOG_FILE_DIR = "C:/furnaceLog";

	public static int GEAR_H = 0;

	public static int GEAR_M = 0;

	public static int GEAR_L = 0;

	public static int GEAR_S = 0;

	static {
		Properties prop = new Properties();
		try {
			InputStream in = new FileInputStream(new File(
					"resources/furnace.properties"));
			prop.load(in);
			LOG_FILE_DIR = prop.getProperty("LOG_FILE_DIR").trim();
			GEAR_H = Integer.valueOf(prop.getProperty("GEAR_H").trim());
			GEAR_M = Integer.valueOf(prop.getProperty("GEAR_M").trim());
			GEAR_L = Integer.valueOf(prop.getProperty("GEAR_L").trim());
			GEAR_S = Integer.valueOf(prop.getProperty("GEAR_S").trim());

		} catch (IOException e) {
			logger.info("read resources/furnace.properties fail.");
		}
	}

	public static void main(String[] args) {

		System.out.println(Params.GEAR_H);
		System.out.println(Params.GEAR_M);
		System.out.println(Params.GEAR_L);
		System.out.println(Params.GEAR_S);

	}

}
