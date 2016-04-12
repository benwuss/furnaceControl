package com.jl.hl.furnace.model;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;


import com.jl.hl.furnace.plc.moka7.S7;
import com.jl.hl.furnace.util.LoggerUtil;


public class FurnaceControlVO {
	
	private final static Logger logger = LoggerUtil
			.getLogger(FurnaceControlVO.class.getSimpleName());

	// yyyyMMddHHmmss.SSS
	private String timeID;
	
	public static final int STRADEGY_SYNCO = 1;
	
	public static final int STRADEGY_AIR_GAS_RATIO = 2;
	
	public static final int STRADEGY_CAMBER_PRESSURE = 3;
	
	public static final int STRADEGY_SMOKE_TEMP = 4;
	
	public static final int STRADEGY_WALK_W_FLOW = 5;
	
	private int stradegyType = 0;

	private BigDecimal Z1AirEjectSomkeValve = new BigDecimal(-1);

	private BigDecimal Z1GasEjectSomkeValve = new BigDecimal(-1);

	private BigDecimal Z1AirFlowValve = new BigDecimal(-1);

	private BigDecimal Z1GasFlowValve = new BigDecimal(-1);
	
	private BigDecimal Z1AirEjectSomkeValveChange = new BigDecimal(-1);

	private BigDecimal Z1GasEjectSomkeValveChange = new BigDecimal(-1);

	private BigDecimal Z1AirFlowValveChange = new BigDecimal(-1);

	private BigDecimal Z1GasFlowValveChange = new BigDecimal(-1);

	private BigDecimal Z2AirEjectSomkeValve = new BigDecimal(-1);

	private BigDecimal Z2GasEjectSomkeValve = new BigDecimal(-1);

	private BigDecimal Z2AirFlowValve = new BigDecimal(-1);

	private BigDecimal Z2GasFlowValve = new BigDecimal(-1);
	
	private BigDecimal Z2AirEjectSomkeValveChange = new BigDecimal(-1);

	private BigDecimal Z2GasEjectSomkeValveChange = new BigDecimal(-1);

	private BigDecimal Z2AirFlowValveChange = new BigDecimal(-1);

	private BigDecimal Z2GasFlowValveChange = new BigDecimal(-1);

	private BigDecimal Z3AirEjectSomkeValve = new BigDecimal(-1);

	private BigDecimal Z3GasEjectSomkeValve = new BigDecimal(-1);

	private BigDecimal Z3AirFlowValve = new BigDecimal(-1);

	private BigDecimal Z3GasFlowValve = new BigDecimal(-1);
	
	private BigDecimal Z3AirEjectSomkeValveChange = new BigDecimal(-1);

	private BigDecimal Z3GasEjectSomkeValveChange = new BigDecimal(-1);

	private BigDecimal Z3AirFlowValveChange = new BigDecimal(-1);

	private BigDecimal Z3GasFlowValveChange = new BigDecimal(-1);

	private BigDecimal gasValve = new BigDecimal(-1);

	private BigDecimal airValve = new BigDecimal(-1);

	public byte[] getBytes() {
		
		byte[] buffer = new byte[56];
		S7.SetFloatAt(buffer, 0, Z1AirEjectSomkeValve.floatValue());
		S7.SetFloatAt(buffer, 4, Z1GasEjectSomkeValve.floatValue());
		S7.SetFloatAt(buffer, 8, Z1AirFlowValve.floatValue());
		S7.SetFloatAt(buffer, 12, Z1GasFlowValve.floatValue());

		S7.SetFloatAt(buffer, 16, Z2AirEjectSomkeValve.floatValue());
		S7.SetFloatAt(buffer, 20, Z2GasEjectSomkeValve.floatValue());
		S7.SetFloatAt(buffer, 24, Z2AirFlowValve.floatValue());
		S7.SetFloatAt(buffer, 28, Z2GasFlowValve.floatValue());

		S7.SetFloatAt(buffer, 32, Z3AirEjectSomkeValve.floatValue());
		S7.SetFloatAt(buffer, 36, Z3GasEjectSomkeValve.floatValue());
		S7.SetFloatAt(buffer, 40, Z3AirFlowValve.floatValue());
		S7.SetFloatAt(buffer, 44, Z3GasFlowValve.floatValue());

		S7.SetFloatAt(buffer, 48, gasValve.floatValue());
		S7.SetFloatAt(buffer, 52, airValve.floatValue());

		logger.info("set Data into S7:" + toString());
		
		return buffer;
	}

	public String getTimeID() {
		return timeID;
	}	
	

	public int getStradegyType() {
		return stradegyType;
	}

	public void setStradegyType(int stradegyType) {
		this.stradegyType = stradegyType;
	}

	// set by FurnaceVO timeID
	public void setTimeID(String timeID) {
		this.timeID = timeID;
	}

	public BigDecimal getZ1AirEjectSomkeValve() {
		return Z1AirEjectSomkeValve;
	}

	public void setZ1AirEjectSomkeValve(BigDecimal z1AirEjectSomkeValve) {
		this.Z1AirEjectSomkeValve = z1AirEjectSomkeValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z1AirEjectSomkeValve.intValue() < 0)
			this.Z1AirEjectSomkeValve = new BigDecimal(0);
		
	}

	public BigDecimal getZ1GasEjectSomkeValve() {
		return Z1GasEjectSomkeValve;
	}

	public void setZ1GasEjectSomkeValve(BigDecimal z1GasEjectSomkeValve) {
		this.Z1GasEjectSomkeValve = z1GasEjectSomkeValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z1GasEjectSomkeValve.intValue() < 0)
			this.Z1GasEjectSomkeValve = new BigDecimal(0);
	}

	public BigDecimal getZ1AirFlowValve() {
		return Z1AirFlowValve;
	}

	public void setZ1AirFlowValve(BigDecimal z1AirFlowValve) {
		this.Z1AirFlowValve = z1AirFlowValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z1AirFlowValve.intValue() < 0)
			this.Z1AirFlowValve = new BigDecimal(0);
	}

	public BigDecimal getZ1GasFlowValve() {
		return Z1GasFlowValve;
	}

	public void setZ1GasFlowValve(BigDecimal z1GasFlowValve) {
		this.Z1GasFlowValve = z1GasFlowValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z1GasFlowValve.intValue() < 0)
			this.Z1GasFlowValve = new BigDecimal(0);
	}

	public BigDecimal getZ2AirEjectSomkeValve() {
		return Z2AirEjectSomkeValve;
	}

	public void setZ2AirEjectSomkeValve(BigDecimal z2AirEjectSomkeValve) {
		this.Z2AirEjectSomkeValve = z2AirEjectSomkeValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z2AirEjectSomkeValve.intValue() < 0)
			this.Z2AirEjectSomkeValve = new BigDecimal(0);
	}

	public BigDecimal getZ2GasEjectSomkeValve() {
		return Z2GasEjectSomkeValve;
	}

	public void setZ2GasEjectSomkeValve(BigDecimal z2GasEjectSomkeValve) {
		this.Z2GasEjectSomkeValve = z2GasEjectSomkeValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z2GasEjectSomkeValve.intValue() < 0)
			this.Z2GasEjectSomkeValve = new BigDecimal(0);
	}

	public BigDecimal getZ2AirFlowValve() {
		return Z2AirFlowValve;
	}

	public void setZ2AirFlowValve(BigDecimal z2AirFlowValve) {
		this.Z2AirFlowValve = z2AirFlowValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z2AirFlowValve.intValue() < 0)
			this.Z2AirFlowValve = new BigDecimal(0);
	}

	public BigDecimal getZ2GasFlowValve() {
		return Z2GasFlowValve;
	}

	public void setZ2GasFlowValve(BigDecimal z2GasFlowValve) {
		this.Z2GasFlowValve = z2GasFlowValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z2GasFlowValve.intValue() < 0)
			this.Z2GasFlowValve = new BigDecimal(0);
	}

	public BigDecimal getZ3AirEjectSomkeValve() {
		return Z3AirEjectSomkeValve;
	}

	public void setZ3AirEjectSomkeValve(BigDecimal z3AirEjectSomkeValve) {
		this.Z3AirEjectSomkeValve = z3AirEjectSomkeValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z3AirEjectSomkeValve.intValue() < 0)
			this.Z3AirEjectSomkeValve = new BigDecimal(0);
	}

	public BigDecimal getZ3GasEjectSomkeValve() {
		return Z3GasEjectSomkeValve;
	}

	public void setZ3GasEjectSomkeValve(BigDecimal z3GasEjectSomkeValve) {
		this.Z3GasEjectSomkeValve = z3GasEjectSomkeValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z3GasEjectSomkeValve.intValue() < 0)
			this.Z3GasEjectSomkeValve = new BigDecimal(0);
	}

	public BigDecimal getZ3AirFlowValve() {
		return Z3AirFlowValve;
	}

	public void setZ3AirFlowValve(BigDecimal z3AirFlowValve) {
		this.Z3AirFlowValve = z3AirFlowValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z3AirFlowValve.intValue() < 0)
			this.Z3AirFlowValve = new BigDecimal(0);
	}

	public BigDecimal getZ3GasFlowValve() {
		return Z3GasFlowValve;
	}

	public void setZ3GasFlowValve(BigDecimal z3GasFlowValve) {
		this.Z3GasFlowValve = z3GasFlowValve.setScale(1,
				BigDecimal.ROUND_HALF_UP);
		if(this.Z3GasFlowValve.intValue() < 0)
			this.Z3GasFlowValve = new BigDecimal(0);
	}

	public BigDecimal getGasValve() {
		return gasValve;
	}

	public void setGasValve(BigDecimal gasValve) {
		this.gasValve = gasValve.setScale(1, BigDecimal.ROUND_HALF_UP);
		if(this.gasValve.intValue() < 0)
			this.gasValve = new BigDecimal(0);
	}

	public BigDecimal getAirValve() {
		return airValve;
	}

	public void setAirValve(BigDecimal airValve) {
		this.airValve = airValve.setScale(1, BigDecimal.ROUND_HALF_UP);
		if(this.airValve.intValue() < 0)
			this.airValve = new BigDecimal(0);
	}

	public static void main(String[] args) {
		FurnaceControlVO fcvo = new FurnaceControlVO();
		fcvo.setAirValve(new BigDecimal(50.12345));
		fcvo.setGasValve(new BigDecimal(30.56789));

		System.out.println(fcvo.toString());
	}
	
	public BigDecimal getZ1AirEjectSomkeValveChange() {
		return Z1AirEjectSomkeValveChange;
	}

	public void setZ1AirEjectSomkeValveChange(BigDecimal z1AirEjectSomkeValveChange) {
		Z1AirEjectSomkeValveChange = z1AirEjectSomkeValveChange;
	}

	public BigDecimal getZ1GasEjectSomkeValveChange() {
		return Z1GasEjectSomkeValveChange;
	}

	public void setZ1GasEjectSomkeValveChange(BigDecimal z1GasEjectSomkeValveChange) {
		Z1GasEjectSomkeValveChange = z1GasEjectSomkeValveChange;
	}

	public BigDecimal getZ1AirFlowValveChange() {
		return Z1AirFlowValveChange;
	}

	public void setZ1AirFlowValveChange(BigDecimal z1AirFlowValveChange) {
		Z1AirFlowValveChange = z1AirFlowValveChange;
	}

	public BigDecimal getZ1GasFlowValveChange() {
		return Z1GasFlowValveChange;
	}

	public void setZ1GasFlowValveChange(BigDecimal z1GasFlowValveChange) {
		Z1GasFlowValveChange = z1GasFlowValveChange;
	}

	public BigDecimal getZ2AirEjectSomkeValveChange() {
		return Z2AirEjectSomkeValveChange;
	}

	public void setZ2AirEjectSomkeValveChange(BigDecimal z2AirEjectSomkeValveChange) {
		Z2AirEjectSomkeValveChange = z2AirEjectSomkeValveChange;
	}

	public BigDecimal getZ2GasEjectSomkeValveChange() {
		return Z2GasEjectSomkeValveChange;
	}

	public void setZ2GasEjectSomkeValveChange(BigDecimal z2GasEjectSomkeValveChange) {
		Z2GasEjectSomkeValveChange = z2GasEjectSomkeValveChange;
	}

	public BigDecimal getZ2AirFlowValveChange() {
		return Z2AirFlowValveChange;
	}

	public void setZ2AirFlowValveChange(BigDecimal z2AirFlowValveChange) {
		Z2AirFlowValveChange = z2AirFlowValveChange;
	}

	public BigDecimal getZ2GasFlowValveChange() {
		return Z2GasFlowValveChange;
	}

	public void setZ2GasFlowValveChange(BigDecimal z2GasFlowValveChange) {
		Z2GasFlowValveChange = z2GasFlowValveChange;
	}

	public BigDecimal getZ3AirEjectSomkeValveChange() {
		return Z3AirEjectSomkeValveChange;
	}

	public void setZ3AirEjectSomkeValveChange(BigDecimal z3AirEjectSomkeValveChange) {
		Z3AirEjectSomkeValveChange = z3AirEjectSomkeValveChange;
	}

	public BigDecimal getZ3GasEjectSomkeValveChange() {
		return Z3GasEjectSomkeValveChange;
	}

	public void setZ3GasEjectSomkeValveChange(BigDecimal z3GasEjectSomkeValveChange) {
		Z3GasEjectSomkeValveChange = z3GasEjectSomkeValveChange;
	}

	public BigDecimal getZ3AirFlowValveChange() {
		return Z3AirFlowValveChange;
	}

	public void setZ3AirFlowValveChange(BigDecimal z3AirFlowValveChange) {
		Z3AirFlowValveChange = z3AirFlowValveChange;
	}

	public BigDecimal getZ3GasFlowValveChange() {
		return Z3GasFlowValveChange;
	}

	public void setZ3GasFlowValveChange(BigDecimal z3GasFlowValveChange) {
		Z3GasFlowValveChange = z3GasFlowValveChange;
	}

	@Override
	public String toString() {
		return "FurnaceControlVO [timeID=" + timeID + ", Z1AirEjectSomkeValve="
				+ Z1AirEjectSomkeValve + ", Z1GasEjectSomkeValve="
				+ Z1GasEjectSomkeValve + ", Z1AirFlowValve=" + Z1AirFlowValve
				+ ", Z1GasFlowValve=" + Z1GasFlowValve
				+ ", Z2AirEjectSomkeValve=" + Z2AirEjectSomkeValve
				+ ", Z2GasEjectSomkeValve=" + Z2GasEjectSomkeValve
				+ ", Z2AirFlowValve=" + Z2AirFlowValve + ", Z2GasFlowValve="
				+ Z2GasFlowValve + ", Z3AirEjectSomkeValve="
				+ Z3AirEjectSomkeValve + ", Z3GasEjectSomkeValve="
				+ Z3GasEjectSomkeValve + ", Z3AirFlowValve=" + Z3AirFlowValve
				+ ", Z3GasFlowValve=" + Z3GasFlowValve + ", gasValveMain="
				+ gasValve + ", airValveMain=" + airValve + "]";
	}
}
