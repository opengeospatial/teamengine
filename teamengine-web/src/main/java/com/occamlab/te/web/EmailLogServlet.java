/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * **************************************************************************
 *
 * Contributor(s):
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */

package com.occamlab.te.web;

/*-
 * #%L
 * TEAM Engine - Web Application
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.occamlab.te.config.Config;
import com.occamlab.te.util.ZipUtils;

/**
 * Servlet implementation class for Servlet: EmailLogServlet
 *
 */
public class EmailLogServlet extends jakarta.servlet.http.HttpServlet implements jakarta.servlet.Servlet {

	Config Conf;

	public void init() throws ServletException {
		Conf = new Config();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			File userdir = new File(Conf.getUsersDir(), request.getParameter("userId"));
			File zipFile = new File(userdir, request.getParameter("zipFileName"));

			if (sendLog(getServletConfig().getInitParameter("mail.smtp.host"),
					getServletConfig().getInitParameter("mail.smtp.userid"),
					getServletConfig().getInitParameter("mail.smtp.passwd"), request.getParameter("to"),
					request.getParameter("from"), request.getParameter("subject"), request.getParameter("message"),
					zipFile)) {
				request.setAttribute("emailStatus", "Email sent Succesfully");
			}
			else {
				request.setAttribute("emailStatus", "Email failed");
			}

			RequestDispatcher rd = request.getRequestDispatcher("emailSent.jsp");
			rd.forward(request, response);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			String sessionId = request.getParameter("session");
			String zipFileName = sessionId + ".zip";
			File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
			File sessiondir = new File(userdir, sessionId);
			File zipFile = new File(userdir, zipFileName);
			ZipUtils.zipDir(zipFile, sessiondir);
			request.setAttribute("zipFileName", zipFileName);
			request.setAttribute("to", getServletConfig().getInitParameter("mail.to"));
			RequestDispatcher rd = request.getRequestDispatcher("emailLog.jsp");
			rd.forward(request, response);

		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public boolean sendLog(String host, String userId, String password, String to, String from, String subject,
			String message, File filename) {
		boolean success = true;
		System.out.println("host: " + host);
		System.out.println("userId: " + userId);
		// Fortify Mod: commented out clear text password.
		// System.out.println("password: " + password);
		System.out.println("to: " + to);
		System.out.println("from: " + from);
		System.out.println("subject: " + subject);
		System.out.println("message: " + message);
		System.out.println("filename: " + filename.getName());
		System.out.println("filename: " + filename.getAbsolutePath());

		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");

		Session session = Session.getInstance(props, null);
		session.setDebug(true);

		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			InternetAddress[] address = { new InternetAddress(to) };
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(subject);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message);

			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();

			// attach the file to the message
			FileDataSource fds = new FileDataSource(filename);
			mbp2.setDataHandler(new DataHandler(fds));
			mbp2.setFileName(fds.getName());

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);

			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// connect to the transport
			Transport trans = session.getTransport("smtp");
			trans.connect(host, userId, password);

			// send the message
			trans.sendMessage(msg, msg.getAllRecipients());

			// smtphost
			trans.close();

		}
		catch (MessagingException mex) {
			success = false;
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		}
		return success;
	}

}
