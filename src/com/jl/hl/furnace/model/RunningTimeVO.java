package com.jl.hl.furnace.model;

public class RunningTimeVO {

	private String startTime = "";

	private String endTime = "";

	private long duration = 0;

	private int mode = 0;

	public RunningTimeVO() {

	}

	public RunningTimeVO(String startTime, String endTime, long duration,
			int mode) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.mode = mode;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
}
