package fina2.metadata;

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

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

public class MDTNodeBean implements EntityBean {

	private EntityContext ctx;

	public MDTNodePK pk;
	public String code;
	public int description;
	public long parentID;
	public int type = -1;
	public int dataType = -1;
	public String equation;
	public int evalMethod = -1;
	public int disabled = -1;
	public int required = -1;
	public boolean inUsed;
	public boolean forceRemove = false;
	private String oldDescription;
	private LoggerHelper log = new LoggerHelper(MDTNodeBean.class, "MDT Node");

	private boolean itemUsed;

	public MDTNodePK ejbCreate(MDTNodePK parentPK) throws EJBException,
			CreateException {
		MDTNodePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			parentID = parentPK.getId();
			equation = "";

			description = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from IN_MDT_NODES");

			rs = ps.executeQuery();
			log.getLogger().debug("create select 3");

			log.getLogger().debug("create insert 1");
			insert = con
					.prepareStatement("insert into IN_MDT_NODES ( "
							+ "id,code,nameStrID,parentID,type,dataType,equation,sequence,evalMethod,disabled,required) "
							+ "values(?,?,?,?,?,?,?,?,?,?,?)");

			if (rs.next()) {
				pk = new MDTNodePK(rs.getLong(1) + 1);
			} else {
				pk = new MDTNodePK(1);
			}

			code = getNodeCode(con, pk);

			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
			ps = con
					.prepareStatement("select max(sequence) from IN_MDT_NODES where parentID=?");

			ps.setLong(1, parentPK.getId());
			rs = ps.executeQuery();

			int sequence = 0;
			if (rs.next())
				sequence = rs.getInt(1) + 1;

			insert.setLong(1, pk.getId());
			insert.setString(2, code);
			insert.setInt(3, description);
			insert.setLong(4, parentPK.getId());
			insert.setInt(5, getType());
			insert.setInt(6, getDataType());
			insert.setString(7, equation);
			insert.setInt(8, sequence);
			insert.setInt(9, getEvalMethod());
			insert.setInt(10, (disabled == -1) ? 0 : disabled);
			insert.setInt(11, getRequired());
			insert.executeUpdate();

			log.logObjectCreate(pk.getId(), ctx.getCallerPrincipal().getName());
			updateObjectType();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	private String getNodeCode(Connection con, MDTNodePK pk)
			throws SQLException {

		long nodeCode = pk.getId();
		PreparedStatement ps = con
				.prepareStatement("select id from IN_MDT_NODES where rtrim(code)=? order by code");
		ResultSet rs = null;

		try {
			while (true) {
				ps.setString(1, String.valueOf(nodeCode));
				rs = ps.executeQuery();
				if (!rs.next()) {
					break;
				} else {
					nodeCode++;
				}
			}
		} finally {
			DatabaseUtil.closeStatement(ps);
		}
		return String.valueOf(nodeCode);
	}

	public void ejbPostCreate(MDTNodePK parentPK) throws EJBException,
			CreateException {
	}

	public MDTNodePK ejbFindByPrimaryKey(MDTNodePK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_MDT_NODES where id=?");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Metadate Node is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public MDTNodePK ejbFindByCode(String code) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_MDT_NODES where rtrim(code) like ? escape ? order by code");
			ps.setString(1, code.trim().replaceAll("_", "\\\\_") + "%");
			ps.setString(2, "\\");
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Metadate Node is not found.");
			}

			pk = new MDTNodePK(rs.getLong(1));
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public MDTNodePK ejbFindByCodeExact(String code) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_MDT_NODES where rtrim(code)=? order by code");
			ps.setString(1, code.trim());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Metadate Node is not found.");
			}

			pk = new MDTNodePK(rs.getLong(1));
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public MDTNodePK ejbFindByDescription(Handle languageHandle,
			String description) throws EJBException, FinderException,
			RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			Language lang = (Language) languageHandle.getEJBObject();
			LanguagePK langPK = (LanguagePK) lang.getPrimaryKey();

			// Language lang = (Language)languageHandle.getEJBObject();
			// int langID = ((LanguagePK)lang.getPrimaryKey()).getId();
			String encoding = lang.getXmlEncoding();

			ps = con
					.prepareStatement("select a.id from IN_MDT_NODES a, SYS_STRINGS b "
							+ "where b.id=a.nameStrID and (b.langID=? or langID=1) and b.value like ? "
							+ "order by b.langID DESC");

			String s = description;
			if (!DatabaseUtil.isOracle()) {
				try {
					byte[] buf = description.getBytes(encoding);
					s = new String(buf, 0);
				} catch (Exception e) {
					log.getLogger().error(e.getMessage(), e);
				}
			}

			ps.setInt(1, langPK.getId());
			ps.setString(2, s + "%");
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Metadate Node is not found.");
			}

			pk = new MDTNodePK(rs.getInt(1));
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (RemoteException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public Collection ejbFindByParent(MDTNodePK parentPK) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con
					.prepareStatement("select id from IN_MDT_NODES where parentID=?");
			ps.setLong(1, parentPK.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				v.add(new MDTNodePK(rs.getLong(1)));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection ejbFindDependentNodes(MDTNodePK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con
					.prepareStatement("select id from IN_MDT_NODES where id in "
							+ "(select dependentNodeID from IN_MDT_DEPENDENT_NODES where nodeID=?)");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				v.add(new MDTNodePK(rs.getInt(1)));
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
			ps = con.prepareStatement("update IN_MDT_NODES set " + "code=?, "
					+ "nameStrID=?, " + "parentID=?, " + "type=?, "
					+ "dataType=?, " + "equation=?, " + "evalMethod=?, "
					+ "disabled=?, " + "required=? " + "where id=?");
			ps.setString(1, code + " ");
			ps.setInt(2, description);
			ps.setLong(3, parentID);
			ps.setInt(4, getType());
			ps.setInt(5, getDataType());
			ps.setString(6, equation + " ");
			ps.setInt(7, getEvalMethod());
			ps.setInt(8, (disabled == -1) ? 0 : disabled);
			ps.setInt(9, getRequired());
			ps.setLong(10, ((MDTNodePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

			inUsed = checkDependent(con, ((MDTNodePK) ctx.getPrimaryKey())
					.getId());
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
					.prepareStatement("select code, nameStrID, parentID, type, dataType, equation, evalMethod, disabled, required "
							+ "from IN_MDT_NODES where id=?");
			ps.setLong(1, ((MDTNodePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			code = rs.getString("code").trim();
			description = rs.getInt("nameStrID");
			parentID = rs.getLong("parentID");
			type = rs.getInt("type");
			dataType = rs.getInt("dataType");
			equation = rs.getString("equation").trim();
			evalMethod = rs.getInt("evalMethod");
			disabled = rs.getInt("disabled");
			required = rs.getInt("required");

			inUsed = checkDependent(con, ((MDTNodePK) ctx.getPrimaryKey())
					.getId());

			updateObjectType();
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	// Changed by Vaso
	public void ejbRemove() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		try {
			if (!forceRemove) {
				itemUsed = true;
				checkParent(con, ((MDTNodePK) ctx.getPrimaryKey()).getId());
				if (itemUsed
						|| checkDependent(con, ((MDTNodePK) ctx
								.getPrimaryKey()).getId())) {
					throw new EJBException();
				}
			}
			// !checkOnChildes(((MDTNodePK)ctx.getPrimaryKey()).getId())){
			PreparedStatement ps = con
					.prepareStatement("select nameStrID from IN_MDT_NODES where id=?");
			ps.setLong(1, ((MDTNodePK) ctx.getPrimaryKey()).getId());
			ResultSet rs = ps.executeQuery();

			rs.next();
			PreparedStatement del = con
					.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeStatement(del);

			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/metadata/MDTNode");

			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref,
					MDTNodeHome.class);
			Collection children = home.findByParent((MDTNodePK) ctx
					.getPrimaryKey());
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				MDTNode child = (MDTNode) iter.next();
				child.remove();
			}

			ps = con.prepareStatement("delete from IN_MDT_DEPENDENT_NODES "
					+ "where dependentNodeID=?");
			ps.setLong(1, ((MDTNodePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from IN_MDT_DEPENDENT_NODES "
					+ "where nodeID=?");
			ps.setLong(1, ((MDTNodePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from IN_MDT_NODES "
					+ "where id=?");
			ps.setLong(1, ((MDTNodePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			long objectId = ((MDTNodePK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
			log.logPropertyValue("code", this.code, objectId, user);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	// Add by Vaso
	private void checkParent(Connection con, long itemID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select parentID from IN_MDT_NODES where id=?");
			ps.setLong(1, itemID);
			rs = ps.executeQuery();
			if (rs.next()) {
				long pID = rs.getLong(1);
				PreparedStatement isUsed = con
						.prepareStatement("select id from IN_DEFINITION_TABLES where nodeID=?");
				isUsed.setLong(1, pID);
				ResultSet _rs = isUsed.executeQuery();
				if (_rs.next()) {
					this.itemUsed = true;
					DatabaseUtil.closeResultSet(_rs);
					DatabaseUtil.closeStatement(isUsed);
					return;
				} else {
					DatabaseUtil.closeResultSet(_rs);
					DatabaseUtil.closeStatement(isUsed);
					checkParent(con, pID);
				}
			} else {
				this.itemUsed = false;
				return;
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
		}
	}

	public boolean checkDependent(Connection con, long itemID) {
		boolean bRs = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select dependentNodeID from IN_MDT_DEPENDENT_NODES "
							+ "where nodeID=?");

			ps.setLong(1, itemID);
			rs = ps.executeQuery();
			if (rs.next()) {
				bRs = true;
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
		}
		return bRs;
	}

	private boolean checkOnChildes(Connection con, int itemID) {
		boolean bRs = true;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_MDT_NODES "
					+ "where parentID=?");

			ps.setInt(1, itemID);
			rs = ps.executeQuery();
			bRs = rs.next();
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
		return bRs;
	}

	// End
	public void unsetEntityContext() throws EJBException {
		ctx = null;
	}

	public void setEntityContext(EntityContext ctx) throws EJBException {
		this.ctx = ctx;
	}

	public String getDescription(Handle langHandle) throws RemoteException,
			EJBException {
		// store = false;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, description);
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		oldDescription = s;

		return s;
	}

	public void setDescription(Handle langHandle, String desc)
			throws RemoteException, EJBException {
		// store = false;
		log.logPropertySet("description", desc, this.oldDescription,
				((MDTNodePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.oldDescription = desc;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, description, desc);
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) throws EJBException, FinaTypeException,
			EJBException, RemoteException {

		log.logPropertySet("code", code, this.code, ((MDTNodePK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_MDT_NODES where rtrim(code)=? and id != ?");
			ps.setString(1, code.trim());
			ps.setLong(2, ((MDTNodePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Metadata.Node", code });
			}
			this.code = code.trim();
		} catch (FinaTypeException e) {
			log.getLogger().error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public int getType() {
		return (type == -1) ? MDTConstants.NODETYPE_NODE : type;
	}

	public void setType(int type) {
		log.logPropertySet("type", type, this.type, ((MDTNodePK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.type = type;
		updateObjectType();
	}

	private void updateObjectType() {
		switch (getType()) {
		case MDTConstants.NODETYPE_NODE:
			log.setObjectType("MDT Node");
			break;
		case MDTConstants.NODETYPE_INPUT:
			log.setObjectType("MDT Input");
			break;
		case MDTConstants.NODETYPE_VARIABLE:
			log.setObjectType("MDT Variable");
			break;
		}
	}

	public int getDataType() {
		return (dataType == -1) ? 0 : dataType;
	}

	public void setDataType(int dataType) {
		log.logPropertySet("data type", dataType, this.dataType,
				((MDTNodePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.dataType = dataType;
	}

	public String getEquation() {
		return equation;
	}

	public void setEquation(String equation) {
		log.logPropertySet("equation", equation, this.equation,
				((MDTNodePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.equation = equation;
	}

	public int getEvalMethod() {
		return (evalMethod == -1) ? 0 : evalMethod;
	}

	public void setEvalMethod(int evalMethod) {
		log.logPropertySet("eval method", evalMethod, this.evalMethod,
				((MDTNodePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.evalMethod = evalMethod;
	}

	public void setDisabled() throws RemoteException, EJBException {
		long id = ((MDTNodePK) ctx.getPrimaryKey()).getId();
		String user = ctx.getCallerPrincipal().getName();
		if (disabled == 0 || disabled == -1) {
			log.logPropertySet("disabled", "true", "false", id, user);
			disabled = 1;
		} else {
			log.logPropertySet("disabled", "false", "true", id, user);
			disabled = 0;
		}
	}

	public int getRequired() {
		return (required == -1) ? 1 : required;
	}

	public void setRequired(int required) {
		log.logPropertySet("required", required, this.required,
				((MDTNodePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.required = required;
	}

	public boolean getInUsed() {
		return !inUsed;
	}

	public void setForceRemove(boolean remove) throws RemoteException {
		this.forceRemove = remove;
	}
}
