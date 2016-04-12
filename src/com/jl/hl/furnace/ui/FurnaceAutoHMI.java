package com.jl.hl.furnace.ui;

import java.awt.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.*;
import com.jl.hl.furnace.model.FurnaceControlVO;
import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.FurnaceZoneVO;
import com.jl.hl.furnace.model.SmokeValveVO;
import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.mysql.StoreToDB;
import com.jl.hl.furnace.plc.moka7.S7;
import com.jl.hl.furnace.plc.s7.ReadWriteS7;
import com.jl.hl.furnace.util.LoggerUtil;

public class FurnaceAutoHMI {

	private final static Logger logger = LoggerUtil.getLogger(FurnaceAutoHMI.class.getSimpleName());

	private final static Logger valveLogger = LoggerUtil.getLogger("valveLogger");

	private JFrame mainFrame;

	private Color BK_COLOR = new Color(12, 12, 12);
	private Color FG_COLOR = new Color(255, 215, 0);
	private Color FG_COLOR_GAS = new Color(207, 207, 196);
	private Color FG_COLOR_AIR = new Color(166, 231, 255);

	private Color FG_ALARM_COLOR = new Color(255, 111, 255);

	private Font font = new Font("黑体", Font.PLAIN, 20);
	private Font font2 = new Font("黑体", Font.PLAIN, 16);
	private Font textAreaFont = new Font("黑体", Font.PLAIN, 17);

	GridBagLayout bgl = new GridBagLayout();
	GridBagConstraints bgc = new GridBagConstraints();

	ExtJButton btnM = new ExtJButton(ExtJButton.ROUND_RECT);

	JTextArea textArea = new JTextArea();

	private static final JLabel[] zonelabels = { new JLabel("均热段"), new JLabel("加二段"), new JLabel("加一段") };

	private JLabel airGasR_T1 = new JLabel();
	private JLabel airGasR_T2 = new JLabel();
	private JLabel airGasR_T3 = new JLabel();
	private JLabel mark1 = new JLabel();
	private JLabel mark2 = new JLabel();
	private JLabel mark3 = new JLabel();
	private JLabel airGasR_G1 = new JLabel();
	private JLabel airGasR_G2 = new JLabel();
	private JLabel airGasR_G3 = new JLabel();

	private Tick tickT1 = new Tick();
	private Tick tickT2 = new Tick();
	private Tick tickT3 = new Tick();

	private Tick tickG1 = new Tick();
	private Tick tickG2 = new Tick();
	private Tick tickG3 = new Tick();

	private Tick tickCP1 = new Tick();// 均热段炉膛压力
	private Tick tickCP2 = new Tick();// 加一段炉膛压力

	private JLabel markA = new JLabel();
	private JLabel markB = new JLabel();
	private JLabel markC = new JLabel();

	private static final String PLASTIC3D = "com.jgoodies.looks.plastic.Plastic3DLookAndFeel";

	private AutoControlThread autoThread = null;

	private boolean isS7300Connected = true;

	// 1:正常模式，2:煤气模式, 3:手动模式
	private int mode = 3;

	private ReadWriteS7 rwS7 = new ReadWriteS7();

	private TargetTempGas targets = new TargetTempGas();

	private String statusText = "今天天气不错啊！您觉得呢？";

	// 1笔1秒，选择最长的间隔策略是炉膛压力的10秒
	private FIFO<FurnaceVO> FIFO_Furnace = new FIFO<FurnaceVO>(20);

	private FIFO<ValveVO> FIFO_Command = new FIFO<ValveVO>(60);

	private JPanel tPanel = null;

	private JPanel gPanel = null;

	private JPanel tempPanel = null;

	private JPanel gasPanel = null;

	private JPanel cpPanel = null;

	private JPanel chamberPanel = null;

	private long timeLasted_MODE_1 = 0L;

	private long timeLasted_MODE_2 = 0L;

	long timeLasted_MODE_3 = 0L;

	ArrayList<SmokeValveVO> smokeValveList = new ArrayList<SmokeValveVO>();

	public FurnaceAutoHMI() {

	}

	public static void main(String[] args) {

		setLAF();

		FurnaceAutoHMI hmi = new FurnaceAutoHMI();
		hmi.initGUI();
	}

	public static void setLAF() {
		try {
			UIManager.setLookAndFeel(PLASTIC3D);
			// UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
			// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
			// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initGUI() {
		mainFrame = new JFrame("加热炉温度控制系统");
		mainFrame.getContentPane().setBackground(BK_COLOR);
		ImageIcon ic = new ImageIcon("resources/images/go.jpg");
		mainFrame.setIconImage(ic.getImage());

		// 设置全屏
		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		mainFrame.setSize(width, height);
		mainFrame.setLocation(0, 0);
		mainFrame.setResizable(true);
		mainFrame.setLayout(bgl);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 按照以下顺序生成画面
		buttonsPanel();

		tPanel = createThermalControlPanel();
		bgc = new GridBagConstraints(0, 1, 1, 1, 100, 20, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(1, 5, 1, 5), 100, 0);
		mainFrame.add(tPanel, bgc);

		gPanel = createGasControlPanel();
		bgc = new GridBagConstraints(0, 2, 1, 1, 100, 20, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(1, 5, 1, 5), 100, 0);
		mainFrame.add(gPanel, bgc);

		cpPanel = createChamberPanel();
		bgc = new GridBagConstraints(0, 3, 1, 1, 100, 15, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(1, 5, 1, 5), 100, 0);
		mainFrame.add(cpPanel, bgc);

		JPanel buttomPanel = createBottomPanel();
		bgc = new GridBagConstraints(0, 4, 1, 1, 100, 5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(1, 5, 1, 5), 100, 0);
		mainFrame.add(buttomPanel, bgc);
		mainFrame.setVisible(true);

		// 定义了1个线程。
		// 每1秒执行更新画面的值温度与煤气流量，后面可以再增加，让画面动态显示
		// 同时保留最近六笔数据到FIFO对象里。
		autoThread = new AutoControlThread();
		autoThread.start();

	}

	class AutoControlThread extends Thread {

		FurnaceAutoHeatingControl autoControl = new FurnaceAutoHeatingControl();

		long waitingSeconds = 10;
		boolean isARead = true;
		boolean isMRead = true;
		boolean isGRead = true;
		int failedCounts = 0;
		boolean heartBeats = true;
		FurnaceVO fvo = null;

		public void run() {
			while (isS7300Connected) {
				try {
					Thread.sleep(waitingSeconds);
				} catch (InterruptedException e) {
					waitingSeconds = 500;
				}

				long start = System.currentTimeMillis();
				fvo = new FurnaceVO();
				byte[] buffer = rwS7.readS7();
				fvo.loadDataFromS7300(buffer);
				logger.info("GET FURNACE VO FROM PLC FOR {} mills", (System.currentTimeMillis() - start));
				if (fvo.getZone1().getTemp().intValue() == 0) {
					waitingSeconds = 115;
					failedCounts++;
					logger.info("failedCounts:" + failedCounts);
					if (failedCounts > 3) {
						logger.info("读取信号数据失败{}次。不再尝试与PLC通信，请查明原因后再重启本系统。", failedCounts);
						writeTextArea("读取信号数据失败3次。不再尝试与PLC通信，请查明原因后再重启本系统。");
						isS7300Connected = false;
					}
				} else {
					start = System.currentTimeMillis();
					// 主要逻辑在这里！！！
					runningAutoControl(fvo, buffer);
					// 写心跳给S7
					sendHeartBeats();
					logger.info("RUNNING TIME FOR {} mills", (System.currentTimeMillis() - start));
				}
			}
		}

		private void runningAutoControl(FurnaceVO fvo, byte[] buffer) {
			int PLCSignal = fvo.getPLCSignal();
			FIFO_Furnace.addLast(fvo);
			smokeValveList = new ArrayList<SmokeValveVO>();
			FurnaceUtil.loadSmokeValveVOs(smokeValveList, fvo);
			switch (PLCSignal) {
			case 1:
				mode = 1;
				if (isARead) {
					btnM.setText("自动温控模式");
					writeTextArea("收到您下达的自动温控模式指令，现在我们一起努力吧！");
					timeLasted_MODE_1 = System.currentTimeMillis();
					isARead = false;
					isMRead = true;
					isGRead = true;
				} else {
					btnM.setText("自动温控模式已" + FurnaceUtil.getRuningMins(timeLasted_MODE_1) + "分钟");
				}
				break;
			case 2:
				mode = 2;
				if (isGRead) {
					btnM.setText("煤气配额模式");
					writeTextArea("收到您下达的煤气定额模式指令，让我们一起节省煤气而努力哦！");
					timeLasted_MODE_2 = System.currentTimeMillis();
					isARead = true;
					isMRead = true;
					isGRead = false;
				} else {
					btnM.setText("煤气配额模式已" + FurnaceUtil.getRuningMins(timeLasted_MODE_2) + "分钟");
				}
				break;
			case 3:
				mode = 3;
				if (isMRead) {
					btnM.setText("非自动模式");
					timeLasted_MODE_3 = System.currentTimeMillis();
					writeTextArea("您好，我是HAL 9000型智能烧炉辅助系统，我叫Samantha。您可以在HMI画面里选择温控模式或煤气定额模式：");
					isARead = true;
					isMRead = false;
					isGRead = true;
				} else {
					btnM.setText("非自动模式已" + FurnaceUtil.getRuningMins(timeLasted_MODE_3) + "分钟");
				}
				break;
			default:
				isARead = true;
				isMRead = true;
				isGRead = true;
			}
			waitingSeconds = 700;
			updateTargets(buffer);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateUI();
				}
			});
			autoControl.initRunningData(FIFO_Furnace, FIFO_Command, mode, targets);
			autoControl.run();
			ArrayList<ValveVO> valveList = autoControl.getValveList();
			StoreToDB storeDB = new StoreToDB();
			storeDB.setData(fvo, valveList);
			storeDB.start();
			sendValveCommandToS7(valveList, fvo);
			writeTextArea(autoControl.getMsg());
			FurnaceUtil.checkValeActualGivenDiff(fvo);
		}

		private void sendHeartBeats() {
			rwS7.writeHeartBeats(heartBeats);
			if (heartBeats) {
				heartBeats = false;
			} else {
				heartBeats = true;
			}
		}

		private void sendValveCommandToS7(ArrayList<ValveVO> valveList, FurnaceVO fvo) {

			FurnaceControlVO cvo = new FurnaceControlVO();
			FurnaceUtil.initFurnaceControlVO(fvo, cvo);

			if (valveList != null && valveList.size() > 0) {
				valveLogger.info(fvo);
			}
			for (ValveVO vo : valveList) {
				valveLogger.info(vo);
				vo.setTimeID(fvo.getTimeID());
				FIFO_Command.addLast(vo);
				int zone = vo.getZone();
				String fuc = vo.getFunction();
				String type = vo.getType();
				if (zone == 1 && fuc.equals("F") && type.equals("A")) {
					cvo.setZ1AirFlowValve(vo.getNewValue());
				} else if (zone == 1 && fuc.equals("F") && type.equals("G")) {
					cvo.setZ1GasFlowValve(vo.getNewValue());
				} else if (zone == 1 && fuc.equals("S") && type.equals("A")) {
					cvo.setZ1AirEjectSomkeValve(vo.getNewValue());
				} else if (zone == 1 && fuc.equals("S") && type.equals("G")) {
					cvo.setZ1GasEjectSomkeValve(vo.getNewValue());
				} else if (zone == 2 && fuc.equals("F") && type.equals("A")) {
					cvo.setZ2AirFlowValve(vo.getNewValue());
				} else if (zone == 2 && fuc.equals("F") && type.equals("G")) {
					cvo.setZ2GasFlowValve(vo.getNewValue());
				} else if (zone == 2 && fuc.equals("S") && type.equals("A")) {
					cvo.setZ2AirEjectSomkeValve(vo.getNewValue());
				} else if (zone == 2 && fuc.equals("S") && type.equals("G")) {
					cvo.setZ2GasEjectSomkeValve(vo.getNewValue());
				} else if (zone == 3 && fuc.equals("F") && type.equals("A")) {
					cvo.setZ3AirFlowValve(vo.getNewValue());
				} else if (zone == 3 && fuc.equals("F") && type.equals("G")) {
					cvo.setZ3GasFlowValve(vo.getNewValue());
				} else if (zone == 3 && fuc.equals("S") && type.equals("A")) {
					cvo.setZ3AirEjectSomkeValve(vo.getNewValue());
				} else if (zone == 3 && fuc.equals("S") && type.equals("G")) {
					cvo.setZ3GasEjectSomkeValve(vo.getNewValue());
				}
			}
			long start = System.currentTimeMillis();
			rwS7.writeS7(cvo.getBytes());
			logger.info("Write PLC FOR {} mills", (System.currentTimeMillis() - start));
		}

		private void updateTargets(byte[] buffer) {
			targets.setTempZone1(new BigDecimal(S7.GetFloatAt(buffer, 246)));
			targets.setTempZone2(new BigDecimal(S7.GetFloatAt(buffer, 250)));
			targets.setTempZone3(new BigDecimal(S7.GetFloatAt(buffer, 254)));
			targets.setGasZone1(new BigDecimal(S7.GetFloatAt(buffer, 258)));
			targets.setGasZone2(new BigDecimal(S7.GetFloatAt(buffer, 262)));
			targets.setGasZone3(new BigDecimal(S7.GetFloatAt(buffer, 266)));
		}

		private void updateUI() {
			mark1.setText(fvo.getZone3().getTempTarget().toString());
			mark2.setText(fvo.getZone2().getTempTarget().toString());
			mark3.setText(fvo.getZone1().getTempTarget().toString());
			markA.setText(fvo.getZone3().getFlowZoneGASTarget().toString());
			markB.setText(fvo.getZone2().getFlowZoneGASTarget().toString());
			markC.setText(fvo.getZone1().getFlowZoneGASTarget().toString());

			DecimalFormat df = new DecimalFormat("0.00");
			airGasR_T1.setText(df.format(fvo.getZone3().getAirGasRatioACTUAL()) + " vs "
					+ df.format(fvo.getZone3().getAirGasRatioGIVEN()));
			airGasR_T2.setText(df.format(fvo.getZone2().getAirGasRatioACTUAL()) + " vs "
					+ df.format(fvo.getZone2().getAirGasRatioGIVEN()));
			airGasR_T3.setText(df.format(fvo.getZone1().getAirGasRatioACTUAL()) + " vs "
					+ df.format(fvo.getZone1().getAirGasRatioGIVEN()));
			airGasR_G1.setText(df.format(fvo.getZone3().getAirGasRatioACTUAL()) + " vs "
					+ df.format(fvo.getZone3().getAirGasRatioGIVEN()));
			airGasR_G2.setText(df.format(fvo.getZone2().getAirGasRatioACTUAL()) + " vs "
					+ df.format(fvo.getZone2().getAirGasRatioGIVEN()));
			airGasR_G3.setText(df.format(fvo.getZone1().getAirGasRatioACTUAL()) + " vs "
					+ df.format(fvo.getZone1().getAirGasRatioGIVEN()));
			repaintTickTemps(fvo);
			repaintTickGas(fvo);
			repaintTickChamper(fvo);
		}
	}

	private void buttonsPanel() {
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 35, 1));
		bPanel.setBackground(BK_COLOR);
		btnM = createExtJButton("人工模式", "M");
		bPanel.add(btnM);

		bgc = new GridBagConstraints(0, 0, 2, 1, 100, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 100, 0);

		mainFrame.add(bPanel, bgc);

		mainFrame.setVisible(true);
	}

	private ExtJButton createExtJButton(String text, String actionCommand) {

		ExtJButton btn = new ExtJButton(ExtJButton.ROUND_RECT);
		btn.setText(text);
		btn.setFont(font);
		btn.setForeground(FG_COLOR);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setBorder(BorderFactory.createRaisedBevelBorder());
		btn.setActionCommand(actionCommand);
		btn.setPreferredSize(new Dimension(300, 40));
		return btn;
	}

	private JPanel createThermalControlPanel() {
		tempPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		tempPanel.setBackground(BK_COLOR);
		JLabel[] labels = { airGasR_T1, airGasR_T2, airGasR_T3 };
		JLabel[] marks = { mark1, mark2, mark3 };
		Tick[] ticks = { tickT1, tickT2, tickT3 };

		for (int i = 0; i < ticks.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			northPanel.setBackground(BK_COLOR);
			jsPanel.setBackground(BK_COLOR);
			labels[i].setText("0.50 vs 0.50");
			labels[i].setFont(font2);
			labels[i].setForeground(FG_COLOR);
			labels[i].setHorizontalAlignment(SwingConstants.CENTER);
			ticks[i] = createTickTemp(null);
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			marks[i].setText("1100");
			northPanel.add(marks[i]);
			jsPanel.add(labels[i], BorderLayout.SOUTH);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			tempPanel.add(jsPanel);
		}
		return tempPanel;
	}

	private void repaintTickTemps(FurnaceVO fvo) {
		tempPanel.removeAll();
		tempPanel.setBackground(BK_COLOR);
		JLabel[] labels = { airGasR_T1, airGasR_T2, airGasR_T3 };
		JLabel[] marks = { mark1, mark2, mark3 };
		Tick[] ticks = { tickT1, tickT2, tickT3 };

		FurnaceZoneVO[] zvos = { fvo.getZone3(), fvo.getZone2(), fvo.getZone1() };
		for (int i = 0; i < zvos.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			northPanel.setBackground(BK_COLOR);
			jsPanel.setBackground(BK_COLOR);
			northPanel.add(marks[i]);
			ticks[i] = createTickTemp(zvos[i]);
			jsPanel.add(labels[i], BorderLayout.SOUTH);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			tempPanel.add(jsPanel);
		}
		tempPanel.updateUI();
	}

	private Tick createTickTemp(FurnaceZoneVO zvo) {
		String value = "1110";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setFrom(900);
		tick.setTo(1300);
		tick.setTickFontSize(11);
		tick.setMajor(50);
		tick.setMinor(10);
		tick.setUnit("°");
		tick.setBackground(BK_COLOR);
		if (zvo != null) {
			value = String.valueOf(zvo.getTemp().intValue());
			int actualTemp = zvo.getTemp().intValue();
			int givenTemp = zvo.getTempTarget().intValue();
			int diff = actualTemp - givenTemp;
			if (diff > 5 && mode == 1) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setValue(value);
		tick.setForeground(color);
		return tick;
	}

	private Tick createTickGas(FurnaceZoneVO zvo) {
		String value = "5000";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setValue(value);
		tick.setFrom(0);
		tick.setTo(25000);
		tick.setMajor(5000);
		tick.setMinor(1000);
		tick.setUnit("㎥");
		tick.setBackground(BK_COLOR);
		if (zvo != null) {
			value = String.valueOf(zvo.getFlowZoneGAS());
			int actualGas = zvo.getFlowZoneGAS().intValue();
			int givenGas = zvo.getFlowZoneGASTarget().intValue();
			int diff = actualGas - givenGas;
			if (diff > 1000 && mode == 2) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setForeground(color);
		tick.setValue(value);
		return tick;
	}
	
	private void repaintTickChamper(FurnaceVO fvo) {
		chamberPanel.removeAll();
		chamberPanel.setBackground(BK_COLOR);
		
		tickCP1 = createTickChamberPressure(fvo.getZone3());
		tickCP2 = createTickChamberPressure(fvo.getZone1());
		Tick black = createTickChamberPressure(null);
		black.setForeground(BK_COLOR);
		Tick[] ticks = { tickCP1, black, tickCP2 };
		JLabel[] labels = { new JLabel("20-30"), new JLabel(), new JLabel("15-20")};

		for (int i = 0; i < 3; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			northPanel.setBackground(BK_COLOR);
			jsPanel.setBackground(BK_COLOR);
			labels[i].setFont(font2);
			labels[i].setForeground(FG_COLOR);
			labels[i].setHorizontalAlignment(SwingConstants.CENTER);
			northPanel.add(labels[i]);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			chamberPanel.add(jsPanel);
		}
		chamberPanel.updateUI();				
	}

	private JPanel createChamberPanel() {
		chamberPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		chamberPanel.setBackground(BK_COLOR);
		JLabel[] labels = { new JLabel("20-30"), new JLabel(), new JLabel("15-20")};
		
		tickCP1 = createTickChamberPressure(null);
		tickCP2 = createTickChamberPressure(null);
		Tick black = createTickChamberPressure(null);
		black.setForeground(BK_COLOR);
		Tick[] ticks = { tickCP1, black, tickCP2 };

		for (int i = 0; i < 3; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			northPanel.setBackground(BK_COLOR);
			jsPanel.setBackground(BK_COLOR);
			labels[i].setFont(font2);
			labels[i].setForeground(FG_COLOR);
			labels[i].setHorizontalAlignment(SwingConstants.CENTER);
			northPanel.add(labels[i]);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			chamberPanel.add(jsPanel);
		}
		return chamberPanel;
	}

	private SmokeValveVO getSmokeValveVOFromList(int zone, String type, String pos) {
		SmokeValveVO one = null;
		if (smokeValveList != null && smokeValveList.size() > 0) {
			for (SmokeValveVO vo : smokeValveList) {
				if (vo.getZone() == zone && vo.getType().equals(type) && vo.getPosition().equals(pos)) {
					one = vo;
					break;
				}
			}
		}
		return one;
	}

	private Tick createTickSmoke(SmokeValveVO svo) {
		String value = "110";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setValue(value);
		tick.setDimensionHigh(20);
		tick.setDimensionWidth(20);
		tick.setFrom(60);
		tick.setTo(180);
		tick.setMajor(20);
		tick.setMinor(5);
		tick.setUnit("°");
		tick.setBackground(BK_COLOR);
		if (svo != null) {
			value = String.valueOf(svo.getTemp());
			int temp = svo.getTemp();
			if (temp > 160 || temp < 100) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setForeground(color);
		tick.setValue(value);
		return tick;
	}

	private Tick createTickChamberPressure(FurnaceZoneVO zvo) {
		String value = "20";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setValue(value);
		tick.setDimensionHigh(150);
		tick.setDimensionWidth(150);
		tick.setTickFontSize(11);
		tick.setFrom(0);
		tick.setTo(80);
		tick.setMajor(10);
		tick.setMinor(5);
		tick.setUnit("Kpa");
		tick.setBackground(BK_COLOR);
		tick.setForeground(FG_COLOR);
		if (zvo != null) {
			value = String.valueOf(zvo.getChamberPressure());
			float p = zvo.getChamberPressure().floatValue();
			if (zvo.getZoneID() == 1) {
				if (p > 20 || p < 15) {
					color = FG_ALARM_COLOR;
				}
			} else if (zvo.getZoneID() == 3) {
				if (p > 30 || p < 20) {
					color = FG_ALARM_COLOR;
				}
			}
		}
		tick.setForeground(color);
		tick.setValue(value);
		return tick;
	}

	private void repaintTickGas(FurnaceVO fvo) {
		gasPanel.removeAll();
		gasPanel.setBackground(BK_COLOR);
		JLabel[] labels = { airGasR_G1, airGasR_G2, airGasR_G3 };
		JLabel[] marks = { markA, markB, markC };
		Tick[] ticks = { tickG1, tickG2, tickG3 };

		FurnaceZoneVO[] zvos = { fvo.getZone3(), fvo.getZone2(), fvo.getZone1() };
		for (int i = 0; i < zvos.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			northPanel.setBackground(BK_COLOR);
			ticks[i] = createTickGas(zvos[i]);
			northPanel.add(marks[i]);
			jsPanel.add(labels[i], BorderLayout.SOUTH);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			gasPanel.add(jsPanel);
		}
		gasPanel.updateUI();
	}

	private JPanel createGasControlPanel() {
		gasPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 1));
		gasPanel.setBackground(BK_COLOR);
		JLabel[] marks = { markA, markB, markC };
		Tick[] ticks = { tickG1, tickG2, tickG3 };

		String[] names = { "4", "5", "6" };
		for (int i = 0; i < names.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			northPanel.setBackground(BK_COLOR);
			zonelabels[i].setFont(font2);
			zonelabels[i].setHorizontalAlignment(SwingConstants.CENTER);
			zonelabels[i].setForeground(FG_COLOR);
			ticks[i] = createTickGas(null);
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			marks[i].setText("5000");
			northPanel.add(marks[i]);
			jsPanel.add(zonelabels[i], BorderLayout.SOUTH);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			gasPanel.add(jsPanel);
		}
		return gasPanel;
	}

	private JPanel createBottomPanel() {
		JPanel outPanel = new JPanel(new BorderLayout());
		outPanel.add(createTextArea(), BorderLayout.CENTER);
		return outPanel;
	}

	private JScrollPane createTextArea() {
		JPanel outPanel = new JPanel(new BorderLayout());
		outPanel.setPreferredSize(new Dimension(900, 200));
		outPanel.setBackground(BK_COLOR);
		textArea = new JTextArea("", 200, 100);
		writeTextArea(statusText);
		textArea.setPreferredSize(new Dimension(900, 200));
		textArea.setEditable(false);
		textArea.setFont(textAreaFont);
		textArea.setBackground(BK_COLOR);
		textArea.setForeground(FG_COLOR);
		textArea.setSelectedTextColor(Color.WHITE);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane sPane = new JScrollPane(textArea);
		sPane.setBackground(BK_COLOR);
		sPane.setForeground(FG_COLOR);
		sPane.setPreferredSize(new Dimension(900, 200));
		sPane.setWheelScrollingEnabled(true);

		return sPane;
	}

	private void writeTextArea(String msg) {
		if (msg.trim().length() > 0) {
			Date currentTime = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			String time = formatter.format(currentTime);
			StringBuffer sb = new StringBuffer();
			sb.append(time).append(" ").append(msg).append("\n");
			textArea.insert(sb.toString(), 0);
		}
	}
}
