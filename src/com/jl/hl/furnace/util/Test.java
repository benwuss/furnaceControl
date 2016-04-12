package com.jl.hl.furnace.util;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.jl.hl.furnace.*;
import com.jl.hl.furnace.model.FurnaceVO;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FurnaceVO v1 = new FurnaceVO();
		v1.getZone3().setTemp(new BigDecimal(111));
		v1.getZone3().setTempAirEjectSmokeLEFT(new BigDecimal(170));
		FurnaceVO v2 = new FurnaceVO();
		v2.getZone3().setTemp(new BigDecimal(222));
		v2.getZone3().setTempAirEjectSmokeLEFT(new BigDecimal(160));
		
		FurnaceVO v3 = new FurnaceVO();
		v3.getZone3().setTemp(new BigDecimal(333));
		v3.getZone3().setTempAirEjectSmokeLEFT(new BigDecimal(150));
		
		FurnaceVO v4 = new FurnaceVO();
		v4.getZone3().setTemp(new BigDecimal(444));
		v4.getZone3().setTempAirEjectSmokeLEFT(new BigDecimal(140));
		
		FurnaceVO v5 = new FurnaceVO();
		v5.getZone3().setTemp(new BigDecimal(555));
		v5.getZone3().setTempAirEjectSmokeLEFT(new BigDecimal(130));
		
		FurnaceVO v6 = new FurnaceVO();
		v6.getZone3().setTemp(new BigDecimal(666));
		v6.getZone3().setTempAirEjectSmokeLEFT(new BigDecimal(120));
		
		
		FIFO<FurnaceVO> queue = new FIFO<FurnaceVO>(4);

		
		queue.addLast(v1);
		queue.addLast(v2);
		queue.addLast(v3);
		queue.addLast(v4);
		queue.addLast(v5);
		queue.addLast(v6);
		
		System.out.println(queue.getMostRecentOne().getZone3().getTemp());
		
		
		ArrayList<FurnaceVO> q = queue.getFIFOByASC();
        
        ArrayList<FurnaceVO> l = queue.getFIFOByDSC();
        
        int[] tt = FurnaceUtil.getRecentTempArrayByZone(l,3, 3);
        int[] ss = FurnaceUtil.getRecentSomkeTempArray(l, 3, "A", "L", 3);
        
        System.out.println(ss[0]);
        System.out.println(ss[1]);
        System.out.println(ss[2]);
        
        System.out.println(tt[0]);
        System.out.println(tt[1]);
        System.out.println(tt[2]);
        
        for(FurnaceVO v: l){
        	System.out.println("DSC: " + v.getZone3().getTemp());        	
        }  
        
        for(FurnaceVO v: q){
        	System.out.println("ASC: " + v.getZone3().getTemp());     	
        } 

        int[] t = new int[0];
        System.out.println("tttt:"+t.length);
        

	}

}
