package com.jl.hl.furnace;

import org.apache.ibatis.session.SqlSession;

import com.jl.hl.furnace.model.RunningTimeVO;
import com.jl.hl.furnace.mysql.AnalyzeThis;
import com.jl.hl.furnace.mysql.StoreToDB;

public class RecordRunningTimeThread extends Thread {

	RunningTimeVO runningVO = null;
	
	String startTime = "";
	
	String endTime = "";

	public void setData(long lastTime, long mins, int mode) {
		runningVO = new RunningTimeVO(FurnaceUtil.getTimeFormat(lastTime),
				FurnaceUtil.genTimeID(), mins, mode);
		startTime = FurnaceUtil.getTimeFormat3(lastTime);
	    endTime = FurnaceUtil.getTimeFormat3(System.currentTimeMillis());
	}

	public void run() {
		StoreToDB db = new StoreToDB();
		db.insertRunning(runningVO);
		SqlSession session = db.getSession();
		AnalyzeThis analyze = new AnalyzeThis();
		analyze.setSqlSession(session);
		analyze.analyzeResult(startTime, endTime);
		db.commitAndClose(session);		
	}
}
