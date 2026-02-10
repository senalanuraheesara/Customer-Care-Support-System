package lk.sliit.customer_care_system.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * User validation strategy implementation
 * Handles validation for user-related data
 */
@Component
public class UserValidationStrategy implements ValidationStrategy {

    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10}");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,100}$");

    @Override
    public Map<String, String> validate(Map<String, Object> data) {
        Map<String, String> errors = new HashMap<>();

        // Username validation
        if (data.containsKey("username")) {
            String username = (String) data.get("username");
            if (username == null || username.trim().isEmpty()) {
                errors.put("username", "Username is required");
            } else if (!USERNAME_PATTERN.matcher(username).matches()) {
                errors.put("username",
                        "Username must be 3-100 characters and contain only letters, numbers, and underscores");
            }
        }

        // Phone number validation
        if (data.containsKey("phoneNumber")) {
            String phoneNumber = (String) data.get("phoneNumber");
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                errors.put("phoneNumber", "Phone number is required");
            } else if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                errors.put("phoneNumber", "Phone number must be exactly 10 digits");
            }
        }

        // Address validation
        if (data.containsKey("address")) {
            String address = (String) data.get("address");
            if (address == null || address.trim().isEmpty()) {
                errors.put("address", "Address is required");
            } else if (address.trim().length() < 3) {
                errors.put("address", "Address must be at least 3 characters");
            } else if (address.length() > 255) {
                errors.put("address", "Address cannot exceed 255 characters");
            }
        }

        // Password validation
        if (data.containsKey("password")) {
            String password = (String) data.get("password");
            if (password == null || password.isEmpty()) {
                errors.put("password", "Password is required");
            } else if (password.length() < 8) {
                errors.put("password", "Password must be at least 8 characters");
            } else if (!password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
                errors.put("password",
                        "Password must contain at least one digit, one uppercase letter, one special character (@#$%^&+=!), and be at least 8 characters long");
            }
        }

        return errors;
    }

    @Override
    public String getValidationType() {
        return "USER";
    }

    @Override
    public boolean canHandle(String dataType) {
        return "USER".equalsIgnoreCase(dataType) || "REGISTRATION".equalsIgnoreCase(dataType);
    }
}
