package fina2.i18n;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import fina2.db.DatabaseUtil;

public class LocaleUtil {

	private static LanguagePK lpk;
	private static String encoding = null;
	private static Language lang = null;

	/** Creates new LocaleUtil */
	public LocaleUtil() {
	}

	public static int allocateString(Connection con) throws SQLException {
		PreparedStatement pstmt = con.prepareStatement("select max(id) from SYS_STRINGS");

		ResultSet rs = pstmt.executeQuery();
		rs.next();

		int id = rs.getInt(1) + 1;
		rs.close();
		pstmt.close();

		pstmt = con.prepareStatement("insert into SYS_STRINGS (id,langID,value) values(?,?,?)");

		pstmt.setInt(1, id);
		pstmt.setInt(2, 1);
		pstmt.setString(3, "NONAME");

		pstmt.executeUpdate();

		pstmt.close();

		return id;
	}

	public static void setString(Connection con, LanguagePK langPK, int strID, String string) throws SQLException {

		PreparedStatement pstmt = con.prepareStatement("select id from SYS_STRINGS where langID=? and id=?");

		if ((lpk == null) || (!lpk.equals(langPK)) || (encoding == null)) {
			try {
				InitialContext jndi = new InitialContext();
				Object ref = jndi.lookup("fina2/i18n/Language");
				LanguageHome home = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
				lang = home.findByPrimaryKey(langPK);
				encoding = lang.getXmlEncoding();
				lpk = langPK;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		pstmt.setInt(1, langPK.getId());
		pstmt.setInt(2, strID);

		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			rs.close();
			pstmt.close();
			pstmt = con.prepareStatement("update SYS_STRINGS set value=? where id=? and langID=?");

			try {
				String s = string;

				if (!DatabaseUtil.isOracle()) {
					byte[] buf = string.getBytes(encoding);
					s = new String(buf, 0);
				}

				if (string.equals(""))
					pstmt.setString(1, " ");
				else
					pstmt.setString(1, s); // string);
			} catch (Exception e) {
				e.printStackTrace();
			}

			pstmt.setInt(2, strID);
			pstmt.setInt(3, langPK.getId());

			pstmt.executeUpdate();
		} else {
			rs.close();
			pstmt.close();
			pstmt = con.prepareStatement("insert into SYS_STRINGS (value,id,langID) " + "values(?,?,?)");

			try {
				String s = string;
				if (!DatabaseUtil.isOracle()) {
					byte[] buf = string.getBytes(encoding);
					s = new String(buf, 0);
				}
				if (string.equals(""))
					pstmt.setString(1, " ");
				else
					pstmt.setString(1, s); // string);
				pstmt.setInt(2, strID);
				pstmt.setInt(3, langPK.getId());

				pstmt.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		pstmt.close();
	}

	
	
	/** Returns language specific string from DB */
	public static String getString(Connection con, Handle languageHandle, int strID) throws SQLException, RemoteException {

		LanguagePK langPK = LocaleUtil.getLanguagePK(languageHandle);
		return LocaleUtil.getString(con, langPK, strID);
	}

	public static String getString(Connection con, LanguagePK langPK, int strID) throws SQLException {

		if (lpk == null || !lpk.equals(langPK) || encoding == null) {
			try {
				InitialContext jndi = new InitialContext();
				Object ref = jndi.lookup("fina2/i18n/Language");
				LanguageHome home = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
				lang = home.findByPrimaryKey(langPK);
				encoding = lang.getXmlEncoding();
				lpk = langPK;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		PreparedStatement pstmt = con.prepareStatement("select value, langID from SYS_STRINGS where (langID=?) and id=? order by langID DESC");

		pstmt.setInt(1, langPK.getId());
		pstmt.setInt(2, strID);

		ResultSet rs = pstmt.executeQuery();

		String s = "";
		if (rs.next()) {
			s = rs.getString(1);
		}

		if (s == null)
			s = "";
		else
			s = s.trim();

		rs.close();
		pstmt.close();

		if (DatabaseUtil.isOracle()) {
			return s;
		}

		try {
			byte[] buf = new byte[s.length()];
			s.getBytes(0, s.length(), buf, 0); // "windows-1251");

			return new String(buf, encoding);

		} catch (Exception e) {
			e.printStackTrace();
			return s;
		}
	}

	public static String getString(Connection con, int langId, long strID) throws SQLException {

		
		PreparedStatement pstmt = con.prepareStatement("select value from SYS_STRINGS where (langID=?) and id=?");

		pstmt.setInt(1, langId);
		pstmt.setLong(2, strID);

		ResultSet rs = pstmt.executeQuery();

		String s = "";
		if (rs.next()) {
			s = rs.getString(1);
		}

		if (s == null)
			s = "";
		else
			s = s.trim();

		rs.close();
		pstmt.close();

		if (DatabaseUtil.isOracle()) {
			return s;
		}

		try {
			byte[] buf = new byte[s.length()];
			s.getBytes(0, s.length(), buf, 0); // "windows-1251");

			return new String(buf, encoding);

		} catch (Exception e) {
			e.printStackTrace();
			return s;
		}
	}
	
	public static String encode(String s, String encoding) {
		if((s==null)||(s.trim().length()==0))
			return "";
		if (DatabaseUtil.isOracle())
			return s;
		try {
			byte[] buf = new byte[s.length()];
			s.getBytes(0, s.length(), buf, 0); // "windows-1251");
			return new String(buf, encoding);
		} catch (Exception e) {
			e.printStackTrace();
			return s;
		}
	}

	public static String date2string(Handle languageHandle, Date date) throws RemoteException {

		return date2string((Language) languageHandle.getEJBObject(), date);
	}

	public static String date2string(Language lang, Date date) throws RemoteException {

		SimpleDateFormat format = new SimpleDateFormat(lang.getDateFormat());
		format.applyPattern("dd/MM/yyyy");

		return format.format(date);
	}

	public static String time2string(Handle languageHandle, Date date) throws RemoteException {

		return time2string((Language) languageHandle.getEJBObject(), date);
	}

	public static String time2string(Language lang, Date date) throws RemoteException {
		SimpleDateFormat format = new SimpleDateFormat(lang.getDateFormat());
		format.applyPattern("dd/MM/yyyy HH:mm");

		return format.format(date);

	}

	public static Date string2date(Handle languageHandle, String string) throws RemoteException, ParseException {

		SimpleDateFormat format = new SimpleDateFormat();
		format.applyPattern("dd/MM/yyyy");

		return format.parse(string);
	}

	public static Date string2time(Handle languageHandle, String string) throws RemoteException, ParseException {

		SimpleDateFormat format = new SimpleDateFormat();
		format.applyPattern("dd/MM/yyyy HH:mm");

		return format.parse(string);
	}

	public static String number2string(Handle languageHandle, double num) throws RemoteException {

		Language lang = (Language) languageHandle.getEJBObject();
		DecimalFormat format = new DecimalFormat(lang.getNumberFormat());

		return format.format(num);
	}

	public static double string2number(Handle languageHandle, String string) throws RemoteException, ParseException {

		Language lang = (Language) languageHandle.getEJBObject();
		DecimalFormat format = new DecimalFormat(lang.getNumberFormat());

		return format.parse(string).doubleValue();
	}

	public static String getDateAndTimePattern(Handle languageHandle) throws RemoteException {

		return getDatePattern(languageHandle) + " HH:mm";
	}

	public static String getDatePattern(Handle languageHandle) throws RemoteException {

		Language lang = (Language) languageHandle.getEJBObject();
		return lang.getDateFormat();
	}

	public static String getSysString(Handle langHandle, int strId) throws RemoteException, EJBException {

		String result = null;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();

		try {
			result = LocaleUtil.getString(con, langPK, strId);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try {
				con.close();
			} catch (SQLException ex) {
			}
		}

		return result;
	}

	public static void setSysString(Handle langHandle, int strId, String value) throws RemoteException, EJBException {

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();

		try {
			LocaleUtil.setString(con, langPK, strId, value);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try {
				con.close();
			} catch (SQLException ex) {
			}
		}
	}

	/** Returns a language id from given handle */
	public static int getLanguageId(Handle languageHandle) throws RemoteException {
		Language lang = (Language) languageHandle.getEJBObject();
		return ((LanguagePK) lang.getPrimaryKey()).getId();
	}

	/** Returns a language encoding from given handle */
	public static String getEncoding(Handle languageHandle) throws RemoteException {
		Language lang = (Language) languageHandle.getEJBObject();
		return lang.getXmlEncoding();
	}

	/** Returns a language PK from a given handle */
	public static LanguagePK getLanguagePK(Handle languageHandle) throws RemoteException {
		Language lang = (Language) languageHandle.getEJBObject();
		return (LanguagePK) lang.getPrimaryKey();
	}
}
