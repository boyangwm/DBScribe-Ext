package Acciones.Navegacion;

import Acciones.LoginAct;
import Clases.Insumo;
import DBMS.DBMS;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class GestionarIns extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String lab = (String) request.getSession().getAttribute("trabajaLab");
        String sec = (String) request.getSession().getAttribute("trabajaSec");
        HashMap inventario = DBMS.getInstance().gestionIns(lab, sec);
        //request.setAttribute("prodClave", inventario.keySet());
        request.setAttribute("inventario", inventario);
        return mapping.findForward("success");
    }
}
