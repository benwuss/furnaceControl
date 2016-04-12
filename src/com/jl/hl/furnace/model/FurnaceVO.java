package com.jl.hl.furnace.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jl.hl.furnace.plc.moka7.S7;

/**
 * @author benwu
 * 
 */
public class FurnaceVO {

	// yyyyMMddHHmm.SSS
	private String timeID;
	
	private BigDecimal gasPressure = new BigDecimal(0);

	private BigDecimal gasValveGiven = new BigDecimal(0);

	private BigDecimal gasValveActual = new BigDecimal(0);

	private BigDecimal airPressure = new BigDecimal(0);

	private BigDecimal airValveGiven = new BigDecimal(0);

	private BigDecimal airValveActual = new BigDecimal(0);
	
	private BigDecimal oxygen = new BigDecimal(0);
	
	private BigDecimal billetTemp = new BigDecimal(0);

	private FurnaceZoneVO zone1 = new FurnaceZoneVO(FurnaceZoneVO.ZONE_1);

	private FurnaceZoneVO zone2 = new FurnaceZoneVO(FurnaceZoneVO.ZONE_2);

	private FurnaceZoneVO zone3 = new FurnaceZoneVO(FurnaceZoneVO.ZONE_3);

	/**
	 * 1=温度，2=煤气，3=手动
	 */
	private int PLCSignal = 3;

	public void loadDataFromS7300(byte[] buffer) {

		setGasPressure(new BigDecimal(S7.GetFloatAt(buffer, 220)));
		setGasValveGiven(new BigDecimal(S7.GetFloatAt(buffer, 224)));
		setGasValveActual(new BigDecimal(S7.GetFloatAt(buffer, 228)));
		setAirPressure(new BigDecimal(S7.GetFloatAt(buffer, 232)));
		setAirValveGiven(new BigDecimal(S7.GetFloatAt(buffer, 236)));
		setAirValveActual(new BigDecimal(S7.GetFloatAt(buffer, 240)));
		setOxygen(new BigDecimal(S7.GetFloatAt(buffer, 270)));
		setBilletTemp(new BigDecimal(S7.GetFloatAt(buffer, 274)));

		// 手动自动信号开关
		setPLCSignal(S7.GetShortAt(buffer, 244));

		// load Zone1
		zone1.setFireBurningLEFT(S7.GetBitAt(buffer, 0, 0));
		zone1.setFireBurningRIGHT(S7.GetBitAt(buffer, 0, 1));
		try {
			zone1.setAirGasRatioACTUAL(new BigDecimal(S7.GetFloatAt(buffer, 2)));
		} catch (Exception e) {
			zone1.setAirGasRatioACTUAL(new BigDecimal(0));
		}
		zone1.setAirGasRatioGIVEN(new BigDecimal(S7.GetFloatAt(buffer, 14)));
		zone1.setTemp(new BigDecimal(S7.GetFloatAt(buffer, 26)));
		zone1.setTempTarget(new BigDecimal(S7.GetFloatAt(buffer, 246)));
		zone1.setFlowZoneGASTarget(new BigDecimal(S7.GetFloatAt(buffer, 258)));
		zone1.setFlowZoneGAS(new BigDecimal(S7.GetFloatAt(buffer, 30)));		
		zone1.setFlowZoneAIR(new BigDecimal(S7.GetFloatAt(buffer, 34)));
		zone1.setTempAirEjectSmokeLEFT(new BigDecimal(S7.GetFloatAt(buffer, 38)));
		zone1.setTempAirEjectSmokeRIGHT(new BigDecimal(S7
				.GetFloatAt(buffer, 42)));
		zone1.setTempGasEjectSmokeLEFT(new BigDecimal(S7.GetFloatAt(buffer, 46)));
		zone1.setTempGasEjectSmokeRIGHT(new BigDecimal(S7
				.GetFloatAt(buffer, 50)));
		zone1.setValveGivenAirEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				54)));
		zone1.setValveGivenGasEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				58)));
		zone1.setValveGivenAir(new BigDecimal(S7.GetFloatAt(buffer, 62)));
		zone1.setValveGivenGas(new BigDecimal(S7.GetFloatAt(buffer, 66)));
		zone1.setValveActualAirEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				70)));
		zone1.setValveActualGasEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				74)));
		zone1.setValveActualAir(new BigDecimal(S7.GetFloatAt(buffer, 78)));
		zone1.setValveActualGas(new BigDecimal(S7.GetFloatAt(buffer, 82)));
		zone1.setExchangeSeconds(S7.GetShortAt(buffer, 86));
		zone1.setChamberPressure(new BigDecimal(S7.GetFloatAt(buffer, 88)));

		// zone2
		zone2.setFireBurningLEFT(S7.GetBitAt(buffer, 0, 2));
		zone2.setFireBurningRIGHT(S7.GetBitAt(buffer, 0, 3));

		try {
			zone2.setAirGasRatioACTUAL(new BigDecimal(S7.GetFloatAt(buffer, 6)));
		} catch (Exception e1) {
			zone2.setAirGasRatioACTUAL(new BigDecimal(0));
		}

		zone2.setAirGasRatioGIVEN(new BigDecimal(S7.GetFloatAt(buffer, 18)));
		zone2.setTemp(new BigDecimal(S7.GetFloatAt(buffer, 92)));
		zone2.setTempTarget(new BigDecimal(S7.GetFloatAt(buffer, 250)));
		zone2.setFlowZoneGASTarget(new BigDecimal(S7.GetFloatAt(buffer, 262)));
		zone2.setFlowZoneGAS(new BigDecimal(S7.GetFloatAt(buffer, 96)));
		zone2.setFlowZoneAIR(new BigDecimal(S7.GetFloatAt(buffer, 100)));
		zone2.setTempAirEjectSmokeLEFT(new BigDecimal(S7
				.GetFloatAt(buffer, 104)));
		zone2.setTempAirEjectSmokeRIGHT(new BigDecimal(S7.GetFloatAt(buffer,
				108)));
		zone2.setTempGasEjectSmokeLEFT(new BigDecimal(S7
				.GetFloatAt(buffer, 112)));
		zone2.setTempGasEjectSmokeRIGHT(new BigDecimal(S7.GetFloatAt(buffer,
				116)));
		zone2.setValveGivenAirEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				120)));
		zone2.setValveGivenGasEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				124)));
		zone2.setValveGivenAir(new BigDecimal(S7.GetFloatAt(buffer, 128)));
		zone2.setValveGivenGas(new BigDecimal(S7.GetFloatAt(buffer, 132)));
		zone2.setValveActualAirEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				136)));
		zone2.setValveActualGasEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				140)));
		zone2.setValveActualAir(new BigDecimal(S7.GetFloatAt(buffer, 144)));
		zone2.setValveActualGas(new BigDecimal(S7.GetFloatAt(buffer, 148)));
		zone2.setExchangeSeconds(S7.GetShortAt(buffer, 152));

		// zone3  (读取 S7.GetBitAt(buffer, 0, 5)会实际上读取0.6的数)
		zone3.setFireBurningLEFT(S7.GetBitAt(buffer, 0, 4));
		zone3.setFireBurningRIGHT(S7.GetBitAt(buffer, 0, 6));
		
		try {
			zone3.setAirGasRatioACTUAL(new BigDecimal(S7.GetFloatAt(buffer, 10)));
		} catch (Exception e) {
			zone3.setAirGasRatioACTUAL(new BigDecimal(0));
		}
		zone3.setAirGasRatioGIVEN(new BigDecimal(S7.GetFloatAt(buffer, 22)));
		zone3.setTemp(new BigDecimal(S7.GetFloatAt(buffer, 154)));
		zone3.setTempTarget(new BigDecimal(S7.GetFloatAt(buffer, 254)));
		zone3.setFlowZoneGASTarget(new BigDecimal(S7.GetFloatAt(buffer, 266)));
		zone3.setFlowZoneGAS(new BigDecimal(S7.GetFloatAt(buffer, 158)));
		zone3.setFlowZoneAIR(new BigDecimal(S7.GetFloatAt(buffer, 162)));
		zone3.setTempAirEjectSmokeLEFT(new BigDecimal(S7
				.GetFloatAt(buffer, 166)));
		zone3.setTempAirEjectSmokeRIGHT(new BigDecimal(S7.GetFloatAt(buffer,
				170)));
		zone3.setTempGasEjectSmokeLEFT(new BigDecimal(S7
				.GetFloatAt(buffer, 174)));
		zone3.setTempGasEjectSmokeRIGHT(new BigDecimal(S7.GetFloatAt(buffer,
				178)));
		zone3.setValveGivenAirEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				182)));
		zone3.setValveGivenGasEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				186)));
		zone3.setValveGivenAir(new BigDecimal(S7.GetFloatAt(buffer, 190)));
		zone3.setValveGivenGas(new BigDecimal(S7.GetFloatAt(buffer, 194)));
		zone3.setValveActualAirEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				198)));
		zone3.setValveActualGasEjectSmoke(new BigDecimal(S7.GetFloatAt(buffer,
				202)));
		zone3.setValveActualAir(new BigDecimal(S7.GetFloatAt(buffer, 206)));
		zone3.setValveActualGas(new BigDecimal(S7.GetFloatAt(buffer, 210)));
		zone3.setExchangeSeconds(S7.GetShortAt(buffer, 214));
		zone3.setChamberPressure(new BigDecimal(S7.GetFloatAt(buffer, 216)));		
	}

	public FurnaceVO() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		this.timeID = formatter.format(currentTime);
		zone1.setTimeID(timeID);
		zone2.setTimeID(timeID);
		zone3.setTimeID(timeID);
	}

	public BigDecimal getGasPressure() {
		return gasPressure;
	}

	public void setGasPressure(BigDecimal gasPressure) {
		this.gasPressure = gasPressure.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getGasValveGiven() {
		return gasValveGiven;
	}

	public void setGasValveGiven(BigDecimal gasValveGiven) {
		this.gasValveGiven = gasValveGiven
				.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getGasValveActual() {
		return gasValveActual;
	}

	public void setGasValveActual(BigDecimal gasValveActual) {
		this.gasValveActual = gasValveActual.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getAirPressure() {
		return airPressure;
	}

	public void setAirPressure(BigDecimal airPressure) {
		this.airPressure = airPressure.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getAirValveGiven() {
		return airValveGiven;
	}

	public void setAirValveGiven(BigDecimal airValveGiven) {
		this.airValveGiven = airValveGiven
				.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getAirValveActual() {
		return airValveActual;
	}

	public void setAirValveActual(BigDecimal airValveActual) {
		this.airValveActual = airValveActual.setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}

	public FurnaceZoneVO getZone1() {
		return zone1;
	}

	public void setZone1(FurnaceZoneVO zone1) {
		this.zone1 = zone1;
	}

	public FurnaceZoneVO getZone2() {
		return zone2;
	}

	public void setZone2(FurnaceZoneVO zone2) {
		this.zone2 = zone2;
	}

	public FurnaceZoneVO getZone3() {
		return zone3;
	}

	public void setZone3(FurnaceZoneVO zone3) {
		this.zone3 = zone3;
	}

	public String getTimeID() {
		return timeID;
	}

	public int getPLCSignal() {
		return PLCSignal;
	}

	public void setPLCSignal(int pLCSignal) {
		PLCSignal = pLCSignal;
	}
	
	public BigDecimal getOxygen() {
		return oxygen;
	}

	public void setOxygen(BigDecimal oxygen) {
		this.oxygen = oxygen;
	}

	public BigDecimal getBilletTemp() {
		return billetTemp;
	}

	public void setBilletTemp(BigDecimal billetTemp) {
		this.billetTemp = billetTemp;
	}

	public FurnaceZoneVO getZoneVO(int zone) {
		switch (zone) {
		case 1:
			return zone1;
		case 2:
			return zone2;
		case 3:
			return zone3;
		}
		return null;
	}

	@Override
	public String toString() {
		return "FurnaceVO [timeID=" + timeID + ", gasPressure=" + gasPressure + ", gasValveGiven=" + gasValveGiven
				+ ", gasValveActual=" + gasValveActual + ", airPressure=" + airPressure + ", airValveGiven="
				+ airValveGiven + ", airValveActual=" + airValveActual + ", oxygen=" + oxygen + ", billetTemp="
				+ billetTemp + ", zone1=" + zone1 + ", zone2=" + zone2 + ", zone3=" + zone3 + ", PLCSignal=" + PLCSignal
				+ "]";
	}	
}
