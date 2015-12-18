package fina2.security;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.ui.tree.Node;

public class UserSessionBean implements SessionBean {

	private SessionContext ctx;
	private Logger log = Logger.getLogger(UserSessionBean.class);

	public void setSessionContext(javax.ejb.SessionContext ctx) throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.ctx = ctx;
	}

	public UserPK findByLogin(String login) {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		UserPK pk = null;
		try {
			ps = con.prepareStatement("select id from SYS_USERS where RTRIM(login) = ?");
			ps.setString(1, login);
			rs = ps.executeQuery();
			if (rs.next())
				pk = new UserPK(rs.getInt(1));
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void blockUserByLogin(String login, UserPK pk) {
		Connection con = null;
		try {
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/security/User");
			UserHome userHome = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);

			User user = userHome.findByPrimaryKey(pk);
			user.setBlocked(true);

			con = DatabaseUtil.getConnection();
			PreparedStatement ps = con.prepareStatement("update Sys_users set blocked=1 where RTRIM(login)=?");

			ps.setString(1, login);
			ps.executeUpdate();
		} catch (Exception exx) {
			throw new EJBException(exx);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public Node getTreeNodes(Handle userHandle, Handle languageHandle) throws RemoteException, fina2.FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.security.amend");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		Node root = new Node(new UserPK(0), "        ", new Integer(-3));
		try {
			Node roles = new Node(new UserPK(-1), "User Groups", new Integer(-1));
			Node users = new Node(new UserPK(-2), "Users", new Integer(-2));

			Hashtable nodes = new Hashtable();

			ps = con.prepareStatement("select a.id, b.value, b.langID,a.code from SYS_ROLES a left outer join SYS_STRINGS b on a.nameStrID=b.id and (b.langID=?) ");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			Node node = null;
			while (rs.next()) {
				RolePK pk = new RolePK(rs.getInt(1));
				if (node != null) {
					RolePK prevPK = (RolePK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}
				// UserPK parent = new UserPK(rs.getInt(2));
				String code = LocaleUtil.encode(rs.getString(4), encoding);
				String label = rs.getString(2);
				if (label == null)
					label = "NONAME";
				else
					label = LocaleUtil.encode(label.trim(), encoding);

				node = new Node(pk, "[" + code + "]" + label, new Integer(1));

				// if(parent.getId() == 0) {
				roles.addChild(node);
				/*
				 * } else { Node p = (Node)nodes.get(parent); p.addChild(node);
				 * }
				 */
				nodes.put(pk, node);
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			// ps =
			// con.prepareStatement("select a.id, c.roleID, b.value, b.langID, a.login "
			// + "from SYS_USERS a, SYS_STRINGS b, SYS_USERS_ROLES c " +
			// "where b.id=a.nameStrID and (b.langID=?) and c.userID=a.id " +
			// "order by c.roleID, a.id, b.langID DESC");
			ps = con.prepareStatement("select a.id, c.roleID, b.value, b.langID, a.login from SYS_USERS_ROLES c, SYS_USERS a left outer join  SYS_STRINGS b on b.id=a.nameStrID and b.langID=? where c.userID=a.id ");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			node = null;
			RolePK parent = null;
			while (rs.next()) {
				UserPK pk = new UserPK(rs.getInt(1));
				RolePK prevParentPK = parent;
				parent = new RolePK(rs.getInt(2));
				if (node != null) {
					UserPK prevPK = (UserPK) node.getPrimaryKey();
					if (pk.equals(prevPK) && parent.equals(prevParentPK))
						continue;
				}
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				node = new Node(pk, "[" + rs.getString(5).trim() + "] " + desc, new Integer(2));

				/*
				 * if(parent.getId() == 0) { root.addChild(node); } else {
				 */
				Node p = (Node) nodes.get(parent);
				p.addChild(node);
				// }
				// nodes.put(pk, node);
			}

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			nodes = new Hashtable();

			// ps =
			// con.prepareStatement("select a.id, b.value, b.langID,a.login " +
			// "from SYS_USERS a, SYS_STRINGS b " +
			// "where b.id=a.nameStrID and (b.langID=?) " +
			// "order by a.id, b.langID DESC");
			ps = con.prepareStatement("select a.id, b.value, b.langID,a.login from SYS_USERS a  left outer join SYS_STRINGS b on b.id=a.nameStrID and b.langID=? ");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			node = null;
			while (rs.next()) {
				UserPK pk = new UserPK(rs.getInt(1));
				if (node != null) {
					UserPK prevPK = (UserPK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}
				// UserPK parent = new UserPK(rs.getInt(2));
				String desc = rs.getString(2);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);

				node = new Node(pk, "[" + rs.getString(4).trim() + "] " + desc, new Integer(2));

				// if(parent.getId() == 0) {
				users.addChild(node);
				/*
				 * } else { Node p = (Node)nodes.get(parent); p.addChild(node);
				 * }
				 */
				nodes.put(pk, node);
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select a.id, c.userID, b.value, b.langID from SYS_ROLES a, SYS_STRINGS b, SYS_USERS_ROLES c where b.id=a.nameStrID and b.langID=? and c.roleID=a.id ");
			// ps =
			// con.prepareStatement("select a.id, c.userID, b.value, b.langID from   SYS_USERS_ROLES c,SYS_ROLES a left outer join SYS_STRINGS b on b.id=a.nameStrID and b.langID=? and c.roleID=a.id ");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			node = null;
			UserPK _parent = null;
			while (rs.next()) {
				RolePK pk = new RolePK(rs.getInt(1));
				UserPK prevParentPK = _parent;
				_parent = new UserPK(rs.getInt(2));
				if (node != null) {
					RolePK prevPK = (RolePK) node.getPrimaryKey();
					if (pk.equals(prevPK) && _parent.equals(prevParentPK))
						continue;
				}
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);

				node = new Node(pk, desc, new Integer(1));

				/*
				 * if(parent.getId() == 0) { root.addChild(node); } else {
				 */
				Node p = (Node) nodes.get(_parent);
				if (p != null)
					p.addChild(node);
				// }
				// nodes.put(pk, node);
			}
			root.addChild(roles);
			root.addChild(users);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return root;
	}

	/** Updates a given user roles */
	public void setUserRoles(UserPK userPK, Set<Integer> roles) throws RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Deleting user old roles
			 */
			String sql = "delete from sys_users_roles where userId = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Inserting new roles
			 */
			sql = "insert into sys_users_roles (userId, roleId) values (?, ?)";
			ps = con.prepareStatement(sql);

			for (int roleId : roles) {
				ps.setInt(1, userId);
				ps.setInt(2, roleId);
				ps.executeUpdate();

				idList += roleId + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting user roles", userId, "Roles id(s): " + idList);
	}

	/** Updates a given user banks */
	public void setUserBanks(UserPK userPK, Set<Integer> banks) throws RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Deleting user old banks
			 */
			String sql = "delete from sys_user_banks where userId = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Inserting new roles
			 */
			sql = "insert into sys_user_banks (userId, bankId) values (?, ?)";
			ps = con.prepareStatement(sql);

			for (int bankId : banks) {
				ps.setInt(1, userId);
				ps.setInt(2, bankId);
				ps.executeUpdate();

				idList += bankId + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting user banks", userId, "Banks id(s): " + idList);
	}

	/** Updates a given user returns */
	public void setUserReturns(UserPK userPK, Set<Integer> returns) throws RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Deleting the user old returns
			 */
			String sql = "delete from sys_user_returns where user_id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Inserting new returns
			 */
			sql = "insert into sys_user_returns (user_id, definition_id) values (?, ?)";
			ps = con.prepareStatement(sql);

			for (int returnId : returns) {

				ps.setInt(1, userId);
				ps.setInt(2, returnId);
				ps.executeUpdate();

				idList += returnId + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting user returns", userId, "Returns id(s): " + idList);
	}

	/** Updates a given user reports */
	public void setUserReports(UserPK userPK, Set<Integer> reports) throws RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Deleting the user old reports
			 */
			String sql = "delete from sys_user_reports where userid = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Inserting new reports
			 */
			sql = "insert into sys_user_reports (userid, reportid) values (?, ?)";
			ps = con.prepareStatement(sql);

			for (int reportId : reports) {

				ps.setInt(1, userId);
				ps.setInt(2, reportId);
				ps.executeUpdate();

				idList += reportId + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting user reports", userId, "Reports id(s): " + idList);
	}

	/** Returns a tree of given user banks */
	public Map<Integer, TreeSecurityItem> getUserBanks(UserPK userPK, Handle languageHandle) throws RemoteException {
		/*
		 * The result map. At first it contains all banks. Then the given user
		 * banks are retrieved and set into the result map.
		 */
		Map<Integer, TreeSecurityItem> banks = getAllBanks(languageHandle);

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/* Selecting the user banks */
			String sql = "select a.bankid from sys_user_banks a where a.userid = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			/* Looping through the user banks */
			while (rs.next()) {
				/* The given user has some banks */

				int bankId = rs.getInt(1);
				idList += bankId + ", ";

				/* Setting this bank in all banks map */
				TreeSecurityItem item = banks.get(bankId);
				item.setReview(true);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting user banks", userId, "Banks id(s): " + idList);

		/* The result list */
		return banks;
	}

	/* get parameter user banks id list */
	public List<Integer> getUserBanksId(UserPK userPK) throws RemoteException {
		List<Integer> bankIdList = new ArrayList<Integer>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		int userId = userPK.getId();

		/* Select the user Banks ID */
		String sql = "select a.bankid from sys_user_banks a where a.userid = ?";

		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				int bankId = rs.getInt(1);
				bankIdList.add(bankId);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting user banks", userId, "Banks id(s): " + bankIdList);

		return bankIdList;
	}

	/** Returns the tree of user banks only */
	public Map<Integer, TreeSecurityItem> getUserBanksOnly(UserPK userPK, Handle languageHandle) throws RemoteException, EJBException {

		/* The result map. (LinkedHashMap - to keep insertion order.) */
		Map<Integer, TreeSecurityItem> userBanks = new LinkedHashMap<Integer, TreeSecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int userId = userPK.getId();

		try {
			/*
			 * Selecting max id from banks. It is needed to avoid id collision
			 * in hierarchical query (see below)
			 */
			String sql = "select max(a.id) from in_banks a";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			int maxId = 0;
			if (rs.next()) {
				/* There are the banks */
				maxId = rs.getInt(1) + 1;
			}

			/*
			 * Hierarchical query to select only user banks and bank types
			 */
			sql = "select (a.id + ?) as id, a.code, a.namestrid, 0 as parent_id, 0 as leaf " + "from in_bank_types a " + "where a.id in ( " + "  select distinct c.id "
					+ "  from sys_user_banks a, in_banks b, in_bank_types c " + "  where a.bankid = b.id and b.typeid = c.id and a.userid = ? " + ") " + "union "
					+ "select a.id, a.code, a.namestrid, (a.typeid + ?) as parent_id, 1 as leaf " + "from in_banks a " + "where a.id in (select a.bankid from sys_user_banks a where a.userid = ?) "
					+ "order by parent_id, code  ";

			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			ps = con.prepareStatement(sql);
			ps.setInt(1, maxId);
			ps.setInt(2, userId);
			ps.setInt(3, maxId);
			ps.setInt(4, userId);
			rs = ps.executeQuery();

			while (rs.next()) {

				/* Id */
				int id = rs.getInt(1);

				/* Text */
				String bankCode = rs.getString(2).trim();
				String text = "[" + bankCode + "] " + LocaleUtil.getString(con, languageHandle, rs.getInt(3));

				/* Parent id */
				int parentId = rs.getInt(4);

				/* Is bank type or bank itself */
				boolean isLeaf = (rs.getInt(5) == 1) ? true : false;

				/* Addint to the result map */
				TreeSecurityItem item = new TreeSecurityItem(id, text, parentId, isLeaf);
				item.setProperty("code", bankCode);

				userBanks.put(id, item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		/* The result map */
		return userBanks;
	}

	/** Returns user roles */
	public List<SecurityItem> getUserRoles(UserPK userPK, Handle languageHandle) throws RemoteException {

		/*
		 * The result list. At first it contains all roles. Then the given user
		 * roles are retrieved and set into the result list.
		 */
		List<SecurityItem> roles = getAllRoles(languageHandle);

		int userId = userPK.getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String idList = "";

		try {
			/* Selecting the user roles */
			String sql = "select a.roleid from sys_users_roles a where a.userid = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			/* Looping through the user's roles */
			while (rs.next()) {
				/* The given user has some roles */

				int id = rs.getInt(1);
				idList += id + ", ";

				/* Looking for this id in all roles list */
				for (SecurityItem item : roles) {
					if (item.getId() == id) {
						/* Found. This user has this role */
						item.setReview(true);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting user roles", userId, "Roles id(s): " + idList);

		/* The result list */
		return roles;
	}

	/** Updates a given user permissions */
	public void setUserPermissions(UserPK userPK, Set<Integer> permissions) throws RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Deleting user old permissions
			 */
			String sql = "delete from sys_user_permissions where userId = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Inserting new permissions
			 */
			sql = "insert into sys_user_permissions (userId, permissionId) values (?, ?)";
			ps = con.prepareStatement(sql);

			for (int permId : permissions) {
				ps.setInt(1, userId);
				ps.setInt(2, permId);
				ps.executeUpdate();

				idList += permId + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting user permissions", userId, "Permissions id(s): " + idList);
	}

	/** Returns a given user permissions */
	public List<SecurityItem> getUserPermissions(UserPK userPK, Handle languageHandle) throws RemoteException {

		/*
		 * The result list. At first it contains all permissions. Then the given
		 * user permissions are retrieved and set into the result list with
		 * value YES. After it the user roles permissions are retrieved and set
		 * into the result list with value YES_READONLY.
		 */
		List<SecurityItem> permissions = getAllPermissions(languageHandle);

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Processing user permissions
			 */
			String sql = "select a.permissionid from sys_user_permissions a where a.userid = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			/* Looping through the user permissions */
			while (rs.next()) {

				int permId = rs.getInt(1);
				idList += permId + ", ";

				/* Looking for this id in all permissions list */
				for (SecurityItem item : permissions) {
					if (item.getId() == permId) {
						/* Found. This user has this permission */
						item.setReview(true);
						break;
					}
				}
			}

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			/*
			 * Processing user roles permissions
			 */
			sql = "select distinct a.permissionid from sys_role_permissions a where a.roleid in " + "(select a1.roleid from sys_users_roles a1 where a1.userid = ?)";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			/* Looping through the user roles permissions */
			while (rs.next()) {

				int permId = rs.getInt(1);

				/* Looking for this id in all permissions list */
				for (SecurityItem item : permissions) {
					if (item.getId() == permId) {
						/* Found. This some user role has this permission */
						item.setReview(SecurityItem.Status.YES_READONLY);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting user permissions", userId, "Permissions id(s): " + idList);

		/* The result list */
		return permissions;
	}

	/** Returns a given user return versions */
	public List<SecurityItem> getUserReturnVersions(UserPK userPK, Handle languageHandle) throws RemoteException, EJBException {

		/*
		 * The result list. At first it contains all return versions. Then the
		 * given user return versions are retrieved and set into the result list
		 * with value YES. After it the user roles return versions are retrieved
		 * and set into the result list with value YES_READONLY.
		 */
		List<SecurityItem> versions = getAllReturnVersions(languageHandle);

		int userId = userPK.getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String idList = "";

		try {
			/*
			 * Processing the user's return versions
			 */
			String sql = "select a.version_id, a.can_amend " + "from sys_user_return_versions a " + "where a.user_id = ? ";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);

			rs = ps.executeQuery();

			/* Looping through the user's return versions */
			while (rs.next()) {

				int id = rs.getInt(1);
				idList += id + ", ";

				/* Looking for this id in all versions list */
				for (SecurityItem item : versions) {
					if (item.getId() == id) {
						/* Found. This user has this return version */
						item.setReview(true);

						boolean canAmend = (rs.getInt(2) == 1) ? true : false;
						item.setAmend(canAmend);
						break;
					}
				}
			}

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			/*
			 * Processing user roles permissions
			 */
			sql = "select distinct a.version_id, a.can_amend from sys_role_return_versions a " + " where a.role_id in (select a1.roleid from sys_users_roles a1 where a1.userid = ?)";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			/* Looping through the user roles' return versions */
			while (rs.next()) {

				/* Looking for current id in all versions list */
				for (SecurityItem item : versions) {
					if (item.getId() == rs.getInt(1)) {
						/* Found. The user's some role has this return version */
						item.setReview(SecurityItem.Status.YES_READONLY);

						if (rs.getInt(2) == 1) {
							/* There is also amend option */
							item.setAmend(SecurityItem.Status.YES_READONLY);
						}

						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting user return versions", userId, "Versions id(s): " + idList);

		/* The result list */
		return versions;
	}

	/** Returns a given user returns */
	public Map<Integer, TreeSecurityItem> getUserReturns(UserPK userPK, Handle languageHandle) throws RemoteException, EJBException {

		/*
		 * The result map. At first it contains all returns. Then the given user
		 * returns are retrieved and set into the result map with value YES.
		 * After it the user roles returns are retrieved and set into the result
		 * map with value YES_READONLY.
		 */
		Map<Integer, TreeSecurityItem> returns = getAllReturns(languageHandle);

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Processing the user returns
			 */
			String sql = "select a.definition_id from sys_user_returns a where a.user_id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			while (rs.next()) {

				int returnId = rs.getInt(1);
				idList += returnId + ", ";

				/* Setting user return into the result map */
				TreeSecurityItem item = returns.get(returnId);
				if (item != null)
					item.setReview(true);
			}

			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			/*
			 * Processing the user roles returns
			 */
			sql = "select distinct a.definition_id from sys_role_returns a where a.role_id in " + "(select a1.roleid from sys_users_roles a1 where a1.userid = ?)";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			while (rs.next()) {
				/* Some user role has this return */
				TreeSecurityItem item = returns.get(rs.getInt(1));
				item.setReview(SecurityItem.Status.YES_READONLY);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting user returns", userId, "Returns id(s): " + idList);

		/* The result map */
		return returns;
	}

	/** Returns a tree of given user reports */
	public Map<Integer, TreeSecurityItem> getUserReports(UserPK userPK, Handle languageHandle) throws RemoteException, EJBException {

		/*
		 * The result map. At first it contains all reports. Then the given user
		 * reports are retrieved and set into the result map with value YES.
		 * After it the user roles reports are retrieved and set into the result
		 * map with value YES_READONLY.
		 */
		Map<Integer, TreeSecurityItem> reports = getAllReports(languageHandle);

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Processing the user reports
			 */
			String sql = "select a.reportid from sys_user_reports a where a.userid = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			while (rs.next()) {

				int reportId = rs.getInt(1);
				idList += reportId + ", ";

				/* Setting user report into the result map */
				TreeSecurityItem item = reports.get(reportId);
				if (item != null)
					item.setReview(true);
			}

			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			/*
			 * Processing the user roles reports
			 */
			sql = "select distinct a.report_id from sys_role_reports a where a.role_id in " + "(select a1.roleid from sys_users_roles a1 where a1.userid = ?)";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			while (rs.next()) {
				/* Some user role has this reports */
				TreeSecurityItem item = reports.get(rs.getInt(1));
				if (item != null)
					item.setReview(SecurityItem.Status.YES_READONLY);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting user reports", userId, "Reports id(s): " + idList);

		/* The result map */
		return reports;
	}

	/** Updates a given user return versions */
	public void setUserReturnVersions(UserPK userPK, List<SecurityItem> versions) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int userId = userPK.getId();
		String idList = "";

		try {
			/*
			 * Deleting user old return versions
			 */
			String sql = "delete from SYS_USER_RETURN_VERSIONS where user_id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Adding new return versions
			 */
			sql = "insert into SYS_USER_RETURN_VERSIONS (user_id, version_id, can_amend) values (?, ?, ?)";
			ps = con.prepareStatement(sql);

			for (SecurityItem item : versions) {

				ps.setInt(1, userId);
				ps.setInt(2, item.getId());

				int canAmend = (item.getAmend() == SecurityItem.Status.YES) ? 1 : 0;
				ps.setInt(3, canAmend);

				ps.executeUpdate();

				idList += item.getId() + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting user return versions", userId, "Versions id(s): " + idList);
	}

	public Hashtable getUserCanAmendBanks(UserPK userPK) throws EJBException {
		Hashtable v = new Hashtable();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = con.prepareStatement("select a.id " + "from IN_BANKS a "
					+ "where ( (select b.login from SYS_USERS b where id=?)='sa' or a.id in (select c.bankID from SYS_USER_BANKS c where c.userID=?) )");

			ps.setInt(1, userPK.getId());
			ps.setInt(2, userPK.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				Integer key = new Integer(rs.getInt(1));
				v.put(key, key);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Hashtable getUserCanAmendBankCodes(UserPK userPK) throws EJBException {
		Hashtable v = new Hashtable();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = con.prepareStatement("select a.code " + "from IN_BANKS a "
					+ "where ( (select b.login from SYS_USERS b where id=?)='sa' or a.id in (select c.bankID from SYS_USER_BANKS c where c.userID=? and c.canAmend=1) )");

			ps.setInt(1, userPK.getId());
			ps.setInt(2, userPK.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				String key = rs.getString(1).trim();
				v.put(key, key);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Hashtable getUserCanReviewBanks(UserPK userPK) throws EJBException {
		Hashtable v = new Hashtable();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select a.id " + "from IN_BANKS a "
					+ "where ( (select b.login from SYS_USERS b where id=?)='sa' or a.id in (select c.bankID from SYS_USER_BANKS c where c.userID=?) )");

			ps.setInt(1, userPK.getId());
			ps.setInt(2, userPK.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				Integer key = new Integer(rs.getInt(1));
				v.put(key, key);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public void setUserBank(UserPK user, fina2.bank.BankPK bank) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("insert into SYS_USER_BANKS (userID, bankID, canAmend) " + "values (?,?,?)");

			ps.setInt(1, user.getId());
			ps.setInt(2, bank.getId());
			ps.setInt(3, 1);
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

	}

	/** Returns the list of all permissions */
	public List<SecurityItem> getAllPermissions(Handle languageHandle) throws RemoteException, EJBException {

		/* The result list */
		List<SecurityItem> list = new ArrayList<SecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int langId = LocaleUtil.getLanguageId(languageHandle);
		String encoding = LocaleUtil.getEncoding(languageHandle);

		try {
			/* Query to DB */
			String sql = "select a.id, b.value " + "from SYS_PERMISSIONS a, SYS_STRINGS b, " + "(select a.id, max(b.langid) as langid " + "from SYS_PERMISSIONS a, SYS_STRINGS b "
					+ "where b.id=a.nameStrID and (b.langID=? or b.langID=1) " + "group by a.id) c " + "where b.id=a.nameStrID and a.id=c.id and b.langID=c.langid "
					+ "order by b.value, b.langID DESC";

			ps = con.prepareStatement(sql);

			ps.setInt(1, langId);
			rs = ps.executeQuery();

			/* Copying the data to the result list */
			while (rs.next()) {
				int id = rs.getInt(1);
				String text = rs.getString(2);
				text = LocaleUtil.encode(text.trim(), encoding);

				list.add(new SecurityItem(id, text));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		/* The result list */
		return list;
	}

	/** Returns the list of all users */
	public List<SecurityItem> getAllUsers(Handle languageHandle) throws RemoteException {

		/* The result list */
		List<SecurityItem> users = new ArrayList<SecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			/* Query to DB */
			String sql = "select a.id, a.login from sys_users a order by a.login";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			/* Copying result set to result list */
			while (rs.next()) {

				int id = rs.getInt(1);
				String text = rs.getString(2);

				users.add(new SecurityItem(id, text));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		/* The result list */
		return users;
	}

	/** Returns the list of all roles */
	public List<SecurityItem> getAllRoles(Handle languageHandle) throws RemoteException, EJBException {

		/* The result list */
		List<SecurityItem> list = new ArrayList<SecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			/* Selecting all roles */
			String sql = "select a.id, a.code, a.namestrid from sys_roles a order by a.code";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			/* Copying the result set to the result list */
			while (rs.next()) {

				int id = rs.getInt(1);
				String text = "[" + rs.getString(2) + "] " + LocaleUtil.getString(con, languageHandle, rs.getInt(3));

				list.add(new SecurityItem(id, text));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		/* The result list */
		return list;
	}

	/** Returns the list of all return versions */
	public List<SecurityItem> getAllReturnVersions(Handle languageHandle) throws RemoteException {

		/* The result list */
		List<SecurityItem> list = new ArrayList<SecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			/* SQL to select all return versions */
			String sql = "select v.id, v.code, v.descStrID " + "from IN_RETURN_VERSIONS v " + "order by v.sequence ";

			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			/* Copying the result set to the result list */
			while (rs.next()) {

				/* Id */
				int id = rs.getInt(1);

				/* Text */
				String text = "[" + rs.getString(2) + "] " + LocaleUtil.getString(con, languageHandle, rs.getInt(3));

				/* Adding to the result list */
				list.add(new SecurityItem(id, text));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		/* The result list */
		return list;
	}

	/** Returns a tree of all reports */
	public Map<Integer, TreeSecurityItem> getAllReports(Handle languageHandle) throws RemoteException, EJBException {

		/* The result map */
		Map<Integer, TreeSecurityItem> reportsMap = new LinkedHashMap<Integer, TreeSecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int langId = LocaleUtil.getLanguageId(languageHandle);
		String encoding = LocaleUtil.getEncoding(languageHandle);

		try {
			/*
			 * SQL to select reports data.
			 */
			String sql = "select b.id, b.parentid, a.value, b.leaf " + "from sys_strings a, " + "( " + "    select b.id, b.parentid, b.type as leaf, b.namestrid, max(a.langid) as langid "
					+ "    from sys_strings a, out_reports b " + "    where a.id = b.namestrid and (a.langid = ?  or a.langid = 1) " + "    group by b.id, b.parentid, b.type, b.namestrid " + ") b "
					+ "where a.id = b.namestrid and a.langid = b.langid " + "order by upper(a.value) ";

			ps = con.prepareStatement(sql);
			ps.setInt(1, langId);
			rs = ps.executeQuery();

			/* Copying data from the result set to the result map */
			while (rs.next()) {

				/* Id */
				int id = rs.getInt(1);

				/* Parent id */
				int parentId = rs.getInt(2);

				/* Text */
				String text = rs.getString(3);
				text = LocaleUtil.encode(text, encoding);

				/* Leaf item */
				boolean isLeaf = (rs.getInt(4) == 2) ? true : false;

				/* Adding to the result map */
				reportsMap.put(id, new TreeSecurityItem(id, text, parentId, isLeaf));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		/* The result map */
		return reportsMap;
	}

	/** Returns the tree of all returns */
	public Map<Integer, TreeSecurityItem> getAllReturns(Handle languageHandle) throws RemoteException, EJBException {

		/* The result map */
		Map<Integer, TreeSecurityItem> returns = new LinkedHashMap<Integer, TreeSecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int langId = LocaleUtil.getLanguageId(languageHandle);
		String encoding = LocaleUtil.getEncoding(languageHandle);

		try {
			/*
			 * Selecting max id from returns. It is needed to avoid id collision
			 * in hierarchical query (see below)
			 */
			String sql = "select max(id) as maxid from in_return_definitions";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			int maxId = 0;
			if (rs.next()) {
				/* There are the returns */
				maxId = rs.getInt(1) + 1;
			}

			/*
			 * Hierarchical query for the returns and return types
			 */
			sql = "select (b.id + ?) as id, b.code, a.value as text, 0 as parentId, 0 as leaf " + "from SYS_STRINGS a, " + "( " + "    select a.id, a.code, a.namestrid, max(b.langid) as langid "
					+ "    from in_return_types a, sys_strings b " + "    where b.id=a.nameStrID and (b.langID=? or b.langID=1) " + "    group by a.id, a.code, a.namestrid " + ") b "
					+ "where a.id = b.namestrid and a.langid = b.langid " + "union " + "select b.id, b.code, a.value as text, (b.typeid + ?) as parentId, 1 as leaf " + "from SYS_STRINGS a, " + "( "
					+ "    select a.id, a.code, a.namestrid, a.typeid, max(b.langid) as langid " + "    from in_return_definitions a, sys_strings b "
					+ "    where b.id=a.nameStrID and (b.langID=? or b.langID=1) " + "    group by a.id, a.code, a.namestrid, a.typeid " + ") b " + "where a.id = b.namestrid and a.langid = b.langid "
					+ "order by parentId, code";

			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			ps = con.prepareStatement(sql);

			/* Setting bind variables */
			ps.setInt(1, maxId);
			ps.setInt(2, langId);
			ps.setInt(3, maxId);
			ps.setInt(4, langId);

			/* Copying data from the resultset to the result map */
			rs = ps.executeQuery();

			while (rs.next()) {
				/* id */
				int id = rs.getInt(1);

				/* text */
				String text = "[" + rs.getString(2).trim() + "] " + rs.getString(3);
				text = LocaleUtil.encode(text.trim(), encoding);

				/* Parent id */
				int parentId = rs.getInt(4);

				/* Leaf item */
				boolean isLeaf = (rs.getInt(5) == 1) ? true : false;

				/* Adding to the result map */
				returns.put(id, new TreeSecurityItem(id, text, parentId, isLeaf));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* The result map */
		return returns;
	}

	/** Returns the tree of all banks */
	public Map<Integer, TreeSecurityItem> getAllBanks(Handle languageHandle) throws RemoteException, EJBException {

		/* The result map */
		Map<Integer, TreeSecurityItem> banks = new LinkedHashMap<Integer, TreeSecurityItem>();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			/*
			 * Selecting max id from banks. It is needed to avoid id collision
			 * in hierarchical query (see below)
			 */
			String sql = "select max(a.id) from in_banks a";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			int maxId = 0;
			if (rs.next()) {
				/* There are the banks */
				maxId = rs.getInt(1) + 1;
			}

			sql = "select (a.id + ?) as id, a.code, a.namestrid, 0 as parent_id, 0 as leaf " + "from in_bank_types a " + "union "
					+ "select a.id, a.code, a.namestrid, (a.typeid + ?) as parent_id, 1 as leaf " + "from in_banks a " + "order by parent_id, code ";
			/*
			 * Hierarchical query to select banks and bank types
			 */
			// sql =
			// "select (a.id + ?) as id, a.code, a.namestrid, 0 as parent_id, 0 as leaf, s.value from in_bank_types a, sys_strings s where s.id=a.namestrid and s.langid="
			// + LocaleUtil.getLanguageId(languageHandle) + " union " +
			// "select a.id, a.code, a.namestrid, (a.typeid + ?) as parent_id, 1 as leaf ,s.value "
			// +
			// " from in_banks a ,sys_strings s where s.id=a.namestrid and s.langid="
			// + LocaleUtil.getLanguageId(languageHandle) +
			// "order by parent_id, code ";

			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			ps = con.prepareStatement(sql);
			ps.setInt(1, maxId);
			ps.setInt(2, maxId);
			rs = ps.executeQuery();
			LanguagePK langPK = LocaleUtil.getLanguagePK(languageHandle);

			while (rs.next()) {

				int id = rs.getInt(1);

				String text = "[" + rs.getString(2).trim() + "] "; // +
				// LocaleUtil.getString(con,languageHandle,rs.getInt(3));

				PreparedStatement pstmt = con.prepareStatement("select id from SYS_STRINGS where langID=? and id=?");

				pstmt.setInt(1, langPK.getId());
				pstmt.setInt(2, rs.getInt(3));

				ResultSet rss = pstmt.executeQuery();

				String s = "";
				if (rss.next()) {
					s = LocaleUtil.getString(con, languageHandle, rss.getInt(1));// rss.getString(1);
				} else {
					s = "NONAME";
				}

				text += s;

				int parentId = rs.getInt(4);

				boolean isLeaf = (rs.getInt(5) == 1) ? true : false;

				banks.put(id, new TreeSecurityItem(id, text, parentId, isLeaf));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* The result map */
		return banks;
	}

	/** Prints log information */
	private void printLog(String description, int roleId, String details) {

		StringBuffer logText = new StringBuffer();
		logText.append("Caller user: ");
		logText.append(ctx.getCallerPrincipal().getName());
		logText.append(". User id: ");
		logText.append(roleId);
		logText.append(". " + details);

		log.info(description);
		log.info(logText.toString());
	}

	/* Unnecessary in current implementation */
	public void ejbCreate() {
	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	public void ejbRemove() {
	}

}
