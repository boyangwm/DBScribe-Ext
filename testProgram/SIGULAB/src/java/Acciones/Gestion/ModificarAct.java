package Acciones.Gestion;

import Acciones.LoginAct;
import DBMS.DBMS;
import Form.ModificarInsForm;
import Form.ModificarMatForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class ModificarAct extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionErrors errors = new ActionErrors();
        ActionMessages mensaje = new ActionMessages();
        if (form.getClass() == ModificarInsForm.class){
            ModificarInsForm formulario = (ModificarInsForm) form;
            errors = formulario.validate(mapping, request);
            if (errors.isEmpty()){
                boolean modifico = DBMS.getInstance().modificarInsDato(formulario);
                if (modifico) {
                    mensaje.add("modificado", new ActionMessage("success.act.insumo.modificar"));
                    saveMessages(request.getSession(), mensaje);
                }
            }
            saveErrors(request.getSession(), errors);
            return mapping.findForward("redireccionar");
        } else if ((form.getClass() == ModificarMatForm.class)){
            ModificarMatForm formulario = (ModificarMatForm) form;
            errors = formulario.validate(mapping, request);
            if (errors.isEmpty()){
                boolean modifico = DBMS.getInstance().modificarMatDato(formulario);
                if (modifico) {
                    mensaje.add("modificado", new ActionMessage("success.act.material.modificar"));
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
