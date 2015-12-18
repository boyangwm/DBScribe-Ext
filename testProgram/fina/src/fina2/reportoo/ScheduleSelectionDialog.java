package fina2.reportoo;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;

import fina2.Main;
import fina2.i18n.LocaleUtil;

public class ScheduleSelectionDialog extends JDialog {

    JButton jOKButton = new JButton();
    JButton jCancelButton = new JButton();
    JCheckBox jOnDemandCheckBox = new JCheckBox();
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    DateFormatter dateFormatter = new DateFormatter(dateFormat);
    JFormattedTextField jScheduleTimeField = new JFormattedTextField(
            dateFormatter);
    boolean isOK = false;

    public ScheduleSelectionDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();

            dateFormatter.setAllowsInvalid(false);
            dateFormatter.setOverwriteMode(true);

            String pattern = LocaleUtil.getDateAndTimePattern(Main.main
                    .getLanguageHandle());
            dateFormat.applyPattern(pattern);

            pack();
        } catch (Exception ex) {
            Main.generalErrorHandler(ex);
        }
    }

    public ScheduleSelectionDialog() {
        this(null, "", false);
    }

    public void setVisible(boolean show) {

        jScheduleTimeField.requestFocusInWindow();
        super.setVisible(show);
    }

    private void jbInit() throws Exception {
        jScheduleTimeField.setBounds(new Rectangle(12, 38, 251, 24));
        jScheduleTimeField.setValue(new Date());
        jOnDemandCheckBox.setText("On demand");
        jOnDemandCheckBox.setBounds(new Rectangle(9, 7, 113, 23));
        jOnDemandCheckBox
                .addItemListener(new ScheduleSelectionDialog_jOnDemandCheckBox_itemAdapter(
                        this));
        this.getContentPane().setLayout(null);
        jOKButton.setBounds(new Rectangle(110, 77, 73, 25));
        jOKButton.setText("OK");
        jOKButton
                .addActionListener(new ScheduleSelectionDialog_jOKButton_actionAdapter(
                        this));
        jCancelButton.setBounds(new Rectangle(190, 77, 73, 25));
        jCancelButton.setText("Cancel");
        jCancelButton
                .addActionListener(new ScheduleSelectionDialog_jCancelButton_actionAdapter(
                        this));
        this.setResizable(false);
        this.setTitle("Select schedule");
        this.getContentPane().add(jOnDemandCheckBox, null);
        this.getContentPane().add(jScheduleTimeField, null);
        this.getContentPane().add(jCancelButton, null);
        this.getContentPane().add(jOKButton, null);
    }

    public static void main(String[] args) {
        ScheduleSelectionDialog dlg = new ScheduleSelectionDialog();
        dlg.setSize(280, 140);
        dlg.setVisible(true);
    }

    public void jOKButton_actionPerformed(ActionEvent e) {
        isOK = true;
        dispose();
    }

    public void jCancelButton_actionPerformed(ActionEvent e) {
        isOK = false;
        dispose();
    }

    public void jOnDemandCheckBox_itemStateChanged(ItemEvent e) {
        jScheduleTimeField.setEditable(!jOnDemandCheckBox.isSelected());
    }

    public boolean isOK() {
        return isOK;
    }

    public Date getScheduleTime() {
        return (Date) jScheduleTimeField.getValue();
    }

    public boolean isOnDemand() {
        return jOnDemandCheckBox.isSelected();
    }
}

class ScheduleSelectionDialog_jOnDemandCheckBox_itemAdapter implements
        ItemListener {
    private ScheduleSelectionDialog adaptee;

    ScheduleSelectionDialog_jOnDemandCheckBox_itemAdapter(
            ScheduleSelectionDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void itemStateChanged(ItemEvent e) {
        adaptee.jOnDemandCheckBox_itemStateChanged(e);
    }
}

class ScheduleSelectionDialog_jCancelButton_actionAdapter implements
        ActionListener {
    private ScheduleSelectionDialog adaptee;

    ScheduleSelectionDialog_jCancelButton_actionAdapter(
            ScheduleSelectionDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jCancelButton_actionPerformed(e);
    }
}

class ScheduleSelectionDialog_jOKButton_actionAdapter implements ActionListener {
    private ScheduleSelectionDialog adaptee;

    ScheduleSelectionDialog_jOKButton_actionAdapter(
            ScheduleSelectionDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jOKButton_actionPerformed(e);
    }
}
