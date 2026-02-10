package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.UserRepository;
import lk.sliit.customer_care_system.service.SingletonServiceManager;
import lk.sliit.customer_care_system.strategy.NotificationContext;
import lk.sliit.customer_care_system.strategy.UserRoleContext;
import lk.sliit.customer_care_system.strategy.ValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced User Controller using Design Patterns
 * Demonstrates Singleton, Strategy patterns
 */
@RestController
@RequestMapping("/api/enhanced/users")
public class EnhancedUserController {

    @Autowired
    private SingletonServiceManager serviceManager;

    @Autowired
    private NotificationContext notificationContext;

    @Autowired
    private UserRoleContext userRoleContext;

    @Autowired
    private ValidationContext validationContext;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create user with enhanced validation and notification
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // Use Strategy pattern for validation
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("address", user.getAddress());
        userData.put("password", user.getPassword());

        Map<String, String> validationErrors = validationContext.validateUser(userData);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Validation failed", "details", validationErrors));
        }

        // Use Singleton pattern for repository access
        UserRepository userRepository = serviceManager.getUserRepository();

        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Username already exists!"));
        }

        // Save user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        User savedUser = userRepository.save(user);

        // Use Strategy pattern for notifications
        notificationContext.sendNotificationToAllChannels(
                savedUser,
                "Welcome to Customer Support System",
                "Your account has been created successfully!"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User created successfully!", "user", savedUser));
    }

    /**
     * Get user dashboard data using role-based strategy
     */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<?> getUserDashboard(@PathVariable Long id) {
        UserRepository userRepository = serviceManager.getUserRepository();
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        // Use Strategy pattern for role-based dashboard data
        Map<String, Object> dashboardData = userRoleContext.getDashboardData(user);

        return ResponseEntity.ok(dashboardData);
    }

    /**
     * Get user permissions using role-based strategy
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<?> getUserPermissions(@PathVariable Long id) {
        UserRepository userRepository = serviceManager.getUserRepository();
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        // Use Strategy pattern for role-based permissions
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("accessibleMenus", userRoleContext.getAccessibleMenus(user));
        permissions.put("canCreateTicket", userRoleContext.canPerformAction(user, "create_ticket"));
        permissions.put("canManageUsers", userRoleContext.canPerformAction(user, "create_user"));
        permissions.put("canViewAnalytics", userRoleContext.canPerformAction(user, "view_analytics"));

        return ResponseEntity.ok(permissions);
    }

    /**
     * Send notification to user using strategy pattern
     */
    @PostMapping("/{id}/notify")
    public ResponseEntity<?> notifyUser(@PathVariable Long id,
                                        @RequestParam String type,
                                        @RequestParam String subject,
                                        @RequestParam String message) {
        UserRepository userRepository = serviceManager.getUserRepository();
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        boolean success = false;
        switch (type.toUpperCase()) {
            case "EMAIL":
                success = notificationContext.sendEmailNotification(user, subject, message);
                break;
            case "SMS":
                success = notificationContext.sendSMSNotification(user, subject, message);
                break;
            case "IN_APP":
                success = notificationContext.sendInAppNotification(user, subject, message);
                break;
            case "ALL":
                int sentCount = notificationContext.sendNotificationToAllChannels(user, subject, message);
                success = sentCount > 0;
                break;
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid notification type. Use: EMAIL, SMS, IN_APP, or ALL"));
        }

        if (success) {
            return ResponseEntity.ok(Map.of("message", "Notification sent successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send notification"));
        }
    }
}

