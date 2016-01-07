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

public class VisibleAct extends org.apache.struts.action.Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ActionErrors errors = new ActionErrors();
        ActionMessages mensaje = new ActionMessages();
        if (form.getClass() == ItemInsumo.class) {
            ItemInsumo item = (ItemInsumo) form;
            if (errors.isEmpty()){
                DBMS.getInstance().visualizar(item);
            }
            saveErrors(request.getSession(), errors);
            return mapping.findForward("success");

        } else {
            return mapping.findForward("error");
        }
    }
}
