package fina2.script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import fina2.metadata.MDTConstants;
import fina2.returns.ProcessItem;
import fina2.returns.ReturnConstants;
import fina2.returns.ReturnPK;

public class JSTree {

	private Hashtable values;
	private Hashtable updates;
	private Hashtable dependents;
	private Connection con;
	private ReturnPK returnPK;

	private ProcessItem curItem;
	private int versionId;

	private Logger log = Logger.getLogger(JSTree.class);

	public JSTree(Hashtable values, Connection con, ReturnPK returnPK, int versionId) {
		this.values = values;
		this.con = con;
		this.returnPK = returnPK;
		this.versionId = versionId;
	}

	public JSTree(Hashtable values, Hashtable updates, Hashtable dependents, Connection con, ReturnPK returnPK, ProcessItem curItem, int versionId) {
		this.values = values;
		this.updates = updates;
		this.dependents = dependents;
		this.con = con;
		this.returnPK = returnPK;
		this.curItem = curItem;
		this.versionId = versionId;
	}

	public double lookup(String code) {
		try {
			Hashtable dep = null;
			if (dependents.get(code) == null) {
				dep = new Hashtable();
				dependents.put(code, dep);
			} else {
				dep = (Hashtable) dependents.get(code);
			}
			Hashtable depItems = null;
			if (dep.get(curItem.code) == null) {
				depItems = new Hashtable();
				dep.put(curItem.code, depItems);
			} else {
				depItems = (Hashtable) dep.get(curItem.code);
			}
			dep.put(curItem.code, depItems);
			depItems.put(new Integer(curItem.rowNumber), curItem);

			Hashtable h = (Hashtable) values.get(code);
			Hashtable uh = (Hashtable) updates.get(code);

			if ((h == null) && (uh == null)) {
				return 0;
			}

			if (uh != null) {
				h = uh;
			}

			Iterator iter = h.keySet().iterator();
			if (!iter.hasNext()) {
				return 0;
			}
			ProcessItem item = (ProcessItem) h.get(iter.next());

			String value = item.value;
			if (item.tableType == ReturnConstants.TABLETYPE_VARIABLE) {
				if ((curItem.returnID == item.returnID) && (curItem.tableID == item.tableID)) {
					item = (ProcessItem) h.get(new Integer(curItem.rowNumber));
					if (item == null) {
						return 0;
					}
					value = item.value;
				} else {
					double res = 0;

					switch (item.tableEvalType) {
					case ReturnConstants.EVAL_SUM:
						res = evalSum(h.values());
						break;
					case ReturnConstants.EVAL_AVERAGE:
						res = evalAverage(h.values());
						break;
					case ReturnConstants.EVAL_MIN:
						res = evalMin(h.values());
						break;
					case ReturnConstants.EVAL_MAX:
						res = evalMax(h.values());
						break;
					}
					value = Double.toString(res);
				}
			}
			try {
				if (value == null) {
					log.warn("value is null for node [" + code + "]");
					return 0;
				} else {

					Double d = Double.parseDouble(value);

					if (Double.isNaN(d) || Double.isInfinite(d)) {
						return 0;
					} else {
						return d;
					}

				}
			} catch (NullPointerException exception) {
				log.error(exception.getMessage(), exception);
				log.info("Item value=" + item.value + " item code = " + item.code + " equation= " + item.equation);
				return 0;
			} catch (NumberFormatException e) {
				return 0;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return 0;
		}
	}

	public Double lookup(String code, int rowNumber) {
		try {

			Hashtable dep = null;
			if (dependents.get(code) == null) {
				dep = new Hashtable();
				dependents.put(code, dep);
			} else {
				dep = (Hashtable) dependents.get(code);
			}
			Hashtable depItems = null;
			if (dep.get(curItem.code) == null) {
				depItems = new Hashtable();
				dep.put(curItem.code, depItems);
			} else {
				depItems = (Hashtable) dep.get(curItem.code);
			}
			dep.put(curItem.code, depItems);
			depItems.put(new Integer(curItem.rowNumber), curItem);

			Hashtable h = (Hashtable) values.get(code);
			Hashtable uh = (Hashtable) updates.get(code);

			if ((h == null) && (uh == null)) {
				return null;
			}

			if (uh != null) {
				h = uh;
			}

			Iterator iter = h.keySet().iterator();
			if (!iter.hasNext()) {
				return 0D;
			}
			ProcessItem item = (ProcessItem) h.get(new Integer(rowNumber));
			if (item == null) {
				return null;
			}

			String value = item.value;

			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				log.error("CODE=" + code + "ROWNUMBER=" + rowNumber);
				log.error("#" + value + "#" + e.getMessage(), e);

				return 0D;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return 0d;
		}
	}

	public String lookupString(String code) {
		return (String) values.get(code);
	}

	public String lookupString(String code, int rowNumber) {
		String s = null;
		try {
			Hashtable h = (Hashtable) values.get(code);
			ProcessItem item = (ProcessItem) h.get(new Integer(rowNumber));
			if (item == null) {
				log.info("ITEM IS NULL");
				s = null;
			} else
				s = item.value;
			if ((s != null) && (s.trim().length() == 0))
				s = null;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return s;
	}

	public double notrow(String code) {
		try {

			Hashtable dep = null;
			if (dependents.get(code) == null) {
				dep = new Hashtable();
				dependents.put(code, dep);
			} else {
				dep = (Hashtable) dependents.get(code);
			}
			Hashtable depItems = null;
			if (dep.get(curItem.code) == null) {
				depItems = new Hashtable();
				dep.put(curItem.code, depItems);
			} else {
				depItems = (Hashtable) dep.get(curItem.code);
			}
			dep.put(curItem.code, depItems);
			depItems.put(new Integer(curItem.rowNumber), curItem);

			Hashtable h = (Hashtable) values.get(code);
			Hashtable uh = (Hashtable) updates.get(code);

			if ((h == null) && (uh == null)) {
				return 0.0;
			}

			if (uh != null) {
				h = uh;
			}

			Iterator iter = h.keySet().iterator();
			if (!iter.hasNext()) {
				return 0.0;
			}
			ProcessItem item = (ProcessItem) h.get(iter.next());

			String value = "0";
			double res = 0;

			switch (item.tableEvalType) {
			case ReturnConstants.EVAL_SUM:
				res = evalSum(h.values());
				break;
			case ReturnConstants.EVAL_AVERAGE:
				res = evalAverage(h.values());
				break;
			case ReturnConstants.EVAL_MIN:
				res = evalMin(h.values());
				break;
			case ReturnConstants.EVAL_MAX:
				res = evalMax(h.values());
				break;
			}
			value = Double.toString(res);
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return 0;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return 0;
		}
	}

	public double evalSum(Collection<ProcessItem> v) {
		if (v == null) {
			return 0;
		}
		double res = 0;
		for (ProcessItem item : v) {
			double d = 0;
			if (item.value != null) {
				try {
					d = Double.parseDouble(item.value);
				} catch (NumberFormatException e) {
				}
			}
			res += d;
		}
		return res;
	}

	public double evalAverage(Collection<ProcessItem> v) {
		if (v == null) {
			return 0.0;
		}
		double res = 0.0;
		int count = 0;
		for (ProcessItem item : v) {
			double d = 0.0;
			if (item.value != null) {
				try {
					d = Double.parseDouble(item.value);
				} catch (NumberFormatException e) {
				}
			}
			res += d;
		}
		return res / (double) count;
	}

	public double evalMax(Collection<ProcessItem> v) {
		if (v == null) {
			return 0.0;
		}
		double res = Double.MIN_VALUE;
		for (ProcessItem item : v) {
			double d = 0.0;
			if (item.value != null) {
				try {
					d = Double.parseDouble(item.value);
				} catch (NumberFormatException e) {
				}
			}
			res = Math.max(d, res);
		}
		return res;
	}

	public double evalMin(Collection<ProcessItem> v) {
		if (v == null) {
			return 0.0;
		}
		double res = Double.MAX_VALUE;
		for (ProcessItem item : v) {
			double d = 0.0;
			if (item.value != null) {
				try {
					d = Double.parseDouble(item.value);
				} catch (NumberFormatException e) {
				}
			}
			res = Math.min(d, res);
		}
		return res;
	}

	public double children(String code, String f) {
		try {
			PreparedStatement pstmt = con.prepareStatement("select b.code, a.value from IN_RETURN_ITEMS a, IN_MDT_NODES b where " + "a.nodeID=b.id " + "and b.id in "
					+ "    (select c.id from IN_MDT_NODES c where " + "      c.type=? and " + "      c.parentID in (select d.id from IN_MDT_NODES d where d.code=?) " + "    ) " + "and a.returnID in "
					+ "    (select e.id from IN_RETURNS e where e.scheduleID in " + "        (select f.id from IN_SCHEDULES f where " + "            f.periodID in "
					+ "                (select g.periodID from IN_SCHEDULES g, IN_RETURNS j where g.id=j.scheduleID and j.id=?) " + "            and f.bankID in "
					+ "                (select h.bankID from IN_SCHEDULES h, IN_RETURNS k where h.id=k.scheduleID and k.id=?) " + "        ) " + "    ) " + "and a.versionId=? ");

			pstmt.setInt(1, MDTConstants.NODETYPE_INPUT);
			pstmt.setString(2, code);
			pstmt.setInt(3, returnPK.getId());
			pstmt.setInt(4, returnPK.getId());
			pstmt.setInt(5, versionId);

			ResultSet rs = pstmt.executeQuery();

			double result = 0.0;
			int count = 0;
			for (; rs.next(); count++) {
				String v = rs.getString(2);
				double d = 0.0;
				try {
					d = Double.valueOf(v.replace(",", "")).doubleValue();
				} catch (Exception ex) {
				}
				if (f.toLowerCase().equals("sum")) {
					result += d;
				}
				if (f.toLowerCase().equals("average")) {
					result += d;
				}
				if (f.toLowerCase().equals("min")) {
					if (count == 0) {
						d = Double.MAX_VALUE;
					}
					result = Math.min(result, d);
				}
				if (f.toLowerCase().equals("max")) {
					if (count == 0) {
						d = Double.MIN_VALUE;
					}
					result = Math.max(result, d);
				}
			}
			rs.close();
			pstmt.close();

			if (f.toLowerCase().equals("count")) {
				return (double) count;
			} else {
				return result;
			}
		} catch (Exception e) {
			return 0.0;
		}
	}
}
