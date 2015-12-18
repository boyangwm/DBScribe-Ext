package fina2.bank;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

@SuppressWarnings("serial")
public class HistoryDialog extends JDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private Container container;

	private JButton saveButton;
	private JButton cancelButton;

	private JPanel buttonPanel;

	private JEditorPane text;

	public HistoryDialog(java.awt.Frame parent) {
		super(parent, true);
		initComtonents();
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.save", "save.gif");
		this.setSize(300, 200);
	}

	private void initComtonents() {
		container = this.getContentPane();
		// set Title
		this.setTitle("History");

		// Dialog close Listener
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				close();
			}
		});

		StyledEditorKit editor = new StyledEditorKit();

		text = new JEditorPane();
		System.out.println(text.getEditorKit().toString());
		container.add(new JScrollPane(text), BorderLayout.CENTER);
		buttonPanel = new JPanel();

		// Ok Button
		saveButton = new JButton();
		saveButton.setIcon(ui.getIcon("fina2.save"));
		saveButton.setText(ui.getString("fina2.returns.save"));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		buttonPanel.add(saveButton);

		// close Button
		cancelButton = new JButton();
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		buttonPanel.add(cancelButton);

		container.add(buttonPanel, BorderLayout.SOUTH);
	}

	@Override
	public void show() {
		setLocationRelativeTo(getParent());
		super.show();
	}

	private void close() {
		this.dispose();
	}
}
