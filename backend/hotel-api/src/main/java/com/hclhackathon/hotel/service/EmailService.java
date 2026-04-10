package com.hclhackathon.hotel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final ObjectProvider<JavaMailSender> mailSender;

	public EmailService(ObjectProvider<JavaMailSender> mailSender) {
		this.mailSender = mailSender;
	}

	public void sendRegistrationEmail(String toEmail, String fullName) {
		sendOrLog(toEmail, "Welcome to Hotel Booking", "Hi " + fullName + ", your registration is successful.");
	}

	public void sendBookingConfirmationEmail(String toEmail, String bookingId) {
		sendOrLog(toEmail, "Booking confirmed", "Your booking is confirmed. Booking ID: " + bookingId);
	}

	public void sendBookingReceivedEmail(String toEmail, String bookingId) {
		sendOrLog(toEmail, "Booking received", "Your booking request is received and pending confirmation. Booking ID: " + bookingId);
	}

	public void sendBookingCancellationEmail(String toEmail, String bookingId) {
		sendOrLog(toEmail, "Booking cancelled", "Your booking is cancelled. Booking ID: " + bookingId);
	}

	private void sendOrLog(String toEmail, String subject, String body) {
		if (toEmail == null || toEmail.isBlank()) return;

		try {
			var sender = mailSender.getIfAvailable();
			if (sender == null) {
				log.info("email_skipped_missing_mail_sender to={} subject={}", toEmail, subject);
				return;
			}

			var msg = new SimpleMailMessage();
			msg.setTo(toEmail);
			msg.setSubject(subject);
			msg.setText(body);
			sender.send(msg);
		} catch (Exception ex) {
			log.warn("email_send_failed to={} subject={} reason={}", toEmail, subject, ex.getMessage());
		}
	}
}
