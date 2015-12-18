package fina2.upload;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;

import fina2.db.DatabaseUtil;

public class UploadFileBean implements EntityBean {
	
	private static final long serialVersionUID = 1L;
	private EntityContext ctx;
	
	private UploadFilePK pk;
	private String fileName;
	private String bankCode;
	private byte [] file;
	private String username;
	private int status;
	private Date uploadTime;

	private Logger log = Logger.getLogger(UploadFileBean.class);
	
	public UploadFilePK getPk() {
		return pk;
	}

	public void setPk(UploadFilePK pk) {	
		this.pk = pk;
	}

	public byte [] getFile() {
		return file;
	}

	public void setFile(byte [] file) {
		this.file = file;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String user) {
		this.username = user;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
	
	public UploadFilePK ejbFindByPrimaryKey(UploadFilePK pk) throws RemoteException,FinderException,EJBException {
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT id FROM SYS_UPLOADEDFILE WHERE id = ?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();
			if(!rs.next()){
				throw new FinderException("File Don't found in Database");
			}
			this.pk = pk;
		} catch(SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		
		return pk;
	}
	
	public UploadFilePK ejbCreate(byte [] file, String filename) throws EJBException, CreateException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			ps = con.prepareStatement("select max(id) from SYS_UPLOADEDFILE");
			insert = con.prepareStatement("insert into SYS_UPLOADEDFILE (id) values(?)");

			rs = ps.executeQuery();
			rs.next();
			pk = new UploadFilePK(rs.getInt(1) + 1);
			insert.setInt(1, pk.getId());
			insert.executeUpdate();
			
			this.file = file;
			this.fileName = filename;
			this.username = ctx.getCallerPrincipal().getName();
			this.status = UploadFileStatus.UPLOADED.ordinal();
			
			log.info("User '" + username + "' uploaded file '" + filename + "'");
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		
		return pk;
	}

	public void ejbPostCreate(byte [] file, String filename) throws EJBException, CreateException {
	}

	public void ejbActivate() throws EJBException, RemoteException {	
	}

	public void ejbLoad() throws EJBException, RemoteException {
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT fileName,uploadedFile,uploadedTime,username,bankCode,status " +
					"FROM SYS_UPLOADEDFILE WHERE id = ?");
			ps.setInt(1, this.pk.getId());
			rs = ps.executeQuery();
			rs.next();
			this.fileName = rs.getString("fileName");
			Blob blob = rs.getBlob("uploadedFile");
			this.file = new byte[(int)blob.length()];
			blob.getBinaryStream().read(this.file, 0, this.file.length);
			this.uploadTime = rs.getDate("uploadedTime");
			this.username = rs.getString("username");
			this.bankCode = rs.getString("bankCode");
			this.status = rs.getInt("status");
		} catch(Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void ejbPassivate() throws EJBException, RemoteException {	
	}

	public void ejbRemove() throws RemoveException, EJBException,RemoteException {
		
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("DELETE FROM SYS_UPLOADEDFILE WHERE id = ?");
			ps.setInt(1, pk.getId());
			ps.execute();
			log.info("User '" + username + "'deleted uploaded file '" + fileName + "'");
		}catch(Exception e){
			throw new EJBException();
		}finally{
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbStore() throws EJBException, RemoteException {
		
		Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement bank = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			bank = con.prepareStatement("SELECT b.code FROM IN_BANKS b,SYS_USERS u WHERE " +
					"u.login = ? and u.id IN (SELECT ub.USERID FROM SYS_USER_BANKS ub WHERE b.id = ub.BANKID )");
			bank.setString(1, this.username);
			rs = bank.executeQuery();
			if(rs.next()) {
				this.bankCode = rs.getString("code");
			} else {
				this.bankCode = "UNKNOWN";
			}
			ps = con.prepareStatement("UPDATE SYS_UPLOADEDFILE SET username = ?,bankCode =?," +
					"fileName = ?,uploadedFile = ?,status = ?, uploadedTime = ? WHERE id = ?");			
			ps.setString(1, this.username);
			ps.setString(2, this.bankCode);
			ps.setString(3, this.fileName);
			ps.setBinaryStream(4, new ByteArrayInputStream(this.file),this.file.length);
			ps.setInt(5, this.status);
			this.uploadTime = new Date(System.currentTimeMillis());
			ps.setTimestamp(6, new Timestamp(this.uploadTime.getTime()));
			ps.setInt(7, this.pk.getId());
			ps.execute();
		} catch(Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(bank);
		}
	}

	public void setEntityContext(EntityContext ctx) throws EJBException,RemoteException {
		this.ctx = ctx;
	}

	public void unsetEntityContext() throws EJBException, RemoteException {
		this.ctx = null;
	}

}
