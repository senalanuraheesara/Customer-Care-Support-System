package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import org.springframework.stereotype.Component;

/**
 * Email notification strategy implementation
 * Sends notifications via email
 */
@Component
public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public boolean sendNotification(User user, String subject, String message) {
        try {
            // Simulate email sending
            System.out.println("=== EMAIL NOTIFICATION ===");
            System.out.println("To: " + user.getUsername() + "@example.com");
            System.out.println("Subject: " + subject);
            System.out.println("Message: " + message);
            System.out.println("=========================");

            // In a real implementation, you would integrate with an email service
            // like JavaMail, SendGrid, AWS SES, etc.
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getNotificationType() {
        return "EMAIL";
    }
}

