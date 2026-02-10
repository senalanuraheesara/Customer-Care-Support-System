package lk.sliit.customer_care_system.strategy;

import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Agent role strategy implementation
 * Handles agent-specific behaviors and permissions
 */
@Component
public class AgentRoleStrategy implements UserRoleStrategy {

    @Autowired
    private lk.sliit.customer_care_system.repository.TicketRepository ticketRepository;

    @Autowired
    private lk.sliit.customer_care_system.repository.FeedbackRepository feedbackRepository;

    @Autowired
    private lk.sliit.customer_care_system.repository.FAQRepository faqRepository;

    @Override
    public String getRoleName() {
        return "ROLE_AGENT";
    }

    @Override
    public Map<String, Object> getDashboardData(User user) {
        Map<String, Object> dashboardData = new HashMap<>();

        // Agent-specific dashboard data
        dashboardData.put("assignedTickets", ticketRepository.count());
        dashboardData.put("totalTickets", ticketRepository.count());
        dashboardData.put("pendingFeedbacks", feedbackRepository.countByStatus("New"));
        dashboardData.put("pendingFAQs", faqRepository.findByIsApprovedFalseOrderByCreatedAtDesc().size());
        dashboardData.put("userRole", "Agent");
        dashboardData.put("canManageTickets", true);
        dashboardData.put("canManageFeedbacks", true);
        dashboardData.put("canCreateFAQs", true);

        return dashboardData;
    }

    @Override
    public List<String> getAccessibleMenus() {
        return Arrays.asList(
                "agent-dashboard",
                "agent-tickets",
                "agent-faq-form",
                "agent-faq-management",
                "agent-chat"
        );
    }

    @Override
    public boolean canPerformAction(String action) {
        // Agent can perform limited actions
        return Arrays.asList(
                "view_assigned_tickets", "update_ticket_status", "respond_to_ticket",
                "view_feedbacks", "respond_to_feedback",
                "create_faq", "edit_faq", "view_faqs",
                "chat_with_users"
        ).contains(action);
    }
}
