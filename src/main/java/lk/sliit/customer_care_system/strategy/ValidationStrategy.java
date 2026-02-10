package lk.sliit.customer_care_system.strategy;

import java.util.Map;

/**
 * Strategy interface for different validation rules
 * This allows the system to apply different validation logic based on context
 */
public interface ValidationStrategy {

    /**
     * Validate the given data
     * @param data The data to validate
     * @return Map containing validation results (field -> error message)
     */
    Map<String, String> validate(Map<String, Object> data);

    /**
     * Get the validation type name
     * @return String representation of the validation type
     */
    String getValidationType();

    /**
     * Check if this strategy can handle the given data type
     * @param dataType The type of data to validate
     * @return true if this strategy can handle the data type
     */
    boolean canHandle(String dataType);
}

