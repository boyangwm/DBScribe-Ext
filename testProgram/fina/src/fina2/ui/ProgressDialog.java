package fina2.ui;

import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import fina2.Main;

/**
 * Dialog with progress bar component 
 */
public class ProgressDialog extends JDialog {

    private JProgressBar progressBar = null;
    private JLabel messageLabel = null;

    /** Dialog title */
    private String title = null;

    /** Creates an instance of the class */
    public ProgressDialog(Frame owner, String title) {
        super(owner, title, false);

        this.title = title;

        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(owner);

            initComponents();
            pack();
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    /** Creates a new progress bar */
    public static ProgressDialog create(Frame owner, String title) {

        ProgressDialog progressDlg = new ProgressDialog(owner, title);
        progressDlg.setSize(360, 160);
        progressDlg.setVisible(true);

        return progressDlg;
    }

    /** Inits the components */
    private void initComponents() throws Exception {

        this.getContentPane().setLayout(null);
        this.setResizable(false);

        /* Progress bar */
        progressBar = new JProgressBar();
        progressBar.setBounds(new Rectangle(10, 50, 300, 16));
        this.getContentPane().add(progressBar);

        /* Message */
        messageLabel = new JLabel();
        messageLabel.setText("%MESSAGE%");
        messageLabel.setBounds(new Rectangle(10, 30, 178, 15));
        this.getContentPane().add(messageLabel);
    }

    /** Increments value of progress bar */
    public void incProgress() {
        setProgress(progressBar.getValue() + 1);
    }

    /** Sets value of progress bar */
    public void setProgress(int progress) {
        progressBar.setValue(progress);

        String titleStr = title + " - "
                + (progress * 100 / progressBar.getMaximum()) + "%";
        setTitle(titleStr);
    }

    /** Sets the max of progress bar */
    public void setMaxProgress(int maxProgress) {
        progressBar.setMaximum(maxProgress);
    }

}
