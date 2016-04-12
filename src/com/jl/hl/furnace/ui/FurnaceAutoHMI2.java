package com.jl.hl.furnace.ui;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.Logger;

import com.jl.hl.furnace.*;
import com.jl.hl.furnace.model.FurnaceControlVO;
import com.jl.hl.furnace.model.FurnaceVO;
import com.jl.hl.furnace.model.ValveVO;
import com.jl.hl.furnace.mysql.StoreToDB;
import com.jl.hl.furnace.plc.moka7.S7;
import com.jl.hl.furnace.plc.s7.ReadWriteS7;
import com.jl.hl.furnace.util.LoggerUtil;

public class FurnaceAutoHMI2 {

	private final static Logger logger = LoggerUtil
			.getLogger(FurnaceAutoHMI.class.getSimpleName());

	private final static Logger valveLogger = LoggerUtil
			.getLogger("valveLogger");

	private JFrame mainFrame;

	private Color FB_BLUE = new Color(59, 89, 152);
	private Color FB_MEDIUM_BLUE = new Color(59, 89, 152);
	private Color FB_LIGHTER_BLUE = new Color(175, 189, 212);
	private Color FB_LIGHTEST_BLUE = new Color(216, 223, 234);

	private Font font = new Font("黑体", Font.PLAIN, 26);
	private Font font2 = new Font("黑体", Font.PLAIN, 24);
	private Font font3 = new Font("黑体", Font.PLAIN, 22);
	private Font textAreaFont = new Font("黑体", Font.PLAIN, 17);

	GridBagLayout bgl = new GridBagLayout();
	GridBagConstraints bgc = new GridBagConstraints();

	ExtJButton btnM = new ExtJButton(ExtJButton.ROUND_RECT);
	ExtJButton btnA = new ExtJButton(ExtJButton.ROUND_RECT);
	ExtJButton btnG = new ExtJButton(ExtJButton.ROUND_RECT);

	JTextArea textArea = new JTextArea();

	private JSlider slide1 = new JSlider();
	private JSlider slide2 = new JSlider();
	private JSlider slide3 = new JSlider();
	private JSlider slideA = new JSlider();
	private JSlider slideB = new JSlider();
	private JSlider slideC = new JSlider();

	private JLabel barLabel1 = new JLabel();
	private JLabel barLabel2 = new JLabel();
	private JLabel barLabel3 = new JLabel();
	private JLabel mark1 = new JLabel();
	private JLabel mark2 = new JLabel();
	private JLabel mark3 = new JLabel();
	private JLabel slideLabelA = new JLabel();
	private JLabel slideLabelB = new JLabel();
	private JLabel slideLabelC = new JLabel();
	private JLabel markA = new JLabel();
	private JLabel markB = new JLabel();
	private JLabel markC = new JLabel();
	private JLabel markR1 = new JLabel();
	private JLabel markR2 = new JLabel();
	private JLabel markR3 = new JLabel();
	private JLabel markRA = new JLabel();
	private JLabel markRB = new JLabel();
	private JLabel markRC = new JLabel();

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

	public FurnaceAutoHMI2() {

	}

	public static void main(String[] args) {

		setLAF();

		FurnaceAutoHMI2 hmi = new FurnaceAutoHMI2();
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
		mainFrame.getContentPane().setBackground(FB_LIGHTER_BLUE);
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

		JPanel tPanel = createThermalControlPanel();
		JPanel gPanel = createGasControlPanel();

		JSplitPane splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true, tPanel, gPanel);
		splitPane1.setDividerLocation(635);
		splitPane1.setOneTouchExpandable(false);
		splitPane1.setDividerSize(5);
		splitPane1.setBorder(BorderFactory.createLineBorder(FB_BLUE, 5));

		bgc = new GridBagConstraints(0, 1, 2, 1, 100, 10,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 100, 0);

		mainFrame.add(splitPane1, bgc);

		JPanel buttomPanel = createBottomPanel();
		bgc = new GridBagConstraints(0, 2, 2, 2, 100, 100,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 100, 0);
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

		public void run() {
			while (isS7300Connected) {
				try {
					Thread.sleep(waitingSeconds);
				} catch (InterruptedException e) {
					waitingSeconds = 500;
				}

				long start = System.currentTimeMillis();
				FurnaceVO fvo = new FurnaceVO();
				byte[] buffer = rwS7.readS7();
				fvo.loadDataFromS7300(buffer);
				logger.info("GET FURNACE VO FROM PLC FOR {} mills",
						(System.currentTimeMillis() - start));
				if (fvo.getZone1().getTemp().intValue() == 0) {
					waitingSeconds = 115;
					failedCounts++;
					logger.info("failedCounts:" + failedCounts);
					if (failedCounts > 3) {
						logger.info("读取信号数据失败{}次。不再尝试与PLC通信，请查明原因后再重启本系统。",
								failedCounts);
						writeTextArea("读取信号数据失败3次。不再尝试与PLC通信，请查明原因后再重启本系统。");
						isS7300Connected = false;
					}
				} else {
					start = System.currentTimeMillis();
					// 主要逻辑在这里！！！
					runningAutoControl(fvo, buffer);
					// 写心跳给S7
					sendHeartBeats();
					logger.info("RUNNING TIME FOR {} mills",
							(System.currentTimeMillis() - start));
				}
			}
		}

		private void runningAutoControl(FurnaceVO fvo, byte[] buffer) {
			int PLCSignal = fvo.getPLCSignal();
			FIFO_Furnace.addLast(fvo);
			switch (PLCSignal) {
			case 1:
				mode = 1;
				if (isARead) {
					writeTextArea("收到您下达的自动温控模式指令，现在我们一起努力吧！");
					isARead = false;
					isMRead = true;
					isGRead = true;
				}
				break;
			case 2:
				mode = 2;
				if (isGRead) {
					writeTextArea("收到您下达的煤气定额模式指令，让我们一起节省煤气而努力哦！");
					isARead = true;
					isMRead = true;
					isGRead = false;
				}
				break;
			case 3:
				mode = 3;
				if (isMRead) {
					writeTextArea("您好，我是HAL 9000型智能烧炉辅助系统。现在是手动模式。您可以在HMI画面里选择温控模式及煤气定额模式：");
					isARead = true;
					isMRead = false;
					isGRead = true;
				}
				break;
			default:
				isARead = true;
				isMRead = true;
				isGRead = true;
			}
			waitingSeconds = 800;
			updateTargets(buffer);
			updateTemp(fvo);
			autoControl.initRunningData(FIFO_Furnace, FIFO_Command, mode,
					targets);
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

		private void sendValveCommandToS7(ArrayList<ValveVO> valveList,
				FurnaceVO fvo) {

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
			logger.info("Write PLC FOR {} mills",
					(System.currentTimeMillis() - start));
		}

		private void updateTargets(byte[] buffer) {
			targets.setTempZone1(new BigDecimal(S7.GetFloatAt(buffer, 246)));
			targets.setTempZone2(new BigDecimal(S7.GetFloatAt(buffer, 250)));
			targets.setTempZone3(new BigDecimal(S7.GetFloatAt(buffer, 254)));
			targets.setGasZone1(new BigDecimal(S7.GetFloatAt(buffer, 258)));
			targets.setGasZone2(new BigDecimal(S7.GetFloatAt(buffer, 262)));
			targets.setGasZone3(new BigDecimal(S7.GetFloatAt(buffer, 266)));

			mark1.setText(String.valueOf(targets.getTempZone3().intValue()));
			mark2.setText(String.valueOf(targets.getTempZone2().intValue()));
			mark3.setText(String.valueOf(targets.getTempZone1().intValue()));
			markA.setText(String.valueOf(targets.getGasZone3().intValue()));
			markB.setText(String.valueOf(targets.getGasZone2().intValue()));
			markC.setText(String.valueOf(targets.getGasZone1().intValue()));
		}

		private void updateTemp(FurnaceVO fvo) {
			markR1.setText("(" + fvo.getZone3().getTemp().intValue() + ")");
			markR2.setText("(" + fvo.getZone2().getTemp().intValue() + ")");
			markR3.setText("(" + fvo.getZone1().getTemp().intValue() + ")");
			markRA.setText("(" + fvo.getZone3().getFlowZoneGAS() + ")");
			markRB.setText("(" + fvo.getZone2().getFlowZoneGAS() + ")");
			markRC.setText("(" + fvo.getZone1().getFlowZoneGAS() + ")");
		}
	}

	private void buttonsPanel() {

		JPanel bPanel = new JPanel();
		bPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 35, 5));
		bPanel.setBackground(FB_LIGHTER_BLUE);
		btnM = createExtJButton("停止", "M");
		btnA = createExtJButton("温控模式", "A");
		btnG = createExtJButton("煤气模式", "G");
		bPanel.add(btnA);
		bPanel.add(btnM);
		bPanel.add(btnG);
		bgc = new GridBagConstraints(0, 0, 2, 1, 120, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 100, 0);

		mainFrame.add(bPanel, bgc);

		mainFrame.setVisible(true);
	}

	private ExtJButton createExtJButton(String text, String actionCommand) {

		ExtJButton btn = new ExtJButton(ExtJButton.ROUND_RECT);
		btn.setText(text);
		btn.setFont(font);
		btn.setBorder(BorderFactory.createRaisedBevelBorder());
		btn.addActionListener(new ClickListener());
		btn.setActionCommand(actionCommand);
		btn.setPreferredSize(new Dimension(120, 60));

		return btn;

	}

	private JPanel createThermalControlPanel() {
		JPanel outPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		outPanel.setBackground(FB_BLUE);
		JLabel[] labels = { barLabel1, barLabel2, barLabel3 };
		JLabel[] marks = { mark1, mark2, mark3 };
		JLabel[] rmarks = { markR1, markR2, markR3 };
		String[] locs = { "均热段温度", "加2段温度", "加1段温度" };
		JSlider[] jss = { slide1, slide2, slide3 };
		String[] names = { "1", "2", "3" };
		for (int i = 0; i < jss.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5,
					5));
			northPanel.setBackground(FB_BLUE);
			jsPanel.setBackground(FB_BLUE);
			labels[i].setText(locs[i]);
			labels[i].setFont(font2);
			labels[i].setForeground(FB_LIGHTEST_BLUE);
			jss[i] = createJSlider(jss[i], Integer.valueOf(names[i]).intValue());
			jss[i] = doJSProperties(jss[i], names[i]);
			jss[i].setMajorTickSpacing(50);
			jss[i].setMinorTickSpacing(1);
			jss[i].addChangeListener(new SlideChangeListener());
			marks[i] = setMarkProperties(marks[i],
					String.valueOf(jss[i].getValue()));
			rmarks[i] = setMarkProperties(rmarks[i],
					String.valueOf(jss[i].getValue()));
			northPanel.add(marks[i]);
			northPanel.add(rmarks[i]);
			jsPanel.add(labels[i], BorderLayout.SOUTH);
			jsPanel.add(jss[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			outPanel.add(jsPanel);
		}
		return outPanel;
	}

	private JLabel setMarkProperties(JLabel l, String value) {
		l.setFont(font3);
		l.setForeground(FB_LIGHTEST_BLUE);
		l.setText(value);
		l.setHorizontalTextPosition(SwingConstants.CENTER);
		return l;
	}

	private JPanel createBottomPanel() {
		JPanel outPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		outPanel.setPreferredSize(new Dimension(900, 440));
		outPanel.setBackground(FB_LIGHTER_BLUE);
		textArea = new JTextArea("", 500, 100);
		writeTextArea(statusText);
		textArea.setPreferredSize(new Dimension(900, 440));
		textArea.setEditable(false);
		textArea.setFont(textAreaFont);
		textArea.setBackground(FB_MEDIUM_BLUE);
		textArea.setForeground(FB_LIGHTEST_BLUE);
		textArea.setSelectedTextColor(Color.WHITE);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane sPane = new JScrollPane(textArea);
		sPane.setPreferredSize(new Dimension(900, 440));
		sPane.setWheelScrollingEnabled(true);
		sPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		outPanel.add(sPane);
		return outPanel;
	}

	private JSlider createJSlider(JSlider js, int i) {
		switch (i) {
		case 1:
			js = new JSlider(JSlider.VERTICAL, 1150, 1300, 1200);
			targets.setTempZone3(new BigDecimal(1200));
			break;
		case 2:
			js = new JSlider(JSlider.VERTICAL, 1100, 1300, 1170);
			targets.setTempZone2(new BigDecimal(1170));
			break;
		case 3:
			js = new JSlider(JSlider.VERTICAL, 900, 1220, 1100);
			targets.setTempZone1(new BigDecimal(1170));
			break;
		}
		return js;
	}

	private JPanel createGasControlPanel() {
		JPanel outPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 5));
		outPanel.setBackground(FB_BLUE);
		JLabel[] labels = { slideLabelA, slideLabelB, slideLabelC };
		JLabel[] marks = { markA, markB, markC };
		JLabel[] rmarks = { markRA, markRB, markRC };
		String[] locs = { "均热段煤气", "加2段煤气", "加1段煤气" };
		JSlider[] jss = { slideA, slideB, slideC };
		String[] names = { "4", "5", "6" };
		for (int i = 0; i < jss.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(FB_BLUE);
			JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5,
					5));
			northPanel.setBackground(FB_BLUE);
			labels[i].setText(locs[i]);
			labels[i].setFont(font2);
			labels[i].setForeground(FB_LIGHTEST_BLUE);
			jss[i] = createGasJSlider(jss[i], Integer.valueOf(names[i])
					.intValue());
			jss[i] = doJSProperties(jss[i], names[i]);
			jss[i].setMajorTickSpacing(5000);
			jss[i].setMinorTickSpacing(1000);
			jss[i].addChangeListener(new gasSlideChangeListener());
			marks[i] = setMarkProperties(marks[i],
					String.valueOf(jss[i].getValue()));
			rmarks[i] = setMarkProperties(rmarks[i],
					String.valueOf(jss[i].getValue()));
			northPanel.add(marks[i]);
			northPanel.add(rmarks[i]);
			jsPanel.add(labels[i], BorderLayout.SOUTH);
			jsPanel.add(jss[i], BorderLayout.CENTER);
			jsPanel.add(northPanel, BorderLayout.NORTH);
			outPanel.add(jsPanel);
		}
		return outPanel;
	}

	private JSlider createGasJSlider(JSlider js, int i) {
		switch (i) {
		case 4:
			js = new JSlider(JSlider.VERTICAL, 0, 17000, 5000);
			targets.setGasZone3(new BigDecimal(5000));
			break;
		case 5:
			js = new JSlider(JSlider.VERTICAL, 0, 22000, 5000);
			targets.setGasZone2(new BigDecimal(5000));
			break;
		case 6:
			js = new JSlider(JSlider.VERTICAL, 0, 22000, 5000);
			targets.setGasZone1(new BigDecimal(5000));
			break;
		}
		return js;
	}

	private JSlider doJSProperties(JSlider js, String name) {
		js.setPaintTicks(true);
		js.setSnapToTicks(true);
		js.setPaintLabels(true);
		js.setAutoscrolls(true);
		js.setBorder(BorderFactory.createRaisedBevelBorder());
		js.setFont(font3);
		js.setPreferredSize(new Dimension(140, 270));
		js.setBackground(FB_LIGHTER_BLUE);
		js.setName(name);
		return js;
	}

	private class ClickListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			System.out.println("command:" + command);
			if (command.equals("M")) {
				writeTextArea("停止自动控制");
				mode = 3;
			} else if (command.equals("A")) {
				writeTextArea("开始正常温度控制模式");
				mode = 1;
			} else if (command.equals("G")) {
				writeTextArea("开始煤气控制模式");
				mode = 2;
			}
		}
	}

	private class SlideChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider js = (JSlider) e.getSource();
			String barCode = js.getName();

			switch (Integer.valueOf(barCode).intValue()) {
			case 1:
				mark1.setText(String.valueOf(js.getValue()));
				targets.setTempZone3(new BigDecimal(js.getValue()));
				break;
			case 2:
				mark2.setText(String.valueOf(js.getValue()));
				targets.setTempZone2(new BigDecimal(js.getValue()));
				break;
			case 3:
				mark3.setText(String.valueOf(js.getValue()));
				targets.setTempZone1(new BigDecimal(js.getValue()));
				break;
			}
		}
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

	private class gasSlideChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider js = (JSlider) e.getSource();
			String barCode = js.getName();

			switch (Integer.valueOf(barCode).intValue()) {
			case 4:
				markA.setText(String.valueOf(js.getValue()));
				targets.setGasZone3(new BigDecimal(js.getValue()));
				break;
			case 5:
				markB.setText(String.valueOf(js.getValue()));
				targets.setGasZone2(new BigDecimal(js.getValue()));
				break;
			case 6:
				markC.setText(String.valueOf(js.getValue()));
				targets.setGasZone1(new BigDecimal(js.getValue()));
				break;
			}
		}
	}
}

