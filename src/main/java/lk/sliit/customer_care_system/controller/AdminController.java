package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.ChatMessage;
import lk.sliit.customer_care_system.modelentity.ChatSession;
import lk.sliit.customer_care_system.modelentity.FAQ;
import lk.sliit.customer_care_system.modelentity.Ticket;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.ChatSessionRepository;
import lk.sliit.customer_care_system.repository.ChatMessageRepository;
import lk.sliit.customer_care_system.repository.FAQRepository;
import lk.sliit.customer_care_system.repository.TicketRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private FAQRepository faqRepository;

    @Autowired
    private lk.sliit.customer_care_system.repository.FeedbackRepository feedbackRepository;

    // Create new agent
    @PostMapping("/create-agent")
    public String createAgent(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              @RequestParam String phoneNumber,
                              @RequestParam String address,
                              Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "redirect:/admin/dashboard?error=PasswordsDoNotMatch";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters");
            return "redirect:/admin/dashboard?error=PasswordTooShort";
        }

        if (!password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            model.addAttribute("error",
                    "Password must contain at least one digit, one uppercase letter, one special character");
            return "redirect:/admin/dashboard?error=PasswordTooWeak";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "redirect:/admin/dashboard?error=UserExists";
        }

        User agent = new User();
        agent.setUsername(username);
        agent.setPassword(passwordEncoder.encode(password));
        agent.setRole("ROLE_AGENT");
        agent.setPhoneNumber(phoneNumber);
        agent.setAddress(address);

        userRepository.save(agent);

        return "redirect:/admin/dashboard?success=AgentCreated";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model, HttpServletRequest request) {
        List<User> allUsers = userRepository.findAll();

        // Separate users by role
        List<User> users = allUsers.stream()
                .filter(u -> "ROLE_USER".equals(u.getRole()))
                .collect(Collectors.toList());

        List<User> agents = allUsers.stream()
                .filter(u -> "ROLE_AGENT".equals(u.getRole()))
                .collect(Collectors.toList());

        List<User> admins = allUsers.stream()
                .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());

        model.addAttribute("users", users);
        model.addAttribute("agents", agents);
        model.addAttribute("admins", admins);

        // Add CSRF token to model for JavaScript access
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

        return "admin-users";
    }

    @DeleteMapping("/user/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            System.out.println("Delete user request received for ID: " + id);

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("User found: " + user.getUsername() + " with role: " + user.getRole());

            // Prevent deletion of admin accounts
            if ("ROLE_ADMIN".equals(user.getRole())) {
                System.out.println("Attempted to delete admin account, blocking request");
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Cannot delete admin accounts"));
            }

            // Delete related records first to avoid foreign key constraint violations

            // 1. Delete chat sessions where this user is the user
            System.out.println("Deleting chat sessions for user: " + user.getUsername());
            List<ChatSession> userSessions = chatSessionRepository.findByUserOrderByCreatedAtDesc(user);
            for (ChatSession session : userSessions) {
                // Delete messages in each session first using a custom query
                chatMessageRepository.deleteAll(
                        chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(session.getSessionId()));
                // Then delete the session
                chatSessionRepository.delete(session);
            }

            // 2. Delete any remaining chat messages sent by this user
            System.out.println("Deleting remaining chat messages for user: " + user.getUsername());
            // Use a more efficient approach - delete messages in batches
            List<ChatMessage> allMessages = chatMessageRepository.findAll();
            List<ChatMessage> userMessages = allMessages.stream()
                    .filter(msg -> msg.getSender() != null && msg.getSender().getId().equals(user.getId()))
                    .toList();
            if (!userMessages.isEmpty()) {
                chatMessageRepository.deleteAll(userMessages);
            }

            // 3. Update chat sessions where this user is the agent (set agent to null)
            System.out.println("Updating chat sessions where user is agent: " + user.getUsername());
            List<ChatSession> agentSessions = chatSessionRepository.findByAgentOrderByCreatedAtDesc(user);
            for (ChatSession session : agentSessions) {
                session.setAgent(null);
                chatSessionRepository.save(session);
            }

            // 4. Handle Feedbacks (set user to null/anonymous)
            System.out.println("Processing feedbacks for user: " + user.getUsername());
            List<lk.sliit.customer_care_system.modelentity.Feedback> userFeedbacks = feedbackRepository.findByUser(user);
            System.out.println("Found " + userFeedbacks.size() + " feedbacks");
            for (lk.sliit.customer_care_system.modelentity.Feedback feedback : userFeedbacks) {
                feedback.setUser(null);
                feedback.setIsAnonymous(true); // Mark as anonymous since user is gone
                feedbackRepository.save(feedback);
            }

            // 5. Handle FAQs created by user (set createdBy to null)
            System.out.println("Processing created FAQs for user: " + user.getUsername());
            List<FAQ> createdFaqs = faqRepository.findByCreatedBy(user);
            System.out.println("Found " + createdFaqs.size() + " created FAQs");
            for (FAQ faq : createdFaqs) {
                faq.setCreatedBy(null);
                faqRepository.save(faq);
            }

            // 6. Handle FAQs approved by user (set approvedBy to null)
            System.out.println("Processing approved FAQs for user: " + user.getUsername());
            List<FAQ> approvedFaqs = faqRepository.findByApprovedBy(user);
            System.out.println("Found " + approvedFaqs.size() + " approved FAQs");
            for (FAQ faq : approvedFaqs) {
                faq.setApprovedBy(null);
                faqRepository.save(faq);
            }

            // 7. Delete tickets (this should cascade automatically due to @OneToMany
            // cascade)
            System.out.println("Deleting tickets for user: " + user.getUsername());
            // Tickets should be deleted automatically due to cascade configuration

            // 8. Finally delete the user
            System.out.println("Deleting user: " + user.getUsername());
            userRepository.delete(user);
            System.out.println("User deleted successfully: " + user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User deleted successfully"));
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/user/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("phoneNumber", user.getPhoneNumber());
            userData.put("address", user.getAddress());
            userData.put("role", user.getRole());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", userData));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/user/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user details
            String phoneNumber = (String) requestBody.get("phoneNumber");
            String address = (String) requestBody.get("address");
            String password = (String) requestBody.get("password");

            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                user.setPhoneNumber(phoneNumber);
            }

            if (address != null && !address.trim().isEmpty()) {
                user.setAddress(address);
            }

            // Only update password if provided
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/analytics")
    public String getAnalytics(Model model) {
        // User Statistics
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.stream().filter(u -> "ROLE_USER".equals(u.getRole())).count();
        long totalAgents = allUsers.stream().filter(u -> "ROLE_AGENT".equals(u.getRole())).count();
        long totalAdmins = allUsers.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();

        // Ticket Statistics
        List<Ticket> allTickets = ticketRepository.findAll();
        long totalTickets = allTickets.size();
        long openTickets = allTickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProgressTickets = allTickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long resolvedTickets = allTickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
        long closedTickets = allTickets.stream().filter(t -> "CLOSED".equals(t.getStatus())).count();

        // Ticket Category Breakdown
        Map<String, Long> ticketsByCategory = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getCategory, Collectors.counting()));

        // Chat Session Statistics
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        long totalChatSessions = allSessions.size();
        long activeChatSessions = allSessions.stream()
                .filter(s -> s.getStatus() == ChatSession.ChatStatus.ACTIVE)
                .count();
        long waitingChatSessions = allSessions.stream()
                .filter(s -> s.getStatus() == ChatSession.ChatStatus.WAITING)
                .count();
        long closedChatSessions = allSessions.stream()
                .filter(s -> s.getStatus() == ChatSession.ChatStatus.CLOSED)
                .count();

        // Add all statistics to model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAgents", totalAgents);
        model.addAttribute("totalAdmins", totalAdmins);

        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("inProgressTickets", inProgressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("closedTickets", closedTickets);

        model.addAttribute("ticketsByCategory", ticketsByCategory);

        model.addAttribute("totalChatSessions", totalChatSessions);
        model.addAttribute("activeChatSessions", activeChatSessions);
        model.addAttribute("waitingChatSessions", waitingChatSessions);
        model.addAttribute("closedChatSessions", closedChatSessions);

        return "admin-analytics";
    }

    // Manage pending FAQ approvals
    @GetMapping("/faq/pending")
    public String managePendingFAQs(Model model) {
        List<FAQ> pendingFaqs = faqRepository.findByIsApprovedFalseOrderByCreatedAtDesc();
        model.addAttribute("pendingFaqs", pendingFaqs);
        return "admin-faq-approval";
    }

    // Approve FAQ
    @PostMapping("/faq/approve/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveFAQ(@PathVariable Long id) {
        try {
            System.out.println("Approve FAQ called for ID: " + id);

            FAQ faq = faqRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("FAQ not found"));

            System.out.println("FAQ found: " + faq.getQuestion());

            // Get current admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Current authenticated user: " + auth.getName());
            System.out.println("User authorities: " + auth.getAuthorities());

            // Find admin user - optional, approval can work without linking to admin
            User admin = userRepository.findByUsername(auth.getName()).orElse(null);

            if (admin != null) {
                System.out.println("Admin found: " + admin.getUsername() + " (ID: " + admin.getId() + ")");
                faq.setApprovedBy(admin);
            } else {
                System.out.println("Warning: User not found in database, but proceeding with approval");
                faq.setApprovedBy(null); // Approval without linking to user
            }

            faq.setIsApproved(true);
            faq.setApprovedAt(LocalDateTime.now());
            faqRepository.save(faq);

            System.out.println("FAQ approved successfully!");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FAQ approved successfully"));
        } catch (Exception e) {
            System.err.println("Error approving FAQ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Reject FAQ
    @DeleteMapping("/faq/reject/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectFAQ(@PathVariable Long id) {
        try {
            System.out.println("Reject FAQ called for ID: " + id);

            FAQ faq = faqRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("FAQ not found"));

            System.out.println("FAQ found, deleting: " + faq.getQuestion());

            faqRepository.delete(faq);

            System.out.println("FAQ deleted successfully!");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FAQ rejected and deleted"));
        } catch (Exception e) {
            System.err.println("Error rejecting FAQ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
