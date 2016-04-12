package com.jl.hl.furnace;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.model.FurnaceControlVO;
import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.SmokeValveVO;
import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.util.LoggerUtil;

public class EjectingSmokeValveControl {

	private final static Logger logger = LoggerUtil.getLogger(EjectingSmokeValveControl.class.getSimpleName());

	public static int CHAMBER3_PRESSURE_MAX = 25;

	public static int CHAMBER3_PRESSURE_MIN = 20;

	public static int CHAMBER1_PRESSURE_MAX = 20;

	public static int CHAMBER1_PRESSURE_MIN = 15;

	public static int SMOKE_VALVE_SPAN = 5;

	public static int SMOKE_VALVE_MIN = 0;

	public static int SMOKE_VALVE_SPAN_TOOLOW = 10;

	public static int SMOKE_TEMP_MAX = 160;

	public static int SMOKE_TEMP_MIN = 100;

	private StringBuffer msg = new StringBuffer();

	private FurnaceVO fvo = null;

	private ArrayList<ValveVO> valveList = new ArrayList<ValveVO>();

	public EjectingSmokeValveControl() {

	}

	public ArrayList<ValveVO> getChangedValves() {
		return valveList;
	}

	/**
	 * 排烟温度信号检测时，只检测燃烧方向对侧温度点，并且检测时间点需在同一段的换向时间≥6s区间内。
	 * 排烟温度检测周期每隔6S一次（考虑到温度变化的延后性），观察上次及本次检测温度变化,如果有变化，本次就不调整。
	 * 
	 * @param fvo
	 *            本次的信号记录
	 * @param refVO
	 *            6秒前作为判断的信号记录
	 * @param oldVOs
	 *            过去10秒的信号记录,用来判断温度趋势
	 * @return
	 */
	public void smokeTempTooHighStradegy(FIFO<FurnaceVO> FIFO_Furnace) {
		msg = new StringBuffer();
		valveList = new ArrayList<ValveVO>();

		msg.append("进入排烟温度过高策略。\n");

		fvo = FIFO_Furnace.getMostRecentOne(); // 取得最新一笔信号数据。
		ArrayList<SmokeValveVO> nowSmokes = new ArrayList<SmokeValveVO>();
		FurnaceUtil.loadSmokeValveVOs(nowSmokes, fvo);
		int result = 0;

		for (int i = 0; i < nowSmokes.size(); i++) {
			SmokeValveVO so = nowSmokes.get(i);

			// 现场测试后增加的判断，如果两边都一样，代表刚刚换向，这个阀位就不调整。
			if (isBothPosTheSame(so, nowSmokes)) {
				continue;
			}

			// 查过去5秒温度有没有下降的趋势。
			int[] ta = FurnaceUtil.getRecentSomkeTempArray(FIFO_Furnace.getFIFOByDSC(), so.getZone(), so.getType(),
					so.getPosition(), 6);
			if (ta.length > 4) {
				int t1 = ta[0];
				int t2 = ta[4];
				int g1 = t1 - t2;
				if (g1 <= -1) {
					msg.append("有显著降温趋势，本排烟温度跳过。\n");
					continue;
				}
			}

			boolean isGotFire = so.isFireNearBy();
			int temp = so.getTemp();
			int exTime = so.getExchangeSeconds();

			// 排烟温度信号检测时，只检测燃烧方向对侧温度点，并且检测时间点需在同一段的换向时间≥6s区间内
			if (temp > SMOKE_TEMP_MAX && isGotFire == false && exTime >= 6) {

				msg.append("侦测到排烟温度异常位置：" + so.getZoneName() + "\n");

				float newValve = so.getSmokeValve() - SMOKE_VALVE_SPAN;
				if (newValve < 0) {
					newValve = 0;
				}
				float valveChange = newValve - so.getSmokeValve();

				msg.append(so.getSmokeValveName()).append("因排烟温度过大关闭阀位为：").append(newValve).append("\n");
				setSmokeVONewValve(nowSmokes, so, newValve, valveChange, ValveVO.S_STRADEGY);
				// 代表有排烟阀被调整阀位
				result = 1;
			}
		}
		if (result == 0) {
			msg.append("所有排烟温度都正常！排烟策略本次不执行。\n");
		}
		logger.info(msg.toString());
	}

	/**
	 * 排烟温度过低检测策略
	 * 
	 * @param FIFO_Furnace
	 */
	public void smokeTempTooLowStradegy(FIFO<FurnaceVO> FIFO_Furnace) {
		msg = new StringBuffer();
		valveList = new ArrayList<ValveVO>();

		msg.append("进入排烟温度过低策略。\n");

		fvo = FIFO_Furnace.getMostRecentOne(); // 取得最新一笔信号数据。
		ArrayList<SmokeValveVO> nowSmokes = new ArrayList<SmokeValveVO>();
		FurnaceUtil.loadSmokeValveVOs(nowSmokes, fvo);

		for (int i = 0; i < nowSmokes.size(); i++) {
			SmokeValveVO so = nowSmokes.get(i);

			// 全过程暂时跳过均热段右侧煤气排烟温度
			if (so.getZone() == 3 && so.getType().equals("G") && so.getPosition().equals("R")) {
				continue;
			}

			int temp = so.getTemp();

			// 判定依据，较低温度于于100℃。
			if (temp < 100) {
				SmokeValveVO opposite = getOppositeSmoke(nowSmokes, so);
				// 如出现一个阀门的两个排烟温度中，一个大于140℃，另一个小于100℃时，则不执行。
				if (opposite.getTemp() > 140) {
					msg.append(so.getSmokeValveName() + "面对的排烟温度大于140，跳过不执行。\n");
					continue;
				}

				// 查过去10秒温度有没有上升的趋势。
				int[] ta = FurnaceUtil.getRecentSomkeTempArray(FIFO_Furnace.getFIFOByDSC(), so.getZone(), so.getType(),
						so.getPosition(), 12);
				if (ta.length > 10) {
					int t1 = ta[0];
					int t2 = ta[9];
					int g = t1 - t2;
					if (g >= 1) {
						msg.append("有显著升温趋势，本次排烟温对应的阀位不调整。\n");
						continue;
					}
				}

				float newValve = so.getSmokeValve() + SMOKE_VALVE_SPAN_TOOLOW;

				int maxValve = getMaxSmokeValve(so.getZone(), so.getType());
				// 设定排烟阀最大值
				if (newValve > maxValve) {
					newValve = maxValve;
				}

				float valveChange = newValve - so.getSmokeValve();

				ValveVO vs = new ValveVO(so.getZone(), so.getType(), "S");
				vs.setTimeID(fvo.getTimeID());
				vs.setNewValue(new BigDecimal(newValve));
				vs.setStradegy(ValveVO.H_STRADEGY);
				vs.setValueChange(new BigDecimal(valveChange));
				vs.setOldValue(new BigDecimal(so.getSmokeValve()));
				valveListFilter(valveList, vs);
			}
		}
		logger.info(msg.toString());
	}

	/**
	 * 返回对面的排烟阀
	 * 
	 * @param nowSmokes
	 * @param thisOne
	 * @return
	 */
	private SmokeValveVO getOppositeSmoke(ArrayList<SmokeValveVO> nowSmokes, SmokeValveVO thisOne) {
		SmokeValveVO returnVO = null;
		int zone = thisOne.getZone();
		String type = thisOne.getType();
		String pos = thisOne.getPosition();
		for (SmokeValveVO vo : nowSmokes) {
			if (vo.getZone() == zone && vo.getType().equals(type) && !vo.getPosition().equals(pos)) {
				returnVO = vo;
				break;
			}
		}
		return returnVO;
	}

	private boolean isBothPosTheSame(SmokeValveVO so, ArrayList<SmokeValveVO> svs) {
		boolean result = false;
		String type = so.getType();
		int zone = so.getZone();
		String pos = so.getPosition();
		for (int i = 0; i < svs.size(); i++) {
			SmokeValveVO vo = svs.get(i);
			String st = vo.getType();
			int sz = vo.getZone();
			String sp = vo.getPosition();
			if (st.equals(type) && sz == zone && !sp.equals(pos)) {
				if (so.isFireNearBy() == vo.isFireNearBy()) {
					result = true;
					// 找到就跳出了。
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 烟气阀随流量阀联动策略。必须在同步策略执行完后再进入本策略。 1. 计算同步策略里，根据空气阀和煤气阀的变化在判断烟气阀的开度。 2.
	 * 要设定烟气阀的合理范围，若系统得出的阀位给定超过范围，要警示。 3. 同时考虑烟气温度和炉压，若超过合理范围给出警示消息，留给炉压检测策略处理。
	 * 
	 * @param fvo
	 * @param cvo
	 * @param target
	 */
	public void walkWithFlowToZeroStradegy(FIFO<FurnaceVO> FIFO_Furnace) {

		msg = new StringBuffer();
		valveList = new ArrayList<ValveVO>();
		fvo = FIFO_Furnace.getMostRecentOne(); // 取得最新一笔信号数据。

		msg.append("进入均热段流量阀过低与排烟阀联动策略。\n");

		float flowGasGiven = fvo.getZone3().getValveGivenGas().floatValue();
		float flowAirGiven = fvo.getZone3().getValveGivenAir().floatValue();
		if (flowGasGiven <= 3) {
			msg.append("执行均热段煤气排烟阀联动策略，因煤气流量给定为零，所以煤气排烟阀也关至零。\n");
			float smokeGasActual = fvo.getZone3().getValveActualGasEjectSmoke().floatValue();
			ValveVO vs = new ValveVO(3, "G", "S");
			vs.setTimeID(fvo.getTimeID());
			vs.setNewValue(new BigDecimal(0));
			vs.setStradegy(ValveVO.W_STRADEGY);
			vs.setValueChange(new BigDecimal(-smokeGasActual));
			vs.setOldValue(new BigDecimal(smokeGasActual));
			valveList.add(vs);
		}
		if (flowAirGiven <= 3) {
			msg.append("执行均热段空气排烟阀联动策略，因空气流量给定为零，所以空气排烟阀也关至零。\n");
			float smokeAirActual = fvo.getZone3().getValveActualAirEjectSomke().floatValue();
			ValveVO vs = new ValveVO(3, "A", "S");
			vs.setTimeID(fvo.getTimeID());
			vs.setNewValue(new BigDecimal(0));
			vs.setStradegy(ValveVO.W_STRADEGY);
			vs.setValueChange(new BigDecimal(-smokeAirActual));
			vs.setOldValue(new BigDecimal(smokeAirActual));
			valveList.add(vs);
		}
	}

	/**
	 * 排烟阀随动策略 执行各策略（包括升、降温及煤气保护策略、空燃比调整策略）时，在各段空气、煤气调节阀门动作时，
	 * 相应的排烟阀门随调节阀门以相同幅度进行随动开、关。
	 * 
	 * @param FIFO_Furnace
	 * @param flowValves
	 */
	public void danceWithFlowStradegy(FIFO<FurnaceVO> FIFO_Furnace, ArrayList<ValveVO> flowValves) {
		if (flowValves == null || flowValves.size() == 0) {
			return;
		}
		msg = new StringBuffer();

		msg.append("进入排烟阀与流量阀随动策略。\n");

		valveList = new ArrayList<ValveVO>();
		fvo = FIFO_Furnace.getMostRecentOne();
		ArrayList<SmokeValveVO> nowSmokes = new ArrayList<SmokeValveVO>();
		FurnaceUtil.loadSmokeValveVOs(nowSmokes, fvo);

		for (ValveVO vo : flowValves) {

			if (vo.getStradegy().equals("W")) {
				msg.append("均热段流量为零，排烟阀也跟着为零的策略优先执行，不必随动。\n");
				continue;
			}

			int zone = vo.getZone();
			String type = vo.getType();			
			ArrayList<SmokeValveVO> smokeList = getSmokesByValves(nowSmokes, zone, type);
			SmokeValveVO smokeVO1 = smokeList.get(0);
			SmokeValveVO smokeVO2 = smokeList.get(1);

			// 暂时跳过均热段右侧煤气排烟温度
			if (zone == 3 && type.equals("G")
					&& (smokeVO1.getPosition().equals("R") || smokeVO2.getPosition().equals("R"))) {
				msg.append("跳过均热段右侧煤气排烟温度。\n");
				continue;
			}

			int temp1 = smokeVO1.getTemp();
			int temp2 = smokeVO2.getTemp();
			// 如出现一个阀门的两个排烟温度中，一个大于140℃，另一个小于100℃时,策略不执行。
			if (temp1 >= 100 && temp1 <= 140 && temp2 >= 100 && temp2 <= 140) {

				float valueChange = vo.getValueChange().floatValue();
				float newValue = smokeVO1.getSmokeValve() + valueChange;
				
				int maxValve = getMaxSmokeValve(smokeVO1.getZone(), smokeVO1.getType());
				int minValve = getMinSmokeValve(smokeVO1.getZone(), smokeVO1.getType());
				if (newValue >= maxValve) {
					newValue = maxValve;
				}
				if (newValue < minValve) {
					newValue = minValve;
				}	
				valueChange = newValue - smokeVO1.getSmokeValve();
				ValveVO vs = new ValveVO(zone, type, "S");
				vs.setTimeID(fvo.getTimeID());
				vs.setNewValue(new BigDecimal(newValue));
				vs.setStradegy(ValveVO.D_STRADEGY);
				vs.setValueChange(new BigDecimal(valueChange));
				vs.setOldValue(new BigDecimal(smokeVO1.getSmokeValve()));
				valveListFilter(valveList, vs);
			}
		}
		logger.info(msg.toString());
	}

	public void valveListFilter(ArrayList<ValveVO> valveList, ValveVO newVO) {
		boolean isAdd = false;
		if (valveList.size() == 0) {
			valveList.add(newVO);
			isAdd = true;
		} else {
			boolean isExist = false;
			for (ValveVO vo : valveList) {
				if (vo.equals(newVO)) {
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				valveList.add(newVO);
				isAdd = true;
			}
		}
		if (isAdd) {
			msg.append(newVO.getValveName() + "给定" + newVO.getNewValue().floatValue() + "。\n");
		}
	}

	private ArrayList<SmokeValveVO> getSmokesByValves(ArrayList<SmokeValveVO> nowSmokes, int zone, String type) {
		ArrayList<SmokeValveVO> smokeList = new ArrayList<SmokeValveVO>();
		for (SmokeValveVO so : nowSmokes) {
			if (so.getZone() == zone && so.getType().equals(type)) {
				smokeList.add(so);
			}
		}
		return smokeList;
	}

	/**
	 * 炉膛压力检测策略。包含排烟温度异常调整策略。 1. 炉膛压力检测与烟气温度同步检测
	 * 
	 * @param fvo
	 * @param cvo
	 */
	public void pressureCheckStradegy(FurnaceVO inVO) {
		msg = new StringBuffer();
		valveList = new ArrayList<ValveVO>();
		msg.append("进入炉膛压力策略。\n");
		fvo = inVO;

		long startTime = System.currentTimeMillis();

		if (isExchangeTimeOK(fvo) == false) {
			msg.append("各段换向时间没有同时小于等于57秒！\n");
			return;
		}

		// 0. 先检查各段炉膛压力是否正常
		int zone3ChamberPressure = fvo.getZone3().getChamberPressure().intValue();
		int zone1ChamberPressure = fvo.getZone1().getChamberPressure().intValue();

		msg.append("均热段炉膛压力：").append(zone3ChamberPressure).append("。 加一段炉膛压力：").append(zone1ChamberPressure)
				.append("\n");

		boolean isZone3OK = false;
		if (zone3ChamberPressure <= CHAMBER3_PRESSURE_MAX && zone3ChamberPressure >= CHAMBER3_PRESSURE_MIN) {
			isZone3OK = true;
		}
		boolean isZone1OK = false;
		if (zone1ChamberPressure <= CHAMBER1_PRESSURE_MAX && zone1ChamberPressure >= CHAMBER1_PRESSURE_MIN) {
			isZone1OK = true;
		}

		if (isZone3OK && isZone1OK) {
			msg.append("均热段和加一段炉膛压力都正常。\n");
			return;
		}

		// 1. 把12个排烟温度对象生出来玩
		ArrayList<SmokeValveVO> smokeValveList = new ArrayList<SmokeValveVO>();
		FurnaceUtil.loadSmokeValveVOs(smokeValveList, fvo);

		// 1. 均热段炉膛压力检测
		int result1 = 0;
		if (isZone3OK == false) {
			int[] zones = { 3, 2 };
			ArrayList<SmokeValveVO> list = getSmokeListByZones(zones, smokeValveList);
			result1 = doChamberPressureStradegy(zone3ChamberPressure, list, CHAMBER3_PRESSURE_MAX, CHAMBER3_PRESSURE_MIN);
		}
		// 2. 加一段炉膛压力检测
		int result2 = 0;
		if (isZone1OK == false) {
			int[] zones = { 1, 2 };
			ArrayList<SmokeValveVO> list = getSmokeListByZones(zones, smokeValveList);
			result2 = doChamberPressureStradegy(zone1ChamberPressure, list, CHAMBER1_PRESSURE_MAX, CHAMBER1_PRESSURE_MIN);
		}
		// 3. 补偿策略
		// 3.1 均热段炉膛压力策略结果没有任何改动，并且加一段炉膛压力正常下，在均热段执行策略
		int result3 = 0;
		if (result1 == 0 && isZone1OK) {
			int[] zones = { 1 };
			ArrayList<SmokeValveVO> list = getSmokeListByZones(zones, smokeValveList);
			result3 = doChamberPressureStradegy(zone3ChamberPressure, list, CHAMBER3_PRESSURE_MAX, CHAMBER3_PRESSURE_MIN);
		}
		// 3.2 加一段炉膛压力策略结果没有任何改动，并且均热段炉膛压力正常下，在均热段执行策略
		int result4 = 0;
		if (result2 == 0 && isZone3OK) {
			int[] zones = { 3 };
			ArrayList<SmokeValveVO> list = getSmokeListByZones(zones, smokeValveList);
			result4 = doChamberPressureStradegy(zone1ChamberPressure, list, CHAMBER1_PRESSURE_MAX, CHAMBER1_PRESSURE_MIN);
		}

		if (result1 == 0 && result2 == 0 && result3 == 0 && result4 == 0) {
			msg.append("WARNING！！！炉膛压力检测策略没有对排烟阀有任何调整，请立即停止自动模式，切换手动模式。\n");
		}

		logger.info(msg.toString());
		long endTime = System.currentTimeMillis();
		logger.info("Processing Time: {} 毫秒", (endTime - startTime));
	}

	private ArrayList<SmokeValveVO> getSmokeListByZones(int[] zones, ArrayList<SmokeValveVO> smokeValveList) {
		ArrayList<SmokeValveVO> l = new ArrayList<SmokeValveVO>();
		for (int i = 0; i < smokeValveList.size(); i++) {
			SmokeValveVO vo = smokeValveList.get(i);
			for (int j = 0; j < zones.length; j++) {
				int z = zones[j];
				if (vo.getZone() == z) {
					l.add(vo);
				}
			}
		}
		return l;
	}

	public int doChamberPressureStradegy(int chamberP, List<SmokeValveVO> list, int up, int down) {
		int result = 0;
		if (chamberP > up) {
			// 炉膛过高，每次打开排烟温度最低的一个，所以根据温度升幂排序
			sortedSmokeVOs(true, list);
			result = chamberPressureCheck(list, SMOKE_VALVE_SPAN);
		} else if (chamberP < down) {
			// 炉膛过低，每次关闭排烟温度最高的一个，所以根据温度降幂排序
			sortedSmokeVOs(false, list);
			result = chamberPressureCheck(list, -SMOKE_VALVE_SPAN);
		}
		return result;
	}

	public int chamberPressureStradegy(FurnaceVO fvo, FurnaceControlVO cvo, int chamberP, SmokeValveVO[] svos, int up,
			int down) {
		List<SmokeValveVO> smokeList;
		int result = 0;
		if (chamberP > up) {
			// 炉膛过高，每次打开排烟温度最低的一个，所以根据温度升幂排序
			smokeList = getSortedSmokeVOs(true, svos);
			result = chamberPressureCheck(smokeList, SMOKE_VALVE_SPAN);
		} else if (chamberP < down) {
			// 炉膛过低，每次关闭排烟温度最高的一个，所以根据温度降幂排序
			smokeList = getSortedSmokeVOs(false, svos);
			result = chamberPressureCheck(smokeList, -SMOKE_VALVE_SPAN);
		}
		return result;
	}

	/**
	 * 依序调整排烟阀位
	 * 
	 * @param list
	 */
	public int chamberPressureCheck(List<SmokeValveVO> list, int span) {
		int result = 0;
		for (int i = 0; i < list.size(); i++) {
			SmokeValveVO so = list.get(i);
			float smokeValve = so.getSmokeValve();
			int temp = so.getTemp();
			int maxValve = getMaxSmokeValve(so.getZone(), so.getType());

			// 排烟温度过高及在着火位置对面而且换向时间大于6秒的，不参与炉膛压力策略，交给排烟温度检查策略
			// 这是排烟温度过高策略放在炉膛压力策略里执行的逻辑。
			if (temp > SMOKE_TEMP_MAX && so.isFireNearBy() == false && so.getExchangeSeconds() >= 6) {
				msg.append(so.getSmokeValveName()).append("排烟温度超标，不参加炉膛压力策略调整阀位。\n");
				continue;
			}

			// 如果温度最低对应的阀位所对应的另一个排烟温度超标，也跳过,交给排烟温度检查策略去关闭阀位开度。
			if (span > 0) {
				int zone = so.getZone();
				String type = so.getType();
				String pos = so.getPosition();
				SmokeValveVO opposite = null;
				for (SmokeValveVO svo : list) {
					if (svo.getType().equals(type) && svo.getZone() == zone && !pos.equals(svo.getPosition())) {
						opposite = svo;
						break;
					}
				}
				if (opposite.getTemp() > SMOKE_TEMP_MAX) {
					msg.append(opposite.getSmokeTempName() + "对应的另一个位置的排烟温度超标，找排烟温度次低对应的排烟阀位增加开度。\n");
					continue;
				}
			}

			if (so.getNewValve() > 0) {
				msg.append(so.getSmokeValveName()).append("已设定新阀值，不重复设定。\n");
				continue;
			}
			if (span > 0 && smokeValve >= maxValve) {
				msg.append(so.getSmokeValveName()).append("阀位开度已达上限：").append(maxValve).append("\n");
				continue;
			}
			// 这里是否可以调整到某个下限值，均热段空气排烟阀发现往下调整到12就不动了。
			if (span < 0 && smokeValve <= SMOKE_VALVE_MIN) {
				msg.append(so.getSmokeValveName()).append("阀位开度已达下限：").append(SMOKE_VALVE_MIN).append("\n");
				continue;
			}
			float newValve = smokeValve + span;
			if (newValve >= maxValve) {
				newValve = maxValve;
			}
			if (newValve < 0) {
				newValve = 0;
			}

			float valveChange = newValve - smokeValve;

			setSmokeVONewValve(list, so, newValve, valveChange, ValveVO.P_STRADEGY);

			msg.append(so.getSmokeValveName()).append("本次阀位调整幅度：").append(so.getValveChange()).append("。 调整为：")
					.append(newValve).append("\n");
			// 只要一个设定成功就跳出，剩下的不管了。
			result = 1;
			break;
		}
		return result;
	}

	/**
	 * 这是一个很无奈的方式。因为两个排烟温度对应一个排放阀，所以要同时给SmokeValveVO相同 zone和type的设定新阀位。
	 * 
	 * @param list
	 * @param vo
	 *            被选上的排烟温度对象，根据段位及类别（空气或煤气），找出对应的阀位赋值。
	 * @param newValve
	 * @param valveChange
	 * @param cvo
	 */
	private void setSmokeVONewValve(List<SmokeValveVO> list, SmokeValveVO vo, float newValve, float valveChange,
			String stradegy) {
		for (int i = 0; i < list.size(); i++) {
			SmokeValveVO so = list.get(i);
			if (so.getZone() == vo.getZone() && so.getType().equals(vo.getType())) {
				// 把相同zone及type(空气或煤气阀)的SmokeValveVO找出来，应该会有二个。
				// 如果加二段的阀位已被均热段给定阀位开关（根据程序执行顺序，先执行均热段），就不准赋值
				// 最后一个排烟温度上限判断是为了防止执行排烟温度策略进入之用。(反条件，让排烟温度过高的不会进来)
				if (so.getZone() == 2 && so.getNewValve() > 0 && so.getTemp() < SMOKE_TEMP_MAX) {
					continue;
				}
				so.setNewValve(newValve);
				so.setValveChange(valveChange);

				ValveVO vs = new ValveVO(so.getZone(), so.getType(), "S");
				vs.setTimeID(fvo.getTimeID());
				vs.setNewValue(new BigDecimal(newValve));
				vs.setStradegy(stradegy);
				vs.setValueChange(new BigDecimal(valveChange));
				vs.setOldValue(new BigDecimal(vo.getSmokeValve()));

				valveListFilter(valveList, vs);
				// 只要有设一个就跳出，不必重复设定同一个阀位。
				break;
			}
		}
	}

	public void sortedSmokeVOs(boolean isASC, List<SmokeValveVO> l) {
		TemperatureComparator sort = new TemperatureComparator();
		sort.setASC(isASC);
		Collections.sort(l, sort);
	}

	public List<SmokeValveVO> getSortedSmokeVOs(boolean isASC, SmokeValveVO[] smokeVOs) {
		List<SmokeValveVO> l = new ArrayList<SmokeValveVO>();
		for (int i = 0; i < smokeVOs.length; i++) {
			l.add(smokeVOs[i]);
		}
		TemperatureComparator sort = new TemperatureComparator();
		sort.setASC(isASC);
		Collections.sort(l, sort);

		return l;
	}

	/**
	 * 检测各段换向时间是否小于等于57秒
	 * 
	 * @param fvo
	 * @param msg
	 * @return
	 */
	public boolean isExchangeTimeOK(FurnaceVO fvo) {
		boolean isOK = false;
		int z1 = fvo.getZone1().getExchangeSeconds();
		int z2 = fvo.getZone2().getExchangeSeconds();
		int z3 = fvo.getZone3().getExchangeSeconds();
		if (z1 <= 57 && z2 <= 57 && z3 <= 57) {
			isOK = true;
		}

		return isOK;
	}

	/**
	 * 排烟阀最大值设定。
	 * 
	 * @param zone
	 * @param type
	 * @return
	 * @since 2016-3-16 13:24
	 */
	public int getMaxSmokeValve(int zone, String type) {
		int valve = 70;
		if (zone == 1 && type.equals("G")) {
			valve = 80;
		} else if (zone == 1 && type.equals("A")) {
			valve = 55;
		} else if (zone == 2 && type.equals("G")) {
			valve = 70;
		} else if (zone == 2 && type.equals("A")) {
			valve = 90;
		} else if (zone == 3 && type.equals("G")) {
			valve = 50;
		} else if (zone == 3 && type.equals("A")) {
			valve = 40;
		}
		return valve;
	}
	
	/**
	 * @param zone
	 * @param type
	 * @return
	 * @since 2016-3-28
	 */
	public int getMinSmokeValve(int zone, String type) {
		int valve = 70;
		if (zone == 1 && type.equals("G")) {
			valve = 30;
		} else if (zone == 1 && type.equals("A")) {
			valve = 30;
		} else if (zone == 2 && type.equals("G")) {
			valve = 20;
		} else if (zone == 2 && type.equals("A")) {
			valve = 20;
		} else if (zone == 3 && type.equals("G")) {
			valve = 0;
		} else if (zone == 3 && type.equals("A")) {
			valve = 0;
		}
		return valve;
	}

	public String getMsg() {
		return msg.toString();
	}

	public static void main(String[] args) {
		SmokeValveVO s1 = new SmokeValveVO();
		s1.setTemp(170);
		s1.setZone(1);
		SmokeValveVO s2 = new SmokeValveVO();
		s2.setTemp(150);
		s2.setZone(3);
		SmokeValveVO s3 = new SmokeValveVO();
		s3.setTemp(130);
		s3.setZone(2);
		SmokeValveVO s4 = new SmokeValveVO();
		s4.setTemp(110);
		s4.setZone(5);

		SmokeValveVO[] ss = { s2, s4, s1, s3 };

		EjectingSmokeValveControl cc = new EjectingSmokeValveControl();

		List<SmokeValveVO> l = cc.getSortedSmokeVOs(false, ss);

		for (int i = 0; i < l.size(); i++) {
			SmokeValveVO vo = (SmokeValveVO) l.get(i);

			System.out.println(vo);

		}

	}

}
