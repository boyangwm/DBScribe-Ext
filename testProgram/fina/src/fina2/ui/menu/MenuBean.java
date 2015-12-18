/*
 * MenuBean.java
 *
 * Created on October 16, 2001, 1:54 PM
 */

package fina2.ui.menu;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

/**
 * @ejbHome <{MenuHome}>
 * @ejbPrimaryKey <{MenuPK}>
 * @ejbRemote <{Menu}>*/
public class MenuBean implements EntityBean {

	private EntityContext ctx;
	private boolean store = true;

	public MenuPK pk;
	public int parentID;
	public int description;
	public int type = -1;
	public String actionKey;
	public String application;
	private String oldDescription;
	private LoggerHelper log = new LoggerHelper(MenuBean.class, "Menu");

	public MenuPK ejbCreate(MenuPK parentPK) throws EJBException,
			CreateException {
		MenuPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {
			type = 0;
			parentID = parentPK.getId();

			description = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from SYS_MENUS");
			insert = con
					.prepareStatement("insert into SYS_MENUS (id,nameStrID,actionKey,application,parentID,sequence,type) "
							+ "values(?,?,'','',?,?,?)");

			rs = ps.executeQuery();
			rs.next();

			pk = new MenuPK(rs.getInt(1) + 1);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con
					.prepareStatement("select max(sequence) from SYS_MENUS where parentID=?");

			ps.setInt(1, parentPK.getId());
			rs = ps.executeQuery();

			int sequence = 0;
			if (rs.next())
				sequence = rs.getInt(1) + 1;

			insert.setInt(1, pk.getId());
			insert.setInt(2, description);
			insert.setInt(3, parentPK.getId());
			insert.setInt(4, sequence);
			insert.setInt(5, getType());

			insert.executeUpdate();

			actionKey = "";
			application = "";

			log.logObjectCreate(pk.getId(), ctx.getCallerPrincipal().getName());
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	public void ejbPostCreate(MenuPK parentPK) throws EJBException,
			CreateException {
	}

	public MenuPK ejbFindByPrimaryKey(MenuPK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from SYS_MENUS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Menu is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public Collection ejbFindByParent(MenuPK parentPK) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con
					.prepareStatement("select id from SYS_MENUS where parentID=?");
			ps.setInt(1, parentPK.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				v.add(new MenuPK(rs.getInt(1)));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public void ejbActivate() throws EJBException {
		/* Write your code here */
	}

	public void ejbStore() throws EJBException {

		log.logObjectStore();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update SYS_MENUS set " + "parentID=?, "
					+ "nameStrID=?, " + "type=?, " + "actionKey=?, "
					+ "application=? " + "where id=?");
			ps.setInt(1, parentID);
			ps.setInt(2, description);
			ps.setInt(3, getType());
			ps.setString(4, actionKey + " ");
			ps.setString(5, application + " ");
			ps.setInt(6, ((MenuPK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbPassivate() throws EJBException {
		/* Write your code here */
	}

	public void ejbLoad() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select parentID, nameStrID, type, actionKey, application "
							+ "from SYS_MENUS where id=?");
			ps.setInt(1, ((MenuPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			parentID = rs.getInt("parentID");
			description = rs.getInt("nameStrID");
			type = rs.getInt("type");
			actionKey = rs.getString("actionKey");
			application = rs.getString("application");
			actionKey = actionKey == null ? "" : actionKey.trim();
			application = application == null ? "" : application.trim();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void ejbRemove() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement del = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select parentID from SYS_MENUS "
					+ "where id=? ");
			ps.setInt(1, ((MenuPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			rs.next();

			int parentID = rs.getInt(1);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con
					.prepareStatement("select nameStrID from SYS_MENUS where id=?");
			ps.setInt(1, ((MenuPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USER_MENUS "
					+ "where menuID=?");
			ps.setInt(1, ((MenuPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_ROLE_MENUS "
					+ "where menuID=?");
			ps.setInt(1, ((MenuPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/ui/menu/Menu");

			MenuHome home = (MenuHome) PortableRemoteObject.narrow(ref,
					MenuHome.class);
			Collection children = home.findByParent((MenuPK) ctx
					.getPrimaryKey());
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				Menu child = (Menu) iter.next();
				child.remove();
			}

			ps = con.prepareStatement("delete from SYS_MENUS " + "where id=?");
			ps.setInt(1, ((MenuPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			ps = con.prepareStatement("select id from SYS_MENUS "
					+ "where parentID=? " + "order by sequence");
			ps.setInt(1, parentID);
			rs = ps.executeQuery();

			int i = 0;

			while (rs.next()) {
				PreparedStatement update = con
						.prepareStatement("update SYS_MENUS set sequence=? "
								+ "where id=?");
				update.setInt(1, i++);
				update.setInt(2, rs.getInt(1));
				update.executeUpdate();
				update.close();
			}
			int objectId = ((MenuPK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
			log
					.logPropertyValue("type", getMenuType(getType()), objectId,
							user);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void unsetEntityContext() throws EJBException {
		ctx = null;
	}

	public void setEntityContext(EntityContext ctx) throws EJBException {
		this.ctx = ctx;
	}

	public String getDescription(Handle langHandle) throws RemoteException,
			EJBException {
		store = false;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, description);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		this.oldDescription = s;

		return s;
	}

	public void setDescription(Handle langHandle, String desc)
			throws RemoteException, EJBException {
		store = false;

		log.logPropertySet("description", desc, this.oldDescription,
				((MenuPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.oldDescription = desc;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, description, desc);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public int getType() {
		store = false;
		return (type == -1) ? 0 : type;
	}

	public void setType(int type) {
		store = true;
		this.type = type;

		log.logPropertySet("type", getMenuType(type), null, ((MenuPK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
	}

	public String getActionKey() {
		store = false;
		return actionKey;
	}

	public void setActionKey(String actionKey) {
		store = true;
		log.logPropertySet("action key", actionKey, this.actionKey,
				((MenuPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.actionKey = actionKey;
	}

	public String getApplication() {
		store = false;
		return application;
	}

	public void setApplication(String application) {
		store = true;
		log.logPropertySet("application", application, this.application,
				((MenuPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.application = application;
	}

	private String getMenuType(int menuType) {

		String menu;
		if (menuType == MenuConstants.MENU_TYPE) {
			menu = "Menu";
		} else if (menuType == MenuConstants.MENU_ACTION_TYPE) {
			menu = "Menu Item: action";
		} else {
			menu = "Menu Item: application";
		}
		return menu;
	}
}
