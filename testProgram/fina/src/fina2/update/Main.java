package fina2.update;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.swing.JOptionPane;

public class Main {
	private final static String TEMP_DIR = "./lib/temp";
	private final static String GUI_FILE_NAME = "fina-gui.jar";
	private final static String GUI_FILE_LOCATION = "./lib/fina-gui.jar";

	public static void main(String[] args) {
		File temp = new File(TEMP_DIR);
		File srcFile = new File(temp, GUI_FILE_NAME);
		File oldFile = new File(GUI_FILE_LOCATION);

		File backupFile = new File(new File("./lib"), "fina-gui.bak");
		oldFile.renameTo(backupFile);

		try {
			copyFile(srcFile, oldFile);

			// Delete temp files
			srcFile.delete();
			temp.delete();

			Runtime run = Runtime.getRuntime();
			run.exec(createCmdString());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(),
					"Error Message", 0);
			ex.printStackTrace();
		}
	}

	private synchronized static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	private static String[] createCmdString() {
		String[] cmd = new String[5];
		cmd[0] = "cmd.exe";
		cmd[1] = "/C";
		cmd[2] = "start";
		cmd[3] = "/min";
		cmd[4] = "run.bat";
		return cmd;
	}
}
