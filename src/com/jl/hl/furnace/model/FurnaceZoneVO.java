package com.jl.hl.furnace.model;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.util.LoggerUtil;

public class FurnaceZoneVO {

	private final static Logger logger = LoggerUtil
			.getLogger(FurnaceZoneVO.class.getSimpleName());

	public static final int ZONE_1 = 1; // 加一段
	public static final int ZONE_2 = 2; // 加二段
	public static final int ZONE_3 = 3; // 均热段

	// yyyyMMddHHmmss
	private String timeID;

	private int zoneID = 1;

	private boolean isFireBurningLEFT = false;

	private boolean isFireBurningRIGHT = false;

	private BigDecimal airGasRatioACTUAL = new BigDecimal(0);

	private BigDecimal airGasRatioGIVEN = new BigDecimal(0);

	private BigDecimal temp = new BigDecimal(0);

	private BigDecimal tempTarget = new BigDecimal(0);

	private BigDecimal flowZoneGAS = new BigDecimal(0);

	private BigDecimal flowZoneGASTarget = new BigDecimal(0);

	private BigDecimal flowZoneAIR = new BigDecimal(0);

	private BigDecimal tempAirEjectSmokeLEFT = new BigDecimal(0);

	private BigDecimal tempAirEjectSmokeRIGHT = new BigDecimal(0);

	private BigDecimal tempGasEjectSmokeLEFT = new BigDecimal(0);

	private BigDecimal tempGasEjectSmokeRIGHT = new BigDecimal(0);

	private BigDecimal valveGivenAirEjectSmoke = new BigDecimal(0);

	private BigDecimal valveGivenGasEjectSmoke = new BigDecimal(0);

	private BigDecimal valveGivenAir = new BigDecimal(0);

	private BigDecimal valveGivenGas = new BigDecimal(0);

	private BigDecimal valveActualAirEjectSmoke = new BigDecimal(0);

	private BigDecimal valveActualGasEjectSmoke = new BigDecimal(0);

	private BigDecimal valveActualAir = new BigDecimal(0);

	private BigDecimal valveActualGas = new BigDecimal(0);

	private int exchangeSeconds = 0;

	// 加二段没有炉膛压力
	private BigDecimal chamberPressure = new BigDecimal(0);

	public FurnaceZoneVO() {

	}

	public FurnaceZoneVO(int zone) {
		this.zoneID = zone;
	}

	public String getTimeID() {
		return timeID;
	}

	// set by FurnaceVO timeID
	public void setTimeID(String timeID) {
		this.timeID = timeID;
	}

	public void setZoneID(int zoneID) {
		this.zoneID = zoneID;
	}

	public int getZoneID() {
		return zoneID;
	}

	public String getZoneName() {
		String name = "";
		switch (zoneID) {
		case 1:
			name = "加一段";
			break;
		case 2:
			name = "加二段";
			break;
		case 3:
			name = "均热段";
			break;
		}
		return name;
	}

	public boolean isFireBurningLEFT() {
		return isFireBurningLEFT;
	}

	public void setFireBurningLEFT(boolean isFireBurningLEFT) {
		this.isFireBurningLEFT = isFireBurningLEFT;
	}

	public boolean isFireBurningRIGHT() {
		return isFireBurningRIGHT;
	}

	public void setFireBurningRIGHT(boolean isFireBurningRIGHT) {
		this.isFireBurningRIGHT = isFireBurningRIGHT;
	}

	public BigDecimal getAirGasRatioACTUAL() {
		return airGasRatioACTUAL;
	}

	public void setAirGasRatioACTUAL(BigDecimal airGasRatioACTUAL) {
		try {
			this.airGasRatioACTUAL = airGasRatioACTUAL.setScale(2,
					BigDecimal.ROUND_HALF_UP);
		} catch (NumberFormatException e) {
			this.airGasRatioACTUAL = new BigDecimal(0);
		}
	}

	public BigDecimal getAirGasRatioGIVEN() {
		return airGasRatioGIVEN;
	}

	public void setAirGasRatioGIVEN(BigDecimal airGasRatioGIVEN) {
		this.airGasRatioGIVEN = airGasRatioGIVEN.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getTemp() {
		return temp;
	}

	public BigDecimal getTempTarget() {
		return tempTarget;
	}

	public void setTempTarget(BigDecimal tempTarget) {
		this.tempTarget = tempTarget.setScale(0, BigDecimal.ROUND_HALF_UP);
	}

	public void setTemp(BigDecimal temp) {
		this.temp = temp.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getFlowZoneGAS() {
		return flowZoneGAS;
	}

	public void setFlowZoneGAS(BigDecimal flowZoneGAS) {
		this.flowZoneGAS = flowZoneGAS.setScale(0, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getFlowZoneGASTarget() {
		return flowZoneGASTarget;
	}

	public void setFlowZoneGASTarget(BigDecimal flowZoneGASTarget) {
		this.flowZoneGASTarget = flowZoneGASTarget.setScale(0,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getFlowZoneAIR() {
		return flowZoneAIR;
	}

	public void setFlowZoneAIR(BigDecimal flowZoneAIR) {
		this.flowZoneAIR = flowZoneAIR.setScale(0, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getTempAirEjectSmokeLEFT() {
		return tempAirEjectSmokeLEFT;
	}

	public void setTempAirEjectSmokeLEFT(BigDecimal tempAirEjectSomkeLEFT) {
		this.tempAirEjectSmokeLEFT = tempAirEjectSomkeLEFT.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getTempAirEjectSmokeRIGHT() {
		return tempAirEjectSmokeRIGHT;
	}

	public void setTempAirEjectSmokeRIGHT(BigDecimal tempAirEjectSomkeRIGHT) {
		this.tempAirEjectSmokeRIGHT = tempAirEjectSomkeRIGHT.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getTempGasEjectSomkeLEFT() {
		return tempGasEjectSmokeLEFT;
	}

	public void setTempGasEjectSmokeLEFT(BigDecimal tempGasEjectSomkeLEFT) {
		this.tempGasEjectSmokeLEFT = tempGasEjectSomkeLEFT.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getTempGasEjectSmokeRIGHT() {
		return tempGasEjectSmokeRIGHT;
	}

	public void setTempGasEjectSmokeRIGHT(BigDecimal tempGasEjectSomkeRIGHT) {
		this.tempGasEjectSmokeRIGHT = tempGasEjectSomkeRIGHT.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveGivenAirEjectSomke() {
		return valveGivenAirEjectSmoke;
	}

	public void setValveGivenAirEjectSmoke(BigDecimal valveGivenAirEjectSomke) {
		this.valveGivenAirEjectSmoke = valveGivenAirEjectSomke.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveGivenGasEjectSmoke() {
		return valveGivenGasEjectSmoke;
	}

	public void setValveGivenGasEjectSmoke(BigDecimal valveGivenGasEjectSomke) {
		this.valveGivenGasEjectSmoke = valveGivenGasEjectSomke.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveGivenAir() {
		return valveGivenAir;
	}

	public void setValveGivenAir(BigDecimal valveGivenAir) {
		this.valveGivenAir = valveGivenAir
				.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveGivenGas() {
		return valveGivenGas;
	}

	public void setValveGivenGas(BigDecimal valveGivenGas) {
		this.valveGivenGas = valveGivenGas
				.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveActualAirEjectSomke() {
		return valveActualAirEjectSmoke;
	}

	public void setValveActualAirEjectSmoke(BigDecimal valveActualAirEjectSomke) {
		this.valveActualAirEjectSmoke = valveActualAirEjectSomke.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveActualGasEjectSmoke() {
		return valveActualGasEjectSmoke;
	}

	public void setValveActualGasEjectSmoke(BigDecimal valveActualGasEjectSomke) {
		this.valveActualGasEjectSmoke = valveActualGasEjectSomke.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveActualAir() {
		return valveActualAir;
	}

	public void setValveActualAir(BigDecimal valveActualAir) {
		this.valveActualAir = valveActualAir.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getValveActualGas() {
		return valveActualGas;
	}

	public void setValveActualGas(BigDecimal valveActualGas) {
		this.valveActualGas = valveActualGas.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public int getExchangeSeconds() {
		return exchangeSeconds;
	}

	public void setExchangeSeconds(int exchangeSeconds) {
		this.exchangeSeconds = exchangeSeconds;
	}

	public BigDecimal getChamberPressure() {
		return chamberPressure;
	}

	public void setChamberPressure(BigDecimal chamberPressure) {
		this.chamberPressure = chamberPressure.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal valveLimitsAdjust(BigDecimal value) {
		BigDecimal r = new BigDecimal(0);
		int t = value.intValue();
		if (t > 100) {
			r = new BigDecimal(100);
			logger.info("");
		} else if (t > 0 && t <= 100) {
			r = value;
		}
		return r;
	}

	@Override
	public String toString() {
		return "FurnaceZoneVO [timeID=" + timeID + ", zoneID=" + zoneID
				+ ", isFireBurningLEFT=" + isFireBurningLEFT
				+ ", isFireBurningRIGHT=" + isFireBurningRIGHT
				+ ", airGasRatioACTUAL=" + airGasRatioACTUAL
				+ ", airGasRatioGIVEN=" + airGasRatioGIVEN + ", temp=" + temp
				+ ", tempTarget=" + tempTarget + ", flowZoneGAS=" + flowZoneGAS
				+ ", flowZoneGASTarget=" + flowZoneGASTarget + ", flowZoneAIR="
				+ flowZoneAIR + ", tempAirEjectSmokeLEFT="
				+ tempAirEjectSmokeLEFT + ", tempAirEjectSmokeRIGHT="
				+ tempAirEjectSmokeRIGHT + ", tempGasEjectSmokeLEFT="
				+ tempGasEjectSmokeLEFT + ", tempGasEjectSmokeRIGHT="
				+ tempGasEjectSmokeRIGHT + ", valveGivenAirEjectSmoke="
				+ valveGivenAirEjectSmoke + ", valveGivenGasEjectSmoke="
				+ valveGivenGasEjectSmoke + ", valveGivenAir=" + valveGivenAir
				+ ", valveGivenGas=" + valveGivenGas
				+ ", valveActualAirEjectSmoke=" + valveActualAirEjectSmoke
				+ ", valveActualGasEjectSmoke=" + valveActualGasEjectSmoke
				+ ", valveActualAir=" + valveActualAir + ", valveActualGas="
				+ valveActualGas + ", exchangeSeconds=" + exchangeSeconds
				+ ", chamberPressure=" + chamberPressure + "]";
	}
}
