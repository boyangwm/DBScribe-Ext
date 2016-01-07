/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Acciones.Gestion.Insumo;

import DBMS.DBMS;
import Clases.Insumo;
import Clases.ItemInsumo;
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
        String seccion = (String) request.getSession().getAttribute("trabajaSec");
        String laboratorio = (String) request.getSession().getAttribute("trabajaLab");
        if (form.getClass() == Insumo.class){
            Insumo insumo = (Insumo) form;
            errors = insumo.validate(mapping, request);
            if (errors.isEmpty()){
                insumo.setLaboratorio(laboratorio);
                insumo.setSeccion(seccion);
                boolean agrego = DBMS.getInstance().agregar(insumo);
                if (agrego) {
                    mensaje.add("agregado", new ActionMessage("success.insumo.agregar"));
                    saveMessages(request.getSession(), mensaje);
                }
            }
            saveErrors(request.getSession(), errors);
            return mapping.findForward("success");
        } else if (form.getClass() == ItemInsumo.class) {
            ItemInsumo item = (ItemInsumo) form;
            errors = item.validate(mapping, request);
            if (errors.isEmpty()){
                boolean agrego = DBMS.getInstance().agregar(item);
                if (agrego) {
                    mensaje.add("agregado", new ActionMessage("success.iteminsumo.agregar"));
                    saveMessages(request.getSession(), mensaje);
                } else {
                    errors.add("numeroExiste", new ActionMessage("error.numero.existe"));
                }
            }
            saveErrors(request.getSession(), errors);
            return mapping.findForward("success");

        } else {
            return mapping.findForward("error");
        }
    }
}
