package Acciones.Consultas;

import DBMS.DBMS;
import java.util.ArrayList;
import Clases.*;
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

public class ConsultarAct extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {


        if (form.getClass() == Insumo.class){
            Insumo insumo = (Insumo) form;
            HashMap consulta = DBMS.getInstance().consultar(insumo);
            request.getSession().setAttribute("consultaIns", consulta);
            return mapping.findForward("success");
        } else {
            System.out.println(form.getClass().toString());
            return mapping.findForward("error");
        }
    }
}
