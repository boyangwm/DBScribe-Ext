package fina2.upload;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.i18n.LanguageSession;
import fina2.i18n.LanguageSessionHome;
import fina2.returns.ImportManagerSession;
import fina2.returns.ImportManagerSessionHome;
import fina2.security.User;

public class UploadFileSessionBean implements SessionBean {

	private static final long serialVersionUID = 1L;
	private SessionContext ctx;
    private Logger logger=Logger.getLogger(UploadFileSessionBean.class);
    
	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
		this.ctx = ctx;
	}

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
	}

	public void upload(Map<String, byte[]> files) throws RemoteException, EJBException{
		
		try {
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFile");
			UploadFileHome home = (UploadFileHome)PortableRemoteObject.narrow(ref, UploadFileHome.class);
			for (Iterator<String> it = files.keySet().iterator(); it.hasNext();) {
				String fileName = it.next();
				byte [] file = files.get(fileName);
				home.create(file, fileName);
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		}
	}
	
	public List<UploadedFileInfo> getUploadedFiles(String username, String bankCode, int status, Date from, Date to) throws RemoteException, EJBException{
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<UploadedFileInfo> result = new ArrayList<UploadedFileInfo>();
		boolean admin = ctx.isCallerInRole("fina2.web.admin");
		boolean date = false;
		String filter = "";
		
		if(admin && !username.equalsIgnoreCase("ALL")){
			filter = " username = '" + username +"'";
		}
		
		if(!admin){
			filter = " username = '" + ctx.getCallerPrincipal().getName() + "'";
		}
		
		if(admin && !bankCode.equalsIgnoreCase("ALL")){
			if(filter.equals(""))
				filter +=" bankCode = '" + bankCode + "'";
			else 
				filter +=" and bankCode = '" + bankCode + "'";
		}
			
		if(status != -1){			
			if(filter.equals(""))
				filter +=" status = " + status + "";
			else
				filter +=" and status = " + status + "";
		}
		
		if(from != null && to != null){
			date = true;
			if(filter.equals(""))
				filter +=" uploadedTime BETWEEN ? and ? ";
			else
				filter +=" and uploadedTime BETWEEN ? and ? ";
		}		
		
		if(!filter.equalsIgnoreCase(""))
			filter = " WHERE " + filter;
		
		try {
			con = DatabaseUtil.getConnection();
			String sql = "SELECT id,username,fileName,bankCode,status,uploadedTime FROM SYS_UPLOADEDFILE " + filter;
			ps = con.prepareStatement(sql);
			if(date) {
				ps.setDate(1, new java.sql.Date(from.getTime()));
				ps.setDate(2, new java.sql.Date(to.getTime()));
			}
			rs = ps.executeQuery();
			while(rs.next()){
				UploadedFileInfo file = new UploadedFileInfo(rs.getInt("id"),rs.getString("username"),rs.getString("fileName"),
						rs.getInt("status"),rs.getTimestamp("uploadedTime"),rs.getString("bankCode"));
				result.add(file);
			}
		} catch(Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		
		return result;
	}
	
	public void reject(int id)throws RemoteException,EJBException,FinaTypeException {
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("Select * from SYS_UPLOADED_RETURNS WHERE uploadedFileId = ?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(!rs.next()) {
				ps = con.prepareStatement("UPDATE SYS_UPLOADEDFILE SET status = ? where id = ?");
				ps.setInt(1, UploadFileStatus.REJECTED.ordinal());
				ps.setInt(2, id);
				ps.execute();
			} else {
				throw new FinaTypeException(Type.FINA2_WEB_REJECT);
			}
		} catch (FinaTypeException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}
	
	public void remove(int id) throws RemoteException,EJBException{
		
		try {
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFile");
			UploadFile bean = ((UploadFileHome)PortableRemoteObject.narrow(ref, UploadFileHome.class)).findByPrimaryKey(new UploadFilePK(id));
			bean.remove();
		} catch (Exception e) {
			throw new EJBException(e);
		}
	}
	
	public void importReturns(LinkedList<byte[]> xmls, User user, Language lang, int fileId) throws RemoteException,EJBException,FinaTypeException {
		
		Connection con = null;
		PreparedStatement insert = null;
		PreparedStatement update = null;
		try {
			con = DatabaseUtil.getConnection();
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/returns/ImportManagerSession"); 
			ImportManagerSession session = ((ImportManagerSessionHome)PortableRemoteObject.narrow(ref, ImportManagerSessionHome.class)).create();
			List<Integer> ids = session.importedUploadDocuments(user.getHandle(), lang.getHandle(), xmls);
			
			if(ids.size() > 0) {
				insert = con.prepareStatement("INSERT INTO SYS_UPLOADED_RETURNS(uploadedFileId, importedReturnsId) VALUES("+fileId+",?)");
				for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
					insert.setInt(1, it.next());
					insert.addBatch();
				}
				insert.executeBatch();
				update = con.prepareStatement("UPDATE SYS_UPLOADEDFILE SET status = ? WHERE id = ?");
				update.setInt(1, UploadFileStatus.CONVERTED.ordinal());
				update.setInt(2, fileId);
				update.execute();
			}
		} catch (SQLException ex) {
			throw new EJBException(ex);
		} catch (Exception ex) {
			throw new FinaTypeException(Type.FINA2_WEB_INVALID_RETURN);
		}
		finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(insert);
			DatabaseUtil.closeStatement(update);
		}
	}
	
	public List<ImportedReturnInfo> getImportedReturns(int fileId) throws RemoteException, EJBException {
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ImportedReturnInfo> returns = new ArrayList<ImportedReturnInfo>();
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT i.id,i.returnCode,i.versionCode,i.bankCode,u.login,i.periodStart," +
					"i.periodEnd,i.status,i.message FROM IN_IMPORTED_RETURNS i, SYS_USERS u,SYS_UPLOADED_RETURNS r" +
					" WHERE r.uploadedFileId = ? and r.importedReturnsId = i.id and i.userId = u.id");
			ps.setInt(1, fileId);
			rs = ps.executeQuery();
			while(rs.next()) {
				ImportedReturnInfo ret = new ImportedReturnInfo(rs.getInt("id"), rs.getString("returnCode"), rs.getString("versionCode"), rs.getString("bankCode"),
						rs.getString("login"),rs.getDate("periodStart"),rs.getDate("periodEnd"),rs.getInt("status"), rs.getString("message"));
				returns.add(ret);
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		
		return returns;
	}
	
	public List<Language> getLanguages() throws RemoteException, EJBException {
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Language> langs = new ArrayList<Language>();
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT id FROM SYS_LANGUAGES");			
			rs = ps.executeQuery();
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/i18n/Language");
			LanguageHome langHome = (LanguageHome)PortableRemoteObject.narrow(ref, LanguageHome.class);
			while(rs.next()) {	
				langs.add(langHome.findByPrimaryKey(new LanguagePK(rs.getInt("id"))));
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		
		return langs;
	}
	
	public Map<String, String> getMessageBundle(Language lang) throws RemoteException, EJBException {
		
		Map<String, String> bundle = new HashMap<String, String>();
		try {
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/i18n/LanguageSession");
			LanguageSession session = ((LanguageSessionHome)PortableRemoteObject.narrow(ref, LanguageSessionHome.class)).create();
			Properties p = session.getLanguageBundle(lang.getHandle());
			for(Enumeration<Object> en = p.keys(); en.hasMoreElements() ;) {
				String key = en.nextElement().toString();
				bundle.put(key, p.getProperty(key));
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		}
		
		return bundle;
	}
	
	public List<String> getUsers() throws RemoteException,EJBException {
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> users = new ArrayList<String>();
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT DISTINCT username FROM SYS_UPLOADEDFILE");
			rs = ps.executeQuery();
			while(rs.next()) {
				users.add(rs.getString("username"));
			}			
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		
		return users;
	}
	
	public List<String> getBanks() throws RemoteException,EJBException {
	
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> banks = new ArrayList<String>();
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT DISTINCT uf.bankCode FROM SYS_UPLOADEDFILE uf,IN_BANKS b,SYS_USERS u " +
					"WHERE uf.bankCode = b.code and u.login = ? and b.id IN (SELECT BANKID FROM SYS_USER_BANKS WHERE USERID = u.id)");
			ps.setString(1, ctx.getCallerPrincipal().getName());
			rs = ps.executeQuery();
			while (rs.next()) {
				banks.add(rs.getString("bankCode"));
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		
		return banks;
	}
	public void setStatus(int id,int status)throws RemoteException,EJBException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con=DatabaseUtil.getConnection();
			ps=con.prepareStatement("UPDATE SYS_UPLOADEDFILE SET status=? WHERE id=?");
			ps.setInt(1, status);
			ps.setInt(2, id);
			ps.execute();
			
		}
		catch(Exception ex){
			throw new EJBException(ex);
		}
		finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}
}
