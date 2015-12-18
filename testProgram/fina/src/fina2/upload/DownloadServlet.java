package fina2.upload;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class DownloadServlet extends HttpServlet {
	
	private Logger log = Logger.getLogger(DownloadServlet.class);
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("application/octet-stream"); 
		
		if(req.getParameter("id") == null){
			return;
		}
		try {
			ServletOutputStream out = resp.getOutputStream();
			ZipOutputStream zipOutputStream = new ZipOutputStream(out);
			
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFile");
			UploadFileHome home = (UploadFileHome)PortableRemoteObject.narrow(ref, UploadFileHome.class);
			UploadFile file = home.findByPrimaryKey(new UploadFilePK(Integer.parseInt(req.getParameter("id"))));
			
			zipOutputStream.putNextEntry(new ZipEntry(file.getFileName()));
			resp.setHeader("Content-Disposition","inline; filename="+file.getFileName()+".zip;");
			
			byte [] b = file.getFile();
			zipOutputStream.write(b);
			
			zipOutputStream.closeEntry();
			zipOutputStream.close();
			out.close();
		} catch (Exception ex) {
			log.error("Can't Download File id - '"+ req.getParameter("id") + "'", ex);
		}		
	}
	
}
