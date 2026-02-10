package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.Ticket;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.TicketRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PageController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public PageController(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // ----------------------------
    // Home Page
    // ----------------------------
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // ----------------------------
    // Login Page
    // ----------------------------
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ----------------------------
    // Generic Dashboard Redirect
    // ----------------------------
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (role.equals("ROLE_AGENT")) {
            return "redirect:/agent/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }

    // ----------------------------
    // Register Page (redirect to auth/register)
    // ----------------------------
    @GetMapping("/register")
    public String register() {
        return "redirect:/auth/register";
    }

    // ----------------------------
    // User Dashboard
    // ----------------------------
    @GetMapping("/user/dashboard")
    public String userDashboard() {
        return "user-dashboard";
    }

    // ----------------------------
    // User Tickets (Own)
    // ----------------------------
    @GetMapping("/user/tickets")
    public String userTickets(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Ticket> tickets = ticketRepository.findByUser(user);
        model.addAttribute("tickets", tickets);

        return "user-tickets";
    }

    // ----------------------------
    // Admin Dashboard
    // ----------------------------
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    // ----------------------------
    // Agent Dashboard
    // ----------------------------
    @GetMapping("/agent/dashboard")
    public String agentDashboard() {
        return "agent-dashboard";
    }

    // ----------------------------
    // Agent View Tickets
    // ----------------------------
    @Transactional(readOnly = true)
    @GetMapping("/agent/tickets")
    public String agentTickets(@RequestParam(required = false) String deleted,
                               @RequestParam(required = false) String error,
                               @RequestParam(required = false) String success,
                               @RequestParam(required = false) String updated,
                               Model model) {
        // Fetch tickets with user eagerly loaded (responses will be lazily loaded)
        List<Ticket> tickets = ticketRepository.findAllWithUser();

        // Initialize responses collection for each ticket to avoid
        // LazyInitializationException
        tickets.forEach(ticket -> {
            if (ticket.getResponses() != null) {
                ticket.getResponses().size(); // Force initialization
            }
        });

        model.addAttribute("tickets", tickets);

        // Add success/error messages
        if ("true".equals(deleted)) {
            model.addAttribute("message", "✅ Ticket deleted successfully!");
        } else if ("true".equals(error)) {
            model.addAttribute("message", "❌ Error processing request. Please try again.");
        } else if ("true".equals(success)) {
            model.addAttribute("message", "✅ Response sent successfully!");
        } else if ("true".equals(updated)) {
            model.addAttribute("message", "✅ Response updated successfully!");
        }

        return "agent-tickets";
    }

    // ----------------------------
    // Ticket Success Page
    // ----------------------------
    @GetMapping("/ticket-success")
    public String ticketSuccess(@RequestParam Long id, Model model) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket != null) {
            model.addAttribute("ticket", ticket);
            return "ticket-success";
        } else {
            return "error";
        }
    }
}
