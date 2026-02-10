package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Context class for managing user role strategies
 * Uses Strategy pattern to handle different user role behaviors
 */
@Component
public class UserRoleContext {

    @Autowired
    private List<UserRoleStrategy> roleStrategies;

    /**
     * Get dashboard data for a user based on their role
     * @param user The user requesting dashboard data
     * @return Dashboard data as a map
     */
    public Map<String, Object> getDashboardData(User user) {
        UserRoleStrategy strategy = getStrategyByRole(user.getRole());
        if (strategy != null) {
            return strategy.getDashboardData(user);
        }
        return Map.of("error", "Unknown user role: " + user.getRole());
    }

    /**
     * Get accessible menus for a user based on their role
     * @param user The user requesting menu data
     * @return List of accessible menu items
     */
    public List<String> getAccessibleMenus(User user) {
        UserRoleStrategy strategy = getStrategyByRole(user.getRole());
        if (strategy != null) {
            return strategy.getAccessibleMenus();
        }
        return List.of();
    }

    /**
     * Check if user can perform a specific action
     * @param user The user to check
     * @param action The action to check
     * @return true if user can perform the action, false otherwise
     */
    public boolean canPerformAction(User user, String action) {
        UserRoleStrategy strategy = getStrategyByRole(user.getRole());
        if (strategy != null) {
            return strategy.canPerformAction(action);
        }
        return false;
    }

    /**
     * Get role-specific strategy
     * @param role The user role
     * @return UserRoleStrategy instance or null if not found
     */
    private UserRoleStrategy getStrategyByRole(String role) {
        return roleStrategies.stream()
                .filter(strategy -> strategy.getRoleName().equals(role))
                .findFirst()
                .orElse(null);
    }
}

