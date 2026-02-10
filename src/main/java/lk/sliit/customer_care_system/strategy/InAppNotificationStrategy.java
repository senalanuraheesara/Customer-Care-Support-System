package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import org.springframework.stereotype.Component;

/**
 * In-app notification strategy implementation
 * Sends notifications within the application
 */
@Component
public class InAppNotificationStrategy implements NotificationStrategy {

    @Override
    public boolean sendNotification(User user, String subject, String message) {
        try {
            // Simulate in-app notification
            System.out.println("=== IN-APP NOTIFICATION ===");
            System.out.println("User: " + user.getUsername());
            System.out.println("Subject: " + subject);
            System.out.println("Message: " + message);
            System.out.println("Timestamp: " + java.time.LocalDateTime.now());
            System.out.println("===========================");

            // In a real implementation, you would store this in a database
            // or use WebSocket to push real-time notifications
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send in-app notification: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getNotificationType() {
        return "IN_APP";
    }
}

