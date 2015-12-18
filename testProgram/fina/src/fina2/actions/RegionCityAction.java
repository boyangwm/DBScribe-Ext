package fina2.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import fina2.Main;
import fina2.bank.RegionCityDialog;
import fina2.regions.RegionStructureFrame;
import fina2.returns.ImportManagerFrame;

@SuppressWarnings("serial")
public class RegionCityAction extends AbstractAction implements FinaAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private RegionStructureFrame regionsFrame;

	public RegionCityAction() {
		super();
		fina2.Main.main.setLoadingMessage(ui
				.getString("fina2.regions.regionCityManagement"));
		putValue(AbstractAction.NAME, "Region/City Management");

		regionsFrame = new RegionStructureFrame();
		ui.loadIcon("fina2.region", "region.png");
		regionsFrame.setFrameIcon(ui.getIcon("fina2.region"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.region"));
		ui.addAction("fina2.actions.regionCity", this);
	}

	public void actionPerformed(ActionEvent event) {
		try {
			boolean contains = false;
			javax.swing.JInternalFrame[] frames = main.getMainFrame()
					.getDesktop().getAllFrames();
			for (int i = 0; i < frames.length; i++) {
				if (frames[i].equals(regionsFrame))
					contains = true;
			}
			if (!contains)
				main.getMainFrame().getDesktop().add(regionsFrame);
			frames = null;

			regionsFrame.setIcon(false);
			regionsFrame.setSelected(true);
			regionsFrame.setClosable(true);
			regionsFrame.show();
			regionsFrame.setVisible(true);
			regionsFrame.requestFocus();

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public String[] getPermissions() {
		return new String[] { "fina2.regCity.amend" };
	}

}
