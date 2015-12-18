package fina2.returns;

import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

@SuppressWarnings("serial")
public class Package2WorkBookProgressDialog extends JDialog {

	public Package2WorkBookProgressDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		jbInit();
		pack();
		setLocationRelativeTo(owner);
	}

	public void setMessage(String message) {
		messageLabel.setText(message);
	}

	public void setTitleMessage(String msg) {
		titleMessageLabel.setText(msg);
	}

	public void setTotalNumber(int number) {
		totalNumber = number;
		totalProgressBar.setMaximum(number);
		updateTotalMessage();
	}

	public void setCurrentNumber(int number) {
		currentNumber = number;
		totalProgressBar.setValue(number);
		updateTotalMessage();
	}

	private void updateTotalMessage() {
		StringBuffer buff = new StringBuffer();
		buff.append("Total ");
		buff.append(currentNumber);
		buff.append(" of ");
		buff.append(totalNumber);

		totalMessageLabel.setText(buff.toString());
	}

	private void jbInit() {
		this.getContentPane().setLayout(null);
		totalProgressBar.setBounds(new Rectangle(10, 50, 300, 16));
		this.setResizable(false);
		titleMessageLabel.setText("%REPORT_NAME%");
		titleMessageLabel.setBounds(new Rectangle(10, 10, 308, 16));
		messageLabel.setText("%MESSAGE%");
		messageLabel.setBounds(new Rectangle(10, 30, 178, 15));
		totalMessageLabel.setText("%TOTAL_MESSAGE%");
		totalMessageLabel.setBounds(new Rectangle(10, 71, 178, 15));
		this.getContentPane().add(titleMessageLabel);
		this.getContentPane().add(messageLabel);
		this.getContentPane().add(totalProgressBar);

		this.getContentPane().add(totalMessageLabel);
	}

	JProgressBar totalProgressBar = new JProgressBar();
	JLabel titleMessageLabel = new JLabel();
	JLabel messageLabel = new JLabel();
	JLabel totalMessageLabel = new JLabel();

	private int totalNumber;
	private int currentNumber;
}
