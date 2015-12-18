/*
 * MenuSessionBean.java
 *
 * Created on October 16, 2001, 2:31 PM
 */

package fina2.ui.menu;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.security.ServerSecurityUtil;
import fina2.ui.tree.Node;

/**
 * @ejbHome <{MenuSessionHome}>
 * @ejbRemote <{MenuSession}>
 */
public class MenuSessionBean implements SessionBean {

	private SessionContext ctx;

	private static Logger log = Logger.getLogger(MenuSessionBean.class);

	public void ejbCreate() throws CreateException, EJBException,
			RemoteException {
		/* Write your code here */
	}

	public void ejbActivate() throws EJBException {
		/* Write your code here */
	}

	public void ejbPassivate() throws EJBException {
		/* Write your code here */
	}

	public void ejbRemove() throws EJBException {
		/* Write your code here */
	}

	public void setSessionContext(SessionContext ctx) throws EJBException {
		this.ctx = ctx;
	}

	public Node getUserMenuTree(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, EJBException, RemoteException {

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Node root = null;
		try {
			ps = con
					.prepareStatement("select a.id, a.parentID, b.value, a.type, a.actionKey, a.application, a.sequence, b.langID "
							+ "from SYS_MENUS a, SYS_STRINGS b "
							+ "where b.id=a.nameStrID and (b.langID=? or b.langID=1) "
							+ "order by a.parentID, a.sequence, b.langID DESC");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			Hashtable nodes = new Hashtable();
			root = new Node(new MenuPK(0), "        ", new Integer(-1));
			Node node = null;
			while (rs.next()) {
				MenuPK pk = new MenuPK(rs.getInt(1));
				if (node != null) {
					MenuPK prevPK = (MenuPK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}
				MenuPK parent = new MenuPK(rs.getInt(2));

				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				node = new Node(pk, desc, new Integer(rs.getInt(4)));
				switch (((Integer) node.getType()).intValue()) {
				case 1: // Action
					String s = rs.getString(5);
					if (s == null)
						s = new String();
					else
						s = s.trim();
					node.putProperty("actionKey", s);
					break;
				case 2: // Application
					s = rs.getString(6);
					if (s == null)
						s = new String();
					else
						s = s.trim();
					node.putProperty("application", s);
					break;
				}
				if (parent.getId() == 0) {
					root.addChild(node);
				} else {
					Node p = (Node) nodes.get(parent);
					p.addChild(node);
				}
				nodes.put(pk, node);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return root;
	}

	public Node getMenuTree(Handle userHandle, Handle languageHandle)
			throws FinaTypeException, EJBException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.menu.amend");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Node root = null;
		try {
			ps = con
					.prepareStatement("select a.id, a.parentID, b.value, a.type, a.actionKey, a.application, a.sequence, b.langID "
							+ "from SYS_MENUS a, SYS_STRINGS b "
							+ "where b.id=a.nameStrID and (b.langID=? or b.langID=1) "
							+ "order by a.parentID, a.sequence, b.langID DESC");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			Hashtable nodes = new Hashtable();
			root = new Node(new MenuPK(0), "        ", new Integer(-1));
			Node node = null;
			while (rs.next()) {
				MenuPK pk = new MenuPK(rs.getInt(1));
				if (node != null) {
					MenuPK prevPK = (MenuPK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}
				MenuPK parent = new MenuPK(rs.getInt(2));

				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);

				node = new Node(pk, desc, new Integer(rs.getInt(4)));
				switch (((Integer) node.getType()).intValue()) {
				case 1: // Action
					String s = rs.getString(5).trim();
					if (s == null)
						s = new String();
					node.putProperty("actionKey", s);
					break;
				case 2: // Application
					s = rs.getString(6).trim();
					if (s == null)
						s = new String();
					node.putProperty("application", s);
					break;
				}
				if (parent.getId() == 0) {
					root.addChild(node);
				} else {
					Node p = (Node) nodes.get(parent);
					p.addChild(node);
				}
				nodes.put(pk, node);
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return root;
	}

	public void sort(MenuPK pk) throws EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select parentID from SYS_MENUS "
					+ "where id=? ");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();
			rs.next();

			int parentID = rs.getInt(1);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			ps = con.prepareStatement("select id from SYS_MENUS "
					+ "where parentID=? " + "order by sequence");
			ps.setInt(1, parentID);
			rs = ps.executeQuery();

			int i = 0;

			if (rs.next()) {
				PreparedStatement update = con
						.prepareStatement("update SYS_MENUS set sequence=? "
								+ "where id=?");
				update.setInt(1, i++);
				update.setInt(2, rs.getInt(1));
				update.executeUpdate();
				update.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void moveUp(MenuPK pk) throws EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select parentID, sequence from SYS_MENUS "
							+ "where id=? ");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();
			rs.next();

			int parentID = rs.getInt(1);
			int sequence = rs.getInt(2);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (sequence > 0) {
				ps = con.prepareStatement("select id, sequence from SYS_MENUS "
						+ "where parentID=? and (sequence=? or sequence=?) "
						+ "order by sequence DESC");
				ps.setInt(1, parentID);
				ps.setInt(2, sequence);
				ps.setInt(3, sequence - 1);

				rs = ps.executeQuery();
				if (rs.next()) {
					PreparedStatement update = con
							.prepareStatement("update SYS_MENUS set sequence=? "
									+ "where id=?");
					update.setInt(1, sequence - 1);
					update.setInt(2, pk.getId());
					update.executeUpdate();

					DatabaseUtil.closeStatement(update);
				}
				if (rs.next()) {
					PreparedStatement update = con
							.prepareStatement("update SYS_MENUS set sequence=? "
									+ "where id=?");
					update.setInt(1, sequence);
					update.setInt(2, rs.getInt(1));
					update.executeUpdate();

					DatabaseUtil.closeStatement(update);
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void moveDown(MenuPK pk) throws EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select parentID,sequence from SYS_MENUS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			rs.next();

			int parentID = rs.getInt(1);
			int sequence = rs.getInt(2);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con
					.prepareStatement("select max(sequence) from SYS_MENUS where parentID=?");

			ps.setInt(1, parentID);
			rs = ps.executeQuery();
			rs.next();
			int maxsequence = rs.getInt(1);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (sequence < maxsequence) {
				ps = con.prepareStatement("select id, sequence from SYS_MENUS "
						+ "where parentID=? and (sequence=? or sequence=?) "
						+ "order by sequence");
				ps.setInt(1, parentID);
				ps.setInt(2, sequence);
				ps.setInt(3, sequence + 1);

				rs = ps.executeQuery();
				if (rs.next()) {
					PreparedStatement update = con
							.prepareStatement("update SYS_MENUS set sequence=? "
									+ "where id=?");
					update.setInt(1, sequence + 1);
					update.setInt(2, pk.getId());
					update.executeUpdate();

					DatabaseUtil.closeStatement(update);
				}
				if (rs.next()) {
					PreparedStatement update = con
							.prepareStatement("update SYS_MENUS set sequence=? "
									+ "where id=?");

					update.setInt(1, sequence);
					update.setInt(2, rs.getInt(1));
					update.executeUpdate();

					DatabaseUtil.closeStatement(update);
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void copyPaste(Collection pks, MenuPK newParent, boolean cut) {
	}
}
