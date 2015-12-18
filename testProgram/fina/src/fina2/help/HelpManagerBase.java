package fina2.help;

import javax.swing.JButton;

public interface HelpManagerBase {

	public void createDisplayHelpFromSource(JButton helpButton, String helpId);

	public void createDisplayHelpFromFocus(JButton button, String helpId);

	public void createDisplayHelpAfterTracking(JButton button, String helpId);

	public boolean IsHelpSystem();
}
