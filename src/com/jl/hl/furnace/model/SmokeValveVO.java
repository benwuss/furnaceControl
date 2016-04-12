package com.jl.hl.furnace.model;

public class SmokeValveVO {

	public long timeId = 0L;

	// 1=加一段，2=加2段, 3=均热段
	private int zone = 1;

	private String zoneName;

	public static final String AIR_SMOKE = "A";

	public static final String GAS_SMOKE = "G";

	public static final String TEMP_POS_RIGHT = "R";

	public static final String TEMP_POS_LEFT = "L";

	// A = 空气， G = 煤气
	private String type = "A";

	// 火是否在排烟温度这一侧。
	private boolean isFireNearBy = false;

	// 本段换向剩余时间
	private int exchangeSeconds = 0;

	// 排烟温度位置
	private String position = "R";

	private int temp = 0;

	private float smokeValve = 0;
	// smokeValve + valveChange
	private float newValve = 0;

	private float valveChange = 0;

	private int flow = 0;

	public SmokeValveVO() {
		this.timeId = System.currentTimeMillis();
	}

	public long getTimeId() {
		return timeId;
	}

	public boolean isFireNearBy() {
		return isFireNearBy;
	}

	public void setFireNearBy(boolean isFireNearBy) {
		this.isFireNearBy = isFireNearBy;
	}

	public int getExchangeSeconds() {
		return exchangeSeconds;
	}

	public void setExchangeSeconds(int exchangeSeconds) {
		this.exchangeSeconds = exchangeSeconds;
	}

	public String getZoneName() {
		return zoneName;
	}

	public String getSmokeValveName() {
		StringBuffer sb = new StringBuffer();
		sb.append(zoneName);
		if (type.equals("A")) {
			sb.append("空气排烟阀");
		} else if (type.equals("G")) {
			sb.append("煤气排烟阀");
		}
		return sb.toString();
	}

	public String getSmokeTempName() {
		StringBuffer sb = new StringBuffer();
		sb.append(zoneName);
		if (type.equals("A")) {
			sb.append("空气排烟");
		} else if (type.equals("G")) {
			sb.append("煤气排烟");
		}
		if (position.equals("R")) {
			sb.append("左侧温度");
		} else if (position.equals("L")) {
			sb.append("右侧温度");
		}

		return sb.toString();
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public int getFlow() {
		return flow;
	}

	public void setFlow(int flow) {
		this.flow = flow;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTemp() {
		return temp;
	}

	public void setTemp(int temp) {
		this.temp = temp;
	}

	public float getSmokeValve() {
		return smokeValve;
	}

	public void setSmokeValve(float smokeValve) {
		this.smokeValve = smokeValve;
	}

	public float getNewValve() {
		return newValve;
	}

	public void setNewValve(float newValve) {
		this.newValve = newValve;
	}

	public float getValveChange() {
		return valveChange;
	}

	public void setValveChange(float valveChange) {
		this.valveChange = valveChange;
	}

	@Override
	public String toString() {
		return "SmokeValveVO [zone=" + zone + ", zoneName=" + zoneName
				+ ", type=" + type + ", isFireNearBy=" + isFireNearBy
				+ ", exchangeSeconds=" + exchangeSeconds + ", position="
				+ position + ", temp=" + temp + ", smokeValve=" + smokeValve
				+ ", newValve=" + newValve + ", valveChange=" + valveChange
				+ ", flow=" + flow + "]";
	}

}
