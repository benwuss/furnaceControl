package com.jl.hl.furnace;

import java.util.Comparator;

import com.jl.hl.furnace.model.SmokeValveVO;

public class TemperatureComparator implements Comparator<SmokeValveVO> {

	private boolean isASC = true;

	public int compare(SmokeValveVO o1, SmokeValveVO o2) {

		int result = 0;

		Integer temp1 = new Integer(o1.getTemp());
		Integer temp2 = new Integer(o2.getTemp());

		if (isASC) {

			result = temp1.compareTo(temp2);

		} else {

			result = -temp1.compareTo(temp2);
			
		}

		return result;
	}

	public void setASC(boolean isASC) {
		this.isASC = isASC;
	}
	
}
