package com.jl.hl.furnace;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.util.LoggerUtil;

public class FurnaceAutoHeatingControl {

	private final static Logger logger = LoggerUtil
			.getLogger(FurnaceAutoHeatingControl.class.getSimpleName());

	FIFO<FurnaceVO> FIFO_Furnace = null;

	FIFO<ValveVO> FIFO_Command = null;

	/**
	 * 1:正常模式，2：煤气模式，3：手动，0：初始化
	 */
	private int mode = 0;

	/**
	 * 空燃比初始化次数，每执行完3次，就换同步策略
	 */
	private int airGasRatioCounts = 2;

	/**
	 * 第一次执行后，设定为10
	 */
	private int tempInterval = 1;

	private boolean runAirGasStradegy = false;

	private boolean smokeValesTurns = true;

	private int airGasInterval = 2;

	private int pressureInterval = 2;

	private int smokeInterval = 4;

	private int gasInterval = 1;

	private int smokeTooLowInterval = 5;

	private TempControl2 tc = new TempControl2();

	private EjectingSmokeValveControl esvc = new EjectingSmokeValveControl();

	private TargetTempGas targets = new TargetTempGas();

	private ArrayList<ValveVO> valveList = new ArrayList<ValveVO>();

	private StringBuffer msg = new StringBuffer();

	private long startTime_MODE_1 = 0L;

	private long startTime_MODE_2 = 0L;

	public FurnaceAutoHeatingControl() {

	}

	public void initRunningData(FIFO<FurnaceVO> FIFO_Furnace,
			FIFO<ValveVO> FIFO_Command, int newMode, TargetTempGas targets) {
		this.FIFO_Furnace = FIFO_Furnace;
		this.FIFO_Command = FIFO_Command;
		this.targets = targets;
		this.valveList = new ArrayList<ValveVO>();

		if (newMode == 1 && mode != newMode) {
			tempInterval = 1;
			runAirGasStradegy = false;
		} else if (newMode == 2 && mode != newMode) {
			gasInterval = 1;
			runAirGasStradegy = false;
		} else if (newMode == 3 && mode != newMode) {
			airGasRatioCounts = 3;
			tempInterval = 1;
			gasInterval = 1;
			airGasInterval = 3;
			smokeTooLowInterval = 5;
			pressureInterval = 2;
			smokeInterval = 4;
			runAirGasStradegy = false;
		}
		recordRunningTime(newMode);
		mode = newMode;
		tc.setMode(newMode);
	}

	private void recordRunningTime(int newMode) {
		if ((newMode == 1 && mode != newMode)) {
			startTime_MODE_1 = System.currentTimeMillis();
			smokeTooLowInterval = 3;
			logger.info("开始进入温控模式");
		}
		if ((newMode == 2 && mode != newMode)) {
			startTime_MODE_2 = System.currentTimeMillis();
			smokeTooLowInterval = 3;
			logger.info("开始进入煤气定额模式");
		}
		if (mode == 1 && mode != newMode) {
			long now = System.currentTimeMillis();
			long mins = (now - startTime_MODE_1) / 1000 / 60;
			long temp = (now - startTime_MODE_1) % (1000 * 60);
			long seconds = temp / 1000;

			logger.info("本次温控模式运行{}分{}秒", mins, seconds);

			recordDuration(startTime_MODE_1, mins, 1);

		}
		if (mode == 2 && mode != newMode) {
			long now = System.currentTimeMillis();
			long mins = (now - startTime_MODE_2) / 1000 / 60;
			long temp = (now - startTime_MODE_2) % (1000 * 60);
			long seconds = temp / 1000;

			logger.info("本次煤气模式运行{}分{}秒",
					mins, seconds);

			recordDuration(startTime_MODE_2, mins, 2);
		}
	}

	private void recordDuration(long startTime, long mins, int mode) {
		RecordRunningTimeThread record = new RecordRunningTimeThread();
		record.setData(startTime, mins, mode);
		record.start();
	}

	public void run() {
		msg = new StringBuffer();

		if (mode == 1 || mode == 2) {
			counting();

			if (mode == 1 && tempInterval == 0 && runAirGasStradegy == false) {
				doTempControl();
				doWalkWithControl();
				doDanceWithFlowControl();
				if (tc.isInProtectionMode()) {
					airGasInterval = 2;
				} else {
					airGasInterval = 3;
				}
				runAirGasStradegy = true;
				tempInterval = 10;
			} else if (mode == 2 && gasInterval == 0
					&& runAirGasStradegy == false) {
				doGasControl();
				doWalkWithControl();
				doDanceWithFlowControl();
				if (tc.isInProtectionMode()) {
					airGasInterval = 2;
				} else {
					airGasInterval = 3;
				}
				runAirGasStradegy = true;
				gasInterval = 10;
			}

			if (runAirGasStradegy && airGasInterval == 0) {
				doAirGasRatioControl();
				doDanceWithFlowControl();
				airGasInterval = 2;
				airGasRatioCounts--;
				if (airGasRatioCounts == 0) {
					airGasRatioCounts = 3;
					runAirGasStradegy = false;
					gasInterval = 1;
					tempInterval = 1;
				}
			}
			
			/**
			if (smokeTooLowInterval == 0) {
				doSmokeTooLowControl();
				smokeTooLowInterval = 57;
			}
			*/

			if (smokeInterval == 0 && smokeValesTurns) {
				doSmokeTempControl();
				smokeValesTurns = false;
				pressureInterval = 2;
			} else if (pressureInterval == 0 && smokeValesTurns == false) {
				doChamperPressureControl();
				smokeValesTurns = true;
				smokeInterval = 2;
			}			
		}
	}

	private void counting() {
		tempInterval--;
		gasInterval--;
		airGasInterval--;
		pressureInterval--;
		smokeInterval--;
		gasInterval--;
		smokeTooLowInterval--;

		if (tempInterval < 0)
			tempInterval = 0;
		if (gasInterval < 0)
			gasInterval = 0;
		if (airGasInterval < 0)
			airGasInterval = 0;
		if (pressureInterval < 0)
			pressureInterval = 0;
		if (smokeInterval < 0)
			smokeInterval = 0;
		if (gasInterval < 0)
			gasInterval = 0;
		if (smokeTooLowInterval < 0)
			smokeTooLowInterval = 0;
	}

	private void doTempControl() {
		tc.synchroStradegy(FIFO_Furnace, FIFO_Command, targets);
		ArrayList<ValveVO> vos = tc.getChangedValves();
		copyFrom(vos);
		msg.append(tc.getMsg());
	}

	private void doGasControl() {
		tc.gasQuotaStradegy(FIFO_Furnace, FIFO_Command, targets);
		ArrayList<ValveVO> vos = tc.getChangedValves();
		copyFrom(vos);
		msg.append(tc.getMsg());
	}

	private void doAirGasRatioControl() {
		tc.airGasRatioStradegy(FIFO_Furnace, targets, mode);
		ArrayList<ValveVO> vos = tc.getChangedValves();
		copyFrom(vos);
		msg.append(tc.getMsg());
	}

	private void doSmokeTempControl() {
		esvc.smokeTempTooHighStradegy(FIFO_Furnace);
		ArrayList<ValveVO> vos = esvc.getChangedValves();
		copyFrom(vos);
		msg.append(esvc.getMsg());
	}

	private void doWalkWithControl() {
		esvc.walkWithFlowToZeroStradegy(FIFO_Furnace);
		ArrayList<ValveVO> vos = esvc.getChangedValves();
		copyFrom(vos);
		msg.append(esvc.getMsg());
	}

	private void doChamperPressureControl() {
		esvc.pressureCheckStradegy(FIFO_Furnace.getMostRecentOne());
		ArrayList<ValveVO> vos = esvc.getChangedValves();
		copyFrom(vos);
		msg.append(esvc.getMsg());
	}

	private void doDanceWithFlowControl() {
		esvc.danceWithFlowStradegy(FIFO_Furnace, valveList);
		ArrayList<ValveVO> vos = esvc.getChangedValves();
		copyFrom(vos);
		msg.append(esvc.getMsg());
	}

	/** 2016-03-22 除役
	private void doSmokeTooLowControl() {
		esvc.smokeTempTooLowStradegy(FIFO_Furnace);
		ArrayList<ValveVO> vos = esvc.getChangedValves();
		copyFrom(vos);
		msg.append(esvc.getMsg());
	}
    */
	
	public String getMsg() {
		return msg.toString();
	}

	public ArrayList<ValveVO> getValveList() {
		return valveList;
	}

	private void copyFrom(ArrayList<ValveVO> sourceList) {
		for (ValveVO vo : sourceList) {
			valveList.add(vo);
		}
	}

	public static void main(String[] args) throws Exception {
		long startTime_mode_1 = System.currentTimeMillis();

		Thread.sleep(1000 * 60 + 1000 * 19);

		long now = System.currentTimeMillis();
		long mins = (now - startTime_mode_1) / 1000 / 60;
		long temp = (now - startTime_mode_1) % (1000 * 60);
		long seconds = temp / 1000;

		logger.info("本次煤气模式运行{}分{}秒", mins, seconds);
	}
}
