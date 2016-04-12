package com.jl.hl.furnace.model;

import java.math.BigDecimal;

public class ValveVO {

	public final static String T_STRADEGY = "T";

	public final static String G_STRADEGY = "G";

	public final static String A_STRADEGY = "A";

	public final static String P_STRADEGY = "P";

	public final static String S_STRADEGY = "S";

	public final static String L_STRADEGY = "L";

	public final static String W_STRADEGY = "W";

	public final static String B_STRADEGY = "B";

	public final static String D_STRADEGY = "D";

	public final static String H_STRADEGY = "H";

	/**
	 * T:同步，G:煤气定额，A：空燃比微调，P：炉膛压力，S：排烟温度， L:低流量策略, W:均热段流量到零 ,B：保护模式，H：排烟温度低于100
	 * D：流量随动
	 */
	private String stradegy = "";

	/**
	 * 对应FurnaceVO用的
	 */
	private String timeID = "";

	/**
	 * 1：加一段，2：加二段，3：均热段
	 */
	private int zone = 0;

	/**
	 * A：空气，G：煤气
	 */
	private String type = "A";

	/**
	 * F:流量调节阀，S：烟气调节阀
	 */
	private String function = "F";

	/**
	 * 新值
	 */
	private BigDecimal newValue = new BigDecimal(0);

	private BigDecimal oldValue = new BigDecimal(0);

	private BigDecimal valueChange = new BigDecimal(0);

	public ValveVO() {
	}

	public String getValveName() {
		StringBuffer msg = new StringBuffer();
		if (zone == 1) {
			msg.append("加一段");
		} else if (zone == 2) {
			msg.append("加二段");
		} else if (zone == 3) {
			msg.append("均热段");
		}
		if (type.equals("G")) {
			msg.append("煤气");
		} else if (type.equals("A")) {
			msg.append("空气");
		}
		if (function.equals("F")) {
			msg.append("流量阀");
		} else if (function.equals("S")) {
			msg.append("排烟阀");
		}

		return msg.toString();
	}

	public ValveVO(int zone, String type, String function) {
		this.zone = zone;
		this.type = type;
		this.function = function;
	}

	public String getTimeID() {
		return timeID;
	}

	public void setTimeID(String timeID) {
		this.timeID = timeID;
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

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public BigDecimal getNewValue() {
		return newValue;
	}

	public void setNewValue(BigDecimal newValue) {
		this.newValue = newValue.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getOldValue() {
		return oldValue;
	}

	public void setOldValue(BigDecimal oldValue) {
		this.oldValue = oldValue.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValueChange() {
		return valueChange;
	}

	public void setValueChange(BigDecimal valueChange) {
		this.valueChange = valueChange.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public String getStradegy() {
		return stradegy;
	}

	public void setStradegy(String stradegy) {
		this.stradegy = stradegy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
		result = prime * result
				+ ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result
				+ ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result
				+ ((stradegy == null) ? 0 : stradegy.hashCode());
		result = prime * result + ((timeID == null) ? 0 : timeID.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result
				+ ((valueChange == null) ? 0 : valueChange.hashCode());
		result = prime * result + zone;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValveVO other = (ValveVO) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		if (stradegy == null) {
			if (other.stradegy != null)
				return false;
		} else if (!stradegy.equals(other.stradegy))
			return false;
		if (timeID == null) {
			if (other.timeID != null)
				return false;
		} else if (!timeID.equals(other.timeID))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (valueChange == null) {
			if (other.valueChange != null)
				return false;
		} else if (!valueChange.equals(other.valueChange))
			return false;
		if (zone != other.zone)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ValveVO [stradegy=" + stradegy + ", timeID=" + timeID
				+ ", zone=" + zone + ", type=" + type + ", function="
				+ function + ", newValue=" + newValue + ", oldValue="
				+ oldValue + ", valueChange=" + valueChange + "]";
	}
}
