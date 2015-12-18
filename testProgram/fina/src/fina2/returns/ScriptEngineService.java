package fina2.returns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import fina2.db.DatabaseUtil;
import fina2.script.JSTree;

public class ScriptEngineService extends ServiceMBeanSupport {

	private static Logger log = Logger.getLogger(ScriptEngineService.class);

	private static ScriptEngineService instance;

	private Timer timer = new Timer(true);

	private TimerTask timerTask;

	private Map<String, Function> functions;

	private Scriptable scope;

	public static ScriptEngineService getInstance() {
		return instance;
	}

	protected void startService() {

		log.info("Starting Script Engine Service");
		try {
			functions = new ConcurrentHashMap<String, Function>();
			scope = Context.enter().initStandardObjects(null);
			instance = this;
			timerTask = new TimerTask() {
				public void run() {
					updateScripts();
				}
			};
			timer.schedule(timerTask, 0, 60000);
			log.info("Started Script Engine Service");
		} catch (Exception ex) {
			log.error("Error While Starting Script Engine Service", ex);
		} finally {
			Context.exit();
		}
	}

	protected void stopService() {
		log.info("Stopping Script Engine Service");
	}

	public String callFuction(JSTree tree, String source) throws JavaScriptException {
		Context c = Context.enter();
		String res = null;
		try {
			scope.put("tree", scope, Context.toObject(tree, scope));
			res = Context.toString(getFunction(source).call(c, scope, scope, new Object[0]));
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			if (c != null)
				Context.exit();
		}
		return res;
	}

	private Function getFunction(String source) throws JavaScriptException {

		Function func = null;
		if (functions.get(source) != null) {
			func = functions.get(source);
		} else {
			func = compile(source);
			functions.put(source, func);
		}
		return func;
	}

	private Function compile(String source) throws JavaScriptException {
		Function f = null;
		try {
			Context ctx = Context.enter();
			ctx.evaluateString(scope, source, "<script>", 1, null);
			f = (Function) scope.get("fina2_mdt_node", scope);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			Context.exit();
		}
		return f;
	}

	private synchronized void updateScripts() {

		Connection con = null;
		PreparedStatement update = null;
		Set<String> keys = new HashSet<String>();
		try {
			con = DatabaseUtil.getConnection();

			getFormulaSources(con, "IN_MDT_NODES", keys);
			getFormulaSources(con, "IN_MDT_COMPARISON", keys);

			for (Iterator<String> it = functions.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				if (!functions.containsKey(key)) {
					functions.put(key, compile(key));
				}
			}

			for (Iterator<String> it = functions.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				if (!keys.contains(key)) {
					functions.remove(key);
				}
			}
			/*
			 * for (String key : functions.keySet()) { if (!keys.contains(key))
			 * { functions.remove(key); } }
			 */
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(update);
		}
	}

	public void getFormulaSources(Connection con, String tableName, Set<String> keys) throws SQLException {

		PreparedStatement select = null;
		try {
			select = con.prepareStatement("SELECT DISTINCT equation FROM " + tableName + " WHERE equation IS NOT NULL");
			ResultSet rs = select.executeQuery();
			while (rs.next()) {
				keys.add("function fina2_mdt_node() {\n" + rs.getString(1) + "\n}");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeStatement(select);
		}
	}
}
