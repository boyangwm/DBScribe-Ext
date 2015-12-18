package fina2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class ScrollableMessageBox extends JDialog {
    JPanel panel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanelOk = new JPanel();
    JButton jButtonOk = new JButton();
    JLabel jLabel = new JLabel();
    JScrollPane jScrollPane = new JScrollPane();
    JTextArea jTextArea = new JTextArea();
    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    public ScrollableMessageBox(Frame owner, String title, boolean modal,
            String nonScrollableText, String scrollableText) {
        super(owner, title, modal);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            jLabel.setText(nonScrollableText);
            jLabel.setFont(ui.getFont());
            jTextArea.setText(scrollableText);
            jTextArea.setFont(ui.getFont());
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ScrollableMessageBox() {
        this(new Frame(), "MessageBox", false, "", "");
    }

    private void jbInit() throws Exception {
        panel.setLayout(borderLayout1);
        this.getContentPane().setLayout(borderLayout2);
        jButtonOk.setText("Ok");
        jButtonOk
                .addActionListener(new ScrollableMessageBox_jButtonOk_actionAdapter(
                        this));
        jLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jLabel.setText("");
        jTextArea.setBackground(UIManager.getColor("Label.background"));
        jTextArea.setEditable(false);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        jScrollPane.setMaximumSize(new Dimension(600, 600));
        jScrollPane.setPreferredSize(new Dimension(600, 200));
        jPanelOk.add(jButtonOk);
        panel.add(jPanelOk, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(panel, java.awt.BorderLayout.SOUTH);

        this.getContentPane().add(jLabel, java.awt.BorderLayout.NORTH);
        this.getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);
        jScrollPane.getViewport().add(jTextArea);

    }

    public void jButtonOk_actionPerformed(ActionEvent e) {
        dispose();
    }
}

class ScrollableMessageBox_jButtonOk_actionAdapter implements ActionListener {
    private ScrollableMessageBox adaptee;

    ScrollableMessageBox_jButtonOk_actionAdapter(ScrollableMessageBox adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonOk_actionPerformed(e);
    }
}
