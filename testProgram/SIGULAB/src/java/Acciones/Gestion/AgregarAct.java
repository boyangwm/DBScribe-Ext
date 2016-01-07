/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Acciones.Gestion;

import Acciones.LoginAct;
import DBMS.DBMS;
import Form.AgregarMatForm;
import Form.AgregarInsForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class AgregarAct extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ActionErrors errors = new ActionErrors();
        ActionMessages mensaje = new ActionMessages();
        String seccion = (String) request.getSession().getAttribute("codsec");
        String laboratorio = (String) request.getSession().getAttribute("codlab");
        if (form.getClass() == AgregarInsForm.class){
            AgregarInsForm formulario = (AgregarInsForm) form;
            errors = formulario.validate(mapping, request);
            if (errors.isEmpty()){
                formulario.setCodlab(laboratorio);
                formulario.setCodsec(seccion);
                boolean agrego = DBMS.getInstance().agregarDatosIns(formulario);
                if (agrego) {
                    mensaje.add("agregado", new ActionMessage("success.act.insumo.agregar"));
                    saveMessages(request.getSession(), mensaje);
                }
            }
            saveErrors(request.getSession(), errors);
            return mapping.findForward("redireccionar");
        } if (form.getClass() == AgregarMatForm.class){
            AgregarMatForm formulario = (AgregarMatForm) form;
            errors = formulario.validate(mapping, request);
            if (errors.isEmpty()){
                formulario.setCodlab(laboratorio);
                formulario.setCodsec(seccion);
                boolean agrego = DBMS.getInstance().agregarDatosMat(formulario);
                if (agrego) {
                    mensaje.add("agregado", new ActionMessage("success.act.insumo.agregar"));
                    saveMessages(request.getSession(), mensaje);
                }
            }
            saveErrors(request.getSession(), errors);
            return mapping.findForward("redireccionar");
        } else {
            return mapping.findForward("error");
        }
    }
}
