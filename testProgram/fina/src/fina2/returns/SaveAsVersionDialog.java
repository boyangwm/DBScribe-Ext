package fina2.returns;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import fina2.Main;

public class SaveAsVersionDialog extends JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;
    private boolean isOk = false;
    boolean packageSaveAs = false;
    private String bankCode;
    private Date endDate;
    private String returnTypeCode;
    private String currentVersion;
    private ReturnPK returnPK;

    public SaveAsVersionDialog(Frame owner) {
        super(owner, true);

        ui.loadIcon("fina2.ok", "ok.gif");
        ui.loadIcon("fina2.cancel", "cancel.gif");

        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void show(String bankCode, Date endDate, String returnTypeCode,
            String currentVersion) {

        this.packageSaveAs = true;
        this.bankCode = bankCode;
        this.endDate = endDate;
        this.returnTypeCode = returnTypeCode;
        this.currentVersion = currentVersion;

        setTitle(ui.getString("fina2.returns.packageSaveAs"));
        initDialog();

        setVisible(true);
    }

    public void show(ReturnPK returnPK, String currentVersion) {

        this.packageSaveAs = false;
        this.returnPK = returnPK;
        this.currentVersion = currentVersion;

        setTitle(ui.getString("fina2.returns.returnSaveAs"));
        initDialog();

        setVisible(true);
    }

    public void initDialog() {
        loadVersions();

        setLocationRelativeTo(getParent());
    }

    private void jbInit() throws Exception {

        this.getContentPane().setLayout(borderLayout1);

        jVersionPanel.setLayout(flowLayout1);

        jVersionLabel.setText(ui.getString("fina2.returns.versionCode"));
        jVersionLabel.setFont(ui.getFont());

        jNotePanel.setBorder(border2);
        jNotePanel.setPreferredSize(new Dimension(300, 200));
        jNotePanel.setLayout(borderLayout2);

        okButton.setText(ui.getString("fina2.ok"));
        okButton.setFont(ui.getFont());
        okButton.setIcon(ui.getIcon("fina2.ok"));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButton_actionPerformed(e);
            }
        });

        cancelButton.setText(ui.getString("fina2.cancel"));
        cancelButton.setFont(ui.getFont());
        cancelButton.setIcon(ui.getIcon("fina2.cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton_actionPerformed(e);
            }
        });
        jNotesArea.setBorder(BorderFactory.createLoweredBevelBorder());

        this.getContentPane().add(jNotePanel, java.awt.BorderLayout.CENTER);

        jNotesArea.setFont(ui.getFont());
        jNotePanel.add(jNotesArea, java.awt.BorderLayout.CENTER);
        jVersionPanel.add(jVersionLabel);
        jVersionPanel.add(versionCombo);
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(okButton);
        jPanel1.add(cancelButton);

        this.getContentPane().add(jVersionPanel, java.awt.BorderLayout.NORTH);
    }

    private void loadVersions() {
        try {
            InitialContext jndi = main.getJndiContext();
            Object ref = jndi.lookup("fina2/returns/ReturnVersionSession");
            ReturnVersionSessionHome home = (ReturnVersionSessionHome) PortableRemoteObject
                    .narrow(ref, ReturnVersionSessionHome.class);
            ReturnVersionSession session = home.create();

            Collection versions = session.getReturnVersions(main
                    .getLanguageHandle(), main.getUserHandle());

            Vector versionCodes = new Vector();

            for (Iterator iter = versions.iterator(); iter.hasNext();) {
                ReturnVersion rv = (ReturnVersion) iter.next();
                if (!currentVersion.equals(rv.getCode())) {
                    versionCodes.add(rv.getCode());
                }
            }

            versionCombo.setModel(new javax.swing.DefaultComboBoxModel(
                    versionCodes));
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jVersionPanel = new JPanel();
    JPanel jNotePanel = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JLabel jVersionLabel = new JLabel();
    JComboBox versionCombo = new JComboBox();
    Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(
            148, 145, 140));
    Border border2 = new TitledBorder(border1, "Notes");
    BorderLayout borderLayout2 = new BorderLayout();
    JTextArea jNotesArea = new JTextArea();
    JPanel jPanel1 = new JPanel();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();

    public void cancelButton_actionPerformed(ActionEvent e) {
        dispose();
    }

    public void okButton_actionPerformed(ActionEvent e) {

        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/returns/ReturnSession");
            ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject
                    .narrow(ref, ReturnSessionHome.class);
            ReturnSession session = home.create();

            if (this.packageSaveAs) {
                if (session.packageExists(bankCode, endDate, returnTypeCode,
                        getVersionCode())) {
                    if (ui.showConfirmDialog(this, ui
                            .getString("fina2.returns.packageSaveAs"), ui
                            .getString("fina2.returns.packageAlreadyExists"),
                            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            } else {
                if (session.returnExists(returnPK, getVersionCode())) {
                    if (ui.showConfirmDialog(this, ui
                            .getString("fina2.returns.returnSaveAs"), ui
                            .getString("fina2.returns.returnAlreadyExists"),
                            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            Main.generalErrorHandler(ex);
        }

        isOk = true;
        dispose();
    }

    public boolean isOk() {
        return isOk;
    }

    public String getNotes() {
        return jNotesArea.getText();
    }

    public String getVersionCode() {
        return versionCombo.getSelectedItem().toString();
    }
}
