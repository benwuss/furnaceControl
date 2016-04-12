package com.jl.hl.furnace;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.model.FurnaceControlVO;
import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.FurnaceZoneVO;
import com.jl.hl.furnace.model.SmokeValveVO;
import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.util.LoggerUtil;

public class FurnaceUtil {

	private final static Logger logger = LoggerUtil.getLogger("TempControl");
	
	public static final String TIME_FORMAT_1 = "yyyyMMddHHmmss.SSS";
	
	public static final String TIME_FORMAT_2 = "yyyy-MM-dd HH:mm:ss";
	
	public static final String TIME_FORMAT_3 = "yyyyMMddHHmmss";
	
	public static void checkValeActualGivenDiff(FurnaceVO fvo) {

		float airValveDiff = fvo.getAirValveGiven()
				.subtract(fvo.getAirValveActual()).abs().floatValue();
		float diff = 2;
		if (airValveDiff > diff) {
			logger.info("请注意！鼓风机频率给定和实际差距大。实际：{},给定：{}。",
					fvo.getAirValveActual(), fvo.getAirValveGiven());
		}
		float gasValveDiff = fvo.getGasValveGiven()
				.subtract(fvo.getGasValveActual()).abs().floatValue();
		if (gasValveDiff > diff) {
			logger.info("请注意！煤气总管阀门给定和实际差距大。实际：{},给定：{}。",
					fvo.getGasValveActual(), fvo.getGasValveGiven());
		}

		FurnaceZoneVO[] zoneVOs = { fvo.getZone1(), fvo.getZone2(),
				fvo.getZone3() };
		for (int i = 0; i < 3; i++) {
			// 1. 空气排烟阀
			float airSmokeDiff = zoneVOs[i].getValveActualAirEjectSomke()
					.subtract(zoneVOs[i].getValveGivenAirEjectSomke()).abs()
					.intValue();
			if (airSmokeDiff > diff) {
				logger.info("请注意！{}空气排烟阀给定和实际差距大。实际：{},给定：{}。",
						zoneVOs[i].getZoneName(),
						zoneVOs[i].getValveActualAirEjectSomke(),
						zoneVOs[i].getValveGivenAirEjectSomke());
			}
			// 2. 煤气排烟阀
			float gasSmokeDiff = zoneVOs[i].getValveActualGasEjectSmoke()
					.subtract(zoneVOs[i].getValveGivenGasEjectSmoke()).abs()
					.intValue();
			if (gasSmokeDiff > diff) {
				logger.info("请注意！{}煤气排烟阀给定和实际差距大。实际：{},给定：{}。",
						zoneVOs[i].getZoneName(),
						zoneVOs[i].getValveActualGasEjectSmoke(),
						zoneVOs[i].getValveGivenGasEjectSmoke());
			}
			// 3. 空气流量阀
			float airDiff = zoneVOs[i].getValveActualAir()
					.subtract(zoneVOs[i].getValveGivenAir()).abs().floatValue();
			if (airDiff > diff) {
				logger.info("请注意！{}空气流量阀给定和实际差距大。实际：{},给定：{}。",
						zoneVOs[i].getZoneName(),
						zoneVOs[i].getValveActualAir(),
						zoneVOs[i].getValveGivenAir());
			}
			// 4. 煤气流量阀
			float gasDiff = zoneVOs[i].getValveActualGas()
					.subtract(zoneVOs[i].getValveGivenGas()).abs().floatValue();
			if (gasDiff > diff) {
				logger.info("请注意！{}煤气流量阀给定和实际差距大。实际：{},给定：{}。",
						zoneVOs[i].getZoneName(),
						zoneVOs[i].getValveActualGas(),
						zoneVOs[i].getValveGivenGas());
			}
		}
	}

	public static void initFurnaceControlVO(FurnaceVO fvo, FurnaceControlVO cvo) {

		cvo.setTimeID(fvo.getTimeID());
		cvo.setAirValve(fvo.getAirValveGiven());
		cvo.setGasValve(fvo.getGasValveGiven());

		cvo.setZ1AirEjectSomkeValve(fvo.getZone1().getValveGivenAirEjectSomke());
		cvo.setZ1GasEjectSomkeValve(fvo.getZone1().getValveGivenGasEjectSmoke());
		cvo.setZ1AirFlowValve(fvo.getZone1().getValveGivenAir());
		cvo.setZ1GasFlowValve(fvo.getZone1().getValveGivenGas());

		cvo.setZ2AirEjectSomkeValve(fvo.getZone2().getValveGivenAirEjectSomke());
		cvo.setZ2GasEjectSomkeValve(fvo.getZone2().getValveGivenGasEjectSmoke());
		cvo.setZ2AirFlowValve(fvo.getZone2().getValveGivenAir());
		cvo.setZ2GasFlowValve(fvo.getZone2().getValveGivenGas());

		cvo.setZ3AirEjectSomkeValve(fvo.getZone3().getValveGivenAirEjectSomke());
		cvo.setZ3GasEjectSomkeValve(fvo.getZone3().getValveGivenGasEjectSmoke());
		cvo.setZ3AirFlowValve(fvo.getZone3().getValveGivenAir());
		cvo.setZ3GasFlowValve(fvo.getZone3().getValveGivenGas());

	}

	public static FurnaceVO getFurnaceVOByTimeID(String timeID,
			FIFO<FurnaceVO> FIFO_Furnace) {
		ArrayList<FurnaceVO> alist = FIFO_Furnace.getFIFOByDSC();
		FurnaceVO r = null;
		for (FurnaceVO v : alist) {
			if (timeID.equals(v.getTimeID())) {
				r = v;
				break;
			}
		}
		return r;
	}

	/**
	 * 获得最近几笔某段的炉顶温度。 一定要配合FIFO的getFIFOByDSC方法取得最近几笔信号数据
	 * 
	 * @param alist
	 * @param zone
	 * @param rows
	 * @return
	 */
	public static int[] getRecentTempArrayByZone(ArrayList<FurnaceVO> alist,
			int zone, int rows) {
		logger.info("alist:{}", alist.size());
		if (alist.size() < rows) {
			rows = alist.size();
		}
		int[] tempArray = new int[rows];
		for (int i = 0; i < rows; i++) {
			tempArray[i] = alist.get(i).getZoneVO(zone).getTemp().intValue();
		}
		return tempArray;
	}

	public static float getRecentValueChangeSum(FIFO<ValveVO> FIFO_Command,
			int zone, String type, String stradegy, String func, int rows) {
		ArrayList<ValveVO> alist = FIFO_Command.getFIFOByDSC();
		ArrayList<ValveVO> wantedList = new ArrayList<ValveVO>();

		for (ValveVO vo : alist) {
			int z = vo.getZone();
			String t = vo.getType();
			String s = vo.getStradegy();
			String f = vo.getFunction();
			if (z == zone && t.equals(type) && s.equals(stradegy)
					&& f.equals(func)) {
				wantedList.add(vo);
				logger.info(vo);
			}
		}

		logger.info("wantedList.size():{}", wantedList.size());

		if (wantedList.size() < rows) {
			rows = wantedList.size();
		}

		long timeGap = 1000;
		if (rows >= 2) {
			timeGap = getGapSeconds(wantedList.get(0).getTimeID(), wantedList
					.get(rows - 1).getTimeID());
		}

		float sum = 0;
		if (timeGap <= 120) {
			for (int i = 0; i < rows; i++) {
				sum = sum + wantedList.get(i).getValueChange().floatValue();
			}
		}

		return sum;
	}

	public static float getRecentActualValvesGap(FIFO<ValveVO> FIFO_Command,
			int zone, String type, String stradegy, String func, int rows) {
		ArrayList<ValveVO> alist = FIFO_Command.getFIFOByDSC();
		ArrayList<ValveVO> wantedList = new ArrayList<ValveVO>();

		for (ValveVO vo : alist) {
			int z = vo.getZone();
			String t = vo.getType();
			String s = vo.getStradegy();
			String f = vo.getFunction();
			if (z == zone && t.equals(type) && s.equals(stradegy)
					&& f.equals(func)) {
				wantedList.add(vo);
			}
		}

		if (wantedList.size() < rows) {
			rows = wantedList.size();
		}

		long timeGap = 1000;
		if (rows >= 2) {
			timeGap = getGapSeconds(wantedList.get(0).getTimeID(), wantedList
					.get(rows - 1).getTimeID());
		}

		float gap = 0;
		if (timeGap <= 60) {
			gap = wantedList.get(0).getOldValue().floatValue()
					- wantedList.get(rows - 1).getOldValue().floatValue();
		}
		return gap;
	}

	public static float getRecentValveGapRatio(FIFO<ValveVO> FIFO_Command,
			int zone, String type, String stradegy, String func, int rows) {
		float r = 10f;
		float gap = getRecentActualValvesGap(FIFO_Command, zone, type,
				stradegy, func, rows);
		float sum = getRecentValueChangeSum(FIFO_Command, zone, type, stradegy,
				func, rows);

		if (gap != 0 && sum != 0) {
			r = gap / sum;
		}

		return r;
	}

	public static int[] getRecentFlowValvesByZones(
			FIFO<FurnaceVO> FIFO_Furnace, int zone, int rows, String type) {
		ArrayList<FurnaceVO> alist = FIFO_Furnace.getFIFOByDSC();
		if (alist.size() < rows) {
			rows = alist.size();
		}
		int[] tempArray = new int[rows];
		for (int i = 0; i < rows; i++) {
			if (type.equals("A")) {
				tempArray[i] = alist.get(i).getZoneVO(zone).getValveActualAir()
						.intValue();
			} else if (type.equals("G")) {
				tempArray[i] = alist.get(i).getZoneVO(zone).getValveActualGas()
						.intValue();
			}
		}
		return tempArray;
	}

	/**
	 * 获得最近几笔某段的排烟温度值
	 * 
	 * @param alist
	 * @param zone
	 * @param type
	 * @param pos
	 * @param rows
	 * @return
	 */
	public static int[] getRecentSomkeTempArray(ArrayList<FurnaceVO> alist,
			int zone, String type, String pos, int rows) {
		if (alist == null || alist.size() == 0) {
			return new int[0];
		}

		if (alist.size() < rows) {
			rows = alist.size();
		}
		int[] tempArray = new int[rows];
		for (int i = 0; i < rows; i++) {
			FurnaceVO fvo = alist.get(i);
			ArrayList<SmokeValveVO> slist = new ArrayList<SmokeValveVO>();
			loadSmokeValveVOs(slist, fvo);

			for (int j = 0; j < slist.size(); j++) {
				SmokeValveVO svo = slist.get(j);
				if (svo.getZone() == zone && svo.getType().equals(type)
						&& svo.getPosition().equals(pos)) {
					tempArray[i] = svo.getTemp();
				}
			}
		}
		return tempArray;
	}

	/**
	 * 获得最近几笔的炉膛压力趋势
	 * 
	 * @param alist
	 * @param zone
	 * @param rows
	 * @return
	 */
	public static float[] getRecentChamberPressure(ArrayList<FurnaceVO> alist,
			int zone, int rows) {

		if (zone == 2) {
			logger.info("加二段没有炉膛压力！只有加一段和均热段才有");
			return null;
		}

		float[] pArray = null;
		if (alist.size() < rows) {
			rows = alist.size();
		}
		pArray = new float[rows];
		for (int i = 0; i < rows; i++) {
			pArray[i] = alist.get(i).getZoneVO(zone).getChamberPressure()
					.floatValue();
		}
		return pArray;
	}

	/**
	 * 将来要给温差同步策略使用，一次同步间隔4秒后，每个2秒空燃比微调，所以看过去5秒的趋势应该 足以判断趋势。例如如果目标-实际大于10，
	 * 而目前升温趋势明显，可以跳过下一次同步升温策略。
	 * 
	 * @param rows
	 * @return 0:stable, 1:up, -1:down, 4:unknown ，5:less than 5 rows
	 * 
	 */
	public static int checkTempTrend(FIFO<FurnaceVO> FIFO_Furnace,
			TargetTempGas target, int zone) {
		int trend = 0;
		int[] t = FurnaceUtil.getRecentTempArrayByZone(
				FIFO_Furnace.getFIFOByDSC(), zone, 5);
		int s = t.length;
		if (s < 5) {
			return 5;
		}
		FurnaceVO v = FIFO_Furnace.getMostRecentOne();
		logger.info("Target1:Now1={}:{},Target2:Now2={}:{},Target3:Now3={}:{}",
				target.getTempZone1(), v.getZone1().getTemp(),
				target.getTempZone2(), v.getZone2().getTemp(),
				target.getTempZone3(), v.getZone3().getTemp());
		logger.info("Zone {} : Temps : {},{},{},{},{}.", zone, t[0], t[1],
				t[2], t[3], t[4]);
		int g1 = t[0] - t[1];
		int g2 = t[1] - t[2];
		int g3 = t[2] - t[3];
		int g4 = t[3] - t[4];
		int sum = g1 + g2 + g3 + g4;

		if (sum >= 1) {
			trend = 1;
			logger.info("Zone {}, 升温中，trend:{}", zone, trend);
		} else if (sum <= -1) {
			trend = -1;
			logger.info("Zone {}, 降温中，trend:{}", zone, trend);
		}
		return trend;
	}

	public static void loadSmokeValveVOs(
			ArrayList<SmokeValveVO> smokeValveList, FurnaceVO fvo) {
		// 加一段
		SmokeValveVO z1AirRIGHT = genSmokeVO(fvo.getZone1(),
				SmokeValveVO.AIR_SMOKE, SmokeValveVO.TEMP_POS_RIGHT);
		SmokeValveVO z1AirLEFT = genSmokeVO(fvo.getZone1(),
				SmokeValveVO.AIR_SMOKE, SmokeValveVO.TEMP_POS_LEFT);
		SmokeValveVO z1GasRIGHT = genSmokeVO(fvo.getZone1(),
				SmokeValveVO.GAS_SMOKE, SmokeValveVO.TEMP_POS_RIGHT);
		SmokeValveVO z1GasLEFT = genSmokeVO(fvo.getZone1(),
				SmokeValveVO.GAS_SMOKE, SmokeValveVO.TEMP_POS_LEFT);
		// 加二段，本段没有炉膛压力，但要参与上下段炉膛压力的判断。二者都下指令时，以均热段为主
		SmokeValveVO z2AirRIGHT = genSmokeVO(fvo.getZone2(),
				SmokeValveVO.AIR_SMOKE, SmokeValveVO.TEMP_POS_RIGHT);
		SmokeValveVO z2AirLEFT = genSmokeVO(fvo.getZone2(),
				SmokeValveVO.AIR_SMOKE, SmokeValveVO.TEMP_POS_LEFT);
		SmokeValveVO z2GasRIGHT = genSmokeVO(fvo.getZone2(),
				SmokeValveVO.GAS_SMOKE, SmokeValveVO.TEMP_POS_RIGHT);
		SmokeValveVO z2GasLEFT = genSmokeVO(fvo.getZone2(),
				SmokeValveVO.GAS_SMOKE, SmokeValveVO.TEMP_POS_LEFT);
		// 均热段
		SmokeValveVO z3AirRIGHT = genSmokeVO(fvo.getZone3(),
				SmokeValveVO.AIR_SMOKE, SmokeValveVO.TEMP_POS_RIGHT);
		SmokeValveVO z3AirLEFT = genSmokeVO(fvo.getZone3(),
				SmokeValveVO.AIR_SMOKE, SmokeValveVO.TEMP_POS_LEFT);
		SmokeValveVO z3GasRIGHT = genSmokeVO(fvo.getZone3(),
				SmokeValveVO.GAS_SMOKE, SmokeValveVO.TEMP_POS_RIGHT);
		SmokeValveVO z3GasLEFT = genSmokeVO(fvo.getZone3(),
				SmokeValveVO.GAS_SMOKE, SmokeValveVO.TEMP_POS_LEFT);

		smokeValveList.add(z1AirRIGHT);
		smokeValveList.add(z1AirLEFT);
		smokeValveList.add(z1GasRIGHT);
		smokeValveList.add(z1GasLEFT);

		smokeValveList.add(z2AirRIGHT);
		smokeValveList.add(z2AirLEFT);
		smokeValveList.add(z2GasRIGHT);
		smokeValveList.add(z2GasLEFT);

		smokeValveList.add(z3AirRIGHT);
		smokeValveList.add(z3AirLEFT);
		smokeValveList.add(z3GasRIGHT);
		smokeValveList.add(z3GasLEFT);
	}

	public static SmokeValveVO genSmokeVO(FurnaceZoneVO fzo, String type,
			String tempPosition) {
		SmokeValveVO vo = new SmokeValveVO();
		vo.setZone(fzo.getZoneID());
		vo.setPosition(tempPosition);
		vo.setType(type);
		vo.setZoneName(fzo.getZoneName());
		vo.setExchangeSeconds(fzo.getExchangeSeconds());

		if (type.equals(SmokeValveVO.AIR_SMOKE)) {
			vo.setSmokeValve(fzo.getValveActualAirEjectSomke().intValue());
			vo.setFlow(fzo.getFlowZoneAIR().intValue());
		} else {
			vo.setSmokeValve(fzo.getValveActualGasEjectSmoke().intValue());
			vo.setFlow(fzo.getFlowZoneGAS().intValue());
		}
		if (type.equals(SmokeValveVO.AIR_SMOKE)
				&& tempPosition.equals(SmokeValveVO.TEMP_POS_LEFT)) {
			vo.setTemp(fzo.getTempAirEjectSmokeLEFT().intValue());
			vo.setFireNearBy(fzo.isFireBurningLEFT());
		} else if (type.equals(SmokeValveVO.AIR_SMOKE)
				&& tempPosition.equals(SmokeValveVO.TEMP_POS_RIGHT)) {
			vo.setTemp(fzo.getTempAirEjectSmokeRIGHT().intValue());
			vo.setFireNearBy(fzo.isFireBurningRIGHT());
		}
		if (type.equals(SmokeValveVO.GAS_SMOKE)
				&& tempPosition.equals(SmokeValveVO.TEMP_POS_LEFT)) {
			vo.setTemp(fzo.getTempGasEjectSomkeLEFT().intValue());
			vo.setFireNearBy(fzo.isFireBurningLEFT());
		} else if (type.equals(SmokeValveVO.GAS_SMOKE)
				&& tempPosition.equals(SmokeValveVO.TEMP_POS_RIGHT)) {
			vo.setTemp(fzo.getTempGasEjectSmokeRIGHT().intValue());
			vo.setFireNearBy(fzo.isFireBurningRIGHT());
		}
		return vo;
	}

	private static long getGapSeconds(String recent, String farAway) {
		long seconds = 0;
		SimpleDateFormat dd = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		try {
			Date d1 = dd.parse(recent);
			Date d2 = dd.parse(farAway);
			seconds = (d1.getTime() - d2.getTime()) / 1000;
		} catch (ParseException e) {
			// do nothing.
		}

		return seconds;
	}
	
	public static String change2Format1(String fromRunningTime){
		String returnFormat = "";
		SimpleDateFormat dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d1 = dd.parse(fromRunningTime);
			SimpleDateFormat dd2 = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
			returnFormat = dd2.format(d1);			 
		} catch (ParseException e) {
		    // do nothing
		}
		return returnFormat;		
	}
	
	public static String change2Format2(String timeFormat1){
		String returnFormat = "";
		SimpleDateFormat dd = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date d1 = dd.parse(timeFormat1);
			SimpleDateFormat dd2 = new SimpleDateFormat(TIME_FORMAT_2);
			returnFormat = dd2.format(d1);			 
		} catch (ParseException e) {
		    // do nothing
		}
		return returnFormat;		
	}

	public static String genTimeID() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(currentTime);
	}

	public static String getTimeFormat(long timemills) {
		Date date = new Date(timemills);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
	
	public static String getTimeFormat1(long timemills) {
		Date date = new Date(timemills);
		SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT_1);
		return formatter.format(date);
	}
	
	public static String getTimeFormat3(long timemills) {
		Date date = new Date(timemills);
		SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT_3);
		return formatter.format(date);
	}
	
	public static int getRuningMins(long lastTime){
		long now = System.currentTimeMillis();
		int mins = (int)((now - lastTime) / 1000 / 60);
		return mins;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
