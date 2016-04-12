package com.jl.hl.furnace.model;

import java.math.BigDecimal;

public class ResultVO {

	private String startTime;

	private String endTime;

	private String criteria;

	private int counts;

	private int total;

	private BigDecimal ratio;

	public ResultVO() {
	}
	
	

	public ResultVO(String startTime, String endTime) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
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

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public int getCounts() {
		return counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public BigDecimal getRatio() {
		return ratio;
	}
	
	public float getRatioFloatValue() {
		return ratio.floatValue();
	}

	public void setRatio(BigDecimal ratio) {
		this.ratio = ratio.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	public void setRatio(float ratio) {
		this.ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	public void setRatio(double ratio) {
		this.ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	@Override
	public String toString() {
		return "ResultVO [startTime=" + startTime + ", endTime=" + endTime
				+ ", criteria=" + criteria + ", counts=" + counts + ", total="
				+ total + ", ratio=" + ratio + "]";
	}
}
