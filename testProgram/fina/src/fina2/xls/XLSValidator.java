package fina2.xls;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import fina2.db.DatabaseUtil;

public class XLSValidator {
	private XLSValidator() {

	}

	@SuppressWarnings("unused")
	public static boolean isXLSFile(File file) {
		boolean isXLS = true;
		try {
			FileInputStream fis = new FileInputStream(file);
			POIFSFileSystem s = new POIFSFileSystem(fis);
			HSSFWorkbook wb = new HSSFWorkbook(s);
		} catch (Exception ioex) {
			isXLS = false;
			// ioex.printStackTrace();
		}
		return isXLS;
	}

	public static boolean isXLSNameValid(String name) {
		String regEx = null;
		boolean isValid = false;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE PROP_KEY='fina2.mfb.xls.name.pattern'");
			rs = ps.executeQuery();
			while (rs.next()) {
				regEx = rs.getString("VALUE");
				Pattern pattern = Pattern.compile(regEx);
				Matcher matches = pattern.matcher(name);
				if (matches.matches()) {
					isValid = true;
					break;
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return isValid;
	}

	public static boolean isXLSProtected(File xls) {
		boolean isProtected = true;
		try {
			FileInputStream fis = new FileInputStream(xls);
			POIFSFileSystem s = new POIFSFileSystem(fis);
			HSSFWorkbook workbook = new HSSFWorkbook(s);
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				HSSFSheet sheet = workbook.getSheetAt(i);
				if (!sheet.getProtect()) {
					isProtected = false;
					break;
				}
			}
		} catch (Exception ex) {
			isProtected = false;
			// ex.printStackTrace();
		}

		return isProtected;
	}

	public static boolean isXLSProtectionPasswordValid(File xls) {
		boolean passwordValid = true;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE PROP_KEY='fina2.sheet.protection.password'");
			rs = ps.executeQuery();
			String password = null;
			if (rs.next()) {
				password = rs.getString("VALUE");
			}
			FileInputStream fis = new FileInputStream(xls);
			POIFSFileSystem s = new POIFSFileSystem(fis);
			HSSFWorkbook workbook = new HSSFWorkbook(s);

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				HSSFSheet sheet = workbook.getSheetAt(i);
				if (!((Short) sheet.getPassword()).toString().equals(password)) {
					passwordValid = false;
					break;
				}
			}
		} catch (Exception ex) {
			passwordValid = false;
			// ex.printStackTrace();
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return passwordValid;
	}
}