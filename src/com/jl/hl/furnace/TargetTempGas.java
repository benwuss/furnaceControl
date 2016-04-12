package com.jl.hl.furnace;

import java.math.BigDecimal;

public class TargetTempGas {
	
	private BigDecimal tempZone1 = new BigDecimal(0);
	
	private BigDecimal tempZone2 = new BigDecimal(0);
	
	private BigDecimal tempZone3 = new BigDecimal(0);
	
    private BigDecimal gasZone1 = new BigDecimal(0);
	
	private BigDecimal gasZone2 = new BigDecimal(0);
	
	private BigDecimal gasZone3 = new BigDecimal(0);
	
	public TargetTempGas() {
		
	}
	
	public BigDecimal[] getTempsArray() {
		BigDecimal[] temps = {tempZone1, tempZone2, tempZone3};
		return temps;		
	}
	
	public BigDecimal[] getGasArray() {
		BigDecimal[] gas = {gasZone1, gasZone2, gasZone3};
		return gas;		
	}

	public BigDecimal getTempZone1() {
		return tempZone1;
	}

	public void setTempZone1(BigDecimal tempZone1) {
		this.tempZone1 = tempZone1;
	}

	public BigDecimal getTempZone2() {
		return tempZone2;
	}

	public void setTempZone2(BigDecimal tempZone2) {
		this.tempZone2 = tempZone2;
	}

	public BigDecimal getTempZone3() {
		return tempZone3;
	}

	public void setTempZone3(BigDecimal tempZone3) {
		this.tempZone3 = tempZone3;
	}

	public BigDecimal getGasZone1() {
		return gasZone1;
	}

	public void setGasZone1(BigDecimal gasZone1) {
		this.gasZone1 = gasZone1;
	}

	public BigDecimal getGasZone2() {
		return gasZone2;
	}

	public void setGasZone2(BigDecimal gasZone2) {
		this.gasZone2 = gasZone2;
	}

	public BigDecimal getGasZone3() {
		return gasZone3;
	}

	public void setGasZone3(BigDecimal gasZone3) {
		this.gasZone3 = gasZone3;
	}

	@Override
	public String toString() {
		return "TargetTempGas [tempZone1=" + tempZone1 + ", tempZone2="
				+ tempZone2 + ", tempZone3=" + tempZone3 + ", gasZone1="
				+ gasZone1 + ", gasZone2=" + gasZone2 + ", gasZone3="
				+ gasZone3 + "]";
	}
}
