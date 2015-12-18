/*
 * BankBean.java
 *
 * Created on October 22, 2001, 8:07 PM
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public class BankBean implements EntityBean {

	private EntityContext ctx;

	public BankPK pk;
	public String code;
	public int typeID = -1;
	public int shortNameID;
	public int nameID;
	public int addressID;
	public String phone;
	public String fax;
	public String email;
	public String telex;
	public long regionId;
	public String swiftCode;
	public Collection bankGroupPKs = new LinkedList();
	private boolean isStoreNeeded = false;

	private String oldName;

	private LoggerHelper log = new LoggerHelper(BankBean.class, "Bank");

	public BankPK ejbCreate() throws EJBException, CreateException {

		BankPK pk = null;
		PreparedStatement insert = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			shortNameID = LocaleUtil.allocateString(con);
			nameID = LocaleUtil.allocateString(con);
			addressID = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from IN_BANKS");
			insert = con.prepareStatement("insert into IN_BANKS ( "
					+ "id,shortNameStrID,nameStrID,addressStrID) "
					+ "values(?,?,?,?)");

			rs = ps.executeQuery();

			if (rs.next()) {
				pk = new BankPK(rs.getInt(1) + 1);
			} else {
				pk = new BankPK(1);
			}

			code = String.valueOf(pk.getId());

			insert.setInt(1, pk.getId());
			insert.setInt(2, shortNameID);
			insert.setInt(3, nameID);
			insert.setInt(4, addressID);

			insert.executeUpdate();

			storeBankGroups(con);

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

	public BankPK ejbFindByPrimaryKey(BankPK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_BANKS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Bank is not found.");
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
			ps = con.prepareStatement("update IN_BANKS set " + "code=?, "
					+ "typeID=?, " + "shortNameStrID=?, " + "nameStrID=?, "
					+ "addressStrID=?, " + "phone=?, " + "fax=?, "
					+ "email=?, " + "telex=?, " + "swiftCode=?, "
					+ "regionid=? " + "where id=?");
			ps.setString(1, code + " ");
			ps.setInt(2, typeID);
			ps.setInt(3, shortNameID);
			ps.setInt(4, nameID);
			ps.setInt(5, addressID);
			ps.setString(6, phone + " ");
			ps.setString(7, fax + " ");
			ps.setString(8, email + " ");
			ps.setString(9, telex + "");
			ps.setString(10, swiftCode + " ");
			ps.setLong(11, regionId);
			ps.setInt(12, ((BankPK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();

			storeBankGroups(con);

			this.pk = (BankPK) ctx.getPrimaryKey();
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
			ps = con.prepareStatement("select code,typeID,shortNameStrID,nameStrID,addressStrID,phone,fax,email,telex,swiftCode,regionid from IN_BANKS where id=?");
			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			code = rs.getString("code").trim();
			typeID = rs.getInt("typeID");
			shortNameID = rs.getInt("shortNameStrID");
			nameID = rs.getInt("nameStrID");
			addressID = rs.getInt("addressStrID");
			phone = rs.getString("phone").trim();
			fax = rs.getString("fax").trim();
			email = rs.getString("email").trim();
			telex = rs.getString("telex");
			swiftCode = rs.getString("swiftCode").trim();
			regionId = rs.getLong("regionid");
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
			ps = con.prepareStatement("select bankgroupID from MM_BANK_GROUP where bankid=?");
			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			bankGroupPKs.clear();
			while (rs.next()) {
				bankGroupPKs.add(new BankGroupPK(rs.getInt(1)));
			}
			this.pk = (BankPK) ctx.getPrimaryKey();

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
		ResultSet rs = null;
		PreparedStatement exec = null;
		try {
			exec = con
					.prepareStatement("select id from IN_SCHEDULES where bankID=?");
			exec.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
				licenceRemove();
				bankManagRemove();
				log.getLogger().debug("branch manag");
				branchManagRemove();
				log.getLogger().debug("branch");
				branchRemove();
				log.getLogger().debug("End");
				PreparedStatement ps = con
						.prepareStatement("select shortNameStrID,nameStrID,addressStrID from IN_BANKS where id=?");
				ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();

				rs.next();
				PreparedStatement del = con
						.prepareStatement("delete from SYS_STRINGS where id=? or id=? or id=?");
				del.setInt(1, rs.getInt(1));
				del.setInt(2, rs.getInt(2));
				del.setInt(3, rs.getInt(3));
				del.executeUpdate();

				DatabaseUtil.closeStatement(ps);
				DatabaseUtil.closeStatement(del);

				ps = con.prepareStatement("delete from IN_BANKS "
						+ "where id=?");
				ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();
				DatabaseUtil.closeStatement(ps);

				ps = con.prepareStatement("delete from SYS_USER_BANKS "
						+ "where bankID=?");
				ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();

				int objectId = ((BankPK) ctx.getPrimaryKey()).getId();
				String user = ctx.getCallerPrincipal().getName();
				log.logObjectRemove(objectId, user);
				log.logPropertyValue("type id", this.typeID, objectId, user);

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

	private void licenceRemove() {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			ps = con.prepareStatement("select reasonStrID from IN_LICENCES where bankId=?");
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");

			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();
			}
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("delete from IN_LICENCES "
					+ "where bankId=?");
			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}

	}

	private void bankManagRemove() {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			del = con
					.prepareStatement("delete from SYS_STRINGS where id=? or id=? or id=? or id=? or id=? or id=? or id=? or id=?");
			ps = con.prepareStatement("select nameStrID,lastNameStrId,postStrId,registrationStrId1,registrationStrId2, "
					+ "registrationStrId3,commentsStrId1,commentsStrId2 from IN_BANK_MANAGEMENT where bankId=?");

			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				del.setInt(1, rs.getInt(1));
				del.setInt(2, rs.getInt(2));
				del.setInt(3, rs.getInt(3));
				del.setInt(4, rs.getInt(4));
				del.setInt(5, rs.getInt(5));
				del.setInt(6, rs.getInt(6));
				del.setInt(7, rs.getInt(7));
				del.setInt(8, rs.getInt(8));
				del.executeUpdate();
			}
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("delete from IN_BANK_MANAGEMENT "
					+ "where bankId=?");
			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}

	}

	private void branchRemove() {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			del = con
					.prepareStatement("delete from SYS_STRINGS where id=? or id=? or id=? or id=? or id=?");
			ps = con.prepareStatement("select bankRegionStrID,nameStrID,shortNameStrID,addressStrID,commentsStrID from IN_BANK_BRANCHES where bankId=?");

			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				del.setInt(1, rs.getInt(1));
				del.setInt(2, rs.getInt(2));
				del.setInt(3, rs.getInt(3));
				del.setInt(4, rs.getInt(4));
				del.setInt(5, rs.getInt(5));
				del.executeUpdate();
			}
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("delete from IN_BANK_BRANCHES "
					+ "where bankId=?");
			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}

	}

	private void branchManagRemove() {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			del = con
					.prepareStatement("delete from SYS_STRINGS where id=? or id=? or id=? or id=? or id=? or id=? or id=? or id=?");
			ps = con.prepareStatement("select a.nameStrID,a.lastNameStrId,a.postStrId,a.registrationStrId1,a.registrationStrId2, "
					+ "a.registrationStrId3,a.commentsStrId1,a.commentsStrId2 from "
					+ "IN_BRANCH_MANAGEMENT a, IN_BANK_BRANCHES b where b.bankId=? and b.id=a.branchId");

			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				del.setInt(1, rs.getInt(1));
				del.setInt(2, rs.getInt(2));
				del.setInt(3, rs.getInt(3));
				del.setInt(4, rs.getInt(4));
				del.setInt(5, rs.getInt(5));
				del.setInt(6, rs.getInt(6));
				del.setInt(7, rs.getInt(7));
				del.setInt(8, rs.getInt(8));
				del.executeUpdate();
			}
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeStatement(del);
			del = con
					.prepareStatement("delete from IN_BRANCH_MANAGEMENT where branchId=?");
			ps = con.prepareStatement("select id from IN_BANK_BRANCHES where bankId=?");

			ps.setInt(1, ((BankPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				del.setInt(1, rs.getInt(1));
				ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}

	}

	public void unsetEntityContext() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
		ctx = null;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) throws EJBException, FinaTypeException {

		log.logPropertySet("code", code, this.code, ((BankPK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		if (code.equals(""))
			code = " ";

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_BANKS where rtrim(code)=? and id != ?");
			ps.setString(1, code.trim());
			ps.setInt(2, ((BankPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Bank", code });
			}
			this.code = code.trim();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public BankTypePK getTypePK() {
		return new BankTypePK(typeID);
	}

	public void setTypePK(BankTypePK pk) {

		log.logPropertySet("type id", pk.getId(), this.typeID, ((BankPK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		typeID = pk.getId();
	}

	public String getShortName(Handle langHandle) throws RemoteException,
			EJBException {
		return LocaleUtil.getSysString(langHandle, shortNameID);
	}

	public void setShortName(Handle langHandle, String desc)
			throws RemoteException, EJBException {

		LocaleUtil.setSysString(langHandle, shortNameID, desc);
	}

	public String getName(Handle langHandle) throws RemoteException,
			EJBException {

		return LocaleUtil.getSysString(langHandle, nameID);
	}

	public void setName(Handle langHandle, String desc) throws RemoteException,
			EJBException {

		LocaleUtil.setSysString(langHandle, nameID, desc);
	}

	public String getAddress(Handle langHandle) throws RemoteException,
			EJBException {
		return LocaleUtil.getSysString(langHandle, addressID);
	}

	public void setAddress(Handle langHandle, String desc)
			throws RemoteException, EJBException {
		LocaleUtil.setSysString(langHandle, addressID, desc);
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelex() {
		return telex;
	}

	public void setTelex(String telex) {
		this.telex = telex;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}

	public String getSwiftCode() {
		return swiftCode;
	}

	public void setSwiftCode(String swiftCode) {
		this.swiftCode = swiftCode;
	}

	public Collection getBankGroupPKs() {
		return bankGroupPKs;
	}

	public void setBankGroupPKs(Collection bankGroupPKs) {
		this.bankGroupPKs = bankGroupPKs;
		isStoreNeeded = true;
	}

	private void storeBankGroups(Connection con) throws SQLException {
		if (isStoreNeeded) {
			storeBankGroups(con, this.bankGroupPKs, this.pk);
			isStoreNeeded = false;
		}
	}

	public static void storeBankGroups(Connection con,
			Collection bankGroupList, BankPK bankPk) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("delete from mm_bank_group where bankid="
				+ bankPk.getId());

		if (!bankGroupList.isEmpty()) {
			Statement st = con.createStatement();

			for (Iterator iter = bankGroupList.iterator(); iter.hasNext();) {
				BankGroupPK bankGroupPK = (BankGroupPK) iter.next();
				st.addBatch("INSERT INTO  mm_bank_group (bankid, bankgroupid) VALUES ("
						+ bankPk.getId() + " , " + bankGroupPK.getId() + ")");
			}
			st.executeBatch();
			DatabaseUtil.closeStatement(st);
		}
	}
}
