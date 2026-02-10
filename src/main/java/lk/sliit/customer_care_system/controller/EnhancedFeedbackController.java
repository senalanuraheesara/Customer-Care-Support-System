package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.Feedback;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.FeedbackRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import lk.sliit.customer_care_system.service.SingletonServiceManager;
import lk.sliit.customer_care_system.strategy.NotificationContext;
import lk.sliit.customer_care_system.strategy.ValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Feedback Controller using Design Patterns
 * Demonstrates Singleton, Strategy patterns
 */
@RestController
@RequestMapping("/api/enhanced/feedback")
public class EnhancedFeedbackController {

    @Autowired
    private SingletonServiceManager serviceManager;

    @Autowired
    private NotificationContext notificationContext;

    @Autowired
    private ValidationContext validationContext;

    /**
     * Create feedback with enhanced validation and notification
     */
    @PostMapping
    public ResponseEntity<?> createFeedback(@RequestBody Feedback feedback, Authentication authentication) {
        // Use Strategy pattern for validation
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("subject", feedback.getSubject());
        feedbackData.put("message", feedback.getMessage());
        feedbackData.put("category", feedback.getCategory());
        feedbackData.put("rating", feedback.getRating());

        Map<String, String> validationErrors = validationContext.validateFeedback(feedbackData);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Validation failed", "details", validationErrors));
        }

        // Use Singleton pattern for repository access
        FeedbackRepository feedbackRepository = serviceManager.getFeedbackRepository();
        UserRepository userRepository = serviceManager.getUserRepository();

        // Get current user
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set feedback properties
        feedback.setUser(user);
        feedback.setStatus("New");
        feedback.setCreatedAt(LocalDateTime.now());

        // Save feedback
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Use Strategy pattern for notifications
        notificationContext.sendInAppNotification(
                user,
                "Feedback Submitted",
                "Your feedback has been submitted successfully! We'll review it soon.");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Feedback created successfully!", "feedback", savedFeedback));
    }

    /**
     * Respond to feedback with notification
     */
    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respondToFeedback(@PathVariable Long id,
            @RequestParam String response,
            Authentication authentication) {
        // Use Singleton pattern for repository access
        FeedbackRepository feedbackRepository = serviceManager.getFeedbackRepository();

        Feedback feedback = feedbackRepository.findById(id).orElse(null);
        if (feedback == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Feedback not found"));
        }

        // Update feedback with response
        feedback.setAdminResponse(response);
        feedback.setAdminResponseAt(LocalDateTime.now());
        feedback.setStatus("Responded");
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        // Use Strategy pattern for notifications
        if (feedback.getUser() != null) {
            notificationContext.sendInAppNotification(
                    feedback.getUser(),
                    "Feedback Response",
                    "We've responded to your feedback. Please check your feedback page.");
        }

        return ResponseEntity.ok(Map.of("message", "Response added successfully"));
    }

    /**
     * Get feedback statistics using singleton pattern
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getFeedbackStatistics() {
        // Use Singleton pattern for repository access
        FeedbackRepository feedbackRepository = serviceManager.getFeedbackRepository();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalFeedbacks", feedbackRepository.count());
        statistics.put("newFeedbacks", feedbackRepository.countByStatus("New"));
        statistics.put("respondedFeedbacks", feedbackRepository.countByAdminResponseIsNotNull());
        statistics.put("anonymousFeedbacks", feedbackRepository.countByIsAnonymousTrue());

        return ResponseEntity.ok(statistics);
    }
}
