package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.UserRepository;
import lk.sliit.customer_care_system.service.SingletonServiceManager;
import lk.sliit.customer_care_system.strategy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstration Controller showing Design Patterns in action
 * Shows Singleton and Strategy patterns working together
 */
@RestController
@RequestMapping("/api/demo")
public class DesignPatternDemoController {

    @Autowired
    private SingletonServiceManager serviceManager;

    @Autowired
    private NotificationContext notificationContext;

    @Autowired
    private UserRoleContext userRoleContext;

    @Autowired
    private ValidationContext validationContext;

    /**
     * Demonstrate Singleton pattern
     * Shows that the same service manager instance is used
     */
    @GetMapping("/singleton-demo")
    public ResponseEntity<?> singletonDemo() {
        Map<String, Object> response = new HashMap<>();

        // Get singleton instance
        SingletonServiceManager instance1 = SingletonServiceManager.getInstance();
        SingletonServiceManager instance2 = SingletonServiceManager.getInstance();

        response.put("instance1", instance1.hashCode());
        response.put("instance2", instance2.hashCode());
        response.put("sameInstance", instance1 == instance2);
        response.put("message", "Singleton pattern ensures only one instance exists");

        return ResponseEntity.ok(response);
    }

    /**
     * Demonstrate Strategy pattern for notifications
     * Shows different notification strategies in action
     */
    @GetMapping("/notification-strategies")
    public ResponseEntity<?> notificationStrategiesDemo() {
        Map<String, Object> response = new HashMap<>();

        // Create a demo user
        User demoUser = new User();
        demoUser.setUsername("demo_user");
        demoUser.setPhoneNumber("1234567890");

        // Test different notification strategies
        response.put("emailNotification",
                notificationContext.sendEmailNotification(demoUser, "Demo Email", "This is a demo email notification"));

        response.put("smsNotification",
                notificationContext.sendSMSNotification(demoUser, "Demo SMS", "This is a demo SMS notification"));

        response.put("inAppNotification",
                notificationContext.sendInAppNotification(demoUser, "Demo In-App", "This is a demo in-app notification"));

        response.put("allChannelsNotification",
                notificationContext.sendNotificationToAllChannels(demoUser, "Demo All", "This is a demo notification to all channels"));

        return ResponseEntity.ok(response);
    }

    /**
     * Demonstrate Strategy pattern for user roles
     * Shows different role-based behaviors
     */
    @GetMapping("/role-strategies")
    public ResponseEntity<?> roleStrategiesDemo() {
        Map<String, Object> response = new HashMap<>();

        // Create demo users with different roles
        User adminUser = new User();
        adminUser.setUsername("admin_demo");
        adminUser.setRole("ROLE_ADMIN");

        User agentUser = new User();
        agentUser.setUsername("agent_demo");
        agentUser.setRole("ROLE_AGENT");

        User regularUser = new User();
        regularUser.setUsername("user_demo");
        regularUser.setRole("ROLE_USER");

        // Test role-based dashboard data
        response.put("adminDashboard", userRoleContext.getDashboardData(adminUser));
        response.put("agentDashboard", userRoleContext.getDashboardData(agentUser));
        response.put("userDashboard", userRoleContext.getDashboardData(regularUser));

        // Test role-based permissions
        response.put("adminCanManageUsers", userRoleContext.canPerformAction(adminUser, "create_user"));
        response.put("agentCanManageUsers", userRoleContext.canPerformAction(agentUser, "create_user"));
        response.put("userCanManageUsers", userRoleContext.canPerformAction(regularUser, "create_user"));

        return ResponseEntity.ok(response);
    }

    /**
     * Demonstrate Strategy pattern for validation
     * Shows different validation strategies in action
     */
    @GetMapping("/validation-strategies")
    public ResponseEntity<?> validationStrategiesDemo() {
        Map<String, Object> response = new HashMap<>();

        // Test user validation
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "test_user");
        userData.put("phoneNumber", "1234567890");
        userData.put("address", "123 Main St");
        userData.put("password", "password123");

        response.put("userValidation", validationContext.validateUser(userData));

        // Test ticket validation
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("title", "Test Ticket");
        ticketData.put("description", "This is a test ticket description");
        ticketData.put("category", "Technical");

        response.put("ticketValidation", validationContext.validateTicket(ticketData));

        // Test feedback validation
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("subject", "Test Feedback");
        feedbackData.put("message", "This is a test feedback message");
        feedbackData.put("category", "Service");
        feedbackData.put("rating", 5);

        response.put("feedbackValidation", validationContext.validateFeedback(feedbackData));

        // Test auto-validation
        response.put("autoValidation", validationContext.autoValidate(userData));

        return ResponseEntity.ok(response);
    }

    /**
     * Demonstrate all patterns working together
     * Shows a complete workflow using all design patterns
     */
    @PostMapping("/complete-workflow")
    public ResponseEntity<?> completeWorkflowDemo(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Use Singleton pattern to get repositories
            UserRepository userRepository = serviceManager.getUserRepository();
            response.put("step1", "Singleton pattern: Retrieved repository instances");

            // 2. Use Strategy pattern for validation
            Map<String, String> validationErrors = validationContext.autoValidate(requestData);
            if (!validationErrors.isEmpty()) {
                response.put("step2", "Strategy pattern: Validation failed - " + validationErrors);
                return ResponseEntity.badRequest().body(response);
            }
            response.put("step2", "Strategy pattern: Validation passed");

            // 3. Use Strategy pattern for role-based processing
            String userRole = (String) requestData.getOrDefault("role", "ROLE_USER");
            User demoUser = new User();
            demoUser.setUsername("demo_user");
            demoUser.setRole(userRole);

            Map<String, Object> dashboardData = userRoleContext.getDashboardData(demoUser);
            response.put("step3", "Strategy pattern: Role-based dashboard data - " + dashboardData);

            // 4. Use Strategy pattern for notifications
            int sentCount = notificationContext.sendNotificationToAllChannels(
                    demoUser,
                    "Workflow Complete",
                    "All design patterns have been successfully demonstrated!"
            );
            boolean notificationSent = sentCount > 0;
            response.put("step4", "Strategy pattern: Notification sent - " + notificationSent);

            response.put("message", "Complete workflow demonstration successful!");
            response.put("patternsUsed", List.of("Singleton", "Strategy (Validation)", "Strategy (Role)", "Strategy (Notification)"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Workflow failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}