/*
 * BankSessionBean.java
 *
 * Created on October 19, 2001, 7:47 PM
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import fina2.FinaTypeException;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.regions.RegionStructureNodePK;
import fina2.regions.RegionStructureSession;
import fina2.regions.RegionStructureSessionHome;
import fina2.security.ServerSecurityUtil;
import fina2.security.User;
import fina2.security.UserPK;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.Node;

public class BankSessionBean implements SessionBean {

	private SessionContext ctx;

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
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

	/**
	 * 
	 * @param langId
	 * @param encoding
	 * @return TreeMap<BankCode,ShortName>
	 */
	public static TreeMap<String, String> getBankCodeShortName(int langId, String encoding) {
		TreeMap<String, String> banks = new TreeMap<String, String>();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement psBanks = null;
		ResultSet rsBanks = null;
		String banksShortnameSQL = "select b.code,s.value from IN_BANKS b left outer join SYS_STRINGS s on s.ID=b.SHORTNAMESTRID and s.langid=" + langId;
		try {
			psBanks = con.prepareStatement(banksShortnameSQL);
			rsBanks = psBanks.executeQuery();
			if (rsBanks != null)
				while (rsBanks.next()) {
					String des = LocaleUtil.encode(rsBanks.getString(2), encoding);
					banks.put(rsBanks.getString(1).trim(), (des == null) ? "NONAME" : des);
				}
		} catch (Exception ex) {
			// log.error(ex.getMessage(), ex);
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rsBanks, psBanks, con);
		}
		return banks;
	}

	public Collection getBanksRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, EJBException, RemoteException {
		fina2.security.User user = (fina2.security.User) userHandle.getEJBObject();
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			String sql = "";
			if (!user.getLogin().equals("sa"))
				sql = "and a.id in (select c.bankID from SYS_USER_BANKS c where c.userID=" + ((fina2.security.UserPK) user.getPrimaryKey()).getId() + ") ";

			ps = con.prepareStatement("select a.id, a.code, b.value, b.langID " + "from IN_BANKS a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=?) " + sql
					+ "order by a.code, b.langID DESC");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new BankPK(rs.getInt(1)), 2);
				String s = rs.getString(2);
				if (s == null)
					s = "";
				row.setValue(0, s.trim());
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
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

	/** Returns the bank list */
	public Collection getBankListNodes(Handle userHandle, Handle languageHandle) throws RemoteException, fina2.FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Vector bankList = new Vector(); // The result bank list
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		try {
			String sql = "select a.id bankId, a.code bankCode, b.value, a.typeid,a.namestrid as strid from in_banks a left outer join SYS_STRINGS b on  a.namestrid=b.id  and b.langid=? order by bankCode ";

			ps = con.prepareStatement(sql);
			ps.setInt(1, langID);
			String encoding = LocaleUtil.getEncoding(languageHandle);
			rs = ps.executeQuery();

			while (rs.next()) {

				BankPK bankPK = new BankPK(rs.getInt(1));
				String bankCode = "[" + rs.getString(2).trim() + "] ";
				String s = rs.getString(3);
				if (s == null)
					s = "NONAME";
				String bankName = LocaleUtil.encode(s, encoding);// LocaleUtil.getString(con,
																	// languageHandle,
																	// rs.getInt("strid"));
				/*
				 * if (s != null) bankName = new String(s.getBytes(),
				 * lang.getXmlEncoding()); else bankName="NONAME";
				 */
				Integer bankType = rs.getInt(4);

				Node node = new Node(bankPK, bankCode + bankName, BanksConstants.NODETYPE_BANK);
				node.putProperty("code", bankCode);
				node.putProperty("bankType", bankType);

				/* Adding to the result list */
				bankList.add(node);
			}
		}

		catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* The bank list */
		return bankList;
	}

	public Collection getRegionRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException, EJBException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, b.value, c.value " + "from IN_REGIONS a, SYS_STRINGS b, SYS_STRINGS c " + "where b.id=a.cityStrID and (b.langID=? or b.langID=1) "
					+ "and c.id=a.regionStrID and (b.langID=? or b.langID=1) " + "order by a.id, b.langID, c.langID DESC");
			ps.setInt(1, langID);
			ps.setInt(2, langID);
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new BankRegionPK(rs.getInt(1)), 2);

				String desc1 = rs.getString(2);
				if (desc1 == null)
					desc1 = "";
				else
					desc1 = LocaleUtil.encode(desc1.trim(), encoding);
				String desc2 = rs.getString(3);
				if (desc2 == null)
					desc2 = "";
				else
					desc2 = LocaleUtil.encode(desc2.trim(), encoding);
				row.setValue(0, desc1);
				row.setValue(1, desc2);

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

	public Collection getBankTypesRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, EJBException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, a.code, b.value, b.langID " + "from IN_BANK_TYPES a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=? or b.langID=1) "
					+ "order by a.code, b.langID DESC");
			ps.setInt(1, langID);
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new BankTypePK(rs.getInt(1)), 2);
				String s = rs.getString(2);
				if (s == null)
					s = "";
				row.setValue(0, s.trim());
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
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

	public Collection getBankGroupsRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, EJBException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, a.code, b.value, a.criterionId, s.value, c.isDefault " + "from IN_BANK_GROUPS a, SYS_STRINGS b, IN_CRITERION c, SYS_STRINGS s "
					+ "where b.id=a.nameStrID and (b.langID=? or b.langID=1) and " + "c.nameStrId=s.id and (s.langID=? or s.langID=1) and a.criterionid=c.id "
					+ "order by c.id, a.id, b.langID DESC, s.langID DESC");
			ps.setInt(1, langID);
			ps.setInt(2, langID);
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new BankGroupPK(rs.getInt(1)), 6);
				String s = rs.getString(2);
				if (s == null)
					s = "";
				row.setValue(0, s.trim());
				String desc = rs.getString(3);
				if (desc == null) {
					desc = "";
				} else {
					desc = LocaleUtil.encode(desc.trim(), encoding);
				}
				row.setValue(3, desc);

				String cid = rs.getString(4);
				if (cid == null) {
					continue;
				}
				row.setValue(2, cid.trim());

				String cDdesc = rs.getString(5);
				if (cDdesc == null) {
					cDdesc = "";
				} else {
					cDdesc = LocaleUtil.encode(cDdesc.trim(), encoding);
				}
				row.setValue(4, cDdesc);
				row.setValue(1, cDdesc + " / " + desc);
				row.setValue(5, rs.getString(6));

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

	public Collection getBankCriterionRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, EJBException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, a.code, b.value, a.isDefault " + "from IN_CRITERION a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=? or b.langID=1) "
					+ "order by a.id, b.langID DESC");
			ps.setInt(1, langID);
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new BankCriterionPK(rs.getInt(1)), 3);

				String s = rs.getString(2);
				if (s == null)
					s = "";
				row.setValue(0, s.trim());

				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(1, desc);

				row.setValue(2, rs.getString(4));

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

	public Collection getNotAssignedBankRows(Handle userHandle, Handle languageHandle, BankCriterionPK criterionPK) throws FinaTypeException, EJBException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select b.id, b.code, s.value from in_banks b, sys_strings s " + "where b.namestrid=s.id and (s.langID=? or s.langID=1) and "
					+ "b.id not in (select m.bankid from in_bank_groups g, in_criterion c, mm_bank_group m " + "             where c.id=g.criterionid and c.id=? and m.bankgroupid=g.id) "
					+ "order by b.id, s.langid desc");
			ps.setInt(1, langID);
			ps.setInt(2, criterionPK.getId());
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new BankCriterionPK(rs.getInt(1)), 2);

				String s = rs.getString(2);
				if (s == null)
					s = "";
				row.setValue(0, s.trim());

				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
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

	public Collection getLicenceTypesRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, EJBException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, b.value, b.langID " + "from IN_LICENCE_TYPES a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=? or b.langID=1) "
					+ "order by a.id, b.langID DESC");
			ps.setInt(1, langID);
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new LicenceTypePK(rs.getInt(1)), 1);
				String desc = rs.getString(2);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(0, desc);
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

	public Collection getLicencesNodes(Handle userHandle, Handle languageHandle, BankPK bankPK) throws RemoteException, FinaTypeException, EJBException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {
			ps = con.prepareStatement("select c.id, c.code, b.value " + " from IN_LICENCE_TYPES a, SYS_STRINGS b, IN_LICENCES c "
					+ "where b.id=a.nameStrID and (b.langID=? or b.langID=1) and c.typeID=a.id " + "and c.bankID=? order by c.id, b.langID DESC");
			ps.setInt(1, langID);
			ps.setInt(2, bankPK.getId());
			rs = ps.executeQuery();

			Node root = new Node(new LicencePK(0), "        ", new Integer(-1));
			Node node = null;
			while (rs.next()) {
				LicencePK pk = new LicencePK(rs.getInt(1));
				if (node != null) {
					LicencePK prevPK = (LicencePK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}

				String code = rs.getString(2).trim();
				String s = "";

				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				node = new Node(pk, s + desc, new Integer(2));
				node.putProperty("code", code);
				node.putProperty("desc", desc);
				nodes.add(node);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	public Collection getBankGroupNodes(Handle userHandle, Handle languageHandle, BankPK bankPK) throws RemoteException, FinaTypeException, EJBException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		List nodes = new LinkedList();
		try {
			ps = con.prepareStatement("select a.id, a.code, b.value, s.value, c.isDefault " + "from IN_BANK_GROUPS a, MM_BANK_GROUP mm, SYS_STRINGS b, IN_CRITERION c, SYS_STRINGS s "
					+ "where b.id=a.nameStrID and (b.langID=? or b.langID=1) and s.id=c.nameStrID and (s.langID=? or s.langID=1) "
					+ "and mm.bankID=? and mm.bankGroupID=a.id and a.criterionid=c.id order by c.id, a.id, b.langID DESC, s.langid DESC");
			ps.setInt(1, langID);
			ps.setInt(2, langID);
			ps.setInt(3, bankPK.getId());
			rs = ps.executeQuery();

			Node node = null;
			while (rs.next()) {
				BankGroupPK pk = new BankGroupPK(rs.getInt(1));
				if (node != null) {
					BankGroupPK prevPK = (BankGroupPK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}

				String code = rs.getString(2).trim();

				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);

				String cdesc = rs.getString(4);
				if (cdesc != null) {
					cdesc = LocaleUtil.encode(cdesc.trim(), encoding);
					desc = cdesc + " / " + desc;
				}

				boolean isDefault = rs.getInt(5) == BankCriterionConstants.DEF_CRITERION;

				node = new Node(pk, desc, isDefault ? new Integer(BanksConstants.NODETYPE_DEF_BANK_GROUP_NODE) : new Integer(BanksConstants.NODETYPE_BANK_GROUP_NODE));
				node.setDefaultNode(isDefault);
				node.putProperty("code", code);
				node.setParentPK(bankPK);

				nodes.add(node);
			}

			Collections.sort(nodes);

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	public Collection getBranchNodes(Handle userHandle, Handle languageHandle, BankPK bankPK) throws RemoteException, FinaTypeException, EJBException {

		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {
			// Old SQL
			String sql_old = "select a.id, c.value, d.value, e.value " + "from IN_BANK_BRANCHES a, IN_REGIONS b, SYS_STRINGS c, SYS_STRINGS d,SYS_STRINGS e "
					+ "where b.id=a.bankRegionStrID and b.cityStrID=c.id and (c.langID=? or c.langID=1) " + "and b.id=a.bankRegionStrID and b.regionStrID=d.id and (d.langID=? or d.langID=1) "
					+ "and e.id=a.shortNameStrID and (e.langID=? or e.langID=1)  and bankID=? " + "order by a.id, c.langID, d.langID, e.langID DESC";

			String sql = "select a.id,a.BANKREGIONSTRID ,d.value from IN_BANK_BRANCHES a left outer join SYS_STRINGS d on d.langID=? and d.id=a.shortNameStrID where bankID=? order by d.VALUE ,a.id DESC";
			ps = con.prepareStatement(sql);

			// int langID =
			// ((LanguagePK)languageHandle.getEJBObject().getPrimaryKey
			// ()).getId();
			ps.setInt(1, langID);
			// ps.setInt(2, langID);
			// ps.setInt(3, langID);
			ps.setInt(2, bankPK.getId());
			rs = ps.executeQuery();

			Node root = new Node(new BranchPK(0), "        ", new Integer(-1));
			Node node = null;
			while (rs.next()) {
				BranchPK pk = new BranchPK(rs.getInt(1));
				if (node != null) {
					BranchPK prevPK = (BranchPK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}

				// String s = "";

				// String desc1 = rs.getString(2);
				// if (desc1 == null)
				// desc1 = null;
				// else
				// desc1 = LocaleUtil.encode(desc1.trim(), encoding);

				String desc2 = rs.getString(3);
				if (desc2 == null)
					desc2 = "NONAME";
				else
					desc2 = LocaleUtil.encode(desc2.trim(), encoding);
				//
				// String desc3 = rs.getString(4);
				// if (desc3 == null)
				// desc3 = "";
				// else
				// desc3 = LocaleUtil.encode(desc3.trim(), encoding);

				// node = new Node(pk, s + "[" + desc1 + ", " + desc2 + "] " +
				// desc3, new Integer(4));

				node = new Node(pk, desc2, new Integer(4));

				nodes.add(node);
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	public Collection getBankManagNodes(Handle userHandle, Handle languageHandle, BankPK bankPK) throws RemoteException, FinaTypeException, EJBException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {
			ps = con.prepareStatement("select a.id, c.value, d.value, e.value " + "from IN_BANK_MANAGEMENT a, IN_MANAGING_BODIES b, SYS_STRINGS c, SYS_STRINGS d, SYS_STRINGS e "
					+ "where b.id=a.managingBodyID and b.postStrID=c.id and (c.langID=? or c.langID=1) " + "and d.id=a.nameStrId and (d.langID=? or d.langID=1) "
					+ "and e.id=a.lastNameStrId and (e.langID=? or e.langID=1) and bankID=? " + "order by a.id, c.langID, d.langID, e.langID DESC");
			// int langID =
			// ((LanguagePK)languageHandle.getEJBObject().getPrimaryKey
			// ()).getId();
			ps.setInt(1, langID);
			ps.setInt(2, langID);
			ps.setInt(3, langID);
			ps.setInt(4, bankPK.getId());
			rs = ps.executeQuery();

			Node root = new Node(new BankManagPK(0), "        ", new Integer(-1));
			Node node = null;
			while (rs.next()) {
				BankManagPK pk = new BankManagPK(rs.getInt(1));
				if (node != null) {
					BankManagPK prevPK = (BankManagPK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}

				String s = "";

				String desc1 = rs.getString(2);
				if (desc1 == null)
					desc1 = null;
				else
					desc1 = LocaleUtil.encode(desc1.trim(), encoding);

				String desc2 = rs.getString(3);
				if (desc2 == null)
					desc2 = "";
				else
					desc2 = LocaleUtil.encode(desc2.trim(), encoding);

				String desc3 = rs.getString(4);
				if (desc3 == null)
					desc3 = "";
				else
					desc3 = LocaleUtil.encode(desc3.trim(), encoding);
				node = new Node(pk, s + "[" + desc1 + "] " + desc2 + " " + desc3, new Integer(3));

				nodes.add(node);
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getBranchManagNodes(Handle userHandle, Handle languageHandle, BranchPK branchPK) throws RemoteException, FinaTypeException, EJBException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {
			String sql = "select brMan.id as BRANCH_MAN_ID, c.value as MAN_BODY_VAL, d.value as BRANCH_MAN_VAL, e.value as BRANCH_MAN_LASTNAME from IN_MANAGING_BODIES manBody left join SYS_STRINGS c on manBody.postStrID=c.id  and (c.langID=?), IN_BRANCH_MANAGEMENT brMan left join SYS_STRINGS d on d.id=brMan.nameStrId and (d.langID=?) ,IN_BRANCH_MANAGEMENT brMan2 left join SYS_STRINGS e on e.id=brMan2.lastNameStrId and (e.langID=?) where manBody.id=brMan.managingBodyID and (brMan.branchId=?)  order by c.value ,d.value,e.value DESC";

			ps = con.prepareStatement(sql);

			ps.setInt(1, langID);
			ps.setInt(2, langID);
			ps.setInt(3, langID);
			ps.setInt(4, branchPK.getId());
			rs = ps.executeQuery();

			Node node = null;
			while (rs.next()) {
				BranchManagPK pk = new BranchManagPK(rs.getInt(1));
				if (node != null) {
					BranchManagPK prevPK = (BranchManagPK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}

				String manBodyVal = rs.getString("MAN_BODY_VAL");
				if (manBodyVal == null)
					manBodyVal = "NONAME";
				else
					manBodyVal = LocaleUtil.encode(manBodyVal.trim(), encoding);

				String branchManVal = rs.getString("BRANCH_MAN_VAL");
				if (branchManVal == null)
					branchManVal = "NONAME";
				else
					branchManVal = LocaleUtil.encode(branchManVal.trim(), encoding);

				String branchManLastName = rs.getString("BRANCH_MAN_LASTNAME");
				if (branchManLastName == null)
					branchManLastName = "NONAME";
				else
					branchManLastName = LocaleUtil.encode(branchManLastName.trim(), encoding);
				node = new Node(pk, "[" + manBodyVal + "] " + branchManVal + " " + branchManLastName, new Integer(5));

				nodes.add(node);
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return nodes;
	}

	/** Returns list of banks */
	public Collection getBanks(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.review", "fina2.bank.amend");

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<TableRowImpl> bankList = new ArrayList<TableRowImpl>();
		try {
			User user = (User) userHandle.getEJBObject();
			Language lang = (Language) languageHandle.getEJBObject();
			int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
			// If the current user isn't SA, the getting only bank list for
			// the current user.
			String userBanksSql = "";
			if (!user.getLogin().equals("sa")) {
				int userId = ((UserPK) user.getPrimaryKey()).getId();
				userBanksSql = " and b.id in (select c.bankID from SYS_USER_BANKS c where c.userID = " + userId + ") ";
			}

			String sql = "select b.id, bt.code bank_type, bg.code bank_group, b.code, b.namestrid, b.email as bEmail ,b.phone as bPhone ,s.value as bAddress ,b.regionid as bTelex ,bg.criterionid as bg_crit_id ,ss.value from  in_bank_types bt,sys_strings s, IN_BANK_GROUPS bg, MM_BANK_GROUP mm,in_banks b left outer join SYS_STRINGS ss on b.namestrid=ss.ID and ss.langid="
					+ langID + " where b.typeid = bt.id and b.id = mm.bankid and (s.id=b.addressstrid and s.langid=" + langID + ") and bg.id = mm.bankgroupid " + userBanksSql + " order by 2 ";

			ps = con.prepareStatement(sql);

			rs = ps.executeQuery();
			int number = 1;

			String encoding = LocaleUtil.getEncoding(languageHandle);

			while (rs.next()) {

				/*
				 * int critId = rs.getInt("bg_crit_id"); PreparedStatement pss =
				 * con.prepareStatement(
				 * "SELECT * FROM IN_CRITERION WHERE ID=? AND ISDEFAULT=1");
				 * pss.setInt(1, critId); ResultSet rss = pss.executeQuery(); if
				 * (!rss.next()) { continue; }
				 */

				BankPK bankId = new BankPK(rs.getInt(1));
				TableRowImpl row = new TableRowImpl(bankId, 10);

				// Number
				row.setValue(0, String.valueOf(number));

				// Bank type code
				String s = rs.getString(2);
				row.setValue(1, (s == null) ? "" : s.trim());

				// Bank group code
				s = rs.getString(3);
				row.setValue(2, (s == null) ? "" : s.trim());

				// Bank code
				s = rs.getString(4);
				row.setValue(3, (s == null) ? "" : s.trim());

				// Bank name
				String ss = rs.getString(11);// LocaleUtil.getString(con,
												// languageHandle,
												// rs.getInt(5));
				String bankName = null;
				if (ss == null)
					bankName = "NONAME";
				else
					bankName = LocaleUtil.encode(ss, encoding);
				row.setValue(4, bankName);

				Long regionId = rs.getLong("bTelex");
				/*
				 * if ((s != null) && (s.trim().length() > 0)) { StringTokenizer
				 * st = new StringTokenizer(s, "/"); row.setValue(6,
				 * st.nextToken()); row.setValue(5, st.nextToken()); } else {
				 * row.setValue(5, s); row.setValue(6, s); }
				 */
				row.setValue(5, "id:");
				row.setValue(6, regionId + "");

				s = rs.getString("bEmail");
				row.setValue(7, (s == null) ? "" : s.trim());

				s = rs.getString("bPhone");
				row.setValue(8, (s == null) ? "" : s.trim());

				s = rs.getString("bAddress");
				row.setValue(9, (s == null) ? "" : s.trim());

				/*
				 * Collection col = getLicencesNodes(userHandle, languageHandle,
				 * bankId); Iterator iter = col.iterator(); Vector v = null;
				 * while (iter.hasNext()) { Node n = (Node) iter.next(); s +=
				 * n.getProperty("desc") + ";"; }
				 * 
				 * row.setValue(10, (s == null) ? "" : s.trim());
				 */
				// Adding to the result list
				bankList.add(row);

				number++;
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return bankList;
	}

	/** Returns list of banks */
	public Collection loadBanks(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.review", "fina2.bank.amend");

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<TableRowImpl> bankList = new ArrayList<TableRowImpl>();
		try {
			User user = (User) userHandle.getEJBObject();
			Language lang = (Language) languageHandle.getEJBObject();
			int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
			// If the current user isn't SA, the getting only bank list for
			// the current user.
			String userBanksSql = "";
			if (!user.getLogin().equals("sa")) {
				int userId = ((UserPK) user.getPrimaryKey()).getId();
				userBanksSql = " and b.id in (select c.bankID from SYS_USER_BANKS c where c.userID = " + userId + ") ";
			}

			String sql = "select b.id, bt.code bank_type, bg.code bank_group, b.code, b.namestrid, b.email as bEmail ,b.phone as bPhone ,s.value as bAddress ,b.regionid as bRegion ,bg.criterionid as bg_crit_id ,ss.value from  in_bank_types bt,sys_strings s, IN_BANK_GROUPS bg, MM_BANK_GROUP mm,in_banks b left outer join SYS_STRINGS ss on b.namestrid=ss.ID and ss.langid="
					+ langID + " where b.typeid = bt.id and b.id = mm.bankid and (s.id=b.addressstrid and s.langid=" + langID + ") and bg.id = mm.bankgroupid " + userBanksSql + " order by 2 ";

			ps = con.prepareStatement(sql);

			rs = ps.executeQuery();
			int number = 1;

			String encoding = LocaleUtil.getEncoding(languageHandle);

			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject.narrow(ref, RegionStructureSessionHome.class);

			RegionStructureSession session = home.create();

			Map<Integer, String> map = session.getProperties(languageHandle);

			String maxLevel = map.get(0);

			Integer regionsLevelSize = null;
			if (maxLevel != null) {
				regionsLevelSize = Integer.parseInt(maxLevel);
			}
			int tableRowsSize = 0;
			if (regionsLevelSize != null)
				tableRowsSize = 8 + (regionsLevelSize * 2);
			else
				tableRowsSize = 9;

			while (rs.next()) {

				BankPK bankId = new BankPK(rs.getInt(1));
				TableRowImpl row = new TableRowImpl(bankId, tableRowsSize);

				// Number
				row.setValue(0, String.valueOf(number));

				// Bank type code
				String s = rs.getString(2);
				row.setValue(3, (s == null) ? "" : s.trim());

				// Bank group code
				s = rs.getString(3);
				row.setValue(4, (s == null) ? "" : s.trim());

				// Bank code
				s = rs.getString(4);
				row.setValue(1, (s == null) ? "" : s.trim());

				// Bank name
				String ss = rs.getString(11);
				String bankName = null;
				if (ss == null)
					bankName = "NONAME";
				else
					bankName = LocaleUtil.encode(ss, encoding);
				row.setValue(2, bankName);

				// insert regions
				Long regionId = rs.getLong("bRegion");
				if (regionId != null) {
					String regionLabel = session.getNodePathLabel(new RegionStructureNodePK(regionId), languageHandle, new StringBuffer("/"));

					int count = 5;
					StringTokenizer lineToken = new StringTokenizer(regionLabel, "|");
					while (lineToken.hasMoreElements()) {
						StringTokenizer dotToken = new StringTokenizer(lineToken.nextElement().toString(), ":");
						if (dotToken.hasMoreElements()) {
							String regionCode = dotToken.nextElement() + "";
							row.setValue(count, regionCode);
						}
						if (dotToken.hasMoreElements()) {
							String regionDescription = dotToken.nextElement() + "";
							count++;
							row.setValue(count, regionDescription);
							count++;
						}
					}
				}

				s = rs.getString("bEmail");
				row.setValue(tableRowsSize - 3, (s == null) ? "" : s.trim());

				s = rs.getString("bPhone");
				row.setValue(tableRowsSize - 2, (s == null) ? "" : s.trim());

				s = rs.getString("bAddress");
				row.setValue(tableRowsSize - 1, (s == null) ? "" : s.trim());

				bankList.add(row);

				number++;
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return bankList;
	}

	/** Returns bank types */
	public List<Node> getBankTypes(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException {

		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.review");

		ArrayList<Node> bankTypes = new ArrayList<Node>(); // The result list
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select a.id, a.code, a.namestrid " + "from in_bank_types a order by a.code";

			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				Integer id = rs.getInt(1);
				String code = "[" + rs.getString(2).trim() + "] ";
				String name = LocaleUtil.getString(con, languageHandle, rs.getInt(3));

				Node node = new Node(id, code + name, BanksConstants.NODETYPE_BANK_TYPE);

				/* Adding to the result list */
				bankTypes.add(node);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* The result list */
		return bankTypes;
	}

	/** Returns bank id by given bank code */
	public Integer getBankId(String bankCode) {
		Integer bankId = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select a.id from in_banks a where rtrim(a.code) = ?";

			ps = con.prepareStatement(sql);
			ps.setString(1, bankCode);
			rs = ps.executeQuery();

			if (rs.next()) {
				bankId = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return bankId;
	}
}
