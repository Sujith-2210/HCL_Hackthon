package com.hotel.service;

import com.hotel.entity.Booking;
import com.hotel.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@hotelbooking.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendRegistrationConfirmation(User user) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(user.getEmail());
            msg.setSubject("Welcome to HotelBooking – Registration Confirmed");
            msg.setText("Dear " + user.getFirstName() + ",\n\nWelcome! Your account has been created.\n\nEmail: " + user.getEmail() + "\n\nBest regards,\nHotelBooking Team");
            mailSender.send(msg);
            log.info("Registration email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendBookingConfirmation(User user, Booking booking) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(user.getEmail());
            msg.setSubject("Booking Confirmed – " + booking.getBookingReference());
            msg.setText("Dear " + user.getFirstName() + ",\n\nYour booking is confirmed!\n\nRef: " + booking.getBookingReference()
                    + "\nHotel: " + booking.getRoom().getHotel().getName()
                    + "\nRoom: " + booking.getRoom().getRoomNumber()
                    + "\nCheck-in: " + booking.getCheckInDate()
                    + "\nCheck-out: " + booking.getCheckOutDate()
                    + "\nTotal: " + booking.getTotalPrice()
                    + "\n\nBest regards,\nHotelBooking Team");
            mailSender.send(msg);
            log.info("Booking confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation: {}", e.getMessage());
        }
    }

    @Async
    public void sendBookingCancellation(User user, Booking booking) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(user.getEmail());
            msg.setSubject("Booking Cancelled – " + booking.getBookingReference());
            msg.setText("Dear " + user.getFirstName() + ",\n\nYour booking " + booking.getBookingReference() + " has been cancelled.\n\nBest regards,\nHotelBooking Team");
            mailSender.send(msg);
            log.info("Cancellation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }
}
