package fina2.regions;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import fina2.FinaTypeException;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguageBean;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.security.ServerSecurityUtil;
import fina2.ui.tree.Node;
import fina2.util.LoggerHelper;

@SuppressWarnings("serial")
public class RegionStructureSessionBean implements SessionBean {
	@SuppressWarnings("unused")
	private SessionContext ctx;

	private LoggerHelper log = new LoggerHelper(RegionStructureNodeBean.class, "RegionStructure");

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

	public Node getTreeNodes(Handle userHandle, Handle languageHandle) throws EJBException, RemoteException, FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.regCity.amend");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Node root = new Node(new RegionStructureNodePK(0), "                ", new Integer(-1));
		try {
			ps = con.prepareStatement("SELECT a.id , a.parentid , a.code , a.sequence,s.value FROM IN_COUNTRY_DATA a left outer join SYS_STRINGS s on s.id=a.namestrid and s.langID=? order by a.parentid,a.sequence,a.id DESC");
			ps.setLong(1, langID);

			rs = ps.executeQuery();

			HashMap<RegionStructureNodePK, Node> nodes = new HashMap<RegionStructureNodePK, Node>();

			Node node = null;

			while (rs.next()) {
				RegionStructureNodePK pk = new RegionStructureNodePK(rs.getLong(1));
				if (node != null) {
					RegionStructureNodePK prevPK = (RegionStructureNodePK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}

				RegionStructureNodePK parent = new RegionStructureNodePK(rs.getLong(2));

				String code = rs.getString(3);

				String description = rs.getString(5);

				if (description != null) {
					description = LocaleUtil.encode(description, lang.getXmlEncoding());
				} else {
					description = "NONAME";
				}
				node = new Node(pk, "[" + code + "]" + description, new Integer(-1));
				node.setParentPK(parent);
				if (parent.getId() == 0) {
					root.addChild(node);
				} else {
					Node p = (Node) nodes.get(parent);
					if (p != null) {
						p.addChild(node);
					}
				}
				nodes.put(pk, node);
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return root;
	}

	/** Moves up the given node */
	public boolean moveUp(RegionStructureNodePK pk) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select parentID, sequence from IN_COUNTRY_DATA where id=? ");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();
			rs.next();

			int parentID = rs.getInt(1);
			int sequence = rs.getInt(2);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			// Minimal Sequence
			ps = con.prepareStatement("select MIN(sequence) from IN_COUNTRY_DATA where parentid=?");
			ps.setLong(1, parentID);
			rs = ps.executeQuery();
			rs.next();

			int minSequence = rs.getInt(1);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (sequence > minSequence) {
				ps = con.prepareStatement("select sequence from IN_COUNTRY_DATA where parentID=? ");
				ps.setInt(1, parentID);
				rs = ps.executeQuery();

				ArrayList<Integer> list = new ArrayList<Integer>();
				while (rs.next()) {
					list.add(rs.getInt(1));
				}
				Collections.sort(list);
				int moveSequence = list.get(list.indexOf(sequence) - 1);

				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);

				Long id = null;
				ps = con.prepareStatement("select id from IN_COUNTRY_DATA where sequence=? ");
				ps.setInt(1, moveSequence);
				rs = ps.executeQuery();
				if (rs.next()) {
					id = rs.getLong(1);
				}

				PreparedStatement update = null;
				update = con.prepareStatement("update IN_COUNTRY_DATA set sequence=? where id=?");
				update.setInt(1, moveSequence);
				update.setLong(2, pk.getId());
				update.executeUpdate();
				DatabaseUtil.closeStatement(update);

				update = con.prepareStatement("update IN_COUNTRY_DATA set sequence=? where id=?");
				update.setInt(1, sequence);
				update.setLong(2, id);
				update.executeUpdate();
				DatabaseUtil.closeStatement(update);
			} else {
				return false;
			}
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return true;
	}

	public boolean moveDown(RegionStructureNodePK pk) throws EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select parentID,sequence from IN_COUNTRY_DATA where id=?");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();
			rs.next();

			int parentID = rs.getInt(1);
			int sequence = rs.getInt(2);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select max(sequence) from IN_COUNTRY_DATA where parentID=?");

			ps.setInt(1, parentID);
			rs = ps.executeQuery();
			rs.next();
			int maxsequence = rs.getInt(1);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (sequence < maxsequence) {
				ps = con.prepareStatement("select sequence from IN_COUNTRY_DATA " + "where parentID=? ");
				ps.setInt(1, parentID);
				rs = ps.executeQuery();

				ArrayList<Integer> list = new ArrayList<Integer>();
				while (rs.next()) {
					list.add(rs.getInt(1));
				}
				Collections.sort(list);
				int moveSequence = list.get(list.indexOf(sequence) + 1);

				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);

				Long id = null;
				ps = con.prepareStatement("select id from IN_COUNTRY_DATA where sequence=? ");
				ps.setInt(1, moveSequence);
				rs = ps.executeQuery();
				if (rs.next()) {
					id = rs.getLong(1);
				}

				PreparedStatement update = null;

				update = con.prepareStatement("update IN_COUNTRY_DATA set sequence=? " + "where id=?");
				update.setInt(1, moveSequence);
				update.setLong(2, pk.getId());
				update.executeUpdate();
				DatabaseUtil.closeStatement(update);

				update = con.prepareStatement("update IN_COUNTRY_DATA set sequence=? " + "where id=?");
				update.setInt(1, sequence);
				update.setLong(2, id);
				update.executeUpdate();
				DatabaseUtil.closeStatement(update);
			} else {
				return false;
			}
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return true;
	}

	public String findParentNodes(RegionStructureNodePK pk, Handle languageHandle, StringBuffer buf) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		RegionStructureNodePK parentPk = null;
		try {
			ps = con.prepareStatement("SELECT id,parentid,code,namestrid FROM IN_COUNTRY_DATA WHERE id=? ");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				parentPk = new RegionStructureNodePK(rs.getLong(2));
				buf.append(rs.getString(3));
				buf.append(" : ");
				String description = LocaleUtil.getSysString(languageHandle, (int) rs.getLong(4));

				if (description == null) {
					description = "NONAME";
				}

				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);
				buf.append(description);
				buf.append("/");
				findParentNodes(parentPk, languageHandle, buf);
			} else {
				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);
			}
		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return buf.toString();
	}

	public String getNodePathLabel(RegionStructureNodePK pk, Handle languageHandle, StringBuffer buf) throws RemoteException, EJBException {
		String label = findParentNodes(pk, languageHandle, buf);
		String nodeLabels[] = label.split("/");
		StringBuilder sb = new StringBuilder();
		for (int i = nodeLabels.length - 1; i > 0; i--) {
			sb.append(nodeLabels[i]);
			sb.append(" | ");
		}
		sb = new StringBuilder(sb.toString().trim());
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public void setProperties(Map<Integer, String> map, Handle languageHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) languageHandle.getEJBObject().getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String levelName = null;

		ArrayList<Long> list = new ArrayList<Long>();
		try {
			String maxLevel = map.get(0);
			ps = con.prepareStatement("UPDATE SYS_PROPERTIES SET  VALUE=? WHERE RTRIM(PROP_KEY)=?");
			ps.setString(1, maxLevel);
			ps.setString(2, "fina2.regionstructuretree.maxlevel");
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			// get Old Strings
			ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE RTRIM(PROP_KEY)=?");
			ps.setString(1, "fina2.regionstructuretree.levelname");
			rs = ps.executeQuery();
			levelName = null;
			if (rs.next()) {
				levelName = rs.getString(1);
			}
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			// new Strings Buffer
			StringBuffer buf = new StringBuffer();

			if (levelName != null) {

				StringTokenizer tokenNames = new StringTokenizer(levelName, "#");
				while (tokenNames.hasMoreElements()) {
					String tokenName = (String) tokenNames.nextElement();

					StringTokenizer langToken = new StringTokenizer(tokenName, "=");
					String s = (String) langToken.nextElement();
					int lang = Integer.parseInt(s.charAt(s.length() - 1) + "");

					if (langPK.getId() == lang) {
						StringBuilder deletedStrings = new StringBuilder(langToken.nextElement().toString());
						deletedStrings.deleteCharAt(0);
						deletedStrings.deleteCharAt(deletedStrings.length() - 1);
						deletedStrings.deleteCharAt(deletedStrings.length() - 1);

						// Remove old Strings
						ps = con.prepareStatement("DELETE FROM SYS_STRINGS WHERE langID=? and ID in(" + deletedStrings.toString() + ")");
						ps.setLong(1, langPK.getId());
						ps.executeUpdate();
						DatabaseUtil.closeStatement(ps);

					} else {
						buf.append(tokenName);
						buf.append("#");
					}
				}
			}

			// set new Strings
			levelName = null;
			for (int i = 1; i < map.size(); i++) {
				levelName = map.get(i);
				if (levelName != null) {
					long nameStrId = LocaleUtil.allocateString(con);
					LocaleUtil.setString(con, langPK, (int) nameStrId, levelName);
					list.add(nameStrId);
				}
			}

			Map<Integer, ArrayList<Long>> tempMap = new HashMap<Integer, ArrayList<Long>>();
			tempMap.put(langPK.getId(), list);

			buf.append(tempMap.toString());

			ps = con.prepareStatement("UPDATE SYS_PROPERTIES SET  VALUE=? WHERE RTRIM(PROP_KEY)=?");
			ps.setString(1, buf.toString());
			ps.setString(2, "fina2.regionstructuretree.levelname");
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

		} catch (SQLException e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
	}

	public Map<Integer, String> getProperties(Handle languageHandle) throws RemoteException, EJBException {
		Language language = (Language) languageHandle.getEJBObject();
		LanguagePK langPK = ((LanguagePK) language.getPrimaryKey());

		Map<Integer, String> map = null;

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE RTRIM(PROP_KEY)=?");
			ps.setString(1, "fina2.regionstructuretree.maxlevel");
			rs = ps.executeQuery();
			String maxLevel = null;
			if (rs.next()) {
				maxLevel = rs.getString(1);
			}
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);

			map = new HashMap<Integer, String>();
			map.put(0, maxLevel);
			if (maxLevel != null) {
				ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE RTRIM(PROP_KEY)=?");
				ps.setString(1, "fina2.regionstructuretree.levelname");
				rs = ps.executeQuery();
				String levelName = null;
				if (rs.next()) {
					levelName = rs.getString(1);
					if (levelName != null)
						levelName = levelName.trim();
				}
				DatabaseUtil.closeStatement(ps);
				DatabaseUtil.closeResultSet(rs);

				String selectedStrings = null;
				if (levelName != null) {
					selectedStrings = createSelectedStrings(levelName, langPK.getId());
					if (selectedStrings != null) {
						map = getPropertiesLevelNames(con, language, langPK.getId(), selectedStrings, map);
					} else {
						ArrayList<Integer> list = new ArrayList<Integer>();
						StringTokenizer tokenNames = new StringTokenizer(levelName, "#");
						while (tokenNames.hasMoreElements()) {
							String tokenName = (String) tokenNames.nextElement();
							StringTokenizer langToken = new StringTokenizer(tokenName, "=");
							String s = (String) langToken.nextElement();
							int lang = Integer.parseInt(s.charAt(s.length() - 1) + "");
							list.add(lang);
						}
						if (list.size() > 0) {
							Collections.sort(list);
							int newLangId = list.get(0);
							selectedStrings = createSelectedStrings(levelName, newLangId);
							map = getPropertiesLevelNames(con, language, newLangId, selectedStrings, map);
						}
					}
				}
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return map;
	}

	private String createSelectedStrings(String src, int langId) {
		StringTokenizer tokenNames = new StringTokenizer(src, "#");
		while (tokenNames.hasMoreElements()) {
			String tokenName = (String) tokenNames.nextElement();
			StringTokenizer langToken = new StringTokenizer(tokenName, "=");
			String s = (String) langToken.nextElement();
			int lang = Integer.parseInt(s.substring(1));
			if (langId == lang) {
				StringBuilder getNameStrs = new StringBuilder(langToken.nextElement().toString());
				getNameStrs.deleteCharAt(0);
				getNameStrs.deleteCharAt(getNameStrs.length() - 1);
				getNameStrs.deleteCharAt(getNameStrs.length() - 1);
				return getNameStrs.toString();
			}
		}
		return null;
	}

	private Map<Integer, String> getPropertiesLevelNames(Connection con, Language language, int langId, String selectedStrings, Map<Integer, String> map) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT value FROM SYS_STRINGS WHERE ID in(" + selectedStrings + ") and langId=?");
			ps.setLong(1, langId);
			rs = ps.executeQuery();

			int key = 1;
			while (rs.next()) {
				String name = rs.getString(1);
				if (name != null)
					name = LocaleUtil.encode(name, language.getXmlEncoding());
				else {
					name = "NONAME";
				}
				map.put(key++, name);
			}
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return map;
	}
}
