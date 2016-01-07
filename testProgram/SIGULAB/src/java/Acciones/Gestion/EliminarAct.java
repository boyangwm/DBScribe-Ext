package Acciones.Gestion;

import Acciones.LoginAct;
import DBMS.DBMS;
import Form.EliminarForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class EliminarAct extends org.apache.struts.action.Action{

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionErrors errors = new ActionErrors();
        ActionMessages mensaje = new ActionMessages();
        EliminarForm formulario = (EliminarForm) form;

        if (formulario.getTipo().equalsIgnoreCase("insumo") ||
                formulario.getTipo().equalsIgnoreCase("material")){

            DBMS.getInstance().eliminarDato(formulario);
            if (formulario.getTipo().equalsIgnoreCase("insumo")){
                return mapping.findForward("redireccionarIns");
            } else {
                return mapping.findForward("redireccionarMat");
            }
        } else {
            return mapping.findForward("error");
        }
    }

}
