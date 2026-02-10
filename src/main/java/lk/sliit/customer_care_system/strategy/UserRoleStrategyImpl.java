package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Regular user role strategy implementation
 * Handles user-specific behaviors and permissions
 */
@Component
public class UserRoleStrategyImpl implements lk.sliit.customer_care_system.strategy.UserRoleStrategy {

    @Autowired
    private lk.sliit.customer_care_system.repository.TicketRepository ticketRepository;

    @Autowired
    private lk.sliit.customer_care_system.repository.FeedbackRepository feedbackRepository;

    @Override
    public String getRoleName() {
        return "ROLE_USER";
    }

    @Override
    public Map<String, Object> getDashboardData(User user) {
        Map<String, Object> dashboardData = new HashMap<>();

        // User-specific dashboard data
        dashboardData.put("myTickets", ticketRepository.findByUser(user).size());
        dashboardData.put("openTickets", ticketRepository.findByUser(user).stream()
                .filter(ticket -> "Open".equals(ticket.getStatus())).count());
        dashboardData.put("myFeedbacks", feedbackRepository.findByUser(user).size());
        dashboardData.put("userRole", "User");
        dashboardData.put("canCreateTickets", true);
        dashboardData.put("canCreateFeedbacks", true);
        dashboardData.put("canViewFAQs", true);

        return dashboardData;
    }

    @Override
    public List<String> getAccessibleMenus() {
        return Arrays.asList(
                "user-dashboard",
                "submit-ticket",
                "my-tickets",
                "submit-feedback",
                "my-feedback",
                "faq-view"
        );
    }

    @Override
    public boolean canPerformAction(String action) {
        // Regular user can perform basic actions
        return Arrays.asList(
                "create_ticket", "view_my_tickets", "edit_my_ticket", "delete_my_ticket",
                "create_feedback", "view_my_feedbacks", "edit_my_feedback", "delete_my_feedback",
                "view_faqs", "chat_with_agent"
        ).contains(action);
    }
}
