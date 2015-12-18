package fina2.servergate;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.bank.BankPK;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
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

/**
 * Contains set of utility methods for FI. The class contains only static
 * methods and can't have instances.
 */
public class FIGate {

	/** Private constructor to avoid creating the instances of the class */
	private FIGate() {
	}

	// -------------------------------------------------------------------------
	// FI management methods

	/** Returns bank session */
	public static BankSession getBankSession() throws Exception {

		BankSessionHome home = (BankSessionHome) fina2.Main.getRemoteObject(
				"fina2/bank/BankSession", BankSessionHome.class);

		return home.create();
	}

	/** Returns bank types from server */
	public static List<Node> getBankTypes() throws FinaTypeException, Exception {

		BankSession session = getBankSession();
		Handle userHandle = Main.main.getUserHandle();
		Handle languageHandle = Main.main.getLanguageHandle();
		return session.getBankTypes(userHandle, languageHandle);
	}

	/** Returns bank id by given bank code */
	public static Integer getBankId(String bankCode) throws Exception {
		BankSession session = getBankSession();
		return session.getBankId(bankCode);
	}

	/** Returns FI list for the currenct user and language */
	public static Collection getBanks() throws FinaTypeException, Exception {

		BankSession session = getBankSession();
		Handle userHandle = Main.main.getUserHandle();
		Handle languageHandle = Main.main.getLanguageHandle();

		return session.getBanks(userHandle, languageHandle);
	}

	public static Collection loadBanks() throws FinaTypeException, Exception {
		BankSession session = getBankSession();
		Handle userHandle = Main.main.getUserHandle();
		Handle languageHandle = Main.main.getLanguageHandle();
		return session.loadBanks(userHandle, languageHandle);
	}
}
