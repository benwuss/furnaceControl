package com.jl.hl.furnace.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		String timeID = formatter.format(currentTime);
        System.out.println(timeID);
        System.out.println(System.currentTimeMillis());
	}

}
