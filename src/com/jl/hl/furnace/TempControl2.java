package com.jl.hl.furnace;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.FurnaceZoneVO;
import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.util.LoggerUtil;

/**
 * 保护策略第二版本的代码
 * 
 * 非线程安全类，使用小心，只能在初始化一次使用，因为有GAS_MAIN_LOCK_25及GAS_MAIN_LOCK_20两个全局变量供 不同方法使用。
 * 
 * @author benwu
 * 
 */
public class TempControl2 {

	private final static Logger logger = LoggerUtil.getLogger(TempControl.class.getSimpleName());

	public static int LOW_FLOW_LIMIT = 1000;

	private StringBuffer msg = new StringBuffer();

	private FurnaceVO fvo = null;

	private int mode = 0;

	private ArrayList<ValveVO> valveList = new ArrayList<ValveVO>();

	/**
	 * 0:解锁，初始化，1：启动煤气总管低于2.0策略，等到煤气总管压力到2.5才解锁。
	 */
	public static int GAS_MAIN_LOCK = 0;

	public TempControl2() {

	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public ArrayList<ValveVO> getChangedValves() {
		return valveList;
	}

	private void initGlobalParams(FIFO<FurnaceVO> FIFO_Furnace) {
		msg = new StringBuffer();
		valveList = new ArrayList<ValveVO>();
		fvo = FIFO_Furnace.getMostRecentOne();
		float gasMain = fvo.getGasPressure().floatValue();
		// 根据煤气总管压力设定低压力策略开关
		lockStradegyByGasMain(gasMain);
	}

	/**
	 * 根据温差幅度，对煤气和空气阀位进行同步限幅调整
	 * 
	 * @param fvo
	 * @param cvo
	 * @param target
	 */
	public void synchroStradegy(FIFO<FurnaceVO> FIFO_Furnace, FIFO<ValveVO> FIFO_Command, TargetTempGas target) {
		initGlobalParams(FIFO_Furnace);
		msg.append("进入同步温控策略。\n");
		BigDecimal[] tempTargets = target.getTempsArray();
		FurnaceZoneVO[] zoneVOs = { fvo.getZone1(), fvo.getZone2(), fvo.getZone3() };

		for (int i = 0; i < 3; i++) {

			BigDecimal targetT = tempTargets[i];
			BigDecimal nowT = zoneVOs[i].getTemp();
			int tempDiff = targetT.subtract(nowT).abs().intValue(); // 温度差异绝对值

			if (tempDiff <= 5 && isFlowTooLowStradegy(zoneVOs[i])) {
				// 代表温差进入平衡阶段，且低流量策略执行成功，所以下面就不用执行了。
				continue;
			}

			if (GAS_MAIN_LOCK == 0 && targetT.compareTo(nowT) >= 0
					&& zoneVOs[i].getValveActualGas().compareTo(new BigDecimal(90)) >= 0) {
				msg.append(zoneVOs[i].getZoneName() + "升温过程中，煤气阀位已大于90时，不执行同步策略。\n");
				continue;
			}

			// 保护策略先关前2段。无效后再关均热段。
			if (GAS_MAIN_LOCK == 1) {
				if (fvo.getZone1().getValveActualGas().intValue() > 10
						&& fvo.getZone2().getValveActualGas().intValue() > 10) {
					msg.append("当加一、二段阀门关闭至10%前，均热段停止动作\n");
					if (i == 2) {
						msg.append("跳过均热段。\n");
						break;
					}
				}
			}

			float span = getValveSpan(targetT, nowT, zoneVOs[i].getZoneID(), FIFO_Furnace, target,
					fvo.getGasPressure().floatValue());

			if (span != 0) {

				tempDiff = targetT.subtract(nowT).intValue();
				String stradegy = ValveVO.T_STRADEGY;
				if (GAS_MAIN_LOCK == 1) {
					stradegy = "B";
				}
				setFlowValves(zoneVOs[i], FIFO_Command, stradegy, tempDiff, span);

			}
		}
		msg.append("离开温差限幅策略。\n");
		logger.info(msg.toString());
	}

	private void setFlowValves(FurnaceZoneVO zoneVO, FIFO<ValveVO> FIFO_Command, String stradegy, int diff,
			float span) {

		int zoneID = zoneVO.getZoneID();

		float newAirValve = zoneVO.getValveActualAir().floatValue() + span;
		float newGasValve = zoneVO.getValveActualGas().floatValue() + span;

		if (diff >= 0 && GAS_MAIN_LOCK == 0) {
			newGasValve = (float) (zoneVO.getValveActualGas().floatValue() + span * 0.7);
		} else if (diff < 0 || GAS_MAIN_LOCK == 1) {
			if (FIFO_Command.getFIFOQueue().size() >= 5 && stradegy.equals(ValveVO.T_STRADEGY)) {
				float r = FurnaceUtil.getRecentValveGapRatio(FIFO_Command, zoneVO.getZoneID(), "G", ValveVO.T_STRADEGY,
						"F", 5);
				logger.info("RecentValveGapRatio:{}", r);
				if (r < 0.8) {

					// newGasValve = zoneVO.getValveActualGas().floatValue() -
					// 6;
					// newAirValve = zoneVO.getValveActualAir().floatValue() +
					// 0;

					msg.append("煤气保护模式检测到阀位开度不够灵敏。\n");
				}
			}
		}

		if (newAirValve < getAirValveMin(zoneID))
			newAirValve = getAirValveMin(zoneID);

		if (newAirValve > getAirValveMax(zoneID))
			newAirValve = getAirValveMax(zoneID);

		int gasFlow = zoneVO.getFlowZoneGASTarget().intValue();

		// 均热段煤气流量给定值Q≤2500m³/h时，均热段空气流量阀门开度锁定为15%
		if (mode == 2 && zoneID == 3 && gasFlow <= 2500) {
			newAirValve = 15;
		}

		if (mode == 2 && zoneID == 3 && gasFlow <= 3500 && gasFlow > 2500) {
			newAirValve = 25;
		}

		if (newGasValve < 0)
			newGasValve = 0;
		if (newGasValve > 100)
			newGasValve = 100;

		float airSpan = newAirValve - zoneVO.getValveActualAir().floatValue();
		float gasSpan = newGasValve - zoneVO.getValveActualGas().floatValue();

		// 初始化要返回给PLC的Valve对象
		ValveVO vA = new ValveVO(zoneVO.getZoneID(), "A", "F");
		vA.setTimeID(fvo.getTimeID());
		vA.setStradegy(stradegy);
		vA.setNewValue(new BigDecimal(newAirValve));
		vA.setValueChange(new BigDecimal(airSpan));
		vA.setOldValue(zoneVO.getValveActualAir());
		ValveVO vG = new ValveVO(zoneVO.getZoneID(), "G", "F");
		vG.setTimeID(fvo.getTimeID());
		vG.setStradegy(stradegy);
		vG.setNewValue(new BigDecimal(newGasValve));
		vG.setValueChange(new BigDecimal(gasSpan));
		vG.setOldValue(zoneVO.getValveActualGas());
		valveList.add(vA);
		valveList.add(vG);
		if (stradegy.equals(ValveVO.G_STRADEGY)) {
			msg.append(zoneVO.getZoneName()).append("煤气定额执行完成. 煤气阀位给定：").append(newGasValve).append(". 空气阀给定：")
					.append(newAirValve).append(".\n");
		} else if (stradegy.equals(ValveVO.T_STRADEGY)) {
			msg.append(zoneVO.getZoneName()).append("同步策略执行完成. 煤气阀位给定：").append(newGasValve).append(". 空气阀给定：")
					.append(newAirValve).append(".\n");
		}
	}

	private float getAirValveMax(int zoneID) {
		float airValveMax = 50;
		if (zoneID == 3) {
			airValveMax = 50;
		} else {
			airValveMax = 100;
		}
		return airValveMax;
	}

	private float getAirValveMin(int zoneID) {
		float airValveMin = 0;
		if (mode == 1 && (zoneID == 1 || zoneID == 2)) {
			airValveMin = 30;
		}
		return airValveMin;
	}

	public void gasQuotaStradegy(FIFO<FurnaceVO> FIFO_Furnace, FIFO<ValveVO> FIFO_Command, TargetTempGas target) {
		initGlobalParams(FIFO_Furnace);
		msg.append("进入煤气定额策略。\n");
		BigDecimal[] gasTargets = target.getGasArray();
		FurnaceZoneVO[] zoneVOs = { fvo.getZone1(), fvo.getZone2(), fvo.getZone3() };

		for (int i = 0; i < 3; i++) {
			int gasTarget = gasTargets[i].intValue();
			int gasNow = zoneVOs[i].getFlowZoneGAS().intValue();
			int gasDiff = gasTarget - gasNow; // 正数代表升温过程，负数代表降温过程。

			if (GAS_MAIN_LOCK == 0 && gasDiff >= 0
					&& zoneVOs[i].getValveActualGas().compareTo(new BigDecimal(90)) >= 0) {
				msg.append(zoneVOs[i].getZoneName() + "在升温过程中，煤气阀位已大于90，不执行煤气阀开度策略。\n");
				continue;
			}

			// 保护策略先关前2段。无效后再关均热段。
			if (GAS_MAIN_LOCK == 1) {
				if (fvo.getZone1().getValveActualGas().intValue() > 10
						&& fvo.getZone2().getValveActualGas().intValue() > 10) {
					msg.append("当加一、二段阀门关闭至10%前，均热段停止动作。\n");
					if (i == 2) {
						msg.append("跳过均热段。\n");
						break;
					}
				}
			}

			float span = getGasSpan(gasDiff, fvo.getGasPressure().floatValue());

			if (span != 0) {
				setFlowValves(zoneVOs[i], FIFO_Command, ValveVO.G_STRADEGY, gasDiff, span);
			}
		}
		logger.info(msg.toString());
	}

	private float getGasSpan(int gap, float gasMain) {
		float span = 0;
		// 只要煤气压力保护策略生效，都要最小幅度关闭
		if (GAS_MAIN_LOCK == 1) {
			span = -5;
		} else if (gap > 0) {
			// 升温限幅分四档位
			if (gap >= 6000) {
				span = getSpanByGasmain(gasMain, 4);
			} else if (gap < 6000 && gap >= 4000) {
				span = getSpanByGasmain(gasMain, 3);
			} else if (gap < 4000 && gap >= 1500) {
				span = getSpanByGasmain(gasMain, 2);
			} else if (gap < 1500 && gap >= 500) {
				span = 3;
			}
		} else if (gap < 0) {
			// 降温限幅先和升温一样，未来会变再改
			gap = gap * -1;
			if (gap >= 6000) {
				span = -10;
			} else if (gap < 6000 && gap >= 4000) {
				span = -7;
			} else if (gap < 4000 && gap >= 1500) {
				span = -5;
			} else if (gap < 1500 && gap >= 500) {
				span = -3;
			}
		}

		return span;
	}

	/**
	 * 保护策略在保压升至向上相隔的策略（即4恢复2，5恢复到3）到才恢复自动程序， 是为防止系统在“保压”与“正常”模式之间频繁切换
	 * 
	 * @param gasMain
	 */
	private void lockStradegyByGasMain(float gasMain) {

		// modified on 2016-3-28
		float gasLlimit = 1.7f;

		// 上锁
		if (gasMain < gasLlimit) {
			GAS_MAIN_LOCK = 1;
			msg.append("进入煤气保护模式，煤气总管压力低于2.0。\n");
		}
		// 解锁
		if (gasMain >= gasLlimit && GAS_MAIN_LOCK != 0) {
			GAS_MAIN_LOCK = 0;
			msg.append("离开煤气保护模式。\n");
		}
	}

	private float getValveSpan(BigDecimal targetT, BigDecimal nowT, int zoneID, FIFO<FurnaceVO> FIFO_Furnace,
			TargetTempGas target, float gasMain) {
		float span = 0;
		// 只要煤气压力保护策略生效，都要最小幅度关闭
		if (GAS_MAIN_LOCK == 1) {
			span = -5;
			return span;
		}

		// 透过明显的温度趋势，来决定某段是否继续升降限幅。
		int trend = FurnaceUtil.checkTempTrend(FIFO_Furnace, target, zoneID);

		float gap = targetT.subtract(nowT).intValue();

		if (gap > 0) {
			// 升温限幅分四档位
			if (gap >= 12) {
				if (trend == 1) {
					span = 0;
				} else {
					span = getSpanByGasmain(gasMain, 4);
				}
			} else if (gap < 12 && gap >= 8) {
				if (trend == 1) {
					span = 0;
				} else {
					span = getSpanByGasmain(gasMain, 3);
				}
			} else if (gap < 8 && gap > 3) {
				if (trend == 1) {
					span = 0;
				} else {
					span = getSpanByGasmain(gasMain, 2);
				}
			} else if (gap <= 3) {
				if (trend == 1) {
					span = -2;
				} else {
					span = 0;
				}
			}
		} else if (gap < 0) {
			// 降温限幅先和升温一样，未来会变再改
			gap = gap * -1;
			if (gap >= 12) {
				if (trend == -1) {
					span = 0;
				} else {
					span = -10;
				}
			} else if (gap < 12 && gap >= 7) {
				if (trend == -1) {
					span = 0;
				} else {
					span = -7;
				}
			} else if (gap < 7 && gap > 3) {
				if (trend == -1) {
					span = 0;
				} else {
					span = -5;
				}
			} else if (gap <= 3) {
				if (trend == -1) {
					span = 2;
				} else if (trend == 0) {
					span = 0;
				}
			}
		}
		return span;
	}

	/**
	 * 根据煤气总管压力设定限幅大小
	 * 
	 * @param gasMain
	 * @param gear
	 *            1：最低档，2：低档位，3：中档位，4:高档位
	 * @return
	 */
	private int getSpanByGasmain(float gasMain, int gear) {

		int span = 0;

		if (gasMain >= 3.5) {
			switch (gear) {
			case 1:
				span = 3;
				break;
			case 2:
				span = 5;
				break;
			case 3:
				span = 7;
				break;
			case 4:
				span = 10;
				break;
			}
		} else if (gasMain < 3.5 && gasMain >= 3.0) {
			switch (gear) {
			case 1:
				span = 3;
				break;
			case 2:
				span = 5;
				break;
			case 3:
				span = 5;
				break;
			case 4:
				span = 5;
				break;
			}
		} else if (gasMain < 3.0 && gasMain >= 2.5) {
			switch (gear) {
			case 1:
				span = 3;
				break;
			case 2:
				span = 5;
				break;
			case 3:
				span = 5;
				break;
			case 4:
				span = 5;
				break;
			}
		} else if (gasMain < 2.5 && gasMain >= 2.0) {
			span = 3;
		}

		return span;
	}

	/**
	 * 根据各段实际空燃比与给定空燃比，每次修正煤气阀位1%。这个逻辑还待确认。
	 * 
	 * @param fvo
	 * @param cvo
	 * @param target
	 */
	public void airGasRatioStradegy(FIFO<FurnaceVO> FIFO_Furnace, TargetTempGas target, int mode) {
		initGlobalParams(FIFO_Furnace);
		msg.append("进入空燃比煤气调整策略。\n");
		BigDecimal[] tempTargets = target.getTempsArray();
		FurnaceZoneVO[] zoneVOs = { fvo.getZone1(), fvo.getZone2(), fvo.getZone3() };

		for (int i = 0; i < 3; i++) {
			BigDecimal targetT = tempTargets[i];
			BigDecimal nowT = zoneVOs[i].getTemp();
			int zoneID = zoneVOs[i].getZoneID();
			int gasFlow = zoneVOs[i].getFlowZoneGASTarget().intValue();
			int tempDiff = targetT.subtract(nowT).intValue(); // 温度差异
			int exchangeSeconds = zoneVOs[i].getExchangeSeconds();

			if (exchangeSeconds > 57) {
				msg.append("换向时间大于57秒，不根据空燃比调整煤气阀。\n");
				continue;
			}
			
			//流量模式，均热段煤气流量给定值Q≤2500m³/h时，均热段煤气流量给定值2500m³/h＜Q≤3500m³/h时
			//因为锁定空气阀位，所以不执行空燃比
			if(mode == 2 && zoneID==3 && (gasFlow <= 2500 || (gasFlow <= 3500 && gasFlow > 2500))){
				msg.append("流量模式，均热段煤气流量给定值Q≤2500m³/h或2500m³/h＜Q≤3500m³/h时，不执行空燃比。\n");
				continue;
			}
			
			BigDecimal actual = zoneVOs[i].getAirGasRatioACTUAL();
			BigDecimal given = zoneVOs[i].getAirGasRatioGIVEN();

			if (actual.floatValue() <= 0 && mode == 1) {
				msg.append(zoneVOs[i].getZoneName() + "实际空燃比为零，不执行空燃比策略。\n");
				continue;
			}

			if (zoneVOs[i].getFlowZoneGAS().intValue() < 2000) {
				continue;
			}

			int span = getAirGasRatioSpan(actual, given);

			msg.append(zoneVOs[i].getZoneName()).append("实际空燃比:").append(actual.floatValue()).append(". 给定空燃比：")
					.append(given.floatValue()).append(".\n");
			if (span != 0 && mode == 1) {
				// 压力正常及升温模式
				if (GAS_MAIN_LOCK == 0 && tempDiff > 0) {
					if (zoneVOs[i].getValveActualGas().compareTo(new BigDecimal(90)) < 0) {
						adjustGasByAir(zoneVOs[i], span);
					} else {
						adjustAirByGas(zoneVOs[i], span);
					}
				} else if (GAS_MAIN_LOCK == 1 || tempDiff < 0) {
					// 煤气压力保护或降温过程的空气阀位调整策略
					msg.append("进入煤气压力保护或降温过程的空气阀位调整策略");
					// modified on 2016-03-28
					adjustGasByAir(zoneVOs[i], span);
				}
			} else if (span != 0 && mode == 2) {
				adjustAirByGas(zoneVOs[i], span);
			} else {
				msg.append(zoneVOs[i].getZoneName()).append("的实际空燃比在给定值范围，不做调整").append(".\n");
			}
		}
		logger.info(msg.toString());
	}

	private void adjustGasByAir(FurnaceZoneVO zoneVO, int span) {
		float newValve = zoneVO.getValveActualGas().floatValue() + span;
		ValveVO vGas = new ValveVO(zoneVO.getZoneID(), "G", "F");
		vGas.setTimeID(fvo.getTimeID());
		vGas.setStradegy(ValveVO.A_STRADEGY);
		vGas.setNewValue(new BigDecimal(newValve));
		vGas.setValueChange(new BigDecimal(span));
		vGas.setOldValue(zoneVO.getValveActualGas());
		valveList.add(vGas);
		msg.append(zoneVO.getZoneName()).append("根据空燃比调整煤气阀位").append(span).append(". 煤气阀位给定：").append(newValve)
				.append(".\n");
	}

	private void adjustAirByGas(FurnaceZoneVO zoneVOs, int span) {
		float newValve = zoneVOs.getValveActualAir().floatValue() - span;

		ValveVO vA = new ValveVO(zoneVOs.getZoneID(), "A", "F");
		vA.setTimeID(fvo.getTimeID());
		vA.setStradegy(ValveVO.A_STRADEGY);
		vA.setNewValue(new BigDecimal(newValve));
		vA.setValueChange(new BigDecimal(-span));
		vA.setOldValue(zoneVOs.getValveActualAir());
		valveList.add(vA);
		msg.append(zoneVOs.getZoneName()).append("根据空燃比调整空气阀位").append(-span).append(". 空气阀位给定：").append(newValve)
				.append(".\n");
	}

	/**
	 * @return 空燃比微调限幅
	 */
	private int getAirGasRatioSpan(BigDecimal actual, BigDecimal given) {
		float gap = actual.subtract(given).floatValue();
		float airGasRatioDiff = 0.03f;
		int span = 0;

		if (gap > airGasRatioDiff) {
			if (gap > 0.3) {
				span = 5;
			} else if (gap <= 0.3 && gap > 0.2) {
				span = 3;
			} else if (gap <= 0.2 && gap > 0.1) {
				span = 2;
			} else {
				span = 2;
			}
		} else if (gap < -airGasRatioDiff) {
			if (gap < -0.3) {
				span = -5;
			} else if (gap < -0.2 && gap >= -0.3) {
				span = -3;
			} else if (gap < -0.1 && gap >= -0.2) {
				span = -2;
			} else {
				span = -2;
			}
		} else {
			span = 0;
		}
		return span;
	}

	/**
	 * 通常在达到目标温度阶段时，煤气或空气流量过低的调整策略
	 * 
	 * @param zvo
	 * @return
	 */
	public boolean isFlowTooLowStradegy(FurnaceZoneVO zvo) {
		boolean tooLow = false;
		int airFlow = zvo.getFlowZoneAIR().intValue();
		int gasFlow = zvo.getFlowZoneGAS().intValue();
		float airSpan = 0;
		float gasSpan = 0;
		float airChange = 0;
		float gasChange = 0;

		// 如果空气流量低到见底，而煤气没有。空气阀位不动，煤气阀成为空气阀的0.67倍。
		if (airFlow <= LOW_FLOW_LIMIT && gasFlow > LOW_FLOW_LIMIT) {
			tooLow = true;
			gasSpan = zvo.getValveActualAir().multiply(new BigDecimal(0.67)).floatValue();
			gasChange = gasSpan - zvo.getValveActualGas().floatValue();
			airSpan = zvo.getValveActualAir().floatValue();

			// 如果煤气流量见底，而空气没有。煤气阀位不动，空气阀成为煤气阀的1.5倍
		} else if (gasFlow <= LOW_FLOW_LIMIT && airFlow > LOW_FLOW_LIMIT) {
			tooLow = true;
			airSpan = zvo.getValveActualGas().multiply(new BigDecimal(1.5)).floatValue();
			airChange = airSpan - zvo.getValveActualAir().floatValue();
			gasSpan = zvo.getValveActualGas().floatValue();
		}
		// 两个都很低，就不用管了。待确认！！！！
		if (tooLow == true) {

			if (airChange >= 5 || airChange <= -5 || gasChange >= 5 || gasChange <= -5) {
				msg.append("警告！ 低流量策略执行成功，但调整幅度过大。\n");
			}

			ValveVO vA = new ValveVO(zvo.getZoneID(), "A", "F");
			vA.setTimeID(fvo.getTimeID());
			vA.setNewValue(new BigDecimal(airSpan));
			vA.setStradegy(ValveVO.L_STRADEGY);
			vA.setValueChange(new BigDecimal(airChange));
			vA.setOldValue(zvo.getValveActualAir());

			ValveVO vG = new ValveVO(zvo.getZoneID(), "G", "F");
			vG.setTimeID(fvo.getTimeID());
			vG.setNewValue(new BigDecimal(gasSpan));
			vG.setStradegy(ValveVO.L_STRADEGY);
			vG.setValueChange(new BigDecimal(gasChange));
			vG.setOldValue(zvo.getValveActualGas());
			valveList.add(vA);
			valveList.add(vG);

			msg.append(zvo.getZoneName()).append("低流量策略执行成功。").append("煤气阀位：").append(gasSpan).append(". 空气阀位：")
					.append(airSpan).append(".\n");
		}

		return tooLow;
	}

	public boolean isInProtectionMode() {
		boolean isIn = false;

		if (GAS_MAIN_LOCK == 1) {
			isIn = true;
		}

		return isIn;
	}

	// 反馈控温策略下达的指令，用来显示在UI上。
	public String getMsg() {
		return msg.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < 3; i++) {

			if (i == 1) {

				continue;
			}

			System.out.println(i);
		}
	}
}
