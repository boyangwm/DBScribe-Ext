package fina2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import fina2.ui.UIManager;

public class BaseFrame extends JInternalFrame {

	private static UIManager ui = Main.main.ui;
	private static Main main = Main.main;
	protected JButton closeButton;
	protected JButton refreshButton;
	protected JButton helpButton;
	protected JButton printButton;

	public void initBaseComponents() {
		this.setFont(ui.getFont());

		closeButton = new JButton();
		helpButton = new JButton();
		refreshButton = new JButton();
		printButton = new JButton();

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				helpButtonActionPerformed(evt);
			}
		});

		printButton.setIcon(ui.getIcon("fina2.print"));
		printButton.setFont(ui.getFont());
		printButton.setText(ui.getString("fina2.print"));
		printButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				printButtonActionPerformed(evt);
			}
		});

		refreshButton.setIcon(ui.getIcon("fina2.refresh"));
		refreshButton.setFont(ui.getFont());
		refreshButton.setText(ui.getString("fina2.refresh"));
		refreshButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				refreshButtonActionPerformed(evt);
			}
		});
		closeButton.setIcon(ui.getIcon("fina2.close"));
		closeButton.setFont(ui.getFont());
		closeButton.setText(ui.getString("fina2.close"));
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				closeButtonActionPerformed(evt);
			}
		});

		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosed(InternalFrameEvent e) {
				frameClosed(e);
			}
		});
		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				formComponentResized(evt);
			}

			public void componentMoved(java.awt.event.ComponentEvent evt) {
				formComponentMoved(evt);
			}

			public void componentShown(java.awt.event.ComponentEvent evt) {
				formComponentShown(evt);
			}

			public void componentHidden(java.awt.event.ComponentEvent evt) {
				formComponentHidden(evt);
			}
		});
	}

	protected void formComponentResized(ComponentEvent event) {
		ui.putConfigValue(getClassName() + ".width", new Integer(getWidth()));
		ui.putConfigValue(getClassName() + ".height", new Integer(getHeight()));
	}

	protected void formComponentMoved(ComponentEvent event) {
		ui.putConfigValue(getClassName() + ".x", new Integer(getX()));
		ui.putConfigValue(getClassName() + ".y", new Integer(getY()));
	}

	protected void formComponentShown(ComponentEvent event) {
		ui.putConfigValue(getClassName() + ".visible", new Boolean(true));
	}

	protected void formComponentHidden(ComponentEvent event) {
		ui.putConfigValue(getClassName() + ".visible", new Boolean(false));
	}

	protected void frameClosed(InternalFrameEvent e) {
		ui.putConfigValue(getClassName() + ".visible", new Boolean(false));
	}

	private String getClassName() {
		return this.getClass().getName();
	}

	protected void refreshButtonActionPerformed(ActionEvent event) {
	}

	protected void closeButtonActionPerformed(ActionEvent event) {
	}

	protected void helpButtonActionPerformed(ActionEvent event) {
	}

	protected void printButtonActionPerformed(ActionEvent event) {
	}

	public static void ensureVisible(java.awt.Container frame) {
		int x = 0;
		int y = 0;
		int w = 0;
		int h = 0;
		boolean v = false;
		String key = frame.getClass().getName();
		try {
			x = ((Integer) ui.getConfigValue(key + ".x")).intValue();
			y = ((Integer) ui.getConfigValue(key + ".y")).intValue();
			w = ((Integer) ui.getConfigValue(key + ".width")).intValue();
			h = ((Integer) ui.getConfigValue(key + ".height")).intValue();
			if (!key.equals("fina2.MainFrame")) {
				v = ((Boolean) ui.getConfigValue(key + ".visible"))
						.booleanValue();
				if (v)
					main.addToShow(frame);
			}
		} catch (Exception e) {
		}
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (w < 300)
			w = 300;
		if (h < 300)
			h = 300;

		if (setMaxSize(key)) {
			Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			x = 0;
			y = 0;
			w = d.width;
			h = d.height - 40;
		}
		if (frame instanceof JFrame) {
			((JFrame) frame).setResizable(true);
		}

		if (frame instanceof JInternalFrame) {
			((JInternalFrame) frame).setMaximizable(true);
			((JInternalFrame) frame).setIconifiable(true);
			((JInternalFrame) frame).setResizable(true);
			((JInternalFrame) frame).setClosable(true);
		}
		frame.setLocation(x, y);
		frame.setSize(w, h);
	}

	public static boolean setMaxSize(String className) {
		boolean result = false;
		String[] classNames = new String[] {
				"fina2.returns.ReturnAmendReviewFrame",
				"fina2.reportoo.ReportGenerator",
				"fina2.returns.ReturnViewFormatFrame" };
		for (int i = 0; i < classNames.length; i++) {
			if (classNames[i].equals(className)) {
				result = true;
			}
		}
		return result;

	}
}
