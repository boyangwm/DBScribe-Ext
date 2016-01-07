/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Acciones.Navegacion;

import Acciones.LoginAct;
import Clases.Laboratorio;
import Clases.Seccion;
import DBMS.DBMS;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author Esteban
 */
public class Consultar extends org.apache.struts.action.Action{

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        request.setAttribute("listaLab", DBMS.getInstance().listarLaboratorios());
        request.setAttribute("listaSec", DBMS.getInstance().listarSecciones());
        return mapping.findForward("success");
    }

}
