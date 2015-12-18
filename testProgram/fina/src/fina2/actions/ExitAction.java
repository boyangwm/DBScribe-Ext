/*
 * ExitAction.java
 *
 * Created on October 15, 2001, 6:05 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import fina2.ui.sheet.SpreadsheetsManager;

public class ExitAction extends AbstractAction {

    private Logger log = Logger.getLogger(ExitAction.class);

    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    private boolean performed = false;

    public ExitAction() {
        super();

        ui.loadIcon("fina2.exit", "exit.gif");
        putValue(AbstractAction.NAME, ui.getString("fina2.exit"));
        putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.exit"));

        ui.addAction("fina2.actions.exit", this);
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

        if (performed) {
            return;
        } else {
            performed = true;
        }

        // Save configuration
        try {
            ui.saveConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Dispose spreadsheets
        log.debug("Disposing sheets before closing application...");
        SpreadsheetsManager.getInstance().disposeSheets();

        System.exit(0);
    }
}
