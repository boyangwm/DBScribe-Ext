/*
 * Test.java
 *
 * Created on 26 Èþëü 2002 ã., 17:20
 */

package fina2.javascript;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.FinderException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import fina2.javascript.element.Break;
import fina2.javascript.element.Element;
import fina2.javascript.element.Equation;
import fina2.javascript.element.Keyword;
import fina2.javascript.element.Scope;
import fina2.javascript.element.Text;
import fina2.javascript.function.FunctionList;
import fina2.javascript.function.Parameter;
import fina2.metadata.MDTNode;
import fina2.metadata.MDTNodeHome;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRowImpl;

/**
 *
 * @author  David Shalamberidze
 */
public class Wizard extends javax.swing.JFrame implements Runnable {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private FunctionList fl;
    private fina2.javascript.function.Function curF = null;
    private fina2.javascript.element.Function curFun = null;
    private Element curParam;
    private int curIndex = -1;
    private Element curElement = null;

    private boolean edit;

    private Component dialog;

    private Collection refNodes;
    private Collection codes = null;

    private int split1 = 50;
    private int split2 = 50;
    private int split3 = 50;
    private int split4 = 50;

    private int selIndex = 0;

    private Object lastFocus = null;
    private boolean formulaSelection;

    private String sourcePart = "";
    private boolean canRefresh = false;
    private boolean canAmmend = true;
    private EJBTable table;
    private TableRowImpl row;
    
    public Wizard(Component dialog, String s, boolean canAmmend) {
        this(dialog, s);
        this.canAmmend = canAmmend;
    }
    public Wizard(Component dialog, EJBTable table,TableRowImpl row) {
       this(dialog," return ;"); 
       this.table=table;
       this.row=row;
    }
    public Wizard(Component dialog, String s) {
        this.dialog = dialog;

        try {
            split1 = ((Integer) ui
                    .getConfigValue("fina2.javascript.Wizard.split1"))
                    .intValue();
            split2 = ((Integer) ui
                    .getConfigValue("fina2.javascript.Wizard.split2"))
                    .intValue();
            split3 = ((Integer) ui
                    .getConfigValue("fina2.javascript.Wizard.split3"))
                    .intValue();
            split4 = ((Integer) ui
                    .getConfigValue("fina2.javascript.Wizard.split4"))
                    .intValue();
        } catch (Exception e) {
        }

        ui.loadIcon("fina2.ok", "ok.gif");
        ui.loadIcon("fina2.cancel", "cancel.gif");
        ui.loadIcon("fina2.help", "help.gif");
        ui.loadIcon("fina2.next", "forward.gif");
        ui.loadIcon("fina2.back", "back.gif");

        //dialog.setVisible(false);
        fl = new FunctionList(ui);

        Parser p = new Parser(fl);
        p.parse(s);

        initComponents();

        source.requestDefaultFocus();

        jSeparator1.setVisible(false);
        deleteItem.setVisible(false);
        tab.setTitleAt(0, ui.getString("fina2.javascript.functionsTab"));
        tab.setTitleAt(1, ui.getString("fina2.javascript.structureTab"));

        funcDesc.setBackground(getBackground());
        funcDescTop.setBackground(getBackground());
        error.setBackground(getBackground());

        funcList.setModel(new DefaultListModel());
        fl.load();

        java.util.Vector v = new java.util.Vector();
        v.add(ui.getString("fina2.all"));
        v.addAll(fl.getCategories());
        catList.setModel(new DefaultComboBoxModel(v));
        tree.setModel(p.getTreeModel());
        catList.setSelectedIndex(0);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                .getRoot();
        expandNode(root);

        tree.setSelectionPath(new TreePath(root.getPath()));

        sourcePart = getFullSource();
        source.setText(sourcePart);

        if (root.getChildCount() > 0) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root
                    .getChildAt(0);
            if (node.getUserObject() instanceof Keyword) {
                Keyword k = (Keyword) node.getUserObject();
                if (k.getName().equals("return")) {
                    tree.setSelectionPath(new TreePath(node.getPath()));
                    editFunction(findFunction(k), k);
                    source.setCaretPosition(7);
                }
            }
        }

        tree.setShowsRootHandles(false);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            public java.awt.Component getTreeCellRendererComponent(
                    javax.swing.JTree tree, Object value, boolean selected,
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {

                JLabel l = (JLabel) super.getTreeCellRendererComponent(tree,
                        value, selected, expanded, leaf, row, hasFocus);
                Element e = (Element) ((DefaultMutableTreeNode) value)
                        .getUserObject();
                l.setIcon(e.getNodeIcon());
                //updateTree();
                //checkSyntax();
                return l;
            }
        });
        //Thread t = new Thread(this);
        //t.start();
        loadConf();

    }

    public Collection getCodes() {
        return codes;
    }

    private String getFullSource() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                .getRoot();
        return ((Element) root.getUserObject()).print(0, true);
    }

    public void resetFuncList() {
        funcList.setSelectedIndex(0);
    }

    public void show() {
        dialog.setVisible(false);
        super.show();
        splitPane1.setDividerLocation(split1);
        splitPane2.setDividerLocation(split2);
        splitPane3.setDividerLocation(split3);
        splitPane4.setDividerLocation(split4);

        if (!canAmmend) {
            okButton.setVisible(false);
            source.setFocusable(false);
            source.setEditable(false);
        }
    }

    private void loadConf() {
        int x = 0, y = 0, w = 100, h = 200;

        /*int split1 = 50;
        int split2 = 50;
        int split3 = 50;
        int split4 = 50;*/

        boolean v = false;

        try {
            x = ((Integer) ui.getConfigValue("fina2.javascript.Wizard.x"))
                    .intValue();
            y = ((Integer) ui.getConfigValue("fina2.javascript.Wizard.y"))
                    .intValue();
            w = ((Integer) ui.getConfigValue("fina2.javascript.Wizard.width"))
                    .intValue();
            h = ((Integer) ui.getConfigValue("fina2.javascript.Wizard.height"))
                    .intValue();
            v = ((Boolean) ui.getConfigValue("fina2.javascript.Wizard.visible"))
                    .booleanValue();
            /*split1 = ((Integer)ui.getConfigValue("fina2.javascript.Wizard.split1")).intValue();
            split2 = ((Integer)ui.getConfigValue("fina2.javascript.Wizard.split2")).intValue();
            split3 = ((Integer)ui.getConfigValue("fina2.javascript.Wizard.split3")).intValue();
            split4 = ((Integer)ui.getConfigValue("fina2.javascript.Wizard.split4")).intValue();*/
        } catch (Exception e) {
        }

        setLocation(x, y);
        setSize(w, h);

        if (v)
            main.addToShow(this); //this.show();
    }

    public String getSource() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                .getRoot();
        return ((Element) root.getUserObject()).print(0, false);
    }

    public Collection getRefNodes() {

        if (!okButton.isEnabled())
            return null;

        codes = new HashSet();
        try {
            Collection c = findAllScopes();
            error.setText("");
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                Scope s = (Scope) iter.next();

                Collection scopeCodes = (Collection) ((Vector) fina2.script.Engine
                        .evalNodeInterceptor(s.print(0, false))).clone();

                codes.addAll(scopeCodes);
            }
            okButton.setEnabled(true);

        } catch (Exception e) {
            refNodes = null;
        }

        try {
            InitialContext jndi = fina2.Main.main.getJndiContext();
            Object ref = jndi.lookup("fina2/metadata/MDTNode");
            MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref,
                    MDTNodeHome.class);

            refNodes = new Vector();
            String c = "";
            try {
                for (Iterator iter = codes.iterator(); iter.hasNext();) {
                    c = (String) iter.next();
                    MDTNode node = home.findByCodeExact(c);
                    refNodes.add(node.getPrimaryKey());
                }
            } catch (FinderException e) {
                ui.showMessageBox(null, "JavaScript", ui
                        .getString("fina2.metadata.codeNotFound")
                        + c);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ui.showMessageBox(null, ui.getString("fina2.title"), e.toString());
        }
        return refNodes;
    }

    public void addFunctionToParameter(Element e) {
        curParam = e;
        tab.setSelectedIndex(0);
        selIndex = 0;
        funcList.requestFocus();
    }

    public fina2.javascript.element.Function getCurFunction() {
        return curFun;
    }

    public javax.swing.JTextArea getSourceTextArea() {
        return source;
    }

    private void expandNode(DefaultMutableTreeNode node) {
        tree.expandPath(new TreePath(node.getPath()));
        for (Enumeration childEnum = node.children(); childEnum
                .hasMoreElements();) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) childEnum
                    .nextElement();
            if (!(n.getUserObject() instanceof Equation))
                expandNode(n);
        }
    }

    private Collection findAllScopes() {
        Vector v = new Vector();
        try {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree
                    .getModel().getRoot();
            Element rootScope = (Element) root.getUserObject();
            v.add(rootScope);
            findScopes(rootScope, v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    private Scope getRootlScope() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                .getRoot();
        return (Scope) root.getUserObject();
    }

    private void findScopes(Element element, Collection v) throws Exception {
        for (Iterator iter = element.children(); iter.hasNext();) {
            Element e = ((Element) iter.next()).getValidElement();
            if (e instanceof Scope)
                v.add(e);
            findScopes(e, v);
        }
    }

    private void rollBackToFunction() {
        try {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getSelectionPath().getLastPathComponent();
            while (true) {
                node = (DefaultMutableTreeNode) node.getParent();
                if (node.getUserObject() instanceof fina2.javascript.element.Function) {
                    tree.setSelectionPath(new TreePath(node.getPath()));
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

    private DefaultMutableTreeNode findNodeFromRoot(Element e) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                .getRoot();
        return findNode(root, e);
    }

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode node,
            Element e) {

        for (Enumeration childEnum = node.children(); childEnum
                .hasMoreElements();) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) childEnum
                    .nextElement();
            if (n.getUserObject().equals(e))
                return n;
            DefaultMutableTreeNode res = findNode(n, e);
            if (res != null)
                return res;
        }
        return null;
    }

    private void checkSyntax() {
        Thread t = new Thread() {
            public void run() {
                try {
                    System.out.println("-----");
                    System.out.println(getSource());
                    System.out.println("-----");

                    error.setText("");
                    fina2.script.Engine.evalNodeInterceptor(getSource());
                    okButton.setEnabled(true);

                    /*
                                        Collection c = findAllScopes();
                                        error.setText("");
                                        for(Iterator iter=c.iterator(); iter.hasNext(); ) {
                                            Scope s = (Scope)iter.next();
                                            System.out.println("-----");
                                            System.out.println(s.print(0, false));
                                            System.out.println("-----");
                                            fina2.script.Engine.evalNodeInterceptor(
                                                s.print(0, false) //source.getText()
                                            );
                                        }
                                        okButton.setEnabled(true);
                     */
                } catch (Exception e) {
                    okButton.setEnabled(false);
                    error.setText(e.getMessage());
                }
            }
        };
        t.start();
        try {
            long t1 = System.currentTimeMillis();
            t.join(4000);
            long t2 = System.currentTimeMillis();
            if ((t2 - t1) >= 4000) {
                okButton.setEnabled(false);
                error.setText("unterminated loop");

                t.stop();
            }
            t = null;
            //System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private fina2.javascript.element.Function insertFunction(Element parent,
            fina2.javascript.function.Function f, int index) {
        try {
            fina2.javascript.element.Function fun = new fina2.javascript.element.Function(
                    f.getName());
            if (f.getName().equals("if") || f.getName().equals("for")
                    || f.getName().equals("while")
                    || f.getName().equals("switch")
                    || f.getName().equals("return")
                    || f.getName().equals("else")) {
                fun = new Keyword(f.getName());
            }

            //System.out.println("parameters ");
            for (Iterator iter = f.getParameters().iterator(); iter.hasNext();) {
                Parameter p = (Parameter) iter.next();

                Equation eq = new Equation();
                eq.addChild(new Text(p.getName()));
                fun.getParameters().addChild(eq);
                //System.out.println("parameter "+p.getName());
            }
            //System.out.println("aaa "+fun.print(0));

            if (f.getName().equals("if") || f.getName().equals("for")
                    || f.getName().equals("while")
                    || f.getName().equals("switch")
                    || f.getName().equals("else")) {
                fun.addChild(new Scope());
            }

            if (index == -1) {
                if ((parent != null) && (parent.numberOfChildren() == 1)) {
                    Element ch = parent.getChildAt(0);
                    if (ch instanceof Text) {
                        if (((Text) ch).getNodeName().equals("")) {
                            parent.removeChild(ch);
                        }
                    }
                }
                parent.addChild(fun);
                if ((parent instanceof Scope) && (fun.numberOfChildren() == 0)
                        && (!fun.getName().equals("return")))
                    parent.addChild(new Break());
            } else {
                parent.insertChildAt(index, fun);
                if ((parent instanceof Scope) && (fun.numberOfChildren() == 0)
                        && (!fun.getName().equals("return")))
                    parent.insertChildAt(index + 1, new Break());

                DefaultMutableTreeNode node = findNodeFromRoot(parent);
                if (node != null)
                    tree.setSelectionPath(new TreePath(node.getPath()));
            }

            curF = f;
            curFun = fun;
            refreshTree(true);
            //updateTree();

            DefaultMutableTreeNode node = findNodeFromRoot(fun);
            if (node != null)
                tree.setSelectionPath(new TreePath(node.getPath()));

            //editFunction(f, fun);

            return fun;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private fina2.javascript.element.Function insertFunction(
            fina2.javascript.function.Function f) {
        try {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getSelectionPath().getLastPathComponent();
            Element e = (Element) node.getUserObject();

            int index = -1;
            Element scope = null;
            if (e instanceof Scope) {
                scope = e;
            } else {
                if (node.getParent() != null) {
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                            .getParent();
                    scope = (Element) parent.getUserObject();
                    index = scope.getChildIndex(e);
                    index++;
                } else {
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree
                            .getModel().getRoot();
                    scope = (Element) root.getUserObject();

                    tree.setSelectionPath(new TreePath(root.getPath()));
                }
            }
            return insertFunction(scope, f, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private fina2.javascript.element.Function findNextFunction() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                .getSelectionPath().getLastPathComponent();
        if (node == null)
            return null;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                .getParent();
        if (parent == null)
            return null;
        return findNextFunction(node, parent.getIndex(node));
    }

    private fina2.javascript.element.Function findNextFunction(
            DefaultMutableTreeNode parent, int index) {
        int count = parent.getChildCount();
        for (int i = index; i < count; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent
                    .getChildAt(i);
            if (node.getUserObject() instanceof fina2.javascript.element.Function)
                return (fina2.javascript.element.Function) node.getUserObject();
            fina2.javascript.element.Function res = findNextFunction(node, 0);
            if (res != null)
                return res;
        }
        DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent.getParent();
        if (p == null)
            return null;
        fina2.javascript.element.Function res = findNextFunction(p, p
                .getIndex(parent) + 1);
        if (res != null)
            return res;
        return null;
    }

    private fina2.javascript.element.Function findBackFunction() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                .getSelectionPath().getLastPathComponent();
        if (node == null)
            return null;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                .getParent();
        if (parent == null)
            return null;
        return findBackFunction(parent, parent.getIndex(node) - 1);
    }

    private fina2.javascript.element.Function findBackFunction(
            DefaultMutableTreeNode parent, int index) {
        int count = parent.getChildCount();

        for (int i = index; (i >= 0) && (count > 0); i--) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent
                    .getChildAt(i);
            fina2.javascript.element.Function res = findBackFunction(node, node
                    .getChildCount() - 1);
            if (res != null)
                return res;
            if (node.getUserObject() instanceof fina2.javascript.element.Function)
                return (fina2.javascript.element.Function) node.getUserObject();
        }
        if (parent.getUserObject() instanceof fina2.javascript.element.Function)
            return (fina2.javascript.element.Function) parent.getUserObject();

        DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent.getParent();
        if (p == null)
            return null;
        fina2.javascript.element.Function res = findBackFunction(p, p
                .getIndex(parent) - 1);
        if (res != null)
            return res;
        return null;
    }

    private fina2.javascript.function.Function findFunction(
            fina2.javascript.element.Function fun) {
        //!!!!!!!!!!!!
        //return (fina2.javascript.function.Function)funcList.getSelectedValue();
        return fl.findFunction(fun);
    }

    public void editFunction(fina2.javascript.function.Function f,
            fina2.javascript.element.Function fun) {
        javax.swing.JComponent focus = null;
        try {
            funcDescTop.setText(f.getFullName() + "\n" + f.getDescription());
            JPanel pp = new JPanel();
            pp.setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints2;

            int count = 0;
            int i = 0;
            for (Iterator iter = f.getParameters().iterator(); iter.hasNext(); count++) {
                fina2.javascript.function.Parameter p = (fina2.javascript.function.Parameter) iter
                        .next();

                JLabel pName = new JLabel();
                pName.setFont(new java.awt.Font("Dialog", 1, 11));
                pName.setText(p.getName());
                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 0;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
                pp.add(pName, gridBagConstraints2);

                JButton fx = new JButton();
                Element eq = null;
                try {
                    eq = fun.getParameters().getChildAt(count);
                } catch (Exception exx) {
                    eq = new Equation();
                    fun.getParameters().addChild(eq);
                }
                fx.addActionListener(new ParameterAddFunction(this, eq));
                fx.setIcon(Parser.getIcon("fx.gif"));
                fx.setFocusPainted(false);
                fx.setMargin(new java.awt.Insets(1, 1, 1, 1));

                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 1;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
                pp.add(fx, gridBagConstraints2);

                ParameterEditor pEdit = new ParameterEditor(this, fun
                        .getParameters().getChildAt(count));
                DNDTextField pValue = new DNDTextField(pEdit, canAmmend);

                pValue.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        lastFocus = evt.getSource();
                    }
                });

                pValue.addKeyListener(pEdit);

                pValue.setFont(new java.awt.Font("Monospaced", 0, 11));
                pValue.setText(fun.getParameters().getChildAt(count).print(0,
                        true)); //!!!// //p.getName());
                pValue.setColumns(30);
                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 2;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 0);
                pp.add(pValue, gridBagConstraints2);

                if (count == 0)
                    focus = pValue;

                i++;

                JTextArea pDesc = new JTextArea();
                pDesc.setFont(new java.awt.Font("Dialog", 0, 11));
                pDesc.setText(p.getDescription());
                pDesc.setColumns(20);
                pDesc.setRows(2);
                pDesc.setEditable(false);
                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 2;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
                pp.add(pDesc, gridBagConstraints2);
                pDesc.setBackground(getBackground());

                i++;
            }

            if ((fun.numberOfChildren() > 0)
                    && (fun.getChildAt(0) instanceof Scope)) {
                JLabel pName = new JLabel();
                pName.setFont(new java.awt.Font("Dialog", 1, 11));
                if (fun.getName().toLowerCase().equals("if"))
                    pName.setText("then");
                else
                    pName.setText("");
                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 0;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
                pp.add(pName, gridBagConstraints2);

                /*JButton fx = new JButton();
                Element eq = null;
                try {
                    eq = fun.getParameters().getChildAt(count);
                } catch(Exception exx) {
                    eq = new Equation();
                    fun.getParameters().addChild(eq);
                }
                fx.addActionListener(
                    new ParameterAddFunction(this, eq)
                );
                fx.setIcon(Parser.getIcon("fx.gif"));
                fx.setFocusPainted(false);
                fx.setMargin(new java.awt.Insets(1, 1, 1, 1));

                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 1;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
                pp.add(fx, gridBagConstraints2);*/

                Scope scope = (Scope) fun.getChildAt(0);
                ParameterEditor pEdit = new ParameterEditor(this, scope);
                JTextArea pValue = new ScopeEditor(pEdit); /*{
                                   public void copy() {
                                       processEvent(
                                           new java.awt.event.KeyEvent(this, 0, 0, 0, 0)
                                       );
                                   }
                               };*/

                pValue.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        lastFocus = evt.getSource();
                    }
                });

                pValue.addKeyListener(pEdit);

                pValue.setFont(new java.awt.Font("Monospaced", 0, 11));
                pValue.setText(scope.print(0, true)); //!!!// //p.getName());
                pValue.setColumns(30);
                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 2;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 0);

                pValue.setLineWrap(true);
                pValue.setWrapStyleWord(true);
                pValue.setRows(3);

                JScrollPane ps = new JScrollPane(pValue);
                ps
                        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                pp.add(ps, gridBagConstraints2);

                //if(count == 0)
                //    focus = pValue;

                i++;

                JTextArea pDesc = new JTextArea();
                pDesc.setFont(new java.awt.Font("Dialog", 0, 11));
                pDesc.setText("");
                pDesc.setColumns(20);
                pDesc.setRows(2);
                pDesc.setEditable(false);
                gridBagConstraints2 = new java.awt.GridBagConstraints();
                gridBagConstraints2.gridx = 2;
                gridBagConstraints2.gridy = i;
                gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
                pp.add(pDesc, gridBagConstraints2);
                pDesc.setBackground(getBackground());

                i++;

                if (fun.getName().toLowerCase().equals("if")) {
                    if (fun.numberOfChildren() == 1) {
                        Keyword els = new Keyword("else");
                        scope = new Scope();
                        els.addChild(scope);
                        fun.addChild(els);
                        /*int c = source.getCaretPosition();
                        sourceKeyReleased(
                            new java.awt.event.KeyEvent(source, 0,0,0,0)
                        );
                        source.setCaretPosition(c);
                        sourceCaretControl(c);
                        return;*/
                    } else {
                        Element els = fun.getChildAt(1);
                        scope = (Scope) els.getChildAt(0);
                    }
                    pName = new JLabel();
                    pName.setFont(new java.awt.Font("Dialog", 1, 11));
                    pName.setText("else");
                    gridBagConstraints2 = new java.awt.GridBagConstraints();
                    gridBagConstraints2.gridx = 0;
                    gridBagConstraints2.gridy = i;
                    gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
                    pp.add(pName, gridBagConstraints2);

                    pEdit = new ParameterEditor(this, scope);
                    pValue = new ScopeEditor(pEdit); /*{
                                           public void copy() {
                                               processEvent(
                                                   new java.awt.event.KeyEvent(this, 0, 0, 0, 0)
                                               );
                                           }
                                       };*/

                    pValue.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusGained(java.awt.event.FocusEvent evt) {
                            lastFocus = evt.getSource();
                        }
                    });

                    pValue.addKeyListener(pEdit);

                    pValue.setFont(new java.awt.Font("Monospaced", 0, 11));
                    pValue.setText(scope.print(0, true)); //!!!// //p.getName());
                    pValue.setColumns(30);
                    gridBagConstraints2 = new java.awt.GridBagConstraints();
                    gridBagConstraints2.gridx = 2;
                    gridBagConstraints2.gridy = i;
                    gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 0);

                    pValue.setLineWrap(true);
                    pValue.setWrapStyleWord(true);
                    pValue.setRows(3);

                    ps = new JScrollPane(pValue);
                    ps
                            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    pp.add(ps, gridBagConstraints2);

                    //if(count == 0)
                    //    focus = pValue;

                    i++;

                    pDesc = new JTextArea();
                    pDesc.setFont(new java.awt.Font("Dialog", 0, 11));
                    pDesc.setText("");
                    pDesc.setColumns(20);
                    pDesc.setRows(2);
                    pDesc.setEditable(false);
                    gridBagConstraints2 = new java.awt.GridBagConstraints();
                    gridBagConstraints2.gridx = 2;
                    gridBagConstraints2.gridy = i;
                    gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
                    gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
                    pp.add(pDesc, gridBagConstraints2);
                    pDesc.setBackground(getBackground());

                    i++;
                }
            }

            paramScroll.setViewportView(pp);
            curF = f;
            curFun = fun;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!source.hasFocus()) {
            //source.grabFocus();
            //source.setSelectedTextColor(java.awt.Color.white);
            //source.setSelectionColor(java.awt.Color.black);
            if (focus != null)
                focus.grabFocus();

            source.select(fun.getStartPosition(), fun.getEndPosition() - 1);
            // source.grabFocus();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        popupMenu = new javax.swing.JPopupMenu();
        addItem = new javax.swing.JMenuItem();
        insertItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        deleteItem = new javax.swing.JMenuItem();
        splitPane1 = new javax.swing.JSplitPane();
        splitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        source = new javax.swing.JTextArea();
        splitPane3 = new javax.swing.JSplitPane();
        paramPanel = new javax.swing.JPanel();
        paramScroll = new javax.swing.JScrollPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        funcDescTop = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        error = new javax.swing.JTextArea();
        tab = new javax.swing.JTabbedPane();
        splitPane4 = new javax.swing.JSplitPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        funcDesc = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        funcList = new javax.swing.JList();
        catList = new javax.swing.JComboBox();
        jScrollPane7 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        helpButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        addItem.setFont(new java.awt.Font("Dialog", 0, 11));
        addItem.setText("Add function...");
        addItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemActionPerformed(evt);
            }
        });

        popupMenu.add(addItem);

        insertItem.setFont(new java.awt.Font("Dialog", 0, 11));
        insertItem.setText("Insert Function After...");
        insertItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertItemActionPerformed(evt);
            }
        });

        popupMenu.add(insertItem);

        popupMenu.add(jSeparator1);

        deleteItem.setFont(new java.awt.Font("Dialog", 0, 11));
        deleteItem.setText("Delete");
        deleteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteItemActionPerformed(evt);
            }
        });

        popupMenu.add(deleteItem);

        setTitle(ui.getString("fina2.javascript.wizard"));
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
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        splitPane1.setDividerSize(2);
        splitPane1
                .addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(
                            java.beans.PropertyChangeEvent evt) {
                        splitPane1PropertyChange(evt);
                    }
                });

        splitPane2.setDividerSize(3);
        splitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane2
                .addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(
                            java.beans.PropertyChangeEvent evt) {
                        splitPane2PropertyChange(evt);
                    }
                });

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane2
                .setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        source.setWrapStyleWord(true);
        source.setLineWrap(true);
        source.setTabSize(4);
        source.setFont(new java.awt.Font("Monospaced", 1, 12));
        source.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                sourceCaretUpdate(evt);
            }
        });
        source.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                sourceFocusLost(evt);
            }
        });
        source.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sourceKeyReleased(evt);
            }
        });
        source.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sourceMouseClicked(evt);
            }
        });

        jScrollPane2.setViewportView(source);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        splitPane2.setRightComponent(jPanel2);

        splitPane3.setDividerSize(3);
        splitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane3
                .addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(
                            java.beans.PropertyChangeEvent evt) {
                        splitPane3PropertyChange(evt);
                    }
                });

        paramPanel.setLayout(new java.awt.BorderLayout());

        paramScroll.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(1, 1, 1, 1)));
        paramScroll
                .setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        paramPanel.add(paramScroll, java.awt.BorderLayout.CENTER);

        jScrollPane3.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(10, 10, 10, 10)));
        jScrollPane3
                .setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        funcDescTop.setWrapStyleWord(true);
        funcDescTop.setLineWrap(true);
        funcDescTop.setEditable(false);
        funcDescTop.setRows(3);
        funcDescTop.setFont(ui.getFont());
        funcDescTop.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                funcDescTopFocusLost(evt);
            }
        });

        jScrollPane3.setViewportView(funcDescTop);

        paramPanel.add(jScrollPane3, java.awt.BorderLayout.NORTH);

        splitPane3.setLeftComponent(paramPanel);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel1
                .setBorder(new javax.swing.border.TitledBorder(null,
                        "Error Message",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION, ui
                                .getFont()));
        jScrollPane6.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(1, 1, 1, 1)));
        jScrollPane6
                .setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        error.setWrapStyleWord(true);
        error.setLineWrap(true);
        error.setForeground(java.awt.Color.red);
        error.setFont(ui.getFont());
        error.setEditable(false);
        error.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                errorFocusLost(evt);
            }
        });

        jScrollPane6.setViewportView(error);

        jPanel1.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        splitPane3.setRightComponent(jPanel1);

        splitPane2.setLeftComponent(splitPane3);

        splitPane1.setRightComponent(splitPane2);

        tab.setFont(ui.getFont());
        tab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabStateChanged(evt);
            }
        });

        splitPane4.setDividerSize(3);
        splitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane4
                .addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(
                            java.beans.PropertyChangeEvent evt) {
                        splitPane4PropertyChange(evt);
                    }
                });

        jScrollPane5.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(1, 1, 1, 1)));
        jScrollPane5
                .setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        funcDesc.setWrapStyleWord(true);
        funcDesc.setLineWrap(true);
        funcDesc.setEditable(false);
        funcDesc.setFont(new java.awt.Font("Dialog", 0, 10));
        funcDesc.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(1, 1, 1, 1)));
        jScrollPane5.setViewportView(funcDesc);

        splitPane4.setRightComponent(jScrollPane5);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(1, 1, 1, 1)));
        funcList.setFont(new java.awt.Font("Dialog", 0, 11));
        funcList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                funcListFocusGained(evt);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                funcListFocusLost(evt);
            }
        });
        funcList
                .addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                    public void valueChanged(
                            javax.swing.event.ListSelectionEvent evt) {
                        funcListValueChanged(evt);
                    }
                });
        funcList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                funcListMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(funcList);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        catList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                catListActionPerformed(evt);
            }
        });

        jPanel3.add(catList, java.awt.BorderLayout.NORTH);

        splitPane4.setLeftComponent(jPanel3);

        tab.addTab("Functions", null, splitPane4, "");

        tree.setFont(new java.awt.Font("Dialog", 0, 11));
        tree.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeFocusLost(evt);
            }
        });
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });
        tree
                .addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
                    public void valueChanged(
                            javax.swing.event.TreeSelectionEvent evt) {
                        treeValueChanged(evt);
                    }
                });

        jScrollPane7.setViewportView(tree);

        tab.addTab("Structure", null, jScrollPane7, "");

        splitPane1.setLeftComponent(tab);

        getContentPane().add(splitPane1, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.BorderLayout());

        helpButton.setIcon(ui.getIcon("fina2.help"));
        helpButton.setFont(ui.getFont());
        helpButton.setText(ui.getString("fina2.help"));
        helpButton.setEnabled(false);
        jPanel6.add(helpButton);

        jPanel5.add(jPanel6, java.awt.BorderLayout.WEST);

        backButton.setIcon(ui.getIcon("fina2.back"));
        backButton.setFont(ui.getFont());
        backButton.setText(ui.getString("fina2.back"));
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        jPanel7.add(backButton);

        nextButton.setIcon(ui.getIcon("fina2.next"));
        nextButton.setFont(ui.getFont());
        nextButton.setText(ui.getString("fina2.next"));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        jPanel7.add(nextButton);

        jPanel7.add(jPanel8);

        okButton.setIcon(ui.getIcon("fina2.ok"));
        okButton.setFont(ui.getFont());
        okButton.setText(ui.getString("fina2.ok"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jPanel7.add(okButton);

        cancelButton.setIcon(ui.getIcon("fina2.cancel"));
        cancelButton.setFont(ui.getFont());
        cancelButton.setText(ui.getString("fina2.cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel7.add(cancelButton);

        jPanel5.add(jPanel7, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    private void funcListFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_funcListFocusGained
        nextButton.setEnabled(true);
    }//GEN-LAST:event_funcListFocusGained

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        backFunction();
    }//GEN-LAST:event_backButtonActionPerformed

    private void funcListFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_funcListFocusLost
        formulaSelection = true;
    }//GEN-LAST:event_funcListFocusLost

    private void sourceKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sourceKeyReleased
        if (evt.getSource() != source)
            return;

        sourcePart = source.getText();
        try {
            Parser p = new Parser(fl);
            p.parse(sourcePart);

            DefaultTreeModel model = p.getTreeModel(); //((DefaultTreeModel)tree.getModel());
            tree.setModel(model);
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
                    .getRoot();

            expandNode(root);

            /*tree.setSelectionPath(
                new TreePath(root.getPath())
            );*/

            sourcePart = getFullSource();
            //source.setText(sourcePart);

            checkSyntax();
            //} catch(EOFException ex) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sourceCaretControl(source.getCaretPosition());
    }//GEN-LAST:event_sourceKeyReleased

    private synchronized void sourceCaretControl(int pos) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                .getRoot();
        Element rootElement = (Element) root.getUserObject();

        curFun = rootElement.findByPosition(pos);
        //System.out.println("caret: "+f);
        if (curFun == null) {
            closeFunctionEditor();
        } else {
            curF = findFunction(curFun);
            DefaultMutableTreeNode node = findNodeFromRoot(curFun);
            if (node != null) {
                tree.setSelectionPath(new TreePath(node.getPath()));
            }

            //editFunction(findFunction(f), f);
        }
    }

    private void sourceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_sourceCaretUpdate
        if (source.hasFocus())
            sourceCaretControl(evt.getMark());
    }//GEN-LAST:event_sourceCaretUpdate

    private void sourceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sourceFocusLost
        lastFocus = source;
        formulaSelection = false;
        if (error.getText().trim().equals("")) {
            /*sourcePart = getFullSource();
            source.setText(sourcePart);*/
            //source.setText(getFullSource());
        }
        /*sourcePart = source.getText();
        try {
            Parser p = new Parser(fl);
            p.parse(sourcePart);

            DefaultTreeModel model = p.getTreeModel(); //((DefaultTreeModel)tree.getModel());
            tree.setModel(model);
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

            expandNode(root);

            tree.setSelectionPath(
                new TreePath(root.getPath())
            );

            sourcePart = getFullSource();
            source.setText(sourcePart);

            checkSyntax();
        } catch(Exception ex) {
            ex.printStackTrace();
        }*/
    }//GEN-LAST:event_sourceFocusLost

    private void sourceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceMouseClicked
        formulaSelection = false;
        sourceKeyReleased(new java.awt.event.KeyEvent(source, 0, 0, 0, 0));
        //sourceCaretControl(source.getCaretPosition());
        /*        sourcePart = source.getText();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
                tree.setSelectionPath(
                    new TreePath(root.getPath())
                );
                lastFocus = source;*/
    }//GEN-LAST:event_sourceMouseClicked

    private void treeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeFocusLost
        formulaSelection = false;
        lastFocus = evt.getSource();
    }//GEN-LAST:event_treeFocusLost

    private void errorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_errorFocusLost
        formulaSelection = false;
        lastFocus = evt.getSource();
    }//GEN-LAST:event_errorFocusLost

    private void funcDescTopFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_funcDescTopFocusLost
        formulaSelection = false;
        lastFocus = evt.getSource();
    }//GEN-LAST:event_funcDescTopFocusLost

    private void splitPane4PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_splitPane4PropertyChange
        ui.putConfigValue("fina2.javascript.Wizard.split4", new Integer(
                splitPane4.getDividerLocation()));
    }//GEN-LAST:event_splitPane4PropertyChange

    private void splitPane3PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_splitPane3PropertyChange
        ui.putConfigValue("fina2.javascript.Wizard.split3", new Integer(
                splitPane3.getDividerLocation()));
    }//GEN-LAST:event_splitPane3PropertyChange

    private void splitPane2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_splitPane2PropertyChange
        ui.putConfigValue("fina2.javascript.Wizard.split2", new Integer(
                splitPane2.getDividerLocation()));
    }//GEN-LAST:event_splitPane2PropertyChange

    private void splitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_splitPane1PropertyChange
        ui.putConfigValue("fina2.javascript.Wizard.split1", new Integer(
                splitPane1.getDividerLocation()));
    }//GEN-LAST:event_splitPane1PropertyChange

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
        ui
                .putConfigValue("fina2.javascript.Wizard.visible", new Boolean(
                        false));
    }//GEN-LAST:event_formComponentHidden

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        ui.putConfigValue("fina2.javascript.Wizard.visible", new Boolean(true));
    }//GEN-LAST:event_formComponentShown

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        ui.putConfigValue("fina2.javascript.Wizard.x", new Integer(getX()));
        ui.putConfigValue("fina2.javascript.Wizard.y", new Integer(getY()));
    }//GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        ui.putConfigValue("fina2.javascript.Wizard.width", new Integer(
                getWidth()));
        ui.putConfigValue("fina2.javascript.Wizard.height", new Integer(
                getHeight()));
    }//GEN-LAST:event_formComponentResized

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
        setVisible(false);
        if(table!=null)
        table.addRow(row);
        dialog.setVisible(true);
        
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        okButton.setEnabled(false);
        dispose();
        refNodes = null;
        setVisible(false);
        dialog.setVisible(true);
  
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void deleteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteItemActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_deleteItemActionPerformed

    private void insertItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertItemActionPerformed
        try {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getSelectionPath().getLastPathComponent();
            Element e = (Element) node.getUserObject();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                    .getParent();
            Element pelement = (Element) parent.getUserObject();
            curIndex = pelement.getChildIndex(e) + 1;
            curF = null;
            curFun = null;
            curElement = pelement;
            tab.setSelectedIndex(0);
            selIndex = 0;
        } catch (Exception e) {
        }
    }//GEN-LAST:event_insertItemActionPerformed

    private void addItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemActionPerformed
        try {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getSelectionPath().getLastPathComponent();
            Element e = (Element) node.getUserObject();
            if ((e instanceof Keyword)
                    && (((Keyword) e).getName().equals("return"))) {
                e = ((Keyword) e).getFirstParameter();
            }
            curIndex = e.numberOfChildren();
            curElement = e;

            curF = null;
            curFun = null;
            tab.setSelectedIndex(0);
            selIndex = 0;
        } catch (Exception e) {
        }
    }//GEN-LAST:event_addItemActionPerformed

    private void treeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseClicked
        if ((evt.getModifiers() & evt.BUTTON3_MASK) != 0) {
            try {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                        .getSelectionPath().getLastPathComponent();
                Element e = (Element) node.getUserObject();
                if ((e instanceof Scope)
                        || (e instanceof Equation)
                        || ((e instanceof Keyword) && (((Keyword) e)
                                .getParameters().numberOfChildren() == 1))) {
                    addItem.setEnabled(true);
                } else {
                    addItem.setEnabled(false);
                }
                popupMenu.show(tree, evt.getX(), evt.getY());
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_treeMouseClicked

    private void tabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabStateChanged
        /*nextButton.setEnabled(false);
        if( (selIndex == 0) && (funcList.getSelectedValue() != null) ){
            nextButton.setEnabled(true);
        }
        if( (selIndex == 1) && (curFun != null) ){
            nextButton.setEnabled(true);
        }*/
    }//GEN-LAST:event_tabStateChanged

    private void nextFunction() {
        Element e = findNextFunction();
        if (e != null) {
            DefaultMutableTreeNode node = findNodeFromRoot(e);
            if (e != null) {
                tree.setSelectionPath(new TreePath(node.getPath()));
            }
            if (((fina2.javascript.element.Function) node.getUserObject())
                    .getName().equals("else"))
                nextFunction();
        }
    }

    private void backFunction() {
        Element e = findBackFunction();
        if (e != null) {
            DefaultMutableTreeNode node = findNodeFromRoot(e);
            if (e != null) {
                tree.setSelectionPath(new TreePath(node.getPath()));
            }
            if (((fina2.javascript.element.Function) node.getUserObject())
                    .getName().equals("else"))
                backFunction();
        }
    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if ((tab.getSelectedIndex() == 0) && (formulaSelection)) {
            formulaSelection = false;
            if (funcList.getSelectedValue() != null) {
                fina2.javascript.function.Function f = (fina2.javascript.function.Function) funcList
                        .getSelectedValue();
                if ((lastFocus instanceof DNDTextField)
                        || ((lastFocus instanceof JTextArea) && (!lastFocus
                                .equals(source)))) {
                    JTextComponent text = (JTextComponent) lastFocus;
                    StringBuffer sb = new StringBuffer(text.getText());
                    int caret = text.getCaretPosition();
                    sb.insert(caret, f.getFullName());
                    text.setText(sb.toString());

                    /*text.getParameterEditor().keyReleased(
                        new java.awt.event.KeyEvent(text, 0, 0, 0, 0)
                    );*/
                    text.copy();

                    caret = caret + f.getFullName().length();
                    text.setCaretPosition(caret);
                    //sourceCaretControl(caret);
                } else {
                    if ((lastFocus != null) && (lastFocus.equals(source))) {
                        JTextArea text = (JTextArea) lastFocus;
                        StringBuffer sb = new StringBuffer(text.getText());
                        int caret = text.getCaretPosition();
                        sb.insert(caret, f.getFullName());
                        text.setText(sb.toString());
                        /*text.getParameterEditor().keyReleased(
                            new java.awt.event.KeyEvent(text, 0, 0, 0, 0)
                        );*/
                        if (f.getName().equals("if")
                                || f.getName().equals("for")
                                || f.getName().equals("while")
                                || f.getName().equals("return"))
                            caret = caret + 1;
                        else
                            caret = caret + f.getFullName().length() - 1;
                        text.setCaretPosition(caret);
                        sourceKeyReleased(new java.awt.event.KeyEvent(source,
                                0, 0, 0, 0));
                        //text.grabFocus();
                    }
                }
            }
            nextButton.setEnabled(false);
            return;
        }
        formulaSelection = false;
        nextFunction();
    }//GEN-LAST:event_nextButtonActionPerformed

    private void closeFunctionEditor() {
        curIndex = -1;
        curF = null;
        curFun = null;
        paramScroll.setViewportView(new JPanel());
        funcDescTop.setText("");
        //nextButton.setEnabled(false);
        rollBackToFunction();
    }

    private void funcListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_funcListMouseClicked
        if (evt.getClickCount() == 2) {
            formulaSelection = true;
            nextButtonActionPerformed(null);
            /*if(curF != null) {
                curF = (fina2.javascript.function.Function)funcList.getSelectedValue();
                curFun = insertFunction(curParam, curF, -1);
            } else {
                if(curIndex != -1) {
                    curF = (fina2.javascript.function.Function)funcList.getSelectedValue();
                    curFun = insertFunction(curElement, curF, curIndex);
                } else {
                    curF = (fina2.javascript.function.Function)funcList.getSelectedValue();
                    curFun = insertFunction(curF);
                }
            }
            selIndex = 1;*/
        }
    }//GEN-LAST:event_funcListMouseClicked

    private void funcListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_funcListValueChanged
        try {
            fina2.javascript.function.Function f = (fina2.javascript.function.Function) funcList
                    .getSelectedValue();
            funcDesc.setText(f.getFullDescription());
            funcDesc.setCaretPosition(0);
            //nextButton.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
            funcDesc.setText("");
        }
    }//GEN-LAST:event_funcListValueChanged

    private void catListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_catListActionPerformed
        try {
            DefaultListModel m = (DefaultListModel) funcList.getModel();
            m.removeAllElements();

            if (catList.getSelectedItem().equals(ui.getString("fina2.all"))) {
                for (Iterator cIter = fl.getCategories().iterator(); cIter
                        .hasNext();) {
                    Iterator fIter = fl.getFunctions((String) cIter.next())
                            .iterator();

                    for (; fIter.hasNext();) {
                        m.addElement(fIter.next());
                    }
                }
            } else {
                Iterator fIter = fl.getFunctions(
                        (String) catList.getSelectedItem()).iterator();

                for (Iterator iter = fIter; iter.hasNext();) {
                    m.addElement(iter.next());
                }
            }
            funcDesc.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_catListActionPerformed

    /*private void selectNode(DefaultMutableTreeNode node, Object o) {
        try {
            if(node == null) {
                DefaultTreeModel model = ((DefaultTreeModel)tree.getModel());
                node = (DefaultMutableTreeNode)model.getRoot();

            }

            if(node.getUserObject() == o) {
                tree.setSelectionPath(
                    new TreePath(node.getPath())
                );
                return;
            }
            System.out.println("fdgdfgdfgdfgdfg");
            for(Enumeration enum=node.children(); enum.hasMoreElements(); ) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode)enum.nextElement();
                //if( ((Element)n.getUserObject()).print(0).equals( ((Element)o).print(0))) {
                if(n.getUserObject() == o) {
                    tree.setSelectionPath(
                        new TreePath(n.getPath())
                    );
                    return;
                }
                System.out.println("! "+((Element)n.getUserObject()).print(0)+"="+((Element)o).print(0));
                selectNode(n, o);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }*/

    public synchronized void updateTree() {
        parseTree();
        refreshTree(true);
    }

    private synchronized void parseTree() {
        try {
            DefaultTreeModel model = ((DefaultTreeModel) tree.getModel());
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
                    .getRoot();
            DefaultMutableTreeNode node = root; //(DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
            Element e = (Element) node.getUserObject();

            //System.out.println("Source: "+sourcePart);
            Tokenizer tokenizer = new Tokenizer(sourcePart + " "); //source.getText()+" ");
            try {
                e.removeChildren();
                if (root != node)
                    tokenizer.nextToken();
                Token token = e.parse(tokenizer, null);
                if (e instanceof Keyword) {
                    if (token.getType() == Token.LEFT_SCOPE) {
                        Scope s = new Scope();
                        s.parse(tokenizer, e);
                    }
                    /*} else {
                        e.parse(tokenizer, null);*/
                }
            } catch (EOFException ex) {
            }
        } catch (Exception exx) {
            exx.printStackTrace();
        }
    }

    public void refreshTree(boolean select) {
        try {
            edit = select;
            //System.out.println("1");
            //checkSyntax();
            //System.out.println("2");
            int caret = source.getCaretPosition();

            DefaultTreeModel model = ((DefaultTreeModel) tree.getModel());
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
                    .getRoot();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getSelectionPath().getLastPathComponent();
            Element e = (Element) node.getUserObject();
            //System.out.println("3");

            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                    .getParent();
            if (parent == null) {
                node = e.getValidElement().getNode();
                model.setRoot(node);
                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                expandNode(node);
                //System.out.println("4");
            } else {
                int index = parent.getIndex(node);
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);

                node = e.getValidElement().getNode();

                ((DefaultTreeModel) tree.getModel()).insertNodeInto(node,
                        parent, index);
                expandNode(node);
                model.nodeChanged(root);
                //System.out.println("4");
            }
            tree.setSelectionPath(new TreePath(node.getPath()));
            checkSyntax();
            //if(select) {
            //    source.setCaretPosition(caret);
            //}
            //System.out.println("5");

            source.setText(getFullSource());
        } catch (Exception exx) {
            exx.printStackTrace();
        }
    }

    private void treeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeValueChanged
        try {
            //source.setText(getFullSource());
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getSelectionPath().getLastPathComponent();
            Element e = (Element) node.getUserObject();
            if (!(e instanceof fina2.javascript.element.Function)) {
                curF = null;
                curFun = null;
                //nextButton.setEnabled(false);
                paramScroll.setViewportView(new JPanel());
                funcDescTop.setText("");
            } else {
                fina2.javascript.element.Function fun = (fina2.javascript.element.Function) e;
                fina2.javascript.function.Function f = findFunction(fun);
                if (f == null) {
                    curF = null;
                    curFun = null;
                    //nextButton.setEnabled(false);
                    paramScroll.setViewportView(new JPanel());
                    funcDescTop.setText("");
                } else {
                    curF = f;
                    curFun = fun;
                    if (edit) {
                        editFunction(f, fun);
                    } else {
                        edit = true;
                    }
                    //nextButton.setEnabled(true);
                }
            }
            if (node == tree.getModel().getRoot()) {
                if (e instanceof Keyword)
                    //source.setText(e.print(1, true)); //!!!//
                    sourcePart = e.print(1, true); //!!!//
                else
                    //source.setText(e.print(0, true)); //!!!//
                    sourcePart = e.print(0, true); //!!!//
            } else {
                if (e instanceof Keyword)
                    //source.setText(e.print(1, true)); //!!!//
                    sourcePart = e.print(1, true); //!!!//
                else
                    sourcePart = e.print(-1, true); //!!!//
            }
            //source.setCaretPosition(0);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        Element e = null;
        try {
            e = findNextFunction();
            //if(((fina2.javascript.element.Function)e).getName().equals("else"))
            //    e = findNextFunction();
        } catch (Exception ex) {
        }
        if (e == null)
            nextButton.setEnabled(false);
        else
            nextButton.setEnabled(true);

        e = null;
        try {
            e = findBackFunction();
            //if(((fina2.javascript.element.Function)e).getName().equals("else"))
            //    e = findBackFunction();
        } catch (Exception ex) {
        }
        if (e == null)
            backButton.setEnabled(false);
        else
            backButton.setEnabled(true);
    }//GEN-LAST:event_treeValueChanged

    /**
     * Exit the Application
     *
     * @param evt WindowEvent
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
//        dispose();
//        refNodes = null;
//        setVisible(false);
//        dialog.setVisible(true);
        
        okButton.setEnabled(false);
        dispose();
        refNodes = null;
        setVisible(false);
        dialog.setVisible(true);
    }//GEN-LAST:event_exitForm

    public void run() {
        while (true) {
            if (source.hasFocus()) {
                //updateTree();
            }
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addItem;
    private javax.swing.JButton backButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox catList;
    private javax.swing.JMenuItem deleteItem;
    private javax.swing.JTextArea error;
    private javax.swing.JTextArea funcDesc;
    private javax.swing.JTextArea funcDescTop;
    private javax.swing.JList funcList;
    private javax.swing.JButton helpButton;
    private javax.swing.JMenuItem insertItem;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel paramPanel;
    private javax.swing.JScrollPane paramScroll;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTextArea source;
    private javax.swing.JSplitPane splitPane1;
    private javax.swing.JSplitPane splitPane2;
    private javax.swing.JSplitPane splitPane3;
    private javax.swing.JSplitPane splitPane4;
    private javax.swing.JTabbedPane tab;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables

}

class ParameterEditor implements java.awt.event.KeyListener {

    private Element element;
    private Wizard wizard;

    ParameterEditor(Wizard wizard, Element element) {
        this.wizard = wizard;
        this.element = element;
    }

    public void keyPressed(java.awt.event.KeyEvent keyEvent) {
    }

    public void keyReleased(java.awt.event.KeyEvent keyEvent) {
        try {
            element.removeChildren();
            if (((JTextComponent) keyEvent.getSource()).getText().trim()
                    .equals("")) {
                //System.out.println("111+");
                throw new EOFException();
            }

            //System.out.println("111 "+element);
            String src = null;
            if (element instanceof Scope)
                src = "{\n" + ((JTextComponent) keyEvent.getSource()).getText()
                        + "\n} ";
            else
                src = ((JTextComponent) keyEvent.getSource()).getText() + " ";
            Tokenizer tokenizer = new Tokenizer(src);
            tokenizer.nextToken();
            element.parse(tokenizer, null);
        } catch (EOFException e) {
            element = element.getValidElement();
            //System.out.println("222+");
            try {
                if (element instanceof Keyword)
                    wizard.getSourceTextArea().setText(
                            wizard.getCurFunction().print(1, true)); //!!!//
                else
                    wizard.getSourceTextArea().setText(
                            wizard.getCurFunction().print(1, true)); //!!!//
            } catch (Exception ex) {
            }
            wizard.refreshTree(false);
            //System.out.println("222++");
            ((JTextComponent) keyEvent.getSource()).grabFocus();//requestFocus();
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void keyTyped(java.awt.event.KeyEvent keyEvent) {
    }

    public Element getElement() {
        return element;
    }
}

class ParameterAddFunction implements java.awt.event.ActionListener {

    private Wizard wizard;
    private Element element;

    ParameterAddFunction(Wizard wizard, Element element) {
        this.wizard = wizard;
        this.element = element;
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        wizard.resetFuncList();
        wizard.addFunctionToParameter(element);
    }

}

class DNDTextField extends javax.swing.JTextField implements
        java.awt.dnd.DropTargetListener {

    private java.awt.dnd.DropTarget dropTarget = null;
    private ParameterEditor pEdit;

    public DNDTextField(ParameterEditor pEdit, boolean canAmmend) {
        super();

        if (canAmmend) {
            dropTarget = new java.awt.dnd.DropTarget(this, this);
        } else {
            setEditable(false);
            setFocusable(false);
        }

        this.pEdit = pEdit;
    }

    public void copy() {
        super.copy();
        pEdit.keyReleased(new java.awt.event.KeyEvent(this, 0, 0, 0, 0));
    }

    public ParameterEditor getParameterEditor() {
        return pEdit;
    }

    public Element getElement() {
        return pEdit.getElement();
    }

    public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent) {
    }

    public void dragEnter(java.awt.dnd.DropTargetDragEvent event) {
        event.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    public void drop(java.awt.dnd.DropTargetDropEvent event) {
        try {
            Transferable transferable = event.getTransferable();

            // we accept only Strings
            if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {

                event.acceptDrop(DnDConstants.ACTION_MOVE);
                String s = (String) transferable
                        .getTransferData(DataFlavor.stringFlavor);
                setText(s);
                event.getDropTargetContext().dropComplete(true);

                Thread t = new Thread() {
                    public void run() {
                        pEdit.keyReleased(new java.awt.event.KeyEvent(
                                DNDTextField.this, 0, 0L, 0, 0));
                    }
                };
                t.start();
            } else {
                event.rejectDrop();
            }
        } catch (IOException exception) {
            event.rejectDrop();
        } catch (UnsupportedFlavorException ufException) {
            event.rejectDrop();
        }
    }

    public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }

    public void dropActionChanged(
            java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }

}

class ScopeEditor extends JTextArea {
    private ParameterEditor pEdit;

    public ScopeEditor(ParameterEditor pEdit) {
        super();
        this.pEdit = pEdit;
    }

    public ParameterEditor getParameterEditor() {
        return pEdit;
    }

    public void copy() {
        super.copy();
        pEdit.keyReleased(new java.awt.event.KeyEvent(this, 0, 0, 0, 0));
    }
}
