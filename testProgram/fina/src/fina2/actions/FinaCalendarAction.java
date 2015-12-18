

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.security.users.UsersFrame;

/**
 *
 * @author  Davit Beradze
 * @version
 */
public class FinaCalendarAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private UsersFrame frame;

    /** Creates new UsersAction */
    public FinaCalendarAction() {
        ui.loadIcon("fina2.finacalendar.nextmon", "nextmon.gif");
        ui.loadIcon("fina2.finacalendar.nextyear", "nextyear.gif");
        ui.loadIcon("fina2.finacalendar.premon", "premon.gif");
        ui.loadIcon("fina2.finacalendar.preyear", "preyear.gif");
        
      
        
        
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
     
    }

    public String[] getPermissions() {
		return null;
    }

}
