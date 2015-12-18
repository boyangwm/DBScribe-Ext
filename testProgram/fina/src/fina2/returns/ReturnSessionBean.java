package fina2.returns;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.bank.BankPK;
import fina2.bank.BankSessionBean;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.metadata.MDTConstants;
import fina2.metadata.MDTNodePK;
import fina2.period.PeriodPK;
import fina2.security.ServerSecurityUtil;
import fina2.security.User;
import fina2.security.UserBean;
import fina2.security.UserPK;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.Node;
import fina2.util.CommonUtils;

public class ReturnSessionBean implements SessionBean {

	private SessionContext ctx;
	private static Logger log = Logger.getLogger(ReturnSessionBean.class);

	// private int length=40;

	public void toAuditLog(String str, Handle userHandle, Handle languageHandle) throws RemoteException {
		try {
			fina2.security.User user = (fina2.security.User) userHandle.getEJBObject();
			log.info(user.getLogin() + " " + str.replace("\"", "").replace(",", " "));
		} catch (Exception e) {
			log.debug(e);
		}

	}

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
		/* Write your code here */
	}

	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void setSessionContext(javax.ejb.SessionContext ctx) throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.ctx = ctx;
	}

	public Collection getReturnTypesRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, EJBException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.returns.definition.review", "fina2.returns.definition.amend");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, a.code, b.value, b.langID from IN_RETURN_TYPES a left outer join  SYS_STRINGS b  on b.id=a.nameStrID and (b.langID=?) order by a.code asc");
			ps.setInt(1, langID);
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new ReturnTypePK(rs.getInt(1)), 2);
				String s = rs.getString(2);
				if (s == null)
					s = "";
				row.setValue(0, s.trim());

				String desc = rs.getString(3);
				if (desc == null)
					desc = "NONAME";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(1, desc);

				if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);

			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return v;
	}

	/** Returns a list of returns of given user */
	public Collection getReturnDefinitionsRows(Handle userHandle, Handle languageHandle) throws fina2.FinaTypeException, RemoteException {

		/* Can throw PermissionDeniedException */
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.returns.definition.review", "fina2.returns.definition.amend");

		Vector<TableRowImpl> returns = new Vector<TableRowImpl>();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			/* Selecting a given user returns */
			String sql = "select a.id, a.code, a.namestrid, b.namestrid as type_str_id " + "from IN_RETURN_DEFINITIONS a, IN_RETURN_TYPES b " + "where a.typeid = b.id and a.id in " + "( "
					+ "  select a.definition_id from sys_user_returns a where a.user_id = ? " + "  union " + "  select a.definition_id from sys_role_returns a where a.role_id in "
					+ "  (select a.roleid from sys_users_roles a where a.userid = ?) " + ") " + "order by a.code";

			ps = con.prepareStatement(sql);

			int userId = UserBean.getUserId(userHandle);
			ps.setInt(1, userId);
			ps.setInt(2, userId);

			rs = ps.executeQuery();

			/* Copying the result set to the result list */
			while (rs.next()) {

				int id = rs.getInt(1);
				String code = rs.getString(2);
				String desc = LocaleUtil.getString(con, languageHandle, rs.getInt(3));
				String type = LocaleUtil.getString(con, languageHandle, rs.getInt(4));

				TableRowImpl record = new TableRowImpl(new ReturnDefinitionPK(id), 3);
				record.setValue(0, code);
				record.setValue(1, desc);
				record.setValue(2, type);

				/* Adding to the result list */
				returns.add(record);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* The result list: contains only given user returns */
		return returns;
	}

	public Collection getAllDefinitionTables(Handle languageHandle) throws RemoteException, EJBException {

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, c.id, c.type, b.id, a.code, d.value, c.code, e.value "
					+ "from IN_RETURN_DEFINITIONS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c, SYS_STRINGS d, SYS_STRINGS e " + "where c.definitionID=a.id and b.id=c.nodeID and  "
					+ "      d.id=a.nameStrID and (d.langID=1 or d.langID=?) and  " + "      e.id=b.nameStrID and e.langID=d.langID " + "order by a.id,c.id,d.langID DESC ");

			ps.setInt(1, langID);
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new ReturnDefinitionTablePK(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)), 4);

				// String code1 = LocaleUtil.encode(rs.getString(5).trim(),
				// encoding);
				String desc1 = rs.getString(6);
				if (desc1 == null)
					desc1 = "";
				else
					desc1 = LocaleUtil.encode(desc1.trim(), encoding);
				// String code2 = LocaleUtil.encode(rs.getString(7).trim(),
				// encoding);
				String desc2 = rs.getString(8);
				if (desc2 == null)
					desc2 = "";
				else
					desc2 = LocaleUtil.encode(desc2.trim(), encoding);

				row.setValue(0, rs.getString(5).trim());
				row.setValue(1, desc1);
				row.setValue(2, rs.getString(7).trim());
				row.setValue(3, desc2);

				if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);

			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return v;
	}

	public Collection getDefinitionTables(Handle languageHandle, ReturnDefinitionPK pk) throws RemoteException, EJBException {

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		if (pk == null)
			return v;
		try {
			ps = con.prepareStatement(

			"select a.code, a.type, c.code,  b.value, a.nodeid, a.nodevisible, a.visibleLevel, a.evaltype, a.id, b.langID " + "from IN_DEFINITION_TABLES a, SYS_STRINGS b, IN_MDT_NODES c "
					+ "where a.definitionId=? and c.id=a.nodeid and (b.langID=? or b.langID=1) and b.id=c.nameStrID " + "order by a.id, b.langID DESC");

			ps.setLong(1, pk.getId());
			ps.setInt(2, langID);
			rs = ps.executeQuery();

			log.info("Getting Return Definition parameters. Return Definition id: " + pk.getId());

			DefinitionTable prevRow = null;
			while (rs.next()) {
				DefinitionTable row = new DefinitionTable();

				String desc = rs.getString(4);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);

				row.setCode(rs.getString(1).trim());

				row.setType(rs.getInt(2));
				String s = rs.getString(3);
				if (s == null)
					s = "";
				row.setNodeName("[" + s.trim() + "] " + desc);
				row.setNode(new MDTNodePK(rs.getLong(5)));
				row.setNodeVisible(rs.getInt(6));
				row.setLevel(rs.getInt(7));
				row.setEvalType(rs.getInt(8));

				if ((prevRow != null) && (prevRow.getCode().equals(row.getCode()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				// { Logging
				StringBuffer buff = new StringBuffer();

				buff.append(" Caller user: ");
				buff.append(ctx.getCallerPrincipal().getName());
				buff.append(". Node id: ");
				buff.append(row.getNode().getId());
				buff.append(", Level: ");
				buff.append(row.getLevel());
				buff.append(", Type: ");
				buff.append(getType(row.getType()));
				buff.append(", Eval Type: ");
				buff.append(getEvalType(row.getEvalType()));

				log.info(buff.toString());
				// }

				v.add(row);

			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection getDefinitionTablesFormat(Handle languageHandle, ReturnDefinitionPK pk) throws RemoteException, EJBException {

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		if (pk == null)
			return v;
		try {
			String sql = "select a.code, a.type, c.code,  b.value, a.nodeid, a.nodevisible, a.visibleLevel, a.evaltype, a.id, b.langID from IN_DEFINITION_TABLES a, IN_MDT_NODES c left outer join SYS_STRINGS b on b.id=c.nameStrID and (b.langID=?) where (a.definitionId=?) and c.id=a.nodeid";

			ps = con.prepareStatement(sql);

			ps.setInt(1, langID);
			ps.setLong(2, pk.getId());

			rs = ps.executeQuery();

			DefinitionTable prevRow = null;
			while (rs.next()) {
				DefinitionTable row = new DefinitionTable();
				row.setCode(rs.getString(1).trim());
				row.setType(rs.getInt(2));
				String desc = rs.getString(4);
				if (desc == null) {
					desc = "NONAME";
				} else
					desc = LocaleUtil.encode(desc, encoding);
				row.setNodeName(desc);
				row.setNode(new MDTNodePK(rs.getLong(5)));
				row.setNodeVisible(rs.getInt(6));
				row.setLevel(rs.getInt(7));
				row.setEvalType(rs.getInt(8));

				if ((prevRow != null) && (prevRow.getCode().equals(row.getCode()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);

			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection getReturnTables(Handle languageHandle, ReturnPK pk) throws RemoteException, EJBException {
		// length=40;
		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		if (pk == null)
			return v;
		try {
			// ps =
			// con.prepareStatement("select a.code, a.type, c.code,  b.value, a.nodeid, a.nodevisible, a.visibleLevel, a.evaltype, a.id, b.langID "
			// +
			// "from IN_DEFINITION_TABLES a, SYS_STRINGS b, IN_MDT_NODES c, IN_RETURNS d, IN_SCHEDULES e "
			// +
			// "where d.id=? and c.id=a.nodeID and (b.langID=?) and b.id=c.nameStrID and d.scheduleID=e.id and a.definitionID=e.definitionID "
			// + "order by a.id, b.langID DESC");
			ps = con.prepareStatement("select a.code, a.type, c.code,  b.value, a.nodeid, a.nodevisible, a.visibleLevel, a.evaltype, a.id, b.langID "
					+ " from IN_DEFINITION_TABLES a,  IN_MDT_NODES c left join SYS_STRINGS b on (b.langID=?) and b.id=c.nameStrID, IN_RETURNS d, IN_SCHEDULES e "
					+ " where d.id=? and c.id=a.nodeID and d.scheduleID=e.id and a.definitionID=e.definitionID " + " order by a.id, b.langID DESC");
			ps.setInt(1, langID);
			ps.setInt(2, pk.getId());

			rs = ps.executeQuery();

			DefinitionTable prevRow = null;
			while (rs.next()) {
				DefinitionTable row = new DefinitionTable();
				row.setCode(rs.getString(1).trim());
				row.setType(rs.getInt(2));
				String desc = rs.getString(4);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setNodeName( /* "["+rs.getString(3).trim()+"] "+ */desc);
				row.setNode(new MDTNodePK(rs.getLong(5)));
				row.setNodeVisible(rs.getInt(6));
				row.setLevel(rs.getInt(7));
				row.setEvalType(rs.getInt(8));

				if ((prevRow != null) && (prevRow.getCode().equals(row.getCode()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);

			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public void setReturnDefinitionReviewFormat(ReturnDefinitionPK pk, byte[] fv) throws FinaTypeException, RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
			// "update IN_RETURN_DEFINITIONS "+
			// "set format=? "+
			// "where id in ("+
			// "select a.definitionID "+
			// "from IN_SCHEDULES a, IN_RETURNS b "+
			// "where a.id=b.scheduleID and b.id=?)"

			"update IN_RETURN_DEFINITIONS " + "set format=? " + "where id=?");
			// pstmt.setBytes(1,fv);
			ps.setBinaryStream(1, new java.io.ByteArrayInputStream(fv), fv.length);
			// pstmt.setObject(1,null);
			ps.setLong(2, pk.getId());
			ps.executeUpdate();

			StringBuffer buff = new StringBuffer();
			buff.append("Setting Return Definition format. Return Definition id: ");
			buff.append(pk.getId());
			buff.append(". Caller user: ");
			buff.append(ctx.getCallerPrincipal().getName());

			log.info(buff.toString());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public byte[] getReturnDefinitionReviewFormat(ReturnDefinitionPK pk) throws FinaTypeException, RemoteException, EJBException {
		byte[] a = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select c.format from IN_RETURN_DEFINITIONS c " + "where c.id=?");

			// pstmt.setObject(1,fv);
			// pstmt.setObject(1,null);
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				a = (byte[]) rs.getObject(1);
			}

			// log.debug(a.size());
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return a;
	}

	public byte[] getReturnReviewFormat(ReturnPK pk) throws FinaTypeException, RemoteException, EJBException {
		byte[] a = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select c.format from IN_RETURN_DEFINITIONS c " + "where id in (" + "select a.definitionID " + "from IN_SCHEDULES a, IN_RETURNS b "
					+ "where a.id=b.scheduleID and b.id=?)");

			// pstmt.setObject(1,fv);
			// pstmt.setObject(1,null);
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				a = (byte[]) rs.getObject(1);
			}

			// log.debug(a.size());
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return a;
	}

	public void setDefinitionTables(Handle languageHandle, ReturnDefinitionPK pk, Collection tables) throws FinaTypeException, RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement del = null;
		try {
			Language lang = (Language) languageHandle.getEJBObject();
			int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
			String encoding = lang.getXmlEncoding();

			del = con.prepareStatement("delete from IN_DEFINITION_TABLES where definitionid=?");
			del.setLong(1, pk.getId());
			del.executeUpdate();

			ps = con.prepareStatement("insert into IN_DEFINITION_TABLES (id,code,definitionid,nodeid,nodevisible,visibleLevel,type,evaltype) values (?,?,?,?,?,?,?,?)");

			log.info("Setting Return Definition parameters. Return Definition id: " + pk.getId());

			int i = 1;
			for (java.util.Iterator iter = tables.iterator(); iter.hasNext(); i++) {

				DefinitionTable table = (DefinitionTable) iter.next();

				// byte[] buf =
				// table.getCode(languageHandle).getBytes(lang.getXmlEncoding
				// ());
				// String s = new String(buf, 0);

				// log.debug("set "+s);

				ps.setInt(1, i);
				ps.setString(2, table.getCode() + " ");
				ps.setLong(3, pk.getId());
				ps.setLong(4, table.getNode().getId());
				ps.setInt(5, table.getNodeVisible());
				ps.setInt(6, table.getLevel());
				ps.setInt(7, table.getType());
				ps.setInt(8, table.getEvalType());
				ps.addBatch();
				StringBuffer buff = new StringBuffer();

				buff.append(" Node id: ");
				buff.append(table.getNode().getId());
				buff.append(", Level: ");
				buff.append(table.getLevel());
				buff.append(", Type: ");
				buff.append(getType(table.getType()));
				buff.append(", Eval Type: ");
				buff.append(getEvalType(table.getEvalType()));

				log.info(buff.toString());

				// ps.executeUpdate();
			}
			ps.executeBatch();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(del);
			DatabaseUtil.closeStatement(ps);
		}
	}

	private void createDefaultValues(Connection con, int returnId, int versionId) throws FinaTypeException, SQLException {

		PreparedStatement pstmt = con.prepareStatement("select dt.id, dt.nodeID, dt.nodeVisible, dt.type " + "from IN_DEFINITION_TABLES dt, IN_SCHEDULES s, IN_RETURNS r "
				+ "where dt.definitionID=s.definitionID and s.id=r.scheduleid and r.id=?");

		pstmt.setInt(1, returnId);
		ResultSet tablesRs = pstmt.executeQuery();

		PreparedStatement rootNodesStmt = con.prepareStatement("select id, type, sequence from IN_MDT_NODES " + "where parentID=? and disabled=0 order by sequence");

		while (tablesRs.next()) {
			int tableID = tablesRs.getInt(1);
			long tableNodeID = tablesRs.getLong(2);
			int tableType = tablesRs.getInt(4);

			Vector cols = new Vector();
			switch (tableType) {
			case ReturnConstants.TABLETYPE_MULTIPLE:
				rootNodesStmt.setLong(1, tableNodeID);
				ResultSet rootNodesRs = rootNodesStmt.executeQuery();
				while (rootNodesRs.next()) {
					long rootNodeID = rootNodesRs.getLong(1);
					int rootNodeType = rootNodesRs.getInt(2);
					if (rootNodeType != MDTConstants.NODETYPE_NODE)
						continue;

					cols.add(selectChildNodes(rootNodeID, con));
				}
				int rowCount = -1;
				int coln = 1;
				for (Iterator iter = cols.iterator(); iter.hasNext(); coln++) {
					Vector rows = (Vector) iter.next();
					if (rowCount == -1) {
						rowCount = rows.size();
					} else if (rowCount != rows.size()) {
						rootNodesRs.close();
						rootNodesStmt.close();
						tablesRs.close();
						pstmt.close();

						log.error("WrongMDT Column: " + coln + " required rows: " + rowCount + " exist rows: " + rows.size());
						throw new RuntimeException("WrongMDT Column: " + coln + " required rows: " + rowCount + " exist rows: " + rows.size());
					}
				}
				PreparedStatement insert = con.prepareStatement("insert into IN_RETURN_ITEMS " + "(id,returnID,tableID,nodeID,rowNumber,value,versionID) " + "values (?,?,?,?,?,\'\',?)");
				int idCount = 1;
				for (int i = 0; i < rowCount; i++) {
					int j = 0;
					for (Iterator iter = cols.iterator(); iter.hasNext(); j++) {
						long nodeID = ((Long) ((Vector) iter.next()).elementAt(i));
						insert.setInt(1, idCount);
						insert.setInt(2, returnId);
						insert.setInt(3, tableID);
						insert.setLong(4, nodeID);
						insert.setInt(5, i);
						insert.setInt(6, versionId);
						insert.addBatch();
						// insert.executeUpdate();
						idCount++;
					}
				}
				insert.executeBatch();
				insert.close();
				rootNodesRs.close();
				break;
			case ReturnConstants.TABLETYPE_NORMAL:
				cols.add(selectChildNodes(tableNodeID, con));
				rowCount = -1;
				for (Iterator iter = cols.iterator(); iter.hasNext();) {
					Vector rows = (Vector) iter.next();
					if (rowCount == -1)
						rowCount = rows.size();
				}
				insert = con.prepareStatement("insert into IN_RETURN_ITEMS " + "(id,returnID,tableID,nodeID,rowNumber,value,versionID) " + "values (?,?,?,?,?,\'\',?)");
				idCount = 1;
				for (int i = 0; i < rowCount; i++) {
					int j = 0;
					for (Iterator iter = cols.iterator(); iter.hasNext(); j++) {
						long nodeID = ((Long) ((Vector) iter.next()).elementAt(i));
						insert.setInt(1, idCount);
						insert.setInt(2, returnId);
						insert.setInt(3, tableID);
						insert.setLong(4, nodeID);
						insert.setInt(5, i);
						insert.setInt(6, versionId);
						insert.addBatch();
						// insert.executeUpdate();
						idCount++;
					}
				}
				insert.executeBatch();
				insert.close();
				break;
			case ReturnConstants.TABLETYPE_VARIABLE:
				break;
			}
		}

		rootNodesStmt.close();

		tablesRs.close();
		pstmt.close();
	}

	public ReturnPK createReturn(Handle userHandle, Handle languageHandle, SchedulePK schedulePK, String versionCode) throws RemoteException, EJBException, FinaTypeException {

		ReturnPK pk = null;

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			int versionId = getVersionId(con, versionCode);
			int returnId = -1;

			ps = con.prepareStatement("select id from IN_RETURNS where scheduleID=?");
			ps.setInt(1, schedulePK.getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				returnId = rs.getInt(1);
			}

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (returnId != -1 && hasVersion(con, returnId, versionId)) {
				// Return with such version already exists
				throw new FinaTypeException(Type.RETURNS_RETURN_NOT_UNIQUE);
			}

			if (returnId != -1) {
				pk = new ReturnPK(returnId);
			} else {
				ps = con.prepareStatement("select max(id) from IN_RETURNS");

				rs = ps.executeQuery();
				if (rs.next()) {
					pk = new ReturnPK(rs.getInt(1) + 1);
				} else {
					pk = new ReturnPK(1);
				}

				PreparedStatement insert = con.prepareStatement("insert into IN_RETURNS (id,scheduleId,versionId) values(?,?,?)");
			    insert.setInt(1, pk.getId());
			    insert.setInt(2, schedulePK.getId());
			    insert.setInt(3, versionId);

				insert.executeUpdate();
				DatabaseUtil.closeStatement(insert);
			}

			createDefaultValues(con, pk.getId(), versionId);
			changeReturnStatus(userHandle, languageHandle, pk, ReturnConstants.STATUS_CREATED, " ", versionCode);

			updateReturnVersions(con, String.valueOf(returnId));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			if (pk != null)
				deleteReturn(pk, versionCode);
			throw e;
		} catch (FinaTypeException e) {
			log.error(e.getMessage(), e);
			if (pk != null)
				deleteReturn(pk, versionCode);
			throw e;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void deleteReturn(ReturnPK returnPK, String versionCode) throws RemoteException, EJBException, FinaTypeException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			int versionId = getVersionId(con, versionCode);

			ps = con.prepareStatement("delete from IN_RETURN_ITEMS where returnId=? and versionId=?");
			ps.setInt(1, returnPK.getId());
			ps.setInt(2, versionId);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("delete from IN_RETURN_STATUSES where returnId=? and versionId=?");
			ps.setInt(1, returnPK.getId());
			ps.setInt(2, versionId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			if (!hasAnyVersion(con, returnPK.getId())) {
				ps = con.prepareStatement("delete from IN_RETURNS where id=?");
				ps.setInt(1, returnPK.getId());
				ps.executeUpdate();
			} else {
				updateReturnVersions(con, String.valueOf(returnPK.getId()));
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new FinaTypeException(Type.RETURNS_DELETING_RETURN_ERROR);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public boolean returnExists(ReturnPK returnPK, String versionCode) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		try {
			return hasVersion(con, returnPK.getId(), getVersionId(con, versionCode));
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public boolean packageExists(String bankCode, Date endDate, String retunTypeCode, String versionCode) throws RemoteException, EJBException {

		boolean exists = false;

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select r.id from IN_RETURNS r, IN_SCHEDULES s, IN_PERIODS p, " + " IN_BANKS b, IN_RETURN_TYPES rt, IN_RETURN_DEFINITIONS rd "
					+ " where r.scheduleID=s.id and s.periodID=p.id and s.bankID=b.id and " + " s.definitionID=rd.id and rd.typeID=rt.id and " + " rtrim(b.code)=? and rtrim(rt.code)=? and p.todate=?");

			ps.setString(1, bankCode);
			ps.setString(2, retunTypeCode);
			ps.setDate(3, new java.sql.Date(endDate.getTime()));

			rs = ps.executeQuery();

			int versionId = getVersionId(con, versionCode);

			while (rs.next()) {
				if (hasVersion(con, rs.getInt(1), versionId)) {
					exists = true;
					break;
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return exists;
	}

	public void updateReturnVersions(String returnIds) throws RemoteException, EJBException {

		Connection con = null;
		try {
			con = DatabaseUtil.getConnection();
			updateReturnVersions(con, returnIds);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	private void updateReturnVersions(Connection con, String returnIds) {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("UPDATE IN_RETURNS SET IN_RETURNS.VERSIONID=" + " (SELECT RV.ID FROM IN_RETURN_VERSIONS RV WHERE  RV.SEQUENCE = "
					+ "  (SELECT MAX(RV2.SEQUENCE) FROM IN_RETURN_VERSIONS RV2, IN_RETURN_ITEMS RI " + "   WHERE RI.RETURNID=IN_RETURNS.ID AND RV2.ID=RI.VERSIONID)" + " )"
					+ "WHERE IN_RETURNS.ID IN (" + returnIds + ")");

			ps.executeUpdate();

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeStatement(ps);
		}
	}

	private boolean hasVersion(Connection con, int returnId, int versionId) throws SQLException {

		boolean result = false;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_RETURN_STATUSES where returnID=? and versionID=?");
			ps.setInt(1, returnId);
			ps.setInt(2, versionId);

			rs = ps.executeQuery();
			if (rs.next()) {
				result = true;
			}
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
		return result;
	}

	private boolean hasAnyVersion(Connection con, int returnId) throws SQLException {

		boolean result = false;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_RETURN_STATUSES where returnID=?");
			ps.setInt(1, returnId);

			rs = ps.executeQuery();
			if (rs.next()) {
				result = true;
			}
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
		return result;
	}

	private int getVersionId(Connection con, String versionCode) throws SQLException {

		PreparedStatement ps = null;
		ResultSet rs = null;
		int result;
		try {
			ps = con.prepareStatement("select v.id from IN_RETURN_VERSIONS v where v.code=?");
			ps.setString(1, versionCode);

			rs = ps.executeQuery();
			rs.next();
			result = rs.getInt(1);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
		}
		return result;
	}

	private Vector selectChildNodes(long parentID, Connection con) throws SQLException {

		Vector v = new Vector();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id,type,sequence from IN_MDT_NODES where parentID=? and disabled=0 order by sequence");
			ps.setLong(1, parentID);
			rs = ps.executeQuery();

			while (rs.next()) {
				long id = rs.getLong(1);
				int type = rs.getInt(2);
				v.add(new Long(id));
				if (type == MDTConstants.NODETYPE_NODE) {
					v.addAll(selectChildNodes(id, con));
				}
			}
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
		}
		return v;
	}

	/** Returns schedule rows */
	public Collection getSchedulesRows(Handle userHandle, Handle languageHandle, String bankCode, String code, Date fromDate, Date toDate) throws FinaTypeException, RemoteException {
		long l = System.currentTimeMillis();
		// Throws PermissionDeniedException if the user doesn't have given
		// permissions
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.returns.schedule.review");

		//
		// Prepare filters
		//
		String bankFilter = " ";
		String returnCodeFilter = " ";
		String fromDateFilter = " ";
		String toDateFilter = " ";

		if (!bankCode.trim().equalsIgnoreCase("ALL")) {
			bankFilter = " and rtrim(b.code)=\'" + bankCode.trim() + "\' ";
		}

		if (!code.trim().equalsIgnoreCase("ALL")) {
			returnCodeFilter = " and rtrim(c.code)=\'" + code.trim() + "\' ";
		}

		if (!fromDate.equals(new Date(0L))) {
			fromDateFilter = " and d.fromDate >= ? ";
		}

		if (!toDate.equals(new Date(0L))) {
			toDateFilter = " and d.toDate <= ? ";
		}

		//
		// Process data
		//
		String userBanksFilter = "";
		User user = (User) userHandle.getEJBObject();

		if (!user.getLogin().equals("sa")) {
			// The current user isn't SA. Get only the user's banks.
			int userId = ((UserPK) user.getPrimaryKey()).getId();
			userBanksFilter = "b.id in (select f.bankID from SYS_USER_BANKS f where f.userID=" + userId + ") and ";
		}

		Connection con = DatabaseUtil.getConnection();
		ArrayList<TableRowImpl> resultList = new ArrayList<TableRowImpl>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			Language lang = (Language) languageHandle.getEJBObject();
			LanguagePK langPK = LocaleUtil.getLanguagePK(languageHandle);
			int langId = langPK.getId();

			String sql = "select a.id, c.code, d.fromDate, d.toDate, c.namestrid, b.code, e.code, a.delay,ss.value "
					+ "from IN_BANKS b,IN_SCHEDULES a, IN_PERIODS d,IN_RETURN_TYPES e, IN_RETURN_DEFINITIONS c left outer join SYS_STRINGS ss on ss.ID=c.NAMESTRID and ss.LANGID=" + langId + "   "
					+ "where " + userBanksFilter + "b.id = a.bankID and c.id = a.definitionID and d.id = a.periodID " + "and c.typeId = e.id " + bankFilter + returnCodeFilter + fromDateFilter
					+ toDateFilter;// +
									// " order by c.code, d.fromDate, d.toDate";

			ps = con.prepareStatement(sql);

			// Set the date filter values

			if (!fromDate.equals(new Date(0L))) {
				// From date exists
				ps.setDate(1, new java.sql.Date(fromDate.getTime()));
			}

			if (!toDate.equals(new Date(0L))) {
				// To date exists
				if (!fromDate.equals(new Date(0L))) {
					// From date exists. Set the to date as the 2nd date param.
					ps.setDate(2, new java.sql.Date(toDate.getTime() + 3600000L * 12));
				} else {
					// No from date. Set the to date as the 1st param.
					ps.setDate(1, new java.sql.Date(toDate.getTime() + 3600000L * 12));
				}
			}

			// Get data from DB
			rs = ps.executeQuery();

			SimpleDateFormat f = new SimpleDateFormat(lang.getDateFormat());
			/*
			 * if (DatabaseUtil.isOracle()) { while (rs.next()) {
			 * 
			 * // Id TableRowImpl row = new TableRowImpl(new
			 * SchedulePK(rs.getInt(1)), 7);
			 * 
			 * // Code row.setValue(0, rs.getString(2).trim());
			 * 
			 * // From date row.setValue(1, f.format(rs.getDate(3)));
			 * 
			 * // To date row.setValue(2, f.format(rs.getDate(4)));
			 * 
			 * // Return description String returnDesc = "NONAME";
			 * 
			 * int id = rs.getInt(5);
			 * 
			 * returnDesc = LocaleUtil.getString(con, languageHandle, id);
			 * 
			 * row.setValue(3, returnDesc);
			 * 
			 * // Bank code row.setValue(4, rs.getString(6).trim());
			 * 
			 * // Return type code row.setValue(5, rs.getString(7));
			 * 
			 * // Delay row.setValue(6, String.valueOf(rs.getInt(8)));
			 * 
			 * // Put to the result list resultList.add(row); } } else
			 */{
				while (rs.next()) {

					// Id
					TableRowImpl row = new TableRowImpl(new SchedulePK(rs.getInt(1)), 7);

					// Code
					row.setValue(0, rs.getString(2).trim());

					// From date
					row.setValue(1, f.format(rs.getDate(3)));

					// To date
					row.setValue(2, f.format(rs.getDate(4)));

					// Return description
					/*
					 * int id = rs.getInt(5); int langId=langPK.getId(); String
					 * returnDesc = "NONAME"; PreparedStatement pss =
					 * con.prepareStatement
					 * ("SELECT s.value as val FROM SYS_STRINGS s WHERE s.id="
					 * +id+" AND s.langId="+langId); ResultSet rss =
					 * pss.executeQuery(); returnDesc = rss.next() ?
					 * rss.getString("val") : "NONAME"; pss.close();
					 * rss.close();
					 */
					/*
					 * int id = rs.getInt(5);
					 * 
					 * PreparedStatement pss = con.prepareStatement(
					 * "SELECT VALUE FROM SYS_STRINGS WHERE ID=? AND LANGID=? "
					 * ); pss.setInt(1, id); pss.setInt(2, langPK.getId());
					 * ResultSet rss = pss.executeQuery(); if (rss.next())
					 * returnDesc = rss.getString("VALUE");// pss.close();
					 * rss.close();
					 */
					//
					//
					// LocaleUtil.getString(con,languageHandle,rs.getInt(5));
					String desc = LocaleUtil.getString(con, languageHandle, rs.getInt(5));// rs.getString(9);
					row.setValue(3, (desc == null) ? "NONAME" : desc);

					// Bank code
					row.setValue(4, rs.getString(6).trim());

					// Return type code
					row.setValue(5, rs.getString(7));

					// Delay
					row.setValue(6, String.valueOf(rs.getInt(8)));

					// Put to the result list
					resultList.add(row);
				}
			}
			System.out.println("Time of getting Schedules : " + (System.currentTimeMillis() - l) / 1000);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return resultList;
	}

	/**
	 * @param userHandle
	 * @param languageHandle
	 * @param bankCode
	 * @param code
	 * @param type
	 * @param dodate
	 * @param returnDef
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws FinaTypeException
	 * @throws RemoteException
	 */
	public Collection getSchedulesRows(Handle userHandle, Handle languageHandle, String bankCode, String code, String type, String dodate, String returnDef, Date fromDate, Date toDate)
			throws FinaTypeException, RemoteException {
		// Throws PermissionDeniedException if the user doesn't have given
		// permissions
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.returns.schedule.review");

		//
		// Prepare filters
		//
		String bankFilter = " ";
		String returnCodeFilter = " ";
		String typeFilter = " ";
		String dodateFilter = " ";
		String fromDateFilter = " ";
		String toDateFilter = " ";

		if (!bankCode.trim().equalsIgnoreCase("ALL")) {
			bankFilter = " and rtrim(b.code)=\'" + bankCode.trim() + "\' ";
		}

		if (!code.trim().equalsIgnoreCase("ALL")) {
			returnCodeFilter = " and rtrim(c.code)=\'" + code.trim() + "\' ";
		}

		if (!type.trim().equalsIgnoreCase("ALL")) {
			typeFilter = " and rtrim(e.code)=\'" + type.trim() + "\' ";
		}

		if (!dodate.trim().equalsIgnoreCase("")) {
			dodateFilter = " and rtrim(a.delay) like rtrim(\'%" + dodate.trim() + "%\') ";
		}

		if (!fromDate.equals(new Date(0L))) {
			fromDateFilter = " and d.fromDate >= ? ";
		}

		if (!toDate.equals(new Date(0L))) {
			toDateFilter = " and d.toDate <= ? ";
		}
		Language lang = (Language) languageHandle.getEJBObject();
		LanguagePK langPK = LocaleUtil.getLanguagePK(languageHandle);
		int langId = langPK.getId();
		String encoding = lang.getXmlEncoding();
		//
		// Process data
		//
		String userBanksFilter = "";
		User user = (User) userHandle.getEJBObject();

		if (!user.getLogin().equals("sa")) {
			// The current user isn't SA. Get only the user's banks.
			int userId = ((UserPK) user.getPrimaryKey()).getId();
			userBanksFilter = "b.id in (select f.bankID from SYS_USER_BANKS f where f.userID=" + userId + ") and ";
		}

		Connection con = DatabaseUtil.getConnection();
		ArrayList<TableRowImpl> resultList = new ArrayList<TableRowImpl>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select a.id, c.code, d.fromDate, d.toDate, c.namestrid, b.code, e.code, a.delay,ss.value "
					+ "from IN_BANKS b,IN_SCHEDULES a, IN_PERIODS d,IN_RETURN_TYPES e, IN_RETURN_DEFINITIONS c left outer join SYS_STRINGS ss on ss.ID=c.NAMESTRID and ss.LANGID=" + langId + "   "
					+ "where " + userBanksFilter + "b.id = a.bankID and c.id = a.definitionID and d.id = a.periodID " + "and c.typeId = e.id " + bankFilter + returnCodeFilter + fromDateFilter
					+ toDateFilter + typeFilter;
			// + " order by c.code, d.fromDate, d.toDate";

			ps = con.prepareStatement(sql);

			// Set the date filter values

			if (!fromDate.equals(new Date(0L))) {
				// From date exists
				ps.setDate(1, new java.sql.Date(fromDate.getTime()));
			}

			if (!toDate.equals(new Date(0L))) {
				// To date exists
				if (!fromDate.equals(new Date(0L))) {
					// From date exists. Set the to date as the 2nd date
					// param.
					ps.setDate(2, new java.sql.Date(toDate.getTime() + 3600000L * 12));
				} else {
					// No from date. Set the to date as the 1st param.
					ps.setDate(1, new java.sql.Date(toDate.getTime() + 3600000L * 12));
				}
			}

			// Get data from DB
			rs = ps.executeQuery();

			SimpleDateFormat f = new SimpleDateFormat(lang.getDateFormat());

			while (rs.next()) {

				// Id
				TableRowImpl row = new TableRowImpl(new SchedulePK(rs.getInt(1)), 7);

				// Code
				row.setValue(0, rs.getString(2).trim());

				// From date
				row.setValue(1, f.format(rs.getDate(3)));

				// To date
				row.setValue(2, f.format(rs.getDate(4)));

				// Return description
				/*
				 * int id = rs.getInt(5); int langId=langPK.getId(); String
				 * returnDesc = "NONAME"; PreparedStatement pss =
				 * con.prepareStatement
				 * ("SELECT s.value as val FROM SYS_STRINGS s WHERE s.id="
				 * +id+" AND s.langId="+langId); ResultSet rss =
				 * pss.executeQuery(); returnDesc = rss.next() ?
				 * rss.getString("val") : "NONAME"; pss.close(); rss.close();
				 */
				/*
				 * int id = rs.getInt(5);
				 * 
				 * PreparedStatement pss = con.prepareStatement(
				 * "SELECT VALUE FROM SYS_STRINGS WHERE ID=? AND LANGID=? " );
				 * pss.setInt(1, id); pss.setInt(2, langPK.getId()); ResultSet
				 * rss = pss.executeQuery(); if (rss.next()) returnDesc =
				 * rss.getString("VALUE");// pss.close(); rss.close();
				 */
				//
				//
				// LocaleUtil.getString(con,languageHandle,rs.getInt(5));
				String desc = LocaleUtil.encode(rs.getString(9), encoding); // LocaleUtil.getString(con,
																			// languageHandle,
																			// rs.getInt(5));//
																			// rs.getString(9);
				row.setValue(3, (desc == null) ? "NONAME" : desc);

				// Bank code
				row.setValue(4, rs.getString(6).trim());

				// Return type code
				row.setValue(5, rs.getString(7));

				// Delay
				row.setValue(6, String.valueOf(rs.getInt(8)));

				// Put to the result list
				resultList.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return resultList;

	}

	public TableRow getReturnAdditionalData(ReturnPK returnPK, String versionCode, Handle languageHandle) throws RemoteException {

		TableRow result = new TableRowImpl(returnPK, 4);

		String sql = "select s1.langID, s1.value, s2.langID, s2.value, p.code, s2.id,s1.id " + "from IN_RETURNS a, IN_SCHEDULES e, IN_BANKS b, IN_PERIODS c, "
				+ "IN_PERIOD_TYPES p, SYS_STRINGS s1, SYS_STRINGS s2 " + "where a.ID = ? and a.scheduleID = e.ID and b.ID = e.bankID and " + "c.ID = e.periodID and c.periodTypeID = p.ID and "
				+ "b.nameStrID=s1.ID and (s1.langID = ? ) and " + "p.nameStrID=s2.ID and (s2.langID = ?) " + "order by s1.langID, s2.langID DESC";

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql);

			ps.setInt(1, returnPK.getId());
			ps.setInt(2, langID);
			ps.setInt(3, langID);

			rs = ps.executeQuery();
			int bankLangId = -1, periodTypeLangId = -1;
			while (rs.next()) {

				if (rs.getInt(1) > bankLangId) {
					bankLangId = rs.getInt(1);
					result.setValue(0, LocaleUtil.getString(con, languageHandle, rs.getInt(7)));
				}

				if (rs.getInt(3) > periodTypeLangId) {
					periodTypeLangId = rs.getInt(3);
					result.setValue(1, LocaleUtil.getString(con, languageHandle, rs.getInt(6)));
				}

				result.setValue(2, rs.getString(5));
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select s.value,s.id from IN_RETURN_VERSIONS v  left outer join SYS_STRINGS s on  v.code=? and v.descStrID = s.ID and (s.langID = ? )");

			ps.setString(1, versionCode);
			ps.setInt(2, langID);

			rs = ps.executeQuery();
			if (rs.next()) {
				String s = rs.getString(1);
				if (s != null)
					result.setValue(3, LocaleUtil.getString(con, languageHandle, rs.getInt(2)));
				else
					result.setValue(3, "NONAME");
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return result;
	}

	/** Returns the returns list according to the given filter */
	public Collection getReturnsRows(Handle userHandle, Handle languageHandle, Set<Integer> bankIdSet, String code, int status, Date fromDate, Date toDate, String type, String versionCode,
			int maxReturnsCount) throws FinaTypeException, RemoteException {
		long l = System.currentTimeMillis();
		int rowCount = 0;
		// Can throw PermissionDeniedException
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.returns.review", "fina2.returns.amend");
		Vector returns = new Vector(); // The result list
		if ((bankIdSet == null) || (bankIdSet.size() == 0))
			return returns;
		// ---------------------------------------------------------------------
		// 1. Filters for result query

		String returnFilter = " ";
		String statusFilter = " ";
		String fromDateFilter = " ";
		String toDateFilter = " ";
		String typeFilter = " ";
		String versionFilter = " ";

		// Return filter
		if (!code.trim().equalsIgnoreCase("ALL")) {
			returnFilter = " and rtrim(d.code)=\'" + code.trim() + "\' ";
		}

		// Status filter
		if (status != 0) {
			statusFilter = " and f.status=" + String.valueOf(status).trim() + " ";
		}

		// FromDate filter
		if (!fromDate.equals(new Date(0L))) {
			fromDateFilter = " and c.fromDate >= ? ";
		}

		// ToDate filter
		if (!toDate.equals(new Date(0L))) {
			toDateFilter = " and c.toDate <= ? ";
		}

		// Type filter
		if (!type.trim().equalsIgnoreCase("ALL")) {
			typeFilter = " and rtrim(h.code)=\'" + type.trim() + "\' ";
		}

		// Return version filter
		if (!versionCode.trim().equalsIgnoreCase("ALL")) {
			versionFilter = " and rtrim(v.code)=\'" + versionCode.trim() + "\' ";
		}

		String banksSql = "";
		// ----------------------------------------------------------------
		// Banks
		String bankIdList = "";
		if (bankIdSet != null && bankIdSet.size() > 0) {
			// There is bank list

			// Creating string to contain bank id list

			for (Integer bankId : bankIdSet) {

				if (!bankIdList.equals("")) {
					bankIdList = bankIdList + ", ";
				}

				bankIdList = bankIdList + bankId;
			}

			// For sql WHERE
			banksSql = "b.id in (" + bankIdList + ") and ";
		}
		Language lang = (Language) languageHandle.getEJBObject();
		String encoding = lang.getXmlEncoding();
		// ---------------------------------------------------------------------
		// 2. Limiting the result by given user objects only
		String returnsSql = "";
		String returnVersionsSql = "";

		User user = (User) userHandle.getEJBObject();
		if (!user.getLogin().equals("sa")) {
			// The current user isn't "sa"

			int userId = UserBean.getUserId(userHandle);
			// ----------------------------------------------------------------
			// Returns
			returnsSql = " d.id in " + "(" + "  select a.definition_id from sys_user_returns a where a.user_id = " + userId + "  union "
					+ "  select a.definition_id from sys_role_returns a where a.role_id in " + "  (select a.roleid from sys_users_roles a where a.userid = " + userId + ")" + ") and ";

			// Return versions
			returnVersionsSql = " v.id in " + "(" + "  select a.version_id from sys_user_return_versions a where a.user_id = " + userId + "  union "
					+ "  select a.version_id from sys_role_return_versions a where a.role_id in " + "  (select a.roleid from sys_users_roles a where a.userid = " + userId + ")" + ") and ";
		}
		LanguagePK langPK = LocaleUtil.getLanguagePK(languageHandle);
		int langId = langPK.getId();
		Connection con = null;

		// ---------------------------------------------------------------------

		TreeMap<String, String> banks = BankSessionBean.getBankCodeShortName(langId, encoding);

		// 3. The result query
		String sql = "select a.id, b.code, c.fromDate, c.toDate, d.nameStrID, d.code, v.code, f.status, h.code,s.value from  IN_BANKS b,IN_RETURNS a, IN_PERIODS c,IN_SCHEDULES e,   IN_RETURN_STATUSES f, IN_RETURN_TYPES h,IN_RETURN_VERSIONS v,IN_RETURN_DEFINITIONS d  left outer join SYS_STRINGS s on s.ID=d.NAMESTRID and s.LANGID="
				+ langId
				+ " where "
				+ banksSql
				+ returnsSql
				+ returnVersionsSql
				+ " b.id = e.bankID and a.scheduleID = e.id  and c.id = e.periodID "
				+ "and d.id = e.definitionID and f.returnID = a.id and d.typeID = h.id and v.id = f.versionId "
				+ "and f.id in (select max(j.id) from IN_RETURN_STATUSES j where j.returnID=a.id group by j.versionid) "
				+ returnFilter
				+ statusFilter
				+ fromDateFilter
				+ toDateFilter
				+ typeFilter
				+ versionFilter + " order by a.id, v.code";
		// String sql =
		// "select a.id, b.code, c.fromDate, c.toDate, d.nameStrID, d.code,f.status, v.code,  h.code "
		// +
		// "from IN_RETURNS a, IN_BANKS b, IN_PERIODS c, IN_RETURN_DEFINITIONS d, "
		// +
		// " IN_SCHEDULES e, IN_RETURN_STATUSES f, IN_RETURN_TYPES h, IN_RETURN_VERSIONS v  "
		// + "where " + banksSql + returnsSql + returnVersionsSql +
		// " a.scheduleID = e.id and b.id = e.bankID and c.id = e.periodID " +
		// "and d.id = e.definitionID and  d.typeID = h.id and v.id = f.versionId "
		// +
		// "and f.returnID = a.id and f.id in (select max(j.id) from IN_RETURN_STATUSES j where j.returnID=a.id group by j.versionid) "
		// + returnFilter + statusFilter + fromDateFilter + toDateFilter +
		// typeFilter + versionFilter + " order by a.id, v.code";
		// ---------------------------------------------------------------------
		// 4. Processing the result
		con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql);

			// Setting fromDate filter value
			if (!fromDate.equals(new Date(0L))) {
				ps.setDate(1, new java.sql.Date(fromDate.getTime()));
			}

			// Setting toDate filter value
			if (!toDate.equals(new Date(0L))) {
				if (!fromDate.equals(new Date(0L))) {
					ps.setDate(2, new java.sql.Date(toDate.getTime() + 3600000L * 12));
				} else {
					ps.setDate(1, new java.sql.Date(toDate.getTime() + 3600000L * 12));
				}
			}

			rs = ps.executeQuery();

			SimpleDateFormat f = new SimpleDateFormat(lang.getDateFormat());

			// Copying the result set to the result list
			while (rs.next() && rowCount < maxReturnsCount) {

				int returnId = rs.getInt(1);
				String returnCode = rs.getString(6).trim();
				String fromDateStr = f.format(rs.getDate(3));
				String toDateStr = f.format(rs.getDate(4));
				String desc = LocaleUtil.encode(rs.getString(10), encoding); // LocaleUtil.getString(con,
				// languageHandle,
				// rs.getInt(5));
				/*
				 * PreparedStatement pss = con.prepareStatement(
				 * "SELECT s.value as val FROM SYS_STRINGS s WHERE s.id=" +
				 * rs.getInt(5) + " AND s.langId=" + langId); ResultSet rss =
				 * pss.executeQuery(); desc = rss.next() ? rss.getString("val")
				 * : "NONAME"; pss.close(); rss.close();
				 */

				String bankCodeStr = rs.getString(2).trim();
				bankCodeStr += "[" + banks.get(bankCodeStr) + "]";
				String versionCodeStr = rs.getString(7);
				String statusStr = String.valueOf(rs.getInt(8));
				String typeStr = rs.getString(9);

				TableRowImpl row = new TableRowImpl(new ReturnPK(returnId), 8);
				row.setValue(0, returnCode);
				row.setValue(1, fromDateStr);
				row.setValue(2, toDateStr);
				row.setValue(3, (desc == null) ? "NONAME" : desc);
				row.setValue(4, bankCodeStr);
				row.setValue(5, versionCodeStr);
				row.setValue(6, statusStr);
				row.setValue(7, typeStr);

				// Adding to the result list
				returns.add(row);

				rowCount++;
			}
			banks.clear();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		System.out.println("Time of getting return manager s " + rowCount + " rows " + ((System.currentTimeMillis() - l) / 1000));
		return returns;
	}

	public Collection<ValuesTableRow> getTableValuesRows(int langID, String encoding, int returnPk, MDTNodePK nodePK, String versionCode, int qq) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		Vector<ValuesTableRow> v = new Vector<ValuesTableRow>();
		try {
			PreparedStatement pstmt_type = con
					.prepareStatement("select a.id, a.type from IN_DEFINITION_TABLES a, IN_SCHEDULES b, IN_RETURNS c where a.definitionID=b.definitionID and b.id=c.scheduleID and c.id=? and a.nodeID=? ");
			pstmt_type.setInt(1, returnPk);
			pstmt_type.setLong(2, nodePK.getId());
			ResultSet rs_type = pstmt_type.executeQuery();
			rs_type.next();

			int tableID = rs_type.getInt(1);
			int tableType = rs_type.getInt(2);

			rs_type.close();
			pstmt_type.close();

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			// Get the return version
			int versionId = getVersionId(con, versionCode);

			// Normal
			if (tableType == ReturnConstants.TABLETYPE_NORMAL) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);
				int counter = 0;
				Hashtable ss = new Hashtable();
				String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and versionID=? and (a.nodeID=-1 ";
				StringBuffer sb = new StringBuffer(sql);
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					// Node node = (Node) iter.next();
					sb = sb.append("or a.nodeID=").append(((Long) ((Node) iter.next()).getPrimaryKey()).toString().trim()).append(" ");
					// sql = sql + "or a.nodeID=" + ((Long)
					// node.getPrimaryKey()).toString().trim() + " ";
				}
				sb = sb.append(")");
				// sql = sql + ")";
				pstmt = con.prepareStatement(sb.toString());

				pstmt.setInt(1, returnPk);
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					counter++;
					String val = rs.getString(2);
					if (val == null)
						val = "";
					else
						val = val.trim();
					ss.put(rs.getLong(1), val);
				}
				// length+=(counter);
				log.info("Row length for Normal Table : " + counter);
				rs.close();
				pstmt.close();

				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn("", -1, -1, -1);
				titleRow.addColumn("", -1, -1, -1);
				v.add(titleRow);
				int rowNumber = 1;
				// quantity+=counter;
				for (Iterator iter = vv.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					String value = (String) ss.get((Long) node.getPrimaryKey());
					if (value == null)
						value = "";

					ValuesTableRow row = new ValuesTableRow(rowNumber, 256);
					row.addColumn(node.getLabel(), -1, -1, -1);
					row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					v.add(row);
				}

			}

			// Multiple
			if (tableType == ReturnConstants.TABLETYPE_MULTIPLE) {

				Collection parents = getChildNodes(nodePK.getId(), langID, encoding);
				log.debug("parents.size()=" + parents.size());

				Vector parentsNodesValues = new Vector();
				Vector parentsNodes = new Vector();

				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);

				int pN = 0;
				int counter = 0;
				for (Iterator iterP = parents.iterator(); iterP.hasNext(); pN++) {
					Node parent = (Node) iterP.next();
					counter = 0;
					titleRow.addColumn(parent.getLabel(), -1, -1, -1);

					Collection vv = getRecursiveChildNodes(((Long) parent.getPrimaryKey()).longValue(), langID, encoding);
					log.debug("vv.size()=" + vv.size());

					Hashtable ss = new Hashtable();
					String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
					StringBuffer sb = new StringBuffer(sql);
					for (Iterator iter = vv.iterator(); iter.hasNext();) {
						// Node node = (Node) iter.next();
						sb = sb.append("or a.nodeID=").append(((Long) ((Node) iter.next()).getPrimaryKey()).toString().trim()).append(" ");
						// sql = sql + "or a.nodeID=" + ((Long)
						// node.getPrimaryKey()).toString().trim() + " ";
					}
					sb = sb.append(")");

					pstmt = con.prepareStatement(sb.toString());
					pstmt.setLong(1, returnPk);
					pstmt.setInt(2, tableID);
					pstmt.setInt(3, versionId);

					rs = pstmt.executeQuery();
					while (rs.next()) {
						counter++;
						String val = rs.getString(2);
						if (val == null)
							val = "";
						else
							val = val.trim();
						ss.put(rs.getLong(1), val);
					}
					// log.info("Counter "+counter);
					rs.close();
					pstmt.close();

					parentsNodes.add(vv);
					parentsNodesValues.add(ss);

				}
				// length+=(counter/parents.size());
				// quantity+=(counter+10);
				log.info("Row length for Multiple Cross Tab Table : " + counter + 10);

				v.add(titleRow);
				Collection parentsNodes0 = (Collection) parentsNodes.get(0);
				Vector v1 = new Vector();
				int rowNumber = 0;
				for (Iterator iter = parentsNodes0.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					ValuesTableRow row = new ValuesTableRow(rowNumber, 256);
					row.addColumn(node.getLabel(), -1, -1, -1);
					v1.add(row);
				}

				for (int pp = 0; pp < parents.size(); pp++) {
					Collection parentsNodes_ = (Collection) parentsNodes.get(pp);
					Hashtable ss_ = (Hashtable) parentsNodesValues.get(pp);
					int p1 = 0;
					for (Iterator iter_ = parentsNodes_.iterator(); iter_.hasNext(); p1++) {

						ValuesTableRow row = null;
						try {

							row = (ValuesTableRow) v1.get(p1);
						} catch (Exception e) {
							row = new ValuesTableRow(p1);
						}

						Node node = (Node) iter_.next();
						String value = (String) ss_.get((Long) node.getPrimaryKey());
						if (value == null)
							value = "";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					}
				}

				v.addAll(v1);

			}

			// Variable
			if (tableType == ReturnConstants.TABLETYPE_VARIABLE) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);

				PreparedStatement pstmt_rmax = con.prepareStatement("select MAX(rowNumber) from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmax.setInt(1, returnPk);
				pstmt_rmax.setInt(2, tableID);
				pstmt_rmax.setInt(3, versionId);

				ResultSet rs_rmax = pstmt_rmax.executeQuery();
				int maxRow = -1;
				if (rs_rmax.next()) {
					maxRow = rs_rmax.getInt(1);
				}
				rs_rmax.close();
				pstmt_rmax.close();

				PreparedStatement pstmt_rmin = con.prepareStatement("select MIN(rowNumber) from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmin.setInt(1, returnPk);
				pstmt_rmin.setInt(2, tableID);
				pstmt_rmin.setInt(3, versionId);

				ResultSet rs_rmin = pstmt_rmin.executeQuery();
				int minRow = -1;
				if (rs_rmin.next()) {
					minRow = rs_rmin.getInt(1);
				}
				rs_rmin.close();
				pstmt_rmin.close();

				String sql = "select a.nodeID, a.value, a.rowNumber from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
				StringBuffer sb = new StringBuffer(sql);
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					// Node node = (Node) iter.next();
					sb = sb.append("or a.nodeID=").append(((Long) ((Node) iter.next()).getPrimaryKey()).toString().trim()).append(" ");
					// sql = sql + "or a.nodeID=" + ((Long)
					// node.getPrimaryKey()).toString().trim() + " ";
				}
				sb = sb.append(") order by a.rowNumber, a.nodeID");
				// sql = sql + ") order by a.rowNumber, a.nodeID";
				Vector val = new Vector();
				Hashtable ss = new Hashtable();
				boolean hasItems = false;
				// log.info("#######################SQL################"+sb.toString());
				pstmt = con.prepareStatement(sb.toString());

				pstmt.setInt(1, returnPk);
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();

				int pR = minRow - 1;
				while (rs.next()) {
					hasItems = true;
					long id = rs.getLong(1);
					String value = rs.getString(2);
					if (value == null)
						value = "";
					else
						value = value.trim();
					// value = LocaleUtil.encode(value, encoding);
					int cR = rs.getInt(3);
					if (pR != cR) {
						if (pR > minRow - 1) {
							val.add(ss);
						}
						ss = new Hashtable();

						pR = cR;
					}

					ss.put(id, value);
				}
				if (hasItems)
					val.add(ss);

				rs.close();
				pstmt.close();
				ValuesTableRow titleRow = new ValuesTableRow(-1);

				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					titleRow.addColumn(node.getLabel(), -1, -1, -1);
				}
				v.add(titleRow);

				int addition = v.size();
				for (int i = 0; i < val.size(); i++) {
					addition++;
				}
				// quantity+=(maxRow+10);
				log.info("Row Length for VCT : " + (maxRow));
				for (int i = 0; i < val.size(); i++) {
					ss = (Hashtable) val.get(i);
					ValuesTableRow row = new ValuesTableRow(i, 256);
					for (Iterator iter = vv.iterator(); iter.hasNext();) {

						Node node = (Node) iter.next();
						String value = (String) ss.get((Long) node.getPrimaryKey());
						if (value == null)
							value = "";
						row.addColumn(value.intern(), ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					}

					v.add(row);
				}

				if (!hasItems) {
					ValuesTableRow blankRow = new ValuesTableRow(0);
					blankRow.setBlank(true);

					for (Iterator iter = vv.iterator(); iter.hasNext();) {

						Node node = (Node) iter.next();
						String value = null;

						if (value == null) {
							value = "";
						}

						blankRow.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					}
					v.add(blankRow);
				}

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			log.info(v.size());
		}
		return v;
	}

	public Collection getTableValuesRows(Handle languageHandle, ReturnPK pk, MDTNodePK nodePK, String versionCode) throws RemoteException, EJBException {

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		try {
			PreparedStatement pstmt_type = con
					.prepareStatement("select a.id, a.type from IN_DEFINITION_TABLES a, IN_SCHEDULES b, IN_RETURNS c where a.definitionID=b.definitionID and b.id=c.scheduleID and c.id=? and a.nodeID=? ");
			pstmt_type.setInt(1, pk.getId());
			pstmt_type.setLong(2, nodePK.getId());
			ResultSet rs_type = pstmt_type.executeQuery();
			rs_type.next();

			int tableID = rs_type.getInt(1);
			int tableType = rs_type.getInt(2);

			rs_type.close();
			pstmt_type.close();

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			// Get the return version
			int versionId = getVersionId(con, versionCode);

			// Normal
			if (tableType == ReturnConstants.TABLETYPE_NORMAL) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);

				Hashtable ss = new Hashtable();
				String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and versionID=? and (a.nodeID=-1 ";
				StringBuffer sb = new StringBuffer(sql);
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					// Node node = (Node) iter.next();
					sb = sb.append("or a.nodeID=").append(((Long) ((Node) iter.next()).getPrimaryKey()).toString().trim()).append(" ");
					// sql = sql + "or a.nodeID=" + ((Long)
					// node.getPrimaryKey()).toString().trim() + " ";
				}
				sb = sb.append(")");
				// sql = sql + ")";
				pstmt = con.prepareStatement(sb.toString());

				pstmt.setInt(1, pk.getId());
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					String val = rs.getString(2);
					if (val == null)
						val = "";
					else
						val = val.trim();
					ss.put(rs.getLong(1), val);
				}
				rs.close();
				pstmt.close();
				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn("", -1, -1, -1);
				titleRow.addColumn("", -1, -1, -1);
				v.add(titleRow);
				int rowNumber = 1;
				for (Iterator iter = vv.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					String value = (String) ss.get((Long) node.getPrimaryKey());
					if (value == null)
						value = "";

					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1);
					row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					v.add(row);
				}

			}

			// Multiple
			if (tableType == ReturnConstants.TABLETYPE_MULTIPLE) {

				Collection parents = getChildNodes(nodePK.getId(), langID, encoding);
				log.debug("parents.size()=" + parents.size());

				Vector parentsNodesValues = new Vector();
				Vector parentsNodes = new Vector();

				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);

				int pN = 0;
				for (Iterator iterP = parents.iterator(); iterP.hasNext(); pN++) {
					Node parent = (Node) iterP.next();

					titleRow.addColumn(parent.getLabel(), -1, -1, -1);

					Collection vv = getRecursiveChildNodes(((Long) parent.getPrimaryKey()).longValue(), langID, encoding);
					log.debug("vv.size()=" + vv.size());

					Hashtable ss = new Hashtable();
					String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
					StringBuffer sb = new StringBuffer(sql);
					for (Iterator iter = vv.iterator(); iter.hasNext();) {
						// Node node = (Node) iter.next();
						sb = sb.append("or a.nodeID=").append(((Long) ((Node) iter.next()).getPrimaryKey()).toString().trim()).append(" ");
						// sql = sql + "or a.nodeID=" + ((Long)
						// node.getPrimaryKey()).toString().trim() + " ";
					}
					sb = sb.append(")");

					pstmt = con.prepareStatement(sb.toString());
					pstmt.setLong(1, pk.getId());
					pstmt.setInt(2, tableID);
					pstmt.setInt(3, versionId);

					rs = pstmt.executeQuery();
					while (rs.next()) {
						String val = rs.getString(2);
						if (val == null)
							val = "";
						else
							val = val.trim();
						ss.put(rs.getLong(1), val);
					}

					rs.close();
					pstmt.close();

					parentsNodes.add(vv);
					parentsNodesValues.add(ss);

				}

				v.add(titleRow);
				Collection parentsNodes0 = (Collection) parentsNodes.get(0);
				Vector v1 = new Vector();
				int rowNumber = 0;
				for (Iterator iter = parentsNodes0.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1);
					v1.add(row);
				}
				for (int pp = 0; pp < parents.size(); pp++) {
					Collection parentsNodes_ = (Collection) parentsNodes.get(pp);
					Hashtable ss_ = (Hashtable) parentsNodesValues.get(pp);
					int p1 = 0;
					for (Iterator iter_ = parentsNodes_.iterator(); iter_.hasNext(); p1++) {

						ValuesTableRow row = null;
						try {

							row = (ValuesTableRow) v1.get(p1);
						} catch (Exception e) {
							row = new ValuesTableRow(p1);
						}

						Node node = (Node) iter_.next();
						String value = (String) ss_.get((Long) node.getPrimaryKey());
						if (value == null)
							value = "";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					}
				}

				v.addAll(v1);

			}

			// Variable
			if (tableType == ReturnConstants.TABLETYPE_VARIABLE) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);

				PreparedStatement pstmt_rmax = con.prepareStatement("select MAX(rowNumber) from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmax.setInt(1, pk.getId());
				pstmt_rmax.setInt(2, tableID);
				pstmt_rmax.setInt(3, versionId);

				ResultSet rs_rmax = pstmt_rmax.executeQuery();
				int maxRow = -1;
				if (rs_rmax.next()) {
					maxRow = rs_rmax.getInt(1);
				}
				rs_rmax.close();
				pstmt_rmax.close();

				PreparedStatement pstmt_rmin = con.prepareStatement("select MIN(rowNumber) from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmin.setInt(1, pk.getId());
				pstmt_rmin.setInt(2, tableID);
				pstmt_rmin.setInt(3, versionId);

				ResultSet rs_rmin = pstmt_rmin.executeQuery();
				int minRow = -1;
				if (rs_rmin.next()) {
					minRow = rs_rmin.getInt(1);
				}
				rs_rmin.close();
				pstmt_rmin.close();

				String sql = "select a.nodeID, a.value, a.rowNumber from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
				StringBuffer sb = new StringBuffer(sql);
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					// Node node = (Node) iter.next();
					sb = sb.append("or a.nodeID=").append(((Long) ((Node) iter.next()).getPrimaryKey()).toString().trim()).append(" ");
					// sql = sql + "or a.nodeID=" + ((Long)
					// node.getPrimaryKey()).toString().trim() + " ";
				}
				sb = sb.append(") order by a.rowNumber, a.nodeID");
				// sql = sql + ") order by a.rowNumber, a.nodeID";
				Vector val = new Vector();
				Hashtable ss = new Hashtable();
				boolean hasItems = false;

				pstmt = con.prepareStatement(sb.toString());

				pstmt.setInt(1, pk.getId());
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();
				int rowNumber = 0;
				int pR = minRow - 1;
				while (rs.next()) {
					hasItems = true;
					long id = rs.getLong(1);
					String value = rs.getString(2);
					if (value == null)
						value = "";
					else
						value = value.trim();
					// value = LocaleUtil.encode(value, encoding);
					int cR = rs.getInt(3);
					if (pR != cR) {
						if (pR > minRow - 1) {
							val.add(ss);
						}
						ss = new Hashtable();

						pR = cR;
					}

					ss.put(id, value);
				}
				if (hasItems)
					val.add(ss);

				rs.close();
				pstmt.close();
				ValuesTableRow titleRow = new ValuesTableRow(-1);

				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					titleRow.addColumn(node.getLabel(), -1, -1, -1);
				}
				v.add(titleRow);

				for (int i = 0; i < val.size(); i++) {
					ss = (Hashtable) val.get(i);
					ValuesTableRow row = new ValuesTableRow(i);
					for (Iterator iter = vv.iterator(); iter.hasNext();) {

						Node node = (Node) iter.next();
						String value = (String) ss.get((Long) node.getPrimaryKey());
						if (value == null)
							value = "";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					}
					v.add(row);
				}

				if (!hasItems) {
					ValuesTableRow blankRow = new ValuesTableRow(0);
					blankRow.setBlank(true);

					for (Iterator iter = vv.iterator(); iter.hasNext();) {

						Node node = (Node) iter.next();
						String value = null;

						if (value == null) {
							value = "";
						}

						blankRow.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
					}
					v.add(blankRow);
				}

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return v;
	}

	public Collection getReviewTableFormatRows(Handle languageHandle, MDTNodePK nodePK) throws RemoteException, EJBException {
		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		try {

			/*
			 * PreparedStatement pstmt_type = con.prepareStatement(
			 * "select a.id, a.type from IN_DEFINITION_TABLES a, IN_SCHEDULES b, IN_RETURNS c "
			 * +
			 * "where a.definitionID=b.definitionID and b.id=c.scheduleID and c.id=? and a.nodeID=? "
			 * );
			 */

			PreparedStatement pstmt_type = con.prepareStatement("select a.id, a.type from IN_DEFINITION_TABLES a " + "where a.nodeID=? ");

			// pstmt_type.setInt(1, pk.getId());
			pstmt_type.setLong(1, nodePK.getId());
			ResultSet rs_type = pstmt_type.executeQuery();
			rs_type.next();

			int tableID = rs_type.getInt(1);
			int tableType = rs_type.getInt(2);

			rs_type.close();
			pstmt_type.close();

			// Normal
			if (tableType == ReturnConstants.TABLETYPE_NORMAL) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);
				/*
				 * Hashtable ss = new Hashtable(); String sql =
				 * "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and (a.nodeID=-1 "
				 * ; for(Iterator iter = vv.iterator(); iter.hasNext();) { Node
				 * node = (Node)iter.next(); sql = sql +
				 * "or a.nodeID="+((Integer
				 * )node.getPrimaryKey()).toString().trim()+" "; } sql =
				 * sql+")"; PreparedStatement pstmt = con.prepareStatement(sql);
				 * 
				 * pstmt.setInt(1, pk.getId()); pstmt.setInt(2, tableID);
				 * ResultSet rs = pstmt.executeQuery(); while(rs.next()) {
				 * ss.put(new Integer(rs.getInt(1)),
				 * LocaleUtil.encode(rs.getString(2), encoding)); } rs.close();
				 * pstmt.close();
				 */
				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);
				titleRow.addColumn(" ", -1, -1, -1);
				v.add(titleRow);
				int rowNumber = 1;
				for (Iterator iter = vv.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					// String value =
					// (String)ss.get((Integer)node.getPrimaryKey());
					String value = "";
					// if(value==null) value="#N/A";

					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1, (String) node.getProperty("code"));
					row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue(),
							(String) node.getProperty("code"));
					v.add(row);
				}

			}

			// Multiple
			if (tableType == ReturnConstants.TABLETYPE_MULTIPLE) {

				Collection parents = getChildNodes(nodePK.getId(), langID, encoding);

				Vector parentsNodesValues = new Vector();
				Vector parentsNodes = new Vector();

				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);

				int pN = 0;
				for (Iterator iterP = parents.iterator(); iterP.hasNext(); pN++) {
					Node parent = (Node) iterP.next();

					titleRow.addColumn(parent.getLabel(), -1, -1, -1, (String) parent.getProperty("code"));

					Collection vv = getRecursiveChildNodes(((Long) parent.getPrimaryKey()).longValue(), langID, encoding);
					/*
					 * Hashtable ss = new Hashtable(); String sql =
					 * "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and (a.nodeID=-1 "
					 * ;
					 * 
					 * for(Iterator iter = vv.iterator(); iter.hasNext();) {
					 * Node node = (Node)iter.next(); sql = sql +
					 * "or a.nodeID="+
					 * ((Integer)node.getPrimaryKey()).toString().trim()+" "; }
					 * sql = sql+")"; PreparedStatement pstmt =
					 * con.prepareStatement(sql); pstmt.setInt(1, pk.getId());
					 * pstmt.setInt(2, tableID); ResultSet rs =
					 * pstmt.executeQuery(); while(rs.next()) { ss.put(new
					 * Integer(rs.getInt(1)), LocaleUtil.encode(rs.getString(2),
					 * encoding)); }
					 * 
					 * rs.close(); pstmt.close();
					 */
					parentsNodes.add(vv);
					// parentsNodesValues.add(ss);

				}

				v.add(titleRow);
				Collection parentsNodes0 = (Collection) parentsNodes.get(0);
				Vector v1 = new Vector();
				int rowNumber = 0;
				for (Iterator iter = parentsNodes0.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1, (String) node.getProperty("code"));
					v1.add(row);
				}
				for (int pp = 0; pp < parents.size(); pp++) {
					Collection parentsNodes_ = (Collection) parentsNodes.get(pp);
					// Hashtable ss_ = (Hashtable)parentsNodesValues.get(pp);
					int p1 = 0;

					for (Iterator iter_ = parentsNodes_.iterator(); iter_.hasNext(); p1++) {
						ValuesTableRow row = null;
						try {
							row = (ValuesTableRow) v1.get(p1);
						} catch (Exception e) {
							row = new ValuesTableRow(p1);
						}
						Node node = (Node) iter_.next();
						// String value =
						// (String)ss_.get((Integer)node.getPrimaryKey());
						String value = "";
						if (value == null)
							value = "#N/A";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue(),
								(String) node.getProperty("code"));
					}
				}

				v.addAll(v1);

			}

			// Variable
			if (tableType == ReturnConstants.TABLETYPE_VARIABLE) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);
				/*
				 * PreparedStatement pstmt_rmax = con.prepareStatement(
				 * "select MAX(rowNumber) from IN_RETURN_ITEMS a "+
				 * "where a.returnID=? and a.tableID=?" ); pstmt_rmax.setInt(1,
				 * pk.getId()); pstmt_rmax.setInt(2, tableID);
				 * 
				 * ResultSet rs_rmax= pstmt_rmax.executeQuery(); int maxRow =
				 * -1; if(rs_rmax.next()) { maxRow = rs_rmax.getInt(1); }
				 * rs_rmax.close(); pstmt_rmax.close();
				 * 
				 * PreparedStatement pstmt_rmin = con.prepareStatement(
				 * "select MIN(rowNumber) from IN_RETURN_ITEMS a "+
				 * "where a.returnID=? and a.tableID=?" ); pstmt_rmin.setInt(1,
				 * pk.getId()); pstmt_rmin.setInt(2, tableID);
				 * 
				 * ResultSet rs_rmin= pstmt_rmin.executeQuery(); int minRow =
				 * -1; if(rs_rmin.next()) { minRow = rs_rmin.getInt(1); }
				 * rs_rmin.close(); pstmt_rmin.close();
				 */
				/*
				 * String sql =
				 * "select a.nodeID, a.value, a.rowNumber from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and (a.nodeID=-1 "
				 * ; for(Iterator iter = vv.iterator(); iter.hasNext();) { Node
				 * node = (Node)iter.next(); sql = sql +
				 * "or a.nodeID="+((Integer
				 * )node.getPrimaryKey()).toString().trim()+" "; } sql =
				 * sql+") order by a.rowNumber, a.nodeID"; PreparedStatement
				 * pstmt = con.prepareStatement(sql);
				 * 
				 * pstmt.setInt(1, pk.getId()); pstmt.setInt(2, tableID);
				 * ResultSet rs = pstmt.executeQuery(); int rowNumber=0; int pR
				 * = minRow-1; Vector val = new Vector(); Hashtable ss = new
				 * Hashtable(); boolean hasItems = false; while(rs.next()) {
				 * hasItems = true; int id = rs.getInt(1); String value =
				 * LocaleUtil.encode(rs.getString(2), encoding); int cR =
				 * rs.getInt(3); if(pR!=cR) { if(pR>minRow-1) { val.add(ss);
				 * log.debug("added"); } ss = new Hashtable();
				 * 
				 * pR=cR; log.debug(pR); }
				 * 
				 * ss.put(new Integer(id), value); } if(hasItems) val.add(ss);
				 * 
				 * rs.close(); pstmt.close();
				 */
				ValuesTableRow titleRow = new ValuesTableRow(-1);

				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					titleRow.addColumn(node.getLabel(), -1, -1, -1);
				}
				v.add(titleRow);

				// for(int i = 0; i<val.size(); i++) {
				// ss = (Hashtable)val.get(i);
				ValuesTableRow row = new ValuesTableRow(1);
				for (Iterator iter = vv.iterator(); iter.hasNext();) {

					Node node = (Node) iter.next();
					// String value =
					// (String)ss.get((Integer)node.getPrimaryKey());
					String value = "";
					if (value == null)
						value = "#N/A";
					row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()).longValue());
				}
				v.add(row);
				// }
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return v;

	}

	public Collection getReviewTableValuesRows(int langID, String encoding, int pk, MDTNodePK nodePK, String versionCode) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		try {
			PreparedStatement pstmt_type = con.prepareStatement("select a.id, a.type from IN_DEFINITION_TABLES a, IN_SCHEDULES b, IN_RETURNS c "
					+ "where a.definitionID=b.definitionID and b.id=c.scheduleID and c.id=? and a.nodeID=? ");
			pstmt_type.setInt(1, pk);
			pstmt_type.setLong(2, nodePK.getId());
			ResultSet rs_type = pstmt_type.executeQuery();
			rs_type.next();

			int tableID = rs_type.getInt(1);
			int tableType = rs_type.getInt(2);

			rs_type.close();
			pstmt_type.close();

			int versionId = getVersionId(con, versionCode);

			PreparedStatement pstmt = null;
			ResultSet rs = null;

			// Normal
			if (tableType == ReturnConstants.TABLETYPE_NORMAL) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);

				Hashtable ss = new Hashtable();
				String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					sql = sql + "or a.nodeID=" + ((Long) node.getPrimaryKey()).toString().trim() + " ";
				}
				sql = sql + ")";
				pstmt = con.prepareStatement(sql);

				pstmt.setInt(1, pk);
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					String val = rs.getString(2);
					if (val == null)
						val = "";
					else
						val = val.trim();
					ss.put(rs.getLong(1), LocaleUtil.encode(val, encoding));

				}
				rs.close();
				pstmt.close();
				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);
				titleRow.addColumn(" ", -1, -1, -1);
				v.add(titleRow);
				int rowNumber = 1;
				for (Iterator iter = vv.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					String value = (String) ss.get((Long) node.getPrimaryKey());
					if (value == null)
						value = "#N/A";
					log.debug(((Long) node.getPrimaryKey()).toString());
					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1);
					row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()));
					v.add(row);
				}

			}

			// Multiple
			if (tableType == ReturnConstants.TABLETYPE_MULTIPLE) {

				Collection parents = getChildNodes(nodePK.getId(), langID, encoding);

				Vector parentsNodesValues = new Vector();
				Vector parentsNodes = new Vector();

				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);

				int pN = 0;
				for (Iterator iterP = parents.iterator(); iterP.hasNext(); pN++) {
					Node parent = (Node) iterP.next();

					titleRow.addColumn(parent.getLabel(), -1, -1, -1);

					Collection vv = getRecursiveChildNodes(((Integer) parent.getPrimaryKey()).intValue(), langID, encoding);

					Hashtable ss = new Hashtable();
					String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";

					for (Iterator iter = vv.iterator(); iter.hasNext();) {
						Node node = (Node) iter.next();
						sql = sql + "or a.nodeID=" + ((Long) node.getPrimaryKey()).toString().trim() + " ";
					}
					sql = sql + ")";
					pstmt = con.prepareStatement(sql);

					pstmt.setInt(1, pk);
					pstmt.setInt(2, tableID);
					pstmt.setInt(3, versionId);

					rs = pstmt.executeQuery();

					while (rs.next()) {
						String val = rs.getString(2);
						if (val == null)
							val = "";
						else
							val = val.trim();
						ss.put(rs.getLong(1), LocaleUtil.encode(val, encoding));
					}

					rs.close();
					pstmt.close();

					parentsNodes.add(vv);
					parentsNodesValues.add(ss);

				}

				v.add(titleRow);
				Collection parentsNodes0 = (Collection) parentsNodes.get(0);
				Vector v1 = new Vector();
				int rowNumber = 0;
				for (Iterator iter = parentsNodes0.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1);
					v1.add(row);
				}
				for (int pp = 0; pp < parents.size(); pp++) {
					Collection parentsNodes_ = (Collection) parentsNodes.get(pp);
					Hashtable ss_ = (Hashtable) parentsNodesValues.get(pp);
					int p1 = 0;
					for (Iterator iter_ = parentsNodes_.iterator(); iter_.hasNext(); p1++) {

						ValuesTableRow row = null;
						try {

							row = (ValuesTableRow) v1.get(p1);
						} catch (Exception e) {
							row = new ValuesTableRow(p1);
						}

						Node node = (Node) iter_.next();
						String value = (String) ss_.get((Long) node.getPrimaryKey());
						if (value == null)
							value = "#N/A";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Long) node.getPrimaryKey()));
					}
				}

				v.addAll(v1);

			}

			// Variable
			if (tableType == ReturnConstants.TABLETYPE_VARIABLE) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);

				PreparedStatement pstmt_rmax = con.prepareStatement("select MAX(rowNumber) from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmax.setInt(1, pk);
				pstmt_rmax.setInt(2, tableID);
				pstmt_rmax.setInt(3, versionId);

				ResultSet rs_rmax = pstmt_rmax.executeQuery();
				int maxRow = -1;
				if (rs_rmax.next()) {
					maxRow = rs_rmax.getInt(1);
				}
				rs_rmax.close();
				pstmt_rmax.close();

				PreparedStatement pstmt_rmin = con.prepareStatement("select MIN(rowNumber) from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmin.setInt(1, pk);
				pstmt_rmin.setInt(2, tableID);
				pstmt_rmin.setInt(3, versionId);

				ResultSet rs_rmin = pstmt_rmin.executeQuery();
				int minRow = -1;
				if (rs_rmin.next()) {
					minRow = rs_rmin.getInt(1);
				}
				rs_rmin.close();
				pstmt_rmin.close();

				String sql = "select a.nodeID, a.value, a.rowNumber from IN_RETURN_ITEMS a where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					sql = sql + "or a.nodeID=" + ((Long) node.getPrimaryKey()).toString().trim() + " ";
				}
				sql = sql + ") order by a.rowNumber, a.nodeID";
				pstmt = con.prepareStatement(sql);

				pstmt.setInt(1, pk);
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();
				int rowNumber = 0;
				int pR = minRow - 1;
				Vector val = new Vector();
				Hashtable ss = new Hashtable();
				boolean hasItems = false;
				while (rs.next()) {
					hasItems = true;
					long id = rs.getLong(1);
					String value = rs.getString(2);
					if (value == null)
						value = "";
					else
						value = value.trim();
					value = LocaleUtil.encode(value, encoding);
					int cR = rs.getInt(3);
					if (pR != cR) {
						if (pR > minRow - 1) {
							val.add(ss);
							log.debug("added");
						}
						ss = new Hashtable();

						pR = cR;
						log.debug(String.valueOf(pR));
					}

					ss.put(id, value);
				}
				if (hasItems)
					val.add(ss);

				rs.close();
				pstmt.close();
				ValuesTableRow titleRow = new ValuesTableRow(-1);

				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					titleRow.addColumn(node.getLabel(), -1, -1, -1);
				}
				v.add(titleRow);

				for (int i = 0; i < val.size(); i++) {
					ss = (Hashtable) val.get(i);
					ValuesTableRow row = new ValuesTableRow(i);
					for (Iterator iter = vv.iterator(); iter.hasNext();) {

						Node node = (Node) iter.next();
						String value = (String) ss.get((Long) node.getPrimaryKey());
						if (value == null)
							value = "#N/A";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), (Long) node.getPrimaryKey());
					}
					v.add(row);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return v;

	}

	public Collection getReviewTableValuesRows(Handle languageHandle, ReturnPK pk, MDTNodePK nodePK, String versionCode) throws RemoteException, EJBException {

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		try {
			PreparedStatement pstmt_type = con.prepareStatement("select a.id, a.type from IN_DEFINITION_TABLES a, IN_SCHEDULES b, IN_RETURNS c "
					+ "where a.definitionID=b.definitionID and b.id=c.scheduleID and c.id=? and a.nodeID=? ");
			pstmt_type.setInt(1, pk.getId());
			pstmt_type.setLong(2, nodePK.getId());
			ResultSet rs_type = pstmt_type.executeQuery();
			rs_type.next();

			int tableID = rs_type.getInt(1);
			int tableType = rs_type.getInt(2);

			rs_type.close();
			pstmt_type.close();

			int versionId = getVersionId(con, versionCode);

			PreparedStatement pstmt = null;
			ResultSet rs = null;

			// Normal
			if (tableType == ReturnConstants.TABLETYPE_NORMAL) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);

				Hashtable ss = new Hashtable();
				String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where " + "a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					sql = sql + "or a.nodeID=" + ((Integer) node.getPrimaryKey()).toString().trim() + " ";
				}
				sql = sql + ")";
				pstmt = con.prepareStatement(sql);

				pstmt.setInt(1, pk.getId());
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();
				while (rs.next()) {
					String val = rs.getString(2);
					if (val == null)
						val = "";
					else
						val = val.trim();
					ss.put(new Integer(rs.getInt(1)), LocaleUtil.encode(val, encoding));

				}
				rs.close();
				pstmt.close();
				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);
				titleRow.addColumn(" ", -1, -1, -1);
				v.add(titleRow);
				int rowNumber = 1;
				for (Iterator iter = vv.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					String value = (String) ss.get((Integer) node.getPrimaryKey());
					if (value == null)
						value = "#N/A";
					log.debug(((Integer) node.getPrimaryKey()).toString());
					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1);
					row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Integer) node.getPrimaryKey()).intValue());
					v.add(row);
				}

			}

			// Multiple
			if (tableType == ReturnConstants.TABLETYPE_MULTIPLE) {

				Collection parents = getChildNodes(nodePK.getId(), langID, encoding);

				Vector parentsNodesValues = new Vector();
				Vector parentsNodes = new Vector();

				ValuesTableRow titleRow = new ValuesTableRow(-1);
				titleRow.addColumn(" ", -1, -1, -1);

				int pN = 0;
				for (Iterator iterP = parents.iterator(); iterP.hasNext(); pN++) {
					Node parent = (Node) iterP.next();

					titleRow.addColumn(parent.getLabel(), -1, -1, -1);

					Collection vv = getRecursiveChildNodes(((Integer) parent.getPrimaryKey()).intValue(), langID, encoding);

					Hashtable ss = new Hashtable();
					String sql = "select a.nodeID, a.value from IN_RETURN_ITEMS a where " + "a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";

					for (Iterator iter = vv.iterator(); iter.hasNext();) {
						Node node = (Node) iter.next();
						sql = sql + "or a.nodeID=" + ((Integer) node.getPrimaryKey()).toString().trim() + " ";
					}
					sql = sql + ")";
					pstmt = con.prepareStatement(sql);

					pstmt.setInt(1, pk.getId());
					pstmt.setInt(2, tableID);
					pstmt.setInt(3, versionId);

					rs = pstmt.executeQuery();

					while (rs.next()) {
						String val = rs.getString(2);
						if (val == null)
							val = "";
						else
							val = val.trim();
						ss.put(new Integer(rs.getInt(1)), LocaleUtil.encode(val, encoding));
					}

					rs.close();
					pstmt.close();

					parentsNodes.add(vv);
					parentsNodesValues.add(ss);

				}

				v.add(titleRow);
				Collection parentsNodes0 = (Collection) parentsNodes.get(0);
				Vector v1 = new Vector();
				int rowNumber = 0;
				for (Iterator iter = parentsNodes0.iterator(); iter.hasNext(); rowNumber++) {
					Node node = (Node) iter.next();
					ValuesTableRow row = new ValuesTableRow(rowNumber);
					row.addColumn(node.getLabel(), -1, -1, -1);
					v1.add(row);
				}
				for (int pp = 0; pp < parents.size(); pp++) {
					Collection parentsNodes_ = (Collection) parentsNodes.get(pp);
					Hashtable ss_ = (Hashtable) parentsNodesValues.get(pp);
					int p1 = 0;
					for (Iterator iter_ = parentsNodes_.iterator(); iter_.hasNext(); p1++) {

						ValuesTableRow row = null;
						try {

							row = (ValuesTableRow) v1.get(p1);
						} catch (Exception e) {
							row = new ValuesTableRow(p1);
						}

						Node node = (Node) iter_.next();
						String value = (String) ss_.get((Integer) node.getPrimaryKey());
						if (value == null)
							value = "#N/A";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Integer) node.getPrimaryKey()).intValue());
					}
				}

				v.addAll(v1);

			}

			// Variable
			if (tableType == ReturnConstants.TABLETYPE_VARIABLE) {
				Collection vv = getRecursiveChildNodes(nodePK.getId(), langID, encoding);

				PreparedStatement pstmt_rmax = con.prepareStatement("select MAX(rowNumber) from IN_RETURN_ITEMS a " + "where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmax.setInt(1, pk.getId());
				pstmt_rmax.setInt(2, tableID);
				pstmt_rmax.setInt(3, versionId);

				ResultSet rs_rmax = pstmt_rmax.executeQuery();
				int maxRow = -1;
				if (rs_rmax.next()) {
					maxRow = rs_rmax.getInt(1);
				}
				rs_rmax.close();
				pstmt_rmax.close();

				PreparedStatement pstmt_rmin = con.prepareStatement("select MIN(rowNumber) from IN_RETURN_ITEMS a " + "where a.returnID=? and a.tableID=? and a.versionID=?");
				pstmt_rmin.setInt(1, pk.getId());
				pstmt_rmin.setInt(2, tableID);
				pstmt_rmin.setInt(3, versionId);

				ResultSet rs_rmin = pstmt_rmin.executeQuery();
				int minRow = -1;
				if (rs_rmin.next()) {
					minRow = rs_rmin.getInt(1);
				}
				rs_rmin.close();
				pstmt_rmin.close();

				String sql = "select a.nodeID, a.value, a.rowNumber from IN_RETURN_ITEMS a " + "where a.returnID=? and a.tableID=? and a.versionID=? and (a.nodeID=-1 ";
				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					sql = sql + "or a.nodeID=" + ((Integer) node.getPrimaryKey()).toString().trim() + " ";
				}
				sql = sql + ") order by a.rowNumber, a.nodeID";
				pstmt = con.prepareStatement(sql);

				pstmt.setInt(1, pk.getId());
				pstmt.setInt(2, tableID);
				pstmt.setInt(3, versionId);

				rs = pstmt.executeQuery();
				int rowNumber = 0;
				int pR = minRow - 1;
				Vector val = new Vector();
				Hashtable ss = new Hashtable();
				boolean hasItems = false;
				while (rs.next()) {
					hasItems = true;
					int id = rs.getInt(1);
					String value = rs.getString(2);
					if (value == null)
						value = "";
					else
						value = value.trim();
					value = LocaleUtil.encode(value, encoding);
					int cR = rs.getInt(3);
					if (pR != cR) {
						if (pR > minRow - 1) {
							val.add(ss);
							log.debug("added");
						}
						ss = new Hashtable();

						pR = cR;
						log.debug(String.valueOf(pR));
					}

					ss.put(new Integer(id), value);
				}
				if (hasItems)
					val.add(ss);

				rs.close();
				pstmt.close();
				ValuesTableRow titleRow = new ValuesTableRow(-1);

				for (Iterator iter = vv.iterator(); iter.hasNext();) {
					Node node = (Node) iter.next();
					titleRow.addColumn(node.getLabel(), -1, -1, -1);
				}
				v.add(titleRow);

				for (int i = 0; i < val.size(); i++) {
					ss = (Hashtable) val.get(i);
					ValuesTableRow row = new ValuesTableRow(i);
					for (Iterator iter = vv.iterator(); iter.hasNext();) {

						Node node = (Node) iter.next();
						String value = (String) ss.get((Integer) node.getPrimaryKey());
						if (value == null)
							value = "#N/A";
						row.addColumn(value, ((Integer) node.getType()).intValue(), ((Integer) node.getProperty("dataType")).intValue(), ((Integer) node.getPrimaryKey()).intValue());
					}
					v.add(row);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return v;

	}

	private Collection getChildNodes(long parentID, int langID, String encoding) throws SQLException {
		Vector v = new Vector();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// ps =
			// con.prepareStatement("select a.id, a.code, b.value, a.type, a.dataType, a.evalMethod, a.required "
			// + "from IN_MDT_NODES a left outer join SYS_STRINGS b "
			// +
			// "on b.id=a.nameStrID and b.langID=? and a.parentID=? and a.disabled!=1 "
			// + "order by a.sequence, a.id, b.langID DESC");
			// String
			// sql="select a.id, a.code, b.value, a.type, a.dataType, a.evalMethod, a.required "
			// + "from IN_MDT_NODES a, SYS_STRINGS b "
			// +
			// "where b.id=a.nameStrID and (b.langID=?) and a.parentID=? and a.disabled!=1 "
			// + "order by a.sequence, a.id, b.langID DESC";
			String sql = "select a.id, a.code, a.namestrid, a.type, a.dataType, a.evalMethod, a.required from IN_MDT_NODES a " + "where a.parentID=? and a.disabled!=1 "
					+ "order by a.sequence, a.id DESC";
			ps = con.prepareStatement(sql);
			// ps.setInt(1, langID);
			ps.setLong(1, parentID);
			rs = ps.executeQuery();
			long prevID_ = -1;
			while (rs.next()) {
				long id = rs.getLong(1);
				String code = rs.getString(2).trim();
				String desc1 = LocaleUtil.getString(con, langID, rs.getLong(3));
				// String desc1 = rs.getString(3);
				if ((desc1 == null) || (desc1.trim().length() == 0))
					desc1 = "NONAME";
				else
					desc1 = desc1.trim();

				// String desc = LocaleUtil.encode(desc1, encoding);
				String desc = desc1;

				int type = rs.getInt(4);
				int dataType = rs.getInt(5);
				int evalMethod = rs.getInt(6);
				int required = rs.getInt(7);
				if (prevID_ == id) {
					prevID_ = id;
					continue;
				}
				prevID_ = id;
				Node node = new Node(id, desc, type);
				node.putProperty("code", code);
				node.putProperty("dataType", dataType);
				node.putProperty("evalMethod", evalMethod);
				node.putProperty("required", required);
				node.putProperty("value", "");
				v.add(node);
				// if(type == MDTConstants.NODETYPE_NODE) {
				// v.addAll(getRecursiveChildNodes(id, langID, encoding));
				// }
			}
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	private Collection getRecursiveChildNodes(long parentID, int langID, String encoding) throws SQLException {
		Vector v = new Vector();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// String
			// sql="select a.id, a.code, b.value, a.type, a.dataType, a.evalMethod, a.required "
			// + "from IN_MDT_NODES a, SYS_STRINGS b "
			// +
			// "where b.id=a.nameStrID and (b.langID=?) and a.parentID=? and a.disabled!=1 "
			// + "order by a.sequence, a.id, b.langID DESC";
			String sql = "select a.id, a.code, a.namestrid, a.type, a.dataType, a.evalMethod, a.required " + "from IN_MDT_NODES a " + "where a.parentID=? and a.disabled!=1 "
					+ "order by a.sequence, a.id DESC";
			ps = con.prepareStatement(sql);
			// ps.setInt(1, langID);
			ps.setLong(1, parentID);
			rs = ps.executeQuery();
			long prevID_ = -1;
			while (rs.next()) {
				long id = rs.getLong(1);
				String code = rs.getString(2).trim();
				String desc1 = LocaleUtil.getString(con, langID, rs.getLong(3));
				// String desc1 = rs.getString(3);
				if ((desc1 == null) || (desc1.trim().equals("")))
					desc1 = "NONAME";
				else
					desc1 = desc1.trim();

				// String desc = LocaleUtil.encode(desc1, encoding);
				String desc = desc1;

				int type = rs.getInt(4);
				int dataType = rs.getInt(5);
				int evalMethod = rs.getInt(6);
				int required = rs.getInt(7);
				if (prevID_ == id) {
					prevID_ = id;
					continue;
				}
				prevID_ = id;
				Node node = new Node(id, desc, type);
				node.putProperty("code", code);
				node.putProperty("dataType", dataType);
				node.putProperty("evalMethod", evalMethod);
				node.putProperty("required", required);
				node.putProperty("value", "");
				v.add(node);
				if (type == MDTConstants.NODETYPE_NODE) {
					v.addAll(getRecursiveChildNodes(id, langID, encoding));
				}
			}
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public void setTableValuesRows(ReturnPK pk, MDTNodePK nodePK, Collection rows, String versionCode) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select a.id,a.type from IN_DEFINITION_TABLES a, IN_SCHEDULES b, IN_RETURNS c "
					+ "where a.definitionID=b.definitionID and b.id=c.scheduleID and c.id=? and a.nodeID=? ");
			ps.setInt(1, pk.getId());
			ps.setLong(2, nodePK.getId());
			rs = ps.executeQuery();
			rs.next();

			int tableID = rs.getInt(1);
			int tableType = rs.getInt(2);

			log.debug(String.valueOf(pk.getId()));

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select max(a.id) from IN_RETURN_ITEMS a " + "where returnID=? and tableID=?");
			ps.setInt(1, pk.getId());
			ps.setInt(2, tableID);
			rs = ps.executeQuery();
			rs.next();

			int maxId = rs.getInt(1);
			DatabaseUtil.closeStatement(ps);

			int versionId = getVersionId(con, versionCode);

			if (tableType == ReturnConstants.TABLETYPE_VARIABLE) {
				ps = con.prepareStatement("delete from IN_RETURN_ITEMS " + "where returnID=? and tableID=? and versionID=?");

				ps.setInt(1, pk.getId());
				ps.setInt(2, tableID);
				ps.setInt(3, versionId);
				ps.executeUpdate();
				DatabaseUtil.closeStatement(ps);

				ps = con.prepareStatement("insert into IN_RETURN_ITEMS (value,nvalue,returnID,id,tableID,rowNumber,nodeID,versionID) " + "values(?,?,?,?,?,?,?,?)");
			} else {
				ps = con.prepareStatement("update IN_RETURN_ITEMS " + "set value=?, nvalue=? " + "where returnID=? and nodeID=? and tableID=? and versionID=? ");
			}
			PreparedStatement pstmt_i = con.prepareStatement("insert into IN_RETURN_ITEMS (value,nvalue,returnID,id,tableID,rowNumber,nodeID,versionID) " + "values(?,?,?,?,?,?,?,?)");
			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				Node node = (Node) iter.next();

				String value = (String) node.getProperty("value");
				long id = ((Long) node.getPrimaryKey()).longValue();
				int rowNumber = ((Integer) node.getProperty("row")).intValue();
				log.debug((new Long(id)).toString() + "  " + value);
				if (tableType != ReturnConstants.TABLETYPE_VARIABLE) {
					ps.setString(1, value);
					ReturnHelper.assignNumericValue(ps, 2, value);
					ps.setInt(3, pk.getId());
					ps.setLong(4, id);
					ps.setInt(5, tableID);
					ps.setInt(6, versionId);

					int res = ps.executeUpdate();
					log.debug(String.valueOf(res));
					// int res = 0;
					if (res == 0) {
						maxId++;

						pstmt_i.setString(1, value);
						ReturnHelper.assignNumericValue(pstmt_i, 2, value);
						pstmt_i.setInt(3, pk.getId());
						pstmt_i.setInt(4, maxId);
						pstmt_i.setInt(5, tableID);
						pstmt_i.setInt(6, rowNumber);
						pstmt_i.setLong(7, id);
						pstmt_i.setInt(8, versionId);
						pstmt_i.addBatch();

						// pstmt_i.executeUpdate();

					}

				} else {
					maxId++;
					ps.setString(1, value);
					ReturnHelper.assignNumericValue(ps, 2, value);
					ps.setInt(3, pk.getId());
					ps.setInt(4, maxId);
					ps.setInt(5, tableID);
					ps.setInt(6, rowNumber);
					ps.setLong(7, id);
					ps.setInt(8, versionId);
					ps.executeUpdate();
				}
			}
			pstmt_i.executeBatch();
			pstmt_i.close();

			updateReturnVersions(String.valueOf(pk.getId()));
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void changeReturnStatus(Handle userHandle, Handle languageHandle, ReturnPK returnPK, int status, String note, String versionCode) throws RemoteException, EJBException {

		int userID = ((UserPK) userHandle.getEJBObject().getPrimaryKey()).getId();

		Language lang = (Language) languageHandle.getEJBObject();
		String encoding = lang.getXmlEncoding();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			int versionId = getVersionId(con, versionCode);
			ps = con.prepareStatement("insert into IN_RETURN_STATUSES " + "(id,returnID,status,statusDate,userID,versionID,note) " + "values (?,?,?,?,?,?,?)");

			ps.setInt(1, getReturnStatusId(con));
			ps.setInt(2, returnPK.getId());
			ps.setInt(3, status);
			ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
			ps.setInt(5, userID);
			ps.setInt(6, versionId);

			String value = note.trim();
			if (!DatabaseUtil.isOracle()) {
				byte[] buf = value.getBytes(encoding);
				value = new String(buf, 0);
			}

			ps.setString(7, value);

			ps.executeUpdate();

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	private int getReturnStatusId(Connection con) throws SQLException {

		ResultSet rs = null;
		PreparedStatement ps = null;
		int id = 1;
		try {
			ps = con.prepareStatement("select max(id) from IN_RETURN_STATUSES");
			rs = ps.executeQuery();

			if (rs.next())
				id = rs.getInt(1) + 1;
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
		return id;
	}

	public Collection getReturnStatuses(Handle languageHandle, ReturnPK pk, String versionCode) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		Language lang = (Language) languageHandle.getEJBObject();
		LanguagePK langPK = (LanguagePK) languageHandle.getEJBObject().getPrimaryKey();
		String encoding = lang.getXmlEncoding();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("select a.id, a.status, a.statusDate, d.value , a.note " + "from IN_RETURN_STATUSES a, SYS_USERS c , SYS_STRINGS d "
					+ "where a.returnID=? and a.versionId=? and " + "c.id=a.userID and d.id=c.nameStrID and (d.langID=? or d.langID=1) " + "order by a.statusDate, d.langID DESC");

			ps.setInt(1, pk.getId());
			ps.setInt(2, getVersionId(con, versionCode));
			ps.setInt(3, langPK.getId());

			rs = ps.executeQuery();

			int prevId = -1;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new ReturnPK(1), 5);

				int id = rs.getInt(1);
				row.setValue(0, String.valueOf(rs.getInt(2)));
				java.sql.Timestamp statusDate = rs.getTimestamp(3);
				row.setValue(1, LocaleUtil.date2string(lang, (Date) statusDate) + " " + statusDate.getHours() + ":" + statusDate.getMinutes() + ":" + statusDate.getSeconds());
				row.setValue(2, rs.getString(4));
				String note = "" + rs.getString(5);

				if (note == null)
					note = "";
				else
					note = LocaleUtil.encode(note.trim(), encoding);

				row.setValue(3, note.trim());
				row.setValue(4, statusDate.toString());

				if (prevId == id) {
					continue;
				} else {
					prevId = id;
				}

				v.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public void resetReturnVersion(ReturnPK returnPK, String versionCode) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;

		try {
			// Get the version ID
			int versionId = getVersionId(con, versionCode);

			ps = con.prepareStatement("delete from IN_RETURN_ITEMS where returnId=? and versionID=?");

			ps.setInt(1, returnPK.getId());
			ps.setInt(2, versionId);
			ps.executeUpdate();

			createDefaultValues(con, returnPK.getId(), versionId);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void copyPackage(Handle userHandle, String bankCode, Date endDate, String retunTypeCode, String sourVersionCode, String destVersionCode, String note) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			User user = ((User) userHandle.getEJBObject());
			int userId = ((UserPK) user.getPrimaryKey()).getId();

			String additionalQueryFilter = 
					" and (" +
							"((r.VERSIONID != NULL) and (r.VERSIONID in (select ID from IN_RETURN_VERSIONS where CODE = ?)))" +
								" or " +
							"(r.ID in (select distinct(RETURNID) from IN_RETURN_ITEMS where VERSIONID in (select ID from IN_RETURN_VERSIONS where CODE = ?)))" +
					")";

			ps = con.prepareStatement(
					"select r.id from IN_RETURNS r, IN_SCHEDULES s, IN_PERIODS p, " + 
							" IN_BANKS b, IN_RETURN_TYPES rt, IN_RETURN_DEFINITIONS rd " + 
							" where r.scheduleID=s.id and s.periodID=p.id and s.bankID=b.id and " + 
							" s.definitionID=rd.id and rd.typeID=rt.id and " + 
							" rtrim(b.code)=? and rtrim(rt.code)=? and p.todate=?" + additionalQueryFilter);

			StringTokenizer fiCodeParser = new StringTokenizer(bankCode, "[");

			ps.setString(1, fiCodeParser.nextElement().toString());
			ps.setString(2, retunTypeCode);
			ps.setDate(3, new java.sql.Date(endDate.getTime()));
			ps.setString(4, sourVersionCode);
			ps.setString(5, sourVersionCode);

			rs = ps.executeQuery();

			ArrayList retIds = new ArrayList();
			while (rs.next()) {
				retIds.add(new Integer(rs.getInt(1)));
			}
			int sourVersionId = getVersionId(con, sourVersionCode);
			int destVersionId = getVersionId(con, destVersionCode);

			deleteOldPackageData(con, getReturnIds(retIds), destVersionId);
			copyReturnItems(con, getReturnIds(retIds), sourVersionId, destVersionId);
			insertReturnStatuses(con, retIds, destVersionId, userId, note);

			updateReturnVersions(con, getReturnIds(retIds));

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	private String getReturnIds(ArrayList retIds) {

		StringBuffer buff = new StringBuffer();

		for (Iterator iter = retIds.iterator(); iter.hasNext();) {
			Integer retId = (Integer) iter.next();
			if (buff.length() != 0) {
				buff.append(",");
			}
			buff.append(retId);
		}

		return buff.length() != 0 ? buff.toString() : "0";
	}

	private void deleteOldPackageData(Connection con, String returnIds, int versionId) throws SQLException {

		PreparedStatement ps = con.prepareStatement("delete from IN_RETURN_ITEMS where returnID in (" + returnIds + ") and versionID=?");

		ps.setInt(1, versionId);
		ps.executeUpdate();
		DatabaseUtil.closeStatement(ps);

		ps = con.prepareStatement("delete from IN_RETURN_STATUSES where returnID in (" + returnIds + ") and versionID=?");

		ps.setInt(1, versionId);
		ps.executeUpdate();
		DatabaseUtil.closeStatement(ps);
	}

	private void copyReturnItems(Connection con, String returnIds, int sourVersionId, int destVersionId) throws SQLException {

		PreparedStatement ps = con.prepareStatement("select ri.id, ri.returnID, ri.tableID, ri.nodeID, " + " ri.rowNumber, ri.value, ri.nvalue, ri.versionID " + " from IN_RETURN_ITEMS ri "
				+ " where ri.returnID in (" + returnIds + ") and ri.versionID=?");

		ps.setInt(1, sourVersionId);

		PreparedStatement insert = con.prepareStatement("insert into IN_RETURN_ITEMS (id, returnID, tableID, nodeID, " + " rowNumber, value, nvalue, versionID) " + "values(?,?,?,?,?,?,?,?)");

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			insert.setInt(1, rs.getInt(1));
			insert.setInt(2, rs.getInt(2));
			insert.setInt(3, rs.getInt(3));
			insert.setLong(4, rs.getLong(4));
			insert.setInt(5, rs.getInt(5));
			insert.setString(6, rs.getString(6));

			double nvalue = rs.getDouble(7);
			if (!rs.wasNull()) {
				insert.setDouble(7, nvalue);
			} else {
				insert.setNull(7, java.sql.Types.DOUBLE);
			}
			insert.setInt(8, destVersionId);
			insert.addBatch();
			// insert.executeUpdate();
		}
		insert.executeBatch();
		DatabaseUtil.closeResultSet(rs);
		DatabaseUtil.closeStatement(ps);
		DatabaseUtil.closeStatement(insert);
	}

	private void insertReturnStatuses(Connection con, ArrayList returnIds, int versionId, int userId, String note) throws SQLException {

		PreparedStatement ps = con.prepareStatement("insert into IN_RETURN_STATUSES " + "(id,returnID,status,statusDate,userID,versionID,note) " + "values (?,?,?,?,?,?,?)");

		for (Iterator iter = returnIds.iterator(); iter.hasNext();) {
			Integer returnID = (Integer) iter.next();

			ps.setInt(1, getReturnStatusId(con));
			ps.setInt(2, returnID.intValue());
			ps.setInt(3, ReturnConstants.STATUS_CREATED);
			ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
			ps.setInt(5, userId);
			ps.setInt(6, versionId);
			ps.setString(7, "Package created");

			ps.executeUpdate();

			ps.setInt(1, getReturnStatusId(con));
			ps.setInt(2, returnID.intValue());
			ps.setInt(3, ReturnConstants.STATUS_AMENDED);
			ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
			ps.setInt(5, userId);
			ps.setInt(6, versionId);
			ps.setString(7, note.trim());
			// ps.addBatch();
			ps.executeUpdate();
		}
		// ps.executeBatch();
		DatabaseUtil.closeStatement(ps);
	}

	public Collection getAutoSchedulesRows(Handle userHandle, Handle languageHandle, Collection bankPK, Collection definitionPK, Collection periodPK) throws FinaTypeException, EJBException,
			RemoteException {

		fina2.security.User user = (fina2.security.User) userHandle.getEJBObject();
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.returns.definition.review", "fina2.returns.definition.amend");

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			log.info("Getting Auto Schedules");
			Language lang = (Language) languageHandle.getEJBObject();
			SimpleDateFormat format = new SimpleDateFormat(lang.getDateFormat());
			format.applyPattern("dd/MM/yyyy");

			con = DatabaseUtil.getConnection();

			ps = con.prepareStatement("select a.code, b.code, c.fromDate, c.toDate " + "from IN_BANKS a, IN_RETURN_DEFINITIONS b, IN_PERIODS c " + "where a.ID=? and b.ID=? and c.ID=?");

			for (Iterator iterBank = bankPK.iterator(); iterBank.hasNext();) {
				TableRow bankRow = (TableRow) iterBank.next();
				BankPK bankPk = (BankPK) bankRow.getPrimaryKey();
				for (Iterator iterDef = definitionPK.iterator(); iterDef.hasNext();) {
					TableRow defRow = (TableRow) iterDef.next();
					ReturnDefinitionPK defPk = (ReturnDefinitionPK) defRow.getPrimaryKey();
					for (Iterator iterPer = periodPK.iterator(); iterPer.hasNext();) {
						TableRow perRow = (TableRow) iterPer.next();
						PeriodPK perPk = (PeriodPK) perRow.getPrimaryKey();

						ps.setInt(1, bankPk.getId());
						ps.setLong(2, defPk.getId());
						ps.setInt(3, perPk.getId());

						rs = ps.executeQuery();

						while (rs.next()) {
							TableRowImpl row = new TableRowImpl(null, 4);
							// String code1 =
							// LocaleUtil.encode(rs.getString(1).trim(),
							// encoding);
							// String code2 =
							// LocaleUtil.encode(rs.getString(2).trim(),
							// encoding);
							row.setValue(0, rs.getString(1).trim());
							row.setValue(1, rs.getString(2).trim());
							row.setValue(2, format.format(rs.getDate(3)));
							row.setValue(3, format.format(rs.getDate(4)));
							v.add(row);
						}

					}
				}
			}
			log.info("Got Auto Schedules");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	private SchedulePK sheduleCreate() throws SQLException {
		SchedulePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select max(id) from IN_SCHEDULES");
			insert = con.prepareStatement("insert into IN_SCHEDULES (id) " + "values(?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new SchedulePK(rs.getInt(1) + 1);
			else
				pk = new SchedulePK(1);

			// code = String.valueOf(pk.getId());
			insert.setInt(1, pk.getId());
			insert.executeUpdate();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	public Collection setAutoSchedulesRows(Handle languageHandle, Collection bankPK, Collection definitionPK, Collection periodPK, int doa) throws EJBException, RemoteException {

		Language lang = (Language) languageHandle.getEJBObject();
		// int langID = ((LanguagePK)lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		Vector rep = null;
		PreparedStatement ps = null;
		PreparedStatement update = null;
		ResultSet rs = null;
		int repPK = 0;

		try {
			ps = con.prepareStatement("select s.id, b.code , d.code, p.fromDate, p.toDate " + "from IN_SCHEDULES s, IN_BANKS b, IN_BANK_TYPES t, IN_RETURN_DEFINITIONS d, IN_PERIODS p "
					+ "where  s.BankId=? and s.DefinitionId=? and s.PeriodId=? " + "and s.BankId=b.id and s.DefinitionId=d.id and s.PeriodId=p.id");
			update = con.prepareStatement("update IN_SCHEDULES set " + "BankId=?, " + "DefinitionId=?, " + "PeriodId=?, " + "delay=? " + "where ID=?");

			log.info("Autoinserting Schedules.");
			for (Iterator iterBank = bankPK.iterator(); iterBank.hasNext();) {
				TableRow bankRow = (TableRow) iterBank.next();
				BankPK bankPk = (BankPK) bankRow.getPrimaryKey();
				for (Iterator iterDef = definitionPK.iterator(); iterDef.hasNext();) {
					TableRow defRow = (TableRow) iterDef.next();
					ReturnDefinitionPK defPk = (ReturnDefinitionPK) defRow.getPrimaryKey();
					for (Iterator iterPer = periodPK.iterator(); iterPer.hasNext();) {
						TableRow perRow = (TableRow) iterPer.next();
						PeriodPK perPk = (PeriodPK) perRow.getPrimaryKey();

						ps.setInt(1, bankPk.getId());
						ps.setLong(2, defPk.getId());
						ps.setInt(3, perPk.getId());

						rs = ps.executeQuery();

						SimpleDateFormat format = new SimpleDateFormat(lang.getDateFormat());
						format.applyPattern("dd/MM/yyyy");

						if (!rs.next()) {
							int id = sheduleCreate().getId();
							update.setInt(1, bankPk.getId());
							update.setLong(2, defPk.getId());
							update.setInt(3, perPk.getId());
							update.setInt(4, doa);
							update.setInt(5, id);
							update.addBatch();
							// update.executeUpdate();

							// { Logging
							StringBuffer buff = new StringBuffer();

							buff.append("Caller user: ");
							buff.append(ctx.getCallerPrincipal().getName());
							buff.append(". Schedule id: ");
							buff.append(id);
							buff.append(", Bank id: ");
							buff.append(bankPk.getId());
							buff.append(", Return Definition id: ");
							buff.append(defPk.getId());
							buff.append(", Period id: ");
							buff.append(perPk.getId());
							buff.append(", Due Day: ");
							buff.append(doa);

							log.info(buff.toString());
							// }

						} else {
							if (rep == null)
								rep = new Vector();
							repPK++;
							TableRowImpl row = new TableRowImpl(new SchedulePK(rs.getInt(1)), 4);

							String code1 = LocaleUtil.encode(rs.getString(2).trim(), encoding);
							String code2 = LocaleUtil.encode(rs.getString(3).trim(), encoding);

							row.setValue(0, code1);
							row.setValue(1, code2);
							row.setValue(2, format.format(rs.getDate(4)));
							row.setValue(3, format.format(rs.getDate(5)));
							rep.add(row);
						}
					}
				}
			}
			update.executeBatch();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(update);
		}
		return rep;
	}

	// Vaso End

	public Collection getParentNodes(MDTNodePK childPK) throws RemoteException, FinaTypeException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {
			ps = con.prepareStatement("select parentID from IN_MDT_NODES where id=?");
			ps.setLong(1, childPK.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				MDTNodePK pk = new MDTNodePK(rs.getLong(1));
				// log.debug("parent "+pk.getId());
				if (pk.getId() == 0)
					break;
				rs.close();
				nodes.add(pk);
				ps.setLong(1, pk.getId());
				rs = ps.executeQuery();
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	public Collection getDependendedReturnDefinition(MDTNodePK pk) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		if (pk == null)
			return v;
		try {
			ps = con.prepareStatement("select b.id " + "from IN_DEFINITION_TABLES a, IN_RETURN_DEFINITIONS b " + "where a.nodeID=? and b.id=a.definitionID");
			ps.setLong(1, pk.getId());

			rs = ps.executeQuery();
			while (rs.next()) {
				v.add(new Long(rs.getLong(1)));
			}
			Collection nodes = getParentNodes(pk);
			for (Iterator iter = nodes.iterator(); iter.hasNext();) {
				ps.setLong(1, ((MDTNodePK) iter.next()).getId());
				rs = ps.executeQuery();
				while (rs.next()) {
					v.add(new Long(rs.getLong(1)));
				}
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Hashtable getReturnDependecies(ReturnDefinitionPK pk) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		PreparedStatement ps1 = null;
		Vector v = new Vector();
		Hashtable h = new Hashtable();
		try {
			ps = con.prepareStatement("select b.id, b.code " + "from IN_DEFINITION_TABLES a, IN_MDT_NODES b where a.definitionid=? and a.nodeid=b.id");

			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				long id = rs.getLong(1);
				v.add(new Long(id));
				v.addAll(selectChildNodes(id, con));
			}
			String sql = "";
			for (Iterator iter = v.iterator(); iter.hasNext();) {
				if (!sql.equals(""))
					sql += ", ";
				sql += iter.next();
			}
			if (sql.equals(""))
				sql = "0";
			String arr[] = sql.split(", ");
			HashMap<Integer, String> map = CommonUtils.getData(sql);
			if (arr.length < 500) {
				ps1 = con.prepareStatement("select b.id " + "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b " + "where a.dependentNodeID in (" + sql + ") and b.id=a.nodeID and b.type=3"
				// "select b.id "+
				// "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b "+
				// "where a.nodeID in ("+sql+") and b.id=a.dependentNodeID "
						);

				rs1 = ps1.executeQuery();
				while (rs1.next()) {
					long idd = rs1.getLong(1);
					Collection rd = getDependendedReturnDefinition(new MDTNodePK(idd));
					for (Iterator iter = rd.iterator(); iter.hasNext();) {
						ReturnDefinitionPK rdpk = new ReturnDefinitionPK(((Long) iter.next()).longValue());
						h.put(rdpk, rdpk);
					}
				}
			} else {

				for (int i = 0; i < map.size(); i++) {
					ps1 = con.prepareStatement("select b.id " + "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b " + "where a.dependentNodeID in (" + map.get(i) + ") and b.id=a.nodeID and b.type=3"
					// "select b.id "+
					// "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b "+
					// "where a.nodeID in ("+sql+") and b.id=a.dependentNodeID "
							);

					rs1 = ps1.executeQuery();
					while (rs1.next()) {
						long idd = rs1.getLong(1);
						Collection rd = getDependendedReturnDefinition(new MDTNodePK(idd));
						for (Iterator iter = rd.iterator(); iter.hasNext();) {
							ReturnDefinitionPK rdpk = new ReturnDefinitionPK(((Long) iter.next()).longValue());
							h.put(rdpk, rdpk);
						}
					}
				}
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeResultSet(rs1);
			DatabaseUtil.closeStatement(ps1);
		}
		return h;
	}

	public Collection getUsedInReturns(ReturnPK pk) throws RemoteException, EJBException {
		// Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		return v;
		/*
		 * Hashtable h = new Hashtable(); try { PreparedStatement pstmt =
		 * con.prepareStatement( "select id,bankID,periodID from IN_SCHEDULES "+
		 * "where id = (select scheduleID from IN_RETURNS where id=?)" );
		 * pstmt.setInt(1, pk.getId()); ResultSet rs = pstmt.executeQuery();
		 * rs.next(); int scheduleID = rs.getInt(1); int bankID = rs.getInt(2);
		 * int periodID = rs.getInt(3); rs.close(); pstmt.close();
		 * 
		 * pstmt = con.prepareStatement(
		 * "select nodeID from IN_RETURN_ITEMS where returnID=?" );
		 * pstmt.setInt(1, pk.getId()); rs = pstmt.executeQuery(); String nodes
		 * = ""; while(rs.next()) { if(nodes.length() > 0) nodes += ","; nodes
		 * += rs.getInt(1); } if(nodes.length() == 0) nodes = "0"; rs.close();
		 * pstmt.close();
		 * 
		 * pstmt = con.prepareStatement(
		 * 
		 * "select a.returnID, g.status, k.definitionID from IN_RETURN_ITEMS a, IN_RETURN_STATUSES g, IN_SCHEDULES k "
		 * +
		 * "where a.nodeID in (select c.dependentNodeID from IN_MDT_DEPENDENT_NODES c where c.nodeID in "
		 * +
		 * "         (select d.nodeID from IN_RETURN_ITEMS d where d.returnID=?) "
		 * + "                  ) "+
		 * "                   and a.returnID in (select e.id from IN_RETURNS e where e.scheduleID in "
		 * +
		 * "                        (select f.id from IN_SCHEDULES f where f.bankID in "
		 * +
		 * "                           (select b.bankID from IN_RETURNS a, IN_SCHEDULES b where a.id = ? and a.scheduleID=b.id) "
		 * + "                           and f.periodID in "+
		 * "                           (select b.periodID from IN_RETURNS a, IN_SCHEDULES b where a.id = ? and a.scheduleID=b.id) "
		 * + "                        ) "+ "                        ) "+
		 * "      and a.returnID!=? "+
		 * "      and g.returnID=a.returnID and g.statusDate= "+
		 * "          (select max(j.statusDate) from IN_RETURN_STATUSES j where j.returnID=a.returnID) "
		 * +
		 * "      and k.id in (select l.scheduleID from IN_RETURNS l where l.id=a.returnID) "
		 * + "group by a.returnID, g.status, k.definitionID \n"+
		 * "order by a.returnID "
		 */
		/*
		 * "select a.returnID, g.status, k.definitionID from IN_RETURN_ITEMS a, IN_RETURN_STATUSES g, IN_SCHEDULES k "
		 * +
		 * "where a.nodeID in (select c.dependentNodeID from IN_MDT_DEPENDENT_NODES c where c.nodeID in "
		 * +
		 * "         (select d.nodeID from IN_RETURN_ITEMS d where d.returnID=?) "
		 * + "                  ) "+
		 * "                   and a.returnID in (select e.id from IN_RETURNS e where e.scheduleID in "
		 * +
		 * "                        (select f.id from IN_SCHEDULES f where f.bankID in "
		 * +
		 * "                           (select b.bankID from IN_RETURNS a, IN_SCHEDULES b where a.id = ? and a.scheduleID=b.id) "
		 * + "                           and f.periodID in "+
		 * "                           (select b.periodID from IN_RETURNS a, IN_SCHEDULES b where a.id = ? and a.scheduleID=b.id) "
		 * + "                        ) "+ "                        ) "+
		 * "      and a.returnID!=? "+
		 * "      and g.returnID=a.returnID and g.statusDate= "+
		 * "          (select max(j.statusDate) from IN_RETURN_STATUSES j where j.returnID=a.returnID) "
		 * +
		 * "      and k.id in (select l.scheduleID from IN_RETURNS l where l.id=a.returnID) "
		 * + "group by a.returnID, g.status, k.definitionID \n"+
		 * "order by a.returnID "
		 */
		/*
		 * ); pstmt.setInt(1, pk.getId()); pstmt.setInt(2, pk.getId());
		 * pstmt.setInt(3, pk.getId()); pstmt.setInt(4, pk.getId()); rs =
		 * pstmt.executeQuery();
		 * 
		 * while(rs.next()) { int p = rs.getInt(1); TableRowImpl row = new
		 * TableRowImpl(new ReturnPK(p), 3); row.setValue(0, new
		 * Integer(p).toString()); row.setValue(1, new
		 * Integer(rs.getInt(2)).toString()); row.setValue(2, new
		 * Integer(rs.getInt(3)).toString()); h.put(new Integer(p), row);
		 * //v.add(row); } rs.close(); pstmt.close(); con.close(); }
		 * catch(Exception e) { try { con.close(); } catch(SQLException ex) {}
		 * throw new EJBException(e); }
		 * 
		 * 
		 * for(Iterator it = ((Collection)h.values()).iterator(); it.hasNext();)
		 * { v.add(it.next()); } return v;
		 */

		// return (Collection)h.values();
	}

	public Collection getDependentReturns(ReturnPK pk) throws RemoteException, EJBException {
		// Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		return v;
		/*
		 * Hashtable h = new Hashtable(); try { PreparedStatement pstmt =
		 * con.prepareStatement(
		 * 
		 * "select a.returnID, g.status, k.definitionID from IN_RETURN_ITEMS a, IN_RETURN_STATUSES g, IN_SCHEDULES k "
		 * +
		 * "where a.nodeID in (select c.nodeID from IN_MDT_DEPENDENT_NODES c where c.dependentNodeID in "
		 * +
		 * "         (select d.nodeID from IN_RETURN_ITEMS d where d.returnID=?) "
		 * + "                  ) "+
		 * "                   and a.returnID in (select e.id from IN_RETURNS e where e.scheduleID in "
		 * +
		 * "                        (select f.id from IN_SCHEDULES f where f.bankID in "
		 * +
		 * "                           (select b.bankID from IN_RETURNS a, IN_SCHEDULES b where a.id = ? and a.scheduleID=b.id) "
		 * + "                           and f.periodID in "+
		 * "                           (select b.bankID from IN_RETURNS a, IN_SCHEDULES b where a.id = ? and a.scheduleID=b.id) "
		 * + "                        ) "+ "                        ) "+
		 * "      and a.returnID!=? "+
		 * "      and g.returnID=a.returnID and g.statusDate= "+
		 * "          (select max(j.statusDate) from IN_RETURN_STATUSES j where j.returnID=a.returnID) "
		 * +
		 * "      and k.id in (select l.scheduleID from IN_RETURNS l where l.id=a.returnID) "
		 * + "group by a.returnID, g.status, k.definitionID "+
		 * "order by a.returnID" );
		 * 
		 * pstmt.setInt(1, pk.getId()); pstmt.setInt(2, pk.getId());
		 * pstmt.setInt(3, pk.getId()); pstmt.setInt(4, pk.getId()); ResultSet
		 * rs = pstmt.executeQuery();
		 * 
		 * while(rs.next()) { int p = rs.getInt(1); TableRowImpl row = new
		 * TableRowImpl(new ReturnPK(p), 3); row.setValue(0, new
		 * Integer(p).toString()); row.setValue(1, new
		 * Integer(rs.getInt(2)).toString()); row.setValue(2, new
		 * Integer(rs.getInt(3)).toString()); log.debug(p); h.put(new
		 * Integer(p), row); //v.add(row); }
		 * 
		 * rs.close(); pstmt.close(); con.close(); } catch(Exception e) {
		 * log.error(e.getMessage(), e); try { con.close(); } catch(SQLException
		 * ex) {} //throw new EJBException(e); return v; }
		 * 
		 * 
		 * for(Iterator it = ((Collection)h.values()).iterator(); it.hasNext();)
		 * { v.add(it.next()); } return v;
		 */

		// return (Collection)h.values();
	}

	public Collection getReturnsStatuses(Handle languageHandle, String banksCode, String bankGroupsCode, String bankTypesCode, String returnsCode, String returnTypesCode, String returnVersionCode,
			int pk) throws FinaTypeException, EJBException, RemoteException {

		Language lang = (Language) languageHandle.getEJBObject();
		LanguagePK langPK = (LanguagePK) languageHandle.getEJBObject().getPrimaryKey();
		String encoding = lang.getXmlEncoding();

		String filtr1 = "";
		String filtr2 = "";

		log.info("Generating returns statuses.");

		StringBuffer buff = new StringBuffer();
		buff.append("Caller user: ");
		buff.append(ctx.getCallerPrincipal().getName());
		buff.append(". Bank Code: ");
		if (!banksCode.equals("") && !banksCode.equals("ALL")) {
			filtr1 = " and rtrim(d.code)=\'" + banksCode.trim() + "\' ";
			buff.append(banksCode.trim());
		} else {
			buff.append("'ALL'");
		}

		buff.append(", Bank Group Code: ");
		if (!bankGroupsCode.equals("") && !bankGroupsCode.equals("ALL")) {
			filtr1 = " and (d.groupID=f.id and rtrim(f.code)=\'" + bankGroupsCode.trim() + "\') ";
			buff.append(bankGroupsCode.trim());
		} else {
			buff.append("'ALL'");
		}

		buff.append(", Bank Type Code: ");
		if (!bankTypesCode.equals("") && !bankTypesCode.equals("ALL")) {
			filtr1 = " and (d.typeID=g.id and rtrim(g.code)=\'" + bankTypesCode.trim() + "\') ";
			buff.append(bankTypesCode.trim());
		} else {
			buff.append("'ALL'");
		}

		buff.append(", Returns Code: ");
		if (!returnsCode.equals("") && !returnsCode.equals("ALL")) {
			filtr2 = " and rtrim(c.code)=\'" + returnsCode.trim() + "\' ";
			buff.append(returnsCode.trim());
		} else {
			buff.append("'ALL'");
		}

		buff.append(", Return Type Code: ");
		if (!returnTypesCode.equals("") && !returnTypesCode.equals("ALL")) {
			filtr2 = " and (c.typeID=h.id and rtrim(h.code)=\'" + returnTypesCode.trim() + "\') ";
			buff.append(returnTypesCode.trim());
		} else {
			buff.append("'ALL'");
		}

		log.info(buff.toString());

		Connection con = DatabaseUtil.getConnection();
		int langId = langPK.getId();
		Vector v = new Vector();
		try {

			int versionId = getVersionId(con, returnVersionCode);

			/**
			 * @todo IN_RETURN_TYPES exists in from staremen but not used in
			 *       where< need to check if it's correct
			 */
			String sql_p = "SELECT distinct c.code, a.value, d.code, b.value, e.delay, i.toDate " + "FROM IN_SCHEDULES e,IN_BANKS d, "
					+ "IN_RETURN_DEFINITIONS c,SYS_STRINGS a, SYS_STRINGS b, IN_BANK_GROUPS f, IN_BANK_TYPES g, IN_RETURN_TYPES h, IN_PERIODS i "
					+ "WHERE e.bankID = d.id and e.definitionID = c.id and c.nameStrID = a.id and d.nameStrID = b.id and e.periodID=i.id and " + "(e.periodID = ?) " + filtr1 + filtr2
					+ "AND (a.langID = ?) AND " + "(b.langID = ?) ORDER by c.code asc";
			PreparedStatement pstmt = con.prepareStatement(sql_p);// a.langID,
																	// b.langID
																	// desc");

			PreparedStatement pstmtr = con.prepareStatement("SELECT IN_RETURN_DEFINITIONS.code, " + "SYS_STRINGS.value, IN_BANKS.code AS Expr1, " + "SYS_STRINGS1.value AS Expr2, "
					+ "IN_RETURN_STATUSES.status " + "FROM IN_SCHEDULES INNER JOIN " + "IN_BANKS ON " + "IN_SCHEDULES.bankID = IN_BANKS.id INNER JOIN " + "IN_RETURNS ON "
					+ "IN_SCHEDULES.id = IN_RETURNS.scheduleID INNER JOIN " + "IN_RETURN_DEFINITIONS ON " + "IN_SCHEDULES.definitionID = IN_RETURN_DEFINITIONS.id " + "INNER JOIN " + "SYS_STRINGS ON "
					+ "IN_RETURN_DEFINITIONS.nameStrID = SYS_STRINGS.id " + "INNER JOIN " + "SYS_STRINGS SYS_STRINGS1 ON " + "IN_BANKS.nameStrID = SYS_STRINGS1.id INNER JOIN "
					+ "IN_RETURN_STATUSES ON " + "IN_RETURNS.id = IN_RETURN_STATUSES.returnID " + "WHERE (IN_SCHEDULES.periodID = ?) AND " + "(IN_RETURN_STATUSES.id IN "
					+ "(SELECT MAX(j.id) FROM IN_RETURN_STATUSES j " + "WHERE j.returnID = IN_RETURNS.id AND j.versionID = ?))");

			pstmt.setInt(1, pk);
			pstmt.setInt(2, langId);
			pstmt.setInt(3, langId);
			log.info("PSTMT Query");
			// log.info(sql_p);
			ResultSet rs = pstmt.executeQuery();
			log.info("PSTMT Query Ended");
			// Hashtable retCodes = new Hashtable();
			LinkedHashMap retCodes = new LinkedHashMap();
			Hashtable bankCodes = new Hashtable();
			Hashtable required = new Hashtable();
			Hashtable requiredDelay = new Hashtable();
			Hashtable requiredDate = new Hashtable();
			log.info("Iterating over PSTMT Query");
			while (rs.next()) {
				String retC = rs.getString(1);
				String retN = rs.getString(2);
				if (retN == null)
					retN = "";
				else
					retN = LocaleUtil.encode(retN.trim(), encoding); // rs.
				// getString
				// (2);
				String bankC = rs.getString(3);
				String bankN = rs.getString(4);
				if (bankN == null)
					bankN = "";
				else
					bankN = LocaleUtil.encode(bankN.trim(), encoding); // rs.
				// getString
				// (4);
				Integer retD = rs.getInt(5); // //
				Date retDate = rs.getDate(6);

				Hashtable returns = (Hashtable) required.get(bankC);
				Hashtable returnsDelay = (Hashtable) requiredDelay.get(bankC); // /
				// /
				Hashtable returnsDate = (Hashtable) requiredDate.get(bankC); // //

				if (returns == null) {
					returns = new Hashtable();
					returnsDelay = new Hashtable(); // /
					returnsDate = new Hashtable(); // /
					required.put(bankC, returns);
					requiredDelay.put(bankC, returnsDelay); // //
					requiredDate.put(bankC, returnsDate); // //
				}
				returns.put(retC, retC);

				retCodes.put(retC, retN);

				bankCodes.put(bankC, bankN);
				returnsDelay.put(retC, retD); // /
				returnsDate.put(retC, retDate); // /
			}
			log.info("PSTMT Query Iterated");
			pstmtr.setInt(1, pk);
			pstmtr.setInt(2, versionId);
			ResultSet rsr = pstmtr.executeQuery();
			Hashtable bankCodesIs = new Hashtable();
			Hashtable is = new Hashtable();

			while (rsr.next()) {
				String retC = rsr.getString(1);
				String retN = rsr.getString(2);
				if (retN == null)
					retN = "";
				else
					retN = LocaleUtil.encode(retN.trim(), encoding);
				String bankC = rsr.getString(3);
				String bankN = rsr.getString(4);
				if (bankN == null)
					bankN = "";
				else
					bankN = LocaleUtil.encode(bankN.trim(), encoding);
				int status = rsr.getInt(5);

				Hashtable returns = (Hashtable) is.get(bankC);

				if (returns == null) {
					returns = new Hashtable();

					is.put(bankC, returns);

				}
				returns.put(retC, status);

			}

			// Enumeration rets = retCodes.keys();
			Set set = retCodes.keySet();
			Iterator iter = set.iterator();
			Vector s = new Vector();
			s.add("Code");
			s.add("Description");
			while (iter.hasNext()) {
				Object retK = iter.next();
				// s.add(
				// "("+retK.toString().trim()+") "+((String)retCodes.get(retK
				// )).trim());
				s.add(" " + retK.toString().trim() + " ");

			}

			v.add(s);

			Date today = new java.sql.Date(System.currentTimeMillis());
			long oneDay = 1000 * 60 * 60 * 24;

			Enumeration banks = bankCodes.keys();
			while (banks.hasMoreElements()) {
				s = new Vector();
				Object bankK = banks.nextElement();

				s.add(bankK.toString().trim());
				s.add(((String) bankCodes.get(bankK)).trim());

				// Enumeration ret = retCodes.keys();
				set = retCodes.keySet();
				iter = set.iterator();
				while (iter.hasNext()) {
					Object retK = iter.next();
					Hashtable retReq = ((Hashtable) required.get(bankK));
					Hashtable retIs = ((Hashtable) is.get(bankK));

					Hashtable retDelay = ((Hashtable) requiredDelay.get(bankK)); // /
					Hashtable retDays = ((Hashtable) requiredDate.get(bankK));

					if (retReq.get(retK) != null) {
						if (retIs != null) {
							if (retIs.get(retK) != null) {
								// s.add("Yes");
								s.add(((Integer) retIs.get(retK)).toString());
							} else {
								if (((Date) retDays.get(retK)).getTime() + (oneDay * (((Integer) retDelay.get(retK)).longValue() + 1)) > today.getTime())
									s.add("-2");
								else
									s.add("-1");

							}
						} else {
							if (((Date) retDays.get(retK)).getTime() + (oneDay * ((Integer) retDelay.get(retK)).longValue()) > today.getTime())
								s.add("-2");
							else
								s.add("-1");
						}
					} else {
						s.add("0");
					}
				}

				v.add(s);
			}

			rs.close();
			pstmt.close();
			rsr.close();
			pstmtr.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return v;
	}

	private String getType(int type) {
		String ret = null;
		if (type == ReturnConstants.TABLETYPE_MULTIPLE) {
			ret = "Multiple";
		} else if (type == ReturnConstants.TABLETYPE_VARIABLE) {
			ret = "Variable";
		} else if (type == ReturnConstants.TABLETYPE_NORMAL) {
			ret = "Normal";
		}
		return ret;
	}

	private String getEvalType(int evalType) {

		String ret = null;
		if (evalType == ReturnConstants.EVAL_AVERAGE) {
			ret = "Average";
		} else if (evalType == ReturnConstants.EVAL_EQUATION) {
			ret = "Equation";
		} else if (evalType == ReturnConstants.EVAL_MAX) {
			ret = "Max";
		} else if (evalType == ReturnConstants.EVAL_MIN) {
			ret = "Min";
		} else if (evalType == ReturnConstants.EVAL_SUM) {
			ret = "Sum";
		}

		return ret;
	}
}

class ReturnHelper {
	public static void assignNumericValue(PreparedStatement ps, int index, String value) throws SQLException {
		double val = Double.NaN;
		try {
			val = Double.parseDouble(value.replace(',', '.'));
		} catch (Exception ex) {
		}
		if ((!Double.isNaN(val)) && (!Double.isInfinite(val))) {
			ps.setDouble(index, val);
		} else {
			ps.setNull(index, java.sql.Types.DOUBLE);
		}
	}
}
