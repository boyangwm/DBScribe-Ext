package fina2.returns;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.FinaTypeException.Type;
import fina2.security.SecurityItem;
import fina2.security.TreeSecurityItem;
import fina2.security.UserPK;
import fina2.security.users.AbstractTreeView;
import fina2.security.users.TreeViewModel;
import fina2.servergate.SecurityGate;
import fina2.ui.AbstractDialog;
import fina2.ui.UIManager;
import fina2.util.search.SearchWord;
import fina2.util.search.TreeSearcher;
import fina2.util.search.TreeSearcherBase;

/**
 * Dialog to select FI in Return Manager.
 */
public class FISelectionDialog extends AbstractDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	/** Bank list view */
	private FISelectionView bankSelectionView = null;

	public String selectedFIKey;

	private String buttonTextKey;

	private JLabel fiSearchLabel;
	private JTextField fiSearchTextField;

	private TreeSearcherBase treeSearcher = new TreeSearcher();

	/**
	 * Creates the instance of the dialog with a given mode
	 * 
	 * @throws Exception
	 *             if there are errors while loading data
	 */
	public FISelectionDialog(JFrame owner, UserPK userPK, String selectedFIKey,
			String buttonTextKey) throws Exception {
		super(owner, true);
		this.selectedFIKey = selectedFIKey;
		this.buttonTextKey = buttonTextKey;

		// These can throw exceptions
		checkArguments(userPK);
		initComponents(userPK);
	}

	/**
	 * Throws IllegalArgumentException if actionMode is AMEND or REVIEW, and
	 * userPK is null.
	 */
	private void checkArguments(UserPK userPK) throws IllegalArgumentException {
		if (userPK == null) {
			String error = "UserPK must be specified";
			throw new IllegalArgumentException(error);
		}
	}

	/** Inits the components */
	private void initComponents(UserPK userPK) throws Exception {

		setSize(550, 550);
		setTitle(Main.getString("fina2.security.fi"));

		initBankSelectionView(userPK);
	}

	/** Inits the bank selection view */
	private void initBankSelectionView(UserPK userPK) throws Exception {

		bankSelectionView = new FISelectionView(userPK, selectedFIKey,
				buttonTextKey);
		super.getMainPane().add(bankSelectionView);

		JPanel northPanel = new JPanel();
		fiSearchLabel = new JLabel("    " + ui.getString("fina2.web.search"));
		fiSearchLabel.setFont(ui.getFont());
		northPanel.add(fiSearchLabel);

		fiSearchTextField = new JTextField(20);
		fiSearchTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				searchEvent(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		northPanel.add(fiSearchTextField);
		super.getSouthPanel().add(northPanel, BorderLayout.WEST);
	}

	/* Search key Listener */
	private void searchEvent(KeyEvent e) {
		searchAndSelectNode(bankSelectionView.getTreeTable().getTree(),
				fiSearchTextField.getText());
	}

	private void searchAndSelectNode(JTree tree, String text) {
		TreeViewModel model = (TreeViewModel) tree.getModel();
		Object rootObject = model.getRoot();

		ArrayList<Object> list = new ArrayList<Object>();

		for (int i = 1; i < SearchWord.MAX_SEARCH_LEVEL; i++) {
			try {
				iter(model, rootObject, list, i);
			} catch (NodeFoundException ex) {
				break;
			}
			list.clear();
		}

		if (!list.contains(rootObject))
			list.add(0, rootObject);

		TreePath selPath = new TreePath(list.toArray());
		tree.scrollPathToVisible(selPath);
		tree.setSelectionPath(selPath);
		bankSelectionView.getTreeTable().scrollRectToVisible(
				tree.getPathBounds(selPath));

		model.updateBranchNodesSelection();
	}

	@SuppressWarnings("serial")
	private class NodeFoundException extends Exception {
	}

	private void iter(TreeViewModel model, Object node, ArrayList<Object> list,
			int searchLevel) throws NodeFoundException {
		int count = model.getChildCount(node);

		list.add(node);

		for (int i = 0; i < count; i++) {

			Object ch = model.getChild(node, i);

			boolean searchResult = SearchWord.searchWord(ch.toString()
					.toLowerCase(), fiSearchTextField.getText().toLowerCase(),
					searchLevel);

			if (searchResult) {
				list.add(ch);
				throw new NodeFoundException();
			}
			iter(model, ch, list, searchLevel);
		}
		list.remove(node);
	}

	/** Handles the OK button */
	protected void okButtonPressed() {
		try {
			// Throws exception if checking is failed
			bankSelectionView.check();

			// Throws exception if saving is failed
			bankSelectionView.save();

			// The data checked and saved successfully
			dispose();

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}
}

/**
 * View to display only user FI in FISelectionDialog.
 * 
 * @author Askhat Asanaliev
 */
class FISelectionView extends AbstractTreeView<UserPK> {

	/** Banks data map */
	private Map<Integer, TreeSecurityItem> fiMap = null;

	/** Tree model */
	private TreeViewModel treeModel = null;

	/** Parameter key where selected FI will be stored **/
	private String selectedFIKey;

	/** Parameter key where button text will be stored **/
	private String buttonTextKey;

	private static boolean fiSelected = true;

	/** Creates an instance of the class */
	FISelectionView(UserPK key, String selectedFIKey, String buttonTextKey)
			throws Exception {
		super(ModeType.REVIEW, key);

		this.selectedFIKey = selectedFIKey;
		this.buttonTextKey = buttonTextKey;
		this.fiMap = loadBanksMap();
		initTree(fiMap);
		loadSelectedBanks();
	}

	/** Loads the banks map */
	private Map<Integer, TreeSecurityItem> loadBanksMap() throws Exception {
		UserPK userPK = super.getKey();
		return SecurityGate.getUserBanksOnly(userPK);
	}

	/** Inits the FI tree */
	private void initTree(Map<Integer, TreeSecurityItem> modelData) {

		String textColumn = Main.getString("fina2.security.fi");
		Icon branchIcon = Main.getIcon("folder.gif");
		Icon leafIcon = Main.getIcon("banks.gif");

		// Data model for tree
		treeModel = new TreeViewModel(modelData, textColumn, false);
		super.initTree(treeModel, branchIcon, leafIcon);
	}

	/** Returns a set of selected FI */
	private Set<Integer> getSelectedFI() {

		// The result set
		Set<Integer> fiSet = new HashSet<Integer>();

		// All items in the FI map
		Collection<TreeSecurityItem> items = fiMap.values();

		// Looping and retrieving selected FI
		for (TreeSecurityItem item : items) {
			if (item.isLeaf() && (item.getReview() == SecurityItem.Status.YES)) {
				// This is a selected FI
				fiSet.add(item.getId());
			}
		}

		// The result set. Contains only the selected banks.
		return fiSet;
	}

	/** Checks validity of FI selection */
	public void check() throws FinaTypeException {
		Set<Integer> selectedFI = getSelectedFI();
		fiSelected = true;
		if (selectedFI.size() == 0) {
			// No FI is selected
			// throw new FinaTypeException(Type.RETURNS_NO_FI_SELECTED);
			fiSelected = false;
		}
	}

	/** Saves FI selection info to the configuration file */
	public void save() {
		saveSelectedFI();
		defineFISelectionButtonText();
	}

	/** Defines the text of fiSelectionButton in Return Manager */
	private void defineFISelectionButtonText() {

		String text = getFISelectionText();

		UIManager ui = fina2.Main.main.ui;
		ui.putConfigValue(buttonTextKey, text);
	}

	/** Returns the selection text according to the FI selection in the tree */
	private String getFISelectionText() {

		String result = "";

		// Contains only selected items in the tree
		Set<Integer> selectedFI = getSelectedFI();

		if (selectedFI.size() == 1) {
			// Only one FI is selected. The result is its code.
			Integer firstId = selectedFI.iterator().next();
			TreeSecurityItem item = fiMap.get(firstId);

			// FI code is stored as "code" property.
			// See: UserSessionBean.getUserBanksOnly
			result = (String) item.getProperty("code");

		} else if (selectedFI.size() > 1) {
			// Several FI are selected

			if (oneTypeFI(selectedFI)) {
				// The FIs of one type are selected. Returning their type.
				Integer firstId = selectedFI.iterator().next();
				TreeSecurityItem item = fiMap.get(firstId);

				int parentId = item.getParentId();
				TreeSecurityItem parentItem = fiMap.get(parentId);

				// FI code is stored as "code" property.
				// See: UserSessionBean.getUserBanksOnly
				result = (String) parentItem.getProperty("code");

			} else {
				// Selected FIs have different types. Returning "FI" value
				// from messages file.
				result = Main.getString("fina2.security.fi");
			}
		}

		return result;
	}

	/** Returns true if selectedFI items have the same type */
	private boolean oneTypeFI(Set<Integer> selectedFI) {

		int typeCount = 0;
		int previousType = 0;

		for (int id : selectedFI) {
			TreeSecurityItem item = fiMap.get(id);

			if (item.getParentId() != previousType) {
				// The current item type differs from the previous
				typeCount++;
				previousType = item.getParentId();
			}
		}

		// Result
		return (typeCount == 1);
	}

	/** Saves the selected FI to the conf file */
	private void saveSelectedFI() {
		Set<Integer> selectedFI = getSelectedFI();

		UIManager ui = fina2.Main.main.ui;
		ui.putConfigValue(selectedFIKey, selectedFI);
	}

	/** Loads the selected FI from the config file */
	private void loadSelectedBanks() {

		// Loading from the config file
		fina2.ui.UIManager ui = fina2.Main.main.ui;
		Set<Integer> selectedFI = (Set<Integer>) ui
				.getConfigValue(selectedFIKey);

		if (selectedFI == null) {
			// There is no such config value
			return;
		}

		// Selecting FI in the tree

		Collection<TreeSecurityItem> items = fiMap.values();

		for (TreeSecurityItem item : items) {

			int fiId = item.getId();
			if (selectedFI.contains(fiId)) {
				// This FI was selected
				item.setReview(true);
			}
		}

		// Updating selection state of the branch nodes
		treeModel.updateBranchNodesSelection();
	}

	public static boolean getFiSelected() {
		return fiSelected;
	}
}
