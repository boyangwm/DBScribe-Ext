/*
 * RepositorySessionBean.java
 *
 * Created on 10 Сентябрь 2002 г., 0:11
 */

package fina2.reportoo.repository;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import fina2.bank.BankGroupPK;
import fina2.bank.BankPK;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.metadata.MDTNodePK;
import fina2.period.PeriodPK;
import fina2.ui.table.TableRowImpl;

/**
 * 
 * @author Shota Shalamberidze
 * @version
 */
public class RepositorySessionBean implements SessionBean {

	private SessionContext ctx;

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
	}

	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void setSessionContext(javax.ejb.SessionContext sessionContext) throws javax.ejb.EJBException, java.rmi.RemoteException {
		ctx = sessionContext;
	}

	public int createFormula(Formula formula, int parentID) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int id = 1;
		try {
			ps = con.prepareStatement("select max(id) from OUT_REPOSITORY");
			rs = ps.executeQuery();
			if (rs.next()) {
				id = rs.getInt(1) + 1;
			}
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("insert into OUT_REPOSITORY (id, name, script, parentID, type) values(?,?,?,?,1)");
			ps.setInt(1, id);
			ps.setString(2, formula.getName());
			ps.setString(3, formula.getFormula());
			ps.setInt(4, parentID);

			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			int pId = 1;

			ps = con.prepareStatement("insert into OUT_REPOSITORY_PARAMS (id, formulaID, type, name) values(?,?,?,?)");
			for (Iterator iter = formula.getParameters().iterator(); iter.hasNext(); pId++) {
				Parameter p = (Parameter) iter.next();

				ps.setInt(1, pId);
				ps.setInt(2, id);
				ps.setInt(3, p.getType());
				ps.setString(4, p.getName());

				ps.executeUpdate();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return id;
	}

	public int createFolder(String name, int parentID) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int id = 1;
		try {
			ps = con.prepareStatement("select max(id) from OUT_REPOSITORY");
			rs = ps.executeQuery();

			if (rs.next()) {
				id = rs.getInt(1) + 1;
			}
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("insert into OUT_REPOSITORY (id, name, parentID, type) values(?,?,?,0)");
			ps.setInt(1, id);
			ps.setString(2, name);
			ps.setInt(3, parentID);

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return id;
	}

	public Formula findFormula(int id) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Formula f = null;
		try {
			ps = con.prepareStatement("select name, script from OUT_REPOSITORY where id=?");
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (!rs.next()) {
				return null;
			}
			String name = rs.getString(1);
			if (name == null || name.trim().length() == 0)
				name = "NONAME";
			f = new Formula(name, "");
			f.setId(id);
			f.setFormula(rs.getString(2));

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select name, type from OUT_REPOSITORY_PARAMS where formulaID=? order by id");
			ps.setInt(1, id);
			rs = ps.executeQuery();

			while (rs.next()) {
				Parameter p = new Parameter(rs.getString(1), "", rs.getInt(2));
				f.getParameters().add(p);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return f;
	}

	public Collection getFormulas() throws EJBException {

		Connection con = DatabaseUtil.getConnection();
		ArrayList formulas = new ArrayList();
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id, name, script from OUT_REPOSITORY where type=1");

			ps2 = con.prepareStatement("select name, type from OUT_REPOSITORY_PARAMS where formulaID=? order by id");

			rs = ps.executeQuery();
			while (rs.next()) {

				int formulaId = rs.getInt(1);
				Formula formula = new Formula(rs.getString(2), "");
				formula.setFormula(rs.getString(3));

				formula.setId(formulaId);

				ps2.setInt(1, formulaId);
				ResultSet rs2 = ps2.executeQuery();

				while (rs2.next()) {
					Parameter param = new Parameter(rs2.getString(1), "", rs2.getInt(2));
					formula.getParameters().add(param);
				}
				DatabaseUtil.closeResultSet(rs2);

				formulas.add(formula);
			}
		} catch (SQLException ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(ps2);
		}
		return formulas;
	}

	public void deleteFormula(int id) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("delete from OUT_REPOSITORY_PARAMS where formulaID=?");
			ps.setInt(1, id);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from OUT_REPOSITORY where id=?");
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void deleteFolder(int id) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("delete from OUT_REPOSITORY_PARAMS where formulaID in " + "    (select id from OUT_REPOSITORY where parentID=?)");
			ps.setInt(1, id);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from OUT_REPOSITORY where parentID=?");
			ps.setInt(1, id);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from OUT_REPOSITORY where id=?");
			ps.setInt(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
	}

	public void updateFormula(int id, Formula formula) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("delete from OUT_REPOSITORY_PARAMS where formulaID=?");
			ps.setInt(1, id);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			int pId = 1;
			ps = con.prepareStatement("insert into OUT_REPOSITORY_PARAMS (id, formulaID, type, name) values(?,?,?,?)");
			for (Iterator iter = formula.getParameters().iterator(); iter.hasNext(); pId++) {
				Parameter p = (Parameter) iter.next();

				ps.setInt(1, pId);
				ps.setInt(2, id);
				ps.setInt(3, p.getType());
				ps.setString(4, p.getName());

				ps.executeUpdate();
			}
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("update OUT_REPOSITORY " + "    set name=?, " + "    script=? " + "where id=?");
			ps.setString(1, formula.getName());
			ps.setString(2, formula.getFormula());
			ps.setInt(3, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getFolderName(int id) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String folderName = "";
		try {
			ps = con.prepareStatement("select name from OUT_REPOSITORY where id=?");
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (rs.next())
				folderName = rs.getString(1);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return folderName;
	}

	public void setFolderName(int id, String name) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update OUT_REPOSITORY " + "    set name=? " + "where id=?");
			ps.setString(1, name);
			ps.setInt(2, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public Folder getRepositoryTree() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Folder root = new Folder("    ");
		try {
			Hashtable h = new Hashtable();

			ps = con.prepareStatement("select type, name, id, parentID, script " + "from OUT_REPOSITORY " + "order by parentID");
			rs = ps.executeQuery();

			root.setId(0);
			h.put(new Integer(0), root);

			while (rs.next()) {
				int type = rs.getInt(1);
				if (type == 0) { // Folder
					String name = rs.getString(2);
					if ((name == null) || (name.trim().length() == 0)) {
						name = "NONAME";
					}
					Folder f = new Folder(name);
					int id = rs.getInt(3);
					f.setId(id);
					h.put(new Integer(id), f);

					Integer parentID = new Integer(rs.getInt(4));
					Folder parent = (Folder) h.get(parentID);
					parent.getChildren().add(f);
				} else { // Formula
					String name = rs.getString(2);
					if ((name == null) || (name.trim().length() == 0)) {
						name = "NONAME";
					}
					Formula f = new Formula(name, "");
					int id = rs.getInt(3);
					f.setId(id);

					Integer parentID = new Integer(rs.getInt(4));
					Folder parent = (Folder) h.get(parentID);
					if (parent != null)
						parent.getChildren().add(f);

					f.setFormula(rs.getString(5));
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return root;
	}

	public Collection getPeerValues(Collection values, Handle userHandle, Handle languageHandle) throws EJBException, RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		Vector v = new Vector();
		try {
			/*
			 * PreparedStatement pstmt = con.prepareStatement(
			 * "select a.id, a.code, b.value, b.langID "+
			 * "from IN_BANK_GROUPS a, SYS_STRINGS b "+
			 * "where a.id=? and b.id=a.nameStrID and (b.langID=? or b.langID=1) "
			 * + "order by a.id, b.langID DESC" );
			 */
			ps = con.prepareStatement("select a.id, a.code, b.value, b.langID " + "from IN_BANK_GROUPS a, SYS_STRINGS b "
					+ "where rtrim(a.code)=? and b.id=a.nameStrID and (b.langID=? or b.langID=1) " + "order by a.id, b.langID DESC");

			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object value = iter.next();
				// if(value instanceof String) {
				// TableRowImpl row = new TableRowImpl((String)value, 2);
				// row.setValue(0, (String)value);
				// v.add(row);
				// }
				// if(value instanceof BankGroupPK) {
				// pstmt.setInt(1, ((BankGroupPK)value).getId());
				// pstmt.setInt(1, ((BankGroupPK)value).getId());
				ps.setString(1, (String) value);
				ps.setInt(2, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId());
				ResultSet rs = ps.executeQuery();

				Language language = (Language) languageHandle.getEJBObject();

				TableRowImpl row = new TableRowImpl((String) value, 2);
				row.setValue(0, (String) value);
				TableRowImpl prevRow = null;
				while (rs.next()) {
					row = new TableRowImpl(new BankGroupPK(rs.getInt(1)), 2);

					row.setValue(0, LocaleUtil.encode(rs.getString(2).trim(), language.getXmlEncoding()));
					row.setValue(1, LocaleUtil.encode(rs.getString(3).trim(), language.getXmlEncoding()));
					if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
						prevRow = row;
						continue;
					}
					prevRow = row;

					v.add(row);
				}

				DatabaseUtil.closeResultSet(rs);
				// }
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
		return v;
	}

	public Collection getBankValues(Collection values, Handle userHandle, Handle languageHandle) throws EJBException, RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		Vector v = new Vector();
		try {
			/*
			 * PreparedStatement pstmt = con.prepareStatement(
			 * "select a.id, a.code, b.value, b.langID "+
			 * "from IN_BANKS a, SYS_STRINGS b "+
			 * "where a.id=? and b.id=a.nameStrID and (b.langID=? or b.langID=1) "
			 * + "order by a.id, b.langID DESC" );
			 */

			ps = con.prepareStatement("select a.id, a.code, b.value, b.langID " + "from IN_BANKS a, SYS_STRINGS b " + "where rtrim(a.code)=? and b.id=a.nameStrID and (b.langID=? or b.langID=1) "
					+ "order by a.id, b.langID DESC");

			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object value = iter.next();
				/*
				 * if(value instanceof String) { TableRowImpl row = new
				 * TableRowImpl((String)value, 2); row.setValue(0,
				 * (String)value); v.add(row); }
				 */
				// if(value instanceof BankPK) {
				// pstmt.setInt(1, ((BankPK)value).getId());
				ps.setString(1, (String) value);
				ps.setInt(2, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId());
				ResultSet rs = ps.executeQuery();

				TableRowImpl row = new TableRowImpl((String) value, 2);
				row.setValue(0, (String) value);

				TableRowImpl prevRow = null;
				while (rs.next()) {
					row = new TableRowImpl(new BankPK(rs.getInt(1)), 2);
					row.setValue(0, rs.getString(2).trim());
					row.setValue(1, rs.getString(3).trim());

					if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
						prevRow = row;
						continue;
					}
					prevRow = row;

					v.add(row);

				}

				DatabaseUtil.closeResultSet(rs);
				// }
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
		return v;
	}

	public Collection getPeriodValues(Collection values, Handle userHandle, Handle languageHandle) throws EJBException, RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		Vector v = new Vector();

		try {
			ps = con.prepareStatement("select a.id, b.value, a.periodNumber, a.fromDate, a.toDate " + "from IN_PERIODS a, SYS_STRINGS b, IN_PERIOD_TYPES c "
					+ "where a.id=? and c.id=a.periodTypeID and b.id=c.nameStrID and (b.langID=? or b.langID=1) " + "order by a.id, b.langID DESC");

			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object value = iter.next();
				// if(value instanceof String) {
				// TableRowImpl row = new TableRowImpl((String)value, 4);
				// row.setValue(0, (String)value);
				// v.add(row);
				// }
				if (value instanceof PeriodPK) {
					ps.setInt(1, ((PeriodPK) value).getId());
					ps.setInt(2, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId());
					ResultSet rs = ps.executeQuery();
					Language lang = (Language) languageHandle.getEJBObject();

					TableRowImpl prevRow = null;
					while (rs.next()) {
						TableRowImpl row = new TableRowImpl(new PeriodPK(rs.getInt(1)), 4);
						row.setValue(0, LocaleUtil.encode(rs.getString(2).trim(), lang.getXmlEncoding()));
						row.setValue(1, rs.getString(3).trim());
						row.setValue(2, LocaleUtil.date2string(lang, rs.getDate(4)));
						row.setValue(3, LocaleUtil.date2string(lang, rs.getDate(5)));

						if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
							prevRow = row;
							continue;
						}
						prevRow = row;

						v.add(row);
					}
					DatabaseUtil.closeResultSet(rs);
				}
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
		return v;
	}

	public Collection getNodeValues(Collection values, Handle userHandle, Handle languageHandle) throws EJBException, RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, a.code, b.value, b.langID " + "from IN_MDT_NODES a, SYS_STRINGS b " + "where rtrim(a.code)=? and b.id=a.nameStrID and (b.langID=? or b.langID=1) "
					+ "order by a.id, b.langID DESC");

			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object value = iter.next();
				/*
				 * if(value instanceof String) { TableRowImpl row = new
				 * TableRowImpl((String)value, 2); row.setValue(0,
				 * (String)value); v.add(row); }
				 */
				// if(value instanceof MDTNodePK) {
				ps.setString(1, (String) value);
				// pstmt.setInt(1, ((MDTNodePK)value).getId());
				ps.setInt(2, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId());
				ResultSet rs = ps.executeQuery();

				TableRowImpl row = new TableRowImpl((String) value, 2);
				row.setValue(0, (String) value);
				TableRowImpl prevRow = null;
				while (rs.next()) {
					row = new TableRowImpl(new MDTNodePK(rs.getInt(1)), 2);
					String code = rs.getString(2).trim();
					row.setValue(0, "[" + code + "] " + rs.getString(3).trim());
					row.setValue(1, code);

					if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
						prevRow = row;
						continue;
					}
					prevRow = row;

					v.add(row);
				}
				DatabaseUtil.closeResultSet(rs);
				// }
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
		return v;
	}
}
