package lk.sliit.customer_care_system.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Ticket validation strategy implementation
 * Handles validation for ticket-related data
 */
@Component
public class TicketValidationStrategy implements ValidationStrategy {

    private static final String[] VALID_CATEGORIES = {
            "Technical", "Billing", "Account", "General", "Other"
    };

    @Override
    public Map<String, String> validate(Map<String, Object> data) {
        Map<String, String> errors = new HashMap<>();

        // Title validation
        if (data.containsKey("title")) {
            String title = (String) data.get("title");
            if (title == null || title.trim().isEmpty()) {
                errors.put("title", "Title is required");
            } else if (title.trim().length() < 5) {
                errors.put("title", "Title must be at least 5 characters");
            } else if (title.length() > 150) {
                errors.put("title", "Title cannot exceed 150 characters");
            }
        }

        // Description validation
        if (data.containsKey("description")) {
            String description = (String) data.get("description");
            if (description == null || description.trim().isEmpty()) {
                errors.put("description", "Description is required");
            } else if (description.trim().length() < 10) {
                errors.put("description", "Description must be at least 10 characters");
            } else if (description.length() > 2000) {
                errors.put("description", "Description cannot exceed 2000 characters");
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

        return errors;
    }

    @Override
    public String getValidationType() {
        return "TICKET";
    }

    @Override
    public boolean canHandle(String dataType) {
        return "TICKET".equalsIgnoreCase(dataType);
    }
}

