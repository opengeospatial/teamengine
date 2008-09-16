package com.occamlab.te.web;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.occamlab.te.Test;
import com.occamlab.te.util.ZipUtils;

/**
 * Servlet implementation class for Servlet: DownloadLogServlet
 *
 */
 public class DownloadLogServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	  Config Conf;

	  public void init() throws ServletException {
	    Conf = new Config();
	  }

	  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
	    try {
	      String sessionId = request.getParameter("session");
	      String zipFileName = sessionId + ".zip";
	      File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
	      //File userdir = new File(Conf.getUsersDir(), "tester1");
	      File sessiondir = new File(userdir, sessionId);
	      File zipFile = new File(userdir, zipFileName);
	      ZipUtils.zipDir(zipFile, sessiondir);
	      response.setContentType("application/zip");
	      response.setHeader("Content-Disposition", "attachment; filename="+zipFileName+";");
	      response.setHeader("Cache-Control", "no-cache");
	      byte[] buf = new byte[response.getBufferSize()];
	      response.setContentLength((int)zipFile.length());
	      System.out.println("file length : " + (int)zipFile.length());
	      int length;

	      BufferedInputStream fileInBuf = new BufferedInputStream(new FileInputStream (zipFile));

	      ByteArrayOutputStream baos = new ByteArrayOutputStream();

	      while((length = fileInBuf.read(buf)) > 0) {
	      baos.write(buf, 0, length);
	      }

	      response.getOutputStream().write(baos.toByteArray());
	      response.getOutputStream().flush();
	      response.getOutputStream().close();
	      }
	      catch(Exception e)
	      {
	      System.out.println(e.getMessage());
	      }
	  }
}