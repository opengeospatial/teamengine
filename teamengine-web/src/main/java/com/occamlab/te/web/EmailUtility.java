package com.occamlab.te.web;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtility {

  public static void sendEmail(String host, String portNo,
      final String userName, final String pwd, String toAddress,
      String subject, String message) throws AddressException,
      MessagingException {

    Properties properties = new Properties();
    properties.put("mail.smtp.host", host);
    properties.put("mail.smtp.port", portNo);
    properties.put("mail.smtp.starttls.enable", "true");

    Session session = Session.getDefaultInstance(properties);
    Message msg = new MimeMessage(session);
    try {
      msg.setFrom(new InternetAddress(userName));
      InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
      msg.setRecipients(Message.RecipientType.TO, toAddresses);
      msg.setSubject(subject);
      msg.setSentDate(new Date());
      msg.setText(message);

      Transport.send(msg);
    } catch (Exception e) {
      throw new RuntimeException("Failed send mail : " + e.getMessage());
    }
  }
  
  public static String getRandomNumberString() {
    Random randomNo = new Random();
    int number = randomNo.nextInt(999999);
    return String.format("%06d", number);
  }
}
