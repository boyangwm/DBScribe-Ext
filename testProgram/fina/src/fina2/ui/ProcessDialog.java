/*
 * ProcessDialog.java
 *
 * Created on 11 ������ 2002 �., 20:31
 */

package fina2.ui;

/**
 * 
 * @author David Shalamberidze
 */

public class ProcessDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	/** Creates new form ProcessDialog */
	public ProcessDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		ui.loadIcon("fina2.machine", "machine.gif");
		initComponents();
		message.setFont(ui.getFont());
		message.setIcon(ui.getIcon("fina2.machine"));
		progressBar.setVisible(false);
		System.out.println("icon " + ui.getIcon("fina2.machine"));
		setLocationRelativeTo(parent);
		pack();
	}

	public ProcessDialog(java.awt.Frame parent, boolean modal, int maxProgress) {
		super(parent, modal);
		ui.loadIcon("fina2.machine", "machine.gif");
		initComponents();
		message.setFont(ui.getFont());
		// message.setIcon(ui.getIcon("fina2.machine"));
		progressBar.setMaximum(maxProgress);
		progressBar.setValue(0);
		setLocationRelativeTo(parent);
		pack();
	}

	public void incProgress() {
		progressBar.setValue(progressBar.getValue() + 1);
	}

	public void setProgress(int progress) {
		progressBar.setValue(progress);
	}

	public void setMaxProgress(int maxProgress) {
		progressBar.setMaximum(maxProgress);
	}

	public void setMessage(String msg) {
		message.setText(msg);
	}

	public void show() {
		super.show();
		pack();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		message = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		progressBar = new javax.swing.JProgressBar();

		setResizable(false);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		message.setFont(new java.awt.Font("Dialog", 0, 11));
		message.setIcon(new javax.swing.ImageIcon(""));
		message.setText("Processing...");
		message.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(11, 11, 11, 11)));
		getContentPane().add(message, java.awt.BorderLayout.CENTER);

		jPanel1.setPreferredSize(new java.awt.Dimension(280, 24));
		progressBar.setPreferredSize(new java.awt.Dimension(270, 14));
		jPanel1.add(progressBar);

		getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

		pack();
	}// GEN-END:initComponents

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_closeDialog
		// setVisible(false);
		// dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JLabel message;
	private javax.swing.JProgressBar progressBar;
	// End of variables declaration//GEN-END:variables

}