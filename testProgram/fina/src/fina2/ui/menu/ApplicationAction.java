/*
 * ApplicationAction.java
 *
 * Created on 27 јпрель 2002 г., 12:23
 */

package fina2.ui.menu;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import fina2.actions.ExitAction;

@SuppressWarnings("serial")
public class ApplicationAction extends javax.swing.AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private Logger log = Logger.getLogger(ExitAction.class);

	private String app;

	/** Creates new ApplicationAction */
	public ApplicationAction(String app) {
		this.app = app;
		ui.loadIcon("fina2.other.menu", "fina2-other-menu.png");
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.other.menu"));
	}

	@SuppressWarnings("deprecation")
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			Runtime.getRuntime().exec(app);
		} catch (Exception e) {
			log.log(Priority.ERROR, e.getMessage(), e);
			e.printStackTrace();
		}
	}

}
