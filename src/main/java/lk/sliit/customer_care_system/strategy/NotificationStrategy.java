package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;

/**
 * Strategy interface for different notification methods
 * This allows the system to send notifications via different channels
 */
public interface NotificationStrategy {

    /**
     * Send notification to a user
     * @param user The user to notify
     * @param subject The notification subject
     * @param message The notification message
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendNotification(User user, String subject, String message);

    /**
     * Get the notification type name
     * @return String representation of the notification type
     */
    String getNotificationType();
}

