package com.jl.hl.furnace.ui;

import java.math.BigDecimal;
import java.util.Random;

import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.FurnaceZoneVO;

public class FurnaceTest {
	
	public static FurnaceVO genFurnaceVO(){
		FurnaceVO fvo = new FurnaceVO();
		FurnaceZoneVO[] zvos = {new FurnaceZoneVO(1), new FurnaceZoneVO(2), new FurnaceZoneVO(3)};
		
	    for(FurnaceZoneVO vo : zvos){
	    	vo.setTemp(new BigDecimal(gR(200, 1000)));
	    	vo.setTempTarget(new BigDecimal(gR(200, 1000)));
	    	vo.setFlowZoneGAS(new BigDecimal(gR(20000, 1000)));
	    	vo.setFlowZoneGASTarget(new BigDecimal(gR(20000, 1000)));	 	    	
	    }
	    fvo.setZone1(zvos[0]);		
	    fvo.setZone2(zvos[1]);	
	    fvo.setZone3(zvos[2]);		
	    fvo.setPLCSignal(2);
		
		return fvo;
	}
	
	public static int gR(int x, int y){
		Random rand = new Random();		
		return rand.nextInt(x)+y;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
