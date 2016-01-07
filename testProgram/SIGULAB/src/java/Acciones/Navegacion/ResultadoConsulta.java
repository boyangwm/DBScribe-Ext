package Acciones.Navegacion;

import Clases.Insumo;
import DBMS.DBMS;
import Acciones.ConsultarAct;
import Acciones.LoginAct;
import Form.AgregarMatForm;
import Form.AgregarInsForm;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class ResultadoConsulta extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {


        if ((ConsultarAct.consultaIns != null) && (!ConsultarAct.consultaIns.isEmpty())){
            request.setAttribute("consultaIns", ConsultarAct.consultaIns);
            request.removeAttribute("consultaMat");
        }
        else if ((ConsultarAct.consultaMat != null) && (!ConsultarAct.consultaMat.isEmpty())) {
            request.setAttribute("consultaMat", ConsultarAct.consultaMat);
            request.removeAttribute("consultaIns");
        }

        return mapping.findForward("success");
    }
}
