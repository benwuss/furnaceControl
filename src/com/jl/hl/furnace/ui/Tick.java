package com.jl.hl.furnace.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 刻度盘
 * 
 * @author xdoc
 */
public class Tick extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int VALUE_FONT_SIZE = 14;
	private double from = 0;
	private int tickFontSize = 10;
	private double to = 10;
	private String type = "line";
	private double major = 1;
	private double minor = 0.1;
	private String value = "";
	private String unit = "";
	private int dimensionWidth = 170;
	private int dimensionHigh = 170;
	
	public void setDimensionWidth(int dimensionWidth) {
		this.dimensionWidth = dimensionWidth;
	}

	public void setDimensionHigh(int dimensionHigh) {
		this.dimensionHigh = dimensionHigh;
	}

	public void setTickFontSize(int tickFontSize) {
		this.tickFontSize = tickFontSize;
	}

	public double getFrom() {
		return from;
	}

	public Tick() {
		super();
		this.setPreferredSize(new Dimension(dimensionWidth, dimensionHigh));
		this.setBackground(Color.WHITE);
	}

	public void setFrom(double from) {
		this.from = from;
	}

	public double getMajor() {
		return major;
	}

	public void setMajor(double major) {
		this.major = major;
	}

	public double getMinor() {
		return minor;
	}

	public void setMinor(double minor) {
		this.minor = minor;
	}

	public double getTo() {
		return to;
	}

	public void setTo(double to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	/**
	 * 直尺
	 */
	public static final String LINE = "line";
	/**
	 * 120度圆环
	 */
	public static final String RING_120 = "ring120";
	public static final String RING_180 = "ring180";
	public static final String RING_240 = "ring240";
	/**
	 * 圆盘
	 */
	public static final String CIRCLE = "circle";

	public void setType(String type) {
		this.type = type;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void paintComponent(Graphics g) {
		double w = this.getWidth();
		double h = this.getHeight();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, (int) w, (int) h);
		g2.setColor(this.getForeground());
		g2.setStroke(new BasicStroke(1));
		int fontSize = tickFontSize;
		g2.setFont(new Font(Font.SERIF, Font.PLAIN, fontSize));
		if (major <= 0) {
			major = to - from;
		}
		if (to > from) {
			if (type.startsWith("ring") || type.equals("circle")) {
				double angle;
				double angleStart;
				if (type.equals("circle")) {
					angle = 360;
					angleStart = Math.PI / 2;
				} else {
					angle = toDouble(type.substring(4));
					angleStart = (180 + (angle - 180) / 2) / 180 * Math.PI;
				}
				double r = angle <= 180 ? Math.min(w / 2, h) : Math.min(w / 2,
						h / 2);
				double voff = angle <= 180 ? 0 : r;
				double dunit = (angle / 180 * Math.PI) / (to - from);
				for (int i = 0; i <= (to - from) / major; i++) {
					g2.draw(new Line2D.Double(Math.cos(angleStart - i * major
							* dunit)
							* r + w / 2, h - voff
							- Math.sin(angleStart - i * major * dunit) * r,
							Math.cos(angleStart - i * major * dunit) * r * 0.75
									+ w / 2, h - voff
									- Math.sin(angleStart - i * major * dunit)
									* r * 0.75));
					if (minor > 0 && i < (to - from) / major) {
						for (int j = 1; j < major / minor; j++) {
							if (i * major + j * minor < to - from) {
								g2.draw(new Line2D.Double(Math.cos(angleStart
										- (i * major + j * minor) * dunit)
										* r + w / 2, h
										- voff
										- Math.sin(angleStart
												- (i * major + j * minor)
												* dunit) * r, Math
										.cos(angleStart
												- (i * major + j * minor)
												* dunit)
										* r * 0.875 + w / 2, h
										- voff
										- Math.sin(angleStart
												- (i * major + j * minor)
												* dunit) * r * 0.875));
							}
						}
					}
				}
				if (value.length() > 0) {
					double val = toDouble(value);
					GeneralPath p = new GeneralPath();
					p.moveTo(
							Math.cos(angleStart - (val - from) * dunit) * r
									* 0.875 + w / 2,
							h
									- voff
									- Math.sin(angleStart - (val - from)
											* dunit) * r * 0.875);
					p.lineTo(
							Math.cos(angleStart - (val - from) * dunit
									+ Math.PI * 0.5)
									* 2 + w / 2,
							h
									- voff
									- Math.sin(angleStart - (val - from)
											* dunit + Math.PI * 0.5) * 2);
					p.lineTo(
							Math.cos(angleStart - (val - from) * dunit
									- Math.PI * 0.5)
									* 2 + w / 2,
							h
									- voff
									- Math.sin(angleStart - (val - from)
											* dunit - Math.PI * 0.5) * 2);
					p.closePath();
					g2.fill(p);
				}
			} else {
				if (w > h) {
					double dunit = w / (to - from);
					for (int i = 0; i <= (to - from) / major; i++) {
						g2.draw(new Line2D.Double(i * major * dunit, 0, i
								* major * dunit, h - fontSize));
						if (i < (to - from) / major && minor > 0) {
							for (int j = 1; j < major / minor; j++) {
								g2.draw(new Line2D.Double((i * major + j
										* minor)
										* dunit, 0, (i * major + j * minor)
										* dunit, (h - fontSize) / 2));
							}
						}
					}
					if (value.length() > 0) {
						double val = toDouble(value);
						GeneralPath p = new GeneralPath();
						p.moveTo((val - from) * dunit, (h - fontSize) / 2);
						p.lineTo((val - from) * dunit - 4, h - fontSize);
						p.lineTo((val - from) * dunit + 4, h - fontSize);
						p.closePath();
						g2.fill(p);
					}
				} else {
					int max = (int) Math.max(getStrBounds(g2, format(from))
							.getWidth(), getStrBounds(g2, format(to))
							.getWidth());
					double dunit = h / (to - from);
					for (int i = 0; i <= (to - from) / major; i++) {
						g2.draw(new Line2D.Double(0, h - i * major * dunit, w
								- max, h - i * major * dunit));
						if (i < (to - from) / major && minor > 0) {
							for (int j = 1; j < major / minor; j++) {
								g2.draw(new Line2D.Double(0, h
										- (i * major + j * minor) * dunit,
										(w - max) / 2, h
												- (i * major + j * minor)
												* dunit));
							}
						}
					}
					if (value.length() > 0) {
						double val = toDouble(value);
						GeneralPath p = new GeneralPath();
						p.moveTo((w - max) / 2, h - (val - from) * dunit);
						p.lineTo(w - max, h - (val - from) * dunit - 4);
						p.lineTo(w - max, h - (val - from) * dunit + 4);
						p.closePath();
						g2.fill(p);
					}
				}
			}
		}
		if (to > from) {
			if (major > 0) {
				if (type.startsWith("ring") || type.equals("circle")) {
					double angle;
					double angleStart;
					if (type.equals("circle")) {
						angle = 360;
						angleStart = Math.PI / 2;
					} else {
						angle = toDouble(type.substring(4));
						angleStart = (180 + (angle - 180) / 2) / 180 * Math.PI;
					}
					double r = angle <= 180 ? Math.min(w / 2, h) : Math.min(
							w / 2, h / 2);
					double voff = angle <= 180 ? 0 : r;
					double dunit = (angle / 180 * Math.PI) / (to - from);
					int xoff = 0;
					int yoff = 0;
					double strAngle;
					for (int i = type.equals("circle") ? 1 : 0; i <= (to - from)
							/ major; i++) {
						String str;
						str = format(from + i * major);
						strAngle = (angleStart - i * major * dunit + Math.PI * 2)
								% (Math.PI * 2);
						xoff = 0;
						yoff = 0;
						if (strAngle >= 0 && strAngle < Math.PI * 0.25) {
							xoff = (int) -getStrBounds(g2, str).getWidth();
							yoff = fontSize / 2;
							if (strAngle == 0 && angle == 180) {
								yoff = 0;
							}
						} else if (near(strAngle, Math.PI * 0.5)) {
							xoff = (int) -getStrBounds(g2, str).getWidth() / 2;
							yoff = fontSize;
						} else if (strAngle >= Math.PI * 0.25
								&& strAngle < Math.PI * 0.5) {
							xoff = (int) -getStrBounds(g2, str).getWidth();
							yoff = fontSize;
						} else if (strAngle >= Math.PI * 0.5
								&& strAngle < Math.PI * 0.75) {
							yoff = fontSize;
						} else if (strAngle >= Math.PI * 0.75
								&& strAngle < Math.PI) {
							yoff = fontSize / 2;
						} else if (near(strAngle, Math.PI)) {
							xoff = 1;
							yoff = fontSize / 2;
							if (angle == 180) {
								yoff = 0;
							}
						} else if (strAngle >= Math.PI
								&& strAngle < Math.PI * 1.25) {
							yoff = fontSize / 4;
						} else if (near(strAngle, Math.PI * 1.5)) {
							xoff = (int) -getStrBounds(g2, str).getWidth() / 2;
						} else if (strAngle >= Math.PI * 1.5
								&& strAngle < Math.PI * 2) {
							xoff = (int) -getStrBounds(g2, str).getWidth();
						}
						g2.drawString(
								str,
								(int) (Math.cos(strAngle) * r * 0.75 + w / 2)
										+ xoff,
								(int) (h - voff - Math.sin(strAngle) * r * 0.75)
										+ yoff);
					}
					g2.setFont(new Font("", Font.BOLD, VALUE_FONT_SIZE));
					if (value.length() > 0) {
						voff = angle <= 180 ? 10 : r - fontSize / 2;
						drawValue(g2, value + unit,
								(int) (w / 2 - getStrBounds(g2, value)
										.getWidth() / 2), (int) (h - voff));
					}
				} else {
					if (w > h) {
						double dunit = w / (to - from);
						int off = 0;
						String str;
						for (int i = 0; i <= (to - from) / major; i++) {
							str = format(from + i * major);
							if (i == 0) {
								str += unit;
								off = 0;
							} else if (i == (to - from) / major) {
								off = (int) -getStrBounds(g2, str).getWidth();
							} else {
								off = (int) (-getStrBounds(g2, str).getWidth() / 2);
							}
							g2.drawString(str, (int) (i * major * dunit) + off,
									(int) (h - 2));
						}
						if (value.length() > 0) {
							double val = toDouble(value);
							value = format(val);
							if (val == from) {
								off = 0;
							} else if (val == to) {
								off = (int) -getStrBounds(g2, value + unit)
										.getWidth();
							} else {
								off = (int) (-getStrBounds(g2, value + unit)
										.getWidth() / 2);
							}
							if ((h - fontSize) / 2 >= fontSize) {
								drawValue(g2, value + unit,
										(int) ((val - from) * dunit) + off,
										(int) ((h - fontSize) / 2));
							} else {
								drawValue(g2, value + unit,
										(int) ((val - from) * dunit) + off,
										fontSize);
							}
						}
					} else {
						double dunit = h / (to - from);
						int max = (int) Math.max(getStrBounds(g2, format(from))
								.getWidth(), getStrBounds(g2, format(to))
								.getWidth());
						int off = 0;
						for (int i = 0; i <= (to - from) / major; i++) {
							if (i == 0) {
								off = 0;
							} else if (i == (to - from) / major) {
								off = fontSize;
							} else {
								off = fontSize / 2;
							}
							g2.drawString(format(from + i * major), (int) (w
									- max + 1), (int) (h - i * major * dunit)
									+ off);
						}
						if (unit.length() > 0) {
							g2.drawString(unit, (int) ((w - max) / 2 + 1),
									(int) (h - fontSize));
						}
						if (value.length() > 0) {
							double val = toDouble(value);
							value = format(val);
							if (val == 0) {
								off = 0;
							} else if (val == to) {
								off = VALUE_FONT_SIZE;
							} else {
								off = VALUE_FONT_SIZE / 2;
							}
							drawValue(g2, value + unit,
									(int) ((w - getStrBounds(g2, value + unit)
											.getWidth()) / 2), (int) (h
											- (val - from) * dunit + off));
						}
					}
				}
			}
		}		
	}

	private void drawValue(Graphics2D g2, String value, int x, int y) {
		g2.setFont(new Font(Font.SERIF, Font.BOLD, VALUE_FONT_SIZE));
		g2.drawString(value, x, y);
	}

	private String format(double d) {
		if ((int) d == d) {
			return String.valueOf((int) d);
		} else {
			return String.valueOf(d);
		}
	}

	private double toDouble(String string) {
		try {
			return Double.valueOf(string);
		} catch (Exception e) {
			return 0;
		}
	}

	private boolean near(double d1, double d2) {
		return Math.round(d1 * 1000000) == Math.round(d2 * 1000000);
	}

	private static Rectangle2D getStrBounds(Graphics2D g, String str) {
		Font f = g.getFont();
		Rectangle2D rect = f.getStringBounds(str, g.getFontRenderContext());
		if (rect.getHeight() < f.getSize()) {
			rect.setFrame(rect.getX(), rect.getY(), rect.getWidth(),
					f.getSize() + 1);
		}
		return rect;
	}

	public static void main(String[] args) {
		try {
			JFrame f = new JFrame("刻度盘测试");
			Container p = f.getContentPane();
			Tick tick;
			tick = new Tick();
			tick.setType(Tick.LINE);
			tick.setValue("3");
			tick.setUnit("CM");
			p.add(tick, BorderLayout.NORTH);
			tick = new Tick();
			tick.setType(Tick.LINE);
			p.add(tick, BorderLayout.WEST);
			JPanel panel = new JPanel(new GridLayout(2, 2));
			tick = new Tick();
			tick.setForeground(Color.BLUE);
			tick.setType(Tick.RING_180);
			tick.setDimensionHigh(100);
			tick.setDimensionWidth(100);
			tick.setFrom(40);
			tick.setTo(180);
			tick.setMajor(20);
			tick.setMinor(2);
			tick.setUnit("°");
			tick.setValue("98");			
			panel.add(tick);
			tick = new Tick();
			tick.setType(Tick.CIRCLE);
			tick.setValue("20");
			tick.setDimensionHigh(240);
			tick.setDimensionWidth(240);
			tick.setFrom(0);
			tick.setTo(100);
			tick.setMajor(10);
			tick.setMinor(1);
			tick.setUnit("Kpa");
			panel.add(tick);
			tick = new Tick();
			tick.setType(Tick.RING_240);
			tick.setDimensionWidth(80);
			tick.setDimensionHigh(80);
			tick.setValue("1110");
			tick.setFrom(900);
			tick.setTo(1300);
			tick.setMajor(50);
			tick.setMinor(5);
			tick.setUnit("度");
			tick.setForeground(Color.red);
			panel.add(tick);
			tick = new Tick();
			tick.setType(Tick.CIRCLE);
			tick.setDimensionWidth(100);
			tick.setDimensionHigh(100);
			tick.setValue("3");
			tick.setUnit("Kg");
			tick.setBackground(Color.BLACK);
			tick.setForeground(Color.WHITE);
			panel.add(tick);
			p.add(panel, BorderLayout.CENTER);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setSize(640, 480);
			f.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}