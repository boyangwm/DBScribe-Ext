/*
 * ReportBean.java
 *
 * Created on January 7, 2002, 3:33 PM
 */

package fina2.reportoo.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.reportoo.ReportConstants;
import fina2.reportoo.ReportInfo;
import fina2.security.UserPK;
import fina2.util.LoggerHelper;

public class ReportBean implements EntityBean {

	private EntityContext ctx;

	public ReportPK pk;
	public int description;
	public int parentID;
	public int type;
	public byte[] template;
	public ReportInfo info;
	public Hashtable langTemplates;
	public Hashtable langTemplatesNew;
	private String oldDescription;
	private LoggerHelper log = new LoggerHelper(ReportBean.class, "Report");

	public ReportPK ejbCreate(Handle userHandle, ReportPK parentPK) throws EJBException, CreateException {
		pk = null;
		int userID = 0;
		try {
			userID = ((UserPK) userHandle.getEJBObject().getPrimaryKey()).getId();
		} catch (Exception e) {
			throw new EJBException(e);
		}
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			type = ReportConstants.NODETYPE_FOLDER;
			parentID = parentPK.getId();

			description = LocaleUtil.allocateString(con);

			info = new ReportInfo();

			ps = con.prepareStatement("select max(id) from OUT_REPORTS");

			rs = ps.executeQuery();

			insert = con.prepareStatement("insert into OUT_REPORTS ( " + "id,nameStrID,parentID,type) " + "values(?,?,?,?)");

			if (rs.next())
				pk = new ReportPK(rs.getInt(1) + 1);
			else
				pk = new ReportPK(1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, description);
			insert.setInt(3, parentPK.getId());
			insert.setInt(4, type);

			insert.executeUpdate();
			DatabaseUtil.closeStatement(insert);

			insert = con.prepareStatement("insert into SYS_USER_REPORTS " + "(userID, reportID) " + "values (?,?)");
			insert.setInt(1, userID);
			insert.setInt(2, pk.getId());
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

	public void ejbPostCreate(Handle userHandle, ReportPK parentPK) throws EJBException, CreateException {
	}

	public ReportPK ejbFindByPrimaryKey(ReportPK pk) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from OUT_REPORTS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Report is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void ejbStore() throws javax.ejb.EJBException, java.rmi.RemoteException {

		log.logObjectStore();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update OUT_REPORTS set " + "nameStrID=?, " + "parentID=?, " + "type=?, " + "template=?, " + "info=? " + "where id=?");
			ps.setInt(1, description);
			ps.setInt(2, parentID);
			ps.setInt(3, type);
			if (!DatabaseUtil.isOracle()) {
				ps.setBytes(4, template);
			} else {
				if (template == null) {
					ps.setBytes(4, null);
				} else {
					ps.setBinaryStream(4, new java.io.ByteArrayInputStream(template), template.length);
				}
			}

			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(info);

			byte[] b = bo.toByteArray();
			if (!DatabaseUtil.isOracle()) {
				ps.setBytes(5, b);
			} else {
				ps.setBinaryStream(5, new java.io.ByteArrayInputStream(b), b.length);
			}

			oo.close();
			bo.close();

			ps.setInt(6, ((ReportPK) ctx.getPrimaryKey()).getId());

			if (template == null)
				log.getLogger().debug("store template null");
			else
				log.getLogger().debug("store template " + template.length);
			ps.executeUpdate();

			PreparedStatement update = con.prepareStatement("update OUT_REPORTS_LANG set " + "template=? " + "where reportID=? and langID=?");
			if (langTemplates == null)
				langTemplates = new Hashtable();
			if (langTemplatesNew == null)
				langTemplatesNew = new Hashtable();
			for (Iterator iter = langTemplates.keySet().iterator(); iter.hasNext();) {
				LanguagePK lpk = (LanguagePK) iter.next();
				byte[] temp = (byte[]) langTemplates.get(lpk);
				if (!DatabaseUtil.isOracle()) {
					update.setBytes(1, temp);
				} else {
					update.setBinaryStream(1, new java.io.ByteArrayInputStream(temp), temp.length);
				}
				update.setInt(2, ((ReportPK) ctx.getPrimaryKey()).getId());
				update.setInt(3, lpk.getId());
				update.executeUpdate();
			}
			DatabaseUtil.closeStatement(update);

			if (langTemplatesNew.size() > 0) {
				PreparedStatement insert = con.prepareStatement("insert into OUT_REPORTS_LANG (reportID,langID,template) " + "values(?,?,?)");

				for (Iterator iter = langTemplatesNew.keySet().iterator(); iter.hasNext();) {
					LanguagePK lpk = (LanguagePK) iter.next();
					byte[] temp = (byte[]) langTemplatesNew.get(lpk);
					insert.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());
					insert.setInt(2, lpk.getId());
					if (!DatabaseUtil.isOracle()) {
						insert.setBytes(3, temp);
					} else {
						insert.setBinaryStream(3, new java.io.ByteArrayInputStream(temp), temp.length);
					}
					insert.executeUpdate();
					langTemplates.put(lpk, temp);
					langTemplatesNew.remove(lpk);
				}
				DatabaseUtil.closeStatement(insert);
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbLoad() throws javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select nameStrID, parentID, type, template, info " + "from OUT_REPORTS where id=?");
			ps.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			description = rs.getInt("nameStrID");
			parentID = rs.getInt("parentID");
			type = rs.getInt("type");
			if (!DatabaseUtil.isOracle()) {
				template = rs.getBytes("template");
			} else {
				template = (byte[]) rs.getObject("template");
			}
			if (template == null)
				template = new byte[0];

			byte[] buf = null; // new byte[0]; //rs.getBytes("info");
			if (!DatabaseUtil.isOracle()) {
				buf = rs.getBytes("info");
			} else {
				ByteArrayOutputStream st = new ByteArrayOutputStream();
				java.io.InputStream in = rs.getBinaryStream("info");
				ReportBean.copy(in, st);
				buf = st.toByteArray();
				st.close();
			}
			try {
				ByteArrayInputStream bi = new ByteArrayInputStream(buf);
				ObjectInputStream oi = new ObjectInputStream(bi);
				info = (ReportInfo) oi.readObject();
				oi.close();
				bi.close();
			} catch (Exception e) {
				log.getLogger().error(e.getMessage(), e);
			}

			if (buf == null)
				log.getLogger().debug("load info null");
			else
				log.getLogger().debug("load info " + buf.length);

			if (template == null)
				log.getLogger().debug("load template null");
			else
				log.getLogger().debug("load template " + template.length);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			langTemplates = new Hashtable();
			langTemplatesNew = new Hashtable();
			ps = con.prepareStatement("select langID,template from OUT_REPORTS_LANG " + "where reportID=?");
			ps.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();

			while (rs.next()) {
				LanguagePK lpk = new LanguagePK(rs.getInt(1));
				byte[] temp = null;
				if (!DatabaseUtil.isOracle()) {
					temp = rs.getBytes("template");
				} else {
					temp = (byte[]) rs.getObject("template");
				}
				if (temp == null)
					temp = new byte[0];
				langTemplates.put(lpk, temp);
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public void ejbRemove() throws javax.ejb.RemoveException, javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			ps = con.prepareStatement("select nameStrID from OUT_REPORTS where id=?");
			ps.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from OUT_STORED_REPORTS " + "where reportID=?");
			ps.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from OUT_REPORTS_LANG " + "where reportID=?");
			ps.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from OUT_REPORTS " + "where id=?");
			ps.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USER_REPORTS " + "where reportID=?");
			ps.setInt(1, ((ReportPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

			int objectId = ((ReportPK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);

		} catch (SQLException e) {
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

	public String getDescription(Handle langHandle) throws RemoteException, EJBException {
		// store = false;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, description);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		oldDescription = s;

		return s;
	}

	public void setDescription(Handle langHandle, String desc) throws RemoteException, EJBException {
		// store = false;
		String propertyName = "Folder name";
		if (getType() == ReportConstants.NODETYPE_REPORT) {
			propertyName = "Report name";
		}
		log.logPropertySet(propertyName, desc, this.oldDescription, ((ReportPK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		this.oldDescription = desc;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select b.id from OUT_REPORTS a, SYS_STRINGS b " + "where a.nameStrID=b.id and b.langID=? and a.id!=? and ltrim(rtrim(b.value))=?");
			ps.setInt(1, langPK.getId());
			ps.setInt(2, ((ReportPK) ctx.getPrimaryKey()).getId());
			ps.setString(3, desc);
			rs = ps.executeQuery();
			if (rs.next()) {
				throw new EJBException("Report name is not unique");
			}
			LocaleUtil.setString(con, langPK, description, desc);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

	}

	public int getType() {
		return type;
	}

	public ReportPK getParentPK() {
		return new ReportPK(parentID);
	}

	public void setType(int type) {
		this.type = type;
	}

	public ReportInfo getInfo() {
		return info;
	}

	public void setInfo(ReportInfo info) {
		this.info = info;
	}

	public byte[] getTemplate() throws EJBException {
		return template;
	}

	public byte[] getLangTemplate(LanguagePK langPK) throws EJBException {

		byte[] langTemplate = null;

		if (langTemplates == null)
			langTemplates = new Hashtable();
		if (langTemplatesNew == null)
			langTemplatesNew = new Hashtable();

		langTemplate = (byte[]) langTemplates.get(langPK);
		if (langTemplate == null)
			langTemplate = (byte[]) langTemplatesNew.get(langPK);

		return langTemplate;
	}

	public void setTemplate(LanguagePK langPK, byte[] template) throws EJBException {
		if (langTemplates == null)
			langTemplates = new Hashtable();
		if (langTemplatesNew == null)
			langTemplatesNew = new Hashtable();

		if (template == null)
			log.getLogger().debug("setTemplate null");
		else
			log.getLogger().debug("setTemplate " + template.length);

		this.template = template;
		try {
			// LanguagePK langPK =
			// (LanguagePK)langHandle.getEJBObject().getPrimaryKey();
			byte[] temp = (byte[]) langTemplates.get(langPK);
			if (temp == null) {
				langTemplatesNew.put(langPK, template);
			} else {
				langTemplates.put(langPK, template);
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		}
	}
}
