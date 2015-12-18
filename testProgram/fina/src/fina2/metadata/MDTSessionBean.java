package fina2.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.metadata.jaxb.MDT;
import fina2.metadata.jaxb.MDTComparison;
import fina2.metadata.jaxb.MDTDependentNode;
import fina2.metadata.jaxb.MDTDescription;
import fina2.metadata.jaxb.MDTNodeComparison;
import fina2.metadata.jaxb.MDTNodeData;
import fina2.metadata.jaxb.MDTNodeDescription;
import fina2.security.ServerSecurityUtil;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.Node;

public class MDTSessionBean implements SessionBean {

	private SessionContext ctx;

	private List<MDTNodeData> nodesToExport = new ArrayList<MDTNodeData>();

	private Connection connection = DatabaseUtil.getConnection();
	private PreparedStatement prepStatement = null;
	private ResultSet nodeCompRes = null;
	private ResultSet rs1 = null;
	private ResultSet depNodeRes = null;
	private ResultSet nodeDescRes = null;
	private PreparedStatement pss = null;
	private boolean rootAdded = false;

	private static Logger log = Logger.getLogger(MDTSessionBean.class);

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

	/** Returns sequence value of given node */
	public int getNodeSequence(long nodeId) throws EJBException {
		int seq;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select sequence from IN_MDT_NODES where id=?";
			ps = con.prepareStatement(sql);
			ps.setLong(1, nodeId);

			rs = ps.executeQuery();
			rs.next();
			seq = rs.getInt(1);

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return seq;
	}

	public void setNodeSequence(long nodeId, int sequence) throws RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			String sql = "update IN_MDT_NODES set sequence = ? where id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, sequence);
			ps.setLong(2, nodeId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	/** Moves up the given node */
	public void moveUp(MDTNodePK pk) throws EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select parentID, sequence from IN_MDT_NODES " + "where id=? ");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();
			rs.next();

			int parentID = rs.getInt(1);
			int sequence = rs.getInt(2);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (sequence > 0) {
				ps = con.prepareStatement("select id, sequence from IN_MDT_NODES " + "where parentID=? and (sequence=? or sequence=?) " + "order by sequence DESC");
				ps.setInt(1, parentID);
				ps.setInt(2, sequence);
				ps.setInt(3, sequence - 1);

				rs = ps.executeQuery();
				PreparedStatement update = null;
				if (rs.next()) {
					update = con.prepareStatement("update IN_MDT_NODES set sequence=? " + "where id=?");
					update.setInt(1, sequence - 1);
					update.setLong(2, pk.getId());
					update.executeUpdate();
					DatabaseUtil.closeStatement(update);
				}
				if (rs.next()) {
					update = con.prepareStatement("update IN_MDT_NODES set sequence=? " + "where id=?");
					update.setInt(1, sequence);
					update.setInt(2, rs.getInt(1));
					update.executeUpdate();
					DatabaseUtil.closeStatement(update);
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void moveDown(MDTNodePK pk) throws EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select parentID,sequence from IN_MDT_NODES where id=?");
			ps.setLong(1, pk.getId());
			rs = ps.executeQuery();
			rs.next();

			int parentID = rs.getInt(1);
			int sequence = rs.getInt(2);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select max(sequence) from IN_MDT_NODES where parentID=?");

			ps.setInt(1, parentID);
			rs = ps.executeQuery();
			rs.next();
			int maxsequence = rs.getInt(1);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (sequence < maxsequence) {
				ps = con.prepareStatement("select id, sequence from IN_MDT_NODES " + "where parentID=? and (sequence=? or sequence=?) " + "order by sequence");
				ps.setInt(1, parentID);
				ps.setInt(2, sequence);
				ps.setInt(3, sequence + 1);

				rs = ps.executeQuery();
				PreparedStatement update = null;
				if (rs.next()) {
					update = con.prepareStatement("update IN_MDT_NODES set sequence=? " + "where id=?");
					update.setInt(1, sequence + 1);
					update.setLong(2, pk.getId());
					update.executeUpdate();
					DatabaseUtil.closeStatement(update);
				}
				if (rs.next()) {
					update = con.prepareStatement("update IN_MDT_NODES set sequence=? " + "where id=?");

					update.setInt(1, sequence);
					update.setInt(2, rs.getInt(1));
					update.executeUpdate();
					DatabaseUtil.closeStatement(update);
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	// public void copyPaste(Collection pks, MDTNodePK newParentPk, boolean cut)
	// throws EJBException {
	public void copyPaste(MDTNodePK pk, MDTNodePK newParentPk, boolean cut) throws EJBException {

		if (cut) {
			Connection con = DatabaseUtil.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			PreparedStatement update = null;
			try {

				ps = con.prepareStatement("select max(sequence) from IN_MDT_NODES where parentID=?");

				ps.setLong(1, newParentPk.getId());
				rs = ps.executeQuery();
				rs.next();
				long maxsequence = rs.getLong(1);
				update = con.prepareStatement("update IN_MDT_NODES set sequence=?, parentId=? " + "where id=?");
				update.setLong(1, maxsequence + 1);
				update.setLong(2, newParentPk.getId());
				update.setLong(3, pk.getId());
				update.executeUpdate();
			} catch (SQLException e) {
				throw new EJBException(e);
			} finally {
				DatabaseUtil.close(rs, ps, con);
				DatabaseUtil.closeStatement(update);
			}
		}
	}

	public Node getTreeNodes(Handle userHandle, Handle languageHandle) throws RemoteException, FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.metadata.amend", "fina2.metadata.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Node root = new Node(new MDTNodePK(0), "        ", new Integer(-1));
		try {
			ps = con.prepareStatement("select a.id, a.parentID, a.code, a.disabled, b.value, a.type, a.sequence, b.langID " + "from IN_MDT_NODES a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=? or b.langID=1) " + "order by a.parentID, a.sequence, a.id, b.langID DESC");
			ps.setInt(1, langID);

			rs = ps.executeQuery();

			Hashtable nodes = new Hashtable(14000);
			Node node = null;
			while (rs.next()) {
				MDTNodePK pk = new MDTNodePK(rs.getLong(1));
				if (node != null) {
					MDTNodePK prevPK = (MDTNodePK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}
				MDTNodePK parent = new MDTNodePK(rs.getLong(2));

				String code = rs.getString(3).trim();
				String s = "";
				if (rs.getInt(4) != 0)
					s = "[DISABLED]";

				String desc = LocaleUtil.encode(rs.getString(5).trim(), encoding);

				node = new Node(pk, s + "[" + code + "] " + desc, new Integer(rs.getInt(6)));
				node.putProperty("code", code);

				if (parent.getId() == 0) {
					root.addChild(node);
				} else {
					Node p = (Node) nodes.get(parent);
					if (p != null)
						p.addChild(node);
				}
				nodes.put(pk, node);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return root;
	}

	public HashMap<String, String> getNodeCodeDescriptions() throws RemoteException {
		HashMap<String, String> map = new HashMap<String, String>();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT m.CODE,s.VALUE FROM SYS_STRINGS s , IN_MDT_NODES m where s.ID=m.NAMESTRID");
			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString("CODE").trim(), rs.getString("VALUE"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return map;
	}

	public Collection getChildNodes(Handle userHandle, Handle languageHandle, MDTNodePK parentPK) throws RemoteException, FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.metadata.amend", "fina2.metadata.review");
		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {
			ps = con.prepareStatement("select a.id, a.parentID, a.code, a.disabled, b.value, a.type, a.sequence, b.langID " + "from IN_MDT_NODES a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=? or b.langID=1) and a.parentID=? " + "order by a.sequence, a.id, b.langID DESC");
			ps.setLong(1, langID);
			ps.setLong(2, parentPK.getId());

			rs = ps.executeQuery();

			Node root = new Node(new MDTNodePK(0), "        ", new Integer(-1));
			Node node = null;
			while (rs.next()) {
				MDTNodePK pk = new MDTNodePK(rs.getLong(1));
				if (node != null) {
					MDTNodePK prevPK = (MDTNodePK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}
				MDTNodePK parent = new MDTNodePK(rs.getLong(2));
				String code = rs.getString(3).trim();
				String s = "";
				if (rs.getInt(4) != 0)
					s = "[DISABLED]";

				String desc = rs.getString(5);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);

				node = new Node(pk, s + "[" + code + "] " + desc, new Integer(rs.getInt(6)));
				node.putProperty("code", code);

				/*
				 * if(parent.getId() == 0) { root.addChild(node); } else { Node
				 * p = (Node)nodes.get(parent); if(p != null) p.addChild(node);
				 * } nodes.put(pk, node);
				 */
				nodes.add(node);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	public Collection getChNodes(MDTNodePK parentPK) throws RemoteException {
		// log.debug("ecl1");
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {

			// Language lang = (Language)languageHandle.getEJBObject();
			// int langID = ((LanguagePK)lang.getPrimaryKey()).getId();
			// String encoding = lang.getXmlEncoding();

			ps = con.prepareStatement("select a.id, a.parentID, a.code, a.disabled, b.value, a.type, a.dataType " + "from IN_MDT_NODES a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=? or b.langID=1) and a.parentID=? " + "order by a.sequence, a.id, b.langID DESC");
			ps.setInt(1, 1);
			ps.setLong(2, parentPK.getId());
			rs = ps.executeQuery();

			Node root = new Node(new MDTNodePK(0), "        ", new Integer(-1));
			Node node = null;
			while (rs.next()) {
				log.debug("ecl");
				MDTNodePK pk = new MDTNodePK(rs.getLong(1));
				if (node != null) {
					MDTNodePK prevPK = (MDTNodePK) node.getPrimaryKey();
					if (pk.equals(prevPK))
						continue;
				}
				MDTNodePK parent = new MDTNodePK(rs.getLong(2));

				String code = rs.getString(3).trim();
				String s = "";
				if (rs.getInt(4) != 0)
					s = "[DISABLED]";

				String desc = rs.getString(5);
				if (desc == null)
					desc = "";
				else
					desc = desc.trim();
				node = new Node(pk, s + "[" + code + "] " + desc, new Integer(rs.getInt(6)));
				node.putProperty("code", code);
				node.putProperty("dataType", new Integer(rs.getInt(7)));

				/*
				 * if(parent.getId() == 0) { root.addChild(node); } else { Node
				 * p = (Node)nodes.get(parent); if(p != null) p.addChild(node);
				 * } nodes.put(pk, node);
				 */
				nodes.add(node);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	public Collection getParentNodes(MDTNodePK childPK) throws RemoteException, FinaTypeException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector nodes = new Vector();
		try {
			ps = con.prepareStatement("select parentID from IN_MDT_NODES where id=?");
			ps.setLong(1, childPK.getId());

			rs = ps.executeQuery();

			while (rs.next()) {
				MDTNodePK pk = new MDTNodePK(rs.getLong(1));
				log.debug("parent " + pk.getId());
				if (pk.getId() == 0)
					break;

				rs.close();
				nodes.add(pk);

				ps.setLong(1, pk.getId());
				rs = ps.executeQuery();
			}

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return nodes;
	}

	/*
	 * private Node getNode(ResultSet rs, Node parentNode) throws SQLException {
	 * MDTNodePK prevPK = new MDTNodePK(-1); Node node = null; while(rs.next()){
	 * MDTNodePK pk = new MDTNodePK(rs.getInt(1));
	 * 
	 * if(node != null) { MDTNodePK prevPK = (MDTNodePK)node.getPrimaryKey();
	 * if(pk.equals(prevPK)) continue; } MDTNodePK parent = new
	 * MDTNodePK(rs.getInt(2));
	 * 
	 * String code = rs.getString(3).trim(); node = new Node(pk,
	 * "["+code+"] "+rs.getString(4).trim(), new Integer(rs.getInt(5)));
	 * node.putProperty("code", code);
	 * 
	 * if(parent.getId() == 0) { root.addChild(node); } else { Node p =
	 * (Node)nodes.get(parent); if(p != null) p.addChild(node); } nodes.put(pk,
	 * node); } }
	 */

	public void setDependentNodes(MDTNodePK pk, Collection nodes) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		try {
			ps = con.prepareStatement("delete from IN_MDT_DEPENDENT_NODES " + "where dependentNodeID=?");
			ps.setLong(1, pk.getId());
			ps.executeUpdate();

			insert = con.prepareStatement("insert into IN_MDT_DEPENDENT_NODES (nodeID, dependentNodeID) values(?, ?)");
			for (Iterator iter = nodes.iterator(); iter.hasNext();) {
				MDTNodePK nodePK = (MDTNodePK) iter.next();
				insert.setLong(1, nodePK.getId());
				insert.setLong(2, pk.getId());
				insert.executeUpdate();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(insert);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public Collection getDependencies(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		if (pk == null)
			return v;
		try {

			Language lang = (Language) languageHandle.getEJBObject();
			int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
			String encoding = lang.getXmlEncoding();

			ps = null;

			ps = con.prepareStatement("select b.id, b.code, c.value " + "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b, SYS_STRINGS c " + "where a.nodeID=? and b.id=a.dependentNodeID and " + "c.id=b.nameStrID and c.langID=? " + "order by c.langID DESC");
			ps.setLong(1, pk.getId());
			ps.setInt(2, langID);

			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				MDTNodePK _pk = new MDTNodePK(rs.getLong(1));
				// ComparisonPK comPK = new ComparisonPK(rs.getInt(1),
				// rs.getInt(2));
				TableRowImpl row = new TableRowImpl(_pk, 3);
				row.setValue(0, "Used by ");
				row.setValue(1, rs.getString(2).trim());
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(2, desc);
				if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);
			}
			log.debug("Used By = " + v.size());

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select b.id, b.code, c.value " + "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b, SYS_STRINGS c " + "where a.dependentNodeID=? and b.id=a.nodeID and " + "c.id=b.nameStrID and c.langID=? " + "order by c.langID DESC");
			ps.setLong(1, pk.getId());
			ps.setInt(2, langID);

			rs = ps.executeQuery();

			prevRow = null;
			while (rs.next()) {
				MDTNodePK _pk = new MDTNodePK(rs.getLong(1));
				// ComparisonPK comPK = new ComparisonPK(rs.getInt(1),
				// rs.getInt(2));
				TableRowImpl row = new TableRowImpl(_pk, 3);
				row.setValue(0, "Depended on ");
				row.setValue(1, rs.getString(2).trim());
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(2, desc);

				if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);
			}
			log.debug("Depended on = " + v.size());
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;

	}

	public Collection getChildren(Handle languageHandle, MDTNodePK parentPK) throws RemoteException {
		Vector v = new Vector();
		if (parentPK == null)
			return v;
		try {
			v = (Vector) getRecursiveChildren(v, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId(), parentPK);
		} catch (Exception e) {
			throw new EJBException(e);
		}
		return v;
	}

	private Collection getRecursiveChildren(Vector v, int langId, MDTNodePK parentPK) {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			// Language lang = (Language)languageHandle.getEJBObject();
			// int langID = ((LanguagePK)lang.getPrimaryKey()).getId();
			// String encoding = lang.getXmlEncoding();

			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/i18n/Language");
			LanguageHome home = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
			Language lang = home.findByPrimaryKey(new LanguagePK(langId));
			String encoding = lang.getXmlEncoding();

			ps = con.prepareStatement("select a.id, a.code, b.value, a.equation, a.type " + "from IN_MDT_NODES a, SYS_STRINGS b " + "where b.id=a.nameStrID and (b.langID=? or b.langID=1) and a.parentID=? " + "order by a.sequence, a.id, b.langID DESC");
			ps.setInt(1, langId);
			ps.setLong(2, parentPK.getId());

			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getInt(5) == 1) {
					v = (Vector) getRecursiveChildren(v, langId, new MDTNodePK(rs.getLong(1)));
				} else if (rs.getInt(5) == 3) {
					TableRowImpl row = new TableRowImpl(new MDTNodePK(rs.getLong(1)), 3);
					row.setValue(0, rs.getString(2).trim());
					String desc = rs.getString(3);
					if (desc == null)
						desc = "";
					else
						desc = LocaleUtil.encode(desc.trim(), encoding);
					row.setValue(1, desc);
					String s = rs.getString(4);
					if (s == null)
						s = "";
					else
						s = s.trim();
					row.setValue(2, s);
					v.add(row);
				}
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection getDependsOn(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		Vector v = new Vector();
		if (pk == null) {
			return v;
		}
		try {
			Language lang = (Language) languageHandle.getEJBObject();
			int langId = ((LanguagePK) lang.getPrimaryKey()).getId();
			String encoding = lang.getXmlEncoding();
			v = (Vector) getRecursiveDependsOn(con, v, langId, encoding, pk);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		log.debug("getDependsOn=" + v.size());
		return v;
	}

	private Collection getRecursiveDependsOn(Connection con, Vector v, int langId, String encoding, MDTNodePK pk) throws RemoteException, EJBException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select b.id, b.code, c.value , b.equation " + "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b, SYS_STRINGS c " + "where a.dependentNodeID=? and b.id=a.nodeID and " + "c.id=b.nameStrID and (c.langID=? or c.langID=1) " + "order by c.langID DESC");
			ps.setLong(1, pk.getId());
			ps.setInt(2, langId);

			rs = ps.executeQuery();

			while (rs.next()) {
				MDTNodePK _pk = new MDTNodePK(rs.getLong(1));
				TableRowImpl row = new TableRowImpl(_pk, 3);
				row.setValue(0, rs.getString(2).trim());
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(1, desc);
				row.setValue(2, rs.getString(4).trim());

				v.add(row);

				v = (Vector) getRecursiveDependsOn(con, v, langId, encoding, _pk);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
		return v;
	}

	public Collection getUsedBy(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException {

		Vector v = new Vector();
		if (pk == null) {
			return v;
		}

		Connection con = DatabaseUtil.getConnection();
		try {
			Language lang = (Language) languageHandle.getEJBObject();
			int langId = ((LanguagePK) lang.getPrimaryKey()).getId();
			String encoding = lang.getXmlEncoding();
			v = (Vector) getRecursiveUsedBy(con, v, langId, encoding, pk);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return v;
	}

	private Collection getRecursiveUsedBy(Connection con, Vector v, int langId, String encoding, MDTNodePK pk) throws RemoteException, EJBException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select b.id, b.code, c.value , b.equation " + "from IN_MDT_DEPENDENT_NODES a, IN_MDT_NODES b, SYS_STRINGS c " + "where a.nodeID=? and b.id=a.dependentNodeID and " + "b.type=3 and c.id=b.nameStrID and (c.langID=? or c.langID=1) " + "order by c.langID DESC");
			ps.setLong(1, pk.getId());
			ps.setInt(2, langId);

			rs = ps.executeQuery();

			while (rs.next()) {
				MDTNodePK _pk = new MDTNodePK(rs.getLong(1));
				TableRowImpl row = new TableRowImpl(_pk, 3);
				row.setValue(0, rs.getString(2).trim());
				String desc = rs.getString(3);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(1, desc);
				String s = rs.getString(4);
				if (s == null)
					s = "";
				row.setValue(2, s.trim());

				v.add(row);

				v = (Vector) getRecursiveUsedBy(con, v, langId, encoding, _pk);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
		return v;
	}

	public Collection getDependendedReturnDefinition(Handle languageHandle, MDTNodePK pk) throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		if (pk == null)
			return v;
		try {

			Language lang = (Language) languageHandle.getEJBObject();
			int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
			String encoding = lang.getXmlEncoding();

			ps = con.prepareStatement("select b.code, c.value, a.code " + "from IN_DEFINITION_TABLES a, IN_RETURN_DEFINITIONS b, SYS_STRINGS c " + "where a.nodeID=? and b.id=a.definitionID and " + "c.id=b.nameStrID and (c.langID=?) " + "order by c.langID DESC");
			ps.setLong(1, pk.getId());
			ps.setInt(2, langID);

			rs = ps.executeQuery();

			TableRowImpl prevRow = null;

			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(pk, 3);
				row.setValue(0, rs.getString(1).trim());
				String desc = rs.getString(2);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(1, desc);
				String s = rs.getString(3);
				if (s == null)
					s = "";
				row.setValue(2, s.trim());

				if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);
			}

			Collection nodes = getParentNodes(pk);
			for (Iterator iter = nodes.iterator(); iter.hasNext();) {

				ps.setLong(1, ((MDTNodePK) iter.next()).getId());
				ps.setInt(2, langID);

				rs = ps.executeQuery();

				prevRow = null;

				while (rs.next()) {
					TableRowImpl row = new TableRowImpl(pk, 3);
					row.setValue(0, rs.getString(1).trim());
					String desc = rs.getString(2);
					if (desc == null)
						desc = "";
					else
						desc = LocaleUtil.encode(desc.trim(), encoding);
					row.setValue(1, desc);
					String s = rs.getString(3);
					if (s == null)
						s = "";
					row.setValue(2, s.trim());

					if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
						prevRow = row;
						continue;
					}
					prevRow = row;

					v.add(row);
				}
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection getComparisons(Handle userHandle, MDTNodePK pk) throws RemoteException, FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.metadata.amend", "fina2.metadata.review");

		Vector v = new Vector();
		if (pk == null)
			return v;

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (pk != null) {
				ps = con.prepareStatement("select a.id, a.nodeID, b.code, a.condition, a.equation " + "from IN_MDT_COMPARISON a, IN_MDT_NODES b " + "where a.nodeID=? and b.id=a.nodeID");
				ps.setLong(1, pk.getId());
			}
			if (pk.getId() == -1) {
				ps = con.prepareStatement("select a.id, a.nodeID, b.code, a.condition, a.equation " + "from IN_MDT_COMPARISON a, IN_MDT_NODES b " + "where b.id=a.nodeID order by b.code, a.id");
			}

			rs = ps.executeQuery();

			// TableRowImpl prevRow = null;
			while (rs.next()) {
				ComparisonPK comPK = new ComparisonPK(rs.getInt(1), rs.getInt(2));
				TableRowImpl row = new TableRowImpl(comPK, 3);
				row.setValue(0, rs.getString(3).trim());
				int cond = rs.getInt(4);
				switch (cond) {
				case 1:
					row.setValue(1, "=");
					break;
				case 2:
					row.setValue(1, "<>");
					break;
				case 3:
					row.setValue(1, ">");
					break;
				case 4:
					row.setValue(1, ">=");
					break;
				case 5:
					row.setValue(1, "<");
					break;
				case 6:
					row.setValue(1, "<=");
					break;
				default:
					row.setValue(1, "=");
				}

				// row.setValue(1, String.valueOf(rs.getInt(2)));
				row.setValue(2, rs.getString(5).trim());

				v.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;

	}

	public void setComparisons(MDTNodePK pk, Collection rows) throws FinaTypeException, RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement del = null;
		PreparedStatement ps = null;
		try {
			del = con.prepareStatement("delete from IN_MDT_COMPARISON where nodeid=?");
			del.setLong(1, pk.getId());
			del.executeUpdate();

			// Temporary solution of incompability issue.
			if (DatabaseUtil.isMysql()) {
				ps = con.prepareStatement("insert into IN_MDT_COMPARISON (id, nodeID, `condition`, equation) values (?,?,?,?)");
			} else {
				ps = con.prepareStatement("insert into IN_MDT_COMPARISON (id, nodeID, condition, equation) values (?,?,?,?)");
			}

			int i = 1;
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext(); i++) {

				TableRowImpl row = (TableRowImpl) iter.next();

				ps.setInt(1, i);
				ps.setLong(2, pk.getId());
				int cond = 1;
				if (String.valueOf(row.getValue(1)).trim().equals("="))
					cond = 1;
				// pstmt.setInt(3,1);
				if (String.valueOf(row.getValue(1)).trim().equals("<>"))
					cond = 2;
				// pstmt.setInt(3,2);
				if (String.valueOf(row.getValue(1)).trim().equals(">"))
					cond = 3;
				// pstmt.setInt(3,3);
				if (String.valueOf(row.getValue(1)).trim().equals(">="))
					cond = 4;
				// pstmt.setInt(3,4);
				if (String.valueOf(row.getValue(1)).trim().equals("<"))
					cond = 5;
				// pstmt.setInt(3,5);
				if (String.valueOf(row.getValue(1)).trim().equals("<="))
					cond = 6;
				// pstmt.setInt(3,6);

				ps.setInt(3, cond);
				ps.setString(4, String.valueOf(row.getValue(2)));
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} catch (Exception e) {
			throw new FinaTypeException(Type.METADATA_COMPARISON_NOT_UNIQUE);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void setComparison(TableRow row) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ComparisonPK pk = (ComparisonPK) row.getPrimaryKey();
			// Temporary solution of incompability issue.
			if (DatabaseUtil.isMysql()) {
				ps = con.prepareStatement("update IN_MDT_COMPARISON set `condition`=?, equation=? where id=? and nodeID=?");
			} else {
				ps = con.prepareStatement("update IN_MDT_COMPARISON set condition=?, equation=? where id=? and nodeID=?");
			}

			int cond = 1;
			if (String.valueOf(row.getValue(1)).trim().equals("=")) {
				cond = 1;
			}
			if (String.valueOf(row.getValue(1)).trim().equals("<>")) {
				cond = 2;
			}
			if (String.valueOf(row.getValue(1)).trim().equals(">")) {
				cond = 3;
			}
			if (String.valueOf(row.getValue(1)).trim().equals(">=")) {
				cond = 4;
			}
			if (String.valueOf(row.getValue(1)).trim().equals("<")) {
				cond = 5;
			}
			if (String.valueOf(row.getValue(1)).trim().equals("<=")) {
				cond = 6;
			}

			ps.setInt(1, cond);
			ps.setString(2, String.valueOf(row.getValue(2)).trim() + " ");
			ps.setInt(3, pk.getId());
			ps.setInt(4, pk.getNodeID());

			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void removeComparison(TableRow row) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ComparisonPK pk = (ComparisonPK) row.getPrimaryKey();
			ps = con.prepareStatement("delete from IN_MDT_COMPARISON where id=? and nodeID=?");
			ps.setInt(1, pk.getId());
			ps.setInt(2, pk.getNodeID());

			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public MDTNodeData getSelectedNodeData(MDTNodePK pk) throws RemoteException {

		Connection con = null;
		PreparedStatement ps = null;
		MDTNodeData mnd = new MDTNodeData();
		try {
			if (con == null)
				con = DatabaseUtil.getConnection();

			ps = con.prepareStatement("SELECT * FROM IN_MDT_NODES WHERE id=?");
			ps.setLong(1, pk.getId());
			ResultSet nodeRes = ps.executeQuery();
			if (nodeRes.next()) {

				mnd.setID(nodeRes.getLong("id"));
				mnd.setCODE(nodeRes.getString("code"));
				mnd.setPARENTID(nodeRes.getLong("parentid"));
				mnd.setTYPE(nodeRes.getLong("type"));
				mnd.setDATATYPE(nodeRes.getLong("datatype"));
				mnd.setEQUATION(nodeRes.getString("equation"));
				mnd.setSEQUENCE(nodeRes.getLong("sequence"));
				mnd.setEVALMETHOD(nodeRes.getLong("evalmethod"));
				mnd.setDISABLED(nodeRes.getLong("disabled"));
				mnd.setREQUIRED(nodeRes.getLong("required"));

				MDTComparison mdtComp = new MDTComparison();
				List<MDTNodeComparison> nodeComparisons = new ArrayList<MDTNodeComparison>();

				ps = con.prepareStatement("SELECT * FROM IN_MDT_COMPARISON WHERE nodeid=?");
				ps.setLong(1, pk.getId());
				ResultSet nodeCompRes = ps.executeQuery();
				while (nodeCompRes.next()) {
					MDTNodeComparison mdtNodeComp = new MDTNodeComparison();
					mdtNodeComp.setID(nodeCompRes.getLong("id"));
					mdtNodeComp.setNODEID(pk.getId());
					mdtNodeComp.setCONDITION(nodeCompRes.getLong("condition"));
					mdtNodeComp.setEQUATION(nodeCompRes.getString("equation"));
					nodeComparisons.add(mdtNodeComp);
				}
				mdtComp.setCOMPARISON(nodeComparisons);
				mnd.setCOMPARISONS(mdtComp);

				MDTDependentNode depNodes = new MDTDependentNode();
				List<Long> ids = new ArrayList<Long>();

				ps = con.prepareStatement("SELECT * FROM IN_MDT_DEPENDENT_NODES WHERE DEPENDENTNODEID=?");
				ps.setLong(1, pk.getId());
				ResultSet depNodeRes = ps.executeQuery();
				while (depNodeRes.next()) {
					ids.add(depNodeRes.getLong("nodeid"));
				}
				depNodes.setID(ids);
				mnd.setDEPENDENT_NODES(depNodes);

				MDTDescription mdtDesc = new MDTDescription();
				List<MDTNodeDescription> descList = new ArrayList<MDTNodeDescription>();

				ps = con.prepareStatement("SELECT id,langid,value FROM SYS_STRINGS WHERE id IN(SELECT namestrid FROM IN_MDT_NODES WHERE id=?)");
				ps.setLong(1, pk.getId());
				ResultSet nodeDescRes = ps.executeQuery();
				while (nodeDescRes.next()) {
					long langId = nodeDescRes.getLong("langid");
					MDTNodeDescription desc = new MDTNodeDescription();

					desc.setVALUE(LocaleUtil.getString(con, (int) langId, nodeDescRes.getLong("id")));
					PreparedStatement pss = con.prepareStatement("SELECT code FROM SYS_LANGUAGES WHERE id=?");
					pss.setLong(1, langId);
					ResultSet rs = pss.executeQuery();
					if (rs.next())
						desc.setLANG_CODE(rs.getString("code"));
					pss.close();
					// desc.setVALUE(nodeDescRes.getString("value"));

					descList.add(desc);
				}
				mdtDesc.setDESCRIPTION(descList);
				mnd.setDESCRIPTIONS(mdtDesc);

			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
		return mnd;
	}

	public List<MDTNodeData> getAllSubTreeWithParent(MDTNodePK pk) throws RemoteException {
		// connection = DatabaseUtil.getConnection();
		if (!rootAdded) {
			MDTNodeData mm = getSelectedNodeData(pk);
			nodesToExport.add(mm);
			rootAdded = true;
		}

		try {

			prepStatement = connection.prepareStatement("SELECT * FROM IN_MDT_NODES WHERE parentid=?");
			prepStatement.setLong(1, pk.getId());
			ResultSet nodeRes = prepStatement.executeQuery();
			while (nodeRes.next()) {
				MDTNodeData mnd = new MDTNodeData();
				mnd.setID(nodeRes.getLong("id"));
				mnd.setCODE(nodeRes.getString("code"));
				mnd.setPARENTID(nodeRes.getLong("parentid"));
				mnd.setTYPE(nodeRes.getLong("type"));
				mnd.setDATATYPE(nodeRes.getLong("datatype"));
				mnd.setEQUATION(nodeRes.getString("equation"));
				mnd.setSEQUENCE(nodeRes.getLong("sequence"));
				mnd.setEVALMETHOD(nodeRes.getLong("evalmethod"));
				mnd.setDISABLED(nodeRes.getLong("disabled"));
				mnd.setREQUIRED(nodeRes.getLong("required"));

				MDTComparison mdtComp = new MDTComparison();
				List<MDTNodeComparison> nodeComparisons = new ArrayList<MDTNodeComparison>();

				Connection c = DatabaseUtil.getConnection();
				prepStatement = c.prepareStatement("SELECT * FROM IN_MDT_COMPARISON WHERE nodeid=?");
				prepStatement.setLong(1, mnd.getID());

				nodeCompRes = prepStatement.executeQuery();
				while (nodeCompRes.next()) {
					MDTNodeComparison mdtNodeComp = new MDTNodeComparison();
					mdtNodeComp.setID(nodeCompRes.getLong("id"));
					mdtNodeComp.setNODEID(mnd.getID());
					mdtNodeComp.setCONDITION(nodeCompRes.getLong("condition"));
					mdtNodeComp.setEQUATION(nodeCompRes.getString("equation"));
					nodeComparisons.add(mdtNodeComp);
				}
				DatabaseUtil.close(nodeCompRes, prepStatement, c);

				mdtComp.setCOMPARISON(nodeComparisons);
				mnd.setCOMPARISONS(mdtComp);

				MDTDependentNode depNodes = new MDTDependentNode();
				List<Long> ids = new ArrayList<Long>();

				Connection cc = DatabaseUtil.getConnection();
				prepStatement = cc.prepareStatement("SELECT * FROM IN_MDT_DEPENDENT_NODES WHERE DEPENDENTNODEID=?");
				prepStatement.setLong(1, mnd.getID());

				depNodeRes = prepStatement.executeQuery();
				while (depNodeRes.next()) {
					ids.add(depNodeRes.getLong("nodeid"));
				}
				DatabaseUtil.close(depNodeRes, prepStatement, cc);
				// DatabaseUtil.closeResultSet(depNodeRes);

				depNodes.setID(ids);
				mnd.setDEPENDENT_NODES(depNodes);

				MDTDescription mdtDesc = new MDTDescription();
				List<MDTNodeDescription> descList = new ArrayList<MDTNodeDescription>();

				prepStatement = connection.prepareStatement("SELECT langid,id,value FROM SYS_STRINGS WHERE id IN(SELECT namestrid FROM IN_MDT_NODES WHERE id=?)");
				prepStatement.setLong(1, mnd.getID());
				nodeDescRes = prepStatement.executeQuery();
				while (nodeDescRes.next()) {
					MDTNodeDescription desc = new MDTNodeDescription();
					Connection con = DatabaseUtil.getConnection();
					PreparedStatement pss = con.prepareStatement("SELECT code FROM SYS_LANGUAGES WHERE id=?");
					long langId = nodeDescRes.getLong("langid");
					pss.setLong(1, langId);

					ResultSet rs = pss.executeQuery();
					if (rs.next())
						desc.setLANG_CODE(rs.getString("code"));
					desc.setVALUE(LocaleUtil.getString(con, (int) langId, nodeDescRes.getLong("id")));
					DatabaseUtil.close(rs, pss, con);

					rs = null;
					pss = null;

					// desc.setVALUE(nodeDescRes.getString("value"));
					
					descList.add(desc);
				}

				DatabaseUtil.closeResultSet(nodeDescRes);
				DatabaseUtil.closeStatement(prepStatement);

				mdtDesc.setDESCRIPTION(descList);
				mnd.setDESCRIPTIONS(mdtDesc);
				if (!containsNode(nodesToExport, mnd.getCODE()))
					nodesToExport.add(mnd);
				DatabaseUtil.closeStatement(prepStatement);
				getAllSubTreeWithParent(new MDTNodePK(mnd.getID()));
			}
			// DatabaseUtil.closeStatement(prepStatement);
			// DatabaseUtil.closeResultSet(nodeRes);

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			// DatabaseUtil.closeConnection(connection);
			DatabaseUtil.closeStatement(prepStatement);
		}
		return nodesToExport;
	}

	public String exportMDT(List<MDTNodeData> data, Handle languageHandle) throws RemoteException {
		String cont = "";
		try {
			String encoding = ((Language) languageHandle.getEJBObject()).getXmlEncoding();
			log.info("Exporting MDT ");
			JAXBContext jc = JAXBContext.newInstance("fina2.metadata.jaxb");
			Marshaller m = jc.createMarshaller();

			MDT mdt = new MDT();
			List<MDTNodeData> nodeList = new ArrayList<MDTNodeData>();
			for (int i = 0; i < data.size(); i++) {
				MDTNodeData node = data.get(i);
				nodeList.add(node);
			}
			mdt.setNODE(nodeList);
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.setProperty(Marshaller.JAXB_ENCODING, encoding);

			// File f = new File("temp.xml");
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			// OutputStreamWriter outputStreamWriter=new
			// OutputStreamWriter(arrayOutputStream,"UTF8");
			m.marshal(mdt, arrayOutputStream);

			// BufferedReader br = new BufferedReader(new InputStreamReader(new
			// FileInputStream(f),"UTF8"));
			// // InputStreamReader inputStreamReader=new InputStreamReader(new
			// FileInputStream(f));
			// //
			// String s = "";
			// while (((s = br.readLine()) != null) && (s.length() != 0)) {
			// cont += s + "\n";
			// }
			// br.close();
			// if (f.exists())
			// f.delete();
			//
			//

			cont = new String(arrayOutputStream.toByteArray(), encoding);

			nodesToExport.clear();
			rootAdded = false;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return cont;
	}
	
	private void updateCodesInEquations_forImportMDT(List<MDTNodeData> allNodes, String oldCode, String newCode) {
		for (int k = 0; k < allNodes.size(); k++) {
			String equation = allNodes.get(k).getEQUATION();
			String newEquation = equation.replace('"' + oldCode + '"', '"' + newCode + '"');
			newEquation = newEquation.replace("'" + oldCode + "'", "'" + newCode + "'");
			newEquation = newEquation.replace('(' + oldCode + ')', '(' + newCode + ')');
			allNodes.get(k).setEQUATION(newEquation);
		}
	}

	private void updateCodesInComparisons_forImportMDT(List<MDTNodeData> allNodes, String oldCode, String newCode) {
		for (int m = 0; m < allNodes.size(); m++) {
			MDTComparison mdtComp = allNodes.get(m).getCOMPARISONS();
			List<MDTNodeComparison> mdtNodeComp = mdtComp.getCOMPARISON();
			for (int j = 0; j < mdtNodeComp.size(); j++) {
				String eq = mdtNodeComp.get(j).getEQUATION();
				String newEq = eq.replace('"' + oldCode + '"', '"' + newCode + '"');
				newEq = newEq.replace("'" + oldCode + "'", "'" + newCode + "'");
				newEq = newEq.replace('(' + oldCode + ')', '(' + newCode + ')');
				mdtNodeComp.get(j).setEQUATION(newEq);
			}
			mdtComp.setCOMPARISON(mdtNodeComp);
			allNodes.get(m).setCOMPARISONS(mdtComp);
		}
	}
	
	public void importMDT(String content, MDTNodePK parentPk,String encoding) {
		log.info("Importing MDT ");
		PreparedStatement ps = null;
		try {
			//Initializing XML Reader
			JAXBContext jc = JAXBContext.newInstance("fina2.metadata.jaxb");
			Unmarshaller unm = jc.createUnmarshaller();

			//Reading XML
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(content.getBytes(encoding));
			MDT rootElement = (MDT) unm.unmarshal(arrayInputStream);

			//Getting list of nodes
			List<MDTNodeData> allNodes = rootElement.getNODE();
			
			//Getting list of node ids
			List<Long> ids = new ArrayList<Long>();
			for (int i = 0; i < allNodes.size(); i++) {
				ids.add(allNodes.get(i).getID());
			}
			
			//Sorting list of node ids
			Collections.sort(ids);

			//Initializing variables
			long maxNodeId = getMaxNodeId() + 1;
			long maxSysStringId;
			long maxComparison = getMaxCompId() + 1;
			long parentId = parentPk.getId();
			long incrementVal = Math.abs(maxNodeId - ids.get(0));
			
			//Updating imported nodes ids and codes
			for (int i = 0; i < allNodes.size(); i++) {
				//updating id
				allNodes.get(i).setID(allNodes.get(i).getID() + incrementVal);
				
				if (i == 0) {
					//if the node is root of the nodes being imported
					//updating parent id
					allNodes.get(i).setPARENTID(parentId);
					//updating sequence number
					allNodes.get(0).setSEQUENCE(getMaxSequence(parentId) + 1);
				} else {
					//if node is not root of the nodes being imported
					allNodes.get(i).setPARENTID(allNodes.get(i).getPARENTID() + incrementVal);
				}
				
				//if node with same code already exists
				if (nodeExists(allNodes.get(i).getCODE())) {
					//updating code
					String oldCode = allNodes.get(i).getCODE().trim();
					String newCode = oldCode + "_" + allNodes.get(i).getID();
					allNodes.get(i).setCODE(newCode);
					
					//updating code occurences in other nodes equations
					updateCodesInEquations_forImportMDT(allNodes, oldCode, newCode);
					
					//updating code occurences in other nodes comparisons
					updateCodesInComparisons_forImportMDT(allNodes, oldCode, newCode);
				}

				//updating nodes comparisons
				MDTComparison mdtComp = allNodes.get(i).getCOMPARISONS();
				List<MDTNodeComparison> mdtNodeComp = mdtComp.getCOMPARISON();
				for (int j = 0; j < mdtNodeComp.size(); j++) {
					mdtNodeComp.get(j).setID(maxComparison + mdtNodeComp.get(j).getID());
					mdtNodeComp.get(j).setNODEID(allNodes.get(i).getID());
				}
				mdtComp.setCOMPARISON(mdtNodeComp);
				allNodes.get(i).setCOMPARISONS(mdtComp);
				
				//updating node ids that current node depends on
				MDTDependentNode mdtDepNode = allNodes.get(i).getDEPENDENT_NODES();
				List<Long> mdtDeps = mdtDepNode.getID();
				for (int j = 0; j < mdtDeps.size(); j++) {
					mdtDeps.set(j, mdtDeps.get(j) + incrementVal);
				}
				mdtDepNode.setID(mdtDeps);
				allNodes.get(i).setDEPENDENT_NODES(mdtDepNode);
			}

			boolean isOracle = DatabaseUtil.isOracle();
			for (int i = 0; i < allNodes.size(); i++) {
				MDTDescription mdtDescs = allNodes.get(i).getDESCRIPTIONS();
				List<MDTNodeDescription> mdtNodeDesc = mdtDescs.getDESCRIPTION();
				maxSysStringId = getMaxSysStringId() + 1;

				ps = connection.prepareStatement("INSERT INTO SYS_STRINGS(id,langid,value) VALUES(?,?,?)");
				for (int j = 0; j < mdtNodeDesc.size(); j++) {
					ps.clearParameters();
					ps.setLong(1, maxSysStringId);
					ps.setLong(2, getLangId(connection, mdtNodeDesc.get(j).getLANG_CODE()));
					String description = mdtNodeDesc.get(j).getVALUE();
					if (!isOracle) {
						ps.setString(3, new String(description.getBytes(encoding), 0));
					} else {
						ps.setString(3, description);
					}
					ps.execute();
				}
				DatabaseUtil.closeStatement(ps);

				ps = connection.prepareStatement("INSERT INTO IN_MDT_NODES(id,code,namestrid,parentid,type,datatype,equation,sequence,evalmethod,disabled,required) VALUES(?,?,?,?,?,?,?,?,?,?,?)");
				ps.setLong(1, allNodes.get(i).getID());
				ps.setString(2, allNodes.get(i).getCODE());
				ps.setLong(3, maxSysStringId);
				ps.setLong(4, allNodes.get(i).getPARENTID());
				ps.setLong(5, allNodes.get(i).getTYPE());
				ps.setLong(6, allNodes.get(i).getDATATYPE());
				ps.setString(7, allNodes.get(i).getEQUATION());
				ps.setLong(8, allNodes.get(i).getSEQUENCE());
				ps.setLong(9, allNodes.get(i).getEVALMETHOD());
				ps.setLong(10, allNodes.get(i).getDISABLED());
				ps.setLong(11, allNodes.get(i).getREQUIRED());
				ps.execute();
				DatabaseUtil.closeStatement(ps);

				MDTDependentNode mdtDepNode = allNodes.get(i).getDEPENDENT_NODES();
				List<Long> mdtDepNodeIds = mdtDepNode.getID();

				ps = connection.prepareStatement("INSERT INTO IN_MDT_DEPENDENT_NODES(nodeid,dependentnodeid) VALUES(?,?)");
				for (int j = 0; j < mdtDepNodeIds.size(); j++) {
					ps.clearParameters();
					ps.setLong(1, mdtDepNodeIds.get(j));
					ps.setLong(2, allNodes.get(i).getID());
					ps.execute();
				}
				DatabaseUtil.closeStatement(ps);

				MDTComparison mdtComp = allNodes.get(i).getCOMPARISONS();
				List<MDTNodeComparison> mdtNodeComp = mdtComp.getCOMPARISON();

				ps = connection.prepareStatement("INSERT INTO IN_MDT_COMPARISON(id,nodeid,condition,equation) VALUES(?,?,?,?)");
				for (int j = 0; j < mdtNodeComp.size(); j++) {
					if (!comparisonExists(mdtNodeComp.get(j).getNODEID(), mdtNodeComp.get(j).getEQUATION())) {
						ps.clearParameters();
						ps.setLong(1, mdtNodeComp.get(j).getID());
						ps.setLong(2, mdtNodeComp.get(j).getNODEID());
						ps.setLong(3, mdtNodeComp.get(j).getCONDITION());
						ps.setString(4, mdtNodeComp.get(j).getEQUATION());
						ps.execute();
					}
				}
				DatabaseUtil.closeStatement(ps);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeStatement(ps);
			log.info("MDT Imported");
		}
	}

	public long getMaxSequence(long id) {
		long maxSeq = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT MAX(sequence) AS MAX_SEQ FROM IN_MDT_NODES WHERE parentid=?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				maxSeq = rs.getLong("MAX_SEQ");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
		return maxSeq;
	}

	public long getMaxNodeId() {
		long maxId = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT MAX(ID) AS MAX_NODE_ID FROM IN_MDT_NODES");
			rs = ps.executeQuery();
			if (rs.next()) {
				maxId = rs.getLong("MAX_NODE_ID");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
		return maxId;
	}

	public long getMaxSysStringId() {
		long maxId = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT MAX(ID) AS MAX_STRING_ID FROM SYS_STRINGS");
			rs = ps.executeQuery();
			if (rs.next()) {
				maxId = rs.getLong("MAX_STRING_ID");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
		return maxId;
	}

	public long getMaxDepNodeId() {
		long maxId = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT MAX(NODEID) AS MAX_DEPNODE_ID FROM IN_MDT_DEPENDENT_NODES");
			rs = ps.executeQuery();
			if (rs.next()) {
				maxId = rs.getLong("MAX_DEPNODE_ID");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
		return maxId;
	}

	public long getMaxCompId() {
		long maxId = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT MAX(ID) AS MAX_ID FROM IN_MDT_COMPARISON");
			rs = ps.executeQuery();
			if (rs.next()) {
				maxId = rs.getLong("MAX_ID");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
		return maxId;
	}

	public long getLangId(Connection con, String code) {
		long langId = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT id FROM SYS_LANGUAGES WHERE RTRIM(code)=?");
			ps.setString(1, code.trim());
			rs = ps.executeQuery();
			if (rs.next())
				langId = rs.getLong("id");
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

		}
		return langId;
	}

	public boolean containsNode(List<MDTNodeData> nodes, String code) {

		boolean exists = false;
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).getCODE().trim().equals(code.trim())) {
				exists = true;
				break;
			}
		}
		return exists;
	}

	public boolean nodeExists(String code) {
		boolean exists = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT code FROM IN_MDT_NODES WHERE UPPER(RTRIM(code))=?");
			ps.setString(1, code.trim().toUpperCase());
			rs = ps.executeQuery();
			if (rs.next()) {
				exists = true;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
		return exists;
	}

	public boolean comparisonExists(long nodeId, String equation) {
		boolean exists = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT id FROM IN_MDT_COMPARISON WHERE nodeid=? AND UPPER(RTRIM(equation))=?");
			ps.setLong(1, nodeId);
			ps.setString(2, equation.trim().toUpperCase());
			rs = ps.executeQuery();
			if (rs.next())
				exists = true;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeConnection(con);
		}
		return exists;
	}

}
