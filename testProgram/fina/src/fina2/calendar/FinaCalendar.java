package fina2.calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

import fina2.i18n.Language;

public class FinaCalendar implements Runnable, WindowFocusListener,
		WindowListener {
	protected static Font plain = new Font("Arial", Font.PLAIN, 10);
	protected static Font bold = new Font("Arial", Font.BOLD, 10);
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private static long lastTime = 0;
	public static boolean ACTIVE = false;

	public static class DayLabel extends JLabel implements MouseInputListener,
			MouseMotionListener {
		private FinaCalendar parent;

		public DayLabel(FinaCalendar parent, int day) {
			super(Integer.toString(day));
			this.parent = parent;
			setHorizontalAlignment(SwingConstants.CENTER);
			setFont(plain);
			this.addMouseListener(this);
		}

		public void setCurrentDayStyle() {
			setFont(bold);
			setForeground(Color.RED);
		}

		public void setSelectedDayStyle() {
			setFont(plain);
			setForeground(Color.BLUE);
			setBorder(BorderFactory.createLineBorder(Color.GRAY));
		}

		public void setWeekendStyle() {
			setFont(plain);
			setForeground(Color.GRAY);
		}

		public void mouseClicked(MouseEvent e) {
			parent.dayPicked(Integer.parseInt(getText()));
		}

		public void mousePressed(MouseEvent e) {

		}

		public void mouseReleased(MouseEvent e) {

		}

		private Border oldBorder;

		public void mouseEntered(MouseEvent e) {
			oldBorder = this.getBorder();
			Border b = BorderFactory
					.createBevelBorder(javax.swing.border.BevelBorder.RAISED);
			b = BorderFactory.createEtchedBorder();
			this.setBorder(b);
		}

		public void mouseExited(MouseEvent e) {
			this.setBorder(oldBorder);
		}

		public void mouseDragged(MouseEvent e) {

		}

		public void mouseMoved(MouseEvent e) {
		}

	}

	public static class MonthPanel extends JPanel {
		private FinaCalendar parent;

		public MonthPanel(FinaCalendar parent, Calendar c) {
			this.parent = parent;
			GridLayout g = new GridLayout();
			g.setColumns(7);
			g.setRows(0);
			this.setLayout(g);

			for (int i = 0; i < 7; i++) {
				JLabel wd = new JLabel(parent.getString("fina2.calendar.week."
						+ i, ""));
				wd.setHorizontalAlignment(SwingConstants.CENTER);
				if (i == 0)
					wd.setForeground(Color.RED);
				else if (i == 6)
					wd.setForeground(Color.gray);
				this.add(wd);
			}
			setDaysOfMonth(c);
			this.setPreferredSize(new Dimension(200, 120));

		}

		private void setDaysOfMonth(Calendar c) {
			Calendar curr = new GregorianCalendar();
			int currdate = curr.get(Calendar.DAY_OF_MONTH);
			int currmon = curr.get(Calendar.MONTH);
			int curryear = curr.get(Calendar.YEAR);

			int seldate = -1;
			int selmon = -1;
			int selyear = -1;
			if (parent.selectedDate != null) {
				seldate = parent.selectedDate.get(Calendar.DAY_OF_MONTH);
				selmon = parent.selectedDate.get(Calendar.MONTH);
				selyear = parent.selectedDate.get(Calendar.YEAR);
			}

			int date = c.get(Calendar.DAY_OF_MONTH);
			int mon = c.get(Calendar.MONTH);
			int year = c.get(Calendar.YEAR);
			int day = c.get(Calendar.DAY_OF_WEEK);
			int start = (7 - (date - day) % 7) % 7;
			int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);

			for (int i = 0; i < start; i++) {
				JLabel lbl = new JLabel("");
				add(lbl);
			}
			int pos = start;
			for (int i = 1; i <= days; i++) {
				pos++;
				DayLabel lbl = new DayLabel(parent, i);
				if (seldate == i && selmon == mon && selyear == year)
					lbl.setSelectedDayStyle();
				if (currdate == i && currmon == mon && curryear == year)
					lbl.setCurrentDayStyle();
				if (pos % 7 == 0 || pos % 7 == 1)
					lbl.setWeekendStyle();
				add(lbl);

			}
		}
	}

	public static class NavigatePanel extends JPanel implements ActionListener {
		private fina2.ui.UIManager ui = fina2.Main.main.ui;

		private FinaCalendar parent;
		private JButton premon;
		private JButton preyear;
		private JButton nextmon;
		private JButton nextyear;
		private JLabel lbl;

		private byte[] getImage(String fileName) {
			InputStream is = null;

			try {
				is = new BufferedInputStream(FinaCalendar.class
						.getClassLoader().getResourceAsStream(fileName));
				byte[] b = new byte[is.available()];
				is.read(b);
				return b;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}

		public NavigatePanel(FinaCalendar parent) {
			this.parent = parent;
			ui.loadIcon("fina2.finacalendar.nextmon", "nextmon.gif");
			ui.loadIcon("fina2.finacalendar.nextyear", "nextyear.gif");
			ui.loadIcon("fina2.finacalendar.premon", "premon.gif");
			ui.loadIcon("fina2.finacalendar.preyear", "preyear.gif");
			setLayout(new BorderLayout());
			Dimension d = new Dimension(20, 20);
			Box box = new Box(BoxLayout.X_AXIS);
			preyear = new JButton();
			preyear.setToolTipText(parent.getString("fina2.calendar.pre.year",
					"Previous year."));
			ImageIcon icon = ui.getIcon("fina2.finacalendar.preyear");
			preyear.setIcon(icon);
			preyear.addActionListener(this);
			preyear.setPreferredSize(d);
			box.add(preyear);

			box.add(Box.createHorizontalStrut(3));

			premon = new JButton();
			premon.setToolTipText(parent.getString("fina2.calendar.pre.mon",
					"Previous Month"));

			icon = ui.getIcon("fina2.finacalendar.premon");
			premon.setIcon(icon);
			premon.addActionListener(this);
			premon.setPreferredSize(d);
			box.add(premon);

			add(box, BorderLayout.WEST);

			box = new Box(BoxLayout.X_AXIS);
			nextmon = new JButton();
			nextmon.setToolTipText(parent.getString("fina2.calendar.next.mon",
					"Next month."));
			icon = ui.getIcon("fina2.finacalendar.nextmon");
			nextmon.setIcon(icon);
			nextmon.setPreferredSize(d);
			nextmon.addActionListener(this);
			box.add(nextmon);

			box.add(Box.createHorizontalStrut(3));

			nextyear = new JButton();
			nextyear.setToolTipText(parent.getString(
					"fina2.calendar.next.year", "Next year."));
			icon = ui.getIcon("fina2.finacalendar.nextyear");
			nextyear.setIcon(icon);
			nextyear.setPreferredSize(d);
			nextyear.addActionListener(this);
			box.add(nextyear);

			add(box, BorderLayout.EAST);
			setCurrentMonth(parent.calendar);
		}

		private JComboBox monthBox;
		private JComboBox yearBox;
		private String[] months;
		private Integer[] years;
		private Box box;
		final int height = 10;

		public void setCurrentMonth(Calendar c) {
			setMonthComboBox(c);
			setYearComboBox(c);

			if (box == null) {
				box = new Box(BoxLayout.X_AXIS);
				box.add(monthBox);
				box.add(yearBox);
				add(box, BorderLayout.CENTER);
			}

		}

		private void setMonthComboBox(Calendar c) {
			if (months == null) {
				months = new String[12];
				for (int i = 0; i < 12; i++) {
					String m = parent
							.getString("fina2.calendar.month." + i, "");
					months[i] = m;
				}
			}
			if (monthBox == null) {
				monthBox = new JComboBox();
				monthBox.addActionListener(this);
				monthBox.setFont(FinaCalendar.plain);
				monthBox.setSize(monthBox.getWidth(), height);
				monthBox.setPreferredSize(new Dimension(monthBox.getWidth(),
						height));
			}
			monthBox.setModel(new DefaultComboBoxModel(months));
			monthBox.setSelectedIndex(c.get(Calendar.MONTH));
		}

		private void setYearComboBox(Calendar c) {
			int y = c.get(Calendar.YEAR);
			years = new Integer[7];
			for (int i = y - 3, j = 0; i <= y + 3; i++, j++) {
				years[j] = new Integer(i);
			}
			if (yearBox == null) {
				yearBox = new JComboBox();
				yearBox.addActionListener(this);
				yearBox.setFont(FinaCalendar.plain);
				yearBox.setSize(yearBox.getWidth(), height);
				yearBox.setPreferredSize(new Dimension(yearBox.getWidth(),
						height));
			}
			yearBox.setModel(new DefaultComboBoxModel(years));
			yearBox.setSelectedItem(years[3]);
		}

		public void setLabel(Calendar c) {
			if (lbl != null)
				remove(lbl);
			lbl = new JLabel(parent.getString(
					"fina2.calendar.month." + c.get(Calendar.MONTH), "")
					+ ", " + c.get(Calendar.YEAR));
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			add(lbl, BorderLayout.CENTER);

		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			Calendar c = new GregorianCalendar();
			c.setTime(parent.getCalendar().getTime());
			if (src instanceof JButton) {
				if (e.getSource() == premon)
					c.add(Calendar.MONTH, -1);
				else if (e.getSource() == nextmon)
					c.add(Calendar.MONTH, 1);
				else if (e.getSource() == nextyear)
					c.add(Calendar.YEAR, 1);
				if (e.getSource() == preyear)
					c.add(Calendar.YEAR, -1);
				parent.updateScreen(c);
			} else if (src instanceof JComboBox) {
				JComboBox jcb = (JComboBox) src;
				if (src == monthBox) {
					c.set(Calendar.MONTH, jcb.getSelectedIndex());
				} else if (e.getSource() == yearBox) {
					c.set(Calendar.YEAR,
							years[jcb.getSelectedIndex()].intValue());
					setYearComboBox(c);
				}
				parent.setMonthPanel(c);
				parent.screen.pack();
			}
		}

	}

	private MonthPanel monthPanel;

	private NavigatePanel navPanel;

	protected Calendar calendar;

	private Calendar selectedDate;

	private boolean closeOnSelect = true;

	private Locale locale = Locale.US;

	private DateFormat sdf;

	private JDialog screen;

	private JTextField textField;

	public FinaCalendar(JTextField textField) {
		this(textField, new Date());
	}

	public FinaCalendar(JTextField textField, Date selecteddate) {
		this(textField, selecteddate, Locale.US);
	}

	public FinaCalendar(JTextField textField, Locale locale) {
		this(textField, new Date(), locale);
	}

	public FinaCalendar(JTextField textField, Date selecteddate, Locale locale) {
		super();
		sdf = ui.getDateFromat();
		plain = ui.getFont();
		bold = new Font(plain.getName(), Font.BOLD, 10);
		this.locale = locale;
		screen = new JDialog();
		screen.addWindowFocusListener(this);
		screen.setSize(200, 200);
		screen.setResizable(false);
		screen.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		screen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		screen.getContentPane().setLayout(new BorderLayout());
		calendar = new GregorianCalendar();
		this.textField = textField;
		setSelectedDate(selecteddate);
		Calendar c = calendar;
		if (selectedDate != null)
			c = selectedDate;
		updateScreen(c);
		screen.getContentPane().add(navPanel, BorderLayout.NORTH);

		screen.setTitle(getString("fina2.calendar.program.title", "Calendar"));

		screen.addWindowListener(this);
	}

	private DateFormat SimpleDateFormat(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public void start(Component c) {
		start(c, -1, -1);
	}

	public void start(Component c, int xC, int yC) {
		if (System.currentTimeMillis() - lastTime < 400)
			return;
		ACTIVE = true;
		lastTime = 0;
		if (c != null) {
			if (xC == -1 && yC == -1) {
				Component p = c.getParent();
				int x = c.getX(), y = c.getY() + c.getHeight() + 7;
				while (p != null) {
					x += p.getX();
					y += p.getY();
					p = p.getParent();
				}
				screen.setLocation(x, y);
			} else {
				screen.setLocation(xC, yC);
			}
		} else {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			screen.setLocation((int) (dim.getWidth() - screen.getWidth()) / 2,
					(int) (dim.getHeight() - screen.getHeight()) / 2);
		}
		SwingUtilities.invokeLater(this);
	}

	public void exit() {
		lastTime = System.currentTimeMillis();
		ACTIVE = false;
		screen.dispose();
		screen.setVisible(false);
	}

	public void run() {
		screen.pack();
		screen.setVisible(true);

	}

	public Date parseDate(String date) {
		if (date.length() == 0)
			return null;
		if (sdf == null)
			sdf = ui.getDateFromat();
		try {
			return sdf.parse(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String formatDate(Date date) {
		if (date == null)
			return "";
		if (sdf == null)
			sdf = ui.getDateFromat();
		return sdf.format(date);
	}

	public String formatDate(Date date, String pattern) {
		if (date == null)
			return "";
		return new SimpleDateFormat(pattern).format(date);
	}

	public String formatDate(Calendar date) {
		if (date == null)
			return "";
		return formatDate(date.getTime());
	}

	public String formatDate(Calendar date, String pattern) {
		if (date == null)
			return "";
		return new SimpleDateFormat(pattern).format(date.getTime());
	}

	public void setLocale(Locale l) {
		this.locale = l;
	}

	public Locale getLocale() {
		return this.locale == null ? Locale.US : locale;
	}

	private Calendar getCalendar() {
		return calendar;
	}

	private void setCalendar(Calendar c) {
		this.calendar = c;
	}

	public void setSelectedDate(Date d) {
		if (d != null) {
			if (selectedDate == null)
				selectedDate = new GregorianCalendar();
			this.selectedDate.setTime(d);
			updateScreen(selectedDate);
		}
	}

	protected void updateScreen(Calendar c) {
		if (navPanel == null)
			navPanel = new NavigatePanel(this);
		// navPanel.setLabel(c);
		navPanel.setCurrentMonth(c);
		setMonthPanel(c);
		screen.pack();
	}

	protected void setMonthPanel(Calendar calendar) {
		if (calendar != null)
			this.calendar.setTime(calendar.getTime());
		if (monthPanel != null)
			screen.getContentPane().remove(monthPanel);
		monthPanel = new MonthPanel(this, calendar);
		screen.getContentPane().add(monthPanel, BorderLayout.CENTER);
	}

	protected void dayPicked(int day) {
		// this.day = day;
		calendar.set(Calendar.DAY_OF_MONTH, day);
		setSelectedDate(calendar.getTime());
		String year = selectedDate.get(Calendar.YEAR) + "";
		String days = selectedDate.get(Calendar.DATE) + "";
		String month = (selectedDate.get(Calendar.MONTH) + 1) + "";// first is
		if (days.length() == 1)
			days = "0" + days;
		if (month.length() == 1)
			month = "0" + month;

		DateFormat dfm = new SimpleDateFormat("dd/MM/yyyy");
		String myDate = days + "/" + month + "/" + year;

		try {
			Date date = dfm.parse(myDate);
			dfm = ui.getDateFromat();
			textField.setText(dfm.format(date));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (closeOnSelect) {
			screen.dispose();
			screen.setVisible(false);
		}
		ACTIVE = false;
		lastTime = System.currentTimeMillis();
	}

	private Properties i18n;

	public String getString(String key, String dv) {
		if (i18n == null) {
			i18n = ui.getMessages();
		}
		String val = i18n.getProperty(key);
		if (val == null)
			return dv;
		else
			return val;
	}

	public boolean isCloseOnSelect() {
		return closeOnSelect;
	}

	public void windowGainedFocus(WindowEvent e) {
	}

	public void windowLostFocus(WindowEvent e) {
		screen.toFront();
	}

	public JDialog getScreen() {
		return this.screen;
	}

	public void setCloseOnSelect(boolean closeOnSelect) {
		this.closeOnSelect = closeOnSelect;
	}

	public static void main(String[] argv) {
		FinaCalendar dp = new FinaCalendar(null);

		dp.start(null);
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		exit();

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}
