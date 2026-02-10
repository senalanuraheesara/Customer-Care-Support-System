package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Context class for managing notification strategies
 * Uses Strategy pattern to send notifications via different channels
 */
@Component
public class NotificationContext {

    @Autowired
    private List<NotificationStrategy> notificationStrategies;

    /**
     * Send notification using the specified strategy
     * @param strategy The notification strategy to use
     * @param user The user to notify
     * @param subject The notification subject
     * @param message The notification message
     * @return true if notification was sent successfully
     */
    public boolean sendNotification(NotificationStrategy strategy, User user, String subject, String message) {
        return strategy.sendNotification(user, subject, message);
    }

    /**
     * Send notification via email
     * @param user The user to notify
     * @param subject The notification subject
     * @param message The notification message
     * @return true if notification was sent successfully
     */
    public boolean sendEmailNotification(User user, String subject, String message) {
        NotificationStrategy emailStrategy = getStrategyByType("EMAIL");
        return emailStrategy != null && emailStrategy.sendNotification(user, subject, message);
    }

    /**
     * Send notification via SMS
     * @param user The user to notify
     * @param subject The notification subject
     * @param message The notification message
     * @return true if notification was sent successfully
     */
    public boolean sendSMSNotification(User user, String subject, String message) {
        NotificationStrategy smsStrategy = getStrategyByType("SMS");
        return smsStrategy != null && smsStrategy.sendNotification(user, subject, message);
    }

    /**
     * Send in-app notification
     * @param user The user to notify
     * @param subject The notification subject
     * @param message The notification message
     * @return true if notification was sent successfully
     */
    public boolean sendInAppNotification(User user, String subject, String message) {
        NotificationStrategy inAppStrategy = getStrategyByType("IN_APP");
        return inAppStrategy != null && inAppStrategy.sendNotification(user, subject, message);
    }

    /**
     * Send notification via all available channels
     * @param user The user to notify
     * @param subject The notification subject
     * @param message The notification message
     * @return number of successful notifications sent
     */
    public int sendNotificationToAllChannels(User user, String subject, String message) {
        int successCount = 0;
        for (NotificationStrategy strategy : notificationStrategies) {
            if (strategy.sendNotification(user, subject, message)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * Get notification strategy by type
     * @param type The notification type
     * @return NotificationStrategy instance or null if not found
     */
    private NotificationStrategy getStrategyByType(String type) {
        return notificationStrategies.stream()
                .filter(strategy -> strategy.getNotificationType().equals(type))
                .findFirst()
                .orElse(null);
    }
}

