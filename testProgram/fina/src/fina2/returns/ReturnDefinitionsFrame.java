package fina2.returns;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;

import org.tigris.gef.base.LayerManager;
import org.tigris.gef.base.LayerPerspective;
import org.tigris.gef.base.SelectionManager;
import org.tigris.gef.event.GraphSelectionEvent;
import org.tigris.gef.event.GraphSelectionListener;
import org.tigris.gef.graph.presentation.DefaultGraphModel;
import org.tigris.gef.ui.ToolBar;

import fina2.BaseFrame;
import fina2.Main;
import fina2.ui.UIManager;
import fina2.ui.diagram.FigGraphNode;
import fina2.ui.diagram.GraphNode;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRowImpl;

public class ReturnDefinitionsFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;

	private ReturnDefinitionsAmendAction amendAction;
	private ReturnDefinitionsInsertAction insertAction;
	private ReturnDefinitionsReviewAction reviewAction;
	private ReturnDefinitionsDeleteAction deleteAction;
	private ReturnDefinitionsFormatAction formatAction;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;
	private boolean canFormat = false;

	private Thread t;
	private DiagramProgress progress;

	private Hashtable h = new Hashtable();

	public ReturnDefinitionsFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.format", "format_cells.gif");

		table = new EJBTable();

		amendAction = new ReturnDefinitionsAmendAction(main.getMainFrame(), table);
		insertAction = new ReturnDefinitionsInsertAction(main.getMainFrame(), table);
		reviewAction = new ReturnDefinitionsReviewAction(main.getMainFrame(), table);
		deleteAction = new ReturnDefinitionsDeleteAction(main.getMainFrame(), table);
		formatAction = new ReturnDefinitionsFormatAction(main.getMainFrame(), table);

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					if (amendAction.isEnabled()) {
						amendAction.actionPerformed(null);
					} else {
						if (reviewAction.isEnabled()) {
							reviewAction.actionPerformed(null);
						}
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (table.getSelectedRow() == -1) {
					insertAction.setEnabled(canAmend);
					amendAction.setEnabled(false);
					reviewAction.setEnabled(false);
					deleteAction.setEnabled(false);
					formatAction.setEnabled(false);
					dependenciesButton.setEnabled(false);
				} else {
					insertAction.setEnabled(canAmend);
					amendAction.setEnabled(canAmend);
					deleteAction.setEnabled(canDelete);
					reviewAction.setEnabled(canReview || canAmend);
					formatAction.setEnabled(canFormat);
					dependenciesButton.setEnabled(canReview || canAmend);
				}
			}
		});

		initComponents();

		table.setPopupMenu(popupMenu);

		scrollPane.setViewportView(table);
		BaseFrame.ensureVisible(this);
	}

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));
			colNames.add(ui.getString("fina2.type"));

			table.initTable(colNames, session.getReturnDefinitionsRows(main.getUserHandle(), main.getLanguageHandle()));

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void show() {
		if (isVisible())
			return;

		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.returns.definition.amend");
			canDelete = user.hasPermission("fina2.returns.definition.delete");
			canReview = user.hasPermission("fina2.returns.definition.review");
			canFormat = user.hasPermission("fina2.returns.definition.format");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		amendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		amendMenuItem.setVisible(canAmend);

		insertAction.setEnabled(canAmend);
		createButton.setVisible(canAmend);
		createMenuItem.setVisible(canAmend);

		deleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);
		deleteMenuItem.setVisible(canDelete);

		formatAction.setEnabled(canFormat);
		formatButton.setVisible(canFormat);
		formatMenuItem.setVisible(canFormat);

		reviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		reviewMenuItem.setVisible(canAmend || canReview);
		dependenciesButton.setEnabled(canAmend || canReview);
		dependenciesButton.setVisible(canAmend || canReview);

		initTable();

		super.show();

	}

	private void initComponents() {// GEN-BEGIN:initComponents

		popupMenu = new javax.swing.JPopupMenu();
		createMenuItem = new javax.swing.JMenuItem();
		amendMenuItem = new javax.swing.JMenuItem();
		reviewMenuItem = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		formatMenuItem = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		deleteMenuItem = new javax.swing.JMenuItem();
		jPanel3 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		dependenciesButton = new javax.swing.JButton();
		formatButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();

		setTitle(ui.getString("fina2.returns.returnDefinitionsAction"));
		initBaseComponents();

		createMenuItem.setFont(ui.getFont());
		createMenuItem.setText("Item");
		createMenuItem.setAction(insertAction);
		popupMenu.add(createMenuItem);

		amendMenuItem.setFont(ui.getFont());
		amendMenuItem.setText("Item");
		amendMenuItem.setAction(amendAction);
		popupMenu.add(amendMenuItem);

		reviewMenuItem.setFont(ui.getFont());
		reviewMenuItem.setText("Item");
		reviewMenuItem.setAction(reviewAction);
		popupMenu.add(reviewMenuItem);

		popupMenu.add(jSeparator1);

		formatMenuItem.setFont(ui.getFont());
		formatMenuItem.setAction(formatAction);
		popupMenu.add(formatMenuItem);

		popupMenu.add(jSeparator2);

		deleteMenuItem.setFont(ui.getFont());
		deleteMenuItem.setText("Item");
		deleteMenuItem.setAction(deleteAction);
		popupMenu.add(deleteMenuItem);

		setFont(ui.getFont());

		jPanel3.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Return_Definition");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel7.add(helpButton);

		jPanel3.add(jPanel7, java.awt.BorderLayout.WEST);

		jPanel8.add(printButton);

		jPanel8.add(refreshButton);

		jPanel8.add(closeButton);

		jPanel3.add(jPanel8, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 5)));
		jPanel6.setLayout(new java.awt.GridBagLayout());

		createButton.setFont(ui.getFont());
		createButton.setAction(insertAction);
		createButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel6.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendAction);
		amendButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel6.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		reviewButton.setFont(ui.getFont());
		reviewButton.setAction(reviewAction);
		reviewButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel6.add(reviewButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(deleteAction);
		deleteButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel6.add(deleteButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		dependenciesButton.setFont(ui.getFont());
		dependenciesButton.setText(ui.getString("fina2.metadata.dependencies"));
		dependenciesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dependenciesButtonActionPerformed(evt);
			}
		});
		jPanel6.add(dependenciesButton, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 0, 0, 0)));

		formatButton.setFont(ui.getFont());
		formatButton.setAction(formatAction);
		formatButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		formatButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// TODO
			}
		});
		jPanel6.add(formatButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 0, 0, 0)));

		jPanel5.add(jPanel6, java.awt.BorderLayout.NORTH);

		jPanel4.add(jPanel5, java.awt.BorderLayout.EAST);

		scrollPane.setFont(ui.getFont());
		jPanel4.add(scrollPane, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

	}// GEN-END:initComponents

	private void calcDependence1() {
		// t = new Thread() {
		// public void run() {

		// private int parentsLevel = 0;
		// private int childsLevel = 0;
		// private Hashtable visibleNodes = new Hashtable();
		// private Hashtable nodeLevels = new Hashtable();

		final Hashtable parents = new Hashtable();

		Hashtable childs = new Hashtable();

		// private DefaultGraphModel dgm = null;
		// private LayerPerspective lay = null;

		// parentsLevel = 0;
		// childsLevel = 0;
		// parents = new Hashtable();
		h = new Hashtable();

		// final Hashtable parents = new Hashtable();
		// final Hashtable h = new Hashtable();
		final org.tigris.gef.graph.presentation.JGraphFrame frame = new org.tigris.gef.graph.presentation.JGraphFrame("Graph");
		DefaultGraphModel dgm = (DefaultGraphModel) frame.getGraphModel();
		LayerManager lm = frame.getGraph().getEditor().getLayerManager();
		LayerPerspective lay = (LayerPerspective) lm.getActiveLayer();

		frame.setToolBar(new ToolBar());
		frame.setJMenuBar(new JMenuBar());

		java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		frame.setSize(screen.width / 2 + 200, screen.height / 2 + 200);
		frame.setLocation(screen.width / 2 - screen.width / 4 - 100, screen.height / 2 - screen.height / 4 - 100);

		Vector v = new Vector();
		int i = 0;
		int yy = 50;
		int xx = 50;
		int level = 1;

		final Collection rows = table.getRows();

		for (Iterator iter = rows.iterator(); iter.hasNext();) {

			GraphNode sn = new GraphNode();
			sn.initialize(null);
			TableRowImpl row = (TableRowImpl) iter.next();
			sn.setTitle(row.getValue(0));
			sn.setPK(row.getPrimaryKey());
			sn.setLevel(level);
			dgm.addNode(sn);
			sn.setLocation(yy, xx);

			yy = yy + 100;
			if (yy == 750) {
				yy = 50;
				xx = xx + 50;
				level++;
			}

			v.add(sn);
			h.put(row.getPrimaryKey(), sn);

		}
		final SelectionManager sm = new SelectionManager(frame.getGraph().getEditor());

		frame.getGraph().getEditor().addGraphSelectionListener(new GraphSelectionListener() {
			public void selectionChanged(GraphSelectionEvent ev) {

				for (Iterator it = ev.getSelections().iterator(); it.hasNext();) {
					Object o = it.next();
					FigGraphNode f = new FigGraphNode();
					if (o.getClass().equals(f.getClass())) {
						GraphNode nn = (GraphNode) ((FigGraphNode) o).getOwner();
						Vector ch = (Vector) parents.get(nn.getPK());
						for (Iterator iter = rows.iterator(); iter.hasNext();) {
							TableRowImpl roww = (TableRowImpl) iter.next();
							ReturnDefinitionPK pkk = (ReturnDefinitionPK) roww.getPrimaryKey();
							((GraphNode) h.get(pkk)).unfill();
						}
						nn.fill(Color.getHSBColor(10, 10, 50));
						if (ch != null && ch.size() > 0) {
							for (Iterator it1 = ch.iterator(); it1.hasNext();) {
								((GraphNode) h.get(it1.next())).fill(Color.getHSBColor(10, 10, 45));
							}
						}
						LayerManager lm = frame.getGraph().getEditor().getLayerManager();
						LayerPerspective lay = (LayerPerspective) lm.getActiveLayer();
						lay.refreshEditors();
					}
				}
			}
		});

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			i = 1;

			for (Iterator iter = rows.iterator(); iter.hasNext(); i++) {

				TableRowImpl row = (TableRowImpl) iter.next();
				ReturnDefinitionPK pk = (ReturnDefinitionPK) row.getPrimaryKey();

				((GraphNode) h.get(pk)).fill(Color.lightGray);
				lay.refreshEditors();
				Hashtable vv = session.getReturnDependecies(pk);
				Collection vvv = vv.values();
				progress.setProgress(i);

				Vector childes = new Vector();
				for (Iterator iterr = vvv.iterator(); iterr.hasNext();) {
					ReturnDefinitionPK pkk = (ReturnDefinitionPK) iterr.next();

					if (!pkk.equals(pk)) { // 1 //0
						dgm.connect(((GraphNode) h.get(pkk)).getPort(1), ((GraphNode) h.get(pk)).getPort(0));
						childes.add(pkk);
					}
				}
				parents.put(pk, childes);
				((GraphNode) h.get(pk)).unfill();
				lay.refreshEditors();
			}

			progress.dispose();
			frame.show();
			for (Iterator iter = rows.iterator(); iter.hasNext(); i++) {
				TableRowImpl row = (TableRowImpl) iter.next();
				ReturnDefinitionPK pk = (ReturnDefinitionPK) row.getPrimaryKey();
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		// }
		// };
		// t.start();
	}

	private void dependenciesButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_dependenciesButtonActionPerformed

		progress = new DiagramProgress(null, true);
		final Collection rows = table.getRows();
		Thread tt = new Thread() {
			public void run() {
				progress.setProgress(1);
				calcDependence2();
			}
		};
		progress.show(tt, 1, rows.size());

	}// GEN-LAST:event_dependenciesButtonActionPerformed

	private void calcDependence2() {
		// t = new Thread() {
		// public void run() {
		h = new Hashtable();
		org.tigris.gef.graph.presentation.JGraphFrame frame = new org.tigris.gef.graph.presentation.JGraphFrame("Graph");
		DefaultGraphModel dgm = (DefaultGraphModel) frame.getGraphModel();
		LayerManager lm = frame.getGraph().getEditor().getLayerManager();
		LayerPerspective lay = (LayerPerspective) lm.getActiveLayer();

		frame.setToolBar(new ToolBar());
		// frame.setJMenuBar(new JMenuBar());
		JMenuBar menuBar = frame.getJMenuBar();

		menuBar.remove(1);
		menuBar.remove(1);
		menuBar.remove(1);

		JMenu menu = menuBar.getMenu(0);
		menu.setText(ui.getString("fina2.file"));
		menu.setFont(ui.getFont());

		menu.remove(0);
		menu.remove(0);
		menu.remove(0);
		menu.remove(0);
		menu.remove(1);
		menu.remove(1);
		menu.remove(1);

		JMenuItem menuItem = menu.getItem(0);
		menuItem.setText(ui.getString("fina2.print"));
		menuItem.setFont(ui.getFont());

		/*
		 * javax.swing.JMenuItem closeMenu = new javax.swing.JMenuItem();
		 * closeMenu.setFont(ui.getFont());
		 * closeMenu.setText(ui.getString("fina2.close"));
		 * closeMenu.setIcon(ui.getIcon("fina2.close"));
		 * closeMenu.addActionListener(new java.awt.event.ActionListener() {
		 * public void actionPerformed(java.awt.event.ActionEvent evt) {
		 * closeMenuActionPerformed(evt); } }); menu.add(new
		 * javax.swing.JSeparator()); menu.add(closeMenu);
		 */
		java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		frame.setSize(screen.width / 2 + 200, screen.height / 2 + 200);
		frame.setLocation(screen.width / 2 - screen.width / 4 - 100, screen.height / 2 - screen.height / 4 - 100);

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
			ReturnSession session = home.create();
			// final Collection rows =
			// session.getReturnDefinitionsRows(main.getUserHandle(),
			// main.getLanguageHandle());
			// table.sort(1);
			Collection rows = table.getRows();

			Vector v = new Vector();
			int i = 0;
			int yy = 50;
			int xx = 50;
			int level = 1;

			for (Iterator iter = rows.iterator(); iter.hasNext();) {

				GraphNode sn = new GraphNode();
				sn.initialize(null);
				TableRowImpl row = (TableRowImpl) iter.next();
				sn.setTitle(row.getValue(0));
				sn.setPK(row.getPrimaryKey());
				sn.setLevel(0);
				sn.setHasParents(false);

				h.put((ReturnDefinitionPK) row.getPrimaryKey(), sn);
			}
			SelectionManager sm = new SelectionManager(frame.getGraph().getEditor());

			i = 1;
			for (Iterator iter = rows.iterator(); iter.hasNext(); i++) {
				TableRowImpl row = (TableRowImpl) iter.next();
				ReturnDefinitionPK pk = (ReturnDefinitionPK) row.getPrimaryKey();

				Hashtable vv = session.getReturnDependecies(pk);
				Collection vvv = vv.values();
				progress.setProgress(i);
				for (Iterator iterPar = vvv.iterator(); iterPar.hasNext();) {
					ReturnDefinitionPK pkk = (ReturnDefinitionPK) iterPar.next();
					if (!pk.equals(pkk))
						((GraphNode) h.get((ReturnDefinitionPK) pk)).addParent(pkk);
				}
			}

			progress.dispose();
			frame.show();

			for (Iterator iter = rows.iterator(); iter.hasNext(); i++) {
				TableRowImpl row = (TableRowImpl) iter.next();
				ReturnDefinitionPK pk1 = (ReturnDefinitionPK) row.getPrimaryKey();
				getP(pk1, new ReturnDefinitionPK(0));
				getC(pk1, new ReturnDefinitionPK(0));
			}

			Collection hh = (Collection) h.values();
			int maxRow = 0;
			int maxLevel = 0;
			int minLevel = 0;
			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				if (((GraphNode) h.get((ReturnDefinitionPK) row.getPrimaryKey())).getLevel() > maxLevel && ((GraphNode) h.get((ReturnDefinitionPK) row.getPrimaryKey())).getLevel() != 999)
					maxLevel = ((GraphNode) h.get((ReturnDefinitionPK) row.getPrimaryKey())).getLevel();
				if (((GraphNode) h.get((ReturnDefinitionPK) row.getPrimaryKey())).getLevel() < minLevel)
					minLevel = ((GraphNode) h.get((ReturnDefinitionPK) row.getPrimaryKey())).getLevel();
			}

			for (Iterator iter = rows.iterator(); iter.hasNext(); i++) {
				TableRowImpl row = (TableRowImpl) iter.next();
				ReturnDefinitionPK pk1 = (ReturnDefinitionPK) row.getPrimaryKey();
				boolean hP = ((GraphNode) h.get((ReturnDefinitionPK) pk1)).getHasParents();
				// boolean hC = ((GraphNode)h.get( (ReturnDefinitionPK)pk1 )
				// ).getHasChilds();

				if (hP == false)
					((GraphNode) h.get((ReturnDefinitionPK) pk1)).setLevel(minLevel - 1);
			}
			minLevel--;

			for (i = maxLevel; i >= minLevel; i--) {
				int curRow = 0;
				for (Iterator iter = hh.iterator(); iter.hasNext();) {
					GraphNode sn = (GraphNode) iter.next();
					if (sn.getLevel() == i) {
						curRow++;
					}
				}
				if (maxRow < curRow)
					maxRow = curRow;
			}

			int nk = 0;
			for (i = maxLevel; i >= minLevel; i--) {
				int jj = 0;
				int curRow = 0;
				for (Iterator iter = hh.iterator(); iter.hasNext();) {

					GraphNode sn = (GraphNode) iter.next();
					if (sn.getLevel() == i) {
						curRow++;
						if (curRow > 10)
							curRow = 1;
					}
				}

				for (Iterator iter = hh.iterator(); iter.hasNext();) {
					GraphNode sn = (GraphNode) iter.next();

					if (sn.getLevel() == i) {
						dgm.addNode(sn);
						if (jj > 10) {
							jj = 1;
							nk++;
						}
						// sn.setLocation( (maxRow-curRow)/2*80+(jj++)*80,
						// (maxLevel - i+1+nk)*100);
						sn.setLocation((12 - curRow) / 2 * 80 + (jj++) * 80, (maxLevel - i + 1 + nk) * 100);
						lay.refreshEditors();
					}
				}
			}

			for (Iterator iter = hh.iterator(); iter.hasNext();) {

				GraphNode sn = (GraphNode) iter.next();
				Collection par = sn.getParents();
				for (Iterator iterP = par.iterator(); iterP.hasNext();) {
					GraphNode snn = (GraphNode) h.get((ReturnDefinitionPK) iterP.next());
					if (snn != null)
						dgm.connect(snn.getPort(1), sn.getPort(0));
				}
				lay.refreshEditors();
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		// }
		// };
		// t.start();

	}

	private void getP(ReturnDefinitionPK pk, ReturnDefinitionPK pkO) {
		try {
			Collection v = ((GraphNode) h.get((ReturnDefinitionPK) pk)).getParents();
			for (Iterator iterator = v.iterator(); iterator.hasNext();) {
				ReturnDefinitionPK pkk = (ReturnDefinitionPK) iterator.next();
				((GraphNode) h.get((ReturnDefinitionPK) pk)).setHasParents(true);
				((GraphNode) h.get((ReturnDefinitionPK) pkk)).setLevel(((GraphNode) h.get((ReturnDefinitionPK) pk)).getLevel() + 1);
				if (!pkO.equals(pkk))
					getP(pkk, pk);
			}
		} catch (Exception e) {
			// Main.generalErrorHandler(e);
		}

	}

	private void getC(ReturnDefinitionPK pk, ReturnDefinitionPK pkO) {
		try {
			Collection hp = (Collection) h.values();
			for (Iterator tp = hp.iterator(); tp.hasNext();) {

				ReturnDefinitionPK aaa = (ReturnDefinitionPK) ((GraphNode) tp.next()).getPK();
				Collection v = ((GraphNode) h.get(aaa)).getParents();
				for (Iterator iterator = v.iterator(); iterator.hasNext();) {
					ReturnDefinitionPK pkk = (ReturnDefinitionPK) iterator.next();
					if (pkk.equals(pk)) {
						((GraphNode) h.get((ReturnDefinitionPK) pk)).setHasParents(true);
						((GraphNode) h.get((ReturnDefinitionPK) aaa)).setLevel(((GraphNode) h.get((ReturnDefinitionPK) pk)).getLevel() - 1);
						if (!pkO.equals(aaa))
							getC(aaa, pk);
					}
				}
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	/** Exit the Application */

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton createButton;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JButton dependenciesButton;
	private javax.swing.JButton reviewButton;
	private javax.swing.JMenuItem reviewMenuItem;
	private javax.swing.JButton amendButton;
	private javax.swing.JMenuItem deleteMenuItem;
	private javax.swing.JButton formatButton;
	private javax.swing.JMenuItem amendMenuItem;
	private javax.swing.JMenuItem createMenuItem;
	private javax.swing.JMenuItem formatMenuItem;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton deleteButton;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JPopupMenu popupMenu;
	// End of variables declaration//GEN-END:variables

}

class ReturnDefinitionsAmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnDefinitionAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	private boolean canAmend;

	ReturnDefinitionsAmendAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ReturnDefinitionAmendDialog(parent, true);

		ui.loadIcon("fina2.amend", "amend.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		int index = table.getSelectedRow();

		dialog.show(table.getSelectedTableRow(), true);

		table.updateRow(index, dialog.getTableRow());
	}

}

class ReturnDefinitionsInsertAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnDefinitionAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	ReturnDefinitionsInsertAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ReturnDefinitionAmendDialog(parent, true);

		ui.loadIcon("fina2.insert", "insert.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.create"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.insert"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show(null, true);
		if (dialog.getTableRow() != null)
			table.addRow(dialog.getTableRow());
	}

}

class ReturnDefinitionsReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnDefinitionAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	ReturnDefinitionsReviewAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ReturnDefinitionAmendDialog(parent, true);
		ui.loadIcon("fina2.review", "review.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show(table.getSelectedTableRow(), false);
	}

}

class ReturnDefinitionsDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;
	private ReturnDefinition returnDefinition;

	ReturnDefinitionsDeleteAction(java.awt.Frame parent, EJBTable table) {
		super();

		ui.loadIcon("fina2.delete", "delete.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (!ui.showConfirmBox(parent, ui.getString("fina2.returns.returnDefinitionDeleteQuestion")))
			return;

		try {
			int index = table.getSelectedRow();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnDefinition");
			ReturnDefinitionHome home = (ReturnDefinitionHome) PortableRemoteObject.narrow(ref, ReturnDefinitionHome.class);

			returnDefinition = home.findByPrimaryKey((ReturnDefinitionPK) table.getSelectedPK());
			returnDefinition.remove();

			table.removeRow(index);
		} catch (Exception e) {
			Main.errorHandler(parent, Main.getString("fina2.title"), Main.getString("fina2.delete.returnDefinition"));
		}
	}
}

class ReturnDefinitionsFormatAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnViewFormatFrame dialog;
	private EJBTable table;

	ReturnDefinitionsFormatAction(java.awt.Frame parent, EJBTable table) {

		super();

		ui.loadIcon("fina2.format", "format_cells.gif");

		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.format"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.format"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		dialog = new ReturnViewFormatFrame();
		dialog.setExtendedState(ReturnViewFormatFrame.MAXIMIZED_BOTH);
		dialog.show((ReturnDefinitionPK) table.getSelectedPK(), table.getSelectedTableRow());

	}
}
