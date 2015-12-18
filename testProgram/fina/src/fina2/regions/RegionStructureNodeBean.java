package fina2.regions;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

@SuppressWarnings({ "serial", "unused" })
public class RegionStructureNodeBean implements EntityBean {
	private EntityContext ctx;

	private RegionStructureNodePK pk;

	private String code;
	private long description;
	private long parentId;
	private String oldDescription;
	private int sequence;

	public static boolean inUsed = true;

	private LoggerHelper log = new LoggerHelper(RegionStructureNodeBean.class, "RegionStructureNode");
	Logger l = log.getLogger();

	public RegionStructureNodePK ejbCreate() throws EJBException, CreateException {
		RegionStructureNodePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;

		try {
			ps = con.prepareStatement("SELECT max(id) FROM IN_COUNTRY_DATA");
			insert = con.prepareStatement("INSERT INTO IN_COUNTRY_DATA (id) VALUES (?)");
			rs = ps.executeQuery();
			rs.next();
			// initial description
			description = LocaleUtil.allocateString(con);

			pk = new RegionStructureNodePK(rs.getInt(1) + 1);

			insert.setLong(1, pk.getId());
			insert.executeUpdate();
			/*
			 * Set Default sequence
			 */
			ps = con.prepareStatement("SELECT max(sequence) FROM IN_COUNTRY_DATA");
			rs = ps.executeQuery();
			rs.next();
			sequence = rs.getInt(1) + 1;

			log.logObjectCreate(pk.getId(), ctx.getCallerPrincipal().getName());
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	public void ejbPostCreate() throws EJBException, CreateException {
	}

	public RegionStructureNodePK ejbFindByPrimaryKey(RegionStructureNodePK pk) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_COUNTRY_DATA where id=?");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Region is not found in database.");
			}
		} catch (SQLException e) {
			log.getLogger().debug(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void ejbActivate() throws EJBException {
		/* Write your code here */
	}

	public void ejbStore() throws EJBException {
		log.logObjectStore();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update IN_COUNTRY_DATA set code=? ,parentid=? ,namestrid=? , sequence=? where id=?");
			ps.setString(1, code);
			ps.setLong(2, parentId);
			ps.setLong(3, description);
			ps.setInt(4, sequence);
			ps.setLong(5, ((RegionStructureNodePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
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
			ps = con.prepareStatement("select code, nameStrID, parentID,sequence from IN_COUNTRY_DATA where id=?");
			ps.setLong(1, ((RegionStructureNodePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			if (rs.next()) {
				code = rs.getString(1);
				description = rs.getLong(2);
				parentId = rs.getLong(3);
				sequence = rs.getInt(4);
			}
		} catch (Exception e) {
			l.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void ejbRemove() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Map<Long, Long> map = getDeletedNodes(new HashMap<Long, Long>(), (RegionStructureNodePK) ctx.getPrimaryKey());
			// get banks region
			Set<Long> al = new HashSet<Long>();
			ps = con.prepareStatement("select regionid from IN_BANKS");
			rs = ps.executeQuery();
			while (rs.next()) {
				al.add(rs.getLong(1));
			}

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			Set<Long> regions = map.keySet();

			// is used!
			inUsed = true;
			for (long b : al) {
				for (long r : regions) {
					if (b == r) {
						inUsed = false;
						return;
					}
				}
			}

			// Delete description
			ps = con.prepareStatement("delete from SYS_STRINGS where id In" + createSetStrings(map.values()));
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			// Delete Regions
			ps = con.prepareStatement("delete from IN_COUNTRY_DATA where id In" + createSetStrings(map.keySet()));
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			// Logged info
			long objectId = ((RegionStructureNodePK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
			log.logPropertyValue("code", this.code, objectId, user);
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
		}
	}

	private static String createSetStrings(Collection<Long> set) {
		StringBuilder sb = new StringBuilder("(");
		for (Long l : set) {
			sb.append(l.toString());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}

	private Map<Long, Long> getDeletedNodes(Map<Long, Long> map, RegionStructureNodePK pk) {
		Connection con = DatabaseUtil.getConnection();
		long nstr = 0;
		try {
			PreparedStatement ps = con.prepareStatement("select nameStrID from IN_COUNTRY_DATA where id=?");
			ps.setLong(1, pk.getId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				nstr = rs.getLong(1);
			}

			// Closed Connections
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			Collection<RegionStructureNodePK> children = findChilds(pk);

			for (Iterator<RegionStructureNodePK> iter = children.iterator(); iter.hasNext();) {
				getDeletedNodes(map, iter.next());
			}
			map.put(pk.getId(), nstr);
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return map;
	}

	// find parent
	public Collection<RegionStructureNodePK> findChilds(RegionStructureNodePK parentPK) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<RegionStructureNodePK> list = new ArrayList<RegionStructureNodePK>();
		try {
			ps = con.prepareStatement("select id from IN_COUNTRY_DATA where parentID=?");
			ps.setLong(1, parentPK.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new RegionStructureNodePK(rs.getLong(1)));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return list;
	}

	public void unsetEntityContext() throws EJBException {
		ctx = null;
	}

	public void setEntityContext(EntityContext ctx) throws EJBException {
		this.ctx = ctx;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code, boolean isAmend) throws EJBException, FinaTypeException {
		log.logPropertySet("code", code, this.code, ((RegionStructureNodePK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT id FROM IN_COUNTRY_DATA WHERE rtrim(code)=?");
			ps.setString(1, code.trim());
			rs = ps.executeQuery();
			if (!isAmend) {
				if (rs.next()) {
					getFinaTypeException(code);
				}
			} else {
				if (rs.next()) {
					if (((RegionStructureNodePK) ctx.getPrimaryKey()).getId() != rs.getLong(1)) {
						getFinaTypeException(code);
					}
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		this.code = code.trim();
	}

	private void getFinaTypeException(String code) throws FinaTypeException {
		throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] { "FinaTypeException.Language", code });
	}

	public EntityContext getCtx() {
		return ctx;
	}

	public void setCtx(EntityContext ctx) {
		this.ctx = ctx;
	}

	public String getDescription(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, (int) description);
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		oldDescription = s;

		return s;
	}

	public void setDescription(Handle langHandle, String desc) throws RemoteException, EJBException {
		log.logPropertySet("description", desc, this.oldDescription, ((RegionStructureNodePK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		this.oldDescription = desc;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();

		try {
			LocaleUtil.setString(con, langPK, (int) description, desc);
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public boolean isInUsed() throws RemoteException, EJBException {
		return inUsed;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public LoggerHelper getLog() {
		return log;
	}

	public void setLog(LoggerHelper log) {
		this.log = log;
	}

	public int getSequence() throws RemoteException, EJBException {
		return sequence;
	}
}
