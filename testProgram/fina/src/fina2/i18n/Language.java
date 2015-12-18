/*
 * Language.java
 *
 * Created on October 15, 2001, 2:44 PM
 */

package fina2.i18n;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;

import fina2.FinaTypeException;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */

public interface Language extends EJBObject {

    String getCode() throws RemoteException, EJBException;

    void setCode(String param) throws RemoteException, EJBException,
            FinaTypeException;

    String getDescription() throws RemoteException, EJBException;

    void setDescription(String param) throws RemoteException, EJBException;

    String getFontFace() throws RemoteException, EJBException;

    void setFontFace(String param) throws RemoteException, EJBException;

    int getFontSize() throws RemoteException, EJBException;

    void setFontSize(int param) throws RemoteException, EJBException;

    String getDateFormat() throws RemoteException, EJBException;

    void setDateFormat(String param) throws RemoteException, EJBException;

    String getNumberFormat() throws RemoteException, EJBException;

    void setNumberFormat(String param) throws RemoteException, EJBException;

    String getHtmlCharset() throws RemoteException, EJBException;

    void setHtmlCharset(String param) throws RemoteException, EJBException;

    String getXmlEncoding() throws RemoteException, EJBException;

    void setXmlEncoding(String param) throws RemoteException, EJBException;

    /** @link dependency */
    /*#LanguageBean lnkLanguageBean;*/
}
