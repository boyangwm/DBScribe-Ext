/*
 * LanguageBean.java
 *
 * Created on October 15, 2001, 2:47 PM
 */

package fina2.i18n;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.util.LoggerHelper;

/**
 * Language information. CMP Entity Bean.
 * 
 * All business methods of this bean must have SUPPORTS transaction attribute
 * value.
 * 
 * @ejbHome <{LanguageHome}>
 * @ejbPrimaryKey <{LanguagePK}>
 * @ejbRemote <{Language}>
 * @author David Shalamberidze
 * @version 1
 * @since 1*/

public class LanguageBean implements EntityBean {

	private EntityContext ctx;
	// private boolean store = true;

	public LanguagePK pk;
	public String code;
	public String description;
	public String fontFace;
	public int fontSize = -1;
	public String dateFormat;
	public String numberFormat;
	public String htmlCharset;
	public String xmlEncoding;

	private LoggerHelper log = new LoggerHelper(LanguageBean.class, "Language");

	public LanguagePK ejbCreate() throws EJBException, CreateException {

		LanguagePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			ps = con.prepareStatement("select max(id) from SYS_LANGUAGES");
			insert = con
					.prepareStatement("insert into SYS_LANGUAGES (id) values(?)");

			rs = ps.executeQuery();
			rs.next();

			pk = new LanguagePK(rs.getInt(1) + 1);
			insert.setInt(1, pk.getId());
			insert.executeUpdate();

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

	public LanguagePK ejbFindByPrimaryKey(LanguagePK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from SYS_LANGUAGES where (id=?)");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Language is not found in database.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public LanguagePK ejbFindByCode(String code) throws EJBException,
			FinderException {
		LanguagePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from SYS_LANGUAGES where rtrim(code)=?");
			ps.setString(1, code);
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Language is not found in database.");
			} else {
				pk = new LanguagePK(rs.getInt(1));
			}
		} catch (SQLException e) {
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
			ps = con.prepareStatement("update SYS_LANGUAGES set " + "code=?, "
					+ "name=?, " + "dateFormat=?, " + "numberFormat=?, "
					+ "fontFace=?, " + "fontSize=?, " + "htmlCharset=?, "
					+ "xmlEncoding=? " + "where id=?");
			ps.setString(1, code + " ");
			ps.setString(2, description + " ");
			ps.setString(3, dateFormat + " ");
			ps.setString(4, numberFormat + " ");
			ps.setString(5, fontFace + " ");
			ps.setInt(6, fontSize);
			ps.setString(7, htmlCharset + " ");
			ps.setString(8, xmlEncoding + " ");
			ps.setInt(9, ((LanguagePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
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
					.prepareStatement("select code, name, dateFormat, numberFormat, fontFace, fontSize, htmlCharset, xmlEncoding "
							+ "from SYS_LANGUAGES where id=?");
			ps.setInt(1, ((LanguagePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			code = rs.getString("code").trim();
			description = rs.getString("name").trim();
			dateFormat = rs.getString("dateFormat").trim();
			numberFormat = rs.getString("numberFormat").trim();
			fontFace = rs.getString("fontFace").trim();
			fontSize = rs.getInt("fontSize");
			htmlCharset = rs.getString("htmlCharset").trim();
			xmlEncoding = rs.getString("xmlEncoding").trim();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void ejbRemove() throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		ResultSet rs = null;
		PreparedStatement exec = null;
		try {
			exec = con
					.prepareStatement("select id from SYS_STRINGS where langID=?");
			exec.setInt(1, ((LanguagePK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
				PreparedStatement ps = con
						.prepareStatement("delete from SYS_LANGUAGES "
								+ "where id=?");
				ps.setInt(1, ((LanguagePK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();
				DatabaseUtil.closeStatement(ps);
			} else {
				throw new EJBException();
			}
			int objectId = ((LanguagePK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
			log.logPropertyValue("code", this.code, objectId, user);
			log.logPropertyValue("description", this.description, objectId,
					user);

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, exec, con);
		}
	}

	public void unsetEntityContext() throws EJBException {
		ctx = null;
	}

	public void setEntityContext(EntityContext ctx) throws EJBException {
		this.ctx = ctx;
	}

	public String getCode() {
		// store = false;
		return code;
	}

	public void setCode(String code) throws EJBException, FinaTypeException {

		log.logPropertySet("code", code, this.code, ((LanguagePK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		if (code.equals(""))
			code = " ";

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from SYS_LANGUAGES where rtrim(code)=? and id != ?");
			ps.setString(1, code.trim());
			ps.setInt(2, ((LanguagePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Language", code });
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		this.code = code.trim();
	}

	public String getDescription() {
		// store = false;
		return description;
	}

	public void setDescription(String description) {
		// store = true;
		log.logPropertySet("description", description, this.description,
				((LanguagePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.description = description;
	}

	public String getFontFace() {
		// store = false;
		return fontFace;
	}

	public void setFontFace(String fontFace) {
		// store = true;
		log.logPropertySet("font face", fontFace, this.fontFace,
				((LanguagePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.fontFace = fontFace;
	}

	public int getFontSize() {
		// store = false;
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		// store = true;
		log.logPropertySet("font size", fontSize, this.fontSize,
				((LanguagePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.fontSize = fontSize;
	}

	public String getDateFormat() {
		// store = false;
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		// store = true;
		log.logPropertySet("date format", dateFormat, this.dateFormat,
				((LanguagePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.dateFormat = dateFormat;
	}

	public String getNumberFormat() {
		// store = false;
		return numberFormat;
	}

	public void setNumberFormat(String numberFormat) {
		// store = true;
		log.logPropertySet("number format", numberFormat, this.numberFormat,
				((LanguagePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.numberFormat = numberFormat;
	}

	public String getHtmlCharset() {
		// store = false;
		return htmlCharset;
	}

	public void setHtmlCharset(String htmlCharset) {
		// store = true;
		log.logPropertySet("HTML charset", htmlCharset, this.htmlCharset,
				((LanguagePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.htmlCharset = htmlCharset;
	}

	public String getXmlEncoding() {
		// store = false;
		return xmlEncoding;
	}

	public void setXmlEncoding(String xmlEncoding) {
		// store = true;
		log.logPropertySet("XML encoding", xmlEncoding, this.xmlEncoding,
				((LanguagePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.xmlEncoding = xmlEncoding;
	}
}
