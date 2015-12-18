/*
 * ManagingBodySessionBean.java
 *
 * Created on 1 јпрель 2002 г., 10:05
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import fina2.FinaTypeException;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.security.ServerSecurityUtil;
import fina2.ui.table.TableRowImpl;

/**
 * 
 * @author Vasop
 * @version
 */
public class ManagingBodySessionBean implements SessionBean {

	/** Creates new ManagingBodySessionBean */

	private SessionContext ctx;

	/** Creates new BankRegionSessionBean */
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

	public Collection getManagingBodyRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException, EJBException {
		fina2.security.User user = (fina2.security.User) userHandle.getEJBObject();

		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, b.value " + "from IN_MANAGING_BODIES a, SYS_STRINGS b " + "where b.id=a.postStrId and (b.langID=? or b.langID=1) " + "order by a.id, b.langID DESC");
			ps.setInt(1, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId());
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new ManagingBodyPK(rs.getInt(1)), 1);
				row.setValue(0, rs.getString(2).trim());

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
}
