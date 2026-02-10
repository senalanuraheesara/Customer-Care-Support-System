package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import java.util.List;

/**
 * Strategy interface for different user role behaviors
 * This allows the system to handle different user types with specific logic
 */
public interface UserRoleStrategy {

    /**
     * Get the role name this strategy handles
     * @return String representation of the role
     */
    String getRoleName();

    /**
     * Get dashboard data for this user role
     * @param user The user requesting dashboard data
     * @return Dashboard data as a map
     */
    java.util.Map<String, Object> getDashboardData(User user);

    /**
     * Get accessible menu items for this role
     * @return List of menu items this role can access
     */
    List<String> getAccessibleMenus();

    /**
     * Check if user can perform a specific action
     * @param action The action to check
     * @return true if user can perform the action, false otherwise
     */
    boolean canPerformAction(String action);
}

