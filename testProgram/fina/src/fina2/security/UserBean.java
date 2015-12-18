/*
 * UserBean.java
 *
 * Created on October 16, 2001, 11:43 AM
 */
package fina2.security;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.bank.BankPK;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.system.PropertySession;
import fina2.system.PropertySessionHome;
import fina2.util.LoggerHelper;

public class UserBean implements EntityBean {

	private EntityContext ctx;
	private boolean store = true;

	public String login;
	public String password;
	public int name;
	public int title;
	public String phone;
	public String email;
	private String oldName;
	private String oldTitle;
	private boolean changePassword;
	private boolean blocked;
	private Date lastPasswordChangeDate;
	private Map sysProps;

	public static final String errStatus = "Account did not used too long and hence blocked.";

	private static Map loginErrors = Collections.synchronizedMap(new HashMap());

	private LoggerHelper log = new LoggerHelper(UserBean.class, "User");

	public UserPK ejbCreate() throws EJBException, CreateException {
		UserPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {

			name = LocaleUtil.allocateString(con);
			title = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from SYS_USERS");
			insert = con.prepareStatement("insert into SYS_USERS (id,nameStrID,titleStrID) values(?,?,?)");

			rs = ps.executeQuery();
			rs.next();

			pk = new UserPK(rs.getInt(1) + 1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, name);
			insert.setInt(3, title);

			insert.executeUpdate();

			log.logObjectCreate(pk.getId(), ctx.getCallerPrincipal().getName());
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	public void ejbPostCreate() throws EJBException, CreateException {
	}

	public UserPK ejbFindByPrimaryKey(UserPK pk) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from SYS_USERS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("User is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return pk;
	}

	public UserPK ejbFindByLogin(String login) throws EJBException, RemoteException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		UserPK pk = null;
		Logger logger = log.getLogger();

		try {

			ps = con.prepareStatement("select id from SYS_USERS where RTRIM(login) = ?");
			ps.setString(1, login);
			rs = ps.executeQuery();

			if (!rs.next()) {
				logger.info("Login failed. User is not found. User name: " + login);
				throw new FinderException("User is not found.");
			}
			pk = new UserPK(rs.getInt("id"));
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return pk;
	}

	public UserPK ejbFindByLoginPassword(String login, String password) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		UserPK pk = null;

		Logger logger = log.getLogger();
		logger.info("Login request arrived. User name: " + login);

		try {
			loadSystemProperties();

			ps = con.prepareStatement("select id, password, blocked, lastLoginDate, lastPasswordChangeDate from SYS_USERS where RTRIM(login) = ?");
			ps.setString(1, login);
			rs = ps.executeQuery();

			if (!rs.next()) {

				logger.info("Login failed. User is not found. User name: " + login);
				throw new FinderException("User is not found.");

			} else {

				pk = new UserPK(rs.getInt("id"));
				boolean blocked = (rs.getInt("blocked") == 1) ? true : false;
				if (blocked) {
					logger.info("Login failed. User account is blocked. User name: " + login);
					throw new FinderException("User account is blocked.");
				} else {

					Calendar lastLogin = new GregorianCalendar();
					Calendar currTime = new GregorianCalendar();

					int value = getSysPropertyValue(PropertySession.ALLOWED_ACCOUNT_INACTIVITY_PERIOD);

					java.sql.Date llTime = rs.getDate("lastLoginDate");
					if (llTime == null) {
						llTime = new java.sql.Date(System.currentTimeMillis());
					}
					lastLogin.setTime(llTime);
					lastLogin.add(Calendar.DAY_OF_YEAR, value);

					if (!lastLogin.after(currTime) && value != -1) {
						logger.info("Login failed.");
						logger.info(errStatus);
						blockUser(login, pk);
						throw new FinderException(errStatus);
					} else {
						String encPassword = rs.getString("password");
						if (!encPassword.equals(encode(password))) {
							logger.info("Login failed. Password check failed for user - " + login);
							if (blockUser(login)) {
								logger.info("Blocking user after " + getSysPropertyValue(PropertySession.ALLOWED_LOGIN_ATTEMPT_NUMBER) + " times of inappropriate typing of password");

								blockUser(login, pk);

								throw new FinderException(errStatus);
							}

							throw new FinderException("Password check failed.");
						}
					}
				}
			}

			loginSuccess(con, login, pk, rs.getDate("lastPasswordChangeDate"));

		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	private boolean blockUser(String login) {

		Integer loginAttemptNumber = (Integer) loginErrors.get(login);

		if (loginAttemptNumber == null) {
			loginAttemptNumber = new Integer(1);
		} else {
			loginAttemptNumber = new Integer(loginAttemptNumber.intValue() + 1);
		}

		loginErrors.put(login, loginAttemptNumber);

		return loginAttemptNumber.intValue() > getSysPropertyValue(PropertySession.ALLOWED_LOGIN_ATTEMPT_NUMBER);
	}

	private void blockUser(String login, UserPK pk) throws SQLException, NamingException, RemoteException, FinderException {

		try {

			InitialContext jndi = new InitialContext();

			Object ref = jndi.lookup("fina2/security/UserSession");
			fina2.security.UserSessionHome home = (fina2.security.UserSessionHome) PortableRemoteObject.narrow(ref, fina2.security.UserSessionHome.class);

			fina2.security.UserSession session = home.create();
			session.blockUserByLogin(login, pk);

			loginErrors.remove(login);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void loginSuccess(Connection con, String login, UserPK pk, Date lastPasswordChangeDate) throws SQLException, NamingException, RemoteException, FinderException {

		log.getLogger().info("Login sucessful.");
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update SYS_USERS set lastLoginDate = ? where RTRIM(login)=? ");

			ps.setDate(1, new Date(System.currentTimeMillis()));
			ps.setString(2, login);

			ps.executeUpdate();
			loginErrors.remove(login);
		} finally {
			DatabaseUtil.closeStatement(ps);
		}

		Calendar lastPwndChangeDate = new GregorianCalendar();
		Calendar currTime = new GregorianCalendar();

		int passValPeriod = getSysPropertyValue(PropertySession.PASSWORD_VALIDITY_PERIOD);

		if (lastPasswordChangeDate == null) {
			lastPwndChangeDate.setTime(new java.util.Date());
		} else {
			lastPwndChangeDate.setTime(lastPasswordChangeDate);
		}

		lastPwndChangeDate.add(Calendar.DAY_OF_YEAR, passValPeriod);

		if (!lastPwndChangeDate.after(currTime) && passValPeriod != -1) {
			log.getLogger().info("Password expired. Forcing user to change password. User name: " + login);
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/security/User");
			UserHome userHome = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);

			User user = userHome.findByPrimaryKey(pk);
			user.setChangePassword(true);
		}
	}

	public void ejbActivate() throws EJBException {
	}

	public void ejbStore() throws EJBException {
		log.logObjectStore();
		if (!store) {
			store = true;
			return;
		}
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update SYS_USERS set " + "login=?, " + "password=?, " + "nameStrID=?, " + "titleStrID=?, " + "phone=?, " + "email=?, " + "changePassword=? ," + "blocked=?, "
					+ "lastPasswordChangeDate=? " + "where id=?");
			ps.setString(1, login + " ");
			ps.setString(2, password);
			ps.setInt(3, name);
			ps.setInt(4, title);
			ps.setString(5, phone + " ");
			ps.setString(6, email + " ");
			ps.setInt(7, changePassword ? 1 : 0);
			ps.setInt(8, blocked ? 1 : 0);
			ps.setDate(9, lastPasswordChangeDate);
			ps.setInt(10, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbPassivate() throws EJBException {
	}

	public void ejbLoad() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select login, password, nameStrID, titleStrID, phone, email, " + "changePassword, blocked, lastPasswordChangeDate " + "from SYS_USERS where id=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			login = rs.getString("login").trim();
			password = rs.getString("password");
			name = rs.getInt("nameStrID");
			title = rs.getInt("titleStrID");
			phone = rs.getString("phone").trim();
			email = rs.getString("email").trim();
			changePassword = (rs.getInt("changePassword") == 1) ? true : false;
			lastPasswordChangeDate = rs.getDate("lastPasswordChangeDate");
			blocked = (rs.getInt("blocked") == 1) ? true : false;
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void ejbRemove() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			ps = con.prepareStatement("select nameStrID, titleStrID from SYS_USERS where id=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=? or id=?");
			del.setInt(1, rs.getInt(1));
			del.setInt(2, rs.getInt(2));
			del.executeUpdate();

			DatabaseUtil.closeStatement(del);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			ps = con.prepareStatement("delete from SYS_USER_MENUS " + "where userID=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			String sqlCheck = "SELECT username from SYS_UPLOADEDFILE where username = ?";

			ps = con.prepareStatement(sqlCheck);
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ResultSet rss = ps.executeQuery();
			if (rss.next()) {
				throw new Exception("Could not remove user ,it is being used by SYS_UPLOADEDFILE");
			}
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USERS_ROLES " + "where userID=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USER_PERMISSIONS " + "where userID=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USER_BANKS " + "where userID=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USER_REPORTS " + "where userID=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USER_PASSWORDS " + "where userID=?");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

			DatabaseUtil.closeStatement(ps);

			/*
			 * Deleting from table sys_user_returns
			 */
			int userId = ((UserPK) ctx.getPrimaryKey()).getId();
			String sql = "delete from sys_user_returns where user_id = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Deleting from table sys_user_return_versions
			 */
			sql = "delete from sys_user_return_versions where user_id = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Deleting from table SYS_USERS
			 */
			sql = "delete from SYS_USERS where id = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			int objectId = ((UserPK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
			log.logPropertyValue("userID", this.login, objectId, user);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}
	}

	public void unsetEntityContext() throws EJBException {
		ctx = null;
	}

	public void setEntityContext(EntityContext ctx) throws EJBException {
		this.ctx = ctx;
	}

	public boolean hasPermission(String permissionCode) throws EJBException {
		store = false;
		log.getLogger().debug(permissionCode + " - check");
		if (login.trim().equals("sa")) {
			log.getLogger().debug(permissionCode + " - sa");
			return true;
		}

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select a.id from SYS_PERMISSIONS a, SYS_ROLE_PERMISSIONS b, SYS_USERS_ROLES c "
					+ "where a.idName=? and b.permissionID=a.id and c.roleID=b.roleID and c.userID=?");
			ps.setString(1, permissionCode);
			ps.setInt(2, ((UserPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				log.getLogger().debug(permissionCode + " - role");
				return true;
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select a.id from SYS_PERMISSIONS a, SYS_USER_PERMISSIONS b " + "where a.idName=? and b.permissionID=a.id and b.userID=?");
			ps.setString(1, permissionCode);
			ps.setInt(2, ((UserPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				log.getLogger().debug(permissionCode + " - user");
				return true;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		log.getLogger().debug(permissionCode + " - denied");
		return false;
	}

	public boolean hasPermissions(String[] permissions) throws EJBException {
		store = false;
		if (login.trim().equals("sa")) {
			return true;
		}

		String sql = "";
		for (int i = 0; i < permissions.length; i++) {
			if (!sql.equals(""))
				sql += ", ";
			sql += "'" + permissions[i] + "'";
		}
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select a.id from SYS_PERMISSIONS a, SYS_ROLE_PERMISSIONS b, SYS_USERS_ROLES c " + "where a.idName in (" + sql
					+ ") and b.permissionID=a.id and c.roleID=b.roleID and c.userID=?");
			// pstmt.setString(1, permissionCode);
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				// log.getLogger().debug(permissionCode+" - role");
				return true;
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select a.id from SYS_PERMISSIONS a, SYS_USER_PERMISSIONS b " + "where a.idName in (" + sql + ") and b.permissionID=a.id and b.userID=?");
			// pstmt.setString(1, permissionCode);
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				// log.getLogger().debug(permissionCode+" - user");
				return true;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		// log.getLogger().debug(permissionCode+" - denied");
		return false;
	}

	public Collection getPermissions() throws EJBException {

		ArrayList permissions = new ArrayList();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if ("sa".equals(login.trim())) {

				ps = con.prepareStatement("select a.idName from SYS_PERMISSIONS a");
				rs = ps.executeQuery();

				while (rs.next()) {
					permissions.add(rs.getString(1));
				}
			} else {
				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);

				ps = con.prepareStatement("select a.idName from SYS_PERMISSIONS a, SYS_ROLE_PERMISSIONS b, SYS_USERS_ROLES c " + "where b.permissionID=a.id and c.roleID=b.roleID and c.userID=?");
				ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();

				while (rs.next()) {
					permissions.add(rs.getString(1));
				}
				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);

				ps = con.prepareStatement("select a.idName from SYS_PERMISSIONS a, SYS_USER_PERMISSIONS b " + "where b.permissionID=a.id and b.userID=?");
				ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();
				if (rs.next()) {
					permissions.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return permissions;
	}

	public boolean canAccessBank(BankPK bankPK) throws EJBException {
		store = false;
		if (login.equals("sa"))
			return true;

		return false;
	}

	public String getLogin() {
		store = false;
		return login;
	}

	/** Sets a user login */
	public void setLogin(String login) throws EJBException, FinaTypeException {
		store = true;

		int id = ((UserPK) ctx.getPrimaryKey()).getId();
		log.logPropertySet("userID", login, this.login, id, ctx.getCallerPrincipal().getName());

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from SYS_USERS where rtrim(login)=? and id!=?");
			ps.setString(1, login.trim());
			ps.setInt(2, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] { "FinaTypeException.Login", login });
			}
			this.login = login;
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public String getPassword() {
		store = false;
		return password;
	}

	public void setPassword(String password) throws FinaTypeException {

		store = true;
		Connection con = null;
		try {
			loadSystemProperties();
			con = DatabaseUtil.getConnection();

			String newPassword = encode(password);

			/* Throws exception if a password format is incorrect */
			checkPasswordFormat(password);

			if (passwordAlreadyUsed(newPassword, con)) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] { "FinaTypeException.Password", password });
			} else {
				storePassword(newPassword, con);
			}

			this.lastPasswordChangeDate = new Date(System.currentTimeMillis());
			this.password = newPassword;

		} catch (FinaTypeException e) {
			throw e;
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	private void checkPasswordFormat(String password) throws FinaTypeException {

		if (password.length() < getSysPropertyValue(PropertySession.MINIMAL_PASSWORD_LENGTH)) {
			throw new FinaTypeException(Type.SECURITY_PASSWORD_TOO_SHORT);
		} else if (getSysPropertyValue(PropertySession.PASSWORD_WITH_NUMS_AND_CHARS) == 1) {

			boolean number = false;
			boolean character = false;
			for (int i = 0; i < password.length(); i++) {
				char passChar = password.charAt(i);
				if (passChar >= '0' && passChar <= '9') {
					number = true;
				} else {
					character = true;
				}
			}

			if (number == false || character == false) {
				throw new FinaTypeException(Type.SECURITY_PASSWORD_CHAR_NUM_REQUIRED);
			}
		}
	}

	private boolean passwordAlreadyUsed(String newPassword, Connection con) throws SQLException {

		boolean usedBefore = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Timestamp time = null;
		try {
			ps = con.prepareStatement("select password, storedate from SYS_USER_PASSWORDS where userid = ? order by storedate DESC");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			int i;
			for (i = 0; i < getSysPropertyValue(PropertySession.OLD_STORED_PASSWORDS_NUMBER) && rs.next(); i++) {
				if (rs.getString("password").trim().equalsIgnoreCase(newPassword.trim())) {
					usedBefore = true;
				}
				time = rs.getTimestamp("storedate");
			}

			if (!usedBefore && i > 0 && i >= getSysPropertyValue(PropertySession.OLD_STORED_PASSWORDS_NUMBER)) {
				ps = con.prepareStatement("delete from SYS_USER_PASSWORDS where userid = ? and storedate <= ?");
				ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
				ps.setTimestamp(2, time);
				ps.execute();
			}
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
		}
		return usedBefore;
	}

	private void storePassword(String newPassword, Connection con) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("insert into SYS_USER_PASSWORDS(userid, password, storedate) values(?, ?, ?)");
			ps.setInt(1, ((UserPK) ctx.getPrimaryKey()).getId());
			ps.setString(2, newPassword);
			ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			ps.executeUpdate();
		} finally {
			DatabaseUtil.closeStatement(ps);
		}
	}

	public String getName(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, name);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		this.oldName = s;

		return s;
	}

	public void setName(Handle langHandle, String name) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		log.logPropertySet("name", name, this.oldName, ((UserPK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.oldName = name;

		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, this.name, name);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getTitle(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, title);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}

		this.oldTitle = s;

		return s;
	}

	public void setTitle(Handle langHandle, String title) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		log.logPropertySet("title", title, this.oldTitle, ((UserPK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.oldTitle = title;

		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, this.title, title);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getPhone() {
		store = false;
		return phone;
	}

	public void setPhone(String phone) {
		store = true;
		log.logPropertySet("phone", phone, this.phone, ((UserPK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.phone = phone;
	}

	public String getEmail() {
		store = false;
		return email;
	}

	public void setEmail(String email) {
		store = true;
		log.logPropertySet("E-mail", email, this.email, ((UserPK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.email = email;
	}

	public boolean getChangePassword() {
		store = false;
		return changePassword;
	}

	public boolean getBlocked() {
		store = false;
		return blocked;
	}

	public void setChangePassword(boolean changePassword) {
		store = true;
		log.logPropertySet("changePassword", changePassword ? "true" : "false", this.changePassword ? "true" : "false", ((UserPK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.changePassword = changePassword;
	}

	public void setBlocked(boolean blocked) {
		store = true;
		log.logPropertySet("blocked", blocked ? "true" : "false", this.blocked ? "true" : "false", ((UserPK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		if (!blocked) {
			changeLastLoginDate();
		}
		this.blocked = blocked;
	}

	/*
	 * Change user last login date when, account activate period is end and
	 * other user change user block option(in GUI).
	 */
	private void changeLastLoginDate() {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Calendar lastLogin = new GregorianCalendar();
			Calendar currTime = new GregorianCalendar();

			loadSystemProperties();
			int value = getSysPropertyValue(PropertySession.ALLOWED_ACCOUNT_INACTIVITY_PERIOD);

			ps = con.prepareStatement("select lastLoginDate from SYS_USERS where RTRIM(login) = ?");
			ps.setString(1, login);
			rs = ps.executeQuery();
			if (rs.next()) {
				java.sql.Date llTime = rs.getDate("lastLoginDate");
				if (llTime == null) {
					llTime = new java.sql.Date(System.currentTimeMillis());
				}
				lastLogin.setTime(llTime);
				lastLogin.add(Calendar.DAY_OF_YEAR, value);
				if (!lastLogin.after(currTime) && value != -1) {
					ps.close();
					ps = con.prepareStatement("update SYS_USERS set lastLoginDate = ? where RTRIM(login)=? ");
					ps.setDate(1, new Date(System.currentTimeMillis()));
					ps.setString(2, login);
					ps.executeUpdate();
					log.getLogger().info("User : " + login + " is unblocked. Last login date is reset(Reset Date is Current Date).");
				}
			} else {
				log.getLogger().info("User " + login + "'s last login date doesn't found.");
			}
		} catch (Exception ex) {
			log.getLogger().error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	private void loadSystemProperties() throws RemoteException, NamingException, CreateException {

		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/system/PropertySession");
		PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);

		PropertySession session = home.create();
		sysProps = session.getSystemProperties();
	}

	private int getSysPropertyValue(String key) throws NumberFormatException {
		int result = -1;
		String value = (String) this.sysProps.get(key);
		if (value != null) {
			result = Integer.parseInt(value);
		}
		return result;
	}

	private String encode(String pass) {

		String encodedPass = null;

		if (pass != null) {

			byte[] password = pass.getBytes();
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				password = md.digest(password);
			} catch (NoSuchAlgorithmException ex) {
				log.getLogger().warn("SHA-1 cryptographic algorithm is not available in the environment", ex);
			}

			StringBuffer str = new StringBuffer();

			for (int i = 0; i < password.length; i++) {
				str.append(Integer.toHexString((password[i] & 0xf0) >> 4));
				str.append(Integer.toHexString(password[i] & 0x0f));
			}
			encodedPass = str.toString();
		}
		return encodedPass;
	}

	/** Returns user id from given user handle */
	public static int getUserId(Handle userHandle) throws RemoteException {

		User user = (User) userHandle.getEJBObject();
		UserPK userPK = (UserPK) user.getPrimaryKey();

		return userPK.getId();
	}

}
