/*
 * BankGroupsAction.java
 *
 * Created on October 19, 2001, 8:49 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.bank.BankGroupsFrame;

/**
 * 
 * @author Administrator
 * @version
 */
public class BankGroupsAction extends AbstractAction implements FinaAction {

	/** Creates new BankTypesAction */
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private BankGroupsFrame frame;

	/** Creates new LanguagesAction */
	public BankGroupsAction() {
		super();

		putValue(AbstractAction.NAME, ui.getString("fina2.bank.bankGroupsAction"));

		frame = new BankGroupsFrame();
		ui.loadIcon("fina2.bank.bankGroups", "bank_groups.gif");
		frame.setFrameIcon(ui.getIcon("fina2.bank.bankGroups"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.bank.bankGroups"));

		ui.addAction("fina2.actions.bankGroups", this);
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
			frame.setSelected(true);
			frame.show();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		frame.requestFocus();
	}

	public String[] getPermissions() {
		return new String[] { "fina2.bank.amend", "fina2.bank.delete", "fina2.bank.review" };
	}

}
