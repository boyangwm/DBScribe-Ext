/*
 * LanguageHome.java
 *
 * Created on October 15, 2001, 2:46 PM
 */

package fina2.i18n;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author  David Shalamberidze
 * @version
 */

public interface LanguageHome extends EJBHome {

    Language create() throws EJBException, CreateException, RemoteException;

    Language findByPrimaryKey(LanguagePK pk) throws EJBException,
            FinderException, RemoteException;

    Language findByCode(String code) throws EJBException, FinderException,
            RemoteException;
}
