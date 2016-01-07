/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Acciones.Solicitudes;

import Clases.*;
import DBMS.DBMS;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class AgregarProdAct extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ActionErrors errors = new ActionErrors();
        ActionMessages mensaje = new ActionMessages();
        String seccion = (String) request.getSession().getAttribute("trabajaSec");
        String laboratorio = (String) request.getSession().getAttribute("trabajaLab");
        Producto producto = (Producto) form;
        errors = producto.validate(mapping, request);
        saveErrors(request.getSession(), errors);

        if (errors.isEmpty()){
            if (producto.getTipo().equalsIgnoreCase("insumo")){
                boolean agrego = DBMS.getInstance().solicitar(producto);
                if (agrego) {
                    mensaje.add("agregado", new ActionMessage("success.insumo.agregar"));
                    saveMessages(request.getSession(), mensaje);
                } else {
                    errors.add("existeSol", new ActionMessage("error.producto.existe"));
                }

            } else if (producto.getTipo().equalsIgnoreCase("material")){
            } else {
                return mapping.findForward("error");
            }
        }
        return mapping.findForward("success");

    }
}
