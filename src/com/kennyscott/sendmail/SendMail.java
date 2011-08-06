
package com.kennyscott.sendmail;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * This is mainly lifted from (URL over 2 lines here:)
 * http://www.velocityreviews.com/forums/t141237-send-smtp-mail-using-javamail
 * -with-gmail-account.html but with the main difference that the authentication details are not
 * stored in the source, and are instead stored in a Java properties file. This is made slightly
 * more tricky on the basis that you can't just implement an inner class, as otherwise the
 * Properties file needs to be final, which defeats the purpose of it. You'll note that there is no
 * properties file included in the Git project, because I'm not wanting to tell you my
 * authentication details ;)
 * 
 * @author mkns
 * 
 */
public class SendMail {

	private static final String SMTP_HOST_NAME = "smtp.gmail.com";
	private static final String SMTP_PORT = "465";
	private static final String emailMsgTxt = "Test Message Contents";
	private static final String emailSubjectTxt = "Yet another test from gmail";
	private static final String emailFromAddress = "kenny@example.com";
	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	private static final String[] sendTo = { "kenny@kennyscott.co.uk" };

	public static void main( String args[] ) throws Exception {
		SendMail sm = new SendMail();
		sm.sendSSLMessage( sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress );
	}

	private void sendSSLMessage( String sendTo[], String emailSubjectTxt, String emailMsgTxt, String emailFromAddress ) throws MessagingException {
		boolean debug = true;

		Properties properties = new Properties();
		try {
			properties.load( new FileInputStream( "sendmail.properties" ) );
		}
		catch ( IOException e ) {
			e.printStackTrace();
			System.exit( 1 );
		}
		properties.put( "mail.smtp.host", SMTP_HOST_NAME );
		properties.put( "mail.smtp.port", SMTP_PORT );
		properties.put( "mail.smtp.socketFactory.port", SMTP_PORT );
		properties.put( "mail.smtp.socketFactory.class", SSL_FACTORY );
		properties.put( "mail.smtp.socketFactory.fallback", "false" );
		properties.put( "mail.smtp.auth", "true" );
		properties.put( "mail.debug", debug );

		SMTPAuthenticator auth = new SMTPAuthenticator();
		auth.setProperties( properties );
		Session session = Session.getDefaultInstance( properties, auth );

		session.setDebug( debug );

		Message msg = new MimeMessage( session );
		InternetAddress addressFrom = new InternetAddress( emailFromAddress );
		msg.setFrom( addressFrom );

		InternetAddress[] addressTo = new InternetAddress[sendTo.length];
		for ( int i = 0 ; i < sendTo.length ; i++ ) {
			addressTo[i] = new InternetAddress( sendTo[i] );
		}
		msg.setRecipients( Message.RecipientType.TO, addressTo );

		msg.setSubject( emailSubjectTxt );
		msg.setContent( emailMsgTxt, "text/plain" );
		Transport.send( msg );
	}

	/**
	 * Simple implementation with the addition of a method to set the Properties file, within which
	 * are the authentication details
	 * 
	 * @author mkns
	 * 
	 */
	class SMTPAuthenticator extends javax.mail.Authenticator {

		Properties properties = null;

		void setProperties( Properties properties ) {
			this.properties = properties;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication( properties.getProperty( "username" ), properties.getProperty( "password" ) );
		}
	}
}
