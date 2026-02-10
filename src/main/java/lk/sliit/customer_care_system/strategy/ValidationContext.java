package lk.sliit.customer_care_system.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class for managing validation strategies
 * Uses Strategy pattern to apply different validation rules
 */
@Component
public class ValidationContext {

    @Autowired
    private List<ValidationStrategy> validationStrategies;

    /**
     * Validate data using the specified strategy
     * @param strategy The validation strategy to use
     * @param data The data to validate
     * @return Map containing validation results (field -> error message)
     */
    public Map<String, String> validate(ValidationStrategy strategy, Map<String, Object> data) {
        return strategy.validate(data);
    }

    /**
     * Validate user data
     * @param data The user data to validate
     * @return Map containing validation results
     */
    public Map<String, String> validateUser(Map<String, Object> data) {
        ValidationStrategy strategy = getStrategyByType("USER");
        if (strategy != null) {
            return strategy.validate(data);
        }
        return new HashMap<>();
    }

    /**
     * Validate ticket data
     * @param data The ticket data to validate
     * @return Map containing validation results
     */
    public Map<String, String> validateTicket(Map<String, Object> data) {
        ValidationStrategy strategy = getStrategyByType("TICKET");
        if (strategy != null) {
            return strategy.validate(data);
        }
        return new HashMap<>();
    }

    /**
     * Validate feedback data
     * @param data The feedback data to validate
     * @return Map containing validation results
     */
    public Map<String, String> validateFeedback(Map<String, Object> data) {
        ValidationStrategy strategy = getStrategyByType("FEEDBACK");
        if (strategy != null) {
            return strategy.validate(data);
        }
        return new HashMap<>();
    }

    /**
     * Auto-detect and validate data based on content
     * @param data The data to validate
     * @return Map containing validation results
     */
    public Map<String, String> autoValidate(Map<String, Object> data) {
        Map<String, String> allErrors = new HashMap<>();

        for (ValidationStrategy strategy : validationStrategies) {
            if (strategy.canHandle(detectDataType(data))) {
                Map<String, String> errors = strategy.validate(data);
                allErrors.putAll(errors);
            }
        }

        return allErrors;
    }

    /**
     * Get validation strategy by type
     * @param type The validation type
     * @return ValidationStrategy instance or null if not found
     */
    private ValidationStrategy getStrategyByType(String type) {
        return validationStrategies.stream()
                .filter(strategy -> strategy.getValidationType().equals(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * Auto-detect data type based on content
     * @param data The data to analyze
     * @return Detected data type
     */
    private String detectDataType(Map<String, Object> data) {
        if (data.containsKey("username") || data.containsKey("phoneNumber")) {
            return "USER";
        } else if (data.containsKey("title") && data.containsKey("description")) {
            return "TICKET";
        } else if (data.containsKey("subject") && data.containsKey("message")) {
            return "FEEDBACK";
        }
        return "UNKNOWN";
    }
}

