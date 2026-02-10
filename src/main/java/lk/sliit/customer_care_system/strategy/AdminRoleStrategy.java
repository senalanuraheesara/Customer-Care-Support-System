package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Admin role strategy implementation
 * Handles admin-specific behaviors and permissions
 */
@Component
public class AdminRoleStrategy implements UserRoleStrategy {

    @Autowired
    private lk.sliit.customer_care_system.repository.UserRepository userRepository;

    @Autowired
    private lk.sliit.customer_care_system.repository.TicketRepository ticketRepository;

    @Autowired
    private lk.sliit.customer_care_system.repository.FeedbackRepository feedbackRepository;

    @Autowired
    private lk.sliit.customer_care_system.repository.FAQRepository faqRepository;

    @Override
    public String getRoleName() {
        return "ROLE_ADMIN";
    }

    @Override
    public Map<String, Object> getDashboardData(User user) {
        Map<String, Object> dashboardData = new HashMap<>();

        // Admin-specific dashboard data
        dashboardData.put("totalUsers", userRepository.count());
        dashboardData.put("totalTickets", ticketRepository.count());
        dashboardData.put("totalFeedbacks", feedbackRepository.count());
        dashboardData.put("pendingFAQs", faqRepository.findByIsApprovedFalseOrderByCreatedAtDesc().size());
        dashboardData.put("userRole", "Admin");
        dashboardData.put("canManageUsers", true);
        dashboardData.put("canManageTickets", true);
        dashboardData.put("canManageFeedbacks", true);
        dashboardData.put("canApproveFAQs", true);

        return dashboardData;
    }

    @Override
    public List<String> getAccessibleMenus() {
        return Arrays.asList(
                "admin-dashboard",
                "admin-users",
                "admin-feedback-management",
                "admin-faq-approval",
                "admin-analytics"
        );
    }

    @Override
    public boolean canPerformAction(String action) {
        // Admin can perform all actions
        return Arrays.asList(
                "create_user", "edit_user", "delete_user",
                "view_all_tickets", "assign_ticket", "close_ticket",
                "view_all_feedbacks", "respond_to_feedback",
                "approve_faq", "reject_faq", "view_analytics"
        ).contains(action);
    }
}
