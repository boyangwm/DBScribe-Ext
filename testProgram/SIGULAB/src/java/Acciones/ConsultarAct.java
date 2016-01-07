package Acciones;

import DBMS.DBMS;
import java.util.ArrayList;
import Form.ConsultaForm;
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

    public static ArrayList consultaIns;
    public static ArrayList consultaMat;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {


        ConsultaForm formulario = (ConsultaForm) form;
        if (formulario.getTipo().equalsIgnoreCase("insumo")
                || formulario.getTipo().equalsIgnoreCase("material")) {

            consultaIns = new ArrayList();
            consultaMat = new ArrayList();
            ArrayList consulta = DBMS.getInstance().consultarDatos(formulario);
            if (formulario.getTipo().equalsIgnoreCase("insumo")) {
                consultaIns = consulta;
            } else {
                consultaMat = consulta;
            }
            return mapping.findForward("redireccionar");
        } else {
            return mapping.findForward("error");
        }
    }
}
