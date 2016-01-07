/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Acciones.Navegacion;


import Acciones.LoginAct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Esteban
 */
public class Basico extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HashMap<Integer,Integer> x = new HashMap<Integer, Integer>();
        x.put(1, 2);
        x.put(3, 4);
        HttpSession session = request.getSession(true);
        session.setAttribute("prueba", x);
        Set s =  x.keySet();
        session.setAttribute("set", s);
        return mapping.findForward("success");
    }

}
