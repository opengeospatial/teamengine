package com.occamlab.te.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.occamlab.te.util.DocumentationHelper;
import com.occamlab.te.util.Misc;


public class GenerateDocumentationServlet extends HttpServlet implements Servlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4555573278222613701L;
	Config Conf;
	//DocumentHelper docHelper;
	DocumentationHelper docHelper;

	public void init() throws ServletException {
		Conf = new Config();
		File stylesheet = Misc.getResourceAsFile("com/occamlab/te/test_report_html.xsl");
		//docHelper= new DocumentHelper(stylesheet);
		docHelper= new DocumentationHelper(stylesheet);
	}


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			Map<String,List<File>> srcs = Conf.getSources();
			List<File> dir= srcs.get(request.getParameter("sources"));
			for (Iterator iterator = dir.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				System.out.println(" file sorgente: " +file.getAbsolutePath());
			}
			
			Map params = request.getParameterMap();
			Iterator i = params.keySet().iterator();

			while ( i.hasNext() )
			  {
			    String key = (String) i.next();
			    String value = ((String[]) params.get( key ))[0];
			    System.out.println("Kiave "+key+" Valore "+value);
			  }
			
			//Conf.getSuites()
			
//			String sessionId = request.getParameter("session");
//			String reportFileName = sessionId + ".html";
//			File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
//			//File userdir = new File(Conf.getUsersDir(), "tester1");
//			File sessiondir = new File(userdir, sessionId);
//			File prettyPrintReportFile = new File(userdir, reportFileName);
//			docHelper.prettyPrintsReport(sessiondir,prettyPrintReportFile);
//			response.setContentType("text/html");
//			response.setHeader("Content-Disposition", "attachment; filename="+reportFileName+";");
//			response.setHeader("Cache-Control", "no-cache");
//			byte[] buf = new byte[response.getBufferSize()];
//			response.setContentLength((int)prettyPrintReportFile.length());
//			System.out.println("file length : " + (int)prettyPrintReportFile.length());
//			int length;
//
//			BufferedInputStream fileInBuf = new BufferedInputStream(new FileInputStream (prettyPrintReportFile));
//
//			OutputStream out = response.getOutputStream();
//
//			while((length = fileInBuf.read(buf)) > 0) {
//				out.write(buf, 0, length);
//			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}


}
