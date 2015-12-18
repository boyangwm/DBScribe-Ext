package fina2.returns;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.returns.jaxb.RETURN;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.security.UserPK;
import fina2.ui.table.TableRowImpl;

public class ImportManagerSessionBean implements SessionBean {

	private static Logger log = Logger.getLogger(ImportManagerSessionBean.class);

	private static Schema schema;

	private SessionContext ctx;

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
	}

	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void setSessionContext(javax.ejb.SessionContext sessionContext) throws EJBException, RemoteException {
		ctx = sessionContext;
	}

	public Collection getImporterUsers(Handle userHandle, Handle languageHandle) throws RemoteException {

		Vector users = new Vector(); // The result list

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;

		User user = (User) userHandle.getEJBObject();
		TableRowImpl row = new TableRowImpl(((UserPK) user.getPrimaryKey()).getId(), 1);
		if (user.getName(languageHandle) != null && !"".equals(user.getName(languageHandle))) {
			// row.setValue(0, user.getName(languageHandle) + "(" +
			// user.getLogin() + ")");
			row.setValue(0, user.getLogin());
		} else {
			row.setValue(0, user.getLogin());
		}
		users.add(row);

		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("select distinct u.id, u.login, s.value from IN_IMPORTED_RETURNS i, SYS_USERS u, SYS_STRINGS s "
					+ "where u.id = i.userId and s.id = u.nameStrId and s.langid = ? and u.id <> ?");

			Language lang = (Language) languageHandle.getEJBObject();
			ps.setInt(1, ((LanguagePK) lang.getPrimaryKey()).getId());
			ps.setInt(2, ((UserPK) user.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			// Copying the result set to the result list
			while (rs.next()) {
				int id = rs.getInt(1);
				String login = rs.getString(2).trim();
				String name = rs.getString(3).trim();

				row = new TableRowImpl(id, 1);
				if (name != null && !"".equals(name)) {
					// row.setValue(0, name + "(" + login + ")");
					row.setValue(0, login);
				} else {
					row.setValue(0, login);
				}
				// Adding to the result list
				users.add(row);
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		// The result list
		return users;
	}

	public Collection getImportedDocuments(Handle userHandle, Handle languageHandle, Set<Integer> bankIdSet, String code, int status, Date fromDate, Date toDate, Date importedAfter, String userId,
			String versionCode, int maxReturnsCount, boolean isFISelected) throws RemoteException {
		log.info("Loading returns for return manager");
		// ---------------------------------------------------------------------
		// Filters for result query
		String returnFilter = " ";
		String statusFilter = " ";
		String fromDateFilter = " ";
		String toDateFilter = " ";
		String afterImportFilter = " ";
		String userFilter = " ";
		String versionFilter = " ";

		Language lang = (Language) languageHandle.getEJBObject();
		int langId = ((LanguagePK) lang.getPrimaryKey()).getId();

		// Return filter
		if ((code != null) && (!code.trim().equalsIgnoreCase("ALL"))) {
			returnFilter = " and i.returnCode=\'" + code.trim() + "\' ";
		}

		// Status filter
		if (status != -1) {
			statusFilter = " and i.status=" + status + " ";
		}

		// FromDate filter
		if (!fromDate.equals(new Date(0L))) {
			fromDateFilter = " and i.periodStart >= ? ";
		}

		// ToDate filter
		if (!toDate.equals(new Date(0L))) {
			toDateFilter = " and i.periodEnd <= ? ";
		}

		// ImportedAfter filter
		if (!importedAfter.equals(new Date(0L))) {
			afterImportFilter = " and i.uploadTime >= ? ";
		}

		// userLogin
		if (!userId.trim().equalsIgnoreCase("ALL")) {
			userFilter = " and i.userId=\'" + userId.trim() + "\' ";
		}

		// Return version filter
		if ((versionCode != null) && (!versionCode.trim().equalsIgnoreCase("ALL"))) {
			versionFilter = " and i.versionCode=\'" + versionCode.trim() + "\' ";
		}

		String banksSql = "";
		// ----------------------------------------------------------------
		// Banks
		if (bankIdSet != null && bankIdSet.size() > 0) {
			// There is bank list
			// Creating string to contain bank id list
			String bankIdList = "";

			for (Integer bankId : bankIdSet) {
				if (!bankIdList.equals("")) {
					bankIdList = bankIdList + ", ";
				}
				bankIdList = bankIdList + bankId;
			}

			// For sql WHERE
			banksSql = "b.id in (" + bankIdList + ") and ";
		}

		// ---------------------------------------------------------------------
		// The result query

		String sql = "";
		if (isFISelected) {
			sql = "select distinct i.id, i.returnCode, i.bankCode, i.periodStart, i.periodEnd, "
					+ "i.importStart, i.importEnd, i.status, i.versionCode, u.login, s.value, i.message, i.uploadTime,i.type as TYPE,i.xlsId as XLSID, u.email as USER_EMAIL "
					+ "from IN_IMPORTED_RETURNS i, IN_BANKS b, SYS_USERS u, SYS_STRINGS s where " + banksSql + " rtrim(b.code) = i.bankCode and u.id = i.userId and s.id = u.nameStrId and s.langid = "
					+ langId + returnFilter + statusFilter + fromDateFilter + toDateFilter + afterImportFilter + userFilter + versionFilter + "  order by i.returnCode, i.versionCode";
		} else {
			sql = "select distinct i.id, i.returnCode, i.bankCode, i.periodStart, i.periodEnd, "
					+ "i.importStart, i.importEnd, i.status, i.versionCode, u.login, s.value, i.message, i.uploadTime ,i.type as TYPE,i.xlsId as XLSID, u.email as USER_EMAIL "
					+ "from SYS_USERS u, SYS_STRINGS s,IN_IMPORTED_RETURNS i LEFT OUTER JOIN IN_BANKS b on rtrim(b.code) = i.bankCode where " + banksSql
					+ " u.id = i.userId and s.id = u.nameStrId and s.langid = " + langId + returnFilter + statusFilter + fromDateFilter + toDateFilter + afterImportFilter + userFilter + versionFilter
					+ "   order by i.returnCode, i.versionCode";
		}

		// ---------------------------------------------------------------------
		// Processing the result
		Vector<TableRowImpl> returns = new Vector<TableRowImpl>(); // The result
																	// list

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			con = DatabaseUtil.getConnection();

			ps = con.prepareStatement(sql);

			int paramIdx = 1;
			// Setting fromDate filter value
			if (!fromDate.equals(new Date(0L))) {
				ps.setDate(paramIdx++, new java.sql.Date(fromDate.getTime()));
			}
			// Setting toDate filter value
			if (!toDate.equals(new Date(0L))) {
				ps.setDate(paramIdx++, new java.sql.Date(toDate.getTime() + 3600000L * 12));
			}
			// Setting importedAfter filter value
			if (!importedAfter.equals(new Date(0L))) {
				ps.setTimestamp(paramIdx++, new java.sql.Timestamp(importedAfter.getTime()));
			}

			rs = ps.executeQuery();

			SimpleDateFormat dateFormat = new SimpleDateFormat(lang.getDateFormat());
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat(lang.getDateFormat() + " HH:mm:ss");
			int rowCount = 0;

			// Copying the result set to the result list

			while (rs.next() && (rowCount < maxReturnsCount)) {
				int id = rs.getInt(1);
				String returnCode = (rs.getString(2) != null) ? rs.getString(2).trim() : "#N/A";
				String bankCodeStr = rs.getString(3).trim();
				String periodStart = dateFormat.format(rs.getDate(4));
				String periodEnd = dateFormat.format(rs.getDate(5));
				String importStart = "";
				if (rs.getTimestamp(6) != null) {
					importStart = dateTimeFormat.format(rs.getTimestamp(6));
				}
				String importEnd = "";
				if (rs.getTimestamp(7) != null) {
					importEnd = dateTimeFormat.format(rs.getTimestamp(7));
				}
				String statusStr = ImportStatus.values()[rs.getInt(8)].getCode();
				String versionCodeStr = rs.getString(9);
				String userLoginStr = rs.getString(10);
				String userEmail = rs.getString("USER_EMAIL");
				String userNameStr = rs.getString(11);
				String messageStr = rs.getString(12);
				String uploadTime = dateTimeFormat.format(rs.getTimestamp(13));
				Long xlsId = rs.getLong("XLSID");
				int t = rs.getInt("TYPE");
				String type = null;
				if (t == 1) {
					type = "DCS";
				} else if (t == 2) {
					type = "Mail";
				} else {
					type = "Manuall";
				}

				String userStr = null;
				if (userNameStr != null && !"".equals(userNameStr)) {
					// userStr = userNameStr + "(" + userLoginStr + ")";
					userStr = userLoginStr;
				} else {
					userStr = "#N/A";
				}

				TableRowImpl row = new TableRowImpl(id, 14);
				row.setValue(0, returnCode + " [" + type + "]");
				row.setValue(1, periodStart);
				row.setValue(2, periodEnd);
				row.setValue(3, bankCodeStr);
				row.setValue(4, versionCodeStr);
				row.setValue(5, userStr);
				row.setValue(6, statusStr);
				row.setValue(7, importStart);
				row.setValue(8, importEnd);
				row.setValue(9, messageStr);
				row.setValue(10, uploadTime);
				row.setValue(11, type);
				if (xlsId != null)
					row.setValue(12, userStr + " | " + ((userEmail == null || userEmail.trim().equals("")) ? "#N/A" : userEmail.trim()));
				row.setValue(13, rs.getInt(1) + "");
				// Adding to the result list
				returns.add(row);
				rowCount++;
			}

			log.info("Returns loaded");
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		// The result list
		return returns;
	}

	public void deleteUploadedDocuments(Collection docIds) throws RemoteException {

		Connection con = DatabaseUtil.getConnection();
		try {
			PreparedStatement del = con.prepareStatement("delete from IN_IMPORTED_RETURNS where id = ?");
			for (Integer id : (Collection<Integer>) docIds) {
				del.setInt(1, id);
				del.executeUpdate();
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public Date uploadImportedDocuments(Handle userHandle, Handle languageHandle, LinkedList<byte[]> xmls) throws RemoteException {
		Date uploadTime = new Date();
		uploadDocuments(userHandle, languageHandle, xmls, uploadTime);
		return uploadTime;
	}

	public List<Integer> importedUploadDocuments(Handle userHandle, Handle languageHandle, LinkedList<byte[]> xmls) throws RemoteException {
		Date uploadTime = new Date();
		return uploadDocuments(userHandle, languageHandle, xmls, uploadTime);
	}

	private List<Integer> uploadDocuments(Handle userHandle, Handle languageHandle, LinkedList<byte[]> xmls, Date uploadTime) throws RemoteException {

		Connection con = null;
		PreparedStatement insert = null;
		List<Integer> result = new LinkedList<Integer>();

		try {
			con = DatabaseUtil.getConnection();

			Language lang = (Language) languageHandle.getEJBObject();
			User user = (User) userHandle.getEJBObject();

			int id = getImportedReturnMaxId(con);

			insert = con.prepareStatement("insert into IN_IMPORTED_RETURNS (id, returnCode, bankCode, versionCode, " + "periodStart, periodEnd, userId, langId, uploadTime, status, content, message) "
					+ "values(?,?,?,?,?,?,?,?,?,?,?,?)");

			HashMap<String, Language> langs = new LinkedHashMap<String, Language>();

			for (int i = 0; i < xmls.size(); i++) {
				RETURN ret = readReturn(xmls.get(i));

				Language retLang = langs.get(ret.getHEADER().getLNG());
				if (retLang == null) {
					retLang = getLanguage(ret.getHEADER().getLNG());
					langs.put(ret.getHEADER().getLNG(), lang);
				}
				if (uploadTime == null) {
					uploadTime = new Date();
				}

				SimpleDateFormat df = new SimpleDateFormat(retLang.getDateFormat());
				log.debug("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + ret.getHEADER().getRETURNCODE());
				insert.setInt(1, ++id);
				result.add(id);
				insert.setString(2, ret.getHEADER().getRETURNCODE());
				insert.setString(3, ret.getHEADER().getBANKCODE());
				insert.setString(4, ret.getHEADER().getVER());
				insert.setDate(5, new java.sql.Date(df.parse(ret.getHEADER().getPERIODFROM()).getTime()));
				insert.setDate(6, new java.sql.Date(df.parse(ret.getHEADER().getPERIODEND()).getTime()));
				insert.setInt(7, ((UserPK) user.getPrimaryKey()).getId());
				insert.setInt(8, ((LanguagePK) retLang.getPrimaryKey()).getId());
				insert.setTimestamp(9, new java.sql.Timestamp(uploadTime.getTime()));
				insert.setInt(10, ImportStatus.UPLOADED.ordinal());
				DatabaseUtil.setBlob(insert, xmls.get(i), 11);
				insert.setString(12, "Uploaded");

				insert.execute();
				// insert.addBatch();
			}

			// insert.executeBatch();
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeStatement(insert);
			DatabaseUtil.closeConnection(con);
		}

		return result;
	}

	public void resetStatuses() throws RemoteException {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("update IN_IMPORTED_RETURNS set status = " + ImportStatus.UPLOADED.ordinal() + " where status = " + ImportStatus.IN_PROGRESS.ordinal());

			ps.executeUpdate();
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
	}

	public LinkedList<ImportedReturn> getReturnsToImport() throws RemoteException {

		LinkedList<ImportedReturn> returns = new LinkedList<ImportedReturn>();
		Connection con = DatabaseUtil.getConnection();
		try {
			loadReturnsToImport(con, false, returns);
			loadReturnsToImport(con, true, returns);
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
		}

		return returns;
	}

	public void started(int id) throws RemoteException {
		changeStatus(id, ImportStatus.IN_PROGRESS, "Processing started");
	}

	public void rejected(int id, String message) throws RemoteException {
		changeStatus(id, ImportStatus.REJECTED, message);
	}

	public void queued(int id, String message) throws RemoteException {
		changeStatus(id, ImportStatus.QUEUED, message);
	}

	public void declined(int id, String message) throws RemoteException {
		changeStatus(id, ImportStatus.DECLINED, message);
	}

	public void errors(int id, String message) throws RemoteException {
		changeStatus(id, ImportStatus.ERRORS, message);
	}

	public void imported(int id, String message) throws RemoteException {
		changeStatus(id, ImportStatus.IMPORTED, message);
	}

	private void changeStatus(int id, ImportStatus status, String message) {

		StringBuffer sql = new StringBuffer();
		sql.append("update IN_IMPORTED_RETURNS set status = ?, message = ?, ");
		if (status == ImportStatus.IN_PROGRESS) {
			sql.append("importStart = ?, importEnd = null");
		} else {
			sql.append("importEnd = ?");
		}
		sql.append(" where id = ?");

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement(sql.toString());

			ps.setInt(1, status.ordinal());
			if (message.length() > 4000)
				ps.setString(2, message.substring(0, 3900) + "...");
			else
				ps.setString(2, message);
			ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
			ps.setInt(4, id);

			ps.executeUpdate();
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
	}

	private void loadReturnsToImport(Connection con, boolean queued, LinkedList<ImportedReturn> returns) throws Exception {

		StringBuffer sql = new StringBuffer();
		sql.append("select i.id, i.userId, i.langId, i.versionCode, i.content, i.status, i.bankCode");
		sql.append(" from IN_IMPORTED_RETURNS i where i.status = ");
		if (queued == true) {
			sql.append(ImportStatus.QUEUED.ordinal()).append(" order by i.importEnd");
		} else {
			sql.append(ImportStatus.UPLOADED.ordinal()).append(" order by i.importStart");
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ImportedReturn ret = new ImportedReturn();
				ret.setId(rs.getInt(1));
				ret.setUserHandle(getUser(rs.getInt(2)).getHandle());
				ret.setLanguageHandle(getLanguage(rs.getInt(3)).getHandle());
				ret.setVersion(rs.getString(4));
				ret.setXml(DatabaseUtil.getBlob(rs, 5));
				ret.setStatus(ImportStatus.values()[rs.getInt(6)]);
				ret.setBankCode(rs.getString(7));

				returns.add(ret);
			}
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
	}

	private int getImportedReturnMaxId(Connection con) throws Exception {

		int maxId = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement("select max(id) from IN_IMPORTED_RETURNS");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				maxId = rs.getInt(1);
			}
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(pstmt);
		}
		return maxId;
	}

	private Language getLanguage(String code) throws Exception {
		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/i18n/Language");
		LanguageHome home = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
		return home.findByCode(code);
	}

	private Language getLanguage(int langId) throws Exception {
		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/i18n/Language");
		LanguageHome home = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
		return home.findByPrimaryKey(new LanguagePK(langId));
	}

	private User getUser(int userId) throws Exception {
		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/security/User");
		UserHome home = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);
		return home.findByPrimaryKey(new UserPK(userId));
	}

	private RETURN readReturn(byte[] xmls) throws EJBException {
		try {
			JAXBContext jaxbCtx = JAXBContext.newInstance(RETURN.class);
			Unmarshaller um = jaxbCtx.createUnmarshaller();
			um.setSchema(getSchema());

			ReturnXMLValidator retXmlVal = new ReturnXMLValidator();
			um.setEventHandler(retXmlVal);

			RETURN ret = (RETURN) um.unmarshal(new ByteArrayInputStream(xmls));
			if (!retXmlVal.isValid()) {
				throw new EJBException("Invalid Return XML. " + retXmlVal.getValidationErrorMessage());
			}
			return ret;
		} catch (Exception ex) {
			if (ex instanceof javax.xml.bind.UnmarshalException) {

				javax.xml.bind.UnmarshalException uex = (javax.xml.bind.UnmarshalException) ex;

				org.xml.sax.SAXParseException e = (org.xml.sax.SAXParseException) uex.getLinkedException();

				String exceptionString = "";
				exceptionString += "Line:  " + e.getLineNumber();
				exceptionString += ", Column: " + e.getColumnNumber();
				exceptionString += ", Message: " + e.getMessage();

				throw new EJBException("Error occured reading XML, Exception: " + exceptionString, ex);

			} else {
				throw new EJBException("Error occured reading XML", ex);
			}
		}
	}

	private Schema getSchema() throws SAXException {
		if (schema == null) {
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schema = sf.newSchema(getClass().getResource("/fina2/returns/fina-returns.xsd"));
		}
		return schema;
	}
}
