package Acciones.Navegacion;

import Clases.Insumo;
import DBMS.DBMS;
import Acciones.Gestion.ArchivoAct;
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

public class ResultadoArchivo extends org.apache.struts.action.Action {


    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (!ArchivoAct.agregadosIns.isEmpty()){
            request.setAttribute("agregadosIns", ArchivoAct.agregadosIns);
        }
        if (!ArchivoAct.erroneosIns.isEmpty()){
            request.setAttribute("erroneosIns", ArchivoAct.erroneosIns);
        }
        if (!ArchivoAct.agregadosMat.isEmpty()){
            request.setAttribute("agregadosMat", ArchivoAct.agregadosMat);
        }
        if (!ArchivoAct.erroneosMat.isEmpty()){
            request.setAttribute("erroneosMat", ArchivoAct.erroneosMat);
        }

        return mapping.findForward("success");
    }
}
