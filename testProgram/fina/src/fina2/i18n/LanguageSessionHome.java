/*
 * LanguageSessionHome.java
 *
 * Created on October 15, 2001, 6:45 PM
 */

package fina2.i18n;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface LanguageSessionHome extends EJBHome {

    LanguageSession create() throws CreateException, EJBException,
            RemoteException;

}
