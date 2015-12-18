package fina2.actions;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import fina2.about.AboutVersionsFrame;

@SuppressWarnings("serial")
public class AboutVersionAction extends AbstractAction implements FinaAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private AboutVersionsFrame frame;

	public AboutVersionAction() {
		super();
		boolean isValid = validJavaArchType();
		ui.putConfigValue("arch.data.model.isValid", isValid);
		fina2.Main.main.setLoadingMessage(ui.getString("fina2.AboutVersionFrame.loading"));
		putValue(AbstractAction.NAME, ui.getString("fina2.AboutVersionFrame"));

		frame = new AboutVersionsFrame();
		ui.loadIcon("fina2.about", "help.gif");
		frame.setFrameIcon(ui.getIcon("fina2.about"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.about"));

		ui.addAction("fina2.about.AboutAction", this);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			boolean contains = false;
			javax.swing.JInternalFrame[] frames = main.getMainFrame().getDesktop().getAllFrames();
			for (int i = 0; i < frames.length; i++) {
				if (frames[i].equals(frame))
					contains = true;
			}
			if (!contains)
				main.getMainFrame().getDesktop().add(frame);
			frames = null;

			frame.setIcon(false);
			frame.setResizable(false);
			frame.setClosable(true);
			frame.setSelected(true);
			frame.show();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		frame.requestFocus();
	}

	public String[] getPermissions() {
		return new String[] { "" };
	}

	private boolean validJavaArchType() {
		String archModel = System.getProperty("sun.arch.data.model");
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("win") > -1) {
			if (!archModel.equals("32")) {
				JOptionPane.showMessageDialog(main.getMainFrame(), "You dont have installed java x86 therefor openoffice interfaces wont work ", "Warning", JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}
		return true;
	}
}
