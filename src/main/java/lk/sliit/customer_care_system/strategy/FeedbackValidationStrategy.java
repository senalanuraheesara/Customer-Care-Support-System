package lk.sliit.customer_care_system.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Feedback validation strategy implementation
 * Handles validation for feedback-related data
 */
@Component
public class FeedbackValidationStrategy implements ValidationStrategy {

    private static final String[] VALID_CATEGORIES = {
            "Service", "Product", "Complaint", "Suggestion", "Other"
    };

    @Override
    public Map<String, String> validate(Map<String, Object> data) {
        Map<String, String> errors = new HashMap<>();

        // Subject validation
        if (data.containsKey("subject")) {
            String subject = (String) data.get("subject");
            if (subject == null || subject.trim().isEmpty()) {
                errors.put("subject", "Subject is required");
            } else if (subject.trim().length() < 5) {
                errors.put("subject", "Subject must be at least 5 characters");
            } else if (subject.length() > 200) {
                errors.put("subject", "Subject cannot exceed 200 characters");
            }
        }

        // Message validation
        if (data.containsKey("message")) {
            String message = (String) data.get("message");
            if (message == null || message.trim().isEmpty()) {
                errors.put("message", "Message is required");
            } else if (message.trim().length() < 10) {
                errors.put("message", "Message must be at least 10 characters");
            } else if (message.length() > 2000) {
                errors.put("message", "Message cannot exceed 2000 characters");
            }
        }

        // Category validation
        if (data.containsKey("category")) {
            String category = (String) data.get("category");
            if (category == null || category.trim().isEmpty()) {
                errors.put("category", "Category is required");
            } else {
                boolean validCategory = false;
                for (String validCat : VALID_CATEGORIES) {
                    if (validCat.equals(category)) {
                        validCategory = true;
                        break;
                    }
                }
                if (!validCategory) {
                    errors.put("category", "Invalid category. Must be one of: " + String.join(", ", VALID_CATEGORIES));
                }
            }
        }

        // Rating validation (optional)
        if (data.containsKey("rating")) {
            Object ratingObj = data.get("rating");
            if (ratingObj != null) {
                try {
                    Integer rating = Integer.parseInt(ratingObj.toString());
                    if (rating < 1 || rating > 5) {
                        errors.put("rating", "Rating must be between 1 and 5");
                    }
                } catch (NumberFormatException e) {
                    errors.put("rating", "Rating must be a valid number between 1 and 5");
                }
            }
        }

        return errors;
    }

    @Override
    public String getValidationType() {
        return "FEEDBACK";
    }

    @Override
    public boolean canHandle(String dataType) {
        return "FEEDBACK".equalsIgnoreCase(dataType);
    }
}

