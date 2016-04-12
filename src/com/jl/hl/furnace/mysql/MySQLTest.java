package com.jl.hl.furnace.mysql;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.FurnaceUtil;
import com.jl.hl.furnace.RecordRunningTimeThread;
import com.jl.hl.furnace.model.RunningTimeVO;
import com.jl.hl.furnace.util.LoggerUtil;

public class MySQLTest {

	private final static Logger logger = LoggerUtil.getLogger(MySQLTest.class
			.getSimpleName());

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		logger.entry();
			
		long mins = 1234;
		
		
		
		RunningTimeVO runningVO = new RunningTimeVO(FurnaceUtil.genTimeID(), FurnaceUtil.genTimeID(), mins, 1);		
		
		StoreToDB db = new StoreToDB();
		db.insertRunning(runningVO);
		
		Thread.sleep(1001);
		
		RecordRunningTimeThread tt = new RecordRunningTimeThread();
		tt.setData(System.currentTimeMillis(), mins, 2);
		tt.start();
		
	}

}
