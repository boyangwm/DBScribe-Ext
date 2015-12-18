/*
 * ReturnDefinitionBean.java
 *
 * Created on October 31, 2001, 11:26 AM
 */

package fina2.returns;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import fina2.security.UserBean;
import fina2.util.LoggerHelper;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public class ReturnDefinitionBean implements EntityBean {

	private EntityContext ctx;

	public ReturnDefinitionPK pk;
	public String code;
	public int description;
	public int type = -1;
	public String oldDescription;
	private LoggerHelper log = new LoggerHelper(ReturnDefinitionBean.class,	"Return Definition");

	public ReturnDefinitionPK ejbCreate(Handle userHandle) throws EJBException,
			CreateException {
		pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {

			description = LocaleUtil.allocateString(con);

			ps = con
					.prepareStatement("select max(id) from IN_RETURN_DEFINITIONS");
			insert = con
					.prepareStatement("insert into IN_RETURN_DEFINITIONS (id,nameStrID) values(?,?)");

			rs = ps.executeQuery();
			rs.next();

			int definitionId = rs.getInt(1) + 1;
			pk = new ReturnDefinitionPK(definitionId);

			// Unnecessary and incorrect code. Should be removed.
			// code = String.valueOf(pk.getId());

			insert.setLong(1, pk.getId());
			insert.setInt(2, description);

			insert.executeUpdate();

			// Give permission to the current user
			givePermissionToReturn(userHandle, definitionId);

			log.logObjectCreate(pk.getId(), ctx.getCallerPrincipal().getName());
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	/**
	 * Gives permission to the current user for the return definition with given
	 * id
	 */
	private void givePermissionToReturn(Handle userHandle,
			int returnDefinitionId) {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			String sql = "insert into sys_user_returns values (?, ?)";
			ps = con.prepareStatement(sql);

			int userId = UserBean.getUserId(userHandle);
			ps.setInt(1, userId);
			ps.setInt(2, returnDefinitionId);
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbPostCreate(Handle userHandle) throws EJBException,
			CreateException {
	}

	public ReturnDefinitionPK ejbFindByPrimaryKey(ReturnDefinitionPK pk)
			throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_RETURN_DEFINITIONS where id=?");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Return Definition is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void ejbStore() throws javax.ejb.EJBException,
			java.rmi.RemoteException {

		log.logObjectStore();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con
					.prepareStatement("update IN_RETURN_DEFINITIONS set "
							+ "code=?, " + "nameStrID=?, " + "typeID=? "
							+ "where id=?");
			ps.setString(1, code + "");
			ps.setInt(2, description);
			ps.setInt(3, getReturnType());
			ps.setLong(4, ((ReturnDefinitionPK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbActivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void ejbLoad() throws javax.ejb.EJBException,
			java.rmi.RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
                ResultSet rs = null;
		try {
			ps = con.prepareStatement("select code, nameStrID, typeID "
					+ "from IN_RETURN_DEFINITIONS where id=?");
			ps.setLong(1, ((ReturnDefinitionPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			code = rs.getString("code").trim();
			description = rs.getInt("nameStrID");
			type = rs.getInt("typeID");
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void setEntityContext(javax.ejb.EntityContext ctx)
			throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.ctx = ctx;
	}

	public void ejbRemove() throws javax.ejb.RemoveException,
			javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement exec = null;
		ResultSet rs = null;
		try {
			exec = con
					.prepareStatement("select id from IN_SCHEDULES where definitionID=?");
			exec.setLong(1, ((ReturnDefinitionPK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
				PreparedStatement del = con
						.prepareStatement("delete from IN_DEFINITION_TABLES where definitionid=?");
				del.setLong(1, ((ReturnDefinitionPK) ctx.getPrimaryKey())
						.getId());
				del.executeUpdate();
				DatabaseUtil.closeStatement(del);

				PreparedStatement ps = con
						.prepareStatement("select nameStrID from IN_RETURN_DEFINITIONS where id=?");
				ps
						.setLong(1, ((ReturnDefinitionPK) ctx.getPrimaryKey())
								.getId());
				rs = ps.executeQuery();

				rs.next();
				del = con
						.prepareStatement("delete from SYS_STRINGS where id=?");
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();

				DatabaseUtil.closeStatement(del);
				DatabaseUtil.closeStatement(ps);

				long id = ((ReturnDefinitionPK) ctx.getPrimaryKey()).getId();
				String sql = "delete from sys_user_returns where definition_id = ?";

				ps = con.prepareStatement(sql);
				ps.setLong(1, id);
				ps.executeUpdate();

				//
				// Delete from sys_user_returns
				//

				sql = "delete from sys_role_returns where definition_id = ?";

				ps = con.prepareStatement(sql);
				ps.setLong(1, id);
				ps.executeUpdate();
				
				ps = con.prepareStatement("delete from IN_RETURN_DEFINITIONS "
						+ "where id=?");
				ps
						.setLong(1, ((ReturnDefinitionPK) ctx.getPrimaryKey())
								.getId());
				
				ps.executeUpdate();
				DatabaseUtil.closeStatement(ps);
				//
				// Delete from sys_role_returns
				//

			

				//
				// Other
				//

				long objectId = ((ReturnDefinitionPK) ctx.getPrimaryKey())
						.getId();
				String user = ctx.getCallerPrincipal().getName();
				log.logObjectRemove(objectId, user);
				log.logPropertyValue("code", this.code, objectId, user);

				DatabaseUtil.closeStatement(ps);
			} else {
				throw new EJBException();
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, exec, con);
		}

	}

	public void unsetEntityContext() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
		ctx = null;
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
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		this.oldDescription = s;

		return s;
	}

	public void setDescription(Handle langHandle, String desc)
			throws RemoteException, EJBException {

		log.logPropertySet("description", desc, this.oldDescription,
				((ReturnDefinitionPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());

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
		this.oldDescription = desc;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) throws EJBException, FinaTypeException {

		log.logPropertySet("code", code, this.code, ((ReturnDefinitionPK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		//Checking if is null
		if (code == null) {
			FinaTypeException e = new FinaTypeException(Type.FINA2_GENERAL_ERROR, new String[] {"Code must not be NULL."});
			e.printStackTrace();
			throw e;
		};
		
		//Checking emptiness
		if (code.equals("")) code = " ";
		
		//Checking code length
		if (code.length() > 12) {
			FinaTypeException e = new FinaTypeException(Type.FINA2_GENERAL_ERROR, new String[] {"Code length must be &lt;= 12."});
			e.printStackTrace();
			throw e;
		}

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// Language lang = (Language)languageHandle.getEJBObject();
			// String encoding = lang.getXmlEncoding();

			ps = con.prepareStatement("select id from IN_RETURN_DEFINITIONS where code=? and id != ?");

			// byte[] buf = code.getBytes(lang.getXmlEncoding());
			// String s = new String(buf, 0);

			ps.setString(1, code);
			ps.setLong(2, ((ReturnDefinitionPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.DEFINITION_CODE_NOT_UNIQUE, new String[] {code});
			}

			this.code = code.trim();
		} catch (FinaTypeException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public ReturnTypePK getType() {
		return new ReturnTypePK(getReturnType());
	}

	private int getReturnType() {
		return this.type == -1 ? 0 : this.type;
	}

	public void setType(ReturnTypePK typePK) {
		int newType = typePK.getId();
		log.logPropertySet("type", newType, this.type,
				((ReturnDefinitionPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		type = newType;
	}
}
